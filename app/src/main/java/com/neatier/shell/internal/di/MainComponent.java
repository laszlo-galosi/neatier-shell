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

import com.neatier.shell.navigation.NavigationMenuPresenter;
import dagger.Component;

/**
 * Created by László Gálosi on 17/08/15
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = {
      ActivityModule.class,
      MainModule.class,
})
public interface MainComponent extends ActivityComponent {
    //HomeComponent plus(HomeModule sectionModule);
    NavigationMenuPresenter navigationMenuPresenter();

    //    void inject(HomeFragment fragment);
}
