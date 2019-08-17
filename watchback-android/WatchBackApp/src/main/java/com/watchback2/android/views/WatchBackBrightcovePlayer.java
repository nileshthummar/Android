package com.watchback2.android.views;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.brightcove.player.event.Component;
import com.brightcove.player.event.Default;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventLogger;
import com.brightcove.player.event.ListensFor;
import com.brightcove.player.pictureinpicture.PictureInPictureManager;
import com.brightcove.player.util.LifecycleUtil;
import com.brightcove.player.view.BaseVideoView;
import com.brightcove.player.view.BrightcoveVideoView;
import com.watchback2.android.R;
import com.watchback2.android.activities.BaseThemeableActivity;

/**
 * Same as BrightcovePlayer class, but we cannot use that with our app-themes as it extends
 * Activity class instead of AppCompatActivity, hence this class.
 */
@ListensFor(
        events = {"activitySaveInstanceState"}
)
public abstract class WatchBackBrightcovePlayer extends BaseThemeableActivity implements Component {

    public static final String TAG = "WbBrightcovePlayer";

    protected BaseVideoView brightcoveVideoView;
    private LifecycleUtil lifecycleUtil;
    private EventLogger eventLogger;
    private Bundle savedInstanceState;
    private boolean pictureInPictureEnabled;

    public BrightcoveVideoView getBrightcoveVideoView() {
        BrightcoveVideoView result = null;
        if (this.brightcoveVideoView instanceof BrightcoveVideoView) {
            result = (BrightcoveVideoView) this.brightcoveVideoView;
        }

        return result;
    }

    public BaseVideoView getBaseVideoView() {
        return this.brightcoveVideoView;
    }

    public void showClosedCaptioningDialog() {
        this.brightcoveVideoView.getClosedCaptioningController().showCaptionsDialog();
    }

    public void fullScreen() {
        if (!this.brightcoveVideoView.isFullScreen()) {
            this.brightcoveVideoView.getEventEmitter().emit("enterFullScreen");
        } else {
            Log.e(TAG, "The video view is already in full screen mode.");
        }

    }

    public void normalScreen() {
        if (this.brightcoveVideoView.isFullScreen()) {
            this.brightcoveVideoView.getEventEmitter().emit("exitFullScreen");
        } else {
            Log.e(TAG, "The video view is not in full screen mode!");
        }

    }

