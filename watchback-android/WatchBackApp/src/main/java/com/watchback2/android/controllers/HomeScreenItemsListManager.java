package com.watchback2.android.controllers;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.request.auth.AuthAPIRequestController;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.adapters.HomeFragmentListAdapter;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.api.WatchbackAPIController;
import com.watchback2.android.helper.UserInfoValidator;
import com.watchback2.android.models.HomeScreenItem;
import com.watchback2.android.utils.AbstractVideoDataCache;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by perk on 06/21/18.
 * Manager to handle fetching list of the HomeScreen items
 */

public final class HomeScreenItemsListManager extends AbstractVideoDataCache {

    /**
     * Singleton instance of the class that would be used for accessing RecommendedVideosManager.
     */
    public static final HomeScreenItemsListManager INSTANCE = new HomeScreenItemsListManager();

    private static final String LOG_TAG = "HomeScreenItemsMgr";

    private final ObservableField<List<HomeScreenItem>>
            mHomeScreenItemList = new ObservableField<>();

    private boolean isRequestInProgress;

    @Nullable
    private String mProtocolUri;

    private HomeScreenItemsListFetchedCallback mCallback;

    public interface HomeScreenItemsListFetchedCallback {
        void onHomeScreenItemsListFetched(
                @Nullable List<HomeScreenItem> homeScreenItemList);
    }

    private HomeScreenItemsListManager() {
        // Private constructor for Singleton class
        super();
    }

    private void fetchHomeScreenItems(@NonNull final Context context) {
        if (!AppUtility.isNetworkAvailable(context)) {
            PerkLogger.d(LOG_TAG, "fetchHomeScreenItems: Returning as network is unavailable!");
            isRequestInProgress = false;
            notifyCallback();
            return;
        }

        if (isRequestInProgress) {
            PerkLogger.d(LOG_TAG,
                    "fetchHomeScreenItems: Returning as request is already in progress!");
            return;
        }

        PerkLogger.d(LOG_TAG, "Loading Homescreen items list...");

        isRequestInProgress = true;

        if (mHomeScreenItemList.get() != null) {
            invalidateHomeScreenItemList();
        }

        final boolean isLoggedIn = UserInfoValidator.isAuthenticated(
                AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(context));

        final List<String> selectedInterests = new ArrayList<>();

        // No interests/channels selection: https://jira.rhythmone.com/browse/PEWAN-487
        // When using app without logging in, the interests are to be sent manually to the
        // homescreen API:
        /*if (!isLoggedIn) {
            // Get the selected-interests first:
            List<Interest> interestList =
                    PerkPreferencesManager.INSTANCE
                            .getUserInterestsListFromPreferences();

            if (interestList != null) {
                for (Interest interest : interestList) {
                    if (interest != null && interest.getSelected() && !TextUtils.isEmpty(
                            interest.getId())) {
                        selectedInterests.add(interest.getId());
                    }
                }

                *//*String interests = selectedInterests.isEmpty() ? null : TextUtils.join(",",
                        selectedInterests);*//*
            }

            PerkLogger.d(LOG_TAG, "selectedInterests set to:\n" + selectedInterests.toString());
        }*/

        WatchbackAPIController.INSTANCE.getHomescreenList(context, selectedInterests,
                new OnRequestFinishedListener<List<HomeScreenItem>>() {

                    @Override
                    public void onSuccess(@NonNull List<HomeScreenItem> homeScreenItems,
                            @Nullable String s) {
                        PerkLogger.d(LOG_TAG, "Loading Homescreen items list successful!");

                        isRequestInProgress = false;

                        if (homeScreenItems.isEmpty()) {
                            PerkLogger.e(LOG_TAG, "Empty list returned for homescreen!");
                            mHomeScreenItemList.set(homeScreenItems);
                            notifyCallback();
                            return;
                        }

                        mHomeScreenItemList.set(homeScreenItems);
//                        removeWatchedLongformIfNeeded();
                        cacheVideoDataFor(DUMMY_KEY, null);
                        notifyCallback();
                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType,
                            @Nullable PerkResponse<List<HomeScreenItem>> perkResponse) {
                        isRequestInProgress = false;

                        PerkLogger.e(LOG_TAG,
                                "Loading Homescreen items list failed! " + (perkResponse != null
                                        ? perkResponse.getMessage() : ""));

                        PerkUtils.showErrorMessageToast(errorType,
                                (perkResponse != null ? perkResponse.getMessage()
                                        : context.getString(R.string.generic_error)));

                        notifyCallback();
                    }
                });
    }

