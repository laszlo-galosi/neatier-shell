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

package com.neatier.shell.appframework;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import butterknife.BindView;
import com.fernandocejas.arrow.collections.Lists;
import com.neatier.commons.helpers.BundleWrapper;
import com.neatier.shell.R;
import com.neatier.shell.eventbus.EventParam;
import java.util.List;
import trikita.log.Log;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link WebViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WebViewFragment extends BaseFragment implements TaggedBaseFragment {

    public static final String TAG = "WebViewFragment";
    @BindView(R.id.web_view) WebView mWebView;
    private WebViewClient mWebViewClient;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided bundle parameters.
     *
     * @param bundleWrapper {@link BundleWrapper} containing some initialization arguments.
     * @return A new instance of fragment.
     */
    public static WebViewFragment newInstance(final BundleWrapper bundleWrapper) {
        WebViewFragment fragment = new WebViewFragment();
        Log.w("newInstance ", bundleWrapper);
        if (bundleWrapper != null) {
            BundleWrapper copyBundle = bundleWrapper.copy();
            fragment.setArguments(copyBundle.getBundle());
        } else {
            fragment.setArguments(new Bundle());
        }
        return fragment;
    }

    public WebViewFragment() {
        // Required empty public constructor
    }

    @Override
    protected void onInflateLayout(final View contentView, final Bundle savedInstanceState) {
        super.onInflateLayout(contentView, savedInstanceState);
        setHasOptionsMenu(false);
        setupWebView();
    }

    @Override public List<Integer> getDisplayableToolbarIcons() {
        return Lists.newArrayList();
    }

    @Override
    protected void initialize() {
        super.initialize();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
        mWebView.pauseTimers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
        mWebView.resumeTimers();
        mWebView.loadUrl(getUrl());
    }

    @Override
    public String getFragmentTag() {
        return String.format("%s_%s", TAG, getUrl());
    }

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_webview;
    }

    @Override
    public boolean hasProgressView() {
        return false;
    }

    @Override
    public void onModelReady() {
        mLayoutReady = true;
        Log.d("onModelReady", mLayoutReady);
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

    public String getUrl() {
        return BundleWrapper.wrap(getArguments()).getAs(EventParam.itemUrl().name, String.class);
    }

    private void setupWebView() {
        mWebViewClient = new WebViewClient();
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mWebView.setVisibility(View.VISIBLE);
            }
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        setWebViewSettings(mWebView.getSettings());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setWebViewSettings(WebSettings webSettings) {
        webSettings.setLoadWithOverviewMode(false);
        webSettings.setUseWideViewPort(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webSettings.setDisplayZoomControls(false);
        }
    }
}
