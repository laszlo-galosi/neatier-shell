/*
 * Copyright (C) 2016 Extremenet Ltd., All Rights Reserved
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
package com.neatier.shell.appframework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.fernandocejas.arrow.collections.Lists;
import com.fernandocejas.arrow.optional.Optional;
import com.neatier.commons.helpers.BundleWrapper;
import com.neatier.commons.helpers.KeyValuePairs;
import com.neatier.commons.helpers.Leakable;
import com.neatier.commons.helpers.RxUtils;
import com.neatier.shell.R;
import com.neatier.shell.activities.MainActivity;
import com.neatier.shell.appframework.helpers.DialogMaker;
import com.neatier.shell.eventbus.Event;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.eventbus.EventParam;
import com.neatier.shell.eventbus.Item;
import com.neatier.shell.eventbus.RxBus;
import com.neatier.shell.exception.ErrorMessageFactory;
import com.neatier.shell.exception.RxLogger;
import com.neatier.shell.factorysettings.AppSettings;
import com.neatier.shell.internal.di.HasComponent;
import java.util.Arrays;
import java.util.List;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import trikita.log.Log;

/**
 * Created by László Gálosi on 27/03/16
 */
@SuppressWarnings("ConstantConditions")
public abstract class BaseFragment extends Fragment implements AppMvp.LongTaskBaseView, Leakable {

    protected Subscriber<EventBuilder> mEventSubscriber;
    protected Func1<EventBuilder, Boolean> mEventFilter;
    protected CompositeSubscription subscription;
    protected View mProgressLayout;
    protected ProgressBar mProgressView;
    protected Unbinder mUnbinder;
    protected boolean mWaitingForModelUpdate;
    protected boolean mLayoutReady;
    protected boolean mInitWithError;
    protected boolean mWaitingForDestroy;
    protected BroadcastReceiver mBroadcastEventReceiver;
    private IntentFilter mEventIntentFilter;

    @CallSuper
    @Override public void onCreate(Bundle savedInstanceState) {
        Log.v(getFragmentTag(), "onCreate")
           .v("savedState:", savedInstanceState);
        super.onCreate(savedInstanceState);
        setRetainInstance(shouldRetainInstance());
    }

    protected void onLocalBroadcastReceived(final Intent intent) {
        int requestCode = intent.getExtras().getInt(EventParam.requestCode().name);
        int resultCode = intent.getExtras().getInt(EventParam.resultCode().name);
        onActivityResult(requestCode, resultCode, intent);
    }

    protected IntentFilter getLocalBroadcastIntentFilter() {
        return new IntentFilter(AppSettings.ACTION_REQUIRE_PERMISSION);
    }

    protected boolean shouldRetainInstance() {
        return true;
    }

    protected String[] argumentsToRetain() {
        return new String[] {};
    }

