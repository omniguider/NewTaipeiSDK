package omni.com.newtaipeisdk.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BannerData implements Serializable {

    @SerializedName("b_id")
    private String b_id;
    @SerializedName("b_title")
    private String b_title;
    @SerializedName("b_link")
    private String b_link;
    @SerializedName("b_image")
    private String b_image;
    @SerializedName("b_on_time")
    private String b_on_time;
    @SerializedName("b_off_time")
    private String b_off_time;
    @SerializedName("b_order")
    private int b_order;

    public String getB_id() {
        return b_id;
    }

    public String getB_title() {
        return b_title;
    }

    public String getB_link() {
        return b_link;
    }

    public String getB_image() {
        return b_image;
    }

    public String getB_on_time() {
        return b_on_time;
    }

    public String getB_off_time() {
        return b_off_time;
    }

    public int getB_order() {
        return b_order;
    }
}