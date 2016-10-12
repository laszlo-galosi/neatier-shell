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

package com.neatier.shell.data.network.di;

import android.content.Context;
import android.support.annotation.NonNull;
import com.neatier.commons.helpers.JsonSerializer;
import com.neatier.data.network.ChangeableBaseUrl;
import com.neatier.shell.data.network.RestApi;
import com.neatier.shell.data.network.retrofit.RetrofitRestApi;
import com.neatier.shell.data.network.retrofit.ServiceFactory;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class RestApiModule {

    @NonNull
    private final ChangeableBaseUrl changeableBaseUrl;

    public RestApiModule(@NonNull String baseUrl) {
        changeableBaseUrl = new ChangeableBaseUrl(baseUrl);
    }

    @Provides @NonNull RestApi provideRestApi(
          final Context context,
          final OkHttpClient okHttpClient,
          @NonNull ChangeableBaseUrl changeableBaseUrl, JsonSerializer jsonSerializer) {
        ServiceFactory serviceFactory = new ServiceFactory(changeableBaseUrl, okHttpClient,
                                                           jsonSerializer);
        return new RetrofitRestApi(context, serviceFactory, changeableBaseUrl, okHttpClient);
    }

    @Provides @NonNull
    public ChangeableBaseUrl provideChangeableBaseUrl() {
        return changeableBaseUrl;
    }
}
