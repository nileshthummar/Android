package com.watchback2.android.fragments;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import com.watchback2.android.R;

/**
 * Created by perk on 22/08/18.
 */
public class NoInternetDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.no_internet_title)
                .setMessage(R.string.no_internet_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.ok,
                        (dialog, which) -> dismissAllowingStateLoss());

        return builder.create();
    }

}
