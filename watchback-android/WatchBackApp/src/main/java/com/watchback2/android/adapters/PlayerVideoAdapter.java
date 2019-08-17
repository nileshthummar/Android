package com.watchback2.android.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.watchback2.android.R;
import com.watchback2.android.adapters.viewholders.PlayerVideoViewHolder;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.databinding.LayoutBrandDetailsItemBinding;
import com.watchback2.android.databinding.PlayerVideoItemBinding;
import com.watchback2.android.navigators.IVideosRecyclerViewNavigator;

import java.util.List;

/**
 * Adapter used by VideoPlayerActivity, BrandDetailsActivity,
 * SearchActivity & for AccountFragment
 *
 * *NOTE*: Make sure any changes done do not break either of the classes that use this adapter!
 */
public class PlayerVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        IVideosRecyclerViewNavigator {
    private static final String TAG = "tag";
    private List<BrightcovePlaylistData.BrightcoveVideo> mVideoList;
    private final IVideosRecyclerViewNavigator mNavigator;

    private int mNowPlayingIndex;

    private int mPlayNextIndex;

    private boolean mIsForChannelsList;

    public PlayerVideoAdapter(@NonNull List<BrightcovePlaylistData.BrightcoveVideo> videoList,
            @NonNull IVideosRecyclerViewNavigator navigator, boolean isForChannelsList) {
        super();
        mNavigator = navigator;
        mIsForChannelsList = isForChannelsList;
        setList(videoList);
        updateIndexes(-1, -1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mIsForChannelsList) {
            LayoutBrandDetailsItemBinding videoItemBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()), R.layout.layout_brand_details_item, parent,
                    false);

            return new BrandDetailsItemViewHolder(videoItemBinding, this);
        }else{
            PlayerVideoItemBinding mPlayerVideoItemBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()),R.layout.player_video_item, parent, false);
            return new PlayerVideoViewHolder(mPlayerVideoItemBinding, this);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PlayerVideoViewHolder) {
            ((PlayerVideoViewHolder) holder).bind(mVideoList.get(position),
                    position == mNowPlayingIndex, position == mPlayNextIndex, mIsForChannelsList);
        }else if(holder instanceof BrandDetailsItemViewHolder){
            ((BrandDetailsItemViewHolder) holder).bind(mVideoList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mVideoList != null ? mVideoList.size() : 0;
    }

    private void setList(@NonNull List<BrightcovePlaylistData.BrightcoveVideo> videoList) {
        mVideoList = videoList;
        notifyDataSetChanged();
    }

    public void replaceData(@NonNull List<BrightcovePlaylistData.BrightcoveVideo> videoList) {
        setList(videoList);
    }

    public void updateIndexes(int nowPlayingIndex, int playNextIndex) {
        mNowPlayingIndex = nowPlayingIndex;
        mPlayNextIndex = playNextIndex;
        notifyDataSetChanged();
    }

    public void setForChannelsList(boolean isForChannelsList) {
        mIsForChannelsList = isForChannelsList;
        notifyDataSetChanged();
    }

    @Override
    public void handleVideoItemClick(@NonNull BrightcovePlaylistData.BrightcoveVideo videoData) {
        mNavigator.handleVideoItemClick(videoData);
    }

    /*package*/ class BrandDetailsItemViewHolder extends RecyclerView.ViewHolder{
        private LayoutBrandDetailsItemBinding binding;
        private IVideosRecyclerViewNavigator listener;

        /*package*/ BrandDetailsItemViewHolder(@NonNull LayoutBrandDetailsItemBinding binding, IVideosRecyclerViewNavigator listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        /*package*/ void bind(BrightcovePlaylistData.BrightcoveVideo video){
            binding.setVideoData(video);

            binding.imgCarousel.setOnClickListener(
                    v -> mNavigator.handleVideoItemClick(
                            binding.getVideoData()));
        }
    }

}