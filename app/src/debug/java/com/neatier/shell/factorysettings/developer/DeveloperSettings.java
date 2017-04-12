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
import com.neatier.shell.factorysettings.PrefKey;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Wrapper over {@link SharedPreferences} to store developer settings.
 */
public class DeveloperSettings {

    private static final String KEY_IS_LEAK_CANARY_ENABLED = "is_leak_canary_enabled";
    private static final String KEY_PICASSO_INDICATOR_ENABLED = "use_picasso_indicator";
    private static final String KEY_IS_CRASHLYTICS_ENABLED = "is_crashlytics_enabled";
    private static final String KEY_ENABLE_AUTO_FILL_VALUES = "enable_autofill_test_values";
    private static final String KEY_PICASSO_ENABLE_LOGGING = "enable_picasso_logging";
    private static final String KEY_HTTP_LOGGING_LEVEL = "key_http_logging_level";
    private static final String KEY_ENABLE_CRASH_ON_RXLOG = "key_enable_crash_on_rxlog";
    private static final String KEY_FORCE_PORTRAIT_ORIENTATION = "key_force_portrait_orientation";
    private static final String KEY_SHOW_DEBUG_VIEWS = "key_enable_debug_views";
    private static final String KEY_STRICT_MODE_ENABLED = "key_strict_mode_enabled";

    public static final String DEV_HELLO_PASS = "{"
          //+ "\token\" : \"3c3d6e72-7057-4d98-abc4-e79d97b28294\", "
          + "\"email\" : \"laszlo.galosi@gmail.com\", "
          + "\"name\" : \"Gálosi László\", "
          + "\"password\" : \"asdasdasd\", "
          + "\"termsAccepted\" : true "
          + "}";

    @NonNull
    private final SharedKeyValueStore<String, Object> mSharedKeyValueStore;

    public DeveloperSettings(@NonNull SharedKeyValueStore<String, Object> sharedKeyValueStore) {
        mSharedKeyValueStore = sharedKeyValueStore;
    }

    public boolean isLeakCanaryEnabled() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_IS_LEAK_CANARY_ENABLED, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void setLeakCanaryEnabled(boolean isLeakCanaryEnabled) {
        mSharedKeyValueStore.put(KEY_IS_LEAK_CANARY_ENABLED, isLeakCanaryEnabled).apply();
    }

    public boolean isPicassoIndicatorEnabled() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_PICASSO_INDICATOR_ENABLED, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void setPicassoIndicatorEnabled(boolean enable) {
        mSharedKeyValueStore.put(KEY_PICASSO_INDICATOR_ENABLED, enable).apply();
    }

    public boolean isCrashlyticsEnabled() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_IS_CRASHLYTICS_ENABLED, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void setIsCrashlyticsEnabled(boolean enable) {
        mSharedKeyValueStore.put(KEY_IS_CRASHLYTICS_ENABLED, enable).apply();
    }

    public boolean isAutoFillTestValuesEnabled() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_ENABLE_AUTO_FILL_VALUES, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void setAutoFillTestValuesEnabled(boolean enable) {
        mSharedKeyValueStore.put(KEY_ENABLE_AUTO_FILL_VALUES, enable).apply();
    }

    public boolean isPicassoLoggingEnabled() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_PICASSO_ENABLE_LOGGING, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void setPicassoLoggingEnabled(boolean enabled) {
        mSharedKeyValueStore.put(KEY_PICASSO_ENABLE_LOGGING, enabled).apply();
    }

    public String getHttpLoggingLevel() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_HTTP_LOGGING_LEVEL, String.class,
                                                   HttpLoggingInterceptor.Level.BASIC.name());
    }

    public void setHttpLoggingLevel(HttpLoggingInterceptor.Level level) {
        mSharedKeyValueStore.put(KEY_HTTP_LOGGING_LEVEL, level.name()).apply();
    }

    public boolean isCrashOnRxLogEnabled() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_ENABLE_CRASH_ON_RXLOG, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void setCrashOnRxLogEnabled(boolean enabled) {
        mSharedKeyValueStore.put(KEY_ENABLE_CRASH_ON_RXLOG, enabled).apply();
    }

    public boolean isPortraitOrientationForced() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_FORCE_PORTRAIT_ORIENTATION, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void setForcePortraitOrientation(boolean forced) {
        mSharedKeyValueStore.put(KEY_FORCE_PORTRAIT_ORIENTATION, forced).apply();
    }

    public boolean shouldShowDebugViews() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_SHOW_DEBUG_VIEWS, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void setShowDebugViews(boolean forced) {
        mSharedKeyValueStore.put(KEY_SHOW_DEBUG_VIEWS, forced).apply();
    }

    public boolean isStrictModeEnabled() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_STRICT_MODE_ENABLED, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void setStrictModeEbabled(boolean enable) {
        mSharedKeyValueStore.put(KEY_STRICT_MODE_ENABLED, enable).apply();
    }

    public DeveloperSettings setPref(PrefKey prefKey, Object value) {
        mSharedKeyValueStore.put(prefKey.name, value).apply();
        return this;
    }

    public DeveloperSettings clearPref(PrefKey prefKey) {
        mSharedKeyValueStore.remove(prefKey.name).apply();
        return this;
    }
}
