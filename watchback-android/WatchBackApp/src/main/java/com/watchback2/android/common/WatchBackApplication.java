package com.watchback2.android.common;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.leanplum.Leanplum;
import com.leanplum.LeanplumActivityHelper;
import com.leanplum.annotations.Parser;
import com.leanplum.callbacks.StartCallback;
import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.request.auth.SecretKeys;
import com.perk.request.auth.SecretKeysRequester;
import com.perk.request.util.DeviceInfoUtil;
import com.perk.request.util.EnvironmentUtil;
import com.perk.util.PerkLogger;
import com.singular.sdk.Singular;
import com.singular.sdk.SingularConfig;
import com.watchback2.android.BuildConfig;
import com.watchback2.android.R;
import com.watchback2.android.activities.LaunchActivity;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.api.LeanplumAPIController;
import com.watchback2.android.controllers.WatchBackSettingsController;
import com.watchback2.android.models.LeanplumPostModel;
import com.watchback2.android.utils.DateTimeUtil;
import com.watchback2.android.utils.PerkPreferencesManager;
import com.watchback2.android.utils.PerkUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;
import io.fabric.sdk.android.Fabric;


/**
 * Created by Nilesh on 10/25/16.
 */

public class WatchBackApplication extends Application implements SecretKeysRequester,
        DefaultLifecycleObserver {

    private static final String LOG_TAG = "WatchBackApp";

    private boolean mAppRunning;

    private boolean mAppsFlyerStarted;

    /** Specifies whether AppsFlyer user-retention events for days 1,3,7 or 15 need to be tracked */
    private boolean mTrackAppsFlyerRetentionEvents;

    /** Holds the day number for which the AppsFlyer user-retention events would be tracked */
    private int mAppsFlyerRetentionDay;

    private boolean mLaunchEventTracked;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        mAppsFlyerStarted = false;
        mTrackAppsFlyerRetentionEvents = false;
        mAppsFlyerRetentionDay = 0;
        mLaunchEventTracked = false;

        // Use Debug log-level for unsigned builds:
        if (BuildConfig.DEBUG) {
            PerkLogger.setLogLevel(PerkLogger.LogLevel.DEGUG);
        } else {
            PerkLogger.setLogLevel(PerkLogger.LogLevel.ERROR);
        }

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        PerkPreferencesManager.INSTANCE.initSharedPreferences(this);

        AdobeTracker.INSTANCE.setContext(WatchBackApplication.this);
        PerkUtils.sApplication = WatchBackApplication.this;
        if ("prod".equalsIgnoreCase(BuildConfig.buildMode)) {
            EnvironmentUtil.INSTANCE.switchEnvironment(this, EnvironmentUtil.Environment.PROD);
        } else {
            EnvironmentUtil.INSTANCE.switchEnvironment(this, EnvironmentUtil.Environment.DEV);
        }

        try{
            PerkUtils.getAdvertisingId();
        }catch (Exception e){}

        // AppsFlyer init:
        AppsFlyerConversionListener conversionDataListener = new AppsFlyerConversionListener() {

            @Override
            public void onInstallConversionDataLoaded(Map<String, String> map) {
                PerkLogger.d(LOG_TAG, "onInstallConversionDataLoaded()");

                if (map != null) {
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        PerkLogger.d(LOG_TAG,
                                "onInstallConversionDataLoaded- key: " + entry.getKey() + " value: "
                                        + entry.getValue());
                    }

                    // Check for AppsFlyer user-retention, for non-organic installs:
                    String installStatus = map.get("af_status");

                    if (StringUtils.equalsIgnoreCase(installStatus, "Non-organic")
                            && map.containsKey("install_time")) {
                        if (Boolean.parseBoolean(map.get("is_first_launch"))) {
                            // first-launch:
                            mTrackAppsFlyerRetentionEvents = true;
                            mAppsFlyerRetentionDay = 1;
                        } else {
                            // Sample: install_time: 2019-01-08 07:19:31.479
                            String installTimeStr = map.get("install_time");
                            Date installDate = DateTimeUtil.getDateFromDateTimeString(installTimeStr);
                            if (installDate != null) {
                                PerkLogger.d(LOG_TAG,
                                        "Got install date: " + installDate.toString());

                                final Calendar installDateCalendar = Calendar.getInstance();
                                installDateCalendar.setTime(installDate);
                                DateTimeUtil.setTimePartToZero(installDateCalendar);

                                final Calendar currentDate = Calendar.getInstance(Locale.US);
                                DateTimeUtil.setTimePartToZero(currentDate);

                                // calculate difference:
                                long difference = currentDate.getTimeInMillis()
                                        - installDateCalendar.getTimeInMillis();

                                long differenceInDays = TimeUnit.MILLISECONDS.toDays(difference);

                                PerkLogger.d(LOG_TAG,
                                        "Got difference between install date & today as : "
                                                + difference + " differenceInDays: "
                                                + differenceInDays);

                                switch ((int)differenceInDays) {
                                    case 0:
                                    case 2:
                                    case 6:
                                    case 14:
                                        mAppsFlyerRetentionDay = (int)differenceInDays + 1;
                                        mTrackAppsFlyerRetentionEvents = true;
                                        break;
                                    default:
                                        mAppsFlyerRetentionDay = 0;
                                        mTrackAppsFlyerRetentionEvents = false;
                                }
                            }
                        }

                        PerkLogger.d(LOG_TAG,
                                "mTrackAppsFlyerRetentionEvents: " + mTrackAppsFlyerRetentionEvents
                                        + " mAppsFlyerRetentionDay: " + mAppsFlyerRetentionDay);
                    }

                    trackLaunchEventIfNeeded();

                    String uriScheme = map.get("af_dp");

                    // Could be any of the 'watchback://' formats, as mentioned in AppUtility.handleProtocolUri()
                    PerkLogger.d(LOG_TAG, "onInstallConversionDataLoaded: Got uriScheme " + uriScheme);
                    if (!TextUtils.isEmpty(uriScheme)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriScheme));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intent);
                        } catch (Exception ex) {
                            PerkLogger.e(LOG_TAG,
                                    "Exception starting activity from URI-scheme: " + uriScheme,
                                    ex);
                        }
                    }
                }
            }

            @Override
            public void onInstallConversionFailure(String s) {
                PerkLogger.d(LOG_TAG, "onInstallConversionFailure(): s: " + s);
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> map) {
                PerkLogger.d(LOG_TAG, "onAppOpenAttribution()");
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    PerkLogger.d(LOG_TAG,
                            "onAppOpenAttribution- key: " + entry.getKey() + " value: "
                                    + entry.getValue());
                }
            }

            @Override
            public void onAttributionFailure(String s) {
                PerkLogger.d(LOG_TAG, "onAttributionFailure(): s: " + s);
            }
        };
        AppsFlyerLib.getInstance().init(BuildConfig.appsFlyerKey, conversionDataListener,
                getApplicationContext());

        String senderId = "645651075327"; /* A.K.A Project Number */
        AppsFlyerLib.getInstance().enableUninstallTracking(senderId);

        // Leanplum START
        Leanplum.setApplicationContext(this);
        Parser.parseVariables(this);

        //  For session lifecyle tracking: (Ignore Splash-screen)
        LeanplumActivityHelper.deferMessagesForActivities(LaunchActivity.class);
        LeanplumActivityHelper.enableLifecycleCallbacks(this);

        SingularConfig config;

        // Leanplum API Keys & Singular init:
        if (BuildConfig.DEBUG) {
            Leanplum.setAppIdForDevelopmentMode(BuildConfig.leanplumAppId, BuildConfig.leanplumDevKey);

            config = new SingularConfig(BuildConfig.singularApiKey,
                    BuildConfig.singularSecret)
                    .withLoggingEnabled() //To enable logging
                    .withLogLevel(Log.VERBOSE); //Default log level is Log.ERROR
        } else {
            Leanplum.setAppIdForProductionMode(BuildConfig.leanplumAppId, BuildConfig.leanplumProdKey);

            config = new SingularConfig(BuildConfig.singularApiKey,
                    BuildConfig.singularSecret);
        }

        // Init Singular SDK
        Singular.init(this, config);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            String id = getClass().getPackage().getName();
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }

            // Send channel info to Leanplum:
            PerkLogger.d(LOG_TAG, "Sending Notification Channel information to Leanplum:");
            LeanplumAPIController.INSTANCE.addAndroidNotificationChannel(channel,
                    new OnRequestFinishedListener<LeanplumPostModel>() {
                        @Override
                        public void onSuccess(@NonNull LeanplumPostModel leanplumPostModel,
                                @Nullable String s) {
                            PerkLogger.d(LOG_TAG,
                                    "Successful sending Notification Channel information to "
                                            + "Leanplum!\n"
                                            + leanplumPostModel.toString());
                            ///

                           // AdobeTracker.INSTANCE.trackAppsFlyerEvent(getApplicationContext(), "allow_push_notifications",null);
                            ///
                        }

                        @Override
                        public void onFailure(@NonNull ErrorType errorType,
                                @Nullable PerkResponse<LeanplumPostModel> perkResponse) {
                            PerkLogger.e(LOG_TAG,
                                    "Failed sending Notification Channel information to Leanplum: "
                                            + (perkResponse != null ? perkResponse.toString()
                                            : ""));
                        }
                    });
        }
        else{
            ///

           // AdobeTracker.INSTANCE.trackAppsFlyerEvent(getApplicationContext(), "allow_push_notifications",null);
            ///

        }

        // This will only run once per session, even if the activity is restarted.
        Leanplum.start(this, new StartCallback() {
            @Override
            public void onResponse(boolean b) {
                PerkLogger.d(LOG_TAG, "Leanplum:StartCallback: onResponse(): " + b);
            }
        });
        // Leanplum END

       Fabric.with(this, new Crashlytics());

        // Fetch device info on the background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                String deviceInfo = DeviceInfoUtil.INSTANCE.getDeviceInfo(WatchBackApplication.this);

                PerkUtils.initializationTracking(getApplicationContext(), deviceInfo);
                // Get all the stored segmentation data
                PerkUtils.trackEvent("app_open");
            }
        }).start();

        ////
        // FB init
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        AppEventsLogger.activateApp(this);

        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityStopped(Activity activity) {


            }

            @Override
            public void onActivityStarted(Activity activity) {


            }

            @Override
            public void onActivitySaveInstanceState(Activity activity,
                                                    Bundle outState) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

                PerkUtils.setTopActivity(activity);

                try{
                    Crashlytics.setString("topActivity", activity.getClass().getSimpleName());
                    AdobeTracker.INSTANCE.onResume(activity);
                }catch (Exception e){

                }

            }

            @Override
            public void onActivityPaused(Activity activity) {
                try{
                    AdobeTracker.INSTANCE.onPause();
                }catch (Exception e){}
            }
            @Override
            public void onActivityDestroyed(Activity activity) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onActivityCreated(Activity activity,
                                          Bundle savedInstanceState) {
                if (!mAppsFlyerStarted) {
                    mAppsFlyerStarted = true;
                    AppsFlyerLib.getInstance().startTracking(WatchBackApplication.this);
                    trackLaunchEventIfNeeded();
                }
                PerkUtils.setTopActivity(activity);

            }
        });
        try{
            AdobeTracker.initAdobeSDK(getApplicationContext());

            ////Moat

            /*MoatOptions options = new MoatOptions();
            //options.loggingEnabled = true; // for testing
            MoatAnalytics.getInstance().start(options, this);*/

        }catch (Exception e){}

        WatchBackSettingsController.INSTANCE.refreshSettings(this, false);
    }

    /**
     * Enabling multidex for OS 20 or lower
     *
     * @param base context of base application
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    // -----------------------------------------------------------------------------------------
    // SecretKeysRequester implementation
    // -----------------------------------------------------------------------------------------

    @Override
    public @NonNull
    SecretKeys getSecretKeys() {
        return BuildConfig.secretKeys;
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        freeMemory();
        Glide.get(this).onLowMemory();
    }

    public void freeMemory() {
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
        trimCache();
    }

    public  void trimCache() {
        try {

            File dir = getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);

            }

        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    public  boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }


    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        PerkLogger.d(LOG_TAG, "ProcessLifecycleOwner: ON_CREATE");
        mAppRunning = false;
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        PerkLogger.d(LOG_TAG, "ProcessLifecycleOwner: ON_START");
        mAppRunning = true;
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        PerkLogger.d(LOG_TAG, "ProcessLifecycleOwner: ON_RESUME");
        mAppRunning = true;
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        PerkLogger.d(LOG_TAG, "ProcessLifecycleOwner: ON_PAUSE");
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        PerkLogger.d(LOG_TAG, "ProcessLifecycleOwner: ON_STOP");
        mAppRunning = false;

        // Save cached video play-head positions & the watched-video list:
        try {
            PerkPreferencesManager.INSTANCE.persistPlayheadPositionsCache();
            PerkPreferencesManager.INSTANCE.persistWatchedVideosCache();
        } catch (Exception ex) {
            PerkLogger.e(LOG_TAG, "Exception with persistPlayheadPositionsCache:", ex);
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        // Not called for ProcessLifecycleOwner
        PerkLogger.d(LOG_TAG, "ProcessLifecycleOwner: ON_DESTROY");
    }

    public boolean isAppRunning() {
        return mAppRunning;
    }

    public boolean shouldTrackAppsFlyerRetentionEvents() {
        return mTrackAppsFlyerRetentionEvents;
    }

    public int getAppsFlyerRetentionDay() {
        return mAppsFlyerRetentionDay;
    }

    private void trackLaunchEventIfNeeded() {
        if (!mLaunchEventTracked && shouldTrackAppsFlyerRetentionEvents()) {
            mLaunchEventTracked = true;
            String event = "D" + getAppsFlyerRetentionDay() + "_app_open";
            PerkLogger.d(LOG_TAG, "Tracking retention event: " + event);
            AdobeTracker.INSTANCE.trackAppsFlyerEvent(WatchBackApplication.this,
                    event, null);
        }
    }
}
