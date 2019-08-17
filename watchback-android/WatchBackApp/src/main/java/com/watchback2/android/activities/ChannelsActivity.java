package com.watchback2.android.activities;

import android.os.Bundle;

import com.watchback2.android.R;
import com.watchback2.android.adapters.ChannelsListAdapter;
import com.watchback2.android.analytics.FacebookEventLogger;
import com.watchback2.android.databinding.ActivityChannelsBinding;
import com.watchback2.android.models.Channel;
import com.watchback2.android.navigators.IChannelsListNavigator;
import com.watchback2.android.viewmodels.ChannelsViewModel;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

public class ChannelsActivity extends BaseThemeableActivity implements IChannelsListNavigator {

    private ChannelsViewModel channelsViewModel;

    public static final String FROM_SETTINGS = "is_from_settings";

    @SuppressWarnings({"PMD.UnusedPrivateField"})
    private static final String TAG = "ChannelsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isFromSettings = getIntent() != null && getIntent().getBooleanExtra(FROM_SETTINGS,
                false);

        // Facebook-event tracking for Channels screen: (isFromSettings is always true as of now
        // -since we removed select-channels from app-start flow)
        FacebookEventLogger.getInstance().logEvent("edit_channels");

        ActivityChannelsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_channels);
        channelsViewModel = new ChannelsViewModel(this);
        channelsViewModel.setiChannelsListNavigator(this);
        channelsViewModel.setEditingFromSettings(isFromSettings);
        binding.setViewModel(channelsViewModel);

        ChannelsListAdapter adapter = new ChannelsListAdapter(new ArrayList<Channel>(0), this);
        binding.idChannelsList.setAdapter(adapter);
        binding.idChannelsList.setLayoutManager(new LinearLayoutManager(this));
        binding.idChannelsList.setItemAnimator(new DefaultItemAnimator());

        channelsViewModel.loadChannelsList();
    }

    @Override
    public void gotoNextActivity() {
        /*if (!channelsViewModel.isEditingFromSettings()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }*/
        finish();
    }

    @Override
    public void onItemUpdated(@NonNull Channel channelItem) {
        if (channelsViewModel != null) {
            channelsViewModel.onItemUpdated(channelItem);
        }
    }

    @Override
    public void finishChannelActivity() {
        finish();
    }
}
