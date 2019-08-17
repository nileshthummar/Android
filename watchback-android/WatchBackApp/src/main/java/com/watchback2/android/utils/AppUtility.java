package com.watchback2.android.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.brightcove.player.event.Event;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.request.auth.AuthAPIRequestController;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.activities.BrandDetailsActivity;
import com.watchback2.android.activities.LoginActivity;
import com.watchback2.android.activities.MainActivity;
import com.watchback2.android.activities.SignupActivity;
import com.watchback2.android.activities.VideoDetailsActivity;
import com.watchback2.android.activities.VideoPlayerActivity;
import com.watchback2.android.adapters.HomeFragmentListAdapter;
import com.watchback2.android.api.BrightcoveAPIController;
import com.watchback2.android.api.BrightcovePlaylistData;
import com.watchback2.android.databinding.LayoutDialogLoginBinding;
import com.watchback2.android.fragments.NoInternetDialogFragment;
import com.watchback2.android.helper.Bindings;
import com.watchback2.android.helper.UserInfoValidator;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by perk on 22/03/18.
 * Utility class for functionality to be used throughout the app
 */

public final class AppUtility {

    private static final int MAX_VALUE = 999999;

    private static final String LOG_TAG = "AppUtility";

    private static final Pattern PERK_PATTERN = Pattern.compile("(?i)Perk");

    private static final String LOYALTY = "Thank You";

    public static final String REDEMPTION_PARTNER_TAG = "redemption_partner";
    public static final String REDEMPTION_PARTNER_TAG2 = "redemption_partner_";

    public static final String LEANPLUM_INTERESTS_ATTRIBUTE = "Selected Interests";

    public static final String LEANPLUM_CHANNELS_ATTRIBUTE = "Selected Channels";

    private static final String LEANPLUM_PERK_UUID_ATTRIBUTE = "user_uuid";

    private AppUtility() {
        // Private constructor to prevent instantiation
    }

    /**
     * Checks if network connectivity is available & shows a dialog message if unavailable
     *
     * @param context Context from where this is called
     * @return true if network is not available
     */
    public static boolean isNetworkAvailable(@NonNull Context context) {
        return isNetworkAvailable(context, true);
    }

