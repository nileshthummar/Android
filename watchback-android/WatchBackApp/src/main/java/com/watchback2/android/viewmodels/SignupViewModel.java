package com.watchback2.android.viewmodels;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableFloat;
import androidx.databinding.ObservableInt;

import com.perk.util.PerkLogger;
import com.watchback2.android.R;
import com.watchback2.android.helper.UserInfoValidator;
import com.watchback2.android.navigators.ISignupNavigator;
import com.watchback2.android.utils.AppUtility;
import com.watchback2.android.utils.DateTimeUtil;
import com.watchback2.android.utils.PerkPreferencesManager;

import java.util.Date;

/**
 * ViewModel for SignupActivity, parent to TermsGateViewModel
 */
public class SignupViewModel {

    private static final String LOG_TAG = "SignupViewModel";

    private final ObservableField<String> email = new ObservableField<>("");

    private final ObservableField<String> password = new ObservableField<>("");

    private final ObservableField<String> passwordConfirm = new ObservableField<>("");

    private final ObservableBoolean visibleErrorText = new ObservableBoolean(false);

    private final ObservableBoolean highlightPasswordError = new ObservableBoolean(false);

    private final ObservableField<String> doBText = new ObservableField<>("");

    private final ObservableField<String> genderText = new ObservableField<>("");

    private final ObservableInt passwordTextColor = new ObservableInt(LoginViewModel.WHITE);

    private final ObservableInt passwordConfirmationTextColor = new ObservableInt(LoginViewModel.WHITE);

    private final ObservableInt emailTextColor = new ObservableInt(LoginViewModel.WHITE);

    private final ObservableInt doBTextColor = new ObservableInt(LoginViewModel.WHITE);

    private final ObservableInt genderTextColor = new ObservableInt(LoginViewModel.WHITE);

    private final ObservableFloat continueButtonOpacity = new ObservableFloat(.5f);

    private final ObservableBoolean continueBtnClickable = new ObservableBoolean(false);

    private String mobileNumber;

    private ISignupNavigator mNavigator;

    public SignupViewModel() {
        setNormalEmailTextColor();
        setNormalPasswordTextColor();
        setNormalDoBTextColor();
        setNormalGenderTextColor();
    }

    private void setNormalEmailTextColor() {
        getEmailTextColor().set(TextUtils.isEmpty(getEmail().get()) ? LoginViewModel.STEEL
                : LoginViewModel.getThemeTextColor());
    }

    private void setNormalPasswordTextColor() {
        getPasswordTextColor().set(TextUtils.isEmpty(getPassword().get()) ? LoginViewModel.STEEL
                : LoginViewModel.getThemeTextColor());

        getPasswordConfirmationTextColor().set(
                TextUtils.isEmpty(getPasswordConfirm().get()) ? LoginViewModel.STEEL
                        : LoginViewModel.getThemeTextColor());
    }

    private void setNormalDoBTextColor() {
        getDoBTextColor().set(TextUtils.isEmpty(getDoBText().get()) ? LoginViewModel.STEEL
                : LoginViewModel.getThemeTextColor());
    }

    private void setNormalGenderTextColor() {
        getGenderTextColor().set(TextUtils.isEmpty(getGenderText().get()) ? LoginViewModel.STEEL
                : LoginViewModel.getThemeTextColor());
    }

    /*
    connecting with view via Interface
     */
    public void setNavigator(ISignupNavigator signupNavigator) {
        mNavigator = signupNavigator;
    }

    public void handleBackClick(View view) {
        if (mNavigator != null) {
            mNavigator.onBackClick();
        }
    }

    public void handleSignUpClick(View view) {
        trySignUp();
    }

    public void handleLoginClick(View view) {
        if (mNavigator != null) {
            mNavigator.onCallLoginClick();
        }
    }

    public void handleDobClick(View view) {
        if (mNavigator != null) {
            mNavigator.onDobClick();
        }
    }

