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

package com.neatier.shell.navigation;

import android.content.Context;
import android.support.v7.view.menu.MenuBuilder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.neatier.shell.R;
import com.neatier.shell.activities.MainActivity;
import com.neatier.shell.appframework.BasePresenterImpl;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.internal.di.PerActivity;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by László Gálosi on 18/05/16
 */
@PerActivity
public class XmlNavigationMenuPresenterImpl extends BasePresenterImpl
      implements NavigationMenuPresenter {

    private List<MenuItem> mMenuItems = new ArrayList<>(16);

    @Inject public XmlNavigationMenuPresenterImpl() {
        super();
    }

    @Override public List<MenuItem> getMenuItems() {
        return mMenuItems;
    }

    @Override public void onEvent(final EventBuilder event) {

    }

    @Override public void initialize() {
        super.initialize();
        mView.onUpdateStarted();
        mMenuItems.clear();

        //Todo: replace this with the result of the API getChannels.
        buildStaticMenu((MainActivity) mView);
        mView.onUpdateFinished();
        mView.onModelReady();
    }

    private void buildStaticMenu(final Context context) {
        Menu menu = new MenuBuilder(context);
        MenuInflater mi = new MenuInflater(context);
        mi.inflate(R.menu.menu_navigation, menu);
        mMenuItems.clear();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem subitem = menu.getItem(i);
            mMenuItems.add(subitem);
            if (subitem.hasSubMenu()) {
                for (int j = 0, lenj = subitem.getSubMenu().size(); j < lenj; j++) {
                    MenuItem item = subitem.getSubMenu().getItem(j);
                    mMenuItems.add(item);
                }
            }
        }
    }
}
