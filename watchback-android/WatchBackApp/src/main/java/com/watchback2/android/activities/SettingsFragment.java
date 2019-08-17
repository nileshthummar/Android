package com.watchback2.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;

import com.watchback2.android.R;
import com.watchback2.android.analytics.AdobeTracker;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.databinding.ActivitySettingsBinding;
import com.watchback2.android.navigators.ISettingsNavigator;
import com.watchback2.android.viewmodels.SettingsViewModel;

import java.util.HashMap;

public class SettingsFragment extends Fragment implements ISettingsNavigator {

    private static final String LOG_TAG = "SettingsFragment";

    public static final String TAG = "SettingsFragment";

    private SettingsViewModel mSettingsViewModel;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public SettingsFragment(){
        super();
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.activity_settings, container, false);
        final ActivitySettingsBinding binding = ActivitySettingsBinding.bind(rootView);

        mSettingsViewModel = new SettingsViewModel(this.getContext());
        mSettingsViewModel.setNavigator(this);
        binding.setViewModel(mSettingsViewModel);

//        registerFinishReceiver();

        try{
            /////
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Settings");
            contextData.put("tve.userpath","Settings");
            contextData.put("tve.contenthub","Settings");

            AdobeTracker.INSTANCE.trackState("Settings",contextData);
            /////
        }catch (Exception e){}

        // Facebook Analytics: Settings Screen - When a user lands on the Settings Screen
        FacebookEventLogger.getInstance().logEvent("Settings Screen");

        return binding.getRoot();
    }

    @Override
    public void onBackClick() {
//        onBackPressed();
    }

    @Override
    public void onTermsClick() {
        if(getActivity() == null){
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("perk_wb_https://" + getString(R.string.terms_url_identifier)),
                getActivity(), WatchbackWebViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(WatchbackWebViewActivity.INTENT_TITLE_EXTRA_KEY, getString(R.string.terms_and_conditions));
        startActivity(intent);
    }

    @Override
    public void onPrivacyClick() {
        if(getActivity() == null){
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("perk_wb_https://" + getString(R.string.privacy_url_identifier)),
                getActivity(), WatchbackWebViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(WatchbackWebViewActivity.INTENT_TITLE_EXTRA_KEY, getString(R.string.privacy_policy));
        startActivity(intent);
    }

    @Override
    public void onVppaClick() {
        if(getActivity() == null){
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("perk_wb_https://" + getString(R.string.vppa_url_identifier)),
                getActivity(), WatchbackWebViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(WatchbackWebViewActivity.INTENT_TITLE_EXTRA_KEY, getString(R.string.vppa));
        startActivity(intent);
    }

    @Override
    public void onMyAccountClick() {
        if(getActivity() == null){
            return;
        }
        Intent intent = new Intent(getActivity(), MyAccountActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onShareClick() {
        if(getActivity() == null){
            return;
        }
        ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .setChooserTitle("Choose one")
                .setText(getString(R.string.share_watchback_url))
                .startChooser();
    }

    @Override
    public void onRateUsClick() {
        if(getActivity() == null){
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getActivity().getApplicationContext().getPackageName())));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://play.google.com/store/apps/details?id="
                            + getActivity().getApplicationContext().getPackageName())));
        }
    }

    @Override
    public void onEditInterestsClick() {
        if(getActivity() == null){
            return;
        }
        Intent intent = new Intent(getActivity(), InterestsActivity.class);
        intent.putExtra(InterestsActivity.EXTRA_EDIT_MODE, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onEditChannelsClick() {
        if(getActivity() == null){
            return;
        }
        Intent intent = new Intent(getActivity(), ChannelsActivity.class);
        intent.putExtra(ChannelsActivity.FROM_SETTINGS, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onSupportClick() {
        if(getActivity() == null){
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.support_url)),
                getActivity(), WatchbackWebViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(WatchbackWebViewActivity.INTENT_TITLE_EXTRA_KEY, getString(R.string.support_center));
        startActivity(intent);
    }

    @Override
    public void onAboutNielsenClick() {
        if(getActivity() == null){
            return;
        }
        Intent intent = new Intent(
                getActivity(), WatchbackOptOutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

   /* @Override
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
    }*/
}