    public void fetchHomeScreenItemList(@NonNull Context context, boolean forceRefresh,
            @Nullable HomeScreenItemsListFetchedCallback callback) {
        mCallback = callback;

        List<HomeScreenItem> itemList = mHomeScreenItemList.get();

        if (itemList == null || itemList.isEmpty() || forceRefresh) {
            fetchHomeScreenItems(context);
        } else {
            notifyCallback();
        }
    }

    private void notifyCallback() {
        if (mCallback != null) {
            mCallback.onHomeScreenItemsListFetched(mHomeScreenItemList.get());
            mCallback = null;
        }
    }

    public void invalidateHomeScreenItemList() {
        // Clear cache in this case
        clearCache();

        PerkLogger.d(LOG_TAG, "Invalidating home-screen item list!");
        mHomeScreenItemList.set(new ArrayList<>(0));
    }

    public void setProtocolUri(String protocolUri) {
        synchronized (HomeScreenItemsListManager.INSTANCE) {
            mProtocolUri = protocolUri;
            PerkLogger.d(LOG_TAG, "setProtocolUri: mProtocolUri set to: " + mProtocolUri);
        }
    }

    @Nullable
    public String getProtocolUri() {
        synchronized (HomeScreenItemsListManager.INSTANCE) {
            return mProtocolUri;
        }
    }

    @Override
    public void expired(Object key, Object value) {
        PerkLogger.d(LOG_TAG, "Expired invoked with key: " + key + " value: " + value);

        if (key instanceof String && TextUtils.equals(DUMMY_KEY, (String)key)) {

            if (isRequestInProgress) {
                PerkLogger.w(LOG_TAG,
                        "Ignoring expired callback since request to fetch home-screen items is "
                                + "already in progress");
                return;
            }

            PerkLogger.d(LOG_TAG, "Clearing cached home-screen items data on expiry");
            invalidateHomeScreenItemList();
        }
    }

    public void removeWatchedLongformIfNeeded() {
        int longformCap = WatchBackSettingsController.INSTANCE.getLongformCapSetting();

        PerkLogger.d(LOG_TAG, "removeWatchedLongformIfNeeded: Got longformCap = " + longformCap);

        if (longformCap == -1) {
            PerkLogger.d(LOG_TAG,
                    "removeWatchedLongformIfNeeded: Ignoring as longformCap is not available!");
            return;
        }

        final List<HomeScreenItem> homeScreenItems = mHomeScreenItemList.get();

        if (homeScreenItems == null) {
            PerkLogger.e(LOG_TAG, "removeWatchedLongformIfNeeded: homeScreenItems is null!");
            return;
        }

        PerkLogger.d(LOG_TAG,
                "removeWatchedLongformIfNeeded: Size of homeScreenItems before removal: "
                        + homeScreenItems.size());

        // Reset, to allow change notifications to be sent:
        mHomeScreenItemList.set(new ArrayList<>(0));

        for (Iterator<HomeScreenItem> iterator = homeScreenItems.iterator(); iterator.hasNext(); ) {
            final HomeScreenItem item = iterator.next();

            if (item == null) {
                continue;
            }

            BrightcovePlaylistData.BrightcoveVideo video = item.getFeatured();

            if (HomeFragmentListAdapter.FEATURED.equalsIgnoreCase(item.getType()) && video != null
                    && AppUtility.isVideoWatchedUntilCap(video, longformCap)) {
                PerkLogger.d(LOG_TAG, "removeWatchedLongformIfNeeded: Removing watched video: "
                        + video.toString());
                iterator.remove();
            }
        }

        PerkLogger.d(LOG_TAG,
                "removeWatchedLongformIfNeeded: Size of homeScreenItems after removal: "
                        + homeScreenItems.size());

        mHomeScreenItemList.set(homeScreenItems);
    }
}
