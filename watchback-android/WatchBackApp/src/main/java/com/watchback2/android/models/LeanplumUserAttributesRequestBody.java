package com.watchback2.android.models;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.leanplum.Leanplum;
import com.perk.util.PerkLogger;
import com.watchback2.android.BuildConfig;
import com.watchback2.android.controllers.PerkUserManager;
import com.watchback2.android.utils.PerkUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * Request body sent for adding a Notification Channel to Leanplum
 */
public class LeanplumUserAttributesRequestBody implements Serializable {

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

    /** The current user ID. You can set this to whatever your company uses for user IDs. Leave
     * it blank to use the device ID */
    @SerializedName("userId")
    private final String userId;

    /** A map of user attributes as key-value pairs. Each key must be a string. Attributes are
     * saved across sessions. Only supplied attributes will be updated (i.e., if you omit an
     * existing attribute, it will be preserved). Example: {"gender":"F","age":21} */
    @SerializedName("userAttributes")
    private final Map<String, String> userAttributes;

    // More fields can be added here -if needed in future

    public LeanplumUserAttributesRequestBody(@NonNull Map<String, String> userAttributes) {
        appId = BuildConfig.leanplumAppId;
        clientKey = BuildConfig.leanplumDevKey;
        apiVersion = "1.0.6";

        PerkUser perkUser = PerkUserManager.INSTANCE.getUser(PerkUtils.sApplication);
        if (perkUser != null && !TextUtils.isEmpty(perkUser.getUuid())) {
            userId = perkUser.getUuid();
        } else {
            userId = Leanplum.getDeviceId();
        }
        PerkLogger.d("LeanplumUserAttrBody", "userId set to: " + userId);
        this.userAttributes = userAttributes;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
