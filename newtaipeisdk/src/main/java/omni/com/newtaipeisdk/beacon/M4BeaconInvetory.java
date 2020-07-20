package omni.com.newtaipeisdk.beacon;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * Created by macmini on 2017/2/8.
 */
@IgnoreExtraProperties
public class M4BeaconInvetory   {

    BeaconStatus beaconStatus;
    String address;
    public M4BeaconInvetory(BeaconStatus beaconStatus, String address)
    {
        this.beaconStatus = beaconStatus;
        this.address = address;


    }
    public String getAddress() {
        return address;
    }

    public BeaconStatus getBeaconStatus() {
        return beaconStatus;
    }

    public void setAddress(String address) {
        address = address;
    }

    public void setBeaconStatus(BeaconStatus beaconStatus) {
        this.beaconStatus = beaconStatus;
    }
}
