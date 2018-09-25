package omni.com.newtaipeisdk;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
                @Override
                public void onClick(View view) {
                    callRecordApi(ON_DUTY, NewTaipeiSDKActivity.username, NewTaipeiSDKActivity.userid,
                            NewTaipeiSDKActivity.major, NewTaipeiSDKActivity.minor, getString(R.string.on_duty_time));
                }
            });
            mView.findViewById(R.id.fragment_service_tv_query_off_duty).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callRecordApi(OFF_DUTY, NewTaipeiSDKActivity.username, NewTaipeiSDKActivity.userid,
                            NewTaipeiSDKActivity.major, NewTaipeiSDKActivity.minor, getString(R.string.off_duty_time));
                }
            });
            mView.findViewById(R.id.fragment_service_tv_go_out).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callRecordApi(GO_OUT, NewTaipeiSDKActivity.username, NewTaipeiSDKActivity.userid,
                            NewTaipeiSDKActivity.major, NewTaipeiSDKActivity.minor, getString(R.string.go_out_time));
                }
            });
            mView.findViewById(R.id.fragment_service_tv_come_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callRecordApi(COME_BACK, NewTaipeiSDKActivity.username, NewTaipeiSDKActivity.userid,
                            NewTaipeiSDKActivity.major, NewTaipeiSDKActivity.minor, getString(R.string.come_back_time));
                }
            });
            mView.findViewById(R.id.fragment_service_tv_on_duty_overtime).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callRecordApi(ON_DUTY_OT, NewTaipeiSDKActivity.username, NewTaipeiSDKActivity.userid,
                            NewTaipeiSDKActivity.major, NewTaipeiSDKActivity.minor, getString(R.string.on_duty_overtime_time));
                }
            });
            mView.findViewById(R.id.fragment_service_tv_off_duty_overtime).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callRecordApi(OFF_DUTY_OT, NewTaipeiSDKActivity.username, NewTaipeiSDKActivity.userid,
                            NewTaipeiSDKActivity.major, NewTaipeiSDKActivity.minor, getString(R.string.off_duty_overtime_time));
                }
            });
            mView.findViewById(R.id.fragment_service_tv_for_testing).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callRecordApi(FOR_TESTING, NewTaipeiSDKActivity.username, NewTaipeiSDKActivity.userid,
                            NewTaipeiSDKActivity.major, NewTaipeiSDKActivity.minor, getString(R.string.for_testing_time));
                }
            });
        }
        return mView;
    }

    private void callRecordApi(String status, String username, String idcount, String major, String minor, final String title) {

        NewTaipeiSDKApi.getInstance().setRecord(getActivity(), status, username, idcount, major, minor,
                new NetworkManager.NetworkManagerListener<ClockResponse>() {
                    @Override
                    public void onSucceed(ClockResponse object) {
                        showSuccessMessage(title, object.getTimestamp());
                    }

                    @Override
                    public void onFail(String errorMsg, boolean shouldRetry) {
                        showErrorMessage(errorMsg);
                    }
                });
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
