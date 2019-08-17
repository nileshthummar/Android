package com.watchback2.android.viewmodels;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.request.auth.AuthAPIRequestController;
import com.perk.util.PerkLogger;
import com.watchback2.android.api.BrightcoveAPIController;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.api.WatchbackAPIController;
import com.watchback2.android.helper.UserInfoValidator;
import com.watchback2.android.models.channels.singlechannel.SingleChannelWithVideoResponseModel;

import java.util.List;

/**
 * Created by perk on 15/03/18.
 * Common view-model class used by VideosListViewHolder & by
 * BrandDetailsActivity to fetch videos for provided playlist for displaying on home/channel
 * fragments
 * Parent class for BrandDetailsViewModel
 */

public class VideosListViewModel {

    private final Context mContext;

    private final ObservableBoolean dataLoading = new ObservableBoolean(false);

    private final ObservableBoolean hideHeader = new ObservableBoolean(false);

    private final ObservableBoolean isEmptyList = new ObservableBoolean(true);

    private final ObservableBoolean isDummyLoader = new ObservableBoolean(false);

    private final ObservableField<String> title = new ObservableField<>("");

    private final ObservableField<String> logo = new ObservableField<>("");

    private final ObservableField<List<BrightcovePlaylistData.BrightcoveVideo>> videosList =
            new ObservableField<>();

    private final String LOG_TAG;

    public VideosListViewModel(@NonNull Context context) {
        mContext = context.getApplicationContext();
        LOG_TAG = getClass().getSimpleName();
    }

    public void loadPlaylistVideos(final String playlistId, final String title, final String logo,
            final int offset) {
        if (dataLoading.get()) {
            return;
        }

        if (TextUtils.isEmpty(playlistId)) {
            PerkLogger.e(LOG_TAG, "Returning as playlistId not provided");
            return;
        }

        PerkLogger.d(LOG_TAG, "Loading Videos for playlistId:" + playlistId + " title: " + title);

        dataLoading.set(true);

        if (videosList.get() != null) {
            videosList.get().clear();
        }

        updateListEmptyState();

        BrightcoveAPIController.INSTANCE.getBrightCovePlaylistVideos(playlistId,
                offset,
                new OnRequestFinishedListener<BrightcovePlaylistData>() {
                    @Override
                    public void onSuccess(@NonNull BrightcovePlaylistData brightcovePlaylistData,
                            @Nullable String s) {
                        PerkLogger.d(LOG_TAG,
                                "Loading videos data successful for playlistId: " + playlistId);

                        dataLoading.set(false);
                        getTitle().set(title);
                        getLogo().set(logo);

                        // Remove 'longform' videos from the playlist videos since we show them
                        // only in the 'featured' section on home-screen:
                        /*List<BrightcovePlaylistData.BrightcoveVideo> videoList =
                                AppUtility.removeLongformVideos(brightcovePlaylistData.getVideos());*/

                        // Also remove videos having 'redemption_partner' tag set:
                        onVideosFetched(brightcovePlaylistData.getVideos()/*AppUtility.removeRedemptionPartnerVideos(videoList)*/);
                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType,
                            @Nullable PerkResponse<BrightcovePlaylistData> perkResponse) {
                        dataLoading.set(false);
                        onVideosFetched(null);
                        PerkLogger.e(LOG_TAG,
                                "Loading videos data failed for playlistId: " + playlistId + ": "
                                        + (perkResponse != null ? perkResponse.getMessage() : ""));
                    }
                });
    }

    // This is not used anymore since we get videos for all channels in the GET
    // "/watchback/channels/all" API.
    // This is just kept here in case it needs to be added back in future:

    /*public void loadChannelVideos(final String uuid, final String title, final String logo,
            final int offset) {
        loadChannelVideos(uuid, title, logo, offset, PerkAPI.DEFAULT_SORT_MODE, true);
    }*/

    /* package */ void loadChannelVideos(final String uuid, final String title, final String logo,
            final int offset, @NonNull final String sort) {
        loadChannelVideos(uuid, title, logo, offset, sort, false);
    }

