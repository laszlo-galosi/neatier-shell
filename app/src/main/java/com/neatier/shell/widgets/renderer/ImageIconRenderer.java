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

package com.neatier.shell.widgets.renderer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import com.fernandocejas.arrow.optional.Optional;
import com.neatier.commons.helpers.Preconditions;
import com.neatier.shell.R;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.eventbus.EventParam;
import com.neatier.widgets.helpers.DrawableHelper;
import com.neatier.widgets.renderers.Renderable;

/**
 * Created by László Gálosi on 06/05/16
 */
public class ImageIconRenderer<T, C> implements Renderable<EventBuilder> {

    private final Class<T> mImageDataClass;
    private Class<C> mColorDataClass;

    public ImageIconRenderer(final Class<T> mImageDataClass) {
        this.mImageDataClass = mImageDataClass;
    }

    @Override public void render(final View view, @NonNull final EventBuilder dataItem) {
        Preconditions.checkNotNull(view, "ImageView");

        Optional<T> imgData = dataItem.getParamAs(EventParam.PRM_ITEM_IMAGE, mImageDataClass);
        String paramName = EventBuilder.getParamName(EventParam.PRM_ITEM_IMAGE);

        Preconditions.checkArgument(imgData.isPresent(), paramName);

        Preconditions.checkClass(imgData.get(), mImageDataClass,
                                 String.format("%s is a type of %s", paramName,
                                               mImageDataClass.getName())
        );

        Drawable drawable = null;
        Context context = view.getContext();
        if (mImageDataClass == Integer.class) {
            drawable = ContextCompat.getDrawable(context, (Integer) imgData.get());
        } else if (mImageDataClass == Drawable.class) {
            drawable = (Drawable) imgData.get();
        }

        Preconditions.checkNotNull(drawable, "extracted drawable");

        DrawableHelper drawableHelper = DrawableHelper.withContext(context)
                                                      .withDrawable(drawable);

        Optional<Integer> colorData = dataItem.getParamAs(EventParam.PRM_ITEM_COLOR, Integer.class);
        if (colorData.isPresent()) {
            ColorStateList colorState =
                  ContextCompat.getColorStateList(context, colorData.get());
            if (colorState.isStateful()) {
                drawableHelper.withColorState(
                      colorState, view.getDrawableState(),
                      ContextCompat.getColor(context, R.color.colorControlNormal)
                ).tint();
            } else {
                try {
                    drawableHelper.withColorRes(colorData.get()).tint();
                } catch (final Resources.NotFoundException e) {
                    drawableHelper.withColor(colorData.get()).tint();
                }
            }
        }
        ((ImageView) view).setImageDrawable(drawableHelper.get());
    }

    public ImageIconRenderer withColorClass(final Class<C> colorDataClass) {
        mColorDataClass = colorDataClass;
        return this;
    }
}
