/*
 * Copyright (C) 2017 Extremenet Ltd., All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  Proprietary and confidential.
 *  All information contained herein is, and remains the property of Extremenet Ltd.
 *  The intellectual and technical concepts contained herein are proprietary to Extremenet Ltd.
 *   and may be covered by U.S. and Foreign Patents, pending patents, and are protected
 *  by trade secret or copyright law. Dissemination of this information or reproduction of
 *  this material is strictly forbidden unless prior written permission is obtained from
 *   Extremenet Ltd.
 *
 */

package com.neatier.shell.data.repository;

import com.neatier.commons.exception.ErrorBundleException;
import com.neatier.commons.helpers.KeyValuePairs;
import com.neatier.commons.helpers.Preconditions;
import com.neatier.shell.data.network.ApiSettings;
import com.neatier.shell.data.network.RestApi;
import javax.inject.Inject;
import rx.Observable;
import trikita.log.Log;

public class SimpleStringResponseApiDataSource
      implements DataSources.SimpleStringResponseDataSource {

    private final RestApi restApiClient;

    @Inject
    public SimpleStringResponseApiDataSource(RestApi restApiClient) {
        this.restApiClient = restApiClient;
    }

    @Override
    public Observable getSimpleStringResponse(
          final KeyValuePairs<String, Object> requestParams) {
        Log.d("getSimpleJsonResponse").v(requestParams);
        Observable<String> responseObservable = Observable.error(new ErrorBundleException(
              String.format("Unsupported API call: getSimpleJsonResponse:%s", requestParams)));
        //Todo: implement api call logic by override responseObservable
        final String action = (String) requestParams.get(ApiSettings.KEY_API_ACTION);
        Preconditions.checkNotNull(action, "action cannot be null.");
      /*  switch (action) {
            case ApiSettings.ACTION_GET_STATIC_CONTENT:
                responseObservable = restApiClient.getStaticContent(requestParams);
                break;
        }*/
        return responseObservable;
    }
}
