package omni.com.newtaipeisdk.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BeaconInfoData implements Serializable {

    @SerializedName("UUID")
    private String UUID;
    @SerializedName("HWID")
    private String HWID;
    @SerializedName("LATITUDE")
    private double LATITUDE;
    @SerializedName("LONGITUDE")
    private double LONGITUDE;
    @SerializedName("UPDTE_RATE")
    private int UPDTE_RATE;
    @SerializedName("DESC")
    private String DESC;
    @SerializedName("FloorNumber")
    private int FloorNumber;
    @SerializedName("FloorName")
    private String FloorName;
    @SerializedName("ClockEnabled")
    private String ClockEnabled;
    @SerializedName("Vendor")
    private String Vendor;

    public String getUUID() {
        return UUID;
    }

    public String getHWID() {
        return HWID;
    }

    public double getLATITUDE() {
        return LATITUDE;
    }

    public double getLONGITUDE() {
        return LONGITUDE;
    }

    public int getUPDTE_RATE() {
        return UPDTE_RATE;
    }

    public String getDESC() {
        return DESC;
    }

    public int getFloorNumber() {
        return FloorNumber;
    }

    public String getFloorName() {
        return FloorName;
    }

    public String getClockEnabled() {
        return ClockEnabled;
    }

    public String getVendor() {
        return Vendor;
    }
}