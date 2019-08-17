package com.watchback2.android.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;
import androidx.databinding.DataBindingUtil;

import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.controllers.ChannelsManager;
import com.watchback2.android.databinding.ActivityVideoDetailsBinding;
import com.watchback2.android.models.Channel;
import com.watchback2.android.models.VideoDetailsModelWrapper;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.viewmodels.VideoDetailsViewModel;

/**
 * Shown when user click on any thumbnail in the app
 * Has description of video
 * <p>
 * Task: https://jira.rhythmone.com/browse/PEWAN-527
 */
public class VideoDetailsActivity extends BaseThemeableActivity {

    private static final String LOG_TAG = "VideoDetailsActivity";

    public static final String VIDEO_DATA = "VIDEO_DATA";
    public static final String DATA_BUNDLE = "DATA_BUNDLE";
    public static final String IS_CAROUSAL_ITEM = "IS_CAROUSAL_ITEM";
    private VideoDetailsModelWrapper videoDetailsModelWrapper;
    private BrightcovePlaylistData.BrightcoveVideo videoData;
    private Bundle dataBundle = null;
    private boolean isPlayClicked = false;
    private VideoDetailsViewModel viewModel;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // No data available to show. Finish and return.
        if (getIntent() == null) {
            finish();
            return;
        }
        // Access and set required data to model and the model to ViewModel.
        videoData = (BrightcovePlaylistData.BrightcoveVideo) getIntent().getSerializableExtra(VIDEO_DATA);
        dataBundle = getIntent().getBundleExtra(DATA_BUNDLE);
        VideoDetailsModelWrapper model = setModelData(videoData);
        viewModel = new VideoDetailsViewModel(model);
        ActivityVideoDetailsBinding viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_video_details);
        viewDataBinding.setViewmodel(viewModel);

        // Event-tracking: 'Video Details Screen' - When a user lands on the video details screen for any video
        // Params:
        // 'Video ID', value: BC video id number string
        Bundle params = new Bundle();
        params.putString("Video ID", videoData.getId());
        FacebookEventLogger.getInstance().logEventWithParams("Video Details Screen", params);

        GestureDetectorCompat mDetector = new GestureDetectorCompat(this, new VideoDetailsGestureListener());
        viewDataBinding.clParentBg.setOnTouchListener((v, event) -> mDetector.onTouchEvent(event));

    }

    // Derives the required data from respective api models and sets it to
    // VideoDetailsModelWrapper. This data is used to populate this activity's ViewModel.
    private VideoDetailsModelWrapper setModelData(BrightcovePlaylistData.BrightcoveVideo videoData) {
        if (videoDetailsModelWrapper == null) {
            videoDetailsModelWrapper = new VideoDetailsModelWrapper();
        }
        // This might have season and episode number in it. We don't want that.
        // So, value from custom field will overwrite this, if available.
        String showTitle = videoData.getName();
        // Need Season and Episode number to show as subtitle.
        if (videoData.getCustomFields() != null) {
            videoDetailsModelWrapper.setSeasonNumber(videoData.getCustomFields().getSeasonNumber());
            videoDetailsModelWrapper.setEpisodeNumber(videoData.getCustomFields().getEpisodeNumber());
//            showTitle = videoData.getCustomFields().getShow();
            videoDetailsModelWrapper.setProviderName(videoData.getCustomFields().getProvider());
        }
        videoDetailsModelWrapper.setTitleText(showTitle);
        // Long description as returned from the api.
        videoDetailsModelWrapper.setLongDescription(videoData.getLongDescription());
        // Large background banner.
        videoDetailsModelWrapper.setCoverImageUrl(videoData.getPoster());
        videoDetailsModelWrapper.setShortDescription(videoData.getDescription());
        if (videoData.getSchedule() != null) {
            videoDetailsModelWrapper.setEndTime(videoData.getSchedule().getEndsAt());
        }

        Channel channel = ChannelsManager.INSTANCE.getChannelForVideoProvider(videoData);
        if (channel != null) {
            videoDetailsModelWrapper.setIconImageUrl(channel.getLogoImageUrl());
            videoDetailsModelWrapper.setRedirectBrandUrl(channel.getDestinationUrl(), channel.getmAndroidDestinationUrl(), channel.getmDefaultDestinationUrl());
            videoDetailsModelWrapper.setRedirectBrandBtnText(channel.getButtonText());
        }
        // Video id, so as to check progress
        videoDetailsModelWrapper.setVideoID(videoData.getId());
        videoDetailsModelWrapper.setDuration(videoData.getDuration());
        return videoDetailsModelWrapper;
    }

    public void onCloseClick(View view) {
        finish();
    }

    public void onPlayButtonClick(View view) {
        if (isPlayClicked) {
            // already processing the previous click.
            return;
        }
        isPlayClicked = true;

        if (VideoDetailsViewModel.PLAY.equalsIgnoreCase(viewModel.getPlayButtonText())) {
            // Facebook Analytics: Video Details-Play - When a user hits "Play" from the video details screen
            FacebookEventLogger.getInstance().logEvent("Video Details-Play");
        } else {
            // Facebook Analytics: Video Details-Resume - When a user hits "Resume" from the video details screen
            FacebookEventLogger.getInstance().logEvent("Video Details-Resume");
        }

        if (dataBundle != null && !dataBundle.getBoolean(IS_CAROUSAL_ITEM)) {
            Intent intent = new Intent(VideoDetailsActivity.this, VideoPlayerActivity.class);
            String mTitle = dataBundle.getString(VideoPlayerActivity.PLAYLIST_NAME);
            boolean mIsForChannelsList = dataBundle.getBoolean(VideoPlayerActivity.IS_CHANNEL_LIST);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!TextUtils.isEmpty(mTitle)) {
                intent.putExtra(VideoPlayerActivity.PLAYLIST_NAME, mTitle);
            }
            intent.putExtra(VideoPlayerActivity.IS_CHANNEL_LIST, mIsForChannelsList);
            startActivity(intent);
            finish();

        } else {
            AppUtility.fetchRelatedVideosAndStartPlayer(videoData, null, true,
                    successful -> {
                        PerkLogger.d(LOG_TAG,
                                "Searching for related videos completed! successful: "
                                        + successful);
                        finish();
                    });
        }

    }

    public void onWatchOnProviderClick(View view) {
        PerkLogger.d(LOG_TAG, "on watch provider click");
        if (viewModel == null || TextUtils.isEmpty(viewModel.getRedirectUrl())) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(AppUtility.replaceAccessTokenMacro(viewModel.getRedirectUrl())), view.getContext(),
                WatchbackWebViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        view.getContext().startActivity(intent);

        if (videoData == null || videoData.getChannel() == null) {
            return;
        }
        String value = videoData.getChannel().getName() + " - " + videoData.getChannel().getUuid();
        // Event-tracking: 'Video Details:Watch on Provider'
        // - When a user hits the Watch on Provider button on the Video Details Screen;
        // Params:
        // 'provider', value: channel uuid - Channel Name
        Bundle params = new Bundle();
        params.putString("provider", value);
        FacebookEventLogger.getInstance().logEventWithParams("Video Details-Watch on Provider", params);
    }

    private class VideoDetailsGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float x1 = e1.getX();
            float x2 = e2.getX();
            float z = Math.abs(x1 - x2);

            PerkLogger.d(LOG_TAG,
                    "User swipe detected. displacement:" + z);

            if (z > 200) {
                PerkLogger.d(LOG_TAG,
                        "User swipe detected. Dismissing the screen");
                finish();
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

}
