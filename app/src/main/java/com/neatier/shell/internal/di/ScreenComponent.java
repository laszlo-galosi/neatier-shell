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

package com.neatier.shell.internal.di;

import com.neatier.shell.appframework.PhotoViewFragment;
import dagger.Subcomponent;

/**
 * A component, for {@link SectionFragment} to inject Presenters, and other objects,
 * only for invidiual section screens with diferrent channel id-s.
 * Created by László Gálosi on 21/05/16
 */
@PerScreen
@Subcomponent(modules = { ScreenModule.class })
public interface ScreenComponent {
    void inject(PhotoViewFragment target);
}
