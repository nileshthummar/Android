/**
 * App SDK Plugin
 * Copyright (C) 2016, The Nielsen Company (US) LLC. All Rights Reserved.
 *
 * Software contains the Confidential Information of Nielsen and is subject to your relevant agreements with Nielsen.
 */
package com.watchback2.android.analytics;

/**
 * Created by sockam03 on 9/9/2015.
 * Version 1 - 01/05/2016 - Initial Plugin
 * Version 2 - 01/11/2016 - Changes for OnceUx ads which provide AdPause and AdResume
 *           - 02/19/2016 - Added error notification
 */

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.util.Log;
import android.view.ViewGroup;

import com.brightcove.player.display.ExoPlayerVideoDisplayComponent;
import com.brightcove.player.event.AbstractComponent;
import com.brightcove.player.event.Component;
import com.brightcove.player.event.Default;
import com.brightcove.player.event.Emits;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.event.ListensFor;
import com.brightcove.player.view.BrightcoveExoPlayerVideoView;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.id3.Id3Frame;
import com.google.android.exoplayer2.metadata.id3.PrivFrame;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.utils.AppConstants;

import java.util.HashMap;

@Emits(events={})

@ListensFor(events = {
        EventType.COMPLETED, // Indicates video playback has completed (i.e., played entire media)
        EventType.ERROR,
        EventType.CUE_POINT, // Indicates a cue point has been fired at a specific time position.
        EventType.DID_CHANGE_LIST, //Indicates that the list of videos changed.
        EventType.DID_SELECT_SOURCE, // Indicates a Source rendition was selected, in response to SELECT_SOURCE.
        EventType.DID_PAUSE, //Indicates playback just stopped, in response to PAUSE.
        EventType.DID_PLAY, //Indicates playback just began, in response to PLAY.
        EventType.DID_SEEK_TO, // Indicates a previously requested seek action completed, in response to SEEK_TO.
        EventType.DID_SET_SOURCE, // Indicates a Source has been successfully loaded, in response to SET_SOURCE.
        EventType.DID_SET_VIDEO, // Indicates a Video loaded successfully, in response to SET_VIDEO.
        EventType.DID_STOP, //  Indicates playback just stopped, in response to STOP.
        EventType.PROGRESS, // Indicates playback just stopped, in response to STOP.
        EventType.WILL_CHANGE_VIDEO, // Indicates the current video will change to the next video in the list.
        EventType.SET_VIDEO, // Indicates a Video is available for any component that can use one.
        EventType.SET_SOURCE, //  Indicates a Source is available for any component that requires a Source to function.
        EventType.PLAY, // Indicates playback should begin.
        EventType.PAUSE, // Indicates playback should be paused.
        EventType.SEEK_TO, // Instructs player to seek to a specific position on the timeline.
        EventType.STOP, // Indicates the current media will not be played any more and that resources related to the current media should be released.
        EventType.SET_CUE_POINT,
        EventType.AD_PROGRESS,
        EventType.AD_BREAK_STARTED,
        EventType.AD_BREAK_COMPLETED,
        EventType.AD_ERROR,
        EventType.AD_STARTED,
        EventType.AD_COMPLETED,
        EventType.AD_PAUSED,
        EventType.AD_RESUMED,
        EventType.AD_PROGRESS,
        EventType.VIDEO_DURATION_CHANGED,
        EventType.BUFFERING_STARTED,
        EventType.BUFFERING_COMPLETED,
        EventType.ACTIVITY_STOPPED,
        "OPT_IN", // Indicates the change in the Opt-In/Opt-Out option
        "APP_DISABLE", // Indicates if the AppSDK is enabled/disabled
        "SETTINGS_REQUEST", // Indicates a change in the config parameter
        "STATIC_CONTENT", // Indicates a STATIC Content needs to be sent
        "NETWORK_AVAILABLE_CHANGE", // Indicates a change in the network availability due to airplane mode or wifi ON/OFF
})


