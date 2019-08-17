package com.watchback2.android.adapters.channelsfragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.watchback2.android.R;
import com.watchback2.android.databinding.ChannelFragmentChannelItemBinding;
import com.watchback2.android.databinding.LayoutFavoriteListItemBinding;
import com.watchback2.android.fragments.ChannelsFragment;
import com.watchback2.android.models.Channel;

import java.util.List;

/**
 * Adapter for list of channels under each Genre in Channels Fragment and its viewHolder : GenreItemViewHolder.
 * This adapter is set by ChannelsFragmentListViewHolder in ChannelsFragmentListAdapter.
 */
public class ChannelsGenreListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Channel> channelsList;
    private final Context context;
    private ChannelsFragment.OnChannelItemClicked clickListener;
    private boolean isCircular = false;

    public ChannelsGenreListItemAdapter(List<Channel> channelsList, Context context, boolean isCircular, ChannelsFragment.OnChannelItemClicked clickListener) {
        this.channelsList = channelsList;
        this.context = context;
        this.clickListener = clickListener;
        this.isCircular = isCircular;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (isCircular) {
            LayoutFavoriteListItemBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(context), R.layout.layout_favorite_list_item, parent,
                    false);
            return new CircularItemViewHolder(binding, clickListener, isCircular);

        } else {
            ChannelFragmentChannelItemBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(context), R.layout.channel_fragment_channel_item, parent,
                    false);
            return new GenreItemViewHolder(binding, clickListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Channel channel = channelsList.get(position);
        if (holder instanceof CircularItemViewHolder) {
            ((CircularItemViewHolder) holder).bind(channel);
        } else if (holder instanceof GenreItemViewHolder) {
            ((GenreItemViewHolder) holder).bind(channel);
        }
    }

    @Override
    public int getItemCount() {
        return channelsList == null ? 0 : channelsList.size();
    }

    public void replaceData(List<Channel> channelsList) {
        this.channelsList = channelsList;
        notifyDataSetChanged();
    }

    //    ----------------------------------------------------------------------------------------------
    //    GenreItemViewHolder
    //    ----------------------------------------------------------------------------------------------
    /*package*/ class GenreItemViewHolder extends RecyclerView.ViewHolder {

        private ChannelFragmentChannelItemBinding binding;
        private ChannelsFragment.OnChannelItemClicked clickListener;

        /*package*/ GenreItemViewHolder(ChannelFragmentChannelItemBinding binding, ChannelsFragment.OnChannelItemClicked clickListener) {
            super(binding.getRoot());
            this.binding = binding;
            this.clickListener = clickListener;
        }

        /*package*/ void bind(Channel channel) {
            Glide.with(binding.idIvChannelIcon).load(channel.getLogoImageUrl()).into(binding.idIvChannelIcon);
            binding.idTvChannelTitle.setText(channel.getName());
            binding.executePendingBindings();

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClicked(channel);
                }
            });
        }
    }

    //    ----------------------------------------------------------------------------------------------
    //    CircularItemViewHolder
    //    ----------------------------------------------------------------------------------------------
    /*package*/ class CircularItemViewHolder extends RecyclerView.ViewHolder {

        private LayoutFavoriteListItemBinding binding;
        private ChannelsFragment.OnChannelItemClicked clickListener;
        private boolean isCircular;

        /*package*/ CircularItemViewHolder(LayoutFavoriteListItemBinding binding, ChannelsFragment.OnChannelItemClicked clickListener, boolean isCircular) {
            super(binding.getRoot());
            this.binding = binding;
            this.clickListener = clickListener;
            this.isCircular = isCircular;
        }

        /*package*/ void bind(Channel channel) {
            if (isCircular && channel.getmDetailsScreenLogoUrl() != null) {
                //gives priority to square image
                Glide.with(binding.idIvChannelIcon).load(channel.getmDetailsScreenLogoUrl()).into(binding.idIvChannelIcon);
            } else {
                //gives priority to rectangular image
                Glide.with(binding.idIvChannelIcon).load(channel.getLogoImageUrl()).into(binding.idIvChannelIcon);
            }
            binding.idTvChannelTitle.setText(channel.getName());
            binding.executePendingBindings();

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClicked(channel);
                }
            });
        }
    }
}
