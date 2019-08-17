package com.watchback2.android.viewmodels;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableFloat;

import com.perk.request.auth.AuthAPIRequestController;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.api.PostFavChannels;
import com.watchback2.android.api.WatchbackAPI;
import com.watchback2.android.helper.UserInfoValidator;
import com.watchback2.android.models.Channel;
import com.watchback2.android.navigators.IBrandDetailsNavigator;
import com.watchback2.android.utils.PerkPreferencesManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by perk on 27/03/18.
 */

public class BrandDetailsViewModel extends VideosListViewModel {

    private static final String LOG_TAG = "BrandDetailsViewModel";
    private static final String REQUEST_MORE_INFO = "Request More Info";
    private static final String ADD_TO_MY_FAVORITES = "Add to My Favorites";
    private static final String EMAIL_SENT = "Email Sent";
    private static final String REMOVE_MY_FAVORITES = "Remove from My Favorites";

    private final IBrandDetailsNavigator mNavigator;

    private final ObservableField<String> currentSortMode = new ObservableField<>("");

    private final ObservableBoolean hasNextVideos = new ObservableBoolean(true);

    private int mNextOffset;

    private final Channel mChannel;

    private ObservableField<String> reqButtonText = new ObservableField<>(REQUEST_MORE_INFO);

    private ObservableField<String> favoritesButtonText = new ObservableField<>();

    private ObservableFloat favoritesButtonAlpha = new ObservableFloat();

    private boolean isFavoriteRequestInProgress = false;


    public BrandDetailsViewModel(@NonNull Context context,
                                 @NonNull IBrandDetailsNavigator navigator, @NonNull Channel channel) {
        super(context);

        mNavigator = navigator;

        // Default sort mode is most_recent
        getCurrentSortMode().set(context.getString(R.string.most_recent));

        mNextOffset = -1;

        mChannel = channel;
        List<Channel> userChannelListFromPreferences = PerkPreferencesManager.INSTANCE.getUserChannelListFromPreferences();
        if (PerkPreferencesManager.INSTANCE.areUserChannelsSavedToPreferences() && userChannelListFromPreferences != null && userChannelListFromPreferences.size() > 0) {
            for(Channel savedChannel: userChannelListFromPreferences){
                if(savedChannel.getUuid().equalsIgnoreCase(mChannel.getUuid())){
                    mChannel.setFavorite(true);
                    break;
                }
            }
        }
        //initial setup
        if (mChannel.isFavorite()) {
            favoritesButtonText.set(REMOVE_MY_FAVORITES);
            favoritesButtonAlpha.set(.70f);
        } else {
            favoritesButtonText.set(ADD_TO_MY_FAVORITES);
            favoritesButtonAlpha.set(1f);
        }
    }

    public void onBackClick(View view) {
        mNavigator.onBackClick();
    }

    public void onButtonClick(View view) {
        mNavigator.onButtonClick();
    }

    public void handleSortClick(View view) {
        mNavigator.onSortClick();
    }

    public void onSortModeChanged(@NonNull String newSortMode) {
        if (TextUtils.equals(newSortMode, getCurrentSortMode().get())) {
            PerkLogger.d(LOG_TAG,
                    "onSortModeChanged(): Ignoring as current sort-mode is same as provided "
                            + "sort-mode");
            return;
        }

        getCurrentSortMode().set(newSortMode);

        PerkLogger.d(LOG_TAG, "onSortModeChanged: sort-mode set to " + newSortMode);

        // refresh the videos-list so that it is sorted based on current sort-mode
        loadVideos();
    }

    @Override
    public void onVideosFetched(List<BrightcovePlaylistData.BrightcoveVideo> videoList) {
        PerkLogger.d(LOG_TAG,
                "onVideosFetched: size: " + (videoList != null ? videoList.size() : "0"));

        if (videoList == null || videoList.isEmpty()) {
            // We probably reached to the end of the list as no videos were returned... so set
            // the hasNext value to false in this case after showing a toast message
            /*Toast.makeText(getContext(),
                    getContext().getResources().getString(R.string.no_more_videos),
                    Toast.LENGTH_LONG).show();*/
            hasNext().set(false);

            // return in this case as we do not have any videos to show & we have reached the end
            // of the list already
            return;
        }

        // Update offset
        if (mNextOffset == -1) {
            // This is the first fetch...

            // mNextOffset = videoList.size();

            // We always set offset to the number of videos specified in 'limit' param, to
            // prevent duplicate videos, as mentioned in https://jira.rhythmone.com/browse/PEWAN-381
            // The offset is then incremented by every 'limit' value for each subsequent API call
            mNextOffset = WatchbackAPI.CHANNEL_DETAILS_DEFAULT_LIMIT;

            hasNext().set(true);

            PerkLogger.d(LOG_TAG,
                    "onVideosFetched: First load -Values set to: mNextOffset: " + mNextOffset);

            getVideosList().set(videoList);

        } else {
            // mNextOffset = mNextOffset + videoList.size();
            // See above comment inside if statement, for mNextOffset:
            mNextOffset = mNextOffset + WatchbackAPI.CHANNEL_DETAILS_DEFAULT_LIMIT;

            PerkLogger.d(LOG_TAG,
                    "onVideosFetched: (Fetched Next set of Videos) -Values set to: mNextOffset: "
                            + mNextOffset);

            if (getVideosList() != null) {
                final List<BrightcovePlaylistData.BrightcoveVideo> list = getVideosList().get();
                if (list != null) {
                    list.addAll(videoList);

                    // Create a copy so that the ObservableField triggers the listeners:
                    getVideosList().set(new ArrayList<>(list));
                }
            }
        }

        updateListEmptyState();
    }

