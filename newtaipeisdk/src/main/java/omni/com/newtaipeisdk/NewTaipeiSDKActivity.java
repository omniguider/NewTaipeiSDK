package omni.com.newtaipeisdk;

import static com.m4grid.lib.m4Beacon.RawDevice.Decrypt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.THLight.Omniguider.Lib.OmniguiderData;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.ArmaRssiFilter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import omni.com.newtaipeisdk.beacon.BaseBleActivity;
import omni.com.newtaipeisdk.beacon.M4Beacon;
import omni.com.newtaipeisdk.beacon.M4BeaconWithCounter;
import omni.com.newtaipeisdk.model.BeaconInfoData;
import omni.com.newtaipeisdk.model.MessageResponse;
import omni.com.newtaipeisdk.model.PermissionResponse;
import omni.com.newtaipeisdk.model.SendBeaconBatteryResponse;
import omni.com.newtaipeisdk.network.NetworkManager;
import omni.com.newtaipeisdk.network.NewTaipeiSDKApi;
import omni.com.newtaipeisdk.tool.DialogTools;

public class NewTaipeiSDKActivity extends BaseBleActivity implements BeaconConsumer, BluetoothAdapter.LeScanCallback {

    List<M4BeaconWithCounter> currentBeacons = new ArrayList<M4BeaconWithCounter>();
    public static String TAG = "NewTaipeiSDKActivity";
    public static final int REQUEST_PERMISSIONS = 1;
    final int PUNCH_TIME_OUT = 10000;
    private BeaconManager mBeaconManager;
    private HandlerThread mBBHandlerThread;
    private Handler mBBHandler;
    private HandlerThread mTimeoutHandlerThread;
    private Handler mTimeoutHandler;
    private BluetoothAdapter mBTAdapter = BluetoothAdapter.getDefaultAdapter();
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
    private BluetoothAdapter bluetoothAdapter;

    private BeaconInfoData[] mBeaconInfoData;
    private boolean isClockBeacon = false;
    private boolean userPermission = false;
    public static int randomLevel = 1;

    private TextView decrypt_TV;

    @Override
    public void onReceivedBeacon(List<M4BeaconWithCounter> resultBeacons) {
        Log.e(TAG, "onReceivedBeacon");
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
            punch_time_service_TV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.ntsdk_activity_main_fl, ServiceFragment.newInstance())
                            .addToBackStack(null)
                            .commit();
                }
            });
//            lastScanTime = Calendar.getInstance().getTime().getTime();
        }

        return realUid;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ntsdk_activity_main);

        super.ProximityUUID = "26cbdba2-8dd8-4e54-adf4-b51f0caea6e6";
        super.ProcessLengthSec = 10; //10 second to clear not found beacon

        username = getIntent().getStringExtra(ARG_KEY_USERNAME);
        userid = getIntent().getStringExtra(ARG_KEY_USERID);

//        for (int i = 1601; i <= beaconNum; i++) {
//            BEACON_ID_LIST.add(String.valueOf(i));
//        }

        findViewById(R.id.ntsdk_activity_main_fl_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        punch_time_service_TV = findViewById(R.id.ntsdk_activity_main_tv_punch_time_service);
        query_the_records_TV = findViewById(R.id.ntsdk_activity_main_tv_query_the_records);
        outside_range_TV = findViewById(R.id.ntsdk_activity_main_tv_outside_range);
        message_TV = findViewById(R.id.ntsdk_activity_main_tv_message);
        decrypt_TV = findViewById(R.id.ntsdk_activity_main_decrypt_tv);

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
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.ntsdk_activity_main_fl, QueryFragment.newInstance())
                        .addToBackStack(null)
                        .commit();
            }
        });

        mSendBatteryMac = new ArrayList<>();
        mSendBatteryId = new ArrayList<>();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        checkLocationService();
