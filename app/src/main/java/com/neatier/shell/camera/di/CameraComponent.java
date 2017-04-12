/*
 * Copyright (C) 2017 Extremenet Ltd., All Rights Reserved
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

import com.neatier.shell.appframework.helpers.PermissionInteraction;
import com.neatier.shell.internal.di.ActivityComponent;
import com.neatier.shell.internal.di.ActivityModule;
import com.neatier.shell.internal.di.ApplicationComponent;
import com.neatier.shell.internal.di.PerActivity;
import com.squareup.picasso.Picasso;
import dagger.Component;

/**
 * Created by László Gálosi on 17/08/15
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = {
      ActivityModule.class,
      CameraModule.class,
})
public interface CameraComponent extends ActivityComponent {
    PermissionInteraction permissionInteraction();

    Picasso picassoInstance();
}
