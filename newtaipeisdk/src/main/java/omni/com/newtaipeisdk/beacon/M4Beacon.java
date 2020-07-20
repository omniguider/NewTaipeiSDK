/**
 * Radius Networks, Inc.
 * http://www.radiusnetworks.com
 * 
 * @author David G. Young
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package omni.com.newtaipeisdk.beacon;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Locale;

/**
* The <code>IBeacon</code> class represents a single hardware iBeacon detected by 
* an Android device.
* 
* <pre>An iBeacon is identified by a three part identifier based on the fields
* proximityUUID - a string ProximityUUID typically identifying the owner of a
*                 number of ibeacons
* major - a 16 bit integer indicating a group of iBeacons
* minor - a 16 bit integer identifying a single iBeacon</pre>
*
* An iBeacon sends a Bluetooth Low Energy (BLE) advertisement that contains these
* three identifiers, along with the calibrated tx power (in RSSI) of the 
* iBeacon's Bluetooth transmitter.  
* 
* This class may only be instantiated from a BLE packet, and an RSSI measurement for
* the packet.  The class parses out the three part identifier, along with the calibrated
* tx power.  It then uses the measured RSSI and calibrated tx power to do a rough
* distance measurement (the accuracy field) and group it into a more reliable buckets of 
* distance (the proximity field.)
* 
* @author  David G. Young
*/
@IgnoreExtraProperties
public class M4Beacon {
    /**
     * Less than half a meter away
     */
    public static final int PROXIMITY_IMMEDIATE = 1;
    /**
     * More than half a meter away, but less than four meters away
     */
    public static final int PROXIMITY_NEAR = 2;
    /**
     * More than four meters away
     */
    public static final int PROXIMITY_FAR = 3;
    /**
     * No distance estimate was possible due to a bad RSSI value or measured TX power
     */
    public static final int PROXIMITY_UNKNOWN = 0;

    final private static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
    private static final String TAG = "M4Beacon";

    /**
     * A 16 byte ProximityUUID that typically represents the company owning a number of iBeacons
     * Example: E2C56DB5-DFFB-48D2-B060-D0F5A71096E0
     */
    protected String proximityUuid;
    /**
     * A 16 bit integer typically used to represent a group of iBeacons
     */
    protected int major;
    /**
     * A 16 bit integer that identifies a specific iBeacon within a group
     */
    protected int minor;
    /**
     * An integer with four possible values representing a general idea of how far the iBeacon is away
     * @see #PROXIMITY_IMMEDIATE
     * @see #PROXIMITY_NEAR
     * @see #PROXIMITY_FAR
     * @see #PROXIMITY_UNKNOWN
     */
    protected Integer proximity;
    /**
     * A double that is an estimate of how far the iBeacon is away in meters.  This name is confusing, but is copied from
     * the iOS7 SDK terminology.   Note that this number fluctuates quite a bit with RSSI, so despite the name, it is not
     * super accurate.   It is recommended to instead use the proximity field, or your own bucketization of this value.
     */
    protected Double accuracy;
    /**
     * The measured signal strength of the Bluetooth packet that led do this iBeacon detection.
     */
    protected int rssi;
    /**
     * The calibrated measured Tx power of the iBeacon in RSSI
     * This value is baked into an iBeacon when it is manufactured, and
     * it is transmitted with each packet to aid in the distance estimate
     */
    protected int txPower;

    /**
     * If multiple RSSI samples were available, this is the running average
     */
    protected Double runningAverageRssi = null;

    /*
    //Battery
     */
    protected int battery;
    /* Temperature
     */
    protected int temperature;

    /* uid of this device , can be fix mac address or enc address from uid service of m4 beacon which is mac address anyway

     */
    protected String Uid;
    /**
     * @see #Uid
     * @set  Uid
     */
    public void setUid(String Uid)
    {
        this.Uid=Uid;
    }
    public String getUid()
    {
        return this.Uid;
    }
    protected  String shortName; //Bluetoothname

