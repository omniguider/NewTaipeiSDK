package omni.com.newtaipeisdk.network;

import android.app.Activity;
import android.util.Log;

import omni.com.newtaipeisdk.model.BeaconInfoData;
import omni.com.newtaipeisdk.model.ClockResponse;
import omni.com.newtaipeisdk.model.CommonArrayResponse;
import omni.com.newtaipeisdk.model.MessageResponse;
import omni.com.newtaipeisdk.model.PermissionResponse;
import omni.com.newtaipeisdk.model.RecordData;
import omni.com.newtaipeisdk.model.SendBeaconBatteryResponse;
import omni.com.newtaipeisdk.tool.DialogTools;
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
    }

    private ClockService getClockService() {
        return NetworkManager.getInstance().getRetrofit().create(ClockService.class);
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
                NetworkManager.getInstance().getDeviceId(activity), "2", currentTimestamp + "", mac);

        NetworkManager.getInstance().addPostRequest(activity, call, ClockResponse.class, listener);
    }

    public void getRecord(Activity activity, String idcount, String st_date, String ed_date,
                          NetworkManager.NetworkManagerListener<RecordData[]> listener) {

        DialogTools.getInstance().showProgress(activity);

        long currentTimestamp = System.currentTimeMillis() / 1000L;
        String mac = NetworkManager.getInstance().getMacStr(currentTimestamp);

        Call<CommonArrayResponse> call = getClockService().getRecord(
                idcount, st_date, ed_date, currentTimestamp + "", mac);

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
        Log.e("LOG", "timestamp" + currentTimestamp);
        Log.e("LOG", "mac" + mac);
        Call<CommonArrayResponse> call = getClockService().getBeaconInfo(idcount);
        NetworkManager.getInstance().addPostRequestToCommonArrayObj(activity, call, BeaconInfoData[].class, listener);
    }

    public void getMessage(Activity activity, NetworkManager.NetworkManagerListener<MessageResponse> listener) {

        Call<MessageResponse> call = getClockService().getMessage();
        NetworkManager.getInstance().addPostRequest(activity, call, MessageResponse.class, listener);
    }
}
