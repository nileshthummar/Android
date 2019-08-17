package com.watchback2.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.request.model.Data;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class WatchBackSettingsModel extends Data {

    @SerializedName("settings")
    @Expose
    private Settings settings;
    private final static long serialVersionUID = 1L;

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("settings", settings).toString();
    }
}