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

import com.neatier.commons.helpers.KeyValuePairs;
import com.neatier.commons.helpers.LongTaskScheduler;
import com.neatier.shell.navigation.NavigationMenuPresenter;
import com.neatier.shell.navigation.XmlNavigationMenuPresenterImpl;
import dagger.Module;
import dagger.Provides;

/**
 * Created by László Gálosi on 18/05/16
 */
@Module
public class MainModule {

    private final LongTaskScheduler mLongTaskScheduler;
    private final KeyValuePairs<String, Object> mApiParams;

    /**
     * Constructor.
     */
    public MainModule(final KeyValuePairs<String, Object> params,
          LongTaskScheduler longTaskScheduler) {
        this.mApiParams = params;
        this.mLongTaskScheduler = longTaskScheduler;
    }

    @Provides @PerActivity protected NavigationMenuPresenter provideNavigationMenuPresenter() {
        return new XmlNavigationMenuPresenterImpl();
    }
}
