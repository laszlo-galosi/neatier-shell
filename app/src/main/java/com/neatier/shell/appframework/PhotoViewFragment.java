/*
 * Copyright (C) 2017 Extremenet Ltd., All Rights Reserved
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

package com.neatier.shell.appframework;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import butterknife.BindView;
import com.neatier.commons.helpers.BundleWrapper;
import com.neatier.shell.R;
import com.neatier.shell.activities.MainActivity;
import com.neatier.shell.eventbus.EventParam;
import com.neatier.shell.internal.di.HasComponent;
import com.neatier.shell.internal.di.ScreenComponent;
import com.neatier.shell.internal.di.ScreenModule;
import com.neatier.widgets.helpers.DrawableHelper;
import com.neatier.widgets.helpers.WidgetUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;
import javax.inject.Inject;
import trikita.log.Log;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link PhotoViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhotoViewFragment extends BaseFragment implements HasComponent<ScreenComponent>,
                                                               TaggedBaseFragment, Target {

    public static final String TAG = "PhotoViewFragment";
    @BindView(R.id.itemImage) ImageView mImageView;

    @Inject Picasso mPicasso;
    private ScreenComponent mComponent;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided bundle parameters.
     *
     * @param bundleWrapper {@link BundleWrapper} containing some initialization arguments.
     * @return A new instance of fragment.
     */
    public static PhotoViewFragment newInstance(final BundleWrapper bundleWrapper) {
        PhotoViewFragment fragment = new PhotoViewFragment();
        Log.w("newInstance ", bundleWrapper);
        if (bundleWrapper != null) {
            BundleWrapper copyBundle = bundleWrapper.copy();
            fragment.setArguments(copyBundle.getBundle());
        } else {
            fragment.setArguments(new Bundle());
        }
        return fragment;
    }

    @Override protected String[] argumentsToRetain() {
        return new String[] {
              EventParam.itemUrl().name,
              EventParam.itemImage().name
        };
    }

    public PhotoViewFragment() {
        // Required empty public constructor
    }

    @Override
    protected void onInflateLayout(final View contentView, final Bundle savedInstanceState) {
        super.onInflateLayout(contentView, savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initialize();
    }

    @Override
    protected void initialize() {
        super.initialize();
        createComponent().inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadImage();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override public void onDestroy() {
        super.onDestroy();
        releaseComponent();
    }

    @Override
    public String getFragmentTag() {
        return String.format("%s_%s", TAG, getUrl());
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_photoview;
    }

    @Override
    public boolean hasProgressView() {
        return true;
    }

    @Override
    public void onModelReady() {
        mLayoutReady = true;
        Log.d("onModelReady", mLayoutReady);
        onUpdateFinished();
    }

    @Override
    public boolean isResultFromCloud() {
        return true;
    }

    @Override
    protected boolean shouldRetainInstance() {
        return true;
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void clearLeakables() {
    }

    @Override public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
        mImageView.setImageBitmap(bitmap);
        onUpdateFinished();
    }

    @Override public void onBitmapFailed(final Drawable errorDrawable) {
        Log.e("Failed to load Bitmap from resource", getUrl());
        mImageView.setImageDrawable(
              DrawableHelper.withContext(getContext())
                            .withColorRes(R.color.field_error)
                            .withDrawable(errorDrawable)
                            .tint().get()
        );
        onUpdateFinished();
    }

    @Override public void onPrepareLoad(final Drawable placeHolderDrawable) {
        mImageView.setImageDrawable(placeHolderDrawable);
        onUpdateStarted();
    }

    @Nullable public String getUrl() {
        return BundleWrapper.wrap(getArguments()).getAs(EventParam.itemUrl().name, String.class);
    }

    @Nullable public Object getImage() {
        return getArguments().get(EventParam.itemImage().name);
    }

    private void loadImage() {
        //int heightPx =
        //      mContext.getResources().getDimensionPixelSize(R.dimen.news_item_height);
        String imageUrl = getUrl();
        if (imageUrl != null) {
            Log.d("loadImage", imageUrl);
            mPicasso.load(Uri.parse(imageUrl))
                    //.centerCrop()
                    //.resize(heightPx, heightPx)
                    .error(R.drawable.img_placeholder)
                    //.placeholder(R.drawable.img_placeholder)
                    .into(this);
        }
        Object imageObject = getImage();
        if (imageObject != null) {
            Log.d("loadImage", imageObject);
            if (imageObject instanceof Integer) {
                mPicasso.load((int) imageObject)
                        //.centerCrop()
                        //.resize(heightPx, heightPx)
                        .error(R.drawable.img_placeholder)
                        //.placeholder(R.drawable.img_placeholder)
                        .into(this);
            } else if (imageObject instanceof Bitmap) {
                mImageView.setImageBitmap(
                      getResizeTransform(mImageView).transform((Bitmap) imageObject)
                );
            } else if (imageObject instanceof Drawable) {
                mImageView.setImageBitmap(
                      getResizeTransform(mImageView)
                            .transform(WidgetUtils.drawableToBitmap((Drawable) imageObject))
                );
            }
        }
    }

    public static Transformation getResizeTransform(ImageView targetImageView) {
        return new Transformation() {
            @Override
            public Bitmap transform(Bitmap source) {
                int targetWidth = targetImageView.getWidth();

                double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                int targetHeight = (int) (targetWidth * aspectRatio);
                Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                if (result != source) {
                    // Same bitmap is returned if sizes are the same
                    source.recycle();
                }
                return result;
            }

            @Override
            public String key() {
                return "transformation" + " desiredWidth";
            }
        };
    }

    @Override public ScreenComponent getComponent() {
        return mComponent;
    }

    @Override public ScreenComponent createComponent() {
        mComponent = ((MainActivity) getActivity()).getComponent()
                                                   .plus(new ScreenModule(TAG));
        return mComponent;
    }

    @Override public void releaseComponent() {
        mComponent = null;
    }
}
