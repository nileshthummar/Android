package com.watchback2.android.helper;

import android.text.TextUtils;

import com.perk.request.auth.AuthenticatedSession;

/**
 * Created by perk on 21/03/18.
 * Check for valid user information
 */

public final class UserInfoValidator {

    private static final int MAX_PASSWORD_LENGTH = 6;

    private static final int MAX_PASSWORD_LENGTH_FOR_SIGNUP = 8;

    /**
     * Added private constructor to stop instantiation of the class
     */
    private UserInfoValidator() {

    }

    /**
     * Will veriify if provided string matches a valid email-pattern
     *
     * @param email the email String to verify
     * @return true if provided String follows a valid email pattern
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email)
                && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Will return true if length of provided 'password' is at least 6 characters (or 8
     * characters for signup)
     *
     * @param password The password String to verify
     */
    public static boolean isValidPassword(String password, boolean forSignUp) {
        return password != null && password.length() >= (forSignUp ? MAX_PASSWORD_LENGTH_FOR_SIGNUP : MAX_PASSWORD_LENGTH);
    }

    /**
     * Will return true if length of provided 'password' is at least 6 characters
     *
     * @param password The password String to verify
     */
    public static boolean isValidPassword(String password) {
        return isValidPassword(password, false);
    }

    /**
     * Will return true if provided 'password' contains at least one lowercase character,
     * one uppercase character & one digit
     *
     * @param password The password String to verify
     */
    public static boolean isValidCharacterPasswordForSignUp(String password) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }

        boolean hasDigit = false;
        boolean hasUpperCaseChar = false;
        boolean hasLowerCaseChar = false;

        final int len = password.length();
        for (int cp, i = 0; i < len; i += Character.charCount(cp)) {
            cp = Character.codePointAt(password, i);
            if (!hasDigit && Character.isDigit(cp)) {
                hasDigit = true;
            } else if (!hasUpperCaseChar && Character.isUpperCase(cp)) {
                hasUpperCaseChar = true;
            } else if (!hasLowerCaseChar && Character.isLowerCase(cp)) {
                hasLowerCaseChar = true;
            } else if (hasDigit && hasLowerCaseChar && hasUpperCaseChar) {
                // No need to continue the loop if we already have got all the requirements
                // fuilfilled
                return true;
            }
        }

        return hasDigit && hasLowerCaseChar && hasUpperCaseChar;
    }

    /**
     * @param authenticatedSession Pass Authenticationed Session from Activity
     * @return true if user is logged in and valid access token is available
     */
    public static boolean isAuthenticated(AuthenticatedSession authenticatedSession) {

        if (authenticatedSession == null) {
            return false;
        }

        String accessToken = authenticatedSession.getAccessToken();
        return accessToken != null && !TextUtils.isEmpty(accessToken);
    }
}
