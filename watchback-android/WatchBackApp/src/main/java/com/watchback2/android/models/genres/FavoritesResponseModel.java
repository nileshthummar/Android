package com.watchback2.android.models.genres;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.request.model.Data;
import com.watchback2.android.models.channels.ChannelsEntity;

import java.util.List;

public class FavoritesResponseModel extends Data {

    @Expose
    @SerializedName("channels")
    private List<ChannelsEntity> mChannels;

    public List<ChannelsEntity> getChannels() {
        return mChannels;
    }
}
