package com.watchback2.android.api;

import android.text.TextUtils;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.watchback2.android.BR;
import com.watchback2.android.models.Images;
import com.watchback2.android.models.Link;
import com.watchback2.android.models.Schedule;
import com.watchback2.android.models.channels.ChannelsEntity;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;


/**
 * Created by Nilesh on 11/25/16.
 */

public class BrightcovePlaylistData implements Serializable {

    @SerializedName("videos")
    @Expose
    private List<BrightcoveVideo> videos = null;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("reference_id")
    @Expose
    private Object referenceId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("description")
    @Expose
    private Object description;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("account_id")
    @Expose
    private String accountId;
    private final static long serialVersionUID = -6161094446938232983L;


    public List<BrightcoveVideo> getVideos() {
        return videos;
    }

    public void setVideos(List<BrightcoveVideo> videos) {
        this.videos = videos;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Object referenceId) {
        this.referenceId = referenceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("videos", videos).append("updatedAt",
                updatedAt).append("type", type).append("referenceId", referenceId).append("name",
                name).append("id", id).append("description", description).append("createdAt",
                createdAt).append("accountId", accountId).toString();
    }

    public static class BrightcoveVideo extends BaseObservable implements Serializable {

        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("poster_sources")
        @Expose
        private List<PosterSource> posterSources = null;
        @SerializedName("tags")
        @Expose
        private List<String> tags = null;
        @SerializedName("cue_points")
        @Expose
        private List<Object> cuePoints = null;
        @SerializedName("custom_fields")
        @Expose
        private CustomFields customFields;
        @SerializedName("account_id")
        @Expose
        private String accountId;
        @SerializedName("sources")
        @Expose
        private List<Source> sources = null;
        @SerializedName("name")
        @Expose
        private String name;

        @SerializedName("reference_id")
        @Expose
        private Object referenceId;
        @SerializedName("long_description")
        @Expose
        private String longDescription;
        @SerializedName("duration")
        @Expose
        private long duration;
        @SerializedName("economics")
        @Expose
        private String economics;
        @SerializedName("published_at")
        @Expose
        private String publishedAt;
        @SerializedName("text_tracks")
        @Expose
        private List<Object> textTracks = null;
        @SerializedName("updated_at")
        @Expose
        private String updatedAt;
        @SerializedName("thumbnail")
        @Expose
        private String thumbnail;
        @SerializedName("poster")
        @Expose
        private String poster;
        @SerializedName("offline_enabled")
        @Expose
        private boolean offlineEnabled;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("ad_keys")
        @Expose
        private Object adKeys;
        @SerializedName("thumbnail_sources")
        @Expose
        private List<ThumbnailSource> thumbnailSources = null;
        @SerializedName("created_at")
        @Expose
        private String createdAt;
        @SerializedName("clip_source_video_id")
        @Expose
        private Object clipSourceVideoId;
        @SerializedName("complete")
        @Expose
        private boolean complete;
        @SerializedName("delivery_type")
        @Expose
        private String deliveryType;
        @SerializedName("digital_master_id")
        @Expose
        private Object digitalMasterId;
        @SerializedName("folder_id")
        @Expose
        private Object folderId;
        @SerializedName("geo")
        @Expose
        private Object geo;
        @SerializedName("has_digital_master")
        @Expose
        private boolean hasDigitalMaster;
        @SerializedName("images")
        @Expose
        private Images images;
        @SerializedName("link")
        @Expose
        private Link link;
        @SerializedName("original_filename")
        @Expose
        private String originalFilename;
        @SerializedName("projection")
        @Expose
        private Object projection;
        @SerializedName("schedule")
        @Expose
        private Schedule schedule;
        @SerializedName("sharing")
        @Expose
        private Object sharing;
        @SerializedName("state")
        @Expose
        private String state;
        /**
         * type will be either of 'longform', 'shortform' OR 'url'
         * actual-value when this is set to 'url' will be available in {@link #value} & the
         * corresponding channel/provider image would be available in {@link #imageUrl}
         */
        @SerializedName("type")
        @Expose
        private String type;
        /**
         * See {@link #type} for description
         */
        @SerializedName("value")
        @Expose
        private String value;
        /**
         * See {@link #type} for description
         */
        @SerializedName("image_url")
        @Expose
        private String imageUrl;
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
        @SerializedName("channel")
        @Expose
        private ChannelsEntity channel;

