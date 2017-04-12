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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import com.neatier.commons.helpers.KeyValuePairs;
import com.neatier.commons.helpers.LongTaskScheduler;
import com.neatier.commons.helpers.SharedKeyValueStore;
import com.neatier.commons.interactors.CameraInteraction;
import com.neatier.shell.R;
import com.neatier.shell.appframework.helpers.PermissionInteraction;
import com.neatier.shell.appframework.helpers.PermissionInteractor;
import com.neatier.shell.camera.CameraIntentInteractor;
import com.neatier.shell.eventbus.Event;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.eventbus.EventParam;
import com.neatier.shell.eventbus.Item;
import com.neatier.shell.eventbus.RxBus;
import com.neatier.shell.factorysettings.developer.DeveloperSettings;
import com.neatier.shell.navigation.NavigationMenuPresenter;
import com.neatier.shell.navigation.XmlNavigationMenuPresenterImpl;
import com.neatier.shell.navigation.bottomnav.BottomNavigationMenuPresenter;
import com.squareup.picasso.Picasso;
import dagger.Module;
import dagger.Provides;
import rx.Observable;
import rx.functions.Func1;
import trikita.log.Log;

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

    @Provides @PerActivity
    protected BottomNavigationMenuPresenter provideBottomNavigationMenuPresenter(final
    SharedKeyValueStore<String, Object> settingsStore) {
        return new BottomNavigationMenuPresenter() {
            @Override public Func1<MenuItem, Boolean> getMenuItemFilter() {
                return item -> Boolean.TRUE;
            }

            @Override public Func1<MenuItem, Boolean> getDefaultSelectionFilter() {
                return item -> item.getItemId() == R.id.action_xboxone;
            }
        };
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
        return new PermissionInteractor(sharedKeyValueStore, activity) {
            @Override
            public void sendIntentForPermissionRequest(final int requestCode, final int grantResult,
                  final Intent intentForResult) {
                Log.d("sendIntentForPermissionRequest", requestCode, "result", grantResult,
                      intentForResult);
                if (intentForResult != null) {
                    Log.d(intentForResult).v("extras", intentForResult.getExtras());
                    EventBuilder extIntentEvent =
                          EventBuilder.withItemAndType(Item.EXT_INTENT, Event.EVT_RESULT)
                                      .addParam(EventParam.PRM_ACTION, intentForResult.getAction())
                                      .addParam(EventParam.PRM_REQUEST_CODE, requestCode)
                                      .addParam(EventParam.PRM_RESULT_CODE, grantResult);
                    extIntentEvent.putParcelable(EventParam.externalIntent().name, intentForResult);
                    Bundle extras = intentForResult.getExtras();
                    if (extras != null) {
                        //put all extras parameter for easy acces to the event except request code.
                        Observable.from(extras.keySet())
                                  .filter(key -> !EventParam.requestCode().name.equals(key))
                                  .subscribe(key -> extIntentEvent.put(key, extras.get(key)));
                    }
                    extIntentEvent.deliver(RxBus.getInstance());
                    //extIntentEvent.deliver(BehaviorRxBus.getInstance());

                    intentForResult.putExtra(EventParam.resultCode().name, grantResult);
                    //set the new intent filter send
                    LocalBroadcastManager.getInstance(activity).sendBroadcast(intentForResult);
                    //intent was served, so remove from map.
                }
            }
        };
    }

    @Provides @NonNull @PerActivity
    public CameraInteraction provideCameraInteraction(Context context) {
        return new CameraIntentInteractor(context);
    }
}
