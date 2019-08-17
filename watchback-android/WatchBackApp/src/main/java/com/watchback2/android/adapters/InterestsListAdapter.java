package com.watchback2.android.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.perk.util.PerkLogger;
import com.watchback2.android.adapters.viewholders.InterestListItemViewHolder;
import com.watchback2.android.databinding.InterestsListItemBinding;
import com.watchback2.android.models.Interest;
import com.watchback2.android.navigators.IInterestsNavigator;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by perk on 24/3/18.
 * Adapter class for Interests list recycler view
 */

public class InterestsListAdapter extends RecyclerView.Adapter<InterestListItemViewHolder> {

    private static final String TAG = "InterestsListAdapter";

    private List<Interest> mInterestsList;

    private final WeakReference<IInterestsNavigator> mNavigatorReference;

    public InterestsListAdapter(@NonNull List<Interest> interestsList, @NonNull IInterestsNavigator navigator) {
        super();
        mNavigatorReference = new WeakReference<IInterestsNavigator>(navigator);
        setList(interestsList);
    }

    @NonNull
    @Override
    public InterestListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        InterestsListItemBinding interestsListItemBinding = InterestsListItemBinding.inflate(
                layoutInflater, parent, false);
        return new InterestListItemViewHolder(interestsListItemBinding, this);
    }

    @Override
    public void onBindViewHolder(@NonNull InterestListItemViewHolder holder, int position) {
        holder.bind(mInterestsList.get(position));
    }

    @Override
    public int getItemCount() {
        return mInterestsList != null ? mInterestsList.size() : 0;
    }

    private void setList(@NonNull final List<Interest> interestsList) {
        mInterestsList = interestsList;
        notifyDataSetChanged();
    }

    public void replaceData(@NonNull final List<Interest> interestsList) {
        setList(interestsList);
    }

    public void notifyItemUpdated(@NonNull Interest interest) {
        int index = mInterestsList.indexOf(interest);

        if (index == -1) {
            PerkLogger.e(TAG, "Updated item not found in data list: " + interest);
            return;
        }

        notifyItemChanged(index);

        if (mNavigatorReference != null) {
            IInterestsNavigator navigator = mNavigatorReference.get();
            if (navigator != null) {
                navigator.onItemUpdated(interest);
            }
        }
    }
}
