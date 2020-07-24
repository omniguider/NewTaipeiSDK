package omni.com.newtaipeisdk.beacon;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import omni.com.newtaipeisdk.NewTaipeiSDKActivity;
import omni.com.newtaipeisdk.model.SendBeaconBatteryResponse;
import omni.com.newtaipeisdk.network.NetworkManager;
import omni.com.newtaipeisdk.network.NewTaipeiSDKApi;

import static omni.com.newtaipeisdk.NewTaipeiSDKActivity.NLPI_BEACON_ID_LIST;
import static omni.com.newtaipeisdk.NewTaipeiSDKActivity.randomLevel;
import static omni.com.newtaipeisdk.NewTaipeiSDKActivity.randomNum;

//import static lib
public abstract class BaseBleActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier {
//    static {
//        System.loadLibrary("native-lib");
//    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    public String getAESKey() {
        return "0227022850022709690808932355050f";
    }

    ;

    public String getIVector() {
        return "65cc4c0b6cf9c56e2a2d801df1b99d01";
    }

    ;

    //abstract method
    public abstract void onReceivedBeacon(List<M4BeaconWithCounter> findBeacons);

    public abstract String onDecryptBeacon(String rawUid);

    public static String ProximityUUID = "26cbdba2-8dd8-4e54-adf4-b51f0caea6e6"; //國泰
    protected static int ProcessLengthSec = 30;
    // Used to load the 'native-lib' library on application startup.

    protected static final int REQUEST_ENABLE_BT = 1001;
    private static final int MY_PERMISSIONS_REQUEST_BT = 1;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 2;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 3;
    org.altbeacon.beacon.BeaconManager mBeaconManager;
    static final String TAG = BaseBleActivity.class.getSimpleName();
    Context context;
    long lastBeconTime;
    Boolean findBeaconFlag = false;
    List<M4BeaconWithCounter> findBeacons = new ArrayList<M4BeaconWithCounter>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        checkBTState();
    }

    @Override
    public void onResume() {
        super.onResume();
        //0201041aff 4c00 0215 25cbdba28dd84e54adf4b51f0caea6e6 03000001 b6 04160f1857 0b160a187ff31ba02e8132fa 03086d34 0000000000000000000000 newdevice mac=59:74:1C:1B:19:1F
        //           0 1  2 3  4                                20  22   24 25         303132333435363738394041 42

        //0201041aff 4c00 0215 26cbdba28dd84e54adf4b51f0caea6e6 b1a772b6 b6 04160f1856 0f160a18b94b631efbb9bd6fb1a772b6 0308434c00000000000000
        //                                                                                     D7822E283E2F2C62 B1A772B6 02F8B6
        mBeaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(context.getApplicationContext());
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:29-29,i:34-39,i:40-41,i:46-47")); //ibeacon for m4beacon
        //s:27-28=0f18,s:32-33=0a18
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        /*
        https://altbeacon.github.io/android-beacon-library/javadoc/org/altbeacon/beacon/BeaconParser.html#setBeaconLayout(java.lang.String)
        Four prefixes are allowed in the string:

       m - matching byte sequence for this beacon type to parse (exactly one required)
       s - ServiceUuid for this beacon type to parse (optional, only for Gatt-based beacons)
       i - identifier (at least one required, multiple allowed)
       p - power calibration field (exactly one required)
       d - data field (optional, multiple allowed)
       x - extra layout.  Signifies that the layout is secondary to a primary layout with the same
       matching byte sequence (or ServiceUuid).  Extra layouts do not require power or
       identifier fields and create M4Beacon objects without identifiers.

        ALTBEACON      m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25
        EDDYSTONE TLM  x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15
        EDDYSTONE UID  s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19
        EDDYSTONE URL  s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v
        IBEACON        m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24
         */
        mBeaconManager.bind(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        mBeaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(context.getApplicationContext());
        mBeaconManager.unbind(this);
        findBeacons.clear();
    }

    public void onBeaconServiceConnect() {
        Log.i(TAG, "onBeaconServiceConnect");
        Region region = new Region("all-beacons-region", Identifier.parse(ProximityUUID), null, null);
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.setRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        Log.i(TAG, "didRangeBeaconsInRegion");
        long nowBeconTime = (new Date()).getTime();
        if (lastBeconTime == 0)
            lastBeconTime = nowBeconTime;

        final List<Beacon> tmpfindBeacons = new ArrayList<Beacon>();

        findBeaconFlag = false;
        for (Beacon beacon : beacons) {
            //if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00)
            Identifier namespaceId = beacon.getId1();
            if (namespaceId.toString().equalsIgnoreCase(ProximityUUID)) {
                findBeaconFlag = true;
                tmpfindBeacons.add(beacon);

            }
        }
        Log.i(TAG, "Have Found bacons total of = " + beacons.size() + " but ours proxiimity beacon no= " + tmpfindBeacons.size());
        if (findBeaconFlag) {


            for (Beacon beacon : tmpfindBeacons) { //current found beacon
                Identifier namespaceId = beacon.getId1();
                Identifier majorId = beacon.getId2(); //major
                Identifier minorId = beacon.getId3(); //minor
                String shortName = beacon.getBluetoothName();
                Log.i(TAG, "I see a beacon transmitting namespace id: " + namespaceId + " name:" + shortName +
                        " and major id: " + majorId + " and minor id: " + minorId +
                        " approximately " + beacon.getDistance() + " meters away.");
//                Log.i(TAG, "mac id=" + beacon.getBluetoothAddress() + " service " + beacon.getIdentifier(3) + " " + beacon.getIdentifier(4).toHexString());

                String rawUid = "";
                try {
                    if (beacon.getIdentifier(3) != null) {
                        rawUid = beacon.getIdentifier(3).toHexString().substring(2) + "" + beacon.getIdentifier(4).toHexString().substring(2);
                    }
                } catch (IndexOutOfBoundsException e) {
                    continue;
                }

                M4Beacon m4Beacon = new M4Beacon(ProximityUUID, majorId.toInt(), minorId.toInt(), beacon.getTxPower(), beacon.getRssi(), beacon.getDataFields().get(0).intValue());

                if (!shortName.isEmpty()) {
                    m4Beacon.setShortName(shortName);
                }

                final String realUid = onDecryptBeacon(rawUid);
                if (realUid.isEmpty()) { //may not work
                    m4Beacon.setUid(beacon.getBluetoothAddress(), beacon.getBluetoothAddress());
                } else {
                    m4Beacon.setUid(realUid, rawUid); //
                    try {
                        m4Beacon.setAdvInterval(beacon.getIdentifier(5).toInt());
                    } catch (UnsupportedOperationException us) {

                    }
                }

                final String fRealUid = realUid;
                Log.v(TAG, "mLastSendBatteryId " + NewTaipeiSDKActivity.mLastSendBatteryId);
                Log.v(TAG, "realUid " + realUid);
                Log.v(TAG, "randomNum " + randomNum);
                Log.v(TAG, "randomLevel " + randomLevel);
                if (!NewTaipeiSDKActivity.mLastSendBatteryId.equals(realUid) && NLPI_BEACON_ID_LIST.contains(realUid) && randomNum < randomLevel) {
                    Log.v(TAG, "setBeaconBatteryLevel holden");
                    NewTaipeiSDKApi.getInstance().setBeaconBatteryLevel(BaseBleActivity.this,
                            realUid,
                            String.valueOf(m4Beacon.getBattery() * 0.017 + 1.8),
                            new NetworkManager.NetworkManagerListener<SendBeaconBatteryResponse>() {
                                @Override
                                public void onSucceed(SendBeaconBatteryResponse response) {
                                    if (response.isSuccess()) {
                                        NewTaipeiSDKActivity.mLastSendBatteryId = fRealUid;
                                    }
                                }

                                @Override
                                public void onFail(String errorMsg, boolean shouldRetry) {

                                }
                            });
                }

                boolean findFLag = false;
                for (int index = 0; index < findBeacons.size(); index++) {
                    M4BeaconWithCounter checkbeacon = findBeacons.get(index);
                    findBeacons.get(index).setNewFlag(false);//mark it is old one , we have found already
                    //Log.i(TAG,"realUid="+realUid + " checkbeacon="+checkbeacon.getUid());
                    if (realUid.equalsIgnoreCase(checkbeacon.getUid()) && beacon.getId2().toInt() == checkbeacon.getMajor() && beacon.getId3().toInt() == checkbeacon.getMinor()) {
                        findFLag = true;
                        long count = checkbeacon.getBcount();

                        //add time count durationn when the beacon is close by < -64, smaller .> closer
                        //ignore distance
                        //if (beacon.getRssi()>=-80)
                        {
                            count = count + (nowBeconTime - lastBeconTime);
                        }
                        checkbeacon.setBcount(count);
                        checkbeacon.setLastUpdatedTime(nowBeconTime);
                        //create m4Beacon
                        String prevCipherId = checkbeacon.getCipherId();
                        M4BeaconWithCounter bc = new M4BeaconWithCounter(m4Beacon, count, true); //update is just found
                        bc.setLastUpdatedTime(nowBeconTime);
                        bc.incDBCout(checkbeacon);
                        if (prevCipherId.equalsIgnoreCase(rawUid)) //same beacons rawid, can be fake or not change yet
                        {
                            //do not update authCount
                            bc.setAuthCount(checkbeacon.getAuthCount());
                            Log.i(TAG, "mac prevCipherId=" + prevCipherId + " rawUid" + rawUid + "aucthcount=" + checkbeacon.getAuthCount());
                        } else {
                            bc.setCipherId(rawUid);
                            bc.setAuthCount(checkbeacon.getAuthCount() + 1);
                            //update authCount;
                        }

                        findBeacons.set(index, bc);
                        break;
                    }

                }
                if (!findFLag) { //new one just add to list
                    M4BeaconWithCounter bc = new M4BeaconWithCounter(m4Beacon);
                    bc.setLastUpdatedTime(nowBeconTime);
                    findBeacons.add(bc);
                }


            }
            lastBeconTime = nowBeconTime;
        } else {
            //if it too long clear it
            if ((nowBeconTime - lastBeconTime) > ProcessLengthSec * 1000) {
                Log.i(TAG, "update " + (nowBeconTime - lastBeconTime) + "ms before");
                findBeacons.clear();
            }

        }
        //Log.i(TAG,"Have bacons total= " + beacons.size() + " but mine is "+tmpfindBeacons.size() + " but will use history "+findBeacons.size());
        onReceivedBeacon(findBeacons);

    }


    /* This routine is called when an activity completes. */
    // cpAppManger will open VehicleManagement and it will call onActivity to Here
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK)
                checkBTState(); //re-check

        } else {
            //finish();//shall use dialog view
        }


    }

    //BleControl

    protected void checkBTState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(BaseBleActivity.this,
                    android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(BaseBleActivity.this,
                            android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(BaseBleActivity.this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED

            ) { //WRITE_EXTERNAL_STORAGE
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(BaseBleActivity.this,
                        android.Manifest.permission.BLUETOOTH)


                ) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    //Toast.makeText(TakePhotoActivity.this, "Camera permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("This app needs background location access");
                    builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @TargetApi(23)
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                            ActivityCompat.requestPermissions(BaseBleActivity.this,
                                    new String[]{android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_BT);


                        }

                    });
                    builder.show();

                    ActivityCompat.requestPermissions(BaseBleActivity.this,
                            new String[]{android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_REQUEST_BT);

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(BaseBleActivity.this,
                            new String[]{android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_REQUEST_BT);

                }
                return;
            }

        }


        startBT();


    }

    protected void startBT() {
        // BT check
        BluetoothAdapter mBTAdapter;

        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager != null) {
            mBTAdapter = manager.getAdapter();

            if (mBTAdapter == null) {
                Log.d(TAG, "SHOW STOPPER: Unable to get bluetooth adapter."); //藍芽狀態:未支援

            } else {
                if (mBTAdapter.isEnabled()) {
                    // tvout.append("\n藍芽狀態:已啟動");

                } else {
                    //askPermission();//use default dialog
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }

            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_BT:

            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "fine location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
            case PERMISSION_REQUEST_BACKGROUND_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "background location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }
}
