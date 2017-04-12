/*
 * Copyright (C) 2016 Extremenet Ltd., All Rights Reserved
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

package com.neatier.shell.navigation.bottomnav;

import android.content.Context;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.v7.view.menu.MenuBuilder;
import com.neatier.shell.navigation.XmlNavigationMenuPresenterImpl;
import rx.Observable;
import trikita.log.Log;

/**
 * Created by László Gálosi on 08/11/16
 */
public class BottomNavigationMenuPresenter extends XmlNavigationMenuPresenterImpl {

    private final InternalPresenter mInternalPresenter = new InternalPresenter();

    public BottomNavigationMenuPresenter() {
    }

    @Override public void initMenu(final Context context, final MenuBuilder menuBuilder) {
        Log.d("initMenu");
        mInternalPresenter.setUpdateSuspended(true);
        super.initMenu(context, menuBuilder);
        //mInternalPresenter.initForMenu(mView.getContext(), menuBuilder);

    }

    public void initView(final BottomNavigationMenuView menuView, MenuBuilder menu) {
        mInternalPresenter.setBottomNavigationMenuView(menuView);
        menuView.initialize(menu);
        menuView.setPresenter(mInternalPresenter);
        menu.addMenuPresenter(mInternalPresenter);
    }

    class InternalPresenter extends android.support.design.internal.BottomNavigationPresenter {
        public InternalPresenter() {
            super();
        }

        @Override public void initForMenu(final Context context, final MenuBuilder menu) {
            //Remove menu items that is filtered out.
            //super.initForMenu(context, menu);
            mView.onUpdateStarted();
            initMenu(context, menu);
            final int menuSize = menu.size();
            Log.d("initForMenu", mMenuItems, "menuSize", menuSize);
            setUpdateSuspended(true);
            menu.clear();
            Observable.from(mMenuItems)
                      .subscribe(item -> menu.add(item.getGroupId(), item.getItemId(),
                                                  item.getOrder(), item.getTitle()
                      ).setIcon(item.getIcon()));
            setUpdateSuspended(false);
            updateMenuView(true);
            mView.onUpdateFinished();
        }
    }
}
