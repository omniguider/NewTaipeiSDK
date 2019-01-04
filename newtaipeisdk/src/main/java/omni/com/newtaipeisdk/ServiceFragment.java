package omni.com.newtaipeisdk;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;

import omni.com.newtaipeisdk.model.ClockResponse;
import omni.com.newtaipeisdk.network.NetworkManager;
import omni.com.newtaipeisdk.network.NewTaipeiSDKApi;
import omni.com.newtaipeisdk.tool.DialogTools;

public class ServiceFragment extends Fragment {

    private View mView;
    private String ON_DUTY = "1";
    private String OFF_DUTY = "2";
    private String GO_OUT = "3";
    private String COME_BACK = "4";
    private String ON_DUTY_OT = "5";
    private String OFF_DUTY_OT = "6";
    private String FOR_TESTING = "7";
    public static boolean isServiceFragment;
    private Long currentTime = 0L;
    final int PUNCH_TIME_OUT = 600000;
    private SharedPreferences settings = null;

    public static ServiceFragment newInstance() {
        Bundle args = new Bundle();
        ServiceFragment fragment = new ServiceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        isServiceFragment = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isServiceFragment = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_service, container, false);
            mView.findViewById(R.id.fragment_service_fl_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });

            mView.findViewById(R.id.fragment_service_tv_punch_on_duty).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onClick(View view) {
                    callRecordApi(ON_DUTY, NewTaipeiSDKActivity.username, NewTaipeiSDKActivity.userid,
                            NewTaipeiSDKActivity.hwid, getString(R.string.on_duty_time));
                }
            });
            mView.findViewById(R.id.fragment_service_tv_query_off_duty).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onClick(View view) {
                    callRecordApi(OFF_DUTY, NewTaipeiSDKActivity.username, NewTaipeiSDKActivity.userid,
                            NewTaipeiSDKActivity.hwid, getString(R.string.off_duty_time));
                }
            });
            mView.findViewById(R.id.fragment_service_tv_go_out).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onClick(View view) {
                    callRecordApi(GO_OUT, NewTaipeiSDKActivity.username, NewTaipeiSDKActivity.userid,
                            NewTaipeiSDKActivity.hwid, getString(R.string.go_out_time));
                }
            });
            mView.findViewById(R.id.fragment_service_tv_come_back).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onClick(View view) {
                    callRecordApi(COME_BACK, NewTaipeiSDKActivity.username, NewTaipeiSDKActivity.userid,
                            NewTaipeiSDKActivity.hwid, getString(R.string.come_back_time));
                }
            });
            mView.findViewById(R.id.fragment_service_tv_on_duty_overtime).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onClick(View view) {
                    callRecordApi(ON_DUTY_OT, NewTaipeiSDKActivity.username, NewTaipeiSDKActivity.userid,
                            NewTaipeiSDKActivity.hwid, getString(R.string.on_duty_overtime_time));
                }
            });
            mView.findViewById(R.id.fragment_service_tv_off_duty_overtime).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onClick(View view) {
                    callRecordApi(OFF_DUTY_OT, NewTaipeiSDKActivity.username, NewTaipeiSDKActivity.userid,
                            NewTaipeiSDKActivity.hwid, getString(R.string.off_duty_overtime_time));
                }
            });
            mView.findViewById(R.id.fragment_service_tv_for_testing).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onClick(View view) {
                    callRecordApi(FOR_TESTING, NewTaipeiSDKActivity.username, NewTaipeiSDKActivity.userid,
                            NewTaipeiSDKActivity.hwid, getString(R.string.for_testing_time));
                }
            });
        }
        return mView;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void callRecordApi(String status, String username, String idcount, String hwid, final String title) {

        currentTime = Calendar.getInstance().getTime().getTime();
        settings = getActivity().getSharedPreferences(NewTaipeiSDKActivity.TAG, 0);
        Long lastPunchTime = settings.getLong("lastPunchTime", 0);
        if (currentTime - lastPunchTime < PUNCH_TIME_OUT && !status.equals(FOR_TESTING)) {
            showErrorMessage(getString(R.string.error_dialog_message_text_ten_minutes));
            getActivity().getSupportFragmentManager().popBackStack();
        } else {
            NewTaipeiSDKApi.getInstance().setRecord(getActivity(), status, username, idcount, hwid,
                    new NetworkManager.NetworkManagerListener<ClockResponse>() {
                        @Override
                        public void onSucceed(ClockResponse object) {
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putLong("lastPunchTime", Calendar.getInstance().getTime().getTime());
                            editor.commit();

                            if (object.getErrorMessage() != null && object.getErrorMessage().equals("ACCESS_DENY")) {
                                showErrorMessage(getString(R.string.error_dialog_message_text_system_time));
                            } else if (object.getResult().equals("false") && object.getErrorMessage() != null) {
                                showErrorMessage(object.getErrorMessage());
                            } else {
                                String[] separated = object.getTimestamp().split("\\s+");
                                showSuccessMessage(title, separated[1]);
                            }
                        }

                        @Override
                        public void onFail(String errorMsg, boolean shouldRetry) {
                            if (errorMsg.equals("Forbidden")) {
                                showErrorMessage(getString(R.string.error_dialog_message_text_outside_domain));
                            }
                        }
                    });
        }
    }

    private void showSuccessMessage(String title, String successMsg) {
        DialogTools.getInstance().showErrorMessage(getActivity(), title, successMsg, new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
    }

    private void showErrorMessage(String errorMsg) {
        DialogTools.getInstance().showErrorMessage(getActivity(), R.string.error, errorMsg, new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
    }
}
