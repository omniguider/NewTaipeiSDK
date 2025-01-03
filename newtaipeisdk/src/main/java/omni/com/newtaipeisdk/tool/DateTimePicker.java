package omni.com.newtaipeisdk.tool;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import java.util.Calendar;

import omni.com.newtaipeisdk.R;

public class DateTimePicker extends FrameLayout {
    private final NumberPicker mYearSpinner;
    private final NumberPicker mMonthSpinner;
    private final NumberPicker mDaySpinner;
    private Calendar mDate;
    private int mYear, mMouth, mDay;
    private OnDateTimeChangedListener mOnDateTimeChangedListener;

    public DateTimePicker(Context context) {
        super(context);
        mDate = Calendar.getInstance();
//        mYear = mDate.get(Calendar.YEAR) - 1911;
        mYear = mDate.get(Calendar.YEAR);
        mMouth = mDate.get(Calendar.MONTH) + 1;
        mDay = mDate.get(Calendar.DAY_OF_MONTH);
        inflate(context, R.layout.custom_time_picker, this);

        mYearSpinner = this.findViewById(R.id.np_year);
        mYearSpinner.setMinValue(1);
        mYearSpinner.setMaxValue(200);
        mYearSpinner.setValue(mYear - 1911);
        mYearSpinner.setOnValueChangedListener(mOnYearChangedListener);

        mMonthSpinner = this.findViewById(R.id.np_month);
        mMonthSpinner.setMaxValue(12);
        mMonthSpinner.setMinValue(1);
        mMonthSpinner.setValue(mMouth);
        mMonthSpinner.setOnValueChangedListener(mOnMonthChangedListener);

        mDaySpinner = this.findViewById(R.id.np_day);
        mDaySpinner.setMaxValue(31);
        mDaySpinner.setMinValue(1);
        mDaySpinner.setValue(mDay);
        mDaySpinner.setOnValueChangedListener(mOnDayChangedListener);
    }

    private NumberPicker.OnValueChangeListener mOnYearChangedListener = new OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mYear = mYearSpinner.getValue() + 1911;
            onDateTimeChanged();
        }
    };

    private NumberPicker.OnValueChangeListener mOnMonthChangedListener = new OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mMouth = mMonthSpinner.getValue();
            onDateTimeChanged();
        }
    };

    private NumberPicker.OnValueChangeListener mOnDayChangedListener = new OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            mDay = mDaySpinner.getValue();
            onDateTimeChanged();
        }
    };

    public interface OnDateTimeChangedListener {
        void onDateTimeChanged(DateTimePicker view, int year, int month, int day);
    }

    public void setOnDateTimeChangedListener(OnDateTimeChangedListener callback) {
        mOnDateTimeChangedListener = callback;
    }

    private void onDateTimeChanged() {
        if (mOnDateTimeChangedListener != null) {
            mOnDateTimeChangedListener.onDateTimeChanged(this, mYear, mMouth, mDay);
        }
    }
}
