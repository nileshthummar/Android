package com.watchback2.android.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class Source implements Serializable {

    @SerializedName("ext_x_version")
    @Expose
    private String extXVersion;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("src")
    @Expose
    private String src;
    @SerializedName("profiles")
    @Expose
    private String profiles;
    @SerializedName("avg_bitrate")
    @Expose
    private long avgBitrate;
    @SerializedName("width")
    @Expose
    private long width;
    @SerializedName("size")
    @Expose
    private long size;
    @SerializedName("height")
    @Expose
    private long height;
    @SerializedName("duration")
    @Expose
    private long duration;
    @SerializedName("container")
    @Expose
    private String container;
    @SerializedName("codec")
    @Expose
    private String codec;
    private final static long serialVersionUID = -3617108877703009559L;

    public String getExtXVersion() {
        return extXVersion;
    }

    public void setExtXVersion(String extXVersion) {
        this.extXVersion = extXVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getProfiles() {
        return profiles;
    }

    public void setProfiles(String profiles) {
        this.profiles = profiles;
    }

    public long getAvgBitrate() {
        return avgBitrate;
    }

    public void setAvgBitrate(long avgBitrate) {
        this.avgBitrate = avgBitrate;
    }

    public long getWidth() {
        return width;
    }

    public void setWidth(long width) {
        this.width = width;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("extXVersion", extXVersion).append("type", type).append("src", src).append("profiles", profiles).append("avgBitrate", avgBitrate).append("width", width).append("size", size).append("height", height).append("duration", duration).append("container", container).append("codec", codec).toString();
    }
}