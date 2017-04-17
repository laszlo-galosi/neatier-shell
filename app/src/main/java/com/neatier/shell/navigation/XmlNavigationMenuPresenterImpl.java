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
import android.view.MenuInflater;
import android.view.MenuItem;
import com.neatier.shell.appframework.BasePresenterImpl;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.internal.di.PerActivity;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by László Gálosi on 18/05/16
 */
@PerActivity
public class XmlNavigationMenuPresenterImpl extends BasePresenterImpl
      implements NavigationMenuPresenter {

    protected List<MenuItem> mMenuItems = new ArrayList<>(16);

    @Inject public XmlNavigationMenuPresenterImpl() {
        super();
    }

    @Override public List<MenuItem> getMenuItems() {
        return mMenuItems;
    }

    @Override public void initialize() {
        super.initialize();
        mView.onUpdateStarted();
        mMenuItems.clear();

        //Todo: replace this with the result of the API getChannels.
        Context context = mView.getContext();
        MenuBuilder menu = new MenuBuilder(context);
        MenuInflater mi = new MenuInflater(context);
        mi.inflate(((MvpNavigationView) mView).getMenuResource(), menu);
        initMenu(context, menu);
        mView.onUpdateFinished();
        mView.onModelReady();
    }

    @Override public void onEvent(final EventBuilder event) {

    }

    /**
     * Initializes  menu items by applying item and sub menu group filters
     * for the inflated {@link MenuBuilder} items.
     *
     * @param menu the inflated {@link MenuBuilder}
     */
    public void initMenu(final Context context, final MenuBuilder menu) {
        mMenuItems.clear();
        getMenuItemsAsListStream(menu).subscribe();
    }

    @Override public Observable<NavigationMenu$NavigationMenuModel_> getMenuItemStream() {
        Context context = mView.getContext();
        MenuBuilder menu = new MenuBuilder(context);
        MenuInflater mi = new MenuInflater(context);
        mi.inflate(((MvpNavigationView) mView).getMenuResource(), menu);
        return Observable
              .range(0, menu.size())
              .flatMap(i -> Observable.just(menu.getItem(i)))
              .switchMap(menuItem -> {
                  if (menuItem.hasSubMenu()) {
                      return Observable
                            .just(menuItem).concatWith(
                                  Observable.range(0, menuItem.getSubMenu().size())
                                            .flatMap(i -> Observable.just(
                                                  menuItem.getSubMenu().getItem(i)
                                            ).filter(getMenuItemFilter()))
                            ).filter(getSubMenuFilter());
                  }
                  return Observable.just(menuItem).filter(getMenuItemFilter());
              }).flatMap(menuItem -> Observable.just(
                    NavigationMenu.NavigationMenuModel
                          .from(menuItem)
              ));
    }

    public Observable<List<MenuItem>> getMenuItemsAsListStream(final MenuBuilder menu) {
        return Observable.range(0, menu.size())
                         .flatMap(i -> Observable.just(menu.getItem(i)))
                         .switchMap(menuItem -> {
                             if (menuItem.hasSubMenu()) {
                                 return Observable
                                       .just(menuItem).concatWith(
                                             Observable.range(0, menuItem.getSubMenu().size())
                                                       .flatMap(i -> Observable.just(
                                                             menuItem.getSubMenu().getItem(i)
                                                       ).filter(getMenuItemFilter()))
                                       ).filter(getSubMenuFilter());
                             }
                             return Observable.just(menuItem).filter(getMenuItemFilter());
                         }).collect(() -> mMenuItems, (list, item) -> list.add(item));
    }

    /**
     * Returns a filter {@link Func1} filter function to filter menu items from the inflated menu
     * xml. The filters input {@link MenuItem} either has sub menu or not.
     */
    public Func1<MenuItem, Boolean> getMenuItemFilter() {
        return item -> Boolean.TRUE;
    }

    public Func1<MenuItem, Boolean> getDefaultSelectionFilter() {
        return item -> mMenuItems.indexOf(item) == 0;
    }

    /**
     * Returns a filter {@link Func1} filter function to filter sub menu item for a menu group
     * from the inflated menu.
     * Use {@link #getMenuItemFilter()} to filter items of this sub menu group.
     */
    public Func1<MenuItem, Boolean> getSubMenuFilter() {
        return item -> Boolean.TRUE;
    }
}
