package com.watchback2.android.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.appsflyer.AppsFlyerLib;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.leanplum.Leanplum;
import com.perk.request.auth.AuthAPIRequestController;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.controllers.AppVersionUpdateCheck;
import com.watchback2.android.controllers.HomeScreenItemsListManager;
import com.watchback2.android.controllers.PerkUserManager;
import com.watchback2.android.controllers.WatchBackSettingsController;
import com.watchback2.android.databinding.ActivityMainBinding;
import com.watchback2.android.fragments.ChannelsFragment;
import com.watchback2.android.fragments.HomeFragment;
import com.watchback2.android.fragments.MovieTicketFragment;
import com.watchback2.android.fragments.UnregisteredMovieTicketFragment;
import com.watchback2.android.helper.UserInfoValidator;
import com.watchback2.android.navigators.ISettingsContainerNavigator;
import com.watchback2.android.utils.AppConstants;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class MainActivity extends BaseThemeableActivity implements ISettingsContainerNavigator,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String LOG_TAG = "MainActivity";

    public static final String SELECTED_TAB_INDEX = "selected_tab_index";

    //private static final int PERMISSIONS_REQUEST_CODE = 100;

    private HomeFragment fragmentHome;
    private ChannelsFragment fragmentChannels;
    private SettingsFragment fragmentSettings;

    private BottomNavigationViewEx navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // AppsFlyer DeepLinking:
        AppsFlyerLib.getInstance().sendDeepLinkData(this);

        PerkUserManager.INSTANCE.getUserInfo(getApplicationContext());

        WatchBackSettingsController.INSTANCE.refreshSettings(getApplicationContext());

        // No interests/channels selection: https://jira.rhythmone.com/browse/PEWAN-487
        /*if (!PerkPreferencesManager.INSTANCE.areUserInterestsSavedToPreferences()) {
            startActivity(new Intent(MainActivity.this, InterestsActivity.class));
            finish();
            return;
        }*/

        // Check if app version update is available
        new AppVersionUpdateCheck();

        // Location permissions removed as per https://jira.rhythmone.com/browse/PEWAN-351
        /*
        // Ask for location permission:
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Permission(s) not granted... request for it:
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_CODE);
        } else {
            PerkLogger.d(LOG_TAG, "Location permissions have been granted already");
        }*/

        ActivityMainBinding activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // code to increment app launch counter
        int _launchCount = PerkUtils.getSharedPreferences().getInt("launch_count", 0);
        _launchCount = _launchCount + 1;
        PerkUtils.getSharedPreferencesEditor().putInt("launch_count", _launchCount);
        PerkUtils.getSharedPreferencesEditor().commit();

        /*if (isReturnVisitUser()) {
            PerkUtils.trackEvent("android-perk-screen-returnvisit");
        }
        else
        {
            PerkUtils.trackEvent("android-perk-screen-newvisit");
        }*/
        registerFinishReceiver();

        navigation = activityMainBinding.navigation;
        navigation.enableAnimation(false);
        navigation.enableShiftingMode(false);
        navigation.enableItemShiftingMode(false);
        navigation.setTextVisibility(true);
        navigation.setTextSize(11f);
        navigation.setTypeface(ResourcesCompat.getFont(this, R.font.sf_pro_text_regular));
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // handling for BottomNavigationView selection text padding until Google fixes this bug:
        // https://issuetracker.google.com/issues/115754572
        int childCount = navigation.getChildCount();
        BottomNavigationMenuView navigationMenuView = null;
        for (int i = 0; i < childCount; i++) {
            View view = navigation.getChildAt(i);
            if (view instanceof BottomNavigationMenuView) {
                navigationMenuView = (BottomNavigationMenuView) view;
                break;
            }
        }

        if (navigationMenuView != null) {
            childCount = navigationMenuView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View itemView = navigationMenuView.getChildAt(i);
                if (itemView != null) {
                    View activeLabel = itemView.findViewById(
                            com.google.android.material.R.id.largeLabel);
                    if (activeLabel instanceof TextView) {
                        ((TextView) activeLabel).setPadding(0, 0, 0, 0);
                        ((TextView) activeLabel).setEllipsize(TextUtils.TruncateAt.END);
                    }
                }
            }
        }

        fragmentHome = HomeFragment.newInstance();
        fragmentChannels = ChannelsFragment.newInstance();
        fragmentSettings = SettingsFragment.newInstance();

        // Required in case we are returning back here after change of Theme (day/night mode)...
        // in this case, since we are not adding the first HomeFragment to the back-stack, it
        // remains visible when user tries to go back to other fragments...which looks weird on UI
        clearBackstack();

        // No back-stack for first fragment:
        AppUtility.addFragmentToActivityWithoutBackStack(getSupportFragmentManager(),
                fragmentHome, R.id.homeScreenPlacement, HomeFragment.TAG);
        trackLeanplumState(getString(R.string.tab_home));

        checkForProtocolUri(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // delay slightly so the the activity gets a chance to be resumed first (for showing
        // progress dialog -if required)
        new Handler().postDelayed(() -> checkForProtocolUri(intent), 600);
    }

    private void checkForProtocolUri(Intent intent) {
        String protocolUri = HomeScreenItemsListManager.INSTANCE.getProtocolUri();

        PerkLogger.d(LOG_TAG, "checkForProtocolUri(): Got protocolUri as " + protocolUri);

        if (!TextUtils.isEmpty(protocolUri)) {
            HomeScreenItemsListManager.INSTANCE.setProtocolUri(null);
            AppUtility.handleProtocolUri(protocolUri);

        } else if (intent != null && intent.hasExtra(SELECTED_TAB_INDEX)) {
            // check for tab selection:
            int pageIndex = intent.getIntExtra(SELECTED_TAB_INDEX, 0);

            if (navigation != null) {
                navigation.post(() -> navigation.setCurrentItem(pageIndex));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        /*if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;

            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
            } else {
                allGranted = false;
            }

            if (allGranted) {
                // permissions were granted, yay!
                PerkLogger.d(LOG_TAG, "Location permissions granted!");
            } else {
                // permissions denied, boo!
                PerkLogger.e(LOG_TAG, "Location permissions were not granted!");
            }
        }*/

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void clearBackstack() {
        while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(
                    0);
            getSupportFragmentManager().popBackStack(entry.getId(),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().executePendingTransactions();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current tab selection state:
        savedInstanceState.putInt(SELECTED_TAB_INDEX, navigation.getCurrentItem());

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            final int selection = savedInstanceState.getInt(SELECTED_TAB_INDEX, 0);
            navigation.post(() -> navigation.setCurrentItem(selection));
        }
    }

    @Override
    protected void onStop() {
        if(isApplicationBroughtToBackground(this)) {
            PerkUtils.getSharedPreferencesEditor().putBoolean(KEY_APP_IN_BACKGROUND, true).commit();
        }
        super.onStop();
    }

    Boolean bIsPNPH = false;

    @Override
    protected void onResume() {

        if (!bIsPNPH) {
            boolean comingFromBackground = PerkUtils.getSharedPreferences().getBoolean(
                    KEY_APP_IN_BACKGROUND, true);
            if (comingFromBackground) {
                if (PerkUtils.m_bIsLog) Log.w(PerkUtils.TAG, "FacebookCall");
                PerkUtils.getSharedPreferencesEditor().putBoolean(KEY_APP_IN_BACKGROUND,
                        false).commit();

                new Handler().postDelayed(() -> {
                    if (!AppUtility.isNetworkAvailable(PerkUtils.getAppContext())) {
                        PerkLogger.w(LOG_TAG, "MainActivity: onResume: network is unavailable!");
                    }
                }, 1000);
            }
        }

        bIsPNPH = false;

        super.onResume();

        WatchBackSettingsController.INSTANCE.refreshSettings(getApplicationContext());
    }

    private static final String KEY_APP_IN_BACKGROUND = "AppInBackground";
    private static boolean isApplicationBroughtToBackground(final Activity activity) {
        try
        {


            ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = null;
            try {
                tasks = activityManager.getRunningTasks(1);
            } catch (SecurityException e) {
                //if(PerkUtils.PerkUtils.m_bIsLog)Log.w("Nilesh", "Missing required permission: \"android.permission.GET_TASKS\".", e);
                return false;
            }
            if (tasks != null && !tasks.isEmpty()) {
                ComponentName topActivity = tasks.get(0).topActivity;
                try {
                    PackageInfo pi = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_ACTIVITIES);
                    for (ActivityInfo activityInfo : pi.activities) {
                        if(topActivity.getClassName().equals(activityInfo.name)) {
                            return false;
                        }
                    }
                } catch( PackageManager.NameNotFoundException e) {
                    //if(PerkUtils.PerkUtils.m_bIsLog)Log.w("Nilesh", "Package name not found: " + activity.getPackageName());
                    return false; // Never happens.
                }
            }


        }catch(Exception e)
        {

        }
        return true;
    }

    @Override
    protected void onDestroy() {
        unregisterFinishReceiver();
        super.onDestroy();
    }
    private FinishReceiver finishReceiver;
    private  void registerFinishReceiver()
    {
        try {
            unregisterFinishReceiver();
            finishReceiver = new FinishReceiver();
            registerReceiver(finishReceiver, new IntentFilter(AppConstants.ACTION_FINISH));
        }catch (Exception e){}
    }
    private  void unregisterFinishReceiver()
    {
        try {
            if (finishReceiver != null)
            {
                unregisterReceiver(finishReceiver);
                finishReceiver = null;
            }
        }catch (Exception e){}
    }

    private final class FinishReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AppConstants.ACTION_FINISH))
                finish();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            PerkLogger.d(LOG_TAG, "onNavigationItemSelected(): " + item.toString());

            switch (item.getItemId()) {
                case R.id.navigation_discover:
                    doFragmentTransition(fragmentHome, HomeFragment.TAG);
                    trackLeanplumState(getString(R.string.tab_home));
                    return true;

                case R.id.navigation_channels:
                    doFragmentTransition(fragmentChannels, ChannelsFragment.TAG);
                    trackLeanplumState(getString(R.string.tab_channels));
                    return true;

                case R.id.navigation_tickets:
                    if (!isUserLoggedIn()) {
                        showUnregisteredFragment(item.getItemId());
                    } else {
                        showMovieTicketFragment();

                        // visit_tysweeps_screen - user taps on thank you icon in bottom menu
                        // (logged in users only)

                        // Singular:
                        //Singular.event("visit_tysweeps_screen");

                        // Facebook Analytics:
                        //FacebookEventLogger.getInstance().logEvent("visit_tysweeps_screen");
                    }
                    trackLeanplumState(getString(R.string.tab_thank_you));
                    return true;
                case R.id.navigation_settings:
                    doFragmentTransition(fragmentSettings, SettingsFragment.TAG);
                    trackLeanplumState(getString(R.string.tab_settings));
                    return true;

                default:
                    return false;
            }
        }
    };

    private boolean isUserLoggedIn() {
        return UserInfoValidator.isAuthenticated(
                AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(
                        getApplicationContext()));
    }

    private void showUnregisteredFragment(int menuItemId) {
        UnregisteredMovieTicketFragment unregisteredFragment;
        String fragmentTag;

        fragmentTag = UnregisteredMovieTicketFragment.TAG;
        unregisteredFragment =
                (UnregisteredMovieTicketFragment) getSupportFragmentManager().findFragmentByTag(
                        fragmentTag);

        if (unregisteredFragment == null) {
            unregisteredFragment = UnregisteredMovieTicketFragment.newInstance();

            Bundle bundle = new Bundle();
            unregisteredFragment.setArguments(bundle);
        }

        if (unregisteredFragment.isVisible()) {
            PerkLogger.d(LOG_TAG,
                    "showUnregisteredFragment(): Returning as fragment already visible for tag: "
                            + fragmentTag);
            return;
        }

        doFragmentTransition(unregisteredFragment, fragmentTag);
    }

    private void showMovieTicketFragment(){
        MovieTicketFragment movieTicketFragment =
                (MovieTicketFragment) getSupportFragmentManager().findFragmentByTag(
                        MovieTicketFragment.TAG);

        if (movieTicketFragment == null) {
            movieTicketFragment = MovieTicketFragment.newInstance();

            Bundle bundle = new Bundle();
            movieTicketFragment.setArguments(bundle);
        }

        if (movieTicketFragment.isVisible()) {
            PerkLogger.d(LOG_TAG, "showMovieTicketFragment(): Returning as fragment already visible");
            return;
        }

        doFragmentTransition(movieTicketFragment, MovieTicketFragment.TAG);
    }

    public void showHomeFragment(){
        HomeFragment homeFragment =
                (HomeFragment) getSupportFragmentManager().findFragmentByTag(
                        HomeFragment.TAG);

        if (homeFragment == null) {
            homeFragment = HomeFragment.newInstance();

            Bundle bundle = new Bundle();
            homeFragment.setArguments(bundle);
        }

        if (homeFragment.isVisible()) {
            PerkLogger.d(LOG_TAG, "showMovieTicketFragment(): Returning as fragment already visible");
            return;
        }
        navigation.setCurrentItem(getResources().getInteger(R.integer.home));
        trackLeanplumState(getString(R.string.tab_home));
        doFragmentTransition(homeFragment, HomeFragment.TAG);
    }

    @Override
    public void handleEditClick(View view) {
        // Unused
    }

    @Override
    public void handleSettingsClick(View view) {
        // Facebook Analytics: settings_tap - user taps on Settings:
        /*FacebookEventLogger.getInstance().logEvent("settings_tap");

        Intent intent = new Intent(this, SettingsFragment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // highlight correct tab on bottom-navigation-view, by getting currently visible Fragment
        // from container-view-ID:
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.homeScreenPlacement);
        if (fragment != null && fragment.isVisible()) {
            // Temporarily remove the listener since, when we update selection, it will trigger
            // the listener which causes issues going to previous fragment as the listener
            // recognizes change & tries to show the fragment for current selection... & so, that
            // gets added to back-stack & we can never move back from it
            navigation.setOnNavigationItemSelectedListener(null);

            String state = null;

            if (fragment instanceof HomeFragment) {
                navigation.setCurrentItem(getResources().getInteger(R.integer.home));
                state = getString(R.string.tab_home);
            } else if (fragment instanceof ChannelsFragment) {
                navigation.setCurrentItem(getResources().getInteger(R.integer.channels));
                state = getString(R.string.tab_channels);
            } else if (fragment instanceof MovieTicketFragment
                    || fragment instanceof UnregisteredMovieTicketFragment) {
                navigation.setCurrentItem(getResources().getInteger(R.integer.sweeps));
                state = getString(R.string.tab_thank_you);
            }/* else if (fragment instanceof AccountFragment
                    || fragment instanceof UnregisteredAccountFragment) {
                navigation.setCurrentItem(getResources().getInteger(R.integer.account));
                state = getString(R.string.tab_account);
            }*/ else if (fragment instanceof SettingsFragment) {
                navigation.setCurrentItem(getResources().getInteger(R.integer.settings));
                state = getString(R.string.tab_settings);
            }

            trackLeanplumState(state);

            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        }
    }

    private void doFragmentTransition(@NonNull Fragment fragment, @NonNull String tag) {
        AppUtility.addFragmentToActivity(getSupportFragmentManager(), fragment,
                R.id.homeScreenPlacement, tag);
    }

    private void trackLeanplumState(String state) {
        if (!TextUtils.isEmpty(state)) {
            PerkLogger.d(LOG_TAG, "trackLeanplumState(): Tracking state: " + state);
            Leanplum.advanceTo(state);

            if (StringUtils.equalsIgnoreCase(state, getString(R.string.tab_home))) {
                // // FB tracking for home-screen: 'visit_home_screen'
                FacebookEventLogger.getInstance().logEvent("visit_home_screen");
            }
        }
    }
}
