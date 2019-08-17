package com.watchback2.android.models;

import android.text.TextUtils;
import android.text.format.DateUtils;

import com.perk.util.PerkLogger;
import com.watchback2.android.utils.DateTimeUtil;

import java.util.Date;

/**
 * This model holds all data needed by VideoDetailsViewModel
 * (presently from BrightcovePlaylistData and ChannelsManager)
 * <p>
 * Hence ensures VideoDetailsViewModel stays decoupled from api models.
 */
public class VideoDetailsModelWrapper {

    private static final String TAG = "VideoDetailsModel";

    private String titleText;
    private String subTitleText;
    private String bodyText;
    private String iconImageUrl;
    private String redirectBrandUrl;
    private String redirectBrandBtnText;
    private String coverImageUrl;
    private String episodeNumber;
    private String seasonNumber;
    private String videoID;
    private String endsAt;
    private String shortDescription;
    private String longDescription;
    private String providerName;
    private long duration;


    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public String getSubTitleText() {
        subTitleText = "";
        String season = "";
        String episode = "";

        if (!TextUtils.isEmpty(getSeasonNumber()) && !"unknown".equalsIgnoreCase(getSeasonNumber())) {
            season = "S" + getSeasonNumber();
        }

        if (!TextUtils.isEmpty(getEpisodeNumber()) && !"unknown".equalsIgnoreCase(getEpisodeNumber())) {
            episode = "Ep" + getEpisodeNumber();
        }

        if (!TextUtils.isEmpty(season) && !TextUtils.isEmpty(episode)) {
            // If both the values are present then separate them with a comma
            subTitleText = season + ", " + episode;
        } else {
            // Only season or episode value is available. Show just that.
            subTitleText = season + episode;
        }

        if (providerName != null && !TextUtils.isEmpty(providerName)) {
            if (TextUtils.isEmpty(subTitleText)) {
                subTitleText = providerName;
            } else {
                subTitleText = providerName + " - " + subTitleText;
            }
        }

        if (duration > 0) {
            String videoDuration = DateUtils.formatElapsedTime(duration / 1000);
            if (TextUtils.isEmpty(subTitleText)) {
                subTitleText = videoDuration;
            } else {
                subTitleText = subTitleText + " - " + videoDuration;
            }
        }
        return subTitleText;
    }

    public String getBodyText() {
        bodyText = "";

        if (!TextUtils.isEmpty(longDescription)) {
            bodyText = bodyText + longDescription + "\n\n";
        }

        if (!TextUtils.isEmpty(shortDescription)) {
            bodyText = bodyText + shortDescription + "\n\n";
        }

        if (!TextUtils.isEmpty(endsAt)) {
            bodyText = bodyText + "Available Until " + getFormatedDate(endsAt);
        }

        return bodyText;
    }

    public String getIconImageUrl() {
        return iconImageUrl;
    }

    public void setIconImageUrl(String iconImageUrl) {
        this.iconImageUrl = iconImageUrl;
    }

    public String getRedirectBrandUrl() {
        return redirectBrandUrl;
    }

    public void setRedirectBrandUrl(String redirectBrandUrl, String androidDestUrl, String defaultDestUrl) {
        if (!TextUtils.isEmpty(androidDestUrl)) {
            this.redirectBrandUrl = androidDestUrl;
        } else if (!TextUtils.isEmpty(defaultDestUrl)) {
            this.redirectBrandUrl = defaultDestUrl;
        } else {
            this.redirectBrandUrl = redirectBrandUrl;
        }

    }

    public String getRedirectBrandBtnText() {
        return redirectBrandBtnText;
    }

    public void setRedirectBrandBtnText(String redirectBrandBtnText) {
        this.redirectBrandBtnText = redirectBrandBtnText;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    private String getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(String episodeNumber) {
        this.episodeNumber = episodeNumber;
    }


    private String getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(String seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public String getVideoID() {
        return videoID;
    }

    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setEndTime(String endsAt) {
        this.endsAt = endsAt;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    private String getFormatedDate(String unformattedDate) {
        String formattedDate = null;
        Date endDate = DateTimeUtil.getDateFromRawDate(unformattedDate);

        try {
            if (endDate != null) {
                formattedDate = DateTimeUtil.getReadableDateString(endDate);
            }
        } catch (Exception e) {
            PerkLogger.e(TAG, "Exception parsing date: ", e);
        }
        return formattedDate;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
}
