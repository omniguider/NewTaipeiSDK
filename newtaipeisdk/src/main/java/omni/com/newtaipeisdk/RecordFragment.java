package omni.com.newtaipeisdk;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import omni.com.newtaipeisdk.model.RecordData;

public class RecordFragment extends Fragment {

    private View mView;
    private static final String ARG_KEY_RECORD_DATA = "arg_key_record_data";
    private RecordData[] mRecordData;
    private ListView record_list;
    private String record_time[];
    private String beacon_desc[];
    private String record_status[];

    public static RecordFragment newInstance(RecordData[] object) {
        RecordFragment fragment = new RecordFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_KEY_RECORD_DATA, object);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecordData = (RecordData[]) getArguments().getSerializable(ARG_KEY_RECORD_DATA);
        record_time = new String[mRecordData.length];
        record_status = new String[mRecordData.length];
        beacon_desc = new String[mRecordData.length];
        for (int i = 0; i < mRecordData.length; i++) {
            record_time[i] = mRecordData[mRecordData.length - i - 1].getTimestamp();
            record_status[i] = mRecordData[mRecordData.length - i - 1].getStatus_name();
            beacon_desc[i] = mRecordData[mRecordData.length - i - 1].getBeacon_desc();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_record, container, false);

            mView.findViewById(R.id.fragment_record_fl_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });

            record_list = mView.findViewById(R.id.fragment_record_lv);
            RecordAdapter recordAdapter = new RecordAdapter(getActivity().getApplicationContext(), record_time, record_status, beacon_desc);
            record_list.setAdapter(recordAdapter);
        }
        return mView;
    }

    public class RecordAdapter extends BaseAdapter {
        Context context;
        String time[];
        String status[];
        String desc[];
        LayoutInflater inflater;

        public RecordAdapter(Context applicationContext, String[] time, String[] status, String[] desc) {
            this.context = context;
            this.time = time;
            this.status = status;
            this.desc = desc;
            inflater = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return status.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            view = inflater.inflate(R.layout.record_list_item, null);
            TextView date_tv = view.findViewById(R.id.record_list_item_date);
            TextView time_tv = view.findViewById(R.id.record_list_item_time);
            TextView desc_tv = view.findViewById(R.id.record_list_item_loc);
            TextView status_tv = view.findViewById(R.id.record_list_item_status);
            String[] separated = time[position].split("\\s+");
            date_tv.setText(separated[0]);
            time_tv.setText(separated[1]);
            status_tv.setText(status[position]);
            switch (status[position]) {
                case "上班":
                case "下班":
                    status_tv.setTextColor(getResources().getColor(R.color.ntsdk_yellow));
                    break;
                case "外出":
                case "返回":
                    status_tv.setTextColor(getResources().getColor(R.color.ntsdk_green));
                    break;
                case "加班上班":
                case "加班下班":
                    status_tv.setTextColor(getResources().getColor(R.color.ntsdk_red));
                    break;
                case "測試":
                    status_tv.setTextColor(getResources().getColor(R.color.ntsdk_purple));
                    break;
            }
            desc_tv.setText(desc[position]);
            return view;
        }
    }
}
