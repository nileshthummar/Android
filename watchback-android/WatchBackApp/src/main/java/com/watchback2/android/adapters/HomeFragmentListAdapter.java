package com.watchback2.android.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.perk.util.PerkLogger;
import com.watchback2.android.activities.VideoPlayerActivity;
import com.watchback2.android.adapters.viewholders.CarouselVideoItemViewHolder;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.databinding.HomeCarouselItemBinding;

import java.util.List;

/**
 * Created by perk on 15/3/18.
 * Adapter class for main home-fragment recycler view which contains other RecyclerView as items
 */

public class HomeFragmentListAdapter extends RecyclerView.Adapter<CarouselVideoItemViewHolder> {

    public static final String FEATURED = "FEATURED";

    private static final String LOG_TAG = "HomeFragmentListAdapter";

    private List<BrightcovePlaylistData.BrightcoveVideo> mHomeItemsList;

    public HomeFragmentListAdapter(@NonNull List<BrightcovePlaylistData.BrightcoveVideo> homeItemsList) {
        super();
        setList(homeItemsList);
    }

    @NonNull
    @Override
    public CarouselVideoItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        HomeCarouselItemBinding videoItemBinding = HomeCarouselItemBinding.inflate(
                layoutInflater, parent,
                false);
        return new CarouselVideoItemViewHolder(videoItemBinding, null, new CarouselVideoItemViewHolder.INotifyAdapter() {
            @Override
            public void notifyAdapterOfClick(BrightcovePlaylistData.BrightcoveVideo videoData) {
                VideoPlayerActivity.nCurrentIndex = mHomeItemsList.indexOf(videoData);
                VideoPlayerActivity.arrVideos = mHomeItemsList;
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselVideoItemViewHolder holder, int position) {
        if (mHomeItemsList.get(position) == null) {
            PerkLogger.e(LOG_TAG,
                    "Returning as HomeScreenItem at position " + position + " is null");
            return;
        }
        BrightcovePlaylistData.BrightcoveVideo homeScreenItem = mHomeItemsList.get(position);
        holder.bind(homeScreenItem, position);
    }

    @Override
    public int getItemCount() {
        return mHomeItemsList != null ? mHomeItemsList.size() : 0;
    }

    private void setList(@NonNull List<BrightcovePlaylistData.BrightcoveVideo> homeItemsList) {
        mHomeItemsList = homeItemsList;
        notifyDataSetChanged();
    }

    public void replaceData(@NonNull List<BrightcovePlaylistData.BrightcoveVideo> homeItemsList) {
        setList(homeItemsList);
    }
}
