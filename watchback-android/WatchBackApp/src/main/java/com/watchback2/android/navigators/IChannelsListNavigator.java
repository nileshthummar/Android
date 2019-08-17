package com.watchback2.android.navigators;

import androidx.annotation.NonNull;

import com.watchback2.android.models.Channel;

public interface IChannelsListNavigator {

    void gotoNextActivity();

    void onItemUpdated(@NonNull Channel channelItem);

    void finishChannelActivity();
}
