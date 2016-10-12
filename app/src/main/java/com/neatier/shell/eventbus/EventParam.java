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
public class EventParam {

    public static final int PRM_VALUE = R.string.PRM_VALUE;
    public static final int PRM_ERROR = R.string.PRM_ERROR;
    public static final int PRM_ITEM_ID = R.string.PRM_ITEM_ID;
    public static final int PRM_ITEM_POS = R.string.PRM_ITEM_POS;
    public static final int PRM_ITEM_TEXT = R.string.PRM_ITEM_TEXT;
    public static final int PRM_ITEM_DATE = R.string.PRM_ITEM_DATE;
    public static final int PRM_ITEM_TYPE = R.string.PRM_ITEM_TYPE;
    public static final int PRM_ITEM_IMAGE = R.string.PRM_ITEM_IMAGE;
    public static final int PRM_ITEM_THUMBNAIL = R.string.PRM_ITEM_THUMBNAIL;
    public static final int PRM_ITEM_COLOR = R.string.PRM_ITEM_COLOR;
    public static final int PRM_ITEM_URL = R.string.PRM_ITEM_URL;
    public static final int PRM_DETAILS = R.string.PRM_DETAILS;
    public static final int PRM_REQUEST_CODE = R.string.PRM_REQUEST_CODE;
    public static final int PRM_RESULT_CODE = R.string.PRM_RESULT_CODE;
    public static final int PRM_ACTION = R.string.PRM_ACTION;

    private static SparseArray<EventParam> values;

    static TypedArray sEventParamIds;

    public static void init(final Context context) {
        sEventParamIds = context.getResources().obtainTypedArray(R.array.event_params);
        int len = sEventParamIds.length();
        values = new SparseArray<>(len);
        for (int i = 0; i < len; i++) {
            int id = sEventParamIds.getResourceId(i, -1);
            values.put(id, new EventParam(id, sEventParamIds.getString(i)));
        }
    }

    public static Optional<EventParam> find(int id) {
        return Optional.fromNullable(values.get(id));
    }

    public static Optional<EventParam> find(String name) {
        for (int i = 0, len = values.size(); i < len; i++) {
            @EventParamId int paramId = values.keyAt(i);
            if (values.get(paramId).equals(name)) {
                return find(paramId);
            }
        }
        return Optional.absent();
    }

    @EventParamId
    int id;
    String name;

    private EventParam(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @IntDef({ PRM_VALUE, PRM_ITEM_ID, PRM_ITEM_POS, PRM_ITEM_TEXT, PRM_ITEM_DATE, PRM_ITEM_TYPE,
                  PRM_ITEM_IMAGE, PRM_ITEM_THUMBNAIL, PRM_ITEM_COLOR, PRM_ITEM_URL, PRM_DETAILS,
                  PRM_REQUEST_CODE, PRM_RESULT_CODE, PRM_ACTION
            })
    @Retention(RetentionPolicy.SOURCE)
    public @interface EventParamId {
    }
}
