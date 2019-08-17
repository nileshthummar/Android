package com.watchback2.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;

import com.singular.sdk.Singular;
import com.watchback2.android.R;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.databinding.ActivityTermsGateBinding;
import com.watchback2.android.navigators.ISignupNavigator;
import com.watchback2.android.utils.AppConstants;
import com.watchback2.android.utils.PerkPreferencesManager;
import com.watchback2.android.viewmodels.TermsGateViewModel;

public class TermsGateActivity extends BaseThemeableActivity implements ISignupNavigator {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityTermsGateBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_terms_gate);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        TermsGateViewModel termsGateViewModel = new TermsGateViewModel();
        termsGateViewModel.setNavigator(this);
        binding.setViewModel(termsGateViewModel);
    }

    @Override
    protected void setAppropriateTheme() {
        // Always use night mode for this
        setTheme(R.style.AppTheme);
    }

    // Called on 'I Agree' button click:
    @Override
    public void onSignupClick() {
        // Save that user can proceed next time onwards directly on doing 'Skip'
        PerkPreferencesManager.INSTANCE.putUserAllowedToSkipTermsGateIntoPreference(true);

        // Singular: skip_agree_terms - user agrees to terms after skipping registration
        Singular.event("skip_agree_terms");

        // Facebook Analytics: skip_agree_terms - user agrees to terms after skipping registration
        FacebookEventLogger.getInstance().logEvent("skip_agree_terms");

        sendBroadcast(new Intent(AppConstants.ACTION_FINISH));

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCallLoginClick() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onGenderClick() {
        // Unused
    }

    @Override
    public void onDobClick() {
        // Unused
    }

    @Override
    public void onContinueClick() {
        // Unused
    }

    @Override
    public void onBackClick() {
        finish();
    }

}
