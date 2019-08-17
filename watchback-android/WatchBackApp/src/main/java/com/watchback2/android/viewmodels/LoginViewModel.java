package com.watchback2.android.viewmodels;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.watchback2.android.helper.UserInfoValidator;
import com.watchback2.android.navigators.ILoginNavigator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

/**
 * Created by perk on 21/03/18.
 */

public class LoginViewModel {

    public static final int RED = Color.parseColor("#EC415A");
    public static final int WHITE = Color.parseColor("#FFFFFF");
    public static final int STEEL = Color.parseColor("#788995");

    @Nullable
    private ILoginNavigator mNavigator;

    private final ObservableField<String> email = new ObservableField<>();

    private final ObservableField<String> password = new ObservableField<>();

    private final ObservableBoolean enableLoginButton = new ObservableBoolean(false);

    private final ObservableBoolean visibleErrorText = new ObservableBoolean(false);

    private final ObservableInt passwordTextColor = new ObservableInt(WHITE);

    private final ObservableInt emailTextColor = new ObservableInt(WHITE);

    private final Context mAppContext;

    public LoginViewModel(@NonNull Context context) {
        mAppContext = context.getApplicationContext();
        setNormalEmailTextColor();
        setNormalPasswordTextColor();
    }

    public static int getThemeTextColor() {
        //return PerkPreferencesManager.INSTANCE.isNightMode() ? WHITE : BLACK;
        // return WHITE always since we are temporarily not supporting day mode for Login/Signup
        return WHITE;
    }

    private void setNormalEmailTextColor() {
        getEmailTextColor().set(TextUtils.isEmpty(getEmail().get()) ? LoginViewModel.STEEL
                : LoginViewModel.getThemeTextColor());
    }

    private void setNormalPasswordTextColor() {
        getPasswordTextColor().set(TextUtils.isEmpty(getPassword().get()) ? LoginViewModel.STEEL
                : LoginViewModel.getThemeTextColor());
    }

    public void setNavigator(@NonNull ILoginNavigator navigator) {
        mNavigator = navigator;
    }

    public void handleLoginClick(View view) {
        manageLogin();
    }

    public void handleBackClick(View view) {
        if (mNavigator != null) {
            mNavigator.onBackClick();
        }
    }

    public void handleForgotPasswordClick(View view) {
        if (mNavigator != null) {
            mNavigator.onForgotPasswordClick();
        }
    }

    public void handleSignupClick(View view) {
        if (mNavigator != null) {
            mNavigator.onCallSignupClick();
        }
    }

    public void handleEmailTextChanged(Editable s) {
        setNormalEmailTextColor();
    }

    public void handlePasswordTextChanged(Editable s) {
        setNormalPasswordTextColor();
    }

    public boolean handleEditorAction(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            manageLogin();
        }
        return false;
    }

    private void manageLogin() {
        if (UserInfoValidator.isValidEmail(getEmail().get())
                && UserInfoValidator.isValidPassword(getPassword().get())) {

            if (mNavigator != null) {
                mNavigator.onLoginClick();
            }
        } else {
            if (!UserInfoValidator.isValidEmail(getEmail().get())
                    && !UserInfoValidator.isValidPassword(getPassword().get())) {
                showToast("Invalid email and/or password");
                getPasswordTextColor().set(RED);
                getEmailTextColor().set(RED);

            } else if (!UserInfoValidator.isValidEmail(getEmail().get())) {
                showToast("Invalid email");
                setNormalPasswordTextColor();
                getEmailTextColor().set(RED);
            } else {
                showToast("Invalid password length");
                getPasswordTextColor().set(RED);
                setNormalEmailTextColor();
            }

        }
    }

    private void showToast(String message) {
        Toast.makeText(mAppContext, message, Toast.LENGTH_SHORT).show();
    }

    public ObservableBoolean getEnableLoginButton() {
        return enableLoginButton;
    }

    public ObservableField<String> getEmail() {
        return email;
    }

    public ObservableField<String> getPassword() {
        return password;
    }

    public ObservableBoolean getVisibleErrorText() {
        return visibleErrorText;
    }

    public ObservableInt getEmailTextColor() {
        return emailTextColor;
    }

    public ObservableInt getPasswordTextColor() {
        return passwordTextColor;
    }

}
