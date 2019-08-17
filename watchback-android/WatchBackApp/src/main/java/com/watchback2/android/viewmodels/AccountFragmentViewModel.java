package com.watchback2.android.viewmodels;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.navigators.IAccountFragmentNavigator;
import com.watchback2.android.utils.AppUtility;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;

/**
 * Created by perk on 03/04/18.
 */

public class AccountFragmentViewModel {

    private static final String LOG_TAG = "AccountFragmentViewModel";

    private final ObservableBoolean dataLoading = new ObservableBoolean(false);

    private final ObservableBoolean isHistoryTab = new ObservableBoolean(true);

    private final ObservableField<List<BrightcovePlaylistData.BrightcoveVideo>> videosHistoryList = new ObservableField<>();

    private final ObservableField<String> appVersion = new ObservableField<>("");

    // No recommended videos anymore: https://jira.rhythmone.com/browse/PEWAN-487
    //private final ObservableField<List<BrightcovePlaylistData.BrightcoveVideo>> recommendedVideosList = new ObservableField<>();

    private final ObservableBoolean noHistoryResults = new ObservableBoolean(false);

    private final WeakReference<Context> mContextWeakReference;

    private WeakReference<IAccountFragmentNavigator> mNavigatorWeakReference;

    public AccountFragmentViewModel(@NonNull Context context) {
        mContextWeakReference = new WeakReference<>(context.getApplicationContext());

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    0);
            String versionInfo = context.getResources().getString(R.string.app_version_message,
                    Calendar.getInstance().get(Calendar.YEAR),
                    (pInfo != null ? pInfo.versionName : ""));
            getAppVersion().set(versionInfo);
        } catch (PackageManager.NameNotFoundException e) {
            PerkLogger.e(LOG_TAG, "Exception trying to get version information:", e);
        }
    }

    public void setNavigator(IAccountFragmentNavigator navigator) {
        mNavigatorWeakReference = new WeakReference<>(navigator);
    }

    public void handleDonateClick(View view) {
        if (getNavigator() != null) {
            getNavigator().onDonateClick();
        }
    }

    public void handleShopClick(View view) {
        if (getNavigator() != null) {
            getNavigator().onShopClick();
        }
    }

    public void handleLogOutClick(View view) {
        if (getContext() != null) {
            PerkLogger.d(LOG_TAG, "handleLogOutClick: Initiating Log out...");
            AppUtility.logOutUser(getContext());
        }
    }

    public void loadUserVideoHistory() {

        if (getDataLoading().get()) {
            return;
        }

        if (getContext() == null || !AppUtility.isNetworkAvailable(getContext())) {
            PerkLogger.d(LOG_TAG, "Returning due to no context OR network unavailability!");
            return;
        }

        PerkLogger.d(LOG_TAG, "Loading video history for user...");

        getDataLoading().set(true);
        getNoHistoryResults().set(false);

        if (getVideosHistoryList().get() != null) {
            getVideosHistoryList().get().clear();
        }

       /* PerkAPIController.INSTANCE.getUserVideoHistory(getContext(),
                new OnRequestFinishedListener<UserVideoHistoryData>() {

                    @Override
                    public void onSuccess(@NonNull UserVideoHistoryData userVideoHistoryData,
                            @Nullable String s) {
                        getDataLoading().set(false);

                        List<BrightcovePlaylistData.BrightcoveVideo> finalVideoList =
                                userVideoHistoryData.getVideos();

                        *//*
                        Do not remove longform OR redemption_partner videos as per:
                        https://rhythmone.atlassian.net/browse/PEWAN-199

                        List<BrightcovePlaylistData.BrightcoveVideo> videoList = userVideoHistoryData.getVideos();

                        // Remove 'longform' videos from the playlist videos since we show them
                        // only in the 'featured' section on home-screen:
                        List<BrightcovePlaylistData.BrightcoveVideo> videoListNoLongForm =
                                AppUtility.removeLongformVideos(videoList);

                        // Also remove videos having 'redemption_partner' tag set:
                        List<BrightcovePlaylistData.BrightcoveVideo> finalVideoList =
                                AppUtility.removeRedemptionPartnerVideos(videoListNoLongForm); *//*

                        PerkLogger.d(LOG_TAG,
                                "Loading video history for user successful! Video-list size: " + (
                                        finalVideoList != null ? finalVideoList.size() : "0"));


                        if (finalVideoList != null && !finalVideoList.isEmpty()) {
                            getVideosHistoryList().set(finalVideoList);
                        } else {
                            // No history results obtained:
                            onNoHistoryVideoItemsAvailable();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType,
                            @Nullable PerkResponse<UserVideoHistoryData> perkResponse) {
                        getDataLoading().set(false);

                        String errorMsg = (perkResponse != null && !TextUtils.isEmpty(
                                perkResponse.getMessage()) ? perkResponse.getMessage()
                                : (getContext() != null ? getContext().getResources().getString(
                                        R.string.generic_error) : null));

                        PerkLogger.e(LOG_TAG,
                                "Loading video history for user failed! " + errorMsg);

                        // Do not show toast if user has no history: https://jira.rhythmone.com/browse/PEWAN-432
                        if (StringUtils.containsIgnoreCase(errorMsg, "No video history")) {
                            errorMsg = null;
                        }

                        PerkUtils.showErrorMessageToast(errorType, errorMsg);

                        // No history results:
                        onNoHistoryVideoItemsAvailable();
                    }
                });*/
    }

    private void onNoHistoryVideoItemsAvailable() {
        getNoHistoryResults().set(true);

        /* // No recommended videos anymore: https://jira.rhythmone.com/browse/PEWAN-487
        // Load the 'Recommended for you' videos in this case if we don't have them fetched already
        if (getRecommendedVideosList().get() != null && !getRecommendedVideosList().get().isEmpty()) {
            final List<BrightcovePlaylistData.BrightcoveVideo> clonedList = new ArrayList<>(getRecommendedVideosList().get());
            getVideosHistoryList().set(clonedList);
        } else {
            getDataLoading().set(true);

            if (getVideosHistoryList().get() != null) {
                getVideosHistoryList().get().clear();
            }

            RecommendedVideosManager.INSTANCE.getRecommendedVideosList(getContext(), false, this);
        }
        */
    }

    public ObservableBoolean getDataLoading() {
        return dataLoading;
    }

    public ObservableField<List<BrightcovePlaylistData.BrightcoveVideo>> getVideosHistoryList() {
        return videosHistoryList;
    }

    // Used for setting drawable-left via data-Binding since we cannot currently get the
    // drawable's integer resource value via xml
    public int getFacebookIconResourceId() {
        return R.drawable.ic_fb;
    }

    public int getRightIconResourceId() {
        return R.drawable.ic_right_arrow;
    }

    @Nullable
    private Context getContext() {
        if (mContextWeakReference == null) {
            return null;
        }

        return mContextWeakReference.get();
    }

    @Nullable
    private IAccountFragmentNavigator getNavigator() {
        if (mNavigatorWeakReference == null) {
            return null;
        }

        return mNavigatorWeakReference.get();
    }

    public ObservableField<String> getAppVersion() {
        return appVersion;
    }

    public ObservableBoolean getNoHistoryResults() {
        return noHistoryResults;
    }

    /*public ObservableField<List<BrightcovePlaylistData.BrightcoveVideo>> getRecommendedVideosList() {
        return recommendedVideosList;
    }*/

    public boolean hasRecommendedVideos() {
        return false;
        /*return getRecommendedVideosList().get() != null
                && !getRecommendedVideosList().get().isEmpty();*/
    }

    /*@Override
    public void onRecommendedVideosListFetched(
            @Nullable List<BrightcovePlaylistData.BrightcoveVideo> recommendedVideosList) {
        getDataLoading().set(false);

        if (recommendedVideosList != null) {
            PerkLogger.d(LOG_TAG, "Recommended videos list size: " + recommendedVideosList.size());

            final List<BrightcovePlaylistData.BrightcoveVideo> clonedList = new ArrayList<>(recommendedVideosList);
            getVideosHistoryList().set(clonedList);

            // Also save it to the recommended videos list so that we do not need
            // to fetch it again
            getRecommendedVideosList().set(recommendedVideosList);
        }
    }*/

    public ObservableBoolean getIsHistoryTab() {
        return isHistoryTab;
    }
}
