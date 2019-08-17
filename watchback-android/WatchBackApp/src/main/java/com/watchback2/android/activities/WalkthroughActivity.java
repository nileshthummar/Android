package com.watchback2.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.perk.util.PerkLogger;
import com.singular.sdk.Singular;
import com.watchback2.android.R;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.databinding.ActivityWalkthroughBinding;
import com.watchback2.android.navigators.IWalkthroughNavigator;
import com.watchback2.android.utils.AppConstants;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkPreferencesManager;
import com.watchback2.android.viewmodels.WalkthroughViewModel;

import java.util.HashMap;


public class WalkthroughActivity extends BaseThemeableActivity implements IWalkthroughNavigator {

    private static final String TAG = "WalkthroughActivity";

    public static final String LOGO_URL = "logo_url";

    public static final String BACKGROUND_URL = "background_url";

    public static final String DESCRIPTION_TXT = "description_txt";

    private FinishReceiver mFinishReceiver;

    private ActivityWalkthroughBinding mActivityWalkthroughBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityWalkthroughBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_walkthrough);

        WalkthroughViewModel viewModel = new WalkthroughViewModel();

        mActivityWalkthroughBinding.setViewModel(viewModel);

        viewModel.setNavigator(this);
        if(getIntent() != null && getIntent().getExtras() != null){
            String bgImage = getIntent().getStringExtra(BACKGROUND_URL);
            String logoImage = getIntent().getStringExtra(LOGO_URL);
            String desTxt = getIntent().getStringExtra(DESCRIPTION_TXT);

            if(!TextUtils.isEmpty(bgImage)){
                viewModel.getBackgroundURl().set(bgImage);
                viewModel.getBackgroundVisibility().set(View.VISIBLE);
            }
            if(!TextUtils.isEmpty(logoImage)){
                viewModel.getLogoURl().set(logoImage);
                viewModel.getLogoVisbility().set(View.VISIBLE);
            }
            if(!TextUtils.isEmpty(desTxt)){
                viewModel.getDescriptionTxt().set(desTxt);
            }
            mActivityWalkthroughBinding.idProgressBar.setVisibility(View.GONE);

        }else{
            viewModel.loadWalkthrough(getApplicationContext());
        }

        registerFinishReceiver();

        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("tve.title","Sign-Up Fork");
        contextData.put("tve.userpath","Sign-Up Fork");
        contextData.put("tve.contenthub","Sign-Up");
        AdobeTracker.INSTANCE.trackState("Sign-Up Fork",contextData);
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

    private void registerFinishReceiver() {
        unregisterFinishReceiver();
        mFinishReceiver = new FinishReceiver();
        registerReceiver(mFinishReceiver, new IntentFilter(AppConstants.ACTION_FINISH));
    }

    private void unregisterFinishReceiver() {
        if (mFinishReceiver != null) {
            unregisterReceiver(mFinishReceiver);
            mFinishReceiver = null;
        }
    }

    @Override
    public void handleSkipClick() {
        //////
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("tve.title","Sign-Up Fork");
        contextData.put("tve.userpath","Click:Skip");
        contextData.put("tve.contenthub","Sign-Up");
        contextData.put("tve.action","true");
        AdobeTracker.INSTANCE.trackAction("Click:Skip",contextData);
        //////

        // Singular: skip_registration - user taps 'Watch Now' to skip registration
        Singular.event("skip_registration");

        AdobeTracker.INSTANCE.trackAppsFlyerEvent(this,"skip_registration",null);

        // Facebook Analytics: skip_registration - user taps 'Watch Now' to skip registration
        FacebookEventLogger.getInstance().logEvent("skip_registration");

        if (!isLocked()) {

            if (PerkPreferencesManager.INSTANCE.getUserAllowedToSkipTermsGateFromPreferences()) {
                sendBroadcast(new Intent(AppConstants.ACTION_FINISH));

                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, TermsGateActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }

    @Override
    public void handleSignUpClick() {
        //////
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("tve.title","Sign-Up Fork");
        contextData.put("tve.userpath","Click:Continue with Facebook");
        contextData.put("tve.contenthub","Sign-Up");
        contextData.put("tve.action","true");
        AdobeTracker.INSTANCE.trackAction("Click:Continue with Facebook",contextData);
        //////


        if (!isLocked()) {
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            intent.putExtra(AppConstants.LOGIN_MODE, 1);
            startActivity(intent);
        }
    }

    @Override
    public void handleLogInClick() {

        //////
        HashMap<String, Object> contextData = new HashMap<String, Object>();
        contextData.put("tve.title","Sign-Up Fork");
        contextData.put("tve.userpath","Click:Log-In");
        contextData.put("tve.contenthub","Sign-Up");
        contextData.put("tve.action","true");
        AdobeTracker.INSTANCE.trackAction("Click:Log-In",contextData);
        //////

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    private final class FinishReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && AppConstants.ACTION_FINISH.equals(intent.getAction())) {
                PerkLogger.w(TAG, "FinishReceiver: ACTION_FINISH");
                finish();
            }
        }
    }

    private boolean isLocked() {
        if (PerkPreferencesManager.INSTANCE.is24HourLockEnforced()) {
            AppUtility.showGenericDialog(this,
                    getResources().getString(R.string.signup_validation_under_13_years_old),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppUtility.dismissDialog(dialog);
                        }
                    });
            return true;
        }

        return false;
    }
}
