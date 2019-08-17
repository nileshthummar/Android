package com.watchback2.android.controllers;

/**
 * Created by perk on 10/11/18.
 *  * Manager to handle fetching & caching the carousel API data for Sweeps screen
 */

public final class SweepsCarouselDataManager extends CarouselDataManager {

    /**
     * Singleton instance of the class that would be used for accessing SweepsCarouselDataManager.
     */
    public static final SweepsCarouselDataManager
            INSTANCE = new SweepsCarouselDataManager();

    private static final String LOG_TAG = "SweepsCarouselMgr";

    private static final String CAROUSEL_PLACEMENT_SWEEPS = "Sweeps Screen";

    private SweepsCarouselDataManager() {
        // Private constructor for Singleton class
        super();
    }

    @Override
    protected String getTag() {
        return LOG_TAG;
    }

    @Override
    protected String getCarouselPlacement() {
        return CAROUSEL_PLACEMENT_SWEEPS;
    }

    @Override
    protected boolean shouldUseV2Carousel() {
        return false;
    }
}
