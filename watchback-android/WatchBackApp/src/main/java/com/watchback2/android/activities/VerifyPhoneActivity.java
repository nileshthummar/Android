package com.watchback2.android.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.databinding.ActivityVerifyPhoneBinding;
import com.watchback2.android.utils.AppConstants;
import com.watchback2.android.utils.PerkUtils;
import com.watchback2.android.viewmodels.VerifyPhoneActivityViewModel;

public class VerifyPhoneActivity extends BaseThemeableActivity implements TextWatcher, VerifyPhoneActivityViewModel.IVerifyPhoneNavigator {
    private static final String TAG = "VerifyPhoneActivity";

    private ActivityVerifyPhoneBinding activityVerifyPhoneBinding;

    private VerifyPhoneActivityViewModel viewModel;

    public static final String PHONE_NUMBER = "phone_number";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!PerkUtils.isUserLoggedIn()){
            finish();
            return;
        }

        activityVerifyPhoneBinding = DataBindingUtil.setContentView(this, R.layout.activity_verify_phone);
        activityVerifyPhoneBinding.idEtCode1.addTextChangedListener(this);
        activityVerifyPhoneBinding.idEtCode2.addTextChangedListener(this);
        activityVerifyPhoneBinding.idEtCode3.addTextChangedListener(this);
        activityVerifyPhoneBinding.idEtCode4.addTextChangedListener(this);
        activityVerifyPhoneBinding.idEtCode5.addTextChangedListener(this);

        String mobileNumber = "";
        if(getIntent() != null && getIntent().hasExtra(PHONE_NUMBER)){
            mobileNumber = getIntent().getStringExtra(PHONE_NUMBER);
        }

        viewModel = new VerifyPhoneActivityViewModel(this);
        viewModel.setMobileNumber(mobileNumber);

        activityVerifyPhoneBinding.setViewModel(viewModel);

        registerFinishReceiver();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        activityVerifyPhoneBinding.idEtCode1.requestFocus();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        // disabling back press
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
    protected void setAppropriateTheme() {
        // Always use night mode for this
        setTheme(R.style.AppTheme);
    }

    // ---------------------------------------------------------------------------------------------
    // TextWatcher Implementation
    // ---------------------------------------------------------------------------------------------

    // This is handled in activity do that we can use activity's context to link the edittexts to
    // act like a single edit text when the focus moves to next field as user inputs text.
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        TextView text = (TextView) getCurrentFocus();

        if (text != null && text.length() > 0 && count == 1) {   //on input move to right
            View next = text.focusSearch(View.FOCUS_RIGHT);
            if (next != null && getVerificationCode().length() < 5) {
                next.requestFocus();
            } else if (getVerificationCode().length() == 5) {
                hideSoftKeyboard();
                //activate continue button
                viewModel.activateContinueButton();
            }

        } else if (before == 1 && count == 0) {
            //on deletion move left
            /*View next = text.focusSearch(View.FOCUS_LEFT);
            if (next != null) {
                next.requestFocus();
            }*/
            if (getVerificationCode().length() < 5) {
                viewModel.deactivateContinueButton();
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    // ---------------------------------------------------------------------------------------------
    // VerifyPhoneActivityViewModel.IVerifyPhoneNavigator Implementation
    // ---------------------------------------------------------------------------------------------
    @Override
    public void handleBackClick() {
        clearAllEditTexts();
        hideSoftKeyboard();
        finish();
    }

    @Override
    public void handleResendClick() {
        clearAllEditTexts();

        viewModel.makeResendCall(this);
    }

    @Override
    public void handleContinueClick() {
        PerkLogger.d(TAG, getVerificationCode());
        hideSoftKeyboard();

        if (getVerificationCode().length() == 5) {
            viewModel.makeCodeVerificationCall(this, getVerificationCode());
        } else {
            Toast.makeText(this, "Please recheck your verification code.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onSuccessfulResendPost() {
        Toast.makeText(this, "Code sent", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccessfulCodeVerification() {
        unregisterFinishReceiver();

//        sendBroadcast(new Intent(AppConstants.ACTION_FINISH));

        Intent intent = new Intent(VerifyPhoneActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void resendFailed() {

    }

    @Override
    public void verificationFailed() {

    }

    // ---------------------------------------------------------------------------------------------
    // private methods
    // ---------------------------------------------------------------------------------------------

    private String getVerificationCode() {
        return activityVerifyPhoneBinding.idEtCode1.getText().toString() +
                activityVerifyPhoneBinding.idEtCode2.getText().toString() +
                activityVerifyPhoneBinding.idEtCode3.getText().toString() +
                activityVerifyPhoneBinding.idEtCode4.getText().toString() +
                activityVerifyPhoneBinding.idEtCode5.getText().toString();
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = getCurrentFocus();
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void clearAllEditTexts() {
        activityVerifyPhoneBinding.idEtCode1.setText("");
        activityVerifyPhoneBinding.idEtCode2.setText("");
        activityVerifyPhoneBinding.idEtCode3.setText("");
        activityVerifyPhoneBinding.idEtCode4.setText("");
        activityVerifyPhoneBinding.idEtCode5.setText("");

        activityVerifyPhoneBinding.idEtCode1.requestFocus();
    }

    // ---------------------------------------------------------------------------------------------
    // FinishReceiver
    // ---------------------------------------------------------------------------------------------
    private final class FinishReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AppConstants.ACTION_FINISH)) {
                finish();
            }
        }
    }

}
