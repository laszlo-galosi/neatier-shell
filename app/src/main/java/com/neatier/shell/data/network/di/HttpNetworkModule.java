package com.neatier.shell.data.network.di;

import android.content.Context;
import android.support.annotation.NonNull;
import com.neatier.data.network.retrofit.OkHttpInterceptors;
import com.neatier.data.network.retrofit.OkHttpNetworkInterceptors;
import com.neatier.shell.data.network.ApiSettings;
import dagger.Module;
import dagger.Provides;
import java.io.File;
import java.util.List;
import javax.inject.Singleton;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * Created by László Gálosi on 12/10/16
 */
@Module
public class HttpNetworkModule {
    @Provides @NonNull @Singleton
    public OkHttpClient provideOkHttpClient(Context context,
          @OkHttpInterceptors @NonNull List<Interceptor> interceptors,
          @OkHttpNetworkInterceptors @NonNull List<Interceptor> networkInterceptors) {

        File cacheDir = new File(context.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, ApiSettings.DISK_CACHE_SIZE);
        final OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
              .cache(cache)
              .connectTimeout(ApiSettings.CONNECTION_TIMEOUT, ApiSettings.TIMEOUT_UNIT)
              .readTimeout(ApiSettings.READ_TIMEOUT, ApiSettings.TIMEOUT_UNIT);
        for (Interceptor interceptor : interceptors) {
            okHttpBuilder.addInterceptor(interceptor);
        }
        for (Interceptor networkInterceptor : networkInterceptors) {
            okHttpBuilder.addNetworkInterceptor(networkInterceptor);
        }
        return okHttpBuilder.build();
    }
}
