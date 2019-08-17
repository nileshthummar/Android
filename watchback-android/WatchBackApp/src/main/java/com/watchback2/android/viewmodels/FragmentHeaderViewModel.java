package com.watchback2.android.viewmodels;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.leanplum.Leanplum;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.activities.WatchbackWebViewActivity;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.controllers.PerkUserManager;
import com.watchback2.android.models.PerkUser;
import com.watchback2.android.utils.PerkUtils;
import com.watchback2.android.utils.TrackingEvents;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by perk on 15/03/18.
 */

public class FragmentHeaderViewModel extends Observable.OnPropertyChangedCallback {

    private static final String LOG_TAG = "FragmentHeaderViewModel";

    private final ObservableField<String> profileImg = new ObservableField<>("");

    private final ObservableBoolean facebookConnected = new ObservableBoolean(false);

    private final ObservableBoolean isUserInfoAvailable = new ObservableBoolean(false);

    private final ObservableBoolean hasCoverImage = new ObservableBoolean(false);

    private final ObservableField<String> fullName = new ObservableField<>("");

    private final ObservableField<String> email = new ObservableField<>("");

    private final ObservableField<String> phone = new ObservableField<>("");

    private final ObservableField<String> gender = new ObservableField<>("");

    private final ObservableField<String> pointsMessage = new ObservableField<>("");

    private final ObservableField<String> referralCode = new ObservableField<>("");

    private final ObservableBoolean dataLoading = new ObservableBoolean(false);

    private final WeakReference<Context> mContextWeakReference;

    public FragmentHeaderViewModel(@NonNull Context context) {
        mContextWeakReference = new WeakReference<>(context.getApplicationContext());
        PerkUserManager.INSTANCE.getUser().addOnPropertyChangedCallback(this);
        initHeaderValues();
    }

    private void initHeaderValues() {
        PerkUser user = PerkUserManager.INSTANCE.getUser().get();
        if (user == null) {
            PerkLogger.w(LOG_TAG, "Returning as Perk-user information is unavailable!");

            getDataLoading().set(true);

            PerkUserManager.INSTANCE.getUserInfo(PerkUtils.getAppContext());

            getFacebookConnected().set(false);
            getIsUserInfoAvailable().set(false);

            getProfileImg().set("");
            getFullName().set("");
            getEmail().set("");
            getPhone().set("Unknown");
            getGender().set("Unknown");
            getReferralCode().set("");

            return;
        }

        getDataLoading().set(false);

        getIsUserInfoAvailable().set(true);

        /*String imageUrl = user.getProfileImage();
        if (!TextUtils.isEmpty(imageUrl)) {
            getProfileImg().set(imageUrl);
        }*/

        String userFirstName = user.getFirstName();
        String userLastName = user.getLastName();

        // Populate full name if available:
        if (!TextUtils.isEmpty(userFirstName) && !TextUtils.isEmpty(userLastName)) {
            getFullName().set(userFirstName + " " + userLastName);
        } else if (!TextUtils.isEmpty(userFirstName)) {
            getFullName().set(userFirstName);
        } else {
            getFullName().set("");
        }

        // Set email:
        getEmail().set(user.getEmail());

        // Set gender:
        String gender = user.getGender();

        if (!TextUtils.isEmpty(gender) && gender.toLowerCase(Locale.US).startsWith("m")) {
            gender = "Male";
        } else if (!TextUtils.isEmpty(gender) && gender.toLowerCase(Locale.US).startsWith("f")) {
            gender = "Female";
        } else{
            gender = "Unknown";
        }
        getGender().set(gender);

        // Set phone number:
        if (!TextUtils.isEmpty(user.getPhone())) {
            SpannableStringBuilder formattedPhoneNumber = new SpannableStringBuilder(
                    user.getPhone());
            PhoneNumberUtils.formatNanpNumber(formattedPhoneNumber);
            getPhone().set(formattedPhoneNumber.toString());
        }

        // Set referral code:
        getReferralCode().set("");

        // Set facebookConnected - we don't have facebook login anymore
        getFacebookConnected().set(false);

        // Set points text: - do not have points anymore
//        getPointsMessage().set(user.getAvailablePerks());
    }

    public ObservableField<String> getProfileImg() {
        return profileImg;
    }

    @Override
    public void onPropertyChanged(Observable observable, int i) {
        initHeaderValues();
    }

    public ObservableBoolean getFacebookConnected() {
        return facebookConnected;
    }

    public ObservableBoolean getHasCoverImage() {
        return hasCoverImage;
    }

    public ObservableField<String> getEmail() {
        return email;
    }

    public ObservableField<String> getGender() {
        return gender;
    }

    public ObservableField<String> getPhone() {
        return phone;
    }

    public ObservableField<String> getReferralCode() {
        return referralCode;
    }

    public ObservableField<String> getFullName() {
        return fullName;
    }

    public ObservableField<String> getPointsMessage() {
        return pointsMessage;
    }

    public ObservableBoolean getIsUserInfoAvailable() {
        return isUserInfoAvailable;
    }

    public ObservableBoolean getDataLoading() {
        return dataLoading;
    }

    public int getInfoIconResourceId() {
        return R.drawable.ic_info_tab;
    }

    @Nullable
    private Context getContext() {
        if (mContextWeakReference == null) {
            return null;
        }

        return mContextWeakReference.get();
    }

    public void handleInfoClick(View view) {
        PerkLogger.d(LOG_TAG, "handleInfoClick(): User tapped info icon!");

        // TODO: Pass referral code here if needed:
        Leanplum.track(TrackingEvents.LEANPLUM_CODE_INFO_TAP);
    }

    public void handleEditClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(WatchbackWebViewActivity.PROFILE_URL), view.getContext(),
                WatchbackWebViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(WatchbackWebViewActivity.INTENT_TITLE_EXTRA_KEY, view.getContext().getResources().getString(R.string.edit));
        intent.putExtra(WatchbackWebViewActivity.INTENT_PAGE_NEEDS_TOKEN, true);
        view.getContext().startActivity(intent);

        try{
            // Facebook Analytics: edit_profile_tap - user taps on Edit Profile on account screen:
            FacebookEventLogger.getInstance().logEvent("edit_profile_tap");

            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Profile:Edit");
            contextData.put("tve.userpath","Profile:Edit");
            contextData.put("tve.contenthub","Profile");

            AdobeTracker.INSTANCE.trackState("Profile:Edit",contextData);
            /////
        }catch (Exception e){}
    }
}
