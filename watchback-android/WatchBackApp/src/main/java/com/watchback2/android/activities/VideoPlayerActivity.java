package com.watchback2.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brightcove.player.analytics.Analytics;
import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.mediacontroller.BrightcoveMediaController;
import com.brightcove.player.mediacontroller.ShowHideController;
import com.brightcove.player.model.DeliveryType;
import com.brightcove.player.model.Source;
import com.brightcove.player.model.Video;
import com.brightcove.player.view.BrightcoveExoPlayerVideoView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.metadata.id3.Id3Frame;
import com.google.android.exoplayer2.metadata.id3.PrivFrame;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.leanplum.Leanplum;
import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.request.auth.AuthAPIRequestController;
import com.perk.util.PerkLogger;
import com.singular.sdk.Singular;
import com.watchback2.android.BuildConfig;
import com.watchback2.android.R;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.analytics.Plugin;
import com.watchback2.android.analytics.PluginEventListener;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.api.PerkAdView;
import com.watchback2.android.api.WatchbackAPIController;
import com.watchback2.android.controllers.WatchBackSettingsController;
import com.watchback2.android.databinding.ActivityVideoPlayerBinding;
import com.watchback2.android.exoplayer.TrackSelectionHelper;
import com.watchback2.android.helper.Bindings;
import com.watchback2.android.helper.UserInfoValidator;
import com.watchback2.android.models.NullableDataModel;
import com.watchback2.android.navigators.IVideosRecyclerViewNavigator;
import com.watchback2.android.utils.AppConstants;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkPreferencesManager;
import com.watchback2.android.utils.PerkUtils;
import com.watchback2.android.utils.TrackingEvents;
import com.watchback2.android.views.WatchBackBrightcovePlayer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class VideoPlayerActivity extends WatchBackBrightcovePlayer implements
        IVideosRecyclerViewNavigator {

    // TODO: Refactor to get the below 2 values as Intent extras
    @NonNull
    public static List<BrightcovePlaylistData.BrightcoveVideo> arrVideos = new ArrayList<>();
    public static int nCurrentIndex = 0;

    public static final String PLAYLIST_NAME = "playlist_name";
    public static final String IS_CHANNEL_LIST = "is_channel_list";

    private String strBRAccountID = BuildConfig.brightcoveAccountId;

    // ========================================================================
    // Class members
    // =============================================
    // The tag used by this class for logging
    private static final String TAG = "VideoPlayerActivity";


    //private static String post_ad_sdk = "none";
    private static int post_ad_filled = 0;
    //@Nullable
    //private PerkVideoView perkVideoView;
    private BrightcoveExoPlayerVideoView brightcoveVideoView;

    private boolean mUserPausedVideo = false;

    private int mVideoPlayCounter = 0;
    private int mAutoPlaySetting;

    /** Holds the FFWD-percent setting value returned by settings-API (in millis)  */
    private float mFwdPercecntSetting = -1f;

    /** Holds the calculated value in millis depending on {@link #mFwdPercecntSetting} & current video
     *  duration */
    private long mFwdMillisSetting = -1L;

    /** Duration in millis of the current video being played */
    private long mCurrentVideoDuration = -1L;

    /** Will be set to true if the current video was fast-forwarded by amount >=
     * {@link #mFwdMillisSetting} */
    private boolean mHasFastForwardedVideo = false;

    /** Will be true when an ad starts */
    private boolean mAdPlaying = false;

    /** Handler instance used for showing AYSW prompt */
    private Handler mHandler;

    /** Indicates if the AYSW prompt has already been shown for current video */
    private boolean mHasShownAyswPrompt = false;

    /** true if video being currently played is a longform video */
    private boolean mIsCurrentVideoLongform = false;

    /** Duration in millis after which the AYSW prompt will be shown once for currently being
     * played video if longform */
    private long mAyswPromptDelay = -1L;

    /** Duration after which the video will be closed if there is no response until this time
     * runs out */
    private long mAYSWPromptNoResponseMillis = -1L;

    /** Used to identify if video-start tracking events have already been sent -Since event
     * 'Play' is sometimes called twice when user manually clicks on a new video in the list */
    private boolean mHasTrackedInitialEvents = false;

    /** Holds the last duration reached for current video, used to determine the percentage
     * completion */
    private long mCompletedMillis = -1L;

    /** Will be true if the current longform video is being seeked to the last-viewed position,
     * so that it isn't considered as user-fast-forwarded manually */
    private boolean mIsAutoSeeked = false;

    /** Stores the last play-head position -used to calculate when the next save is to be
     * performed. */
    private long mLastPlayheadPosition = 0L;

    /** Holds the longform_complete_pct setting value returned by settings-API (in percent)  */
    private float mCompletePercentSetting = -1f;

    private EventEmitter eventEmitter; //FreeWheel

    //private FreeWheelController freeWheelController;

    ///

    private RecyclerView mVideosRecyclerView;
//    private PlayerVideoAdapter mPlayerVideoAdapter;

    //

    TextView video_title;

    String strVideoID = "";
    String strPerkReward = "";

    private boolean mLastPreRollAdFilled = false;

    private boolean mLastVideoCompleted = false;

    private boolean mResumeVideoOnConnectivity = false;

    private String mPlaylistName;

    private boolean mIsForChannelsList;

    private int mDesiredVideoPlayerHeight;

    private ActivityVideoPlayerBinding mActivityVideoPlayerBinding;

    private BrightcovePlaylistData.BrightcoveVideo mVideoData;

    private Plugin plugin;

    ///for exoplayer
    private PlayerView exoPlayerView;
    private SimpleExoPlayer exoPlayer;
    Button btnCC;
    DefaultTrackSelector trackSelector;
    TrackSelectionHelper trackSelectionHelper;
    boolean bIsExoBufferStarged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        registerFinishReceiver();

        mHandler = new Handler();

        mVideoPlayCounter = 0;
        mUserPausedVideo = false;
        mAutoPlaySetting = WatchBackSettingsController.INSTANCE.getAutoPlayVideoCountSetting(
                getApplicationContext());

        PerkLogger.d(TAG, "mAutoPlaySetting count set to : " + mAutoPlaySetting);

        mFwdPercecntSetting =
                WatchBackSettingsController.INSTANCE.getFastforwardPercentSetting(
                        getApplicationContext());

        PerkLogger.d(TAG, "mFwdPercecntSetting set to : " + mFwdPercecntSetting);

        mAyswPromptDelay =
                WatchBackSettingsController.INSTANCE.getAYSWPromptMillisSetting(
                        getApplicationContext());

        PerkLogger.d(TAG, "mAyswPromptDelay set to : " + mAyswPromptDelay);

        mAYSWPromptNoResponseMillis =
                WatchBackSettingsController.INSTANCE.getAYSWPromptNoResponseMillisSetting(
                        getApplicationContext());

        PerkLogger.d(TAG, "mAYSWPromptNoResponseMillis set to : " + mAYSWPromptNoResponseMillis);

        mCompletePercentSetting =
                WatchBackSettingsController.INSTANCE.getLongformCompletionPercentSetting(
                        getApplicationContext());

        PerkLogger.d(TAG, "mCompletePercentSetting set to : " + mCompletePercentSetting);


        mActivityVideoPlayerBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_video_player);

        mActivityVideoPlayerBinding.idContinue.setOnClickListener(v -> {
            if (brightcoveVideoView == null) {
                PerkLogger.e(TAG, "on-Continue: brightcoveVideoView is null!");
                return;
            }

            mHasShownAyswPrompt = false;

            toggleAYSWPrompt(false);
            if (mIsCurrentVideoLongform){
                playExoPlayer();
            }
            else{
                brightcoveVideoView.post(() -> {
                    if (brightcoveVideoView != null) {
                        brightcoveVideoView.start();
                    }
                });
            }

        });

        mActivityVideoPlayerBinding.idExit.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setMessage("Are you sure you want to exit the video?");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", (dialog, which) -> {
                mHasShownAyswPrompt = false;
                AppUtility.dismissDialog(dialog);
                VideoPlayerActivity.this.finish();
            });
            builder.setNegativeButton("No", (dialog, which) -> {
                AppUtility.dismissDialog(dialog);
                mHandler.removeCallbacks(mAYSWPromptNoResponseRunnable);
                mHandler.postDelayed(mAYSWPromptNoResponseRunnable, mAYSWPromptNoResponseMillis);
            });
            builder.show();

            mHandler.removeCallbacks(mAYSWPromptNoResponseRunnable);
            mHandler.postDelayed(mAYSWPromptNoResponseRunnable,mAYSWPromptNoResponseMillis);
        });

        mActivityVideoPlayerBinding.idResumePlaying.setOnClickListener(v -> {
            if (brightcoveVideoView == null) {
                PerkLogger.e(TAG, "on-ResumePlaying: brightcoveVideoView is null!");
                return;
            }
            toggleResumePlayingPrompt(false);
            mIsAutoSeeked = true;
            if (mIsCurrentVideoLongform) {
                exoPlayer.seekTo(PerkPreferencesManager.INSTANCE.getLastPositionForLongformVideo(
                        strVideoID));

                playExoPlayer();

            } else {
                brightcoveVideoView.post(() -> {
                    if (brightcoveVideoView == null) {
                        PerkLogger.e(TAG,
                                "on-ResumePlaying:postCall: brightcoveVideoView is null!");
                        return;
                    }
                    brightcoveVideoView.seekTo((int)
                            PerkPreferencesManager.INSTANCE.
                                    getLastPositionForLongformVideo(strVideoID));

                    brightcoveVideoView.start();
                });
            }

        });

        mActivityVideoPlayerBinding.idResumePlayingFromStart.setOnClickListener(v -> {
            if (brightcoveVideoView == null) {
                PerkLogger.e(TAG, "on-ResumePlayingFromStart: brightcoveVideoView is null!");
                return;
            }

            toggleResumePlayingPrompt(false);

            // clear stored value since we are resuming from beginning:
            PerkLogger.d(TAG, "on-ResumePlayingFromStart: Clearing saved playhead position");
            PerkPreferencesManager.INSTANCE.saveLongformVideoPosition(strVideoID, -1);

            if (mIsCurrentVideoLongform) {
                playExoPlayer();
            } else {
                brightcoveVideoView.post(() -> {
                    if (brightcoveVideoView != null) {
                        brightcoveVideoView.start();
                    }
                });
            }

        });

        mIsForChannelsList = false;

        if (getIntent() != null) {
            mPlaylistName = getIntent().getStringExtra(PLAYLIST_NAME);
            mIsForChannelsList = getIntent().getBooleanExtra(IS_CHANNEL_LIST, false);
        }
        updateVideoSubtitle(mPlaylistName);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        brightcoveVideoView = findViewById(R.id.brightcove_video_view);
        exoPlayerView  = findViewById(R.id.exo_player_view);
        initExoPlayer();
        initFullscreenButton();
        // Calculate & set video-player height assuming aspect ratio of 16:9
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        int referenceDimension = screenHeight > screenWidth ? screenWidth : screenHeight;
        mDesiredVideoPlayerHeight = Math.round(referenceDimension * 9f / 16f);
        PerkLogger.d(TAG, "Player width/height set to: " + referenceDimension + "/" + mDesiredVideoPlayerHeight);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            brightcoveVideoView.setLayoutParams(param);
            //exoPlayerView.setLayoutParams(param);
        } else {
            // PORTRAIT:
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, mDesiredVideoPlayerHeight);
            param.topMargin = (int) getResources().getDimension(
                    R.dimen.video_player_toolbar_height);
            brightcoveVideoView.setLayoutParams(param);
          //  exoPlayerView.setLayoutParams(param);
        }

        mActivityVideoPlayerBinding.playIcon.setOnClickListener(v -> {
            if (brightcoveVideoView == null) {
                PerkLogger.e(TAG, "play-icon: onClick: brightcoveVideoView is null!");
                return;
            }
            if (mIsCurrentVideoLongform){
                playExoPlayer();
            }
            else{
                brightcoveVideoView.post(() -> {
                    if (brightcoveVideoView != null) {
                        brightcoveVideoView.start();
                    }
                });
            }

        });

        // Bring Video-view to front:
        brightcoveVideoView.bringToFront();
        // Bring the play-icon in front so that it is shown whenever required:
        mActivityVideoPlayerBinding.playContainer.bringToFront();

        // Bring scrim-view, AYSW-view & the resume-playing view to front
        mActivityVideoPlayerBinding.scrimView.bringToFront();
        mActivityVideoPlayerBinding.resumePlayingContainer.bringToFront();
        mActivityVideoPlayerBinding.ayswContainer.bringToFront();
        // Bring the progress-bar to the top:
        mActivityVideoPlayerBinding.idProgressBar.bringToFront();

        // Bring the close-button in front of the video-view:
        mActivityVideoPlayerBinding.btnCloseBg.bringToFront();
        mActivityVideoPlayerBinding.btnClose.bringToFront();

        mVideosRecyclerView = mActivityVideoPlayerBinding.playerRecyclerView;
        video_title = mActivityVideoPlayerBinding.videoTitle;
        initPerkMediaPlayer();
