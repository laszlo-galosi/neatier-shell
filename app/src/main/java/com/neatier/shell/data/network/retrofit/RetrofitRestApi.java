/*
 *  Copyright (C) 2016 Delight Solutions Ltd., All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited.
 *  Proprietary and confidential.
 *
 *  All information contained herein is, and remains the property of Delight Solutions Kft.
 *  The intellectual and technical concepts contained herein are proprietary to Delight Solutions
  *  Kft.
 *   and may be covered by U.S. and Foreign Patents, pending patents, and are protected
 *  by trade secret or copyright law. Dissemination of this information or reproduction of
 *  this material is strictly forbidden unless prior written permission is obtained from
 *   Delight Solutions Kft.
 */

package com.neatier.shell.data.network.retrofit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neatier.commons.exception.ErrorBundleException;
import com.neatier.commons.exception.InternalErrorException;
import com.neatier.commons.exception.NetworkConnectionException;
import com.neatier.commons.exception.NullArgumentException;
import com.neatier.commons.exception.RestApiResponseException;
import com.neatier.commons.exception.RestApiResponseException.ErrorKind;
import com.neatier.commons.helpers.BundleWrapper;
import com.neatier.commons.helpers.JsonSerializer;
import com.neatier.commons.helpers.KeyValuePairs;
import com.neatier.commons.helpers.Preconditions;
import com.neatier.data.network.ChangeableBaseUrl;
import com.neatier.shell.data.network.ApiSettings;
import com.neatier.shell.data.network.RestApi;
import com.neatier.shell.internal.di.PerActivity;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.adapter.rxjava.Result;
import rx.Observable;
import rx.functions.Func1;
import trikita.log.Log;

import static com.neatier.commons.exception.RestApiResponseException.RESP_BODY;
import static com.neatier.commons.exception.RestApiResponseException.RESP_KIND;
import static com.neatier.commons.exception.RestApiResponseException.RESP_REASON;
import static com.neatier.commons.exception.RestApiResponseException.RESP_STATUS;
import static com.neatier.commons.exception.RestApiResponseException.RESP_URL_FROM;

/**
 * Created by László Gálosi on 27/07/15
 */
@PerActivity
public class RetrofitRestApi implements RestApi {

    private final Context mContext;
    private final OkHttpClient mOkHttpClient;
    private final ChangeableBaseUrl mServerEndpoint;
    private ServiceFactory mServiceFactory;
    private JsonSerializer mSerializer;

    /**
     * Constructor of a {@link RestApi} implementation
     *
     * @param context the application context
     * @param serviceFactory {@link ServiceFactory} singleton.
     * @param endPoint the server end point
     * @param okHttpClient the http client implementation.
     */
    @Inject public RetrofitRestApi(@NonNull Context context, @NonNull ServiceFactory serviceFactory,
          @NonNull ChangeableBaseUrl endPoint, @NonNull OkHttpClient okHttpClient) {
        Preconditions.checkNotNull(context, "Context can't be null!");
        Preconditions.checkNotNull(serviceFactory, "ServiceFactory can't be null!");
        Preconditions.checkNotNull(endPoint, "endPoint can't be null!");
        Preconditions.checkNotNull(okHttpClient, "okHttpClient can't be null!");
        this.mContext = context;
        this.mServerEndpoint = endPoint;
        this.mOkHttpClient = okHttpClient;
        this.mServiceFactory = serviceFactory;
        mSerializer = serviceFactory.getSerializer();
    }

    @Override
    public Observable<JsonElement> listGames(final KeyValuePairs<String, Object> requestParams,
          List<String> optionalParamNames) {
        final String xUid = (String) requestParams.get(ApiSettings.KEY_API_XUID);
        String lang =
              (String) requestParams.getOrDefault(ApiSettings.KEY_API_LANG,
                                                  Locale.getDefault().toString());
        final String listType = (String) requestParams.getOrDefault(ApiSettings.KEY_API_LIST_TYPE,
                                                                    ApiSettings
                                                                          .LIST_LATEST_XBOXONE);
        Map<String, Object> queryMap = new HashMap<>(optionalParamNames.size());
        Observable.from(optionalParamNames)
                  .filter(key -> requestParams.containsKey(key))
                  .subscribe(key -> queryMap.put(key, requestParams.get(key)));
        try {
            if (isThereInternetConnection()) {
                NeatierShellApiService service = mServiceFactory.create();
                Observable<Result<JsonElement>> resultObservable = Observable.empty();
                if (xUid == null) {
                    resultObservable =
                          service.listTitles(getXAuthHeader(requestParams), lang, listType,
                                             queryMap);
                } else {
                    resultObservable =
                          service.listUserTitles(getXAuthHeader(requestParams), lang, xUid,
                                                 listType,
                                                 queryMap);
                }
                return resultObservable.compose(transformResult())
                                       .flatMap(responseObject -> checkErrorResponse(
                                             responseObject.getAsJsonObject()
                                       ));
            }
            return Observable.error(createRestApiResponseException(Result.error(
                  new NetworkConnectionException("No internet connection detected"))));
        } catch (ErrorBundleException e) {
            return Observable.error(createRestApiResponseException(Result.error(e)));
        }
    }

