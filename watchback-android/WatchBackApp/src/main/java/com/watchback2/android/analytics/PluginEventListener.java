package com.watchback2.android.analytics;

import com.brightcove.player.event.Event;

public interface PluginEventListener {

    enum PluginEvent {
        PLUGIN_INITIALISATION_FAILED,
        SDK_INITIALISATION_FAILED,
        PROVIDED_PLUGIN_DATA_ERROR,
        PLAYER_ERROR
    }

    /**
     * Nielsen SDK events are forwarded from Plugin via this method
     */
    void onAppSdkEvent(final long timestamp, final int code, final String description);

    /**
     * Plugin events are sent via this method
     * @param pluginEvent
     */
    void onPluginEvent(final PluginEvent pluginEvent);

    void OnBCEvent (Event event);
}
