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

package com.neatier.shell.internal.di;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.neatier.commons.helpers.JsonSerializer;
import com.neatier.commons.helpers.SharedKeyValueStore;
import com.neatier.data.entity.AutoValueAdapterFactory;
import com.neatier.data.network.ChangeableBaseUrl;
import com.neatier.shell.appframework.Navigator;
import com.neatier.shell.appframework.helpers.DialogMaker;
import com.neatier.shell.data.network.RestApi;
import com.neatier.shell.data.network.retrofit.RetrofitRestApi;
import com.neatier.shell.data.network.retrofit.ServiceFactory;
import com.neatier.shell.data.repository.SimpleJsonResponseDataSource;
import com.neatier.shell.factorysettings.AppSettings;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;

@Module
public class ApplicationModule {
    @NonNull
    private final Application application;

    public ApplicationModule(@NonNull Application application) {
        this.application = application;
    }

    @Provides
    @NonNull
    @Singleton
    public Application provideNeatierApplication() {
        return application;
    }

    @Provides
    @Singleton
    protected Navigator provideNavigator() {
        return Navigator.getInstance();
    }

    @Provides
    @Singleton
    protected DialogMaker provideDialogMaker() {
        return DialogMaker.getInstance();
    }

    @Provides @Singleton
    public Context provideContext() {
        return application;
    }

    @Provides @NonNull @Singleton
    public Gson provideGson() {
        return new GsonBuilder()
              .setLenient()
              .registerTypeAdapterFactory(new AutoValueAdapterFactory())
              .create();
    }

    @Provides @NonNull @Singleton
    public JsonSerializer provideJsonSerializer(final Gson gson) {
        return new JsonSerializer(gson, new JsonParser());
    }

    @Provides
    @Singleton
    protected SharedKeyValueStore<String, Object> provideSettingsStore(Context context) {
        return new SharedKeyValueStore<>(context, AppSettings.PREF_DEFAULT_STORAGE_FILE);
    }

    @Provides @NonNull @Singleton RestApi provideRestApi(
          final Context context,
          final OkHttpClient okHttpClient,
          @NonNull ChangeableBaseUrl changeableBaseUrl, JsonSerializer jsonSerializer) {
        ServiceFactory serviceFactory = new ServiceFactory(changeableBaseUrl, okHttpClient,
                                                           jsonSerializer);
        return new RetrofitRestApi(context, serviceFactory, changeableBaseUrl, okHttpClient);
    }

    @Provides @Singleton @NonNull
    public SimpleJsonResponseDataSource provideSimpleApiDataSource(RestApi restApi,
          JsonSerializer serializer) {
        return new SimpleJsonResponseDataSource(restApi, serializer);
    }
}
