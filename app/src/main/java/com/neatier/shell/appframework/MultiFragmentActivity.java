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

package com.neatier.shell.appframework;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.Unbinder;
import com.fernandocejas.arrow.optional.Optional;
import com.neatier.commons.helpers.BundleWrapper;
import com.neatier.commons.helpers.KeyValuePairs;
import com.neatier.commons.helpers.RxUtils;
import com.neatier.commons.helpers.SharedKeyValueStore;
import com.neatier.shell.NeatierShellApplication;
import com.neatier.shell.R;
import com.neatier.shell.activities.MainActivity;
import com.neatier.shell.appframework.helpers.DialogMaker;
import com.neatier.shell.eventbus.Event;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.eventbus.EventParam;
import com.neatier.shell.eventbus.Item;
import com.neatier.shell.eventbus.RxBus;
import com.neatier.shell.factorysettings.AppSettings;
import com.neatier.shell.internal.di.ActivityModule;
import com.neatier.shell.internal.di.ApplicationComponent;
import com.neatier.widgets.helpers.DrawableHelper;
import com.neatier.widgets.helpers.WidgetUtils;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import trikita.log.Log;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Base {@link AppCompatActivity} activity for navigation between multiple screens of {@link
 * BaseFragment},
 * Navigation uses {@link FragmentTransaction#add(Fragment, String)} or
 * {@link FragmentTransaction#replace(int, Fragment)}. Navigation can be handled with {@link
 * Event#EVT_NAVIGATE}
 * event type with specifying {@link Event}
 * Only several (or one) activity required
 * for the entire application. Contains Activity related helper API-s such as automatic
 * {@link PermissionRequest} flow, or BackStack navigation.
 */
@SuppressWarnings("ConstantConditions")
public abstract class MultiFragmentActivity extends AppCompatActivity
      implements FragmentManager.OnBackStackChangedListener,
                 ActivityCompat.OnRequestPermissionsResultCallback, SnackbarDisplayable {

    protected CompositeSubscription subscriptions = new CompositeSubscription();

    protected RxUtils.SubscriberAdapter mEventSubscriber;
    protected BundleWrapper mMainBundle;
    protected String mCurrentFragmentTag;

    protected KeyValuePairs<String, Object> mApiParams;
    protected Toolbar mToolbar;
    protected Unbinder mUnbinder;

    @Inject Navigator navigator;
    @Inject DialogMaker dialogMaker;
    @Inject protected SharedKeyValueStore<String, Object> sharedKeyValueStore;

    @BindView(R.id.mainLayout) protected CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.main_appbar) protected AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar_logo) protected ImageView mToolbarLogo;

    @Override protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setupToolbar();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        setupToolbar();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        setupToolbar();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        unsubscribe();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    /**
     * saving all the previously stored argument to a saved instance.
     */
    @Override protected void onSaveInstanceState(Bundle outState) {
        Log.d("onSaveInstanceState").v(mMainBundle);
        outState.putAll(mMainBundle.getBundle());
        outState.putString(TaggedBaseFragment.ARG_CURRENT_FRAGMENT_TAG, mCurrentFragmentTag);
        super.onSaveInstanceState(outState);
    }

    private void unsubscribe() {
        Log.d("unsubscribe:", "hasSubscribers:",
              this.subscriptions != null && this.subscriptions.hasSubscriptions());
        RxUtils.unsubscribeIfNotNull(this.subscriptions);
    }

    protected void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            final String title = getString(R.string.app_name);
            ab.setTitle(null);
            ab.setDisplayShowHomeEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
        }
        if (mToolbar != null) {
            mToolbar.setTitle(null);
        }
    }

    /**
     * Restores the previously saved and active fragment from the savedInstanceState bundle.
     */
    @Override protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        Log.d("onRestoreInstanceState").v(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
        BundleWrapper instanceStateWrapper = BundleWrapper.wrap(savedInstanceState);
        Optional<TaggedBaseFragment> defaultFragment =
              getDefaultFragmentInstance(instanceStateWrapper);

        if (defaultFragment.isPresent()) {
            mCurrentFragmentTag = instanceStateWrapper.getString(
                  TaggedBaseFragment.ARG_CURRENT_FRAGMENT_TAG,
                  defaultFragment.get()
                                 .getFragmentTag());
            addOrReplaceFragment(savedInstanceState, defaultFragment.get());
        }
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        final int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        switch (i) {
            case android.R.id.home:
                if (backStackEntryCount >= 1) {
                    onBackPressed();
                }
                return true;
        }
        return false;
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
        if (backStackCount <= 1) {
            super.onBackPressed();
            finish();
        }
        /*if (mIsDetailVisible) {
            hideDetail();
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            super.onBackPressed();
        }*/
    }

    protected boolean shouldGoBack() {
        int stackSize = getSupportFragmentManager().getBackStackEntryCount();
        return stackSize > 1;
    }

    @Override protected void onPause() {
        super.onPause();
        unsubscribe();
    }

    @Override protected void onResume() {
        Log.d("onResume");
        super.onResume();
        resubscribe();
        onBackStackChanged();
    }

    protected void resubscribe() {
        this.subscriptions = ensureSubs();
        subscribeActivity(getOrCreateSubscriber(), null);
    }

    /**
     * Checks if the activity subscriptions is exist and valid if or had been
     * unsubscribed, creates a new {@link CompositeSubscription}
     *
     * @return the checked subscription object.
     */
    protected final CompositeSubscription ensureSubs() {
        subscriptions = RxUtils.getNewCompositeSubIfUnsubscribed(this.subscriptions);
        Log.d("ensureSubs:", "hasSubscribers:",
              this.subscriptions != null && this.subscriptions.hasSubscriptions());
        return subscriptions;
    }

    /**
     * Adds th specified {@link RxBus} event subscriber to this activity  {@link
     * CompositeSubscription}
     *
     * @param subscriber {@link RxBus} event subscriber
     * @param filterFunction the {@link RxBus} event filter function
     */
    protected void subscribeActivity(Subscriber<? super EventBuilder> subscriber,
          @Nullable Func1<EventBuilder, Boolean> filterFunction) {
        Log.d("subscribeActivity: " + this.getClass().getSimpleName(), "filter:", filterFunction);
        if (filterFunction != null) {
            ensureSubs().add(
                  //AppObservable.bindActivity(this, RxBus.getInstance().toObservable
                  // (filterFunction))
                  RxBus.getInstance().toObservable(filterFunction)
                       .onBackpressureBuffer(AppSettings.BACKPRESSURE_CAPACITY)
                       .subscribeOn(Schedulers.io())
                       .observeOn(AndroidSchedulers.mainThread())
                       .subscribe(subscriber)
            );
        } else {
            ensureSubs().add(
                  //AppObservable.bindActivity(this, RxBus.getInstance().toObservable())
                  RxBus.getInstance().toObservable()
                       .onBackpressureBuffer(AppSettings.BACKPRESSURE_CAPACITY)
                       .subscribeOn(Schedulers.io())
                       .observeOn(AndroidSchedulers.mainThread())
                       .subscribe(subscriber)
            );
        }
    }

    /**
     * @return a {@link RxBus} event subscriber lazily.
     */
    @SuppressWarnings("unchecked")
    protected Subscriber<BundleWrapper> getOrCreateSubscriber() {
        final Context thisContext = this;
        if (mEventSubscriber == null || mEventSubscriber.isUnsubscribed()) {
            mEventSubscriber = new RxUtils.SubscriberAdapter<EventBuilder>() {
                @Override public void onCompleted() {
                }

                @Override public void onError(final Throwable e) {
                    Log.e(e);
                    dialogMaker.setMessageRes(R.string.snack_internal_error)
                               .positiveAction(R.string.snackbar_action_ok)
                               .make(thisContext, getSnackbarAnchorView());
                }

                @Override public void onNext(final EventBuilder event) {
                    Item item = event.getItem();
                    Event eventType = event.getEventType();
                    if (eventType.id == Event.EVT_INTERNAL_ERROR
                          || eventType.id == Event.EVT_MESSAGE) {
                        event.logReceived(getClass().getSimpleName()).v(event);
                        String message = event.getStringParam(
                              EventParam.PRM_VALUE, MultiFragmentActivity.this,
                              getString(R.string.snack_internal_error));
                        dialogMaker.setMessage(message)
                                   .positiveAction(R.string.snackbar_action_ok);
                        if (eventType.id == Event.EVT_INTERNAL_ERROR) {
                            Log.e(message);
                        }
                        makeSnackBar(item.id == Item.DIALOG);
                    } else if (item.id == Item.TOOLBAR
                          && event.hasParamWithType(EventParam.PRM_VALUE, String.class)) {
                        event.logReceived(getClass().getSimpleName()).v(event);
                        mToolbar.setTitle(
                              event.getParamAs(EventParam.PRM_VALUE, String.class).get());
                    } else if (eventType.id == Event.EVT_NAVIGATE) {
                        handleNavigation(event);
                    }
                    onAppEvent(event);
                }
            };
        }
        return mEventSubscriber;
    }

    protected void setSystemBarsColor(@ColorInt int systemBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(
                  WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // finally change the color
            window.setStatusBarColor(systemBarColor);
            //window.setNavigationBarColor(systemBarColor);
        }
    }

    @SuppressWarnings("deprecation")
    @Override public void onBackStackChanged() {
        int stackSize = getSupportFragmentManager().getBackStackEntryCount();
        Log.d("onBackStackChanged", stackSize);
        if (stackSize == 0) {
            switchToolbarTheme(R.drawable.ic_menu_24dp, R.drawable.ic_arrow_back_24dp);
            mToolbar.setTitle(null);
            return;
        }
        String fragmentTag =
              getSupportFragmentManager().getBackStackEntryAt(stackSize - 1).getName();
        Log.d("onBackStackChanged", "currentFragment", fragmentTag);
        switchToolbarTheme(R.drawable.ic_menu_24dp, R.drawable.ic_arrow_back_24dp);
        mCurrentFragmentTag = fragmentTag;
        TaggedBaseFragment fragment =
              (TaggedBaseFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (fragment != null) {
            boolean showLogo = fragment.shouldShowLogoOnToolbar();
            WidgetUtils.setVisibilityOf(mAppBarLayout, fragment.shouldShowToolbar());
            if (fragment.shouldShowToolbar()) {
                String toolbarTitle = fragment.getToolbarTitle();
                WidgetUtils.setVisibilityOf(mToolbarLogo, showLogo);
                mToolbar.setTitle(toolbarTitle);
                setToolbarItems(fragment);
            }
        }
    }

    protected void onAppEvent(final EventBuilder event) {

    }

    protected void handleNavigation(final EventBuilder event) {
        Item item = event.getItem();
        switch (item.id) {
            case Item.EXT_INTENT:
                handleExternalIntentEvent(event);
                break;
            default:
        }
    }

    protected void handleExternalIntentEvent(final EventBuilder event) {

    }

    /**
     * Add or replace a fragment to the activity container view, based on it's fragment save.
     *
     * @param savedInstanceState the saved instance of this activity
     * @param fragment to be add or replace.
     */
    @SuppressWarnings("unchecked")
    protected <T extends TaggedBaseFragment> void addOrReplaceFragment(
          @Nullable Bundle savedInstanceState,
          @NonNull T fragment) {
        if (fragment == null) {
            throw new NullPointerException("addOrReplaceFragment fragment");
        }
        String fragmentTag = fragment.getFragmentTag();
        T foundFragment;
        BundleWrapper savedStateWrapper = BundleWrapper.wrap(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TaggedBaseFragment.ARG_CURRENT_FRAGMENT_TAG)) {
                fragmentTag = savedStateWrapper.getString(
                      TaggedBaseFragment.ARG_CURRENT_FRAGMENT_TAG, fragmentTag);
            }
            Log.d("Restoring fragment", fragmentTag, " from savedState:", savedInstanceState);
        }
        foundFragment = (T) getSupportFragmentManager().findFragmentByTag(fragmentTag);
        mCurrentFragmentTag = fragmentTag;
        if (foundFragment == null) {
            Log.d(String.format("Cannot find fragment with tag %s", fragmentTag));
            foundFragment = fragment;
            addFragment(R.id.content_main, foundFragment, fragmentTag);
        } else {
/*            BaseFragment f = (BaseFragment) foundFragment;
            //Copy all arguments from the new instance.
            Bundle copyBundle = ((Fragment) fragment).getArguments();
            if (f.getArguments() != null && copyBundle != null) {
                f.getArgumentBundle().putAll(copyBundle);
            }*/
            replaceFragment(R.id.content_main, foundFragment, fragmentTag);
        }
    }

    protected void addFragment(int containerViewId, TaggedBaseFragment fragment) {
        this.addFragment(containerViewId, fragment, fragment.getFragmentTag());
    }

    @SuppressLint("CommitTransaction")
    private void addFragment(int containerViewId, TaggedBaseFragment fragment, String fragmentTag) {
        Fragment f = (Fragment) fragment;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragment instanceof BaseFragment) {
            ((BaseFragment) fragment)
                  .setCustomTransitionAnimations(ft, getLastXFragmentTagOnStack(1));
        }
        ft.add(containerViewId, (Fragment) fragment, fragmentTag)
          .replace(containerViewId, f, fragmentTag);
        if (shouldAddToBackStack(fragment)) {
            ft.addToBackStack(fragment.getFragmentTag());
        }
        ft.commit();
    }

    protected boolean shouldAddToBackStack(final TaggedBaseFragment fragmentToAdd) {
        final String fragmentTagToAdd = fragmentToAdd.getFragmentTag();
        final int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        String previousFragmentTag = null;
        if (backStackCount == 1) {
            previousFragmentTag = getSupportFragmentManager().getBackStackEntryAt(0).getName();
        }
        if (backStackCount > 1) {
            previousFragmentTag =
                  getSupportFragmentManager().getBackStackEntryAt(backStackCount - 1).getName();
        }
        return previousFragmentTag == null || !previousFragmentTag.equals(fragmentTagToAdd);
    }

    @SuppressLint("CommitTransaction")
    private void replaceFragment(int containerViewId, final TaggedBaseFragment fragment,
          String fragmentTag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragment instanceof BaseFragment) {
            ((BaseFragment) fragment)
                  .setCustomTransitionAnimations(ft, getLastXFragmentTagOnStack(1));
        }
        ft.replace(containerViewId, (Fragment) fragment, fragmentTag);
        if (shouldAddToBackStack(fragment)) {
            ft.addToBackStack(fragment.getFragmentTag());
        }
        ft.commit();
    }

    public void switchToolbarTheme(final @DrawableRes int navIconRes,
          final @DrawableRes int backIconRes) {
        @DrawableRes int navigationIconRes = shouldGoBack() ? backIconRes
                                                            : navIconRes;
        @ColorRes int colorRes = R.color.white;
        final Drawable navDrawable = navigationIconRes == 0
                                     ? null : DrawableHelper
                                           .withContext(this)
                                           .withColorRes(colorRes)
                                           .withDrawable(navigationIconRes)
                                           .tint()
                                           .get();
        //ab.setHomeAsUpIndicator(drawable);
        int color = ContextCompat.getColor(this, colorRes);
        mToolbar.setTitleTextColor(color);
        mToolbar.setSubtitleTextColor(color);
        mToolbar.setNavigationIcon(navDrawable);
        mToolbar.setPopupTheme(R.style.AppPopup);
    }

    public Optional<String> getLastXFragmentTagOnStack(int lastX) {
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackCount > 0) {
            return Optional.fromNullable(getSupportFragmentManager()
                                               .getBackStackEntryAt(
                                                     Math.max(0, backStackCount - lastX)).getName()
            );
        }
        return Optional.absent();
    }

    public void makeSnackBar(final boolean showInAlert) {
        dialogMaker.check();
        if (showInAlert) {
            dialogMaker.makeAlert(this);
        } else {
            dialogMaker.make(this, getSnackbarAnchorView());
        }
    }

    public void makAlertDialog(final AlertDialog alertDialog) {
        alertDialog.show();
    }

    @Nullable
    protected Toolbar toolbar() {
        return mToolbar;
    }

    /**
     * Returns the starter fragment of the {@link MultiFragmentActivity}
     */
    public <T extends TaggedBaseFragment> Optional<T> getDefaultFragmentInstance(
          final BundleWrapper instanceBundleWrapper) {
        return Optional.absent();
    }

    /**
     * @return a Bundle containing all the meta data specified in the android manifest file.
     */
    protected BundleWrapper getMetaData() {
        if (mMainBundle == null) {
            mMainBundle = BundleWrapper.wrap(new Bundle());
        }
        try {
            ComponentName activityCompName = new ComponentName(this, MainActivity.class);
            Bundle metaData =
                  getPackageManager().getActivityInfo(activityCompName,
                                                      PackageManager.GET_META_DATA).metaData;
            if (metaData != null) {
                mMainBundle.putAll(metaData);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("getMetaData: error", e);
        }
        return mMainBundle;
    }

    public Navigator getNavigator() {
        return navigator;
    }

    /**
     * Get the Main Application component for dependency injection.
     *
     * @return {@link ApplicationComponent
     * .ApplicationComponent}
     */
    protected ApplicationComponent getApplicationComponent() {
        return ((NeatierShellApplication) getApplication()).getComponent();
    }

    /**
     * Get an Activity module for dependency injection.
     *
     * @return {@link ActivityModule}
     * .ActivityModule}
     */
    protected ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }

    public DialogMaker dialogMaker() {
        return dialogMaker;
    }

    @Override public View getSnackbarAnchorView() {
        return mCoordinatorLayout;
    }

    public KeyValuePairs<String, Object> getApiParams() {
        return mApiParams;
    }

    public SharedKeyValueStore getSharedKeyValueStore() {
        return sharedKeyValueStore;
    }

    protected void setToolbarItems(final TaggedBaseFragment fragment) {
        List<Integer> actionIds = fragment.getDisplayableToolbarIcons();
        Observable.range(0, mToolbar.getMenu().size())
                  .map(index -> mToolbar.getMenu().getItem(index))
                  .map(menuItem -> menuItem.getItemId())
                  .concatWith(Observable.from(actionIds))
                  .distinct()
                  .subscribe(itemId -> {
                      MenuItem menuItem = mToolbar.getMenu().findItem(itemId);
                      if (menuItem != null) {
                          menuItem.setVisible(actionIds.contains(itemId));
                      }
                  });
    }
}
