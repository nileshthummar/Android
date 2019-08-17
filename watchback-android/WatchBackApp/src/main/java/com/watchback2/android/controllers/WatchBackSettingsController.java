package com.watchback2.android.controllers;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.api.WatchbackAPIController;
import com.watchback2.android.models.Settings;
import com.watchback2.android.models.WatchBackSettingsModel;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkUtils;

/**
 * Created by perk on 07/04/18.
 * Controller to fetch & store Settings information from API
 */
public class WatchBackSettingsController {

    /**
     * Singleton instance of the class that would be used for accessing WatchBackSettingsController.
     */
    public static final WatchBackSettingsController INSTANCE = new WatchBackSettingsController();

    public static final long THREE_HOURS_MILLIS = 3 * 60 * 60 * 1000;

    private static final String LOG_TAG = "WatchBackSettingsController";

    private Settings mSettings;

    private boolean isRequestInProgress;

    private WatchBackSettingsController() {
        // Private constructor for Singleton class
    }

    private void getSettings(@NonNull final Context context, boolean showDialog) {
        if (!AppUtility.isNetworkAvailable(context, showDialog)) {
            PerkLogger.d(LOG_TAG, "getSettings: Returning as network is unavailable!");
            return;
        }

        if (isRequestInProgress) {
            PerkLogger.d(LOG_TAG, "getSettings: Returning as request is already in progress!");
            return;
        }

        isRequestInProgress = true;

        PerkLogger.d(LOG_TAG, "Loading WatchBack-Settings...");

        WatchbackAPIController.INSTANCE.getSettings(context, new OnRequestFinishedListener<WatchBackSettingsModel>() {

            @Override
            public void onSuccess(@NonNull WatchBackSettingsModel settingsModel, @Nullable String s) {
                PerkLogger.d(LOG_TAG, "Loading WatchBack-Settings successful");
                isRequestInProgress = false;

                if (settingsModel.getSettings() != null) {
                    mSettings = settingsModel.getSettings();

                    PerkLogger.d(LOG_TAG, "Got WatchBack-Settings: " + mSettings.toString());
                }
            }

            @Override
            public void onFailure(@NonNull ErrorType errorType,
                    @Nullable PerkResponse<WatchBackSettingsModel> perkResponse) {
                isRequestInProgress = false;
                PerkLogger.e(LOG_TAG, "Loading WatchBack-Settings failed: " + (perkResponse != null
                        ? perkResponse.getMessage() : context.getString(R.string.generic_error)));
                if (showDialog) {
                    PerkUtils.showErrorMessageToast(errorType,
                            (perkResponse != null ? perkResponse.getMessage()
                                    : context.getString(R.string.generic_error)));
                }
            }
        });
    }

    public int getAutoPlayVideoCountSetting(@NonNull Context context) {
        Settings settings = getSettings();
        if (settings == null || TextUtils.isEmpty(settings.getAutoplay())) {
            PerkLogger.e(LOG_TAG,
                    "getAutoPlayVideoCountSetting: setting unavailable! Returning max value!");

            return Short.MAX_VALUE;
        }

        try {
            return Integer.parseInt(settings.getAutoplay());
        } catch (Exception ex) {
            PerkLogger.e(LOG_TAG,
                    "getAutoPlayVideoCountSetting: Exception reading setting! Returning max value!",
                    ex);
            return Short.MAX_VALUE;
        }
    }

    public long getAYSWPromptMillisSetting(@NonNull Context context) {
        Settings settings = getSettings();
        if (settings == null || TextUtils.isEmpty(settings.getAyswPromptSeconds())) {
            PerkLogger.e(LOG_TAG,
                    "getAYSWPromptMillisSetting: setting unavailable! Returning max value (3 hours)!");
            return THREE_HOURS_MILLIS;
        }

        try {
            // Convert to millis since API returns value in seconds
            return Long.parseLong(settings.getAyswPromptSeconds()) * 1000;
        } catch (Exception ex) {
            PerkLogger.e(LOG_TAG,
                    "getAYSWPromptMillisSetting: Exception reading setting! Returning max value (3 hours)!",
                    ex);
            return THREE_HOURS_MILLIS;
        }
    }

