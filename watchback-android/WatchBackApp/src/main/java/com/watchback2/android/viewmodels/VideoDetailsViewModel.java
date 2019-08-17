package com.watchback2.android.viewmodels;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.perk.util.PerkLogger;
import com.watchback2.android.models.VideoDetailsModelWrapper;
import com.watchback2.android.utils.PerkPreferencesManager;

public class VideoDetailsViewModel {

    private static final String TAG = "VideoDetailsVM";
    public static final String PLAY = "Play";
    public static final String RESUME = "Resume";

    private final VideoDetailsModelWrapper videoDetailsModelWrapper;

    public VideoDetailsViewModel(VideoDetailsModelWrapper videoDetailsModelWrapper) {
        this.videoDetailsModelWrapper = videoDetailsModelWrapper;
    }

    public String getVideoTitle() {
        return videoDetailsModelWrapper.getTitleText();
    }

    public String getVideoSubTitle() {
        return videoDetailsModelWrapper.getSubTitleText();
    }

    public String getVideoBody() {
        return videoDetailsModelWrapper.getBodyText();
    }

    public String getBackgroundImageUrl() {
        Log.d(TAG, "getBackgroundImageUrl: " + videoDetailsModelWrapper.getCoverImageUrl());
        return videoDetailsModelWrapper.getCoverImageUrl();
    }

    public String getRedirectBtnText() {
        return videoDetailsModelWrapper.getRedirectBrandBtnText();
    }

    public String getRedirectUrl() {
        return videoDetailsModelWrapper.getRedirectBrandUrl();
    }

    public String getChannelIconUrl() {
        return videoDetailsModelWrapper.getIconImageUrl();
    }

    public int getRedirectButtonVisibility() {
        int visibility = View.GONE;
        if (!TextUtils.isEmpty(getRedirectBtnText()) ||
                !TextUtils.isEmpty(getRedirectUrl())) {
            visibility = View.VISIBLE;
        }
        Log.d(TAG, "getRedirectButtonVisibility: " + getRedirectBtnText() + " : " + getRedirectUrl());
        return visibility;
    }

    public String getPlayButtonText() {
        if (getVideoProgress() <= 0) {
            return PLAY;
        } else {
            return RESUME;
        }
    }

    public int getProgressVisibility() {
        int visibility = View.GONE;
        if (getVideoProgress() > 0) {
            visibility = View.VISIBLE;
        }
        return visibility;
    }

    public int getVideoProgress() {
        float progress = 0;
        long timeWatched = PerkPreferencesManager.INSTANCE.getLastPositionForLongformVideo(getVideoID());
        long timeRemainingInMilliSecs = getVideoDuration() - timeWatched;

        if (getVideoDuration() == 0) {
            return 0;
        }

        if (timeRemainingInMilliSecs <= 0) {
            progress = 100;
        } else {
            progress = ((float)(timeWatched * 100) / getVideoDuration());
        }
        PerkLogger.d(TAG, "video progress percentage: "+progress);
        // if progress is greater than 0 but less that 1% set it to 1% for UI.
        return (progress > 0 && progress < 1 )? 1 : (int) progress;
    }

    public String getTimeLeft() {
        String timeLeft = "";
        long timeWatched = PerkPreferencesManager.INSTANCE.getLastPositionForLongformVideo(getVideoID());
        long timeRemainingInMilliSecs = getVideoDuration() - timeWatched;

        if (timeRemainingInMilliSecs <= 0) {
            return "";
        }

        long timeRemainingInSecs = timeRemainingInMilliSecs / 1000;
        long hrs = timeRemainingInSecs / (60 * 60);
        long mins = (timeRemainingInSecs % (60 * 60)) / 60;
        long secs = (timeRemainingInSecs % (60 * 60)) % 60;

        if (hrs > 0) {
            timeLeft = timeLeft + hrs + "h ";
        }

        if (mins > 0) {
            timeLeft = timeLeft + mins + "m ";
        }

        if (secs > 0) {
            timeLeft = timeLeft + secs + "s";
        }

        return timeLeft + " remaining";
    }

    private String getVideoID() {
        return videoDetailsModelWrapper.getVideoID();
    }

    private long getVideoDuration() {
        return videoDetailsModelWrapper.getDuration();
    }


}
