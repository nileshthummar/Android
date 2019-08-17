package com.watchback2.android.viewmodels;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.perk.util.PerkLogger;
import com.watchback2.android.controllers.ChannelsManager;
import com.watchback2.android.models.Channel;
import com.watchback2.android.models.genres.AllGenresWrapper;
import com.watchback2.android.utils.PerkPreferencesManager;
import com.watchback2.android.utils.PerkUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by perk on 15/03/18.
 * ViewModel class for ChannelsFragment
 */

public class ChannelsFragmentViewModel {

    private static final String LOG_TAG = "ChannelsFragmentViewModel";

    private final Context mContext;

    private final ObservableBoolean dataLoading = new ObservableBoolean(false);

    private final ObservableBoolean loadingNext = new ObservableBoolean(false);

    private final ObservableBoolean hasNextVideos = new ObservableBoolean(true);

    private int mNextOffset;

    private final ObservableField<List<Channel>> channelItemsList = new ObservableField<>();
    private final ObservableField<List<AllGenresWrapper>> genreList = new ObservableField<>();
    private final ObservableField<List<Channel>> favList = new ObservableField<>();

    //visibility observables
    private final ObservableInt shimmerVisibility = new ObservableInt(View.GONE);
    private final ObservableInt genreListVisibility = new ObservableInt(View.GONE);
    private final ObservableInt favListVisibility = new ObservableInt(View.GONE);

    public ChannelsFragmentViewModel(@NonNull Context context) {
        mContext = context.getApplicationContext();

        mNextOffset = -1;
    }

    public void loadChannels(boolean forceRefresh) {
        if (getDataLoading().get()) {
            return;
        }

        PerkLogger.d(LOG_TAG, "loading initial set of channels screen data... setting mNextOffset to -1");

        mNextOffset = -1;

        getDataLoading().set(true);

        if (getGenreList().get() != null) {
            getGenreList().set(new ArrayList<>(0));
        }

        /*GenreManager.INSTANCE.getAllGenreWithFavorites(mContext, forceRefresh, genreMap -> {
            PerkLogger.d(LOG_TAG, "Loading channels screen data successful");

            getDataLoading().set(false);

            onGenreDataFetched(genreMap);
        });
        */
        ChannelsManager.INSTANCE.fetchAllChannelsListWithoutVideos(mContext, PerkUtils.isUserLoggedIn(), new ChannelsManager.ChannelListFetchedCallback() {
            @Override
            public void onChannelListFetched(@Nullable List<Channel> channelList) {
                PerkLogger.d(LOG_TAG, "Loading channels screen data successful");
                getDataLoading().set(false);
                if (channelList != null && channelList.size() > 0) {
                    onGenreDataFetched(channelList);
                }
            }
        });

    }

    private void onGenreDataFetched(List<Channel> channelList) {
        List<Channel> favoriteChannels = new ArrayList<>();

        favoriteChannels = PerkPreferencesManager.INSTANCE.getUserChannelListFromPreferences();

        if(favoriteChannels != null && favoriteChannels.size() > 0){
            favList.set(favoriteChannels);
            favListVisibility.set(View.VISIBLE);
        }else{
            favListVisibility.set(View.GONE);
        }
        getChannelItemsList().set(channelList);

        /*if (genreMap.get(GenreManager.MY_FAVORITE) != null && genreMap.get(GenreManager.MY_FAVORITE).getChannels().size() > 0) {
            favList.set(genreMap.get(GenreManager.MY_FAVORITE).getChannels());
            favListVisibility.set(View.VISIBLE);
        }else{
            favListVisibility.set(View.GONE);
        }

        SortedMap<String, Channel> uniqueList = new TreeMap<>();
        for(AllGenresWrapper item: genreMap.values()){
            if(item.getChannels() == null){
                continue;
            }
            for(Channel channel: item.getChannels()){
                uniqueList.put(channel.getName(), channel);
            }
        }

        getChannelItemsList().set(new ArrayList<>(uniqueList.values()));*/

    }

    public void loadNextChannels() {
        if (getLoadingNext().get()) {
            return;
        }

        PerkLogger.d(LOG_TAG, "loadNextChannels: mNextOffset = " + mNextOffset);

        getLoadingNext().set(true);

        ChannelsManager.INSTANCE.loadNextChannels(mContext, mNextOffset,
                new ChannelsManager.NextChannelsListFetchedCallback() {
                    @Override
                    public void onSuccess(@Nullable List<Channel> channelList) {
                        PerkLogger.d(LOG_TAG,
                                "loadNextChannels: Loading next set of channels screen data "
                                        + "successful");

                        getLoadingNext().set(false);

                        onChannelsFetched(channelList);
                    }

                    @Override
                    public void onFailure() {
                        PerkLogger.e(LOG_TAG, "loadNextChannels: Loading channels data failed!");

                        getLoadingNext().set(false);
                    }
                });
    }

    private void onChannelsFetched(List<Channel> channelList) {
        PerkLogger.d(LOG_TAG,
                "onChannelsFetched: size: " + (channelList != null ? channelList.size() : "0"));

        if (channelList == null || channelList.isEmpty()) {
            // We probably reached to the end of the list as no channels were returned... so set
            // the hasNext value to false in this case
            hasNext().set(false);

            // return in this case as we do not have any channels to show & we have reached the end
            // of the list already
            return;
        }

        // Update offset
        if (mNextOffset == -1) {
            // This is the first fetch...
            mNextOffset = channelList.size();

            hasNext().set(true);

            PerkLogger.d(LOG_TAG,
                    "onChannelsFetched: First load -Values set to: mNextOffset: " + mNextOffset);

            getChannelItemsList().set(new ArrayList<>(channelList));

        } else {
            mNextOffset = mNextOffset + channelList.size();

            PerkLogger.d(LOG_TAG,
                    "onChannelsFetched: (Fetched Next set of channels) -Values set to: mNextOffset: "
                            + mNextOffset);

            if (getChannelItemsList() != null) {
                final List<Channel> channels = getChannelItemsList().get();
                if (channels != null) {
                    channels.addAll(channelList);

                    // Create a copy so that the ObservableField triggers the listeners:
                    getChannelItemsList().set(new ArrayList<>(channels));
                }
            }
        }
    }

    public ObservableBoolean getDataLoading() {
        return dataLoading;
    }

    public ObservableBoolean getLoadingNext() {
        return loadingNext;
    }

    public ObservableField<List<Channel>> getChannelItemsList() {
        return channelItemsList;
    }

    public ObservableField<List<AllGenresWrapper>> getGenreList() {
        return genreList;
    }

    public ObservableBoolean hasNext() {
        return hasNextVideos;
    }

    /* package */ Context getContext() {
        return mContext;
    }

    public ObservableInt getShimmerVisibility() {
        return shimmerVisibility;
    }

    public ObservableInt getGenreListVisibility() {
        return genreListVisibility;
    }

    public ObservableField<List<Channel>> getFavList() {
        return favList;
    }

    public ObservableInt getFavListVisibility() {
        return favListVisibility;
    }
}
