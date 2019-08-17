package com.watchback2.android.controllers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.request.auth.AuthAPIRequestController;
import com.perk.request.auth.AuthenticatedSession;
import com.perk.util.PerkLogger;
import com.singular.sdk.Singular;
import com.watchback2.android.R;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.api.PerkFileManager;
import com.watchback2.android.api.PostFavChannels;
import com.watchback2.android.models.Channel;
import com.watchback2.android.models.Interest;
import com.watchback2.android.models.PerkUser;
import com.watchback2.android.models.genres.AllGenresWrapper;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkPreferencesManager;
import com.watchback2.android.utils.PerkUtils;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by perk on 22/03/18.
 * Login Signup Manager manages Perk Email login and signup and facebook connect with perk.
 */

public final class LoginSignupManager {

    /**
     * Singleton instance of the class that would be used for accessing PerkAuthenticationController.
     */
    public static final LoginSignupManager INSTANCE = new LoginSignupManager();

    private static final String LOG_TAG = "LoginSignupManager";

    private ProgressDialog progressDialog;

    private ILoginSignupManager iLoginSignupManager;

    private IInterestsUpdatedCallback mInterestsUpdatedCallback;

    /* Private for Singleton */
    private LoginSignupManager() {
    }

    /**
     * Interface to send Authentication successful callback
     */
    public interface ILoginSignupManager {
        void onAuthenticationSuccessful();

        void onSignUpWithExistingAccount();

        void onSignUpFailure();
    }

    /**
     * Callback invoked after Interests-update API is finished executing
     */
    public interface IInterestsUpdatedCallback {
        void onInterestsUpdated();
    }

    public void loginWithEmail(@NonNull final Context context, @NonNull final String email,
                               @NonNull String password, boolean bFromSignup, @NonNull final ILoginSignupManager manager) {

        iLoginSignupManager = manager;

        if (!AppUtility.isNetworkAvailable(context)) {
            PerkLogger.d(LOG_TAG, "loginWithEmail: Returning as network is unavailable!");
            return;
        }
        showProgressDialog(context, "Logging in...");

        PerkLogger.d(LOG_TAG, "Logging in with email...");

        AuthAPIRequestController.INSTANCE.authenticateWithEmail(context, email, password, new OnRequestFinishedListener<AuthenticatedSession>() {
            @Override
            public void onSuccess(@NonNull AuthenticatedSession authenticatedSession, @Nullable String msg) {
                dismissProgressDialog();

                PerkLogger.d(LOG_TAG, "Log in successful!");
                //Toast.makeText(context, "Succesfully authenticated!", Toast.LENGTH_SHORT).show();
                PerkUtils.getSharedPreferencesEditor().putBoolean("facebook_login", false);
                PerkUtils.getSharedPreferencesEditor().commit();
                onLoginSuccess(context, bFromSignup);

            }

            @Override
            public void onFailure(@NonNull ErrorType errorType, @Nullable PerkResponse<AuthenticatedSession> perkResponse) {
                dismissProgressDialog();
                String errorMsg = (perkResponse != null ? perkResponse.getMessage()
                        : context.getResources().getString(R.string.invalid_state));

                PerkLogger.e(LOG_TAG, "Log in failed! " + errorMsg);
                showAlertDialogOnAPIFailure(context, errorMsg);

                if (bFromSignup) {
                    //////
                    HashMap<String, Object> contextData = new HashMap<String, Object>();
                    contextData.put("tve.title", "Sign-Up:Email");
                    contextData.put("tve.userpath", "Event:Email:Sign-Up Error");
                    contextData.put("tve.contenthub", "Sign-Up");
                    contextData.put("tve.action", "true");
                    contextData.put("tve.registrationtype", "Email");
                    contextData.put("tve.error", errorMsg);
                    AdobeTracker.INSTANCE.trackAction("Event:Email:Sign-Up Error", contextData);
                    //////
                } else {
                    //////
                    HashMap<String, Object> contextData = new HashMap<String, Object>();
                    contextData.put("tve.title", "Log-In");
                    contextData.put("tve.userpath", "Event:Email:Log-In Error");
                    contextData.put("tve.contenthub", "Log-In");
                    contextData.put("tve.loginerror", "true");
                    contextData.put("tve.registrationtype", "Email");
                    contextData.put("tve.error", errorMsg);
                    AdobeTracker.INSTANCE.trackAction("Event:Email:Log-In Error", contextData);
                    //////

                }
            }
        });
    }

