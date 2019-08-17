package com.watchback2.android.activities;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.adobe.primetime.va.plugins.nielsen.AdobeNielsenAPI;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.databinding.ActivityOptoutBinding;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkEventsBroadcastReceiver;
import com.watchback2.android.utils.PerkUtils;

/**
 * Created by perk on 06/04/18.
 * Common WebView Activity for showing any Web page in the app
 */

public class WatchbackOptOutActivity extends BaseThemeableActivity{

    private PerkEventsBroadcastReceiver mPerkEventsBroadcastReceiver;
    private String LOG_TAG = "WatchbackOptOutActivity";
    private ActivityOptoutBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_optout);

        mBinding.actionBar.idBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (!AppUtility.isNetworkAvailable(this)) {
            PerkLogger.d(LOG_TAG, "Returning as network is unavailable!");
            finish();
        }

        // Register broadcast receiver for finish action on log out
        mPerkEventsBroadcastReceiver = new PerkEventsBroadcastReceiver(this);
        mPerkEventsBroadcastReceiver.registerReceiver();
        ///



        // Clear history and cache before loading anything on th page.
        mBinding.webviewoutput.clearHistory();
        mBinding.webviewoutput.clearFormData();
        mBinding.webviewoutput.clearCache(true);

        mBinding.webviewoutput.getSettings().setJavaScriptEnabled(true);

        mBinding.webviewoutput.setInitialScale(1);
        mBinding.webviewoutput.getSettings().setBuiltInZoomControls(true);
        mBinding.webviewoutput.getSettings().setSupportZoom(true);
        mBinding.webviewoutput.getSettings().setDisplayZoomControls(false);
        mBinding.webviewoutput.getSettings().setLoadWithOverviewMode(true);
        mBinding.webviewoutput.getSettings().setUseWideViewPort(true);
        mBinding.webviewoutput.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        // Add Javascript interface on the page

        PerkLogger.d(LOG_TAG, "Injected Javascript interface! url:" + AdobeNielsenAPI.optOutUrlString());

        mBinding.webviewoutput.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.indexOf("nielsen") == 0) {
                    AdobeNielsenAPI.userOptOut(url);
                    finish();
                    return false;
                }
                else if (url.indexOf("http") == 0) {
                    view.loadUrl(url);
                }
                return true;
            }

        });
        mBinding.webviewoutput.loadUrl(AdobeNielsenAPI.optOutUrlString());

    }


    private void initToolbar(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        if (ab != null) {
            // Disable the Up button
            ab.setHomeButtonEnabled(false);
            ab.setDisplayShowHomeEnabled(false);
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setDisplayShowTitleEnabled(false);
        }

        if (TextUtils.isEmpty(title)) {
            title = getResources().getString(R.string.app_name);
        }

        PerkUtils.setTitleToolbar(this, title, false);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPerkEventsBroadcastReceiver != null) {
            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(
                        mPerkEventsBroadcastReceiver);
                mPerkEventsBroadcastReceiver = null;
            } catch (IllegalArgumentException e) {
                PerkLogger.e(LOG_TAG, "Error while un-registering broadcast receiver", e);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();

        if (mBinding.webviewoutput != null) {
            try {
                mBinding.webviewoutput.destroy();
            } catch (Exception e) {
                // ignored
            }
           // mBinding.webviewoutput = null;
        }
    }



}
