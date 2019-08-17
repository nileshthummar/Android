package com.watchback2.android.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.leanplum.Leanplum;
import com.perk.request.auth.AuthAPIRequestController;
import com.perk.util.PerkLogger;
import com.perk.webview.Constants;
import com.perk.webview.PerkWebViewController;
import com.singular.sdk.Singular;
import com.watchback2.android.BuildConfig;
import com.watchback2.android.R;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.controllers.PerkUserManager;
import com.watchback2.android.databinding.ActivityWebviewBinding;
import com.watchback2.android.helper.UserInfoValidator;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkEventsBroadcastReceiver;
import com.watchback2.android.utils.PerkUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by perk on 06/04/18.
 * Common WebView Activity for showing any Web page in the app
 */

public class WatchbackWebViewActivity extends BaseThemeableActivity {

    public static final String PERK_PAGE_TYPE = "perk_page_type";

    public static final String INTENT_TITLE_EXTRA_KEY = "title";

    public static final String INTENT_PAGE_NEEDS_TOKEN = "page_needs_token";

    public static final String INTENT_IS_FOR_REDEMPTION = "page_for_redemption";

    /*private static final String ENVIRONMENT = "https://" + ("prod".equalsIgnoreCase(
            BuildConfig.buildMode) ? "" : "web-dev.");*/

    /*private static final String WATCHBACK_ENVIRONMENT = "https://" + ("prod".equalsIgnoreCase(
            BuildConfig.buildMode) ? "www." : "dev.");*/

    private static final String WATCHBACK_ENVIRONMENT_NO_WWW = "https://" + ("prod".equalsIgnoreCase(
            BuildConfig.buildMode) ? "" : "dev.");

    public static final String WATCHBACK_FORGOT_PASSWORD_URL =
            WATCHBACK_ENVIRONMENT_NO_WWW + "watchback.com/auth/forgot-password" ;

    public static final String PROFILE_URL = WATCHBACK_ENVIRONMENT_NO_WWW + "watchback.com/profile" ;

/*
    private static final String WATCHBACK_ENVIRONMENT_NO_WWW = "https://" + ("prod".equalsIgnoreCase(
            BuildConfig.buildMode) ? "dtjph35benjeu.cloudfront.net/" : "dev.watchback.com/");

    public static final String WATCHBACK_FORGOT_PASSWORD_URL =
            WATCHBACK_ENVIRONMENT_NO_WWW + "auth/forgot-password" ;

    public static final String PROFILE_URL = WATCHBACK_ENVIRONMENT_NO_WWW + "profile" ;
*/

    private static final String LOG_TAG = "WatchbackWebViewActivity";

    private PerkEventsBroadcastReceiver mPerkEventsBroadcastReceiver;

