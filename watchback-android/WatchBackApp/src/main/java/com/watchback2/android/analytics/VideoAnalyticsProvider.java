/*
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2014 Adobe Systems Incorporated
 * All Rights Reserved.

 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a
 * source other than Adobe, then your use, modification, or distribution of it requires the prior
 * written permission of Adobe.
 */

package com.watchback2.android.analytics;

import android.util.Log;

import com.adobe.primetime.va.simple.MediaHeartbeat;
import com.adobe.primetime.va.simple.MediaHeartbeat.MediaHeartbeatDelegate;
import com.adobe.primetime.va.simple.MediaHeartbeatConfig;
import com.adobe.primetime.va.simple.MediaObject;
import com.comscore.streaming.AdType;
import com.comscore.streaming.ContentType;
import com.comscore.streaming.ReducedRequirementsStreamingAnalytics;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.utils.AppConstants;
import com.watchback2.android.utils.PerkUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;



public class VideoAnalyticsProvider implements  MediaHeartbeatDelegate {
    private static final String LOG_TAG = "WB_NielsenAdobe";//VideoAnalyticsProvider.class.getSimpleName();

    private Plugin _plugin;
    private static MediaHeartbeat _heartbeat;

    private ReducedRequirementsStreamingAnalytics stremingAnalytics = new ReducedRequirementsStreamingAnalytics();
   private String mVideoIDForComScore;
   private boolean mbIsSessionValid = false;