        private String parentListTitle;

        private boolean isFirstVideo = false;

        private final static long serialVersionUID = 5L;

        public boolean isLongform() {
            //return !TextUtils.isEmpty(getType()) && "longform".equalsIgnoreCase(getType());

            // As discussed with Nilesh, we check for longform/shortform only from the
            // custom-fields & not from the 'type'
            CustomFields custFields = getCustomFields();
            return custFields != null && Boolean.parseBoolean(custFields.getLongform());
        }

        public boolean isUrl() {
            return !TextUtils.isEmpty(getType()) && "url".equalsIgnoreCase(getType());
        }

        public boolean isShortform() {
            //return !TextUtils.isEmpty(getType()) && "shortform".equalsIgnoreCase(getType());

            return !isLongform();
        }

        public String getProviderName() {
            String provider = StringUtils.EMPTY;

            if (getCustomFields() != null) {
                provider = getCustomFields().getProvider();
            }

            return provider;
        }

        /**
         * For use with non-featured-items: -returns the URL for the
         * image which should be shown on UI for this video-item
         */
        public String getVideoPoster() {
            if (isUrl()) {
                return getImageUrl();
            }

            // fallback to imageUrl if thumbnail / poster unavailable:
            return !TextUtils.isEmpty(getThumbnail()) ? getThumbnail()
                    : (getImages() != null && getImages().getPoster() != null && !TextUtils.isEmpty(
                            getImages().getPoster().getSrc()) ? getImages().getPoster().getSrc()
                            : (!TextUtils.isEmpty(getPoster()) ? getPoster() : getImageUrl()));
        }

        /**
         * For use with the featured-items:
         */
        public String getFeaturedVideoPoster() {
            // fallback to getVideoPoster() if 'poster' unavailable:
            return !TextUtils.isEmpty(getPoster()) ? getPoster() : getVideoPoster();
        }

