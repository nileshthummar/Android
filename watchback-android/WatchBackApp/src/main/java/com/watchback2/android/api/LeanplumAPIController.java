package com.watchback2.android.api;

import android.app.NotificationChannel;
import android.os.Build;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.util.PerkLogger;
import com.watchback2.android.models.LeanplumAddChannelRequestBody;
import com.watchback2.android.models.LeanplumPostModel;
import com.watchback2.android.models.LeanplumUserAttributesRequestBody;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

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

public class LeanplumAPIController implements Interceptor {

    // -----------------------------------------------------------------------------------------
    // Singleton instance
    // -----------------------------------------------------------------------------------------

    /**
     * Singleton instance of the class that would be used for accessing v.
     */
    public static LeanplumAPIController INSTANCE = new LeanplumAPIController();

    // -----------------------------------------------------------------------------------------
    // Private static final variables
    // -----------------------------------------------------------------------------------------

    /** Tag that should be used for logging. */
    private static final String LOG_TAG = LeanplumAPIController.class.getSimpleName();

    private static final String LEANPLUM_API_URL = "https://www.leanplum.com";

    // -----------------------------------------------------------------------------------------
    // Private variables
    // -----------------------------------------------------------------------------------------

    /** LeanplumAPI that should be used for making API requests. */
    private LeanplumAPI mLeanplumAPI = null;

    /** OK HTTP client that should be used for intercepting requests. */
    private OkHttpClient mOkHttpClient = null;

    // -----------------------------------------------------------------------------------------
    // Private methods
    // -----------------------------------------------------------------------------------------

    /**
     * Constructor.
     */
    /*private*/ LeanplumAPIController() {
        // Empty
    }

    private void createLeanplumAPI() {

        // If Leanplum API is already created, then do nothing
        if (mLeanplumAPI != null) {
            return;
        }

        PerkLogger.i(LOG_TAG, "Initializing API request facade for base URL: " + LEANPLUM_API_URL);

        // If HTTP client is not created yet, then create one now
        if (mOkHttpClient == null) {
            mOkHttpClient = new OkHttpClient.Builder().addInterceptor(this).build();
        }

        // Create Brightcove API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(LEANPLUM_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(mOkHttpClient)
                .build();

        mLeanplumAPI = retrofit.create(LeanplumAPI.class);
    }

    // -----------------------------------------------------------------------------------------
    // Public methods
    // -----------------------------------------------------------------------------------------

    public void addAndroidNotificationChannel(@NonNull NotificationChannel notificationChannel,
            @NonNull final OnRequestFinishedListener<LeanplumPostModel> listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            PerkLogger.a(LOG_TAG,
                    "addAndroidNotificationChannel(): Error! Can be done only on Android 8 & above devices!");
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
            return;
        }

        try {
            createLeanplumAPI();

            Call<LeanplumPostModel> call = mLeanplumAPI.postAction(
                    "addAndroidNotificationChannel", new LeanplumAddChannelRequestBody(notificationChannel));

            call.enqueue(new Callback<LeanplumPostModel>() {
                @Override
                public void onResponse(Call<LeanplumPostModel> call,
                        Response<LeanplumPostModel> response) {
                    if (response.isSuccessful()) {
                        final int responseCode = response.code();
                        if (responseCode >= 200 && responseCode < 300) {
                            listener.onSuccess(response.body(), "");
                        } else {
                            listener.onFailure(ErrorType.SERVER_ERROR, null);
                        }
                    } else {
                        // error response
                        listener.onFailure(ErrorType.SERVER_ERROR, null);
                    }
                }

                @Override
                public void onFailure(Call<LeanplumPostModel> call, Throwable t) {
                    listener.onFailure(ErrorType.NETWORK_ERROR, null);
                }
            });

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error with addAndroidNotificationChannel: ", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
        }
    }

    public void updateUserAttributes(@NonNull Map<String, String> userAttributes,
            @NonNull final OnRequestFinishedListener<LeanplumPostModel> listener) {

        try {
            createLeanplumAPI();

            Call<LeanplumPostModel> call = mLeanplumAPI.postUserAttributesAction(
                    "setUserAttributes", new LeanplumUserAttributesRequestBody(userAttributes));

            call.enqueue(new Callback<LeanplumPostModel>() {
                @Override
                public void onResponse(Call<LeanplumPostModel> call,
                        Response<LeanplumPostModel> response) {
                    if (response.isSuccessful()) {
                        final int responseCode = response.code();
                        if (responseCode >= 200 && responseCode < 300) {
                            listener.onSuccess(response.body(), "");
                        } else {
                            listener.onFailure(ErrorType.SERVER_ERROR, null);
                        }
                    } else {
                        // error response
                        listener.onFailure(ErrorType.SERVER_ERROR, null);
                    }
                }

                @Override
                public void onFailure(Call<LeanplumPostModel> call, Throwable t) {
                    listener.onFailure(ErrorType.NETWORK_ERROR, null);
                }
            });

        } catch (final Exception e) {
            PerkLogger.a(LOG_TAG, "Error with updateUserAttributes: ", e);
            listener.onFailure(ErrorType.UNEXPECTED_ERROR, null);
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