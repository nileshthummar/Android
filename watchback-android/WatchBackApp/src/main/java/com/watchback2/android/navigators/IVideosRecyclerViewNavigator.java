package com.watchback2.android.navigators;

import com.watchback2.android.api.BrightcovePlaylistData;

/**
 * Created by perk on 15/03/18.
 */

public interface IVideosRecyclerViewNavigator {

    void handleVideoItemClick(BrightcovePlaylistData.BrightcoveVideo videoData);
}
