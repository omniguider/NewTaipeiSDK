package omni.com.newtaipeisdk.model.user;

import java.io.Serializable;

public class UserLoginInfo implements Serializable {

    private String account;
    private String password;
    private String token;
    private String name;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class Builder {

        private UserLoginInfo mUserLoginInfo;

        public Builder() {
            if (mUserLoginInfo == null) {
                mUserLoginInfo = new UserLoginInfo();
            }
        }

        public UserLoginInfo build() {
            return mUserLoginInfo;
        }

        public Builder setAccount(String account) {
            mUserLoginInfo.setAccount(account);
            return this;
        }

        public Builder setToken(String login_token) {
            mUserLoginInfo.setToken(login_token);
            return this;
        }

        public Builder setName(String name) {
            mUserLoginInfo.setName(name);
            return this;
        }

    }
}
