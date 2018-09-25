package omni.com.newtaipeisdk.network;

import android.app.Activity;

import omni.com.newtaipeisdk.model.ClockResponse;
import omni.com.newtaipeisdk.model.CommonArrayResponse;
import omni.com.newtaipeisdk.model.RecordData;
import omni.com.newtaipeisdk.model.SendBeaconBatteryResponse;
import omni.com.newtaipeisdk.tool.DialogTools;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class NewTaipeiSDKApi {

    private static NewTaipeiSDKApi mNewTaipeiSDKApi;

    public static NewTaipeiSDKApi getInstance() {
        if (mNewTaipeiSDKApi == null) {
            mNewTaipeiSDKApi = new NewTaipeiSDKApi();
        }
        return mNewTaipeiSDKApi;
    }

    interface ClockService {

        @FormUrlEncoded
        @POST("api/record")
        Call<ClockResponse> setRecord(@Field("status") String status,
                                      @Field("username") String username,
                                      @Field("idcount") String idcount,
                                      @Field("major") String major,
                                      @Field("minor") String minor,
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
        Call<SendBeaconBatteryResponse> setBeaconBatteryLevel(@Field("beacon_mac") String beaconMac,
                                                              @Field("voltage") String voltage,
                                                              @Field("timestamp") String timestamp,
                                                              @Field("mac") String mac);
    }

    private ClockService getClockService() {
        return NetworkManager.getInstance().getRetrofit().create(ClockService.class);
    }

    public void setRecord(Activity activity, String status, String username, String idcount,
                          String major, String minor, NetworkManager.NetworkManagerListener<ClockResponse> listener) {

        DialogTools.getInstance().showProgress(activity);

        long currentTimestamp = System.currentTimeMillis() / 1000L;
        String mac = NetworkManager.getInstance().getMacStr(currentTimestamp);
        Call<ClockResponse> call = getClockService().setRecord(
                status, username, idcount, major, minor, currentTimestamp + "", mac);

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

    public void setBeaconBatteryLevel(Activity activity, String beaconMac, String voltage, NetworkManager.NetworkManagerListener<SendBeaconBatteryResponse> listener) {

//        DialogTools.getInstance().showProgress(activity);

        long currentTimestamp = System.currentTimeMillis() / 1000L;
        String mac = NetworkManager.getInstance().getMacStr(currentTimestamp);
        Call<SendBeaconBatteryResponse> call = getClockService().setBeaconBatteryLevel(
                beaconMac, voltage, currentTimestamp + "", mac);

        NetworkManager.getInstance().addPostRequest(activity, call, SendBeaconBatteryResponse.class, listener);
    }
}
