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

package com.neatier.shell.home;

import android.content.Context;
import com.neatier.commons.helpers.HeapHog;
import com.neatier.shell.R;
import com.neatier.shell.appframework.BasePresenterImpl;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.eventbus.EventParam;
import com.neatier.shell.internal.di.PerScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import org.joda.time.DateTime;
import rx.Observable;

/**
 * Created by László Gálosi on 13/04/16
 */
@PerScreen
public class FakeHomePresenterImpl extends BasePresenterImpl
      implements HomePresenter, HeapHog {

    private List<EventBuilder> mItemList = new ArrayList<>(25);

    @Inject public FakeHomePresenterImpl() {
    }

    @Override public void initialize() {
        super.initialize();
        mItemList.clear();
        mView.onUpdateStarted();
        int len = 10;
        Integer[] indexes = new Integer[len];
        for (int i = 0; i < len; i++) {
            indexes[i] = i;
        }

        final Random random = new Random(DateTime.now().getMillis());
        Observable.from(indexes)
                  .map(index -> getDemoItem(index, random))
                  .subscribe(
                        new PresenterSubscriber<EventBuilder>() {
                            @Override public void onCompleted() {
                                super.onCompleted();
                                mView.onModelReady();
                            }

                            @Override public void onError(final Throwable e) {
                                super.onError(e);
                                mView.onUpdateFinished(e);
                            }

                            @Override public void onNext(final EventBuilder newsItem) {
                                mItemList.add(newsItem);
                            }
                        });
    }

    @Override public void freeUpHeap() {
        super.freeUpHeap();
        mItemList.clear();
    }

    @Override public void pause() {
        super.pause();
    }

    @Override public void destroy() {
        freeUpHeap();
        super.destroy();
    }

    @Override public void onEvent(final EventBuilder event) {
    }

    private EventBuilder getDemoItem(final Integer index, final Random random) {
        //Now creating an event with the common parameters.
        final Context context = mView.getContext();
        EventBuilder eventBuilder = EventBuilder.create()
                                                .addParam(EventParam.PRM_ITEM_TEXT,
                                                          context.getString(R.string.lorem_ipsum))
                                                .addParam(EventParam.PRM_ITEM_IMAGE,
                                                          R.drawable.img_placeholder)
                                                .addParam(EventParam.PRM_ITEM_POS, index);
        //Adding params with media type specific parameters.
        return eventBuilder;
    }

    @Override public List<EventBuilder> getItems() {
        return mItemList;
    }
}
