package com.watchback2.android.models.movietickets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ScheduleEntity {
    @Expose
    @SerializedName("starts_at")
    private String mStartsAt;
    @Expose
    @SerializedName("ends_at")
    private String mEndsAt;

    public String getStartsAt() {
        return mStartsAt;
    }

    public String getEndsAt() {
        return mEndsAt;
    }
}
