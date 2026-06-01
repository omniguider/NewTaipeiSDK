package omni.com.newtaipeisdk;

import static com.m4grid.lib.m4Beacon.RawDevice.Decrypt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.THLight.Omniguider.Lib.OmniguiderData;
import com.google.android.material.tabs.TabLayout;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.ArmaRssiFilter;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import omni.com.newtaipeisdk.adapter.PicPagerAdapter;
import omni.com.newtaipeisdk.beacon.BaseBleActivity;
import omni.com.newtaipeisdk.beacon.M4Beacon;
import omni.com.newtaipeisdk.beacon.M4BeaconWithCounter;
import omni.com.newtaipeisdk.manager.AnimationFragmentManager;
import omni.com.newtaipeisdk.manager.UserInfoManager;
import omni.com.newtaipeisdk.model.BannerData;
import omni.com.newtaipeisdk.model.BeaconInfoData;
import omni.com.newtaipeisdk.model.CommonResponse;
import omni.com.newtaipeisdk.model.ConfigResponse;
import omni.com.newtaipeisdk.model.MessageResponse;
import omni.com.newtaipeisdk.model.PermissionResponse;
import omni.com.newtaipeisdk.model.SendBeaconBatteryResponse;
import omni.com.newtaipeisdk.model.user.UserLoginInfo;
import omni.com.newtaipeisdk.network.NetworkManager;
import omni.com.newtaipeisdk.network.NewTaipeiSDKApi;
import omni.com.newtaipeisdk.tool.DialogTools;
import omni.com.newtaipeisdk.tool.PreferencesTools;

public class NewTaipeiSDKActivity extends BaseBleActivity implements BeaconConsumer, BluetoothAdapter.LeScanCallback {

    List<M4BeaconWithCounter> currentBeacons = new ArrayList<M4BeaconWithCounter>();
    public static String TAG = "NewTaipeiSDKActivity";
    public static final int REQUEST_PERMISSIONS = 1;
    public static final String LOG_TAG = "NewTaipei_LOG";
    final int PUNCH_TIME_OUT = 10000;
    private BeaconManager mBeaconManager;
    private HandlerThread mBBHandlerThread;
    private Handler mBBHandler;
    private HandlerThread mTimeoutHandlerThread;
    private Handler mTimeoutHandler;
    private BluetoothAdapter mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothLeScanner bluetoothLeScanner = mBTAdapter.getBluetoothLeScanner();
    final List<String> NLPI_BEACON_MAJOR_LIST = new ArrayList<String>() {{
        add("7016");
    }};
    public static final List<String> BEACON_ID_LIST = new ArrayList<>();
    public static final ArrayList<BeaconInfoData> BEACON_LIST = new ArrayList<>();
    public static BeaconInfoData beaconInfoData = new BeaconInfoData();
    public static final String BEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:29-29,i:34-39,i:40-41,i:46-47";
    private static final float beaconTrigger10 = 10f;
    private static final int beaconNum = 1620;
    private static final String uid = "F0CE7A96E6A4";
    final int MSG_LE_START_SCAN = 1000;
    final int MSG_LE_STOP_SCAN = 1001;
    final int MSG_GET_DATA = 1002;
    final int MSG_STOP_SCAN = 1003;
    public static String username;
    public static String userid;
    public static String uuid;
    public static String major;
    public static String minor;
    public static String hwid;
    public static int selectPos = -1;
    public static String beaconName = "";
    public static Boolean beaconSelect = false;
    public static Boolean byHand = false;
    private TextView punch_time_service_TV;
    private TextView query_the_records_TV;
    private TextView outside_range_TV;
    private TextView message_TV;
    private String ARG_KEY_USERNAME = "arg_key_username";
    private String ARG_KEY_USERID = "arg_key_userid";
    public static final String WEBVIEW_TITLE = "key_website_title";
    public static final String WEBVIEW_URL = "key_website_url";
    private String mLastSendBatteryMac;
    private ArrayList<String> mSendBatteryMac;
    public static String mLastSendBatteryId = "mLastSendBatteryId";
    private ArrayList<String> mSendBatteryId;
    public static int randomNum;
    private Long currentTime = 0L;
    private Long lastScanTime = 0L;
    private boolean checkBluetooth = false;
    private boolean openBluetoothHint = false;
    private boolean isResumed = false;
    private boolean isActive = false;
    private boolean isResumedFirst = false;
    private BluetoothAdapter bluetoothAdapter;
    private TextView tvLog;
    private ScrollView svLog;
    private LinearLayout layoutLogPanel;
    private StringBuilder logBuffer = new StringBuilder();
    private static final int MAX_LOG_LINES = 20;
    private int logLineCount = 0;
    private BeaconInfoData[] mBeaconInfoData;
    private boolean isClockBeacon = false;
    private boolean userPermission = false;
    public static int randomLevel = 1;
    private TextView decrypt_TV;
    private TextView userNameTV;
    private static final int KEY_SETUP_UNLOCK_TAP_COUNT = 5;
    private int keySetupTapCount = 0;

    private ViewPager infoImageViewPager;
    private TabLayout infoImageTableLayout;
    private PicPagerAdapter mPicPagerAdapter;
    private Handler mTimeHandler;
    private int mCountTime = 0;
    private int mPicVPIndex = 0;
    private Runnable timerRun = new Runnable() {
        @Override
        public void run() {
            ++mCountTime;
            if (mCountTime == 3 && infoImageViewPager != null) {
                mCountTime = 0;
                mPicVPIndex = infoImageViewPager.getCurrentItem();
                if (mPicPagerAdapter != null) {
                    if (mPicVPIndex < mPicPagerAdapter.getCount() - 1) {
                        mPicVPIndex++;
                    } else {
                        mPicVPIndex = 0;
                    }
                    infoImageViewPager.setCurrentItem(mPicVPIndex, true);
                }
            }
            mTimeHandler.removeCallbacks(this);
            mTimeHandler.postDelayed(this, 1000);
        }
    };

