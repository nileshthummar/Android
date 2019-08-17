package com.watchback2.android.adapters.channelsfragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.perk.util.PerkLogger;
import com.watchback2.android.controllers.GenreManager;
import com.watchback2.android.databinding.ChannelFragmentGenreListitemBinding;
import com.watchback2.android.fragments.ChannelsFragment;
import com.watchback2.android.models.genres.AllGenresWrapper;

import java.util.Iterator;
import java.util.List;

/**
 * Created by perk on 16/03/18.
 * <p>
 * Adapter for List of Genres shown in Channels fragment and its ViewHolder : ChannelsFragmentListViewHolder
 * ChannelsFragmentListViewHolder: sets the adapter for the RecyclerView that holds list of channels under each Genre.
 */
public class ChannelsFragmentListAdapter extends RecyclerView.Adapter<ChannelsFragmentListAdapter.ChannelsFragmentListViewHolder> {

    private static final String LOG_TAG = "ChannelsFragmentListAdapter";

    private List<AllGenresWrapper> mChannelItemsList;

    private ChannelsFragment.OnChannelItemClicked clickListener;

    public ChannelsFragmentListAdapter(@NonNull List<AllGenresWrapper> channelItemsList, ChannelsFragment.OnChannelItemClicked clickListener) {
        setHasStableIds(true);
        setList(channelItemsList);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ChannelsFragmentListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ChannelFragmentGenreListitemBinding binding = ChannelFragmentGenreListitemBinding.inflate(layoutInflater, parent, false);

        return new ChannelsFragmentListViewHolder(binding, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelsFragmentListViewHolder holder, int position) {
        AllGenresWrapper item = mChannelItemsList.get(position);

        if (item == null || item.getChannels().size() == 0) {
            PerkLogger.e(LOG_TAG, "Returning as Channel data OR UUID not available!");
            return;
        }

        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return mChannelItemsList != null ? mChannelItemsList.size() : 0;
    }

    private void setList(@NonNull List<AllGenresWrapper> channelList) {
        // No matter what order list is in, show favorites as the first item
        if (channelList != null && channelList.size() != 0 && !channelList.get(0).getGenreName().equalsIgnoreCase(GenreManager.MY_FAVORITE) ) {
            AllGenresWrapper temp = null;
            for (Iterator<AllGenresWrapper> iterator = channelList.iterator(); iterator.hasNext(); ) {
                AllGenresWrapper item = iterator.next();
                if (item.getGenreName().equalsIgnoreCase(GenreManager.MY_FAVORITE)) {
                    temp = item;
                    iterator.remove();
                    break;
                }
            }
            if (temp != null) {
                channelList.add(0,temp);
            }
        }
        mChannelItemsList = channelList;
        notifyDataSetChanged();
    }

    public void replaceData(@NonNull List<AllGenresWrapper> channelList) {
        setList(channelList);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

//    ----------------------------------------------------------------------------------------------
//    ChannelsFragmentListViewHolder
//    ----------------------------------------------------------------------------------------------

    /*package*/ class ChannelsFragmentListViewHolder extends RecyclerView.ViewHolder {

        private ChannelFragmentGenreListitemBinding binding;
        private ChannelsFragment.OnChannelItemClicked clickListener;

        /*package*/ ChannelsFragmentListViewHolder(@NonNull ChannelFragmentGenreListitemBinding binding,
                                                   ChannelsFragment.OnChannelItemClicked clickListener) {
            super(binding.getRoot());
            this.binding = binding;
            this.clickListener = clickListener;
        }

        /*package*/ void bind(AllGenresWrapper allGenresWrapper, int position) {

            binding.idTitleGenre.setText(allGenresWrapper.getGenreName());

            RecyclerView channelRecyclerView = binding.idListGenre;

            ChannelsGenreListItemAdapter itemAdapter = new ChannelsGenreListItemAdapter(allGenresWrapper.getChannels(), channelRecyclerView.getContext(),
                    false,clickListener);
            channelRecyclerView.setLayoutManager(new LinearLayoutManager(channelRecyclerView.getContext(), RecyclerView.HORIZONTAL, false));
            channelRecyclerView.setItemAnimator(new DefaultItemAnimator());
            channelRecyclerView.setItemViewCacheSize(1);
            channelRecyclerView.setAdapter(itemAdapter);
        }
    }
}
