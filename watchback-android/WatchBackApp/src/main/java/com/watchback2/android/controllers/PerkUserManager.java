package com.watchback2.android.controllers;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import com.leanplum.Leanplum;
import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.request.auth.AuthAPIRequestController;
import com.perk.request.auth.AuthenticatedSession;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.activities.VerifyPhoneActivity;
import com.watchback2.android.api.LeanplumAPIController;
import com.watchback2.android.api.PerkFileManager;
import com.watchback2.android.api.WatchbackAPIController;
import com.watchback2.android.helper.UserInfoValidator;
import com.watchback2.android.models.LeanplumPostModel;
import com.watchback2.android.models.PerkUser;
import com.watchback2.android.utils.AppConstants;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkPreferencesManager;
import com.watchback2.android.utils.PerkUtils;
import com.watchback2.android.viewmodels.SignupViewModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by perk on 08/01/18.
 * User Manager to handle user information
 */

public final class PerkUserManager {

    /**
     * Singleton instance of the class that would be used for accessing PerkAuthenticationController.
     */
    public static final PerkUserManager INSTANCE = new PerkUserManager();

    private static final String LOG_TAG = "PerkUserManager";

    private final ObservableField<PerkUser> mUser = new ObservableField<>();

    private static final int YEAR_LIMIT = 1900;

    private boolean isUserInfoCalling;

    private PerkUserManager() {
        // Private constructor for Singleton class
    }

