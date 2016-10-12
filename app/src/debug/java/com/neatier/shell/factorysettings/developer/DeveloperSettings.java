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

package com.neatier.shell.factorysettings.developer;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import com.neatier.commons.helpers.SharedKeyValueStore;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Wrapper over {@link SharedPreferences} to store developer settings.
 */
public class DeveloperSettings {

    private static final String KEY_IS_STETHO_ENABLED = "is_stetho_enabled";
    private static final String KEY_IS_LEAK_CANARY_ENABLED = "is_leak_canary_enabled";
    private static final String KEY_PICASSO_INDICATOR_ENABLED = "use_picasso_indicator";
    private static final String KEY_IS_CRASHLYTICS_ENABLED = "is_crashlytics_enabled";
    private static final String KEY_ENABLE_AUTO_FILL_VALUES = "enable_autofill_test_values";
    private static final String KEY_PICASSO_ENABLE_LOGGING = "enable_picasso_logging";
    private static final String KEY_HTTP_LOGGING_LEVEL = "key_http_logging_level";
    private static final String KEY_ENABLE_CRASH_ON_RXLOG = "key_enable_crash_on_rxlog";


    @NonNull
    private final SharedKeyValueStore<String, Object> mSharedKeyValueStore;

    public DeveloperSettings(@NonNull SharedKeyValueStore<String, Object> sharedKeyValueStore) {
        mSharedKeyValueStore = sharedKeyValueStore;
    }

    public boolean isStethoEnabled() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_IS_STETHO_ENABLED, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void saveIsStethoEnabled(boolean isStethoEnabled) {
        mSharedKeyValueStore.put(KEY_IS_STETHO_ENABLED, isStethoEnabled).apply();
    }

    public boolean isLeakCanaryEnabled() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_IS_LEAK_CANARY_ENABLED, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void saveIsLeakCanaryEnabled(boolean isLeakCanaryEnabled) {
        mSharedKeyValueStore.put(KEY_IS_LEAK_CANARY_ENABLED, isLeakCanaryEnabled).apply();
    }

    public boolean isPicassoIndicatorEnabled() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_PICASSO_INDICATOR_ENABLED, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void savePicassoIndicatorEnabled(boolean enable) {
        mSharedKeyValueStore.put(KEY_PICASSO_INDICATOR_ENABLED, enable).apply();
    }

    public boolean isCrashlyticsEnabled() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_IS_CRASHLYTICS_ENABLED, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void saveIsCrashlyticsEnabled(boolean enable) {
        mSharedKeyValueStore.put(KEY_IS_CRASHLYTICS_ENABLED, enable).apply();
    }

    public boolean isAutoFillTestValuesEnabled() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_ENABLE_AUTO_FILL_VALUES, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void saveAutoFillTestValuesEnabled(boolean enable) {
        mSharedKeyValueStore.put(KEY_ENABLE_AUTO_FILL_VALUES, enable).apply();
    }

    public boolean isPicassoLoggingEnabled() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_PICASSO_ENABLE_LOGGING, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void savePicassoLoggingEnabled(boolean enabled) {
        mSharedKeyValueStore.put(KEY_PICASSO_ENABLE_LOGGING, enabled).apply();
    }

    public String getHttpLoggingLevel() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_HTTP_LOGGING_LEVEL, String.class,
                                                   HttpLoggingInterceptor.Level.BASIC.name());
    }

    public void saveHttpLoggingLevel(HttpLoggingInterceptor.Level level) {
        mSharedKeyValueStore.put(KEY_HTTP_LOGGING_LEVEL, level.name()).apply();
    }

    public boolean isCrashOnRxLogEnabled() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_ENABLE_CRASH_ON_RXLOG, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void saveCrashOnRxLogEnabled(boolean enabled) {
        mSharedKeyValueStore.put(KEY_ENABLE_CRASH_ON_RXLOG, enabled).apply();
    }
}
