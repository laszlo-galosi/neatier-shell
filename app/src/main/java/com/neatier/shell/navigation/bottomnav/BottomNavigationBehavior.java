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

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import com.neatier.shell.R;

/**
 * Created by László Gálosi on 09/11/16
 */
public class BottomNavigationBehavior<V extends View> extends VerticalScrollingBehavior<V> {
    private static final Interpolator INTERPOLATOR = new LinearOutSlowInInterpolator();
    private final int mBottomNavHeight;
    private final int mDefaultOffset;
    private final int mTabLayoutId;
    private boolean isScrollAware = false;
    private boolean isTablet = false;

    private ViewPropertyAnimatorCompat mTranslationAnimator;
    private boolean hidden = false;
    private int mSnackbarHeight = -1;
    private final BottomNavigationWithSnackbar mWithSnackBarImpl =
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
          ? new LollipopBottomNavWithSnackBarImpl() : new PreLollipopBottomNavWithSnackBarImpl();
    private boolean mScrollingEnabled = true;
    private boolean mHideAlongSnackbar = false;

    int[] attrsArray = new int[] {
          android.R.attr.id, android.R.attr.elevation };
    private int mElevation = 8;
    private ViewGroup mTabLayout;

    public static <V extends View> BottomNavigationBehavior<V> from(@NonNull V view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
              .getBehavior();
        if (!(behavior instanceof BottomNavigationBehavior)) {
            throw new IllegalArgumentException(
                  "The view is not associated with BottomNavigationBehavior");
        }
        return (BottomNavigationBehavior<V>) behavior;
    }

    public BottomNavigationBehavior(int bottomNavHeight, int defaultOffset, boolean shy,
          boolean tablet) {
        mBottomNavHeight = bottomNavHeight;
        mDefaultOffset = defaultOffset;
        isScrollAware = shy;
        isTablet = tablet;
        mScrollingEnabled = !isTablet && isScrollAware;
        mTabLayoutId = View.NO_ID;
    }

