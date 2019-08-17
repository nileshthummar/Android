package com.watchback2.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class Settings implements Serializable {

    @Expose
    @SerializedName("save_playhead_count")
    private String savePlayheadCount;
    @Expose
    @SerializedName("monthly_video_goal")
    private String monthlyVideoGoal;
    @Expose
    @SerializedName("monthly_reward_cap")
    private String monthlyRewardCap;
    @Expose
    @SerializedName("longform_pts_reward")
    private String longformPtsReward;
    @Expose
    @SerializedName("longform_complete_pct")
    private String longformCompletePct;
    @Expose
    @SerializedName("longform_cap")
    private String longformCap;
    @Expose
    @SerializedName("fastforward_limit_pct")
    private String fastforwardLimitPct;
    @Expose
    @SerializedName("daily_pts_video_count")
    private String dailyPtsVideoCount;
    @Expose
    @SerializedName("daily_pts_reward")
    private String dailyPtsReward;
    @Expose
    @SerializedName("daily_pts_overall_total")
    private String dailyPtsOverallTotal;
    @Expose
    @SerializedName("daily_pts_accts_per_ip")
    private String dailyPtsAcctsPerIp;
    @Expose
    @SerializedName("daily_cap")
    private String dailyCap;
    @Expose
    @SerializedName("aysw_prompt_seconds")
    private String ayswPromptSeconds;
    @Expose
    @SerializedName("aysw_no_response_minutes")
    private String ayswNoResponseMinutes;
    @Expose
    @SerializedName("autoplay")
    private String autoplay;
    @Expose
    @SerializedName("new_keys")
    private String newKeys;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("autoplay", autoplay).append("ayswPromptSeconds",
                ayswPromptSeconds).append("dailyCap", dailyCap).append("fastforwardLimitPct",
                fastforwardLimitPct).append("ayswNoResponseMinutes", ayswNoResponseMinutes).append(
                "savePlayheadCount", savePlayheadCount).append("longformCompletePct",
                longformCompletePct).append("longformCap", longformCap)
                .append("monthlyVideoGoal", monthlyVideoGoal)
                .append("monthlyRewardCap", monthlyRewardCap)
                .append("longformPtsReward", longformPtsReward)
                .append("dailyPtsVideoCount", dailyPtsVideoCount)
                .append("dailyPtsReward", dailyPtsReward)
                .append("dailyPtsOverallTotal", dailyPtsOverallTotal)
                .append("dailyPtsAcctsPerIp", dailyPtsAcctsPerIp)
                .append("newKeys", newKeys)
                .toString();
    }

    public String getSavePlayheadCount() {
        return savePlayheadCount;
    }

    public String getMonthlyVideoGoal() {
        return monthlyVideoGoal;
    }

    public String getMonthlyRewardCap() {
        return monthlyRewardCap;
    }

    public String getLongformPtsReward() {
        return longformPtsReward;
    }

    public String getLongformCompletePct() {
        return longformCompletePct;
    }

    public String getLongformCap() {
        return longformCap;
    }

    public String getFastforwardLimitPct() {
        return fastforwardLimitPct;
    }

    public String getDailyPtsVideoCount() {
        return dailyPtsVideoCount;
    }

    public String getDailyPtsReward() {
        return dailyPtsReward;
    }

    public String getDailyPtsOverallTotal() {
        return dailyPtsOverallTotal;
    }

    public String getDailyPtsAcctsPerIp() {
        return dailyPtsAcctsPerIp;
    }

    public String getDailyCap() {
        return dailyCap;
    }

    public String getAyswPromptSeconds() {
        return ayswPromptSeconds;
    }

    public String getAyswNoResponseMinutes() {
        return ayswNoResponseMinutes;
    }

    public String getAutoplay() {
        return autoplay;
    }

    public String getNewKeys() {
        return newKeys;
    }
}