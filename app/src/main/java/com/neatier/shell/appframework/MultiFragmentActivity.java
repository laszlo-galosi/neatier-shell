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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.widget.TextView;
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
import rx.android.app.AppObservable;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import trikita.log.Log;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.neatier.shell.factorysettings.AppSettings.PENDING_CALL_REQUEST_CODE;
import static com.neatier.shell.factorysettings.AppSettings.PREFKEY_PERMISION_DENIED_COUNT;

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
                 ActivityCompat.OnRequestPermissionsResultCallback {

    protected CompositeSubscription subscriptions = new CompositeSubscription();

    protected RxUtils.SubscriberAdapter mEventSubscriber;
    protected BundleWrapper mMainBundle;
    protected String mCurrentFragmentTag;

    protected KeyValuePairs<String, Object> mApiParams;
    protected KeyValuePairs<String, PendingIntent> mRequestedPermissionIntents =
          new KeyValuePairs<>(2);
    protected KeyValuePairs<String, Integer> mRequestedPermissionRequestCodes =
          new KeyValuePairs<>(2);

    protected Toolbar mToolbar;
    protected Unbinder mUnbinder;

    @Inject Navigator navigator;
    @Inject DialogMaker dialogMaker;
    @Inject protected SharedKeyValueStore<String, Object> sharedKeyValueStore;

    @BindView(R.id.mainLayout) protected CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.main_appbar) protected AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar_title) protected TextView mToolbarTitleAndLogo;

    @Override protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppSettings.REQUEST_GOOGLE_PLAY_SERVICES) {
            EventBuilder.withItemAndType(Item.PUSH_NOTIFICATION, Event.EVT_RESULT)
                        .addParam(EventParam.PRM_VALUE, resultCode)
                        .send();
        } else {
            findPermmissionForRequestCode(requestCode).subscribe(foundPermission -> {
                EventBuilder resultEvent =
                      EventBuilder.withItemAndType(Item.EXT_INTENT, Event.EVT_RESULT)
                                  .addParam(EventParam.PRM_REQUEST_CODE, requestCode)
                                  .addParam(EventParam.PRM_RESULT_CODE, resultCode);
                if (data != null) {
                    resultEvent.addParam(EventParam.PRM_VALUE, data).send();
                }
                resultEvent.send();
                mRequestedPermissionRequestCodes.remove(foundPermission);
            });
            super.onActivityResult(requestCode, resultCode, data);
        }
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
        final String title = getString(R.string.app_name);
        ab.setTitle(null);
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
        mToolbar.setTitle(null);
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
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        if (shouldGoBack()) {
            mCurrentFragmentTag =
                  getSupportFragmentManager().getBackStackEntryAt(backStackCount - 1).getName();
            super.onBackPressed();
        }
        if (backStackCount == 1) {
            super.onBackPressed();
            finish();
        }
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
                  AppObservable.bindActivity(this, RxBus.getInstance().toObservable(filterFunction))
                               .subscribe(subscriber));
        } else {
            ensureSubs().add(
                  AppObservable.bindActivity(this, RxBus.getInstance().toObservable())
                               .subscribe(subscriber));
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
                               .make(thisContext, mCoordinatorLayout);
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

    protected void onAppEvent(final EventBuilder event) {

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
            switchToolbarTheme(R.drawable.ic_menu_24dp);
            mToolbar.setTitle(null);
            return;
        }
        switchToolbarTheme(R.drawable.ic_menu_24dp);
        String fragmentTag =
              getSupportFragmentManager().getBackStackEntryAt(stackSize - 1).getName();
        Log.d("onBackStackChanged", "currentFragment", fragmentTag);
        mCurrentFragmentTag = fragmentTag;
        TaggedBaseFragment fragment =
              (TaggedBaseFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (fragment != null) {
            boolean showLogo = fragment.shouldShowLogoOnToolbar();
            String toolbarTitle = fragment.getToolbarTitle();
            if (!showLogo && TextUtils.isEmpty(toolbarTitle)) {
                mToolbar.setTitle(toolbarTitle);
            }
            WidgetUtils.setTextAndVisibilityOf(mToolbarTitleAndLogo, toolbarTitle, showLogo);
            setToolbarItems(fragment);
        }
    }

    protected void handleNavigation(final EventBuilder event) {
        Item item = event.getItem();
        if (item.id == Item.EXT_INTENT) {
            Optional<String> actionOpt = event.getParamAs(EventParam.PRM_ACTION, String.class);
            Optional<String> urlOpt = event.getParamAs(EventParam.PRM_ITEM_URL, String.class);
            Optional<String> contentOpt = event.getParamAs(EventParam.PRM_ITEM_TEXT, String.class);
            Optional<String> typeOpt = event.getParamAs(EventParam.PRM_ITEM_TYPE, String.class);
            Optional<Parcelable> extraOpt =
                  event.getParamAs(EventParam.PRM_INTENT_EXTRAS, Parcelable.class);
            if (actionOpt.isPresent()) {
                String intentAction =
                      event.getCheckedParam(EventParam.PRM_ACTION, String.class).get();
                if (urlOpt.isPresent() && actionOpt.get().equals(WebViewFragment.TAG)) {
                    addOrReplaceFragment(null, WebViewFragment.newInstance(
                          getMetaData().put(EventParam.itemUrl().name, urlOpt.get())));
                    return;
                }
                Intent intent = new Intent(intentAction);
                if (urlOpt.isPresent()) {
                    Uri dataUri = Uri.parse(urlOpt.get());
                    if (typeOpt.isPresent()) {
                        intent.setDataAndType(dataUri, typeOpt.get());
                    } else {
                        intent.setData(dataUri);
                    }
                }
                if (contentOpt.isPresent()) {
                    if (typeOpt.isPresent()) {
                        intent.setType(typeOpt.get());
                    }
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, contentOpt.get());
                }
                if (extraOpt.isPresent()) {
                    intent.putExtras(((BundleWrapper) extraOpt.get()).getBundle());
                }
                if (intent.resolveActivity(getPackageManager()) != null) {
                    Optional<Integer> requestCodeOpt =
                          event.getParamAs(EventParam.PRM_REQUEST_CODE, Integer.class);
                    if (requestCodeOpt.isPresent()) {
                        int requestPermCode = requestCodeOpt.get();
                        String permission;
                        switch (requestPermCode) {
                            case AppSettings.PENDING_CALL_REQUEST_CODE:
                            default:
                                permission = Manifest.permission.CALL_PHONE;
                        }
                        if (Build.VERSION.SDK_INT >= 21) {
                            grantMePermission(intent, permission, requestPermCode);
                        } else {
                            startActivityForResult(intent, requestPermCode);
                        }
                    } else {
                        startActivity(intent);
                    }
                    overridePendingTransition(R.anim.enter_from_right,
                                              R.anim.exit_to_left);
                }
            }
        }
    }

    @Override public void onRequestPermissionsResult(final int requestCode,
          @NonNull final String[] permissions,
          @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Checks whether how many times the user denied the permission so we should not bother
        //again with an alert dialog.
        for (int i = 0; i < permissions.length; i++) {
            Log.d("onRequestPermissionsResult", permissions[i], grantResults[i]);
            String denialPrefKey = String.format("%s_%s", PREFKEY_PERMISION_DENIED_COUNT,
                                                 permissions[i]);
            final int deniedCount =
                  sharedKeyValueStore.getAsOrDefault(denialPrefKey, Integer.class, 0);
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                //String message = getPermissionExplanation(permissions[i], deniedCount);
                explainPermissionRequest(permissions[i]);
            } else {
                //Remove denialCount from preferences to restart the permission request flow
                //if the user revoke permissions later.
                sharedKeyValueStore.remove(denialPrefKey).commit();
                //Check if there is any pending intent for the requested permission.
                Log.d("Checking pendintents for", permissions[i]);
                PendingIntent pendingIntent = mRequestedPermissionIntents.get(permissions[i]);
                int onGrantRequestCode =
                      mRequestedPermissionRequestCodes.getOrDefault(permissions[i],
                                                                    PENDING_CALL_REQUEST_CODE);
                if (pendingIntent != null) {
                    try {
                        pendingIntent.send(onGrantRequestCode);
                    } catch (PendingIntent.CanceledException e) {
                        Log.e("Pending intent cancelled", pendingIntent, e);
                    }
                }
            }
        }
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

    public void switchToolbarTheme(final @DrawableRes int drawableRes) {
        @DrawableRes int navigationIconRes = shouldGoBack() ? R.drawable.ic_arrow_back_24dp
                                                            : drawableRes;
        @ColorRes int colorRes = R.color.white;
        final Drawable navDrawable = DrawableHelper
              .withContext(MultiFragmentActivity.this)
              .withColorRes(colorRes)
              .withDrawable(navigationIconRes)
              .tint()
              .get();
        //ab.setHomeAsUpIndicator(drawable);
        mToolbar.setTitleTextColor(ContextCompat.getColor(this, colorRes));
        mToolbarTitleAndLogo.setTextColor(ContextCompat.getColor(this, colorRes));
        mToolbarTitleAndLogo.setCompoundDrawablesWithIntrinsicBounds(
              DrawableHelper.withContext(this)
                            .withColorRes(colorRes)
                            .withDrawable(R.drawable.ic_xbox_24dp).tint().get(),
              null, null, null
        );
        mToolbar.setSubtitleTextColor(ContextCompat.getColor(this, colorRes));
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

    public void navigateToUrl(final String url) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        } catch (ActivityNotFoundException e) {
            Log.e("navigateToUrl", "Open browser failed", url, e);
        }
    }

    public void makeSnackBar(final boolean showInAlert) {
        dialogMaker.check();
        if (showInAlert) {
            dialogMaker.makeAlert(this);
        } else {
            dialogMaker.make(this, mCoordinatorLayout);
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

    public void grantMePermission(final Intent intentToStart, final String permission,
          int requestCodeForResult) {

        String denialPrefKey = String.format("%s_%s", PREFKEY_PERMISION_DENIED_COUNT,
                                             permission);
        final int deniedCount =
              sharedKeyValueStore.getAsOrDefault(denialPrefKey, Integer.class, Integer.valueOf(0));
        Log.d("requestPermission", permission, denialPrefKey, deniedCount);
        mRequestedPermissionRequestCodes.put(permission, requestCodeForResult);
        if (ActivityCompat.checkSelfPermission(this, permission)
              != PackageManager.PERMISSION_GRANTED) {
            ;
            mRequestedPermissionIntents.put(
                  permission, PendingIntent.getActivity(this, requestCodeForResult,
                                                        intentToStart,
                                                        PendingIntent.FLAG_CANCEL_CURRENT)
            );
            boolean showRationale = shouldExplainPermissionRequest(permission, deniedCount);
            if (deniedCount == 0) {
                ActivityCompat.requestPermissions(
                      MultiFragmentActivity.this, new String[] { permission },
                      PackageManager.PERMISSION_GRANTED);
            } else {
                explainPermissionRequest(permission);
            }
            //We store the intent in a pendingIntent
            // Explicit intent to wrap
            // Create pending intent and wrap our intent
            return;
        }
        try {
            mRequestedPermissionRequestCodes.put(permission, requestCodeForResult);
            startActivityForResult(intentToStart, requestCodeForResult);
        } catch (final Exception ex) {
            Log.e(ex);
        }
    }

    protected Observable<String> findPermmissionForRequestCode(int requestCode) {
        return mRequestedPermissionRequestCodes.keysAsStream()
                                               .filter(key -> mRequestedPermissionRequestCodes.get(
                                                     key) == requestCode);
    }

    private boolean shouldExplainPermissionRequest(final String permission, int denialCount) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
              && denialCount < AppSettings.MAX_PERMISSION_DENIED_LIMIT) {
            return shouldShowRequestPermissionRationale(permission);
        }
        return denialCount < AppSettings.MAX_PERMISSION_DENIED_LIMIT;
    }

    /**
     * Displays a snackbar or an alert message based on the user previous denial count for the
     * specified
     * permission, and requestPermissions if the denialCount <= {@link
     * AppSettings#MAX_PERMISSION_DENIED_LIMIT}
     *
     * @param permission permission to request.
     */
    private void explainPermissionRequest(final String permission) {
        final String denialPrefKey =
              String.format("%s_%s", PREFKEY_PERMISION_DENIED_COUNT, permission);
        final int deniedCount =
              sharedKeyValueStore.getAsOrDefault(denialPrefKey, Integer.class, 0);
        boolean showPermissionRationale = shouldExplainPermissionRequest(permission, deniedCount);
        String message = getPermissionExplanation(permission, deniedCount);
        dialogMaker.title(R.string.alert_title_request_permission)
                   .setMessage(message)
                   .positiveAction(
                         R.string.snackbar_action_next);
        if (showPermissionRationale) {
            dialogMaker.negativeAction(R.string.snackbar_action_defer)
                       .positiveActionClicked(v -> ActivityCompat.requestPermissions(
                             MultiFragmentActivity.this, new String[] { permission },
                             PackageManager.PERMISSION_GRANTED))
                       .negativeActionClicked(new View.OnClickListener() {
                           @Override public void onClick(final View v) {
                               //If the user denies increment and store the denial count.
                               sharedKeyValueStore.putInt(denialPrefKey, deniedCount + 1)
                                                  .commit();
                           }
                       });
            dialogMaker.makeAlert(this);
        } else {
            //If the denial count is reached the limit, simply show a snackbar warning, on positive
            //action navigates to the application settings screen.
            final ComponentName activityCompName =
                  new ComponentName(MultiFragmentActivity.this, MainActivity.class);
            final String packageName = activityCompName.getPackageName();
            dialogMaker.positiveAction(R.string.snackbar_action_show_settings)
                       .positiveActionClicked(v -> {
                           try {
                               //Open the specific App Info page:
                               Intent intent = new Intent(
                                     android.provider.Settings
                                           .ACTION_APPLICATION_DETAILS_SETTINGS);
                               intent.setData(Uri.parse("package:" + packageName));
                               startActivity(intent);
                           } catch (ActivityNotFoundException e) {
                               Log.w("Cannot open application settings", packageName,
                                     activityCompName);
                               //Open the generic Apps page:
                               Intent intent = new Intent(
                                     android.provider.Settings
                                           .ACTION_MANAGE_APPLICATIONS_SETTINGS);
                               startActivity(intent);
                           }
                       })
                       .make(this, mCoordinatorLayout);
        }
    }

    @NonNull
    private String getPermissionExplanation(final String permission, final int deniedCount) {
        String message = "";
        boolean showRationale = shouldExplainPermissionRequest(permission, deniedCount);
        if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            message = getString(showRationale
                                ? R.string.message_read_ext_storage_permission_required
                                : R.string.message_read_ext_storage_permission_not_granted);
        }
        return message;
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

    private View getSnackbarAnchorView() {
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
