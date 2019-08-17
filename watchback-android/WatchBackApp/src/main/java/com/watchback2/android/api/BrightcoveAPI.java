package com.watchback2.android.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BrightcoveAPI {

    String ACCEPT_HEADER_PREFIX = "application/json;pk=";

    int DEFAULT_LIMIT = 20;

    /**
     * The default sorting mode which provides the latest created video at the top
     */
    String DEFAULT_SORT = "-created_at";

    @GET("/playback/v1/accounts/{account_id}/playlists/{playlist_id}")
    Call<BrightcovePlaylistData> getBrightCovePlaylistVideos(
            @Header("Accept") String accept,
            @Path("account_id") String account_id,
            @Path("playlist_id") String playlist_id,
            @Query("offset") int offset,
            @Query("limit") int limit);

    @GET("/playback/v1/accounts/{account_id}/videos/{video_id}")
    Call<BrightcovePlaylistData.BrightcoveVideo> getBrightCoveVideos(
            @Header("Accept") String accept,
            @Path("account_id") String account_id,
            @Path("video_id") String video_id);



    @GET("/playback/v1/accounts/{account_id}/videos")
    Call<BrightcovePlaylistData> searchBrightCoveVideos(
            @Header("Accept") String accept,
            @Path("account_id") String account_id,
            @Query("q") String search_string,
            @Query("sort") String sort);

}