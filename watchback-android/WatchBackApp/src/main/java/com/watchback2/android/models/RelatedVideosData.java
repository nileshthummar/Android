package com.watchback2.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.request.model.Data;
import com.watchback2.android.api.BrightcovePlaylistData;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * Model for getting related videos for a video, via it's UUID
 */
public class RelatedVideosData extends Data {

    private final static long serialVersionUID = 3L;
    @SerializedName("related")
    @Expose
    private List<BrightcovePlaylistData.BrightcoveVideo> related = null;
    /**
     * Same as BrightcoveVideo.type
     */
    @SerializedName("type")
    @Expose
    private String type;
    /**
     * See {@link #type}
     */
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("image_url")
    @Expose
    private String imageUrl;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("long_description")
    @Expose
    private String longDescription;
    @SerializedName("duration")
    @Expose
    private long duration;
    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;
    @SerializedName("custom_fields")
    @Expose
    private BrightcovePlaylistData.CustomFields customFields;
    @SerializedName("economics")
    @Expose
    private String economics;
    @SerializedName("published_at")
    @Expose
    private String publishedAt;
    @SerializedName("poster")
    @Expose
    private String poster;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("video_id")
    @Expose
    private long videoId;
    @SerializedName("bc_poster")
    @Expose
    private String bcPoster;
    @SerializedName("has_gold_border")
    @Expose
    private int hasGoldBorder;
    @SerializedName("bottom_left_icon_url")
    @Expose
    private String bottomLeftIconUrl;
    @SerializedName("watched")
    @Expose
    private boolean watched;
    @SerializedName("uuid")
    @Expose
    private String uuid;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("related", related).append("type", type).append(
                "value", value).append("imageUrl", imageUrl).append("name", name).append(
                "description", description).append("longDescription", longDescription).append(
                "duration", duration).append("thumbnail", thumbnail).append("poster",
                poster).append("bcPoster", bcPoster).append("hasGoldBorder", hasGoldBorder).append(
                "bottomLeftIconUrl", bottomLeftIconUrl).append("uuid", uuid).append("id",
                id).append("videoId", videoId).append("economics", economics).append("customFields",
                customFields).append("publishedAt", publishedAt).append("watched",
                watched).toString();
    }

    public BrightcovePlaylistData.BrightcoveVideo createBrightcoveVideoInstance() {
        BrightcovePlaylistData.BrightcoveVideo video = new BrightcovePlaylistData.BrightcoveVideo();
        video.setType(getType());
        video.setValue(getValue());
        video.setImageUrl(getImageUrl());
        video.setName(getName());
        video.setDescription(getDescription());
        video.setLongDescription(getLongDescription());
        video.setDuration(getDuration());
        video.setThumbnail(getThumbnail());
        video.setPoster(getPoster());
        video.setBcPoster(getBcPoster());
        video.setHasGoldBorder(getHasGoldBorder());
        video.setBottomLeftIconUrl(getBottomLeftIconUrl());
        video.setUuid(getUuid());
        video.setId(getId());
        video.setVideoId(getVideoId());
        video.setEconomics(getEconomics());
        video.setCustomFields(getCustomFields());
        video.setPublishedAt(getPublishedAt());
        video.setWatched(getWatched());
        return video;
    }

    public List<BrightcovePlaylistData.BrightcoveVideo> getRelated() {
        return related;
    }

    public void setRelated(
            List<BrightcovePlaylistData.BrightcoveVideo> related) {
        this.related = related;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public BrightcovePlaylistData.CustomFields getCustomFields() {
        return customFields;
    }

    public void setCustomFields(BrightcovePlaylistData.CustomFields customFields) {
        this.customFields = customFields;
    }

    public String getEconomics() {
        return economics;
    }

    public void setEconomics(String economics) {
        this.economics = economics;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getVideoId() {
        return videoId;
    }

    public void setVideoId(long videoId) {
        this.videoId = videoId;
    }

    public String getBcPoster() {
        return bcPoster;
    }

    public void setBcPoster(String bcPoster) {
        this.bcPoster = bcPoster;
    }

    public int getHasGoldBorder() {
        return hasGoldBorder;
    }

    public void setHasGoldBorder(int hasGoldBorder) {
        this.hasGoldBorder = hasGoldBorder;
    }

    public String getBottomLeftIconUrl() {
        return bottomLeftIconUrl;
    }

    public void setBottomLeftIconUrl(String bottomLeftIconUrl) {
        this.bottomLeftIconUrl = bottomLeftIconUrl;
    }

    public boolean getWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}