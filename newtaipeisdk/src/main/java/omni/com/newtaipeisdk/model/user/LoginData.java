package omni.com.newtaipeisdk.model.user;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LoginData implements Serializable {

    @SerializedName("token")
    private String token;
    @SerializedName("idcount")
    private String idcount;
    @SerializedName("name")
    private String name;

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public String getIdcount() {
        return idcount;
    }
}
