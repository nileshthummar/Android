package com.watchback2.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.adapters.HomeFragmentParentListAdapter;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.controllers.WatchBackSettingsController;
import com.watchback2.android.databinding.FragmentHomeBinding;
import com.watchback2.android.navigators.ISettingsContainerNavigator;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.viewmodels.HomeFragmentViewModel;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by perk on 15/03/18.
 */

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "HomeFragment";

    private FragmentHomeBinding mViewDataBinding;

    private HomeFragmentViewModel mHomeFragmentViewModel;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    public HomeFragment() {
        super();
        PerkLogger.d(TAG, "Constructor this: " + HomeFragment.this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        PerkLogger.d(TAG, "onCreateView() this: " + HomeFragment.this);

        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        if (mViewDataBinding == null) {
            mViewDataBinding = FragmentHomeBinding.bind(rootView);
        }

        if (getActivity() != null) {
            mHomeFragmentViewModel = new HomeFragmentViewModel(
                    getActivity().getApplicationContext());
            mViewDataBinding.setViewModel(mHomeFragmentViewModel);
        }

        mViewDataBinding.swipeContainer.setOnRefreshListener(this);
        mViewDataBinding.swipeContainer.setColorSchemeResources(R.color.colorAccent,
                R.color.colorPrimary);
       try{
           /////
           HashMap<String, Object> contextData = new HashMap<String, Object>();
           contextData.put("tve.title","Featured");
           contextData.put("tve.userpath","Featured");
           contextData.put("tve.contenthub","Featured");
           contextData.put("tve.contentmode","Portrait");

           AdobeTracker.INSTANCE.trackState("Featured",contextData);
           /////
       }catch (Exception e){}

        // Facebook Analytics: Discover Screen - When a user lands on the Discover/Home screen
        FacebookEventLogger.getInstance().logEvent("Discover Screen");

        return mViewDataBinding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        PerkLogger.d(TAG, "onCreate() this: " + HomeFragment.this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        PerkLogger.d(TAG, "onViewCreated() this: " + HomeFragment.this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        PerkLogger.d(TAG, "onActivityCreated() this: " + HomeFragment.this);
        super.onActivityCreated(savedInstanceState);

        setupViews();
    }

    private void setupViews() {
        initHeader();

        // Set up the RecyclerView:
        if (getContext() == null || !AppUtility.isNetworkAvailable(getContext())) {
            PerkLogger.e(TAG, "Unable to proceed due to null context OR no-network");
            return;
        }

        // Fetch the video-play settings:
        WatchBackSettingsController.INSTANCE.refreshSettings(getContext().getApplicationContext());

        HomeFragmentParentListAdapter adapter = new HomeFragmentParentListAdapter(
                new ArrayList<BrightcovePlaylistData.BrightcoveVideo>(0));
        mViewDataBinding.idHomeList.setAdapter(adapter);
        mViewDataBinding.idHomeList.setLayoutManager(new LinearLayoutManager(getContext()));
        mViewDataBinding.idHomeList.setItemAnimator(new DefaultItemAnimator());
    }

    private void initHeader() {
        if (getActivity() instanceof ISettingsContainerNavigator) {
            mViewDataBinding.idHeader.setSettingsContainerNavigator((ISettingsContainerNavigator)getActivity());
        }
    }

    @Override
    public void onStart() {
        PerkLogger.d(TAG, "onStart() this: " + HomeFragment.this);
        super.onStart();

        if (mHomeFragmentViewModel != null) {
            mHomeFragmentViewModel.loadHomeItems(false);
        }
    }

    @Override
    public void onResume() {
        PerkLogger.d(TAG, "onResume() this: " + HomeFragment.this);
        super.onResume();
    }

    @Override
    public void onPause() {
        PerkLogger.d(TAG, "onPause() this: " + HomeFragment.this);
        super.onPause();
    }

    @Override
    public void onStop() {
        PerkLogger.d(TAG, "onStop() this: " + HomeFragment.this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        PerkLogger.d(TAG, "onDestroy() this: " + HomeFragment.this);
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        PerkLogger.d(TAG, "onDestroyView() this: " + HomeFragment.this);
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        PerkLogger.d(TAG, "onAttach() this: " + HomeFragment.this);
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        PerkLogger.d(TAG, "onDetach() this: " + HomeFragment.this);
        super.onDetach();
    }

    @Override
    public void onRefresh() {
        // Clear the adapter in case of refresh to prevent crash seen when we swipe & immediately
        // scroll the RecyclerView
        if (mViewDataBinding != null
                && mViewDataBinding.idHomeList.getAdapter() instanceof
                HomeFragmentParentListAdapter) {
            ((HomeFragmentParentListAdapter) mViewDataBinding.idHomeList.getAdapter()).replaceData(
                    new ArrayList<>(0));
        }

        if (mHomeFragmentViewModel != null) {
            mHomeFragmentViewModel.loadHomeItems(true);
        }
    }
}
