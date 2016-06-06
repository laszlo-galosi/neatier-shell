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

/**
 * Created by László Gálosi on 09/05/16
 */

import android.content.Context;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.fernandocejas.arrow.optional.Optional;
import com.neatier.shell.R;
import com.neatier.shell.eventbus.Event;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.eventbus.EventParam;
import com.neatier.shell.eventbus.Item;
import com.neatier.widgets.Bindable;
import com.neatier.widgets.recyclerview.ItemViewHolderBase;
import com.neatier.widgets.recyclerview.ItemWidgetAdapter;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.functions.Action1;

public class NavigationMenuItemAdapter
      extends ItemWidgetAdapter<MenuItem> {

    private @LayoutRes int mHeaderLayout = 0;

    public NavigationMenuItemAdapter(final Context context,
          @Nullable final List<MenuItem> dataSet) {
        super(context, R.layout.listitem_nav_menu, 10, dataSet);
    }

    public NavigationMenuItemAdapter(final Context context, final @MenuRes int menuResId) {
        super(context, 10);
        Menu menu = new MenuBuilder(context);
        MenuInflater mi = new MenuInflater(context);
        mi.inflate(menuResId, menu);
        mDataset = new ArrayList<>(16);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem subitem = menu.getItem(i);
            mDataset.add(subitem);
            if (subitem.hasSubMenu()) {
                for (int j = 0, lenj = subitem.getSubMenu().size(); j < lenj; j++) {
                    MenuItem item = subitem.getSubMenu().getItem(j);
                    mDataset.add(item);
                }
            }
        }
    }

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
          int viewType) {
        mItemLayout = getItemLayout(viewType);
        View itemWidget =
              LayoutInflater.from(mContext).inflate(mItemLayout, parent, false);
        if (viewType == R.layout.widget_nav_header) {
            return new MenuHeaderViewHolder(itemWidget, mContext);
        } else {
            boolean clickable = viewType == R.layout.listitem_nav_menu;
            itemWidget.setClickable(clickable);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && clickable) {
                itemWidget.setForeground(ContextCompat.getDrawable(mContext,
                                                                   android.R.drawable
                                                                         .list_selector_background));
            }
            return new MenuItemViewHolder(itemWidget, mContext);
        }
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        if (viewType == R.layout.widget_nav_header) {
            ((Bindable) holder).bind(getHeaderData());
        } else {
            Optional<MenuItem> item = getItem(position);
            if (item.isPresent()) {
                ((Bindable) holder).bind(item.get());
            }
        }
    }

    public Optional<MenuItem> getItem(int position) {
        return Optional.fromNullable(mDataset.get(getOffsetPosition(position)));
    }

    @Override public long getItemId(int position) {
        final int viewType = getItemViewType(position);
        if (viewType != R.layout.widget_nav_header) {
            return getItem(position).get().getItemId();
        }
        return RecyclerView.NO_ID;
    }

    @Override public @LayoutRes int getItemLayout(int viewType) {
        return viewType;
    }

    @Override public int getItemViewType(final int position) {
        if (position == 0 && hasHeader()) {
            return R.layout.widget_nav_header;
        }
        Optional<MenuItem> item = getItem(position);
        if (item.isPresent()) {
            return R.layout.listitem_nav_menu;
        }
        throw new IllegalStateException(
              String.format("Invalid view type for postion: %d, item: %s", position,
                            getItem(position)));
    }

    public NavigationMenuItemAdapter withHeader(final @LayoutRes int headerLayout) {
        mHeaderLayout = headerLayout;
        return this;
    }

    public Observable<?> getHeaderData() {
        return Observable.empty();
    }

    public boolean hasHeader() {
        return mHeaderLayout > 0;
    }

    private int getOffsetPosition(final int position) {
        return position - (hasHeader() ? 1 : 0);
    }

    static class MenuItemViewHolder extends ItemViewHolderBase {

        @BindView(R.id.itemText) TextView mItemTextView;

        protected MenuItemViewHolder(final View itemView, final Context context) {
            super(itemView, context);
            ButterKnife.bind(this, itemView);
        }

        @Override public void bind(final Object dataItem) {
            super.bind(dataItem);
            MenuItem menuItem = (MenuItem) dataItem;
            mItemTextView.setText(menuItem.getTitle());
            if (mItemTextView.isClickable()) {
                mItemTextView.setOnClickListener(
                      v -> EventBuilder.withItemAndType(Item.NAV_MENU_ITEM, Event.EVT_NAVIGATE)
                                       .addParam(EventParam.PRM_ITEM_ID, menuItem.getItemId())
                                       .send()
                );
            }
        }
    }

    static class MenuHeaderViewHolder extends ItemViewHolderBase {

        protected MenuHeaderViewHolder(final View itemView, final Context context) {
            super(itemView, context);
        }

        @Override public void bind(final Object dataItem) {
            if (dataItem instanceof Observable) {
                ((Observable<?>) dataItem).subscribe(new Action1<Object>() {
                    @Override public void call(final Object o) {
                        EventBuilder event = (EventBuilder) o;
                        int itemId =
                              event.getParamAs(EventParam.PRM_ITEM_ID, Integer.class).or(0);
                        Optional<View> renderableView =
                              Optional.fromNullable(mItemView.findViewById(itemId));
                        if (renderableView.isPresent()) {
                            View view = renderableView.get();
                            if (view.isClickable()) {
                                view.setOnClickListener(
                                      v -> event.withItemAndType(Item.NAV_MENU_ITEM,
                                                                 Event.EVT_NAVIGATE)
                                                .addParam(EventParam.PRM_ITEM_ID, view.getId())
                                                .send()
                                );
                            }
                        }
                    }
                });
            }
        }
    }
}
