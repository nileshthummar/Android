package com.watchback2.android.controllers;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.request.auth.AuthAPIRequestController;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.api.WatchbackAPIController;
import com.watchback2.android.helper.UserInfoValidator;
import com.watchback2.android.models.Channel;
import com.watchback2.android.models.channels.AllChannelsResponseModel;
import com.watchback2.android.utils.AbstractVideoDataCache;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkPreferencesManager;
import com.watchback2.android.utils.PerkUtils;
import com.watchback2.android.utils.WrapResponseModels;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by perk on 07/04/18.
 * Manager to handle fetching all Channels information
 */

public final class ChannelsManager extends AbstractVideoDataCache {

    /**
     * Singleton instance of the class that would be used for accessing ChannelsManager.
     */
    public static final ChannelsManager INSTANCE = new ChannelsManager();

    private static final String LOG_TAG = "ChannelsManager";

    private final ObservableField<List<Channel>> allChannelsList = new ObservableField<>();

    /**
     * Contains the list of channels without any videos -used for launching
     * Channel/BrandDetails-page from either the home-screen OR from notifications (via
     * watchback://channel:uuid protocol)
     */
    private final ObservableField<List<Channel>> allChannelsListWithoutVideos = new ObservableField<>();

    private boolean isRequestInProgress;

    private ChannelListFetchedCallback mCallback;

    public interface ChannelListFetchedCallback {
        void onChannelListFetched(@Nullable List<Channel> channelList);
    }

    public interface NextChannelsListFetchedCallback {
        void onSuccess(@Nullable List<Channel> channelList);

        void onFailure();
    }

    private ChannelsManager() {
        // Private constructor for Singleton class
        super();
    }

    private void getAllChannels(@NonNull final Context context) {
        // Clear cache of channel-videos in this case
        clearCache();

        if (!AppUtility.isNetworkAvailable(context)) {
            PerkLogger.d(LOG_TAG, "getAllChannels: Returning as network is unavailable!");
            isRequestInProgress = false;
            notifyCallback();
            return;
        }

        if (isRequestInProgress) {
            PerkLogger.d(LOG_TAG, "getAllChannels: Returning as request is already in progress!");
            return;
        }

        if (allChannelsList.get() != null) {
            PerkLogger.d(LOG_TAG, "getAllChannels: Invalidating channels list!");
            allChannelsList.set(new ArrayList<>(0));
        }

        boolean isLoggedIn = UserInfoValidator.isAuthenticated(
                AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(context));

        isRequestInProgress = true;

        fetchAllChannelsListWithoutVideos(context, isLoggedIn, null);

        PerkLogger.d(LOG_TAG, "getAllChannels: Loading channels data... isLoggedIn:" + isLoggedIn);

        /*PerkAPIController.INSTANCE.getAllChannelsVideos(context, isLoggedIn, 0,
                new OnRequestFinishedListener<List<Channel>>() {

                    @Override
                    public void onSuccess(@NonNull List<Channel> channelsList,
                                          @Nullable String s) {
                        PerkLogger.d(LOG_TAG, "getAllChannels: Loading channels data successful");
                        isRequestInProgress = false;

                        allChannelsList.set(new ArrayList<>(channelsList));
                        cacheVideoDataFor(DUMMY_KEY, null);
                        notifyCallback();
                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType,
                                          @Nullable PerkResponse<List<Channel>> perkResponse) {
                        isRequestInProgress = false;
                        PerkLogger.e(LOG_TAG,
                                "getAllChannels: Loading channels data failed: " + (perkResponse != null
                                        ? perkResponse.getMessage()
                                        : context.getString(R.string.generic_error))
                                        + " -error-type: " + errorType);

                        PerkUtils.showErrorMessageToast(errorType,
                                (perkResponse != null ? perkResponse.getMessage()
                                        : context.getString(R.string.generic_error)));

                        notifyCallback();
                    }
                });*/
    }