    @CallSuper @Nullable
    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
          final Bundle savedInstanceState) {
        Log.d(getFragmentTag(), "onCreateView")
           .v("savedState:", savedInstanceState);
        View fragmentView = inflater.inflate(getContentLayout(), container, false);
        onInflateLayout(fragmentView, savedInstanceState);
        return fragmentView;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_toolbar, menu);
        ((MainActivity) getActivity()).setToolbarItems((TaggedBaseFragment) this);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.action_settings:
            default:
                EventBuilder.withItemAndType(Item.NAV_MENU_ITEM, Event.EVT_NAVIGATE)
                            .addParam(EventParam.PRM_ITEM_ID, item.getItemId())
                            .send();
        }
        return true;
    }

    /**
     * Called right after the fragment finished inflating the layout which specified with the
     * {#getContentLayout}. Used to initialize UI elements. Use to create member initialization
     *
     * @param contentView the inflated view
     * @param savedInstanceState - If non-null, this fragment is being re-constructed from a
     * previous saved state as given here.
     */
    @CallSuper
    protected void onInflateLayout(final View contentView, final Bundle savedInstanceState) {
        Log.v(getFragmentTag(), "onInflateLayout").v("savedState:", savedInstanceState);
        mUnbinder = ButterKnife.bind(this, contentView);
        if (hasProgressView()) {
            bindProgressView(contentView);
        }
        setupBottomNavigation(contentView, savedInstanceState);
    }

    protected abstract @LayoutRes int getContentLayout();

    @CallSuper
    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        Log.v(getFragmentTag(), "onViewCreated")
           .v("savedState:", savedInstanceState);
        super.onViewCreated(view, savedInstanceState);
    }

    @CallSuper
    @Override public void onActivityCreated(final Bundle savedInstanceState) {
        Log.v(getFragmentTag(), "onActivityCreated")
           .v("savedState:", savedInstanceState)
           .v("arguments:", getArguments());
        super.onActivityCreated(savedInstanceState);
    }

    @CallSuper
    @Override public void onViewStateRestored(@Nullable final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.v(getFragmentTag(), "onViewStateRestored")
           .v("outState", savedInstanceState).v("arguments", getArguments());
    }

    @CallSuper
    protected void initialize() {
        Log.v(getFragmentTag(), ".initialize");
    }

    @CallSuper
    @Override public void onResume() {
        Log.d(getFragmentTag(), "onResume");
        loadStateArguments();
        resubscribe();
        super.onResume();
    }

    @CallSuper
    @Override public void onSaveInstanceState(final Bundle outState) {
        Log.v(getFragmentTag(), "onSaveInstanceState")
           .v("outState", outState).v("arguments", getArguments());
        super.onSaveInstanceState(outState);
    }

    @CallSuper
    @Override public void onPause() {
        Log.d(getFragmentTag(), "onPause");
        unsubscribe();
        saveStateArguments();
        super.onPause();
        hideSoftKeyboard(getView());
    }

    @CallSuper
    @Override public void onDestroyView() {
        Log.d(getFragmentTag(), "onDestroyView");
        mWaitingForDestroy = true;
        hideSoftKeyboard(getView());
        unsubscribe();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        LocalBroadcastManager.getInstance(getContext())
                             .unregisterReceiver(mBroadcastEventReceiver);
        mBroadcastEventReceiver = null;
        mEventIntentFilter = null;
        super.onDestroyView();
    }

    @Override public void onDestroy() {
        Log.d(getFragmentTag(), "onDestroy");
        mWaitingForDestroy = true;
        clearLeakables();
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext())
                             .unregisterReceiver(mBroadcastEventReceiver);
        mWaitingForDestroy = false;
    }

    @Override public void clearLeakables() {
        Log.v(getFragmentTag(), "clearLeakables");
    }

    /**
     * Unsubscribe all subscriptions from this fragment {@link CompositeSubscription}.
     */
    protected void unsubscribe() {
        Log.v(getFragmentTag(), "unsubscribe");
        unsubscribe(this.subscription);
    }

    /**
     * Unsubscribe the specified {@link Subscription} from the {@link RxBus}.
     */
    protected void unsubscribe(Subscription sub) {
        //subscription.remove(sub);
        RxUtils.unsubscribeIfNotNull(sub);
    }

    /**
     * Resubscribing to {@link RxBus} events.
     */
    @SuppressWarnings("unchecked")
    protected void resubscribe() {
        Log.v(getFragmentTag(), "resubscribe", this.subscription);
        ensureSubs().add(
              /*AndroidObservable.bindFragment(this, RxBus.getInstance()
                                                       .toObservable(getEventFilter()))*/
              RxBus.getInstance().toObservable(getEventFilter())
                   .onBackpressureBuffer(AppSettings.BACKPRESSURE_CAPACITY)
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe((Observer<? super EventBuilder>) getOrCreateEventSubscriber()));
    }

    /**
     * @return a new {@link CompositeSubscription} object if not existing, or had been unsubscribed.
     */
    public CompositeSubscription ensureSubs() {
        subscription = RxUtils.getNewCompositeSubIfUnsubscribed(this.subscription);
        return subscription;
    }

    /**
     * Lazily creates an {@link RxBus} event filter
     * Override this in extended fragment.
     *
     * @return a filter function to filter {@link RxBus} events.
     */
    public Func1<EventBuilder, Boolean> getEventFilter() {
        if (mEventFilter == null) {
            mEventFilter = event -> filterEvent(event);
        }
        return mEventFilter;
    }

    protected <T extends BundleWrapper> Boolean filterEvent(final T event) {
        return Boolean.FALSE;
    }

    /**
     * Lazily created an {@link RxBus} event subscriber for this fragment.
     *
     * @return an {@link RxBus} event subscriber.
     */
    private Subscriber<? extends BundleWrapper> getOrCreateEventSubscriber() {
        if (mEventSubscriber == null || mEventSubscriber.isUnsubscribed()) {
            mEventSubscriber = new Subscriber<EventBuilder>() {
                @Override public void onCompleted() {
                }

                @Override public void onError(final Throwable e) {
                    onAppEventError(e);
                }

                @Override public void onNext(final EventBuilder event) {
                    onAppEvent(event);
                }
            };
        }
        return mEventSubscriber;
    }

    /**
     * Override this function for custom {@link RxBus} error handling.
     */
    protected void onAppEventError(final Throwable throwable) {
        RxLogger.logRxError().call(throwable);
        RxBus.getInstance().send(
              EventBuilder.withItemAndType(Item.SNACKBAR, Event.EVT_INTERNAL_ERROR)
        );
    }

    /**
     * {@link RxBus} event handling.
     */
    public void onAppEvent(EventBuilder event) {
        event.logReceived(getFragmentTag(), "onAppEvent").v(event);
    }

    /**
     * @return the custom fragment tag of the implementing {@link BaseFragment}
     */
    public abstract String getFragmentTag();

    /**
     * Gets a component for dependency injection by its type.
     */
    @SuppressWarnings("unchecked")
    protected <C> C getComponent(Class<C> componentType) {
        return componentType.cast(((HasComponent<C>) getActivity()).getComponent());
    }

    /**
     * Loads all the previously saved argument of this fragment.
     */
    protected void loadStateArguments() {
        Log.v(getFragmentTag(), "loadStateArguments").v(getArguments());
    }

    /**
     * Stores all important argument to restore from a savedInstanceState {@link Bundle} later.
     */
    protected void saveStateArguments() {
        Log.v(getFragmentTag(), "saveStateArguments").v(getArguments());
    }

    /**
     * @return te custom toolbar title string of the implementing {@link BaseFragment}.
     */
    public String getToolbarTitle() {
        return "";
    }

    public boolean shouldGoBack() {
        return false;
    }

    public abstract boolean hasProgressView();

    public boolean hasBottomNavigationBar() {
        return true;
    }

    protected void bindProgressView(View fragmentView) {
        Log.v(getFragmentTag(), "bindProgressView");
        mProgressLayout = fragmentView.findViewById(R.id.pv_container);
        mProgressView = (ProgressBar) fragmentView.findViewById(R.id.pv_progress);
    }

    @Override public void showProgress() {
        if (!hasProgressView()) {
            return;
        }
        Log.v(getFragmentTag(), "showProgress");
        this.mProgressLayout.setVisibility(View.VISIBLE);
    }

    @Override public void hideProgress() {
        if (!hasProgressView() || mProgressLayout == null) {
            return;
        }
        Log.v(getFragmentTag(), "hideProgress");
        if (mProgressLayout.getVisibility() != View.GONE) {
            this.mProgressLayout.setVisibility(View.GONE);
        }
    }

    public boolean shouldShowLogoOnToolbar() {
        return true;
    }

    public List<Integer> getDisplayableToolbarIcons() {
        return Lists.newArrayList(android.R.id.home, R.id.action_settings);
    }

    @Override public void showError(final ErrorMessageFactory.Error error) {
        StringBuilder message = new StringBuilder();
        if (error.getMessageRes() > 0) {
            message.append(getString(error.getMessageRes()));
        }
        if (error.getRawMessage() != null) {
            message.append(error.getRawMessage());
        }
        DialogMaker.getInstance()
                   .setMessage(message.toString())
                   .positiveAction(R.string.snackbar_action_ok);
        ((MainActivity) getActivity()).makeSnackBar(error.showAsAlert());
    }

    @Override public KeyValuePairs<String, Object> getApiParams() {
        return ((MainActivity) getActivity()).getApiParams();
    }

    @Override public boolean isResultFromCloud() {
        return true;
    }

    @Override public void onUpdateStarted() {
        Log.v(getFragmentTag(), "onUpdateStarted");
        mWaitingForModelUpdate = true;
        if (hasProgressView()) {
            showProgress();
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override public void onUpdateFinished(Throwable... errors) {
        Log.v(getFragmentTag(), "onUpdateFinished", Arrays.asList(errors));
        if (hasProgressView()) {
            hideProgress();
        }
        if (errors.length > 0) {
            mInitWithError = true;
            mWaitingForModelUpdate = false;
        } else {
            mWaitingForModelUpdate = false;
        }
    }

    @Override public BundleWrapper getArgumentBundle() {
        return BundleWrapper.wrap(getArguments());
    }

    public void hideSoftKeyboard(View view) {
        if (getActivity().getCurrentFocus() != null && view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                  Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected @Nullable Optional<String> getMessage(final EventBuilder event) {
        Optional<String> paramAsString = Optional.absent();
        if (event.hasParamWithType(EventParam.PRM_VALUE, String.class)) {
            paramAsString = event.getParamAs(EventParam.PRM_VALUE, String.class);
        } else if (event.hasParamWithType(EventParam.PRM_VALUE, Integer.class)) {
            paramAsString = Optional.of(
                  getString(event.getParamAs(EventParam.PRM_VALUE, Integer.class).get()));
        }
        event.addParam(EventParam.PRM_VALUE, paramAsString.get());
        return paramAsString;
    }

    public void setCustomTransitionAnimations(FragmentTransaction fragmentTransaction,
          Optional<String> openAtFragmentTag) {
        fragmentTransaction.setCustomAnimations(R.anim.fade_in,
                                                R.anim.fade_out,
                                                R.anim.fade_in,
                                                R.anim.fade_out);
    }

    protected void setupBottomNavigation(View contentView, Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setupBottomNavigationView(contentView, savedInstanceState);
        //((MainActivity) getActivity()).showHideBottomBar(true);
    }

    public boolean shouldShowToolbar() {
        return true;
    }
}
