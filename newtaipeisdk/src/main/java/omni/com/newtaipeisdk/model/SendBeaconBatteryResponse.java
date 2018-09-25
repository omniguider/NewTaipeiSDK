package omni.com.newtaipeisdk.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SendBeaconBatteryResponse implements Serializable {

    @SerializedName("result")
    private String result;
    @SerializedName("error_message")
    private String errorMsg;

    public String getResult() {
        return result;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public boolean isSuccess() {
        return result.equals("true");
    }

}
