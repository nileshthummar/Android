package com.watchback2.android.models;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.watchback2.android.api.BrightcovePlaylistData;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * Objective:: This acts as a wrapper class for channel details returned from server.
 * Value here is set manually on a successfully api response (ref: ChannelsManager).
 * Object of this class is passed around in the app (as opposed to ChannelsEntity class)
 * This decouples and protects the data from future api changes.
 * <p>
 * Favorite, name, uuid, logoImageUrl and Videos data can be set separately.
 */
public class Channel implements Serializable {

    //-----------------------------------------------------------------------
    // private final static constant
    //-----------------------------------------------------------------------
    private final static long serialVersionUID = 2L;

    //-----------------------------------------------------------------------
    // private final variables
    //-----------------------------------------------------------------------
    private final List<Genre> mGenres;
    private final String mAndroidDestinationUrl;
    private final String mDefaultDestinationUrl;
    private final String mDetailsScreenLogoUrl;
    private final String mListScreenLogoUrl;
    private final String tag;
    private final String description;
    private final String shortDescription;
    private final String backgroundImageUrl;
    private final String buttonText;
    private final String destinationUrl;
    private final boolean dummyChannelWithNoData;

    //-----------------------------------------------------------------------
    // private variables. These have setter methods.
    //-----------------------------------------------------------------------
    private String name;
    private String uuid;
    private String logoImageUrl;
    private boolean mFavorite;
    private List<BrightcovePlaylistData.BrightcoveVideo> videos = null;

    //-----------------------------------------------------------------------
    // public constructors
    //-----------------------------------------------------------------------

    public Channel(boolean mFavorite, List<Genre> mGenres, String mAndroidDestinationUrl, String mDefaultDestinationUrl,
                   String mDestinationUrl, String mButtonText, String mBackgroundImageUrl, String mDetailsScreenLogoUrl,
                   String mListScreenLogoUrl, String mShortDescription, String mDescription, String mTag, String mName,
                   String mUuid, @Nullable List<BrightcovePlaylistData.BrightcoveVideo> videos) {
        this.mFavorite = mFavorite;
        this.mGenres = mGenres;
        this.mAndroidDestinationUrl = mAndroidDestinationUrl;
        this.mDefaultDestinationUrl = mDefaultDestinationUrl;
        this.destinationUrl = mDestinationUrl;
        this.buttonText = mButtonText;
        this.backgroundImageUrl = mBackgroundImageUrl;
        this.mDetailsScreenLogoUrl = mDetailsScreenLogoUrl;
        this.mListScreenLogoUrl = mListScreenLogoUrl;
        this.shortDescription = mShortDescription;
        this.description = mDescription;
        this.tag = mTag;
        this.name = mName;
        this.uuid = mUuid;
        this.videos = videos;
        this.dummyChannelWithNoData = false;
    }

    /**
     * Constructor to create a dummy channel with no data
     * Data can be set for variables with setter methods
     *
     * @param dummyChannelWithNoData Indicates if channel is a dummy.
     *                               Serves no purpose other than let anybody trying to access this know that
     *                               There is another valid constructor to create a valid channel with data.
     */
    public Channel(boolean dummyChannelWithNoData) {
        this.mFavorite = false;
        this.mGenres = null;
        this.mAndroidDestinationUrl = "";
        this.mDefaultDestinationUrl = "";
        this.destinationUrl = "";
        this.buttonText = "";
        this.backgroundImageUrl = "";
        this.mDetailsScreenLogoUrl = "";
        this.mListScreenLogoUrl = "";
        this.shortDescription = "";
        this.description = "";
        this.tag = "";
        this.name = "";
        this.uuid = "";
        this.videos = null;
        // Irrespective of what value is passed, this is "true" since other variables lack valid data.
        this.dummyChannelWithNoData = true;
    }

    //-----------------------------------------------------------------------
    // Getter methods
    //-----------------------------------------------------------------------
    public String getUuid() {
        return uuid;
    }

    public String getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLogoImageUrl() {
        logoImageUrl = "";
        //rectangle image
        if (!TextUtils.isEmpty(getmListScreenLogoUrl())) {
            logoImageUrl = getmListScreenLogoUrl();
            //square image
        } else if (!TextUtils.isEmpty(getmDetailsScreenLogoUrl())) {
            logoImageUrl = getmDetailsScreenLogoUrl();
        }
        return logoImageUrl;
    }

    public String getBrandDetailsScreenLogo(){

        String logoURl = "";
        //square image
        if (!TextUtils.isEmpty(getmDetailsScreenLogoUrl())) {
            logoURl = getmDetailsScreenLogoUrl();
            //rectangle image
        }else if (!TextUtils.isEmpty(getmListScreenLogoUrl())) {
            logoURl = getmListScreenLogoUrl();

        }
        return logoURl;
    }

    public String getBackgroundImageUrl() {
        return backgroundImageUrl;
    }

    public String getButtonText() {
        return buttonText;
    }

    public String getDestinationUrl() {
        return TextUtils.isEmpty(destinationUrl) ? "" : destinationUrl;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public List<BrightcovePlaylistData.BrightcoveVideo> getVideos() {
        return videos;
    }

    public boolean isFavorite() {
        return mFavorite;
    }

    public List<Genre> getmGenres() {
        return mGenres;
    }

    public String getmAndroidDestinationUrl() {
        return mAndroidDestinationUrl;
    }

    public String getmDefaultDestinationUrl() {
        return mDefaultDestinationUrl;
    }

    public String getmDetailsScreenLogoUrl() {
        return mDetailsScreenLogoUrl;
    }

    public String getmListScreenLogoUrl() {
        return mListScreenLogoUrl;
    }

    public boolean isDummyChannelWithNoData() {
        return dummyChannelWithNoData;
    }

    //-----------------------------------------------------------------------
    // Setter methods
    //-----------------------------------------------------------------------
    public void setFavorite(boolean mFavorite) {
        this.mFavorite = mFavorite;
    }

    public void setVideos(
            List<BrightcovePlaylistData.BrightcoveVideo> videos) {
        this.videos = videos;
    }

    public void setLogoImageUrl(String logoImageUrl) {
        this.logoImageUrl = logoImageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("uuid", uuid)
                .append("tag", tag)
                .append("name", name)
                .append("description", description)
                .append("shortDescription", shortDescription)
                .append("logoImageUrl", logoImageUrl)
                .append("backgroundImageUrl", backgroundImageUrl)
                .append("buttonText", buttonText)
                .append("destinationUrl", destinationUrl)
                .append("videos", videos)
                .append("mListScreenLogoUrl", mListScreenLogoUrl)
                .append("mDetailsScreenLogoUrl", mDetailsScreenLogoUrl)
                .append("shortDescription", shortDescription)
                .append("mDefaultDestinationUrl", mDefaultDestinationUrl)
                .append("mAndroidDestinationUrl", mAndroidDestinationUrl)
                .append("mFavorite", mFavorite)
                .append("logoImageUrl", logoImageUrl)
                .append("mGenres", mGenres).toString();
    }
}