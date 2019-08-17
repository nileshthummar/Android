package com.watchback2.android.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.request.annotation.NullableData;
import com.perk.request.model.Data;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Created by Nilesh on 11/25/16.
 */
@NullableData
public class PerkAdView extends Data {

    private static final long serialVersionUID = 2L;
    @SerializedName("award_type")
    @Expose
    private String awardType;
    @SerializedName("amount")
    @Expose
    private Integer amount;
    @SerializedName("points_awarded")
    @Expose
    private Integer pointsAwarded;
    @SerializedName("point_type")
    @Expose
    private String pointType;

    /**
     *
     * @return
     *     The awardType
     */
    public String getAwardType() {
        return awardType;
    }

    /**
     *
     * @param awardType
     *     The award_type
     */
    public void setAwardType(String awardType) {
        this.awardType = awardType;
    }

    /**
     *
     * @return
     *     The amount
     */
    public Integer getAmount() {
        return amount != null && amount != 0 ? amount : getPointsAwarded();
    }

    /**
     *
     * @param amount
     *     The amount
     */
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public void setPointsAwarded(Integer pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public Integer getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointType(String pointType) {
        this.pointType = pointType;
    }

    public String getPointType() {
        return pointType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("award_type", awardType).append("amount",
                amount).append("points_awarded", pointsAwarded).append("point_type",
                pointType).toString();
    }
}
