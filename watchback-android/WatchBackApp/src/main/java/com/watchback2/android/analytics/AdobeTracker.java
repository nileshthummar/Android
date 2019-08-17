package com.watchback2.android.analytics;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.adobe.mobile.*;
import com.adobe.primetime.va.simple.MediaHeartbeat;
import com.appsflyer.AppsFlyerLib;
import com.comscore.PublisherConfiguration;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.api.PerkFileManager;
import com.watchback2.android.models.PerkUser;
import com.watchback2.android.utils.AppConstants;
import com.watchback2.android.utils.PerkUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdobeTracker {
    public static final AdobeTracker INSTANCE = new AdobeTracker();
    private static String TAG = "AdobeTracker";
    public  void setContext(Context context){
        try{
            Config.setContext(context);
        }catch (Exception e){}

    }
    public  void onResume(Activity activity){

        try{
            if(AppConstants.kWatchbackDebug) Log.d(TAG, "onResume");
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.did", PerkUtils.getAdvertisingId());

            contextData = appendCommonParam(contextData);

            Config.collectLifecycleData(activity, contextData);
        }catch (Exception e){}

    }
    public  void onPause(){
        try{

            if(AppConstants.kWatchbackDebug) Log.d(TAG, "onPause");
            Config.pauseCollectingLifecycleData();
        }catch (Exception e){}

    }

    public  void  trackState(String strName, HashMap<String, Object> contextData){
        try{
            contextData = appendCommonParam(contextData);
            Analytics.trackState(strName,contextData);
        }catch (Exception e){}

    }
    public  void  trackAction(String strName, HashMap<String, Object> contextData){
        try{
            contextData = appendCommonParam(contextData);
            Analytics.trackAction(strName,contextData);
        }catch (Exception e){}

    }

    private  HashMap<String, Object> appendCommonParam(HashMap<String, Object> contextData){
        try{
            if (contextData == null){
                contextData = new HashMap<>();
            }
            ///////
            contextData.put("tve.app", "NBCUniversal Watchback Rewards");
            contextData.put("tve.platform", "Android");
            contextData.put("tve.network", "NBCUniversal");
            SimpleDateFormat format = new SimpleDateFormat("hh:mm", Locale.US);
            String minute = format.format(new Date());
            contextData.put("tve.minute", minute);

            format = new SimpleDateFormat("hh:00", Locale.US);
            String hour = format.format(new Date());
            contextData.put("tve.hour", hour);
            format = new SimpleDateFormat("EEEE", Locale.US);
            String day = format.format(new Date());
            contextData.put("tve.day", day);
            format = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            String date = format.format(new Date());
            contextData.put("tve.date", date);
            ////////
        }catch (Exception e){}

        return contextData;
    }
    public   void trackAdobeLoadingEvents(){
        try{
            HashMap<String, Object> contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Loading");
            contextData.put("tve.userpath","Loading");
            contextData.put("tve.contenthub","Loading");
            trackState("Loading",contextData);
            //////
            contextData = new HashMap<String, Object>();
            contextData.put("tve.title","Loading");
            contextData.put("tve.userpath","Event:Log-In Check");
            contextData.put("tve.contenthub","Loading");

            if (PerkUtils.isUserLoggedIn()) {
                contextData.put("tve.userlogin","true");
                long userId = PerkFileManager.loadPerkUser().getUserId();
                contextData.put("tve.userid",String.valueOf(userId));
                contextData.put("tve.userstatus","Logged In");


                String birthday = "";
                String gender = "";

                try {
                    PerkUser perkUser = PerkFileManager.loadPerkUser();
                    if (perkUser != null){
                        birthday = perkUser.getBirthDate();
                        gender = perkUser.getGender();
                    }

                }catch (Exception e){}

                if (birthday != null && birthday.length() > 0 && !birthday.equalsIgnoreCase("null")) {
                    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
                    String inputDateStr = birthday;
                    Date date = null;
                    try {
                        date = inputFormat.parse(inputDateStr);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    DateFormat df = new SimpleDateFormat("yyyy", Locale.US);
                    contextData.put("tve.usercat1",df.format(date));
                }

                if (gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female")) {
                    contextData.put("tve.usercat2",gender);
                }

                boolean facebook_login =  PerkUtils.getSharedPreferences().getBoolean("facebook_login",false);
                if (facebook_login) {
                    contextData.put("tve.registrationtype","Facebook");
                }
                else{
                    contextData.put("tve.registrationtype","Email");
                }

            }
            else{
                contextData.put("tve.userlogin","false");
                contextData.put("tve.userid","unkown");
                contextData.put("tve.userstatus","Not Logged In");
                contextData.put("tve.usercat1","unkown");
                contextData.put("tve.usercat2","unkown");
            }
            trackAction("Event:Log-In Check",contextData);
        }catch (Exception e){}




    }

    public static HashMap<String,Object> appendVideoData(HashMap<String, Object> contextData, BrightcovePlaylistData.BrightcoveVideo videoData) {
        try{
            if (videoData.isLongform()) {
                contextData.put("tve.contenttype","VOD Episode");
            }
            else{
                contextData.put("tve.contenttype","VOD Clip");
            }
            contextData.put("tve.episodetitle",videoData.getName());
            contextData.put("tve.contentprovider",videoData.getCustomFields().getProvider());
            contextData.put("tve.program",videoData.getCustomFields().getShow());

        }catch (Exception e){}
        return contextData;
    }

    public  static void initAdobeSDK(Context context){
        initHeartbeat(context);
        initComScore(context);
    }
    private static void initHeartbeat(Context context){

        try{
            Map <String, Object> appInfo = new HashMap<String, Object>();
            appInfo.put("appid", "PA1C9EB66-D4BB-4781-9AE0-2592BF256862");
            appInfo.put("appname", "NBCUniversal Watchback - Android");
            appInfo.put("appversion", PerkUtils.getAppVersionNumber());
            //appInfo.put("no1_devDebug", "DEBUG"); //for testing
            appInfo.put("sfcode", "dcr");

            MediaHeartbeat.nielsenConfigure(context, appInfo);
            /////
        }catch (Exception e){}

    }
    private static void initComScore(Context context){
        try{
            PublisherConfiguration myPublisherConfig = new PublisherConfiguration.Builder()
                    .publisherId("6035083")
                    .publisherSecret("5f94da45f8b0635bdfd6c1ccd9df1227")
                    .applicationName("NBCUniversal Watchback Rewards")
                    .build();
            com.comscore.Analytics.getConfiguration().addClient(myPublisherConfig);
            com.comscore.Analytics.start(context);
            if(AppConstants.kWatchbackDebug) Log.d("comscore","stremingAnalytics initComScore");
        }catch (Exception e){}
    }

    public  void  trackAppsFlyerEvent(Context context, String strName, HashMap<String, Object> contextData){
        try{
            AppsFlyerLib.getInstance().trackEvent(context,strName,contextData);
        }catch (Exception e){}

    }
}
