package omni.com.newtaipeisdk.network;

import static omni.com.newtaipeisdk.NewTaipeiSDKActivity.LOG_TAG;

import android.app.Activity;
import android.util.Log;

import omni.com.newtaipeisdk.model.BannerData;
import omni.com.newtaipeisdk.model.BeaconInfoData;
import omni.com.newtaipeisdk.model.ClockResponse;
import omni.com.newtaipeisdk.model.CommonResponse;
import omni.com.newtaipeisdk.model.CommonArrayResponse;
import omni.com.newtaipeisdk.model.ConfigResponse;
import omni.com.newtaipeisdk.model.LogoutResponse;
import omni.com.newtaipeisdk.model.MessageResponse;
import omni.com.newtaipeisdk.model.PermissionResponse;
import omni.com.newtaipeisdk.model.RecordData;
import omni.com.newtaipeisdk.model.SendBeaconBatteryResponse;
import omni.com.newtaipeisdk.model.user.LoginData;
import omni.com.newtaipeisdk.tool.DialogTools;
import omni.com.newtaipeisdk.tool.Tools;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class NewTaipeiSDKApi {

    private static NewTaipeiSDKApi mNewTaipeiSDKApi;

    public static NewTaipeiSDKApi getInstance() {
        if (mNewTaipeiSDKApi == null) {
            mNewTaipeiSDKApi = new NewTaipeiSDKApi();
        }
        return mNewTaipeiSDKApi;
    }

    interface NewTaipeiService {

        @FormUrlEncoded
        @POST("api/login")
        Call<CommonArrayResponse> login(@Field("idcount") String idcount,
                                        @Field("password") String password,
                                        @Field("device_id") String deviceId,
                                        @Field("platform") String platform,
                                        @Field("timestamp") String timestamp,
                                        @Field("mac") String mac);

        @FormUrlEncoded
        @POST("api/logout")
        Call<LogoutResponse> logout(@Field("idcount") String idcount,
                                    @Field("timestamp") String timestamp,
                                    @Field("mac") String mac);

        @FormUrlEncoded
        @POST("api/verify_login_token")
        Call<CommonResponse> verifyLoginToken(@Field("idcount") String idcount,
                                              @Field("token") String token,
                                              @Field("device_id") String deviceId,
                                              @Field("login") String login,
                                              @Field("timestamp") String timestamp,
                                              @Field("mac") String mac);

        @FormUrlEncoded
        @POST("api/verify_login_device")
        Call<CommonResponse> verifyLoginDevice(@Field("idcount") String idcount,
                                               @Field("device_id") String deviceId,
                                               @Field("timestamp") String timestamp,
                                               @Field("mac") String mac);

        @FormUrlEncoded
        @POST("api/get_config")
        Call<ConfigResponse> getConfig(@Field("timestamp") String timestamp,
                                       @Field("mac") String mac);
    }

    interface ClockService {

        @GET("api/check_enabled")
        Call<PermissionResponse> checkEnabled(@Query("idcount") String idcount);

        @FormUrlEncoded
        @POST("api/record")
        Call<ClockResponse> setRecord(@Field("status") String status,
                                      @Field("username") String username,
                                      @Field("idcount") String idcount,
                                      @Field("hwid") String hwid,
                                      @Field("device_id") String device_id,
                                      @Field("plateform") String plateform,
                                      @Field("source") String source,
                                      @Field("timestamp") String timestamp,
                                      @Field("mac") String mac);

        @FormUrlEncoded
        @POST("api/get_record")
        Call<CommonArrayResponse> getRecord(@Field("idcount") String idcount,
                                            @Field("st_date") String st_date,
                                            @Field("ed_date") String ed_date,
                                            @Field("timestamp") String timestamp,
                                            @Field("mac") String mac);

        @FormUrlEncoded
        @POST("api/set_beacon")
        Call<SendBeaconBatteryResponse> setBeaconBatteryLevel(@Field("hwid") String hwid,
                                                              @Field("voltage") String voltage,
                                                              @Field("timestamp") String timestamp,
                                                              @Field("mac") String mac);

        @GET("api/beacon")
        Call<CommonArrayResponse> getBeaconInfo(@Query("idcount") String idcount);

        @GET("api/get_message")
        Call<MessageResponse> getMessage();

        @FormUrlEncoded
        @POST("api/get_banner")
        Call<CommonArrayResponse> getBanner(@Field("timestamp") String timestamp,
                                            @Field("mac") String mac);
    }

    private NewTaipeiService getNewTaipeiService() {
        return NetworkManager.getInstance().getRetrofitApp().create(NewTaipeiService.class);
    }

    private ClockService getClockService() {
        return NetworkManager.getInstance().getRetrofit().create(ClockService.class);
    }

    private ClockService getClockServiceApp() {
        return NetworkManager.getInstance().getRetrofitApp().create(ClockService.class);
    }

    public void login(Activity activity, String idcount, String password,
                      NetworkManager.NetworkManagerListener<LoginData[]> listener) {
        long currentTimestamp = System.currentTimeMillis() / 1000L;
        String mac = NetworkManager.getInstance().getMacStr(currentTimestamp);
        Log.e(LOG_TAG, "idcount " + idcount);
        Log.e(LOG_TAG, "password " + password);
        Log.e(LOG_TAG, "device " + Tools.getInstance().getDeviceId(activity));
        Log.e(LOG_TAG, "currentTimestamp " + currentTimestamp);
        Log.e(LOG_TAG, "mac " + mac);
        Call<CommonArrayResponse> call = getNewTaipeiService().login(
                idcount,
                password,
                Tools.getInstance().getDeviceId(activity),
                "2",
                currentTimestamp + "",
                mac);

        NetworkManager.getInstance().addPostRequestToCommonArrayObj(activity, call, LoginData[].class, listener);
    }

    public void logout(Activity activity, String idcount,
                       NetworkManager.NetworkManagerListener<LogoutResponse> listener) {
        long currentTimestamp = System.currentTimeMillis() / 1000L;
        String mac = NetworkManager.getInstance().getMacStr(currentTimestamp);
        Log.e(LOG_TAG, "idcount " + idcount);
        Log.e(LOG_TAG, "currentTimestamp " + currentTimestamp);
        Log.e(LOG_TAG, "mac " + mac);
        Call<LogoutResponse> call = getNewTaipeiService().logout(
                idcount,
                currentTimestamp + "",
                mac);

        NetworkManager.getInstance().addPostRequest(activity, call, LogoutResponse.class, listener);
    }

    public void verifyLoginToken(Activity activity, String idcount, String token, String login,
                                 NetworkManager.NetworkManagerListener<CommonResponse> listener) {
        long currentTimestamp = System.currentTimeMillis() / 1000L;
        String mac = NetworkManager.getInstance().getMacStr(currentTimestamp);
        Log.e(LOG_TAG, "verifyLoginToken idcount " + idcount);
        Log.e(LOG_TAG, "verifyLoginToken token " + token);
        Log.e(LOG_TAG, "verifyLoginToken device " + Tools.getInstance().getDeviceId(activity));
        Log.e(LOG_TAG, "verifyLoginToken login " + login);
        Log.e(LOG_TAG, "verifyLoginToken currentTimestamp " + currentTimestamp);
        Log.e(LOG_TAG, "verifyLoginToken mac " + mac);
        Call<CommonResponse> call = getNewTaipeiService().verifyLoginToken(
                idcount,
                token,
                Tools.getInstance().getDeviceId(activity),
                login,
                currentTimestamp + "",
                mac);

        NetworkManager.getInstance().addPostRequest(activity, call, CommonResponse.class, listener);
    }

    public void verifyLoginDevice(Activity activity, String idcount,
                                  NetworkManager.NetworkManagerListener<CommonResponse> listener) {
        long currentTimestamp = System.currentTimeMillis() / 1000L;
        String mac = NetworkManager.getInstance().getMacStr(currentTimestamp);
        Call<CommonResponse> call = getNewTaipeiService().verifyLoginDevice(
                idcount,
                Tools.getInstance().getDeviceId(activity),
                currentTimestamp + "",
                mac);

        NetworkManager.getInstance().addPostRequest(activity, call, CommonResponse.class, listener);
    }

    public void getConfig(Activity activity, NetworkManager.NetworkManagerListener<ConfigResponse> listener) {
        long currentTimestamp = System.currentTimeMillis() / 1000L;
        String mac = NetworkManager.getInstance().getMacStr(currentTimestamp);
        Call<ConfigResponse> call = getNewTaipeiService().getConfig(
                currentTimestamp + "",
                mac);

        NetworkManager.getInstance().addPostRequest(activity, call, ConfigResponse.class, listener);
    }

    public void checkEnabled(Activity activity, String idcount, NetworkManager.NetworkManagerListener<PermissionResponse> listener) {

//        DialogTools.getInstance().showProgress(activity);
        Log.e("NTS", "idcount" + idcount);
        long currentTimestamp = System.currentTimeMillis() / 1000L;
        String mac = NetworkManager.getInstance().getMacStr(currentTimestamp);
        Call<PermissionResponse> call = getClockService().checkEnabled(idcount);

        NetworkManager.getInstance().addPostRequest(activity, call, PermissionResponse.class, listener);
    }

    public void setRecord(Activity activity, String status, String username, String idcount,
                          String hwid, NetworkManager.NetworkManagerListener<ClockResponse> listener) {

        DialogTools.getInstance().showProgress(activity);
        Log.e("NTS", "status" + status);
        Log.e("NTS", "username" + username);
        Log.e("NTS", "idcount" + idcount);
        Log.e("NTS", "hwid" + hwid);
        long currentTimestamp = System.currentTimeMillis() / 1000L;
        String mac = NetworkManager.getInstance().getMacStr(currentTimestamp);
        Call<ClockResponse> call = getClockService().setRecord(
                status, username, idcount, hwid,
                NetworkManager.getInstance().getDeviceId(activity), "2", "APP", currentTimestamp + "", mac);

        NetworkManager.getInstance().addPostRequest(activity, call, ClockResponse.class, listener);
    }

    public void getRecord(Activity activity, String idcount, String st_date, String ed_date,
                          NetworkManager.NetworkManagerListener<RecordData[]> listener) {

        DialogTools.getInstance().showProgress(activity);

        long currentTimestamp = System.currentTimeMillis() / 1000L;
        String mac = NetworkManager.getInstance().getMacStr(currentTimestamp);

        Call<CommonArrayResponse> call = getClockService().getRecord(
                idcount, st_date, ed_date, currentTimestamp + "", mac);

        Log.e("NTS", "getRecord" + call.request());
        NetworkManager.getInstance().addPostRequestToCommonArrayObj(activity, call, RecordData[].class, listener);
    }

    public void setBeaconBatteryLevel(Activity activity, String hwid, String voltage, NetworkManager.NetworkManagerListener<SendBeaconBatteryResponse> listener) {

//        DialogTools.getInstance().showProgress(activity);

        long currentTimestamp = System.currentTimeMillis() / 1000L;
        String mac = NetworkManager.getInstance().getMacStr(currentTimestamp);
        Call<SendBeaconBatteryResponse> call = getClockService().setBeaconBatteryLevel(
                hwid, voltage, currentTimestamp + "", mac);

        NetworkManager.getInstance().addPostRequest(activity, call, SendBeaconBatteryResponse.class, listener);
    }

    public void getBeaconInfo(Activity activity, String idcount, NetworkManager.NetworkManagerListener<BeaconInfoData[]> listener) {

        long currentTimestamp = System.currentTimeMillis() / 1000L;
        String mac = NetworkManager.getInstance().getMacStr(currentTimestamp);
        Log.e(LOG_TAG, "timestamp" + currentTimestamp);
        Log.e(LOG_TAG, "mac" + mac);
        Call<CommonArrayResponse> call = getClockService().getBeaconInfo(idcount);
        NetworkManager.getInstance().addPostRequestToCommonArrayObj(activity, call, BeaconInfoData[].class, listener);

        Log.e("NTS", "getBeaconInfo" + call.request());
    }

    public void getBanner(Activity activity, NetworkManager.NetworkManagerListener<BannerData[]> listener) {

        long currentTimestamp = System.currentTimeMillis() / 1000L;
        String mac = NetworkManager.getInstance().getMacStr(currentTimestamp);
        Call<CommonArrayResponse> call = getClockServiceApp().getBanner(currentTimestamp + "", mac);
        NetworkManager.getInstance().addPostRequestToCommonArrayObj(activity, call, BannerData[].class, listener);
    }

    public void getMessage(Activity activity, NetworkManager.NetworkManagerListener<MessageResponse> listener) {

        Call<MessageResponse> call = getClockService().getMessage();
        NetworkManager.getInstance().addPostRequest(activity, call, MessageResponse.class, listener);
    }
}