public class Plugin extends AbstractComponent
        implements Component, OnCompletionListener, OnErrorListener, ExoPlayerVideoDisplayComponent.MetadataListener {
    public static final String TAG = "Adobe->";//Plugin.class.getSimpleName();


//    // Used to play the simulated advertisement.

    private enum adState {AD_PROGRESS, AD_BUFFERING, AD_PAUSED, AD_STOP}
    private enum adBreakState {ADBREAK_PROGRESS, ADBREAK_BUFFERING, ADBREAK_PAUSED, ADBREAK_STOP}

    private adState mAdState = adState.AD_STOP;
    private adBreakState mAdBreakState = adBreakState.ADBREAK_STOP;


    private PluginEventListener pluginEventListener;
    // Used to contain the VideoView.
    private ViewGroup viewGroup;

    private int videoDuration = -1;
    private boolean mShouldLoadContentMetadata = true;
    //private VideoAnalyticsProvider _analyticsProvider;
    private BrightcovePlaylistData.BrightcoveVideo mVideoData;
    //private int mCurrentVideoPosition = 0;
    private SimpleExoPlayer mExoPlayer;
    public Plugin(EventEmitter emitter, ViewGroup viewGroup, SimpleExoPlayer exoPlayer) {
        super(emitter, Plugin.class);

        this.viewGroup = viewGroup;
        this.mExoPlayer = exoPlayer;
        if(AppConstants.kWatchbackDebug) Log.d(TAG, "Initializing ");
        initializeListeners();
        ((ExoPlayerVideoDisplayComponent) (((BrightcoveExoPlayerVideoView) viewGroup).getVideoDisplay())).setMetadataListener(this);
    }

    private void initializeListeners() {

        //_analyticsProvider = new VideoAnalyticsProvider(this);
        VideoAnalyticsProvider.INSTANCE.setPlugin(this);

        if(AppConstants.kWatchbackDebug) Log.d(TAG, "Initializing Listeners");
        addListener(EventType.COMPLETED, new OnCompletedListener()); // One item in the playlist is completed
        addListener(EventType.ERROR, new OnErrorListener());
        addListener(EventType.DID_PAUSE, new OnDidPauseListener());
        addListener(EventType.DID_PLAY, new OnDidPlayListener());
        addListener(EventType.DID_SEEK_TO, new OnDidSeekToListener());
        addListener(EventType.DID_STOP, new OnDidStopListener());
        addListener(EventType.PROGRESS, new OnProgressListener());

        addListener(EventType.SET_VIDEO, new OnSetVideoListener());
        addListener(EventType.SET_SOURCE, new OnSetSourceListener());
        addListener(EventType.PLAY, new OnPlayListener());
        addListener(EventType.PAUSE, new OnPauseListener());
        addListener(EventType.SEEK_TO, new OnSeekToListener());
        addListener(EventType.STOP, new OnStopListener());
        addListener(EventType.AD_BREAK_STARTED, new OnAdBreakStartedListener());
        addListener(EventType.AD_BREAK_COMPLETED, new OnAdBreakCompltedListener());
        addListener(EventType.AD_ERROR, new OnAdErrorListener());
        addListener(EventType.AD_STARTED, new OnAdStartListener());
        addListener(EventType.AD_COMPLETED, new OnAdCompleteListener());
        addListener(EventType.AD_PAUSED, new OnAdPausedListener());
        addListener(EventType.AD_RESUMED, new OnAdResumedListener());
        addListener(EventType.AD_PROGRESS, new OnAdProgressListener());
        addListener(EventType.VIDEO_DURATION_CHANGED, new OnVideoDurationChangedListener());
        addListener(EventType.BUFFERING_STARTED, new OnBufferingStartedListener());
        addListener(EventType.BUFFERING_COMPLETED, new OnBufferingCompletedListener());
        addListener(EventType.ACTIVITY_STOPPED, new OnActivityStoppedListener());

    }

    public class OnVideoDurationChangedListener implements EventListener {
        @Default
        @Override
        public void processEvent(Event event) {
            if (event == null) {
                Log.e(TAG, "OnVideoDurationChangedListener: event is NULL");
                return;
            }
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnVideoDurationChangedListener: " + event.properties);
            if (event.properties.containsKey("duration")) {
                videoDuration = event.getIntegerProperty(Event.VIDEO_DURATION);
            }
            passBCEvent(event);
        }
    }


    public class OnBufferingStartedListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnBuffering Started " + event);

            VideoAnalyticsProvider.INSTANCE.onBufferStart();
            passBCEvent(event);
        }
    }


    public class OnBufferingCompletedListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnBuffering Completed " + event);

            VideoAnalyticsProvider.INSTANCE.onBufferCompete();
            passBCEvent(event);
        }
    }


    public class OnActivityStoppedListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if (event == null) {
                Log.e(TAG, "OnActivityStoppedListener: event is NULL");
                return;
            }
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnActivityStoppedListener: " + event.properties);
            if (mAdState == adState.AD_PROGRESS){
                VideoAnalyticsProvider.INSTANCE.onAdPause();
            }

            passBCEvent(event);
        }
    };
    public class OnAdBreakStartedListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if (event == null) {
                Log.e(TAG, "OnAdBreakStartedListener: event is NULL");
                return;
            }
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnAdBreakStartedListener: " + event.properties);
            passBCEvent(event);
        }
    };
    public class OnAdBreakCompltedListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if (event == null) {
                Log.e(TAG, "OnAdBreakCompltedListener: event is NULL");
                return;
            }
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnAdBreakCompltedListener: " + event.properties);
            if (mAdBreakState == adBreakState.ADBREAK_PROGRESS) {
                VideoAnalyticsProvider.INSTANCE.onAdBreakComplete();
            }
            mAdBreakState = adBreakState.ADBREAK_STOP;
            passBCEvent(event);

            /////



        }
    };

    public class OnAdStartListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if (event == null) {
                Log.e(TAG, "OnAdStartListener: event is NULL");
                return;
            }
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnAdStartListener called " + event.properties);

