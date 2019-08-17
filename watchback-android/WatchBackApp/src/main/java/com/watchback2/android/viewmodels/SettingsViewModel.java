package com.watchback2.android.viewmodels;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.CompoundButton;

import com.perk.request.auth.AuthAPIRequestController;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.helper.UserInfoValidator;
import com.watchback2.android.navigators.ISettingsNavigator;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkPreferencesManager;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

/**
 * Created by perk on 05/04/18.
 */

public class SettingsViewModel {

    private static final String LOG_TAG = "SettingsViewModel";

    private final ObservableField<String> appVersion = new ObservableField<>("");

    private final ObservableBoolean nightTheme = new ObservableBoolean(true);

    private final WeakReference<Context> mContextWeakReference;

    private WeakReference<ISettingsNavigator> mNavigatorWeakReference;

    public SettingsViewModel(@NonNull Context context) {
        mContextWeakReference = new WeakReference<>(context.getApplicationContext());

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    0);
            String versionInfo = context.getResources().getString(R.string.app_version_message,
                    Calendar.getInstance().get(Calendar.YEAR),
                    (pInfo != null ? pInfo.versionName : ""));
            getAppVersion().set(versionInfo);
        } catch (PackageManager.NameNotFoundException e) {
            PerkLogger.e(LOG_TAG, "Exception trying to get version information:", e);
        }

        getNightTheme().set(PerkPreferencesManager.INSTANCE.isNightMode());
    }

    public void setNavigator(ISettingsNavigator navigator) {
        mNavigatorWeakReference = new WeakReference<>(navigator);
    }

    // Used for setting drawable-left via data-Binding since we cannot currently get the
    // drawable's integer resource value via xml
    // ----------- VectorDrawable Resources (START) -----------
    public int getFacebookIconResourceId() {
        return R.drawable.ic_account_icon;
    }

    public int getThemeIconResourceId() {
        return R.drawable.ic_theme;
    }

    public int getCarrotIconResourceId() {
        return R.drawable.ic_carrot_settings;
    }

    public int getShareIconResourceId() {
        return R.drawable.ic_share;
    }

    public int getRateIconResourceId() {
        return R.drawable.ic_rate_icon;
    }

    // ----------- VectorDrawable Resources (END) -----------

    @Nullable
    private Context getContext() {
        if (mContextWeakReference == null) {
            return null;
        }

        return mContextWeakReference.get();
    }

    @Nullable
    private ISettingsNavigator getNavigator() {
        if (mNavigatorWeakReference == null) {
            return null;
        }

        return mNavigatorWeakReference.get();
    }

    public ObservableField<String> getAppVersion() {
        return appVersion;
    }

    public ObservableBoolean getNightTheme() {
        return nightTheme;
    }

    public void handleBackClick(View view) {
        if (getNavigator() != null) {
            getNavigator().onBackClick();
        }
    }

    public void handleThemeClick(CompoundButton buttonView, boolean isChecked) {
        PerkPreferencesManager.INSTANCE.saveAppThemeIntoPreference(isChecked);
        getNightTheme().set(isChecked);
    }

    public void handleMyAccountClick(View view) {
        if (getNavigator() != null) {
            getNavigator().onMyAccountClick();
        }
        try{
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Settings");
            contextData.put("tve.userpath","Click:My Account");
            contextData.put("tve.contenthub","Settings");
            contextData.put("tve.share","true");
            AdobeTracker.INSTANCE.trackAction("Click:My Account",contextData);
            /////
        }catch (Exception e){}
    }

    public void handleShareClick(View view) {
        if (getNavigator() != null) {
            getNavigator().onShareClick();
        }
        try{
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Settings");
            contextData.put("tve.userpath","Click:Share");
            contextData.put("tve.contenthub","Settings");
            contextData.put("tve.share","true");
            AdobeTracker.INSTANCE.trackAction("Click:Share",contextData);
            /////
        }catch (Exception e){}
    }

    public void handleRateUsClick(View view) {
        if (getNavigator() != null) {
            getNavigator().onRateUsClick();
        }

        try{
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Settings");
            contextData.put("tve.userpath","Click:Rate Us");
            contextData.put("tve.contenthub","Settings");
            contextData.put("tve.action","true");
            AdobeTracker.INSTANCE.trackAction("Click:Rate Us",contextData);
            /////
        }catch (Exception e){}
    }

    // No channels / interests selection:
    // https://jira.rhythmone.com/browse/PEWAN-487
    /*public void handleEditInterestsClick(View view) {
        if (getNavigator() != null) {
            getNavigator().onEditInterestsClick();
        }
    }

    public void handleEditChannelsClick(View view) {
        if (getNavigator() != null) {
            getNavigator().onEditChannelsClick();
        }
    }*/

    public void handleSupportClick(View view) {
        if (getNavigator() != null) {
            getNavigator().onSupportClick();
        }
    }

    public void handleTermsClick(View view) {
        if (getNavigator() != null) {
            getNavigator().onTermsClick();
        }
        try{
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Settings:Terms & Conditions");
            contextData.put("tve.userpath","Settings:Terms & Conditions");
            contextData.put("tve.contenthub","Settings");

            AdobeTracker.INSTANCE.trackState("Settings:Terms & Conditions",contextData);
            /////
        }catch (Exception e){}
    }

    public void handlePrivacyClick(View view) {
        if (getNavigator() != null) {
            getNavigator().onPrivacyClick();
        }

        try{
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Settings:Privacy Policy");
            contextData.put("tve.userpath","Settings:Privacy Policy");
            contextData.put("tve.contenthub","Settings");

            AdobeTracker.INSTANCE.trackState("Settings:Privacy Policy",contextData);
            /////
        }catch (Exception e){}
    }

    public void handleVppaClick(View view) {
        if (getNavigator() != null) {
            getNavigator().onVppaClick();
        }

        try{
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Settings:Video Viewing Policy");
            contextData.put("tve.userpath","Settings:Video Viewing Policy");
            contextData.put("tve.contenthub","Settings");

            AdobeTracker.INSTANCE.trackState("Settings:Video Viewing Policy",contextData);
            /////
        }catch (Exception e){}
    }

    public void handleAboutNielsenClick(View view) {
        if (getNavigator() != null) {
            getNavigator().onAboutNielsenClick();
        }

        try{
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Settings:About Nielsen Measurement");
            contextData.put("tve.userpath","Settings:About Nielsen Measurement");
            contextData.put("tve.contenthub","Settings");

            AdobeTracker.INSTANCE.trackState("Settings:About Nielsen Measurement",contextData);
            /////
        }catch (Exception e){}
    }

    public void handleLogOutClick(View view) {
        if (getContext() != null) {
            PerkLogger.d(LOG_TAG, "handleLogOutClick: Initiating Log out...");

            if (!AppUtility.isNetworkAvailable(getContext())) {
                PerkLogger.w(LOG_TAG, "handleLogOutClick: Returning as network is unavailable!");
                return;
            }
            AppUtility.logOutUser(getContext());
        }

        try{
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Settings");
            contextData.put("tve.userpath","Click:Log-Out");
            contextData.put("tve.contenthub","Settings");
            contextData.put("tve.action","true");
            contextData.put("tve.userid","unknown");
            contextData.put("tve.userstatus","Not Logged In");
            contextData.put("tve.registrationtype","unknown");
            contextData.put("tve.usercat1","unknown");
            contextData.put("tve.usercat2","unknown");
            AdobeTracker.INSTANCE.trackAction("Click:Log-Out",contextData);
            /////
        }catch (Exception e){}
    }

    public boolean isLoggedIn() {
        return getContext() != null && UserInfoValidator.isAuthenticated(
                AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(
                        getContext()));
    }
}
