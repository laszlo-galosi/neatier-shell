package com.neatier.shell.xboxgames.di;

import com.neatier.shell.internal.di.PerScreen;
import com.neatier.shell.xboxgames.GameTitleTitleListFragment;
import dagger.Subcomponent;

/**
 * A component, for {@link SectionFragment} to inject Presenters, and other objects,
 * only for invidiual section screens with diferrent channel id-s.
 * Created by László Gálosi on 21/05/16
 */
@PerScreen
@Subcomponent(modules = { XboxModule.class })
public interface XboxComponent {
    void inject(GameTitleTitleListFragment target);
}