    private String getXAuthHeader(final KeyValuePairs<String, Object> requestParams)
          throws ErrorBundleException {
        String xAuth = (String) requestParams.getOrThrows(
              ApiSettings.KEY_API_XAUTH,
              new InternalErrorException(new NullArgumentException(ApiSettings.KEY_API_XAUTH)));
        return xAuth;
    }

    private static BundleWrapper checkParams(final KeyValuePairs<String, Object> params,
          final String... requiredParams) throws ErrorBundleException {
        int len = requiredParams.length;
        BundleWrapper checkedParams = BundleWrapper.wrap(new Bundle());
        for (int i = 0; i < len; i++) {
            ErrorBundleException t =
                  new InternalErrorException("No request parameter found:" + requiredParams[i]);
            checkedParams.put(requiredParams[i], params.getOrThrows(requiredParams[i], t));
        }
        return checkedParams;
    }

    private Func1<Response, Observable<Boolean>> response() {
        return response -> response.isSuccessful()
                           ? Observable.just(Boolean.TRUE)
                           : Observable.error(new RestApiResponseException(responseInfo(response)));
    }

    private KeyValuePairs<String, Object> responseInfo(final Response response) {
        KeyValuePairs<String, Object> respInfo =
              new KeyValuePairs<>().put(RESP_STATUS, response.code())
                                   .put(RESP_REASON, response.message());

        try {
            respInfo.put(RESP_BODY, response.errorBody().string());
        } catch (IOException e) {
            Log.e("Error creating responseInfo", e);
        }
        return respInfo;
    }

    /**
     * Transforms the call {@link Result<R>} to a {@link Response<R>} with error handling, which
     * in case of Network or Server error retries the call with a specified number of times.
     * or returns the result.
     */
    public <R> Observable.Transformer<Result<R>, R> transformResult() {
        return observable -> observable.flatMap(
              result -> {
                  Response<R> response = result.response();
                  if (response != null && response.isSuccessful()) {
                      //Log.d("Response body", response.body());
                      return Observable.just(response.body());
                  } else {
                      return Observable.error(createRestApiResponseException(result));
                  }
              }
        )/*.retryWhen(
              new RetryWithDelayCondition(3, 1000, t -> shouldRetry(t))
        )*/;
    }

    public static boolean shouldRetry(final Throwable throwable) {
        if (throwable instanceof RestApiResponseException) {
            return ((RestApiResponseException) throwable).shouldRetry();
        }
        return false;
    }

    @NonNull private Observable<JsonObject> checkErrorResponse(final JsonObject jsonRespObj) {
        String errorMessage =
              mSerializer.getAsChecked(ApiSettings.PROP_ERROR_MESSAGE, jsonRespObj, String.class);
        if (TextUtils.isEmpty(errorMessage)) {
            return Observable.just(jsonRespObj);
        }
        int errorCode =
              mSerializer.getAsChecked(ApiSettings.PROP_ERROR_CODE, jsonRespObj, Integer.class);
        KeyValuePairs<String, Object> errorInfo = new KeyValuePairs<>()
              .put(RESP_STATUS, errorCode)
              .put(RESP_KIND, ErrorKind.REQUEST)
              .put(RESP_REASON, errorMessage)
              .put(RESP_URL_FROM, mServerEndpoint.getUrl().toString())
              .put(RestApiResponseException.RESP_BODY, jsonRespObj);
        return Observable.error(new RestApiResponseException(errorInfo));
    }

    /**
     * @param result the {@link Result} Retrofit call result of type T.
     * @param <T> the Result parameter type
     * @return a {@link RestApiResponseException} from the specified {@link Result} object of type
     * T.
     */
    @NonNull private <T> RestApiResponseException createRestApiResponseException(
          final Result<T> result) {
        KeyValuePairs<String, Object> responseInfo = new KeyValuePairs<>();
        ErrorKind errorKind;
        Throwable throwable = result.error();
        if (result.isError()) {
            Log.e("Retrofit connection error.", throwable);
            if (throwable instanceof IOException
                  || throwable instanceof NetworkConnectionException) {
                errorKind = ErrorKind.find(ErrorKind.NETWORK).get();
            } else {
                errorKind = ErrorKind.find(ErrorKind.UNEXPECTED).get();
            }
            responseInfo.put(RESP_STATUS, new Integer(500))
                        .put(RESP_KIND, errorKind.id)
                        .put(RESP_URL_FROM, mServerEndpoint.getUrl().toString())
                        .put(RESP_REASON, errorKind.description);
        } else {
            Response<T> response = result.response();
            int code = response.code();
            errorKind = ErrorKind.getByCode(code);
            try {
                responseInfo.put(RESP_BODY, response.errorBody().string());
            } catch (IOException e) {
                Log.e("Error reading errorBody from response", e);
            }
            responseInfo.put(RESP_STATUS, code)
                        .put(RESP_REASON, response.message())
                        .put(RESP_URL_FROM, mServerEndpoint.getUrl().toString())
                        .put(RESP_KIND, errorKind.id);
        }
        return new RestApiResponseException(responseInfo, throwable);
    }

    /**
     * Checks if the device has any active internet connection.
     *
     * @return true device with internet connection, otherwise false.
     */
    public boolean isThereInternetConnection() {
        boolean isConnected;
        ConnectivityManager connectivityManager =
              (ConnectivityManager) this.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = (networkInfo != null && networkInfo.isConnectedOrConnecting());
        return isConnected;
    }
}
