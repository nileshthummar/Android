package com.watchback2.android.activities;

import androidx.databinding.Observable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.utils.PerkPreferencesManager;

/**
 * Created by perk on 05/04/18.
 */
public abstract class BaseThemeableActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @SuppressWarnings({"PMD.AccessorMethodGeneration"})
    private final String LOG_TAG = getClass().getSimpleName();

    /* default */ boolean mThemeUpdated;

    /* default */ boolean isResumed;

    private final Observable.OnPropertyChangedCallback mThemeChangedCallback =
            new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    PerkLogger.d(LOG_TAG,
                            "onPropertyChanged(): theme changed! Night-mode: " + isNightModeTheme()
                                    + " resumed: " + isResumed);
                    if (isResumed) {
                        PerkLogger.d(LOG_TAG, "onPropertyChanged(): Recreating UI!");
                        recreate();
                    } else {
                        mThemeUpdated = true;
                    }
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mThemeUpdated = false;

        PerkPreferencesManager.INSTANCE.getNightMode().addOnPropertyChangedCallback(
                mThemeChangedCallback);

        setAppropriateTheme();

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;

        if (mThemeUpdated) {
            PerkLogger.d(LOG_TAG, "onResume(): mThemeUpdated is true! Recreating UI!");
            mThemeUpdated = false;
            recreate();
        }
    }

    @Override
    protected void onPause() {
        isResumed = false;
        super.onPause();
    }

    @Override
    public void recreate() {
        // Remove callback on re-creation since we are adding it once Activity is created...
        // otherwise we have multiple callbacks being added, even though we need just 1 per activity
        PerkPreferencesManager.INSTANCE.getNightMode().removeOnPropertyChangedCallback(
                mThemeChangedCallback);
        super.recreate();
    }

    @Override
    public void finish() {
        PerkPreferencesManager.INSTANCE.getNightMode().removeOnPropertyChangedCallback(
                mThemeChangedCallback);
        super.finish();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).onTrimMemory(level);
    }

    protected boolean isNightModeTheme() {
        return PerkPreferencesManager.INSTANCE.isNightMode();
    }

    protected void setAppropriateTheme() {
        if (isNightModeTheme()) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppThemeLight);
        }
    }

}
