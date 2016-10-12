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

import android.content.Context;
import android.support.annotation.NonNull;
import com.neatier.commons.helpers.SharedKeyValueStore;
import com.neatier.shell.NeatierShellApplication;
import com.neatier.shell.appframework.MultiFragmentActivity;
import com.neatier.shell.data.network.di.HttpInterceptorModule;
import com.neatier.shell.data.network.di.HttpNetworkModule;
import com.neatier.shell.data.network.di.RestApiModule;
import com.neatier.shell.factorysettings.developer.DeveloperSettings;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {
      ApplicationModule.class,
      DeveloperSettingsModule.class,
      HttpNetworkModule.class,
      HttpInterceptorModule.class,
      RestApiModule.class
})
public interface ApplicationComponent {

    // Provide LeakCanary without injection to leave
    //@NonNull LeakCanaryProxy leakCanaryProxy();

    void inject(@NonNull NeatierShellApplication application);

    void inject(@NonNull MultiFragmentActivity activity);

    @NonNull DeveloperSettingsComponent plusDeveloperSettingsComponent();

    @NonNull Context context();

    @NonNull DeveloperSettings developerSettings();

    SharedKeyValueStore<String, Object> settingsStore();
}
