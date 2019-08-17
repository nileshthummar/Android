package com.watchback2.android.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.request.model.Data;

/**
 * Created by Nilesh on 11/25/16.
 */

public class PerkAppVersion extends Data {

    private static final long serialVersionUID = 1L;
    @SerializedName("update")
    @Expose
    private Boolean update;
    @SerializedName("force_update")
    @Expose
    private Boolean forceUpdate;
    @SerializedName("latest_version")
    @Expose
    private String latestVersion;
    @SerializedName("minimum_version")
    @Expose
    private String minimumVersion;
    @SerializedName("url")
    @Expose
    private String url;

    /**
     *
     * @return
     *     The update
     */
    public Boolean getUpdate() {
        return update;
    }

    /**
     *
     * @param update
     *     The update
     */
    public void setUpdate(Boolean update) {
        this.update = update;
    }

    /**
     *
     * @return
     *     The forceUpdate
     */
    public Boolean getForceUpdate() {
        return forceUpdate;
    }

    /**
     *
     * @param forceUpdate
     *     The force_update
     */
    public void setForceUpdate(Boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    /**
     *
     * @return
     *     The latestVersion
     */
    public String getLatestVersion() {
        return latestVersion;
    }

    /**
     *
     * @param latestVersion
     *     The latest_version
     */
    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    /**
     *
     * @return
     *     The minimumVersion
     */
    public String getMinimumVersion() {
        return minimumVersion;
    }

    /**
     *
     * @param minimumVersion
     *     The minimum_version
     */
    public void setMinimumVersion(String minimumVersion) {
        this.minimumVersion = minimumVersion;
    }

    /**
     *
     * @return
     *     The url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     *     The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

}
