package omni.com.newtaipeisdk.beacon;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * Created by macmini on 2017/2/8.
 */
@IgnoreExtraProperties
public class BeaconStatus extends M4Beacon {

    private long lastUpdatedTime;
    String latLng ;
    String address ;
    String name;
    public BeaconStatus() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
        super();
        long nowBeconTime = (new Date()).getTime();
        lastUpdatedTime=nowBeconTime;

    }
    public BeaconStatus(BeaconStatus beaconCounter)
    {


        lastUpdatedTime=beaconCounter.lastUpdatedTime;

    }

    public BeaconStatus(M4Beacon beacon)
    {
        super(beacon);
        long nowBeconTime = (new Date()).getTime();
        lastUpdatedTime=nowBeconTime;

    }
    public BeaconStatus(M4BeaconWithCounter beacon)
    {
        super(beacon);
        lastUpdatedTime=beacon.getLastUpdatedTime();


    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    public String getLatLng() {
        return latLng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }



}
