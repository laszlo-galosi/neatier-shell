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

package com.neatier.shell.eventbus;

import android.content.Context;
import android.support.annotation.Nullable;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by László Gálosi on 27/03/16
 */
public class RxBus {

    // If multiple threads are going to emit events to this
    // then it must be made thread-safe like this instead
    private final Subject<EventBuilder, EventBuilder> mBus;

    public static void initConstants(Context context) {
        Item.init(context);
        Event.init(context);
        EventParam.init(context);
    }

    public static RxBus getInstance() {
        return SInstanceHolder.sInstance;
    }

    public RxBus send(EventBuilder event) {
        event.logMe("[RxBus]", "-->", "Sending event").v(event);
        mBus.onNext(event);
        return this;
    }

    public RxBus send(@Item.ItemId int itemId, @Event.EventType int eventType) {
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
        private static final RxBus sInstance = new RxBus();
    }

    private RxBus() {
        this.mBus = new SerializedSubject<>(
              PublishSubject.<EventBuilder>create());
    }
}
