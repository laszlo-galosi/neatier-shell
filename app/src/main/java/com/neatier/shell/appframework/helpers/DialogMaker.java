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

package com.neatier.shell.appframework.helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import com.neatier.commons.helpers.Leakable;
import com.neatier.shell.R;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Builder class fr making message windows and Snackbars.
 * Created by László Gálosi on 20/08/15
 */
@Singleton
public class DialogMaker implements Leakable {

    @StringRes int messageRes = 0;
    @Nullable String message;
    @StringRes int positiveActionRes = 0;
    @StringRes int negativeActionRes = 0;
    @StringRes int titleRes = 0;
    @Nullable
    @ColorRes
    int actionColorRes = R.color.colorPrimary;
    @Nullable View.OnClickListener positiveActionClickListener;
    @Nullable View.OnClickListener negativeActionClickListener;
    int duration = Snackbar.LENGTH_LONG;

    public static View.OnClickListener emptyClickListener = v -> {
        //empty click listener for dismissing snackbar
    };
    private View mCustomView;
    private @LayoutRes int mCustomViewRes = 0;

    public static DialogMaker getInstance() {
        return SInstanceHolder.sInstance;
    }

    @Inject protected DialogMaker() {
    }

    @Override public void clearLeakables() {
        reset();
    }

    public DialogMaker setMessageRes(final int messageRes) {
        this.message = null;
        this.messageRes = messageRes;
        return this;
    }

    private void reset() {
        this.messageRes = 0;
        this.message = null;
        this.titleRes = 0;
        this.actionColorRes = R.color.colorPrimary;
        this.positiveActionRes = R.string.snackbar_action_ok;
        this.negativeActionRes = R.string.snackbar_action_cancel;
        this.positiveActionClickListener = null;
        this.negativeActionClickListener = null;
        this.mCustomView = null;
        this.mCustomViewRes = 0;
    }

    /**
     * Verifies if the singleton instance has been set correctly to be shown.
     */
    public DialogMaker check() {
        if (messageRes == 0 && message == null) {
            throw new IllegalArgumentException("SnackbarMake instance hasn't set.");
        }
        return this;
    }

    public DialogMaker setMessage(@Nullable final String message) {
        reset();
        this.message = message;
        return this;
    }

    public DialogMaker positiveAction(@StringRes final int actionRes) {
        this.positiveActionRes = actionRes;
        return this;
    }

    public DialogMaker negativeAction(@StringRes final int actionRes) {
        this.negativeActionRes = actionRes;
        return this;
    }

    public DialogMaker title(@StringRes final int titleRes) {
        this.titleRes = titleRes;
        return this;
    }

    public DialogMaker setActionColorRes(final int actionColorRes) {
        this.actionColorRes = actionColorRes;
        return this;
    }

    public DialogMaker positiveActionClicked(
          @Nullable final View.OnClickListener clickListener) {
        this.positiveActionClickListener = clickListener;
        return this;
    }

    public DialogMaker negativeActionClicked(
          @Nullable final View.OnClickListener clickListener) {
        this.negativeActionClickListener = clickListener;
        return this;
    }

    public DialogMaker setDuration(final int duration) {
        this.duration = duration;
        return this;
    }

    public DialogMaker setCustomView(final View customView) {
        mCustomView = customView;
        return this;
    }

    public DialogMaker setCustomViewRes(final int customViewRes) {
        mCustomViewRes = customViewRes;
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    public void make(Context context, View parentView) {
        Snackbar snackbar;
        if (messageRes > 0) {
            snackbar = Snackbar.make(parentView, context.getString(messageRes), duration);
        } else {
            snackbar = Snackbar.make(parentView, message, duration);
        }
        if (positiveActionRes != 0) {
            snackbar.setAction(positiveActionRes, positiveActionClickListener == null
                                                  ? emptyClickListener
                                                  : positiveActionClickListener);
        }
        if (actionColorRes != 0) {
            @ColorInt int actionColor = context.getResources().getColor(actionColorRes);
            snackbar.setActionTextColor(actionColor);
        }
        snackbar.show();
    }

    public void makeAlert(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        if (messageRes > 0) {
            builder.setMessage(context.getString(messageRes));
        } else {
            builder.setMessage(message);
        }
        final String posActionText = context.getString(
              positiveActionRes == 0 ? R.string.snackbar_action_ok : positiveActionRes)
                                            .toUpperCase();
        String negActionText = context.getString(
              negativeActionRes == 0 ? R.string.snackbar_action_ok : negativeActionRes)
                                      .toUpperCase();

        DialogInterface.OnClickListener dialogClickListener =
              new DialogInterface.OnClickListener() {
                  @Override public void onClick(final DialogInterface dialog, final int which) {
                      if (which == DialogInterface.BUTTON_POSITIVE) {
                          if (positiveActionClickListener != null) {
                              positiveActionClickListener.onClick(null);
                          }
                      } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                          if (negativeActionClickListener != null) {
                              negativeActionClickListener.onClick(null);
                          }
                      }
                      reset();
                  }
              };

        builder.setTitle(context.getString(titleRes == 0 ? R.string.alert_title : titleRes))
               .setPositiveButton(posActionText, dialogClickListener);
        if (negativeActionClickListener != null) {
            builder.setNegativeButton(negActionText, dialogClickListener);
        }
        if (mCustomViewRes > 0) {
            builder.setView(mCustomViewRes);
        }
        if (mCustomView != null) {
            builder.setView(mCustomView);
        }
        builder.show();
    }

    private static class SInstanceHolder {
        private static final DialogMaker sInstance = new DialogMaker();
    }
}
