package com.watchback2.android.controllers;

/**
 * Created by perk on 09/21/18.
 * Manager to handle fetching & caching the carousel API data for case when user is unregistered
 */

public final class UnregisteredUserCarouselDataManager extends CarouselDataManager {

    /**
     * Singleton instance of the class that would be used for accessing UnregisteredUserCarouselDataManager.
     */
    public static final UnregisteredUserCarouselDataManager
            INSTANCE = new UnregisteredUserCarouselDataManager();

    private static final String LOG_TAG = "UnregisteredUsrMgr";

    private static final String CAROUSEL_PLACEMENT = "Logged Out Screens";

    private UnregisteredUserCarouselDataManager() {
        // Private constructor for Singleton class
        super();
    }

    @Override
    protected String getTag() {
        return LOG_TAG;
    }

    @Override
    protected String getCarouselPlacement() {
        return CAROUSEL_PLACEMENT;
    }

    @Override
    protected boolean shouldUseV2Carousel() {
        return false;
    }
}
