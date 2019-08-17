package com.watchback2.android.controllers;

/**
 * Created by perk on 05/Jun/19.
 * Manager to handle fetching & caching the carousel API data for Splash screen
 */

public final class SplashCarouselDataManager extends CarouselDataManager {

    private static final String LOG_TAG = "SplashCarouselMgr";

    private static final String CAROUSEL_PLACEMENT = "Splash Screen";

    public SplashCarouselDataManager() {
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
        return true;
    }
}