    public BottomNavigationBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                                                      attrsArray);
        mDefaultOffset = 0;
        mBottomNavHeight = context.getResources()
                                  .getDimensionPixelSize(R.dimen.design_bottom_navigation_height);
        mTabLayoutId = a.getResourceId(0, View.NO_ID);
        mElevation = a.getResourceId(1, (int)
              TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mElevation,
                                        context.getResources().getDisplayMetrics()));
        a.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
        mWithSnackBarImpl.updateSnackbar(parent, dependency, child);
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public void onNestedVerticalOverScroll(CoordinatorLayout coordinatorLayout, V child,
          @ScrollDirection int direction, int currentOverScroll, int totalOverScroll) {
    }

    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, V child, View dependency) {
        updateScrollingForSnackbar(dependency, child, true);
        super.onDependentViewRemoved(parent, child, dependency);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        boolean layoutChild = super.onLayoutChild(parent, child, layoutDirection);
        if (mTabLayout == null && mTabLayoutId != View.NO_ID) {
            mTabLayout = findTabLayout(child);
            elevateNavigationView();
        }

        return layoutChild;
    }

    public boolean isScrollAware() {
        return isScrollAware;
    }

    public BottomNavigationBehavior setScrollAware(final boolean scrollAware) {
        isScrollAware = scrollAware;
        mScrollingEnabled = !isTablet && isScrollAware;
        return this;
    }

    @Nullable
    private ViewGroup findTabLayout(@NonNull View child) {
        if (mTabLayoutId == 0) {
            return (ViewGroup) child;
        }
        ViewGroup tabLayout = (ViewGroup) child.findViewById(mTabLayoutId);
        if (tabLayout == null) {
            tabLayout = (ViewGroup) child;
        }
        return tabLayout;
    }

    private void elevateNavigationView() {
        if (mTabLayout != null) {
            ViewCompat.setElevation(mTabLayout, mElevation);
        }
    }

    private void updateScrollingForSnackbar(View dependency, V child, boolean enabled) {
        if (dependency instanceof Snackbar.SnackbarLayout) {
            mScrollingEnabled = enabled && isScrollAware;
            if (!mHideAlongSnackbar && ViewCompat.getTranslationY(child) < 0) {
                ViewCompat.setTranslationY(child, 0);
                hidden = false;
                mHideAlongSnackbar = true;
            } else if (mHideAlongSnackbar) {
                hidden = true;
                animateOffset(child, -child.getHeight());
            }
        }
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {
        updateScrollingForSnackbar(dependency, child, false);
        return super.onDependentViewChanged(parent, child, dependency);
    }

    @Override
    public void onDirectionNestedPreScroll(CoordinatorLayout coordinatorLayout, V child,
          View target, int dx, int dy, int[] consumed, @ScrollDirection int scrollDirection) {
        handleDirection(child, scrollDirection);
    }

    private void handleDirection(V child, int scrollDirection) {
        if (!mScrollingEnabled) {
            return;
        }
        if (scrollDirection == ScrollDirection.SCROLL_DIRECTION_DOWN && hidden) {
            hidden = false;
            animateOffset(child, -mDefaultOffset);
        } else if (scrollDirection == ScrollDirection.SCROLL_DIRECTION_UP && !hidden) {
            hidden = true;
            animateOffset(child, mBottomNavHeight + mDefaultOffset);
        }
    }

    @Override
    protected boolean onNestedDirectionFling(CoordinatorLayout coordinatorLayout, V child,
          View target, float velocityX, float velocityY, @ScrollDirection int scrollDirection) {
        handleDirection(child, scrollDirection);
        return true;
    }

    private void animateOffset(final V child, final int offset) {
        ensureOrCancelAnimator(child);
        mTranslationAnimator.translationY(offset).start();
    }

    private void ensureOrCancelAnimator(V child) {
        if (mTranslationAnimator == null) {
            mTranslationAnimator = ViewCompat.animate(child);
            mTranslationAnimator.setDuration(300);
            mTranslationAnimator.setInterpolator(INTERPOLATOR);
        } else {
            mTranslationAnimator.cancel();
        }
    }

    public void setHidden(@NonNull V view, boolean hide) {
        if (!hide && hidden) {
            animateOffset(view, -mDefaultOffset);
        } else if (hide && !hidden) {
            animateOffset(view, mBottomNavHeight + mDefaultOffset);
        }
        hidden = hide;
    }

    private interface BottomNavigationWithSnackbar {
        void updateSnackbar(CoordinatorLayout parent, View dependency, View child);
    }

    private class PreLollipopBottomNavWithSnackBarImpl implements BottomNavigationWithSnackbar {
        @Override
        public void updateSnackbar(CoordinatorLayout parent, View dependency, View child) {
            if (dependency instanceof Snackbar.SnackbarLayout) {
                if (mSnackbarHeight == -1) {
                    mSnackbarHeight = dependency.getHeight();
                }

                int targetPadding = (hidden ? 0 : child.getMeasuredHeight());

                int shadow = (int) ViewCompat.getElevation(child);
                ViewGroup.MarginLayoutParams layoutParams =
                      (ViewGroup.MarginLayoutParams) dependency.getLayoutParams();
                layoutParams.bottomMargin = targetPadding - shadow;
                child.bringToFront();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    child.getParent().requestLayout();
                    ((View) child.getParent()).invalidate();
                }
            }
        }
    }

    private class LollipopBottomNavWithSnackBarImpl
          implements BottomNavigationWithSnackbar {

        @Override
        public void updateSnackbar(CoordinatorLayout parent, View dependency, View child) {
            if (dependency instanceof Snackbar.SnackbarLayout) {
                if (mSnackbarHeight == -1) {
                    mSnackbarHeight = dependency.getHeight();
                }
                int targetPadding =
                      mSnackbarHeight + (hidden ? 0 : child.getMeasuredHeight());
                dependency.setPadding(dependency.getPaddingLeft(),
                                      dependency.getPaddingTop(), dependency.getPaddingRight(),
                                      targetPadding
                );
            }
        }
    }
}

