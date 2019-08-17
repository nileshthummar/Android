package com.watchback2.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.watchback2.android.api.BrightcovePlaylistData;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;

public class HomeScreenItem implements Serializable {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("playlist_id")
    @Expose
    private String playlistId;
    @SerializedName("channels")
    @Expose
    private List<Channel> channels = null;
    @SerializedName("tag")
    @Expose
    private List<String> tag = null;
    @SerializedName("featured")
    @Expose
    private BrightcovePlaylistData.BrightcoveVideo featured = null;
    @SerializedName("videos")
    @Expose
    private List<BrightcovePlaylistData.BrightcoveVideo> videos = null;

    private final static long serialVersionUID = 6L;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public List<String> getTag() {
        return tag;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public BrightcovePlaylistData.BrightcoveVideo getFeatured() {
        return featured;
    }

    public void setFeatured(BrightcovePlaylistData.BrightcoveVideo featured) {
        this.featured = featured;
    }

    public List<BrightcovePlaylistData.BrightcoveVideo> getVideos() {
        return videos;
    }

    public void setVideos(
            List<BrightcovePlaylistData.BrightcoveVideo> videos) {
        this.videos = videos;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("type", type).append(
                "playlistId", playlistId).append("channels", channels).append("tag", tag).append(
                "featured", featured).append("videos", videos).toString();
    }

}