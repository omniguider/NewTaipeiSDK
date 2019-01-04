package omni.com.newtaipeisdk.network;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import omni.com.newtaipeisdk.tool.DialogTools;
import omni.com.newtaipeisdk.R;
import omni.com.newtaipeisdk.model.CommonArrayResponse;
import omni.com.newtaipeisdk.model.CommonResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkManager {

    public static final String DOMAIN_NAME = "http://bp.ntpc.gov.tw/";
//    public static final String DOMAIN_NAME = "http://bp-test.ntpc.gov.tw/";
    public static final String API_RESULT_TRUE = "true";
    private static NetworkManager mNetworkManager;
    private Retrofit mRetrofit;
    private Gson mGson;
    public static final int TIME_OUT = 120;

    public static NetworkManager getInstance() {
        if (mNetworkManager == null) {
            mNetworkManager = new NetworkManager();
        }
        return mNetworkManager;
    }

    public interface NetworkManagerListener<T> {
        void onSucceed(T object);

        void onFail(String errorMsg, boolean shouldRetry);
    }

    public Retrofit getRetrofit() {
        if (mRetrofit == null) {
            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                    .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
                    .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                    .build();

            mRetrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(DOMAIN_NAME)
                    .callbackExecutor(Executors.newSingleThreadExecutor())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return mRetrofit;
    }

    public Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }

    public String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getMacStr(long currentTimestamp) {
        try {
//            return SHA1("ntpcapp://" + currentTimestamp);
            return SHA256("ntpcapp://" + currentTimestamp);
        } catch (NoSuchAlgorithmException e) {
            Log.e("@W@", "NoSuchAlgorithmException cause : " + e.getCause());
            return "";
        } catch (UnsupportedEncodingException e) {
            Log.e("@W@", "UnsupportedEncodingException cause : " + e.getCause());
            return "";
        }
    }

    private void sendResponseFailMessage(Context context,
                                         Response response,
                                         NetworkManagerListener listener) {
        String errorMsg = response.message();
        listener.onFail(errorMsg, false);
    }

    private void sendAPIFailMessage(Context context, String errorMsg, NetworkManagerListener listener) {
        listener.onFail(errorMsg, false);
    }

    public <T> void addPostRequest(final Activity activity,
                                   Call<T> call,
                                   final Class<T> responseClass,
                                   final NetworkManagerListener<T> listener) {

        if (!checkNetworkStatus(activity)) {
            return;
        }

        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, final Response<T> response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            if (response.body() == null) {
                                listener.onFail(activity.getString(R.string.error_dialog_title_text_unknown), false);
                            } else {
                                listener.onSucceed(response.body());
                            }
                        } else {
                            sendResponseFailMessage(activity, response, listener);
                        }
                        DialogTools.getInstance().dismissProgress(activity);
                    }
                });
            }

            @Override
            public void onFailure(Call<T> call, final Throwable t) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFail(activity.getString(R.string.dialog_message_network_connect_not_good), false);
                        DialogTools.getInstance().dismissProgress(activity);
                    }
                });
            }
        });
    }

    public <T> void addPostRequestToCommonObj(final Activity activity,
                                              Call<CommonResponse> call,
                                              final Class<T> responseClass,
                                              final NetworkManagerListener<T> listener) {

        if (!checkNetworkStatus(activity)) {
            return;
        }

        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, final Response<CommonResponse> response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {

                            if (response.body() == null) {
                                listener.onFail(activity.getString(R.string.error_dialog_title_text_unknown), false);
                            } else {

                                if (response.body().getResult().equals(API_RESULT_TRUE)) {
                                    String json = getGson().toJson(response.body().getData());
                                    T data = getGson().fromJson(json, responseClass);

                                    listener.onSucceed(data);

                                } else {
                                    sendAPIFailMessage(activity, response.body().getErrorMessage(), listener);
                                }
                            }

                        } else {
                            sendResponseFailMessage(activity, response, listener);
                        }

                        DialogTools.getInstance().dismissProgress(activity);
                    }
                });
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFail(activity.getString(R.string.dialog_message_network_connect_not_good), false);
                        DialogTools.getInstance().dismissProgress(activity);
                    }
                });
            }
        });
    }

    public <T> void addPostRequestToCommonArrayObj(final Activity activity,
                                                   Call<CommonArrayResponse> call,
                                                   final Class<T[]> responseClass,
                                                   final NetworkManagerListener<T[]> listener) {

        if (!checkNetworkStatus(activity)) {
            return;
        }

        call.enqueue(new Callback<CommonArrayResponse>() {
            @Override
            public void onResponse(Call<CommonArrayResponse> call, final Response<CommonArrayResponse> response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            if (response.body() == null) {
                                listener.onFail(activity.getString(R.string.error_dialog_title_text_unknown), false);
                            } else {
                                if (response.body().getResult().equals(API_RESULT_TRUE)) {
                                    String json = getGson().toJson(response.body().getData());
                                    T[] data = getGson().fromJson(json, responseClass);

                                    listener.onSucceed(data);
                                } else {
                                    sendAPIFailMessage(activity, response.body().getErrorMessage(), listener);
                                }
                            }
                        } else {
                            sendResponseFailMessage(activity, response, listener);
                        }
                        DialogTools.getInstance().dismissProgress(activity);
                    }
                });
            }

            @Override
            public void onFailure(Call<CommonArrayResponse> call, final Throwable t) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFail(activity.getString(R.string.dialog_message_network_connect_not_good), false);
                        DialogTools.getInstance().dismissProgress(activity);
                    }
                });
            }
        });
    }


    private boolean checkNetworkStatus(Context context) {
        if (!isNetworkAvailable(context)) {
            DialogTools.getInstance().dismissProgress(context);
            DialogTools.getInstance().showNoNetworkMessage(context);
            return false;
        }
        return true;
    }

    public boolean isNetworkAvailable(Context context) {
        if (context != null) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo wifiNetwork = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiNetwork != null && wifiNetwork.isConnected()) {

                return true;
            }
            NetworkInfo mobileNetwork = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mobileNetwork != null && mobileNetwork.isConnected()) {
                return true;
            }

            boolean isNetworkEnable = (manager != null &&
                    manager.getActiveNetworkInfo() != null &&
                    manager.getActiveNetworkInfo().isConnectedOrConnecting());

            return isNetworkEnable;
        } else {
            return false;
        }
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] textBytes = text.getBytes("iso-8859-1");
        md.update(textBytes, 0, textBytes.length);
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    public static String SHA256(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] textBytes = text.getBytes("iso-8859-1");
        md.update(textBytes, 0, textBytes.length);
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }
}
