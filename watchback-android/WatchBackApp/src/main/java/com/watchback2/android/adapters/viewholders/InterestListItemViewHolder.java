package com.watchback2.android.adapters.viewholders;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.watchback2.android.adapters.InterestsListAdapter;
import com.watchback2.android.databinding.InterestsListItemBinding;
import com.watchback2.android.models.Interest;

import java.lang.ref.WeakReference;

/**
 * Created by perk on 24/3/18.
 * View Holder to contain UI for the items in the interests list recycler view
 */

public class InterestListItemViewHolder extends RecyclerView.ViewHolder {

    private final InterestsListItemBinding mInterestsListItemBinding;

    private final WeakReference<InterestsListAdapter> mListAdapterReference;

    public InterestListItemViewHolder(@NonNull InterestsListItemBinding binding,
            @NonNull InterestsListAdapter adapter) {
        super(binding.getRoot());
        mInterestsListItemBinding = binding;
        mListAdapterReference = new WeakReference<>(adapter);
    }

    public void bind(final Interest interestData) {
        mInterestsListItemBinding.setInterestItemData(interestData);
        mInterestsListItemBinding.executePendingBindings();

        mInterestsListItemBinding.idInterestNameContainer.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Interest interest = mInterestsListItemBinding.getInterestItemData();
                        interest.setSelected(!interest.getSelected());
                        if (mListAdapterReference != null) {
                            InterestsListAdapter adapter = mListAdapterReference.get();
                            if (adapter != null) {
                                adapter.notifyItemUpdated(
                                        mInterestsListItemBinding.getInterestItemData());
                            }
                        }
                    }
                });
    }
}
