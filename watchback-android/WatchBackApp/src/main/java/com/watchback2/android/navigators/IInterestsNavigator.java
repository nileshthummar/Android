package com.watchback2.android.navigators;

import androidx.annotation.NonNull;

import com.watchback2.android.models.Interest;

/**
 * Created by perk on 24/03/18.
 */

public interface IInterestsNavigator {

    void onSubmitInterestsClick();

    void onCancelClick();

    void onItemUpdated(@NonNull Interest interestItem);
}
