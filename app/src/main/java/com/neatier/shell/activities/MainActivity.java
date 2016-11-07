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

package com.neatier.shell.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.fernandocejas.arrow.optional.Optional;
import com.neatier.commons.helpers.BundleWrapper;
import com.neatier.commons.helpers.KeyValuePairs;
import com.neatier.commons.helpers.LongTaskOnIOScheduler;
import com.neatier.shell.NeatierShellApplication;
import com.neatier.shell.R;
import com.neatier.shell.appframework.AppMvp;
import com.neatier.shell.appframework.MultiFragmentActivity;
import com.neatier.shell.appframework.Navigator;
import com.neatier.shell.appframework.TaggedBaseFragment;
import com.neatier.shell.appframework.helpers.DialogMaker;
import com.neatier.shell.data.network.ApiSettings;
import com.neatier.shell.eventbus.Event;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.eventbus.EventParam;
import com.neatier.shell.eventbus.Item;
import com.neatier.shell.exception.ErrorMessageFactory;
import com.neatier.shell.home.HomeFragment;
import com.neatier.shell.internal.di.DaggerMainComponent;
import com.neatier.shell.internal.di.HasComponent;
import com.neatier.shell.internal.di.MainComponent;
import com.neatier.shell.internal.di.MainModule;
import com.neatier.shell.navigation.NavigationMenuItemAdapter;
import com.neatier.shell.navigation.NavigationMenuPresenter;
import com.neatier.shell.xboxgames.GameTitleTitleListFragment;
import javax.inject.Inject;
import trikita.log.Log;

