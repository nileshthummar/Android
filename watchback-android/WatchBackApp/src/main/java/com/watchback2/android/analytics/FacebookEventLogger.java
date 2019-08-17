package com.watchback2.android.analytics;

import android.os.Bundle;

import com.facebook.appevents.AppEventsLogger;
import com.perk.util.PerkLogger;
import com.watchback2.android.utils.PerkUtils;

import androidx.annotation.NonNull;

/**
 * Created by perk on 03/09/18.
 * Singleton class to handle Facebook Event logging
 */
public final class FacebookEventLogger {

    /**
     * Singleton instance of the class that would be used for accessing FacebookEventLogger.
     */
    private static FacebookEventLogger INSTANCE;

    private static final String LOG_TAG = "FacebookEventLogger";

    private final AppEventsLogger mAppEventsLogger;

    private FacebookEventLogger() {
        // Private constructor for Singleton class
        mAppEventsLogger = AppEventsLogger.newLogger(
                PerkUtils.getTopActivity().getApplicationContext());
    }

    public static FacebookEventLogger getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FacebookEventLogger();
        }
        return INSTANCE;
    }

    public void logEvent(String event) {
        if (mAppEventsLogger == null) {
            PerkLogger.e(LOG_TAG, "mAppEventsLogger is null!");
            return;
        }

        PerkLogger.d(LOG_TAG, "logEvent: " + event);

        mAppEventsLogger.logEvent(event);
    }

    public void logEventWithParams(@NonNull String event, @NonNull Bundle params) {
        if (mAppEventsLogger == null) {
            PerkLogger.e(LOG_TAG, "logEventWithParams(): mAppEventsLogger is null!");
            return;
        }

        PerkLogger.d(LOG_TAG,
                "logEventWithParams: " + event + " params: " + (params != null ? params.toString()
                        : "null!"));

        mAppEventsLogger.logEvent(event, params);
    }
}
