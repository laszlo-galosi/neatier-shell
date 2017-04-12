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
import android.os.StrictMode;
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
    private AtomicBoolean leakCanaryAlreadyEnabled = new AtomicBoolean();

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

    @Override
    public void apply() {
        // LeakCanary can not be enabled twice.
        if (leakCanaryAlreadyEnabled.compareAndSet(false, true)) {
            //Todo: set from Developer Settings UI instead of enabling from default.
            developerSettings.setLeakCanaryEnabled(false);
            if (developerSettings.isLeakCanaryEnabled()) {
                leakCanaryProxy.init();
            }
        }
        developerSettings.setIsCrashlyticsEnabled(false);
        if (developerSettings.isCrashlyticsEnabled()) {
            Fabric.with(application, new Crashlytics());
        }
        developerSettings.setCrashOnRxLogEnabled(true);

        //Enables showing Picasso indicators of loaded image (network/disk/memory)
        developerSettings.setPicassoIndicatorEnabled(true);
        //Enables Picasso logging of loading images.
        developerSettings.setPicassoLoggingEnabled(false);
        developerSettings.setAutoFillTestValuesEnabled(false);
        //Set whether RxLogger.logRxError call causes crashes the app or only logs to the err.
        //Force portrait orientation
        developerSettings.setForcePortraitOrientation(true);
        //Show/hide debug views
        developerSettings.setShowDebugViews(false);
        //Change the HttpLogging level of RetroFit.
        developerSettings.setHttpLoggingLevel(HttpLoggingInterceptor.Level.BODY);
        //Strict mode  programming mistakes early warnings.
        developerSettings.setStrictModeEbabled(false);
        if (developerSettings.isStrictModeEnabled()) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                                             .detectDiskReads()
                                             .detectDiskWrites()
                                             .detectNetwork()   // or .detectAll() for all
                                             // detectable problems
                                             .penaltyLog()
                                             .penaltyFlashScreen()
                                             .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                                         .detectLeakedClosableObjects()
                                         .detectLeakedRegistrationObjects()
                                         .detectActivityLeaks()
                                         .penaltyLog()
                                         //.penaltyDeath()
                                         .build());
        }

        //Setup initial preferences.
        //Minimal settings required for default function
        //Developer settings for testing purpose.
        //developerSettings
        //      .clearPref(PrefKey.userProfile())
        ;
    }
}
