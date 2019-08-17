package com.watchback2.android.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.activities.LoginActivity;
import com.watchback2.android.activities.SignupActivity;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.databinding.FragmentUnregisteredMovieticketBinding;
import com.watchback2.android.navigators.ISettingsContainerNavigator;
import com.watchback2.android.navigators.IWalkthroughNavigator;
import com.watchback2.android.utils.AppConstants;

public class UnregisteredMovieTicketFragment extends Fragment implements IWalkthroughNavigator, ISettingsContainerNavigator {

    public static final String TAG = "UnregisteredMovieTicketFragment";

    public static UnregisteredMovieTicketFragment newInstance() {
        return new UnregisteredMovieTicketFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        PerkLogger.d(TAG, "onCreateView()");

        final View rootView = inflater.inflate(R.layout.fragment_unregistered_movieticket, container, false);
        FragmentUnregisteredMovieticketBinding binding = FragmentUnregisteredMovieticketBinding.bind(rootView);

        binding.setSettingsContainerNavigator(this);
        binding.setWalkthroughNavigator(this);

        // Facebook Analytics: Movie Tickets Screen - When a user lands on the Rewards screen
        FacebookEventLogger.getInstance().logEvent("Movie Tickets Screen");

        return binding.getRoot();
    }

    @Override
    public void handleSettingsClick(View view) {
        // Settings icon is hidden from UI now - visibility set to GONE
        // Facebook Analytics: settings_tap - user taps on Settings:
        /*FacebookEventLogger.getInstance().logEvent("settings_tap");

        Intent intent = new Intent(view.getContext(), SettingsFragment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/
    }

    @Override
    public void handleSignUpClick() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), SignupActivity.class);
            intent.putExtra(AppConstants.LOGIN_MODE, 1);
            startActivity(intent);
        }
    }

    @Override
    public void handleLogInClick() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void handleEditClick(View view) {
        // Un-used
    }

    @Override
    public void handleSkipClick() {
        // Do Nothing
    }
}
