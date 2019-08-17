package com.watchback2.android.utils;

import androidx.annotation.Nullable;

import com.watchback2.android.api.BrightcovePlaylistData;

import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by perk on 09/05/18.
 * Abstract class that handles caching of data
 */

public abstract class AbstractVideoDataCache implements ExpirationListener<Object, Object> {

    protected static final String DUMMY_KEY = "0";

    private final ExpiringMap<String, List<BrightcovePlaylistData.BrightcoveVideo>> mCacheMap;

    protected AbstractVideoDataCache() {
        mCacheMap = ExpiringMap.builder()
                .expiration(5, TimeUnit.MINUTES)
                .expirationPolicy(ExpirationPolicy.CREATED)
                .asyncExpirationListener(this)
                .build();
    }

    @Nullable
    public List<BrightcovePlaylistData.BrightcoveVideo> getCachedVideoDataFor(String key) {
        return mCacheMap.get(key);
    }

    public void cacheVideoDataFor(String key, List<BrightcovePlaylistData.BrightcoveVideo> videoList) {
        mCacheMap.put(key, videoList);
    }

    public void clearCache() {
        mCacheMap.clear();
    }
}
