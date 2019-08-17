package com.watchback2.android.models;

import com.google.gson.annotations.SerializedName;
import com.watchback2.android.utils.AppUtility;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

/**
 * Created by perk on 22/03/18.
 * Model contains user information
 */

public class User extends BaseObservable {
    @SerializedName("u_id")
    private int uId;
    @SerializedName("uuid")
    private String uuid;
    @SerializedName("u_email")
    private String uEmail;
    @SerializedName("email_confirmed")
    private boolean emailConfirmed;
    @SerializedName("u_country")
    private String uCountry;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("facebook_user_id")
    private long facebookUserId;
    @SerializedName("fb_email")
    private String fbEmail;
    @SerializedName("fb_like")
    private boolean fbLike;
    @SerializedName("fb_connect")
    private boolean fbConnect;
    @SerializedName("last_login_ua")
    private String lastLoginUa;
    @SerializedName("referral_id")
    private String referralId;
    @SerializedName("available_perks")
    private long availablePerks;
    @SerializedName("available_tokens")
    private long availableTokens;
    @SerializedName("pending_perks")
    private int pendingPerks;
    @SerializedName("lifetime_perks")
    private int lifetimePerks;
    @SerializedName("search_perks")
    private int searchPerks;
    @SerializedName("shopping_perks")
    private int shoppingPerks;
    @SerializedName("misc_perks")
    private int miscPerks;
    @SerializedName("redeemed_perks")
    private int redeemedPerks;
    @SerializedName("cancelled_perks")
    private int cancelledPerks;
    @SerializedName("21_and_over")
    private boolean m21AndOver;
    @SerializedName("user_name")
    private String userName;
    @SerializedName("profile_image")
    private String profileImage;
    @SerializedName("signed_up")
    private String signedUp;
    @SerializedName("gender")
    private String gender;
    @SerializedName("birthday")
    private String birthday;
    @SerializedName("winback_awarded")
    private boolean winbackAwarded;
    @SerializedName("plastik")
    private String plastik;

    public int getUId() {
        return uId;
    }

    public String getUuid() {
        return uuid;
    }

    public String getUEmail() {
        return uEmail;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    /* default */ String getUCountry() {
        return uCountry;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    /* default */ long getFacebookUserId() {
        return facebookUserId;
    }

    /* default */ String getFbEmail() {
        return fbEmail;
    }

    /* default */ boolean isFbLike() {
        return fbLike;
    }

    public boolean isFbConnect() {
        return fbConnect;
    }

    /* default */ String getLastLoginUa() {
        return lastLoginUa;
    }

    public String getReferralId() {
        return referralId;
    }

    @Bindable
    public String getAvailablePerks() {
        return AppUtility.getFormattedPoints(availablePerks);
    }

    @Bindable
    public String getAvailableTokens() {
        return AppUtility.getFormattedPoints(availableTokens);
    }

    /* default */ int getPendingPerks() {
        return pendingPerks;
    }

    /* default */ int getLifetimePerks() {
        return lifetimePerks;
    }

    /* default */ int getSearchPerks() {
        return searchPerks;
    }

    /* default */ int getShoppingPerks() {
        return shoppingPerks;
    }

    /* default */ int getMiscPerks() {
        return miscPerks;
    }

    public int getRedeemedPerks() {
        return redeemedPerks;
    }

    /* default */ int getCancelledPerks() {
        return cancelledPerks;
    }

    /* default */ boolean isM21AndOver() {
        return m21AndOver;
    }

    /* default */ String getUserName() {
        return userName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    /* default */ String getSignedUp() {
        return signedUp;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthday() {
        return birthday;
    }

    /* default */ boolean isWinbackAwarded() {
        return winbackAwarded;
    }

    /* default */ String getPlastik() {
        return plastik;
    }

    /*public void setAvailablePerks(long availablePerks) {
        this.availablePerks = AppUtility.getFormattedPoints(availablePerks);
        notifyPropertyChanged(BR.availablePerks);
    }

    public void setAvailableTokens(long availableTokens) {
        this.availableTokens = AppUtility.getFormattedPoints(availableTokens);
        notifyPropertyChanged(BR.availableTokens);
    }*/

}
