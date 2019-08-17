package com.watchback2.android.models;

import com.perk.request.PerkResponse;
import com.watchback2.android.api.BrightcovePlaylistData;

/**
 * Created by perk on 27/03/18.
 */
public final class BrightcoveSearchFailedResponse extends PerkResponse<BrightcovePlaylistData> {

    /** Contains the encoded search query for which this response instance was generated */
    private String searchQuery;

    public BrightcoveSearchFailedResponse(String query) {
        searchQuery = query;
    }

    /**
     * Returns the encoded search query for which this response instance was generated
     */
    public String getSearchQuery() {
        return searchQuery;
    }
}
