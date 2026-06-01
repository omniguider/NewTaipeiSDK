package omni.com.newtaipeisdk.model.user;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LogoutResponse implements Serializable {

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
}