    private boolean mIsForRedemption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWebviewBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_webview);

        binding.actionBar.idBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (!AppUtility.isNetworkAvailable(this)) {
            PerkLogger.d(LOG_TAG, "Returning as network is unavailable!");
            finish();
        }

        Intent intent = getIntent();

        final String title = intent != null ? intent.getStringExtra(INTENT_TITLE_EXTRA_KEY) : null;

        mIsForRedemption = intent != null && intent.getBooleanExtra(INTENT_IS_FOR_REDEMPTION,
                false);

        initToolbar(title);

        // Register broadcast receiver for finish action on log out
        mPerkEventsBroadcastReceiver = new PerkEventsBroadcastReceiver(this){
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);

                if (!mIsForRedemption) {
                    PerkLogger.d(LOG_TAG,
                            "mPerkEventsBroadcastReceiver: Ignoring broadcast as page is not for "
                                    + "redemption!");
                    return;
                }

                PerkLogger.d(LOG_TAG,
                        "mPerkEventsBroadcastReceiver: Got broadcast intent: " + intent);

                if (intent == null) {
                    return;
                }

                // Make sure action is valid
                final String action = intent.getAction();
                if (TextUtils.isEmpty(action)) {
                    return;
                }

                if (Constants.ACTION_POINTS_UPDATED.equals(action) && intent.hasExtra(
                        Constants.EXTRA_KEY_PARTNER_NAME)) {
                    String partnerName = intent.getStringExtra(Constants.EXTRA_KEY_PARTNER_NAME);
                    long rewardPrice = intent.getLongExtra(Constants.EXTRA_KEY_POINTS, 0L);

                    PerkLogger.d(LOG_TAG,
                            "mPerkEventsBroadcastReceiver: on Redemption: partnerName="
                                    + partnerName + " rewardPrice=" + rewardPrice + " title: " + title);

                    try {
                        if ("Donate".equalsIgnoreCase(title)) {

                            /////
                            HashMap<String, Object> contextData = new HashMap<String, Object>();
                            contextData.put("tve.title", "Donations:Checkout:Complete");
                            contextData.put("tve.userpath", "Donations:Checkout:Complete");
                            contextData.put("tve.contenthub", "Donations");
                            contextData.put("tve.partnertype", "Donations");
                            contextData.put("tve.partner", partnerName);
                            contextData.put("tve.redemptionvalue", rewardPrice + "");
                            contextData.put("tve.rewardsconfirmation", "true");

                            AdobeTracker.INSTANCE.trackState("Donations:Checkout:Complete",
                                    contextData);
                            /////
                            ///
                            HashMap<String, Object> contextDataAppsFlyer = new HashMap<String, Object>();
                            contextDataAppsFlyer.put("partner_name",partnerName);
                            AdobeTracker.INSTANCE.trackAppsFlyerEvent(context, "donate_points",contextDataAppsFlyer);
                            ///

                            // Singular: donate_points - user redeems points by donating to b&gc
                            Singular.event("donate_points");
                        } else {
                            /////
                            HashMap<String, Object> contextData = new HashMap<String, Object>();
                            contextData.put("tve.title", "Rewards:Checkout:Complete");
                            contextData.put("tve.userpath", "Rewards:Checkout:Complete");
                            contextData.put("tve.contenthub", "Rewards");
                            contextData.put("tve.partnertype", "Rewards");
                            contextData.put("tve.partner", partnerName);
                            contextData.put("tve.redemptionvalue", rewardPrice + "");
                            contextData.put("tve.rewardsconfirmation", "true");

                            AdobeTracker.INSTANCE.trackState("Rewards:Checkout:Complete",
                                    contextData);
                            /////
                            ///
                            HashMap<String, Object> contextDataAppsFlyer = new HashMap<String, Object>();
                            contextDataAppsFlyer.put("partner_name",partnerName);
                            AdobeTracker.INSTANCE.trackAppsFlyerEvent(context, "redeem_gift_card",contextDataAppsFlyer);
                            ///
                        }
                    } catch (Exception e) {
                        // ignored
                    }
                }
            }
        };

        mPerkEventsBroadcastReceiver.registerReceiver();

        Fragment perkWebPageFragment = getPerkWebPageFragment(intent);

        if (perkWebPageFragment == null) {
            return;
        }

        // Replace the fragment in the current activity
        try {

            // Replace the fragment in the activity
            final FragmentManager fragmentManager = getSupportFragmentManager();
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // NOTE: We should use Constants.FRAGMENT_TAG_PERK_WEB_VIEW only when adding/replacing fragment in activity.
            //       This way we can reuse the fragment in the activity if there is aleady one that hosts perk web page
            //       and can load the new page in the same fragment. Current SDK tries to find the fragment by this tag
            //       and if available then reuses to load new page.

            fragmentTransaction.replace(
                    R.id.fragment_container,                 // ID of the view in which fragment should be replaced
                    perkWebPageFragment,                     // fragment that hosts Perk web page
                    Constants.FRAGMENT_TAG_PERK_WEB_VIEW     // Tag that should be used while replacing fragment
            );

            // Allow commit with state loss as well.
            fragmentTransaction.commitAllowingStateLoss();

        } catch (final IllegalStateException e) {
            PerkLogger.e(LOG_TAG, "Error while opening WebView fragment.", e);
        }
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

    private Fragment getPerkWebPageFragment(Intent intent) {
        // Get user's current authenticated session
        String accessToken;

        if (UserInfoValidator.isAuthenticated(
                AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(
                        getApplicationContext()))) {
            accessToken = PerkUserManager.getUserAccessToken(this);
        } else {
            accessToken = null;
        }

        Fragment perkWebPageFragment;

        String pageURL = intent != null ? intent.getDataString() : null;

        boolean pageNeedsToken = intent != null && intent.getBooleanExtra(INTENT_PAGE_NEEDS_TOKEN,
                false);

        if (TextUtils.isEmpty(pageURL)) {
            pageURL = "";
        }

        PerkLogger.d(LOG_TAG,
                "Got URL: " + pageURL + "\npageNeedsToken=" + pageNeedsToken + "\nmIsForRedemption:"
                        + mIsForRedemption);

        pageURL = pageURL.replaceFirst("perk_wb_https", "https");
        pageURL = pageURL.replaceFirst("perk_wb_http", "http");

        // Check for protocol-handler if required:
        if (pageURL.startsWith("watchback://")) {
            finish();
            AppUtility.handleProtocolUri(pageURL);
            return null;
        } else if (pageURL.startsWith("leanplum://")) {
            // Special handling for 'leanplum://' featured-content URLs

            pageURL = pageURL.replaceFirst("leanplum://", "");

            PerkLogger.d(LOG_TAG,
                    "Firing Leanplum event 'featured_url_tap' with url param: " + pageURL);

            Map<String, String> params = new HashMap<>(1);
            params.put("url", pageURL);

            Leanplum.track("featured_url_tap", params);

            finish();
            return null;
        }

        // Check if the URL should be opened outside of the app:
        // As per https://jira.rhythmone.com/browse/PEWAN-346, the URL will be opened outside of
        // the app if it has 'safari=1'
        if (StringUtils.containsIgnoreCase(pageURL, "safari=1")) {
            PerkLogger.d(LOG_TAG, "Launching Intent for handling URL in external app(s) as it contains 'safari=1'");
            Intent externalIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(pageURL));
            externalIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(externalIntent);
                finish();
                return null;
            } catch (ActivityNotFoundException ex) {
                PerkLogger.e(LOG_TAG, "No activity found to handle URL: " + pageURL
                        + " - proceeding to handle in internal WebView...", ex);
            }
        }

        if (!pageURL.startsWith("http")) {
            pageURL = "http://" + pageURL;
        }

        if (pageURL.endsWith("/")) {
            pageURL = pageURL.substring(0, pageURL.lastIndexOf("/"));
        }

        PerkLogger.d(LOG_TAG, "Replaced URL: " + pageURL);

        // Special handling for hyper-links from strings.xml for terms / privacy-policy & VPPA links:
        if (pageURL.contains(getString(R.string.terms_url_identifier))) {
            pageURL = WATCHBACK_ENVIRONMENT_NO_WWW + getString(R.string.terms_url)/*"terms"*/;

            // Update title:
            PerkUtils.setTitleToolbar(this, "Terms", false);

        } else if (pageURL.contains(getString(R.string.privacy_url_identifier))) {
            pageURL = WATCHBACK_ENVIRONMENT_NO_WWW + getString(R.string.privacy_url)  /*"privacy"*/;

            // Update title:
            PerkUtils.setTitleToolbar(this, "Privacy Policy", false);

        } else if (pageURL.contains(getString(R.string.vppa_url_identifier))) {
            pageURL = WATCHBACK_ENVIRONMENT_NO_WWW + getString(R.string.vppa_url) /*"vppa"*/;

            // Update title:
            PerkUtils.setTitleToolbar(this, getString(R.string.vppa), false);

        }

        if (pageURL.contains("?")) {
            pageURL = pageURL + "&source=app";
        } else {
            pageURL = pageURL + "?source=app";
        }

        PerkLogger.d(LOG_TAG, "Replaced URL after special-handling for custom hyperlinks: " + pageURL);

        // Remove access_token if already present:
        if (pageURL.contains("access_token")) {
            PerkLogger.d(LOG_TAG,
                    "Setting accessToken value to null as it is already included in the URL");
            accessToken = null;
        }

        // Remove access_token if this page doesn't need it
        if (!pageNeedsToken) {
            PerkLogger.d(LOG_TAG, "Setting accessToken value to null as URL doesn't require it");
            accessToken = null;
        }

        if (!pageNeedsToken && !pageURL.toLowerCase(Locale.ENGLISH).contains("perk.com")) {
            PerkLogger.d(LOG_TAG,
                    "Setting accessToken value to null as URL doesn't belong to perk.com");
            accessToken = null;
        }

        PerkWebViewController.INSTANCE.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);

        perkWebPageFragment =
                PerkWebViewController.INSTANCE.getPerkWebPageFragment(
                        this,       // activity in which Perk web page fragment would be hosted
                        pageURL,        // URL of the page that should be opened
                        accessToken,     // user's authenticated session's access token
                        mIsForRedemption // true if this page is for redemption
                );

        return perkWebPageFragment;
    }

    @Override
    public void finish() {
        super.finish();
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
    public void onBackPressed() {
        if (!PerkWebViewController.INSTANCE.handleBackPressForWebView(this)) {
            super.onBackPressed();
        } else {
            PerkLogger.d(LOG_TAG, "Back-press event was handled by Perk web page fragment");
        }
    }
}