    public void handleGenderClick(View view) {
        if (mNavigator != null) {
            mNavigator.onGenderClick();
        }
    }

    public void handleContinueClick(View view) {
        PerkLogger.d(LOG_TAG, "handleContinueClick");
        if (mNavigator != null) {
            mNavigator.onContinueClick();
        }
    }

    public void handleMobileNumberTextChanged(CharSequence number, int start, int before, int isInput) {
        PerkLogger.d(LOG_TAG, number.toString());

        if (number.length() >= 10) {
            continueBtnClickable.set(true);
            continueButtonOpacity.set(1);
            mobileNumber = number.toString();

        } else if (continueBtnClickable.get()) {
            continueBtnClickable.set(false);
            continueButtonOpacity.set(0.5f);
        }
    }

    public void handleEmailTextChanged(Editable s) {
        setNormalEmailTextColor();
    }

    public void handlePasswordTextChanged(Editable s) {
        onPasswordValuesModified();
    }

    public void handlePasswordConfirmationTextChanged(Editable s) {
        onPasswordValuesModified();
    }

    private void onPasswordValuesModified() {
        setNormalPasswordTextColor();
        getVisibleErrorText().set(false);
        getHighlightPasswordError().set(false);

        // Try to match the passwords if length is same for both
        /*if (getPassword().get().length() == getPasswordConfirm().get().length()) {
            arePasswordsMatching(true);
        }*/

        if (UserInfoValidator.isValidPassword(getPassword().get(), true)) {
            PerkLogger.d(LOG_TAG, "Password length valid!");

            if (!UserInfoValidator.isValidCharacterPasswordForSignUp(getPassword().get())) {
                PerkLogger.d(LOG_TAG, "Password character requirements not met!");
                getHighlightPasswordError().set(true);
            }

            // Also see if the passwords match:
            arePasswordsMatching(true);
        }
    }

    public void handleDoBTextChanged(Editable s) {
        setNormalDoBTextColor();
    }

    public void handleGenderChanged(Editable s) {
        setNormalGenderTextColor();
    }

    /* package */ void doSignUp() {
        if (mNavigator != null) {
            mNavigator.onSignupClick();
        }
    }

    private void trySignUp() {
        if (UserInfoValidator.isValidEmail(getEmail().get())
                && UserInfoValidator.isValidPassword(getPassword().get(), true)
                && UserInfoValidator.isValidCharacterPasswordForSignUp(getPassword().get())
                && arePasswordsMatching(false)
                && !TextUtils.isEmpty(getDoBText().get())) {

            // Everything is fine... we can proceed:
            doSignUp();
        } else {
            if (!UserInfoValidator.isValidEmail(getEmail().get())) {
                getEmailTextColor().set(LoginViewModel.RED);
            }

            if (!UserInfoValidator.isValidPassword(getPassword().get(), true)) {
                getPasswordTextColor().set(LoginViewModel.RED);
                getPasswordConfirmationTextColor().set(LoginViewModel.RED);
            }

            if (!UserInfoValidator.isValidCharacterPasswordForSignUp(getPassword().get())) {
                getPasswordTextColor().set(LoginViewModel.RED);
                getPasswordConfirmationTextColor().set(LoginViewModel.RED);
            }

            if (!arePasswordsMatching(true)) {
                getPasswordTextColor().set(LoginViewModel.RED);
                getPasswordConfirmationTextColor().set(LoginViewModel.RED);
            }

            if (TextUtils.isEmpty(getDoBText().get())) {
                getDoBTextColor().set(LoginViewModel.RED);
            }

           /* if (TextUtils.isEmpty(getGenderText().get())) {
                getGenderTextColor().set(LoginViewModel.RED);
            }*/
        }
    }

