package com.watchback2.android.api;

import androidx.annotation.NonNull;

import com.perk.request.PerkRequest;
import com.perk.request.auth.annotation.PerkAccessToken;
import com.perk.request.model.Data;
import com.watchback2.android.models.ChannelUpdateRequestBody;
import com.watchback2.android.models.HomeScreenItem;
import com.watchback2.android.models.NullableDataModel;
import com.watchback2.android.models.PerkUser;
import com.watchback2.android.models.WatchBackSettingsModel;
import com.watchback2.android.models.channels.AllChannelsResponseModel;
import com.watchback2.android.models.channels.singlechannel.SingleChannelWithVideoResponseModel;
import com.watchback2.android.models.genres.AllGenresResponseModel;
import com.watchback2.android.models.genres.FavoritesResponseModel;
import com.watchback2.android.models.movietickets.VideoHistoryResponseModel;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WatchbackAPI {

    //    -----------------------------------------------------------------------------------------
    //    Constants
    //    -----------------------------------------------------------------------------------------
    int CHANNEL_DETAILS_DEFAULT_LIMIT = 50;

    String SORT_MOST_POPULAR = "popular";

    String SORT_MOST_RECENT = "date";

    String SORT_SHORTEST = "duration";

    //    -----------------------------------------------------------------------------------------
    //    GET calls
    //    -----------------------------------------------------------------------------------------

    /**
     * Gets all genres and all channels under them
     *
     * @return
     */
    @GET("/genres")
    PerkRequest<List<AllGenresResponseModel>> getAllGenresWithChannels(
    );

    /**
     * Gets all channels under a specified genre
     *
     * @param genre_name Genre for which data need to be retrieved.
     * @return
     */
    @GET("/genre/{genre_name}")
    PerkRequest<AllGenresResponseModel> getChannelsOfGenre(
            @Path("genre_name") @NonNull String genre_name
    );

    /**
     * Gets logged in user's list of favorite channels
     *
     * @param access_token logged in user's access token
     * @return Note:- for user's who haven't logged in this Data is stored locally.
     */
    @GET("/channels/favorites")
    PerkRequest<FavoritesResponseModel> getFavoriteChannels(
            @Query("access_token") @NonNull @PerkAccessToken String access_token
    );

    /**
     * Gets the list of all channels. This does not return video details.
     *
     * @param access_token logged in user's access token.
     * @return
     */
    @GET("/channels")
    PerkRequest<AllChannelsResponseModel> getAllChannelsWithoutVideos(
            @Query("access_token") @NonNull @PerkAccessToken String access_token
    );

    /**
     * Gets the list of all channels. This does not return video details.
     * To be used when user is not logged in.
     *
     * @return
     */
    @GET("/channels")
    PerkRequest<AllChannelsResponseModel> getAllChannelsWithoutVideos(
    );

    /**
     * Gets data of a single channel along with videos
     *
     * @param channel_uuid uuid of channel who's data we need.
     * @param access_token logged in user's access token.
     * @param limit        number of videos to be fetched.
     * @param offset       offset from which videos needs to be fetched.
     * @return
     */
    @GET("/channel/{channel_uuid}")
    PerkRequest<SingleChannelWithVideoResponseModel> getSingleChannelWithVideos(
            @Path("channel_uuid") @NonNull String channel_uuid,
            @Query("access_token") @NonNull @PerkAccessToken String access_token,
            @Query("limit") @NonNull String limit,
            @Query("offset") @NonNull String offset,
            @Query("sort") @NonNull String sort
    );

    /**
     * Gets data of a single channel along with videos
     *
     * @param channel_uuid uuid of channel who's data we need.
     * @param limit        number of videos to be fetched.
     * @param offset       offset from which videos needs to be fetched.
     * @return
     */
    @GET("/channel/{channel_uuid}")
    PerkRequest<SingleChannelWithVideoResponseModel> getSingleChannelWithVideos(
            @Path("channel_uuid") @NonNull String channel_uuid,
            @Query("limit") @NonNull String limit,
            @Query("offset") @NonNull String offset,
            @Query("sort") @NonNull String sort
    );

    /**
     * Api to get video history details to show in movieTickets fragment.
     * This returns the number of videos remaining, list of videos watched etc.
     *
     * @param access_token logged-in user's perk access_token
     */
    @GET("/rewards")
    PerkRequest<VideoHistoryResponseModel> getVideoHistory(
            @Query("access_token") @NonNull @PerkAccessToken String access_token
    );

    @GET("/carousels/{placement}")
    PerkRequest<CarouselData> getCarousel(
            @Path("placement") String placement);

    @GET("/users/me")
    PerkRequest<PerkUser> getPerkUser(
            @Query("access_token") @NonNull @PerkAccessToken String access_token

    );

    @GET("/v4/versions.json")
    PerkRequest<PerkAppVersion> getLatestAppVersion(
    );

    @GET("/settings")
    PerkRequest<WatchBackSettingsModel> getSettings();

    @GET("/homescreen")
    PerkRequest<List<HomeScreenItem>> getHomescreenList();

    //    -----------------------------------------------------------------------------------------
    //    POST calls
    //    -----------------------------------------------------------------------------------------

    /**
     * POST call to verify user's phone number.
     * This posts code entered by user to server.
     *
     * @param code         code entered by user.
     * @param access_token logged in user's access_token
     * @return
     */
    @FormUrlEncoded
    @POST("/verify-phone")
    PerkRequest<Data> postVerificationCode(
            @Field("code") String code,
            @Field("access_token") @NonNull @PerkAccessToken String access_token
    );

    /**
     * POST call to notify server that user need his/her verification code to be send again to
     * complete the verification process.
     *
     * @param access_token logged in user's access_token
     * @return
     */
    @FormUrlEncoded
    @POST("/resend-verification")
    PerkRequest<Data> postRsendVerificationCode(
            @Field("access_token") @NonNull @PerkAccessToken String access_token
    );

    @FormUrlEncoded
    @POST("/logout")
    PerkRequest<NullableDataModel> postUserLogOut(
            @Field("access_token") @NonNull @PerkAccessToken String access_token
    );

    @FormUrlEncoded
    @POST("/views")
    PerkRequest<PerkAdView> postVideoView(
            @Field("video_id") String video_id,
            @Field("perk_reward") String perk_reward,
            @Field("ad_filled") String ad_filled,
            @Field("fastforward") boolean fastforward,
            @Field("percent") String percent,
            @Field("access_token") @NonNull @PerkAccessToken String access_token
    );

    /**
     * Saves user's favorite channels
     */
    @POST("/channels/favorites")
    PerkRequest<NullableDataModel> postUserFavorites(
            @Body @NonNull ChannelUpdateRequestBody body);

    //    -----------------------------------------------------------------------------------------
    //    PUT calls
    //    -----------------------------------------------------------------------------------------


    //    -----------------------------------------------------------------------------------------
    //    DEL calls
    //    -----------------------------------------------------------------------------------------


}
