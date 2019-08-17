package com.watchback2.android.models;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.os.Build;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.watchback2.android.BuildConfig;

import java.io.Serializable;

/**
 * Request body sent for adding a Notification Channel to Leanplum
 */
public class LeanplumAddChannelRequestBody implements Serializable {

    /** Serialization UID for the class. */
    private static final long serialVersionUID = 1L;

    /** The application ID */
    @SerializedName("appId")
    private final String appId;

    /** The Development key for your Leanplum App. */
    @SerializedName("clientKey")
    private final String clientKey;

    /** The version of the Leanplum API to use. The current version is 1.0.6 */
    @SerializedName("apiVersion")
    private final String apiVersion;

    /** The channel ID. Also used when updating existing channels. */
    @SerializedName("id")
    private final String id;

    /** The human-readable name that will appear on the Leanplum dashboard (e.g. Promotions,
     * Transactional, etc.). Should be distinguishable from all other channel names. */
    @SerializedName("name")
    private final String name;

    /** Sets the importance of all notifications in the channel, which determines how much the
     * channel can interrupt the user. The default is 3: notifications will show everywhere, make
     * noise, but does not visually intrude. */
    @SerializedName("importance")
    private final int importance;

    /** The user-visible description of this channel */
    @SerializedName("description")
    private final String description;

    /** Whether to enable lights. */
    @SerializedName("enableLights")
    private final boolean enableLights;

    /** Whether to enable vibration. */
    @SerializedName("enableVibration")
    private final boolean enableVibration;

    @SerializedName("vibrationPattern")
    private final long[] vibrationPattern;

    /** This is a Leanplum parameter, indicating whether this channel should be the default
     * channel referenced in the dashboard. Leanplum automatically defines the first channel you
     * create as the 'default' channel, preventing the dashboard user from having to manually
     * choose a channel for every campaign. */
    @SerializedName("default")
    private final boolean mDefault;

    // More fields can be added here -if needed in future

    @TargetApi(Build.VERSION_CODES.O)
    public LeanplumAddChannelRequestBody(@NonNull NotificationChannel notificationChannel) {
        appId = BuildConfig.leanplumAppId;
        clientKey = BuildConfig.leanplumDevKey;
        apiVersion = "1.0.6";

        id = notificationChannel.getId();
        name = notificationChannel.getName().toString();
        importance = notificationChannel.getImportance();
        description = notificationChannel.getDescription();
        enableLights = notificationChannel.shouldShowLights();
        enableVibration = notificationChannel.shouldVibrate();
        vibrationPattern = notificationChannel.getVibrationPattern();

        mDefault = true;
    }
}
