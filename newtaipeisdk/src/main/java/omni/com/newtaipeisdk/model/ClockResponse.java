package omni.com.newtaipeisdk.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ClockResponse implements Serializable{

    @SerializedName("result")
    private String result;
    @SerializedName("message")
    private String message;
    @SerializedName("status")
    private String status;
    @SerializedName("timestamp")
    private String timestamp;
    @SerializedName("error_message")
    private String errorMessage;
    @SerializedName("data")
    private Object data;

    public String getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Object getData() {
        return data;
    }

}
