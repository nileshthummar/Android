package com.watchback2.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.controllers.LoginSignupManager;
import com.watchback2.android.databinding.ActivityLoginBinding;
import com.watchback2.android.navigators.ILoginNavigator;
import com.watchback2.android.utils.AppConstants;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkPreferencesManager;
import com.watchback2.android.utils.PerkUtils;
import com.watchback2.android.viewmodels.LoginViewModel;

import java.util.HashMap;

public class LoginActivity extends BaseThemeableActivity implements ILoginNavigator,
        LoginSignupManager.ILoginSignupManager {

    private static final String TAG = "LoginActivity";

    private static final int ANIMATION_DELAY = 500;

    private LoginViewModel mLoginViewModel;

    private ActivityLoginBinding mActivityLoginBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityLoginBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mLoginViewModel = new LoginViewModel(this);
        mLoginViewModel.setNavigator(this);
        mActivityLoginBinding.setViewModel(mLoginViewModel);

        mActivityLoginBinding.idLoginContainer.getLayoutTransition().setDuration(ANIMATION_DELAY);

        mActivityLoginBinding.idEmail.setText(PerkUtils.getDefaultEmail(getApplicationContext()));

        /*if (PerkPreferencesManager.INSTANCE.getUserAllowedToSkipTncFromPreferences()) {
            PerkLogger.d(TAG,
                    "Auto-checking T&C disclaimer as user has logged in on this "
                            + "device previously!");
            mActivityLoginBinding.idTermsCheck.setChecked(true);

        } else if (PerkPreferencesManager.INSTANCE.getUserAllowedToSkipTermsGateFromPreferences()) {
            PerkLogger.d(TAG,
                    "Auto-checking T&C disclaimer as user has passed the terms gate "
                            + "previously!");
            mActivityLoginBinding.idTermsCheck.setChecked(true);
        }*/

        /*if (PerkPreferencesManager.INSTANCE.shouldPrecheckEmailOpt()) {
            PerkLogger.d(TAG, "Auto-checking email-opt checkbox as user has allowed it before");
            mActivityLoginBinding.idEmailOptCheck.setChecked(true);
        }*/

        /*mLoginViewModel.onCheckChanged(mActivityLoginBinding.idTermsCheck,
                mActivityLoginBinding.idTermsCheck.isChecked());*/

        registerFinishReceiver();

        //////
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("tve.title","Log-In");
        contextData.put("tve.userpath","Log-In");
        contextData.put("tve.contenthub","Log-In");
        contextData.put("tve.loginstart","true");

        AdobeTracker.INSTANCE.trackState("Log-In",contextData);
        //////
    }

    @Override
    protected void setAppropriateTheme() {
        // Always use night mode for this
        setTheme(R.style.AppTheme);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterFinishReceiver();
    }

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

    @Override
    public void onForgotPasswordClick() {
        //Forgot password
        Log.d(TAG, "onForgotPasswordClick");

        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(WatchbackWebViewActivity.WATCHBACK_FORGOT_PASSWORD_URL), this,
                WatchbackWebViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(WatchbackWebViewActivity.INTENT_TITLE_EXTRA_KEY, getString(R.string.forgot_password));
        startActivity(intent);
    }

    @Override
    public void onCallSignupClick() {

        //////
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("tve.title","Log-In");
        contextData.put("tve.userpath","Click:Sign-Up with Email");
        contextData.put("tve.contenthub","Log-In");
        contextData.put("tve.action","true");

        AdobeTracker.INSTANCE.trackAction("Click:Sign-Up with Email",contextData);
        //////

        if (!isLocked()) {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean isLocked() {
        if (PerkPreferencesManager.INSTANCE.is24HourLockEnforced()) {
            AppUtility.showGenericDialog(this,
                    getResources().getString(R.string.signup_validation_under_13_years_old),
                    (dialog, which) -> AppUtility.dismissDialog(dialog));
            return true;
        }

        return false;

    }

    @Override
    public void onLoginClick() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (!termsAccepted((dialog, which) -> {
            // Unused for non-facebook login
        }, false)) {
            PerkLogger.d(TAG, "onLoginClick: returning as terms not accepted");
            return;
        }

        doEmailLogin();
    }

    private void doEmailLogin() {
        LoginSignupManager.INSTANCE.loginWithEmail(this, mLoginViewModel.getEmail().get(),
                mLoginViewModel.getPassword().get(),false, this);
    }

    private boolean termsAccepted(@NonNull DialogInterface.OnClickListener listener,
            boolean isForFbLogin) {
        if (mActivityLoginBinding == null) {
            PerkLogger.e(TAG, "termsAccepted: mActivityLoginBinding or idTermsCheck is null");
            return false;
        }

        // Update local setting if user has opted in/out for marketing emails:
        PerkPreferencesManager.INSTANCE.putOptedInForEmailsIntoPreference(true);
                //mActivityLoginBinding.idEmailOptCheck.isChecked();

        /*if (mActivityLoginBinding.idEmailOptCheck.isChecked()) {
            // Update the setting that user has already agreed to marketing emails once on this
            // device, so that it is pre-checked always, the next time onwards
            PerkPreferencesManager.INSTANCE.putPrecheckEmailOptIntoPreference(true);
        }*/

        /*if (!mActivityLoginBinding.idTermsCheck.isChecked()) {
            PerkLogger.d(TAG, "termsAccepted: Terms not accepted!");

            if (isForFbLogin) {
                AppUtility.showHtmlTextDialog(this, getString(R.string.terms_unaccepted_facebook),
                        listener, true);
            } else {
                AppUtility.showHtmlTextDialog(this, getString(R.string.terms_unaccepted), listener,
                        false);
            }

            return false;
        }*/

        return true;
    }

    @Override
    public void onBackClick() {
        finish();
    }

    @Override
    public void onAuthenticationSuccessful() {
        // Unregister since we also have receiver for same action:
        unregisterFinishReceiver();

        sendBroadcast(new Intent(AppConstants.ACTION_FINISH));

        new Handler().postDelayed(() -> {
            // new PerkUtils.assignDeviceToUser().execute();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }, 300);
    }

    @Override
    public void onSignUpWithExistingAccount() {
        // Unused for Login
    }

    @Override
    public void onSignUpFailure() {
       // Unused for Login
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
