package com.watchback2.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import com.watchback2.android.R;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.controllers.PerkUserManager;
import com.watchback2.android.databinding.ActivityMyAccountBinding;
import com.watchback2.android.navigators.IAccountNavigator;
import com.watchback2.android.utils.AppConstants;
import com.watchback2.android.viewmodels.FragmentHeaderViewModel;

public class MyAccountActivity extends BaseThemeableActivity implements IAccountNavigator {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActivityMyAccountBinding binding = DataBindingUtil.setContentView(this,
                R.layout.activity_my_account);

        FragmentHeaderViewModel fragmentHeaderViewModel = new FragmentHeaderViewModel(this);
        binding.setUserInfoViewModel(fragmentHeaderViewModel);
        binding.setNavigator(this);

        registerFinishReceiver();

        // Facebook Analytics: Account Screen - When a user lands on the Account Screen
        FacebookEventLogger.getInstance().logEvent("Account Screen");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh the user-info
        PerkUserManager.INSTANCE.getUserInfo(this);
    }

    @Override
    public void onBackClick() {
        onBackPressed();
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

    private final class FinishReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AppConstants.ACTION_FINISH)) {
                finish();
            }
        }
    }
}
