package com.neatier.shell.home.di;

import com.neatier.shell.home.HomeFragment;
import com.neatier.shell.internal.di.PerScreen;
import dagger.Subcomponent;

/**
 * A component, for {@link SectionFragment} to inject Presenters, and other objects,
 * only for invidiual section screens with diferrent channel id-s.
 * Created by László Gálosi on 21/05/16
 */
@PerScreen
@Subcomponent(modules = { HomeModule.class })
public interface HomeComponent {
    void inject(HomeFragment target);
}
