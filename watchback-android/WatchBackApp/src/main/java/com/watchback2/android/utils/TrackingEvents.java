package com.watchback2.android.utils;

/**
 * Created by perk on 12/06/18.
 */
public final class TrackingEvents {

    /**
     * Event when a user starts a long-form video
     */
    public static final String LEANPLUM_LONGFORM_START = "longform_start";

    /**
     * Event when a user completes a long-form video
     */
    public static final String LEANPLUM_LONGFORM_COMPLETE = "longform_complete";

    /**
     * Event when a user exits a long-form video prior to completing it
     */
    public static final String LEANPLUM_LONGFORM_EXIT = "longform_exit";

    /**
     * Parameter used with events to specify the video-id
     */
    public static final String LEANPLUM_PARAMETER_VIDEO_ID = "videoid";

    /**
     * Event when a user starts a short-form video
     */
    public static final String LEANPLUM_SHORTFORM_START = "shortform_start";

    /**
     * Event when a user completes a short-form video
     */
    public static final String LEANPLUM_SHORTFORM_COMPLETE = "shortform_complete";

    /**
     * Event when a user exits a short-form video prior to completing it
     */
    public static final String LEANPLUM_SHORTFORM_EXIT = "shortform_exit";

    /**
     * Event when a user taps on info icon next to his/her referral code, on account page
     */
    public static final String LEANPLUM_CODE_INFO_TAP= "code_info_tap";
    /**
     * Event when user signed up successfully
     */
    public static final String LEANPLUM_REGISTRATION_SUCCESS= "registration_success";

}