        public String getDescription() {
            if (StringUtils.equalsIgnoreCase(description, "unknown")) {
                return "";
            }
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<PosterSource> getPosterSources() {
            return posterSources;
        }

        public void setPosterSources(List<PosterSource> posterSources) {
            this.posterSources = posterSources;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public List<Object> getCuePoints() {
            return cuePoints;
        }

        public void setCuePoints(List<Object> cuePoints) {
            this.cuePoints = cuePoints;
        }

        public CustomFields getCustomFields() {
            return customFields;
        }

        public void setCustomFields(CustomFields customFields) {
            this.customFields = customFields;
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public List<Source> getSources() {
            return sources;
        }

        public void setSources(List<Source> sources) {
            this.sources = sources;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getReferenceId() {
            return referenceId;
        }

        public void setReferenceId(Object referenceId) {
            this.referenceId = referenceId;
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

        public List<Object> getTextTracks() {
            return textTracks;
        }

        public void setTextTracks(List<Object> textTracks) {
            this.textTracks = textTracks;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        public String getPoster() {
            return poster;
        }

        public void setPoster(String poster) {
            this.poster = poster;
        }

        public boolean isOfflineEnabled() {
            return offlineEnabled;
        }

        public void setOfflineEnabled(boolean offlineEnabled) {
            this.offlineEnabled = offlineEnabled;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Object getAdKeys() {
            return adKeys;
        }

        public void setAdKeys(Object adKeys) {
            this.adKeys = adKeys;
        }

        public List<ThumbnailSource> getThumbnailSources() {
            return thumbnailSources;
        }

        public void setThumbnailSources(List<ThumbnailSource> thumbnailSources) {
            this.thumbnailSources = thumbnailSources;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getDeliveryType() {
            return deliveryType;
        }

        public void setDeliveryType(String deliveryType) {
            this.deliveryType = deliveryType;
        }

        public Object getDigitalMasterId() {
            return digitalMasterId;
        }

        public void setDigitalMasterId(Object digitalMasterId) {
            this.digitalMasterId = digitalMasterId;
        }

        public Object getFolderId() {
            return folderId;
        }

        public void setFolderId(Object folderId) {
            this.folderId = folderId;
        }

        public Object getGeo() {
            return geo;
        }

        public void setGeo(Object geo) {
            this.geo = geo;
        }

        public boolean isHasDigitalMaster() {
            return hasDigitalMaster;
        }

        public void setHasDigitalMaster(boolean hasDigitalMaster) {
            this.hasDigitalMaster = hasDigitalMaster;
        }

        public Images getImages() {
            return images;
        }

        public void setImages(Images images) {
            this.images = images;
        }

        public Link getLink() {
            return link;
        }

        public void setLink(Link link) {
            this.link = link;
        }

        public String getOriginalFilename() {
            return originalFilename;
        }

        public void setOriginalFilename(String originalFilename) {
            this.originalFilename = originalFilename;
        }

        public Object getProjection() {
            return projection;
        }

        public void setProjection(Object projection) {
            this.projection = projection;
        }

        public Schedule getSchedule() {
            return schedule;
        }

        public void setSchedule(Schedule schedule) {
            this.schedule = schedule;
        }

        public Object getSharing() {
            return sharing;
        }

        public void setSharing(Object sharing) {
            this.sharing = sharing;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
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

            if(getChannel() != null){
                imageUrl = getChannel().getmDetailsScreenLogoUrl();
            }
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
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

        public boolean getHasGoldBorder() {
            return true/*hasGoldBorder == 1*/;
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

        public boolean hasValidBottomLeftIcon() {
            return !TextUtils.isEmpty(getBottomLeftIconUrl());
        }

        @Bindable
        public boolean getWatched() {
            return watched;
        }

        public void setWatched(boolean watched) {
            this.watched = watched;
            notifyPropertyChanged(BR.watched);
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }


        public ChannelsEntity getChannel() {
            return channel;
        }

        public String getParentListTitle() {
            return parentListTitle;
        }

        public void setParentListTitle(String parentListTitle) {
            this.parentListTitle = parentListTitle;
        }

        public boolean isFirstVideo() {
            return isFirstVideo;
        }

        public void setFirstVideo(boolean firstVideo) {
            isFirstVideo = firstVideo;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("description", description).append(
                    "posterSources", posterSources).append("tags", tags).append("cuePoints",
                    cuePoints).append("customFields", customFields).append("accountId",
                    accountId).append("sources", sources).append("name", name).append("referenceId",
                    referenceId).append("longDescription", longDescription).append("duration",
                    duration).append("economics", economics).append("publishedAt",
                    publishedAt).append("textTracks", textTracks).append("updatedAt",
                    updatedAt).append("thumbnail", thumbnail).append("poster", poster).append(
                    "offlineEnabled", offlineEnabled).append("id", id).append("adKeys",
                    adKeys).append("thumbnailSources", thumbnailSources).append("createdAt",
                    createdAt).append("clipSourceVideoId", clipSourceVideoId).append("deliveryType",
                    deliveryType).append("digitalMasterId", digitalMasterId).append("folderId",
                    folderId).append("geo", geo).append("hasDigitalMaster",
                    hasDigitalMaster).append("images", images).append("link", link).append(
                    "originalFilename", originalFilename).append("projection", projection).append(
                    "schedule", schedule).append("sharing", sharing).append("state", state).append(
                    "type", type).append("value", value).append("imageUrl", imageUrl).append(
                    "videoId", videoId).append("bcPoster", bcPoster).append("hasGoldBorder",
                    hasGoldBorder).append("bottomLeftIconUrl", bottomLeftIconUrl).append("watched",
                    watched).append("uuid", uuid).toString();
        }
    }


    public static class CustomFields implements Serializable {

        @SerializedName("test_id")
        @Expose
        private String testId;
        @SerializedName("perk_reward_uuid")
        @Expose
        private String perkRewardUuid;
        @SerializedName("perk_reward")
        @Expose
        private String perkReward;
        @SerializedName("watch_cap")
        @Expose
        private String watchCap;
        @SerializedName("disable_seek")
        @Expose
        private String disableSeek;
        @SerializedName("longform")
        @Expose
        private String longform;
        @SerializedName("provider")
        @Expose
        private String provider;
        @SerializedName("show")
        @Expose
        private String show;

        @SerializedName("episode_number")
        @Expose
        private String episodeNumber;

        @SerializedName("season_number")
        @Expose
        private String seasonNumber;

        @SerializedName("advertisingGenre")
        @Expose
        private String advertisingGenre;

        @SerializedName("programmingType")
        @Expose
        private String programmingType;


        @SerializedName("guid")
        @Expose
        private String guid;

        @SerializedName("gracenote_ep_id")
        @Expose
        private String gracenoteEpId;

        @SerializedName("externaladvertiserid")
        @Expose
        private String externaladvertiserid;


        private final static long serialVersionUID = 2L;

        public String getTestId() {
            return testId;
        }

        public void setTestId(String testId) {
            this.testId = testId;
        }

        public String getPerkReward() {
            return perkReward;
        }

        public void setPerkReward(String perkReward) {
            this.perkReward = perkReward;
        }

        public String getPerkRewardUuid() {
            return perkRewardUuid;
        }

        public void setPerkRewardUuid(String perkRewardUuid) {
            this.perkRewardUuid = perkRewardUuid;
        }

        public String getWatchCap() {
            return watchCap;
        }

        public void setWatchCap(String watchCap) {
            this.watchCap = watchCap;
        }

        public String getDisableSeek() {
            return disableSeek;
        }

        public void setDisableSeek(String disableSeek) {
            this.disableSeek = disableSeek;
        }

        public String getLongform() {
            return longform;
        }

        public void setLongform(String longform) {
            this.longform = longform;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getShow() {
            return show;
        }

        public void setShow(String show) {
            this.show = show;
        }

        public String getEpisodeNumber() {
            return episodeNumber;
        }

        public void setEpisodeNumber(String episodeNumber) {
            this.episodeNumber = episodeNumber;
        }

        public String getSeasonNumber() {
            return seasonNumber;
        }

        public void setSeasonNumber(String seasonNumber) {
            this.seasonNumber = seasonNumber;
        }

        public String getAdvertisingGenre() {
            return advertisingGenre;
        }

        public void setAdvertisingGenre(String advertisingGenre) {
            this.advertisingGenre = advertisingGenre;
        }


        public String getProgrammingType() {
            return programmingType;
        }

        public void setProgrammingType(String programmingType) {
            this.programmingType = programmingType;
        }

        public String getGuid() {
            return guid;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public String getGracenoteEpId() {
            return gracenoteEpId;
        }

        public void setGracenoteEpId(String gracenoteEpId) {
            this.gracenoteEpId = gracenoteEpId;
        }

        public String getExternaladvertiserid() {
            return externaladvertiserid;
        }

        public void setExternaladvertiserid(String externaladvertiserid) {
            this.externaladvertiserid = externaladvertiserid;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("perkRewardUuid", perkRewardUuid).append(
                    "perkReward", perkReward).append("watchCap", watchCap).append("disableSeek",
                    disableSeek).append("longform", longform).append("show", show).append(
                    "provider", provider).toString();
        }

    }

}