    private TextView timeTextView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            String currentTime = new SimpleDateFormat("yyyy/MM/dd EEEE HH:mm:ss", Locale.getDefault()).format(new Date());
            timeTextView.setText(currentTime);
            handler.postDelayed(this, 1000); // 每隔 1 秒更新一次
        }
    };

    private ImageView menuIV;

    @Override
    public void onReceivedBeacon(List<M4BeaconWithCounter> resultBeacons) {
//        Log.e(TAG, "onReceivedBeacon");
        currentBeacons.clear();
        currentBeacons.addAll(resultBeacons);

        decrypt_TV.setText("Have Found bacons total of = " + beaconSize +
                "\nI see a beacon transmitting\nnamespace id: "
                + namespaceId + "\nname:" + shortName +
                "\nand major id: " + majorId + "\nand minor id: " + minorId +
                "\nisClockBeacon：" + isClockBeacon + "\nrealUid：" + realUid);
    }

    @Override
    public String onDecryptBeacon(String rawUid) {
        Log.e(TAG, "onDecryptBeacon");
        byte[] keyBytes = M4Beacon.hexStringToByteArray(getAESKey().toString());//from native C string
        byte[] ivBytes = M4Beacon.hexStringToByteArray(getIVector().toString());//from native C string

        //get UID based on AESKEY and IV
        String realUid = Decrypt(rawUid, keyBytes, ivBytes);
        Log.e(TAG, "realUid" + realUid);

        isClockBeacon = false;
        if (mBeaconInfoData != null) {
            for (BeaconInfoData beaconInfoData : mBeaconInfoData) {
                if (beaconInfoData.getHWID().equals(realUid) && beaconInfoData.getClockEnabled().equals("Y")) {
                    isClockBeacon = true;
                    randomLevel = beaconInfoData.getUPDTE_RATE();
                    BEACON_ID_LIST.add(realUid);
                    boolean duplicate = false;
                    if (!BEACON_LIST.contains(beaconInfoData)) {
                        for (BeaconInfoData data : BEACON_LIST)
                            if (data.getDESC().equals(beaconInfoData.getDESC())) {
                                duplicate = true;
                                break;
                            }
                        if (!duplicate)
                            BEACON_LIST.add(beaconInfoData);
                    }
                    lastScanTime = Calendar.getInstance().getTime().getTime();
                    break;
                }
            }
        }

        Log.e(TAG, "userPermission" + userPermission);
        if (isClockBeacon && !isActive && userPermission) {
//            hwid = realUid;
            isActive = true;
            outside_range_TV.setVisibility(View.GONE);
            punch_time_service_TV.setBackgroundResource(R.mipmap.btn_bg_yellow_m);
            punch_time_service_TV.setClickable(true);
//            punch_time_service_TV.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.ntsdk_activity_main_fl, ServiceFragment.newInstance())
//                            .addToBackStack(null)
//                            .commit();
//                }
//            });
//            lastScanTime = Calendar.getInstance().getTime().getTime();
        }

        return realUid;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ntsdk_activity_main);

        EditText etVendorKey = findViewById(R.id.et_vendor_key);
        EditText etLotKey = findViewById(R.id.et_lot_key);
        LinearLayout layoutMainContent = findViewById(R.id.layout_main_content);
        FrameLayout layoutKeySetup = findViewById(R.id.layout_key_setup);
        ImageView ivKeySetupClose = findViewById(R.id.iv_key_setup_close);
        Button btnConfirmKey = findViewById(R.id.btn_confirm_key);
        layoutLogPanel = findViewById(R.id.layout_log_panel);
        tvLog = findViewById(R.id.tv_log);
        svLog = findViewById(R.id.sv_log);
        layoutMainContent.setVisibility(View.VISIBLE);
        layoutKeySetup.setVisibility(View.GONE);
        findViewById(R.id.view_key_setup_trigger).setOnClickListener(v -> {
            keySetupTapCount++;
            if (keySetupTapCount >= KEY_SETUP_UNLOCK_TAP_COUNT) {
                keySetupTapCount = 0;
                layoutKeySetup.setVisibility(View.VISIBLE);
                layoutLogPanel.setVisibility(View.VISIBLE);
            }
        });
        ivKeySetupClose.setOnClickListener(v -> {
            keySetupTapCount = 0;
            layoutKeySetup.setVisibility(View.GONE);
            layoutLogPanel.setVisibility(View.GONE);
            logBuffer.setLength(0);
            logLineCount = 0;
            tvLog.setText("");
        });
        layoutLogPanel.setVisibility(View.GONE);

        findViewById(R.id.tv_log_clear).setOnClickListener(v -> {
            logBuffer.setLength(0);
            logLineCount = 0;
            tvLog.setText("");
        });
// 帶入預設值
        etVendorKey.setText(lineVendorKeyHex);
        etLotKey.setText(lineLotKeyHex);

        btnConfirmKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vendorInput = etVendorKey.getText().toString().trim();
                String lotInput = etLotKey.getText().toString().trim();

                // 簡單驗證
                if (vendorInput.length() != 8) {
                    Toast.makeText(NewTaipeiSDKActivity.this,
                            "Vendor Key 必須是 8 碼 Hex", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (lotInput.length() != 16) {
                    Toast.makeText(NewTaipeiSDKActivity.this,
                            "Lot Key 必須是 16 碼 Hex", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 儲存 Key
                lineVendorKeyHex = vendorInput;
                lineLotKeyHex = lotInput;

                // 隱藏設定面板，顯示主畫面
                keySetupTapCount = 0;
                // 顯示主 LinearLayout
                ((LinearLayout) ((FrameLayout) findViewById(R.id.ntsdk_activity_main_fl))
                        .getChildAt(0)).setVisibility(View.VISIBLE);
                // 顯示其他元件
                findViewById(R.id.ntsdk_activity_main_tv_outside_range).setVisibility(View.VISIBLE);
                findViewById(R.id.ntsdk_activity_main_tv_message).setVisibility(View.VISIBLE);

                Toast.makeText(NewTaipeiSDKActivity.this,
                        "Key 已設定，開始掃描", Toast.LENGTH_SHORT).show();

                // 啟動掃描
            }
        });

        super.ProximityUUID = "26cbdba2-8dd8-4e54-adf4-b51f0caea6e6";
        super.ProcessLengthSec = 10; //10 second to clear not found beacon

        username = getIntent().getStringExtra(ARG_KEY_USERNAME);
        userid = getIntent().getStringExtra(ARG_KEY_USERID);

        userNameTV = findViewById(R.id.userName);
        userNameTV.setText(username);

        NewTaipeiSDKApi.getInstance().getBanner(this,
                new NetworkManager.NetworkManagerListener<BannerData[]>() {
                    @Override
                    public void onSucceed(BannerData[] bannerData) {
                        Log.e(LOG_TAG, "bannerData" + bannerData.length);
                        ArrayList<BannerData> banner = new ArrayList<>(Arrays.asList(bannerData));
                        infoImageViewPager = findViewById(R.id.fragment_home_vp_pic);
                        infoImageTableLayout = findViewById(R.id.fragment_home_tl);
                        infoImageTableLayout.setupWithViewPager(infoImageViewPager, true);
                        mPicPagerAdapter = new PicPagerAdapter(NewTaipeiSDKActivity.this, banner);
                        infoImageViewPager.setAdapter(mPicPagerAdapter);

                        if (mTimeHandler == null) {
                            mTimeHandler = new Handler();
                            mTimeHandler.postDelayed(timerRun, 3000);
                        }
                    }

                    @Override
                    public void onFail(String errorMsg, boolean shouldRetry) {
                        Log.e(LOG_TAG, "bannerData" + errorMsg);
                    }
                });

        timeTextView = findViewById(R.id.timeTextView);
        handler.post(updateTimeRunnable); // 啟動定時任務

        menuIV = findViewById(R.id.menu);
        menuIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFragmentPage(MenuFragment.Companion.newInstance(), MenuFragment.TAG);
            }
        });

