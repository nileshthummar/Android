package com.watchback2.android.activities;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.appsflyer.AppsFlyerLib;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.controllers.ChannelsManager;
import com.watchback2.android.controllers.HomeScreenItemsListManager;
import com.watchback2.android.controllers.SplashCarouselDataManager;
import com.watchback2.android.databinding.ActivityLaunchBinding;
import com.watchback2.android.helper.Bindings;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkUtils;


/**
 * The Splash-screen activity
 */
public class LaunchActivity extends BaseThemeableActivity {

    private static final String LOG_TAG = "LaunchActivity";

    private static final int ANIMATION_DELAY = 500;

    private static final int VALIDATION_START_DELAY = 3500;

    private ActivityLaunchBinding mActivityLaunchBinding;

    private SplashCarouselDataManager mCarouselDataManager;

    private String logoURL = "";

    private String backgroundURL = "";

    private String description = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // AppsFlyer DeepLinking:
        AppsFlyerLib.getInstance().sendDeepLinkData(this);

        // Fetch the channels list on-creation... so that it is available the next time
        // when any one wants it
        if (AppUtility.isNetworkAvailable(this, false)) {
            ChannelsManager.INSTANCE.fetchAllChannelsListWithoutVideos(getApplicationContext(),
                    PerkUtils.isUserLoggedIn(), null);
        }

        mActivityLaunchBinding = DataBindingUtil.setContentView(this, R.layout.activity_launch);
        mActivityLaunchBinding.idSplashContainer.getLayoutTransition().setDuration(ANIMATION_DELAY);

        mCarouselDataManager = new SplashCarouselDataManager();

        PerkLogger.d(getClass().getSimpleName(),
                "onCreate(): isAppRunning: " + PerkUtils.sApplication.isAppRunning()
                        + " isProtocolUriIntent(): " + isProtocolUriIntent());

        // toggle visibility of elements on splash screen:
        mActivityLaunchBinding.idProgressBar.setVisibility(View.VISIBLE);
        if (isProtocolUriIntent() && PerkUtils.sApplication.isAppRunning()) {
            // Do not show UI in this case:
            mActivityLaunchBinding.idAppLogo.setVisibility(View.GONE);
            mActivityLaunchBinding.idBgImage.setVisibility(View.GONE);
            mActivityLaunchBinding.idSplashContainer.setBackground(null);

            new Handler().postDelayed(this::checkAppStatusAndRedirect,
                    VALIDATION_START_DELAY);

        } else {
            TypedArray a = getTheme().obtainStyledAttributes(
                    new int[]{R.attr.viewBackground});

            int color = a.getColor(0, 0);
            if (color != 0) {
                mActivityLaunchBinding.idSplashContainer.setBackgroundColor(color);
            } else {
                mActivityLaunchBinding.idSplashContainer.setBackground(null);
            }

            a.recycle();

            mCarouselDataManager.fetchCarouselData(getApplicationContext(), false,
                    carouselData -> {

                        if (carouselData != null && carouselData.getItems() != null
                                && !carouselData.getItems().isEmpty()) {

                            final String logoImage = carouselData.getItems().get(0).getImage();
                            final String bgImage = carouselData.getItems().get(0).getImage2();

                            logoURL = logoImage;
                            backgroundURL = bgImage;
                            description = carouselData.getItems().get(0).getFields().getTxt1();

                            AppUtility.preloadImage(getApplicationContext(),
                                    logoImage,
                                    new RequestListener<Drawable>() {
                                        private int requestsCompleted = 0;
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model, Target<Drawable> target,
                                                boolean isFirstResource) {
                                            requestsCompleted++;
                                            PerkLogger.w(LOG_TAG, "onLoadFailed! " + logoImage, e);

                                            if (requestsCompleted == 2) {
                                                onComplete();
                                            } else {
                                                loadNextImage();
                                            }
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource,
                                                Object model, Target<Drawable> target,
                                                DataSource dataSource, boolean isFirstResource) {
                                            requestsCompleted++;
                                            PerkLogger.d(LOG_TAG, "onResourceReady! " + logoImage);
                                            if (requestsCompleted == 2) {
                                                onComplete();
                                            } else {
                                                loadNextImage();
                                            }
                                            return false;
                                        }

                                        private void loadNextImage() {
                                            AppUtility.preloadImage(getApplicationContext(),
                                                    bgImage, this);
                                        }

                                        private void onComplete() {
                                            mActivityLaunchBinding.idProgressBar.setVisibility(View.GONE);

                                            mActivityLaunchBinding.idAppLogo.setVisibility(View.VISIBLE);
                                            mActivityLaunchBinding.idBgImage.setVisibility(View.VISIBLE);

                                            Bindings.setImageUrl(mActivityLaunchBinding.idAppLogo,
                                                    logoImage);

                                            Bindings.setImageUrl(mActivityLaunchBinding.idBgImage,
                                                    bgImage);

                                            new Handler().postDelayed(() -> checkAppStatusAndRedirect(),
                                                    VALIDATION_START_DELAY);
                                        }
                                    });
                        } else {
                            new Handler().postDelayed(this::checkAppStatusAndRedirect,
                                    VALIDATION_START_DELAY);
                        }
                    });
        }

        PerkUtils.getSharedPreferencesEditor().putInt("callstate", 0);
        PerkUtils.getSharedPreferencesEditor().commit();

        // TODO: Check with Nilesh if this is needed:
        // Add code to print out the key hash
        try {
/*
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.watchback2.android",
                    PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
            */
            AdobeTracker.INSTANCE.trackAdobeLoadingEvents();
        } catch (Exception e) {
        }
    }

    @Override
    protected void setAppropriateTheme() {
        // Always use night mode for this
        if (PerkUtils.sApplication.isAppRunning()) {
            setTheme(R.style.AppTheme_Transparent);
        } else {
            setTheme(R.style.AppTheme_Fullscreen);
        }
    }

    private void checkAppStatusAndRedirect() {
        if (isProtocolUriIntent()) {
            HomeScreenItemsListManager.INSTANCE.setProtocolUri(getIntent().getDataString());
        }

        if (PerkUtils.isUserLoggedIn()) {
            Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(LaunchActivity.this, WalkthroughActivity.class);
            intent.putExtra(WalkthroughActivity.BACKGROUND_URL, backgroundURL);
            intent.putExtra(WalkthroughActivity.LOGO_URL, logoURL);
            intent.putExtra(WalkthroughActivity.DESCRIPTION_TXT, description);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, R.anim.no_anim_transition);
            finish();
        }
    }

    private boolean isProtocolUriIntent() {
        Intent launchIntent = getIntent();

        PerkLogger.d(getClass().getSimpleName(),
                "Got Intent: " + AppUtility.safeReflectionToString(launchIntent));

        return launchIntent != null && Intent.ACTION_VIEW.equalsIgnoreCase(launchIntent.getAction());
    }

    /*@SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super().
    }*/

}
