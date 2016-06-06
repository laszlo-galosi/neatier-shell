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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.fernandocejas.arrow.optional.Optional;
import com.neatier.commons.helpers.BundleWrapper;
import com.neatier.commons.helpers.Preconditions;
import java.util.Locale;
import rx.Observable;
import rx.functions.Func1;
import trikita.log.Log;

/**
 * Created by László Gálosi on 29/04/16
 */
public class EventBuilder extends BundleWrapper {

    public static Item DEFAULT_ITEM = Item.find(Item.UNKNOWN_ITEM).get();
    public static Event DEFAULT_EVENT = Event.find(Event.EVT_UNKNOWN).get();

    private EventBuilder() {
        super();
    }

    private EventBuilder(final Bundle bundle) {
        super(bundle);
    }

    public EventBuilder addParam(@EventParam.EventParamId int paramId, final Object value) {
        put(getParamName(paramId), value);
        return this;
    }

    public EventBuilder put(final String key, final Object value) {
        super.put(key, value);
        return this;
    }

    public void send() {
        RxBus.getInstance().send(this);
    }

    @NonNull
    public static String getParamName(final @EventParam.EventParamId int paramId) {
        Optional<EventParam> eventParam = EventParam.find(paramId);
        Preconditions.checkArgument(eventParam.isPresent(),
                                    String.format(Locale.getDefault(),
                                                  "EventParam with id %d not found.", paramId));
        return eventParam.get().name;
    }

    public Item getItem() {
        return Item.find(getAs(EventKey.ITEM_NAME, Integer.class, Item.UNKNOWN_ITEM)).get();
    }

    public Event getEventType() {
        return Event.find(getAs(EventKey.EVENT_NAME, Integer.class, Event.EVT_UNKNOWN)).get();
    }

    public <T> Optional<T> getParamAs(@EventParam.EventParamId int paramId,
          Class<T> resultClass, T... fallback) {
        return Optional.fromNullable(
              (T) super.getAs(getParamName(paramId), resultClass, fallback));
    }

    public <T> Optional<T> getCheckedParam(@EventParam.EventParamId int paramId,
          final Class<T> clazz,
          T... fallback) {
        Preconditions.checkArgument(
              hasParamWithType(paramId, clazz),
              String.format("Missing event param %s with type of %s",
                            EventParam.find(paramId).get(), clazz.getName())
        );
        return getParamAs(paramId, clazz, fallback);
    }

    public <T> boolean hasParamWithType(@EventParam.EventParamId int paramId,
          final Class<T> clazz) {
        try {
            //noinspection unchecked
            Optional<T> valueOptional =
                  Optional.fromNullable(super.getAs(getParamName(paramId), clazz));
            return valueOptional.isPresent() && valueOptional.get().getClass() == clazz;
        } catch (final Exception ex) {
            Log.e(ex);
            return false;
        }
    }

    public void removeParam(@EventParam.EventParamId int paramId) {
        getBundle().remove(getParamName(paramId));
    }

    public static EventBuilder create() {
        return new EventBuilder();
    }

    public static EventBuilder create(final Bundle bundle) {
        return new EventBuilder(bundle);
    }

    public static EventBuilder withItemAndType(@Item.ItemId int itemId,
          @Event.EventType int eventType) {
        return new EventBuilder()
              .put(EventKey.ITEM_NAME, itemId)
              .put(EventKey.EVENT_NAME, Integer.valueOf(eventType));
    }

    public <T extends BundleWrapper> EventBuilder copyFrom(final T source) {
        getBundle().putAll(source.getBundle());
        return this;
    }

    public Observable<EventParam> getParamsAsStream() {
        return Observable.from(getBundle().keySet())
                         .map(name -> EventParam.find(name))
                         .filter(optional -> optional.isPresent())
                         .map(optional -> optional.get());
    }

    public void filterParams(final Func1<String, Boolean> filterFunction) {
        Observable.from(getBundle().keySet())
                  .filter(filterFunction)
                  .map(name -> EventParam.find(name))
                  .filter(optional -> optional.isPresent())
                  .map(optional -> optional.get())
                  .subscribe(eventParam -> removeParam(eventParam.id));
    }

    public Log logMe(String... args) {
        String message = TextUtils.join(" ", args);
        return Log.d(message, getItem(), getEventType());
    }
}
