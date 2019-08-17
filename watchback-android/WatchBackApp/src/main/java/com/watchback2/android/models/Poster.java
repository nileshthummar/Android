package com.watchback2.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;

public class Poster implements Serializable {

    @SerializedName("src")
    @Expose
    private String src;
    @SerializedName("sources")
    @Expose
    private List<Source> sources = null;
    private final static long serialVersionUID = 1L;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("src", src).append("sources", sources).toString();
    }

}