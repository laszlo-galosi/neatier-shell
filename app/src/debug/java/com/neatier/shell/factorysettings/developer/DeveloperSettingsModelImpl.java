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

import android.app.Application;
import android.support.annotation.NonNull;
import com.crashlytics.android.Crashlytics;
import com.neatier.shell.BuildConfig;
import io.fabric.sdk.android.Fabric;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by László Gálosi on 04/05/16
 */
public class DeveloperSettingsModelImpl implements DeveloperSettingsModel {

    @NonNull
    private final Application application;

    @NonNull
    private final DeveloperSettings developerSettings;

    @NonNull
    private final LeakCanaryProxy leakCanaryProxy;

    @NonNull
    private AtomicBoolean stethoAlreadyEnabled = new AtomicBoolean();

    @NonNull
    private AtomicBoolean leakCanaryAlreadyEnabled = new AtomicBoolean();

    @NonNull
    private AtomicBoolean tinyDancerDisplayed = new AtomicBoolean();

    public DeveloperSettingsModelImpl(@NonNull Application application,
          @NonNull DeveloperSettings developerSettings,
          @NonNull LeakCanaryProxy leakCanaryProxy) {
        this.application = application;
        this.developerSettings = developerSettings;
        this.leakCanaryProxy = leakCanaryProxy;
    }

    @NonNull
    public String getBuildVersionCode() {
        return String.valueOf(BuildConfig.VERSION_CODE);
    }

    @NonNull
    public String getBuildVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public boolean isLeakCanaryEnabled() {
        return developerSettings.isLeakCanaryEnabled();
    }

    public void changeLeakCanaryState(boolean enabled) {
        developerSettings.saveIsLeakCanaryEnabled(enabled);
        apply();
    }

    public boolean isPicassoIndicatorEnabled() {
        return developerSettings.isPicassoIndicatorEnabled();
    }

    public void changePicassoIndicatorState(boolean enabled) {
        developerSettings.savePicassoIndicatorEnabled(enabled);
    }

    public boolean isCrashlyticsEnabled() {
        return developerSettings.isCrashlyticsEnabled();
    }

    public void changeCrashlyticsState(boolean enabled) {
        developerSettings.saveIsCrashlyticsEnabled(enabled);
    }

    public void changeisAutoFillTestValuesEnabled(boolean enabled) {
        developerSettings.saveAutoFillTestValuesEnabled(enabled);
    }

    public void changePicassoLoggingEnabled(boolean enabled) {
        developerSettings.savePicassoLoggingEnabled(enabled);
    }

    public void changeHttpLoggingLevel(HttpLoggingInterceptor.Level level) {
        developerSettings.saveHttpLoggingLevel(level);
    }

    public void changeCrashOnRxLogEnabled(boolean enabled) {
        developerSettings.saveCrashOnRxLogEnabled(enabled);
    }

    @Override
    public void apply() {
        // LeakCanary can not be enabled twice.
        if (leakCanaryAlreadyEnabled.compareAndSet(false, true)) {
            //Todo: set from Developer Settings UI instead of enabling from default.
            changeLeakCanaryState(true);
            if (isLeakCanaryEnabled()) {
                leakCanaryProxy.init();
            }
        }
        changeCrashlyticsState(false);
        if (isCrashlyticsEnabled()) {
            Fabric.with(application, new Crashlytics());
        }
        //Enables showing Picasso indicators of loaded image (network/disk/memory)
        changePicassoIndicatorState(true);
        //Enables Picasso logging of loading images.
        changePicassoLoggingEnabled(false);
        changeisAutoFillTestValuesEnabled(false);
        //Set whether RxLogger.logRxError call causes crashes the app or only logs to the err.
        changeCrashOnRxLogEnabled(false);
        //Change the HttpLogging level of RetroFit.
        changeHttpLoggingLevel(HttpLoggingInterceptor.Level.BODY);
    }
}
