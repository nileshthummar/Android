package com.watchback2.android.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.watchback2.android.databinding.LayoutRecyclerviewHistoryListitemBinding;
import com.watchback2.android.models.movietickets.VideoHistoryModel;

import java.util.List;

/**
 * Adapter that maintains the list of videos watched in MovieTicketsFragment
 */
public class VideoHistoryAdapter extends RecyclerView.Adapter<VideoHistoryAdapter.VideoHistoryViewHolder> {

    private List<VideoHistoryModel> videoList;

    public VideoHistoryAdapter(List<VideoHistoryModel> videoList) {
        this.videoList = videoList;
    }

    @NonNull
    @Override
    public VideoHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        LayoutRecyclerviewHistoryListitemBinding binding = LayoutRecyclerviewHistoryListitemBinding.inflate(layoutInflater, parent, false);
        return new VideoHistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoHistoryViewHolder holder, int position) {
        holder.bind(videoList.get(position));
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public void setList(@NonNull List<VideoHistoryModel> videoList) {
        this.videoList = videoList;
        notifyDataSetChanged();
    }

    /*package*/ class VideoHistoryViewHolder extends RecyclerView.ViewHolder {
        private LayoutRecyclerviewHistoryListitemBinding binding;

        /*package*/ VideoHistoryViewHolder(LayoutRecyclerviewHistoryListitemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /*package*/ void bind(VideoHistoryModel video) {
            binding.setData(video);
            binding.executePendingBindings();
        }
    }
}