    public void loadNextChannels(@NonNull Context context, int offset,
                                 final @NonNull NextChannelsListFetchedCallback callback) {

        if (!AppUtility.isNetworkAvailable(context)) {
            PerkLogger.d(LOG_TAG, "loadNextChannels: Returning as network is unavailable!");
            isRequestInProgress = false;
            callback.onFailure();
            return;
        }

        if (isRequestInProgress) {
            PerkLogger.d(LOG_TAG, "loadNextChannels: Returning as request is already in progress!");
            return;
        }

        boolean isLoggedIn = UserInfoValidator.isAuthenticated(
                AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(context));

        isRequestInProgress = true;

        PerkLogger.d(LOG_TAG, "loadNextChannels: Loading channels data... isLoggedIn:" + isLoggedIn);

        /*PerkAPIController.INSTANCE.getAllChannelsVideos(context, isLoggedIn, offset,
                new OnRequestFinishedListener<List<Channel>>() {

                    @Override
                    public void onSuccess(@NonNull List<Channel> channelsList,
                                          @Nullable String s) {
                        PerkLogger.d(LOG_TAG, "loadNextChannels: Loading channels data successful");
                        isRequestInProgress = false;

                        // 'channels' can be null here in case the cached-data has expired after
                        // the corresponding timeout is reached... so in that case, we fetch the
                        // initial set of videos again & then add the current set of videos to it:
                        List<Channel> allChannelsTestList = allChannelsList.get();
                        if (allChannelsTestList == null || allChannelsTestList.isEmpty()) {
                            // fetch channels again:
                            PerkLogger.d(LOG_TAG,
                                    "allChannelsList returned null! Fetching initial set of "
                                            + "channels again...");

                            getAllChannelsList(context, false, allChannels -> {
                                PerkLogger.d(LOG_TAG,
                                        "onChannelListFetched after cache expiry: "
                                                + (allChannels != null ? allChannels.size() : "0"));

                                // Shouldn't happen, but if it does due to any error, then we
                                // simply return after setting empty list, & the process would be
                                // retried by user later:
                                if (allChannels == null) {
                                    allChannelsList.set(new ArrayList<>());
                                    callback.onSuccess(null);
                                } else {
                                    onChannelsSuccessfullyFetched(allChannels, channelsList);
                                }
                            });
                        } else {
                            onChannelsSuccessfullyFetched(allChannelsTestList, channelsList);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType,
                                          @Nullable PerkResponse<List<Channel>> perkResponse) {
                        isRequestInProgress = false;
                        PerkLogger.e(LOG_TAG,
                                "loadNextChannels: Loading channels data failed: " + (perkResponse != null
                                        ? perkResponse.getMessage()
                                        : context.getString(R.string.generic_error))
                                        + " -error-type: " + errorType);

                        PerkUtils.showErrorMessageToast(errorType,
                                (perkResponse != null ? perkResponse.getMessage()
                                        : context.getString(R.string.generic_error)));

                        callback.onFailure();
                    }

                    private void onChannelsSuccessfullyFetched(
                            @NonNull final List<Channel> allChannels,
                            @NonNull List<Channel> channelsList) {
                        allChannels.addAll(channelsList);
                        allChannelsList.set(new ArrayList<>(allChannels));

                        // cache the data:
                        cacheVideoDataFor(DUMMY_KEY, null);
                        callback.onSuccess(channelsList);
                    }
                });*/
    }

    public void getAllChannelsList(@NonNull Context context, boolean forceRefresh,
                                   @Nullable ChannelListFetchedCallback callback) {
        mCallback = callback;

        if (allChannelsList.get() == null || forceRefresh) {
            getAllChannels(context);
        } else {
            notifyCallback();
        }
    }

    public Channel findChannelByUuid(@NonNull String uuid) {
        Channel channel = findChannelByUuid(uuid, allChannelsList.get());
        return channel != null ? channel : findChannelByUuid(uuid,
                allChannelsListWithoutVideos.get());
    }

