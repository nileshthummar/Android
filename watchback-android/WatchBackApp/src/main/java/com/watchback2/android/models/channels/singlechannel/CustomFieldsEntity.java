package com.watchback2.android.models.channels.singlechannel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CustomFieldsEntity {
    @Expose
    @SerializedName("provider")
    private String mProvider;
    @Expose
    @SerializedName("season_number")
    private String mSeasonNumber;
    @Expose
    @SerializedName("guid")
    private String mGuid;
    @Expose
    @SerializedName("episode_number")
    private String mEpisodeNumber;
    @Expose
    @SerializedName("gracenote_ep_id")
    private String mGracenoteEpId;
    @Expose
    @SerializedName("show")
    private String mShow;
    @Expose
    @SerializedName("advertising_genre")
    private String mAdvertisingGenre;
    @Expose
    @SerializedName("externaladvertiserid")
    private String mExternaladvertiserid;

    public String getProvider() {
        return mProvider;
    }

    public String getSeasonNumber() {
        return mSeasonNumber;
    }

    public String getGuid() {
        return mGuid;
    }

    public String getEpisodeNumber() {
        return mEpisodeNumber;
    }

    public String getGracenoteEpId() {
        return mGracenoteEpId;
    }

    public String getShow() {
        return mShow;
    }

    public String getAdvertisingGenre() {
        return mAdvertisingGenre;
    }

    public String getExternaladvertiserid() {
        return mExternaladvertiserid;
    }
}
