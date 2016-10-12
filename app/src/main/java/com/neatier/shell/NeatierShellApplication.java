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

package com.neatier.shell;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import com.neatier.shell.data.network.ApiSettings;
import com.neatier.shell.data.network.di.HttpInterceptorModule;
import com.neatier.shell.data.network.di.HttpNetworkModule;
import com.neatier.shell.data.network.di.RestApiModule;
import com.neatier.shell.eventbus.RxBus;
import com.neatier.shell.exception.RxLogger;
import com.neatier.shell.factorysettings.AppSettings;
import com.neatier.shell.factorysettings.developer.DeveloperSettingsModel;
import com.neatier.shell.internal.di.ApplicationComponent;
import com.neatier.shell.internal.di.ApplicationModule;
import com.neatier.shell.internal.di.DaggerApplicationComponent;
import com.neatier.shell.internal.di.HasComponent;
import com.squareup.leakcanary.LeakCanary;
import dagger.Lazy;
import javax.inject.Inject;
import net.danlew.android.joda.JodaTimeAndroid;
import trikita.log.Log;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class NeatierShellApplication extends Application
      implements HasComponent<ApplicationComponent> {
    private ApplicationComponent applicationComponent;

    @Inject
    Lazy<DeveloperSettingsModel> developerSettingModel;

    // Prevent need in a singleton (global) reference to the application object.
    @NonNull
    public static NeatierShellApplication get(@NonNull Context context) {
        return (NeatierShellApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

/*        if (!isInUnitTests()) {
            MultiDex.install(this);
        }*/

        applicationComponent = DaggerApplicationComponent.builder()
                                                         .applicationModule(
                                                               new ApplicationModule(this)).build();
        applicationComponent.inject(this);

        //Initialize RxBus event constants.
        RxBus.initConstants(this);
        //Initialize Enum-like factory settings constant classes.
        AppSettings.init(this);

        RxLogger.setCrashOnCall(getComponent().developerSettings().isCrashOnRxLogEnabled());
        //Initialize calligraphy.
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                                            .setDefaultFontPath("fonts/Roboto-Light.ttf")
                                            .setFontAttrId(R.attr.fontPath)
                                            .build());
        Log.useFormat(true);
        JodaTimeAndroid.init(this);

        //apply debug specific initialization
        developerSettingModel.get().apply();
    }

    @NonNull
    protected DaggerApplicationComponent.Builder prepareApplicationComponent() {
        return DaggerApplicationComponent.builder()
                                         .applicationModule(new ApplicationModule(this))
                                         .restApiModule(
                                               new RestApiModule(
                                                     ApiSettings.DEFAULT_SERVER_ENDPOINT)
                                         )
                                         .httpNetworkModule(new HttpNetworkModule())
                                         .httpInterceptorModule(new HttpInterceptorModule());
    }

    @Override
    @NonNull
    public ApplicationComponent getComponent() {
        return applicationComponent;
    }

    /**
     * @Return if the app is under unit test. {@link LeakCanary} doesn't install.
     * Override in unit test Application stub.
     */
    protected boolean isInUnitTests() {
        return false;
    }
}
