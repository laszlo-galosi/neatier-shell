package com.neatier.shell.home.di;

import com.neatier.shell.home.FakeHomePresenterImpl;
import com.neatier.shell.home.HomePresenter;
import dagger.Module;
import dagger.Provides;

/**
 * Created by László Gálosi on 21/05/16
 */
@Module
public class HomeModule {
    public HomeModule() {
    }

    @Provides
    HomePresenter provideHomePresenter() {
        return new FakeHomePresenterImpl();
    }
}
