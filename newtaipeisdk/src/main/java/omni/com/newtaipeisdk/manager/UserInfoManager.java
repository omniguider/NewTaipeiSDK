package omni.com.newtaipeisdk.manager;

import android.app.Activity;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;

import omni.com.newtaipeisdk.model.user.LoginData;
import omni.com.newtaipeisdk.model.user.UserLoginInfo;
import omni.com.newtaipeisdk.tool.DialogTools;
import omni.com.newtaipeisdk.tool.PreferencesTools;
import omni.com.newtaipeisdk.R;

public class UserInfoManager {

    private static UserInfoManager sUserInfoManager;

    public static final String KEY_USER_LOGIN_INFO = "key_preferences_user_login_info";

    public static UserInfoManager getInstance() {
        if (sUserInfoManager == null) {
            sUserInfoManager = new UserInfoManager();
        }
        return sUserInfoManager;
    }

    public boolean isLoggedIn(Activity activity) {
        UserLoginInfo userLoginInfo = getUserInfo(activity);
        return userLoginInfo != null && !TextUtils.isEmpty(userLoginInfo.getToken());
    }

    public void saveUserLoginInfo(Activity activity, LoginData data,
                                  String account, String password) {
        UserLoginInfo userLoginInfo = getUserInfo(activity);
        if (userLoginInfo == null) {
            userLoginInfo = new UserLoginInfo.Builder().build();
        }
        userLoginInfo.setAccount(account);
        userLoginInfo.setPassword(password);
        userLoginInfo.setToken(data.getToken());
        userLoginInfo.setName(data.getName());

        PreferencesTools.getInstance().saveProperty(activity, KEY_USER_LOGIN_INFO, userLoginInfo);
    }

    public void updateUserLoginToken(Activity activity, String loginToken) {
        UserLoginInfo userLoginInfo = getUserInfo(activity);
        if (userLoginInfo == null) {
            return;
        }
        userLoginInfo.setToken(loginToken);
        PreferencesTools.getInstance().saveProperty(activity, KEY_USER_LOGIN_INFO, userLoginInfo);
    }

    public void userLogout(Activity activity) {
        PreferencesTools.getInstance().removeProperty(activity, KEY_USER_LOGIN_INFO);
    }

    @Nullable
    public UserLoginInfo getUserInfo(Activity activity) {
        return PreferencesTools.getInstance().getProperty(activity, KEY_USER_LOGIN_INFO, UserLoginInfo.class);
    }

    @NonNull
    public String getUserLoginToken(Activity activity) {
        UserLoginInfo loginInfo = getUserInfo(activity);
        if (loginInfo == null) {
            DialogTools.getInstance().showErrorMessage(activity, R.string.dialog_title_text_note, R.string.dialog_message_no_login);
            return null;
        } else {
            String loginToken = loginInfo.getToken();
            if (TextUtils.isEmpty(loginToken)) {

                DialogTools.getInstance().showErrorMessage(activity, R.string.dialog_title_text_note, R.string.dialog_message_no_login);
                return null;
            } else {
                return loginToken;
            }
        }
    }
}