    private Channel findChannelByUuid(@NonNull String uuid, List<Channel> channelList) {
        if (channelList == null) {
            return null;
        }

        for (Channel channel : channelList) {
            if (channel != null && TextUtils.equals(uuid, channel.getUuid())) {
                return channel;
            }
        }

        return null;
    }

    public void fetchAllChannelsListWithoutVideos(@NonNull final Context context,
                                                  boolean isLoggedIn, @Nullable ChannelListFetchedCallback mCallback) {
        // Refresh the channels-list (without videos):
        WatchbackAPIController.INSTANCE.getAllChannelsWithoutVideos(context, isLoggedIn, new OnRequestFinishedListener<AllChannelsResponseModel>() {
            @Override
            public void onSuccess(@NonNull AllChannelsResponseModel allChannelsResponseModel, @Nullable String s) {
                PerkLogger.d(LOG_TAG, "Loading all-channels data without videos successful");

                if (allChannelsResponseModel.getChannels() != null) {
                    List<Channel> channelWrapper = WrapResponseModels.getWrappedChannelList(allChannelsResponseModel.getChannels());
                    allChannelsListWithoutVideos.set(channelWrapper);
                    List<Channel> favoriteChannels = new ArrayList<>();
                    if(PerkUtils.isUserLoggedIn()){
                        for(Channel channel: channelWrapper){
                            if(channel.isFavorite()){
                                favoriteChannels.add(channel);
                            }
                        }
                        PerkPreferencesManager.INSTANCE.saveUserChannelsIntoPreference(favoriteChannels);
                    }

                    if(mCallback != null){
                        mCallback.onChannelListFetched(channelWrapper);
                    }
                }else{
                    if(mCallback != null){
                        mCallback.onChannelListFetched(null);
                    }
                }


            }

            @Override
            public void onFailure(@NonNull ErrorType errorType, @Nullable PerkResponse<AllChannelsResponseModel> perkResponse) {
                if(mCallback != null){
                    mCallback.onChannelListFetched(null);
                }
                PerkLogger.e(LOG_TAG,
                        "Loading all-channels data without videos failed: " + (perkResponse != null
                                ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));

                PerkUtils.showErrorMessageToast(errorType,
                        (perkResponse != null ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));
            }
        });
    }

    private void notifyCallback() {
        if (mCallback != null) {
            mCallback.onChannelListFetched(allChannelsList.get());
            mCallback = null;
        }
    }

    @Override
    public void expired(Object key, Object value) {
        PerkLogger.d(LOG_TAG, "Expired invoked with key: " + key + " value: " + value);

        if (key instanceof String && TextUtils.equals(DUMMY_KEY, (String) key)) {

            if (isRequestInProgress) {
                PerkLogger.w(LOG_TAG,
                        "Ignoring expired callback since request to fetch channels is already in progress");
                return;
            }

            PerkLogger.d(LOG_TAG, "Clearing cached channels-data on expiry");
            allChannelsList.set(null);
        }
    }

    public Channel getChannelForVideoProvider(BrightcovePlaylistData.BrightcoveVideo video) {
        if (video == null || TextUtils.isEmpty(video.getProviderName())) {
            return null;
        }

        List<Channel> itemList = allChannelsListWithoutVideos.get();
        if (itemList == null || itemList.isEmpty()) {
            PerkLogger.e(LOG_TAG, "getChannelForVideoProvider: Channel list is null!");
            return null;
        }

        String provider = video.getProviderName();
        PerkLogger.d(LOG_TAG,
                "getChannelForVideoProvider: Searching Channel for provider: " + provider);
        for (Channel channel : itemList) {
            if (TextUtils.equals(channel.getTag(), provider)) {
                PerkLogger.d(LOG_TAG, "Found channel for provider: " + provider + " " + channel);
                return channel;
            }
        }

        return null;
    }
}
