package com.watchback2.android.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;

import com.leanplum.Leanplum;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.controllers.LoginSignupManager;
import com.watchback2.android.databinding.ActivitySignupBinding;
import com.watchback2.android.navigators.ISignupNavigator;
import com.watchback2.android.utils.AppConstants;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkPreferencesManager;
import com.watchback2.android.utils.TrackingEvents;
import com.watchback2.android.viewmodels.SignupViewModel;
import com.watchback2.android.views.DatePickerFragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class SignupActivity extends BaseThemeableActivity implements ISignupNavigator,
        LoginSignupManager.ILoginSignupManager {

    private static final String TAG = "SignupActivity";

    private static final int ANIMATION_DELAY = 500;

    private SignupViewModel mSignupViewModel;

    private ActivitySignupBinding mActivitySignupBinding;

    private Date finalDateOfBirth;

    private boolean isSignupInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivitySignupBinding = DataBindingUtil.setContentView(this, R.layout.activity_signup);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mSignupViewModel = new SignupViewModel();
        mSignupViewModel.setNavigator(this);
        mActivitySignupBinding.setViewModel(mSignupViewModel);

        mActivitySignupBinding.idSignupContainer.getLayoutTransition().setDuration(ANIMATION_DELAY);

        registerFinishReceiver();

        if (PerkPreferencesManager.INSTANCE.getUserAllowedToSkipTncFromPreferences()) {
            PerkLogger.d(TAG,
                    "Auto-checking T&C disclaimer as user has logged in on this "
                            + "device previously!");
            mActivitySignupBinding.idTermsCheck.setChecked(true);

        } else if (PerkPreferencesManager.INSTANCE.getUserAllowedToSkipTermsGateFromPreferences()) {
            PerkLogger.d(TAG,
                    "Auto-checking T&C disclaimer as user has passed the terms gate previously!");
            mActivitySignupBinding.idTermsCheck.setChecked(true);
        }

        /*if (PerkPreferencesManager.INSTANCE.shouldPrecheckEmailOpt()) {
            PerkLogger.d(TAG, "Auto-checking email-opt checkbox as user has allowed it before");
            mActivitySignupBinding.idEmailOptCheck.setChecked(true);
        }*/

        //////
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("tve.title", "Sign-Up:Email");
        contextData.put("tve.userpath", "Sign-Up:Email");
        contextData.put("tve.contenthub", "Sign-Up");
        contextData.put("tve.registrationstart", "true");
        contextData.put("tve.registrationtype", "Email");
        AdobeTracker.INSTANCE.trackState("Sign-Up:Email", contextData);
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
    public void onSignupClick() {
        Date dateOfBirth = null;
        DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        try {
            dateOfBirth = formatter.parse(mSignupViewModel.getDoBText().get());
        } catch (ParseException e) {
            PerkLogger.e(TAG, "ParseException ", e);
        }

        if (dateOfBirth == null) {
            PerkLogger.e(TAG, "Unable to get dateOfBirth!");
            return;
        }

        final Date finalDateOfBirth = dateOfBirth;

        if (SignupViewModel.isValidDateOfBirth(this, finalDateOfBirth) && termsAccepted(
                (dialog, which) -> {
                    // Unused for non-facebook login
                }, false)) {
            showMobileNumberScreen(finalDateOfBirth);
        }
    }

    private void showMobileNumberScreen(final Date finalDateOfBirth) {
        this.finalDateOfBirth = finalDateOfBirth;

        mActivitySignupBinding.idSignupContainer.setVisibility(View.GONE);
        mActivitySignupBinding.mobileNumberLayout.mobileNumberParent.setVisibility(View.VISIBLE);

        mActivitySignupBinding.mobileNumberLayout.idEtPhoneNo.setOnEditorActionListener((v, actionId, event) -> {
            String text = v.getText().toString();
            if (actionId == EditorInfo.IME_ACTION_DONE && !TextUtils.isEmpty(text) && text.length() >= 10) {
                onContinueClick();
            } else {
                // hide softkeyboard
                hideSoftKeyboard();

            }
            return true;
        });
        mActivitySignupBinding.mobileNumberLayout.idEtPhoneNo.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mActivitySignupBinding.mobileNumberLayout.idEtPhoneNo, InputMethodManager.SHOW_IMPLICIT);

    }

    private void doSignUp() {
        isSignupInProgress = true;

        // noinspection ConstantConditions
        LoginSignupManager.INSTANCE.signUpUser(this, mSignupViewModel.getEmail().get(),
                mSignupViewModel.getPassword().get(), mSignupViewModel.getPassword().get(), mSignupViewModel.getGenderText().get(), mSignupViewModel.getMobileNumber(),
                finalDateOfBirth, this);
    }

    private void showSignUpScreen() {
        isSignupInProgress = false;
        mActivitySignupBinding.idSignupContainer.setVisibility(View.VISIBLE);
        mActivitySignupBinding.mobileNumberLayout.mobileNumberParent.setVisibility(View.GONE);
    }

    private boolean termsAccepted(@NonNull DialogInterface.OnClickListener listener,
                                  boolean isForFbLogin) {
        if (mActivitySignupBinding == null || mActivitySignupBinding.idTermsCheck == null) {
            PerkLogger.e(TAG, "termsAccepted: mActivitySignupBinding or idTermsCheck is null");
            return false;
        }

        // Update local setting if user has opted in/out for marketing emails:
        PerkPreferencesManager.INSTANCE.putOptedInForEmailsIntoPreference(true);
        //mActivitySignupBinding.idEmailOptCheck.isChecked()

        /*if (mActivitySignupBinding.idEmailOptCheck.isChecked()) {
            // Update the setting that user has already agreed to marketing emails once on this
            // device, so that it is pre-checked always, the next time onwards
            PerkPreferencesManager.INSTANCE.putPrecheckEmailOptIntoPreference(true);
        }*/

        if (!mActivitySignupBinding.idTermsCheck.isChecked()) {
            PerkLogger.d(TAG, "termsAccepted: Terms not accepted!");

            AppUtility.showHtmlTextDialog(this, getString(R.string.terms_unaccepted), listener,
                    false);

            return false;
        }

        return true;
    }

    @Override
    public void onCallLoginClick() {
        //////
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("tve.title", "Sign-Up:Email");
        contextData.put("tve.userpath", "Click:Log-In");
        contextData.put("tve.contenthub", "Sign-Up");
        contextData.put("tve.action", "true");
        AdobeTracker.INSTANCE.trackAction("Click:Log-In", contextData);
        //////


        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDobClick() {
        final DatePickerFragment newFragment = new DatePickerFragment();

        Bundle bundle = new Bundle();
        bundle.putString(DatePickerFragment.DOBSTRING, mSignupViewModel.getDoBText().get());
        newFragment.setArguments(bundle);

        newFragment.getDoBText().addOnPropertyChangedCallback(
                new Observable.OnPropertyChangedCallback() {
                    @Override
                    public void onPropertyChanged(Observable observable, int i) {
                        mSignupViewModel.getDoBText().set(newFragment.getDoBText().get());
                    }
                });

        newFragment.show(getSupportFragmentManager(), "DatePicker");
    }

    @Override
    public void onGenderClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(R.array.gender,
                (dialog, which) -> mSignupViewModel.getGenderText().set(getResources().getStringArray(R.array.gender)[which]));
        builder.show();
    }

    @Override
    public void onContinueClick() {
        PerkLogger.d(TAG, "onContinueClick activity");
        hideSoftKeyboard();

        if (isSignupInProgress) {
            return;
        }
        mSignupViewModel.getContinueBtnClickable().set(false);

        doSignUp();
    }

    @Override
    public void onBackClick() {
        if (isSignupInProgress) {
            return;
        }

        if (mActivitySignupBinding.mobileNumberLayout.mobileNumberParent.getVisibility() == View.VISIBLE) {
            hideSoftKeyboard();
            showSignUpScreen();
        } else {
            finish();

        }

    }

    @Override
    public void onAuthenticationSuccessful() {
        isSignupInProgress = false;
        // Unregister since we also have receiver for same action:
        unregisterFinishReceiver();

        Leanplum.track(TrackingEvents.LEANPLUM_REGISTRATION_SUCCESS);

        sendBroadcast(new Intent(AppConstants.ACTION_FINISH));

        new Handler().postDelayed(() -> {
            // new PerkUtils.assignDeviceToUser().execute();
            Intent intent = new Intent(SignupActivity.this, VerifyPhoneActivity.class);
            intent.putExtra(VerifyPhoneActivity.PHONE_NUMBER, mSignupViewModel.getMobileNumber());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }, 300);
    }

    @Override
    public void onSignUpWithExistingAccount() {
        isSignupInProgress = false;
        PerkLogger.d(TAG, "onSignUpWithExistingAccount: Starting Login activity");

        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSignUpFailure() {
        isSignupInProgress = false;
        mSignupViewModel.getContinueBtnClickable().set(true);

        showSignUpScreen();
    }

    private final class FinishReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AppConstants.ACTION_FINISH)) {
                finish();
            }
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = getCurrentFocus();
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

}
