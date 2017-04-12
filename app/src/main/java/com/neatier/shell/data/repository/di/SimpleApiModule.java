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

package com.neatier.shell.data.repository.di;

import android.support.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.neatier.commons.helpers.JsonSerializer;
import com.neatier.data.entity.AutoValueAdapterFactory;
import com.neatier.shell.data.network.RestApi;
import com.neatier.shell.data.repository.DataSources;
import com.neatier.shell.data.repository.SimpleJsonResponseApiDataSource;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;

@Module
public class SimpleApiModule {

    public SimpleApiModule() {
    }

    @Provides @NonNull @Named("simpleApiGson")
    public Gson provideGson() {
        return new GsonBuilder()
              .registerTypeAdapterFactory(new AutoValueAdapterFactory())
              .create();
    }

    @Provides @NonNull @Named("simpleApiSerializer")
    public JsonSerializer provideJsonSerializer(final @Named("simpleApiGson") Gson gson) {
        return new JsonSerializer(gson, new JsonParser());
    }

    @Provides @NonNull
    public DataSources.SimpleJsonResponseDataSource provideSimnpleApiDataSource(RestApi restApi,
          @Named("simpleApiSerializer") JsonSerializer serializer) {
        return new SimpleJsonResponseApiDataSource(restApi, serializer);
    }
}

