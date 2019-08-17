package com.watchback2.android.models.channels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GenresEntity {
    @Expose
    @SerializedName("link")
    private String mLink;
    @Expose
    @SerializedName("genre")
    private String mGenre;

    public String getLink() {
        return mLink;
    }

    public String getGenre() {
        return mGenre;
    }
}
