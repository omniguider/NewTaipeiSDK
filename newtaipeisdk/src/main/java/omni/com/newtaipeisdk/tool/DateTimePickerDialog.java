package omni.com.newtaipeisdk.tool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import java.util.Calendar;

public class DateTimePickerDialog extends AlertDialog implements OnClickListener {
    private DateTimePicker mDateTimePicker;
    private Calendar mDate = Calendar.getInstance();
    private OnDateTimeSetListener mOnDateTimeSetListener;
    private int mYear, mMouth, mDay;

    @SuppressWarnings("deprecation")
    public DateTimePickerDialog(Context context, final long date) {
        super(context);
        mDateTimePicker = new DateTimePicker(context);
        setView(mDateTimePicker);
//        mYear = mDate.get(Calendar.YEAR) - 1911;
        mYear = mDate.get(Calendar.YEAR);
        mMouth = mDate.get(Calendar.MONTH) + 1;
        mDay = mDate.get(Calendar.DAY_OF_MONTH);
        mDateTimePicker.setOnDateTimeChangedListener(new DateTimePicker.OnDateTimeChangedListener() {
            @Override
            public void onDateTimeChanged(DateTimePicker view, int year, int month, int day) {
                mYear = year;
                mMouth = month;
                mDay = day;
            }
        });

        setButton("確定", this);
        setButton2("取消", (OnClickListener) null);
    }

    public interface OnDateTimeSetListener {
        void OnDateTimeSet(AlertDialog dialog, int year, int month, int day);
    }

    public void setOnDateTimeSetListener(OnDateTimeSetListener callBack) {
        mOnDateTimeSetListener = callBack;
    }

    public void onClick(DialogInterface arg0, int arg1) {
        if (mOnDateTimeSetListener != null) {
            mOnDateTimeSetListener.OnDateTimeSet(this, mYear, mMouth, mDay);
        }
    }
}
