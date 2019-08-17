package com.watchback2.android.utils;

import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.models.Channel;
import com.watchback2.android.models.Genre;
import com.watchback2.android.models.channels.ChannelsEntity;
import com.watchback2.android.models.channels.GenresEntity;
import com.watchback2.android.models.channels.singlechannel.VideosEntity;
import com.watchback2.android.models.genres.AllGenresResponseModel;
import com.watchback2.android.models.genres.AllGenresWrapper;
import com.watchback2.android.models.movietickets.VideoHistoryResponseModel;
import com.watchback2.android.models.movietickets.VideoHistoryWapper;
import com.watchback2.android.models.movietickets.VideoHistoryModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to wrap Api response Model with a wrapper class
 * This decouples data passed across the app from api response.
 */
public class WrapResponseModels {

    private WrapResponseModels() {
        // private constructor
    }

    /**
     * wraps com.watchback2.android.models.channels.ChannelsEntity
     * with com.watchback2.android.models.Channel
     *
     * @param entityList - api response
     * @return
     */
    public static List<Channel> getWrappedChannelList(List<ChannelsEntity> entityList) {

        if (entityList == null) {
            return null;
        }

        List<Channel> channelWrapper = new ArrayList<>(entityList.size());

        for (ChannelsEntity channelsEntity : entityList) {

            //GenreList
            ArrayList<Genre> genreList = null;
            if (channelsEntity.getGenres() != null) {
                genreList = new ArrayList<>(channelsEntity.getGenres().size());
                for (GenresEntity genre : channelsEntity.getGenres()) {
                    genreList.add(new Genre(genre.getLink(), genre.getGenre()));
                }
            }
            //VideoList
            ArrayList<BrightcovePlaylistData.BrightcoveVideo> videoList = null;
            if (channelsEntity.getVideos() != null) {
                videoList = new ArrayList<>(channelsEntity.getVideos().size());
                for (VideosEntity videosEntity : channelsEntity.getVideos()) {
                    //TODO: do the conversion
                }
            }
            channelWrapper.add(new Channel(channelsEntity.getFavorite(), genreList,
                    channelsEntity.getAndroidDestinationUrl(), channelsEntity.getDefaultDestinationUrl(),
                    channelsEntity.getDestinationUrl(), channelsEntity.getButtonText(),
                    channelsEntity.getBackgroundImageUrl(), channelsEntity.getmDetailsScreenLogoUrl(),
                    channelsEntity.getListScreenLogoUrl(), channelsEntity.getShortDescription(),
                    channelsEntity.getDescription(), channelsEntity.getTag(),
                    channelsEntity.getName(), channelsEntity.getUuid(), videoList));
        }
        return channelWrapper;
    }

    public static AllGenresWrapper wrapAllGenreResponse(AllGenresResponseModel allGenresResponseModel) {
        List<Channel> channelWrapper = getWrappedChannelList(allGenresResponseModel.getChannels());

        if (channelWrapper == null || channelWrapper.size() == 0) {
            return null;
        }

        return new AllGenresWrapper(allGenresResponseModel.getGenre(),
                allGenresResponseModel.getLink(), channelWrapper);
    }

    public static VideoHistoryWapper wrapVideoHistoryResponse(VideoHistoryResponseModel model) {
        List<VideoHistoryModel> videoList = null;

        if (model.getVideos() != null) {
            videoList = new ArrayList<>(model.getVideos().size());

            long id;
            String thumbnailUrl, videoName, episodeNumber = "", seasonNumber = "", providerName = "", redirectUrl = "";

            for (com.watchback2.android.models.movietickets.VideosEntity videosEntity : model.getVideos()) {
                id = videosEntity.getId();
                thumbnailUrl = videosEntity.getThumbnail();
                videoName = videosEntity.getName();

                if (videosEntity.getCustomFields() != null) {
                    episodeNumber = videosEntity.getCustomFields().getEpisodeNumber();
                    seasonNumber = videosEntity.getCustomFields().getSeasonNumber();
                    providerName = videosEntity.getCustomFields().getProvider();
                }

                if (videosEntity.getChannelsEntity() != null) {
                    redirectUrl = videosEntity.getChannelsEntity().getDestinationUrl();
                }

                VideoHistoryModel videoHistoryModel = new VideoHistoryModel(id, videoName, thumbnailUrl, episodeNumber, seasonNumber, providerName, redirectUrl);

                videoList.add(videoHistoryModel);
            }
        }

        return new VideoHistoryWapper(videoList, model.getVideosRemaining(), model.getRewarded(), model.getTotalRewardsRemaining());
    }

}