////Nielsen start
        try {

            plugin = new Plugin(brightcoveVideoView.getEventEmitter(), brightcoveVideoView, exoPlayer);
            plugin.setPluginEventListener(mPluginEventListerner);
        } catch (Exception ex) {
            Log.e(TAG, "Exception during creation of JSONObject: " + ex);
        }
        ////Nielsen end

        /*if (!arrVideos.isEmpty()) {
            LinearLayoutManager manager = new LinearLayoutManager(this);
            DividerItemDecoration itemDecoration = new DividerItemDecoration(this,
                    manager.getOrientation());
            itemDecoration.setDrawable(
                    getResources().getDrawable(R.drawable.videos_list_divider_with_padding));

            mPlayerVideoAdapter = new PlayerVideoAdapter(arrVideos, this, false);
            mVideosRecyclerView.setHasFixedSize(true);
            mVideosRecyclerView.setLayoutManager(manager);
            mVideosRecyclerView.addItemDecoration(itemDecoration);
            mVideosRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mVideosRecyclerView.setAdapter(mPlayerVideoAdapter);

        }*/

        startPlayer(false);
        final ImageView btnClose = findViewById(R.id.btnClose);

        btnClose.setClickable(true);
        btnClose.setOnClickListener(v -> {
            if (brightcoveVideoView != null) {
                brightcoveVideoView.pause();
                brightcoveVideoView.stopPlayback();
            }
            if (exoPlayer != null){
                exoPlayer.stop();
                exoPlayer = null;
            }
            finish();
        });

        postSensorOrientation();

        try{
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Video Player");
            contextData.put("tve.userpath","Video Player");
            contextData.put("tve.contenthub","Video Player");
            if (arrVideos.get(nCurrentIndex) != null) {
                contextData = AdobeTracker.appendVideoData(contextData,arrVideos.get(nCurrentIndex));
            }
            contextData.put("tve.contentmode","Portrait");
            AdobeTracker.INSTANCE.trackState("Video Player",contextData);
            /////
        }catch (Exception e){}
    }


    @Override
    protected void onDestroy() {
        unregisterFinishReceiver();
        unregisterConnectivityChangeReceiver();
        releaseExoPlayer();

        super.onDestroy();
    }

    private void releaseExoPlayer() {
        if (exoPlayer != null) {
            exoPlayer.removeListener(mPlayerEventListener);
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (brightcoveVideoView != null && brightcoveVideoView.isFullScreen()
                && eventEmitter != null) {
            // EXIT FULL SCREEN if we are in fullscreen mode
            eventEmitter.emit(EventType.EXIT_FULL_SCREEN);
        } else {
            finish();
        }
    }

    private void updateVideoSubtitle(String videoSubtitle) {
        if (mActivityVideoPlayerBinding == null) {
            return;
        }

        final String subTitle = !TextUtils.isEmpty(videoSubtitle) ? videoSubtitle : mPlaylistName;

        if (!TextUtils.isEmpty(subTitle)) {
            mActivityVideoPlayerBinding.channelName.setText(subTitle);
            mActivityVideoPlayerBinding.channelName.setVisibility(View.VISIBLE);
        } else {
            mActivityVideoPlayerBinding.channelName.setVisibility(View.GONE);
        }
    }

    private void onNextVideo(boolean postVideoCall) {

        if (!AppUtility.isNetworkAvailable(getApplicationContext(), false)) {
            PerkLogger.w(TAG, "onNextVideo(): Returning as network connectivity is unavailable!");
            return;
        }

        try {
            // Event tracking handling for Leanplum (for longform videos):
            boolean wasLongForm = trackEventsOnVideoDone(mLastVideoCompleted);

            // Do not skip views API call for videos tagged redemption_partner:
            // https://jira.rhythmone.com/browse/PEWAN-486
            //BrightcovePlaylistData.BrightcoveVideo lastVideo = getCurrentVideo();
            /*if (lastVideo != null) {
                wasLastVideoTaggedRedemptionPartner = AppUtility.isRedemptionPartnerTaggedVideo(
                        lastVideo);
                if (wasLastVideoTaggedRedemptionPartner) {
                    PerkLogger.d(TAG,
                            "Last played video was tagged redemption_partner...skipping request to "
                                    + "award points");
                }
            }*/

            if (postVideoCall){

                if (wasLongForm){
                    AdobeTracker.INSTANCE.trackAppsFlyerEvent(getApplicationContext(),"complete_longform_video",null);

                    // Singular: complete_longform_video - user completes longform video
                    Singular.event("complete_longform_video");

                    // Facebook Analytics: Longform Complete - user completes longform video
                    FacebookEventLogger.getInstance().logEvent("Longform Complete");
                    // Facebook Analytics: Video Complete - user completes video
                    FacebookEventLogger.getInstance().logEvent("Video Complete");

                } else {
                    // --'complete_shortform' should be tracked when a user completes a video and
                    // fastforward=false.
                    if (!mHasFastForwardedVideo) {
                        AdobeTracker.INSTANCE.trackAppsFlyerEvent(getApplicationContext(),"complete_shortform_video",null);
                    } else {
                        PerkLogger.d(TAG, "Not tracking 'complete_shortform_video' event on fast-forward");
                    }

                    // Singular: complete_shortform_video - user completes shortform video
                    Singular.event("complete_shortform_video");

                    // Facebook Analytics: Shortform Complete - user completes shortform video
                    FacebookEventLogger.getInstance().logEvent("Shortform Complete");
                    // Facebook Analytics: Video Complete - user completes video
                    FacebookEventLogger.getInstance().logEvent("Video Complete");
                }

                // AppsFlyer user-retention events:
                if (PerkUtils.sApplication.shouldTrackAppsFlyerRetentionEvents()) {
                    String event = "D" + PerkUtils.sApplication.getAppsFlyerRetentionDay()
                            + "_app_open_video_complete";

                    PerkLogger.d(TAG, "Tracking retention event: " + event);

                    AdobeTracker.INSTANCE.trackAppsFlyerEvent(getApplicationContext(),
                            event, null);
                }
            }

            if (postVideoCall) {
                postVideoView();
            } else {
                PerkLogger.d(TAG, "onNextVideo(): Not sending points-award request on error with video");
            }

            nCurrentIndex++;

            if (nCurrentIndex < arrVideos.size()) {
                startPlayer(wasLongForm);
            } else {
                PerkLogger.d(TAG, "onNextVideo(): Stopping as we have reached the end of the list");

                // Move back the nCurrentIndex value, just in case user replays the same video
                // again -so that the methods that rely on getting video via current-index work
                // correctly
                nCurrentIndex--;

                // Also reset all variables here:
                mLastVideoCompleted = false;
                mHasFastForwardedVideo = false;
                mLastPreRollAdFilled = false;
                mHasShownAyswPrompt = false;
                mCurrentVideoDuration = -1L;
                mFwdMillisSetting = -1L;
                mCompletedMillis = -1L;
                mHasTrackedInitialEvents = false;
                mIsAutoSeeked = false;
                mLastPlayheadPosition = 0L;

                mHandler.removeCallbacks(mAyswPromptRunnable);

                brightcoveVideoView.seekTo(0);
                if (exoPlayer != null) exoPlayer.seekToDefaultPosition();
                // Clear current playing video index so that it's updated on UI
//                mPlayerVideoAdapter.updateIndexes(-1, nCurrentIndex + 1);

                // hide seek-bar:
                updateSeekBarState(true);

                // hide loading indicator
                mActivityVideoPlayerBinding.idProgressBar.setVisibility(View.GONE);

                // show play icon:
                togglePlayButton(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void postVideoView() {
        postVideoView(false);
    }

    private void postVideoView(boolean forVideoStartEvent) {
        boolean isUserLoggedIn = UserInfoValidator.isAuthenticated(
                AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(
                        getApplicationContext()));

        try {
            int percentComplete = forVideoStartEvent ? 0 : ((mCompletedMillis > 0) ? Math.round(
                    (mCompletedMillis * 100.0f) / mCurrentVideoDuration) : 0);

            PerkLogger.d(TAG,
                    "postVideoView: \nforVideoStartEvent: " + forVideoStartEvent
                            + "\nstrVideoID: " + strVideoID
                            + "\nstrPerkReward: " + strPerkReward
                            + "\nmLastPreRollAdFilled: " + mLastPreRollAdFilled
                            + "\nmHasFastForwardedVideo: " + mHasFastForwardedVideo
                            + "\nmCompletedMillis: " + mCompletedMillis
                            + "\nmCurrentVideoDuration: " + mCurrentVideoDuration
                            + "\npercentComplete: " + percentComplete
                            + "\nmLastVideoCompleted: " + mLastVideoCompleted);

            if (!forVideoStartEvent && mCompletedMillis <= 0 && mIsCurrentVideoLongform) {
                PerkLogger.d(TAG,
                        "postVideoView: Ignoring making views API call when percent complete is 0 for "
                                + "last long form video");
                return;
            }

            if (!forVideoStartEvent && mLastVideoCompleted) {
                PerkLogger.d(TAG,
                        "postVideoView: Setting percentComplete to 100 on mLastVideoCompleted "
                                + "being true!");
                percentComplete = 100;
            }

            // default to 90%
            float minPercentCompletionLongform = 90;

            if (mCompletePercentSetting == -1f) {
                PerkLogger.d(TAG,
                        "postVideoView: mCompletePercentSetting unavailable, will default to 90!");
                mCompletePercentSetting =
                        WatchBackSettingsController.INSTANCE.getLongformCompletionPercentSetting(
                                getApplicationContext());
            } else {
                minPercentCompletionLongform = mCompletePercentSetting;
            }

            if (!forVideoStartEvent && ((mIsCurrentVideoLongform && percentComplete >= minPercentCompletionLongform) || (
                    !mIsCurrentVideoLongform && mLastVideoCompleted && !mHasFastForwardedVideo))) {
                // locally store that video was watched:
                PerkPreferencesManager.INSTANCE.onVideoWatched(strVideoID, mIsCurrentVideoLongform);
                // Update on UI:
                arrVideos.get(nCurrentIndex).setWatched(true);

                // also check if this video needs to be removed from home-screen (for longform only):
                /*if (mIsCurrentVideoLongform) {
                    HomeScreenItemsListManager.INSTANCE.removeWatchedLongformIfNeeded();
                }*/
            }

            if (!isUserLoggedIn) {
                PerkLogger.d(TAG, "postVideoView: Ignoring request to award points as user is not logged in!");
                return;
            }

            WatchbackAPIController.INSTANCE.postVideoView(getApplicationContext(), strVideoID,
                    strPerkReward,
                    (!forVideoStartEvent && mLastPreRollAdFilled),
                    mHasFastForwardedVideo,
                    ("" + percentComplete),
                    new OnRequestFinishedListener<PerkAdView>() {

                        @Override
                        public void onSuccess(@NonNull PerkAdView perkAdView, @Nullable String s) {
                            try {
                                PerkLogger.d(TAG, "postVideoView():perkAdView: onSuccess: " + s
                                        + " response: " + perkAdView.toString());

                                // PerkUtils.getSharedPreferencesEditor().putString
                                // ("strPointMessage",s).commit();

                                if (!forVideoStartEvent) {
                                    if (!TextUtils.isEmpty(s)) {
                                        // Toast messages shown to inform user about the entries:
                                        // https://jira.rhythmone.com/browse/PEWAN-492
                                        Toast toast = Toast.makeText(getApplicationContext(), s,
                                                Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0,
                                                (int) (mDesiredVideoPlayerHeight / 2.25f));
                                        toast.show();
                                    }

                                    // https://jira.rhythmone.com/browse/PEWAN-507 : Leanplum
                                    // event tracking as per 'point_type' obtained
                                    if (!TextUtils.isEmpty(perkAdView.getPointType())) {
                                        PerkLogger.d(TAG,
                                                "Obtained point-type: " + perkAdView.getPointType()
                                                        + " -tracking same as Leanplum event");
                                        Leanplum.track(perkAdView.getPointType());
                                    }

                                    PerkUtils.trackEvent("Points Rewarded");
                                    PerkUtils.GetUserInfo();
                                }

                                try{
                                    /////

                                    HashMap<String, Object> contextData = new HashMap<String, Object>();
                                    contextData.put("tve.title","Video Player");
                                    contextData.put("tve.userpath","Event:Points Accrued");
                                    contextData.put("tve.contenthub","Video Player");
                                    contextData.put("tve.points","true");
                                    if (mVideoData != null){
                                        contextData = AdobeTracker.appendVideoData(contextData,mVideoData);
                                    }
                                    String strAmount = String.valueOf(perkAdView.getAmount());
                                    contextData.put("tve.rewardsvalue",strAmount);
                                    AdobeTracker.INSTANCE.trackAction("Event:Points Accrued",contextData);

                                    /////
                                }catch (Exception e){}
                            } catch (Exception e) {
                            }

                        }

                        @Override
                        public void onFailure(@NonNull ErrorType errorType,
                                @Nullable PerkResponse<PerkAdView> perkResponse) {

                            PerkLogger.e(TAG,
                                    "postVideoView():perkAdView:onFailure(): " + errorType.getResponseCode()
                                            + " " + (perkResponse != null
                                            ? perkResponse.getMessage() : ""));

                            boolean showToast = true;

                            // 412 will be returned when Max number of devices that can be logged
                            // in simultaneously, is crossed
                            if (errorType.getResponseCode() == 412 && perkResponse != null
                                    && !TextUtils.isEmpty(perkResponse.getMessage())) {

                                showToast = false;

                               /* // Pause any video being played as we are about to be logged-out
                                if (brightcoveVideoView != null) {
                                    brightcoveVideoView.postDelayed(() -> {
                                        if (brightcoveVideoView != null) {
                                            brightcoveVideoView.pause();
                                            brightcoveVideoView.stopPlayback();
                                            brightcoveVideoView.clear();
                                        }

                                        stopExoPlayer();
                                        // Reset count to 0 if user manually opens a video
                                        mVideoPlayCounter = 0;
                                        // Reset state of variable tracking if user manually paused a video:
                                        mUserPausedVideo = false;
                                        // Reset variable specifying if want to resume video on regaining network connectivity:
                                        mResumeVideoOnConnectivity = false;
                                        nCurrentIndex = -1;
                                        // Clear current playing video index in that the adapter so that it's indicated in UI
                                        mPlayerVideoAdapter.updateIndexes(-1, -1);
                                    }, 900);
                                }

                                showToast = false;*/

                                /*AppUtility.showGenericDialog(VideoPlayerActivity.this,
                                        perkResponse.getMessage(),
                                        getResources().getString(R.string.ok),
                                        (dialog, which) -> {*/
                                            PerkLogger.d(TAG, "Initiating Log out for other devices...");
                                            WatchbackAPIController.INSTANCE.postUserLogOut(getApplicationContext(),
                                                    new OnRequestFinishedListener<NullableDataModel>() {
                                                        @Override
                                                        public void onSuccess(@NonNull NullableDataModel nullableDataModel,
                                                                @Nullable String s) {
                                                            PerkLogger.d(TAG, "onUserLogOut(): Successful!");
                                                            onComplete();
                                                        }

                                                        @Override
                                                        public void onFailure(@NonNull ErrorType errorType,
                                                                @Nullable PerkResponse<NullableDataModel> perkResponse) {
                                                            PerkLogger.e(TAG,
                                                                    "onUserLogOut(): Failed! " + (perkResponse != null
                                                                            ? perkResponse.getMessage() : ""));
                                                            onComplete();
                                                        }

                                                        private void onComplete() {
                                                            // As per https://jira.rhythmone.com/browse/PEWAN-501
                                                            // -user should
                                                            // not get logged out of current
                                                            // device anymore:
                                                            //AppUtility.logOutUser(PerkUtils
                                                            // .sApplication);
                                                            //VideoPlayerActivity.this.finish();

//                                                            AppUtility.dismissDialog(dialog);
                                                        }
                                                    });
                                        /*});*/
                            }

                            // No need to show toast for these cases as it is handled by
                            // PerkUtils.showErrorMessageToast
                            if (errorType == ErrorType.UNAUTHORIZED || errorType == ErrorType.ACCOUNT_SUSPENDED) {
                                showToast = false;
                            }

                            try {
                                if (showToast) {
                                    String toastMsg =
                                            (perkResponse != null) ? perkResponse.getMessage() : "";
                                    if (!TextUtils.isEmpty(toastMsg)) {
                                        Toast.makeText(getApplicationContext(), toastMsg,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                                if (perkResponse != null) {
                                    if (perkResponse.getMessage() != null) {

                                        String strPointMessage = perkResponse.getMessage();

                                        if (strPointMessage.length() > 0) {
                                    /*Toast toast = Toast.makeText(getApplicationContext(),
                                    AppUtility.changePerkToLoyalty(strPointMessage) , Toast
                                    .LENGTH_SHORT);
                                    TextView v =  toast.getView().findViewById(android.R.id
                                    .message);
                                    if (v != null) v.setGravity(Gravity.CENTER);*/

                                            // Do not show toast message for points being
                                            // awarded: https://rhythmone.atlassian
                                            // .net/browse/PEWAN-58
                                            //toast.show();

                                            PerkLogger.d(TAG, "perkAdView: onFailure: "
                                                    + AppUtility.changePerkToLoyalty(
                                                    strPointMessage));

                                            PerkUtils.trackEvent("Points Rewarded");
                                            PerkUtils.GetUserInfo();
                                        }
                                    }
                                }

                            } catch (Exception e) {
                            }

                            PerkUtils.showErrorMessageToast(errorType,null);
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Map<String, String> getPlayData(Event event) {
        if (event == null) {
            return null;
        }

        Map<String, String> lMap = new HashMap();
        String video_name = "";
        String video_url = "";
        Video videoTmp;

        Source tmp;
        String channelName = "";
        if (event.properties.containsKey(Event.VIDEO)) {
            videoTmp = (Video) event.properties.get(Event.VIDEO);
            video_name = videoTmp.getStringProperty("name");

            if (video_name.equals("")) {
                video_name = "undefined";
            }

            if (event.properties.containsKey(Event.SOURCE)) {
                tmp = (Source) event.properties.get(Event.SOURCE);
                video_url = tmp.getUrl();
            } else {
                PerkLogger.d(TAG, "Event does not contain SOURCE");
            }
        }
        channelName = video_name + " - " + video_url;
        lMap.put("channelName", channelName);

        return lMap;
    }




    private void setupFreeWheel() {



        /*freeWheelController = new FreeWheelController(this, brightcoveVideoView,
                eventEmitter);
        //configure your own IAdManager or supply connection information


        //NBCU start

        int nAdNetworkID = 169843;
        freeWheelController.setAdNetworkId(nAdNetworkID);
        freeWheelController.setProfile(nAdNetworkID+":watchback_android");
        ////
        String strAdUrl = "http://29773.v.fwmrm.net/ad/p/1";
        freeWheelController.setAdURL(strAdUrl);
        freeWheelController.enable();
        ////
        String csid = "";

        if (PerkUtils.isTablet(getApplicationContext())){
            csid = "watchback_tablet_android_app_ondemand";

        }
        else{
            csid="watchback_phone_android_app_ondemand";

        }
        freeWheelController.setSiteSectionId(csid);*/
        //NBCU end
        eventEmitter.on(EventType.SET_SOURCE, new EventListener() {
            @Override
            public void processEvent(Event event) {
                PerkLogger.d(TAG, "processEvent(): on event SET_SOURCE: " + event.getType());
                mLastPreRollAdFilled = false;
                //boolean disablePreRollAds = false;

                // Reset the current video duration when source is changed:
                mCurrentVideoDuration = -1L;
                mFwdMillisSetting = -1L;
                mCompletedMillis = -1L;

                // if we are being called again when trying to resume a video that paused on
                // losing connectivity, then do not request ads for this condition:
                /*if (mResumeVideoOnConnectivity) {
                    PerkLogger.d(TAG,
                            "processEvent(): SET_SOURCE: Disabling ads as we are trying to resume"
                                    + " video on regaining lost network connectivity!");
                    freeWheelController.setAdRequestingEnabled(false);
                    return;
                }

                Video video = (Video) event.properties.get(Event.VIDEO);
                if (video == null) {
                    PerkLogger.e(TAG, "SET_SOURCE video is null");
                    return;
                }

                PerkLogger.d(TAG,
                        "processEvent(): SET_SOURCE: Is for current video: " + TextUtils.equals(
                                video.getId(), arrVideos.get(nCurrentIndex).getId())
                                + " video-duration: " + video.getDuration());
                if (!TextUtils.equals(video.getId(), arrVideos.get(nCurrentIndex).getId()) ) {
                    return;
                }

                BrightcovePlaylistData.CustomFields customFields = arrVideos.get(
                        nCurrentIndex).getCustomFields();

                // Disable pre-roll ads if this is a longform video OR 'redemption_partner'
                // tagged video:
                if (customFields != null && Boolean.parseBoolean(customFields.getLongform())) {
                    PerkLogger.d(TAG, "processEvent(): Disabling pre-roll ads for longform video");
                    disablePreRollAds = true;
                } else if (AppUtility.isRedemptionPartnerTaggedVideo(arrVideos.get(nCurrentIndex))) {
                    PerkLogger.d(TAG,
                            "processEvent(): Disabling pre-roll ads for redemption-partner video");
                    disablePreRollAds = true;
                }
                ////


                Log.w("Freewheel","strAdUrl -> "+strAdUrl);
                ///

                freeWheelController.setAdRequestingEnabled(!disablePreRollAds);*/


            }
        });

        /*eventEmitter.on(FreeWheelEventType.WILL_SUBMIT_AD_REQUEST, new EventListener() {
            @Override
            public void processEvent(Event event) {
                PerkLogger.d(TAG, "processEvent(): on event: " + event.getType());

                Video video = (Video) event.properties.get(Event.VIDEO);
                IAdContext adContext = (IAdContext) event.properties.get(
                        FreeWheelController.AD_CONTEXT_KEY);
                //for Moat SDK
                ExtensionManager.registerExtension("FWTrackerManager", FWTrackerManager.class);
                adContext.loadExtension("FWTrackerManager");
                ///
                IConstants adConstants = adContext.getConstants();
                AdRequestConfiguration adRequestConfiguration =
                        (AdRequestConfiguration) event.properties.get(
                                FreeWheelController.AD_REQUEST_CONFIGURATION_KEY);

                /////////
                if(adRequestConfiguration != null){

                    ////
                    BrightcovePlaylistData.CustomFields customFields = arrVideos.get(
                            nCurrentIndex).getCustomFields();

                    String strCurrentVideoID = video.getId();
                    String externaladvertiserid = customFields.getExternaladvertiserid() != null? customFields.getExternaladvertiserid():strCurrentVideoID;
                    String _fw_did_google_advertising_id = "";
                   // String _fw_did_android_id = "";
                    String c9 = "";
                    String comscore_did_x = "";
                    String comscore_impl_type = "";
                    String comscore_platform = "";
                    String comscore_device = "";
                    String uoo = "";
                    if (PerkUtils.isLimitAdTrackingEnabled()){

                        _fw_did_google_advertising_id = "optout";
                      //  _fw_did_android_id = "optout";
                        c9 = "optout";
                        comscore_did_x = "none";
                        comscore_impl_type = "none";
                        comscore_platform = "none";
                        comscore_device = "none";
                        uoo = "1";
                    }
                    else{
                        _fw_did_google_advertising_id =  PerkUtils.getAdvertisingId();
                      //  _fw_did_android_id = PerkUtils.getDeviceID();
                        c9 = PerkUtils.getAdvertisingId() != null ? PerkUtils.getAdvertisingId():PerkUtils.getDeviceID();
                        comscore_did_x = PerkUtils.getAdvertisingId() != null ? PerkUtils.getAdvertisingId():PerkUtils.getDeviceID();
                        comscore_impl_type = "a";
                        comscore_platform = "android";
                        comscore_device = PerkUtils.getDeviceName();
                        uoo = "0";
                    }

                    String nielsen_device_group = "";
                    String csid = "";
                    if (PerkUtils.isTablet(getApplicationContext())){
                        nielsen_device_group = "TAB";
                        csid = "watchback_tablet_android_app_ondemand";
                    }
                    else{
                        nielsen_device_group = "PHN";
                        csid="watchback_phone_android_app_ondemand";
                    }


                    adRequestConfiguration.addKeyValueConfiguration(new KeyValueConfiguration("_fw_did_google_advertising_id", _fw_did_google_advertising_id));
                   // adRequestConfiguration.addKeyValueConfiguration(new KeyValueConfiguration("_fw_did_android_id", _fw_did_android_id));
                    adRequestConfiguration.addKeyValueConfiguration(new KeyValueConfiguration("nielsen_device_group", nielsen_device_group));
                    adRequestConfiguration.addKeyValueConfiguration(new KeyValueConfiguration("nielsen_platform", "MBL"));
                    adRequestConfiguration.addKeyValueConfiguration(new KeyValueConfiguration("_fw_nielsen_app_id", "PAD3C6E72-ED61-417F-A865-3AB63FDB6197"));
                    adRequestConfiguration.addKeyValueConfiguration(new KeyValueConfiguration("comscore_did_x", comscore_did_x));
                    adRequestConfiguration.addKeyValueConfiguration(new KeyValueConfiguration("comscore_impl_type", comscore_impl_type));
                    adRequestConfiguration.addKeyValueConfiguration(new KeyValueConfiguration("comscore_platform", comscore_platform));
                    adRequestConfiguration.addKeyValueConfiguration(new KeyValueConfiguration("comscore_device", comscore_device));
                    adRequestConfiguration.addKeyValueConfiguration(new KeyValueConfiguration("c9", "devid,"+c9));
                    adRequestConfiguration.addKeyValueConfiguration(new KeyValueConfiguration("uoo", uoo));
/////////////

                try{
                    CapabilityConfiguration fwCapabilityConfiguration = new CapabilityConfiguration("+emcr+qtcb+slcb+fbad+vicb+sltp",IConstants.CapabilityStatus.DEFAULT);
                    adRequestConfiguration.addCapabilityConfiguration(fwCapabilityConfiguration);
                }catch (Exception e){}

                    try{
                        SiteSectionConfiguration fwSiteSectionConfiguration = new SiteSectionConfiguration(csid,IConstants.IdType.CUSTOM);
                        adRequestConfiguration.setSiteSectionConfiguration(fwSiteSectionConfiguration);
                    }catch (Exception e){}

                }

                //////////////
                // This overrides what the plugin does by default for setVideoAsset() which is to
                // pass in currentVideo.getId().
                /////

                /////
                BrightcovePlaylistData.CustomFields customFields = arrVideos.get(
                        nCurrentIndex).getCustomFields();
                String strCurrentVideoID = arrVideos.get(nCurrentIndex).getId();
                String externaladvertiserid = customFields.getExternaladvertiserid() != null? customFields.getExternaladvertiserid():strCurrentVideoID;
              VideoAssetConfiguration fwVideoAssetConfiguration = new VideoAssetConfiguration(
                      externaladvertiserid,
                      IConstants.IdType.CUSTOM,
                        //FW uses their duration as seconds; Android is in milliseconds
                        video.getDuration() / 1000,
                      IConstants.VideoAssetDurationType.EXACT,
                      IConstants.VideoAssetAutoPlayType.ATTENDED
              );
              try{
                 // fwVideoAssetConfiguration.setVideoPlayRandom(Integer.parseInt(strCurrentVideoID));
                  adRequestConfiguration.setVideoAssetConfiguration(fwVideoAssetConfiguration);
              }catch (Exception e){}



                // Add preroll
                PerkLogger.d(TAG, "Adding temporal slot for prerolls");
                TemporalSlotConfiguration prerollSlot = new TemporalSlotConfiguration("larry",
                        adConstants.ADUNIT_PREROLL(), 0);
                adRequestConfiguration.addSlotConfiguration(prerollSlot);
            }
        });*/

        if (BuildConfig.DEBUG) {
            eventEmitter.on(EventType.ANY, event -> PerkLogger.d(TAG,
                    "DEBUG-processEvent(): Got event: " + event.getType()));
        }

        // TODO: Enable this only for debugging event-properties, otherwise it slows-down the UI:
        /*eventEmitter.on(EventType.ANY, event -> PerkLogger.d(TAG,
                "DEBUG-processEvent(): Got event: " + event.getType() + " " + (new ToStringBuilder(
                        null).append("properties", event.getProperties()).toString())));*/

        //freeWheelController.enable();
    }

    private boolean trackEventsOnVideoDone(boolean videoCompleted) {
        if (arrVideos == null || nCurrentIndex < 0 || nCurrentIndex >= arrVideos.size()) {
            PerkLogger.e(TAG,
                    "trackEventsOnVideoDone(): arrVideos is null OR nCurrentIndex is invalid! "
                            + "nCurrentIndex: "
                            + nCurrentIndex);
            return false;
        }

        BrightcovePlaylistData.BrightcoveVideo currentVideo = arrVideos.get(
                nCurrentIndex);

        if (currentVideo == null) {
            PerkLogger.e(TAG, "trackEventsOnVideoDone(): currentVideo is null!");
            return false;
        }

        BrightcovePlaylistData.CustomFields customFields = currentVideo.getCustomFields();

        boolean isLongForm = true;

        if (customFields == null || !Boolean.parseBoolean(customFields.getLongform())) {
            PerkLogger.d(TAG, "trackEventsOnVideoDone(): Video isn't longform!");
            isLongForm = false;
        }

        PerkLogger.d(TAG,
                "trackEventsOnVideoDone(): Tracking for video-id: " + currentVideo.getId()
                        + " videoCompleted: " + videoCompleted + " isLongForm: " + isLongForm);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(TrackingEvents.LEANPLUM_PARAMETER_VIDEO_ID, String.valueOf(currentVideo.getId()));
        if (isLongForm) {
            Leanplum.track(videoCompleted ? TrackingEvents.LEANPLUM_LONGFORM_COMPLETE
                    : TrackingEvents.LEANPLUM_LONGFORM_EXIT, params);
        } else {
            Leanplum.track(videoCompleted ? TrackingEvents.LEANPLUM_SHORTFORM_COMPLETE
                    : TrackingEvents.LEANPLUM_SHORTFORM_EXIT, params);
        }

        return isLongForm;
    }

    // Method to initialize MediaPlayer
    private void initPerkMediaPlayer() {
        Analytics analytics = brightcoveVideoView.getAnalytics();
        analytics.setAccount(strBRAccountID);
        eventEmitter = brightcoveVideoView.getEventEmitter();

        brightcoveVideoView.setMediaController(
                new BrightcoveMediaController(brightcoveVideoView, R.layout.my_media_controller));


        // Handling for video minimize/maximize actions to exit/enter Full-screen:
        eventEmitter.on(EventType.ENTER_FULL_SCREEN, mFullScreenToggleEventListener);
        eventEmitter.on(EventType.EXIT_FULL_SCREEN, mFullScreenToggleEventListener);

        eventEmitter.on(ShowHideController.HIDE_MEDIA_CONTROLS, mHideMediaControlsListener);
        eventEmitter.on(ShowHideController.DID_HIDE_MEDIA_CONTROLS, mHideMediaControlsListener);

        setupFreeWheel();
    }

    private EventListener mHideMediaControlsListener = new EventListener() {
        @Override
        public void processEvent(Event event) {
            PerkLogger.d(TAG, "mHideMediaControlsListener(): processEvent: " + event.getType());
            if (brightcoveVideoView != null && !brightcoveVideoView.isPlaying()
                    && (!mUserPausedVideo || mResumeVideoOnConnectivity)) {
                PerkLogger.d(TAG,
                        "mHideMediaControlsListener(): Preventing hiding media controls when no "
                                + "video is playing!");
                if (eventEmitter != null && !mIsCurrentVideoLongform) {
                    eventEmitter.emit(ShowHideController.SHOW_MEDIA_CONTROLS);
                }
                event.stopPropagation();
            }
        }
    };

    private EventListener mFullScreenToggleEventListener = new EventListener() {
        @Override
        public void processEvent(Event event) {
            PerkLogger.d(TAG, "mFullScreenToggleEventListener: Got event: " + event.getType());

            switch (event.getType()) {
                case EventType.ENTER_FULL_SCREEN:
                    // Manually set orientation to landscape first
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    break;
                case EventType.EXIT_FULL_SCREEN:
                    // Manually set orientation to portrait first
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                    break;
                default:
                    PerkLogger.d(TAG, "mFullScreenToggleEventListener: Ignoring event: " + event.getType());
                    break;
            }
        }
    };

    private Runnable mOrientationRunnable = () -> {
        if (brightcoveVideoView == null) {
            PerkLogger.e(TAG,
                    "mOrientationRunnable: Returning as brightcoveVideoView instance is null!");
            return;
        }
        PerkLogger.d(TAG, "mOrientationRunnable: Setting orientation to sensor");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    };

    private void postSensorOrientation() {
        if (brightcoveVideoView == null) {
            PerkLogger.e(TAG, "postSensorOrientation(): Ignoring as brightcoveVideoView is null!");
            return;
        }

        brightcoveVideoView.removeCallbacks(mOrientationRunnable);
        PerkLogger.e(TAG, "postSensorOrientation(): Posting delayed mOrientationRunnable call");
        brightcoveVideoView.postDelayed(mOrientationRunnable, 3600);
    }

    private void updateSeekBarState(boolean forceHide) {
        if (forceHide) {
            PerkLogger.d(TAG, "updateSeekBarState(): Force-disabling seek");
            updateSeekView(true);
            return;
        }

        if (arrVideos == null || nCurrentIndex < 0 || nCurrentIndex >= arrVideos.size()) {
            PerkLogger.e(TAG,
                    "updateSeekBarState(): arrVideos is null OR nCurrentIndex is invalid! "
                            + "nCurrentIndex: "
                            + nCurrentIndex);
            return;
        }

        BrightcovePlaylistData.BrightcoveVideo currentVideo = arrVideos.get(nCurrentIndex);

        if (currentVideo == null) {
            PerkLogger.e(TAG, "updateSeekBarState(): currentVideo is null!");
            return;
        }

        BrightcovePlaylistData.CustomFields customFields = currentVideo.getCustomFields();

        PerkLogger.d(TAG, "updateSeekBarState(): customFields for video: " + (customFields != null
                ? customFields.toString() : ""));

        // Disable seek for video, if specified in custom field
        if (customFields != null && Boolean.valueOf(customFields.getDisableSeek())) {
            PerkLogger.d(TAG, "updateSeekBarState(): Disabling seek...");
            updateSeekView(true);
        } else {
            updateSeekView(false);
        }
    }

    private void updateSeekView(boolean disable) {
        if (brightcoveVideoView == null) {
            PerkLogger.w(TAG, "updateSeekView(): brightcoveVideoView is null!");
            return;
        }

        if (disable) {
            brightcoveVideoView.findViewById(R.id.seek_container).setVisibility(View.GONE);
            brightcoveVideoView.findViewById(R.id.empty_spacer).setVisibility(View.VISIBLE);
            brightcoveVideoView.findViewById(R.id.rewind_container).setVisibility(View.GONE);

            // ExoPlayer
            brightcoveVideoView.findViewById(R.id.exo_seek_container).setVisibility(View.GONE);
            brightcoveVideoView.findViewById(R.id.exo_empty_spacer).setVisibility(View.VISIBLE);
        } else {
            brightcoveVideoView.findViewById(R.id.seek_container).setVisibility(View.VISIBLE);
            brightcoveVideoView.findViewById(R.id.empty_spacer).setVisibility(View.GONE);
            brightcoveVideoView.findViewById(R.id.rewind_container).setVisibility(View.VISIBLE);

            // ExoPlayer
            brightcoveVideoView.findViewById(R.id.exo_seek_container).setVisibility(View.VISIBLE);
            brightcoveVideoView.findViewById(R.id.exo_empty_spacer).setVisibility(View.GONE);
        }
    }

    private void startPlayer(final boolean wasPreviousVideoLongForm) {

        if (BuildConfig.DEBUG) {
            new Exception("startPlayer() called: ").printStackTrace();
        }

        PerkUtils.getIdThread();
        // Also reset all variables here:
        mLastPreRollAdFilled = false;
        mCurrentVideoDuration = -1L;
        mFwdMillisSetting = -1L;
        mCompletedMillis = -1L;
        mLastVideoCompleted = false;
        mHasFastForwardedVideo = false;
        mHasShownAyswPrompt = false;
        mIsCurrentVideoLongform = false;
        mHandler.removeCallbacks(mAyswPromptRunnable);
        mHasTrackedInitialEvents = false;
        mIsAutoSeeked = false;
        mLastPlayheadPosition = 0L;

        PerkLogger.d(TAG, "startPlayer(): wasPreviousVideoLongForm = " + wasPreviousVideoLongForm);

        updateSeekBarState(true);

        if (!AppUtility.isNetworkAvailable(getApplicationContext())) {
            PerkLogger.d(TAG, "startPlayer(): Returning as network unavailable!");
            return;
        }

        /*if (mAdPlaying) {
            PerkLogger.d(TAG,
                    "startPlayer(): Called when an ad is already playing... stopping ad!");
            if (freeWheelController != null) {
                freeWheelController.getAdCuePointComponent().skipCurrentAd();
                mAdPlaying = false;
            } else {
                // Shouldn't happen, but in case this happens, then we cannot do anything at this
                // point to stop the ad... so we just wait until the ad finishes
                PerkLogger.e(TAG, "freeWheelController is null! Returning!");
                return;
            }
        }*/

        try {
            setBrightcoveVideoContentVisibility(false);
            stopExoPlayer();

            if (arrVideos.get(nCurrentIndex) != null) {
                strVideoID = arrVideos.get(nCurrentIndex).getId();

                mVideoData = arrVideos.get(nCurrentIndex);
                mIsCurrentVideoLongform = mVideoData.isLongform();
                plugin.setCurrentVideo (mVideoData);
                final Catalog catalog = new Catalog(eventEmitter, strBRAccountID, BuildConfig.brightcoveApiPolicyKey);

                // Show loading indicator:
                mActivityVideoPlayerBinding.idProgressBar.setVisibility(View.VISIBLE);

                catalog.findVideoByID(strVideoID, new VideoListener() {
                    // Add the video found to the queue with add().
                    // Start playback of the video with start().
                    @Override
                    public void onVideo(Video video) {

                        //addCastMetaData(video);

                        PerkLogger.d(TAG, "onVideo: video = " + video + " \nsources: " + video.getSourceCollections().toString());

                        if (brightcoveVideoView == null) {
                            PerkLogger.w(TAG,
                                    "startPlayer(): findVideoByID-VideoListener: "
                                            + "brightcoveVideoView is null!");
                            return;
                        }

                        if (exoPlayer == null || exoPlayerView == null) {
                            PerkLogger.w(TAG,
                                    "startPlayer(): findVideoByID-VideoListener: "
                                            + "exoPlayer or exoPlayerView is null!");
                            return;
                        }


                        brightcoveVideoView.clear();
                        // Sometimes source url can be null then try playing it on brightcove player.
                        if (video.findHighQualitySource(DeliveryType.HLS) == null) {
                            mIsCurrentVideoLongform = false;
                        }

                        if (mIsCurrentVideoLongform){ //use exoplayer
                            exoPlayerView.setVisibility(View.VISIBLE);

                            Log.d("id3tag",video.findHighQualitySource(DeliveryType.HLS).getUrl());

                            // Produces DataSource instances through which media data is loaded.
                            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(),
                                    Util.getUserAgent(getApplicationContext(), "yourApplicationName"));

                            // This is the MediaSource representing the media to be played.
                            //MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse("https://video1.perk.com/files/bc/CMC-C_181018_3814694_Kemper_on_Kemper__Inside_The_Mind_of_a_Seria_HLS/CMC-C_181018_3814694_Kemper_on_Kemper__Inside_The_Mind_of_a_Seria_HLS.m3u8"));
                            String strUrl  = video.findHighQualitySource(DeliveryType.HLS).getUrl();
                           // Uri uri = Uri.parse("https://video1.perk.com/files/bc/CMC-C_181018_3814694_Kemper_on_Kemper__Inside_The_Mind_of_a_Seria_HLS/CMC-C_181018_3814694_Kemper_on_Kemper__Inside_The_Mind_of_a_Seria_HLS.m3u8");
                            Uri uri = Uri.parse(strUrl);
                            MediaSource videoSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
                            //MediaSource videoSource = new HlsMediaSource.Factory(dataSourceFactory).setPlaylistParserFactory(new DefaultHlsPlaylistParserFactory(getOfflineStreamKeys(uri))).createMediaSource(uri);
                            // Prepare the player with the source.
                            exoPlayer.setPlayWhenReady(false);
                            exoPlayer.prepare(videoSource);

                        }
                        else{
                            exoPlayerView.setVisibility(View.GONE);
                            setBrightcoveVideoContentVisibility(true);
                            brightcoveVideoView.add(video);
                        }

                        // Confirm if required to seek to last viewed position, if resuming
                        // last longform video:

                        if (mIsCurrentVideoLongform
                                && PerkPreferencesManager.INSTANCE.getLastPositionForLongformVideo(
                                        strVideoID) != -1) {

                            mActivityVideoPlayerBinding.idProgressBar.setVisibility(View.GONE);
                            toggleResumePlayingPrompt(true);
                        }

                        // Scroll to the item that will be played:
                        if (mVideosRecyclerView != null) {
                            mVideosRecyclerView.postDelayed(() -> {
                                if (mVideosRecyclerView != null) {
                                    try {
                                        mVideosRecyclerView.smoothScrollToPosition(
                                                nCurrentIndex);
                                    } catch (Exception ignored) {
                                    }
                                }
                            }, 300);
                        }

                        if (!wasPreviousVideoLongForm && mVideoPlayCounter < mAutoPlaySetting) {

                            if (!mIsCurrentVideoLongform ||
                                    PerkPreferencesManager.INSTANCE.getLastPositionForLongformVideo(
                                            strVideoID) == -1) {
                                if (mIsCurrentVideoLongform){
                                    playExoPlayer();
                                }
                                else{
                                    brightcoveVideoView.start();
                                }
                            }

                        } else {
                            if (wasPreviousVideoLongForm) {
                                PerkLogger.d(TAG,
                                        "Pausing auto-play as previous video was longform");
                            } else {
                                PerkLogger.d(TAG, "Auto-play video count(" + mVideoPlayCounter
                                        + ") exceeded auto-play setting-count(" + mAutoPlaySetting
                                        + ")");
                            }
                            brightcoveVideoView.seekTo(0);
                            if (exoPlayer != null)exoPlayer.seekToDefaultPosition();
                            // Clear current playing video index so that it's updated on UI
//                            mPlayerVideoAdapter.updateIndexes(-1, nCurrentIndex);

                            // hide loading indicator
                            mActivityVideoPlayerBinding.idProgressBar.setVisibility(View.GONE);

                            // Set the orientation to portrait if it wasn't already:
                            if (brightcoveVideoView.isFullScreen()) {
                                eventEmitter.emit(EventType.EXIT_FULL_SCREEN);
                            }

                            // Show the player controls when not playing video:
                            if( !mIsCurrentVideoLongform) eventEmitter.emit(ShowHideController.SHOW_MEDIA_CONTROLS);

                            // show the play button:
                            togglePlayButton(true);
                        }

                    }
                });

                String strName = arrVideos.get(nCurrentIndex).getName();
                video_title.setText(strName);

                try {
                    strPerkReward = "";

                    BrightcovePlaylistData.CustomFields customFields = arrVideos.get(
                            nCurrentIndex).getCustomFields();
                    if (customFields != null) {
                        String perkReward = customFields.getPerkReward();
                        if (perkReward != null) {
                            strPerkReward = perkReward;
                        }

                        // Set short-description as sub-title for video if this is a longform video:
                        if (mIsCurrentVideoLongform) {

                            String shortDescription = arrVideos.get(nCurrentIndex).getDescription();

                            if (StringUtils.equalsIgnoreCase("unknown", shortDescription)) {
                                shortDescription = null;
                            }

                            if (!StringUtils.isEmpty(shortDescription)) {
                                updateVideoSubtitle(arrVideos.get(nCurrentIndex).getDescription());
                            } else {
                                String title = getTitleForVideo(customFields);
                                PerkLogger.d(TAG,
                                        "startPlayer(): short-description unavailable for "
                                                + "longform video... using provider instead. Title: "
                                                + title);
                                updateVideoSubtitle(title);
                            }
                        } else {

                            // null - will reset to default i.e. Playlist name
                            String title = getTitleForVideo(customFields);
                            PerkLogger.d(TAG,
                                    "startPlayer(): mIsForChannelsList=" + mIsForChannelsList
                                            + "- Title set to: " + title);
                            updateVideoSubtitle(title);
                        }

                        // Enable the provider image if available:
                        if (!TextUtils.isEmpty(arrVideos.get(nCurrentIndex).getImageUrl())) {
                            mActivityVideoPlayerBinding.channelImage.setVisibility(View.VISIBLE);
                            Bindings.setImageUrl(mActivityVideoPlayerBinding.channelImage, arrVideos.get(
                                    nCurrentIndex).getImageUrl());
                        } else {
                            clearAndHideProviderImg();
                        }
                    }

                } catch (Exception e) {
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            finish();

        }

    }

    private String getTitleForVideo(BrightcovePlaylistData.CustomFields customFields) {
        // null - will reset to default i.e. Playlist name
        String title = null;
        if (!mIsForChannelsList && !TextUtils.isEmpty(customFields.getProvider())) {
            if (!TextUtils.isEmpty(mPlaylistName)) {
                title = customFields.getProvider() + " - " + mPlaylistName;
            } else {
                title = customFields.getProvider();
            }
        }
        return title;
    }

    private void clearAndHideProviderImg() {
        if (mActivityVideoPlayerBinding == null) {
            return;
        }

        // clear the ImageView & hide it:
        mActivityVideoPlayerBinding.channelImage.setImageDrawable(null);
        mActivityVideoPlayerBinding.channelImage.setVisibility(View.GONE);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        PerkLogger.d(TAG, "onConfigurationChanged(): orientation: " + newConfig.orientation
                + " mActivityVideoPlayerBinding: " + mActivityVideoPlayerBinding
                + " brightcoveVideoView: " + brightcoveVideoView);

        if (isFinishing() || isDestroyed()) {
            PerkLogger.d(TAG,
                    "Ignoring configuration-changed as activity is finishing or destroyed!");
            return;
        }

        if (brightcoveVideoView == null) {
            if (mActivityVideoPlayerBinding != null) {
                brightcoveVideoView = mActivityVideoPlayerBinding.brightcoveVideoView;
            } else {
                brightcoveVideoView = findViewById(R.id.brightcove_video_view);
            }
            PerkLogger.d(TAG,
                    "onConfigurationChanged(): brightcoveVideoView set to: " + brightcoveVideoView);
        }

        // if we were still unable to get the brightcove-video-view, then return
        if (brightcoveVideoView == null) {
            PerkLogger.e(TAG, "onConfigurationChanged(): brightcoveVideoView is null! Returning!");
            return;
        }

        // Bring Video-view to front:
        brightcoveVideoView.bringToFront();

        // Bring play-icon, close-button, scrim-view, AYSW-view, resume-playing view & the progressbar to front
        if (mActivityVideoPlayerBinding != null) {
            mActivityVideoPlayerBinding.playContainer.bringToFront();


            mActivityVideoPlayerBinding.scrimView.bringToFront();
            mActivityVideoPlayerBinding.resumePlayingContainer.bringToFront();
            mActivityVideoPlayerBinding.ayswContainer.bringToFront();
            mActivityVideoPlayerBinding.idProgressBar.bringToFront();

            mActivityVideoPlayerBinding.btnCloseBg.bringToFront();
            mActivityVideoPlayerBinding.btnClose.bringToFront();
        } else {
            PerkLogger.w(TAG, "onConfigurationChanged(): mActivityVideoPlayerBinding is null!");
            findViewById(R.id.play_container).bringToFront();

            findViewById(R.id.scrimView).bringToFront();
            findViewById(R.id.resume_playing_container).bringToFront();
            findViewById(R.id.aysw_container).bringToFront();
            findViewById(R.id.id_progress_bar).bringToFront();

            findViewById(R.id.btnClose_bg).bringToFront();
            findViewById(R.id.btnClose).bringToFront();
        }

        brightcoveVideoView.removeCallbacks(mOrientationRunnable);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        HashMap<String, Object> contextData = new HashMap<String, Object>();
        try{
            /////

            contextData.put("tve.title","Video Player");
            contextData.put("tve.userpath","Video Player");
            contextData.put("tve.contenthub","Video Player");
            if (mVideoData != null) {
                contextData = AdobeTracker.appendVideoData(contextData,mVideoData);
            }

            /////
        }catch (Exception e){}

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            brightcoveVideoView.setLayoutParams(param);

            brightcoveVideoView.post(() -> eventEmitter.emit(EventType.ENTER_FULL_SCREEN));

            contextData.put("tve.contentmode","Landscape");

            updateSeekBarOnConfigChange();

          //  exoPlayerView.setLayoutParams(param);

            openFullscreenDialog(true);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            eventEmitter.emit(EventType.EXIT_FULL_SCREEN);

            brightcoveVideoView.postDelayed(() -> {
                if (brightcoveVideoView == null) {
                    PerkLogger.e(TAG, "onConfigurationChanged: brightcove-VideoView is null!");
                    return;
                }
                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, mDesiredVideoPlayerHeight);
                param.topMargin = (int) getResources().getDimension(
                        R.dimen.video_player_toolbar_height);
                brightcoveVideoView.setLayoutParams(param);
                updateSeekBarOnConfigChange();

             //   exoPlayerView.setLayoutParams(param);
            }, 100);

            contextData.put("tve.contentmode","Portrait");
            closeFullscreenDialog(true);
        }

        try{
            AdobeTracker.INSTANCE.trackState("Video Player",contextData);
        }catch (Exception e){}
    }

    private void updateSeekBarOnConfigChange() {
        // Delay the call so that BrightCoveVideoView gets a chance to finish the layout on the
        // screen & then update the UI:
        if (brightcoveVideoView != null) {
            brightcoveVideoView.postDelayed(() -> {
                updateSeekBarState(false);
                updateControlsVisibilityOnAdPlay();
                postSensorOrientation();

                // TODO: Enable this for delaying the AYSW prompt -if orientation change should delay AYSW
                /*mHandler.removeCallbacks(mAyswPromptRunnable);
                if (brightcoveVideoView != null && brightcoveVideoView.isPlaying()
                        && mIsCurrentVideoLongform && !mHasShownAyswPrompt) {
                    mHandler.postDelayed(mAyswPromptRunnable, mAyswPromptDelay);
                } else {
                    PerkLogger.d(TAG, "OnConfigChange: Not posting AYSW prompt");
                }*/
            }, 300);
        }
    }

    private void toggleAYSWPrompt(boolean show) {
        if (mActivityVideoPlayerBinding == null) {
            PerkLogger.e(TAG, "mActivityVideoPlayerBinding is null!");
            return;
        }

        PerkLogger.d(TAG,"toggleAYSWPrompt: show = " + show);
        mActivityVideoPlayerBinding.scrimView.setVisibility(show ? View.VISIBLE : View.GONE);
        mActivityVideoPlayerBinding.ayswContainer.setVisibility(show ? View.VISIBLE : View.GONE);

        mHandler.removeCallbacks(mAYSWPromptNoResponseRunnable);
        if (show) {
            mHandler.postDelayed(mAYSWPromptNoResponseRunnable,mAYSWPromptNoResponseMillis);
        }
    }

    private void toggleResumePlayingPrompt(boolean show) {
        if (mActivityVideoPlayerBinding == null) {
            PerkLogger.e(TAG, "toggleResumePlayingPrompt: mActivityVideoPlayerBinding is null!");
            return;
        }

        PerkLogger.d(TAG,"toggleResumePlayingPrompt: show = " + show);

        // hide 'play' icon when showing resume-prompt:
        if (show) {
            togglePlayButton(false);
            mActivityVideoPlayerBinding.scrimView.setVisibility(View.VISIBLE);
            mActivityVideoPlayerBinding.resumePlayingContainer.setVisibility(View.VISIBLE);
        } else {
            mActivityVideoPlayerBinding.scrimView.setVisibility(View.GONE);
            mActivityVideoPlayerBinding.resumePlayingContainer.setVisibility(View.GONE);
        }
    }

    private void togglePlayButton(boolean show) {
        if (mActivityVideoPlayerBinding == null) {
            PerkLogger.e(TAG, "togglePlayButton is null!");
            return;
        }

        PerkLogger.d(TAG,"togglePlayButton: show = " + show + " mAdPlaying: " + mAdPlaying);
        runOnUiThread(() -> mActivityVideoPlayerBinding.playContainer.setVisibility(
                show && !mAdPlaying ? View.VISIBLE : View.GONE));

    }

    @Override
    public void handleVideoItemClick(@NonNull BrightcovePlaylistData.BrightcoveVideo videoData) {
        if (BuildConfig.DEBUG) {
            new Exception("handleVideoItemClick(): clicked video: " + videoData).printStackTrace();
        }

        if (!AppUtility.isNetworkAvailable(getApplicationContext())) {
            PerkLogger.d(TAG, "handleVideoItemClick(): Returning as network unavailable!");
            return;
        }

        // check if user is clicking same video as the one currently being played:
        int clickIndex = arrVideos.indexOf(videoData);
        if (clickIndex == nCurrentIndex) {
            PerkLogger.w(TAG,
                    "handleVideoItemClick: Ignoring as the clicked video is already being played!");
            return;
        }

        // Event tracking handling for Leanplum (for longform videos):
        boolean wasLongForm = trackEventsOnVideoDone(mLastVideoCompleted);

        // Make '/views' API-call for long-form videos, on changing video:
        if (wasLongForm) {
            postVideoView();
        }

        if (brightcoveVideoView != null) {
            brightcoveVideoView.stopPlayback();
            brightcoveVideoView.seekTo(0);
            brightcoveVideoView.clear();
            setBrightcoveVideoContentVisibility(false);
        }
        stopExoPlayer();
        // Reset count to 0 if user manually opens a video
        mVideoPlayCounter = 0;
        // Reset state of variable tracking if user manually paused a video:
        mUserPausedVideo = false;
        // Reset variable specifying if want to resume video on regaining network connectivity:
        mResumeVideoOnConnectivity = false;
        nCurrentIndex = clickIndex;
        // Set current playing video index in that the adapter so that it's indicated in UI
//        mPlayerVideoAdapter.updateIndexes(nCurrentIndex, -1);
        startPlayer(wasLongForm);

        try{
            /////

            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Video Player");
            contextData.put("tve.userpath","Click:Video");
            contextData.put("tve.contenthub","Video Player");

            String strModule =  "Player:1:"+(nCurrentIndex+1);
            contextData.put("tve.module",strModule);
            contextData.put("tve.action","true");

            contextData = AdobeTracker.appendVideoData(contextData,videoData);

            AdobeTracker.INSTANCE.trackAction("Click:Video",contextData);

            /////
        }catch (Exception e){}
    }

    private void setBrightcoveVideoContentVisibility(boolean show) {
        if (brightcoveVideoView == null) {
            return;
        }

        try {
            brightcoveVideoView.getVideoDisplay().getRenderView().setVisibility(show ? View.VISIBLE : View.GONE);
        } catch (Exception ex) {
            PerkLogger.e(TAG, "Exception trying to hide RenderView: ", ex);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Save cached video play-head positions:
        try {
            PerkPreferencesManager.INSTANCE.persistPlayheadPositionsCache();
        } catch (Exception ex) {
            PerkLogger.e(TAG, "Exception with persistPlayheadPositionsCache:", ex);
        }
    }

    @Override
    public void finish() {
        PerkLogger.d(TAG,
                "finish called! tracking events: mLastVideoCompleted=" + mLastVideoCompleted);
        boolean wasLongForm = trackEventsOnVideoDone(mLastVideoCompleted);
        mHandler.removeCallbacks(mAyswPromptRunnable);
        mHandler.removeCallbacks(mAYSWPromptNoResponseRunnable);
        brightcoveVideoView = null;

        // Make '/views' API-call for long-form videos, on closing video-player:
        if (wasLongForm) {
            postVideoView();
        }

        if (plugin != null){
            plugin.destroy();
        }
        if (exoPlayer != null){
            exoPlayer.stop(true);
            releaseExoPlayer();
        }
        super.finish();
    }


    private PluginEventListener mPluginEventListerner = new PluginEventListener() {
        @Override
        public void onAppSdkEvent(long timestamp, int code, String description) {

        }

        @Override
        public void onPluginEvent(PluginEvent pluginEvent) {
//            switch (pluginEvent){
//                case PLAYER_ERROR:
//
//            }
        }

        @Override
        public void OnBCEvent(Event event) {
            PerkLogger.d(TAG, "OnBCEvent(): on event: " + event.getType());

            switch (event.getType()) {
                case EventType.AD_STARTED:
                    mAdPlaying = true;
                    updateControlsVisibilityOnAdPlay();
                case EventType.AD_BREAK_STARTED:
                case EventType.AD_ERROR:
                    if (TextUtils.equals(EventType.AD_ERROR, event.getType())) {
                        PerkLogger.e(TAG, "OnBCEvent(): on-AD-ERROR event: " + (new ToStringBuilder(
                                null).append("properties", event.getProperties()).toString()));
                    }
                    mLastPreRollAdFilled = false;
                    // hide loading indicator
                    mActivityVideoPlayerBinding.idProgressBar.setVisibility(View.GONE);
                    break;

                case EventType.AD_COMPLETED:
                    mAdPlaying = false;
                    updateControlsVisibilityOnAdPlay();
                    break;

                case EventType.ERROR:
                    PerkLogger.e(TAG, "OnBCEvent(): on-ERROR event: " + (new ToStringBuilder(
                            null).append("properties", event.getProperties()).toString()));
                    mLastVideoCompleted = false;
                    onNextVideo(false);
                    break;

                case EventType.COMPLETED:
                    mLastVideoCompleted = true;

                    // clear the last playHead-position on completing longform video:
                    if (mIsCurrentVideoLongform) {
                        PerkLogger.d(TAG,
                                "processEvent(): COMPLETED: Clearing saved playhead position");

                        PerkPreferencesManager.INSTANCE.saveLongformVideoPosition(strVideoID, -1);
                    }
                    onNextVideo(true);
                    break;

                case EventType.PLAY:
                    PerkLogger.d(TAG,
                        "PLAY event: " + event.getType() + " mVideoPlayCounter: "
                                + mVideoPlayCounter + " mAutoPlaySetting:" + mAutoPlaySetting
                                + " mUserPausedVideo: " + mUserPausedVideo);

                    togglePlayButton(false);

                    // save current video id -if longform:
                    /*if (mIsCurrentVideoLongform) {
                        PerkPreferencesManager.INSTANCE.saveLastLongformVideoId(strVideoID);
                    }*/
                
                    // Reset counter when user manually clicks play on video, ignore if playing a paused video
                    if (!mUserPausedVideo && mVideoPlayCounter == mAutoPlaySetting) {
                        PerkLogger.d(TAG, "mPlayEventListener: Setting mVideoPlayCounter to 0");
                        mVideoPlayCounter = 0;
                    }

                    if (mCurrentVideoDuration == -1L && !mHasTrackedInitialEvents) {
                        // SET_SOURCE sets mCurrentVideoDuration to -1 on loading new video. So
                        // this means that PLAY event started for new video:

                        PerkLogger.d(TAG,
                                "PLAY event obtained for new video with mIsCurrentVideoLongform "
                                        + "set to "
                                        + mIsCurrentVideoLongform);

                        mHasTrackedInitialEvents = true;

                        // Make the views API call for video-start event:
                        // https://jira.rhythmone.com/browse/PEWAN-485
                        postVideoView(true);



                        // Video-start event tracking:
                        if (mIsCurrentVideoLongform) {
                            // Singular: start_longform_video - user starts a longform video
                            Singular.event("start_longform_video");

                            // AppsFlyer: video_start - user starts a video
                            AdobeTracker.INSTANCE.trackAppsFlyerEvent(getApplicationContext(),"start_longform_video",null);


                            // Facebook Analytics: Longform Start - user starts a longform video
                            FacebookEventLogger.getInstance().logEvent("Longform Start");
                            FacebookEventLogger.getInstance().logEvent("Video Start");

                            // Leanplum:
                            Map<String, Object> params = new HashMap<String, Object>();
                            params.put(TrackingEvents.LEANPLUM_PARAMETER_VIDEO_ID, strVideoID);
                            Leanplum.track(TrackingEvents.LEANPLUM_LONGFORM_START, params);
                        } else {
                            // Singular: start_shortform_video - user starts a shortform video
                            Singular.event("start_shortform_video");

                            // AppsFlyer: video_start - user starts a video
                            AdobeTracker.INSTANCE.trackAppsFlyerEvent(getApplicationContext(),"start_shorform_video",null);

                            // Facebook Analytics: Shortform Start - user starts a shortform video
                            FacebookEventLogger.getInstance().logEvent("Shortform Start");
                            FacebookEventLogger.getInstance().logEvent("Video Start");

                            // Leanplum:
                            Map<String, Object> params = new HashMap<String, Object>();
                            params.put(TrackingEvents.LEANPLUM_PARAMETER_VIDEO_ID, strVideoID);
                            Leanplum.track(TrackingEvents.LEANPLUM_SHORTFORM_START, params);
                        }
                    }

                    // Set current playing video index in that the adapter so that it's indicated in UI
//                    mPlayerVideoAdapter.updateIndexes(nCurrentIndex, -1);

                    // Moved to DID_PLAY below as there's a gap between this event & the actual
                    // video-play action:
                    // hide loading indicator
                    //mActivityVideoPlayerBinding.idProgressBar.setVisibility(View.GONE);

                    if (mAyswPromptDelay == -1L
                            || mAyswPromptDelay == WatchBackSettingsController.THREE_HOURS_MILLIS) {
                        // Probably the setting wasn't available the last time we tried to get
                        // it... so get it again now:
                        mAyswPromptDelay =
                                WatchBackSettingsController.INSTANCE
                                        .getAYSWPromptMillisSetting(
                                                getApplicationContext());
                        PerkLogger.d(TAG,
                                "PLAY event: mAyswPromptDelay set to : " + mAyswPromptDelay);
                    }

                    if (mAYSWPromptNoResponseMillis == -1L
                            || mAYSWPromptNoResponseMillis == WatchBackSettingsController.THREE_HOURS_MILLIS) {
                        // Probably the setting wasn't available the last time we tried to get
                        // it... so get it again now:
                        mAYSWPromptNoResponseMillis =
                                WatchBackSettingsController.INSTANCE
                                        .getAYSWPromptNoResponseMillisSetting(
                                        getApplicationContext());

                        PerkLogger.d(TAG, "PLAY event - mAYSWPromptNoResponseMillis set to : "
                                + mAYSWPromptNoResponseMillis);
                    }
                    break;

                case EventType.PAUSE:
                    PerkLogger.d(TAG,
                        "PAUSE event: " + event.getType() + " mVideoPlayCounter: "
                                + mVideoPlayCounter + " mAutoPlaySetting:" + mAutoPlaySetting
                                + " mUserPausedVideo: " + mUserPausedVideo
                                + " mHasTrackedInitialEvents: " + mHasTrackedInitialEvents
                                + " mHasShownAyswPrompt: " + mHasShownAyswPrompt);

                    // Do this only if a video is playing, since we get pause event on completing
                    // long-form video (-as a result of using ExoPlayer & tying it up with
                    // Brightcove events manually)
                    if (mHasTrackedInitialEvents) {
                        mUserPausedVideo = true;
                    } else {
                        PerkLogger.d(TAG,
                                "Ignoring PAUSE event obtained without a video being played");
                    }

                    mHandler.removeCallbacks(mAyswPromptRunnable);
                    break;

                case EventType.DID_PLAY:
                    PerkLogger.d(TAG,
                        "DID_PLAY event: " + event.getType() + " mVideoPlayCounter: "
                                + mVideoPlayCounter + " mAutoPlaySetting:" + mAutoPlaySetting
                                + " mUserPausedVideo: " + mUserPausedVideo
                                + " mCurrentVideoDuration: " + mCurrentVideoDuration
                                + " mIsCurrentVideoLongform: " + mIsCurrentVideoLongform
                                + " mHasShownAyswPrompt: " + mHasShownAyswPrompt);

                    togglePlayButton(false);

                    // hide loading indicator
                    mActivityVideoPlayerBinding.idProgressBar.setVisibility(View.GONE);

                    updateCurrentVideoDuration(event);

                    // If we are successful in playing video after being paused due to no
                    // connectivity, then update the state here accordingly:
                    if (mResumeVideoOnConnectivity) {
                        mResumeVideoOnConnectivity = false;
                    }

                    mHandler.removeCallbacks(mAyswPromptRunnable);
                    if (!mHasShownAyswPrompt) {
                        mHandler.postDelayed(mAyswPromptRunnable, mAyswPromptDelay);
                    } else {
                        PerkLogger.d(TAG,
                                "DID_PLAY event: Not posting AYSW prompt as the prompt has already been shown");
                    }

                    if (mUserPausedVideo) {
                        // If we are playing after last manual-pause by user, then ignore:
                        mUserPausedVideo = false;
                        PerkLogger.d(TAG, "mPlayEventListener: Ignoring event as paused video was resumed");
                        return;
                    }
                    mVideoPlayCounter++;
                
                    // disable seek-bar if needed:
                    updateSeekBarState(false);

                    break;

                case EventType.AD_PROGRESS:
                    if (!mLastPreRollAdFilled) {
                        PerkLogger.d(TAG,
                                "processEvent(): Ad filled... setting mLastPreRollAdFilled to true");
                        mLastPreRollAdFilled = true;
                    }
                    break;

                case EventType.PROGRESS:
                    // hide the big 'play' button if it was visible. If already hidden, then this
                    // is a no-op:
                    togglePlayButton(false);

                    // Additions for Exoplayer -that was added for long-form videos:
                    // We keep getting the PROGRESS event from Plugin.java, so we ignore this
                    // event if we haven't got PLAY / DID_PLAY event yet:
                    if (!mHasTrackedInitialEvents) {
                        PerkLogger.d(TAG,
                                "processEvent(): PROGRESS: Ignoring as events PLAY / DID_PLAY "
                                        + "haven't been obtained yet!");
                        return;
                    }

                    updateCurrentVideoDuration(event);

                    // Check if user has reached the percent completion required for sweepstakes:
                    long currentPlayheadPosition = 0;
                    if (mIsCurrentVideoLongform){
                        //long exoPlayerCurrentPosition = exoPlayer == null ? 0 : exoPlayer.getCurrentPosition()/1000;
                        long exoPlayerCurrentPosition = exoPlayer == null ? 0 : exoPlayer.getCurrentPosition();
                        if (exoPlayerCurrentPosition < 0)
                            exoPlayerCurrentPosition = 0;
                        currentPlayheadPosition = exoPlayerCurrentPosition;
                    }
                    else{
                        currentPlayheadPosition = AppUtility.getLongEventProperty(event,
                                "playheadPosition");
                    }

                    mCompletedMillis = currentPlayheadPosition;

                    // no need to proceed if current video is not longform:
                    if (!mIsCurrentVideoLongform) {
                        PerkLogger.d(TAG,
                                "processEvent(): PROGRESS: Ignoring as current video is not "
                                        + "longform!");
                        return;
                    }

                    if (mLastPlayheadPosition == 0L) {
                        mLastPlayheadPosition = currentPlayheadPosition;
                    }

                    // store the last playHead-position every second, for long-form videos:
                    long diff = currentPlayheadPosition - mLastPlayheadPosition;

                    // we allow a +/-150ms of error to be included since the millis-duration may
                    // not be exact multiples of 1000
                    if (diff >= 850 && diff <= 1150) {
                        PerkLogger.d(TAG,
                                "processEvent(): PROGRESS: 1 second elapsed at position: "
                                        + currentPlayheadPosition + " -Saving values");

                        mLastPlayheadPosition = currentPlayheadPosition;

                        PerkPreferencesManager.INSTANCE.saveLongformVideoPosition(strVideoID,
                                currentPlayheadPosition);
                    } else if (diff > 1150) {
                        // if we somehow went over the +/- 150ms window, then reset to latest
                        // duration so that we can save further, otherwise the
                        // mLastPlayheadPosition would never be updated & no more saving would be
                        // done
                        mLastPlayheadPosition = currentPlayheadPosition;
                    }

                    break;

                case EventType.SEEK_TO:
                    mHandler.removeCallbacks(mAyswPromptRunnable);
                    if (!mHasShownAyswPrompt) {
                        mHandler.postDelayed(mAyswPromptRunnable, mAyswPromptDelay);
                    } else {
                        PerkLogger.d(TAG, "SEEK_TO event: Not posting AYSW prompt");
                    }

                    // In case user drags the seekbar completely to the end, then the below
                    // DID_SEEK_TO event isn't obtained & the video gets 'COMPLETED' directly...
                    // so we instead use the seek time from this event & compare it to the video
                    // duration to decide if the user did FFWD the video:

                    // Sample response with the properties sent with this event:
                    // seekTo [properties={seekPosition=3624000, video=Video{name: "ABC
                    // ", sourceCollections: 1, cuePoints: 0, custom fields:
                    // "{}"}}]

                    // Ignore if we do not have the current video's duration for any reason:
                    if (mCurrentVideoDuration == -1L) {
                        PerkLogger.e(TAG, "SEEK_TO: mCurrentVideoDuration is unavailable!");
                        return;
                    }

                    // Ignore if the seek event is due to being automatically forwarded to last
                    // viewed position (for longform video):
                    if (mIsCurrentVideoLongform && mIsAutoSeeked) {
                        PerkLogger.d(TAG,
                                "SEEK_TO: Ignoring as video is automatically forwarded to last "
                                        + "play-position!");
                        return;
                    }

                    long seekPos;
                    if (mIsCurrentVideoLongform) {
                        seekPos = exoPlayer == null ? 0 : exoPlayer.getCurrentPosition();
                        if (seekPos < 0) {
                            seekPos = 0;
                        }
                    } else {
                        seekPos = AppUtility.getLongEventProperty(event, "seekPosition");
                    }

                    if (seekPos >= mCurrentVideoDuration) {
                        // Singular: fast_forward - Whenever a user fast-forwards
                        Singular.event("fast_forward");

                        // if we know that user already did FFWD the video before, then no need to
                        // proceed ahead:
                        if (mHasFastForwardedVideo) {
                            PerkLogger.e(TAG,
                                    "SEEK_TO: mHasFastForwardedVideo is already true! Ignoring "
                                            + "event");
                            return;
                        }

                        mHasFastForwardedVideo = true;
                    }

                    PerkLogger.d(TAG,
                            "SEEK_TO: seekPosition: " + seekPos + " mCurrentVideoDuration: "
                                    + mCurrentVideoDuration + " mHasFastForwardedVideo: "
                                    + mHasFastForwardedVideo);

                    break;

                case EventType.DID_SEEK_TO:
                    // there can be a delay between SEEK_TO event & the actual play due to
                    // buffering... so we post-delay the AYSW prompt here again when the video
                    // actually starts playing
                    mHandler.removeCallbacks(mAyswPromptRunnable);
                    if (!mHasShownAyswPrompt) {
                        mHandler.postDelayed(mAyswPromptRunnable, mAyswPromptDelay);
                    } else {
                        PerkLogger.d(TAG, "DID_SEEK_TO event: Not posting AYSW prompt");
                    }

                    // Ignore if the seek event is due to being automatically forwarded to last
                    // viewed position (for longform video):
                    if (mIsCurrentVideoLongform && mIsAutoSeeked) {
                        PerkLogger.d(TAG,
                                "DID_SEEK_TO: Ignoring as video is automatically forwarded to "
                                        + "last play-position!");

                        // set to false here, so that any further events are processed:
                        mIsAutoSeeked = false;
                        return;
                    }

                    if (mFwdMillisSetting == -1L) {
                        PerkLogger.e(TAG, "DID_SEEK_TO: mFwdMillisSetting is not set! Ignoring...");
                        return;
                    }

                    // Sample response with the properties sent with this event:
                    // didSeekTo [properties={playheadPosition=1920554,
                    // fromSeekPosition=27787, seekPosition=1920553, source=Source{deliveryType:
                    // video/mp4, url: http://abc.zxc.mp4}, video=Video{name: "ABC
                    // ", sourceCollections: 1, cuePoints: 0, custom fields:
                    // "{}"}}]

                    long seekPosition, fromSeekPosition;

                    if (mIsCurrentVideoLongform) {
                        seekPosition = exoPlayer == null ? 0 : exoPlayer.getCurrentPosition();
                        if (seekPosition < 0) {
                            seekPosition = 0;
                        }

                        fromSeekPosition = mLastPlayheadPosition;

                    } else {
                        seekPosition = AppUtility.getLongEventProperty(event, "seekPosition");
                        fromSeekPosition = AppUtility.getLongEventProperty(event,
                                "fromSeekPosition");
                    }

                    if ((seekPosition - fromSeekPosition) >= mFwdMillisSetting) {
                        // Singular: fast_forward - Whenever a user fast-forwards
                        Singular.event("fast_forward");

                        // if we know that user already did FFWD the video before, then no need to
                        // proceed ahead:
                        if (mHasFastForwardedVideo) {
                            PerkLogger.e(TAG,
                                    "DID_SEEK_TO: mHasFastForwardedVideo is already true! "
                                            + "Ignoring event");
                            return;
                        }

                        mHasFastForwardedVideo = true;
                    }

                    PerkLogger.d(TAG,
                            "DID_SEEK_TO: fromSeekPosition: " + fromSeekPosition + " seekPosition: "
                                    + seekPosition + " difference: " + (seekPosition
                                    - fromSeekPosition) + " mFwdMillisSetting: "
                                    + mFwdMillisSetting + " mHasFastForwardedVideo: "
                                    + mHasFastForwardedVideo);

                    break;
            }

        }
    };

    private void updateControlsVisibilityOnAdPlay() {
        PerkLogger.d(TAG, "updateControlsVisibilityOnAdPlay: mAdPlaying = " + mAdPlaying);

        if (brightcoveVideoView == null) {
            PerkLogger.e(TAG,
                    "updateControlsVisibilityOnAdPlay: Returning as brightcoveVideoView is null!");
            return;
        }

        brightcoveVideoView.findViewById(R.id.controls_container).setVisibility(
                mAdPlaying ? View.GONE : View.VISIBLE);
    }

    private void updateCurrentVideoDuration(Event event) {
        // Update the duration for currently-playing video in case it isn't already
        // done before:
        if (mCurrentVideoDuration == -1L) {
            if (mIsCurrentVideoLongform){

                long exoPlayerTotalDuration = exoPlayer == null ? 0 : exoPlayer.getDuration();
                if (exoPlayerTotalDuration > 0)
                    mCurrentVideoDuration = exoPlayerTotalDuration;
                else return;

            }
            else{
                mCurrentVideoDuration = AppUtility.getLongEventProperty(event, "duration");
            }

            PerkLogger.d(TAG, "updateCurrentVideoDuration: Updated mCurrentVideoDuration to "
                    + mCurrentVideoDuration);

        }

        // Probably the setting(s) were unavailable the last time we tried to get
        // it... so get it again now:

        // Fast-forward:
        if (mFwdPercecntSetting == -1f || mFwdPercecntSetting == 100) {
            mFwdPercecntSetting = WatchBackSettingsController.INSTANCE.getFastforwardPercentSetting(
                    getApplicationContext());
            PerkLogger.d(TAG, "updateCurrentVideoDuration: mFwdPercecntSetting set to : "
                    + mFwdPercecntSetting);

            updateForwardMillis();
        }

        if (mFwdMillisSetting == -1L) {
            updateForwardMillis();
            PerkLogger.d(TAG,
                    "updateCurrentVideoDuration: mFwdMillisSetting set to : " + mFwdMillisSetting);
        }

        // Longform completion percent:
        if (mCompletePercentSetting == -1f) {
            mCompletePercentSetting =
                    WatchBackSettingsController.INSTANCE.getLongformCompletionPercentSetting(
                            getApplicationContext());
            PerkLogger.d(TAG, "updateCurrentVideoDuration: mCompletePercentSetting set to : "
                    + mCompletePercentSetting);
        }
    }

    private void updateForwardMillis() {
        mFwdMillisSetting = (long) (mCurrentVideoDuration * (mFwdPercecntSetting / 100));
        PerkLogger.d(TAG, "updateForwardMillis: mFwdMillisSetting set to : " + mFwdMillisSetting);
    }

    @Nullable
    private BrightcovePlaylistData.BrightcoveVideo getCurrentVideo() {
        if (arrVideos == null || nCurrentIndex < 0 || nCurrentIndex >= arrVideos.size()) {
            PerkLogger.e(TAG,
                    "getCurrentVideo(): arrVideos is null OR nCurrentIndex is invalid! "
                            + "nCurrentIndex: "
                            + nCurrentIndex);
            return null;
        }

        return arrVideos.get(nCurrentIndex);
    }

    private BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean connected = AppUtility.isNetworkAvailable(context);
            PerkLogger.e(TAG, "ConnectivityChangeReceiver: onReceive(): Connected= " + connected
                    + " mResumeVideoOnConnectivity: " + mResumeVideoOnConnectivity);

            if (VideoPlayerActivity.this.isFinishing() || VideoPlayerActivity.this.isDestroyed()) {
                PerkLogger.w(TAG,
                        "Ignoring connectivity change when VideoPlayer is finishing/destroyed!");
                return;
            }

            boolean ayswContainerShown =
                    (mActivityVideoPlayerBinding != null ? mActivityVideoPlayerBinding.ayswContainer
                            : findViewById(R.id.aysw_container)).getVisibility() == View.VISIBLE;

            boolean resumePlayingContainerShown = (mActivityVideoPlayerBinding != null
                    ? mActivityVideoPlayerBinding.resumePlayingContainer : findViewById(
                    R.id.resume_playing_container)).getVisibility() == View.VISIBLE;

            PerkLogger.d(TAG, "ConnectivityChangeReceiver: onReceive(): ayswContainerShown="
                    + ayswContainerShown + " resumePlayingContainerShown="
                    + resumePlayingContainerShown);

            if (ayswContainerShown || resumePlayingContainerShown) {
                PerkLogger.d(TAG,
                        "Ignoring connectivity-changes when showing AYSW or resume-playing "
                                + "prompts!");
                return;
            }

            if (connected && mResumeVideoOnConnectivity && brightcoveVideoView != null) {
                // Resume video if we were paused on account of losing connectivity

                // do not request ads for this condition:
                /*if (freeWheelController != null) {
                    PerkLogger.e(TAG,
                            "Disabling ads as we are trying to resume video on regaining lost "
                                    + "network connectivity!");
                    freeWheelController.setAdRequestingEnabled(false);
                }*/

                // We delay the call since trying to play immediately fails as the network calls
                // continue to fail for sometime until the network connection isn't established
                // fully (specially in cases where the device is using a VPN service)
                brightcoveVideoView.removeCallbacks(mResumeVideoRunnable);
                brightcoveVideoView.postDelayed(mResumeVideoRunnable, 6000);
            } else if (!connected && !mResumeVideoOnConnectivity) {
                // Pause any video being played when we lose connectivity (and we are not already
                // waiting for resuming connectivity from previous connection lost state)
                if (brightcoveVideoView != null && (brightcoveVideoView.isPlaying()
                        || mUserPausedVideo)) {
                    PerkLogger.e(TAG,
                            "Pausing/stopping video on losing connectivity... mUserPausedVideo: "
                                    + mUserPausedVideo);

                    // Pause video if user had not paused it already when we lost connectivity:
                    if (!mUserPausedVideo) {
                        brightcoveVideoView.pause();
                        mResumeVideoOnConnectivity = true;
                    }
                    brightcoveVideoView.stopPlayback();
                    if (eventEmitter != null && !mIsCurrentVideoLongform) {
                        eventEmitter.emit(ShowHideController.SHOW_MEDIA_CONTROLS);
                    }
                }
                if (exoPlayer != null
                        || mUserPausedVideo) {
                    PerkLogger.e(TAG,
                            "Pausing/stopping video on losing connectivity... mUserPausedVideo: "
                                    + mUserPausedVideo);

                    // Pause video if user had not paused it already when we lost connectivity:
                    if (!mUserPausedVideo) {
                        pauseExoPlayer();
                        mResumeVideoOnConnectivity = true;
                    }
                }
            }
        }
    };

    private void registerConnectivityChangeReceiver() {
        unregisterConnectivityChangeReceiver();
        registerReceiver(mConnectivityChangeReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unregisterConnectivityChangeReceiver() {
        try {
            unregisterReceiver(mConnectivityChangeReceiver);
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerConnectivityChangeReceiver();

        if (exoPlayer != null && mIsCurrentVideoLongform && exoPlayerView != null) {
            exoPlayerView.postDelayed(() -> {
                try {
                    if (exoPlayer == null) {
                        PerkLogger.d(TAG, "onResume(): exo-player is null!");
                        return;
                    }

                    PerkLogger.d(TAG, "onResume(): resumeContainer-visible: " + (
                            mActivityVideoPlayerBinding.resumePlayingContainer.getVisibility()
                                    == View.VISIBLE) + " mIsCurrentVideoLongform: "
                            + mIsCurrentVideoLongform + " exoPlayer.state: "
                            + exoPlayer.getPlaybackState() + " mResumeVideoOnConnectivity: "
                            + mResumeVideoOnConnectivity + " aysw-visible: " + (
                            mActivityVideoPlayerBinding.ayswContainer.getVisibility()
                                    == View.VISIBLE));

                    if (mIsCurrentVideoLongform && exoPlayer.getPlaybackState() != Player.STATE_IDLE
                            && mActivityVideoPlayerBinding.resumePlayingContainer.getVisibility()
                            != View.VISIBLE && mActivityVideoPlayerBinding.ayswContainer.getVisibility()
                            != View.VISIBLE && !mResumeVideoOnConnectivity) {
                        playExoPlayer();
                    }
                } catch (Exception e) {
                }
            }, 300);
        }
    }

    @Override
    protected void onPause() {
        unregisterConnectivityChangeReceiver();
        try{
            //if (mIsCurrentVideoLongform)
            pauseExoPlayer();
        }catch (Exception e){}
        super.onPause();
    }

    private Runnable mResumeVideoRunnable = new Runnable() {
        @Override
        public void run() {
            // Continue only if we actually have network connectivity:
            if (!AppUtility.isNetworkAvailable(getApplicationContext(), false)) {
                PerkLogger.e(TAG,
                        "mResumeVideoRunnable: Ignoring since network connectivity isn't available yet");

                if (brightcoveVideoView != null) {
                    brightcoveVideoView.removeCallbacks(mResumeVideoRunnable);
                    brightcoveVideoView.postDelayed(mResumeVideoRunnable, 6000);
                }
                return;
            }

            // Resume video if we were paused on account of losing connectivity
            if (mResumeVideoOnConnectivity && brightcoveVideoView != null
                    && !brightcoveVideoView.isPlaying() && brightcoveVideoView.isShown()) {
                PerkLogger.d(TAG, "Resuming video on connectivity obtained");
                brightcoveVideoView.start();
            }
            if (mResumeVideoOnConnectivity && exoPlayer != null
                    && mIsCurrentVideoLongform) {
                PerkLogger.d(TAG, "Resuming video on connectivity obtained");
                playExoPlayer();
            }
        }
    };

    private Runnable mAyswPromptRunnable = new Runnable() {
        @Override
        public void run() {
            if (mHasShownAyswPrompt) {
                PerkLogger.d(TAG,
                        "mAyswPromptRunnable: AYSW prompt has already been shown... ignoring event");
                return;
            }

            /*if (brightcoveVideoView == null) {
                PerkLogger.e(TAG, "mAyswPromptRunnable: brightcoveVideoView is null!");
                return;
            }

            if (!brightcoveVideoView.isPlaying()) {
                PerkLogger.e(TAG,
                        "mAyswPromptRunnable: Returning as no video is playing currently!");
                return;
            }*/

            if (exoPlayer == null) {
                PerkLogger.e(TAG, "mAyswPromptRunnable: exoPlayer is null!");
                return;
            }

            /*  if (playWhenReady && playbackState == Player.STATE_READY) {
                  // media actually playing
                } else if (playWhenReady) {
                  // might be idle (plays after prepare()),
                  // buffering (plays when data available)
                  // or ended (plays when seek away from end)
                } else {
                  // player paused in any state
                }
            */

            if (!isExoPlayerCurrentlyPlaying() && (brightcoveVideoView != null && !brightcoveVideoView.isPlaying())) {
                PerkLogger.e(TAG,
                        "mAyswPromptRunnable: Returning as no video is playing currently!");
                return;
            }

            PerkLogger.d(TAG,"mAyswPromptRunnable: Showing AYSW prompt!");

            mHasShownAyswPrompt = true;
            if (brightcoveVideoView != null) {
                brightcoveVideoView.pause();
            }
            pauseExoPlayer();
            toggleAYSWPrompt(true);
        }
    };

    private boolean isExoPlayerCurrentlyPlaying() {
        if (exoPlayer == null) {
            PerkLogger.e(TAG, "isExoPlayerCurrentlyPlaying: exoPlayer is null!");
            return false;
        }

        return exoPlayer.getPlayWhenReady()
                && (exoPlayer.getPlaybackState() == Player.STATE_BUFFERING
                || exoPlayer.getPlaybackState() == Player.STATE_READY);
    }

    private Runnable mAYSWPromptNoResponseRunnable = new Runnable() {
        @Override
        public void run() {
            if (brightcoveVideoView == null || VideoPlayerActivity.this.isFinishing()
                    || VideoPlayerActivity.this.isDestroyed()) {
                PerkLogger.e(TAG, "mAYSWPromptNoResponseRunnable: brightcoveVideoView is null or activity is gone!");
                return;
            }

            PerkLogger.d(TAG,"mAYSWPromptNoResponseRunnable: Finishing due to no response from user!");
            VideoPlayerActivity.this.finish();
        }
    };

    private FinishReceiver finishReceiver;

    private void registerFinishReceiver() {
        try {
            unregisterFinishReceiver();
            finishReceiver = new FinishReceiver();
            registerReceiver(finishReceiver, new IntentFilter(AppConstants.ACTION_FINISH));
        } catch (Exception e) {
        }
    }

    private void unregisterFinishReceiver() {
        try {
            if (finishReceiver != null) {
                unregisterReceiver(finishReceiver);
                finishReceiver = null;
            }
        } catch (Exception e) {
        }
    }

    private final class FinishReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AppConstants.ACTION_FINISH)) {
                finish();
            }
        }
    }
    private void initExoPlayer(){

        //playerView.setControllerVisibilityListener(this);

        //playerView.setErrorMessageProvider(new PlayerErrorMessageProvider());
        exoPlayerView.requestFocus();
        //////


        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);


        trackSelectionHelper = new TrackSelectionHelper(trackSelector, videoTrackSelectionFactory);
// 2. Create the player
        exoPlayer =
                ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector);
        exoPlayer.addMetadataOutput(new MetadataOutput() {
            @Override
            public void onMetadata(Metadata metadata) {
                Log.d(TAG, "Metadata received: " + metadata);

                // Make sure we have valid list of metadata
                if (metadata == null || metadata.length() == 0) {
                    return;
                }

                ////
                Log.d(TAG, "onMetadata");
                for(int i = 0; i < metadata.length(); i++) {
                    Metadata.Entry entry = metadata.get(i);
                    Log.d(TAG, "Metadata.Entry "+entry);
                    if (entry instanceof Id3Frame) {
                        Id3Frame id3Frame = (Id3Frame) entry;

                        Log.d(TAG, "onMetadata" + id3Frame);
                        if (id3Frame instanceof PrivFrame) {
                            PrivFrame privframe = (PrivFrame)id3Frame;
                            String aStr = (String) (privframe.owner);
                            Log.d(TAG, "Priv id3tag: " + aStr);
                            // Make sure we only send 249 bytes
                            if (aStr.length() >= 249) {
                                plugin.exoTrackTimedMetaData(aStr.substring(0, 249));
                            }

                        }
                    }
                }
            }
        });
        //player = ExoPlayerFactory.newSimpleInstance(MainActivity.this);
        exoPlayerView.setPlayer(exoPlayer);

        //player.addListener(new PlayerEventListener());
        exoPlayer.setPlayWhenReady(false);
        //player.addAnalyticsListener(new EventLogger(trackSelector));
        exoPlayerView.setPlayer(exoPlayer);
        //playerView.setPlaybackPreparer(this);

/*
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(MainActivity.this,
                Util.getUserAgent(MainActivity.this, "yourApplicationName"));


// This is the MediaSource representing the media to be played.
        //MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse("https://video1.perk.com/files/bc/CMC-C_181018_3814694_Kemper_on_Kemper__Inside_The_Mind_of_a_Seria_HLS/CMC-C_181018_3814694_Kemper_on_Kemper__Inside_The_Mind_of_a_Seria_HLS.m3u8"));


        Uri uri = Uri.parse("https://video1.perk.com/files/bc/CMC-C_181018_3814694_Kemper_on_Kemper__Inside_The_Mind_of_a_Seria_HLS/CMC-C_181018_3814694_Kemper_on_Kemper__Inside_The_Mind_of_a_Seria_HLS.m3u8");
        MediaSource videoSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        //MediaSource videoSource = new HlsMediaSource.Factory(dataSourceFactory).setPlaylistParserFactory(new DefaultHlsPlaylistParserFactory(getOfflineStreamKeys(uri))).createMediaSource(uri);
// Prepare the player with the source.
        player.prepare(videoSource);

        */

        btnCC = findViewById(R.id.btn_captions);
        btnCC.setVisibility(View.GONE);
        btnCC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    trackSelectionHelper.showSelectionDialog(
                            VideoPlayerActivity.this, "", mappedTrackInfo, (int) view.getTag());
                }

            }
        });

        exoPlayer.addListener(mPlayerEventListener);
    }

    private final Player.EventListener mPlayerEventListener = new Player.EventListener() {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
            PerkLogger.d(TAG,
                    "onTimelineChanged: timeline: " + AppUtility.safeReflectionToString(
                            timeline) + " reason: " + reason + " manifest: "
                            + AppUtility.safeReflectionToString(manifest));
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            PerkLogger.d(TAG,
                    "onTracksChanged: trackGroups: " + AppUtility.safeReflectionToString(
                            trackGroups) + " trackSelections: "
                            + AppUtility.safeReflectionToString(trackSelections));
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            PerkLogger.d(TAG, "onLoadingChanged: isLoading: " + isLoading);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            PerkLogger.d(TAG, "onPlayerStateChanged: playWhenReady: " + playWhenReady
                    + " playbackState: " + playbackState);

            switch (playbackState) {

                case Player.STATE_IDLE:

                    // notifyNielsenVideoEvent(false);
                    break;

                case Player.STATE_READY:

                    bufferEndedExoPlayer();

                    notifyNielsenVideoEvent(playWhenReady);
                    break;

                case Player.STATE_BUFFERING:
                    bufferStartedExoPlayer();
                    break;

                case Player.STATE_ENDED:
                    finishedVideoExoPlayer(playWhenReady);
                    break;

                default:

                    break;
            }
            updateButtonVisibilities();
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            PerkLogger.d(TAG, "onRepeatModeChanged: repeatMode: " + repeatMode);
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            PerkLogger.d(TAG, "onShuffleModeEnabledChanged: shuffleModeEnabled: " + shuffleModeEnabled);
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            PerkLogger.d(TAG,
                    "onPlayerError: error: " + AppUtility.safeReflectionToString(error));
            Event event = new Event(EventType.ERROR);
            mPluginEventListerner.OnBCEvent(event);
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            PerkLogger.d(TAG, "onPositionDiscontinuity: reason: " + reason);

            // Do this only if a video is playing:
            if (mHasTrackedInitialEvents && exoPlayer != null
                    && exoPlayer.getPlaybackState() != Player.STATE_ENDED
                    && reason == Player.DISCONTINUITY_REASON_SEEK) {

                Event event = new Event(EventType.DID_SEEK_TO);
                mPluginEventListerner.OnBCEvent(event);
            } else {
                PerkLogger.d(TAG,
                        "onPositionDiscontinuity: Ignoring event obtained without a video being "
                                + "played");
            }
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            PerkLogger.d(TAG, "onPlaybackParametersChanged: playbackParameters: "
                    + AppUtility.safeReflectionToString(playbackParameters));
        }

        @Override
        public void onSeekProcessed() {
            PerkLogger.d(TAG, "onSeekProcessed");
        }
    };

    // User controls

    private void updateButtonVisibilities() {
        btnCC.setVisibility(View.GONE);
        if (exoPlayer == null) {
            return;
        }

        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo == null) {
            return;
        }

        for (int i = 0; i < mappedTrackInfo.length; i++) {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                Button button = new Button(this);
                int label;
                switch (exoPlayer.getRendererType(i)) {
                    case C.TRACK_TYPE_AUDIO:

                        break;
                    case C.TRACK_TYPE_VIDEO:

                        break;
                    case C.TRACK_TYPE_TEXT:
                        btnCC.setVisibility(View.VISIBLE);
                        btnCC.setTag(i);
                        break;
                    default:
                        continue;
                }

            }
        }


    }
    private void notifyNielsenVideoEvent(boolean playWhenReady) {

        // Make sure we have valid exo player controller

        if (exoPlayer == null) {
            return;
        }

        // Notify Nielsen that video has started playing
        final boolean isVideoStarting = exoPlayer.getCurrentPosition() == 0;

        final boolean hasVideoEnded =  hasExoPlayerVideoEnded();

        PerkLogger.d(TAG,
                "notifyNielsenVideoEvent: isVideoStarting: " + isVideoStarting + " hasVideoEnded: "
                        + hasVideoEnded + " current: " + exoPlayer.getCurrentPosition()
                        + " duration: " + exoPlayer.getDuration());

        // NOTE: In Exo player there is no state as video paused or resumed. All that is notified
        //       using the play back state READY and boolean video should start plating immediately
        //       when ready.

        if (playWhenReady) { // Notify Nielsen SDK that video has started playing
            //NielsenController.INSTANCE.play();
            // TODO: Remove - currently here just to test long-form videos, without watching them completely
            // exoPlayer.seekTo(exoPlayer.getDuration() - 3000);

            plugin.exoPlay();
            Event event = new Event(EventType.DID_PLAY);
            mPluginEventListerner.OnBCEvent(event);

        } else if (exoPlayer.getPlaybackState() != Player.STATE_IDLE && !isVideoStarting
                && !hasVideoEnded) { // Notify Nielsen SDK that video has been paused
            //NielsenController.INSTANCE.stop();
            plugin.exoPause();
        }
    }

    private boolean hasExoPlayerVideoEnded() {
        if (exoPlayer == null)
            return false;

        return exoPlayer.getPlaybackState() == Player.STATE_ENDED || (exoPlayer.getDuration() >= 0
                && exoPlayer.getCurrentPosition() >= exoPlayer.getDuration());
    }

    private void playExoPlayer() {
        try{
            plugin.startSessionIfNeeded();
            if (exoPlayer != null) {
                exoPlayer.setPlayWhenReady(true);
            }
            // exoPlayer.getPlaybackState();
        }catch (Exception e){}

    }
    private void pauseExoPlayer(){
        try{
            if (exoPlayer != null)exoPlayer.setPlayWhenReady(false);
            //exoPlayer.getPlaybackState();
        }catch (Exception e){}

    }
    private void stopExoPlayer() {
        try{
            if ( exoPlayer != null){
                exoPlayer.stop(true);
            }

        }catch (Exception e){}

    }
    private  void bufferStartedExoPlayer(){
        try{
            bIsExoBufferStarged = true;
            plugin.exoBufferStarted();
        }catch (Exception e){}

    }
    private  void bufferEndedExoPlayer(){
        try{
            if (bIsExoBufferStarged){
                bIsExoBufferStarged = false;
                plugin.exoBufferEnd();
            }
        }catch (Exception e){}


    }
    private void finishedVideoExoPlayer(boolean finished){
        try{
            pauseExoPlayer();

            // Do complete-actions only if video played... since this is called twice - once
            // normally when play ends & 2nd when we do pause action above, which causes
            // onPlayerStateChanged to be called again.
            if (!mHasTrackedInitialEvents) {
                PerkLogger.d(TAG, "finishedVideoExoPlayer: Ignoring 2nd call!");
                return;
            }

            if (exoPlayer != null) {
                exoPlayer.seekToDefaultPosition();
            }

            plugin.exoComplete(finished);
        }catch (Exception e){}

    }
    ImageView mFullScreenIcon;
    boolean mExoPlayerFullscreen;
   // Dialog mFullScreenDialog;
    private void initFullscreenButton() {


        mFullScreenIcon = findViewById(R.id.exo_fullscreen_icon);

        mFullScreenIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mExoPlayerFullscreen)
                    openFullscreenDialog(false);
                else
                    closeFullscreenDialog(false);
            }
        });
        //initFullscreenDialog();
    }

    private void openFullscreenDialog(boolean bIfFromDeviceOrientaiton) {

        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(VideoPlayerActivity.this, R.drawable.ic_fullscreen_skrink));
        mExoPlayerFullscreen = true;
        if(!bIfFromDeviceOrientaiton)eventEmitter.emit(EventType.ENTER_FULL_SCREEN);
    }
    private void closeFullscreenDialog(boolean bIfFromDeviceOrientaiton) {

        mExoPlayerFullscreen = false;
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(VideoPlayerActivity.this, R.drawable.ic_fullscreen_expand));
        if(!bIfFromDeviceOrientaiton)eventEmitter.emit(EventType.EXIT_FULL_SCREEN);
    }
}
