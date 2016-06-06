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

package com.neatier.shell.appframework.helpers;

import com.neatier.commons.helpers.RxUtils;
import com.neatier.shell.eventbus.Event;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.eventbus.EventParam;
import com.neatier.shell.eventbus.Item;
import com.neatier.shell.exception.ErrorMessageFactory;
import trikita.log.Log;

/**
 * Created by László Gálosi on 11/04/16
 */
public class PresentationSubscriber<T> extends RxUtils.SubscriberAdapter<T> {

    public PresentationSubscriber() {
    }

    @Override public void onCompleted() {
        Log.d("onCompleted");
    }

    @Override public void onError(Throwable e) {
        RxUtils.logRxError().call(e);
        ErrorMessageFactory.Error error = ErrorMessageFactory.getInstance().create(e);
        EventBuilder.withItemAndType(error.showAsAlert() ? Item.DIALOG : Item.SNACKBAR,
                                     Event.EVT_INTERNAL_ERROR)
                    .addParam(EventParam.PRM_VALUE, error.getMessageRes())
                    .send();
    }

    @Override public void onNext(T t) {
    }
}
