package com.watchback2.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.request.model.Data;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class GetChannelsModel extends Data {

    @SerializedName("channels")
    @Expose
    private List<Channel> channels = null;
    private final static long serialVersionUID = 2748375861171625737L;

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("channels", channels).toString();
    }
}