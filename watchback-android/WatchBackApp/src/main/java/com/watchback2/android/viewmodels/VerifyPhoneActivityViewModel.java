package com.watchback2.android.viewmodels;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableFloat;
import androidx.databinding.ObservableInt;

import com.perk.request.ErrorType;
import com.perk.request.OnRequestFinishedListener;
import com.perk.request.PerkResponse;
import com.perk.request.model.Data;
import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.api.WatchbackAPIController;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.PerkUtils;

public class VerifyPhoneActivityViewModel {

    public static final String LOG_TAG = "VerifyPhoneActivityViewModel";

    private final ObservableFloat continueButtonOpacity = new ObservableFloat(.5f);

    private final ObservableBoolean continueBtnClickable = new ObservableBoolean(false);

    private final ObservableBoolean resendBtnClickable = new ObservableBoolean(true);

    private final ObservableInt progressBarVisibility = new ObservableInt(View.GONE);

    private String mobileNumber;

    private String verificationCode;

    private final IVerifyPhoneNavigator iVerifyPhoneNavigator;

    private boolean resendCallInProgress = false;

    private boolean verficationCallInProgress = false;


    // ---------------------------------------------------------------------------------------------
    // public constructor
    // ---------------------------------------------------------------------------------------------

    public VerifyPhoneActivityViewModel(IVerifyPhoneNavigator iVerifyPhoneNavigator) {
        this.iVerifyPhoneNavigator = iVerifyPhoneNavigator;
    }


    // ---------------------------------------------------------------------------------------------
    // interface for communication from viewmodel to parent activity
    // ---------------------------------------------------------------------------------------------

    public interface IVerifyPhoneNavigator {

        void handleBackClick();

        void handleResendClick();

        void handleContinueClick();

        void onSuccessfulResendPost();

        void onSuccessfulCodeVerification();

        void resendFailed();

        void verificationFailed();
    }

    // ---------------------------------------------------------------------------------------------
    // public methods
    // ---------------------------------------------------------------------------------------------

    public void activateContinueButton() {
        continueBtnClickable.set(true);
        continueButtonOpacity.set(1);
    }

    public void deactivateContinueButton() {
        continueBtnClickable.set(false);
        continueButtonOpacity.set(.5f);
    }

    //-------------- Setters -----------------------------------------------------------------------
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    //-------------- Getters -----------------------------------------------------------------------
    public String getDescriptionText() {
        if (TextUtils.isEmpty(mobileNumber)) {
            return "We just sent a message to your registered number with a code for you to enter here:";
        } else {
            String formatedString = mobileNumber;

            if (mobileNumber.length() >= 10) {
                formatedString = "(" + mobileNumber.substring(0, 3) + ")" + " " + mobileNumber.substring(3, 6) + "-" + mobileNumber.substring(6);
            }
            return "We just sent a message to " + formatedString + " with a code for you to enter here:";
        }
    }

    public ObservableFloat getContinueButtonOpacity() {
        return continueButtonOpacity;
    }

    public ObservableBoolean getContinueBtnClickable() {
        return continueBtnClickable;
    }

    public ObservableBoolean getResendBtnClickable() {
        return resendBtnClickable;
    }

    public ObservableInt getProgressBarVisibility() {
        return progressBarVisibility;
    }

    //-------------- Listeners ---------------------------------------------------------------------
    public void handleResendButtonClick(View view) {
        if (iVerifyPhoneNavigator != null) {
            iVerifyPhoneNavigator.handleResendClick();
        }
    }

    public void handleContinueButtonClick(View view) {
        if (iVerifyPhoneNavigator != null) {
            iVerifyPhoneNavigator.handleContinueClick();
        }
    }

    public void handleBackClick(View view) {
        if (iVerifyPhoneNavigator != null) {
            iVerifyPhoneNavigator.handleBackClick();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // private methods
    // ---------------------------------------------------------------------------------------------

    public void makeResendCall(Context context) {

        if (!AppUtility.isNetworkAvailable(context)) {
            PerkLogger.d(LOG_TAG, "makeResendCall: Returning as network is unavailable!");
            return;
        }

        if (resendCallInProgress) {
            PerkLogger.d(LOG_TAG, "makeResendCall: Call already in progress.");
            return;
        }

        resendCallInProgress = true;
        progressBarVisibility.set(View.VISIBLE);
        WatchbackAPIController.INSTANCE.postRsendVerificationCode(context, new OnRequestFinishedListener<Data>() {
            @Override
            public void onSuccess(@NonNull Data data, @Nullable String s) {
                progressBarVisibility.set(View.GONE);
                resendCallInProgress = false;
                if (iVerifyPhoneNavigator != null) {
                    iVerifyPhoneNavigator.onSuccessfulResendPost();
                }
            }

            @Override
            public void onFailure(@NonNull ErrorType errorType, @Nullable PerkResponse<Data> perkResponse) {
                progressBarVisibility.set(View.GONE);
                resendCallInProgress = false;
                PerkLogger.e(LOG_TAG,
                        "Request for resend failed: " + errorType + (perkResponse != null
                                ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));

                PerkUtils.showErrorMessageToast(errorType,
                        (perkResponse != null ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));

                if (iVerifyPhoneNavigator != null) {
                    iVerifyPhoneNavigator.resendFailed();
                }
            }
        });

    }

    public void makeCodeVerificationCall(Context context, String code) {

        if (!AppUtility.isNetworkAvailable(context)) {
            PerkLogger.d(LOG_TAG, "makeCodeVerificationCall: Returning as network is unavailable!");
            return;
        }

        if (verficationCallInProgress) {
            PerkLogger.d(LOG_TAG, "makeCodeVerificationCall: Call already in progress.");
            return;
        }

        verficationCallInProgress = true;
        progressBarVisibility.set(View.VISIBLE);
        WatchbackAPIController.INSTANCE.postVerificationCode(context, code, new OnRequestFinishedListener<Data>() {
            @Override
            public void onSuccess(@NonNull Data data, @Nullable String s) {
                progressBarVisibility.set(View.GONE);
                verficationCallInProgress = false;
                if (iVerifyPhoneNavigator != null) {
                    iVerifyPhoneNavigator.onSuccessfulCodeVerification();
                }
            }

            @Override
            public void onFailure(@NonNull ErrorType errorType, @Nullable PerkResponse<Data> perkResponse) {
                progressBarVisibility.set(View.GONE);
                verficationCallInProgress = false;
                PerkLogger.e(LOG_TAG,
                        "Posting verification code failed: " + errorType + (perkResponse != null
                                ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));

                PerkUtils.showErrorMessageToast(errorType,
                        (perkResponse != null ? perkResponse.getMessage()
                                : context.getString(R.string.generic_error)));

                if (iVerifyPhoneNavigator != null) {
                    iVerifyPhoneNavigator.verificationFailed();
                }
            }
        });

    }

}
