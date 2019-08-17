package com.watchback2.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class Images implements Serializable {

    @SerializedName("poster")
    @Expose
    private Poster poster;
    @SerializedName("thumbnail")
    @Expose
    private Thumbnail thumbnail;

    private final static long serialVersionUID = 1L;

    public Poster getPoster() {
        return poster;
    }

    public void setPoster(Poster poster) {
        this.poster = poster;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("poster", poster).append("thumbnail",
                thumbnail).toString();
    }

}