package omni.com.newtaipeisdk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import omni.com.newtaipeisdk.model.RecordData;
import omni.com.newtaipeisdk.network.NetworkManager;
import omni.com.newtaipeisdk.network.NewTaipeiSDKApi;
import omni.com.newtaipeisdk.tool.DateTimePickerDialog;
import omni.com.newtaipeisdk.tool.DialogTools;

public class QueryFragment extends Fragment {

    private View mView;
    private EditText start_time_et;
    private EditText end_time_et;
    private String start_time_ad;
    private String end_time_ad;
    private Calendar m_Calendar = Calendar.getInstance();
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

            final String myFormat = "yyy/MM/dd";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.TAIWAN);
            m_Calendar.set(Calendar.YEAR, m_Calendar.get(Calendar.YEAR) - 1911);
            start_time_et = mView.findViewById(R.id.fragment_record_start_time_et);
            start_time_et.setText(sdf.format(m_Calendar.getTime()));
            start_time_et.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DateTimePickerDialog dialog = new DateTimePickerDialog(getActivity(), System.currentTimeMillis());
                    dialog.setOnDateTimeSetListener(new DateTimePickerDialog.OnDateTimeSetListener() {
                        public void OnDateTimeSet(AlertDialog dialog, int year, int month, int day) {
                            m_Calendar.set(Calendar.YEAR, year);
                            m_Calendar.set(Calendar.MONTH, month - 1);
                            m_Calendar.set(Calendar.DAY_OF_MONTH, day);
                            String myFormat = "yyy/MM/dd";
                            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.TAIWAN);
                            start_time_et.setText(sdf.format(m_Calendar.getTime()));
                            start_time_ad = sdf.format(m_Calendar.getTime());
                            m_Calendar.set(Calendar.YEAR, year + 1911);
                            start_time_ad = sdf.format(m_Calendar.getTime());
                            startDate = m_Calendar.getTime().getTime();
                        }
                    });
                    dialog.show();
                }
            });

            end_time_et = mView.findViewById(R.id.fragment_record_end_time_et);
            end_time_et.setText(sdf.format(m_Calendar.getTime()));
            end_time_et.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DateTimePickerDialog dialog = new DateTimePickerDialog(getActivity(), System.currentTimeMillis());
                    dialog.setOnDateTimeSetListener(new DateTimePickerDialog.OnDateTimeSetListener() {
                        public void OnDateTimeSet(AlertDialog dialog, int year, int month, int day) {
                            m_Calendar.set(Calendar.YEAR, year);
                            m_Calendar.set(Calendar.MONTH, month - 1);
                            m_Calendar.set(Calendar.DAY_OF_MONTH, day);
                            String myFormat = "yyy/MM/dd";
                            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.TAIWAN);
                            end_time_et.setText(sdf.format(m_Calendar.getTime()));
                            m_Calendar.set(Calendar.YEAR, year + 1911);
                            end_time_ad = sdf.format(m_Calendar.getTime());
                            endDate = m_Calendar.getTime().getTime();
                        }
                    });
                    dialog.show();
                }
            });

            m_Calendar.set(Calendar.YEAR, m_Calendar.get(Calendar.YEAR) + 1911);
            start_time_ad = sdf.format(m_Calendar.getTime());
            end_time_ad = sdf.format(m_Calendar.getTime());
            startDate = m_Calendar.getTime().getTime();
            endDate = m_Calendar.getTime().getTime();

            currentDate = new Date().getTime();
            mView.findViewById(R.id.fragment_record_query_tv).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onClick(View view) {
                    if (startDate == null || endDate == null) {
                        DialogTools.getInstance().showErrorMessage(getActivity(), R.string.error,
                                R.string.hint_input_correct_date, new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                    }
                                });
                    } else if (endDate > currentDate || startDate > currentDate) {
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
                                start_time_ad, end_time_ad,
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
                                        if (errorMsg.equals("Forbidden")) {
                                            DialogTools.getInstance().showErrorMessage(getActivity(), R.string.error, getString(R.string.error_dialog_message_text_outside_domain));
                                        }
//                                        DialogTools.getInstance().showErrorMessage(getActivity(), R.string.error,
//                                                R.string.hint_input_correct_date, new DialogInterface.OnDismissListener() {
//                                                    @Override
//                                                    public void onDismiss(DialogInterface dialog) {
//                                                    }
//                                                });
                                    }
                                });
                    }
                }

            });
        }
        return mView;
    }
}
