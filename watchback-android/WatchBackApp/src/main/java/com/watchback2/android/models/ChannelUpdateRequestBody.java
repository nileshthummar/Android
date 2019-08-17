package com.watchback2.android.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChannelUpdateRequestBody {


    @SerializedName("channels")
    private List<String> mChannels;
    @SerializedName("access_token")
    private String mAccessToken;

    public List<String> getmChannels() {
        return mChannels;
    }

    public void setmChannels(List<String> mChannels) {
        this.mChannels = mChannels;
    }

    public String getmAccessToken() {
        return mAccessToken;
    }

    public void setmAccessToken(String mAccessToken) {
        this.mAccessToken = mAccessToken;
    }
}
