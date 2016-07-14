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
import android.content.Context;
import android.support.annotation.NonNull;
import com.neatier.commons.helpers.SharedKeyValueStore;
import com.neatier.shell.appframework.Navigator;
import com.neatier.shell.appframework.helpers.DialogMaker;
import com.neatier.shell.factorysettings.AppSettings;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
public class ApplicationModule {
    @NonNull
    private final Application application;

    public ApplicationModule(@NonNull Application application) {
        this.application = application;
    }

    @Provides
    @NonNull
    @Singleton
    public Application provideHirTVApplication() {
        return application;
    }

    @Provides
    @Singleton
    protected Navigator provideNavigator() {
        return Navigator.getInstance();
    }

    @Provides
    @Singleton
    protected DialogMaker provideDialogMaker() {
        return DialogMaker.getInstance();
    }

    @Provides @Singleton
    public Context provideContext() {
        return application;
    }

    @Provides
    @Singleton
    protected SharedKeyValueStore<String, Object> provideSharedKeyValueStore(Context context) {
        return new SharedKeyValueStore<>(context, AppSettings.PREF_DEFAULT_STORAGE_FILE);
    }
}