    public long getAYSWPromptNoResponseMillisSetting(@NonNull Context context) {
        Settings settings = getSettings();
        if (settings == null || TextUtils.isEmpty(settings.getAyswNoResponseMinutes())) {
            PerkLogger.e(LOG_TAG,
                    "getAYSWPromptNoResponseMillisSetting: setting unavailable! Returning max value (3 hours)!");
            return THREE_HOURS_MILLIS;
        }

        try {
            // Convert to millis since API returns value in mins
            return Long.parseLong(settings.getAyswNoResponseMinutes()) * 60 * 1000;
        } catch (Exception ex) {
            PerkLogger.e(LOG_TAG,
                    "getAYSWPromptNoResponseMillisSetting: Exception reading setting! Returning "
                            + "max value (3 hours)!",
                    ex);
            return THREE_HOURS_MILLIS;
        }
    }

    public float getFastforwardPercentSetting(@NonNull Context context) {
        Settings settings = getSettings();
        if (settings == null || TextUtils.isEmpty(settings.getFastforwardLimitPct())) {
            PerkLogger.e(LOG_TAG,
                    "getFastforwardPercentSetting: setting unavailable! Returning 100%!");
            return 100;
        }

        try {
            return Float.parseFloat(settings.getFastforwardLimitPct());
        } catch (Exception ex) {
            PerkLogger.e(LOG_TAG,
                    "getFastforwardPercentSetting: Exception reading setting! Returning 100%!",
                    ex);
            return 100;
        }
    }

    public int getSavePlayheadCountSetting(@NonNull Context context) {
        Settings settings = getSettings();
        if (settings == null || TextUtils.isEmpty(settings.getSavePlayheadCount())) {
            PerkLogger.e(LOG_TAG,
                    "getSavePlayheadCountSetting: setting unavailable! Returning 1!");
            return 1;
        }

        try {
            return Integer.parseInt(settings.getSavePlayheadCount());
        } catch (Exception ex) {
            PerkLogger.e(LOG_TAG,
                    "getSavePlayheadCountSetting: Exception reading setting! Returning 1!",
                    ex);
            return 1;
        }
    }

    public float getLongformCompletionPercentSetting(@NonNull Context context) {
        Settings settings = getSettings();
        if (settings == null || TextUtils.isEmpty(settings.getLongformCompletePct())) {
            refreshSettings(context);

            PerkLogger.e(LOG_TAG,
                    "getLongformCompletionPercentSetting: setting unavailable! Returning -1!");
            return -1f;
        }

        try {
            return Float.parseFloat(settings.getLongformCompletePct());
        } catch (Exception ex) {
            PerkLogger.e(LOG_TAG,
                    "getLongformCompletionPercentSetting: Exception reading setting! Returning -1!",
                    ex);
            return -1f;
        }
    }

    public int getLongformCapSetting() {
        Settings settings = getSettings();
        if (settings == null || TextUtils.isEmpty(settings.getLongformCap())) {
            PerkLogger.e(LOG_TAG, "getLongformCapSetting: setting unavailable! Returning -1!");
            return -1;
        }

        try {
            return Integer.parseInt(settings.getLongformCap());
        } catch (Exception ex) {
            PerkLogger.e(LOG_TAG,
                    "getLongformCapSetting: Exception reading setting! Returning -1!",
                    ex);
            return -1;
        }
    }

    private Settings getSettings() {
        if (mSettings == null) {
            refreshSettings(PerkUtils.sApplication);
        }

        return mSettings;
    }

    public void refreshSettings(@NonNull Context context) {
        refreshSettings(context, true);
    }

    public void refreshSettings(@NonNull Context context, boolean showDialog) {
        getSettings(context, showDialog);
    }

}