//        for (int i = 1601; i <= beaconNum; i++) {
//            BEACON_ID_LIST.add(String.valueOf(i));
//        }

        punch_time_service_TV = findViewById(R.id.ntsdk_activity_main_tv_punch_time_service);
        query_the_records_TV = findViewById(R.id.ntsdk_activity_main_tv_query_the_records);
        outside_range_TV = findViewById(R.id.ntsdk_activity_main_tv_outside_range);
        message_TV = findViewById(R.id.ntsdk_activity_main_tv_message);
        decrypt_TV = findViewById(R.id.ntsdk_activity_main_decrypt_tv);

        findViewById(R.id.ntsdk_activity_main_fl_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        NewTaipeiSDKApi.getInstance().getConfig(this,
                new NetworkManager.NetworkManagerListener<ConfigResponse>() {
                    @Override
                    public void onSucceed(ConfigResponse configResponse) {
//                        outside_range_TV.setText(configResponse.getApp_error_message());
                    }

                    @Override
                    public void onFail(String errorMsg, boolean shouldRetry) {
                        Log.e(LOG_TAG, "getConfig" + errorMsg);
                    }
                });

        NewTaipeiSDKApi.getInstance().checkEnabled(this, userid,
                new NetworkManager.NetworkManagerListener<PermissionResponse>() {
                    @Override
                    public void onSucceed(PermissionResponse response) {
                        if (response.getResult())
                            userPermission = true;
                        else
                            DialogTools.getInstance().showErrorMessage(NewTaipeiSDKActivity.this,
                                    R.string.important_message, response.getMessage());
                    }

                    @Override
                    public void onFail(String errorMsg, boolean shouldRetry) {
                    }
                });

        NewTaipeiSDKApi.getInstance().getBeaconInfo(this, userid,
                new NetworkManager.NetworkManagerListener<BeaconInfoData[]>() {
                    @Override
                    public void onSucceed(BeaconInfoData[] beaconInfoData) {
                        mBeaconInfoData = beaconInfoData;
//                Toast.makeText(NewTaipeiSDKActivity.this,
//                        "mBeaconInfoData size is : " + mBeaconInfoData.length, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFail(String errorMsg, boolean shouldRetry) {
//                Toast.makeText(NewTaipeiSDKActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });

        NewTaipeiSDKApi.getInstance().getMessage(this,
                new NetworkManager.NetworkManagerListener<MessageResponse>() {
                    @Override
                    public void onSucceed(MessageResponse response) {
                        if (response.getResult().equals("success")) {
                            message_TV.setText(response.getData());
                        }
                    }

                    @Override
                    public void onFail(String errorMsg, boolean shouldRetry) {
                    }
                });

        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.setAndroidLScanningDisabled(true);
        mBeaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_LAYOUT));
        mBeaconManager.bind(this);

        query_the_records_TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                UserLoginInfo userInfo = getLoginInfoOrOpenLogin();
//                if (userInfo == null) {
//                    return;
//                }

//                NewTaipeiSDKApi.getInstance().verifyLoginToken(NewTaipeiSDKActivity.this,
//                        userInfo.getAccount(),
//                        userInfo.getToken(), "N",
//                        new NetworkManager.NetworkManagerListener<CommonResponse>() {
//                            @Override
//                            public void onSucceed(CommonResponse response) {
//                                if (response.getResult().equals("true")) {
                                    getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.ntsdk_activity_main_fl, QueryFragment.newInstance())
                                            .addToBackStack(null)
                                            .commit();
//                                } else {
//                                    Dialog dialog = new Dialog(NewTaipeiSDKActivity.this);
//                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                                    dialog.setContentView(R.layout.dialog_confirm);
//                                    dialog.setCancelable(false);
//                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//                                    int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
//                                    dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.MATCH_PARENT);
//
//                                    TextView title = dialog.findViewById(R.id.title);
//                                    title.setText("提示");
//
//                                    String error = response.getErrorMessage();
//                                    if (error.contains("ACCESS_DENY(t)")) {
//                                        error = "登入資訊已失效，請重新登入";
//                                    } else if (error.contains("ACCESS_DENY")) {
//                                        error = "安全驗證失敗";
//                                    } else if (error.contains("INVALID_PARAMETER")) {
//                                        error = "傳入參數錯誤";
//                                    } else if (error.contains("INVALID_LDAP")) {
//                                        error = "伺服器連接失敗";
//                                    }
//                                    TextView content = dialog.findViewById(R.id.content);
//                                    content.setText(error);
//
//                                    TextView btn = dialog.findViewById(R.id.btn);
//                                    btn.setText("確認");
//                                    btn.setOnClickListener(v -> {
//                                        dialog.dismiss();
//                                        PreferencesTools.getInstance().saveProperty(
//                                                NewTaipeiSDKActivity.this,
//                                                PreferencesTools.KEY_PREFERENCES_KEEP_PASSWORD, "false"
//                                        );
//                                        PreferencesTools.getInstance().saveProperty(
//                                                NewTaipeiSDKActivity.this,
//                                                PreferencesTools.KEY_PREFERENCES_BIOMETRIC, "false"
//                                        );
//                                        UserInfoManager.getInstance().userLogout(NewTaipeiSDKActivity.this);
//
//                                        Intent intent = new Intent(NewTaipeiSDKActivity.this, LoginActivity.class);
//                                        startActivity(intent);
//                                        finish();
//                                    });
//
//                                    dialog.show();
//                                }
//                            }
//
//                            @Override
//                            public void onFail(String errorMsg, boolean shouldRetry) {
//
//                            }
//                        });
            }
        });

        punch_time_service_TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                UserLoginInfo userInfo = getLoginInfoOrOpenLogin();
