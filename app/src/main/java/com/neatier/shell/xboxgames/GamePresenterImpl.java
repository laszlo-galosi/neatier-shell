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

package com.neatier.shell.xboxgames;

import com.fernandocejas.arrow.collections.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neatier.commons.helpers.DateTimeHelper;
import com.neatier.commons.helpers.HeapHog;
import com.neatier.commons.helpers.JsonSerializer;
import com.neatier.commons.helpers.LongTaskOnIOScheduler;
import com.neatier.commons.helpers.LongTaskScheduler;
import com.neatier.commons.helpers.RxUtils;
import com.neatier.shell.appframework.BasePresenterImpl;
import com.neatier.shell.data.network.ApiSettings;
import com.neatier.shell.data.repository.DataSources;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.internal.di.PerScreen;
import com.neatier.shell.xboxgames.GameTitleView.GameTitleItemModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import rx.Observable;

/**
 * Created by László Gálosi on 13/04/16
 */
@PerScreen
public class GamePresenterImpl extends BasePresenterImpl
      implements GamePresenter, HeapHog {

    private List<GameTitleItemModel> mItemList = new ArrayList<>(25);
    private DataSources.SimpleJsonResponseDataSource simpleApiDataSource;
    private JsonSerializer serializer;
    private LongTaskScheduler mLongTaskScheduler = new LongTaskOnIOScheduler();

    @Inject
    public GamePresenterImpl(DataSources.SimpleJsonResponseDataSource simpleApiDataSource,
          JsonSerializer serializer) {
        this.simpleApiDataSource = simpleApiDataSource;
        this.serializer = serializer;
    }

    @Override public void initialize() {
        super.initialize();
        mItemList.clear();
        mView.onUpdateStarted();
        //Observable.range(0, 20)
        String listType = (String) mView.getApiParams().get(ApiSettings.KEY_API_LIST_TYPE);
        mView.getApiParams().put(ApiSettings.KEY_API_ACTION, ApiSettings.ACTION_LIST_GAMES);
        simpleApiDataSource.getSimpleJsonResponse(mView.getApiParams())
                           //.map(index -> getModelItem(index, new Random(index)))
                           .flatMap(result -> {
                               JsonElement jsonElem = (JsonElement) result;
                               if (!jsonElem.isJsonNull() && jsonElem.isJsonObject()) {
                                   JsonArray titles =
                                         serializer.getAsChecked("titles",
                                                                 jsonElem.getAsJsonObject(),
                                                                 JsonArray.class);
                                   return Observable.range(0, titles.size())
                                                    .flatMap(index -> Observable.just(
                                                          titles.get(index).getAsJsonObject()))
                                                    .flatMap(titleObj -> {
                                                        if (mView != null) {
                                                            return Observable
                                                                  .just(getModelItem(titleObj));
                                                        }
                                                        return Observable.empty();
                                                    });
                               }
                               return Observable.error(new UnsupportedOperationException(
                                     String.format("This request time not implemented yet.%s",
                                                   listType)));
                           })
                           .subscribeOn(mLongTaskScheduler.performMeOn())
                           .observeOn(mLongTaskScheduler.notifyMeOn())
                           .subscribe(new PresenterSubscriber<GameTitleView$GameTitleItemModel_>() {
                               @Override public void onCompleted() {
                                   super.onCompleted();
                                   if (mView != null) {
                                       mView.onModelReady();
                                   }
                               }

                               @Override
                               public void onNext(
                                     final GameTitleView$GameTitleItemModel_ modelItem) {
                                   mItemList.add(modelItem.pos(mItemList.size()));
                               }
                           });
    }

    @Override public void freeUpHeap() {
        super.freeUpHeap();
        mItemList.clear();
    }

    @Override public void destroy() {
        freeUpHeap();
        super.destroy();
    }

    @Override public void onEvent(final EventBuilder event) {
    }

    private GameTitleItemModel getModelItem(final Integer index, final Random random) {
        //Now creating an event with the common parameters.
        final DateTime now = DateTimeHelper.nowLocal();
        return new GameTitleView$GameTitleItemModel_()
              .pos(index)
              .title(String.format("Item %d", index))
              .label(
                    RxUtils.randomFrom(Lists.newArrayList("Label A", "Label B", "Label C"), random)
              ).date(now.minusMinutes(index))
              .imageUrl("");
    }

    private GameTitleView$GameTitleItemModel_ getModelItem(
          final JsonObject json) {
        //Now creating an event with the common parameters.
        String listType = (String) mView.getApiParams().get(ApiSettings.KEY_API_LIST_TYPE);
        String name = serializer.getAsChecked("name", json, String.class);
        Integer gameScore = serializer.getAsChecked("currentGamerscore", json, Integer.class);
        Integer totalGameScore = serializer.getAsChecked("maxGamerscore", json, Integer.class);
        Integer earnedAchi = serializer.getAsChecked("earnedAchievements", json, Integer.class);
        Integer totalAchi = serializer.getAsChecked("totalAchievements", json, Integer.class);
        String dateString = serializer.getAsChecked("lastUnlock", json, String.class);
        switch (listType) {
            case ApiSettings.LIST_XBOX360:
                earnedAchi = serializer.getAsChecked("currentAchievements", json, Integer.class);
                totalGameScore = serializer.getAsChecked("totalGamerscore", json, Integer.class);
                dateString = serializer.getAsChecked("lastPlayed", json, String.class);
                break;
        }
        DateTime dateTime = DateTimeHelper.parseDate(dateString, ApiSettings.API_DATE_TIME_PATTERN,
                                                     DateTimeZone.getDefault());
        GameTitleView$GameTitleItemModel_ itemModel =
              new GameTitleView$GameTitleItemModel_()
                    .title(name)
                    .achivement(totalAchi == null ? String.valueOf(earnedAchi)
                                                  : String.format("%d/%d", earnedAchi, totalAchi))
                    .gamerScore(String.format("%d/%d", gameScore, totalGameScore));
        return itemModel;
    }

    @Override public List<GameTitleItemModel> getItems() {
        return mItemList;
    }
}
