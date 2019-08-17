package com.watchback2.android.adapters.viewholders;

import com.watchback2.android.R;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.databinding.PlayerVideoItemBinding;
import com.watchback2.android.navigators.IVideosRecyclerViewNavigator;
import com.watchback2.android.utils.PerkPreferencesManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by perk on 15/03/18.
 */


public class PlayerVideoViewHolder extends RecyclerView.ViewHolder {

    private final PlayerVideoItemBinding mPlayerVideoItemBinding;

    private final IVideosRecyclerViewNavigator mNavigator;

    public PlayerVideoViewHolder(@NonNull PlayerVideoItemBinding binding,
                               @NonNull IVideosRecyclerViewNavigator navigator) {
        super(binding.getRoot());
        mPlayerVideoItemBinding = binding;
        mNavigator = navigator;
    }

    public void bind(BrightcovePlaylistData.BrightcoveVideo videoData, boolean playingNow,
            boolean willPlayNext, boolean isForChannelsList) {

        mPlayerVideoItemBinding.setVideoData(videoData);
        mPlayerVideoItemBinding.setPlayingNow(playingNow);
        mPlayerVideoItemBinding.setWillPlayNext(willPlayNext);
        mPlayerVideoItemBinding.setIsForChannelsList(isForChannelsList);
        mPlayerVideoItemBinding.executePendingBindings();

        mPlayerVideoItemBinding.idVideoItemContainer.setOnClickListener(
                v -> mNavigator.handleVideoItemClick(
                        mPlayerVideoItemBinding.getVideoData()));
    }

    private int getBackgroundColor() {
        return PerkPreferencesManager.INSTANCE.isNightMode() ? R.color.app_background
                : R.color.app_background_light;
    }
}
