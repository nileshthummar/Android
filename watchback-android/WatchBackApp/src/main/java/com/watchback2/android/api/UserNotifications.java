
package com.watchback2.android.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.request.model.Data;

import java.util.List;

public class UserNotifications extends Data {

    @SerializedName("notifications")
    @Expose

    private List<Notification> notifications = null;

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }



    public class Notification {

        @SerializedName("points_for_tokens")
        @Expose
        private Boolean pointsForTokens;
        @SerializedName("uID")
        @Expose
        private Integer uID;
        @SerializedName("date")
        @Expose
        private String date;
        @SerializedName("source_id")
        @Expose
        private Integer sourceId;
        @SerializedName("type")
        @Expose
        private Integer type;
        @SerializedName("hyperlink")
        @Expose
        private String hyperlink;
        @SerializedName("text")
        @Expose
        private String text;
        @SerializedName("state")
        @Expose
        private String state;
        @SerializedName("amount")
        @Expose
        private Integer amount;
        @SerializedName("currency_type")
        @Expose
        private String currencyType;
        @SerializedName("status")
        @Expose
        private String status;
        @SerializedName("event_type")
        @Expose
        private Integer eventType;

        public Boolean getPointsForTokens() {
            return pointsForTokens;
        }

        public void setPointsForTokens(Boolean pointsForTokens) {
            this.pointsForTokens = pointsForTokens;
        }

        public Integer getUID() {
            return uID;
        }

        public void setUID(Integer uID) {
            this.uID = uID;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Integer getSourceId() {
            return sourceId;
        }

        public void setSourceId(Integer sourceId) {
            this.sourceId = sourceId;
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getHyperlink() {
            return hyperlink;
        }

        public void setHyperlink(String hyperlink) {
            this.hyperlink = hyperlink;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public Integer getAmount() {
            return amount;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }

        public String getCurrencyType() {
            return currencyType;
        }

        public void setCurrencyType(String currencyType) {
            this.currencyType = currencyType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getEventType() {
            return eventType;
        }

        public void setEventType(Integer eventType) {
            this.eventType = eventType;
        }

    }

}
