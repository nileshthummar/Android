package com.watchback2.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.watchback2.android.api.ThumbnailSource;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;

public class Thumbnail implements Serializable {

    @SerializedName("src")
    @Expose
    private String src;
    @SerializedName("sources")
    @Expose
    private List<ThumbnailSource> sources = null;
    private final static long serialVersionUID = 1L;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public List<ThumbnailSource> getSources() {
        return sources;
    }

    public void setSources(List<ThumbnailSource> sources) {
        this.sources = sources;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("src", src).append("sources", sources).toString();
    }

}