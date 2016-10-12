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

import com.google.gson.GsonBuilder;
import com.neatier.commons.helpers.JsonSerializer;
import com.neatier.data.entity.AutoValueAdapterFactory;
import com.neatier.data.network.ChangeableBaseUrl;
import com.neatier.shell.BuildConfig;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Singleton
public class ServiceFactory {

    protected final ChangeableBaseUrl serverEndPoint;
    protected final OkHttpClient okHttpClient;
    protected final JsonSerializer jsonSerializer;

    public ServiceFactory(final ChangeableBaseUrl serverEndPoint, final OkHttpClient httpClient,
          final JsonSerializer jsonSerializer) {
        this.serverEndPoint = serverEndPoint;
        this.okHttpClient = httpClient;
        this.jsonSerializer = jsonSerializer;
    }

    /**
     * Creates a retrofit service from the specified entity class.
     *
     * @return retrofit service with defined endpoint
     */
    public <S> S create() {
        GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(
              new GsonBuilder()
                    .registerTypeAdapterFactory(new AutoValueAdapterFactory())
                    .create());
        Retrofit retrofit = new Retrofit.Builder()
              .baseUrl(serverEndPoint.getUrl())
              .client(okHttpClient)
              .addConverterFactory(gsonConverterFactory)
              .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
              // Fail early: check Retrofit configuration at creation
              // time in Debug build.
              .validateEagerly(BuildConfig.DEBUG)
              .build();
        S service = (S) retrofit.create(NeatierShellApiService.class);
        return service;
    }

    public JsonSerializer getSerializer() {
        return jsonSerializer;
    }
}
