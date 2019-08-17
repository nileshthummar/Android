package com.watchback2.android.models.movietickets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.request.model.Data;

import java.util.List;

public class VideoHistoryResponseModel extends Data {

    @Expose
    @SerializedName("rewarded")
    private boolean mRewarded;
    @Expose
    @SerializedName("total_rewards_remaining")
    private boolean mTotalRewardsRemaining;
    @Expose
    @SerializedName("videos")
    private List<VideosEntity> mVideos;
    @Expose
    @SerializedName("videos_remaining")
    private int mVideosRemaining;

    public List<VideosEntity> getVideos() {
        return mVideos;
    }

    public int getVideosRemaining() {
        return mVideosRemaining;
    }

    public boolean getRewarded() {
        return mRewarded;
    }

    public boolean getTotalRewardsRemaining() {
        return mTotalRewardsRemaining;
    }

}
