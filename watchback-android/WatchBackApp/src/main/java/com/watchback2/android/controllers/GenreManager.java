package com.watchback2.android.controllers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.request.auth.AuthAPIRequestController;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.api.WatchbackAPIController;
import com.watchback2.android.helper.UserInfoValidator;
import com.watchback2.android.models.genres.AllGenresResponseModel;
import com.watchback2.android.models.genres.AllGenresWrapper;
import com.watchback2.android.models.genres.FavoritesResponseModel;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkPreferencesManager;
import com.watchback2.android.utils.PerkUtils;
import com.watchback2.android.utils.WrapResponseModels;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenreManager {

    public static final GenreManager INSTANCE = new GenreManager();

    private static final String LOG_TAG = "GenreManager";

    public static final String MY_FAVORITE = "My Favorites";

    private boolean isGenreRequestInProgress;
    private boolean isFavRequestInProgress;
    private boolean notifyOnOnlyFavoriteFetch = false;

    private int callCount = 2;
    private AllGenresWrapper myFavorites = null;

    private final ObservableField<Map<String, AllGenresWrapper>> allGenresData = new ObservableField<>();
    private final ObservableField<Map<String, AllGenresWrapper>> allGenresDataWithFavorites = new ObservableField<>();

    private FavoriteListFetchedCallback favoriteListFetchedCallback;

    //  ----------------------------------------------------------------------------------------------
//  GenreListFetchedCallback interface
//  ----------------------------------------------------------------------------------------------
    public interface GenreListFetchedCallback {
        void onGenreFetched(Map<String, AllGenresWrapper> allGenreList);
    }

    //  ----------------------------------------------------------------------------------------------
//  FavoriteListFetchedCallback interface
//  ----------------------------------------------------------------------------------------------
    public interface FavoriteListFetchedCallback {
        void onFavFetched(Map<String, AllGenresWrapper> faveList);
    }

    //  ----------------------------------------------------------------------------------------------
//  Private constructor
//  ----------------------------------------------------------------------------------------------
    private GenreManager() {
        // Private constructor for Singleton class
        super();
    }
//  ----------------------------------------------------------------------------------------------
//  public methods
//  ----------------------------------------------------------------------------------------------

    /**
     * Gets the list of Genres with channel info (no video info)
     *
     * @param context
     * @param forceRefresh
     * @param genreListFetchedCallback
     */
    public void getAllGenreList(@NonNull Context context, boolean forceRefresh,
                                @Nullable GenreListFetchedCallback genreListFetchedCallback) {

        if (allGenresData.get() == null || forceRefresh) {
            getAllGenrewithChannels(context, genreListFetchedCallback);
        } else {
            if (genreListFetchedCallback != null) {
                genreListFetchedCallback.onGenreFetched(allGenresData.get());
            }
        }
    }

    public void getAllGenreWithFavorites(@NonNull Context context, boolean forceRefresh, FavoriteListFetchedCallback favoriteListFetchedCallback) {
        this.favoriteListFetchedCallback = favoriteListFetchedCallback;
        notifyOnOnlyFavoriteFetch = false;
        if (allGenresData.get() == null || allGenresDataWithFavorites.get() == null || forceRefresh) {

            callCount = 2;

            getAllGenrewithChannels(context, null);
            getUserFavorites(context);

        } else {
            if (favoriteListFetchedCallback != null) {
                favoriteListFetchedCallback.onFavFetched(allGenresDataWithFavorites.get());
            }
        }

    }

    public void getUserFavorites(Context context, FavoriteListFetchedCallback favoriteListFetchedCallback){
        this.favoriteListFetchedCallback = favoriteListFetchedCallback;
        notifyOnOnlyFavoriteFetch = true;
        getUserFavorites(context);
    }

    public Map<String, AllGenresWrapper> getLoadedGenreWithFavs() {
        if (allGenresDataWithFavorites.get() != null) {
            if (PerkPreferencesManager.INSTANCE.getUserChannelListFromPreferences() != null) {
                allGenresDataWithFavorites.get().put(MY_FAVORITE, new AllGenresWrapper(MY_FAVORITE, "",
                        PerkPreferencesManager.INSTANCE.getUserChannelListFromPreferences()));
            } else {
                if (allGenresDataWithFavorites.get().containsKey(MY_FAVORITE)) {
                    allGenresDataWithFavorites.get().remove(MY_FAVORITE);
                }
            }

            return allGenresDataWithFavorites.get();
        }
        return null;
    }


//  ----------------------------------------------------------------------------------------------
//  private methods
//  ----------------------------------------------------------------------------------------------

    private void createListWithFavorites() {
        Map<String, AllGenresWrapper> favMap = new HashMap<>();

        if (allGenresData.get() == null && myFavorites == null) {
            // do nothing - toast already shown
            return;
        }

        if (myFavorites != null && myFavorites.getChannels() != null && myFavorites.getChannels().size() > 0) {
            favMap.put(myFavorites.getGenreName(), myFavorites);
        }

        if (allGenresData.get() != null) {
            favMap.putAll(allGenresData.get());

        }
        allGenresDataWithFavorites.set(favMap);
        if (favoriteListFetchedCallback != null) {
            favoriteListFetchedCallback.onFavFetched(allGenresDataWithFavorites.get());
        }
    }

    private void getUserFavorites(@NonNull Context context) {
        final boolean isLoggedIn = UserInfoValidator.isAuthenticated(
                AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(context));

        if (!isLoggedIn) {
            myFavorites = new AllGenresWrapper(MY_FAVORITE, "",
                    PerkPreferencesManager.INSTANCE.getUserChannelListFromPreferences());
            coutDown();
            return;
        }

        if (!AppUtility.isNetworkAvailable(context)) {
            PerkLogger.d(LOG_TAG, "load favorites: Returning as network is unavailable!");
            isFavRequestInProgress = false;
            coutDown();
            return;
        }

        if (isFavRequestInProgress) {
            PerkLogger.d(LOG_TAG, "load favorites: Returning as request is already in progress!");
            return;
        }

        isFavRequestInProgress = true;

        WatchbackAPIController.INSTANCE.getFavoriteChannels(context, new OnRequestFinishedListener<FavoritesResponseModel>() {
            @Override
            public void onSuccess(@NonNull FavoritesResponseModel favoritesResponseModel, @Nullable String s) {
                isFavRequestInProgress = false;

                PerkLogger.d(LOG_TAG, "Loading favorites data a success");

                PerkPreferencesManager.INSTANCE.saveUserChannelsIntoPreference(WrapResponseModels.getWrappedChannelList(favoritesResponseModel.getChannels()));

                myFavorites = new AllGenresWrapper(MY_FAVORITE, "",
                        WrapResponseModels.getWrappedChannelList(favoritesResponseModel.getChannels()));

                if(notifyOnOnlyFavoriteFetch && favoriteListFetchedCallback != null){
                    notifyOnOnlyFavoriteFetch = false;
                    Map<String, AllGenresWrapper> map = new HashMap<>();
                    map.put(MY_FAVORITE, myFavorites);
                    favoriteListFetchedCallback.onFavFetched(map);
                }
                coutDown();

            }

            @Override
            public void onFailure(@NonNull ErrorType errorType, @Nullable PerkResponse<FavoritesResponseModel> perkResponse) {
                isFavRequestInProgress = false;
                if(notifyOnOnlyFavoriteFetch && favoriteListFetchedCallback != null){
                    notifyOnOnlyFavoriteFetch = false;
                    favoriteListFetchedCallback.onFavFetched(null);
                }
                PerkLogger.e(LOG_TAG,
                        "Loading favorites data failed:" + errorType + (perkResponse != null
                                ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));

                PerkUtils.showErrorMessageToast(errorType,
                        (perkResponse != null ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));
                coutDown();
            }
        });
    }


    /**
     * This fetches all the genres and all channels under each of them
     * The channel data contained in the response generally do not have video information.
     *
     * @param context context from which the call is made
     */
    private void getAllGenrewithChannels(Context context,
                                         @Nullable GenreListFetchedCallback genreListFetchedCallback) {

        if (!AppUtility.isNetworkAvailable(context)) {
            PerkLogger.d(LOG_TAG, "load all Genre: Returning as network is unavailable!");
            isGenreRequestInProgress = false;
            coutDown();
            return;
        }

        if (isGenreRequestInProgress) {
            PerkLogger.d(LOG_TAG, "load all Genre: Returning as request is already in progress!");
            return;
        }

        isGenreRequestInProgress = true;

        WatchbackAPIController.INSTANCE.getAllGenresWithChannels(context, new OnRequestFinishedListener<List<AllGenresResponseModel>>() {
            @Override
            public void onSuccess(@NonNull List<AllGenresResponseModel> allGenresResponseModelList, @Nullable String s) {
                isGenreRequestInProgress = false;

                PerkLogger.d(LOG_TAG, "Loading all Genres with channels a success");

                // TODO: refactor this to a list.
                // Originally written this way to easily read channels as per Genres but that requirement
                // changed and turns out Genre names are not unique (there is no other unique
                // element available for genre; like uuid), so, this is an hack to prevent data being over written.
                Map<String, AllGenresWrapper> genreMap = new HashMap<>(allGenresResponseModelList.size());
                int i = 0;
                for (AllGenresResponseModel allGenresResponseModel : allGenresResponseModelList) {
                    AllGenresWrapper allGenresWrapper = WrapResponseModels.wrapAllGenreResponse(allGenresResponseModel);
                    if (allGenresWrapper != null) {
                        genreMap.put(Integer.toString(i), allGenresWrapper);
                        i++;
                    }
                }

                allGenresData.set(genreMap);

                if (genreListFetchedCallback != null) {
                    genreListFetchedCallback.onGenreFetched(allGenresData.get());
                }
                coutDown();
            }

            @Override
            public void onFailure(@NonNull ErrorType errorType, @Nullable PerkResponse<List<AllGenresResponseModel>> perkResponse) {
                isGenreRequestInProgress = false;
                PerkLogger.e(LOG_TAG,
                        "Loading all-genres data failed: " + errorType + (perkResponse != null
                                ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));

                PerkUtils.showErrorMessageToast(errorType,
                        (perkResponse != null ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));
                coutDown();
            }
        });
    }

    private void coutDown() {
        callCount--;
        if (callCount == 0) {
            createListWithFavorites();
        }
    }


}
