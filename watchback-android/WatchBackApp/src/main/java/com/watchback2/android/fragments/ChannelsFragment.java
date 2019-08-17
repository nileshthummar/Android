package com.watchback2.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.adapters.channelsfragment.ChannelsGenreListItemAdapter;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.databinding.FragmentChannelsBinding;
import com.watchback2.android.helper.Bindings;
import com.watchback2.android.models.Channel;
import com.watchback2.android.navigators.ISettingsContainerNavigator;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkPreferencesManager;
import com.watchback2.android.viewmodels.ChannelsFragmentViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by perk on 15/3/18
 */

public class ChannelsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ISettingsContainerNavigator {

    public static final String TAG = "ChannelsFragment";

    private FragmentChannelsBinding mViewDataBinding;

    private ChannelsFragmentViewModel mChannelsFragmentViewModel;

    public static ChannelsFragment newInstance() {
        return new ChannelsFragment();
    }

    public ChannelsFragment() {
        super();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_channels, container, false);

        if (mViewDataBinding == null) {
            mViewDataBinding = FragmentChannelsBinding.bind(rootView);
        }

        if (getActivity() != null) {
            mChannelsFragmentViewModel = new ChannelsFragmentViewModel(
                    getActivity().getApplicationContext());
            mViewDataBinding.setViewModel(mChannelsFragmentViewModel);
        }
        mViewDataBinding.setSettingsContainerNavigator(this);
        mViewDataBinding.swipeContainer.setOnRefreshListener(this);
        mViewDataBinding.swipeContainer.setColorSchemeResources(R.color.colorAccent,
                R.color.colorPrimary);

        /////
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("tve.title","Channel Playlist");
        contextData.put("tve.userpath","Channel Playlist");
        contextData.put("tve.contenthub","Channel Playlist");

        AdobeTracker.INSTANCE.trackState("Channel Playlist",contextData);
        /////

        // Facebook Analytics: Subscribe Screen - When a user lands on the Subscribe Screen
        FacebookEventLogger.getInstance().logEvent("Subscribe Screen");

