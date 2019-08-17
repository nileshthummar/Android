package com.watchback2.android.controllers;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.api.CarouselData;
import com.watchback2.android.api.WatchbackAPIController;
import com.watchback2.android.utils.AbstractVideoDataCache;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkUtils;

/**
 * Created by perk on 10/11/18.
 * Abstract manager to handle fetching & caching data for different carousels
 */

public abstract class CarouselDataManager extends AbstractVideoDataCache {

    private final String LOG_TAG;

    private final String CAROUSEL_PLACEMENT;

    protected final ObservableField<CarouselData.Carousel>
            mCarouselData = new ObservableField<>();

    private boolean isRequestInProgress;

    private CarouselDataFetchedCallback mCallback;

    public interface CarouselDataFetchedCallback {
        void onCarouselDataFetched(@Nullable CarouselData.Carousel carouselData);
    }

    protected abstract boolean shouldUseV2Carousel();

    protected abstract String getTag();

    protected abstract String getCarouselPlacement();

    protected CarouselDataManager() {
        super();
        LOG_TAG = getTag();
        CAROUSEL_PLACEMENT = getCarouselPlacement();
    }

    private void fetchCarouselData(@NonNull final Context context) {
        if (!AppUtility.isNetworkAvailable(context)) {
            PerkLogger.d(LOG_TAG, "fetchCarouselData: Returning as network is unavailable!");
            isRequestInProgress = false;
            notifyCallback();
            return;
        }

        if (isRequestInProgress) {
            PerkLogger.d(LOG_TAG,
                    "fetchCarouselData: Returning as request is already in progress!");
            return;
        }

        PerkLogger.d(LOG_TAG,
                "Loading carousel data for (placement=" + CAROUSEL_PLACEMENT + ")...");

        isRequestInProgress = true;

        if (mCarouselData.get() != null) {
            invalidateCarouselData();
        }

        WatchbackAPIController.INSTANCE.getCarousel(context, CAROUSEL_PLACEMENT,
                new OnRequestFinishedListener<CarouselData>() {

                    @Override
                    public void onSuccess(@NonNull CarouselData carouselData, @Nullable String s) {

                        PerkLogger.d(LOG_TAG,
                                "Loading carousel data for placement: " + CAROUSEL_PLACEMENT
                                        + " successful!");

                        isRequestInProgress = false;

                        if (carouselData.getCarousel() == null
                                || carouselData.getCarousel().getItems() == null
                                || carouselData.getCarousel().getItems().isEmpty()) {

                            PerkLogger.e(LOG_TAG,
                                    "Carousel data invalid/unavailable!");

                            Toast.makeText(context,
                                    context.getResources().getString(R.string.generic_error),
                                    Toast.LENGTH_SHORT).show();

                            notifyCallback();
                            return;
                        }

                        mCarouselData.set(carouselData.getCarousel());
                        cacheVideoDataFor(DUMMY_KEY, null);
                        notifyCallback();
                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType,
                            @Nullable PerkResponse<CarouselData> perkResponse) {

                        isRequestInProgress = false;

                        PerkLogger.e(LOG_TAG,
                                "Loading carousel data for placement: " + CAROUSEL_PLACEMENT
                                        + " failed! " + (perkResponse != null
                                        ? perkResponse.getMessage() : ""));

                        PerkUtils.showErrorMessageToast(errorType,
                                (perkResponse != null ? perkResponse.getMessage()
                                        : context.getResources().getString(
                                                R.string.generic_error)));

                        notifyCallback();
                    }
                });
    }

    public void fetchCarouselData(@NonNull Context context, boolean force,
            @Nullable CarouselDataFetchedCallback callback) {
        mCallback = callback;

        CarouselData.Carousel carouselData = mCarouselData.get();

        if (force || carouselData == null) {
            fetchCarouselData(context);
        } else {
            notifyCallback();
        }
    }

    private void notifyCallback() {
        if (mCallback != null) {
            mCallback.onCarouselDataFetched(mCarouselData.get());
            mCallback = null;
        }
    }

    private void invalidateCarouselData() {
        // Clear cache in this case
        clearCache();

        PerkLogger.d(LOG_TAG, "Invalidating Carousel-data!");
        mCarouselData.set(null);
    }

    @Override
    public void expired(Object key, Object value) {
        PerkLogger.d(LOG_TAG, "Expired invoked with key: " + key + " value: " + value);

        if (key instanceof String && TextUtils.equals(DUMMY_KEY, (String)key)) {

            if (isRequestInProgress) {
                PerkLogger.w(LOG_TAG,
                        "Ignoring expired callback since request to fetch carousel data is "
                                + "already in progress");
                return;
            }

            PerkLogger.d(LOG_TAG, "Clearing cached carousel data on expiry");
            invalidateCarouselData();
        }
    }
}
