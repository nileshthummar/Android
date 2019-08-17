package com.watchback2.android.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.perk.request.model.Data;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Nilesh on 11/25/16.
 */

public class CarouselData extends Data {

    private static final long serialVersionUID = 1L;

    @SerializedName("carousel")
    @Expose
    private Carousel carousel;

    public Carousel getCarousel() {
        return carousel;
    }

    public void setCarousel(Carousel carousel) {
        this.carousel = carousel;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("carousel", carousel).toString();
    }

    public class Carousel implements Serializable {

        private static final long serialVersionUID = 1L;

        @SerializedName("items")
        @Expose
        private List<Item> items = null;

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("items", items).toString();
        }
    }

    public static class Item implements Serializable {

        private static final long serialVersionUID = 2L;

        @SerializedName("image")
        @Expose
        private String image;
        @SerializedName("image2")
        @Expose
        private String image2;
        @SerializedName("image3")
        @Expose
        private String image3;
        @SerializedName("destination")
        @Expose
        private String destination;
        @SerializedName("fields")
        @Expose
        private Fields fields;

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getImage2() {
            return image2;
        }

        public void setImage2(String image2) {
            this.image2 = image2;
        }

        public String getImage3() {
            return image3;
        }

        public void setImage3(String image3) {
            this.image3 = image3;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public Fields getFields() {
            return fields;
        }

        public void setFields(Fields fields) {
            this.fields = fields;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("image", image).append("image2", image2).append(
                    "image3", image3).append("destination", destination).append("fields",
                    fields).toString();
        }
    }

    public static class Fields implements Serializable {

        private static final long serialVersionUID = 7L;

        @SerializedName("txt1")
        @Expose
        private String txt1;
        @SerializedName("txt2")
        @Expose
        private String txt2;
        @SerializedName("txt3")
        @Expose
        private String txt3;
        @SerializedName("txt4")
        @Expose
        private String txt4;
        @SerializedName("txt5")
        @Expose
        private String txt5;
        @SerializedName("txt6")
        @Expose
        private String txt6;
        @SerializedName("txt7")
        @Expose
        private String txt7;
        @SerializedName("txt8")
        @Expose
        private String txt8;
        @SerializedName("txt9")
        @Expose
        private String txt9;
        @SerializedName("txt10")
        @Expose
        private String txt10;
        @SerializedName("txt11")
        @Expose
        private String txt11;
        @SerializedName("txt12")
        @Expose
        private String txt12;

        public String getTxt1() {
            return txt1;
        }

        public void setTxt1(String txt1) {
            this.txt1 = txt1;
        }

        public String getTxt2() {
            return txt2;
        }

        public void setTxt2(String txt2) {
            this.txt2 = txt2;
        }

        public String getTxt3() {
            return txt3;
        }

        public void setTxt3(String txt3) {
            this.txt3 = txt3;
        }

        public String getTxt4() {
            return txt4;
        }

        public void setTxt4(String txt4) {
            this.txt4 = txt4;
        }

        public String getTxt5() {
            return txt5;
        }

        public void setTxt5(String txt5) {
            this.txt5 = txt5;
        }

        public String getTxt6() {
            return txt6;
        }

        public void setTxt6(String txt6) {
            this.txt6 = txt6;
        }

        public String getTxt7() {
            return txt7;
        }

        public void setTxt7(String txt7) {
            this.txt7 = txt7;
        }

        public String getTxt8() {
            return txt8;
        }

        public void setTxt8(String txt8) {
            this.txt8 = txt8;
        }

        public String getTxt9() {
            return txt9;
        }

        public void setTxt9(String txt9) {
            this.txt9 = txt9;
        }

        public String getTxt10() {
            return txt10;
        }

        public void setTxt10(String txt10) {
            this.txt10 = txt10;
        }

        public String getTxt11() {
            return txt11;
        }

        public void setTxt11(String txt11) {
            this.txt11 = txt11;
        }

        public String getTxt12() {
            return txt12;
        }

        public void setTxt12(String txt12) {
            this.txt12 = txt12;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("txt1", txt1).append("txt2", txt2).append(
                    "txt3", txt3).append("txt4", txt4).append("txt5", txt5).append("txt6",
                    txt6).append("txt7", txt7).append("txt8", txt8).append("txt9", txt9).append(
                    "txt10", txt10).append("txt11", txt11).append("txt12", txt12).toString();
        }
    }

}
