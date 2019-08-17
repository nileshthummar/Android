package com.watchback2.android.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.perk.util.PerkLogger;
import com.watchback2.android.adapters.viewholders.ChannelsListItemViewHolder;
import com.watchback2.android.databinding.ElementChannelsListBinding;
import com.watchback2.android.models.Channel;
import com.watchback2.android.navigators.IChannelsListNavigator;

import java.lang.ref.WeakReference;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChannelsListAdapter extends RecyclerView.Adapter<ChannelsListItemViewHolder> {

    private static final String TAG = "ChannelsListAdapter";

    private List<Channel> channelList;
    private WeakReference<IChannelsListNavigator> navigatorWeakReference;


    public ChannelsListAdapter(List<Channel> channelList, IChannelsListNavigator navigator) {
        super();
        this.channelList = channelList;
        navigatorWeakReference = new WeakReference<>(navigator);
        setList(channelList);
    }


    @NonNull
    @Override
    public ChannelsListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        ElementChannelsListBinding elementChannelsListBinding = ElementChannelsListBinding.inflate(layoutInflater, parent, false);
        return new ChannelsListItemViewHolder(elementChannelsListBinding, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelsListItemViewHolder holder, int position) {
        holder.bind(channelList.get(position));
    }

    @Override
    public int getItemCount() {
        return channelList != null ? channelList.size() : 0;
    }

    public void replaceData(@NonNull final List<Channel> channelList) {
        setList(channelList);
    }

    private void setList(@NonNull final List<Channel> channelList) {
        this.channelList = channelList;
        notifyDataSetChanged();
    }

    public void notifyItemUpdated(@NonNull Channel channel) {
        int index = channelList.indexOf(channel);

        if (index == -1) {
            PerkLogger.e(TAG, "Updated item not found in data list: " + channel);
            return;
        }

        notifyItemChanged(index);

        if (navigatorWeakReference != null) {
            IChannelsListNavigator navigator = navigatorWeakReference.get();
            if (navigator != null) {
                navigator.onItemUpdated(channel);
            }
        }
    }
}