//            _analyticsProvider.onAdStart(adBreakData,adData);
            passBCEvent(event);
        }
    }

    public class OnAdCompleteListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnAdCompletedListener called ");
            if (mAdState == adState.AD_PROGRESS) {
                VideoAnalyticsProvider.INSTANCE.onAdComplete();
            }
            mAdState = adState.AD_STOP;
            passBCEvent(event);
        }
    }

    private class OnAdPausedListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnAdPausedListener: " + event.properties);

            passBCEvent(event);
        }
    }

    private class OnAdResumedListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnAdResumedListener: " + event.properties);
            passBCEvent(event);
        }
    }
    public class OnAdErrorListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if (event == null) {
                Log.e(TAG, "OnAdErrorListener: event is NULL");
                return;
            }

            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnAdErrorListener: " + event.properties);
            passBCEvent(event);
        }
    };
    private class OnAdProgressListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnAdProgressListener: " + event.properties);
            startSessionIfNeeded();
            if (mAdBreakState != adBreakState.ADBREAK_PROGRESS) {
                mAdBreakState = adBreakState.ADBREAK_PROGRESS;
                //////
                int  videoPosition = ((BrightcoveExoPlayerVideoView)viewGroup).getCurrentPosition()/1000;
                int pos = videoPosition == 0 ? 0 : videoPosition / 1000;
                int duration = videoDuration > 0 ? videoDuration / 1000 : videoDuration;
                String contentType = "";
                if (pos <= 0)
                    contentType = "preroll";
                else if (pos > 0 && pos < duration - 1) { // Changed to ( < duration -1) as in googleIMA the last videoPos is less than duration -1
                    contentType = "midroll";
                } else if (duration == -1) {
                    contentType = "midroll";
                } else {
                    contentType = "postroll";
                }
                HashMap<String, Object> adBreakData = new HashMap<>();
                adBreakData.put("name",contentType);
                adBreakData.put("position","1");
                adBreakData.put("startTime",videoPosition);
                VideoAnalyticsProvider.INSTANCE.onAdBreakStart(adBreakData);
            }
            if (mAdState != adState.AD_PROGRESS) {
                mAdState = adState.AD_PROGRESS;
                //////
                // of the content which is obtained from ProgressListener or DidSetSourceListener
                int  videoPosition = ((BrightcoveExoPlayerVideoView)viewGroup).getCurrentPosition()/1000;
                int pos = videoPosition == 0 ? 0 : videoPosition / 1000;
                int duration = videoDuration > 0 ? videoDuration / 1000 : videoDuration;
                String contentType = "";
                if (pos <= 0)
                    contentType = "preroll";
                else if (pos > 0 && pos < duration - 1) { // Changed to ( < duration -1) as in googleIMA the last videoPos is less than duration -1
                    contentType = "midroll";
                } else if (duration == -1) {
                    contentType = "midroll";
                } else {
                    contentType = "postroll";
                }
                if(AppConstants.kWatchbackDebug) Log.d(TAG, "getAdPosition is " + contentType);
                //////



                String name = "unknown";
                if (event.properties.containsKey(Event.AD_TITLE)) {
                    name = String.valueOf(event.properties.get(Event.AD_TITLE));
                }
                String adId = "unknown";
                if (event.properties.containsKey(Event.AD_ID)) {
                    adId = String.valueOf(event.properties.get(Event.AD_ID));
                }
                String length = "-1";

                if (event.properties.containsKey("duration")) {
                    length = String.valueOf(event.properties.get("duration"));
                }


                HashMap<String, Object> adData = new HashMap<>();
                adData.put("contentType",contentType);
                adData.put("name",name);
                adData.put("adId",adId);
                adData.put("position","1");
                adData.put("length",length);

                VideoAnalyticsProvider.INSTANCE.onAdStart(adData);
            }



            passBCEvent(event);
        }
    }

    // Handle completion by resuming content playback.
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(AppConstants.kWatchbackDebug) Log.d(TAG, "onCompletion");
    }


    // Handle errors by resuming content playback.
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int frameworkError, int implementationError) {
        Log.e(TAG, "onError: " + frameworkError + ", " + implementationError);
        sendPluginEvent(PluginEventListener.PluginEvent.PLAYER_ERROR);
        return true;
    }


    // DID_PLAY happens after playback has begun.
    private class OnDidPlayListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if (event == null) {
                Log.e(TAG, "OnDidPlayListener: event is NULL");
                return;
            }
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnDidPlayListener: " + event.properties);

            VideoAnalyticsProvider.INSTANCE.onVideoPlay(mVideoData);
            passBCEvent(event);
        }
    }

    // DID_PAUSE happens after pause() has been called on the
    // MediaPlayer.
    private class OnDidPauseListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnDidPauseListener: " + event.properties);
            VideoAnalyticsProvider.INSTANCE.onVideoPause();
            passBCEvent(event);
        }

    }

    // DID_SEEK_TO happens after the
    // MediaPlayer.OnSeekCompleteListener() callback has been invoked.
    private class OnDidSeekToListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnDidSeekToListener: " + event.properties);
            VideoAnalyticsProvider.INSTANCE.onSeekStart();
            VideoAnalyticsProvider.INSTANCE.onSeekComplete();
            passBCEvent(event);
        }
    }

    // DID_STOP happens after the MediaPlayer has been released.
    private class OnDidStopListener implements EventListener {
        @Default
        @Override
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnDidStopListener: " + event.properties);
            passBCEvent(event);
        }
    }

    // PROGRESS happens every 500 milliseconds during playback.
    private class OnProgressListener implements EventListener {

        @Default
        @Override
        public void processEvent(Event event) {
            if (event == null) {
              //  Log.e(TAG, "OnProgressListener: event is NULL");
                return;
            }
           // if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnProgressListener: " + event.properties);
            startSessionIfNeeded();
            passBCEvent(event);
        }
    }


    private class OnErrorListener implements EventListener {
        @Override
        @Default
        public void processEvent(Event event) {
            if (event == null) {
                return;
            }
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnErrorListener: " + event.properties);
            String errorid = "";
            if (event.properties.containsKey(Event.ERROR_CODE)){
                errorid = String.valueOf(event.properties.get(Event.ERROR_CODE));
            }
            VideoAnalyticsProvider.INSTANCE.onError(errorid);
            passBCEvent(event);
        }
    };
    // COMPLETED happens after the MediaPlayer.OnCompletionListener()
    // callback has been invoked.
    private class OnCompletedListener implements EventListener {
        @Override
        @Default
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnCompletedListener: " + event.properties);
            VideoAnalyticsProvider.INSTANCE.onChapterCompete();
            VideoAnalyticsProvider.INSTANCE.onComplete();
            VideoAnalyticsProvider.INSTANCE.onVideoUnLoad();
            mShouldLoadContentMetadata = true;
            passBCEvent(event);

        }
    }

    private class OnSetVideoListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnSetVideoListener: " + event.properties);
            mShouldLoadContentMetadata = true;
