package com.watchback2.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class Response implements Serializable {

    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("warning")
    @Expose
    private Warning warning;
    @SerializedName("error")
    @Expose
    private Warning error;
    private final static long serialVersionUID = 1L;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Warning getWarning() {
        return warning;
    }

    public void setWarning(Warning warning) {
        this.warning = warning;
    }

    public Warning getError() {
        return error;
    }

    public void setError(Warning error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("success", success).append("warning",
                warning).append("error", error).toString();
    }

}