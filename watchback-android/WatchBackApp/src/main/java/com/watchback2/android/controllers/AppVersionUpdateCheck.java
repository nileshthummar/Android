package com.watchback2.android.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.activities.ForceUpdateActivity;
import com.watchback2.android.api.PerkAppVersion;
import com.watchback2.android.api.WatchbackAPIController;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkUtils;

/**
 * Created by perk on 09/06/18.
 * Check if App needs to update.
 */

public class AppVersionUpdateCheck {

    private static final String LOG_TAG = "AppVerUpdateChk";

    public AppVersionUpdateCheck() {
        checkForAppUpdate();
    }

    private void checkForAppUpdate() {

        // Make sure activity is valid
        Activity activity = PerkUtils.getTopActivity();
        if (activity == null) {
            PerkLogger.a(LOG_TAG, "There is no valid activity to show the force update view");
            return;
        }

        // Make sure network is available
        if (!AppUtility.isNetworkAvailable(activity)) {
            PerkLogger.d(LOG_TAG, "Returning as network is unavailable!");
            return;
        }

        // get Package details
        PackageInfo pInfo = null;
        try {
            pInfo = activity.getApplicationContext().getPackageManager().getPackageInfo(
                    activity.getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e1) {
            PerkLogger.e(getClass().getSimpleName(), "NameNotFoundException " + e1);
        }

        if (pInfo == null) {
            PerkLogger.d(LOG_TAG, "Returning as package-info is unavailable!");
            return;
        }

        makeCall(activity.getApplicationContext(), pInfo);
    }

    private void makeCall(final Context context, PackageInfo pInfo) {
        WatchbackAPIController.INSTANCE.getLatestAppVersion(context,
                new OnRequestFinishedListener<PerkAppVersion>() {

                    @Override
                    public void onSuccess(@NonNull PerkAppVersion appUpdateInfo,
                            @Nullable String s) {
                        PerkLogger.e(LOG_TAG, "getLatestAppVersion: onSuccess");

                        if (appUpdateInfo.getUpdate()) {
                            ForceUpdateActivity.sIsDisplayed = true;
                            ForceUpdateActivity.sIsForceUpdate = appUpdateInfo.getForceUpdate();
                            ForceUpdateActivity.sStrUrl = appUpdateInfo.getUrl();

                            // Make sure activity is valid
                            Activity activity = PerkUtils.getTopActivity();
                            if (activity == null) {
                                PerkLogger.a(LOG_TAG,
                                        "There is no valid activity to launch the force update view");
                                return;
                            }

                            Intent intent = new Intent(activity,
                                    ForceUpdateActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(intent);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType,
                            @Nullable PerkResponse<PerkAppVersion> perkResponse) {
                        PerkLogger.e(LOG_TAG,
                                "getLatestAppVersion: onFailure: " + (perkResponse != null
                                        ? perkResponse.getMessage()
                                        : context.getResources().getString(R.string.generic_error))
                                        + " -error-type: " + errorType);

                        PerkUtils.showErrorMessageToast(errorType, "");
                    }
                });
    }
}
