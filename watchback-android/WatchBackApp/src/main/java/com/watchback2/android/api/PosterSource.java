package com.watchback2.android.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class PosterSource implements Serializable {

    @SerializedName("src")
    @Expose
    private String src;
    private final static long serialVersionUID = -7108337682794237525L;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("src", src).toString();
    }
}