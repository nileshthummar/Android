package com.watchback2.android.models.movietickets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.watchback2.android.models.channels.ChannelsEntity;

import java.util.List;

public class VideosEntity {
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
    private ScheduleEntity mSchedule;
    @Expose
    @SerializedName("duration")
    private String mDuration;
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
    @Expose
    @SerializedName("channel")
    private ChannelsEntity channelsEntity;

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

    public ScheduleEntity getSchedule() {
        return mSchedule;
    }

    public String getDuration() {
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

    public ChannelsEntity getChannelsEntity() {
        return channelsEntity;
    }
}