//            _analyticsProvider.onVideoLoad(mVideoData);
//            _analyticsProvider.onChapterStart(mVideoData);
            VideoAnalyticsProvider.INSTANCE.onChangedVideo();
            passBCEvent(event);
        }
    }

    private class OnSetSourceListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnSetSourceListener: " + event.properties);
            passBCEvent(event);
        }
    }

    private class OnPlayListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnPlayListener: " + event.properties);
            //startSessionIfNeeded()
            passBCEvent(event);
        }

    }

    private class OnPauseListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnPauseListener: " + event.properties);

            passBCEvent(event);
        }
    }

    private class OnSeekToListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnSeekToListener: " + event.properties);

            passBCEvent(event);

        }
    }

    private class OnStopListener implements EventListener {
        @Override
        public void processEvent(Event event) {
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "OnStopListener: " + event.properties);

            passBCEvent(event);
        }
    }

    // Need to move the loadmetadata to another method that can be called from
    // OnVideoDurationChanged since duration is not available in OnDidSetSourceListener
    // when called for playApi.


    public void setPluginEventListener(PluginEventListener pluginEventListener) {
        this.pluginEventListener = pluginEventListener;
    }

    private void sendPluginEvent(PluginEventListener.PluginEvent event) {
        if (pluginEventListener != null) {
            pluginEventListener.onPluginEvent(event);
        }
    }

   // @Override
    protected void onDestroy() {
        VideoAnalyticsProvider.INSTANCE.destroy();
        VideoAnalyticsProvider.INSTANCE = null;

    }


    @Override
    public void onMetadata(Metadata metadata) {
        if(AppConstants.kWatchbackDebug) Log.d(TAG, "onMetadata");
        for(int i = 0; i < metadata.length(); i++) {
            Metadata.Entry entry = metadata.get(i);
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "Metadata.Entry "+entry);
            if (entry instanceof Id3Frame) {
                Id3Frame id3Frame = (Id3Frame) entry;

                if(AppConstants.kWatchbackDebug) Log.d(TAG, "onMetadata" + id3Frame);
                if (id3Frame instanceof PrivFrame) {
                    PrivFrame privframe = (PrivFrame)id3Frame;
                    String aStr = (String) (privframe.owner);
                    if(AppConstants.kWatchbackDebug) Log.d(TAG, "Priv id3 tag: " + aStr);
                    // Make sure we only send 249 bytes
                    if (aStr.length() >= 249) {
                        startSessionIfNeeded();
                        VideoAnalyticsProvider.INSTANCE.trackTimedMetaData(aStr.substring(0, 249));
                    }

                }
            }
        }
    }
    void passBCEvent(Event event){
      //  Log.w(TAG, String.valueOf(event));
        if (pluginEventListener != null){
            pluginEventListener.OnBCEvent(event);
        }
    }

    public Double getCurrentPlaybackTime() {
        //mVideoData
        //return ((BrightcoveExoPlayerVideoView)viewGroup).getCurrentPosition()/1000.0;//Double.valueOf(mCurrentContentPlayhead);
        /////
        if (mVideoData != null && mVideoData.isLongform()){
            long exoPlayerCurrentPosition = this.mExoPlayer == null ? 0 : this.mExoPlayer.getCurrentPosition()/1000;
            if (exoPlayerCurrentPosition < 0)
                exoPlayerCurrentPosition = 0;

            // Pass the PROGRESS event only if video is being played:
            if (mExoPlayer != null) {

                if (AppConstants.kWatchbackDebug) {
                    Log.d(TAG,
                            "getCurrentPlaybackTime: getPlaybackState: "
                                    + mExoPlayer.getPlaybackState()
                                    + " getPlayWhenReady: " + mExoPlayer.getPlayWhenReady());
                }

                if (mExoPlayer.getPlaybackState() == Player.STATE_READY
                        && mExoPlayer.getPlayWhenReady()) {
                    Event event = new Event(EventType.PROGRESS);
                    passBCEvent(event);
                }
            }
            return exoPlayerCurrentPosition * 1.0;
        }
        int  videoPosition = ((BrightcoveExoPlayerVideoView)viewGroup).getCurrentPosition()/1000;

        if (videoPosition < 0)
            videoPosition = 0;

        return videoPosition * 1.0;
    }
    public void setCurrentVideo(BrightcovePlaylistData.BrightcoveVideo videoData){
        mAdState = adState.AD_STOP;
        mVideoData = videoData;
    }

    public void startSessionIfNeeded(){
        if (mShouldLoadContentMetadata){
            mShouldLoadContentMetadata = false;
            VideoAnalyticsProvider.INSTANCE.onVideoLoad(mVideoData);
            VideoAnalyticsProvider.INSTANCE.onChapterStart(mVideoData);
        }
    }
    public void destroy() {
        if (VideoAnalyticsProvider.INSTANCE != null){
            VideoAnalyticsProvider.INSTANCE.destroy();
        }
    }

    public void exoPlay(){
        startSessionIfNeeded();
        VideoAnalyticsProvider.INSTANCE.onVideoPlay(mVideoData);
        Event event = new Event(EventType.PLAY);
        passBCEvent(event);
    }
    public void exoPause(){

        VideoAnalyticsProvider.INSTANCE.onVideoPause();
        Event event = new Event(EventType.PAUSE);
        passBCEvent(event);
    }
    public void exoComplete(boolean finished){
        if (finished){
            VideoAnalyticsProvider.INSTANCE.onChapterCompete();
            VideoAnalyticsProvider.INSTANCE.onComplete();
        }
        VideoAnalyticsProvider.INSTANCE.onVideoUnLoad();
        mShouldLoadContentMetadata = true;
        Event event = new Event(EventType.COMPLETED);
        passBCEvent(event);
    }
    public void exoBufferStarted(){
        VideoAnalyticsProvider.INSTANCE.onBufferStart();
    }
    public void exoBufferEnd(){
        VideoAnalyticsProvider.INSTANCE.onBufferCompete();
    }
    public void exoTrackTimedMetaData(String substring) {
        startSessionIfNeeded();
        VideoAnalyticsProvider.INSTANCE.trackTimedMetaData(substring);
    }
}