    public ObservableField<String> getCurrentSortMode() {
        return currentSortMode;
    }

    public int getDropDownIconResourceId() {
        return R.drawable.ic_arrow_drop_down_white;
    }

    public void loadNextVideos() {
        PerkLogger.d(LOG_TAG, "loadNextVideos: mNextOffset = " + mNextOffset);
        loadChannelVideos(mChannel.getUuid(), mChannel.getName(), mChannel.getLogoImageUrl(),
                mNextOffset, getCurrentSortModeForAPI());
    }

    public ObservableBoolean hasNext() {
        return hasNextVideos;
    }

    public void loadVideos() {
        PerkLogger.d(LOG_TAG, "loading initial set of Videos... setting mNextOffset to -1");

        // Clear existing data so that we scroll to top:
        getVideosList().set(new ArrayList<>());
        updateListEmptyState();

        mNextOffset = -1;

        loadChannelVideos(mChannel.getUuid(), mChannel.getName(), mChannel.getLogoImageUrl(), 0,
                getCurrentSortModeForAPI());
    }

    private String getCurrentSortModeForAPI() {
        final String currentSortMode = getCurrentSortMode().get();

        if (TextUtils.equals(getContext().getResources().getString(R.string.most_popular),
                currentSortMode)) {
            return WatchbackAPI.SORT_MOST_POPULAR;
        } else if (TextUtils.equals(getContext().getResources().getString(R.string.length_shortest),
                currentSortMode)) {
            return WatchbackAPI.SORT_SHORTEST;
        }

        return WatchbackAPI.SORT_MOST_RECENT;
    }

    public ObservableField<String> getReqButtonText() {
        return reqButtonText;
    }

    public ObservableField<String> getFavoritesButtonText() {
        return favoritesButtonText;
    }

    public ObservableFloat getFavoritesButtonAlpha() {
        return favoritesButtonAlpha;
    }

    public void onRequestMoreInfoClick(View view) {
        if (view != null && ((Button) view).getText().equals(REQUEST_MORE_INFO)) {
            if(isLoggedIn(view.getContext())){
                reqButtonText.set(EMAIL_SENT);
                view.setBackground(view.getResources().getDrawable(R.drawable.rounded_corner));
                ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(view.getResources().getColor(R.color.disabled_btn_color)));
                view.setAlpha(.70f);
            }/*else{
                // do nothing, login popup will be shown by the activity
            }*/

            if (mNavigator != null) {
                mNavigator.onRequestMoreInfoClick();
            }
        }/*else{
            // do nothing - button disabled.
        }*/

    }

    public void onAddToMyFavoritesClick(View view) {
        if (mNavigator == null || view == null || isFavoriteRequestInProgress) {
            return;
        }
        if (((Button) view).getText().equals(ADD_TO_MY_FAVORITES)) {
            favoritesButtonText.set(REMOVE_MY_FAVORITES);
            favoritesButtonAlpha.set(.70f);
            mNavigator.onAddToFavoritesClick(false);

        } else {
            favoritesButtonText.set(ADD_TO_MY_FAVORITES);
            favoritesButtonAlpha.set(1f);
            mNavigator.onAddToFavoritesClick(true);
        }
    }

    public void onSettingsClick(View view) {
        if (mNavigator != null) {
            mNavigator.onSettingsClick();
        }
    }

    public boolean isLoggedIn(Context context) {
        return UserInfoValidator.isAuthenticated(
                AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(context));
    }

    public void addToFavorites(final Context context, Channel channel, boolean isAlreadyAdded) {
        List<Channel> channelList;
        if(PerkPreferencesManager.INSTANCE.areUserChannelsSavedToPreferences()){
            channelList = PerkPreferencesManager.INSTANCE.getUserChannelListFromPreferences();
        }else{
            channelList = new ArrayList<>();
        }
        if(!isAlreadyAdded){
            channelList.add(channel);
        }else{
            for (Iterator<Channel> iterator = channelList.iterator(); iterator.hasNext(); ) {
                Channel savedChannel = iterator.next();
                if (channel.getUuid().equals(savedChannel.getUuid())) {
                    iterator.remove();
                    channel.setFavorite(false);
                    break;
                }
            }
        }
        PerkPreferencesManager.INSTANCE.clearUserChannelsInPreference();
        if (channelList != null && channelList.size() > 0) {
            PerkPreferencesManager.INSTANCE.saveUserChannelsIntoPreference(channelList);
        }
        if (isLoggedIn(context) && channel != null) {
            PostFavChannels postFavChannels = new PostFavChannels(context);
            postFavChannels.setPostChannelCallback(new PostFavChannels.PostChannelCallback() {
                @Override
                public void onCompleted() {
                    isFavoriteRequestInProgress = false;
                    if (context != null) {
                        Toast.makeText(context, "Favorites updated", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            postFavChannels.makeCall(channelList);
            isFavoriteRequestInProgress = true;
        }
    }

}
