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
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.fernandocejas.arrow.optional.Optional;
import com.neatier.commons.helpers.BundleWrapper;
import com.neatier.commons.helpers.Preconditions;
import java.util.Locale;
import rx.Observable;
import rx.functions.Func1;
import trikita.log.Log;

import static com.neatier.widgets.helpers.WidgetUtils.getTextData;

;

/**
 * Created by László Gálosi on 29/04/16
 */
public class EventBuilder extends BundleWrapper {

    public static Item DEFAULT_ITEM = Item.find(Item.UNKNOWN_ITEM).get();
    public static Event DEFAULT_EVENT = Event.find(Event.EVT_UNKNOWN).get();

    public static final Creator<EventBuilder> CREATOR = new Creator<EventBuilder>() {
        @Override
        public EventBuilder createFromParcel(Parcel in) {
            return EventBuilder.create(new BundleWrapper(in).getBundle());
        }

        @Override
        public EventBuilder[] newArray(int size) {
            return new EventBuilder[size];
        }
    };

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

    public static EventBuilder copyWithItemAndType(@Item.ItemId int itemId,
          @Event.EventType int eventType, final EventBuilder source) {
        source.put(EventKey.ITEM_NAME, itemId)
              .put(EventKey.EVENT_NAME, Integer.valueOf(eventType));
        return new EventBuilder(source.getBundle());
    }

    @NonNull
    public static String getParamName(final @EventParam.EventParamId int paramId) {
        Optional<EventParam> eventParam = EventParam.find(paramId);
        Preconditions.checkArgument(eventParam.isPresent(),
                                    String.format(Locale.getDefault(),
                                                  "EventParam with id %d not found.", paramId));
        return eventParam.get().name;
    }

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

    public <T> Optional<T> getIdParamValue(Class<T> returnClass, T... fallback) {
        return getParamAs(EventParam.PRM_ITEM_ID, returnClass, fallback.length > 0 ? fallback[0]
                                                                                   : null);
    }

    public <T> Optional<T> getTextParamValue(Class<T> returnClass, T... fallback) {
        return getParamAs(EventParam.PRM_ITEM_TEXT, returnClass, fallback.length > 0 ? fallback[0]
                                                                                     : null);
    }

    public <T> Optional<T> getPosParamValue(Class<T> returnClass, T... fallback) {
        return getParamAs(EventParam.PRM_ITEM_POS, returnClass, fallback.length > 0 ? fallback[0]
                                                                                    : null);
    }

    public <T> Optional<T> getValueParamValue(Class<T> returnClass, T... fallback) {
        return getParamAs(EventParam.PRM_VALUE, returnClass, fallback.length > 0 ? fallback[0]
                                                                                 : null);
    }

    public Item getItem() {
        return Item.find(getAs(EventKey.ITEM_NAME, Integer.class, Item.UNKNOWN_ITEM)).get();
    }

    public Event getEventType() {
        return Event.find(getAs(EventKey.EVENT_NAME, Integer.class, Event.EVT_UNKNOWN)).get();
    }

    public <T> Optional<T> getParamAs(@EventParam.EventParamId int paramId,
          Class<T> resultClass, T... fallback) throws ClassCastException {
        return Optional.fromNullable(
              (T) super.getAs(getParamName(paramId), resultClass, fallback));
    }

    public <T> Optional<T> getCheckedParam(@EventParam.EventParamId int paramId,
          final Class<T> clazz) {
        Preconditions.checkArgument(
              hasParamWithType(paramId, clazz),
              String.format("Missing event param %s with type of %s : %s",
                            EventParam.find(paramId).get(), clazz.getName(), toString())
        );
        return getParamAs(paramId, clazz);
    }

    public <T> boolean hasParamWithType(@EventParam.EventParamId int paramId,
          final Class<T> clazz) {
        try {
            //noinspection unchecked
            Optional<T> valueOptional =
                  Optional.fromNullable(super.getAs(getParamName(paramId), clazz));
            return valueOptional.isPresent() && valueOptional.get().getClass() == clazz;
        } catch (final ClassCastException ex) {
            //Log.e(ex);
            return false;
        }
    }

    public EventBuilder removeParam(@EventParam.EventParamId int paramId) {
        getBundle().remove(getParamName(paramId));
        return this;
    }

    @Nullable
    public String getStringParam(@EventParam.EventParamId int paramId, @Nullable Context context,
          String... fallback) {
        if (hasParamWithType(paramId, String.class)) {
            return getTextData(getParamAs(paramId, String.class).get(), context,
                               fallback);
        } else if (hasParamWithType(paramId, Integer.class)) {
            return getTextData(getParamAs(paramId, Integer.class).get(), context,
                               fallback);
        }
        return fallback.length > 0 ? fallback[0] : null;
    }

    public <T extends BundleWrapper> EventBuilder copyFrom(final T source) {
        getBundle().putAll(source.getBundle());
        return this;
    }

    public Observable<EventParam> getParamsAsStream(
          @Nullable final Func1<EventParam, Boolean> filterFunction) {
        return Observable.from(getBundle().keySet())
                         .map(name -> EventParam.find(name))
                         .switchMap(optional -> {
                             Observable<EventParam> resultObservable = Observable.empty();
                             if (optional.isPresent()) {
                                 resultObservable = Observable.just(optional.get());
                                 if (filterFunction != null) {
                                     return resultObservable.filter(filterFunction);
                                 }
                             }
                             return resultObservable;
                         });
    }

    public void removeParams(final Func1<EventParam, Boolean> filterFunction) {
        getParamsAsStream(filterFunction)
              .subscribe(eventParam -> removeParam(eventParam.id));
    }

    public Log logMe(String... args) {
        String message = TextUtils.join("\t", args);
        return Log.d(message, getItem(), getEventType());
    }

    public Log logReceived(String... args) {
        String message = TextUtils.join("\t", args);
        return Log.d("[RxBus]", "<--", message, getItem(), getEventType());
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventBuilder)) {
            return false;
        }

        Bundle thatBundle = ((EventBuilder) o).getBundle();
        return this.getBundle().equals(thatBundle);
    }

    @Override public int hashCode() {
        return getBundle().hashCode();
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("EventBuilder{");
        Item item = getItem();
        Event eventType = getEventType();
        if (item.id != Item.UNKNOWN_ITEM) {
            sb.append(EventKey.ITEM_NAME + " : " + item.name);
        }
        if (eventType.id != Event.EVT_UNKNOWN) {
            sb.append(", " + EventKey.EVENT_NAME + " : " + eventType.name);
        }
        Observable.from(getBundle().keySet())
                  .filter(
                        key -> !key.equals(EventKey.ITEM_NAME) && !key.equals(EventKey.EVENT_NAME))
                  .toBlocking()
                  .subscribe(key -> {
                      sb.append("\n\t" + key)
                        .append(" : '" + getBundle().get(key) + "', ");
                  });
        sb.append("\n}");
        return sb.toString();
    }
}
