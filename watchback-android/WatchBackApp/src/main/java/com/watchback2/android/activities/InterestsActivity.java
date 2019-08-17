package com.watchback2.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.request.auth.AuthAPIRequestController;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.adapters.InterestsListAdapter;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.api.LeanplumAPIController;
import com.watchback2.android.controllers.HomeScreenItemsListManager;
import com.watchback2.android.controllers.LoginSignupManager;
import com.watchback2.android.controllers.RecommendedVideosManager;
import com.watchback2.android.databinding.ActivityInterestsBinding;
import com.watchback2.android.helper.UserInfoValidator;
import com.watchback2.android.models.Interest;
import com.watchback2.android.models.LeanplumPostModel;
import com.watchback2.android.navigators.IInterestsNavigator;
import com.watchback2.android.utils.AppConstants;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkPreferencesManager;
import com.watchback2.android.viewmodels.InterestsViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

public class InterestsActivity extends BaseThemeableActivity implements IInterestsNavigator {

    public static final String EXTRA_EDIT_MODE = "edit_mode";

    private static final String TAG = "InterestsActivity";

    private InterestsViewModel mInterestsViewModel;

    private boolean mIsFromSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityInterestsBinding binding = DataBindingUtil.setContentView(this,
                R.layout.activity_interests);

        mIsFromSettings = getIntent() != null && getIntent().getBooleanExtra(EXTRA_EDIT_MODE,
                false);

        // Facebook-event tracking for Interests screen:
        // 'select_interests' OR 'edit_interests'
        FacebookEventLogger.getInstance().logEvent(
                mIsFromSettings ? "edit_interests" : "select_interests");

        mInterestsViewModel = new InterestsViewModel(this);
        mInterestsViewModel.setNavigator(this);
        mInterestsViewModel.getIsEditingFromSettings().set(mIsFromSettings);
        binding.setViewModel(mInterestsViewModel);

        InterestsListAdapter adapter = new InterestsListAdapter(new ArrayList<Interest>(0), this);
        binding.idInterestsList.setAdapter(adapter);
        binding.idInterestsList.setLayoutManager(new LinearLayoutManager(this));
        binding.idInterestsList.setItemAnimator(new DefaultItemAnimator());

        mInterestsViewModel.loadInterestsListItems();

        registerFinishReceiver();

