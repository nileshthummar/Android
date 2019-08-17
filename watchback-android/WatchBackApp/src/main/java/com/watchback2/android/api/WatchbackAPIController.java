package com.watchback2.android.api;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.perk.request.APIRequestFacade;
import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.auth.annotation.PerkAccessToken;
import com.perk.request.model.Data;
import com.perk.request.util.EnvironmentUtil;
import com.perk.util.PerkLogger;
import com.watchback2.android.BuildConfig;
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

public class WatchbackAPIController implements EnvironmentUtil.OnEnvironmentSwitchListener {

    // -----------------------------------------------------------------------------------------
    // Singleton instance
    // -----------------------------------------------------------------------------------------
    public static WatchbackAPIController INSTANCE = new WatchbackAPIController();

    // -----------------------------------------------------------------------------------------
    // Private variables
    // -----------------------------------------------------------------------------------------
    private static final String LOG_TAG = WatchbackAPIController.class.getSimpleName();

    private WatchbackAPI watchbackAPI = null;

    // -----------------------------------------------------------------------------------------
    // EnvironmentUtil.OnEnvironmentSwitchListener Implementation
    // -----------------------------------------------------------------------------------------
    @Override
    public void onEnvironmentSwitched(@NonNull EnvironmentUtil.Environment environment) {
        createWatchbackAPI(environment);
    }

    // -----------------------------------------------------------------------------------------
    // Private methods
    // -----------------------------------------------------------------------------------------
    private WatchbackAPIController() {
        EnvironmentUtil.INSTANCE.addOnEnvironmentSwitchListener(this);

    }

    private @NonNull
    String getWBAPIBaseUrl(@NonNull final EnvironmentUtil.Environment environment) {
        // Get base common API request URL based on environment
        final String wbApiRequestUrl;
        if (environment == EnvironmentUtil.Environment.DEV) {
            wbApiRequestUrl = BuildConfig.secretKeys.apiDevBaseUrl;
        } else {
            wbApiRequestUrl = BuildConfig.secretKeys.apiProdBaseUrl;
        }

        return wbApiRequestUrl;
    }

    private void createWatchbackAPI(@NonNull final EnvironmentUtil.Environment environment) {

        // Get base common API request URL based on environment
        final String wbApiRequestUrl = getWBAPIBaseUrl(environment);

        // Create Perk API facade
        watchbackAPI = APIRequestFacade.INSTANCE.create(wbApiRequestUrl).create(WatchbackAPI.class);
    }

    private void createWatchbackAPI(@NonNull final Context context) {

        // If Perk API is already created, then do nothing
        if (watchbackAPI != null)
            return;

        // Get the current environment
        final EnvironmentUtil.Environment environment =
                EnvironmentUtil.INSTANCE.getCurrentEnvironment(context);

        // Create Perk API
        createWatchbackAPI(environment);
    }

    // -----------------------------------------------------------------------------------------
    // Public POST call methods
    // -----------------------------------------------------------------------------------------

