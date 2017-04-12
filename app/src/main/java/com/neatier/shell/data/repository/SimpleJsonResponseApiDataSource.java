/*
 * Copyright (C) 2015 Karumi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.neatier.shell.data.repository;

import android.text.TextUtils;
import com.google.gson.JsonElement;
import com.neatier.commons.exception.ErrorBundleException;
import com.neatier.commons.exception.RestApiResponseException;
import com.neatier.commons.exception.RestApiResponseException.ErrorKind;
import com.neatier.commons.helpers.JsonSerializer;
import com.neatier.commons.helpers.KeyValuePairs;
import com.neatier.commons.helpers.Preconditions;
import com.neatier.shell.data.network.ApiSettings;
import com.neatier.shell.data.network.RestApi;
import javax.inject.Inject;
import rx.Observable;
import trikita.log.Log;

public class SimpleJsonResponseApiDataSource
      implements DataSources.SimpleJsonResponseDataSource {

    private final RestApi restApiClient;
    private JsonSerializer serializer;

    @Inject
    public SimpleJsonResponseApiDataSource(RestApi restApiClient,
          final JsonSerializer jsonSerializer) {
        this.restApiClient = restApiClient;
        serializer = jsonSerializer;
    }

    @Override
    public Observable getSimpleJsonResponse(
          final KeyValuePairs<String, Object> requestParams) {
        Log.d("getSimpleJsonResponse").v(requestParams);
        Observable<JsonElement> responseObservable = Observable.error(new ErrorBundleException(
              String.format("Unsupported API call: getSimpleJsonResponse:%s", requestParams)));
        //Todo: implement api call logic by override responseObservable
        final String action = (String) requestParams.get(ApiSettings.KEY_API_ACTION);
        Preconditions.checkNotNull(action, "action cannot be null.");
        ErrorKind errorKind = ErrorKind.find(ErrorKind.REQUEST).get();
        switch (action) {
            case ApiSettings.ACTION_LIST_GAMES:
                responseObservable =
                      restApiClient.listGames(requestParams, ApiSettings.GAMES_PARAMS);
                break;
        }
        final ErrorKind finalErrorKind = errorKind;
        return responseObservable.flatMap(respJson -> {
            //Error message handling if there is any errorMessage in the response.
            String errorMessage = serializer.getAsChecked(ApiSettings.PROP_ERROR_MESSAGE,
                                                          respJson.getAsJsonObject(), String.class);
            if (!TextUtils.isEmpty(errorMessage)) {
                KeyValuePairs<String, Object> errorInfo = new KeyValuePairs<>();

                errorInfo.put(RestApiResponseException.RESP_STATUS, new Integer(499))
                         .put(RestApiResponseException.RESP_KIND, finalErrorKind.id)
                         .put(RestApiResponseException.RESP_REASON, finalErrorKind.description)
                         .put(RestApiResponseException.RESP_BODY, respJson);
                return Observable.error(new RestApiResponseException(errorInfo));
            }
            return Observable.just(respJson);
        });
    }
}
