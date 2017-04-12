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

package com.neatier.shell.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.fernandocejas.arrow.optional.Optional;
import com.neatier.commons.helpers.BundleWrapper;
import com.neatier.commons.helpers.Leakable;
import com.neatier.commons.helpers.Preconditions;
import com.neatier.shell.R;
import com.neatier.shell.activities.MainActivity;
import com.neatier.shell.appframework.BaseFragment;
import com.neatier.shell.appframework.TaggedBaseFragment;
import com.neatier.shell.eventbus.Event;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.eventbus.EventKey;
import com.neatier.shell.eventbus.EventParam;
import com.neatier.shell.eventbus.Item;
import com.neatier.shell.home.di.HomeComponent;
import com.neatier.shell.home.di.HomeModule;
import com.neatier.shell.internal.di.HasComponent;
import com.neatier.widgets.recyclerview.DrawableItemDecoration;
import com.neatier.widgets.recyclerview.ItemClickSupport;
import com.neatier.widgets.recyclerview.ItemViewHolderBase;
import com.neatier.widgets.recyclerview.ItemWidget;
import com.neatier.widgets.recyclerview.ItemWidgetAdapter;
import com.neatier.widgets.renderers.TextRenderer;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import trikita.log.Log;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends BaseFragment implements
                                               TaggedBaseFragment,
                                               HasComponent<HomeComponent>,
                                               HomeView, Leakable,
                                               ItemClickSupport.OnItemClickListener,
                                               ViewTreeObserver.OnGlobalLayoutListener,
                                               SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "HomeFragment";

    @Inject HomePresenter mHomePresenter;
    @Inject Picasso mPicassoInstance;

    @BindView(R.id.rv_main_content) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_to_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;

    private HomeItemAdapter mListingAdapter;
    private HomeComponent homeComponent;
    private DrawableItemDecoration mDividerDecoration;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided bundle parameters.
     *
     * @param bundleWrapper {@link BundleWrapper} containing some initialization arguments.
     * @return A new instance of fragment.
     */
    public static HomeFragment newInstance(final BundleWrapper bundleWrapper) {
        HomeFragment homeFragment = new HomeFragment();
        Log.w("newInstance ", bundleWrapper);
        if (bundleWrapper != null) {
            BundleWrapper copyBundle = bundleWrapper.copy();
            homeFragment.setArguments(copyBundle.getBundle());
        } else {
            homeFragment.setArguments(new Bundle());
        }
        return homeFragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    protected void onInflateLayout(final View contentView, final Bundle savedInstanceState) {
        super.onInflateLayout(contentView, savedInstanceState);
        setHasOptionsMenu(false);
        setupSwipeRefreshLayout();
    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(
              ContextCompat.getColor(getActivity(), R.color.white));
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.initialize();
        mLayoutReady = false;
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        setupAdapter();
    }

    @Override
    protected void initialize() {
        super.initialize();
        createComponent().inject(this);
        //Now we hava a presenter...
        this.mHomePresenter.setView(this);
        loadStateArguments();
    }

    @Override
    public void onPause() {
        mHomePresenter.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mHomePresenter != null) {
            mHomePresenter.destroy();
        }
        releaseComponent();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mLayoutReady = false;
        mHomePresenter.resume();
    }

    @Override
    protected Boolean filterEvent(final BundleWrapper event) {
        EventBuilder eventBuilder = EventBuilder.create(event.getBundle());
        Item item = eventBuilder.getItem();
        Event eventType = eventBuilder.getEventType();
        //Todo: implement event filtering for the specific Item and/or Event.
        return Boolean.FALSE;
    }

    @Override
    public void onAppEvent(final EventBuilder event) {
        super.onAppEvent(event);
        Event eventType = event.getEventType();
        //Todo: implement event handling for the specific Item and/or Event.
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_list_w_swp_refresh;
    }

    @Override
    public String getToolbarTitle() {
        return getString(R.string.app_name);
    }

    @Override
    public boolean hasProgressView() {
        return true;
    }

    @Override
    public void onModelReady() {
        mLayoutReady = true;
        Log.d("onModelReady", mLayoutReady);
        mListingAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void onUpdateFinished(Throwable... errors) {
        super.onUpdateFinished(errors);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean isResultFromCloud() {
        return true;
    }

    @Override
    protected boolean shouldRetainInstance() {
        return false;
    }

    @Override
    public void onItemClicked(final RecyclerView recyclerView, final int position, final View v) {
        Log.d("onItemClicked", position);
        Preconditions.checkArgument(v.getTag() != null && v.getTag() instanceof EventBuilder,
                                    "Missing tag for item view.");
        final EventBuilder itemTag = (EventBuilder) v.getTag();

        //On Item Click we display a simple snackbar message with the item text.
        EventBuilder.create(itemTag.getBundle())
                    .put(EventKey.ITEM_NAME, Item.SNACKBAR)
                    .put(EventKey.EVENT_NAME, Event.EVT_MESSAGE)
                    .send();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onGlobalLayout() {
        Log.d("onGlobalLayout", "layourReade", mLayoutReady, "initWithError", mInitWithError);
        if (mLayoutReady || mInitWithError) {
            mRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            hideProgress();
        } else {
            if (!mWaitingForModelUpdate) {
                mHomePresenter.initialize();
            }
        }
    }

    @Override
    public void clearLeakables() {
        //mListingAdapter.clearLeakables();
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(null);
        }
    }

    @Override
    public void onRefresh() {
        if (!mWaitingForModelUpdate) {
            mHomePresenter.initialize();
        }
    }

    @SuppressWarnings("unchecked")
    private void setupAdapter() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLongClickable(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(getOrCreateDivider());
        //final ItemClickSupport itemClickSupport = ItemClickSupport.addTo(mRecyclerView);
        //itemClickSupport.setOnItemClickListener(this);
        if (mListingAdapter == null) {
            mListingAdapter =
                  new HomeItemAdapter(getActivity(), mHomePresenter.getItems());
        }
        mRecyclerView.setAdapter(mListingAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public HomeComponent getComponent() {
        return homeComponent;
    }

    @Override public HomeComponent createComponent() {
        homeComponent = ((MainActivity) getActivity()).getComponent()
                                                      .plus(new HomeModule());
        return homeComponent;
    }

    @Override public void releaseComponent() {
        homeComponent = null;
    }

    private DrawableItemDecoration getOrCreateDivider() {
        if (mDividerDecoration == null) {
            mDividerDecoration =
                  new DrawableItemDecoration(R.drawable.list_divider, getContext()) {
                      @Override
                      public boolean isDecorated(final View view, final RecyclerView parent) {
                          return true;
                      }
                  };
        }
        return mDividerDecoration;
    }

    class HomeItemAdapter<T> extends ItemWidgetAdapter {
        public HomeItemAdapter(final Context context) {
            this(context, new ArrayList<>(30));
        }

        public HomeItemAdapter(final Context context, List<EventBuilder> dataSet) {
            super(context, R.layout.list_item_text_and_image, 72, dataSet);
        }

        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
              int viewType) {
            ItemWidget itemWidget =
                  new ItemWidget(getItemLayout(viewType), getItemHeight(viewType), mContext)
                        .withBackgroundColor(R.color.white)
                        .setContentClickable(true);
            itemWidget.initView(mContext);
            return new HomeItemViewHolder(itemWidget, mContext);
        }

        public Optional<T> getItem(int position) {
            return Optional.fromNullable((T) mDataset.get(position));
        }

        @Override public int getItemViewType(final int position) {
            return mItemLayout;
        }
    }

    class HomeItemViewHolder extends ItemViewHolderBase implements Target {

        //NightMode related container views
        @BindView(R.id.itemText) TextView mTitleView;
        @BindView(R.id.itemImage) ImageView mItemImageView;

        final TextRenderer mTitleRenderer = new TextRenderer();

        private Context mContext;

        public HomeItemViewHolder(final View itemView, final Context context) {
            super(itemView, context);
            mContext = context;
            ButterKnife.bind(this, itemView);
        }

        @Override public void bind(final Object dataItem) {
            super.bind(dataItem);
            final EventBuilder eventBuilder = (EventBuilder) dataItem;
            itemView.setTag(eventBuilder);
            String itemText =
                  eventBuilder.getCheckedParam(EventParam.PRM_ITEM_TEXT, String.class).get();
            int pos = eventBuilder.getCheckedParam(EventParam.PRM_ITEM_POS, Integer.class).get();
            //Todo: implement item click.
            itemView.setOnClickListener(v -> eventBuilder
                  .withItemAndType(Item.SNACKBAR, Event.EVT_MESSAGE)
                  .addParam(EventParam.PRM_VALUE,
                            String.format("%s on pos #%d", Event.find(Event.EVT_CLICK).get().name,
                                          pos))
                  .send()
            );
            //Now render all views, displayed with night mode shame is automatic, as far as
            mTitleRenderer.render(mTitleView, itemText);
            @DrawableRes int drawableRes = eventBuilder.getCheckedParam(EventParam.PRM_ITEM_IMAGE,
                                                                        Integer.class).get();
            try {
                int widthPx = mContext.getResources().getDimensionPixelSize(R.dimen.thumb_width);
                mPicassoInstance.load(drawableRes)
                                .centerCrop()
                                .resize(widthPx, widthPx)
                                .error(R.drawable.img_placeholder)
                                .placeholder(R.drawable.img_placeholder)
                                .into(mItemImageView);
            } catch (final Exception ex) {
                Log.e("Failed to load Bitmap from resource", drawableRes, ex);
            }
        }

        @Override public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
            mItemImageView.setImageBitmap(bitmap);
        }

        @Override public void onBitmapFailed(final Drawable errorDrawable) {
            Log.e("Failed to load Bitmap from resource", getData());
            mItemImageView.setImageDrawable(errorDrawable);
        }

        @Override public void onPrepareLoad(final Drawable placeHolderDrawable) {
            mItemImageView.setImageDrawable(placeHolderDrawable);
        }
    }
}
