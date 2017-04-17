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

import android.content.Context;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import com.airbnb.epoxy.DataBindingEpoxyModel;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelClass;
import com.neatier.commons.helpers.Preconditions;
import com.neatier.shell.BR;
import com.neatier.shell.R;
import com.neatier.shell.appframework.AppMvp;
import com.neatier.shell.widgets.BindableFieldTarget;
import com.squareup.picasso.Picasso;
import org.joda.time.DateTime;

/**
 * Created by László Gálosi on 18/05/16
 */
public interface GameTitleView extends AppMvp.LongTaskBaseView {

    @EpoxyModelClass(layout = R.layout.list_item_workbench)
    abstract class GameTitleItemModel extends DataBindingEpoxyModel {

        @EpoxyAttribute int pos;
        public @EpoxyAttribute String title;
        @EpoxyAttribute String label;
        @EpoxyAttribute DateTime date;
        @EpoxyAttribute String imageUrl;
        public @EpoxyAttribute String gamerScore;
        public @EpoxyAttribute String achievement;

        private Picasso picassoInstance;
        private Context context;

        public ObservableField<Drawable> titleImage;
        private BindableFieldTarget bindableFieldTarget;

        public GameTitleItemModel() {
        }

        public GameTitleItemModel with(final Picasso picassoInstance, final Context context) {
            this.picassoInstance = picassoInstance;
            this.context = context;
            titleImage = new ObservableField<>();
            bindableFieldTarget = new BindableFieldTarget(titleImage, context);
            return this;
        }

        @Override public void bind(final DataBindingHolder holder) {
            super.bind(holder);
            //if (!TextUtils.isEmpty(imageUrl)) {
            Uri uri = Uri.parse(imageUrl);
            picassoInstance.load(uri)
                           //.transform(new WidgetUtils.FixedWidthResizeTransform(widthPx))
                           .resizeDimen(R.dimen.card_width, R.dimen.card_height)
                           .centerInside()
                           .error(R.drawable.img_placeholder)
                           .placeholder(R.drawable.img_placeholder)
                           .into(bindableFieldTarget);
            //}
        }

        @Override protected void setDataBindingVariables(final ViewDataBinding binding) {
            Preconditions.checkArgument(
                  binding.setVariable(BR.viewModel, this),
                  String.format("The attribute %s was defined in your data "
                                      + "binding model %s but a data variable of "
                                      + "that name was not found in the layout.",
                                "viewModel", getClass().getSimpleName())
            );
            Preconditions.checkArgument(
                  binding.setVariable(BR.achiDrawable,
                                      ContextCompat.getDrawable(context,
                                                                R.drawable.ic_trophy_gold_18dp)),
                  String.format("The attribute %s was defined in your data "
                                      + "binding model %s but a data variable of "
                                      + "that name was not found in the layout.",
                                "achiDrawable", getClass().getSimpleName())
            );
            Preconditions.checkArgument(
                  binding.setVariable(BR.gamerScoreDrawable,
                                      ContextCompat.getDrawable(context,
                                                                R.drawable.ic_gamerscore_24dp)),
                  String.format("The attribute %s was defined in your data "
                                      + "binding model %s but a data variable of "
                                      + "that name was not found in the layout.",
                                "gamerScoreDrawable", getClass().getSimpleName())
            );
        }

        @Override public String toString() {
            final StringBuilder sb = new StringBuilder("GameTitleItemModel{");
            sb.append("gamerScore='").append(gamerScore).append('\'');
            sb.append(", imageUrl='").append(imageUrl).append('\'');
            sb.append(", date=").append(date);
            sb.append(", title='").append(title).append('\'');
            sb.append(", pos=").append(pos);
            sb.append('}');
            return sb.toString();
        }
    }
}
