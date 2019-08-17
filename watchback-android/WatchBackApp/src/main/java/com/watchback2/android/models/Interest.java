package com.watchback2.android.models;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.util.PerkLogger;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;

public class Interest implements Serializable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("interest")
    @Expose
    private String interest;
    @SerializedName("selected")
    @Expose
    private Boolean selected;
    @SerializedName("tags")
    @Expose
    private List<Tag> tags = null;
    @SerializedName("videos")
    @Expose
    private Object videos;
    @SerializedName("channels")
    @Expose
    private List<Channel> channels = null;
    private final static long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Object getVideos() {
        return videos;
    }

    public void setVideos(Object videos) {
        this.videos = videos;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    /**
     * Returning a comma-delimited String for all tags related to this Interest, for use with
     * TextUtils.join
     */
    @Override
    public String toString() {
        // Just for logging purposes
        PerkLogger.d(getClass().getSimpleName(), new ToStringBuilder(this).append("id", id).append(
                "interest", interest).append("selected", selected).append("tags", tags).append(
                "videos", videos).toString());

        if (getTags() == null || getTags().isEmpty()) {
            return "";
        }

        return TextUtils.join("\", \"", getTags());
    }
}