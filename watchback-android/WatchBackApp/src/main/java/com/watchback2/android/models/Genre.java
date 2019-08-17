package com.watchback2.android.models;

import java.io.Serializable;

public class Genre implements Serializable {

    private String mLink;
    private String mGenre;

    private final static long serialVersionUID = 0L;

    public Genre(String mLink, String mGenre) {
        this.mLink = mLink;
        this.mGenre = mGenre;
    }

    public String getLink() {
        return mLink;
    }

    public String getGenre() {
        return mGenre;
    }
}
