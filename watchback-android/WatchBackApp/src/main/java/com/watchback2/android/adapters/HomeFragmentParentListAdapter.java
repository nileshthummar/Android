package com.watchback2.android.adapters;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.perk.util.PerkLogger;
import com.watchback2.android.activities.VideoPlayerActivity;
import com.watchback2.android.adapters.viewholders.CarouselVideoItemViewHolder;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.databinding.HomeCarouselItemBinding;
import com.watchback2.android.databinding.HomeFragmentRecyclerviewElementBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.relex.circleindicator.CircleIndicator2;

public class HomeFragmentParentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String LOG_TAG = "HomeFragPListAdapter";
    private static final int TYPE_VERTICAL = 2;
    private static final int TYPE_HORIZONTAL = 1;

    // 1st element - New in WB - this scrolls horizontally
    private List<BrightcovePlaylistData.BrightcoveVideo> mHorizontalScrollist;
    // all else left in the list - scrolls vertically
    private List<BrightcovePlaylistData.BrightcoveVideo> mVerticalScrollList;

    private Map<String, List<BrightcovePlaylistData.BrightcoveVideo>> allSublists = new HashMap<>();

    public HomeFragmentParentListAdapter(List<BrightcovePlaylistData.BrightcoveVideo> mHomeItemsList) {
        setList(mHomeItemsList);
    }

    public void replaceData(@NonNull List<BrightcovePlaylistData.BrightcoveVideo> homeItemsList) {
        setList(homeItemsList);
    }

    private void setList(List<BrightcovePlaylistData.BrightcoveVideo> homeItemsList) {
        if (homeItemsList == null || homeItemsList.size() == 0) {
            return;
        }
        // Title of first list : "new in watchback"
        List<BrightcovePlaylistData.BrightcoveVideo> horizontalScrollist = new ArrayList<>();
        List<BrightcovePlaylistData.BrightcoveVideo> verticalScrollList = new ArrayList<>();
        String firstListTitle = homeItemsList.get(0).getParentListTitle();
        for (BrightcovePlaylistData.BrightcoveVideo video : homeItemsList) {
            if (firstListTitle.equalsIgnoreCase(video.getParentListTitle())) {
                video.setFirstVideo(false); // so that title will always be gone
                horizontalScrollist.add(video);
            } else {
                verticalScrollList.add(video);
            }

            if(!allSublists.containsKey(video.getParentListTitle())){
                allSublists.put(video.getParentListTitle(), new ArrayList<>());
            }
            if (allSublists.get(video.getParentListTitle()) != null) {
                allSublists.get(video.getParentListTitle()).add(video);
            }
        }
        mHorizontalScrollist = horizontalScrollist;
        mVerticalScrollList = verticalScrollList;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_HORIZONTAL) {
            HomeFragmentRecyclerviewElementBinding videoItemBinding = HomeFragmentRecyclerviewElementBinding.inflate(
                    layoutInflater, parent,
                    false);

            return new HomeFragmentRecyclerViewHolder(videoItemBinding);
        } else {
            HomeCarouselItemBinding videoItemBinding = HomeCarouselItemBinding.inflate(
                    layoutInflater, parent,
                    false);
            return new CarouselVideoItemViewHolder(videoItemBinding, null, new CarouselVideoItemViewHolder.INotifyAdapter() {
                @Override
                public void notifyAdapterOfClick(BrightcovePlaylistData.BrightcoveVideo videoData) {
                    if(!TextUtils.isEmpty(videoData.getParentListTitle()) && allSublists.containsKey(videoData.getParentListTitle())){
                        List<BrightcovePlaylistData.BrightcoveVideo> channelVideos = allSublists.get(videoData.getParentListTitle());
                        VideoPlayerActivity.nCurrentIndex = channelVideos.indexOf(videoData);
                        VideoPlayerActivity.arrVideos = channelVideos;
                    }

                }
            });
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == 0 && mHorizontalScrollist == null || position == 1 && mVerticalScrollList == null) {
            PerkLogger.e(LOG_TAG,
                    "Returning as HomeScreenItem at position " + position + " is null");
            return;
        }

        if (position == 0) {
            ((HomeFragmentRecyclerViewHolder) holder).bind(mHorizontalScrollist, position);
        } else {
            BrightcovePlaylistData.BrightcoveVideo homeScreenItem = mVerticalScrollList.get(position - 1);
            ((CarouselVideoItemViewHolder) holder).bind(homeScreenItem, position);
        }
    }

    /*
     * This parent RecyclerView 2 types of elements
     * 1. 1st list that scrolls horizontally
     * 2. individual videos that are vertically scrollable
     * */
    @Override
    public int getItemCount() {
        return mVerticalScrollList != null ? mVerticalScrollList.size() + 1 : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HORIZONTAL;
        } else {
            return TYPE_VERTICAL;
        }
    }

    //    ----------------------------------------------------------------------------------------------
//    ViewHolder
//    ----------------------------------------------------------------------------------------------

    /*package*/ class HomeFragmentRecyclerViewHolder extends RecyclerView.ViewHolder {

        private HomeFragmentRecyclerviewElementBinding binding;
        private CircleIndicator2 indicator;

        private List<BrightcovePlaylistData.BrightcoveVideo> horizontalVideoList;
        final Handler handler = new Handler();
        int count = 0;
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(horizontalVideoList == null || binding == null ){
                    return;
                }
                if(count < horizontalVideoList.size()-1){
                    count = count + 1;
                }else{
                    count = 0;
                }
                binding.idList.smoothScrollToPosition(count);
            }
        };


        /*package*/ HomeFragmentRecyclerViewHolder(@NonNull HomeFragmentRecyclerviewElementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /*package*/ void bind(List<BrightcovePlaylistData.BrightcoveVideo> videoList, int position) {
            if (videoList == null || videoList.size() == 0) {
                return;
            }
            binding.idTitle.setText(videoList.get(0).getParentListTitle());
            HomeFragmentListAdapter adapter = new HomeFragmentListAdapter(new ArrayList<>(videoList));
            if (position == 0) {
                binding.idList.setLayoutManager(new LinearLayoutManager(binding.idList.getContext(), RecyclerView.HORIZONTAL, false));
            } else {
                binding.idList.setLayoutManager(new LinearLayoutManager(binding.idList.getContext()));
                binding.idList.setItemViewCacheSize(2);
            }
            binding.idList.setItemAnimator(new DefaultItemAnimator());
            binding.idList.setAdapter(adapter);

            if (position == 0 && indicator == null) {
                horizontalVideoList = videoList;
                PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
                pagerSnapHelper.attachToRecyclerView(binding.idList);

                indicator = binding.indicator;
                indicator.attachToRecyclerView(binding.idList, pagerSnapHelper);

                adapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());

                handler.postDelayed(runnable, 4000);

                binding.idList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if(newState == RecyclerView.SCROLL_STATE_IDLE){
                            count = recyclerView.getLayoutManager() == null ? 0 : ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                            handler.removeCallbacks(runnable);
                            handler.postDelayed(runnable, 4000);
                        }else if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
                            handler.removeCallbacks(runnable);
                        }

                    }

                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                    }
                });
            }
            binding.executePendingBindings();
        }

    }
}
