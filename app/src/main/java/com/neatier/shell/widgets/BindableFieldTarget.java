package com.neatier.shell.widgets;

import android.content.Context;
import android.databinding.ObservableField;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.neatier.widgets.helpers.DrawableHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import trikita.log.Log;

/**
 * Created by László Gálosi on 18/04/17
 */
public class BindableFieldTarget implements Target {
    private ObservableField<Drawable> observableField;
    private Context mContext;

    public BindableFieldTarget(ObservableField<Drawable> observableField, Context context) {
        this.observableField = observableField;
        this.mContext = context;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        observableField.set(new BitmapDrawable(mContext.getResources(), bitmap));
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        Log.e("Failed to load Bitmap from resource");
        observableField.set(DrawableHelper.withContext(mContext)
                                          .withColor(Color.RED)
                                          .withDrawable(errorDrawable)
                                          .tint()
                                          .get());
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        observableField.set(placeHolderDrawable);
    }
}
