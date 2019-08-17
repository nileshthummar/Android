package com.watchback2.android.views;

import android.app.DatePickerDialog;
import android.app.Dialog;
import androidx.databinding.ObservableField;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.DialogFragment;
import android.text.TextUtils;
import android.widget.DatePicker;

import com.perk.util.PerkLogger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by perk on 23/03/18.
 * Fragment for showing date picker dialog and setting up date to TextView
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private static final long THIRTEEN_YEARS_MILLIS = (long) (1000 * 60 * 60 * 24 * 365.25 * 13);

    private final ObservableField<String> doBText = new ObservableField<>();

    private int mm, dd, yy;

    private final String[] monthArray = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    public static final String DOBSTRING = "dobstring";

    private static final String LOG_TAG = "DatePickerFragment";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getArguments() != null
                && getArguments().containsKey(DOBSTRING)
                && getArguments().getString(DOBSTRING) != null
                && !TextUtils.isEmpty(getArguments().getString(DOBSTRING))) {
            String inputDateStr = getArguments().getString(DOBSTRING);

            if (inputDateStr == null || TextUtils.isEmpty(inputDateStr)) {
                getCurrentDate();
            }
            try {
                final String[] split = inputDateStr != null ? inputDateStr.split("-") : null;
                if (split != null) {
                    yy = Integer.parseInt(split[2]);
                    mm = getMonth(split[1]);
                    dd = Integer.parseInt(split[0]);
                } else {
                    getCurrentDate();
                }
            } catch (IndexOutOfBoundsException e) {
                getCurrentDate();
            } catch (NumberFormatException e) {
                getCurrentDate();
            }
        } else {
            getCurrentDate();
        }

        if (getContext() == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), this, yy, mm, dd);
        /*datePickerDialog.getDatePicker().setMaxDate(
                Calendar.getInstance().getTimeInMillis() - THIRTEEN_YEARS_MILLIS);*/
        return datePickerDialog;
    }

    private void getCurrentDate() {
        final Calendar calendar = Calendar.getInstance();
        yy = calendar.get(Calendar.YEAR);
        mm = calendar.get(Calendar.MONTH);
        dd = calendar.get(Calendar.DAY_OF_MONTH);
    }

    public void onDateSet(DatePicker view, int yy, int mm, int dd) {
        populateSetDate(yy, (mm + 1), dd);
    }

    public void populateSetDate(int year, int month, int day) {

        DateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        DateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        String inputDateStr = day + "/" + month + "/" + year;
        Date date = null;
        try {
            date = inputFormat.parse(inputDateStr);
        } catch (ParseException e) {
            PerkLogger.e(LOG_TAG, "error in parsing date", e);
        }
        String outputDateStr = outputFormat.format(date);
        PerkLogger.d(LOG_TAG, outputDateStr);

        getDoBText().set(outputDateStr);
    }

    @VisibleForTesting
    public int getMonth(@NonNull String monthString) {
        int index = 0;
        for (String monthName : monthArray) {
            if (monthString.toLowerCase(Locale.US).startsWith(monthName.toLowerCase(Locale.US))) {
                return index;
            }
            index++;
        }
        return 0;
    }

    public ObservableField<String> getDoBText() {
        return doBText;
    }
}
