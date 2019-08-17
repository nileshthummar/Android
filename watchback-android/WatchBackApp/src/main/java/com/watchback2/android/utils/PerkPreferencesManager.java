package com.watchback2.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.LruCache;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.perk.util.PerkLogger;
import com.watchback2.android.BuildConfig;
import com.watchback2.android.controllers.WatchBackSettingsController;
import com.watchback2.android.models.Channel;
import com.watchback2.android.models.Interest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by perk on 25/3/18.
 * Preference manager for Putting shared preferences.
 */

public final class PerkPreferencesManager {

    private static final String LOG_TAG = "PerkPreferencesManager";

    public static final PerkPreferencesManager INSTANCE = new PerkPreferencesManager();

    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000;

    private static final int MAX_CACHE_SIZE = 100;

    private SharedPreferences sharedPreferences;

    private SharedPreferences.Editor mEditor;

    private WatchBackObservableBoolean mNightMode = new WatchBackObservableBoolean(true);

    //------- Preferences ----------

    private static final String PERK_USER_UUID = "perk_user_uuid";

    private static final String USER_INTERESTS = "user_interests";

    private static final String USER_CHANNELS = "user_channels";

    private static final String APP_THEME_MODE = "app_theme_mode";

    private static final String DISALLOW_BELOW_13 = "disallow_below_13";

    private static final String ALLOW_TERMS_GATE_SKIP = "allow_skip";

    private static final String ALLOW_TNC_SKIP = "allow_tnc_skip";

    private static final String EMAILS_OPTED = "emails_opted_in";

    private static final String PRECHECK_EMAILS_OPTED = "emails_opted_precheck";

    private static final String LAST_LONG_FORM_VIDEO_ID = "last_long_form_video_id";

    private static final String LAST_LONG_FORM_VIDEO_POSITION = "last_long_form_video_position";

    private static final String LONG_FORM_VIDEO_PLAYHEAD_POSITIONS =
            "long_form_video_playhead_positions";

    private static final String WATCHED_VIDEOS_SET = "watched_videos_set";

    private static final String WATCHED_VIDEOS_SET_STORAGE_DATE = "watched_videos_set_storage_date";

    private static final String WATCHED_LONG_FORM_VIDEOS_COUNT = "watched_long_form_videos_count";

    //private static final String PERK_APP_RATED = "app_rated";

    private final LruCache<String, Long> mLongFormVideoPlayHeadPositionCache = new LruCache<>(
            MAX_CACHE_SIZE);

    private final Set<String> mWatchedVideoIds = new HashSet<>();

    private final Map<String, Integer> mWatchedLongFormVideoCount = new ArrayMap<>();

    private PerkPreferencesManager() {
        // Private constructor to prevent initialization
    }

    public void initSharedPreferences(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = sharedPreferences.edit();
        mEditor.apply();

        // Set nightMode:
        getNightMode().set(sharedPreferences.getBoolean(APP_THEME_MODE, true));

        // read saved play-head positions into the LruCache:
        readPlayheadPositionsInCache();

        // read saved watched-videos list:
        updateWatchedVideosFromPreferences();
    }

    @NonNull
    private SharedPreferences.Editor getEditor() {
        if (mEditor == null) {
            mEditor = sharedPreferences.edit();
            mEditor.apply();
        }

        return mEditor;
    }

    public String getPerkUserUUIDFromPreferences() {
        return sharedPreferences != null ? sharedPreferences.getString(PERK_USER_UUID, "") : "";
    }