//                if (userInfo == null) {
//                    return;
//                }

//                NewTaipeiSDKApi.getInstance().verifyLoginToken(NewTaipeiSDKActivity.this,
//                        userInfo.getAccount(),
//                        userInfo.getToken(), "N",
//                        new NetworkManager.NetworkManagerListener<CommonResponse>() {
//                            @Override
//                            public void onSucceed(CommonResponse response) {
//                                if (response.getResult().equals("true")) {
                                    getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.ntsdk_activity_main_fl, ServiceFragment.newInstance())
                                            .addToBackStack(null)
                                            .commit();
//                                } else {
//                                    Dialog dialog = new Dialog(NewTaipeiSDKActivity.this);
//                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                                    dialog.setContentView(R.layout.dialog_confirm);
//                                    dialog.setCancelable(false);
//                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//                                    int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
//                                    dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.MATCH_PARENT);
//
//                                    TextView title = dialog.findViewById(R.id.title);
//                                    title.setText("提示");
//
//                                    String error = response.getErrorMessage();
//                                    if (error.contains("ACCESS_DENY(t)")) {
//                                        error = "登入資訊已失效，請重新登入";
//                                    } else if (error.contains("ACCESS_DENY")) {
//                                        error = "安全驗證失敗";
//                                    } else if (error.contains("INVALID_PARAMETER")) {
//                                        error = "傳入參數錯誤";
//                                    } else if (error.contains("INVALID_LDAP")) {
//                                        error = "伺服器連接失敗";
//                                    }
//                                    TextView content = dialog.findViewById(R.id.content);
//                                    content.setText(error);
//
//                                    TextView btn = dialog.findViewById(R.id.btn);
//                                    btn.setText("確認");
//                                    btn.setOnClickListener(v -> {
//                                        dialog.dismiss();
//                                        PreferencesTools.getInstance().saveProperty(
//                                                NewTaipeiSDKActivity.this,
//                                                PreferencesTools.KEY_PREFERENCES_KEEP_PASSWORD, "false"
//                                        );
//                                        PreferencesTools.getInstance().saveProperty(
//                                                NewTaipeiSDKActivity.this,
//                                                PreferencesTools.KEY_PREFERENCES_BIOMETRIC, "false"
//                                        );
//                                        UserInfoManager.getInstance().userLogout(NewTaipeiSDKActivity.this);
//
//                                        Intent intent = new Intent(NewTaipeiSDKActivity.this, LoginActivity.class);
//                                        startActivity(intent);
//                                        finish();
//                                    });
//
//                                    dialog.show();
//                                }
//                            }
//
//                            @Override
//                            public void onFail(String errorMsg, boolean shouldRetry) {
//
//                            }
//                        });
            }
        });

        mSendBatteryMac = new ArrayList<>();
        mSendBatteryId = new ArrayList<>();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        checkLocationService();
        checkBluetoothOn();

        mTimeoutHandlerThread = new HandlerThread("HandlerThread");
        mTimeoutHandlerThread.start();
        mTimeoutHandler = new Handler(mTimeoutHandlerThread.getLooper());
        mTimeoutHandler.postDelayed(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                currentTime = Calendar.getInstance().getTime().getTime();
//                Log.e(TAG, "bluetoothAdapter.isEnabled()" + bluetoothAdapter.isEnabled());
                if (currentTime - lastScanTime > PUNCH_TIME_OUT) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            isActive = false;
                            punch_time_service_TV.setBackgroundResource(R.mipmap.btn_bg_gray_m);
                            punch_time_service_TV.setClickable(false);
                            outside_range_TV.setVisibility(View.VISIBLE);
                            if (ServiceFragment.isServiceFragment && isResumed) {
                                getSupportFragmentManager().popBackStack();
                            }
                        }
                    });
                }
                if (!bluetoothAdapter.isEnabled()) {
                    checkBluetooth = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            isActive = false;
                            punch_time_service_TV.setBackgroundResource(R.mipmap.btn_bg_gray_m);
                            punch_time_service_TV.setClickable(false);
                            outside_range_TV.setVisibility(View.VISIBLE);
                            if (ServiceFragment.isServiceFragment) {
                                getSupportFragmentManager().popBackStack();
                            }
                        }
                    });
                    if (!openBluetoothHint) {
                        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBluetoothIntent, 77);
                        openBluetoothHint = true;
                    }
                }
                if (bluetoothAdapter.isEnabled() && !checkBluetooth) {
//                    startScanBeacon();
                    checkBluetooth = true;
                    openBluetoothHint = false;
                }
