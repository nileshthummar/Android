package com.watchback2.android.viewmodels;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.api.CarouselData;
import com.watchback2.android.api.WatchbackAPIController;
import com.watchback2.android.navigators.IWalkthroughNavigator;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkUtils;

/**
 * Created by perk on 21/03/18.
 * Parent Class for UnregisteredUserViewModel
 */

public class WalkthroughViewModel {

    private static final String LOG_TAG = "WalkthroughViewModel";

    // placement "Sign Up Screen" -for Background image on old Walkthrough UI
    private static final String WALKTHROUGH_PLACEMENT = "Splash Screen";

    private final ObservableBoolean allowSettings = new ObservableBoolean(false);

    private final ObservableBoolean dataLoading = new ObservableBoolean(false);

    private ObservableField<String> logoURl = new ObservableField<>();
    private ObservableInt logoVisbility = new ObservableInt(View.GONE);

    private ObservableField<String> backgroundURl = new ObservableField<>();
    private ObservableField<String> descriptionTxt = new ObservableField<>();
    private ObservableInt backgroundVisibility = new ObservableInt(View.GONE);

    @Nullable
    private IWalkthroughNavigator mNavigator;

    public void setNavigator(@NonNull IWalkthroughNavigator navigator) {
        mNavigator = navigator;
    }

    public void loadWalkthrough(@NonNull final Context context) {
        if (getDataLoading().get()) {
            return;
        }

        if (!AppUtility.isNetworkAvailable(context)) {
            PerkLogger.e(LOG_TAG, "Unable to proceed due to null context OR no-network");
            return;
        }

        getDataLoading().set(true);

        PerkLogger.d(LOG_TAG,
                "Loading walkthrough data for (placement=" + WALKTHROUGH_PLACEMENT + ")...");

        WatchbackAPIController.INSTANCE.getCarousel(context, WALKTHROUGH_PLACEMENT,
                new OnRequestFinishedListener<CarouselData>() {

                    @Override
                    public void onSuccess(@NonNull CarouselData carouselData, @Nullable String s) {

                        PerkLogger.d(LOG_TAG,
                                "Loading walkthrough data for placement: " + WALKTHROUGH_PLACEMENT
                                        + " successful!");

                        getDataLoading().set(false);

                        if (carouselData != null && carouselData.getCarousel().getItems() != null
                                && !carouselData.getCarousel().getItems().isEmpty()) {

                            PerkLogger.d(LOG_TAG, "Walkthrough Items size: "
                                    + carouselData.getCarousel().getItems().size());

                            final String logoImage = carouselData.getCarousel().getItems().get(0).getImage();
                            final String bgImage = carouselData.getCarousel().getItems().get(0).getImage2();
                            final String desTxt = carouselData.getCarousel().getItems().get(0).getFields().getTxt1();

                            if (!TextUtils.isEmpty(logoImage)) {
                                logoURl.set(logoImage);
                                logoVisbility.set(View.VISIBLE);
                            } else {
                                PerkLogger.d(LOG_TAG, "Walkthrough logo url is empty");
                            }

                            if (!TextUtils.isEmpty(bgImage)) {
                                backgroundURl.set(bgImage);
                                backgroundVisibility.set(View.VISIBLE);
                            } else {
                                PerkLogger.d(LOG_TAG, "Walkthrough background image url is empty ");
                            }

                            if(!TextUtils.isEmpty(desTxt)){
                                descriptionTxt.set(desTxt);
                            }

                        }else{
                            PerkLogger.e(LOG_TAG,
                                    "Carousel data invalid/unavailable! Walkthrough could "
                                            + "not be loaded");

                        }
                    }

                    @Override
                    public void onFailure(@NonNull ErrorType errorType,
                            @Nullable PerkResponse<CarouselData> perkResponse) {

                        PerkLogger.e(LOG_TAG,
                                "Loading Walkthrough data for placement: " + WALKTHROUGH_PLACEMENT
                                        + " failed! " + (perkResponse != null
                                        ? perkResponse.getMessage() : ""));

                        getDataLoading().set(false);

                        PerkUtils.showErrorMessageToast(errorType,
                                (perkResponse != null ? perkResponse.getMessage()
                                        : context.getString(R.string.generic_error)));
                    }
                });
    }

    public void handleSkipClick(View view) {
        if (mNavigator != null) {
            mNavigator.handleSkipClick();
        }
    }

    public void handleSignUpClick(View view) {
        if (mNavigator != null) {
            mNavigator.handleSignUpClick();
        }
    }

    public void handleLogInClick(View view) {
        if (mNavigator != null) {
            mNavigator.handleLogInClick();
        }
    }

    public ObservableBoolean getAllowSettings() {
        return allowSettings;
    }

    public ObservableBoolean getDataLoading() {
        return dataLoading;
    }

    public ObservableField<String> getLogoURl() {
        return logoURl;
    }

    public ObservableField<String> getBackgroundURl() {
        return backgroundURl;
    }

    public ObservableInt getLogoVisbility() {
        return logoVisbility;
    }

    public ObservableInt getBackgroundVisibility() {
        return backgroundVisibility;
    }

    public ObservableField<String> getDescriptionTxt() {
        return descriptionTxt;
    }
}
