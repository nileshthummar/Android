package com.watchback2.android.models.channels.singlechannel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VideosEntity {
    @Expose
    @SerializedName("watched")
    private boolean mWatched;
    @Expose
    @SerializedName("published_at")
    private String mPublishedAt;
    @Expose
    @SerializedName("created_at")
    private String mCreatedAt;
    @Expose
    @SerializedName("state")
    private String mState;
    @Expose
    @SerializedName("tags")
    private List<String> mTags;
    @Expose
    @SerializedName("custom_fields")
    private CustomFieldsEntity mCustomFields;
    @Expose
    @SerializedName("economics")
    private String mEconomics;
    @Expose
    @SerializedName("schedule")
    private String mSchedule;
    @Expose
    @SerializedName("duration")
    private int mDuration;
    @Expose
    @SerializedName("poster")
    private String mPoster;
    @Expose
    @SerializedName("thumbnail")
    private String mThumbnail;
    @Expose
    @SerializedName("description")
    private String mDescription;
    @Expose
    @SerializedName("name")
    private String mName;
    @Expose
    @SerializedName("id")
    private long mId;

    public boolean getWatched() {
        return mWatched;
    }

    public String getPublishedAt() {
        return mPublishedAt;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public String getState() {
        return mState;
    }

    public List<String> getTags() {
        return mTags;
    }

    public CustomFieldsEntity getCustomFields() {
        return mCustomFields;
    }

    public String getEconomics() {
        return mEconomics;
    }

    public String getSchedule() {
        return mSchedule;
    }

    public int getDuration() {
        return mDuration;
    }

    public String getPoster() {
        return mPoster;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getName() {
        return mName;
    }

    public long getId() {
        return mId;
    }
}
