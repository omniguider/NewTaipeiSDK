package omni.com.newtaipeisdk;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import omni.com.newtaipeisdk.model.RecordData;
import omni.com.newtaipeisdk.network.NetworkManager;
import omni.com.newtaipeisdk.network.NewTaipeiSDKApi;
import omni.com.newtaipeisdk.tool.DialogTools;

public class QueryFragment extends Fragment {

    private View mView;
    private EditText start_time_et;
    private EditText end_time_et;
    private Calendar m_Calendar = Calendar.getInstance();
    private DatePickerDialog.OnDateSetListener datepicker_start;
    private DatePickerDialog.OnDateSetListener datepicker_end;
    private Long currentDate;
    private Long startDate;
    private Long endDate;

    public static QueryFragment newInstance() {
        Bundle args = new Bundle();
        QueryFragment fragment = new QueryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_query, container, false);

            mView.findViewById(R.id.fragment_query_fl_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });

            datepicker_start = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    m_Calendar.set(Calendar.YEAR, year);
                    m_Calendar.set(Calendar.MONTH, monthOfYear);
                    m_Calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String myFormat = "yyyy/MM/dd";
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.TAIWAN);
                    start_time_et.setText(sdf.format(m_Calendar.getTime()));
                    startDate = m_Calendar.getTime().getTime();
                }
            };

            datepicker_end = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    m_Calendar.set(Calendar.YEAR, year);
                    m_Calendar.set(Calendar.MONTH, monthOfYear);
                    m_Calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String myFormat = "yyyy/MM/dd";
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.TAIWAN);
                    end_time_et.setText(sdf.format(m_Calendar.getTime()));
                    endDate = m_Calendar.getTime().getTime();
                }
            };

            start_time_et = mView.findViewById(R.id.fragment_record_start_time_et);
            start_time_et.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePickerDialog dialog = new DatePickerDialog(getActivity(),
                            datepicker_start,
                            m_Calendar.get(Calendar.YEAR),
                            m_Calendar.get(Calendar.MONTH),
                            m_Calendar.get(Calendar.DAY_OF_MONTH));
                    dialog.show();
                }
            });

            end_time_et = mView.findViewById(R.id.fragment_record_end_time_et);
            end_time_et.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePickerDialog dialog = new DatePickerDialog(getActivity(),
                            datepicker_end,
                            m_Calendar.get(Calendar.YEAR),
                            m_Calendar.get(Calendar.MONTH),
                            m_Calendar.get(Calendar.DAY_OF_MONTH));
                    dialog.show();
                }
            });

            currentDate = new Date().getTime();
            mView.findViewById(R.id.fragment_record_query_tv).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (startDate == null || endDate == null){
                        DialogTools.getInstance().showErrorMessage(getActivity(), R.string.error,
                                R.string.hint_input_correct_date, new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                    }
                                });
                    }
                    else if (endDate > currentDate || startDate > currentDate) {
                        DialogTools.getInstance().showErrorMessage(getActivity(), R.string.error,
                                R.string.hint_more_than_current_date, new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                    }
                                });
                    } else if (endDate - startDate < 0) {
                        DialogTools.getInstance().showErrorMessage(getActivity(), R.string.error,
                                R.string.hint_start_more_than_end_date, new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                    }
                                });
                    } else if (Math.abs(startDate - endDate) / (1000 * 60 * 60 * 24) > 7) {
                        DialogTools.getInstance().showErrorMessage(getActivity(), R.string.error,
                                R.string.hint_more_than_seven_days, new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                    }
                                });
                    } else {
                        NewTaipeiSDKApi.getInstance().getRecord(getActivity(), NewTaipeiSDKActivity.userid,
                                start_time_et.getText().toString(), end_time_et.getText().toString(),
                                new NetworkManager.NetworkManagerListener<RecordData[]>() {
                                    @Override
                                    public void onSucceed(RecordData[] object) {
                                        getActivity().getSupportFragmentManager().beginTransaction()
                                                .replace(R.id.ntsdk_activity_main_fl, RecordFragment.newInstance(object))
                                                .addToBackStack(null)
                                                .commit();
                                    }

                                    @Override
                                    public void onFail(String errorMsg, boolean shouldRetry) {
                                        DialogTools.getInstance().showErrorMessage(getActivity(), R.string.error,
                                                R.string.hint_input_correct_date, new DialogInterface.OnDismissListener() {
                                                    @Override
                                                    public void onDismiss(DialogInterface dialog) {
                                                    }
                                                });
                                    }
                                });
                    }
                }

            });
        }
        return mView;
    }
}
