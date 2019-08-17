package com.watchback2.android.controllers;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;

import com.perk.util.PerkLogger;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.models.Interest;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkPreferencesManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by perk on 12/04/18.
 * Manager to handle fetching all 'Recomended' videos for user based on the chosen interests
 */

public final class RecommendedVideosManager {

    /**
     * Singleton instance of the class that would be used for accessing RecommendedVideosManager.
     */
    public static final RecommendedVideosManager INSTANCE = new RecommendedVideosManager();

    private static final String LOG_TAG = "RecommendedVideosManager";

    private final ObservableField<List<BrightcovePlaylistData.BrightcoveVideo>>
            mRecommendedVideosList = new ObservableField<>();

    private boolean isRequestInProgress;

    private RecommendedVideosListFetchedCallback mCallback;

    public interface RecommendedVideosListFetchedCallback {
        void onRecommendedVideosListFetched(
                @Nullable List<BrightcovePlaylistData.BrightcoveVideo> recommendedVideosList);
    }

    private RecommendedVideosManager() {
        // Private constructor for Singleton class
    }

    private void getRecommendedVideos(@NonNull final Context context) {
        if (!AppUtility.isNetworkAvailable(context)) {
            PerkLogger.d(LOG_TAG, "getRecommendedVideos: Returning as network is unavailable!");
            notifyCallback();
            return;
        }

        List<Interest> interestList =
                PerkPreferencesManager.INSTANCE.getUserInterestsListFromPreferences();
        if (interestList == null) {
            PerkLogger.e(LOG_TAG, "interestList is null! Cannot proceed!");
            notifyCallback();
            return;
        }

        if (isRequestInProgress) {
            PerkLogger.d(LOG_TAG,
                    "getRecommendedVideos: Returning as request is already in progress!");
            return;
        }

        isRequestInProgress = true;

        List<String> selectedInterests = new ArrayList<>();

        /*for (Interest interest : interestList) {
            if (interest == null) {
                continue;
            }

            if (interest.getSelected()) {
                selectedInterests.add(interest);
            }
        }

        String encodedInterests = "\"" + TextUtils.join("\", \"", selectedInterests) + "\"";

        PerkLogger.d(LOG_TAG,
                "Loading recommended videos for user with selected interests: " + encodedInterests);
        */

        for (Interest interest : interestList) {
            if (interest != null && interest.getSelected() && !TextUtils.isEmpty(
                    interest.getId())) {
                selectedInterests.add(interest.getId());
            }
        }

        if (mRecommendedVideosList.get() != null) {
            mRecommendedVideosList.get().clear();
        }

        PerkLogger.d(LOG_TAG, "Loading recommended videos for user with selected interests: "
                + selectedInterests.toString());

       /* PerkAPIController.INSTANCE.getRecommendationsByUserInterests(context, selectedInterests,
                new OnRequestFinishedListener<List<BrightcovePlaylistData.BrightcoveVideo>>() {
                    @Override
                    public void onSuccess(
                            @NonNull List<BrightcovePlaylistData.BrightcoveVideo> videoList,
                            @Nullable String s) {

                        PerkLogger.d(LOG_TAG, "Successfully loaded recommended videos data");

                        isRequestInProgress = false;

                        if (videoList != null) {
                            // Remove 'longform' videos from the playlist videos since we show them
                            // only in the 'featured' section on home-screen:
                            List<BrightcovePlaylistData.BrightcoveVideo> videoListNoLongForm =
                                    AppUtility.removeLongformVideos(videoList);

                            // Also remove videos having 'redemption_partner' tag set:
                            List<BrightcovePlaylistData.BrightcoveVideo> finalVideoList =
                                    AppUtility.removeRedemptionPartnerVideos(videoListNoLongForm);

                            PerkLogger.d(LOG_TAG,
                                    "Recommended videos list size: " + finalVideoList.size());

                            final List<BrightcovePlaylistData.BrightcoveVideo> clonedList =
                                    new ArrayList<>(finalVideoList);
                            mRecommendedVideosList.set(clonedList);
                        }

                        notifyCallback();
                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType,
                            @Nullable PerkResponse<List<BrightcovePlaylistData.BrightcoveVideo>> perkResponse) {
                        isRequestInProgress = false;
                        PerkLogger.e(LOG_TAG,
                                "Failed loading videos recommended videos data! "
                                        + (perkResponse != null ? perkResponse.getMessage() : ""));

                        PerkUtils.showErrorMessageToast(errorType,
                                (perkResponse != null ? perkResponse.getMessage()
                                        : context.getString(R.string.generic_error)));

                        notifyCallback();
                    }
                });*/
    }

    public void getRecommendedVideosList(@NonNull Context context, boolean forceRefresh,
            @Nullable RecommendedVideosListFetchedCallback callback) {
        mCallback = callback;

        List<BrightcovePlaylistData.BrightcoveVideo> videos = mRecommendedVideosList.get();

        if (videos == null || videos.isEmpty() || forceRefresh) {
            getRecommendedVideos(context);
        } else {
            notifyCallback();
        }
    }

    private void notifyCallback() {
        if (mCallback != null) {
            mCallback.onRecommendedVideosListFetched(mRecommendedVideosList.get());
            mCallback = null;
        }
    }

}
