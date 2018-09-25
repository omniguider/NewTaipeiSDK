package omni.com.newtaipeisdk;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import omni.com.newtaipeisdk.model.RecordData;

public class RecordFragment extends Fragment {

    private View mView;
    private static final String ARG_KEY_RECORD_DATA = "arg_key_record_data";
    private RecordData[] mRecordData;
    private ListView record_list;
    private String record_time[];
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
        for (int i = 0; i < mRecordData.length; i++) {
            record_time[i] = mRecordData[mRecordData.length-i-1].getTimestamp();
            record_status[i] = mRecordData[mRecordData.length-i-1].getStatus_name();
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
            RecordAdapter recordAdapter = new RecordAdapter(getActivity().getApplicationContext(), record_time, record_status);
            record_list.setAdapter(recordAdapter);
        }
        return mView;
    }

    public class RecordAdapter extends BaseAdapter {
        Context context;
        String time[];
        String status[];
        LayoutInflater inflater;

        public RecordAdapter(Context applicationContext, String[] time, String[] status) {
            this.context = context;
            this.time = time;
            this.status = status;
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
            TextView time_tv = view.findViewById(R.id.record_list_item_time);
            TextView status_tv = view.findViewById(R.id.record_list_item_status);
            time_tv.setText(time[position]);
            status_tv.setText(status[position]);
            return view;
        }
    }
}
