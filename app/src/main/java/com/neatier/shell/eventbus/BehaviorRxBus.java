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

package com.neatier.shell.eventbus;

import android.content.Context;
import android.support.annotation.Nullable;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

/**
 * Created by László Gálosi on 27/03/16
 */
public class BehaviorRxBus {

    // If multiple threads are going to emit events to this
    // then it must be made thread-safe like this instead
    private final BehaviorSubject<EventBuilder> mBus;

    public static void initConstants(Context context) {
        Item.init(context);
        Event.init(context);
        EventParam.init(context);
    }

    public static BehaviorRxBus getInstance() {
        return SInstanceHolder.sInstance;
    }

    public BehaviorRxBus send(EventBuilder event) {
        event.logMe("[BehaviorRxBus]", "-->", "Sending event").v(event);
        mBus.onNext(event);
        return this;
    }

    public BehaviorRxBus send(@Item.ItemId int itemId, @Event.EventType int eventType) {
        return send(EventBuilder.withItemAndType(itemId, eventType));
    }

    //@RxLogObservable
    public Observable<EventBuilder> toObservable() {
        return mBus;
    }

    public boolean hasObservers() {
        return mBus.hasObservers();
    }

    //@RxLogObservable
    public Observable<EventBuilder> toObservable(
          @Nullable Func1<EventBuilder, Boolean> filterFunction) {
        return mBus.filter(filterFunction);
    }

    private static class SInstanceHolder {
        private static final BehaviorRxBus sInstance = new BehaviorRxBus();
    }

    private BehaviorRxBus() {
        this.mBus = BehaviorSubject.create();
    }
}