    public void postVerificationCode(@NonNull final Context context,
                                     @NonNull final String code,
                                     @NonNull final OnRequestFinishedListener<Data> listener) {

        try {
            createWatchbackAPI(context);
            watchbackAPI.postVerificationCode(code, PerkAccessToken.ACCESS_TOKEN).executeAsync(context, listener);

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error while posting verification code", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }

    }

    public void postRsendVerificationCode(@NonNull final Context context,
                                          @NonNull final OnRequestFinishedListener<Data> listener) {

        try {
            createWatchbackAPI(context);
            watchbackAPI.postRsendVerificationCode(PerkAccessToken.ACCESS_TOKEN).executeAsync(context, listener);

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error while resending verification code", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }

    }

    public void postUserLogOut(@NonNull final Context context,
                               @NonNull final OnRequestFinishedListener<NullableDataModel> listener) {
        try {
            createWatchbackAPI(context);
            watchbackAPI.postUserLogOut(PerkAccessToken.ACCESS_TOKEN).executeAsync(context, listener);

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error with onUserLogOut: ", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }
    }

    public void postVideoView(@NonNull final Context context,
                              @NonNull final String video_id,
                              @NonNull final String perk_reward,
                              final boolean ad_filled,
                              final boolean didFastForward,
                              @NonNull final String percent,
                              @NonNull final OnRequestFinishedListener<PerkAdView> listener) {

        try {
            createWatchbackAPI(context);
            watchbackAPI.postVideoView(video_id, perk_reward, ad_filled ? "1" : "0",
                    didFastForward, percent,
                    PerkAccessToken.ACCESS_TOKEN).executeAsync(context, listener);

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error with postVideoView", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }
    }

    public void postUserFavorites(@NonNull final Context context, @NonNull ChannelUpdateRequestBody body,
                                  @NonNull final OnRequestFinishedListener<NullableDataModel> listener) {
        try {
            createWatchbackAPI(context);
            watchbackAPI.postUserFavorites(body).executeAsync(context, listener);

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error with putUserInterests: ", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }
    }

    // -----------------------------------------------------------------------------------------
    // Public get call methods
    // -----------------------------------------------------------------------------------------

    public void getAllGenresWithChannels(@NonNull final Context context,
                                         @NonNull final OnRequestFinishedListener<List<AllGenresResponseModel>> listener) {

        try {
            createWatchbackAPI(context);
            watchbackAPI.getAllGenresWithChannels().executeAsync(context, listener);

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error while getting perkuser", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }

    }

    public void getChannelsOfGenre(@NonNull final Context context,
                                   @NonNull final OnRequestFinishedListener<AllGenresResponseModel> listener,
                                   @NonNull String genre) {

        try {
            createWatchbackAPI(context);
            watchbackAPI.getChannelsOfGenre(genre).executeAsync(context, listener);

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error while getting perkuser", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }

    }

    public void getFavoriteChannels(@NonNull final Context context,
                                    @NonNull final OnRequestFinishedListener<FavoritesResponseModel> listener) {
        try {
            createWatchbackAPI(context);
            watchbackAPI.getFavoriteChannels(PerkAccessToken.ACCESS_TOKEN).executeAsync(context, listener);

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error while getting perkuser", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }

    }

    public void getAllChannelsWithoutVideos(@NonNull final Context context,
                                            boolean isLoggedIn,
                                            @NonNull final OnRequestFinishedListener<AllChannelsResponseModel> listener) {
        try {
            createWatchbackAPI(context);

            if (isLoggedIn) {
                watchbackAPI.getAllChannelsWithoutVideos(PerkAccessToken.ACCESS_TOKEN).executeAsync(context, listener);
            } else {
                watchbackAPI.getAllChannelsWithoutVideos().executeAsync(context, listener);
            }

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error while getting perkuser", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }

    }

    public void getSingleChannelWithVideos(@NonNull final Context context,
                                           boolean isLoggedIn,
                                           String channelUUID,
                                           String limit,
                                           String offset,
                                           @NonNull final OnRequestFinishedListener<SingleChannelWithVideoResponseModel> listener) {
        try {
            createWatchbackAPI(context);
            if (isLoggedIn) {
                watchbackAPI.getSingleChannelWithVideos(channelUUID, PerkAccessToken.ACCESS_TOKEN, limit, offset, "popular").executeAsync(context, listener);
            } else {
                watchbackAPI.getSingleChannelWithVideos(channelUUID, limit, offset, "popular").executeAsync(context, listener);

            }

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error while getting perkuser", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }

    }

    /**
     * Gets the list of longform videos watched by the user this month for
     * showing in movietickets screen
     *
     * @param context
     * @param listener
     */
    public void getVideoHistory(@NonNull final Context context,
                                @NonNull final OnRequestFinishedListener<VideoHistoryResponseModel> listener) {
        try {
            createWatchbackAPI(context);
            watchbackAPI.getVideoHistory(PerkAccessToken.ACCESS_TOKEN).executeAsync(context, listener);

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error while getting perkuser", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }

    }

    public void getCarousel(@NonNull final Context context,
                            @NonNull final String placement,
                            @NonNull final OnRequestFinishedListener<CarouselData> listener) {
        try {
            createWatchbackAPI(context);
            watchbackAPI.getCarousel(placement).executeAsync(context, listener);

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error with getV2Carousel: ", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }
    }

    public void getPerkUser(@NonNull final Context context,

                            @NonNull final OnRequestFinishedListener<PerkUser> listener) {
        try {
            createWatchbackAPI(context);
            watchbackAPI.getPerkUser(PerkAccessToken.ACCESS_TOKEN).executeAsync(context, listener);

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error while getting perkuser", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }
    }

    public void getLatestAppVersion(@NonNull final Context context,
                                    @NonNull final OnRequestFinishedListener<PerkAppVersion> listener) {
        try {
            createWatchbackAPI(context);
            watchbackAPI.getLatestAppVersion().executeAsync(context, listener);

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error while getLatestAppVersion", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }
    }

    public void getSettings(@NonNull final Context context, @NonNull final OnRequestFinishedListener<WatchBackSettingsModel> listener) {
        try {
            createWatchbackAPI(context);
            watchbackAPI.getSettings().executeAsync(context, listener);

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error with getSettings: ", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }
    }

    public void getHomescreenList(@NonNull final Context context,
                                  @Nullable final List<String> interests,
                                  @NonNull final OnRequestFinishedListener<List<HomeScreenItem>> listener) {
        try {
            createWatchbackAPI(context);
            watchbackAPI.getHomescreenList().executeAsync(context, listener);

        } catch (final Exception e) {
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }
    }


}
