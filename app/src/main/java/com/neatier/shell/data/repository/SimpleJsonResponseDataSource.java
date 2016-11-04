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

import com.google.gson.JsonElement;
import com.neatier.commons.helpers.JsonSerializer;
import com.neatier.commons.helpers.KeyValuePairs;
import com.neatier.shell.data.network.ApiSettings;
import com.neatier.shell.data.network.RestApi;
import javax.inject.Inject;
import rx.Observable;
import trikita.log.Log;

public class SimpleJsonResponseDataSource
      implements DataSources.SimpleApiResponseDataSource {

    private final RestApi restApiClient;
    private JsonSerializer serializer;

    @Inject
    public SimpleJsonResponseDataSource(RestApi restApiClient,
          final JsonSerializer jsonSerializer) {
        this.restApiClient = restApiClient;
        serializer = jsonSerializer;
    }

    @Override
    public Observable<JsonElement> getSimpleJsonResponse(
          final KeyValuePairs<String, Object> requestParams) {
        Log.d("getSimpleJsonResponse").v(requestParams);
        Observable<JsonElement> responseObservable =
              restApiClient.listGames(requestParams, ApiSettings.GAMES_PARAMS);
        return responseObservable.flatMap(respJson -> Observable.just(respJson));
    }
}
