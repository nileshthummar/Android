package com.watchback2.android.models.genres;

import com.watchback2.android.models.Channel;

import java.io.Serializable;
import java.util.List;

public class AllGenresWrapper implements Serializable {

    private String genreName;
    private String genreLink;
    private List<Channel> channels;

    private final static long serialVersionUID = 0L;


    public AllGenresWrapper(String genreName, String genreLink, List<Channel> channels) {
        this.genreName = genreName;
        this.genreLink = genreLink;
        this.channels = channels;
    }

    public String getGenreName() {
        return genreName;
    }

    public String getGenreLink() {
        return genreLink;
    }

    public List<Channel> getChannels() {
        return channels;
    }
}
