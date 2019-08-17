package com.watchback2.android.models.channels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.watchback2.android.models.channels.singlechannel.VideosEntity;

import java.io.Serializable;
import java.util.List;

public class ChannelsEntity implements Serializable {
    @Expose
    @SerializedName("videos")
    private List<VideosEntity> mVideos;
    @Expose
    @SerializedName("link")
    private String mLink;
    @Expose
    @SerializedName("favorite")
    private boolean mFavorite;
    @Expose
    @SerializedName("genres")
    private List<GenresEntity> mGenres;
    @Expose
    @SerializedName("amazon_destination_url")
    private String mAmazonDestinationUrl;
    @Expose
    @SerializedName("android_destination_url")
    private String mAndroidDestinationUrl;
    @Expose
    @SerializedName("ios_destination_url")
    private String mIosDestinationUrl;
    @Expose
    @SerializedName("default_destination_url")
    private String mDefaultDestinationUrl;
    @Expose
    @SerializedName("destination_url")
    private String mDestinationUrl;
    @Expose
    @SerializedName("button_text")
    private String mButtonText;
    @Expose
    @SerializedName("background_image_url")
    private String mBackgroundImageUrl;
    @Expose
    @SerializedName("details_screen_logo_url")
    private String mDetailsScreenLogoUrl;
    @Expose
    @SerializedName("list_screen_logo_url")
    private String mListScreenLogoUrl;
    @Expose
    @SerializedName("short_description")
    private String mShortDescription;
    @Expose
    @SerializedName("description")
    private String mDescription;
    @Expose
    @SerializedName("tag")
    private String mTag;
    @Expose
    @SerializedName("name")
    private String mName;
    @Expose
    @SerializedName("uuid")
    private String mUuid;

    public List<VideosEntity> getVideos() {
        return mVideos;
    }

    public String getLink() {
        return mLink;
    }

    public boolean getFavorite() {
        return mFavorite;
    }

    public List<GenresEntity> getGenres() {
        return mGenres;
    }

    public String getAmazonDestinationUrl() {
        return mAmazonDestinationUrl;
    }

    public String getAndroidDestinationUrl() {
        return mAndroidDestinationUrl;
    }

    public String getIosDestinationUrl() {
        return mIosDestinationUrl;
    }

    public String getDefaultDestinationUrl() {
        return mDefaultDestinationUrl;
    }

    public String getDestinationUrl() {
        return mDestinationUrl;
    }

    public String getButtonText() {
        return mButtonText;
    }

    public String getBackgroundImageUrl() {
        return mBackgroundImageUrl;
    }

    public String getmDetailsScreenLogoUrl() {
        return mDetailsScreenLogoUrl;
    }

    public String getListScreenLogoUrl() {
        return mListScreenLogoUrl;
    }

    public String getShortDescription() {
        return mShortDescription;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getTag() {
        return mTag;
    }

    public String getName() {
        return mName;
    }

    public String getUuid() {
        return mUuid;
    }
}
