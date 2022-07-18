package omni.com.newtaipeisdk.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PermissionResponse implements Serializable{

    @SerializedName("result")
    private boolean result;
    @SerializedName("message")
    private String message;

    public boolean getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

}
