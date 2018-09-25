package omni.com.newtaipeisdk;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.THLight.USBeacon.App.Lib.BatteryPowerData;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.ArmaRssiFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import omni.com.newtaipeisdk.model.SendBeaconBatteryResponse;
import omni.com.newtaipeisdk.network.NetworkManager;
import omni.com.newtaipeisdk.network.NewTaipeiSDKApi;

public class NewTaipeiSDKActivity extends AppCompatActivity implements BeaconConsumer, BluetoothAdapter.LeScanCallback {

    private String TAG = "NewTaipeiSDKActivity";
    private BeaconManager mBeaconManager;
    private HandlerThread mBBHandlerThread;
    private Handler mBBHandler;
    private BluetoothAdapter mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    final List<String> NLPI_BEACON_MAJOR_LIST = new ArrayList<String>() {{
        add("7016");
    }};
    public static final String BEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    private static final float beaconTrigger10 = 10f;
    final int MSG_LE_START_SCAN = 1000;
    final int MSG_LE_STOP_SCAN = 1001;
    final int MSG_GET_DATA = 1002;
    final int MSG_STOP_SCAN = 1003;
    public static String username;
    public static String userid;
    public static String uuid;
    public static String major;
    public static String minor;
    private TextView punch_time_service_TV;
    private TextView query_the_records_TV;
    private String ARG_KEY_USERNAME = "arg_key_username";
    private String ARG_KEY_USERID = "arg_key_userid";
    private String mLastSendBatteryMac;
    private ArrayList<String> mSendBatteryMac;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ntsdk_activity_main);

        username = getIntent().getStringExtra(ARG_KEY_USERNAME);
        userid = getIntent().getStringExtra(ARG_KEY_USERID);

        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.setAndroidLScanningDisabled(true);
        mBeaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_LAYOUT));
        mBeaconManager.bind(this);

        punch_time_service_TV = findViewById(R.id.ntsdk_activity_main_tv_punch_time_service);
        query_the_records_TV = findViewById(R.id.ntsdk_activity_main_tv_query_the_records);

        mSendBatteryMac = new ArrayList<>();

        checkLocationService();
        checkBluetoothOn();
        startScanBeacon();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBeaconManager != null) {
            mBeaconManager.unbind(this);
        }
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
    }

    @Override
    public void onBeaconServiceConnect() {
        mBeaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
            }

            @Override
            public void didExitRegion(Region region) {
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                try {
                    mBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
                    mBeaconManager.addRangeNotifier(new RangeNotifier() {
                        @Override
                        public void didRangeBeaconsInRegion(final Collection<Beacon> collection, final Region region) {
                            NewTaipeiSDKActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for (Beacon b : collection) {
                                        uuid = b.getId1().toString();
                                        major = b.getId2().toString();
                                        minor = b.getId3().toString();
                                        if (b.getDistance() <= getBeaconTrigger()) {
                                            if (NLPI_BEACON_MAJOR_LIST.contains(major)) {
                                                Log.v(TAG, "major:" + major + " minor:" + minor);
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
                                                query_the_records_TV.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        getSupportFragmentManager().beginTransaction()
                                                                .replace(R.id.ntsdk_activity_main_fl, QueryFragment.newInstance())
                                                                .addToBackStack(null)
                                                                .commit();
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    });
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException");
                }
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

        BatteryPowerData BP = BatteryPowerData.generateBatteryBeacon(scanRecord);

        if (BP != null && BP.BatteryUuid.toUpperCase().startsWith("00112233-4455-6677-8899-AABBCCDDEEFF")) {
            mBBHandler.obtainMessage(MSG_GET_DATA).sendToTarget();
            Log.e(TAG, "BatteryPower:" + BP.batteryPower +
                    "\naddress : " + device.getAddress() +
                    "\ndevice name : " + device.getName() +
                    "\nrssi : " + i);
            if (!device.getAddress().equals(mLastSendBatteryMac) && !mSendBatteryMac.contains(device.getAddress())) {
                mSendBatteryMac.add(device.getAddress());
                Log.v(TAG, "setBeaconBatteryLevel");
                NewTaipeiSDKApi.getInstance().setBeaconBatteryLevel(NewTaipeiSDKActivity.this,
                        device.getAddress(),
                        BP.batteryPower + "",
                        new NetworkManager.NetworkManagerListener<SendBeaconBatteryResponse>() {
                            @Override
                            public void onSucceed(SendBeaconBatteryResponse response) {
                                if (response.isSuccess()) {
                                    mLastSendBatteryMac = device.getAddress();
                                }
                            }

                            @Override
                            public void onFail(String errorMsg, boolean shouldRetry) {

                            }
                        });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void startScanBeacon() {
        mBBHandlerThread = new HandlerThread("HandlerThread");
        mBBHandlerThread.start();
        mBBHandler = new Handler(mBBHandlerThread.getLooper()) {
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

    private void checkBluetoothOn() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {

        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, 77);
            }
        }
    }

    private void ensurePermissions() {
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
                    Manifest.permission.BLUETOOTH_ADMIN,
            }, 99);
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
