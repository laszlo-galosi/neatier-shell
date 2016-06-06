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

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import com.neatier.commons.exception.DefaultErrorBundle;
import com.neatier.commons.exception.ErrorBundle;
import com.neatier.commons.helpers.Leakable;
import com.neatier.commons.helpers.RxUtils;
import com.neatier.shell.BuildConfig;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.eventbus.RxBus;
import com.neatier.shell.exception.ErrorMessageFactory;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import trikita.log.Log;

/**
 * Created by László Gálosi on 17/08/15
 */
public abstract class BasePresenterImpl implements AppMvp.Presenter, Leakable {

    protected Subscription subscription;
    protected AppMvp.LongTaskBaseView mView;

    private Subscriber<EventBuilder> mAppEventSubscriber;

    protected Func1<EventBuilder, Boolean> mEventFilter;

    public BasePresenterImpl() {
    }

    @CallSuper
    @Override public void resume() {
        Log.d("resume");
        resubscribe();
    }

    @CallSuper
    @Override public void pause() {
        Log.d("pause");
        RxUtils.unsubscribeIfNotNull(this.subscription);
    }

    @CallSuper
    @Override public void destroy() {
        Log.d("destroy");
        clearLeakables();
    }

    //@CallSuper
    @Override public void initialize() {
        Log.d("initialize");
        resubscribe();
    }

    @Override public Subscriber<EventBuilder> getEventSubscriber() {
        return mAppEventSubscriber;
    }

    @CallSuper
    @Override public void clearLeakables() {
        if (mView != null) {
            Log.v("clearLeakables", mView.getClass().getSimpleName());
            mView = null;
        }
        mAppEventSubscriber = null;
        mEventFilter = null;
    }

    @CallSuper
    public void freeUpHeap() {
        Log.d("freeUpHeap");
    }

    /**
     * Abstract method handling {@link RxBus} events
     *
     * @param event tha {@link EventBuilder}
     */
    public abstract void onEvent(EventBuilder event);

    protected void resubscribe() {
        this.subscription =
              RxBus.getInstance()
                   .toObservable(getActionFilter())
                   .onBackpressureBuffer(10000)
                   .subscribe(getOrCreateSubscriber());
    }

    protected void showErrorMessage(ErrorBundle errorBundle) {
        ErrorMessageFactory.Error error =
              ErrorMessageFactory.getInstance().create(errorBundle.getException());
        this.mView.showError(error);
    }

    protected String fromResultString() {
        if (BuildConfig.FLAVOR.startsWith("mock")) {
            return "from Mock";
        }
        return mView.isResultFromCloud() ? "from Cloud" : "from Cache";
    }

    public Func1<EventBuilder, Boolean> getActionFilter() {
        if (mEventFilter == null) {
            mEventFilter = event -> Boolean.FALSE;
        }
        return mEventFilter;
    }

    private Subscriber<EventBuilder> getOrCreateSubscriber() {
        if (mAppEventSubscriber == null || mAppEventSubscriber.isUnsubscribed()) {
            mAppEventSubscriber = new Subscriber<EventBuilder>() {
                @Override public void onCompleted() {
                }

                @Override public void onError(final Throwable e) {
                    showErrorMessage(new DefaultErrorBundle((Exception) e));
                }

                @Override public void onNext(final EventBuilder event) {
                    onEvent(event);
                }
            };
        }
        return mAppEventSubscriber;
    }

    public void setView(@NonNull AppMvp.LongTaskBaseView view) {
        this.mView = view;
    }

    public class PresenterSubscriber<T> extends RxUtils.SubscriberAdapter<T> {

        @CallSuper
        @Override public void onCompleted() {
            if (mView != null) {
                mView.onUpdateFinished();
            }
        }

        @CallSuper
        @Override public void onError(Throwable e) {
            RxUtils.logRxError().call(e);
            if (mView == null) {
                Log.e(e);
                return;
            } else {
                Log.d("onError", mView.getApiParams());
            }
            mView.onUpdateFinished(e);
            ErrorMessageFactory.Error error =
                  ErrorMessageFactory.getInstance().create(e);
            mView.showError(error);
        }

        @Override public void onNext(T event) {
        }
    }
}
