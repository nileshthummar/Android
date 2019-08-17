package com.watchback2.android.models.movietickets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CustomFieldsEntity {
    @Expose
    @SerializedName("show")
    private String mShow;
    @Expose
    @SerializedName("externaladvertiserid")
    private String mExternaladvertiserid;
    @Expose
    @SerializedName("duration")
    private String mDuration;
    @Expose
    @SerializedName("episode_number")
    private String mEpisodeNumber;
    @Expose
    @SerializedName("advertising_genre")
    private String mAdvertisingGenre;
    @Expose
    @SerializedName("season_number")
    private String mSeasonNumber;
    @Expose
    @SerializedName("gracenote_ep_id")
    private String mGracenoteEpId;
    @Expose
    @SerializedName("longform")
    private String mLongform;
    @Expose
    @SerializedName("disable_seek")
    private String mDisableSeek;
    @Expose
    @SerializedName("provider")
    private String mProvider;
    @Expose
    @SerializedName("guid")
    private String mGuid;

    public String getShow() {
        return mShow;
    }

    public String getExternaladvertiserid() {
        return mExternaladvertiserid;
    }

    public String getDuration() {
        return mDuration;
    }

    public String getEpisodeNumber() {
        return mEpisodeNumber;
    }

    public String getAdvertisingGenre() {
        return mAdvertisingGenre;
    }

    public String getSeasonNumber() {
        return mSeasonNumber;
    }

    public String getGracenoteEpId() {
        return mGracenoteEpId;
    }

    public String getLongform() {
        return mLongform;
    }

    public String getDisableSeek() {
        return mDisableSeek;
    }

    public String getProvider() {
        return mProvider;
    }

    public String getGuid() {
        return mGuid;
    }
}
