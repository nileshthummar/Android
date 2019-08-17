package com.watchback2.android.models;

import androidx.annotation.NonNull;

public enum Gender {
    MALE("m"),
    FEMALE("f"),
    UNKNOWN("u");

    @NonNull
    public final String genderType;

    private Gender(String var3) {
        this.genderType = var3;
    }
}
