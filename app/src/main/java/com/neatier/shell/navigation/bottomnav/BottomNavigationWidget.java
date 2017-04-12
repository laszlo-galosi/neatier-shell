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
package com.neatier.shell.navigation.bottomnav;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationMenu;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.ViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.neatier.commons.helpers.BundleWrapper;
import com.neatier.commons.helpers.KeyValuePairs;
import com.neatier.commons.helpers.Preconditions;
import com.neatier.shell.R;
import com.neatier.shell.eventbus.Event;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.eventbus.EventParam;
import com.neatier.shell.eventbus.Item;
import com.neatier.shell.exception.ErrorMessageFactory;
import com.neatier.shell.navigation.MvpNavigationView;
import com.neatier.widgets.ThemeUtil;
import com.neatier.widgets.helpers.WidgetUtils;
import java.util.List;
import rx.Observable;
import trikita.log.Log;

/**
 * Bottom navigation bar component, using {@link BottomNavigationView} with improvement
 * which fixes the lacking features describe in Material Design guidelines.
 * <ul>
 * <li>BottomNavigationPresenter does not have stated guideline elevation of 8dp, out of the
 * box.</li>
 * <li>NavigationView group does not have stated default scrolling behavior</li>
 * <li>NavigationView does not guidelines regarding interaction with Snackbar, where bottom
 * navigation component has to be above (8dp) snackbar (6dp).</li>
 * </ul>
 * Created by László Gálosi on 08/11/16
 */
public class BottomNavigationWidget extends BottomNavigationView implements MvpNavigationView {

