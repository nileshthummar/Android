package com.watchback2.android.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewConfiguration;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.leanplum.Leanplum;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.adapters.PlayerVideoAdapter;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.controllers.ChannelsManager;
import com.watchback2.android.databinding.ActivityBrandDetailsBinding;
import com.watchback2.android.helper.Bindings;
import com.watchback2.android.models.Channel;
import com.watchback2.android.navigators.IBrandDetailsNavigator;
import com.watchback2.android.navigators.IVideosRecyclerViewNavigator;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.viewmodels.BrandDetailsViewModel;
import com.watchback2.android.views.BrandVideosDividerItemDecorator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrandDetailsActivity extends BaseThemeableActivity implements IBrandDetailsNavigator,
        IVideosRecyclerViewNavigator, SwipeRefreshLayout.OnRefreshListener,
        AppBarLayout.OnOffsetChangedListener {

    public static final String EXTRA_CHANNEL_UUID = "channel_uuid";

    /* default */ static final String EXTRA_CHANNEL_NAME = "channel_name";

    private static final String LOG_TAG = "BrandDetailsActivity";

    /**
     * The number of items remaining at the last position when we trigger data-loading
     */
    private static final int ITEM_THRESHOLD = 5;

    private Channel mChannel;

    private BrandDetailsViewModel mBrandDetailsViewModel;

    private ActivityBrandDetailsBinding mActivityBrandDetailsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String channelUuid = getIntent().getStringExtra(EXTRA_CHANNEL_UUID);
        if (TextUtils.isEmpty(channelUuid)) {
            PerkLogger.e(LOG_TAG, "Channel UUID not supplied! Cannot proceed!");
            finish();
            return;
        }

        mChannel = ChannelsManager.INSTANCE.findChannelByUuid(channelUuid);

        if (mChannel == null) {
            PerkLogger.e(LOG_TAG, "Channel not found! Cannot proceed!");
            finish();
            return;
        }

        mActivityBrandDetailsBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_brand_details);

        mBrandDetailsViewModel = new BrandDetailsViewModel(this, this, mChannel);
        mActivityBrandDetailsBinding.setViewModel(mBrandDetailsViewModel);
        mActivityBrandDetailsBinding.setChannel(mChannel);

        // Set up the RecyclerView:
        // Set orientation + divider
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        BrandVideosDividerItemDecorator itemDecoration = new BrandVideosDividerItemDecorator();
        itemDecoration.setDrawable(
                getResources().getDrawable(R.drawable.videos_list_divider_with_padding));

        // Set adapter
        PlayerVideoAdapter adapter = new PlayerVideoAdapter(new ArrayList<>(0), this, true);
        mActivityBrandDetailsBinding.idBrandVideosList.setLayoutManager(manager);
