/*
 * Copyright (C) 2016 Extremenet Ltd., All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  Proprietary and confidential.
 *  All information contained herein is, and remains the property of Extremenet Ltd.
 *  The intellectual and technical concepts contained herein are proprietary to Extremenet Ltd.
 *   and may be covered by U.S. and Foreign Patents, pending patents, and are protected
 *  by trade secret or copyright law. Dissemination of this information or reproduction of
 *  this material is strictly forbidden unless prior written permission is obtained from
 *   Extremenet Ltd.
 *
 */

package com.neatier.shell.camera.di;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import com.neatier.commons.helpers.SharedKeyValueStore;
import com.neatier.commons.interactors.CameraInteraction;
import com.neatier.shell.appframework.helpers.PermissionInteraction;
import com.neatier.shell.appframework.helpers.PermissionInteractor;
import com.neatier.shell.camera.CameraIntentInteractor;
import com.neatier.shell.factorysettings.developer.DeveloperSettings;
import com.neatier.shell.internal.di.PerActivity;
import com.squareup.picasso.Picasso;
import dagger.Module;
import dagger.Provides;
import trikita.log.Log;

/**
 * Created by László Gálosi on 18/05/16
 */
@Module
public class CameraModule {

    /**
     * Constructor.
     */
    public CameraModule() {
    }

    @Provides @NonNull @PerActivity
    public Picasso providePicasso(Context context, final DeveloperSettings developerSettingsModel) {
        return new Picasso.Builder(context)
              .indicatorsEnabled(developerSettingsModel.isPicassoIndicatorEnabled())
              .loggingEnabled(developerSettingsModel.isPicassoLoggingEnabled())
              .listener((picasso1, uri, ex) -> Log.e("Picasso.onImageLoadFailed", uri, ex))
              .build();
    }

    @Provides @NonNull @PerActivity
    public PermissionInteraction providePermissionInteraction(
          final SharedKeyValueStore<String, Object> sharedKeyValueStore,
          Activity activity) {
        return new PermissionInteractor(sharedKeyValueStore, activity);
    }

    @Provides @NonNull @PerActivity
    public CameraInteraction provideCameraInteraction(Context context) {
        return new CameraIntentInteractor(context);
    }
}