    public String getShortName() {
        if (shortName==null)
            return "";
        else
            return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Exclude
    public double getAccuracy() {
        if (accuracy == null) {
            accuracy = calculateAccuracy(txPower, runningAverageRssi != null ? runningAverageRssi : rssi );
        }
        return accuracy;
    }
    /**
     * @see #Uid, cipherId
     * @set  Uid, cipherId
     */
    public void setUid(String Uid, String cipherId)
    {
        this.Uid=Uid;
        this.cipherId=cipherId;
    }
    /**
     * @see #major
     * @return major
     */
    public int getMajor() {
        return major;
    }
    /**
     * @see #minor
     * @return minor
     */
    public int getMinor() {
        return minor;
    }
    /**
     * @see #proximity
     * @return proximity
     */
    public int getProximity() {
        if (proximity == null) {
            proximity = calculateProximity(getAccuracy());
        }
        return proximity;
    }
    /**
     * @see #rssi
     * @return rssi
     */
    public int getRssi() {
        return rssi;
    }
    /**
     * @see #txPower
     * @return txPowwer
     */
    public int getTxPower() {
        return txPower;
    }
    /**
     * @see #battery
     * @return battery
     */
    public int getBattery() {
        return battery;
    }
    /**
     * @see #temperature
     * @return temperature
     */
    public int getTemperature() {
        return temperature;
    }
    /**
     * @see #proximityUuid
     * @return proximityUuid
     */
    public String getProximityUuid() {
        return proximityUuid;
    }

    @Override
    public int hashCode() {
        return minor;
    }

    protected  String cipherId;
    /**
     * @see #cipherId
     * @return cipherId
     */
    @Exclude
    public String getCipherId() {
        return this.cipherId;

    };
    /**
     * @see #cipherId
     * @set cipherId
     */
    public void setCipherId(String cipherId)
    {
        this.cipherId=cipherId;
    }
    protected  int advInterval=760;

    public void setAdvInterval(int advInterval) {
        this.advInterval = advInterval;
    }

    public int getAdvInterval() {
        return advInterval;
    }

    /**
     * Two detected iBeacons are considered equal if they share the same three identifiers, regardless of their distance or RSSI.
     */
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof M4Beacon)) {
            return false;
        }
        M4Beacon thatBeacon = (M4Beacon) that;
        return (thatBeacon.getMajor() == this.getMajor() && thatBeacon.getMinor() == this.getMinor() && thatBeacon.getProximityUuid().equals(this.getProximityUuid()));
    }


    public M4Beacon() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public M4Beacon(M4Beacon otherIBeacon) {
        this.major = otherIBeacon.major;
        this.minor = otherIBeacon.minor;
        this.accuracy = otherIBeacon.accuracy;
        this.proximity = otherIBeacon.proximity;
        this.rssi = otherIBeacon.rssi;
        this.proximityUuid = otherIBeacon.proximityUuid;
        this.txPower = otherIBeacon.txPower;
        this.battery =otherIBeacon.battery;
        this.Uid=otherIBeacon.getUid();
        this.cipherId=otherIBeacon.getCipherId();
        this.shortName = otherIBeacon.shortName;
    }


    public M4Beacon(String proximityUuid, int major, int minor, int txPower, int rssi, int battery) {
        this.proximityUuid = proximityUuid;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
        this.txPower = txPower;
        this.battery =battery;
        this.Uid="";
        this.cipherId="";
    }
    public M4Beacon(String proximityUuid, int major, int minor, int txPower, int rssi, int battery, String uid) {
        this.proximityUuid = proximityUuid;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
        this.txPower = txPower;
        this.battery =battery;
        this.Uid=uid;
        this.cipherId="";
    }
    public M4Beacon(String proximityUuid, int major, int minor, int txPower, int rssi, int battery, String uid, String cipherId) {
        this.proximityUuid = proximityUuid;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
        this.txPower = txPower;
        this.battery =battery;
        this.Uid=uid;
        this.cipherId=cipherId;
    }
    @SuppressLint("DefaultLocale")
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ProximityUUID=").append(this.proximityUuid.toUpperCase());
        sb.append(" Major=").append(this.major);
        sb.append(" Minor=").append(this.minor);
        sb.append(" TxPower=").append(this.txPower);
        sb.append(" TxBattery=").append(this.battery);
        sb.append(" cipherId=").append(this.cipherId);
        sb.append(" Uid=").append(this.Uid);
        return sb.toString();
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.proximityUuid.toUpperCase(Locale.ENGLISH)).append(",");
        sb.append(this.major).append(",");
        sb.append(this.minor).append(",");
        sb.append(this.txPower).append(",");
        sb.append(this.battery).append(",");
        sb.append(this.cipherId).append(",");
        sb.append(this.getUid());

        return sb.toString();
    }

    public static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        Log.i(TAG, "calculating accuracy based on rssi of " + rssi);


        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            Log.i(TAG, " avg rssi: "+rssi+" accuracy: "+accuracy);
            return accuracy;
        }
    }



    public static int calculateProximity(double accuracy) {
        if (accuracy < 0) {
            return PROXIMITY_UNKNOWN;
            // is this correct?  does proximity only show unknown when accuracy is negative?  I have seen cases where it returns unknown when
            // accuracy is -1;
        }
        if (accuracy < 0.5 ) {
            return M4Beacon.PROXIMITY_IMMEDIATE;
        }
        // forums say 3.0 is the near/far threshold, but it looks to be based on experience that this is 4.0
        if (accuracy <= 4.0) {
            return M4Beacon.PROXIMITY_NEAR;
        }
        // if it is > 4.0 meters, call it far
        return M4Beacon.PROXIMITY_FAR;

    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
