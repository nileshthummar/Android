package com.watchback2.android.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.perk.util.PerkLogger;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class for converting between date and strings.
 */
public class DateTimeUtil {

    private static final String TAG = "DateTimeUtil";

    /** UTC date format with time zone. */
    private static final String DATE_FORMAT_UTC_WITHOUT_TIME_ZONE = "yyyy-MM-dd'T'HH:mm:ss'.'S'Z'";

    /** UTC date format without time zone. */
    private static final String DATE_FORMAT_UTC_WITH_TIME_ZONE = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /** UTC date format without time zone & without seconds. */
    private static final String DATE_FORMAT_DATE_AND_TIME_WITHOUT_TIME_ZONE =
            "yyyy-MM-dd'T'HH:mm'Z'";

    /** Date format with date and time only, without time zone. */
    private static final String DATE_FORMAT_WITH_DATE_AND_TIME = "yyyy-MM-dd HH:mm:ss";

    /** Date format representing RFC 2822 format. */
    private static final String DATE_FORMAT_RFC_2822 = "EEE, dd MMM yyyy HH:mm:ss Z";

    /** GMT time zone. */
    private static final String TIME_ZONE_GMT = "GMT";

    private static final String READABLE_DATE_FORMAT = "MMM dd, yyyy h:mm a zzzz";

    /**
     * Returns the date from the raw date string.
     *
     * @param utc8601String date string in UTC 8601 format
     * @return date from the string
     */
    public static Date getDateFromRawDate(final String utc8601String) {

        // ex: 2013-09-30T18:59:49.000Z
        if (TextUtils.isEmpty(utc8601String)) {
            return null;
        }

        try {
            final SimpleDateFormat formatter = new SimpleDateFormat(
                    DATE_FORMAT_UTC_WITHOUT_TIME_ZONE, Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_GMT));
            return formatter.parse(utc8601String);
        } catch (Exception e) {

            try {
                final SimpleDateFormat formatter = new SimpleDateFormat(
                        DATE_FORMAT_UTC_WITH_TIME_ZONE, Locale.ENGLISH);
                return formatter.parse(utc8601String);
            } catch (ParseException e1) {
                return null;
            }
        }
    }

    /**
     * Returns date from date and time.
     *
     * @param rawDate date string that need to be parsed
     * @return date object from raw date
     */
    public static Date getDateFromDateAndTimeWithoutTimezone(final String rawDate) {
        if (TextUtils.isEmpty(rawDate))
            return null;

        try {
            final SimpleDateFormat dateFormat =
                    new SimpleDateFormat(
                            DATE_FORMAT_DATE_AND_TIME_WITHOUT_TIME_ZONE, Locale.ENGLISH
                    );
            dateFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_GMT));
            return dateFormat.parse(rawDate);
        } catch (final IllegalArgumentException | ParseException e) {
            PerkLogger.a(TAG, "Error while parsing date: " + rawDate, e);
            return null;
        }
    }

    /**
     * Returns date from date time string, without time zone
     *
     * @param dateTime date time string that need to be converted
     * @return date from date time string
     */
    public static Date getDateFromDateTimeString(final String dateTime) {

        // Make sure we have valid date time string
        if (TextUtils.isEmpty(dateTime))
            return null;

        try {
            final Format format =
                    new SimpleDateFormat(DATE_FORMAT_WITH_DATE_AND_TIME, Locale.ENGLISH);
            return (Date) format.parseObject(dateTime);
        } catch (final IllegalArgumentException | ParseException | ClassCastException e) {
            PerkLogger.a(TAG, "Unable to convert to date from the format: " +
                    DATE_FORMAT_WITH_DATE_AND_TIME, e);
            return null;
        }
    }

    /**
     * Returns RFC 2822 format date timestamp
     *
     * @param date date for which timestamp is required in RFC 2822 format
     * @return RFC 2822 format date timestamp
     */
    public static String getRFC2822String(@NonNull final Date date) {
        final Format format = new SimpleDateFormat(DATE_FORMAT_RFC_2822, Locale.ENGLISH);
        return format.format(date);
    }

    /**
     * Returns whether date is for today or not.
     *
     * @param date date that need to be checked
     * @return date is for today or not
     */
    public static boolean isToday(final Date date) {
        final Calendar comparisonDate = Calendar.getInstance(Locale.ENGLISH);
        comparisonDate.setTime(date);
        return isToday(comparisonDate);
    }

    /**
     * Returns whether date is for today or not.
     *
     * @param comparisonDate date that need to be compared
     * @return date is for today or not.
     */
    public static boolean isToday(final Calendar comparisonDate) {
        final Calendar today = Calendar.getInstance(Locale.ENGLISH);
        return today.get(Calendar.ERA) == comparisonDate.get(Calendar.ERA) &&
                today.get(Calendar.YEAR) == comparisonDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == comparisonDate.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Returns if date is in future from now or not.
     *
     * @param date date that need to be compared
     * @return date is in future from now or not.
     */
    public static boolean isInFuture(final Date date) {
        final Calendar comparisonDate = Calendar.getInstance(Locale.ENGLISH);
        comparisonDate.setTime(date);
        return isInFuture(comparisonDate);
    }

    /**
     * Returns if date is in future from now or not.
     *
     * @param calendar date that need to be compared
     * @return date is in future from now or not.
     */
    public static boolean isInFuture(final Calendar calendar) {
        final Calendar today = Calendar.getInstance(Locale.ENGLISH);
        return today.before(calendar);
    }

    /**
     * Returns whether a person is older than given number of years
     *
     * @param ageInYears number of years person should be old
     * @param dobDate date of birth of the person
     * @return person is older than given number of years
     */
    public static boolean isOlderThan(final int ageInYears, final Date dobDate) {
        final Calendar dob = Calendar.getInstance();
        dob.setTime(dobDate);
        return DateTimeUtil.isOlderThan(ageInYears, dob);
    }

    /**
     * Returns whether a person is older than given number of years
     *
     * @param ageInYears number of years person should be old
     * @param dobCalendar date of birth of the person
     * @return person is older than given number of years
     */
    public static boolean isOlderThan(final int ageInYears, final Calendar dobCalendar) {

        // Calculate youngest acceptable date
        final Calendar youngestAcceptableDate = Calendar.getInstance(Locale.ENGLISH);
        youngestAcceptableDate.set(Calendar.YEAR,
                (youngestAcceptableDate.get(Calendar.YEAR) - ageInYears));
        final Calendar dob = (Calendar) dobCalendar.clone();

        // Set the time to 00:00 hours for both dates
        DateTimeUtil.setTimePartToZero(youngestAcceptableDate);
        DateTimeUtil.setTimePartToZero(dob);

        // Check if user is older than youngest acceptable date
        return youngestAcceptableDate.after(dobCalendar);
    }

    /**
     * Sets the time part in the date to 00:00 hours
     *
     * @param cal date that should be modified.
     */
    public static void setTimePartToZero(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    public static String getReadableDateString(@NonNull final Date date) {
        final Format format = new SimpleDateFormat(READABLE_DATE_FORMAT, Locale.US);
        return format.format(date);
    }
}