//        checkBluetoothOn();
//        startScanBeacon();

        mTimeoutHandlerThread = new HandlerThread("HandlerThread");
        mTimeoutHandlerThread.start();
        mTimeoutHandler = new Handler(mTimeoutHandlerThread.getLooper());
        mTimeoutHandler.postDelayed(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                currentTime = Calendar.getInstance().getTime().getTime();
                Log.e(TAG, "bluetoothAdapter.isEnabled()" + bluetoothAdapter.isEnabled());
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
                Log.e(TAG, "currentTime" + currentTime);
                Log.e(TAG, "lastScanTime" + lastScanTime);
                mTimeoutHandler.postDelayed(this, 1000);
            }
        }, 1000);

        randomNum = (int) (Math.random() * 99);
    }

    @Override
    public void onPause() {
        super.onPause();
        isResumed = false;

        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        isResumed = true;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onDestroy() {
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
//                try {
//                    mBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
//                    mBeaconManager.addRangeNotifier(new RangeNotifier() {
//                        @Override
//                        public void didRangeBeaconsInRegion(final Collection<Beacon> collection, final Region region) {
//                            NewTaipeiSDKActivity.this.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    for (Beacon b : collection) {
//                                        uuid = b.getId1().toString();
//                                        major = b.getId2().toString();
//                                        minor = b.getId3().toString();
//                                        if (b.getDistance() <= getBeaconTrigger()) {
//                                            if (NLPI_BEACON_MAJOR_LIST.contains(major)) {
//                                                Log.v(TAG, "major:" + major + " minor:" + minor);
//                                                punch_time_service_TV.setBackgroundResource(R.mipmap.btn_bg_yellow_m);
//                                                punch_time_service_TV.setOnClickListener(new View.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(View view) {
//                                                        getSupportFragmentManager().beginTransaction()
//                                                                .replace(R.id.ntsdk_activity_main_fl, ServiceFragment.newInstance())
//                                                                .addToBackStack(null)
//                                                                .commit();
//                                                    }
//                                                });
//                                                query_the_records_TV.setOnClickListener(new View.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(View view) {
//                                                        getSupportFragmentManager().beginTransaction()
//                                                                .replace(R.id.ntsdk_activity_main_fl, QueryFragment.newInstance())
//                                                                .addToBackStack(null)
//                                                                .commit();
//                                                    }
//                                                });
//                                                break;
//                                            }
//                                        }
//                                    }
//                                }
//                            });
//                        }
//                    });
//                } catch (RemoteException e) {
//                    Log.e(TAG, "RemoteException");
//                }
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
//        BatteryPowerData BP = BatteryPowerData.generateBatteryBeacon(scanRecord);
//
//        if (BP != null && BP.BatteryUuid.toUpperCase().startsWith("00112233-4455-6677-8899-AABBCCDDEEFF")) {
//            mBBHandler.obtainMessage(MSG_GET_DATA).sendToTarget();
//            Log.e(TAG, "BatteryPower:" + BP.batteryPower +
//                    "\naddress : " + device.getAddress() +
//                    "\ndevice name : " + device.getName() +
//                    "\nrssi : " + i);
//            if (!device.getAddress().equals(mLastSendBatteryMac) && !mSendBatteryMac.contains(device.getAddress())) {
//                mSendBatteryMac.add(device.getAddress());
//                Log.v(TAG, "setBeaconBatteryLevel");
//                NewTaipeiSDKApi.getInstance().setBeaconBatteryLevel(NewTaipeiSDKActivity.this,
//                        device.getAddress(),
//                        BP.batteryPower + "",
//                        new NetworkManager.NetworkManagerListener<SendBeaconBatteryResponse>() {
//                            @Override
//                            public void onSucceed(SendBeaconBatteryResponse response) {
//                                if (response.isSuccess()) {
//                                    mLastSendBatteryMac = device.getAddress();
//                                }
//                            }
//
//                            @Override
//                            public void onFail(String errorMsg, boolean shouldRetry) {
//
//                            }
//                        });
//            }
//        }

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
//            if (BEACON_ID_LIST.contains(omniguiderData.hwID) && bluetoothAdapter.isEnabled()) {
            if (isClockBeacon && bluetoothAdapter.isEnabled() && userPermission) {
//                hwid = omniguiderData.hwID;
                outside_range_TV.setVisibility(View.GONE);
                punch_time_service_TV.setBackgroundResource(R.mipmap.btn_bg_yellow_m);
                punch_time_service_TV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.ntsdk_activity_main_fl, ServiceFragment.newInstance())
                                .addToBackStack(null)
                                .commit();
                    }
                });

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
            //Log.d("debug", "BLE Data:" + bytesToHex(scanRecord));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS && grantResults.length != 0
        ) {
            checkBluetoothOn();
            startScanBeacon();
        }
    }

    public void startScanBeacon() {
        mBBHandlerThread = new HandlerThread("HandlerThread");
        mBBHandlerThread.start();
        mBBHandler = new Handler(mBBHandlerThread.getLooper()) {
            @SuppressLint("MissingPermission")
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_LE_START_SCAN:
                        if (mBTAdapter.isEnabled()) {
                            mBTAdapter.startLeScan(NewTaipeiSDKActivity.this);
                        }
                        break;

                    case MSG_LE_STOP_SCAN:
                        if (mBTAdapter.isEnabled()) {
                            mBTAdapter.stopLeScan(NewTaipeiSDKActivity.this);
                        }
                        break;

                    case MSG_STOP_SCAN:
                        mBBHandler.removeMessages(MSG_LE_START_SCAN);
                        mBBHandler.removeMessages(MSG_LE_STOP_SCAN);
                        if (mBTAdapter.isEnabled()) {
                            mBTAdapter.stopLeScan(NewTaipeiSDKActivity.this);
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
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                }, REQUEST_PERMISSIONS);
            } else {
                checkBluetoothOn();
                startScanBeacon();
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN
                }, REQUEST_PERMISSIONS);
            } else {
                checkBluetoothOn();
                startScanBeacon();
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

    public float getBeaconTrigger() {
        return beaconTrigger10;
    }
}
