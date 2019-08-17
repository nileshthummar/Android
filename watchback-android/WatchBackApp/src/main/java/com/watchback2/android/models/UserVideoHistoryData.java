package com.watchback2.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.request.model.Data;
import com.watchback2.android.api.BrightcovePlaylistData;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * Common model for User video-watched history and for videos-list for a Channel
 */
public class UserVideoHistoryData extends Data {

    @SerializedName("videos")
    @Expose
    private List<BrightcovePlaylistData.BrightcoveVideo> videos = null;
    private final static long serialVersionUID = 2L;

    public List<BrightcovePlaylistData.BrightcoveVideo> getVideos() {
        return videos;
    }

    public void setVideos(List<BrightcovePlaylistData.BrightcoveVideo> videos) {
        this.videos = videos;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("videos", videos).toString();
    }
}