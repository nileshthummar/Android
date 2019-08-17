package com.watchback2.android.common;

import com.appsflyer.AppsFlyerLib;
import com.google.firebase.messaging.RemoteMessage;
import com.leanplum.LeanplumPushFirebaseMessagingService;
import com.perk.util.PerkLogger;

/**
 * Created by perk on 11/06/18.
 */
public class WatchBackFirebaseMessagingService extends LeanplumPushFirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        PerkLogger.d("### ", "Firebase message received: " + remoteMessage);
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        /* pass the token to the AppsFlyer SDK */
        AppsFlyerLib.getInstance().updateServerUninstallToken(getApplicationContext(), token);
    }
}
