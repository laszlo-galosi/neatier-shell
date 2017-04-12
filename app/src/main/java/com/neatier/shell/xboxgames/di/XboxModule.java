package com.neatier.shell.xboxgames.di;

import android.content.Context;
import com.neatier.commons.helpers.JsonSerializer;
import com.neatier.shell.data.repository.DataSources;
import com.neatier.shell.internal.di.PerScreen;
import com.neatier.shell.xboxgames.GamePresenter;
import com.neatier.shell.xboxgames.GamePresenterImpl;
import com.neatier.shell.xboxgames.GameTitleTitleListFragment;
import com.squareup.picasso.Picasso;
import dagger.Module;
import dagger.Provides;

/**
 * Created by László Gálosi on 21/05/16
 */
@Module
public class XboxModule {
    public XboxModule() {
    }

    @Provides @PerScreen
    GamePresenter provideGameTitlePresenter(
          final DataSources.SimpleJsonResponseDataSource apiDataSource, JsonSerializer serializer) {
        return new GamePresenterImpl(apiDataSource, serializer);
    }

    @Provides @PerScreen
    GameTitleTitleListFragment.GameTitleItemAdapter provideGameTitleAdapter(
          final Picasso picasso,
          final Context context) {
        return new GameTitleTitleListFragment.GameTitleItemAdapter(picasso, context);
    }
}
