package omni.com.newtaipeisdk.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ConfigResponse implements Serializable {

    @SerializedName("result")
    private String result;
    @SerializedName("error_message")
    private String errorMessage;
    @SerializedName("message")
    private String message;
    @SerializedName("enabled")
    private String enabled;
    @SerializedName("disabled_message")
    private String disabled_message;
    @SerializedName("app_error_message")
    private String app_error_message;
    @SerializedName("android_version")
    private String android_version;
    @SerializedName("android_app_url")
    private String android_app_url;

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

    public String getEnabled() {
        return enabled;
    }

    public String getDisabled_message() {
        return disabled_message;
    }

    public String getApp_error_message() {
        return app_error_message;
    }

    public String getAndroid_version() {
        return android_version;
    }

    public String getAndroid_app_url() {
        return android_app_url;
    }
}