    private void onLoginSuccess(@NonNull final Context context, boolean bFromSignup) {
        showProgressDialog(context, "Loading...");

        //upload any favorite channels saved by user
        updateUserFavorites(context);

        // Load user-info
        PerkUserManager.INSTANCE.getUserInfo(context);

        // Save that user has accepted T&C once, so no need to prompt again on next login in this device
        PerkPreferencesManager.INSTANCE.putUserAllowedToSkipTncIntoPreference(true);

        // Clear stored values for last long-form video on successful login:
        PerkPreferencesManager.INSTANCE.clearPlayheadPositions();

        PerkLogger.d(LOG_TAG, "onLoginSuccess: areUserInterestsSavedToPreferences=false");
        dismissProgressDialog();
        iLoginSignupManager.onAuthenticationSuccessful();

        if (bFromSignup) {
            //////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title", "Sign-Up:Email");
            contextData.put("tve.userpath", "Event:Email:Sign-Up Success");
            contextData.put("tve.contenthub", "Sign-Up");
            contextData.put("tve.registrationsuccess", "true");
            contextData.put("tve.registrationtype", "Email");

            contextData = loginSuccessAppendCommonData(contextData);


            AdobeTracker.INSTANCE.trackAction("Event:Email:Sign-Up Success", contextData);
            //////
            ///
            HashMap<String, Object> contextDataAppsFlyer = new HashMap<String, Object>();
            contextDataAppsFlyer.put("registration_method", "email");
            AdobeTracker.INSTANCE.trackAppsFlyerEvent(context, "complete_registration", contextDataAppsFlyer);
            ///

            // Singular: complete_registration - user successfully signs up (new sign up only)
            Singular.event("complete_registration");

            // Facebook Analytics: complete_registration - user successfully signs up
            FacebookEventLogger.getInstance().logEvent("complete_registration");
        } else {
            //////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title", "Log-In");
            contextData.put("tve.userpath", "Event:Email:Log-In Success");
            contextData.put("tve.contenthub", "Log-In");
            contextData.put("tve.loginsuccess", "true");

            contextData.put("tve.registrationtype", "Email");
            contextData = loginSuccessAppendCommonData(contextData);

            AdobeTracker.INSTANCE.trackAction("Event:Email:Log-In Success", contextData);

            AdobeTracker.INSTANCE.trackAppsFlyerEvent(context, "log_in", null);
            //////

            // Singular: log_in - user logs in
            Singular.event("log_in");

            // Facebook Analytics: log_in - user logs in
            FacebookEventLogger.getInstance().logEvent("log_in");
        }
    }

    public void signUpUser(@NonNull final Context context,
                           @NonNull final String email,
                           @NonNull final String password,
                           @NonNull final String confirmPassword,
                           final String gender,
                           @NonNull final String phoneNumber,
                           @Nullable final Date dateOfBirth,
                           @NonNull final ILoginSignupManager manager) {

        if (!AppUtility.isNetworkAvailable(context)) {
            PerkLogger.d(LOG_TAG, "signUpUser: Returning as network is unavailable!");
            return;
        }
        showProgressDialog(context, "Setting up an account for you!");
        PerkLogger.d(LOG_TAG, "Signing up...");

        iLoginSignupManager = manager;

        AuthAPIRequestController.INSTANCE.registerWithEmailAndPhone(
                context, email, password, confirmPassword,
                gender, dateOfBirth, phoneNumber, new OnRequestFinishedListener<AuthenticatedSession>() {
                    @Override
                    public void onSuccess(@NonNull AuthenticatedSession authenticatedSession, @Nullable String s) {
                        PerkLogger.d(LOG_TAG, "Sign up successful!");
                        onLoginSuccess(context, true);

                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType, @Nullable PerkResponse<AuthenticatedSession> perkResponse) {
                        manager.onSignUpFailure();
                        dismissProgressDialog();
                        String errorMsg = (perkResponse != null ? perkResponse.getMessage()
                                : context.getResources().getString(R.string.invalid_state));

                        PerkLogger.e(LOG_TAG, "Sign up failed! " + errorMsg);
                        showAlertDialogOnAPIFailure(context, errorMsg, manager);

                        //////
                        HashMap<String, Object> contextData = new HashMap<String, Object>();
                        contextData.put("tve.title", "Sign-Up:Email");
                        contextData.put("tve.userpath", "Event:Email:Sign-Up Error");
                        contextData.put("tve.contenthub", "Sign-Up");
                        contextData.put("tve.action", "true");
                        contextData.put("tve.registrationtype", "Email");
                        contextData.put("tve.error", errorMsg);
                        AdobeTracker.INSTANCE.trackAction("Event:Email:Sign-Up Error", contextData);
                        //////
                    }
                });

    }

