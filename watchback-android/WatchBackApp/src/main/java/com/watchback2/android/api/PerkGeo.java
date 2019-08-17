package com.watchback2.android.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.request.model.Data;

import java.io.Serializable;

/**
 * Created by Nilesh on 11/25/16.
 */

public class PerkGeo extends Data {

    private static final long serialVersionUID = -3359955046518196751L;
    @SerializedName("geo")
    @Expose
    private Geo geo;

    /**
     *
     * @return
     *     The geo
     */
    public Geo getGeo() {
        return geo;
    }

    /**
     *
     * @param geo
     *     The geo
     */
    public void setGeo(Geo geo) {
        this.geo = geo;
    }
    public class Geo implements Serializable {

        private static final long serialVersionUID = 3443859621681691310L;
        @SerializedName("ip")
        @Expose
        private String ip;
        @SerializedName("country")
        @Expose
        private String country;
        @SerializedName("enable_live_tv")
        @Expose
        private Boolean enableLiveTv;
        @SerializedName("debug")
        @Expose
        private Boolean debug;

        /**
         *
         * @return
         *     The ip
         */
        public String getIp() {
            return ip;
        }

        /**
         *
         * @param ip
         *     The ip
         */
        public void setIp(String ip) {
            this.ip = ip;
        }

        /**
         *
         * @return
         *     The country
         */
        public String getCountry() {
            return country;
        }

        /**
         *
         * @param country
         *     The country
         */
        public void setCountry(String country) {
            this.country = country;
        }

        /**
         *
         * @return
         *     The enableLiveTv
         */
        public Boolean getEnableLiveTv() {
            return enableLiveTv;
        }

        /**
         *
         * @param enableLiveTv
         *     The enable_live_tv
         */
        public void setEnableLiveTv(Boolean enableLiveTv) {
            this.enableLiveTv = enableLiveTv;
        }

        /**
         *
         * @return
         *     The debug
         */
        public Boolean getDebug() {
            return debug;
        }

        /**
         *
         * @param debug
         *     The debug
         */
        public void setDebug(Boolean debug) {
            this.debug = debug;
        }

    }

}
