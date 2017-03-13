package com.sen5.ocup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.sen5.ocup.R;
import com.sen5.ocup.gui.NumberPicker;

/**
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 日期选择对话框界面
 */
public class DatePickerDlg extends BaseActivity implements OnClickListener{

	private static final String TAG = DatePickerDlg.class.getSimpleName();
	public static final int RESULT_CODE = 0x02;
	
	private static final int YEAR_START = 2014;
	private static final int YEAR_END = 2049;
	private static final int MONTH_START = 1;
	private static final int MONTH_END = 12;
	
	private static final int HOUR_START = 0;
	private static final int HOUR_END = 23;
	private static final int MINUTE_START = 0;
	private static final int MINUTE_END = 59;
	
	public static final int dialog_date = 1;
	public static final int dialog_time = 2;
	/**
	 * 对话框类型
	 */
	private int type_dialog;
	
	public static final int timedialog_add = 1;
	public static final int timedialog_edit= 2;
	private int type_timeDialog;//时间对话框的类型：2\edit or 1\add
	
	private NumberPicker pickerYear, pickerMonth;
	private Button btnOK, btnCancel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_date_picker);
		
		getCtrl();
		ctrlRespond();
		initData();
	}
	
	private void getCtrl() {
		pickerYear = (NumberPicker) findViewById(R.id.picker_date_year);
		pickerMonth = (NumberPicker) findViewById(R.id.picker_date_month);
		btnOK = (Button) findViewById(R.id.btn_date_ok);
		btnCancel = (Button) findViewById(R.id.btn_date_cancel);
		
		pickerYear.setFormatter(NumberPicker.getTwoDigitFormatter());
		pickerMonth.setFormatter(NumberPicker.getTwoDigitFormatter());
	}
	
	private void ctrlRespond() {
		
		btnOK.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
	}
	
	private void initData() {
		type_dialog = getIntent().getIntExtra("dialogType", dialog_date);
		Log.d(TAG, "initData------------type_dialog=="+type_dialog);
		int[] date = new int[2];
		if (type_dialog == dialog_date) {
			// Year
			pickerYear.setMinValue(YEAR_START);
			pickerYear.setMaxValue(YEAR_END);
//		pickerYear.setWrapSelectorWheel(false);
			pickerYear.setFocusableInTouchMode(true);
			// Month
			pickerMonth.setMinValue(MONTH_START);
			pickerMonth.setMaxValue(MONTH_END);
			pickerMonth.setFocusableInTouchMode(true);
			// 年与月数据初始化
			Bundle bundle = getIntent().getExtras();
			String[] arrDate = bundle.getString("Date").split(" - ");
			Log.e(TAG, bundle.getString("Date"));
			date[0] = Integer.parseInt(arrDate[0]);
			date[1] =Integer.parseInt(arrDate[1]);
			if(date[0] < YEAR_START) {
				date[0] = YEAR_START;
			} else if(date[0] > YEAR_END) {
				date[0] = YEAR_END;
			}
			
		}else if(type_dialog == dialog_time){
			type_timeDialog = getIntent().getIntExtra("type_timeDialog", timedialog_add);
			// Hour
			pickerYear.setMinValue(HOUR_START);
			pickerYear.setMaxValue(HOUR_END);
			pickerYear.setFocusableInTouchMode(true);
			// minute
			pickerMonth.setMinValue(MINUTE_START);
			pickerMonth.setMaxValue(MINUTE_END);
			pickerMonth.setFocusableInTouchMode(true);
			// 小时与分钟数据初始化
			Bundle bundle = getIntent().getExtras();
			String[] arrDate = bundle.getString("Time").split(":");
			Log.e(TAG, bundle.getString("Time"));
			date[0] = Integer.parseInt(arrDate[0].trim());
			date[1] =Integer.parseInt(arrDate[1].trim());
			if(date[0] < HOUR_START) {
				date[0] = HOUR_START;
			} else if(date[0] > MINUTE_END) {
				date[0] = MINUTE_END;
			}
		}
		
		pickerYear.setValue(date[0]);
		pickerMonth.setValue(date[1]);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btn_date_ok:
			Intent intent = new Intent();
			if (type_dialog == dialog_date) {  
				intent.putExtra("DATE_YEAR", pickerYear.getValue());
				intent.putExtra("DATE_MONTH", pickerMonth.getValue());
			}else if(type_dialog == dialog_time){
				intent.putExtra("dialogType", type_timeDialog);
				intent.putExtra("hour", pickerYear.getValue());
				intent.putExtra("minute", pickerMonth.getValue());
			}
			setResult(RESULT_CODE, intent);
			DatePickerDlg.this.finish();
			break;
			
		case R.id.btn_date_cancel:
			DatePickerDlg.this.finish();
			break;
			
		default:
			break;
		}
	}

}




