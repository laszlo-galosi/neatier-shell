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
import android.support.annotation.StringRes;
import android.util.SparseArray;
import com.fernandocejas.arrow.optional.Optional;
import com.neatier.shell.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by László Gálosi on 28/04/16
 */
public class Item {

    public static final int UNKNOWN_ITEM = R.string.UNKNOWN_ITEM;
    public static final int TOOLBAR = R.string.TOOLBAR;
    public static final int SNACKBAR = R.string.SNACKBAR;
    public static final int NAV_MENU_ITEM = R.string.NAV_MENU_ITEM;
    public static final int SELF = R.string.SELF;
    public static final int SELF_CONTAINER = R.string.SELF_CONTAINER;
    public static final int SETTINGS = R.string.SETTINGS;
    public static final int DIALOG = R.string.DIALOG;
    public static final int PUSH_NOTIFICATION = R.string.PUSH_NOTIFICATION;
    public static final int EXT_INTENT = R.string.EXT_INTENT;
    public static final int CAMERA = R.string.CAMERA;
    public static final int SOFT_KEYBOARD = R.string.SOFT_KEYBOARD;
    public static final int PROGRESS = R.string.PROGRESS;

    private static SparseArray<Item> values;
    static TypedArray sItemIds;

    public static void init(final Context context) {
        sItemIds = context.getResources().obtainTypedArray(R.array.item_ids);
        int len = sItemIds.length();
        values = new SparseArray<>(len);
        for (int i = 0; i < len; i++) {
            @StringRes int id = sItemIds.getResourceId(i, -1);
            values.put(id, new Item(id, sItemIds.getString(i)));
        }
    }

    public static Optional<Item> find(int id) {
        return Optional.fromNullable(values.get(id));
    }

    public @ItemId int id;
    public String name;

    private Item(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override public String toString() {
        return name;
    }

    @IntDef({ UNKNOWN_ITEM, TOOLBAR, SNACKBAR, NAV_MENU_ITEM, SELF, SELF_CONTAINER, SETTINGS,
                  DIALOG, PUSH_NOTIFICATION, EXT_INTENT, CAMERA, SOFT_KEYBOARD, PROGRESS
            })
    @Retention(RetentionPolicy.SOURCE)
    /**
     * {@link InDef} annotation for identifying unique app items.
     */ public @interface ItemId {
    }
}
