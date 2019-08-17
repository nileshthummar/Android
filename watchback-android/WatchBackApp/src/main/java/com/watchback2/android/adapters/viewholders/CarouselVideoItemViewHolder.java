package com.watchback2.android.adapters.viewholders;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.perk.util.PerkLogger;
import com.singular.sdk.Singular;
import com.watchback2.android.activities.WatchbackWebViewActivity;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.databinding.HomeCarouselItemBinding;
import com.watchback2.android.navigators.ICommonViewModelLoadHelper;
import com.watchback2.android.utils.AppUtility;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by perk on 24/3/18.
 * View Holder to contain UI for the items in the featured videos recycler view
 */

public class CarouselVideoItemViewHolder extends RecyclerView.ViewHolder {

    private static final String LOG_TAG = "CarouselItemVH";

    private final HomeCarouselItemBinding mHomeCarouselItemBinding;

    private final WeakReference<ICommonViewModelLoadHelper> mLoadHelperWeakReference;

    private int mTrayNumber = 0;

    private INotifyAdapter notifyAdapter;

    public interface INotifyAdapter{
        void notifyAdapterOfClick( BrightcovePlaylistData.BrightcoveVideo videoData);
    }

    public CarouselVideoItemViewHolder(@NonNull HomeCarouselItemBinding binding,
            ICommonViewModelLoadHelper commonViewModelLoadHelper, INotifyAdapter notifyAdapter) {
        super(binding.getRoot());
        this.notifyAdapter = notifyAdapter;
        mHomeCarouselItemBinding = binding;
        mLoadHelperWeakReference = new WeakReference<>(commonViewModelLoadHelper);

        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        // Set the max height for image:
        int desiredImageHeight = (int) (screenWidth * 0.5f);    // Set to 50% of screen-width
        ViewGroup.LayoutParams imageLayoutParams =
                mHomeCarouselItemBinding.imgCarousel.getLayoutParams();
        imageLayoutParams.height = desiredImageHeight;
        mHomeCarouselItemBinding.imgCarousel.setLayoutParams(imageLayoutParams);
    }

    public void bind(BrightcovePlaylistData.BrightcoveVideo videoData, int trayNumber) {
        mTrayNumber = trayNumber;

        mHomeCarouselItemBinding.setVideoData(videoData);
        mHomeCarouselItemBinding.executePendingBindings();

        mHomeCarouselItemBinding.videoCarouselItem.setOnClickListener(
                v -> handleVideoItemClick(v,
                        mHomeCarouselItemBinding.getVideoData()));
    }

    private void toggleLoading(boolean isLoading) {
        if (mLoadHelperWeakReference != null) {
            ICommonViewModelLoadHelper homeFragmentLoader = mLoadHelperWeakReference.get();
            if (homeFragmentLoader != null) {
                homeFragmentLoader.toggleLoading(isLoading);
            }
        }
    }

    private boolean isLoading() {
        if (mLoadHelperWeakReference != null) {
            ICommonViewModelLoadHelper homeFragmentLoader = mLoadHelperWeakReference.get();
            if (homeFragmentLoader != null) {
                return homeFragmentLoader.isLoading();
            }
        }
        return false;
    }

    private void handleVideoItemClick(View view, BrightcovePlaylistData.BrightcoveVideo videoData) {
        // Disable click events if we are processing a longform-video click event already:
        if (isLoading()) {
            PerkLogger.d(LOG_TAG,
                    "Ignoring video click event as previous click event is being processed...");
            return;
        }

        try{
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title",videoData.getParentListTitle());
            contextData.put("tve.userpath","Click:Video");
            contextData.put("tve.contenthub","Featured");

            String strModule =  "home:"+(mTrayNumber+1)+":"+1;
            contextData.put("tve.module",strModule);
            contextData.put("tve.action","true");

            contextData = AdobeTracker.appendVideoData(contextData,videoData);

            AdobeTracker.INSTANCE.trackAction("Click:Video",contextData);
            /////
        }catch (Exception e){}

        // Separate handling for URLs:
        if (videoData.isUrl()) {

            // Event-tracking: 'featured_content_tap' - Each time a featured item is tapped.
            // Params:
            // 'url', value being the actual URL:
            Bundle params = new Bundle();
            params.putString("url", videoData.getValue());
//            FacebookEventLogger.getInstance().logEventWithParams("featured_content_tap", params);

            // Facebook Analytics: Discover-Thumbnail
            // - When a user hits a thumbnail from Discover to be taken to the Video Details Screen
            FacebookEventLogger.getInstance().logEvent("Discover-Thumbnail");

            Singular.event("featured_content_tap", "url", videoData.getValue());

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoData.getValue()),
                    view.getContext(), WatchbackWebViewActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            view.getContext().startActivity(intent);

        } else {
            // As per https://rhythmone.atlassian.net/browse/PEWAN-102 : Videos listed on the
            // play screen should relate to the content being clicked... we get these
            // by tag search for the video ID. Those will be the videos that populate in the
            // video list.

            toggleLoading(true);

            // Event-tracking: 'featured_content_tap' - Each time a featured item is tapped.
            // Params:
            // 'video_id' value the video ID:

            String searchQuery;
            if (videoData.getVideoId() != 0) {
                searchQuery = "" + videoData.getVideoId();
            } else {
                searchQuery = videoData.getId();
            }

            Bundle params = new Bundle();
            params.putString("video_id", searchQuery);
//            FacebookEventLogger.getInstance().logEventWithParams("featured_content_tap", params);

            // Facebook Analytics: Discover-Thumbnail
            // - When a user hits a thumbnail from Discover to be taken to the Video Details Screen
            FacebookEventLogger.getInstance().logEvent("Discover-Thumbnail");

            Singular.event("featured_content_tap", "video_id", searchQuery);

            if(notifyAdapter != null){
                notifyAdapter.notifyAdapterOfClick(videoData);
            }

            AppUtility.showVideoDetailsActivity(videoData, null);

            toggleLoading(false);


        }
    }
}
