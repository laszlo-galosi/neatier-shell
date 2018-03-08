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

package com.neatier.shell.factorysettings;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.SparseArray;
import com.fernandocejas.arrow.optional.Optional;
import com.neatier.commons.helpers.BundleWrapper;
import com.neatier.commons.helpers.SharedKeyValueStore;
import com.neatier.shell.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by László Gálosi on 19/07/16
 */
public class PrefKey {

    public static final int USER_PROFILE = R.string.PREF_KEY_USER_PROFILE;
    public static final int AUTO_LOGIN = R.string.PREF_KEY_AUTO_LOGIN;

    private static SparseArray<PrefKey> values;
    static TypedArray sPrefKeys;

    public @PrefKeyId int id;
    public String name;

    public PrefKey(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public static void init(final Context context) {
        sPrefKeys = context.getResources().obtainTypedArray(R.array.pref_keys);
        int len = sPrefKeys.length();
        values = new SparseArray<>(len);
        for (int i = 0; i < len; i++) {
            int id = sPrefKeys.getResourceId(i, -1);
            values.put(id, new PrefKey(id, sPrefKeys.getString(i)));
        }
    }

    public static Optional<PrefKey> find(int id) {
        return Optional.fromNullable(values.get(id));
    }

    public static PrefKey userProfile() {
        return find(USER_PROFILE).get();
    }

    public static PrefKey autoLogin() {
        return find(AUTO_LOGIN).get();
    }

    public boolean hasEntryIn(final SharedKeyValueStore keyValueStore) {
        return keyValueStore.containsKey(name);
    }

    public boolean hasEntryIn(final BundleWrapper bundle) {
        return bundle.containsKey(name);
    }

    public PrefKey storeIn(final SharedKeyValueStore<String, Object> sharedKeyValueStore,
          final Object value) {
        sharedKeyValueStore.put(name, value).apply();
        return this;
    }

    public PrefKey storeIn(final BundleWrapper bundle, final Object value) {
        bundle.put(name, value);
        return this;
    }

    public PrefKey clearFrom(final SharedKeyValueStore<String, Object> sharedKeyValueStore) {
        sharedKeyValueStore.remove(name).apply();
        return this;
    }

    public PrefKey clearFrom(final BundleWrapper bundle) {
        bundle.remove(name);
        return this;
    }

    public static Optional<PrefKey> find(String name) {
        for (int i = 0, len = values.size(); i < len; i++) {
            @PrefKeyId int keyId = values.keyAt(i);
            if (values.get(keyId).name.equals(name)) {
                return find(keyId);
            }
        }
        return Optional.absent();
    }

    public boolean isEqualTo(@PrefKeyId int prefKeyId) {
        return find(prefKeyId).isPresent() && id == prefKeyId;
    }

    @IntDef({ USER_PROFILE, AUTO_LOGIN })
    @Retention(RetentionPolicy.SOURCE)
    /**
     * {@link InDef} annotation for identifying unique app events.
     */ public @interface PrefKeyId {

    }

    @Override public String toString() {
        return name;
    }
}
