package com.watchback2.android.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * Request body sent for specifying the user's interests, when using the app without logging in
 * and also used as a body for POST API call to fetch recommended videos for a user, based on interests
 */
public class HomeScreenInterestItemsBody implements Serializable {

    /** Serialization UID for the class. */
    private static final long serialVersionUID = 1L;

    @SerializedName("interests")
    private List<String> mSelectedInterests;

    // More fields can be added here -if needed in future

    public HomeScreenInterestItemsBody(@NonNull List<String> selectedInterests) {
        mSelectedInterests = selectedInterests;
    }
}