    private boolean arePasswordsMatching(boolean updateError) {
        boolean isValidPassword = TextUtils.equals(getPassword().get(), getPasswordConfirm().get());

        PerkLogger.d(LOG_TAG, "arePasswordsMatching: " + isValidPassword);

        if (updateError) {
            getVisibleErrorText().set(!isValidPassword && !TextUtils.isEmpty(getPasswordConfirm().get()));
        }

        return isValidPassword;
    }

    public ObservableField<String> getEmail() {
        return email;
    }

    public ObservableField<String> getPassword() {
        return password;
    }

    public ObservableField<String> getPasswordConfirm() {
        return passwordConfirm;
    }

    public ObservableField<String> getDoBText() {
        return doBText;
    }

    public ObservableField<String> getGenderText() {
        return genderText;
    }

    public ObservableBoolean getVisibleErrorText() {
        return visibleErrorText;
    }

    public ObservableBoolean getHighlightPasswordError() {
        return highlightPasswordError;
    }

    // Used for setting drawable-left via data-Binding since we cannot currently get the
    // drawable's integer resource value via xml
    public int getDropDownIconResourceId() {
        return R.drawable.ic_arrow_drop_down_white;
    }

    public static boolean isValidDateOfBirth(@NonNull final Activity activity, final Date dateOfBirth) {
        return isValidDateOfBirth(activity, dateOfBirth, null);
    }

    public static boolean isValidDateOfBirth(@NonNull final Activity activity,
                                             final Date dateOfBirth, @Nullable final DialogInterface.OnClickListener listener) {
        // Make sure user has not selected today's date
        if (DateTimeUtil.isToday(dateOfBirth)) {
            AppUtility.showGenericDialog(activity,
                    activity.getResources().getString(R.string.signup_validation_born_today),
                    (dialog, which) -> {
                        AppUtility.dismissDialog(dialog);

                        if (listener != null) {
                            listener.onClick(dialog, which);
                        }
                    });
            return false;

            // Make sure user has not selected date in future
        } else if (DateTimeUtil.isInFuture(dateOfBirth)) {
            AppUtility.showGenericDialog(activity,
                    activity.getResources().getString(R.string.signup_validation_born_future),
                    (dialog, which) -> {
                        AppUtility.dismissDialog(dialog);

                        if (listener != null) {
                            listener.onClick(dialog, which);
                        }
                    });
            return false;

            // Make sure user is not younger than 18 years
        } else if (!DateTimeUtil.isOlderThan(18, dateOfBirth)) {
            // NOTE: We intentionally disallow user from using the app for 24 hours under any
            //       conditions if they have put their age under 18. This is intentional even if the
            //       user has entered the wrong age by mistake. In order to reuse the application
            //       either the user needs to wait for 24 hours OR uninstall and re-install the app.

            disallowUserFromApp();
            AppUtility.showGenericDialog(activity,
                    activity.getResources().getString(R.string.signup_validation_under_13_years_old),
                    (dialog, which) -> {
                        AppUtility.dismissDialog(dialog);
                        if (activity != null) {
                            activity.finish();
                        }

                        if (listener != null) {
                            listener.onClick(dialog, which);
                        }
                    });
            return false;
        }

        return true;
    }

    public static void disallowUserFromApp() {
        PerkPreferencesManager.INSTANCE.putInvalidDOBRegistrationAttemptInPreference();
        PerkPreferencesManager.INSTANCE.putUserAllowedToSkipTermsGateIntoPreference(false);
    }

    public ObservableInt getEmailTextColor() {
        return emailTextColor;
    }

    public ObservableInt getPasswordTextColor() {
        return passwordTextColor;
    }

    public ObservableInt getPasswordConfirmationTextColor() {
        return passwordConfirmationTextColor;
    }

    public ObservableInt getDoBTextColor() {
        return doBTextColor;
    }

    public ObservableInt getGenderTextColor() {
        return genderTextColor;
    }

    public ObservableFloat getContinueButtonOpacity() {
        return continueButtonOpacity;
    }

    public ObservableBoolean getContinueBtnClickable() {
        return continueBtnClickable;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }
}