    public EventLogger getEventLogger() {
        return this.eventLogger;
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        if (this.lifecycleUtil != null) {
            this.lifecycleUtil.onConfigurationChanged(configuration);
        }
        super.onConfigurationChanged(configuration);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.brightcoveVideoView == null || (this.lifecycleUtil != null
                && this.lifecycleUtil.baseVideoView == this.brightcoveVideoView)) {
            this.savedInstanceState = savedInstanceState;
        } else {
            this.lifecycleUtil = new LifecycleUtil(this.brightcoveVideoView);
            this.attemptToRegisterPiPActivity();
            this.lifecycleUtil.onCreate(savedInstanceState, this);
            this.eventLogger = new EventLogger(this.brightcoveVideoView.getEventEmitter(), true,
                    this.getClass().getSimpleName());
        }

    }

    private void initializeLifecycleUtil(View view) {
        if (this.brightcoveVideoView == null) {
            this.findBaseVideoView(view);
            if (this.brightcoveVideoView == null) {
                throw new IllegalStateException("A BaseVideoView must be wired up to the layout.");
            }

            this.lifecycleUtil = new LifecycleUtil(this.brightcoveVideoView);
            this.attemptToRegisterPiPActivity();
            this.lifecycleUtil.onCreate(this.savedInstanceState, this);
            this.eventLogger = new EventLogger(this.brightcoveVideoView.getEventEmitter(), true,
                    this.getClass().getSimpleName());
        }

        this.savedInstanceState = null;
    }

    private void attemptToRegisterPiPActivity() {
        // Disable PiP for now:
        this.pictureInPictureEnabled = false;
        Log.w(TAG, "This activity was not set to use Picture-in-Picture.");
        /*if (Build.VERSION.SDK_INT >= 26) {
            try {
                this.setPictureInPictureParams((new PictureInPictureParams.Builder()).build());
                PictureInPictureManager.getInstance().registerActivity(this, this.brightcoveVideoView);
                this.pictureInPictureEnabled = true;
            } catch (IllegalStateException var2) {
                this.pictureInPictureEnabled = false;
                Log.w(TAG, "This activity was not set to use Picture-in-Picture.");
            }
        }*/
    }

    private void findBaseVideoView(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();

            for (int i = 0; i < childCount; ++i) {
                View child = viewGroup.getChildAt(i);
                if (child instanceof BaseVideoView) {
                    this.brightcoveVideoView = (BaseVideoView) child;
                    break;
                }

                this.findBaseVideoView(child);
            }
        }

    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        this.initializeLifecycleUtil(view);
    }

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
        View contentView = this.findViewById(R.id.videoplayer_relativelayout);
        this.initializeLifecycleUtil(contentView);
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        super.setContentView(view, params);
        this.initializeLifecycleUtil(view);
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart");
        super.onStart();
        if (this.lifecycleUtil != null) {
            this.lifecycleUtil.activityOnStart();
        } else {
            new Handler().postDelayed(() -> {
                if (WatchBackBrightcovePlayer.this.lifecycleUtil != null) {
                    WatchBackBrightcovePlayer.this.lifecycleUtil.activityOnStart();
                }
            }, 150);
        }
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause");
        super.onPause();
        if (this.lifecycleUtil != null) {
            this.lifecycleUtil.activityOnPause();
        } else {
            new Handler().postDelayed(() -> {
                if (WatchBackBrightcovePlayer.this.lifecycleUtil != null) {
                    WatchBackBrightcovePlayer.this.lifecycleUtil.activityOnPause();
                }
            }, 150);
        }
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
        if (this.brightcoveVideoView != null) {
            this.brightcoveVideoView.getEventEmitter().on("changeOrientation", new EventListener() {
                public void processEvent(Event event) {
                    int orientation = event.getIntegerProperty("requestedOrientation");
                    WatchBackBrightcovePlayer.this.setRequestedOrientation(orientation);
                }
            });
        }
        if (this.lifecycleUtil != null) {
            this.lifecycleUtil.activityOnResume();
        } else {
            new Handler().postDelayed(() -> {
                if (WatchBackBrightcovePlayer.this.lifecycleUtil != null) {
                    WatchBackBrightcovePlayer.this.lifecycleUtil.activityOnResume();
                }
            }, 150);
        }
    }

    @Override
    protected void onRestart() {
        Log.v(TAG, "onRestart");
        super.onRestart();
        if (this.lifecycleUtil != null) {
            this.lifecycleUtil.onRestart();
        } else {
            new Handler().postDelayed(() -> {
                if (WatchBackBrightcovePlayer.this.lifecycleUtil != null) {
                    WatchBackBrightcovePlayer.this.lifecycleUtil.onRestart();
                }
            }, 150);
        }
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy");
        super.onDestroy();
        if (this.brightcoveVideoView != null) {
            this.brightcoveVideoView.getEventEmitter().on("activityDestroyed", new EventListener() {
                public void processEvent(Event event) {
                    if (WatchBackBrightcovePlayer.this.eventLogger != null) {
                        WatchBackBrightcovePlayer.this.eventLogger.stop();
                    }
                }
            });
        } else if (WatchBackBrightcovePlayer.this.eventLogger != null) {
            WatchBackBrightcovePlayer.this.eventLogger.stop();
        }

        if (this.lifecycleUtil != null) {
            this.lifecycleUtil.activityOnDestroy();
        } else {
            new Handler().postDelayed(() -> {
                if (WatchBackBrightcovePlayer.this.lifecycleUtil != null) {
                    WatchBackBrightcovePlayer.this.lifecycleUtil.activityOnDestroy();
                }
            }, 150);
        }
        PictureInPictureManager.getInstance().unregisterActivity(this);
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "onStop");
        super.onStop();
        if (this.lifecycleUtil != null) {
            this.lifecycleUtil.activityOnStop();
        } else {
            new Handler().postDelayed(() -> {
                if (WatchBackBrightcovePlayer.this.lifecycleUtil != null) {
                    WatchBackBrightcovePlayer.this.lifecycleUtil.activityOnStop();
                }
            }, 150);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle bundle) {
        if (this.brightcoveVideoView != null) {
            this.brightcoveVideoView.getEventEmitter().on("activitySaveInstanceState",
                    new EventListener() {
                        @Default
                        public void processEvent(Event event) {
                            WatchBackBrightcovePlayer.super.onSaveInstanceState(bundle);
                        }
                    });
        } else {
            super.onSaveInstanceState(bundle);
        }
        if (this.lifecycleUtil != null) {
            this.lifecycleUtil.activityOnSaveInstanceState(bundle);
        } else {
            new Handler().postDelayed(() -> {
                if (WatchBackBrightcovePlayer.this.lifecycleUtil != null) {
                    WatchBackBrightcovePlayer.this.lifecycleUtil.activityOnSaveInstanceState(bundle);
                }
            }, 150);
        }
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (this.pictureInPictureEnabled) {
            PictureInPictureManager.getInstance().onUserLeaveHint();
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode,
            Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        PictureInPictureManager.getInstance().onPictureInPictureModeChanged(
                isInPictureInPictureMode, newConfig);
    }
}
