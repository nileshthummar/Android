package com.watchback2.android.models;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.perk.request.auth.SecretKeys;
import com.perk.request.util.DeviceInfoUtil;
import com.perk.request.util.EnvironmentUtil;
import com.watchback2.android.BuildConfig;

import java.io.Serializable;
import java.util.Locale;

import proguard.annotation.Keep;

/**
 * Base auth request object that should be sent to auth API.
 */
@SuppressWarnings("unused")
@Keep
/*package*/ abstract class AuthRequest implements Serializable {

    // -----------------------------------------------------------------------------------------
    // Serializable implementation
    // -----------------------------------------------------------------------------------------

    /** Serial version UID of the class for making it unique. */
    private static final long serialVersionUID = 7526146850873588623L;

    // -----------------------------------------------------------------------------------------
    // Package enums
    // -----------------------------------------------------------------------------------------

    /**
     * Type of authentications that could be requested from Perk auth API request
     */
    /*package*/ enum GrantType {

        /** For making auth request using email and password. */
        PASSWORD,
    }

    // -----------------------------------------------------------------------------------------
    // Private variables
    // -----------------------------------------------------------------------------------------

    /** Client ID that should be sent while making auth API request. */
    @SerializedName("client_id")
    private final String mClientId;

    /** Client secret that should be sent while making auth API request. */
    @SerializedName("client_secret")
    private final String mClientSecret;

    /** Type of authentication that should be granted to the user. */
    @SerializedName("grant_type")
    private final String mGrantType;

    /** Product identifier of the client making the request. */
    @SerializedName("product_identifier")
    private final String mProductIdentifier;

    // -----------------------------------------------------------------------------------------
    // Package methods
    // -----------------------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param context context used for fetching secret keys for the application or library making
     *                auth request
     * @param grantType type of authentication that should be granted to the user
     * @throws IllegalStateException thrown when secret keys for the application or library are not
     *                               available
     */
    public AuthRequest(@NonNull final Context context, @NonNull final GrantType grantType) {
        final SecretKeys secretKeys = BuildConfig.secretKeys;

        mClientId = DeviceInfoUtil.INSTANCE.getProductIdentifier(context);
        mProductIdentifier = secretKeys.authProductIdentifier;

        final EnvironmentUtil.Environment currentEnvironment =
                EnvironmentUtil.INSTANCE.getCurrentEnvironment(context);
        switch (currentEnvironment) {
            case DEV:
                mClientSecret = secretKeys.authDevSecretKey;
                break;

            default:
                mClientSecret = secretKeys.authProdSecretKey;
                break;
        }

        mGrantType = grantType.name().toLowerCase(Locale.ENGLISH);
    }
}