    public static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };
    public static final int[] DISABLED_STATE_SET = { -android.R.attr.state_enabled };

    private boolean mInitWithError;
    private boolean mWaitingForModelUpdate;
    private KeyValuePairs<String, Object> mApiParams = new KeyValuePairs<>(5);
    BottomNavigationMenuPresenter mPresenter;
    @MenuRes int mMenuRes;
    private MenuBuilder mMenu;
    private final BottomNavigationMenuView mMenuView;
    private OnNavigationItemSelectedListener mListener;
    private SupportMenuInflater mMenuInflater;
    private boolean isScrollAware;
    private int mShadowElevation;
    private Drawable mShadowDrawable;
    private boolean mShadowVisible = true;
    private ViewGroup mBottomItemsHolder;
    private int mWidth;
    private int mHeight;
    private int mLastSelection;
    private boolean mLayoutReady;

    /**
     * Adds the BottomBar inside of your CoordinatorLayout and shows / hides
     * it according to scroll state changes.
     *
     * @param coordinatorLayout a CoordinatorLayout for the BottomBar to add itself into
     * @param userContentView the view (usually a NestedScrollView) that has your scrolling
     * content.
     * Needed for tablet support.
     * @param savedInstanceState a Bundle for restoring the state on configuration change.
     * @return a BottomBar at the bottom of the screen.
     */
    public static BottomNavigationWidget attach(CoordinatorLayout coordinatorLayout,
          View userContentView, LayoutInflater layoutInflater, Bundle savedInstanceState) {

        final BottomNavigationWidget bottomBar =
              (BottomNavigationWidget) layoutInflater.inflate(R.layout.widget_bottom_nav,
                                                              coordinatorLayout, false);
        boolean shy = userContentView != null && userContentView instanceof NestedScrollingChild;
        bottomBar.setScrollAware(shy);
        //Its crashing onOrientationChange.
        //bottomBar.onRestoreInstanceState(savedInstanceState);
        coordinatorLayout.addView(bottomBar);
        final int bottomBarHeight = bottomBar.getContext().getResources()
                                             .getDimensionPixelSize(
                                                   R.dimen.design_bottom_navigation_height);
        final int defaultOffset = 0;
       /* ((CoordinatorLayout.LayoutParams) bottomBar.getLayoutParams()).setBehavior(
              //new DesignBottomNavigationBehavior()
              new BottomNavigationBehavior(bottomBarHeight, defaultOffset, shy,Ma false)
        );*/
        return bottomBar;
    }

    public BottomNavigationWidget(Context context) {
        this(context, null);
    }

    public BottomNavigationWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomNavigationWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context);
        ThemeUtil.checkAppCompatTheme(context);

        // Create the menu
        mMenu = new BottomNavigationMenu(context);

        mMenuView = new BottomNavigationMenuView(context);
        LayoutParams params = new LayoutParams(
              ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mMenuView.setLayoutParams(params);

        // Custom attributes
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs,
                                                                 android.support.design.R
                                                                       .styleable
                                                                       .BottomNavigationView,
                                                                 defStyleAttr,
                                                                 android.support.design.R.style
                                                                       .Widget_Design_BottomNavigationView);

        if (a.hasValue(android.support.design.R.styleable.BottomNavigationView_itemIconTint)) {
            mMenuView.setIconTintList(
                  a.getColorStateList(
                        android.support.design.R.styleable.BottomNavigationView_itemIconTint));
        } else {
            mMenuView.setIconTintList(
                  createDefaultColorStateList(android.R.attr.textColorSecondary));
        }
        if (a.hasValue(android.support.design.R.styleable.BottomNavigationView_itemTextColor)) {
            mMenuView.setItemTextColor(
                  a.getColorStateList(
                        android.support.design.R.styleable.BottomNavigationView_itemTextColor));
        } else {
            mMenuView.setItemTextColor(
                  createDefaultColorStateList(android.R.attr.textColorSecondary));
        }

        int itemBackground = a.getResourceId(
              android.support.design.R.styleable.BottomNavigationView_itemBackground, 0);
        mMenuView.setItemBackgroundRes(itemBackground);

        if (a.hasValue(android.support.design.R.styleable.BottomNavigationView_menu)) {
            mMenuRes =
                  a.getResourceId(android.support.design.R.styleable.BottomNavigationView_menu, 0);
        }
        a.recycle();

        addView(mMenuView, params);

        mMenu.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                return mListener != null && mListener.onNavigationItemSelected(item);
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) {
            }
        });

        mShadowDrawable = ContextCompat.getDrawable(getContext(), R.drawable.top_shadow);
        if (mShadowDrawable != null) {
            mShadowDrawable.setCallback(this);
        }
        setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        setShadowVisible(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBottomItemsHolder = (ViewGroup) getChildAt(0);
        mShadowElevation = ThemeUtil.dpToPx(getContext(), 2);
        updateShadowBounds();
        //This sucks.
        MarginLayoutParams layoutParams = (MarginLayoutParams) mBottomItemsHolder.getLayoutParams();
        //layoutParams.topMargin = (mShadowElevation + 2) / 2;
    }

    public void toggleVisibility(boolean visible) {
        BottomNavigationBehavior<BottomNavigationWidget> from = null;
        if (isScrollAware) {
            from = BottomNavigationBehavior.from(this);
        }
        if (from != null) {
            from.setHidden(this, !visible);
        } else {
            WidgetUtils.setVisibilityOf(this, visible);
        }
    }

    @Override public void inflateMenu(int resId) {
        getMenuInflater().inflate(resId, mMenu);
    }

    public void setOnNavigationItemSelectedListener(
          @Nullable OnNavigationItemSelectedListener listener) {
        mListener = listener;
        super.setOnNavigationItemSelectedListener(listener);
    }

    public void setPresenter(BottomNavigationMenuPresenter presenter) {
        mPresenter = presenter;
        Preconditions.checkNotNull(mMenu, "Menu hasn't been initialized yet.");
        Preconditions.checkNotNull(mMenuView, "Menu view hasn't been initialized yet.");
        Preconditions.checkArgument(getMenuResource() > 0,
                                    "Menu resource hasn't been initialized yet.");
        onUpdateStarted();
        mPresenter.setView(this);
        inflateMenu(getMenuResource());
        mPresenter.initView(mMenuView, mMenu);
        mMenuView.getViewTreeObserver().addOnGlobalLayoutListener(
              new ViewTreeObserver.OnGlobalLayoutListener() {
                  @Override public void onGlobalLayout() {
                      if (!mWaitingForModelUpdate || mInitWithError) {
                          mMenuView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                          //onModelReady();
                      }
                  }
              });
    }

    @Override public List<MenuItem> getMenuItems() {
        return mPresenter.getMenuItems();
    }

    public @MenuRes int getMenuResource() {
        return mMenuRes;
    }

    @Override public boolean shouldDefaultSelect(final MenuItem menuItem) {
        return mPresenter.getMenuItemFilter().call(menuItem);
    }

    @NonNull
    @Override public Menu getMenu() {
        return mMenu;
    }

    @Override public void showProgress() {
    }

    @Override public void hideProgress() {
    }

    @Override public void showError(final ErrorMessageFactory.Error error) {
        EventBuilder.withItemAndType(Item.SNACKBAR, Event.EVT_MESSAGE)
                    .addParam(EventParam.PRM_VALUE, error.getMessageRes()).send();
    }

    @Override public void onUpdateFinished(final Throwable... errors) {
        Log.v("onUpdateFinished");
        if (errors.length > 0) {
            mInitWithError = true;
        } else {
            mWaitingForModelUpdate = false;
        }
    }

    @Override public void onUpdateStarted() {
        Log.v("onUpdateStarted");
        mWaitingForModelUpdate = true;
    }

    @Override public void onModelReady() {
        Log.v("onModelReady");
        mLayoutReady = true;
        Observable.from(getMenuItems())
                  .filter(mPresenter.getDefaultSelectionFilter())
                  .subscribe(menuItem -> setSelectedItemId(menuItem.getItemId()));
    }

    @Override public boolean isResultFromCloud() {
        return false;
    }

    @Override public KeyValuePairs<String, Object> getApiParams() {
        return mApiParams;
    }

    @Override public BundleWrapper getArgumentBundle() {
        return BundleWrapper.wrap(new Bundle());
    }

    private MenuInflater getMenuInflater() {
        if (mMenuInflater == null) {
            mMenuInflater = new SupportMenuInflater(getContext());
        }
        return mMenuInflater;
    }

    public boolean isScrollAware() {
        return isScrollAware;
    }

    public BottomNavigationWidget setScrollAware(final boolean scrollAware) {
        isScrollAware = scrollAware;
        BottomNavigationBehavior.from(this).setScrollAware(scrollAware);
        return this;
    }

    public int getSelectedItem() {
        return mLastSelection = findSelectedItem();
    }

    @CallSuper
    public void setSelectedItemId(@IdRes int idRes) {
        setSelectedAt(positionById(idRes));
    }

    @CallSuper
    public void setSelectedAt(int position) {
        if (position >= getMenu().size() || position < 0 || mLastSelection == position) {
            return;
        }
        View menuItemView = findMenuItemViewAt(position);
        if (menuItemView == null) {
            return;
        }
        MenuItemImpl itemData = ((MenuView.ItemView) menuItemView).getItemData();
        itemData.setChecked(true);
        boolean previousHapticFeedbackEnabled = menuItemView.isHapticFeedbackEnabled();
        menuItemView.setSoundEffectsEnabled(false);
        menuItemView.setHapticFeedbackEnabled(
              false); //avoid hearing click sounds, disable haptic and restore settings later of
        // that view
        menuItemView.performClick();
        menuItemView.setHapticFeedbackEnabled(previousHapticFeedbackEnabled);
        menuItemView.setSoundEffectsEnabled(true);
        mLastSelection = position;
    }

    private View findMenuItemViewAt(int position) {
        View bottomItem = mMenuView.getChildAt(position);
        if (bottomItem instanceof MenuView.ItemView) {
            return bottomItem;
        }
        return null;
    }

    private @Nullable View findMenuItemViewById(@IdRes int idRes) {
        int position = positionById(idRes);
        if (position >= getMenu().size() || position < 0) {
            return null;
        }
        return mMenuView.getChildAt(position);
    }

    private int positionById(@IdRes int idRes) {
        int itemCount = getMenu().size();
        for (int i = 0; i < itemCount; i++) {
            View bottomItem = mMenuView.getChildAt(i);
            if (bottomItem instanceof MenuView.ItemView) {
                MenuItemImpl itemData = ((MenuView.ItemView) bottomItem).getItemData();
                if (itemData.getItemId() == idRes) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findSelectedItem() {
        int itemCount = getMenu().size();
        for (int i = 0; i < itemCount; i++) {
            View bottomItem = mMenuView.getChildAt(i);
            if (bottomItem instanceof MenuView.ItemView) {
                MenuItemImpl itemData = ((MenuView.ItemView) bottomItem).getItemData();
                if (itemData.isChecked()) {
                    return i;
                }
            }
        }
        return android.support.design.widget.TabLayout.Tab.INVALID_POSITION;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mShadowDrawable != null && mShadowVisible) {
            mShadowDrawable.draw(canvas);
        }
    }

    public void setShadowVisible(boolean shadowVisible) {
        setWillNotDraw(!mShadowVisible);
        updateShadowBounds();
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h + mShadowElevation, oldw, oldh);
        mWidth = w;
        mHeight = h;
        updateShadowBounds();
    }

    private void updateShadowBounds() {
        if (mShadowDrawable != null && mBottomItemsHolder != null) {
            mShadowDrawable.setBounds(0, 0, mWidth, mShadowElevation);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public int getShadowElevation() {
        return mShadowVisible ? mShadowElevation : 0;
    }

    private ColorStateList createDefaultColorStateList(int baseColorThemeAttr) {
        final TypedValue value = new TypedValue();
        if (!getContext().getTheme().resolveAttribute(baseColorThemeAttr, value, true)) {
            return null;
        }
        ColorStateList baseColor = AppCompatResources.getColorStateList(
              getContext(), value.resourceId);
        if (!getContext().getTheme().resolveAttribute(
              android.support.v7.appcompat.R.attr.colorPrimary, value, true)) {
            return null;
        }
        int colorPrimary = value.data;
        int defaultColor = baseColor.getDefaultColor();
        return new ColorStateList(new int[][] {
              DISABLED_STATE_SET,
              CHECKED_STATE_SET,
              EMPTY_STATE_SET
        }, new int[] {
              baseColor.getColorForState(DISABLED_STATE_SET, defaultColor),
              colorPrimary,
              defaultColor
        });
    }

    static class BottomNavigationState extends BaseSavedState {
        public int lastSelection;

        @TargetApi(Build.VERSION_CODES.N)
        public BottomNavigationState(Parcel in, ClassLoader loader) {
            super(in, loader);
            lastSelection = in.readInt();
        }

        public BottomNavigationState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(lastSelection);
        }

        public static final Creator<NavigationView.SavedState> CREATOR
              = ParcelableCompat.newCreator(
              new ParcelableCompatCreatorCallbacks<NavigationView.SavedState>() {
                  @Override
                  public NavigationView.SavedState createFromParcel(Parcel parcel,
                        ClassLoader loader) {
                      return new NavigationView.SavedState(parcel, loader);
                  }

                  @Override
                  public NavigationView.SavedState[] newArray(int size) {
                      return new NavigationView.SavedState[size];
                  }
              });
    }
}
