package com.watchback2.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class Schedule implements Serializable {

    @SerializedName("ends_at")
    @Expose
    private String endsAt;
    @SerializedName("starts_at")
    @Expose
    private String startsAt;
    private final static long serialVersionUID = 1L;

    public String getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(String endsAt) {
        this.endsAt = endsAt;
    }

    public String getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(String startsAt) {
        this.startsAt = startsAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("endsAt", endsAt).append("startsAt",
                startsAt).toString();
    }

}