//                Log.e(TAG, "currentTime" + currentTime);
//                Log.e(TAG, "lastScanTime" + lastScanTime);
                mTimeoutHandler.postDelayed(this, 1000);
            }
        }, 1000);

        randomNum = (int) (Math.random() * 99);
    }

    private UserLoginInfo getLoginInfoOrOpenLogin() {
        UserLoginInfo userInfo = UserInfoManager.getInstance().getUserInfo(this);
        if (userInfo != null && userInfo.getAccount() != null && userInfo.getToken() != null) {
            return userInfo;
        }

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
        return null;
    }

    @Override
    public void onPause() {
        super.onPause();
        isResumed = false;

        Log.e(LOG_TAG, "onPause");
    }

    @Override
    public void onResume() {
        Log.e(LOG_TAG, "onResume");
        super.onResume();
        isResumed = true;

//        if (!isResumedFirst) {
//            isResumedFirst = true;
//        } else {
//            Intent intent = new Intent(NewTaipeiSDKActivity.this, LoginActivity.class);
//            startActivity(intent);
//            finish();
//        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onDestroy() {
        Log.e(LOG_TAG, "onDestroy");
        super.onDestroy();
        if (mBeaconManager != null) {
            mBeaconManager.unbind(this);
        }
        if (mBBHandler != null) {
            mBBHandler.removeMessages(MSG_LE_START_SCAN);
            mBBHandler.removeMessages(MSG_LE_STOP_SCAN);
        }
        if (mBTAdapter.isEnabled()) {
            mBTAdapter.stopLeScan(this);
        }
        if (mBBHandlerThread != null && mBBHandlerThread.isAlive()) {
            mBBHandlerThread.quit();
        }
        if (mTimeoutHandlerThread != null && mTimeoutHandlerThread.isAlive()) {
            mTimeoutHandlerThread.quit();
        }
        if (mTimeHandler != null && timerRun != null) {
            mTimeHandler.removeCallbacks(timerRun);
        }
        if (handler != null && updateTimeRunnable != null) {
            handler.removeCallbacks(updateTimeRunnable); // 停止定時任務，防止內存洩漏
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.e(TAG, "onBeaconServiceConnect");
        Region region = new Region("all-beacons-region", Identifier.parse(ProximityUUID), null, null);
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.setRangeNotifier(this);

        mBeaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
            }

            @Override
            public void didExitRegion(Region region) {
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
            }
        });
        try {
            mBeaconManager.startMonitoringBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLeScan(final BluetoothDevice device, int i, byte[] scanRecord) {
        Log.e(TAG, "onLeScan");

        OmniguiderData omniguiderData = null;
        try {
            omniguiderData = OmniguiderData.generateiBeacon(scanRecord);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        @SuppressLint("MissingPermission") String devName = device.getName();
        if (omniguiderData != null && mBeaconInfoData != null) {
            Log.d(TAG, "name:" + devName + ",User id:" + omniguiderData.userID + ",HW id:" + omniguiderData.hwID + ",TimeStamp:" + omniguiderData.TimeStamp
                    + ",Stamp:" + omniguiderData.Stamp + ",voltage:" + omniguiderData.voltage + " V\n");

            isClockBeacon = false;
            if (mBeaconInfoData != null) {
                for (BeaconInfoData beaconInfoData : mBeaconInfoData) {
                    if (beaconInfoData.getHWID().equals(omniguiderData.hwID) && beaconInfoData.getClockEnabled().equals("Y")) {
                        isClockBeacon = true;
                        randomLevel = beaconInfoData.getUPDTE_RATE();
                        boolean duplicate = false;
                        if (!BEACON_LIST.contains(beaconInfoData)) {
                            for (BeaconInfoData data : BEACON_LIST)
                                if (data.getDESC().equals(beaconInfoData.getDESC())) {
                                    duplicate = true;
                                    break;
                                }
                            if (!duplicate)
                                BEACON_LIST.add(beaconInfoData);
                        }
                        break;
                    }
                }
            }
            if (isClockBeacon && bluetoothAdapter.isEnabled() && userPermission) {
                outside_range_TV.setVisibility(View.GONE);
                punch_time_service_TV.setBackgroundResource(R.mipmap.btn_bg_yellow_m);
                punch_time_service_TV.setClickable(true);
                lastScanTime = Calendar.getInstance().getTime().getTime();
            }

            if (!omniguiderData.hwID.equals(mLastSendBatteryId) && !mSendBatteryId.contains(omniguiderData.hwID) && randomNum < randomLevel) {
                mSendBatteryId.add(omniguiderData.hwID);
                Log.v(TAG, "setBeaconBatteryLevel");
                final OmniguiderData finalOmniguiderData = omniguiderData;
                NewTaipeiSDKApi.getInstance().setBeaconBatteryLevel(NewTaipeiSDKActivity.this,
                        omniguiderData.hwID,
                        omniguiderData.voltage + "",
                        new NetworkManager.NetworkManagerListener<SendBeaconBatteryResponse>() {
                            @Override
                            public void onSucceed(SendBeaconBatteryResponse response) {
                                if (response.isSuccess()) {
                                    mLastSendBatteryId = finalOmniguiderData.hwID;
                                }
                            }

                            @Override
                            public void onFail(String errorMsg, boolean shouldRetry) {

                            }
                        });
            }

        } else {
            //BLEData
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS && grantResults.length != 0
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.startScan(mScanCallback);
                startScanBeacon();
            }
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            byte[] scanRecord = result.getScanRecord().getBytes();
            final BluetoothDevice device = result.getDevice();
//            Log.d(TAG, "onScanResult" + scanRecord);

            // 1. 嘗試解析原本的 OmniguiderData
            OmniguiderData omniguiderData = null;
            try {
                omniguiderData = OmniguiderData.generateiBeacon(scanRecord);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 用來儲存最終識別到的 HWID
            String targetHwid = null;

            // ---------------------------------------------------------
            // 邏輯 A: 處理原本的 Omniguider Beacon
            // ---------------------------------------------------------
            if (omniguiderData != null) {
                targetHwid = omniguiderData.hwID;

                // --- 原本的電量上傳邏輯 (僅針對 Omniguider，因為有 voltage 欄位) ---
                if (!targetHwid.equals(mLastSendBatteryId) && !mSendBatteryId.contains(targetHwid) && randomNum < randomLevel) {
                    mSendBatteryId.add(targetHwid);
                    Log.v(TAG, "setBeaconBatteryLevel");
                    final OmniguiderData finalOmniguiderData = omniguiderData;
                    NewTaipeiSDKApi.getInstance().setBeaconBatteryLevel(NewTaipeiSDKActivity.this,
                            omniguiderData.hwID,
                            omniguiderData.voltage + "",
                            new NetworkManager.NetworkManagerListener<SendBeaconBatteryResponse>() {
                                @Override
                                public void onSucceed(SendBeaconBatteryResponse response) {
                                    if (response.isSuccess()) {
                                        mLastSendBatteryId = finalOmniguiderData.hwID;
                                    }
                                }

                                @Override
                                public void onFail(String errorMsg, boolean shouldRetry) {
                                }
                            });
                }
            }
            // ---------------------------------------------------------
            // 邏輯 B: 處理 Beacon (修正後，對齊 iOS 的 SHA-256 + XOR-Fold + 暴力時間戳)
            // ---------------------------------------------------------
            // 使用上方定義的 LINE_UUID 常數
            if (result.getScanRecord() != null) {

                byte[] serviceData = null;
                Map<ParcelUuid, byte[]> allServiceData = result.getScanRecord().getServiceData();
                if (allServiceData != null) {
                    for (Map.Entry<ParcelUuid, byte[]> entry : allServiceData.entrySet()) {
                        if (entry.getKey().toString().toLowerCase().contains("fe6f")) {
                            serviceData = entry.getValue();
                            String msg = "找到 LINE serviceData: " + bytesToHex(serviceData);
                            Log.d(TAG, msg);
                            appendLog(msg);
                            break;
                        }
                    }
                }

                if (serviceData == null) {
                    return;
                }

                LineFields fields = parseLineSecureServiceData(serviceData);
                if (fields == null) {
                    String msg = "parseLineSecureServiceData null";
                    Log.w(TAG, msg);
                    appendLog("⚠️ " + msg);
                    return;
                }

                byte[] vendorKey = hexToBytes(lineVendorKeyHex);
                byte[] lotKey = hexToBytes(lineLotKeyHex);

                // 印出解析資訊
                StringBuilder sb = new StringBuilder();
                for (byte b : fields.hwid5) sb.append(String.format("%02x", b));
                String hwid = sb.toString();
                appendLog("HWID: " + hwid);
                appendLog("Battery: " + (fields.battery & 0xFF));
                appendLog("MaskedTS: " + fields.maskedTs);

                Long verifiedTimestamp = verifyLineSecureMessage(serviceData, vendorKey, lotKey);
                Log.d(TAG, "verifiedTimestamp=" + verifiedTimestamp);

                if (verifiedTimestamp == null) {
                    String msg = "❌ Beacon 驗證失敗 (Key 錯誤或假訊號)";
                    Log.w(TAG, msg);
                    appendLog(msg);
                    appendLog("VendorKey: " + lineVendorKeyHex);
                    appendLog("LotKey:    " + lineLotKeyHex);
                    return;
                }

                appendLog("✅ 驗證成功 TS=" + verifiedTimestamp);
                appendLog("HWID: " + hwid);

                // === 驗證成功 ===
                isClockBeacon = false;
                if (mBeaconInfoData != null) {
                    targetHwid = hwid;

                    for (BeaconInfoData beaconInfoData : mBeaconInfoData) {
                        if (beaconInfoData.getHWID().equals(targetHwid) && beaconInfoData.getClockEnabled().equals("Y")) {
                            isClockBeacon = true;
                            randomLevel = beaconInfoData.getUPDTE_RATE();
                            appendLog("✅ isClockBeacon matched: " + targetHwid);
                            boolean duplicate = false;
                            if (!BEACON_LIST.contains(beaconInfoData)) {
                                for (BeaconInfoData data : BEACON_LIST)
                                    if (data.getDESC().equals(beaconInfoData.getDESC())) {
                                        duplicate = true;
                                        break;
                                    }
                                if (!duplicate)
                                    BEACON_LIST.add(beaconInfoData);
                            }
                            break;
                        }
                    }
                    if (!isClockBeacon) {
                        appendLog("⚠️ HWID 未在 BeaconInfo 找到: " + targetHwid);
                    }
                } else {
                    appendLog("⚠️ mBeaconInfoData 為 null");
                }

                if (isClockBeacon && bluetoothAdapter.isEnabled() && userPermission) {
                    outside_range_TV.setVisibility(View.GONE);
                    punch_time_service_TV.setBackgroundResource(R.mipmap.btn_bg_yellow_m);
                    punch_time_service_TV.setClickable(true);
                    lastScanTime = Calendar.getInstance().getTime().getTime();
                    appendLog("🟢 打卡按鈕已啟用");
                }

                targetHwid = hwid;
                Log.v(TAG, "Beacon Verified! HWID: " + targetHwid + " Timestamp: " + verifiedTimestamp);

                int batteryHex = fields.battery & 0xFF;
                String batteryVal = String.valueOf(batteryHex);

                if (!targetHwid.equals(mLastSendBatteryId)
                        && !mSendBatteryId.contains(targetHwid)
                        && randomNum < randomLevel) {

                    mSendBatteryId.add(targetHwid);
                    final String finalHwid = targetHwid;

                    NewTaipeiSDKApi.getInstance().setBeaconBatteryLevel(NewTaipeiSDKActivity.this,
                            finalHwid,
                            batteryVal,
                            new NetworkManager.NetworkManagerListener<SendBeaconBatteryResponse>() {
                                @Override
                                public void onSucceed(SendBeaconBatteryResponse response) {
                                    if (response.isSuccess()) {
                                        mLastSendBatteryId = finalHwid;
                                        appendLog("🔋 電量上傳成功: " + finalHwid);
                                    }
                                }

                                @Override
                                public void onFail(String errorMsg, boolean shouldRetry) {
                                    appendLog("🔋 電量上傳失敗: " + errorMsg);
                                }
                            });
                }
            }

            // ---------------------------------------------------------
            // 共用邏輯: 比對 HWID 與 觸發打卡按鈕 (Omniguider 與 LINE 通用)
            // ---------------------------------------------------------
            @SuppressLint("MissingPermission") String devName = device.getName();

            // 只要有解析出 HWID (targetHwid) 且有後台資料 (mBeaconInfoData) 就進行比對
            if (targetHwid != null && mBeaconInfoData != null) {

                // (Optional) 保留您原本的 Log 格式，但如果是 Beacon，部分欄位可能是空的
                if (omniguiderData != null) {
                    Log.d(TAG, "name:" + devName + ",User id:" + omniguiderData.userID + ",HW id:" + omniguiderData.hwID
                            + ",TimeStamp:" + omniguiderData.TimeStamp + ",Stamp:" + omniguiderData.Stamp + ",voltage:" + omniguiderData.voltage + " V\n");
                }

                isClockBeacon = false;
                for (BeaconInfoData beaconInfoData : mBeaconInfoData) {
                    // 使用 equalsIgnoreCase 避免大小寫差異導致比對失敗
                    if (beaconInfoData.getHWID().equalsIgnoreCase(targetHwid) && beaconInfoData.getClockEnabled().equals("Y")) {
                        isClockBeacon = true;
                        randomLevel = beaconInfoData.getUPDTE_RATE();
                        boolean duplicate = false;
                        if (!BEACON_LIST.contains(beaconInfoData)) {
                            for (BeaconInfoData data : BEACON_LIST)
                                if (data.getDESC().equals(beaconInfoData.getDESC())) {
                                    duplicate = true;
                                    break;
                                }
                            if (!duplicate)
                                BEACON_LIST.add(beaconInfoData);
                        }
                        break;
                    }
                }

                if (isClockBeacon && bluetoothAdapter.isEnabled() && userPermission) {
                    outside_range_TV.setVisibility(View.GONE);
                    punch_time_service_TV.setBackgroundResource(R.mipmap.btn_bg_yellow_m);
                    punch_time_service_TV.setClickable(true);

                    // 更新最後掃描時間
                    lastScanTime = Calendar.getInstance().getTime().getTime();
                }
            } else {
                //BLEData
                //Log.d("debug", "BLE Data:" + bytesToHex(scanRecord));
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    public void startScanBeacon() {
        Log.e(LOG_TAG, "startScanBeacon mBTAdapter" + mBTAdapter.isEnabled());
        mBBHandlerThread = new HandlerThread("HandlerThread");
        mBBHandlerThread.start();
        mBBHandler = new Handler(mBBHandlerThread.getLooper()) {
            @SuppressLint("MissingPermission")
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_LE_START_SCAN:
                        if (mBTAdapter.isEnabled()) {
//                            mBTAdapter.startLeScan(NewTaipeiSDKActivity.this);
                            if (bluetoothLeScanner != null) {
                                if (ActivityCompat.checkSelfPermission(NewTaipeiSDKActivity.this, Manifest.permission.BLUETOOTH_SCAN)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                if (bluetoothLeScanner != null) {
                                    bluetoothLeScanner.startScan(mScanCallback);
                                }
                            }
                        }
                        break;

                    case MSG_LE_STOP_SCAN:
                        if (mBTAdapter.isEnabled()) {
//                            mBTAdapter.stopLeScan(NewTaipeiSDKActivity.this);
                            if (bluetoothLeScanner != null) {
                                bluetoothLeScanner.stopScan(mScanCallback);
                            }
                        }
                        break;

                    case MSG_STOP_SCAN:
                        mBBHandler.removeMessages(MSG_LE_START_SCAN);
                        mBBHandler.removeMessages(MSG_LE_STOP_SCAN);
                        if (mBTAdapter.isEnabled()) {
//                            mBTAdapter.stopLeScan(NewTaipeiSDKActivity.this);
                            if (bluetoothLeScanner != null) {
                                bluetoothLeScanner.stopScan(mScanCallback);
                            }
                        }
                        break;
                    case MSG_GET_DATA:
                        break;

                }
                super.handleMessage(msg);
            }
        };
        mBBHandler.sendEmptyMessage(MSG_LE_START_SCAN);
    }

    @SuppressLint("MissingPermission")
    private void checkBluetoothOn() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {

        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, 77);
                openBluetoothHint = true;
            }
        }
    }

    private void ensurePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                }, REQUEST_PERMISSIONS);
            } else {
                if (bluetoothLeScanner != null) {
                    bluetoothLeScanner.startScan(mScanCallback);
                    startScanBeacon();
                }
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_PERMISSIONS);
            } else {
                if (bluetoothLeScanner != null) {
                    bluetoothLeScanner.startScan(mScanCallback);
                    startScanBeacon();
                }
            }
        }
    }

    private void checkLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ensurePermissions();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("位置服務尚未開啟，請設定");
            dialog.setPositiveButton("open settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    AlertDialog.Builder dialog = new AlertDialog.Builder(NewTaipeiSDKActivity.this);
                    dialog.setMessage("沒有開啟位置服務，無法掃描藍芽設備");
                    dialog.show();
                }
            });
            dialog.show();
        }
    }

    private void openFragmentPage(Fragment fragment, String tag) {
        AnimationFragmentManager.getInstance().addFragmentPage(this,
                R.id.ntsdk_activity_main_fl, fragment, tag);
    }

    public float getBeaconTrigger() {
        return beaconTrigger10;
    }

    // 將 byte[] 轉成 Hex String (例如: 01deadbeef)
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


