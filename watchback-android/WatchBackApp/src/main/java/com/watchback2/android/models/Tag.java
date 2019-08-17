package com.watchback2.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.util.PerkLogger;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class Tag implements Serializable {

    @SerializedName("tag")
    @Expose
    private String tag;
    private final static long serialVersionUID = 1L;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Returning just the tag for with TextUtils.join
     * @return
     */
    @Override
    public String toString() {
        // Just for logging purposes
        PerkLogger.d(getClass().getSimpleName(), new ToStringBuilder(this).append("tag",
                tag).toString());

        return getTag();
    }

}