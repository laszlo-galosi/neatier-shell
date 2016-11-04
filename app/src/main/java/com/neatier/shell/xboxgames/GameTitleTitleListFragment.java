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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import butterknife.BindView;
import com.airbnb.epoxy.EpoxyAdapter;
import com.neatier.commons.helpers.BundleWrapper;
import com.neatier.commons.helpers.KeyValuePairs;
import com.neatier.commons.helpers.Leakable;
import com.neatier.commons.helpers.Preconditions;
import com.neatier.shell.R;
import com.neatier.shell.activities.MainActivity;
import com.neatier.shell.appframework.BaseFragment;
import com.neatier.shell.appframework.TaggedBaseFragment;
import com.neatier.shell.data.network.ApiSettings;
import com.neatier.shell.eventbus.Event;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.eventbus.EventKey;
import com.neatier.shell.eventbus.Item;
import com.neatier.shell.internal.di.HasComponent;
import com.neatier.shell.xboxgames.di.XboxComponent;
import com.neatier.shell.xboxgames.di.XboxModule;
import com.neatier.widgets.recyclerview.ItemClickSupport;
import com.neatier.widgets.recyclerview.ItemSpacingDecoration;
import com.squareup.picasso.Picasso;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import trikita.log.Log;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link GameTitleTitleListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameTitleTitleListFragment extends BaseFragment implements
                                                             TaggedBaseFragment,
                                                             HasComponent<XboxComponent>,
                                                             GameTitleView, Leakable,
                                                             ItemClickSupport.OnItemClickListener,
                                                             ViewTreeObserver
                                                                   .OnGlobalLayoutListener,
                                                             SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "GameTitleTitleListFragment";

    @Inject GamePresenter mWorkbenchPresenter;
    @Inject GameTitleItemAdapter mListingAdapter;

    @BindView(R.id.rv_main_content) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_to_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;

    private XboxComponent mXboxComponent;
    private RecyclerView.ItemDecoration mItemSpacingDecoration;
    private KeyValuePairs<String, Object> mApiParams = new KeyValuePairs<>(5);

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided bundle parameters.
     *
     * @param bundleWrapper {@link BundleWrapper} containing some initialization arguments.
     * @return A new instance of fragment.
     */
    public static GameTitleTitleListFragment newInstance(final BundleWrapper bundleWrapper) {
        GameTitleTitleListFragment homeFragment = new GameTitleTitleListFragment();
        Log.w("newInstance ", bundleWrapper);
        if (bundleWrapper != null) {
            BundleWrapper copyBundle = bundleWrapper.copy();
            homeFragment.setArguments(copyBundle.getBundle());
        } else {
            homeFragment.setArguments(new Bundle());
        }
        return homeFragment;
    }

    public GameTitleTitleListFragment() {
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
        this.mWorkbenchPresenter.setView(this);
        loadStateArguments();
    }

    @Override
    public void onPause() {
        mWorkbenchPresenter.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mWorkbenchPresenter != null) {
            mWorkbenchPresenter.destroy();
        }
        releaseComponent();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mLayoutReady = false;
        mWorkbenchPresenter.resume();
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
        return String.format("%s_%s", TAG, getListType());
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_list_w_swp_refresh;
    }

    @Override
    public String getToolbarTitle() {
        return getString(getListType().equals(ApiSettings.LIST_XBOXONE)
                         ? R.string.action_xboxone
                         : R.string.action_xbox360);
    }

    @Override
    public boolean hasProgressView() {
        return true;
    }

    protected void bindProgressView(View fragmentView) {
        Log.v(getFragmentTag(), "bindProgressView");
        setupSwipeRefreshLayout();
    }

    @Override public void showProgress() {
        Log.v(getFragmentTag(), "showProgress");
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override public void hideProgress() {
        Log.v(getFragmentTag(), "hideProgress");
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onUpdateFinished(Throwable... errors) {
        super.onUpdateFinished(errors);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onModelReady() {
        mLayoutReady = true;
        Log.d("onModelReady", mLayoutReady);
        mListingAdapter.setDataSet(mWorkbenchPresenter.getItems());
        mRecyclerView.scrollToPosition(0);
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
    public Context getContext() {
        return getActivity();
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
        Log.d("onGlobalLayout", "layourReady", mLayoutReady, "initWithError", mInitWithError);
        if (mLayoutReady || mInitWithError) {
            mRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            hideProgress();
        } else {
            if (!mWaitingForModelUpdate) {
                getApiParams().put(ApiSettings.KEY_API_LIST_TYPE, getListType())
                              .put(ApiSettings.KEY_API_XAUTH, getString(R.string.xboxapi_auth_key))
                              .put(ApiSettings.KEY_API_XUID,
                                   getString(R.string.xboxapi_default_userid))
                ;
                mWorkbenchPresenter.initialize();
            }
        }
    }

    private String getListType() {
        return BundleWrapper.wrap(getArguments())
                            .getAs(ApiSettings.KEY_API_LIST_TYPE, String.class,
                                   ApiSettings.LIST_XBOXONE);
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
            mWorkbenchPresenter.initialize();
        }
    }

    @SuppressWarnings("unchecked")
    private void setupAdapter() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLongClickable(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(getOrCreateDivider());
        //final ItemClickSupport itemClickSupport = ItemClickSupport.addTo(mRecyclerView);
        //itemClickSupport.setOnItemClickListener(this);
        mListingAdapter.setDataSet(mWorkbenchPresenter.getItems());
        mRecyclerView.swapAdapter(mListingAdapter, false);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override public KeyValuePairs<String, Object> getApiParams() {
        return mApiParams;
    }

    @Override
    public XboxComponent getComponent() {
        return mXboxComponent;
    }

    @Override public XboxComponent createComponent() {
        mXboxComponent = ((MainActivity) getActivity()).getComponent()
                                                       .plus(new XboxModule());
        return mXboxComponent;
    }

    @Override public void releaseComponent() {
        mXboxComponent = null;
    }

    private RecyclerView.ItemDecoration getOrCreateDivider() {
        if (mItemSpacingDecoration == null) {
            mItemSpacingDecoration = new ItemSpacingDecoration(getContext(), 8);
        }
        return mItemSpacingDecoration;
    }

    public static class GameTitleItemAdapter extends EpoxyAdapter {

        @Inject Picasso picassoInstance;
        @Inject Context context;

        public GameTitleItemAdapter(final Picasso picassoInstance, final Context context) {
            this.picassoInstance = picassoInstance;
            this.context = context;
        }

        @Override protected void notifyModelsChanged() {
            super.notifyModelsChanged();
        }

        public void setDataSet(List<GameTitleItemModel> dataSet) {
            // We are going to use automatic diffing, so we just have to enable it first
            //enableDiffing();
            Observable.from(dataSet)
                      .subscribe(model -> {
                          Log.d("addModel", model);
                          addModel(model.with(picassoInstance, context));
                      });
        }
    }
}
