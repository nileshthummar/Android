package com.watchback2.android.api;

import android.text.TextUtils;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.util.PerkLogger;
import com.watchback2.android.BuildConfig;
import com.watchback2.android.models.BrightcoveSearchFailedResponse;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BrightcoveAPIController implements Interceptor {

    // -----------------------------------------------------------------------------------------
    // Singleton instance
    // -----------------------------------------------------------------------------------------

    /**
     * Singleton instance of the class that would be used for accessing v.
     */
    public static BrightcoveAPIController INSTANCE = new BrightcoveAPIController();

    // -----------------------------------------------------------------------------------------
    // Private static final variables
    // -----------------------------------------------------------------------------------------

    /** Tag that should be used for logging. */
    private static final String LOG_TAG = BrightcoveAPIController.class.getSimpleName();

    private static final String BRIGHTCOVE_API_URL = "https://edge.api.brightcove.com";

    // -----------------------------------------------------------------------------------------
    // Private variables
    // -----------------------------------------------------------------------------------------

    /** BrightcoveAPI API that should be used for making API requests. */
    private BrightcoveAPI mBrightcoveAPI = null;

    /** OK HTTP client that should be used for intercepting requests. */
    private OkHttpClient mOkHttpClient = null;

    // -----------------------------------------------------------------------------------------
    // Private methods
    // -----------------------------------------------------------------------------------------

    /**
     * Constructor.
     */
    /*private*/ BrightcoveAPIController() {
        // Empty
    }

    private void createBrightcoveAPI() {

        // If Brightcove API is already created, then do nothing
        if (mBrightcoveAPI != null) {
            return;
        }

        PerkLogger.i(LOG_TAG, "Initializing API request facade for base URL: " + BRIGHTCOVE_API_URL);

        // If HTTP client is not created yet, then create one now
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder().addInterceptor(this).build();
        }

        // Create Brightcove API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BRIGHTCOVE_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(mOkHttpClient)
                .build();

        mBrightcoveAPI = retrofit.create(BrightcoveAPI.class);
    }

    // -----------------------------------------------------------------------------------------
    // Public methods
    // -----------------------------------------------------------------------------------------

    public void getBrightCovePlaylistVideos(@NonNull final String playlist_id,
            final int offset,
            @NonNull final OnRequestFinishedListener<BrightcovePlaylistData> listener) {

        getBrightCovePlaylistVideos(playlist_id, 0, BrightcoveAPI.DEFAULT_LIMIT, listener);
    }

    public void getBrightCovePlaylistVideos(@NonNull final String playlist_id,
            final int offset,
            final int limit,
            @NonNull final OnRequestFinishedListener<BrightcovePlaylistData> listener) {

        try {
            createBrightcoveAPI();

            Call<BrightcovePlaylistData> call = mBrightcoveAPI.getBrightCovePlaylistVideos(
                    BrightcoveAPI.ACCEPT_HEADER_PREFIX + BuildConfig.brightcoveApiPolicyKey,
                    BuildConfig.brightcoveAccountId, playlist_id, offset, limit);

            call.enqueue(new Callback<BrightcovePlaylistData>() {
                @Override
                public void onResponse(Call<BrightcovePlaylistData> call,
                        Response<BrightcovePlaylistData> response) {
                    if (response.isSuccessful()) {
                        // tasks available

                        final int responseCode = response.code();
                        if (responseCode >= 200 && responseCode < 300) {
                            listener.onSuccess(response.body(), "");
                        } else {
                            listener.onFailure(ErrorType.SERVER_ERROR, null);
                        }
                    } else {
                        // error response, no access to resource?
                        listener.onFailure(ErrorType.SERVER_ERROR, null);
                    }
                }

                @Override
                public void onFailure(Call<BrightcovePlaylistData> call, Throwable t) {
                    listener.onFailure(ErrorType.NETWORK_ERROR, null);
                }
            });
        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error while getBrightCovePlaylistVideos", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }
    }

    public void getBrightCoveVideos(@NonNull final String video_id,
            @NonNull final OnRequestFinishedListener<BrightcovePlaylistData.BrightcoveVideo> listener) {
        try {
            createBrightcoveAPI();

            Call<BrightcovePlaylistData.BrightcoveVideo> call = mBrightcoveAPI.getBrightCoveVideos(
                    BrightcoveAPI.ACCEPT_HEADER_PREFIX + BuildConfig.brightcoveApiPolicyKey,
                    BuildConfig.brightcoveAccountId, video_id);

            call.enqueue(new Callback<BrightcovePlaylistData.BrightcoveVideo>() {
                @Override
                public void onResponse(Call<BrightcovePlaylistData.BrightcoveVideo> call,
                                       Response<BrightcovePlaylistData.BrightcoveVideo> response) {
                    if (response.isSuccessful()) {
                        // tasks available

                        final int responseCode = response.code();
                        if (responseCode >= 200 && responseCode < 300) {
                            listener.onSuccess(response.body(), "");
                        } else {
                            listener.onFailure(ErrorType.SERVER_ERROR, null);
                        }
                    } else {
                        // error response, no access to resource?
                        listener.onFailure(ErrorType.SERVER_ERROR, null);
                    }
                }

                @Override
                public void onFailure(Call<BrightcovePlaylistData.BrightcoveVideo> call, Throwable t) {
                    listener.onFailure(ErrorType.NETWORK_ERROR, null);
                }
            });
        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error while getBrightCovePlaylistVideos", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }
    }

    /**
     * For use with the 'Search' feature. Although this has almost same code as that of
     * searchForRecommendedVideos, we do not make it as a common method because we pass back the
     * original 'search_string' in callback methods after API call is complete. We additionally
     * append 'name:' to the 'search_string' to search by name... so in case of common API, this
     * would have to be selectively stripped out, just for the search-feature calls. So, we keep
     * it separate
     */
    public void searchBrightCoveVideosByName(@NonNull final String search_string,
            @NonNull final OnRequestFinishedListener<BrightcovePlaylistData> listener) {

        // changed to search in all fields instead of just 'name' as per
        // https://jira.rhythmone.com/browse/PEWIOS-475 / https://jira.rhythmone.com/browse/PEWAN-298
        searchBrightcoveVideos(search_string,
                // sort set to empty, to sort by score/relevance to search:
                // https://jira.rhythmone.com/browse/PEWAN-303
                "",
                listener);

        /*try {
            createBrightcoveAPI();

            Call<BrightcovePlaylistData> call = mBrightcoveAPI.searchBrightCoveVideos(
                    BrightcoveAPI.ACCOUNT_ID, "name:" + search_string, BrightcoveAPI.DEFAULT_LIMIT,
                    BrightcoveAPI.DEFAULT_SORT);

            call.enqueue(new Callback<BrightcovePlaylistData>() {
                @Override
                public void onResponse(Call<BrightcovePlaylistData> call,
                        Response<BrightcovePlaylistData> response) {
                    if (response.isSuccessful()) {
                        // tasks available

                        final int responseCode = response.code();
                        if (responseCode >= 200 && responseCode < 300) {
                            listener.onSuccess(response.body(), search_string);
                        } else {
                            listener.onFailure(ErrorType.SERVER_ERROR, new BrightcoveSearchFailedResponse(search_string));
                        }
                    } else {
                        // error response, no access to resource?
                        listener.onFailure(ErrorType.SERVER_ERROR, new BrightcoveSearchFailedResponse(search_string));
                    }
                }

                @Override
                public void onFailure(Call<BrightcovePlaylistData> call, Throwable t) {
                    listener.onFailure(ErrorType.NETWORK_ERROR, new BrightcoveSearchFailedResponse(search_string));
                }
            });
        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error with getBrightCovePlaylistVideos:", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, new BrightcoveSearchFailedResponse(search_string));
        }*/
    }

    public void searchForRecommendedVideos(@NonNull final String search_string,
            @NonNull final OnRequestFinishedListener<BrightcovePlaylistData> listener) {
        searchBrightcoveVideos(search_string, "-plays_trailing_week",
                listener);  // Sort changed as per discussion done on Slack
    }

    public void searchBrightcoveVideos(@NonNull final String search_string, String sort,
            @NonNull final OnRequestFinishedListener<BrightcovePlaylistData> listener) {
        try {
            createBrightcoveAPI();

            Call<BrightcovePlaylistData> call = mBrightcoveAPI.searchBrightCoveVideos(
                    BrightcoveAPI.ACCEPT_HEADER_PREFIX + BuildConfig.brightcoveSearchApiPolicyKey,
                    BuildConfig.brightcoveAccountId, search_string, sort);

            call.enqueue(new Callback<BrightcovePlaylistData>() {
                @Override
                public void onResponse(Call<BrightcovePlaylistData> call,
                        Response<BrightcovePlaylistData> response) {
                    if (response.isSuccessful()) {
                        // tasks available

                        final int responseCode = response.code();
                        if (responseCode >= 200 && responseCode < 300) {
                            listener.onSuccess(response.body(), search_string);
                        } else {
                            listener.onFailure(ErrorType.SERVER_ERROR, new BrightcoveSearchFailedResponse(search_string));
                        }
                    } else {
                        // error response, no access to resource?
                        listener.onFailure(ErrorType.SERVER_ERROR, new BrightcoveSearchFailedResponse(search_string));
                    }
                }

                @Override
                public void onFailure(Call<BrightcovePlaylistData> call, Throwable t) {
                    listener.onFailure(ErrorType.NETWORK_ERROR, new BrightcoveSearchFailedResponse(search_string));
                }
            });
        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error with searchForRecommendedVideos:", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, new BrightcoveSearchFailedResponse(search_string));
        }
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        // Make sure the request is valid
        final Request request = chain.request();
        if (request == null) {
            throw new IllegalStateException(
                    "Request details are not available that's why cannot send the request"
            );
        }

        // Log the request sent
        logRequestSent(request);

        // Execute the request
        final okhttp3.Response response = chain.proceed(request);

        // Log the response received
        logResponseReceived(response);

        // Return the response
        return response;
    }

    /**
     * Logs the request that is going to be sent
     *
     * @param authenticatedRequest authenticated request that is going to be sent
     */
    private void logRequestSent(@NonNull final Request authenticatedRequest) {

        // Log requests only if log level is info or below
        if (PerkLogger.LogLevel.INFO.level > PerkLogger.getLogLevel().level)
            return;

        final StringBuilder requestSB = new StringBuilder();
        requestSB.append(authenticatedRequest.method())
                .append(" ")
                .append(authenticatedRequest.url().toString())
                .append("\n");
        requestSB.append(authenticatedRequest.headers().toString()).append("\n" );
        final RequestBody requestBody = authenticatedRequest.body();
        if (requestBody != null) {
            requestSB.append(readRequestBody(requestBody)).append("\n");
        }

        PerkLogger.i(LOG_TAG, "------------ Request ------------\n" + requestSB.toString());
    }

    /**
     * Logs the response received after executing request.
     *
     * @param response response received after executing request
     */
    private void logResponseReceived(@NonNull final okhttp3.Response response) {

        // Log response only if log level is info or below
        if (PerkLogger.LogLevel.INFO.level > PerkLogger.getLogLevel().level)
            return;

        final StringBuilder responseSB = new StringBuilder();
        responseSB.append(response.code())
                .append(" ")
                .append(response.message())
                .append(" --> ")
                .append(response.request().method())
                .append(" ")
                .append(response.request().url())
                .append("\n");
        responseSB.append(response.headers().toString()).append("\n");

        try {
            final String responseHeader = response.header("Content-Length");
            if (!TextUtils.isEmpty(responseHeader)) {
                final ResponseBody responseBody =
                        response.peekBody(Integer.valueOf(responseHeader.trim()) * 2);
                responseSB.append(responseBody.string()).append("\n");
            }

        } catch (final IOException | NumberFormatException e) {
            PerkLogger.e(LOG_TAG, "Error while parsing response", e);
        }

        PerkLogger.d(LOG_TAG, "------------ Response ------------\n" + responseSB.toString());
    }

    /**
     * Reads the request body and return it in string format
     *
     * @param requestBody request body that need to be read
     * @return request body in readable string
     * @throws IllegalStateException thrown when there is an error while reading the request body in
     *                               string format
     */
    private @NonNull String readRequestBody(@NonNull final RequestBody requestBody)
            throws IllegalStateException
    {

        try {

            // Read the body in the buffer and return the string from it
            final Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            return buffer.readUtf8();

        } catch (final IOException | UnsupportedCharsetException | Error e) {
            throw new IllegalStateException("Error while reading request body", e);
        }
    }
}