// =============================================================================================
    // START: Beacon SECURE MESSAGE VERIFICATION (Swift Porting)
    // =============================================================================================

    // 定義 Beacon 的 UUID
    private static final ParcelUuid LINE_UUID = ParcelUuid.fromString("0000fe6f-0000-1000-8000-00805f9b34fb");

    private String lineVendorKeyHex = "24968156";
    private String lineLotKeyHex = "0255691686013904";

    /**
     * 資料結構: 存放解析後的封包
     */
    private static class LineFields {
        final byte[] hwid5;
        final byte[] mac4;
        final int maskedTs; // 0~65535
        final byte battery;

        LineFields(byte[] hwid5, byte[] mac4, int maskedTs, byte battery) {
            this.hwid5 = hwid5;
            this.mac4 = mac4;
            this.maskedTs = maskedTs;
            this.battery = battery;
        }
    }

    /**
     * 1. 解析 Service Data
     */
    @Nullable
    private static LineFields parseLineSecureServiceData(byte[] serviceData) {
        if (serviceData == null || serviceData.length < 14) return null;
        if ((serviceData[0] & 0xFF) != 0x02) return null;

        byte[] hwid5 = Arrays.copyOfRange(serviceData, 1, 6);
        byte[] mac4 = Arrays.copyOfRange(serviceData, 7, 11);
        int maskedTs = ((serviceData[11] & 0xFF) << 8) | (serviceData[12] & 0xFF);
        byte battery = serviceData[13];

        return new LineFields(hwid5, mac4, maskedTs, battery);
    }

    /**
     * 2. 產生 Secure Message (包含 MAC, MaskedTS, Battery)
     * 對應 Swift 的 generate()
     */
    private static byte[] generateSecureMessage(long timestamp, byte[] hwid, byte[] vendorKey, byte[] lotKey, byte battery) {
        // Step 1: 拼接 26 bytes 輸入
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(26);
        bb.order(java.nio.ByteOrder.BIG_ENDIAN);
        bb.putLong(timestamp); // 8 bytes
        bb.put(hwid);          // 5 bytes
        bb.put(vendorKey);     // 4 bytes
        bb.put(lotKey);        // 8 bytes
        bb.put(battery);       // 1 byte

        // Step 2: SHA-256
        byte[] digest = sha256(bb.array());

        // Step 3: XOR-fold (32 -> 4 bytes)
        byte[] mac = xorFold(digest, 4);

        // Step 4: Masked Timestamp (2 bytes)
        byte[] maskedTS = new byte[2];
        maskedTS[0] = (byte) ((timestamp >> 8) & 0xFF);
        maskedTS[1] = (byte) (timestamp & 0xFF);

        // 回傳完整 7 bytes: [MAC(4)][MaskedTS(2)][Battery(1)]
        // 為了驗證方便，我們這裡其實只需要回傳 MAC 即可，但為了對齊 Swift 的測試結構，我們算好 MAC 比較方便
        return concat(mac, maskedTS, battery);
    }

    /**
     * 3. 驗證主邏輯 (對齊 Swift: 從 0 開始暴力搜尋)
     */
    @Nullable
    private static Long verifyLineSecureMessage(byte[] serviceData, byte[] vendorKey, byte[] lotKey) {
        LineFields f = parseLineSecureServiceData(serviceData);
        if (f == null) return null;

        String receivedMacHex = bytesToHex(f.mac4);
        long masked = (long) f.maskedTs;

        // =========================================================
        // 【新增 Log】印出解析出的資訊與「聽到的 MAC」
        // =========================================================
        Log.e(TAG, "=== Beacon 解析資訊 ===");
        Log.e(TAG, "HWID: " + bytesToHex(f.hwid5));
        Log.e(TAG, "Battery: " + (f.battery & 0xFF));
        Log.e(TAG, "MaskedTS: " + masked);
        Log.e(TAG, "【聽到的 Recv MAC】: " + receivedMacHex);

        // 先印出「用 MaskedTS 當作時間戳」第一次算出來的 MAC，讓你對照一下差異
        byte[] firstComputedMac = computeMacOnly(masked, f.hwid5, vendorKey, lotKey, f.battery);
        Log.e(TAG, "【第1次算出的 Calc MAC】(TS=" + masked + "): " + bytesToHex(firstComputedMac));


        // =========================================================
        // 策略 1: 搜尋 0 ~ 30億 (覆蓋標準 15 秒 Ticks, 或 Unix 秒數)
        // 約 45,000 次迴圈，手機約需 5~10 毫秒
        // =========================================================
        long maxCand1 = 3_000_000_000L;
        long candidate = masked;
        while (candidate <= maxCand1) {
            byte[] computedMac = computeMacOnly(candidate, f.hwid5, vendorKey, lotKey, f.battery);
            if (Arrays.equals(computedMac, f.mac4)) {
                Log.e(TAG, "MAC MATCH (Strategy 1)! TS=" + candidate + " | 算出的=" + bytesToHex(computedMac) + " | 聽到的=" + receivedMacHex);
                return candidate;
            }
            candidate += 65536L;
        }

        // =========================================================
        // 策略 2: 搜尋 Unix 毫秒級時間戳 (例如 Beacon 取 System.currentTimeMillis())
        // 考慮到手機與 Beacon 可能有誤差，往前、往後各搜 15 天 (約各 20,000 步)
        // =========================================================
        long nowMillis = System.currentTimeMillis();
        long baseMillis = nowMillis & ~0xFFFFL;
        long candidateMillis = baseMillis | masked;

        // 往前搜 15 天
        for (int i = 0; i <= 20000; i++) {
            long testCand = candidateMillis - (i * 65536L);
            if (testCand < 0) break;
            byte[] computedMac = computeMacOnly(testCand, f.hwid5, vendorKey, lotKey, f.battery);
            if (Arrays.equals(computedMac, f.mac4)) {
                Log.e(TAG, "MAC MATCH (Strategy 2: Backward)! TS=" + testCand + " | 算出的=" + bytesToHex(computedMac) + " | 聽到的=" + receivedMacHex);
                return testCand;
            }
        }

        // 往後搜 15 天 (防 Beacon 時間比手機快)
        for (int i = 1; i <= 20000; i++) {
            long testCand = candidateMillis + (i * 65536L);
            byte[] computedMac = computeMacOnly(testCand, f.hwid5, vendorKey, lotKey, f.battery);
            if (Arrays.equals(computedMac, f.mac4)) {
                Log.e(TAG, "MAC MATCH (Strategy 2: Forward)! TS=" + testCand + " | 算出的=" + bytesToHex(computedMac) + " | 聽到的=" + receivedMacHex);
                return testCand;
            }
        }

        Log.e(TAG, "驗證失敗: 所有策略皆找不到相符的 MAC。可能是 Key 錯誤，或是時間差異超過限制。");
        return null;
    }

    // 專門計算 MAC 的輕量方法 (優化效能用)
    private static byte[] computeMacOnly(long timestamp, byte[] hwid, byte[] vendorKey, byte[] lotKey, byte battery) {
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(26);
        bb.order(java.nio.ByteOrder.BIG_ENDIAN);
        bb.putLong(timestamp);
        bb.put(hwid);
        bb.put(vendorKey);
        bb.put(lotKey);
        bb.put(battery);

        byte[] digest = sha256(bb.array());
        return xorFold(digest, 4);
    }

    // XOR Fold: 對齊 Swift 的遞迴/迴圈邏輯
    private static byte[] xorFold(byte[] input, int targetLength) {
        byte[] current = input;
        while (current.length > targetLength) {
            int half = current.length / 2;
            byte[] next = new byte[half];
            for (int i = 0; i < half; i++) {
                next[i] = (byte) (current[i] ^ current[i + half]);
            }
            current = next;
        }
        return current;
    }

    private static byte[] sha256(byte[] input) {
        try {
            return java.security.MessageDigest.getInstance("SHA-256").digest(input);
        } catch (java.security.NoSuchAlgorithmException e) {
            return new byte[0];
        }
    }

    private static byte[] hexToBytes(String hex) {
        hex = hex.replace(" ", "").replace(":", "").toLowerCase(Locale.US);
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    // Helper to concat bytes
    private static byte[] concat(byte[] a, byte[] b, byte c) {
        byte[] res = new byte[a.length + b.length + 1];
        System.arraycopy(a, 0, res, 0, a.length);
        System.arraycopy(b, 0, res, a.length, b.length);
        res[res.length - 1] = c;
        return res;
    }
    private final ArrayDeque<String> logLines = new ArrayDeque<>();

    private void appendLog(String msg) {
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String line = "[" + time + "] " + msg;

        Log.d(TAG, line);

        runOnUiThread(() -> {
            logLines.addLast(line);
            if (logLines.size() > MAX_LOG_LINES) {
                logLines.removeFirst(); // 超過就移掉最舊的
            }
            tvLog.setText(String.join("\n", logLines));
            svLog.post(() -> svLog.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }
}
