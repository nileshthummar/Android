package com.watchback2.android.viewmodels;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.request.auth.AuthAPIRequestController;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.api.LeanplumAPIController;
import com.watchback2.android.api.PostFavChannels;
import com.watchback2.android.api.WatchbackAPIController;
import com.watchback2.android.controllers.HomeScreenItemsListManager;
import com.watchback2.android.helper.UserInfoValidator;
import com.watchback2.android.models.Channel;
import com.watchback2.android.models.LeanplumPostModel;
import com.watchback2.android.models.channels.AllChannelsResponseModel;
import com.watchback2.android.navigators.IChannelsListNavigator;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkPreferencesManager;
import com.watchback2.android.utils.PerkUtils;
import com.watchback2.android.utils.WrapResponseModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelsViewModel {

    private static final String LOG_TAG = "ChannelsViewModel";

    private final Context context;

    private final ObservableBoolean dataLoading = new ObservableBoolean(false);

    private final ObservableField<List<Channel>> channelsList = new ObservableField<>();

    private final List<Channel> mSelectedChannels = new ArrayList<>();

    private IChannelsListNavigator iChannelsListNavigator;

    private boolean isEditingFromSettings;

    public ChannelsViewModel(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    public void setiChannelsListNavigator(IChannelsListNavigator iChannelsListNavigator) {
        this.iChannelsListNavigator = iChannelsListNavigator;
    }

    public void setEditingFromSettings(boolean editingFromSettings) {
        isEditingFromSettings = editingFromSettings;
    }

    public boolean isEditingFromSettings() {
        return isEditingFromSettings;
    }

    public void handleCancelButtonClick(View view) {
        iChannelsListNavigator.finishChannelActivity();

    }

    public void handleDoneButtonClick(View view) {
        // Set enabled to false to prevent user from clicking twice
        view.setEnabled(false);

        // Call this again so that the channels are sorted & stored in the way they appear in the
        // original server response, otherwise it will be stored in the way user selected them.
        updateSelectedChannelsList();
        PerkPreferencesManager.INSTANCE.saveUserChannelsIntoPreference(getSelectedChannels());

        // Invalidate the homescreen-data since in case of any updates, we need to get the
        // updated response from homescreen-API
        HomeScreenItemsListManager.INSTANCE.invalidateHomeScreenItemList();

        // Update user's interests via userattributes on Leanplum:
        updateLeanplumUserAttributes(getSelectedChannels());

        if (isLoggedIn() && mSelectedChannels != null) {

            PostFavChannels postFavChannels = new PostFavChannels(context);
            postFavChannels.setPostChannelCallback(new PostFavChannels.PostChannelCallback() {
                @Override
                public void onCompleted() {
                    dataLoading.set(false);
                    if (iChannelsListNavigator != null) {
                        iChannelsListNavigator.gotoNextActivity();
                    }
                }
            });
            dataLoading.set(true);
            postFavChannels.makeCall(mSelectedChannels);

        } else {
            iChannelsListNavigator.gotoNextActivity();
        }
    }

    private void updateLeanplumUserAttributes(List<Channel> channelList) {
        List<String> selectedChannels = new ArrayList<>();

        for (Channel channel : channelList) {
            selectedChannels.add(channel.getName());
        }

        String selectedChannelNames = selectedChannels.isEmpty() ? "" : TextUtils.join(",",
                selectedChannels);

        // Update user's channels via userattributes on Leanplum:
        PerkLogger.d(LOG_TAG,
                "Updating Leanplum userAttributes for channels: " + selectedChannelNames);

        Map<String, String> map = new HashMap<>();
        map.put(AppUtility.LEANPLUM_CHANNELS_ATTRIBUTE, selectedChannelNames);

        LeanplumAPIController.INSTANCE.updateUserAttributes(map,
                new OnRequestFinishedListener<LeanplumPostModel>() {
                    @Override
                    public void onSuccess(@NonNull LeanplumPostModel leanplumPostModel,
                            @Nullable String s) {
                        PerkLogger.d(LOG_TAG,
                                "Successful updating Leanplum userAttributes for channels!\n"
                                        + leanplumPostModel.toString());
                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType,
                            @Nullable PerkResponse<LeanplumPostModel> perkResponse) {
                        PerkLogger.e(LOG_TAG,
                                "Failed updating Leanplum userAttributes for channels: "
                                        + (perkResponse != null ? perkResponse.toString()
                                        : ""));
                    }
                });
    }

    public void loadChannelsList() {
        if (dataLoading.get()) {
            return;
        }

        PerkLogger.d(LOG_TAG, "Loading channels list data...");

        dataLoading.set(true);

        if (getChannelsList().get() != null) {
            getChannelsList().get().clear();
        }

        WatchbackAPIController.INSTANCE.getAllChannelsWithoutVideos(context, isLoggedIn(), new OnRequestFinishedListener<AllChannelsResponseModel>() {

            @Override
            public void onSuccess(@NonNull AllChannelsResponseModel channelsModel, @Nullable String s) {
                PerkLogger.d(LOG_TAG, "Loading channels screen data successful");
                dataLoading.set(false);

                List<Channel> channelList = WrapResponseModels.getWrappedChannelList(channelsModel.getChannels());
                if (channelList != null) {

                    // Update the selection values if user is not logged in (as the
                    // user's selection is stored locally in that case)
                    List<Channel> storedChannelList = isLoggedIn() ? null :
                            PerkPreferencesManager.INSTANCE.getUserChannelListFromPreferences();
                    if (storedChannelList != null) {
                        for (Channel storedChannel : storedChannelList) {
                            if (storedChannel != null && storedChannel.isFavorite()) {
                                for (final Channel channel : channelList) {
                                    if (channel != null && TextUtils.equals(
                                            channel.getUuid(), storedChannel.getUuid())) {
                                        channel.setFavorite(true);
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    getChannelsList().set(channelList);
                }
                updateSelectedChannelsList();
            }

            @Override
            public void onFailure(@NonNull ErrorType errorType,
                                  @Nullable PerkResponse<AllChannelsResponseModel> perkResponse) {
                dataLoading.set(false);
                PerkLogger.e(LOG_TAG, "Loading channels screen data failed: " + (perkResponse != null
                        ? perkResponse.getMessage() : ""));

                PerkUtils.showErrorMessageToast(errorType,
                        (perkResponse != null ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));
            }
        });
    }

    public ObservableBoolean getDataLoading() {
        return dataLoading;
    }

    public ObservableField<List<Channel>> getChannelsList() {
        return channelsList;
    }

    private List<Channel> getSelectedChannels() {
        return mSelectedChannels;
    }

    private boolean isLoggedIn() {
        return UserInfoValidator.isAuthenticated(
                AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(context));
    }

    public void onItemUpdated(@NonNull Channel channelItem) {
        if (channelItem.isFavorite()) {
            getSelectedChannels().add(channelItem);
        } else {
            getSelectedChannels().remove(channelItem);
        }
    }

    private void updateSelectedChannelsList() {
        List<Channel> channelsList = getChannelsList().get();

        if (channelsList == null) {
            PerkLogger.e(LOG_TAG, "channelsList is null! ");
            return;
        }

        getSelectedChannels().clear();
        for (Channel channel : channelsList) {
            if (channel != null && channel.isFavorite()) {
                getSelectedChannels().add(channel);
            }
        }
    }
}
