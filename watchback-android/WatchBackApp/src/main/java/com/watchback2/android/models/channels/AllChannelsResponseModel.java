package com.watchback2.android.models.channels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.request.model.Data;

import java.util.List;

public class AllChannelsResponseModel extends Data {

    @Expose
    @SerializedName("channels")
    private List<ChannelsEntity> mChannels;

    public List<ChannelsEntity> getChannels() {
        return mChannels;
    }
}
