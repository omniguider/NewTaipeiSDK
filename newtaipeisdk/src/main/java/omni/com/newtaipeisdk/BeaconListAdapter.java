package omni.com.newtaipeisdk;

import static omni.com.newtaipeisdk.NewTaipeiSDKActivity.beaconName;
import static omni.com.newtaipeisdk.NewTaipeiSDKActivity.byHand;
import static omni.com.newtaipeisdk.NewTaipeiSDKActivity.hwid;
import static omni.com.newtaipeisdk.NewTaipeiSDKActivity.beaconSelect;
import static omni.com.newtaipeisdk.NewTaipeiSDKActivity.selectPos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import omni.com.newtaipeisdk.model.BeaconInfoData;

public class BeaconListAdapter extends RecyclerView.Adapter<BeaconListAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<BeaconInfoData> mBeaconInfoData;

    public BeaconListAdapter(Context context, ArrayList<BeaconInfoData> beaconInfoData) {
        super();
        mContext = context;
        mBeaconInfoData = beaconInfoData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_beacon_list, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        selectPos = -1;
        final BeaconInfoData beaconInfoData = mBeaconInfoData.get(position);
        holder.title.setText(beaconInfoData.getDESC());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beaconSelect = true;
                byHand = true;
                hwid = beaconInfoData.getHWID();
                beaconName = beaconInfoData.getDESC();
                selectPos = position;
                notifyDataSetChanged();
            }
        });

        if (beaconInfoData.getHWID().equals(NewTaipeiSDKActivity.hwid) && beaconSelect) {
            if (!byHand) {
                mBeaconInfoData.remove(position);
                mBeaconInfoData.add(0, beaconInfoData);
            }
            selectPos = position;
            beaconName = beaconInfoData.getDESC();
        }

        if (selectPos == position) {
            holder.title.setTextColor(mContext.getResources().getColor(android.R.color.white));
            holder.title.setBackgroundColor(mContext.getResources().getColor(R.color.ntsdk_blue));
        } else {
            holder.title.setTextColor(mContext.getResources().getColor(android.R.color.black));
            holder.title.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return mBeaconInfoData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_beacon_list_tv_title);
        }
    }

}
