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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAttribute;
import com.airbnb.epoxy.EpoxyModelWithHolder;
import com.neatier.shell.R;
import com.neatier.shell.appframework.AppMvp;
import com.neatier.widgets.helpers.DrawableHelper;
import com.neatier.widgets.helpers.WidgetUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import org.joda.time.DateTime;
import trikita.log.Log;

/**
 * Created by László Gálosi on 18/05/16
 */
public interface GameTitleView extends AppMvp.LongTaskBaseView {

    class GameTitleItemModel
          extends EpoxyModelWithHolder<GameTitleItemHolder> {

        @EpoxyAttribute int pos;
        @EpoxyAttribute String title;
        @EpoxyAttribute String label;
        @EpoxyAttribute DateTime date;
        @EpoxyAttribute String imageUrl;
        @EpoxyAttribute String gamerScore;
        @EpoxyAttribute String achivement;

        private Picasso picassoInstance;
        private Context context;

        public GameTitleItemModel() {
        }

        public GameTitleItemModel with(final Picasso picassoInstance, final Context context) {
            this.picassoInstance = picassoInstance;
            this.context = context;
            return this;
        }

        @Override
        protected int getDefaultLayout() {
            return R.layout.list_item_workbench;
        }

        @Override public void bind(final GameTitleItemHolder holder) {
            super.bind(holder);
            Log.d("bind", toString());
            WidgetUtils.setTextOf(holder.mTitleView, title);
            WidgetUtils.setTextOf(holder.mScoreView, gamerScore);
            WidgetUtils.setTextOf(holder.mAchiView, achivement);
            try {
                int widthPx = context.getResources().getDimensionPixelSize(R.dimen.card_width);
                Uri uri = Uri.parse(imageUrl);
                picassoInstance.load(uri)
                               .centerCrop()
                               .resize(widthPx, widthPx)
                               .error(R.drawable.img_placeholder)
                               .placeholder(R.drawable.img_placeholder)
                               .into(holder);
            } catch (final Exception ex) {
                Log.e("Failed to load Bitmap from resource", imageUrl, ex);
            }
        }

        @Override protected GameTitleItemHolder createNewHolder() {
            return new GameTitleItemHolder(context);
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

    class GameTitleItemHolder extends BaseEpoxyHolder implements Target {
        @BindView(R.id.itemTitle) TextView mTitleView;
        @BindView(R.id.itemAchi) TextView mAchiView;
        @BindView(R.id.itemImage) ImageView mItemImageView;
        @BindView(R.id.itemScore) TextView mScoreView;
        private Context mContext;

        public GameTitleItemHolder(Context context) {
            mContext = context;
        }

        @Override protected void bindView(final View itemView) {
            super.bindView(itemView);
        }

        @Override public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
            mItemImageView.setImageBitmap(bitmap);
        }

        @Override public void onBitmapFailed(final Drawable errorDrawable) {
            Log.e("Failed to load Bitmap from resource");
            mItemImageView.setImageDrawable(DrawableHelper.withContext(mContext)
                                                          .withColor(Color.RED)
                                                          .withDrawable(errorDrawable)
                                                          .tint()
                                                          .get());
        }

        @Override public void onPrepareLoad(final Drawable placeHolderDrawable) {
            mItemImageView.setImageDrawable(placeHolderDrawable);
        }
    }
}