    /**
     * Same as isNetworkAvailable(context) but prevents showing dialog if specified
     */
    public static boolean isNetworkAvailable(@NonNull Context context, boolean showDialog) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            return true;
        } else if (showDialog) {
            /*Toast.makeText(context, "You don't have an active data connection.",
                    Toast.LENGTH_SHORT).show();*/

            // Show dialog:
            Activity activity = PerkUtils.getTopActivity();
            if (!(activity instanceof AppCompatActivity)) {
                PerkLogger.w(LOG_TAG,
                        "isNetworkAvailable(): No activity available to show dialog!");
                return false;
            }

            FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();

            Fragment dialog = fm.findFragmentByTag("noInternetDialog");
            PerkLogger.d(LOG_TAG,
                    "isNetworkAvailable(): findFragmentByTag(\"noInternetDialog\") returned: "
                            + dialog);

            if (dialog == null || !dialog.isVisible()) {
                if (dialog != null) {
                    ((DialogFragment) dialog).dismissAllowingStateLoss();
                }

                NoInternetDialogFragment noInternetDialogFragment = new NoInternetDialogFragment();
                try {
                    noInternetDialogFragment.show(fm, "noInternetDialog");
                } catch (Exception ex) {
                    // Ignore any failures, the dialog will be shown again when a
                    // network-activity is attempted
                }
            } else {
                PerkLogger.d(LOG_TAG,
                        "isNetworkAvailable(): Not showing dialog as it is already shown");
            }
        }
        return false;
    }

    /**
     * The {@code fragment} is added to the container view with id {@code frameId}. The operation is
     * performed by the {@code fragmentManager}.
     */
    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment, int frameId, @NonNull String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment, tag);
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }

    public static void addFragmentToActivityWithoutBackStack(@NonNull FragmentManager fragmentManager,
                                                             @NonNull Fragment fragment, int frameId, @NonNull String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment, tag);
        transaction.commitAllowingStateLoss();
    }

    /**
     * Will log out the user & send ACTION_LOG_OUT broadcast
     *
     * @param context Context from where user is logging out
     */
    public static void logOutUser(@NonNull Context context) {
        // TODO: Enable this back later. Currently MainActivity displays session-expired dialog on getting this.
        //LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.ACTION_LOG_OUT));

        context.sendBroadcast(new Intent(AppConstants.ACTION_FINISH));

        PerkUtils.logoutUser();
    }

    /**
     * Formats the provided points value, for displaying on UI
     *
     * @return
     */
    public static String getFormattedPoints(long value) {
        try {
            if (value > MAX_VALUE) {
                long finalPoints = value / 1000;
                return (finalPoints + "k");
            } else {
                DecimalFormat formatter = new DecimalFormat("#,###,###");
                return formatter.format(value);
            }
        } catch (ArithmeticException e) {
            PerkLogger.e(LOG_TAG, "Exception with getFormattedPoints: ", e);
        }
        return "";
    }

    public static void dismissDialog(DialogInterface dialog) {
        if (dialog != null) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                // Ignore... dialog is probably already dismissed OR containing activity isn't
                // present any more
            }
        }
    }

    public static String changePerkToLoyalty(String string) {
        if (TextUtils.isEmpty(string)) {
            return null;
        }
        return PERK_PATTERN.matcher(string).replaceAll(LOYALTY);
    }

    public static String getEncodedStringParams(String params) {
        try {
            // Remove leading/trailing spaces -if any...
            params = params.trim();
            // and encode it
            params = URLEncoder.encode(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            PerkLogger.e(LOG_TAG, "Error trying to encode supplied params: " + params, e);
        }

        return params;
    }

    /**
     * Removes the video-elements from provided list that have the custom-field 'longform' set
     */
    public static List<BrightcovePlaylistData.BrightcoveVideo> removeLongformVideos(
            @Nullable List<BrightcovePlaylistData.BrightcoveVideo> videoList) {

        final List<BrightcovePlaylistData.BrightcoveVideo> updatedVideoList = new ArrayList<>();

        PerkLogger.d(LOG_TAG,
                "removeLongformVideos: Original video-list size: " + (videoList != null
                        ? videoList.size() : "0"));

        if (videoList != null && !videoList.isEmpty()) {
            for (BrightcovePlaylistData.BrightcoveVideo video : videoList) {
                if (video == null) {
                    continue;
                }

                BrightcovePlaylistData.CustomFields customFields = video.getCustomFields();

                if (customFields != null && !TextUtils.isEmpty(customFields.getLongform())
                        && Boolean.parseBoolean(customFields.getLongform())) {
                    PerkLogger.d(LOG_TAG,
                            "Ignoring adding longform-video to final list: " + video.toString());
                    continue;
                }

                updatedVideoList.add(video);
            }
        }

        PerkLogger.d(LOG_TAG,
                "removeLongformVideos: updatedVideoList size after removing longform videos: "
                        + updatedVideoList.size());

        return updatedVideoList;
    }

    /**
     * Removes the video-elements from provided list that have the tag 'redemption_partner' set
     */
    public static List<BrightcovePlaylistData.BrightcoveVideo> removeRedemptionPartnerVideos(
            @Nullable List<BrightcovePlaylistData.BrightcoveVideo> videoList) {

        final List<BrightcovePlaylistData.BrightcoveVideo> updatedVideoList = new ArrayList<>();

        PerkLogger.d(LOG_TAG,
                "removeRedemptionPartnerVideos: Original video-list size: " + (videoList != null
                        ? videoList.size() : "0"));

        if (videoList != null && !videoList.isEmpty()) {
            for (BrightcovePlaylistData.BrightcoveVideo video : videoList) {
                if (video == null) {
                    continue;
                }

                if (isRedemptionPartnerTaggedVideo(video)) {
                    PerkLogger.d(LOG_TAG,
                            "Ignoring adding 'redemption_partner' video to final list: "
                                    + video.toString());
                    continue;
                }

                updatedVideoList.add(video);
            }
        }

        PerkLogger.d(LOG_TAG,
                "removeRedemptionPartnerVideos: updatedVideoList size after removing longform "
                        + "videos: "
                        + updatedVideoList.size());

        return updatedVideoList;
    }

    public static boolean isRedemptionPartnerTaggedVideo(@NonNull BrightcovePlaylistData.BrightcoveVideo video) {
        List<String> tags = video.getTags();
        return tags != null && (tags.contains(REDEMPTION_PARTNER_TAG) || tags.contains(REDEMPTION_PARTNER_TAG2));
    }

    /**
     * Sorts the provided videos list by most-recently added & returns the sorted-list
     */
    public static List<BrightcovePlaylistData.BrightcoveVideo> sortVideosByUpdated(
            @NonNull List<BrightcovePlaylistData.BrightcoveVideo> videos) {
        List<BrightcovePlaylistData.BrightcoveVideo> sortedList = new ArrayList<>(videos);

        // Sort mode: MOST RECENT
        Collections.sort(sortedList, new Comparator<BrightcovePlaylistData.BrightcoveVideo>() {
            @Override
            public int compare(BrightcovePlaylistData.BrightcoveVideo video1,
                               BrightcovePlaylistData.BrightcoveVideo video2) {

                Date video1Date = DateTimeUtil.getDateFromRawDate(video1.getUpdatedAt());
                Date video2Date = DateTimeUtil.getDateFromRawDate(video2.getUpdatedAt());

                if (video1Date == null) {
                    PerkLogger.e(LOG_TAG, "video1 Date is null!");
                    return -1;
                } else if (video2Date == null) {
                    PerkLogger.e(LOG_TAG, "video2 Date is null!");
                    return 1;
                }

                return video2Date.compareTo(video1Date);
            }
        });

        return sortedList;
    }

    public static void showGenericDialog(@NonNull Context context, @NonNull String message,
                                         @NonNull DialogInterface.OnClickListener buttonListener) {
        showGenericDialog(context, message, context.getResources().getString(R.string.close),
                buttonListener);
    }

    public static void showGenericDialog(@NonNull Context context, @NonNull String message,
                                         @NonNull String buttonText, @NonNull DialogInterface.OnClickListener buttonListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(buttonText, buttonListener);
        try {
            builder.show();
        } catch (Exception e) {
            // Ignore
            // There can be an exception here if we try to show dialog using a
            // previously-finished Activity as a context. This can happen if
            // the user navigates back from the Activity before the operation is completed
            PerkLogger.e(LOG_TAG, "Exception showing dialog", e);
        }
    }

    public static void showHtmlTextDialog(@NonNull Context context, @NonNull String message,
                                          @NonNull DialogInterface.OnClickListener buttonListener, boolean isForFbLogin) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setCancelable(false);

        if (isForFbLogin) {
            builder.setPositiveButton("Agree", buttonListener);
            builder.setNegativeButton("No", (dialog, which) -> AppUtility.dismissDialog(dialog));
        } else {
            builder.setPositiveButton("Okay", (dialog, which) -> AppUtility.dismissDialog(dialog));
        }

        View layout = LayoutInflater.from(context).inflate(R.layout.dialog_message, null);
        final AppCompatTextView textView = layout.findViewById(R.id.id_msg);
        Bindings.setText(textView, message);
        builder.setView(layout);
        try {
            builder.show();
        } catch (Exception e) {
            // Ignore
            // There can be an exception here if we try to show dialog using a
            // previously-finished Activity as a context. This can happen if
            // the user navigates back from the Activity before the operation is completed
            PerkLogger.e(LOG_TAG, "Exception showing dialog", e);
        }
    }

    public static long getLongEventProperty(Event event, String property) {
        Object value = event.getProperty(property);
        if (value == null) {
            PerkLogger.e(LOG_TAG, "getLongEventProperty - " + property + " is null!");
            return -1L;
        }
        return Long.parseLong(value.toString());
    }

    public static String replacePointsMacro(String original, String points) {
        PerkLogger.d(LOG_TAG,
                "replacePointsMacro: Got original: " + original + "\npoints: " + points);

        if (TextUtils.isEmpty(original)) {
            return original;
        }

        if (original.contains("{{user_points}}")) {
            return original.replace("{{user_points}}", points);
        }

        return original.replace("{user_points}", points);
    }

    public static String replaceAccessTokenMacro(String original) {
        String access_token = PerkUtils.getAccessTocken();

        PerkLogger.d(LOG_TAG,
                "replaceAccessTokenMacro: Got original: " + original + "\naccess_token: "
                        + access_token);

        if (TextUtils.isEmpty(original)) {
            return original;
        }

        if (original.contains("{{access_token}}")) {
            return original.replace("{{access_token}}", access_token);
        }

        return original.replace("{access_token}", access_token);
    }

    public static Map<String, String> getUserAttributeMapFor(String gender, String dob,
                                                             String email, boolean emailVerified, String referralCode) {
        boolean emailsOptedIn = PerkPreferencesManager.INSTANCE.hasOptedInForEmails();
        PerkLogger.d(LOG_TAG, "getUserAttributeMapFor: emailsOptedIn: " + emailsOptedIn);

        Map<String, String> map = new HashMap<>();

        map.put("Gender", TextUtils.isEmpty(gender) ? "" : gender);

        map.put("DOB", TextUtils.isEmpty(dob) ? "" : dob);

        map.put("Email", TextUtils.isEmpty(email) ? "" : email);

        map.put("Email Verified", !TextUtils.isEmpty(email) ? (emailVerified
                ? "yes" : "no") : "");

        map.put("Marketing Email Opt-in", !TextUtils.isEmpty(email) ? (emailsOptedIn
                ? "yes" : "no") : "");

        map.put("Referral Code", TextUtils.isEmpty(referralCode) ? "" : referralCode);

        // UUID removed since we are now setting it via Leanplum.setUserId, as per https://jira
        // .rhythmone.com/browse/PEWAN-337
        //map.put(LEANPLUM_PERK_UUID_ATTRIBUTE, TextUtils.isEmpty(uuid) ? "" : uuid);

        // Setting to empty always, since Leanplum now merges the profiles after we have set our
        // UUID as the Leanplum-UserId & this also causes the old 'custom_uuid' to be merged & shown
        map.put(LEANPLUM_PERK_UUID_ATTRIBUTE, "");

        // Removed since we do not have interests/channels as per https://jira.rhythmone
        // .com/browse/PEWAN-487:
        map.put(AppUtility.LEANPLUM_INTERESTS_ATTRIBUTE, "");
        map.put(AppUtility.LEANPLUM_CHANNELS_ATTRIBUTE, "");

        return map;
    }

    public static void handleProtocolUri(String protocolUri) {
        final ProgressDialog progressDialog;

        protocolUri = protocolUri.replaceFirst("watchback://", "");

        // Could be:
        // 1. Specific channel: watchback://channel:<Channel UUID>
        // 2. Specific video: watchback://video:<Video ID>
        // 3. Launch Sweeps/Thank You screen: watchback://sweeps
        // 4. Launch Donate screen: watchback://donate
        // 5. Launch Account page: watchback://account
        // 6. Launch Channels page: watchback://channels
        // 7. Featured video: watchback://featured:<UUID>

        if (protocolUri.toLowerCase(Locale.US).startsWith("channel:")) {
            // -We open the Brand-details page directly with the uuid for channel:
            protocolUri = protocolUri.replaceFirst("channel:", "");
            PerkLogger.d(LOG_TAG,
                    "checkForProtocolUri(): protocolUri after removing 'channel:': "
                            + protocolUri);

            Intent intent = new Intent(PerkUtils.getAppContext(), BrandDetailsActivity.class);
            intent.putExtra(BrandDetailsActivity.EXTRA_CHANNEL_UUID, protocolUri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PerkUtils.getAppContext().startActivity(intent);

        } else if (protocolUri.toLowerCase(Locale.US).startsWith("video")) {
            // -We fetch the video-details first, then open the VideoPlayerActivity with it:
            final String videoID = protocolUri.replaceFirst("video:", "");
            PerkLogger.d(LOG_TAG,
                    "checkForProtocolUri(): protocolUri after removing 'video:': "
                            + videoID);

            // Fetch video details:
            progressDialog = showProgressDialog();

            BrightcoveAPIController.INSTANCE.getBrightCoveVideos(videoID,
                    new OnRequestFinishedListener<BrightcovePlaylistData.BrightcoveVideo>() {

                        @Override
                        public void onSuccess(
                                @NonNull BrightcovePlaylistData.BrightcoveVideo brightcoveVideoData,
                                @Nullable String s) {

                            PerkLogger.d(LOG_TAG,
                                    "Loading video-data successful for video-ID: " + videoID);

                            // fetch related videos:
                            fetchRelatedVideosAndStartPlayer(brightcoveVideoData, null, false,
                                    successful -> {
                                        PerkLogger.d(LOG_TAG,
                                                "Searching for related videos completed! successful: "
                                                        + successful);
                                        AppUtility.dismissDialog(progressDialog);
                                    });
                        }

                        @Override
                        public void onFailure(@NonNull ErrorType errorType,
                                              @Nullable PerkResponse<BrightcovePlaylistData.BrightcoveVideo>
                                                      perkResponse) {
                            AppUtility.dismissDialog(progressDialog);

                            PerkLogger.e(LOG_TAG,
                                    "Loading video-data failed for video-ID: " + videoID + ": "
                                            + (perkResponse != null ? perkResponse.getMessage()
                                            : "Error!"));
                        }
                    });
        } else if (protocolUri.toLowerCase(Locale.US).startsWith("featured")) {
            // -We will fetch the related videos and then open the VideoPlayerActivity with it:
            final String videoUUID = protocolUri.replaceFirst("featured:", "");
            PerkLogger.d(LOG_TAG,
                    "checkForProtocolUri(): protocolUri after removing 'featured:': "
                            + videoUUID);

            // Fetch video details:
            progressDialog = showProgressDialog();

            // fetch related videos:
            fetchRelatedVideosAndStartPlayer(null, videoUUID, false,
                    successful -> {
                        PerkLogger.d(LOG_TAG,
                                "Searching for related videos for featured items completed! successful: "
                                        + successful);
                        AppUtility.dismissDialog(progressDialog);
                    });
        } else {
            Context context = PerkUtils.getAppContext();
            // -We open the corresponding page on home-screen, depending on the obtained value:
            int pageIndex = context.getResources().getInteger(R.integer.home);  // default (for homescreen)

            protocolUri = protocolUri.toLowerCase(Locale.US);

            if (protocolUri.startsWith("channels")) {
                pageIndex = context.getResources().getInteger(R.integer.channels);
            }/* else if (protocolUri.startsWith("donate")) {
                pageIndex = context.getResources().getInteger(R.integer.account);
            }*/ else if (protocolUri.startsWith("sweeps")) {
                pageIndex = context.getResources().getInteger(R.integer.sweeps);
            } else if (protocolUri.startsWith("account")) {
                pageIndex = context.getResources().getInteger(R.integer.settings);
            }

            PerkLogger.d(LOG_TAG, "checkForProtocolUri(): pageIndex set to: " + pageIndex);

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(MainActivity.SELECTED_TAB_INDEX, pageIndex);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        }
    }

    public static void fetchRelatedVideosAndStartPlayer(
            BrightcovePlaylistData.BrightcoveVideo videoData, String uuid, final boolean forFeaturedItems,
            final OperationCompleteCallback callback) {

        if (videoData == null && TextUtils.isEmpty(uuid)) {
            if (callback != null) {
                callback.onOperationCompleted(false);
            }
            return;
        }

        /*
        Updated to use Perk API for related videos as per:
        https://jira.rhythmone.com/browse/PEWAN-483
        https://jira.rhythmone.com/browse/PAPI-384?focusedCommentId=348205&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-348205
         */
        String searchQuery = videoData != null ? videoData.getUuid() : uuid;
        /*if (videoData.getVideoId() != 0) {
            searchQuery = "tags:" + videoData.getVideoId();
        } else {
            searchQuery = "tags:" + videoData.getId();
        }*/

        if (TextUtils.isEmpty(searchQuery)) {
            // No uuid, cannot search:
            PerkLogger.w(LOG_TAG, "fetchRelatedVideosAndStartPlayer(): uuid unavailable for video: "
                    + (videoData != null ? videoData.toString() : uuid));
            onLoadComplete(videoData, forFeaturedItems, callback, new ArrayList<>());
            return;
        }

        Context context = PerkUtils.getAppContext();

        final boolean isLoggedIn = UserInfoValidator.isAuthenticated(
                AuthAPIRequestController.INSTANCE.getCurrentAuthenticatedSessionDetails(context));

        PerkLogger.d(LOG_TAG,
                "Searching for related videos with query uuid: " + searchQuery + " isLoggedIn: "
                        + isLoggedIn);

       /* PerkAPIController.INSTANCE.getRelatedVideos(context, isLoggedIn, searchQuery,
                new OnRequestFinishedListener<RelatedVideosData>() {
                    @Override
                    public void onSuccess(
                            @NonNull RelatedVideosData relatedVideosData,
                            @Nullable String s) {

                        List<BrightcovePlaylistData.BrightcoveVideo> videoList =
                                relatedVideosData.getRelated();

                        PerkLogger.d(LOG_TAG, "Video-search by tag successful! list size: " + (
                                videoList != null ? videoList.size() : "0"));

                        if (videoList == null) {
                            PerkLogger.e(LOG_TAG, "Video-search by tag returned no videos!");
                            videoList = new ArrayList<>();
                        }

                        BrightcovePlaylistData.BrightcoveVideo brightcoveVideoData = videoData;

                        if (brightcoveVideoData == null) {
                            brightcoveVideoData = relatedVideosData.createBrightcoveVideoInstance();
                            PerkLogger.d(LOG_TAG,
                                    "Created BrightcoveVideoData from relatedVideosData: "
                                            + brightcoveVideoData.toString());
                        }

                        onRelatedVideosAvailable(brightcoveVideoData, videoList, forFeaturedItems, callback);
                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType,
                            @Nullable PerkResponse<RelatedVideosData> perkResponse) {
                        PerkLogger.d(LOG_TAG,
                                "Video-search by tag failed! " + (perkResponse != null
                                        ? perkResponse.getMessage() : ""));

                        // No videos returned... so we will just have a single video in the list
                        onLoadComplete(videoData, forFeaturedItems, callback, new ArrayList<>());
                    }

                });*/
    }

    private static void onRelatedVideosAvailable(BrightcovePlaylistData.BrightcoveVideo videoData,
                                                 List<BrightcovePlaylistData.BrightcoveVideo> videoList,
                                                 boolean forFeaturedItems,
                                                 final OperationCompleteCallback callback) {

        PerkLogger.d(LOG_TAG, "onRelatedVideosAvailable: list size: " + (
                videoList != null ? videoList.size() : "0"));

        if (videoList != null) {

            // In case the videoList contains the video that we have already
            // fetched, then remove
            // it so that there are no duplicates
            String mainVideoId = videoData != null ? videoData.getId() : "";

            // Search for video with same ID as our original clicked video
            // above...
            BrightcovePlaylistData.BrightcoveVideo videoToRemove = null;
            if (!TextUtils.isEmpty(mainVideoId)) {
                for (BrightcovePlaylistData.BrightcoveVideo video : videoList) {
                    if (video != null && TextUtils.equals(mainVideoId,
                            video.getId())) {
                        videoToRemove = video;
                        break;
                    }
                }
            }

            // Removing the video:
            if (videoToRemove != null) {
                PerkLogger.d(LOG_TAG,
                        "Removing entry for duplicated main video: "
                                + videoToRemove.toString());
                videoList.remove(videoToRemove);
            }
        } else {
            PerkLogger.e(LOG_TAG, "Video-search by tag returned no videos!");
            videoList = new ArrayList<>();
        }

        onLoadComplete(videoData, forFeaturedItems, callback, videoList);
    }

    private static void onLoadComplete(
            BrightcovePlaylistData.BrightcoveVideo videoData,
            boolean forFeaturedItems,
            final OperationCompleteCallback callback,
            @NonNull List<BrightcovePlaylistData.BrightcoveVideo> videoList) {

        if (videoData != null) {
            // The obtained videoList will have already removed the video that
            // was originally clicked, so we just add our video to the top of the
            // list, which is now to be used on Video player screen for listing
            // related videos on UI:
            videoList.add(0, videoData);
        }

        if (!videoList.isEmpty()) {
            // Set to 0 since we will always have our video at the first position:
//            VideoPlayerActivity.nCurrentIndex = 0;
//            VideoPlayerActivity.arrVideos = videoList;

            Intent intent = new Intent(PerkUtils.getAppContext(), VideoPlayerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(VideoPlayerActivity.PLAYLIST_NAME,
                    forFeaturedItems ? HomeFragmentListAdapter.FEATURED : "Promo Video");
            intent.putExtra(VideoPlayerActivity.IS_CHANNEL_LIST, false);

            if (callback != null) {
                callback.onOperationCompleted(true);
            }

            PerkUtils.getAppContext().startActivity(intent);
        } else if (callback != null) {
            callback.onOperationCompleted(false);
        }
    }

    public static void showVideoDetailsActivity(@NonNull BrightcovePlaylistData.BrightcoveVideo videoData, @Nullable Bundle bundle) {
        Intent intent = new Intent(PerkUtils.getAppContext(), VideoDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(VideoDetailsActivity.VIDEO_DATA, videoData);
        intent.putExtra(VideoDetailsActivity.DATA_BUNDLE, bundle);
        PerkUtils.getAppContext().startActivity(intent);
    }

    private static ProgressDialog showProgressDialog() {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(
                PerkUtils.getTopActivity(),
                PerkPreferencesManager.INSTANCE.isNightMode() ? R.style.AppCompatAlertDialogStyle
                        : R.style.AppCompatAlertDialogStyleLight);

        ProgressDialog progressDialog = new ProgressDialog(wrapper);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        return progressDialog;
    }

    public static String safeReflectionToString(Object object) {
        if (object == null) {
            return "null";
        }

        return ToStringBuilder.reflectionToString(object);
    }

    public static boolean isVideoWatched(BrightcovePlaylistData.BrightcoveVideo video) {
        return video != null && PerkPreferencesManager.INSTANCE.isVideoWatched(video.getId());
    }

    public static boolean isVideoWatchedUntilCap(BrightcovePlaylistData.BrightcoveVideo video, int watchCap) {
        boolean watched = video != null && (video.getWatched() || isVideoWatched(video));

        if (!watched) {
            return false;
        }

        return PerkPreferencesManager.INSTANCE.getWatchedCountForVideo(video.getId()) == watchCap;
    }

    public static void preloadImage(Context context, String url, RequestListener<Drawable> listener) {
        if (listener == null) {
            return;
        }

        if (TextUtils.isEmpty(url)) {
            listener.onLoadFailed(null, null, null, false);
            return;
        }

        Glide.with(context.getApplicationContext()).load(url).listener(listener).preload();
    }

    public static void showLoginDialog(Context context) {
        if (context == null || !(context instanceof Activity) || ((Activity) context).isFinishing()) {
            return;
        }

        final Dialog popup = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#b2050b0d")));
        LayoutDialogLoginBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.layout_dialog_login, null, false);
        popup.setContentView(binding.getRoot());
        popup.setCanceledOnTouchOutside(false);

        binding.icClose.setOnClickListener(v -> dismissPopUp(popup));
        binding.idTvClose.setOnClickListener(v -> dismissPopUp(popup));
        binding.idBtnSignup.setOnClickListener(v -> {
            dismissPopUp(popup);
            if (context == null || ((Activity) context).isFinishing()) {
                return;
            }
            Intent intent = new Intent(context, SignupActivity.class);
            intent.putExtra(AppConstants.LOGIN_MODE, 1);
            context.startActivity(intent);
        });

        binding.idTvLogin.setOnClickListener(v -> {
            dismissPopUp(popup);
            if (context == null || ((Activity) context).isFinishing()) {
                return;
            }
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
        });
        try {
            if (popup != null && !popup.isShowing()) {
                popup.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void dismissPopUp(Dialog popup) {
        try {
            if (popup != null && popup.isShowing()) {
                popup.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
