package com.neatier.shell.data.network.di;

import android.support.annotation.NonNull;
import com.neatier.data.network.retrofit.OkHttpInterceptors;
import com.neatier.data.network.retrofit.OkHttpNetworkInterceptors;
import com.neatier.shell.factorysettings.developer.DeveloperSettings;
import dagger.Module;
import dagger.Provides;
import java.util.Collections;
import java.util.List;
import javax.inject.Singleton;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;
import trikita.log.Log;

/**
 * Created by László Gálosi on 12/10/16
 */
@Module
public class HttpInterceptorModule {
    @Provides @Singleton @NonNull
    public HttpLoggingInterceptor provideHttpLoggingInterceptor(
          final DeveloperSettings developerSettings) {
        String loggingSettings = developerSettings.getHttpLoggingLevel();
        return new HttpLoggingInterceptor(message -> Log.i("", message))
              .setLevel(HttpLoggingInterceptor.Level.valueOf(loggingSettings));
    }

    @Provides @OkHttpInterceptors @Singleton @NonNull
    public List<Interceptor> provideOkHttpInterceptors(
          @NonNull HttpLoggingInterceptor httpLoggingInterceptor) {
        return Collections.singletonList(httpLoggingInterceptor);
    }

    @Provides @OkHttpNetworkInterceptors @Singleton @NonNull
    public List<Interceptor> provideOkHttpNetworkInterceptors() {
        //return Collections.singletonList(new StethoInterceptor());
        //Todo: setup interceptors for caching directives.
        return Collections.emptyList();
    }
}
