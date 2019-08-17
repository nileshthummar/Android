package com.watchback2.android.api;

import androidx.annotation.NonNull;

import com.watchback2.android.models.LeanplumAddChannelRequestBody;
import com.watchback2.android.models.LeanplumPostModel;
import com.watchback2.android.models.LeanplumUserAttributesRequestBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LeanplumAPI {

    @POST("/api")
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    Call<LeanplumPostModel> postAction(
            @Query("action") @NonNull String action,
            @Body @NonNull LeanplumAddChannelRequestBody body
    );

    @POST("/api")
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    Call<LeanplumPostModel> postUserAttributesAction(
            @Query("action") @NonNull String action,
            @Body @NonNull LeanplumUserAttributesRequestBody body
    );
}