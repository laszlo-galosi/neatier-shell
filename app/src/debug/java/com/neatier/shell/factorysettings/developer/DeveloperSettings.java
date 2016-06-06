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

/**
 * Wrapper over {@link SharedPreferences} to store developer settings.
 */
public class DeveloperSettings {

    private static final String KEY_IS_STETHO_ENABLED = "is_stetho_enabled";
    private static final String KEY_IS_LEAK_CANARY_ENABLED = "is_leak_canary_enabled";
    private static final String KEY_IS_TINY_DANCER_ENABLED = "is_tiny_dancer_enabled";

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

    public boolean isTinyDancerEnabled() {
        return mSharedKeyValueStore.getAsOrDefault(KEY_IS_TINY_DANCER_ENABLED, Boolean.class,
                                                   Boolean.FALSE);
    }

    public void saveIsTinyDancerEnabled(boolean isTinyDancerEnabled) {
        mSharedKeyValueStore.put(KEY_IS_TINY_DANCER_ENABLED, isTinyDancerEnabled).apply();
    }
}
