package com.watchback2.android.models.movietickets;

import android.text.TextUtils;
import android.view.View;

/**
 * Helps manage video history list data shown in MovieTicketFragment
 * Data obtained from /rewards api response
 */
public class VideoHistoryModel {

//  ------------------------------------------------------------------------------------------------
//    private variables
//  ------------------------------------------------------------------------------------------------

    private long id;

    private String videoName;

    private String thumbnailUrl;

    private String episodeNumber;

    private String seasonNumber;

    private String providerName;

    private OnVideoHistoryItemClicked itemClickListener;

    private String redirectUrl;
//  ------------------------------------------------------------------------------------------------
//    Interface that acts as itemClickListener
//  ------------------------------------------------------------------------------------------------

    /**
     * Listener to notify when button in a VideoHistoryModel Item is clicked.
     */
    public interface OnVideoHistoryItemClicked {
        void onItemClick(long videoID);
    }

//  ------------------------------------------------------------------------------------------------
//   public constructor
//  ------------------------------------------------------------------------------------------------
    public VideoHistoryModel(long id, String videoName, String thumbnailUrl, String episodeNumber, String seasonNumber, String providerName, String redirectUrl) {
        this.id = id;
        this.thumbnailUrl = thumbnailUrl;
        this.episodeNumber = episodeNumber;
        this.seasonNumber = seasonNumber;
        this.providerName = providerName;
        this.redirectUrl = redirectUrl;
        this.videoName = videoName;
    }

//  ------------------------------------------------------------------------------------------------
//  public setters
//  ------------------------------------------------------------------------------------------------
    public void setItemClickListener(OnVideoHistoryItemClicked itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
//  ------------------------------------------------------------------------------------------------
//  public getters
//  ------------------------------------------------------------------------------------------------

    /**
     * Video Id
     * Needed to show provider details screen for that particular video.
     *
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     * Url from which thumbnail image is loaded
     *
     * @return
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * Text to show as title in each model item.
     *
     * @return season, episode numbers and provider name
     */
    public String getTitle() {
       /* String season = "";
        String episode = "";

        if (!TextUtils.isEmpty(seasonNumber) && !"unknown".equalsIgnoreCase(seasonNumber)) {
            season = "S" + seasonNumber;
        }

        if (!TextUtils.isEmpty(episodeNumber) && !"unknown".equalsIgnoreCase(episodeNumber)) {
            episode = "Ep" + episodeNumber;
        }

        String title;
        if (!TextUtils.isEmpty(season) && !TextUtils.isEmpty(episode)) {
            // If both the values are present then separate them with a comma
            title = season + ", " + episode;
        } else {
            // Only season or episode value is available. Show just that.
            title = season + episode;
        }

        if (TextUtils.isEmpty(title) && (!TextUtils.isEmpty(providerName) && !"unknown".equalsIgnoreCase(providerName))) {
            title = providerName;
        } else if (!TextUtils.isEmpty(providerName) && !"unknown".equalsIgnoreCase(providerName)) {
            title = title + " - " + providerName;
        }*/
        return videoName;
    }

    /**
     * Text to show on the button that redirects user to provider details screen.
     *
     * @return provider name
     */
    public String getButtontext() {
        String buttontext = "";
        if (!TextUtils.isEmpty(providerName)) {
            buttontext = "Watch on "+providerName;
        }
        return buttontext;
    }

    public int getButtonVisibility() {
        if (TextUtils.isEmpty(getButtontext()) || TextUtils.isEmpty(redirectUrl)) {
            return View.GONE;
        } else {
            return View.VISIBLE;
        }
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    //  ------------------------------------------------------------------------------------------------
//  method listening for click even in provider button
//  ------------------------------------------------------------------------------------------------

    /**
     * Registers click from provider details button.
     *
     * @param view view that was clicked
     */
    public void onShowDetailsClick(View view) {
        if (itemClickListener != null) {
            itemClickListener.onItemClick(getId());
        }
    }
}