        return mViewDataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update favorites
        List<Channel> favChannelList = PerkPreferencesManager.INSTANCE.getUserChannelListFromPreferences();
        if (favChannelList != null && favChannelList.size() > 0 ) {
            mChannelsFragmentViewModel.getFavListVisibility().set(View.VISIBLE);
            mChannelsFragmentViewModel.getFavList().set(favChannelList);

        } else{
            mChannelsFragmentViewModel.getFavListVisibility().set(View.GONE);
        }

    }

    private void setupViews() {
        // Set up the RecyclerView:
        if (getContext() == null || !AppUtility.isNetworkAvailable(getContext())) {
            PerkLogger.e(TAG, "Unable to proceed due to null context OR no-network");
            return;
        }

        ChannelsGenreListItemAdapter adapter = new ChannelsGenreListItemAdapter(
                new ArrayList<Channel>(0), getContext(), true, new OnChannelItemClicked() {
            @Override
            public void onItemClicked(Channel channel) {
                PerkLogger.d(TAG, channel.getName());
                AppUtility.handleProtocolUri("watchback://channel:"+channel.getUuid());
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        mViewDataBinding.idChannelsListFavorite.setLayoutManager(manager);
        mViewDataBinding.idChannelsListFavorite.setItemAnimator(new DefaultItemAnimator());
        mViewDataBinding.idChannelsListFavorite.setItemViewCacheSize(1);
        mViewDataBinding.idChannelsListFavorite.setAdapter(adapter);

        ChannelsGenreListItemAdapter itemAdapter = new ChannelsGenreListItemAdapter(new ArrayList<Channel>(), getContext(), false,new OnChannelItemClicked(){
            @Override
            public void onItemClicked(Channel channel) {
                PerkLogger.d(TAG, channel.getName());
                AppUtility.handleProtocolUri("watchback://channel:"+channel.getUuid());
            }
        });
        mViewDataBinding.idAllChannelsList.setLayoutManager(new GridLayoutManager(getContext(),3));
        mViewDataBinding.idAllChannelsList.setItemAnimator(new DefaultItemAnimator());
        mViewDataBinding.idAllChannelsList.setItemViewCacheSize(1);
        mViewDataBinding.idAllChannelsList.setAdapter(itemAdapter);

        // Pagination: Load more data when we reach the last item of the list:
        mViewDataBinding.idAllChannelsList.clearOnScrollListeners();
        mViewDataBinding.idAllChannelsList.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        if (dy <= 0) {
                            // Ignore if called when not scrolling down:
                            return;
                        }

                        if (mChannelsFragmentViewModel == null) {
                            PerkLogger.e(TAG, "mChannelsFragmentViewModel is null!");
                            return;
                        }

                        int totalItemCount = manager.getItemCount();
                        int lastVisibleItem = manager.findLastVisibleItemPosition();

                        PerkLogger.d(TAG, "onScrolled: totalItemCount: " + totalItemCount
                                + " lastVisibleItem: " + lastVisibleItem + " loading: "
                                + mChannelsFragmentViewModel.getDataLoading().get() + " hasNext: "
                                + mChannelsFragmentViewModel.hasNext().get());

                        if (totalItemCount > 0 && !mChannelsFragmentViewModel.getDataLoading().get()
                                && mChannelsFragmentViewModel.hasNext().get() && totalItemCount ==
                                lastVisibleItem + 1) {

                            // end has almost been reached... load more:
                            if (mChannelsFragmentViewModel != null) {
                                mChannelsFragmentViewModel.loadNextChannels();
                            }
                        } else {
                            PerkLogger.d(TAG,
                                    "Not fetching next videos on scrolling as all "
                                            + "required conditions are not satisfied");
                        }
                    }
                });

        loadChannels(false);
    }
    @Override
    public void onRefresh() {
        // Clear the adapter in case of refresh to prevent crash seen when we swipe & immediately
        // scroll the RecyclerView
        if (mViewDataBinding != null
                && mViewDataBinding.idAllChannelsList.getAdapter() instanceof
                ChannelsGenreListItemAdapter) {
            ((ChannelsGenreListItemAdapter) mViewDataBinding.idAllChannelsList.getAdapter())
                    .replaceData(
                    new ArrayList<>(0));
        }
        if (mViewDataBinding != null
                && mViewDataBinding.idChannelsListFavorite.getAdapter() instanceof
                ChannelsGenreListItemAdapter) {
            ((ChannelsGenreListItemAdapter) mViewDataBinding.idChannelsListFavorite.getAdapter())
                    .replaceData(
                            new ArrayList<>(0));
        }
        loadChannels(true);
    }

    private void loadChannels(boolean force) {
        Bindings.scrollToTop(mViewDataBinding.idChannelsListFavorite);
        Bindings.scrollToTop(mViewDataBinding.idAllChannelsList);

        if (mChannelsFragmentViewModel != null) {
            mChannelsFragmentViewModel.loadChannels(force);
        }
    }
//    ----------------------------------------------------------------------------------------------
//    ISettingsContainerNavigator Implementation
//    ----------------------------------------------------------------------------------------------

    @Override
    public void handleSettingsClick(View view) {
        // Settings icon is hidden from UI now - visibility set to GONE
        // Facebook Analytics: settings_tap - user taps on Settings:
        /*FacebookEventLogger.getInstance().logEvent("settings_tap");

        Intent intent = new Intent(view.getContext(), SettingsFragment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/
    }

    @Override
    public void handleEditClick(View view) {
     // not used
    }

//    ----------------------------------------------------------------------------------------------
//    Interface to notify when a channel item is clicked
//    ----------------------------------------------------------------------------------------------

    public interface OnChannelItemClicked{
        void onItemClicked(Channel channel);
    }

}