    public void updateUserFavorites(Context context) {
        if (!AppUtility.isNetworkAvailable(context)) {
            PerkLogger.d(LOG_TAG, "updateUserInterests: Returning as network is unavailable!");
            notifyInterestsUpdatedCallback();
            return;
        }

        List<Channel> userChannelListFromPreferences = PerkPreferencesManager.INSTANCE.getUserChannelListFromPreferences();
        if (userChannelListFromPreferences != null && userChannelListFromPreferences.size() > 0) {
            // user logged in after saving favorites
            // fetch any already saved favorites from this account.
            GenreManager.INSTANCE.getUserFavorites(context, new GenreManager.FavoriteListFetchedCallback() {
                @Override
                public void onFavFetched(Map<String, AllGenresWrapper> faveList) {
                    // add the fetched data to the data saved while user was logged out
                    if (faveList != null && faveList.get(GenreManager.MY_FAVORITE) != null &&
                            faveList.get(GenreManager.MY_FAVORITE).getChannels() != null &&
                            faveList.get(GenreManager.MY_FAVORITE).getChannels().size() > 0) {
                        userChannelListFromPreferences.addAll(faveList.get(GenreManager.MY_FAVORITE).getChannels());
                        // update preferences
                        PerkPreferencesManager.INSTANCE.saveUserChannelsIntoPreference(userChannelListFromPreferences);
                    }
                    // post complete data to server
                    PostFavChannels postFavChannels = new PostFavChannels(context);
                    postFavChannels.makeCall(userChannelListFromPreferences);
                }
            });

        } else {
            // case where user logged out i.e cleared the preferences and logged in again.
            // get favorites from server and save to preferences
            GenreManager.INSTANCE.getUserFavorites(context, null);
        }

    }

    public void updateUserInterests(@NonNull final Context context, final IInterestsUpdatedCallback callback) {
        mInterestsUpdatedCallback = callback;

        if (!AppUtility.isNetworkAvailable(context)) {
            PerkLogger.d(LOG_TAG, "updateUserInterests: Returning as network is unavailable!");
            notifyInterestsUpdatedCallback();
            return;
        }

        List<Interest> interestList = PerkPreferencesManager.INSTANCE.getUserInterestsListFromPreferences();

        if (interestList == null) {
            PerkLogger.e(LOG_TAG, "updateUserInterests(): interestList is null!");
            return;
        }

        List<String> selectedInterests = new ArrayList<>();

        for (Interest interest : interestList) {
            if (interest == null) {
                continue;
            }

            if (interest.getSelected()) {
                selectedInterests.add(interest.getId());
            }
        }

        PerkLogger.d(LOG_TAG, "Updating selected interests: " + selectedInterests.toString());

       /* PerkAPIController.INSTANCE.postUserInterests(context,
                selectedInterests,
                new OnRequestFinishedListener<NullableDataModel>() {
                    @Override
                    public void onSuccess(@NonNull NullableDataModel nullableDataModel,
                            @Nullable String s) {
                        PerkLogger.d(LOG_TAG, "Updating selected interests successful!");
                        notifyInterestsUpdatedCallback();
                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType,
                            @Nullable PerkResponse<NullableDataModel> perkResponse) {
                        PerkLogger.e(LOG_TAG, "Updating selected interests failed! " +  (perkResponse != null ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));
                        PerkUtils.showErrorMessageToast(errorType,
                                (perkResponse != null ? perkResponse.getMessage()
                                        : context.getString(R.string.generic_error)));
                        notifyInterestsUpdatedCallback();
                    }
                });*/
    }

