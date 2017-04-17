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
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyController;
import com.airbnb.epoxy.EpoxyModelClass;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.neatier.shell.R;
import com.neatier.shell.appframework.AppMvp;
import com.neatier.shell.widgets.BaseEpoxyModelAdapter;
import com.neatier.shell.xboxgames.BaseEpoxyHolder;
import com.neatier.widgets.helpers.DrawableHelper;
import com.neatier.widgets.helpers.WidgetUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import trikita.log.Log;

/**
 * Created by László Gálosi on 18/05/16
 */
public interface NavigationMenu extends AppMvp.LongTaskBaseView {

    @EpoxyModelClass(layout = R.layout.list_item_navmenu)
    abstract class NavigationMenuModel
          extends EpoxyModelWithHolder<MenuItemHolder> {

        @EpoxyAttribute @Nullable String title;
        @EpoxyAttribute Drawable icon;
        @EpoxyAttribute boolean selected;
        @EpoxyAttribute View.OnClickListener clickListener;

        private Context context;

        public NavigationMenuModel() {
        }

        public NavigationMenuModel with(final Context context) {
            this.context = context;
            return this;
        }

        @Override public void bind(final MenuItemHolder holder) {
            super.bind(holder);
            Log.d("bind", title, "selected", selected).v(toString());
            WidgetUtils.setTextOf(holder.mTitleView, title);
            if (holder.mItemImageView != null) {
                WidgetUtils.setImageOf(holder.mItemImageView, icon, context);
            } else {
                holder.mTitleView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            }
            holder.mItemView.setSelected(selected);
            holder.mItemView.refreshDrawableState();
            holder.mItemView.setOnClickListener(clickListener);
            holder.mItemView.refreshDrawableState();
        }

        public static NavigationMenu$NavigationMenuModel_ from(MenuItem menuItem) {
            return new NavigationMenu$NavigationMenuModel_()
                  .id(menuItem.getItemId())
                  .title((String) menuItem.getTitle())
                  .icon(menuItem.getIcon());
        }

        @Override public String toString() {
            final StringBuilder sb = new StringBuilder("NavigationMenuModel{");
            sb.append("id=").append(id());
            sb.append(", title='").append(title).append('\'');
            sb.append(", icon=").append(icon);
            sb.append(", selected=").append(selected);
            sb.append('}');
            return sb.toString();
        }
    }

    @EpoxyModelClass(layout = R.layout.widget_nav_header)
    abstract class MenuHeaderModel extends EpoxyModelWithHolder<MenuHeaderHolder> {
    }

    class MenuHeaderHolder extends BaseEpoxyHolder {
        @Nullable @BindView(R.id.itemIcon) ImageView mItemIconView;
        View mItemView;

        @Override protected void bindView(final View itemView) {
            super.bindView(itemView);
            mItemView = itemView;
            mItemIconView.setImageDrawable(
                  DrawableHelper.withContext(itemView.getContext())
                                .withDrawable(R.drawable.ic_neatier_logo)
                                .withColorRes(R.color.white)
                                .tint().get()
            );

        }
    }

    class MenuItemHolder extends BaseEpoxyHolder {
        @BindView(R.id.itemText) TextView mTitleView;
        @Nullable @BindView(R.id.itemImage) ImageView mItemImageView;

        View mItemView;

        @Override protected void bindView(final View itemView) {
            super.bindView(itemView);
            mItemView = itemView;
        }

        public View getItemView() {
            return mItemView;
        }
    }

    class NavigationMenuItemAdapter extends BaseEpoxyModelAdapter<NavigationMenuModel> {
        @Inject Context context;

        public NavigationMenuItemAdapter(final Context context) {
            this.context = context;
        }

        @Override protected void notifyModelsChanged() {
            super.notifyModelsChanged();
        }

        @Override public void add(final NavigationMenuModel model) {
            Log.d("add model with id", model.id()).v(model);
            super.add(model.with(context));
        }

        @Override public void addAll(final Collection<NavigationMenuModel> collection) {
            clearModels();
            Observable.from(collection)
                      .subscribe(model -> add(model));
        }
    }

    class NavigationMenuController extends EpoxyController {

        public interface AdapterCallbacks {
            void onMenuItemClicked(NavigationMenu$NavigationMenuModel_ model, MenuItemHolder holder,
                  View clickedView, int position);
        }

        private AdapterCallbacks callbacks;

        final Context mContext;

        protected List<? extends NavigationMenuModel> menuItems = Collections.emptyList();

        public NavigationMenuController(final Context context) {
            mContext = context;
        }

        public NavigationMenuController withCallbacks(final AdapterCallbacks callbacks) {
            this.callbacks = callbacks;
            return this;
        }

        public void setMenuItems(List<? extends NavigationMenuModel> menuItems) {
            this.menuItems = menuItems;
            requestModelBuild();
        }

        @Override
        protected void buildModels() {
            new NavigationMenu$MenuHeaderModel_()
                  .id("header model")
                  .addTo(this);
            Observable.from(menuItems)
                      .subscribe(itemModel ->
                                       new NavigationMenu$NavigationMenuModel_()
                                             .id(itemModel.id())
                                             .title(itemModel.title)
                                             .icon(itemModel.icon)
                                             .selected(itemModel.selected)
                                             .with(mContext)
                                             .clickListener(
                                                   (model, holder, v, pos) -> callbacks
                                                         .onMenuItemClicked(
                                                               model, holder, v, pos))
                                             .addTo(this)

                      );
        }
    }
}
