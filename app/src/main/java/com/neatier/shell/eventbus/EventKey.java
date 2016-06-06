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

import android.support.annotation.IntDef;
import android.util.SparseArray;
import com.fernandocejas.arrow.optional.Optional;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by László Gálosi on 28/04/16
 */
public class EventKey {

    public static final int ITEM = 0;
    public static final int EVENT = 1;
    public static int LEN = EVENT - ITEM;
    private static SparseArray<EventKey> values = new SparseArray<>(LEN);

    public static final String ITEM_NAME = "Item";
    public static final String EVENT_NAME = "EventType";

    static {
        values.put(ITEM, new EventKey(ITEM, ITEM_NAME));
        values.put(EVENT, new EventKey(EVENT, EVENT_NAME));
    }

    public static Optional<EventKey> find(int id) {
        if (id < 0 || id > LEN) {
            return Optional.absent();
        }
        return Optional.fromNullable(values.get(id));
    }

    @EventKeyDef int id;
    String name;

    private EventKey(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override public String toString() {
        return name;
    }

    @IntDef({ ITEM, EVENT, })
    @Retention(RetentionPolicy.SOURCE)
    /**
     * {@link InDef} annotation for identifying unique app events.
     */ public @interface EventKeyDef {
    }
}