//        mActivityBrandDetailsBinding.idBrandVideosList.addItemDecoration(itemDecoration);
        mActivityBrandDetailsBinding.idBrandVideosList.setItemAnimator(new DefaultItemAnimator());
        mActivityBrandDetailsBinding.idBrandVideosList.setAdapter(adapter);

        ViewConfiguration vc = ViewConfiguration.get(this);
        //final int touchSlop = vc.getScaledTouchSlop();

        // Pagination: Load more data when we reach end-threshold item of the list:
        mActivityBrandDetailsBinding.idBrandVideosList.clearOnScrollListeners();
        mActivityBrandDetailsBinding.idBrandVideosList.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        if (dy <= 0) {
                            // Ignore if called when not scrolling down:
                            return;
                        }

                        if (mBrandDetailsViewModel == null) {
                            PerkLogger.e(LOG_TAG, "viewmodel is null!");
                            return;
                        }

                        int totalItemCount = manager.getItemCount();
                        int lastVisibleItem = manager.findLastVisibleItemPosition();

                        PerkLogger.d(LOG_TAG, "onScrolled: totalItemCount: " + totalItemCount
                                + " lastVisibleItem: " + lastVisibleItem + " loading: "
                                + mBrandDetailsViewModel.getDataLoading().get() + " hasNext: "
                                + mBrandDetailsViewModel.hasNext().get());

                        if (totalItemCount > 0 && !mBrandDetailsViewModel.getDataLoading().get()
                                && mBrandDetailsViewModel.hasNext().get() && totalItemCount <= (
                                lastVisibleItem + ITEM_THRESHOLD)) {
                            // end has almost been reached... load more:
                            loadNextVideos();
                        } else {
                            PerkLogger.d(LOG_TAG,
                                    "Not fetching next videos on scrolling as all "
                                            + "required conditions are not satisfied");
                        }
                    }
                });

        mActivityBrandDetailsBinding.swipeContainer.setOnRefreshListener(this);
        mActivityBrandDetailsBinding.swipeContainer.setColorSchemeResources(R.color.colorAccent,
                R.color.colorPrimary);

        mBrandDetailsViewModel.getDataLoading().addOnPropertyChangedCallback(
                new Observable.OnPropertyChangedCallback() {
                    @Override
                    public void onPropertyChanged(Observable sender, int propertyId) {
                        if (!mBrandDetailsViewModel.getDataLoading().get()) {
                            mActivityBrandDetailsBinding.swipeContainer.setRefreshing(false);
                        }
                    }
                });

        loadBrandVideos();

        mActivityBrandDetailsBinding.idBrandTitle.setOnClickListener(v -> {
            if (mActivityBrandDetailsBinding == null) {
                PerkLogger.e(LOG_TAG,
                        "on Title click: Ignoring as mActivityBrandDetailsBinding is null!");
                return;
            }

            if (mBrandDetailsViewModel != null
                    && mBrandDetailsViewModel.getDataLoading().get()) {
                PerkLogger.d(LOG_TAG, "Ignoring title click event when fetching videos!");
                return;
            }

            Bindings.smoothScrollToTop(mActivityBrandDetailsBinding.idBrandVideosList);

            // Expand the action-bar:
            mActivityBrandDetailsBinding.idBrandDetailsContainer.postDelayed(() -> {
                if (mActivityBrandDetailsBinding != null) {
                    mActivityBrandDetailsBinding.idBrandDetailsContainer.setExpanded(true);
                }
            }, 330);
        });

        try {
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title", "Brand Portal");
            contextData.put("tve.userpath", "Brand Portal");
            contextData.put("tve.contenthub", "Brand Portal");

            AdobeTracker.INSTANCE.trackState("Brand Portal", contextData);
            /////
        } catch (Exception e) {
        }

        // Event-tracking: 'Provider Details Screen' - When a user lands on the provider details screen for any video
        // Params:
        // 'Provider', value: channel uuid - channel name
        Bundle params = new Bundle();
        params.putString("Provider", mChannel.getUuid() + " - " + mChannel.getName());
        FacebookEventLogger.getInstance().logEventWithParams("Provider Details Screen", params);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityBrandDetailsBinding.idBrandDetailsContainer.addOnOffsetChangedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mActivityBrandDetailsBinding.idBrandDetailsContainer.removeOnOffsetChangedListener(this);
    }

    private void loadBrandVideos() {
        if (mBrandDetailsViewModel != null) {
            mBrandDetailsViewModel.loadVideos();
        }
    }

    private void loadNextVideos() {
        if (mBrandDetailsViewModel != null) {
            mBrandDetailsViewModel.loadNextVideos();
        }
    }

    @Override
    public void onBackClick() {
        onBackPressed();
    }

    @Override
    public void onButtonClick() {
        if (TextUtils.isEmpty(mChannel.getDestinationUrl())) {
            PerkLogger.e(LOG_TAG, "onButtonClick(): Got null/empty URL!");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mChannel.getDestinationUrl()),
                this, WatchbackWebViewActivity.class);
        intent.putExtra(WatchbackWebViewActivity.INTENT_TITLE_EXTRA_KEY, mChannel.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


        try {
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title", "Brand Portal");
            contextData.put("tve.userpath", "Click:Visit Site");
            contextData.put("tve.contenthub", "Brand Portal");
            contextData.put("tve.action", "true");
            AdobeTracker.INSTANCE.trackAction("Click:Visit Site", contextData);
            /////
        } catch (Exception e) {
        }
    }

    @Override
    public void onSortClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(R.array.sort_modes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mBrandDetailsViewModel.onSortModeChanged(
                        getResources().getStringArray(R.array.sort_modes)[which]);
                AppUtility.dismissDialog(dialog);
            }
        });
        builder.show();
    }

    @Override
    public void onRequestMoreInfoClick() {
        if (mBrandDetailsViewModel.isLoggedIn(this)) {
            // user logged in, call leanplum event
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("Channel", mChannel.getUuid());
            Leanplum.track("request_channel_info", params);

        } else {
            // show user login dialog
            AppUtility.showLoginDialog(this);
        }

        // Event-tracking: 'Provider Details-Request More Info'
        // - When a user hits the Request More Info button on the Provider Details Screen
        // Params:
        // 'Provider', value: channel uuid - channel name
        Bundle params = new Bundle();
        params.putString("Provider", mChannel.getUuid() + " - " + mChannel.getName());
        FacebookEventLogger.getInstance().logEventWithParams("Provider Details-Request More Info", params);

    }

    @Override
    public void onAddToFavoritesClick(boolean isAlreadyAdded) {
        mBrandDetailsViewModel.addToFavorites(this, mChannel, isAlreadyAdded);
    }

    @Override
    public void onSettingsClick() {
        // Settings icon is hidden from UI now - visibility set to GONE
        // Facebook Analytics: settings_tap - user taps on Settings:
       /* FacebookEventLogger.getInstance().logEvent("settings_tap");

        Intent intent = new Intent(BrandDetailsActivity.this, SettingsFragment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/
    }

    @Override
    public void handleVideoItemClick(BrightcovePlaylistData.BrightcoveVideo videoData) {
        if (videoData == null) {
            PerkLogger.e(LOG_TAG, "handleVideoItemClick: null videoData supplied!");
            return;
        }

        List<BrightcovePlaylistData.BrightcoveVideo> channelVideos =
                mBrandDetailsViewModel.getVideosList().get();
        if (channelVideos == null) {
            PerkLogger.e(LOG_TAG, "handleVideoItemClick: Videos list not available!");
            return;
        }

        VideoPlayerActivity.nCurrentIndex = channelVideos.indexOf(videoData);
        VideoPlayerActivity.arrVideos = channelVideos;

        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(VideoPlayerActivity.PLAYLIST_NAME, mChannel.getName());
        intent.putExtra(VideoPlayerActivity.IS_CHANNEL_LIST, true);
        startActivity(intent);


        try {
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title", "Brand Portal");
            contextData.put("tve.userpath", "Click:Video");
            contextData.put("tve.contenthub", "Brand Portal");
            String strModule = "Brand:1:" + (channelVideos.indexOf(videoData) + 1);
            contextData.put("tve.module", strModule);
            contextData.put("tve.action", "true");
            contextData = AdobeTracker.appendVideoData(contextData, videoData);
            AdobeTracker.INSTANCE.trackAction("Click:Video", contextData);
            /////
        } catch (Exception e) {
        }
    }

    @Override
    public void onRefresh() {
        // Clear the adapter in case of refresh to prevent crash seen when we swipe & immediately
        // scroll the RecyclerView
        if (mActivityBrandDetailsBinding != null
                && mActivityBrandDetailsBinding.idBrandVideosList.getAdapter() instanceof
                PlayerVideoAdapter) {
            ((PlayerVideoAdapter) mActivityBrandDetailsBinding.idBrandVideosList.getAdapter())
                    .replaceData(
                            new ArrayList<>(0));
        }
        loadBrandVideos();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (mActivityBrandDetailsBinding == null) {
            return;
        }

        // Ignore if already refreshing:
        if (mActivityBrandDetailsBinding.swipeContainer.isRefreshing()) {
            return;
        }

        if (verticalOffset == 0) {
            PerkLogger.d(LOG_TAG,
                    "onOffsetChanged: verticalOffset is 0... enabling SwipeRefreshLayout");
            mActivityBrandDetailsBinding.swipeContainer.post(
                    () -> mActivityBrandDetailsBinding.swipeContainer.setEnabled(true));
        } else {
            mActivityBrandDetailsBinding.swipeContainer.setEnabled(false);
        }
    }
}
