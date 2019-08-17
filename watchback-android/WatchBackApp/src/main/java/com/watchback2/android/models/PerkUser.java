package com.watchback2.android.models;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.request.model.Data;

public class PerkUser extends Data {
    @Expose
    @SerializedName("user_id")
    private long mUserId;
    @Expose
    @SerializedName("created_at")
    private String mCreatedAt;
    @Expose
    @SerializedName("suspended")
    private boolean mSuspended;
    @Expose
    @SerializedName("birth_date")
    private String mBirthDate;
    @Expose
    @SerializedName("gender")
    private String mGender;
    @Expose
    @SerializedName("phone_verified_at")
    private String mPhoneVerifiedAt;
    @Expose
    @SerializedName("email_verified_at")
    private String mEmailVerifiedAt;
    @Expose
    @SerializedName("email")
    private String mEmail;
    @Expose
    @SerializedName("last_name")
    private String mLastName;
    @Expose
    @SerializedName("first_name")
    private String mFirstName;
    @Expose
    @SerializedName("phone")
    private String mPhone;
    @Expose
    @SerializedName("uuid")
    private String mUuid;

    public long getUserId() {
        return mUserId;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public boolean getSuspended() {
        return mSuspended;
    }

    public String getBirthDate() {
        return mBirthDate;
    }

    public String getGender() {
        return mGender;
    }

    public String getPhoneVerifiedAt() {
        return mPhoneVerifiedAt;
    }

    public String getEmailVerifiedAt() {
        return mEmailVerifiedAt;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getLastName() {
        return mLastName;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getPhone() {
        return mPhone;
    }

    public String getUuid() {
        return mUuid;
    }

    public boolean isEmailConfirmed(){
        return !TextUtils.isEmpty(getEmailVerifiedAt());
    }

    public boolean isPhoneVerified(){
        return !TextUtils.isEmpty(getPhoneVerifiedAt());
    }

}
