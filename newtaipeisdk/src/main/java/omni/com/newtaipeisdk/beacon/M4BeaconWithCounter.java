package omni.com.newtaipeisdk.beacon;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

/**
 * Created by macmini on 2017/2/8.
 */
@IgnoreExtraProperties
public class M4BeaconWithCounter extends M4Beacon {

    public M4BeaconWithCounter() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
        super();
        bcount=0;
        newFlag=true;
        lastUpdatedTime=0;
        authCount=0;
    }
    public M4BeaconWithCounter(M4BeaconWithCounter beaconCounter)
    {
        super(beaconCounter);

        bcount=beaconCounter.bcount;
        newFlag=beaconCounter.getNewFlag();
        totalDB=beaconCounter.totalDB;
        totalDbCount=beaconCounter.totalDbCount;
        lastUpdatedTime=beaconCounter.lastUpdatedTime;
        authCount=beaconCounter.getAuthCount();
    }

    public M4BeaconWithCounter(M4Beacon beacon)
    {
        super(beacon);
        bcount=0;
        newFlag=true;
        lastUpdatedTime=0;
        authCount=0;

    }
    public M4BeaconWithCounter(M4Beacon beacon, long count)
    {
        super(beacon);
        bcount=count;
        newFlag=true;
        totalDB=0;
        lastUpdatedTime=0;
        authCount=0;
    }
    public M4BeaconWithCounter(M4Beacon beacon, long count, boolean flag)
    {
        super(beacon);
        bcount=count;
        newFlag=flag;
        totalDB=0;
        lastUpdatedTime=0;
        authCount=0;
    }
    private long bcount;

    public void setBcount(long bcount) {
        this.bcount = bcount;
    }

    public long getBcount() {
        return bcount;
    }

    private boolean newFlag;

    public void setNewFlag(boolean newFlag) {


        this.newFlag = newFlag;
    }
    public boolean getNewFlag( ) {

        if (!newFlag) {
            long nowBeconTime = (new Date()).getTime();
            if ((nowBeconTime - getLastUpdatedTime()) < 5 * 1000) {
                newFlag = true;
            }
        }
        return newFlag;
    }
    private long lastUpdatedTime;

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }
    private long totalDB=0;


    private int totalDbCount=0;
    public   void incDBCout()
    {
        totalDbCount++;
        totalDB=totalDB+getRssi();
    }

    @Exclude
    public int getTotalDbCount() {
        return totalDbCount;
    }

    @Exclude
    public long getTotalDB() {
        return totalDB;
    }

    public   void incDBCout(M4BeaconWithCounter beaconCounter )
    {
        totalDbCount=beaconCounter.getTotalDbCount()+1;
        totalDB=totalDB+beaconCounter.getTotalDB()+getRssi();
    }

    @Exclude
    public int getMyAvgDb()
    {
         if (totalDbCount<=1)
             return getRssi();
        else
            return (int)((1.0f*totalDB)/(1.0f*totalDbCount));
    }
    private int authCount;
    public  int getAuthCount(){
        return this.authCount;
    }

    public  void setAuthCount(int authCount)
    {
        this.authCount=authCount;
    }


}
