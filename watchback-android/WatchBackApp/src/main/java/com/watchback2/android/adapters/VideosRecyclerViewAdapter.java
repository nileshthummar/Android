package com.watchback2.android.adapters;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.watchback2.android.R;
import com.watchback2.android.activities.VideoDetailsActivity;
import com.watchback2.android.activities.VideoPlayerActivity;
import com.watchback2.android.adapters.viewholders.CarouselVideoItemViewHolder;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.databinding.HomeCarouselItemBinding;
import com.watchback2.android.navigators.IVideosRecyclerViewNavigator;
import com.watchback2.android.utils.AppUtility;

import java.util.HashMap;
import java.util.List;

/**
 * Created by perk on 15/3/18.
 * Common RecyclerView Adapter for showing video-items on HomeFragment (non-featured) &
 * ChannelsFragment
 */

public class VideosRecyclerViewAdapter extends RecyclerView.Adapter<CarouselVideoItemViewHolder> implements
        IVideosRecyclerViewNavigator {

    private static final String LOG_TAG = "VideosRecyclerViewAdapter";

    private List<BrightcovePlaylistData.BrightcoveVideo> mVideoList;

    private final Context mContext;

    private String mTitle;

    private int mTrayNumber = 0;

    public VideosRecyclerViewAdapter(@NonNull List<BrightcovePlaylistData.BrightcoveVideo> videoList,
                                     @NonNull Context context) {
        super();
        mContext = context;

        setList(videoList);
    }

    @NonNull
    @Override
    public CarouselVideoItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        HomeCarouselItemBinding videoItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(mContext), R.layout.home_carousel_item, parent,
                false);
        return new CarouselVideoItemViewHolder(videoItemBinding, null, null);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselVideoItemViewHolder holder, int position) {
        holder.bind(mVideoList.get(position), position);
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

    @Override
    public void handleVideoItemClick(@NonNull BrightcovePlaylistData.BrightcoveVideo videoData) {
        VideoPlayerActivity.nCurrentIndex = mVideoList.indexOf(videoData);
        VideoPlayerActivity.arrVideos = mVideoList;

        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(mTitle)) {
            bundle.putString(VideoPlayerActivity.PLAYLIST_NAME, mTitle);
        }
        bundle.putBoolean(VideoDetailsActivity.IS_CAROUSAL_ITEM, false);
        AppUtility.showVideoDetailsActivity(videoData, bundle);



       /* Intent intent = new Intent(mContext, VideoPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!TextUtils.isEmpty(mTitle)) {
            intent.putExtra(VideoPlayerActivity.PLAYLIST_NAME, mTitle);
        }
        intent.putExtra(VideoPlayerActivity.IS_CHANNEL_LIST, mIsForChannelsList);
        mContext.startActivity(intent);
*/
        try {
            /////

            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title", "Featured");
            contextData.put("tve.userpath", "Click:Video");
            contextData.put("tve.contenthub", "Featured");

            String strModule = "home:" + (mTrayNumber + 1) + ":" + (mVideoList.indexOf(videoData) + 1);
            contextData.put("tve.module", strModule);
            contextData.put("tve.action", "true");

            contextData = AdobeTracker.appendVideoData(contextData, videoData);

            AdobeTracker.INSTANCE.trackAction("Click:Video", contextData);


            /////
        } catch (Exception e) {
        }
    }

    public void setVideoPlayerTitleAndTrayNumber(String title, int trayNumber) {
        mTitle = title;
        mTrayNumber = trayNumber;
    }

}
