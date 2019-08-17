package com.watchback2.android.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.perk.util.reference.PerkWeakReference;
import com.perk.webview.Constants;
import com.watchback2.android.controllers.PerkUserManager;

/**
 * Created by perk on 06/04/18.
 * Broadcast receiver to handle Perk Broadcast events
 */

public class PerkEventsBroadcastReceiver extends BroadcastReceiver {

    private PerkWeakReference<Activity> mActivityRef;

    public PerkEventsBroadcastReceiver(@NonNull Activity activity) {
        super();
        mActivityRef = new PerkWeakReference<>(activity);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("xyz", "onReceive " + intent);
        if (intent == null) {
            return;
        }

        // Make sure action is valid
        final String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }

        Activity activity = null;
        if (mActivityRef != null) {
            activity = mActivityRef.get();
        }

        if (Constants.ACTION_LOG_OUT.equals(action) && activity != null) {
            AppUtility.logOutUser(activity);

            /*AuthAPIRequestController.INSTANCE.resetAuthenticationSession(context);
            PerkPreferencesManager.INSTANCE.clearAllPreferences();

            Intent newIntent = new Intent(activity, LoginSignupPlayActivity.class);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(newIntent);
            // finish activity
            activity.finish();*/

            // Clear references from PerkWeakReference:
            mActivityRef.clear();
            mActivityRef = null;

        } else if ((Constants.ACTION_POINTS_UPDATED.equals(action)
                /*|| ReferralConstants.ACTION_POINTS_UPDATED.equals(action)*/) && activity != null) {

            // Refresh user info on points update:
            PerkUserManager.INSTANCE.getUserInfo(activity.getApplicationContext());
        }
    }

    public void registerReceiver() {
        Activity activity = mActivityRef != null ? mActivityRef.get() : null;

        if (activity != null) {
            final IntentFilter perkLogOutIntentFilter = new IntentFilter();
            perkLogOutIntentFilter.addAction(Constants.ACTION_LOG_OUT);
            perkLogOutIntentFilter.addAction(Constants.ACTION_POINTS_UPDATED);
            //perkLogOutIntentFilter.addAction(ReferralConstants.ACTION_POINTS_UPDATED);

            LocalBroadcastManager.getInstance(activity).registerReceiver(this,
                    perkLogOutIntentFilter);
        }
    }
}
