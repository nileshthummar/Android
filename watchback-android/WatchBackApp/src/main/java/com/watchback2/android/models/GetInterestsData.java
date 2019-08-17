package com.watchback2.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.request.model.Data;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class GetInterestsData extends Data {

    @SerializedName("interests")
    @Expose
    private List<Interest> interests = null;
    private final static long serialVersionUID = 1L;

    public List<Interest> getInterests() {
        return interests;
    }

    public void setInterests(List<Interest> interests) {
        this.interests = interests;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("interests", interests).toString();
    }
}