        try{
            if (!mIsFromSettings){
                HashMap<String, Object> contextData = new HashMap<String, Object>();
                contextData.put("tve.title","Set-Up:Personalization");
                contextData.put("tve.userpath","Set-Up:Personalization");
                contextData.put("tve.contenthub","Set-Up");
                AdobeTracker.INSTANCE.trackState("Set-Up:Personalization",contextData);
            }
            else{
                HashMap<String, Object> contextData = new HashMap<String, Object>();
                contextData.put("tve.title","Settings:Personalization");
                contextData.put("tve.userpath","Settings:Personalization");
                contextData.put("tve.contenthub","Settings");
                AdobeTracker.INSTANCE.trackState("Settings:Personalization",contextData);
            }
        }catch (Exception e){}

    }

    @Override
    public void onSubmitInterestsClick() {
        if (mInterestsViewModel == null) {
            PerkLogger.e(TAG, "onSubmitInterestsClick(): mInterestsViewModel is unavailable!");
            return;
        }

        List<Interest> interestList = mInterestsViewModel.getInterestsList().get();

        if (interestList == null) {
            PerkLogger.e(TAG, "onSubmitInterestsClick(): interestList is null! ");
            return;
        }

        PerkPreferencesManager.INSTANCE.saveUserInterestsIntoPreference(interestList);

        // tell the RecommendedVideosManager to refresh the list since there may have been an
        // update in the user's selected interests
        RecommendedVideosManager.INSTANCE.getRecommendedVideosList(getApplicationContext(), true,
                null);

        // Invalidate the homescreen-data since in case of any updates, we need to get the
        // updated response from homescreen-API
        HomeScreenItemsListManager.INSTANCE.invalidateHomeScreenItemList();

        // Update user's interests via userattributes on Leanplum:
        updateLeanplumUserAttributes(interestList);

       if (isLoggedIn()) {
           // Save the details on server-side if logged in
           mInterestsViewModel.getDataLoading().set(true);
           LoginSignupManager.INSTANCE.updateUserInterests(this.getApplicationContext(),
                   new LoginSignupManager.IInterestsUpdatedCallback() {
                       @Override
                       public void onInterestsUpdated() {
                           mInterestsViewModel.getDataLoading().set(false);
                           finishInterestActivity();
                       }
                   });
       } else {
           finishInterestActivity();
       }

        try{
            StringBuilder strInterest = new StringBuilder();

            for (int i = 0 ; i < interestList.size(); i++){
                Interest interest = interestList.get(i);
                if (interest.getSelected()){

                    if (strInterest.length() < 1){
                        strInterest.append(interest.getInterest());
                    }
                    else{
                        strInterest.append(",");
                        strInterest.append(interest.getInterest());
                    }
                }
            }
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            if (!mIsFromSettings){

                contextData.put("tve.title","Set-Up:Personalization");
                contextData.put("tve.contenthub","Set-Up");
            }
            else{

                contextData.put("tve.title","Settings:Personalization");
                contextData.put("tve.contenthub","Settings");

            }
            contextData.put("tve.userpath","Event:Save Interests");
            contextData.put("tve.interests",strInterest.toString());
            contextData.put("tve.personalize","true");
            AdobeTracker.INSTANCE.trackAction("Event:Save Interests",contextData);
        }catch (Exception e){}

    }

    private void updateLeanplumUserAttributes(List<Interest> interestList) {
        List<String> selectedInterests = new ArrayList<>();

        for (Interest interest : interestList) {
            if (interest != null && interest.getSelected() && !TextUtils.isEmpty(
                    interest.getId())) {
                selectedInterests.add(interest.getInterest());
            }
        }

        String selectedInterestNames = selectedInterests.isEmpty() ? "" : TextUtils.join(",",
                selectedInterests);

        PerkLogger.d(TAG,
                "Updating Leanplum userAttributes for Interests: " + selectedInterestNames);

        Map<String, String> map = new HashMap<>();
        map.put(AppUtility.LEANPLUM_INTERESTS_ATTRIBUTE, selectedInterestNames);

        LeanplumAPIController.INSTANCE.updateUserAttributes(map,
                new OnRequestFinishedListener<LeanplumPostModel>() {
                    @Override
                    public void onSuccess(@NonNull LeanplumPostModel leanplumPostModel,
                            @Nullable String s) {
                        PerkLogger.d(TAG,
                                "Successful updating Leanplum userAttributes for Interests!\n"
                                        + leanplumPostModel.toString());
                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType,
                            @Nullable PerkResponse<LeanplumPostModel> perkResponse) {
                        PerkLogger.e(TAG,
                                "Failed updating Leanplum userAttributes for Interests: "
                                        + (perkResponse != null ? perkResponse.toString()
                                        : ""));
                    }
                });
    }

    private void finishInterestActivity() {
        if (!mInterestsViewModel.getIsEditingFromSettings().get()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onItemUpdated(@NonNull Interest interestItem) {
        if (mInterestsViewModel != null) {
            mInterestsViewModel.onItemUpdated(interestItem);
        }
    }

    @Override
    public void onCancelClick() {
        PerkLogger.d(TAG, "onCancelClick(): finishing...");
        finish();
    }

    private boolean isLoggedIn() {
        return UserInfoValidator.isAuthenticated(
                AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(getApplicationContext()));
    }

    @Override
    protected void onDestroy() {
        unregisterFinishReceiver();
        super.onDestroy();
    }

    // TODO: Move this to common base activity. Currently here to fix a bug
    private FinishReceiver finishReceiver;

    private void registerFinishReceiver() {
        try {
            unregisterFinishReceiver();
            finishReceiver = new FinishReceiver();
            registerReceiver(finishReceiver, new IntentFilter(AppConstants.ACTION_FINISH));
        } catch (Exception e) {
        }
    }

    private void unregisterFinishReceiver() {
        try {
            if (finishReceiver != null) {
                unregisterReceiver(finishReceiver);
                finishReceiver = null;
            }
        } catch (Exception e) {
        }
    }

    private final class FinishReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AppConstants.ACTION_FINISH)) {
                finish();
            }
        }
    }
}
