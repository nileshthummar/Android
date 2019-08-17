package com.watchback2.android.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.activities.VideoDetailsActivity;
import com.watchback2.android.activities.VideoPlayerActivity;
import com.watchback2.android.activities.WatchbackWebViewActivity;
import com.watchback2.android.adapters.PlayerVideoAdapter;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.databinding.FragmentAccountBinding;
import com.watchback2.android.navigators.IAccountFragmentNavigator;
import com.watchback2.android.navigators.ISettingsContainerNavigator;
import com.watchback2.android.navigators.IVideosRecyclerViewNavigator;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.viewmodels.AccountFragmentViewModel;
import com.watchback2.android.viewmodels.FragmentHeaderViewModel;
import com.watchback2.android.views.AccountRadioGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by perk on 03/04/18.
 */

public class AccountFragment extends Fragment implements ISettingsContainerNavigator,
        AccountRadioGroup.AccountRadioButtonSelectedListener, IVideosRecyclerViewNavigator {

    public static final String TAG = "AccountFragment";

    private FragmentAccountBinding mViewDataBinding;

    private AccountFragmentViewModel mAccountFragmentViewModel;

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    public AccountFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        PerkLogger.d(TAG, "onCreateView()");

        final View rootView = inflater.inflate(R.layout.fragment_account, container, false);

        if (mViewDataBinding == null) {
            mViewDataBinding = FragmentAccountBinding.bind(rootView);
        }

        if (getActivity() != null) {
            mAccountFragmentViewModel = new AccountFragmentViewModel(getActivity());
            if (getActivity() instanceof IAccountFragmentNavigator) {
                mAccountFragmentViewModel.setNavigator((IAccountFragmentNavigator)getActivity());
            }
            mViewDataBinding.setViewModel(mAccountFragmentViewModel);

            FragmentHeaderViewModel fragmentHeaderViewModel = new FragmentHeaderViewModel(getActivity());
            mViewDataBinding.setUserInfoViewModel(fragmentHeaderViewModel);
        }

        mViewDataBinding.setSettingsContainerNavigator(this);

        mViewDataBinding.idRadioGroup.idRadioGroup.setAccountRadioButtonSelectedListener(this);

        // Set height for background-image
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int desiredImageHeight = (int) (screenWidth * 0.645);    // Set to 64.5% of screen-width
        ViewGroup.LayoutParams layoutParams = mViewDataBinding.idCoverImage.getLayoutParams();
        layoutParams.height = desiredImageHeight;
        mViewDataBinding.idCoverImage.setLayoutParams(layoutParams);

        try{
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Profile:Your Watchback");
            contextData.put("tve.userpath","Profile:Your Watchback");
            contextData.put("tve.contenthub","Profile");

            AdobeTracker.INSTANCE.trackState("Profile:Your Watchback",contextData);
            /////
        }catch (Exception e){}
        return mViewDataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        PerkLogger.d(TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
        setupViews();
    }

    private void setupViews() {
        if (mViewDataBinding == null) {
            PerkLogger.e(TAG, "mViewDataBinding is null");
            return;
        }

        // Set up the RecyclerView:
        PlayerVideoAdapter adapter = new PlayerVideoAdapter(
                new ArrayList<BrightcovePlaylistData.BrightcoveVideo>(0), this, false);
        mViewDataBinding.idHistoryVideosList.setAdapter(adapter);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mViewDataBinding.idHistoryVideosList.setLayoutManager(manager);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), manager.getOrientation());
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.videos_list_divider_with_padding));
        mViewDataBinding.idHistoryVideosList.addItemDecoration(itemDecoration);

        mViewDataBinding.idHistoryVideosList.setItemAnimator(new DefaultItemAnimator());

        // Select History tab on being shown:
        mViewDataBinding.idRadioGroup.idRadioGroup.selectHistoryButton();
    }

    @Override
    public void onStop() {
        PerkLogger.d(TAG, "onStop()");

        // Select History button here, so that it is selected on return to this UI:
        if (mViewDataBinding != null && mAccountFragmentViewModel != null
                && !mAccountFragmentViewModel.getIsHistoryTab().get()) {
            mViewDataBinding.idRadioGroup.idRadioGroup.selectHistoryButton();
        }

        super.onStop();
    }

    @Override
    public void handleSettingsClick(View view) {
        // Facebook Analytics: settings_tap - user taps on Settings on account screen:
        /*FacebookEventLogger.getInstance().logEvent("settings_tap");

        Intent intent = new Intent(view.getContext(), SettingsFragment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/
    }

    @Override
    public void handleEditClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(WatchbackWebViewActivity.PROFILE_URL), view.getContext(),
                WatchbackWebViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(WatchbackWebViewActivity.INTENT_TITLE_EXTRA_KEY, getString(R.string.edit));
        intent.putExtra(WatchbackWebViewActivity.INTENT_PAGE_NEEDS_TOKEN, true);
        startActivity(intent);

        try{
            // Facebook Analytics: edit_profile_tap - user taps on Edit Profile on account screen:
            FacebookEventLogger.getInstance().logEvent("edit_profile_tap");

            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Profile:Edit");
            contextData.put("tve.userpath","Profile:Edit");
            contextData.put("tve.contenthub","Profile");

            AdobeTracker.INSTANCE.trackState("Profile:Edit",contextData);
            /////
        }catch (Exception e){}
    }

    @Override
    public void onUsageSelected() {
        if (mAccountFragmentViewModel == null) {
            PerkLogger.e(TAG, "mAccountFragmentViewModel is null");
            return;
        }

        mAccountFragmentViewModel.getIsHistoryTab().set(false);
        expandAppBar();

        // Facebook Analytics: your_profile_tap - user taps on Profile on account screen:
        FacebookEventLogger.getInstance().logEvent("your_profile_tap");
    }

    @Override
    public void onHistorySelected() {
        if (mAccountFragmentViewModel == null) {
            PerkLogger.e(TAG, "mAccountFragmentViewModel is null");
            return;
        }

        mAccountFragmentViewModel.getIsHistoryTab().set(true);

        expandAppBar();

        if (!isVisible()) {
            PerkLogger.d(TAG, "onHistorySelected: Ignoring when view is not visible");
            return;
        }

        mAccountFragmentViewModel.loadUserVideoHistory();

        try{
            // Facebook Analytics: your_history_tap - user taps on Your History on account screen:
            FacebookEventLogger.getInstance().logEvent("your_history_tap");

            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Profile:Your History");
            contextData.put("tve.userpath","Profile:Your History");
            contextData.put("tve.contenthub","Profile");

            AdobeTracker.INSTANCE.trackState("Profile:Your History",contextData);
            /////
        }catch (Exception e){}
    }

    private void expandAppBar() {
        mViewDataBinding.appbar.setExpanded(true, true);
    }

    @Override
    public void handleVideoItemClick(BrightcovePlaylistData.BrightcoveVideo videoData) {
        if (videoData == null) {
            PerkLogger.e(TAG, "handleVideoItemClick: null videoData supplied!");
            return;
        }

        if (mAccountFragmentViewModel == null || getContext() == null) {
            PerkLogger.e(TAG, "handleVideoItemClick: mAccountFragmentViewModel OR context unavailable!");
            return;
        }

        List<BrightcovePlaylistData.BrightcoveVideo> videoList = mAccountFragmentViewModel.getVideosHistoryList().get();

        if (videoList == null) {
            PerkLogger.e(TAG, "handleVideoItemClick: VideosHistoryList unavailable!");
            return;
        }

        VideoPlayerActivity.nCurrentIndex = videoList.indexOf(videoData);
        VideoPlayerActivity.arrVideos = new ArrayList<>(videoList);

        String title = mAccountFragmentViewModel.getNoHistoryResults().get()
                ? getContext().getResources().getString(R.string.recommended_for_you)
                : getContext().getResources().getString(R.string.your_history);

        /*Intent intent = new Intent(getContext(), VideoPlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(VideoPlayerActivity.PLAYLIST_NAME, title);
        intent.putExtra(VideoPlayerActivity.IS_CHANNEL_LIST, false);
        startActivity(intent);*/

        try{
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Profile:Your History");
            contextData.put("tve.userpath","Click:Video");
            contextData.put("tve.contenthub","Profile");

            String strModule =  "home:1:"+(videoList.indexOf(videoData)+1);
            contextData.put("tve.module",strModule);
            contextData.put("tve.action","true");

            contextData = AdobeTracker.appendVideoData(contextData,videoData);

            AdobeTracker.INSTANCE.trackAction("Click:Video",contextData);
            /////
        }catch (Exception e){}


        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(title)) {
            bundle.putString(VideoPlayerActivity.PLAYLIST_NAME, title);
        }
        bundle.putBoolean(VideoPlayerActivity.IS_CHANNEL_LIST, false);
        bundle.putBoolean(VideoDetailsActivity.IS_CAROUSAL_ITEM, false);
        AppUtility.showVideoDetailsActivity(videoData, bundle);
    }
}
