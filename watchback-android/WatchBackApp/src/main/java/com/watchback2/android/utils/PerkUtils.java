package com.watchback2.android.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.request.auth.AuthAPIRequestController;
import com.perk.request.auth.AuthenticatedSession;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.activities.ForceUpdateActivity;
import com.watchback2.android.activities.LaunchActivity;
import com.watchback2.android.activities.SessionExpiredActivity;
import com.watchback2.android.activities.WalkthroughActivity;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.api.LeanplumAPIController;
import com.watchback2.android.api.PerkFileManager;
import com.watchback2.android.common.WatchBackApplication;
import com.watchback2.android.controllers.HomeScreenItemsListManager;
import com.watchback2.android.controllers.PerkUserManager;
import com.watchback2.android.models.LeanplumPostModel;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nilesh on 10/31/16.
 */

public class PerkUtils {
    public  static boolean m_bIsLog = true;
    public static  String TAG = "PerkUtils";
    public static WatchBackApplication sApplication;
    private static SharedPreferences sUNWSharedPreferences;
    private static SharedPreferences.Editor sUNWSharedPreferencesEditor;
    private static String strAdvertisingId = "";
    private static  boolean bIsLimitAdTrackingEnabled;
    public static Boolean  isUserLoggedIn()
    {
        try {
            String  access_token = "";
            AuthenticatedSession aSession = AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(getAppContext());
            if(aSession != null)
            {
                access_token = aSession.getAccessToken();
            }

            return !access_token.isEmpty() && access_token != null
                    && !access_token.equals("");
        }catch (Exception e){}
        return false;
    }
    public static String  getAccessTocken()
    {
        String  access_token = "";
        try {

            AuthenticatedSession aSession = AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(getAppContext());
            if(aSession != null)
            {
                access_token = aSession.getAccessToken();
            }


        }catch (Exception e){}
        return access_token;
    }
    public static Context getAppContext() {
        return sApplication.getApplicationContext();
    }
    private  static Activity sActivity;
    public static Activity getTopActivity() {
        return  sActivity;
    }
    public static void setTopActivity(Activity activity)
    {
        sActivity = activity;


    }
    public static SharedPreferences getSharedPreferences() {
        if(sUNWSharedPreferences == null)
        {
            sUNWSharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getAppContext());
        }
        return sUNWSharedPreferences;
    }
    public static SharedPreferences.Editor getSharedPreferencesEditor() {
        if(sUNWSharedPreferencesEditor == null)
        {
            sUNWSharedPreferencesEditor = getSharedPreferences().edit();
        }
        return sUNWSharedPreferencesEditor;
    }

    // Function to fetch default registered email address
    public static String getDefaultEmail(Context con) {
        try {


            AccountManager am = AccountManager.get(con);
            Account[] accounts = am.getAccounts();
            for (Account ac : accounts) {
                if (ac.type.equals("com.google")) {
                    // Log.v("email", ac.name);
                    return ac.name;
                }

            }
        } catch (SecurityException se) {
            se.printStackTrace();
        }
        return null;
    }

    public static void initializationTracking(Context context, String deviceInfo) {
        // TODO: Inititalize tracking SDKs here:
    }

    public static void trackEvent(String event) {
        try {

            /*Apsalar.event(event);
            logger.logEvent(event);*/

        } catch (Exception ex) {
            ex.printStackTrace();

        }

    }

    public static void getIdThread() {

        try{
            new Thread(new Runnable() {
                public void run() {
                    try {
                        AdvertisingIdClient.Info adInfo = null;


                        try {
                            adInfo = AdvertisingIdClient
                                    .getAdvertisingIdInfo(getAppContext());
                            strAdvertisingId= adInfo.getId();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ////////

                        try{
                            bIsLimitAdTrackingEnabled = adInfo.isLimitAdTrackingEnabled();
                            getSharedPreferencesEditor().putBoolean("adoptout", adInfo.isLimitAdTrackingEnabled());
                        }
                        catch(Exception e){

                            getSharedPreferencesEditor().putBoolean("adoptout", false);
                        }
                        getSharedPreferencesEditor().commit();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }catch (Exception e){

        }


    }

    public  static String getDeviceID()
    {
        String deviceId = getSharedPreferences().getString("deviceId","");
        if (deviceId == null || deviceId.length() < 1)
        {
            try {
                deviceId = Settings.Secure.getString(getAppContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                if (deviceId == null) {
                    TelephonyManager telephonyManager = (TelephonyManager)getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
                    deviceId= telephonyManager.getDeviceId();
                }
            }catch (Exception e){}
            getSharedPreferencesEditor().putString("deviceId", deviceId);
            getSharedPreferencesEditor().commit();
        }

        return deviceId;

    }
    /*
	 * Get currency value from Server in background
	 */


    public static void GetUserInfo()
    {
        PerkUserManager.INSTANCE.getUserInfo(getTopActivity());
    }

    public static void forceLogout(Activity activity){
        Intent intent = new Intent(
                getAppContext(),

                SessionExpiredActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        getAppContext().startActivity(intent);
    }

    public static void logoutUser() {
        // Clearing values
        try {
            // Update Leanplum userAttributes:
            PerkLogger.d(TAG, "Clearing Leanplum userAttributes on log-out:");

            Map<String, String> map = AppUtility.getUserAttributeMapFor(null, null, null, false,
                    null);
            // The below is now already handled by getUserAttributeMapFor() call above
            /*map.put(AppUtility.LEANPLUM_INTERESTS_ATTRIBUTE, "");
            map.put(AppUtility.LEANPLUM_CHANNELS_ATTRIBUTE, "");*/

            LeanplumAPIController.INSTANCE.updateUserAttributes(map,
                    new OnRequestFinishedListener<LeanplumPostModel>() {
                        @Override
                        public void onSuccess(@NonNull LeanplumPostModel leanplumPostModel,
                                @Nullable String s) {
                            PerkLogger.d(TAG,
                                    "Successful clearing Leanplum userAttributes on log-out!\n"
                                            + leanplumPostModel.toString());
                        }

                        @Override
                        public void onFailure(@NonNull ErrorType errorType,
                                @Nullable PerkResponse<LeanplumPostModel> perkResponse) {
                            PerkLogger.e(TAG,
                                    "Failed clearing Leanplum userAttributes on log-out: "
                                            + (perkResponse != null ? perkResponse.toString()
                                            : ""));
                        }
                    });

            // Clear stored values for last long-form video, and watched-videos list, on log-out:
            PerkPreferencesManager.INSTANCE.clearPlayheadPositions();

            // We persist these values, so read current settings & save those:
            boolean allowTncSkip = PerkPreferencesManager.INSTANCE.getUserAllowedToSkipTncFromPreferences();
            boolean allowTermsGateSkip = PerkPreferencesManager.INSTANCE.getUserAllowedToSkipTermsGateFromPreferences();
            //boolean preCheckEmailOpt = PerkPreferencesManager.INSTANCE.shouldPrecheckEmailOpt();

            Log.w("PerkSession","5");
            //String searchBaseURL = getSharedPreferences().getString("searchBaseURL", "");
            String LAST_LAUNCH_DATE = getSharedPreferences().getString("LAST_LAUNCH_DATE", "");

            LoginManager.getInstance().logOut();

            PerkPreferencesManager.INSTANCE.clearAllPreferences();

            // Clear this if being logged-out due to any reason
            HomeScreenItemsListManager.INSTANCE.setProtocolUri(null);

            if(m_bIsLog)Log.w(TAG,"5");
            try {

                android.webkit.CookieManager.getInstance().removeAllCookie();
            } catch (Exception e) {

            }

            if(m_bIsLog)Log.w(TAG,"6");
            //new Functions.searchBaseUrls().execute();

            // getSharedPreferencesEditor().putString("searchBaseURL", searchBaseURL);
            getSharedPreferencesEditor().putString("LAST_LAUNCH_DATE", LAST_LAUNCH_DATE);
            getSharedPreferencesEditor().commit();

            // Reset authenticated session
            // Commented the below POST /watchback/logout call as the auth-API called for logout
            // below (resetAuthenticationSession), has been updated to internally call this API
            /*PerkAPIController.INSTANCE.onUserLogOut(getAppContext(),
                    new OnRequestFinishedListener<NullableDataModel>() {
                        @Override
                        public void onSuccess(@NonNull NullableDataModel nullableDataModel,
                                @Nullable String s) {
                            PerkLogger.d(TAG, "onUserLogOut(): Successful!");
                        }

                        @Override
                        public void onFailure(@NonNull ErrorType errorType,
                                @Nullable PerkResponse<NullableDataModel> perkResponse) {
                            PerkLogger.e(TAG,
                                    "onUserLogOut(): Failed! " + (perkResponse != null
                                            ? perkResponse.getMessage() : ""));
                        }
                    });*/
            AuthAPIRequestController.INSTANCE.resetAuthenticationSession(getAppContext());

            // Clear User info on log out
            PerkUserManager.INSTANCE.getUser().set(null);

            if(m_bIsLog)Log.w(TAG,"7");

            //getAppContext().sendBroadcast(new Intent(AppConstants.ACTION_FINISH));

            PerkFileManager.deletePerkUser();

            PerkPreferencesManager.INSTANCE.putUserAllowedToSkipTncIntoPreference(allowTncSkip);
            PerkPreferencesManager.INSTANCE.putUserAllowedToSkipTermsGateIntoPreference(allowTermsGateSkip);
            //PerkPreferencesManager.INSTANCE.putPrecheckEmailOptIntoPreference(preCheckEmailOpt);

            Intent intent = new Intent(
                    getAppContext(),
                    WalkthroughActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            getAppContext().startActivity(intent);

            if(m_bIsLog)Log.w(TAG,"8");
        } catch (Exception e) {
            if(m_bIsLog)Log.w(TAG,"9");
            e.printStackTrace();
        }

    }

    public static void setTitleToolbar(Activity activity, String title, boolean bHideLogo)
    {
        try {
           TextView perk_toolbar_title = activity.findViewById(R.id.perk_toolbar_title);
            if (perk_toolbar_title != null)
            {
                perk_toolbar_title.setText(title);
                if (bHideLogo){
                    perk_toolbar_title.setVisibility(View.GONE);

                }
                else{
                    perk_toolbar_title.setVisibility(View.VISIBLE);
                }

            }


        }catch (Exception e){}
    }

    public  static  void  showErrorMessageToast(ErrorType errorType, String message)
    {
        if (errorType == ErrorType.UNAUTHORIZED ) {
            getAppContext().sendBroadcast(new Intent(AppConstants.ACTION_FINISH));

            new Handler().postDelayed(new Runnable() {
                public void run() {
                    Log.w("PerkSession","13");
                    forceLogout(null);
                }
            }, 1000);

        } else if (errorType == ErrorType.ACCOUNT_SUSPENDED) {  // error 403
            // Do not show the dialog for Splash-screen since it changes the UI after few seconds:
            if (!(PerkUtils.getTopActivity() instanceof LaunchActivity)) {
                AppUtility.showGenericDialog(PerkUtils.getTopActivity(),
                        "Sorry, WatchBack is currently not available to you.",
                        (dialog, which) -> {
                            getAppContext().sendBroadcast(new Intent(AppConstants.ACTION_FINISH));
                            AppUtility.dismissDialog(dialog);
                            PerkLogger.e(TAG, "Logging out on error 403!");
                            logoutUser();
                        });
            }
        }
        else if (errorType == ErrorType.FORCE_UPDATE)
        {
            try {
                if (PerkUtils.m_bIsLog) Log.w(PerkUtils.TAG, "FORCE_UPDATE 1");
               // if (!ForceUpdateActivity.sIsDisplayed)
                {
                    if (PerkUtils.m_bIsLog) Log.w(PerkUtils.TAG, "FORCE_UPDATE 2");

                    ForceUpdateActivity.sIsDisplayed = true;
                    ForceUpdateActivity.sIsForceUpdate = true;
                    ForceUpdateActivity.sStrUrl = errorType.getErrorDetails().getAppDetails().getStoreUrl();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            getAppContext().sendBroadcast(new Intent(AppConstants.ACTION_FINISH));
                            Intent intent = new Intent(getAppContext(), ForceUpdateActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            getAppContext().startActivity(intent);
                            if (PerkUtils.m_bIsLog) Log.w(PerkUtils.TAG, "FORCE_UPDATE 3");
                        }
                    }, 3000);

                }

            }catch (Exception e){
                if (PerkUtils.m_bIsLog) Log.w(PerkUtils.TAG, "FORCE_UPDATE 4");
            }

        }
        else
        {
            if (!TextUtils.isEmpty(message))
                Toast.makeText(getAppContext(), message, Toast.LENGTH_SHORT).show();
        }


        try{
            /////
            if (PerkUtils.isUserLoggedIn()) {
                HashMap<String, Object> contextData = new HashMap<String, Object>();
                contextData.put("tve.title","Error");
                contextData.put("tve.userpath","Error");
                contextData.put("tve.contenthub","Error");
                //contextData.put("tve.error","true");
                if (message != null){
                    contextData.put("tve.error", message);
                }

                AdobeTracker.INSTANCE.trackState("Error",contextData);
            }

            /////
        }catch (Exception e){}
    }

    public static void setCrashlyticsDataForCurrentUser(){

        try{
            String userId = PerkFileManager.loadPerkUser().getUuid();
            String email = PerkFileManager.loadPerkUser().getEmail();

            Crashlytics.setUserIdentifier(userId);
            Crashlytics.setUserEmail(email);
        }catch (Exception e){}


    }

    public static boolean m_bIsDOBAndGenderShowing = false;

    public  static String getAdvertisingId(){
        if (strAdvertisingId.length() < 1){
            getIdThread();
        }
        return strAdvertisingId;
    }
    public static boolean isLimitAdTrackingEnabled()
    {
        return bIsLimitAdTrackingEnabled;
    }
    public static String getAppVersionNumber(){

        try {
            PackageInfo pInfo = getAppContext().getPackageManager().getPackageInfo(getAppContext().getPackageName(),
                    0);
            String versionInfo = getAppContext().getResources().getString(R.string.app_version_message,
                    Calendar.getInstance().get(Calendar.YEAR),
                    (pInfo != null ? pInfo.versionName : ""));
            return versionInfo;
        } catch (PackageManager.NameNotFoundException e) {

        }
        return "";
    }
    public static String getDeviceName() {

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }
    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }

    public static boolean isTablet(Context context) {
        /*boolean isTablet = (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;*/

        boolean isTablet = context.getResources().getBoolean(R.bool.isTablet);

        PerkLogger.d(TAG, "isTablet returning " + isTablet);

        return isTablet;
    }
}