    public void getUserInfo(@NonNull final Context context) {

        if (!AppUtility.isNetworkAvailable(context)) {
            PerkLogger.d(LOG_TAG, "getUserInfo: Returning as network is unavailable!");
            return;
        }

        if (!UserInfoValidator.isAuthenticated(
                AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(context))) {
            // Clear user if logged out
            getUser().set(null);
            return;
        }

        if (isUserInfoCalling) {
            PerkLogger.d(LOG_TAG, "getUserInfo: Returning as request is already in progress!");
            return;
        }

        PerkLogger.d(LOG_TAG, "getUserInfo: Getting User-info...");

        isUserInfoCalling = true;
        WatchbackAPIController.INSTANCE.getPerkUser(context, new OnRequestFinishedListener<PerkUser>() {
            @Override
            public void onSuccess(@NonNull PerkUser perkUser, @Nullable String s) {

                PerkLogger.d(LOG_TAG,
                        "getUserInfo(): onSuccess: model=" + AppUtility.safeReflectionToString(
                                perkUser));


                if (perkUser != null) {
                    isUserInfoCalling = false;

                    mUser.set(perkUser);

                    // TODO: Following part is just for being compatible with existing code. To be removed later once code is cleaned up
                    // ----------------------------
                    if (PerkUtils.m_bIsLog) Log.w(PerkUtils.TAG,"GetUserInfo onSuccess");
                    PerkFileManager.savePerkUser(perkUser);
                    context.sendBroadcast(new Intent(AppConstants.ACTION_UPDATE_POINTS));

                        // TODO: Check if needed
                    PerkUtils.setCrashlyticsDataForCurrentUser();

                    String access_token = "";
                    AuthenticatedSession aSession = AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(context);
                    if (aSession != null) {
                        access_token = aSession.getAccessToken();
                    }

                    PerkLogger.d(PerkUtils.TAG, "access_token ->" + access_token);
                    // ----------------------------

                    //fireOneSignalEvents(mUser.get());

                    PerkPreferencesManager.INSTANCE.putPerkUserUUIDIntoPreference(perkUser.getUuid());

                    //passUserData(context, mUser.get());

                    // Set the UUID as Leanplum's UserID:
                    Leanplum.setUserId(perkUser.getUuid());

                    checkUserDobAndGenderInfo(context);

                    if(!perkUser.isPhoneVerified() && PerkUtils.isUserLoggedIn()){
                        PerkLogger.d(LOG_TAG, "phone not verified");
                        // show verify phone activity
                        Intent intent = new Intent(PerkUtils.getAppContext(), VerifyPhoneActivity.class);
                        intent.putExtra(VerifyPhoneActivity.PHONE_NUMBER, perkUser.getPhone());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        PerkUtils.getAppContext().startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull ErrorType errorType, @Nullable PerkResponse<PerkUser> perkResponse) {
                isUserInfoCalling = false;

                PerkLogger.e(LOG_TAG, "getUserInfo(): onFailure: " + (perkResponse != null
                        ? perkResponse.toString()
                        : context.getResources().getString(R.string.generic_error)));

                PerkUtils.showErrorMessageToast(errorType,
                        (perkResponse != null ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));
            }
        });
    }

    /**
     * check user DOB and gender, if null launch the overlay
     */
    private void checkUserDobAndGenderInfo(@NonNull Context context) {
        PerkUser user = mUser.get();
        if (user != null) {
            // Sample response: "birthday": "1969-12-31 00:00:00",
            String birthday = user.getBirthDate();

            if (birthday == null || birthday.trim().length() < 1 ||
                    birthday.equalsIgnoreCase("null")) {

                birthday = "";

                PerkLogger.d(LOG_TAG,
                        "checkUserDobAndGenderInfo(): Cannot update userAttribute for DoB "
                                + "as the data is unavailable!");

            } else {
                PerkLogger.d(LOG_TAG, "checkUserDobAndGenderInfo: no overlay... dob: " + birthday);

                DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
                try {
                    if (!SignupViewModel.isValidDateOfBirth(PerkUtils.getTopActivity(),
                            inputFormat.parse(birthday), (dialog, which) -> {
                                PerkLogger.d(LOG_TAG, "Invalid date-of-birth! Initiating Log out...");
                                AppUtility.logOutUser(context.getApplicationContext());

                                // Log-out operation clears the preferences, so we restore that setting back here:
                                SignupViewModel.disallowUserFromApp();
                            })) {
                        PerkLogger.e(LOG_TAG, "DoB is not valid! Will log out...");
                        return;
                    }
                } catch (ParseException e) {
                    PerkLogger.e(LOG_TAG, "Exception parsing DoB: ", e);
                }
            }

            // Sample response: "gender": "Male",
            String gender = user.getGender();

            if (gender == null || gender.trim().length() < 1
                    || gender.equalsIgnoreCase("unknown")) {

                gender = "";

                PerkLogger.d(LOG_TAG,
                        "checkUserDobAndGenderInfo(): Cannot update userAttributes for gender "
                                + "as the data is unavailable!");

            }

            String email = user.getEmail();

            String uuid = user.getUuid();

            String referralCode = "";

            // Update Leanplum userAttributes
            PerkLogger.d(LOG_TAG,
                    "Updating Leanplum userAttributes: " + gender + ", " + birthday + ", "
                            + email + ", " + uuid + ", " + referralCode);

            LeanplumAPIController.INSTANCE.updateUserAttributes(
                    AppUtility.getUserAttributeMapFor(
                            gender.toLowerCase(Locale.US).startsWith("m") ? "Male" : "Female",
                            birthday, email, user.isEmailConfirmed(), referralCode),
                    new OnRequestFinishedListener<LeanplumPostModel>() {
                        @Override
                        public void onSuccess(@NonNull LeanplumPostModel leanplumPostModel,
                                @Nullable String s) {
                            PerkLogger.d(LOG_TAG,
                                    "Successful updating Leanplum userAttributes!\n"
                                            + leanplumPostModel.toString());
                        }

                        @Override
                        public void onFailure(@NonNull ErrorType errorType,
                                @Nullable PerkResponse<LeanplumPostModel> perkResponse) {
                            PerkLogger.e(LOG_TAG,
                                    "Failed updating Leanplum userAttributes: "
                                            + (perkResponse != null ? perkResponse.toString()
                                            : ""));
                        }
                    });
        }
    }

    @Nullable
    public PerkUser getUser(@NonNull Context context) {

        if (mUser.get() == null && !isUserInfoCalling) {
            PerkLogger.d("xyz", "getUser called " + context);
            getUserInfo(context);
        }
        return mUser.get();
    }

    public ObservableField<PerkUser> getUser() {
        return mUser;
    }

    public ObservableField<PerkUser> getMUser() {
        return mUser;
    }

    public int getUserAge(@Nullable String birthday) {
        int age = -1;
        try {
            if (birthday != null && birthday.length() > 0) {
                final String[] split = birthday.split(" ");
                if (split.length > 0) {
                    final String[] split1 = split[0].split("-");
                    if (split1.length > 0 && split1[0] != null && !split1[0].equalsIgnoreCase("null")) {
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int myear = Integer.parseInt(split1[0]) > YEAR_LIMIT ? Integer.parseInt(split1[0]) : Integer.parseInt(split1[2]);
                        age = year - myear;
                        return age;
                    }
                }
            } else {
                return age;
            }
        } catch (NumberFormatException e) {
            PerkLogger.e(LOG_TAG, "error in number formatting", e);
        }
        return age;
    }

    public void addUserChangedCallback(@NonNull Observable.OnPropertyChangedCallback callback) {
        mUser.addOnPropertyChangedCallback(callback);
    }

    public void removeUserChangedCallback(@NonNull Observable.OnPropertyChangedCallback callback) {
        mUser.removeOnPropertyChangedCallback(callback);
    }

    public static String getUserAccessToken(@NonNull Context context) {
        // Get user's current authenticated session
        String accessToken = "";
        AuthenticatedSession authenticatedSession = AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(context);
        if (UserInfoValidator.isAuthenticated(authenticatedSession)) {
            accessToken = authenticatedSession.getAccessToken();
        }
        PerkLogger.d(LOG_TAG, "access token " + accessToken);
        return accessToken;
    }

    @NonNull
    public String getUserName(@NonNull Context context) {
        PerkUser user = getUser(context);
        if (user == null) {
            return "";
        }

        String userFirstName = user.getFirstName();
        String userLastName = user.getLastName();

        StringBuilder userName = new StringBuilder();
        if (!TextUtils.isEmpty(userFirstName)) {
            userName.append(userFirstName);
        }

        if (!TextUtils.isEmpty(userLastName)) {
            userName.append(' ');
            userName.append(userLastName);
        }

        return userName.toString();
    }

}
