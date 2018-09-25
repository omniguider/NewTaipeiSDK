package omni.com.newtaipeisdk.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RecordData implements Serializable {

    @SerializedName("timestamp")
    private String timestamp;
    @SerializedName("beacon_code")
    private String beacon_code;
    @SerializedName("status_name")
    private String status_name;

    public String getTimestamp() {
        return timestamp;
    }

    public String getBeacon_code() {
        return beacon_code;
    }

    public String getStatus_name() {
        return status_name;
    }

}