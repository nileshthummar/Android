package com.watchback2.android.adapters.viewholders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.watchback2.android.adapters.ChannelsListAdapter;
import com.watchback2.android.databinding.ElementChannelsListBinding;
import com.watchback2.android.models.Channel;

import java.lang.ref.WeakReference;

public class ChannelsListItemViewHolder extends RecyclerView.ViewHolder {

    private final ElementChannelsListBinding elementChannelsListBinding;

    private final WeakReference<ChannelsListAdapter> adapterWeakReference;

    public ChannelsListItemViewHolder(@NonNull ElementChannelsListBinding binding, @NonNull ChannelsListAdapter adapter) {
        super(binding.getRoot());

        elementChannelsListBinding = binding;
        adapterWeakReference = new WeakReference<>(adapter);
    }

    public void bind(final Channel channel) {
        elementChannelsListBinding.setChannelItemData(channel);
        elementChannelsListBinding.executePendingBindings();
        elementChannelsListBinding.clParentElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Channel channel = elementChannelsListBinding.getChannelItemData();
                channel.setFavorite(!channel.isFavorite());

                if (adapterWeakReference != null) {
                    ChannelsListAdapter adapter = adapterWeakReference.get();
                    if (adapter != null) {
                        adapter.notifyItemUpdated(channel);
                    }
                }

            }
        });


    }
}
