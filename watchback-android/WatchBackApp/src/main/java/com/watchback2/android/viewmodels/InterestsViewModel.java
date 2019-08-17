package com.watchback2.android.viewmodels;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.perk.request.auth.AuthAPIRequestController;
import com.perk.util.PerkLogger;
import com.watchback2.android.helper.UserInfoValidator;
import com.watchback2.android.models.Interest;
import com.watchback2.android.navigators.IInterestsNavigator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by perk on 24/03/18.
 */

public class InterestsViewModel {

    private static final String LOG_TAG = "InterestsViewModel";

    private static final int MINIMUM_INTERESTS_NEEDED = 3;

    private final ObservableBoolean dataLoading = new ObservableBoolean(false);

    private final ObservableBoolean enableSubmitButton = new ObservableBoolean(false);

    private final ObservableField<List<Interest>> interestsList = new ObservableField<>();

    private final Context mContext;

    private final List<Interest> mSelectedInterests = new ArrayList<>();

    private final ObservableBoolean isEditingFromSettings = new ObservableBoolean(false);

    @Nullable
    private IInterestsNavigator mNavigator;

    public InterestsViewModel(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    public void setNavigator(@NonNull IInterestsNavigator navigator) {
        mNavigator = navigator;
    }

    public void handleInterestsSelected(View view) {
        if (mNavigator != null) {
            // Set enabled to false to prevent user from clicking twice
            view.setEnabled(false);
            mNavigator.onSubmitInterestsClick();
        }
    }

    public void handleCancelButtonClick(View view) {
        if (mNavigator != null) {
            mNavigator.onCancelClick();
        }
    }

    public void loadInterestsListItems() {

        if (dataLoading.get()) {
            return;
        }

        PerkLogger.d(LOG_TAG, "Loading Interests list data...");

        dataLoading.set(true);

        if (getInterestsList().get() != null) {
            getInterestsList().get().clear();
        }

        /*PerkAPIController.INSTANCE.getInterests(mContext, isLoggedIn(),
                new OnRequestFinishedListener<GetInterestsData>() {

                    @Override
                    public void onSuccess(@NonNull GetInterestsData interestsData, @Nullable String s) {
                        PerkLogger.d(LOG_TAG, "Loading Interests list data successful");
                        dataLoading.set(false);

                        List<Interest> interestList = interestsData.getInterests();
                        if (interestList != null) {

                            // Update the selection values if user is not logged in (as the
                            // user's selection is stored locally in that case)
                            List<Interest> storedInterestList = isLoggedIn() ? null :
                                    PerkPreferencesManager.INSTANCE.getUserInterestsListFromPreferences();
                            if (storedInterestList != null) {
                                for (Interest storedInterest : storedInterestList) {
                                    if (storedInterest != null && storedInterest.getSelected()) {
                                        for (final Interest interest : interestList) {
                                            if (interest != null && TextUtils.equals(
                                                    interest.getId(), storedInterest.getId())) {
                                                interest.setSelected(true);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            getInterestsList().set(interestList);
                            updateSelectedInterestsList();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType,
                            @Nullable PerkResponse<GetInterestsData> perkResponse) {
                        dataLoading.set(false);
                        PerkLogger.e(LOG_TAG,
                                "Loading Interests list data failed: " + (perkResponse != null
                                        ? perkResponse.getMessage() : ""));
                        PerkUtils.showErrorMessageToast(errorType,
                                (perkResponse != null ? perkResponse.getMessage()
                                        : mContext.getString(R.string.generic_error)));
                    }
                });*/
    }

    public ObservableBoolean getDataLoading() {
        return dataLoading;
    }

    public ObservableField<List<Interest>> getInterestsList() {
        return interestsList;
    }

    public ObservableBoolean getEnableSubmitButton() {
        return enableSubmitButton;
    }

    public List<Interest> getSelectedInterests() {
        return mSelectedInterests;
    }

    private boolean isLoggedIn() {
        return UserInfoValidator.isAuthenticated(
                AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(mContext));
    }

    public void onItemUpdated(@NonNull Interest interestItem) {
        if (interestItem.getSelected()) {
            getSelectedInterests().add(interestItem);
        } else {
            getSelectedInterests().remove(interestItem);
        }

        enableSubmitButtonIfNeeded();
    }

    private void updateSelectedInterestsList() {
        List<Interest> interestsList = getInterestsList().get();

        if (interestsList == null) {
            PerkLogger.e(LOG_TAG, "updateSelectedInterestsList(): interestsList is null! ");
            return;
        }

        getSelectedInterests().clear();
        for (Interest interest : interestsList) {
            if (interest != null && interest.getSelected()) {
                getSelectedInterests().add(interest);
            }
        }

        enableSubmitButtonIfNeeded();
    }

    private void enableSubmitButtonIfNeeded() {
        int selectedCount = getSelectedInterests().size();
        PerkLogger.d(LOG_TAG, "enableSubmitButtonIfNeeded(): Currently selected items: " + selectedCount);

        getEnableSubmitButton().set(selectedCount >= MINIMUM_INTERESTS_NEEDED);
    }

    public ObservableBoolean getIsEditingFromSettings() {
        return isEditingFromSettings;
    }
}