    public void putPerkUserUUIDIntoPreference(String perkUserUUID) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(PERK_USER_UUID, perkUserUUID);
        editor.apply();
    }

    /**
     * Returns List<Interest> objects from user interests JSON stored in preferences
     * NOTE: This returns all the interests, the caller needs to filter if it wants just selected
     * Interests
     * @return
     */
    public List<Interest> getUserInterestsListFromPreferences() {
        if (sharedPreferences == null) {
            return null;
        }

        Gson gson = new Gson();

        String interestsJSON = sharedPreferences.getString(USER_INTERESTS, null);
        if (TextUtils.isEmpty(interestsJSON)) {
            return null;
        }

        return gson.fromJson(interestsJSON, new TypeToken<ArrayList<Interest>>(){}.getType());
    }

    /**
     * Returns List<Channel> objects from user channel JSON stored in preferences
     * @return
     */
    public List<Channel> getUserChannelListFromPreferences() {
        if (sharedPreferences == null) {
            return null;
        }

        Gson gson = new Gson();

        String channelsJSON = sharedPreferences.getString(USER_CHANNELS, null);
        if (TextUtils.isEmpty(channelsJSON)) {
            return null;
        }

        return gson.fromJson(channelsJSON, new TypeToken<ArrayList<Channel>>(){}.getType());
    }

    /**
     * Saves specified interests List to preferences after converting to JSON
     * @param interests
     */
    public void saveUserInterestsIntoPreference(@NonNull List<Interest> interests) {
        SharedPreferences.Editor editor = getEditor();

        Gson gson = new Gson();
        String interestsJSON = gson.toJson(interests);

        editor.putString(USER_INTERESTS, interestsJSON);

        // use commit explicitly since we want this to be done immediately (rather than
        // asynchronously), before moving to other screens
        editor.commit();
    }

    /**
     * Saves specified channels List to preferences after converting to JSON
     * @param channels
     */
    public void saveUserChannelsIntoPreference(List<Channel> channels) {
        if (channels == null) {
            return;
        }
        Gson gson = new Gson();
        String channelsJSON = gson.toJson(channels);

        SharedPreferences.Editor editor = getEditor();
        editor.putString(USER_CHANNELS, channelsJSON);
        editor.apply();
    }

    public void clearUserChannelsInPreference() {
        SharedPreferences.Editor editor = getEditor();
        editor.remove(USER_CHANNELS);
        editor.apply();
    }

    public boolean areUserInterestsSavedToPreferences() {
        return sharedPreferences != null && sharedPreferences.contains(USER_INTERESTS);
    }

    public boolean areUserChannelsSavedToPreferences() {
        return sharedPreferences != null && sharedPreferences.contains(USER_CHANNELS);
    }

    public void saveAppThemeIntoPreference(boolean nightMode) {
        saveAppThemeIntoPreference(nightMode, false);
    }

    private void saveAppThemeIntoPreference(boolean nightMode, boolean force) {
        SharedPreferences.Editor editor = getEditor();

        if (!force && getNightMode().get() == nightMode) {
            return;
        }

        editor.putBoolean(APP_THEME_MODE, nightMode);
        editor.apply();

        if (force) {
            getNightMode().setWithoutNotify(nightMode);
        } else {
            getNightMode().set(nightMode);
        }
    }

    public boolean isNightMode() {
        return getNightMode().get();
    }

    public WatchBackObservableBoolean getNightMode() {
        return mNightMode;
    }

    /*public void putAppRatedIntoPreference() {
        if (editor == null) {
            return;
        }
        editor.putBoolean(PERK_APP_RATED, true);
        editor.apply();
    }

    public boolean isAppRatedFromPreferences() {
        return sharedPreferences != null && sharedPreferences.getBoolean(PERK_APP_RATED, false);
    }*/

    public boolean is24HourLockEnforced() {
        if (sharedPreferences == null) {
            return false;
        }

        long lastRegistrationAttemptMillis = sharedPreferences.getLong(DISALLOW_BELOW_13, -1L);

        PerkLogger.d(LOG_TAG, "is24HourLockEnforced(): Got millis from preferences: "
                + lastRegistrationAttemptMillis);

        if (lastRegistrationAttemptMillis == -1L) {
            return false;
        }

        // Check if 24 hours have elapsed since last registration attempt for user aged below 13:
        long elapsedTime = new Date(System.currentTimeMillis()).getTime()
                - lastRegistrationAttemptMillis;

        PerkLogger.d(LOG_TAG, "is24HourLockEnforced(): elapsedTime: " + elapsedTime + " 1 day: "
                + ONE_DAY_MILLIS);

        // clear the saved value if more than 1 day has elapsed since last registration attempt:
        if (elapsedTime >= ONE_DAY_MILLIS) {
            SharedPreferences.Editor editor = getEditor();
            editor.putLong(DISALLOW_BELOW_13, -1L);
            editor.apply();

            return false;
        }

        return true;
    }

    public void putInvalidDOBRegistrationAttemptInPreference() {
        SharedPreferences.Editor editor = getEditor();

        // Get current date/time & save it as the last registration attempt time:
        editor.putLong(DISALLOW_BELOW_13, new Date(System.currentTimeMillis()).getTime());
        editor.apply();
    }

    public boolean getUserAllowedToSkipTermsGateFromPreferences() {
        return sharedPreferences != null && sharedPreferences.getBoolean(ALLOW_TERMS_GATE_SKIP, false);
    }

    public void putUserAllowedToSkipTermsGateIntoPreference(boolean allowed) {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(ALLOW_TERMS_GATE_SKIP, allowed);
        editor.apply();
    }

    public boolean getUserAllowedToSkipTncFromPreferences() {
        return sharedPreferences != null && sharedPreferences.getBoolean(ALLOW_TNC_SKIP, false);
    }

    public void putUserAllowedToSkipTncIntoPreference(boolean allowed) {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(ALLOW_TNC_SKIP, allowed);
        editor.apply();
    }

    public boolean hasOptedInForEmails() {
        return sharedPreferences != null && sharedPreferences.getBoolean(EMAILS_OPTED, false);
    }

    public void putOptedInForEmailsIntoPreference(boolean allowed) {
        PerkLogger.d(LOG_TAG, "putOptedInForEmailsIntoPreference: allowed = " + allowed);

        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(EMAILS_OPTED, allowed);
        editor.apply();
    }

    /*public boolean shouldPrecheckEmailOpt() {
        return sharedPreferences != null && sharedPreferences.getBoolean(PRECHECK_EMAILS_OPTED, false);
    }

    public void putPrecheckEmailOptIntoPreference(boolean allowed) {
        PerkLogger.d(LOG_TAG, "putPrecheckEmailOptIntoPreference: allowed = " + allowed);

        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(PRECHECK_EMAILS_OPTED, allowed);
        editor.apply();
    }*/

    private void clearLastLongformVideoIdAndPosition() {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(LAST_LONG_FORM_VIDEO_ID, "");
        editor.putLong(LAST_LONG_FORM_VIDEO_POSITION, -1);
        editor.apply();
    }

    private String getLastLongformVideoId() {
        return sharedPreferences != null ? sharedPreferences.getString(LAST_LONG_FORM_VIDEO_ID,
                null) : null;
    }

    private long getLastLongformVideoPosition() {
        return sharedPreferences != null ? sharedPreferences.getLong(LAST_LONG_FORM_VIDEO_POSITION,
                -1) : -1;
    }

    public void saveLongformVideoPosition(String videoId, long position) {
        PerkLogger.d(LOG_TAG,
                "saveLongformVideoPosition: videoId = " + videoId + " position = " + position);

        if (TextUtils.isEmpty(videoId)) {
            PerkLogger.w(LOG_TAG,"saveLongformVideoPosition: Ignoring empty video-id!");
            return;
        }

        if (position >= 0 && position <= 3000) {
            // Ignore saving positions for less than 3 seconds
            PerkLogger.d(LOG_TAG,
                    "saveLongformVideoPosition: Ignoring saving position for <=3 seconds");
            return;
        }

        if (position == -1) {
            // this means we need to clear this value, so we remove it from cache:
            try {
                long lastValue = mLongFormVideoPlayHeadPositionCache.remove(videoId);

                PerkLogger.d(LOG_TAG,
                        "saveLongformVideoPosition: Cleared cached value for video-id: " + videoId
                                + " -Last saved value: " + lastValue);

                printPositionCache();
                return;

            } catch (Exception ex) {
                // Sometimes, this throws an exception when there is no value stored for this
                // video. So we don't return from here, & instead manually save -1 for it in that
                // case, to be on safe side.
                PerkLogger.e(LOG_TAG,
                        "saveLongformVideoPosition: Exception clearing cached value for video-id: "
                                + videoId + " -Manually saving it as -1", ex);
            }
        }

        mLongFormVideoPlayHeadPositionCache.put(videoId, position);
        printPositionCache();
    }

    public long getLastPositionForLongformVideo(String videoId) {
        if (TextUtils.isEmpty(videoId)) {
            return -1;
        }

        Long position = mLongFormVideoPlayHeadPositionCache.get(videoId);

        PerkLogger.d(LOG_TAG, "getLastPositionForLongformVideo: videoId = " + videoId);
        printPositionCache();

        return position != null ? position : -1;
    }

    public void persistPlayheadPositionsCache() {
        int maxSaveSetting = WatchBackSettingsController.INSTANCE.getSavePlayheadCountSetting(
                PerkUtils.getAppContext());

        PerkLogger.d(LOG_TAG,
                "persistPlayheadPositionsCache: maxSaveSetting=" + maxSaveSetting + " Lru Cache size: "
                        + mLongFormVideoPlayHeadPositionCache.size());

        mLongFormVideoPlayHeadPositionCache.trimToSize(maxSaveSetting);
        printPositionCache();

        Gson gson = new Gson();
        String cacheString = gson.toJson(mLongFormVideoPlayHeadPositionCache.snapshot());

        PerkLogger.d(LOG_TAG, "persistPlayheadPositionsCache: saving value = " + cacheString);

        SharedPreferences.Editor editor = getEditor();
        editor.putString(LONG_FORM_VIDEO_PLAYHEAD_POSITIONS, cacheString);
        editor.apply();
    }

    public void persistWatchedVideosCache() {
        printWatchedVideosCache();

        Gson gson = new Gson();
        String cacheString = gson.toJson(mWatchedVideoIds.toArray(new String[0]));

        PerkLogger.d(LOG_TAG,
                "persistWatchedVideosCache: saving Watched-videos cache; value = " + cacheString);

        String watchCountString = gson.toJson(mWatchedLongFormVideoCount);

        PerkLogger.d(LOG_TAG,
                "persistWatchedVideosCache: saving watched-long-form videos' count values = "
                        + watchCountString);

        SharedPreferences.Editor editor = getEditor();
        editor.putString(WATCHED_VIDEOS_SET, cacheString);
        editor.putString(WATCHED_LONG_FORM_VIDEOS_COUNT, watchCountString);
        editor.apply();
    }

    private void checkLastStorageDate() {
        final Calendar today = getDateForToday();

        final Calendar lastSaveDate;

        long lastSaveDateMillis = (sharedPreferences != null) ? sharedPreferences.getLong(
                WATCHED_VIDEOS_SET_STORAGE_DATE, -1L) : -1L;

        PerkLogger.d(LOG_TAG,
                "checkLastStorageDate(): Got millis from preferences: " + lastSaveDateMillis);

        if (lastSaveDateMillis == -1L) {

            // Save today's date if it is missing from preferences:
            SharedPreferences.Editor editor = getEditor();
            editor.putLong(WATCHED_VIDEOS_SET_STORAGE_DATE, today.getTimeInMillis());
            editor.apply();

            lastSaveDate = today;
        } else {
            lastSaveDate = Calendar.getInstance();
            lastSaveDate.setTimeInMillis(lastSaveDateMillis);
            DateTimeUtil.setTimePartToZero(lastSaveDate);
        }

        // Check if date has been changed since last save:
        boolean dateChanged = today.after(lastSaveDate);

        PerkLogger.d(LOG_TAG, "checkLastStorageDate(): dateChanged: " + dateChanged);

        // clear the saved values if more than 1 day has elapsed:
        if (dateChanged) {
            SharedPreferences.Editor editor = getEditor();
            editor.putString(WATCHED_VIDEOS_SET, null);
            editor.putString(WATCHED_LONG_FORM_VIDEOS_COUNT, null);
            editor.putLong(WATCHED_VIDEOS_SET_STORAGE_DATE, today.getTimeInMillis());
            editor.apply();
        }
    }

    private Calendar getDateForToday() {
        final Calendar calendar = Calendar.getInstance();
        DateTimeUtil.setTimePartToZero(calendar);
        return calendar;
    }

    private void updateWatchedVideosFromPreferences() {
        mWatchedVideoIds.clear();
        mWatchedLongFormVideoCount.clear();

        checkLastStorageDate();

        String cacheString = sharedPreferences.getString(WATCHED_VIDEOS_SET, null);

        String watchCountString = sharedPreferences.getString(WATCHED_LONG_FORM_VIDEOS_COUNT, null);

        PerkLogger.d(LOG_TAG,
                "updateWatchedVideosFromPreferences: Got saved value for watched-videos: "
                        + cacheString + "; and saved watch-count values for long-form videos: "
                        + watchCountString);

        updateWatchedVideoCountFrom(watchCountString);

        if (TextUtils.isEmpty(cacheString)) {
            PerkLogger.d(LOG_TAG, "updateWatchedVideosFromPreferences: No saved values!");
            printWatchedVideosCache();
            return;
        }

        Gson gson = new Gson();

        try {
            String[] cacheArray = gson.fromJson(cacheString, new TypeToken<String[]>() {
            }.getType());

            if (cacheArray == null || cacheArray.length == 0) {
                PerkLogger.d(LOG_TAG, "updateWatchedVideosFromPreferences: Empty array obtained!");
                printWatchedVideosCache();
                return;
            }

            for (String videoId : cacheArray) {
                onVideoWatched(videoId,
                        false /* here, we always pass false since we separately read & create the
                         mWatchedLongFormVideoCount via updateWatchedVideoCountFrom() method above */);
            }

            PerkLogger.d(LOG_TAG,
                    "updateWatchedVideosFromPreferences: Cache created from saved values!");
        } catch (Exception ex) {
            PerkLogger.e(LOG_TAG, "Exception getting array from saved watched-videos:", ex);
        }

        printWatchedVideosCache();
    }

    private void updateWatchedVideoCountFrom(String watchCountString) {
        if (TextUtils.isEmpty(watchCountString)) {
            PerkLogger.d(LOG_TAG, "updateWatchedVideoCountFrom: No saved values!");
            return;
        }

        Gson gson = new Gson();

        try {
            ArrayMap<String, Integer> cacheMap = gson.fromJson(watchCountString,
                    new TypeToken<ArrayMap<String, Integer>>() {
                    }.getType());

            if (cacheMap == null || cacheMap.isEmpty()) {
                PerkLogger.d(LOG_TAG, "updateWatchedVideoCountFrom: Empty map obtained!");
                return;
            }

            Set<String> keySet = cacheMap.keySet();
            for (String key : keySet) {
                Integer count = cacheMap.get(key);
                if (count != null) {
                    mWatchedLongFormVideoCount.put(key, count);
                }
            }
        } catch (Exception ex) {
            PerkLogger.e(LOG_TAG, "Exception getting Map from saved long-form video count:", ex);
        }
    }

    public void clearPlayheadPositions() {
        // Used to clear the saved playhead-positions & the cache -after a change in the user's
        // login state:

        mLongFormVideoPlayHeadPositionCache.evictAll();
        persistPlayheadPositionsCache();

        mWatchedVideoIds.clear();
        mWatchedLongFormVideoCount.clear();
        persistWatchedVideosCache();
    }

    public void onVideoWatched(@NonNull String videoId, boolean isLongFormVideo) {
        PerkLogger.d(LOG_TAG,
                "Saving that user watched video with ID: " + videoId + " isLongFormVideo: "
                        + isLongFormVideo);
        mWatchedVideoIds.add(videoId);

        if (isLongFormVideo) {
            // Store/increment the watched-count for long-form videos
            Integer count = mWatchedLongFormVideoCount.get(videoId);

            if (count == null) {
                count = 0;
            }

            count++;

            PerkLogger.d(LOG_TAG,
                    "onVideoWatched: User has watched video-id " + videoId + " for " + count
                            + " times");

            mWatchedLongFormVideoCount.put(videoId, count);
        }
    }

    public boolean isVideoWatched(@NonNull String videoId) {
        return mWatchedVideoIds.contains(videoId);
    }

    public int getWatchedCountForVideo(@NonNull String videoId) {
        Integer count = mWatchedLongFormVideoCount.get(videoId);

        PerkLogger.d(LOG_TAG, "getWatchedCountForVideo: " + videoId + "; count = " + count);

        return count == null ? -1 : count;
    }

    private void readPlayheadPositionsInCache() {
        mLongFormVideoPlayHeadPositionCache.evictAll();

        // for backwards compatibility to when we saved just 1 long-form video value, we manually
        // check if there is any existing value saved. If yes, then read & store it in our cache
        // & clear the old setting from preferences so that the new mechanism is used next time
        // onwards...
        String lastSavedVideoId = getLastLongformVideoId();

        PerkLogger.d(LOG_TAG,
                "readPlayheadPositionsInCache: Old saved values: id=" + lastSavedVideoId);

        if (!TextUtils.isEmpty(lastSavedVideoId)) {
            long lastSavedVideoPos = getLastLongformVideoPosition();

            PerkLogger.d(LOG_TAG,
                    "readPlayheadPositionsInCache: Old saved values: id=" + lastSavedVideoId
                            + " pos=" + lastSavedVideoPos);

            if (lastSavedVideoPos != -1) {
                // save values to our cache:
                mLongFormVideoPlayHeadPositionCache.put(lastSavedVideoId, lastSavedVideoPos);
            }

            // clear values from preferences:
            clearLastLongformVideoIdAndPosition();
        }

        String cacheString = sharedPreferences.getString(LONG_FORM_VIDEO_PLAYHEAD_POSITIONS, null);

        PerkLogger.d(LOG_TAG, "readPlayheadPositionsInCache: Got saved value: " + cacheString);

        if (TextUtils.isEmpty(cacheString)) {
            PerkLogger.d(LOG_TAG, "readPlayheadPositionsInCache: No saved values!");
            return;
        }

        Gson gson = new Gson();

        try {
            LinkedHashMap<String, Long> cacheMap = gson.fromJson(cacheString,
                    new TypeToken<LinkedHashMap<String, Long>>() {
                    }.getType());

            if (cacheMap == null || cacheMap.isEmpty()) {
                PerkLogger.d(LOG_TAG, "readPlayheadPositionsInCache: Empty map obtained!");
                return;
            }

            Set<String> keySet = cacheMap.keySet();
            for (String key : keySet) {
                mLongFormVideoPlayHeadPositionCache.put(key, cacheMap.get(key));
            }
        } catch (Exception ex) {
            PerkLogger.e(LOG_TAG, "Exception getting Map from saved playhead-positions:", ex);
        }

        PerkLogger.d(LOG_TAG, "readPlayheadPositionsInCache: Cache created from saved values!");

        printPositionCache();
    }

    private void printPositionCache() {
        // Ignore for PROD builds, as we use reflection internally to print the values, which
        // will slow down the process due to too many calls being made to this
        if (!BuildConfig.DEBUG) {
            return;
        }

        PerkLogger.d(LOG_TAG, "printPositionCache: Current cache values:\n"
                + AppUtility.safeReflectionToString(mLongFormVideoPlayHeadPositionCache));
    }

    private void printWatchedVideosCache() {
        PerkLogger.d(LOG_TAG, "printWatchedVideosCache: Current cache values:\n"
                + mWatchedVideoIds.toString());

        PerkLogger.d(LOG_TAG,
                "printWatchedVideosCache: Current count-values for watched long-form-videos:\n"
                        + mWatchedLongFormVideoCount.toString());
    }

    /* default */ void clearAllPreferences() {
        SharedPreferences.Editor editor = getEditor();

        editor.clear();

        // reset theme on log-out/clear
        saveAppThemeIntoPreference(true, true);

        // Explicitly used commit here instead of apply since we want the preferences to be
        // cleared immediately on log-out
        editor.commit();
    }
}
