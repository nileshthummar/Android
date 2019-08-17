package com.watchback2.android.models.movietickets;

import java.util.List;

public class VideoHistoryWapper {

    private List<VideoHistoryModel> mVideos;

    private int mVideosRemaining;

    private boolean mRewarded;

    private boolean mTotalRewardsRemaining;

    public VideoHistoryWapper(List<VideoHistoryModel> mVideos, int mVideosRemaining, boolean mRewarded, boolean mTotalRewardsRemaining ) {
        this.mVideos = mVideos;
        this.mVideosRemaining = mVideosRemaining;
        this.mRewarded = mRewarded;
        this.mTotalRewardsRemaining = mTotalRewardsRemaining;
    }

    public List<VideoHistoryModel> getmVideos() {
        return mVideos;
    }

    public int getmVideosRemaining() {
        return mVideosRemaining;
    }

    public boolean ismRewarded() {
        return mRewarded;
    }

    public boolean ismTotalRewardsRemaining() {
        return mTotalRewardsRemaining;
    }
}
