package com.watchback2.android.models;

import com.google.gson.annotations.SerializedName;
import com.perk.request.auth.annotation.PerkAccessToken;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Request body sent for updating the user's interests
 */
public class InterestsUpdateRequestBody extends HomeScreenInterestItemsBody {

    /** Serialization UID for the class. */
    private static final long serialVersionUID = 5730305853047160664L;

    /** Access token of the logged in user */
    @SerializedName("access_token")
    private @PerkAccessToken final String mAccessToken;

    // More fields can be added here -if needed in future

    public InterestsUpdateRequestBody(@NonNull List<String> selectedInterests) {
        super(selectedInterests);
        mAccessToken = PerkAccessToken.ACCESS_TOKEN;
    }
}