    private void loadChannelVideos(final String uuid, final String title, final String logo,
            final int offset, @NonNull final String sort, final boolean useCache) {

        if (dataLoading.get()) {
            return;
        }

        if (TextUtils.isEmpty(uuid)) {
            PerkLogger.e(LOG_TAG, "Returning as channel-uuid not provided");
            return;
        }

        dataLoading.set(true);

        boolean isLoggedIn = UserInfoValidator.isAuthenticated(
                AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(mContext));

        // Do not clear list as in case of pagination, doing this clears the data if we are already
        // at end of list & no more videos are loaded
        /*if (videosList.get() != null) {
            videosList.get().clear();
        }*/

        // Not-needed anymore since we have videos from /watchback/channels/all GET API
        /*if (useCache) {
            final List<BrightcovePlaylistData.BrightcoveVideo> cachedData =
                    ChannelsManager.INSTANCE.getCachedVideoDataFor(uuid);

            if (cachedData != null && !cachedData.isEmpty()) {
                PerkLogger.d(LOG_TAG,
                        "Loaded cached videos-list for Channel-UUID:" + uuid + " title: "
                                + title);

                dataLoading.set(false);
                getTitle().set(title);
                getLogo().set(logo);

                onVideosFetched(cachedData);
                return;
            }
        }*/

        PerkLogger.d(LOG_TAG, (useCache ? "Cached data unavailable! " : "") +
                "Loading Videos for Channel-UUID:" + uuid + " title: " + title);

        WatchbackAPIController.INSTANCE.getSingleChannelWithVideos(mContext, isLoggedIn, uuid, "10",
                Integer.toString(offset), new OnRequestFinishedListener<SingleChannelWithVideoResponseModel>() {
                    @Override
                    public void onSuccess(@NonNull SingleChannelWithVideoResponseModel videoData,
                            @Nullable String s) {
                        PerkLogger.d(LOG_TAG,
                                "Loading videos data successful for Channel-UUID: " + uuid);

                        dataLoading.set(false);
                        getTitle().set(title);
                        getLogo().set(logo);

                        // Convert obtained list to BrightcovePlaylistData.BrightcoveVideo format
                        // since that is how the common adapter & video-player activity expect it
                        // to be.
                        List<BrightcovePlaylistData.BrightcoveVideo> videoList =
                                videoData.getVideos();

                        PerkLogger.d(LOG_TAG,
                                "Channel-video-list size: " + (videoList != null
                                        ? videoList.size() : "0"));

                        // Remove 'longform' videos from the playlist videos since we show them
                        // only in the 'featured' section on home-screen:
                        /*List<BrightcovePlaylistData.BrightcoveVideo> videoListNoLongForm =
                                AppUtility.removeLongformVideos(videoList);*/

                        // Also remove videos having 'redemption_partner' tag set:
                        /*List<BrightcovePlaylistData.BrightcoveVideo> finalVideoList =
                                AppUtility.removeRedemptionPartnerVideos(videoListNoLongForm);*/

                        // Not-needed anymore since we have videos from /watchback/channels/all GET API:
                        // Cache the videos only when the list is being fetched for the first set
                        // of videos... we do not use this in case of pagination i.e. on
                        // Brand/Channel details screen
                        /*if (useCache && offset == 0) {
                            ChannelsManager.INSTANCE.cacheVideoDataFor(uuid, finalVideoList);
                        }*/

                        onVideosFetched(videoList);
                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType,
                            @Nullable PerkResponse<SingleChannelWithVideoResponseModel> perkResponse) {
                        dataLoading.set(false);
                        onVideosFetched(null);
                        PerkLogger.e(LOG_TAG,
                                "Loading videos data failed for Channel-UUID: " + uuid
                                        + ": " + (perkResponse != null ? perkResponse.getMessage()
                                        : ""));
                    }
                });
    }

    public void onVideosFetched(List<BrightcovePlaylistData.BrightcoveVideo> videoList) {
        if (videoList != null) {
            getVideosList().set(videoList);
        }
        updateListEmptyState();
    }

    public ObservableBoolean getDataLoading() {
        return dataLoading;
    }

    public ObservableBoolean getIsDummyLoader() {
        return isDummyLoader;
    }

    public ObservableField<List<BrightcovePlaylistData.BrightcoveVideo>> getVideosList() {
        return videosList;
    }

    public ObservableBoolean getIsEmptyList() {
        return isEmptyList;
    }

    /* package */ void updateListEmptyState() {
        List<BrightcovePlaylistData.BrightcoveVideo> list = getVideosList().get();
        getIsEmptyList().set(list == null || list.isEmpty());
    }

    public ObservableField<String> getTitle() {
        return title;
    }

    public ObservableField<String> getLogo() {
        return logo;
    }

    public ObservableBoolean getHideHeader() {
        return hideHeader;
    }

    protected Context getContext() {
        return mContext;
    }

}
