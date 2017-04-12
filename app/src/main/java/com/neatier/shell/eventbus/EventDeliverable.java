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

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by László Gálosi on 07/03/17
 */
public interface EventDeliverable<T> {
    void deliver(T event);

    Observable<T> toObservable(Func1<T, Boolean> eventFilter);

    Observable<T> toObservable();
}