    public static VideoAnalyticsProvider INSTANCE = new VideoAnalyticsProvider();
    private VideoAnalyticsProvider() {
        initHeartbeat();
    }
    void setPlugin(Plugin plugin) {
       _plugin = plugin;
    }
    private void initHeartbeat(){
       if (_heartbeat == null){
           // Media Heartbeat initialization
           MediaHeartbeatConfig config = new MediaHeartbeatConfig();

           config.trackingServer = "nbcume.hb.omtrdc.net";
           config.channel = "On-Domain";
           config.ovp = "Android";
           config.appVersion = PerkUtils.getAppVersionNumber();
           config.playerName = "Brightcove Android Player";
           config.ssl = true; // Set to “false” during QA and validation
           config.debugLogging = false; // Set to ”true” during QA and validation
           config.nielsenConfigKey = "922e1d53d3e10abdde5cbea1b55459f5bacc65d4/55ba372b3336330017000bbf";

           if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "Init MediaHeartbeat");
           _heartbeat = new MediaHeartbeat(this, config);
       }

    }
    public MediaObject getQoSObject() {
        // Build a static/hard-coded QoS info here.
       // if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "getQoSObject");
        Long bitrate = 50000L;
        Double startupTime = 2D;
        Double fps = 24D;
        Long droppedFrames = 10L;
        return MediaHeartbeat.createQoSObject(bitrate, startupTime, fps, droppedFrames);
    }

    public Double getCurrentPlaybackTime() {
        Double currentPlaybackTime = _plugin.getCurrentPlaybackTime();
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "getCurrentPlaybackTime -> "+currentPlaybackTime);
        return currentPlaybackTime;
    }

    void destroy() {

        if(_heartbeat != null)_heartbeat.trackSessionEnd();
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackSessionEnd() from destroy");
       _heartbeat = null;
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat = null");
    }


    void onVideoLoad(BrightcovePlaylistData.BrightcoveVideo videoData){
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "onVideoLoad");
        onChangedVideo();
        initHeartbeat();
        //stremingAnalytics = new ReducedRequirementsStreamingAnalytics();
        //if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "stremingAnalytics new object");
        String videoTitle = videoData.getName();
        String videoDescription = videoData.getDescription() != null ? videoData.getDescription() : "unknown";
        String guid = videoData.getCustomFields().getGuid();
        if (guid == null){
            guid = videoData.getId();
        }
        long duration = videoData.getDuration();
        Double length = duration/1000.0;

        ///
        SimpleDateFormat format = new SimpleDateFormat("hh:mm", Locale.US);
        String minute = format.format(new Date());

        format = new SimpleDateFormat("hh:00", Locale.US);
        String hour = format.format(new Date());

        format = new SimpleDateFormat("EEEE", Locale.US);
        String day = format.format(new Date());

        format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        String date = format.format(new Date());

        ///
        String strTags = "";
        try{
            List<String> arrTags = videoData.getTags();
            if (arrTags != null && arrTags.getClass().isInstance(List.class)){
                StringBuilder csvBuilder = new StringBuilder();

                for(String city : arrTags){
                    csvBuilder.append(city);
                    csvBuilder.append(",");
                }

                strTags = csvBuilder.toString();
            }
        }catch (Exception e){

        }


        ///

        MediaObject mediaInfo = _heartbeat.createMediaObject(
                videoTitle,
                guid,
                length,
                videoData.isLongform()?MediaHeartbeat.StreamType.LINEAR:MediaHeartbeat.StreamType.VOD
        );

        String publishedAt = videoData.getPublishedAt();
        if (publishedAt != null && publishedAt.length() > 19){
            publishedAt = publishedAt.substring(0,19);
        }
        HashMap<String, String> videoMetadata = new HashMap<String, String>();
        videoMetadata.put("videoprogram", videoData.getCustomFields().getShow() != null ? videoData.getCustomFields().getShow():videoData.getCustomFields().getProvider());
        videoMetadata.put("videotitle", videoTitle);
       // videoMetadata.put("videodaypart", "unknown");
        videoMetadata.put("videominute", minute);
        videoMetadata.put("videohour", hour);
        videoMetadata.put("videoday", day);
        videoMetadata.put("videodate",date );
        videoMetadata.put("Videoplayertech", "unknown");
        videoMetadata.put("videoplatform", "Android");
        videoMetadata.put("videonetwork", videoData.getCustomFields().getProvider() != null? videoData.getCustomFields().getProvider(): "unknown");
        videoMetadata.put("videoapp", "NBCUniversal Watchback Rewards");
        videoMetadata.put("videoscreen", "unknown");
        videoMetadata.put("videostatus", "Unrestricted");
        videoMetadata.put("videocallsign","unknown" );
        videoMetadata.put("videoepnumber", videoData.getCustomFields().getEpisodeNumber() != null? videoData.getCustomFields().getEpisodeNumber(): "unknown");
        videoMetadata.put("videoguid", guid);
        videoMetadata.put("videoairdate", publishedAt);
        videoMetadata.put("videoseason", videoData.getCustomFields().getSeasonNumber() != null? videoData.getCustomFields().getSeasonNumber(): "unknown");
        videoMetadata.put("videocliptype", videoData.getCustomFields().getProgrammingType() != null? videoData.getCustomFields().getProgrammingType() : "unknown");
        videoMetadata.put("videosubcat1", videoData.getCustomFields().getAdvertisingGenre()!= null? videoData.getCustomFields().getAdvertisingGenre(): "unknown");
        videoMetadata.put("videosubcat2", strTags);
        videoMetadata.put("videodescription",videoDescription );



        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "videoMetadata-> " + videoMetadata.toString());


        //Set to true if this is a resume playback scenario (not starting from playhead 0)
//                mediaInfo.setValue(MediaHeartbeat.MediaObjectKey.VideoResumed, true);

        // override default Preroll tracking wait time
