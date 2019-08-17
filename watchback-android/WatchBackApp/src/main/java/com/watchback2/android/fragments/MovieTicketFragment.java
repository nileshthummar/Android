package com.watchback2.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.watchback2.android.R;
import com.watchback2.android.activities.MainActivity;
import com.watchback2.android.adapters.VideoHistoryAdapter;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.databinding.FragmentMovieTicketBinding;
import com.watchback2.android.navigators.ISettingsContainerNavigator;
import com.watchback2.android.viewmodels.MovieTicketViewModel;

public class MovieTicketFragment extends Fragment implements ISettingsContainerNavigator {

    public static final String TAG = "MovieTicketFragment";

    private MovieTicketViewModel mViewModel;
    private FragmentMovieTicketBinding binding;

    public static MovieTicketFragment newInstance() {
        return new MovieTicketFragment();
    }

    public MovieTicketFragment() {
        super();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_movie_ticket, container, false);
        binding = FragmentMovieTicketBinding.bind(rootView);

        mViewModel = new MovieTicketViewModel();

        binding.setViewModel(mViewModel);
        binding.setSettingsContainerNavigator(this);

        // Facebook Analytics: Movie Tickets Screen - When a user lands on the Rewards screen
        FacebookEventLogger.getInstance().logEvent("Movie Tickets Screen");

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.setClickListener(new MovieTicketViewModel.IMovieTicketClickListener() {
            @Override
            public void onStartWatchingButtonClick() {
                // TODO: navigate to HomeScreen
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showHomeFragment();
                }
            }

            @Override
            public void onVideoItemClick(long videoID) {
                String uuid = Long.toString(videoID);
                // TODO: navigate branddetails screen
            }
        });
        mViewModel.makeVideoHistoryCall(getContext());

        VideoHistoryAdapter adapter = new VideoHistoryAdapter(mViewModel.getVideoList().get());
        binding.idRvHistory.setAdapter(adapter);
        binding.idRvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.idRvHistory.setItemAnimator(new DefaultItemAnimator());

    }


//    ----------------------------------------------------------------------------------------------
//    ISettingsContainerNavigator Implementation
//    ----------------------------------------------------------------------------------------------

    @Override
    public void handleSettingsClick(View view) {
        // Settings icon is hidden from UI now - visibility set to GONE
        // Facebook Analytics: settings_tap - user taps on Settings:
       /* FacebookEventLogger.getInstance().logEvent("settings_tap");

        Intent intent = new Intent(view.getContext(), SettingsFragment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/
    }

    @Override
    public void handleEditClick(View view) {
        // not used
    }
}