public class MainActivity extends MultiFragmentActivity implements
                                                        HasComponent<MainComponent>,
                                                        AppMvp.LongTaskBaseView,
                                                        ViewTreeObserver.OnGlobalLayoutListener {

    @BindView(R.id.mainLayout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.content_main) View mMainContentView;
    @BindView(R.id.nav_view) NavigationView mNavigationView;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_menu) RecyclerView mNavMenuRecyclerView;
    @BindView(R.id.bottom_navigation_view) BottomNavigationView mBottomNavigationView;

    @Inject
    NavigationMenuPresenter mNavigationMenuPresenter;
    private boolean mLayoutReady;

    private boolean mInitWithError;
    private boolean mWaitingForModelUpdate;
    private MainComponent mainComponent;
    private GestureDetectorCompat mGestureDetector;
    private NavigationMenuItemAdapter mNavMenuAdapter;

    /**
     * Static method returning a starter intent of this Activity, used by the {@link Navigator}.
     */
    public static Intent getStarterIntent(Context context) {
        Intent newIntent = new Intent(context, MainActivity.class);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return newIntent;
    }

    @SuppressLint("InflateParams") // It's okay in our case.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("onCreate", savedInstanceState);
        super.onCreate(savedInstanceState);

        //Inject ApplicationComponent to this activity, so the @Singletons are injected to this.
        ((NeatierShellApplication) getApplication()).getComponent().inject(this);

        setContentView(R.layout.activity_main);
        mUnbinder = ButterKnife.bind(this);
        //listening to backStack events.
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        initializeInjector();
        mNavigationMenuPresenter = getComponent().navigationMenuPresenter();
        mNavigationMenuPresenter.setView(this);
        setupNavigationView();

        mBottomNavigationView.setOnNavigationItemSelectedListener(
              item -> {
                  handleNavigation(
                        EventBuilder.withItemAndType(Item.NAV_MENU_ITEM, Event.EVT_NAVIGATE)
                                    .addParam(EventParam.PRM_ITEM_ID, item.getItemId())
                  );
                  return true;
              }
        );

        //Creating a main bundle for arguments.
        mMainBundle = BundleWrapper.wrap(new Bundle());
        Bundle extras = getIntent().getExtras();
        //merging with the starting intent's bundle.
        if (extras != null) {
            mMainBundle.putAll(extras);
        }
        //setting up status bar for nightmode cchanging
        int systemBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        setSystemBarsColor(systemBarColor);

        //if no saved state storing the default fragment tag, and initializing the activity.
        if (savedInstanceState == null) {
            mCurrentFragmentTag = HomeFragment.TAG;
            onRestoreInstanceState(mMainBundle.getBundle());
        }
    }

    private void initializeInjector() {
        mApiParams = new KeyValuePairs<>(2);
        this.mainComponent = createComponent();
    }

    @Override
    protected void onResume() {
        Log.v("onResume");
        super.onResume();
        mNavigationMenuPresenter.resume();
    }

    @Override
    protected void onPause() {
        Log.v("onPause");
        super.onPause();
        mNavigationMenuPresenter.pause();
    }

    @Override
    protected void onDestroy() {
        Log.v("onDestroy");
        mNavigationMenuPresenter.destroy();

        super.onDestroy();
    }

    @Override
    public void onGlobalLayout() {
        Log.d("onGlobalLayout");
        if (mLayoutReady || mInitWithError) {
            mNavMenuRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            hideProgress();
        } else {
            if (!mWaitingForModelUpdate) {
                mNavigationMenuPresenter.initialize();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        switch (i) {
            case android.R.id.home:
                boolean shouldGoBack = getSupportFragmentManager().getBackStackEntryCount() > 1;
                if (shouldGoBack) {
                    onBackPressed();
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    mNavMenuRecyclerView.scrollToPosition(0);
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public void showProgress() {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void showError(final ErrorMessageFactory.Error error) {
        DialogMaker.getInstance()
                   .setMessageRes(error.getMessageRes())
                   .positiveAction(R.string.snackbar_action_ok);
        makeSnackBar(error.showAsAlert());
    }

    @Override
    public void onUpdateFinished(Throwable... errors) {
        Log.v("onUpdateFinished");
        if (errors.length > 0) {
            mInitWithError = true;
            for (int i = 0, len = errors.length; i < len; i++) {
                showError(ErrorMessageFactory.create(errors[0]));
            }
        } else {
            mWaitingForModelUpdate = false;
        }
    }

    @Override
    public void onUpdateStarted() {
        Log.v("onUpdateStarted");
        mWaitingForModelUpdate = true;
    }

    @Override
    public void onModelReady() {
        mLayoutReady = true;
        mNavMenuAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean isResultFromCloud() {
        return false;
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    protected void handleNavigation(final EventBuilder event) {
        super.handleNavigation(event);
        event.logMe("handleNavigation").v(event);
        Item item = event.getItem();
        if (item.id == Item.NAV_MENU_ITEM) {
            int itemId = event.getParamAs(EventParam.PRM_ITEM_ID, Integer.class, 0).get();
            switch (itemId) {
               /* case R.id.action_home:
                    super.addOrReplaceFragment(null, HomeFragment.newInstance(getMetaData()));
                    break;*/
                case R.id.action_xboxone:
                case R.id.action_xbox360:
                    super.addOrReplaceFragment(
                          null,
                          GameTitleTitleListFragment.newInstance(
                                getMetaData().put(ApiSettings.KEY_API_LIST_TYPE,
                                                  itemId == R.id.action_xbox360
                                                  ? ApiSettings.LIST_XBOX360
                                                  : ApiSettings.LIST_XBOXONE)
                          ));
                    break;
                case R.id.action_settings:
                    break;
                default:
            }
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        Log.i("onBackPressed", "from", mCurrentFragmentTag);
        if (shouldGoBack()) {
            super.onBackPressed();
            return;
        } else {
            ////Remove all fragments until its the last one.
            while (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                getSupportFragmentManager().popBackStackImmediate();
            }
        }
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackCount == 1) {
            super.onBackPressed();
            finish();
        }
    }

    @Override protected boolean shouldGoBack() {
        Optional<String> currentFragmentTag = getLastXFragmentTagOnStack(1);
        if (currentFragmentTag.isPresent()) {
            TaggedBaseFragment fr =
                  (TaggedBaseFragment) getSupportFragmentManager().findFragmentByTag(
                        currentFragmentTag.get());
            return fr != null && fr.shouldGoBack();
        }
        return super.shouldGoBack();
    }

    private void setupNavigationView() {
        //Setting up the navigation view.
        //mNavigationView.setNavigationItemSelectedListener(this);
        // finally change the color
        //set the navigation drawer content padding based on the statusbar height.
        final ViewGroup decorView = (ViewGroup) this.getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(
              new ViewTreeObserver.OnGlobalLayoutListener() {
                  @Override
                  public void onGlobalLayout() {
                      if (Build.VERSION.SDK_INT >= 16) {
                          decorView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                      } else {
                          // Nice one, Google
                          decorView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                      }
                      Rect rect = new Rect();
                      decorView.getWindowVisibleDisplayFrame(rect);
                      if (Build.VERSION.SDK_INT >= 21) {
                          mNavMenuRecyclerView.setPadding(0, rect.top, 0, 0);
                      }
                  }
              });

        //final ItemClickSupport itemClick = ItemClickSupport.addTo(mNavMenuRecyclerView);
        //itemClick.setOnItemClickListener(this);
        if (mNavMenuAdapter == null) {
            mNavMenuAdapter =
                  new NavigationMenuItemAdapter(
                        this,
                        R.layout.list_item_navmenu, 48,
                        mNavigationMenuPresenter.getMenuItems()
                  ).withHeader(R.layout.widget_nav_header);
        }
        mNavMenuRecyclerView.setAdapter(mNavMenuAdapter);
        mNavMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mNavMenuRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public MainComponent getComponent() {
        return mainComponent;
    }

    @Override public MainComponent createComponent() {
        return DaggerMainComponent.builder()
                                  .applicationComponent(getApplicationComponent())
                                  .activityModule(getActivityModule())
                                  .mainModule(new MainModule(mApiParams,
                                                             new LongTaskOnIOScheduler()))
                                  .build();
    }

    @Override public void releaseComponent() {
        mainComponent = null;
    }

    @Override public <T extends TaggedBaseFragment> Optional<T> getDefaultFragmentInstance(
          final BundleWrapper instanceBundleWrapper) {
        return Optional.of((T) GameTitleTitleListFragment.newInstance(
              instanceBundleWrapper.put(ApiSettings.KEY_API_LIST_TYPE, ApiSettings.LIST_XBOXONE)));
    }

    public void setOnGestureListener(final GestureDetector.OnGestureListener gl) {
        if (gl == null) {
            mGestureDetector = null;
            return;
        }
        mGestureDetector = new GestureDetectorCompat(this, gl);
    }

    public GestureDetectorCompat getGestureDetector() {
        return mGestureDetector;
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent motionEvent) {
        boolean defaultResult = super.dispatchTouchEvent(motionEvent);
        if (mGestureDetector != null) {
            return mGestureDetector.onTouchEvent(motionEvent);
        }
        return defaultResult;
    }
}