//                mediaInfo.setValue(MediaHeartbeat.MediaObjectKey.PrerollTrackingWaitingTime, 500L);


        String strGracenoteEpisodeID = "unknown";
        BrightcovePlaylistData.CustomFields customFields = videoData.getCustomFields();
        if (customFields != null){
            if (customFields.getGracenoteEpId() != null){
                strGracenoteEpisodeID = customFields.getGracenoteEpId();
            }
        }
        // nielsen content metadata and channel info
        Map<String, Object> contentMetadata = NielsenMetadata.metadata != null ? NielsenMetadata.metadata : new HashMap<String, Object>();
        contentMetadata.put("clientid","us-800148");
        contentMetadata.put("subbrand","c05");
        contentMetadata.put("type","content");
        contentMetadata.put("assetid",guid);
        contentMetadata.put("program",videoData.getCustomFields().getShow() != null ? videoData.getCustomFields().getShow():videoData.getCustomFields().getProvider());
        contentMetadata.put("title",videoTitle);
        contentMetadata.put("length",length);
        contentMetadata.put("isfullepisode",videoData.isLongform()?"y":"n");
        contentMetadata.put("adloadtype",videoData.isLongform()?"1":"2");
        contentMetadata.put("adModel",videoData.isLongform()?"1":"2");
        contentMetadata.put("airdate",publishedAt);
        contentMetadata.put("tv",videoData.isLongform()?"true":"false");
        contentMetadata.put("datasource",videoData.isLongform()?"ID3":"CMS");
        contentMetadata.put("crossId1",strGracenoteEpisodeID);
        contentMetadata.put("crossId2",videoData.getCustomFields().getProvider() != null? videoData.getCustomFields().getProvider():"unknown");
        contentMetadata.put("hasAds","2");
        contentMetadata.put("segB","unknown");
        contentMetadata.put("segC","unknown");

        mediaInfo.setValue(MediaHeartbeat.MediaObjectKey.NielsenContentMetadata, contentMetadata);
        Map<String, Object> channelInfo = NielsenMetadata.channelInfo != null ? NielsenMetadata.channelInfo : new HashMap<String, Object>();
        channelInfo.put("channelName","NBCUniversal Watchback Rewards");
        mediaInfo.setValue(MediaHeartbeat.MediaObjectKey.NielsenChannelMetadata, channelInfo);
        if(_heartbeat != null)_heartbeat.trackSessionStart(mediaInfo, videoMetadata);

        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackSessionStart");
        mbIsSessionValid = true;

        onVideoPlay(videoData);
    }
    void onComplete(){
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "Playback completed.");

        if(_heartbeat != null)_heartbeat.trackComplete();
        stremingAnalytics.stop();
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "stremingAnalytics stop onComplete");
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackComplete()");
    }
    void onVideoUnLoad(){
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "Video unloaded.");
        if(_heartbeat != null)_heartbeat.trackSessionEnd();
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackSessionEnd()");
        mbIsSessionValid = false;
    }
    void onVideoPlay(BrightcovePlaylistData.BrightcoveVideo videoData){
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "Playback started.");
        if(_heartbeat != null)_heartbeat.trackPlay();
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackPlay()");
        try{
            if (mVideoIDForComScore == null || !mVideoIDForComScore.equalsIgnoreCase(String.valueOf(videoData.getId()))){
                stremingAnalytics = new ReducedRequirementsStreamingAnalytics();
                mVideoIDForComScore = String.valueOf(videoData.getId());
                if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "stremingAnalytics new object");
            }

            String publishedAt = videoData.getPublishedAt();
            if (publishedAt != null && publishedAt.length() > 10){
                publishedAt = publishedAt.substring(0,10);
            }
            String advertisingGenre = "*null";
            if (videoData.getCustomFields().getAdvertisingGenre() != null && videoData.getCustomFields().getAdvertisingGenre().length() > 0){
                advertisingGenre = videoData.getCustomFields().getAdvertisingGenre();
            }

            HashMap<String, String> metadata = new HashMap<String, String>();
            metadata.put("ns_st_pu",videoData.getCustomFields().getProvider());
            metadata.put("ns_st_st",videoData.getCustomFields().getProvider());
            metadata.put("ns_st_pr",videoData.getCustomFields().getShow());
            metadata.put("ns_st_ep",videoData.getName());
            metadata.put("ns_st_sn",videoData.getCustomFields().getSeasonNumber());
            metadata.put("ns_st_en",videoData.getCustomFields().getEpisodeNumber());
            metadata.put("ns_st_ge",advertisingGenre);
            metadata.put("ns_st_ci",videoData.getCustomFields().getGuid());
            metadata.put("ns_st_ia",videoData.isLongform()?"1":"0");
            metadata.put("ns_st_ce",videoData.isLongform()?"1":"0");
            metadata.put("ns_st_ddt",publishedAt);
            metadata.put("ns_st_tdt",publishedAt);
            metadata.put("ns_st_cl",String.valueOf(videoData.getDuration()));
            metadata.put("c3","NBCUniversal Watchback Rewards");
            metadata.put("c4","*null");
            metadata.put("c6",videoData.getCustomFields().getShow());

            stremingAnalytics.playVideoContentPart(metadata,videoData.isLongform()? ContentType.LONG_FORM_ON_DEMAND:ContentType.SHORT_FORM_ON_DEMAND);
            if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "stremingAnalytics playVideoContentPart " + metadata);


        }catch (Exception e){}



    }
    void onVideoPause(){
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "Playback paused.");
        if(_heartbeat != null)_heartbeat.trackPause();
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackPause()");

        stremingAnalytics.stop();
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "stremingAnalytics stop onVideoPause");
    }
    void onSeekStart(){
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "Seek started.");
        if(_heartbeat != null)_heartbeat.trackEvent(MediaHeartbeat.Event.SeekStart, null, null);
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackEvent(MediaHeartbeat.Event.SeekStart)");
    }
    void onSeekComplete(){
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "Seek completed.");
        if(_heartbeat != null)_heartbeat.trackEvent(MediaHeartbeat.Event.SeekComplete, null, null);
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackEvent(MediaHeartbeat.Event.SeekComplete)");
    }
    void onBufferStart(){
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "Buffer started.");
        if(_heartbeat != null)_heartbeat.trackEvent(MediaHeartbeat.Event.BufferStart, null, null);
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackEvent(MediaHeartbeat.Event.BufferStart)");
    }
    void onBufferCompete(){
        if(_heartbeat != null)_heartbeat.trackEvent(MediaHeartbeat.Event.BufferComplete, null, null);
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackEvent(MediaHeartbeat.Event.BufferComplete)");
    }

    void onChapterStart(BrightcovePlaylistData.BrightcoveVideo videoData){

        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "Chapter started.");

        HashMap<String, String> chapterMetadata = new HashMap<String, String>();
        chapterMetadata.put("segmentType", "Sample Segment Type");

        // Chapter Info
        String title = videoData.getName();
        long duration = videoData.getDuration();
        Double length = duration/1000.0;

        //Map<String, Object> chapterData = new HashMap<>();
        String chapterName = title;
        Long chapterPosition = Long.valueOf(1);
        Double chapterLength = length;
        Double chapterStartTime = getCurrentPlaybackTime();

        MediaObject chapterDataInfo = MediaHeartbeat.createChapterObject(chapterName, chapterPosition, chapterLength, chapterStartTime);

        if(_heartbeat != null)_heartbeat.trackEvent(MediaHeartbeat.Event.ChapterStart, chapterDataInfo, chapterMetadata);
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackEvent(MediaHeartbeat.Event.ChapterStart)");

    }
    void onChapterCompete(){
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "Chapter completed.");
        if(_heartbeat != null)_heartbeat.trackEvent(MediaHeartbeat.Event.ChapterComplete, null, null);
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackEvent(MediaHeartbeat.Event.ChapterComplete)");
    }
    void onAdStart( HashMap<String, Object> adData){
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "Ad started.");

        // Ad Break Info

        String contentType = "midroll";
        if (adData.get("contentType") != null){
            contentType = String.valueOf(adData.get("contentType"));
        }

        // Ad Info

        String adName = "unknown";
        if (adData.get("name") != null){
            adName = String.valueOf(adData.get("name"));
        }
        String adId= "unknown";
        if (adData.get("adId") != null){
            adId = String.valueOf(adData.get("adId"));
        }
        String adPosition = "1";
        if (adData.get("position") != null){
            adPosition = String.valueOf(adData.get("position"));
        }
        String adLength = "-1";
        if (adData.get("length") != null){
            adLength = String.valueOf(adData.get("length"));
        }

        MediaObject adInfo = MediaHeartbeat.createAdObject(adName, adId, Long.valueOf(adPosition), Double.valueOf(adLength));

        ////

        HashMap<String, String> adMetadata = new HashMap<String, String>();
        adMetadata.put("assetid", adName);
        adMetadata.put("type", contentType);
        adMetadata.put("length", adLength);


        // nielsen ad metadata
        Map<String, Object> adMetadataInfo = NielsenMetadata.adMetadata != null ? NielsenMetadata.adMetadata : new HashMap<String, Object>();
        adInfo.setValue(MediaHeartbeat.MediaObjectKey.NielsenAdMetadata, adMetadataInfo);


        if(_heartbeat != null)_heartbeat.trackEvent(MediaHeartbeat.Event.AdStart, adInfo, adMetadata);

        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackEvent(MediaHeartbeat.Event.AdStart)");

        try{

            HashMap<String, String> metadata = new HashMap<String, String>();
            metadata.put("ns_st_cl",String.valueOf(adLength));
            stremingAnalytics.playVideoAdvertisement(metadata, AdType.LINEAR_ON_DEMAND_PRE_ROLL);
            if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "stremingAnalytics playVideoAdvertisement " + metadata);

        }catch (Exception e){}
    }
    void onAdComplete(){
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "Ad completed.");
        if(_heartbeat != null)_heartbeat.trackEvent(MediaHeartbeat.Event.AdComplete, null, null);
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackEvent(MediaHeartbeat.Event.AdComplete)");
        stremingAnalytics.stop();
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "stremingAnalytics stop onAdComplete");
    }
    void onAdBreakStart( HashMap<String, Object> adBreakData){
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "Ad started.");

        // Ad Break Info

        String name = "midroll";
        if (adBreakData.get("name") != null){
            name = String.valueOf(adBreakData.get("name"));
        }
        String position =  "1";
        if (adBreakData.get("position") != null){
            position = String.valueOf(adBreakData.get("position"));
        }
        String startTime = "1";
        if (adBreakData.get("startTime") != null){
            startTime = String.valueOf(adBreakData.get("startTime"));
        }
        MediaObject adBreakInfo = MediaHeartbeat.createAdBreakObject(name, Long.valueOf(position), Double.valueOf(startTime));
        // Ad Info
        if(_heartbeat != null) _heartbeat.trackEvent(MediaHeartbeat.Event.AdBreakStart, adBreakInfo, null);
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackEvent(MediaHeartbeat.Event.AdBreakStart)");

    }
    void onAdBreakComplete(){
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "AdBreak completed.");
        if(_heartbeat != null) _heartbeat.trackEvent(MediaHeartbeat.Event.AdBreakComplete, null, null);
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackEvent(MediaHeartbeat.Event.AdBreakComplete)");
    }
    void onAdPause(){
        stremingAnalytics.stop();
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "stremingAnalytics stop onAdPause");
    }
    void onError(String errorid){
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "onError");
        if(_heartbeat != null)_heartbeat.trackError(errorid);
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackError");
        stremingAnalytics.stop();
        if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "stremingAnalytics stop onError");
    }
    void trackTimedMetaData(String id3Tag){
       if (id3Tag.startsWith("www.nielsen.com")){
           MediaObject id3 = MediaHeartbeat.createTimedMetadataObject(id3Tag);
           if(_heartbeat != null)_heartbeat.trackEvent(MediaHeartbeat.Event.TimedMetadataUpdate,id3,null);
           if(AppConstants.kWatchbackDebug) Log.d(LOG_TAG, "_heartbeat.trackEvent(MediaHeartbeat.Event.TimedMetadataUpdate)");
       }

    }
    void onChangedVideo(){
       if (mbIsSessionValid)
           onVideoUnLoad();
    }
}
