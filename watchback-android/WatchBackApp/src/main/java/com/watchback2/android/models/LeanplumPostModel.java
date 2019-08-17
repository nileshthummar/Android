package com.watchback2.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;

public class LeanplumPostModel implements Serializable {

    @SerializedName("response")
    @Expose
    private List<Response> response = null;
    private final static long serialVersionUID = 1L;

    public List<Response> getResponse() {
        return response;
    }

    public void setResponse(List<Response> response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("response", response).toString();
    }

}