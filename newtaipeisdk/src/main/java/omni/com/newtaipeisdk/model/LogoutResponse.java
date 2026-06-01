package omni.com.newtaipeisdk.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LogoutResponse implements Serializable {

    @SerializedName("result")
    private String result;
    @SerializedName("error_message")
    private String errorMessage;
    @SerializedName("message")
    private String message;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
