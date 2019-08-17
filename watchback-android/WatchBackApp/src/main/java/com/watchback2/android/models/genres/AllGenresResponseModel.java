package com.watchback2.android.models.genres;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.request.model.Data;
import com.watchback2.android.models.channels.ChannelsEntity;

import java.util.List;

public class AllGenresResponseModel extends Data {

    @Expose
    @SerializedName("channels")
    private List<ChannelsEntity> mChannels;
    @Expose
    @SerializedName("link")
    private String mLink;
    @Expose
    @SerializedName("genre")
    private String mGenre;

    public List<ChannelsEntity> getChannels() {
        return mChannels;
    }

    public String getLink() {
        return mLink;
    }

    public String getGenre() {
        return mGenre;
    }
}
