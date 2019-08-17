package com.watchback2.android.viewmodels;

import android.content.Context;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.annotation.NonNull;

import com.perk.util.PerkLogger;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.controllers.HomeScreenItemsListManager;
import com.watchback2.android.models.HomeScreenItem;
import com.watchback2.android.navigators.ICommonViewModelLoadHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by perk on 15/03/18.
 * ViewModel class for HomeFragment
 */

public class HomeFragmentViewModel implements ICommonViewModelLoadHelper {

    private static final String LOG_TAG = "HomeFragmentViewModel";

    private final Context mContext;

    private final ObservableBoolean dataLoading = new ObservableBoolean(false);

    private final ObservableBoolean internalDataLoading = new ObservableBoolean(false);

    private final ObservableField<List<BrightcovePlaylistData.BrightcoveVideo>> homeItemsList = new ObservableField<>();

    public HomeFragmentViewModel(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    public void loadHomeItems(boolean forceRefresh) {
        if (getDataLoading().get()) {
            return;
        }

        PerkLogger.d(LOG_TAG, "Loading Homescreen items list... forceRefresh: " + forceRefresh);

        getDataLoading().set(true);

        if (getHomeItemsList().get() != null) {
            getHomeItemsList().set(new ArrayList<>(0));
        }

        HomeScreenItemsListManager.INSTANCE.fetchHomeScreenItemList(mContext, forceRefresh,
                homeScreenItemList -> {
                    getDataLoading().set(false);

                    if (homeScreenItemList != null && !homeScreenItemList.isEmpty()) {
                        PerkLogger.d(LOG_TAG, "Loading Homescreen items list successful!");
                        ArrayList<BrightcovePlaylistData.BrightcoveVideo> videoList = new ArrayList<>();
                        for (HomeScreenItem item : homeScreenItemList) {
                            if (item.getVideos() != null && item.getVideos().size() > 0) {
                                item.getVideos().get(0).setFirstVideo(true);

                                // So that, if needed, we'll know which list a video belongs to.
                                for(BrightcovePlaylistData.BrightcoveVideo video : item.getVideos()){
                                    video.setParentListTitle(item.getName());
                                }
                                videoList.addAll(item.getVideos());
                            }

                        }
                        getHomeItemsList().set(new ArrayList<>(videoList));
                    } else {
                        PerkLogger.e(LOG_TAG, "Loading Homescreen items list failed!");
                    }
                });
    }

    public ObservableBoolean getDataLoading() {
        return dataLoading;
    }

    public ObservableField<List<BrightcovePlaylistData.BrightcoveVideo>> getHomeItemsList() {
        return homeItemsList;
    }

    /* package */ Context getContext() {
        return mContext;
    }

    public ObservableBoolean getInternalDataLoading() {
        return internalDataLoading;
    }

    @Override
    public void toggleLoading(boolean loading) {
        getInternalDataLoading().set(loading);
    }

    @Override
    public boolean isLoading() {
        return getInternalDataLoading().get();
    }
}
