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

package com.neatier.shell.internal.di;

import android.app.Application;
import android.support.annotation.NonNull;
import com.neatier.commons.helpers.SharedKeyValueStore;
import com.neatier.commons.settings.FactorySettings;
import com.neatier.shell.factorysettings.developer.DeveloperSettings;
import com.neatier.shell.factorysettings.developer.DeveloperSettingsModel;
import com.neatier.shell.factorysettings.developer.DeveloperSettingsModelImpl;
import com.neatier.shell.factorysettings.developer.LeakCanaryProxy;
import com.neatier.shell.factorysettings.developer.LeakCanaryProxyImpl;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
public class DeveloperSettingsModule {

    @Provides
    @NonNull
    public DeveloperSettingsModel provideDeveloperSettingsModel(@NonNull
    DeveloperSettingsModelImpl developerSettingsModelImpl) {
        return developerSettingsModelImpl;
    }

    @Provides
    @NonNull
    @Singleton
    public DeveloperSettings provideDeveloperSettings(@NonNull Application application) {
        return new DeveloperSettings(
              new SharedKeyValueStore<>(application.getApplicationContext(),
                                        FactorySettings.PREF_DEV_SETTINGS_FILE));
    }

    @Provides
    @NonNull
    @Singleton
    public LeakCanaryProxy provideLeakCanaryProxy(@NonNull Application application) {
        return new LeakCanaryProxyImpl(application);
    }

    // We will use this concrete type for debug code, but main code will see only
    // DeveloperSettingsModel interface.
    @Provides
    @NonNull
    @Singleton
    public DeveloperSettingsModelImpl provideDeveloperSettingsModelImpl(
          @NonNull Application application,
          @NonNull DeveloperSettings developerSettings,
          @NonNull LeakCanaryProxy leakCanaryProxy) {
        return new DeveloperSettingsModelImpl(application, developerSettings, leakCanaryProxy);
    }
}
