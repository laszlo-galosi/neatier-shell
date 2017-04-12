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
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.SparseArray;
import com.fernandocejas.arrow.optional.Optional;
import com.neatier.shell.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by László Gálosi on 28/04/16
 */
public class Event {

    //Event types.
    public static final int EVT_UNKNOWN = R.string.EVT_UNKNOWN;
    public static final int EVT_NAVIGATE = R.string.EVT_NAVIGATE;
    public static final int EVT_RENDER = R.string.EVT_RENDER;
    public static final int EVT_SHOW_HIDE = R.string.EVT_SHOW_HIDE;
    public static final int EVT_DIALOG = R.string.EVT_DIALOG;
    public static final int EVT_INTERNAL_ERROR = R.string.EVT_INTERNAL_ERROR;
    public static final int EVT_MESSAGE = R.string.EVT_MESSAGE;
    public static final int EVT_CLICK = R.string.EVT_CLICK;
    public static final int EVT_LONGCLICK = R.string.EVT_LONGCLICK;
    public static final int EVT_ITEM_ADDED = R.string.EVT_ITEM_ADDED;
    public static final int EVT_ITEM_CHANGED = R.string.EVT_ITEM_CHANGED;
    public static final int EVT_ITEM_REMOVED = R.string.EVT_ITEM_REMOVED;
    public static final int EVT_PREF_CHANGED = R.string.EVT_PREF_CHANGED;
    public static final int EVT_SEND = R.string.EVT_SEND;
    public static final int EVT_RESULT = R.string.EVT_RESULT;
    public static final int EVT_USER_LOGIN = R.string.EVT_USER_LOGIN;
    public static final int EVT_USER_LOGOUT = R.string.EVT_USER_LOGOUT;
    public static final int EVT_REQUIRE_PERMISSION = R.string.EVT_REQUIRE_PERMISSION;
    public static final int EVT_CONNECT = R.string.EVT_CONNECT;
    public static final int EVT_CANCEL_UPLOAD = R.string.EVT_CANCEL_UPLOAD;

    private static SparseArray<Event> values;

    static TypedArray sEventTypes;

    public static void init(final Context context) {
        sEventTypes = context.getResources().obtainTypedArray(R.array.event_types);
        int len = sEventTypes.length();
        values = new SparseArray<>(len);
        for (int i = 0; i < len; i++) {
            int id = sEventTypes.getResourceId(i, -1);
            values.put(id, new Event(id, sEventTypes.getString(i)));
        }
    }

    public static Optional<Event> find(int id) {
        return Optional.fromNullable(values.get(id));
    }

    public @EventType int id;
    public String name;

    public Event(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override public String toString() {
        return name;
    }

    @IntDef({ EVT_UNKNOWN, EVT_NAVIGATE, EVT_RENDER, EVT_SHOW_HIDE, EVT_DIALOG, EVT_INTERNAL_ERROR,
                  EVT_MESSAGE, EVT_CLICK, EVT_LONGCLICK, EVT_ITEM_ADDED, EVT_ITEM_CHANGED,
                  EVT_ITEM_REMOVED, EVT_PREF_CHANGED, EVT_SEND, EVT_RESULT, EVT_USER_LOGIN,
                  EVT_USER_LOGOUT, EVT_REQUIRE_PERMISSION, EVT_CONNECT, EVT_CANCEL_UPLOAD })
    @Retention(RetentionPolicy.SOURCE)
    /**
     * {@link InDef} annotation for identifying unique app events.
     */ public @interface EventType {
    }
}