    private void notifyInterestsUpdatedCallback() {
        if (mInterestsUpdatedCallback != null) {
            mInterestsUpdatedCallback.onInterestsUpdated();
            mInterestsUpdatedCallback = null;
        }
    }

    /*private void showSignupConfirmDialog(@NonNull final Context context, @NonNull final ILoginSignupManager manager) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Thanks for Signing Up!");

        alertDialog.setMessage("Don't forget to confirm your email to unlock your points.");

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                try {
                    if (alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                } catch (Exception e) {
                    PerkLogger.e(LOG_TAG, "Error while hiding alert dialog ", e);
                }

                manager.onAuthenticationSuccessful();
            }
        });

        alertDialog.show();
    }*/

    private void showAlertDialogOnAPIFailure(@NonNull final Context context, @Nullable String msg) {
        showAlertDialogOnAPIFailure(context, msg, null);
    }

    private void showAlertDialogOnAPIFailure(@NonNull final Context context,
                                             @Nullable final String msg, @Nullable final ILoginSignupManager manager) {

        final AlertDialog alertDialog;

        if (context instanceof Activity && !((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
            alertDialog = new AlertDialog.Builder(context).create();
        } else {
            alertDialog = new AlertDialog.Builder(PerkUtils.getTopActivity()).create();
        }
        alertDialog.setTitle(context.getString(R.string.error_title));
        alertDialog.setMessage(msg);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok),
                (dialog, which) -> {

                    AppUtility.dismissDialog(dialog);

                    if (StringUtils.containsIgnoreCase(msg, "user already exists")
                            && manager != null) {
                        manager.onSignUpWithExistingAccount();
                    }
                });

        try {
            alertDialog.show();
        } catch (WindowManager.BadTokenException ex) {
            PerkLogger.e(LOG_TAG, "Exception showing dialog: ", ex);
        }
    }

    private void showProgressDialog(@NonNull final Context context, String message) {
        ContextThemeWrapper wrapper;

        if (context instanceof Activity && !((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
            wrapper = new ContextThemeWrapper(context,
                    PerkPreferencesManager.INSTANCE.isNightMode() ? R.style.AppCompatAlertDialogStyle
                            : R.style.AppCompatAlertDialogStyleLight);
        } else {
            wrapper = new ContextThemeWrapper(PerkUtils.getTopActivity(),
                    PerkPreferencesManager.INSTANCE.isNightMode() ? R.style.AppCompatAlertDialogStyle
                            : R.style.AppCompatAlertDialogStyleLight);
        }
        progressDialog = new ProgressDialog(wrapper);
        progressDialog.setMessage(message);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        try {
            progressDialog.show();
        } catch (WindowManager.BadTokenException ex) {
            PerkLogger.e(LOG_TAG, "Exception showing progress-dialog: ", ex);
        }
    }

    private void dismissProgressDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception iae) {
            PerkLogger.e(LOG_TAG, "error while dismissing the progress dialog", iae);
        }
    }


    private HashMap<String, Object> loginSuccessAppendCommonData(HashMap<String, Object> contextData) {
        try {
            contextData.put("tve.userlogin", "true");
            contextData.put("tve.userstatus", "Logged In");
            long userId = PerkFileManager.loadPerkUser().getUserId();
            contextData.put("tve.userid", String.valueOf(userId));
            String birthday = "";
            String gender = "";

            try {
                PerkUser perkUser = PerkFileManager.loadPerkUser();
                if (perkUser != null) {
                    birthday = perkUser.getBirthDate();
                    gender = perkUser.getGender();
                }

            } catch (Exception e) {
            }

            if (birthday != null && birthday.length() > 0 && !birthday.equalsIgnoreCase("null")) {
                DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
                String inputDateStr = birthday;
                Date date = null;
                try {
                    date = inputFormat.parse(inputDateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                DateFormat df = new SimpleDateFormat("yyyy", Locale.US);
                contextData.put("tve.usercat1", df.format(date));
            }

            if (gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female")) {
                contextData.put("tve.usercat2", gender);
            }
            ////////
        } catch (Exception e) {
        }

        return contextData;
    }
}
