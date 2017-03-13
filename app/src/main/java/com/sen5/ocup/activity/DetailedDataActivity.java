package com.sen5.ocup.activity;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.sen5.ocup.R;
import com.sen5.ocup.callback.RequestCallback.GetDrinkCallback;
import com.sen5.ocup.gui.CalendarView;
import com.sen5.ocup.gui.CalendarView.OnDateClickListener;
import com.sen5.ocup.gui.ChartView;
import com.sen5.ocup.struct.CalendarDate;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.Tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 数据图界面
 */
public class DetailedDataActivity extends BaseActivity implements GetDrinkCallback, OnClickListener {

	private static final String TAG = DetailedDataActivity.class.getSimpleName();
	private static final int REQUEST_CODE = 0x01;
	private static final int CALENDAR_ITEM = 35;
	private static final int LOAD_CALENDAR = 0x01;
	private static final int UPDATE_DATA = 0x02;
	private LinearLayout llBack;
	private TextView tvDate;
	private CheckBox chbCalendar;
	private GridView gvWeek;
	private CalendarView mCalendarView;
	private ChartView mChartView;
	private int mCalendarItemHeight = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detailed_data);
		
		getCtrl();
		ctrlRespond();
		initData();
	}
	
	private void getCtrl() {
		
		llBack = (LinearLayout) findViewById(R.id.ll_back);
		tvDate = (TextView) findViewById(R.id.tv_date);
		chbCalendar = (CheckBox) findViewById(R.id.chb_calendar);
		gvWeek = (GridView) findViewById(R.id.gv_week);
		mCalendarView = (CalendarView) findViewById(R.id.calendar_view);
		mChartView = (ChartView) findViewById(R.id.chart_view);
	}
	
	private void ctrlRespond() {
		
		llBack.setOnClickListener(this);
		chbCalendar.setOnCheckedChangeListener(new calendarOnChecked());
		mCalendarView.setOnDateClickListener(new onDateClick());
		tvDate.setOnClickListener(this);
		
	}
	
	/**
	 * 初始化星期
	 */
	private void initWeek() {
		
		String[] arrWeek = getResources().getStringArray(R.array.calendar_week);
		List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
		for(String str : arrWeek) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("week", str);
			items.add(item);
		} 
		SimpleAdapter adapter = new SimpleAdapter(this
												, items
												, R.layout.grid_week_item
												, new String[]{"week"}
												, new int[]{R.id.tv_week});
		gvWeek.setAdapter(adapter);
	}
	
	private void initData() {
		
		getNetworkData(Tools.getCurmonthtomillion() / 1000, System.currentTimeMillis() / 1000);
		float fDensity = getResources().getDisplayMetrics().density;
		mCalendarItemHeight = (int)(CALENDAR_ITEM * fDensity);
		// week
		initWeek();
		// display Date
		Calendar calendar = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH);
		setDate(calendar.get(Calendar.YEAR), month + 1);
		// 日历显示
//		setCalendarMeasure(chbCalendar.isChecked());
		
	}
	
	private void setDate(int nYear, int nMonth) {
		
		String strDate = "";
		if(nMonth < 10) {
			strDate = String.format("%d - 0%d", nYear, nMonth);
		} else {
			strDate = String.format("%d - %d", nYear, nMonth);
		}
		tvDate.setText(strDate);
	}

	// 更新数据表方法
	private void getNetworkData(long start, long end) {
		HttpRequest.getInstance().getDrink(this, start, end);
	}
	
	private void setCalendarMeasure(boolean isAllShow) {
		
		LinearLayout.LayoutParams params = null;
		if(isAllShow) {
			int nCount = mCalendarView.loadCurrenCalendar();
			params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, mCalendarItemHeight * nCount);
			
		} else {
			mCalendarView.loadCurrenWeek();
			params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, mCalendarItemHeight);
			
		}
		mCalendarView.setLayoutParams(params);
	}
	
	/**
	 * 更新日历数据
	 */
	private void updateDayData() {
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				String[] arrDate = tvDate.getText().toString().split(" - ");
				int[] date = new int[]{Integer.parseInt(arrDate[0]), Integer.parseInt(arrDate[1])};
				mCalendarView.updateDrinkYieldList(date[0], date[1] - 1);				
				handler.sendEmptyMessage(LOAD_CALENDAR);
			}
		}).start();
	}
	
	private class calendarOnChecked implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Log.e(TAG, "Checked:" + isChecked);
			setCalendarMeasure(isChecked);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_CODE) {
			if(resultCode == DatePickerDlg.RESULT_CODE) {
				final int nYear = data.getIntExtra("DATE_YEAR", 2014);
				final int nMonth = data.getIntExtra("DATE_MONTH", 1);
				setDate(nYear, nMonth);
				new Thread(new Runnable() {

					@Override
					public void run() {
						mCalendarView.updateDayList(nYear, nMonth - 1);
						handler.sendEmptyMessage(UPDATE_DATA);
					}
					
				}).start();
				
			}
		}
		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.ll_back:
			DetailedDataActivity.this.finish();
			break;
			
		case R.id.tv_date:
			Intent intent = new Intent();
			intent.setClass(DetailedDataActivity.this, DatePickerDlg.class);
			intent.putExtra("Date", tvDate.getText());
			intent.putExtra("dialogType", DatePickerDlg.dialog_date);
			startActivityForResult(intent, REQUEST_CODE);
			break;
			
		default:
			break;
		}
	}
	
	private class onDateClick implements OnDateClickListener {

		@Override
		public void onDayClicked(CalendarDate date) {
			mChartView.updateDataByTime(date.getDate());
		}
		
	}
	
	Handler handler = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what) {
			case LOAD_CALENDAR:
				Log.e(TAG, "LOAD_CALENDAR");
				if(chbCalendar.isChecked()) {
					// Calendar
					setCalendarMeasure(true);
				} else {
					// Week
					mCalendarView.loadCurrenWeek();
				}
				try {
					Log.e(TAG, "ChartView Success!!");
					mChartView.updateDataByTime(mCalendarView.getSelectCalendarDate().getDate());					
				} catch(IndexOutOfBoundsException e) {
					Log.e(TAG, "ChartView Failure!!");
					mChartView.updateDataByTime(0L);
				} catch(NullPointerException e) {
					mChartView.updateDataByTime(0L);
				} catch(Exception e) {
					mChartView.updateDataByTime(0L);
				}
				break;
				
			case UPDATE_DATA:
				updateDayData();
				int nDateFlag = mCalendarView.getDateFlag();
				switch(nDateFlag) {
				case 0:
					Log.e(TAG, "StartDate==" + mCalendarView.getStartDate() + "::EndDate==" + mCalendarView.getEndDate());
					getNetworkData(mCalendarView.getStartDate() / 1000, mCalendarView.getEndDate() / 1000);
					break;
					
				case 1:
					getNetworkData(Tools.getCurmonthtomillion() / 1000, System.currentTimeMillis() / 1000);
					break;
					
				case 2:
				default:
					updateDayData();
					break;
				}
				break;
				
			default:
				break;
			}
			return false;
		}
	});

	@Override
	public void getdrinkr_success() {
		Log.e(TAG, "GetDrinkSuccess");
		updateDayData();
	}

	@Override
	public void getdrinkr_failed() {
		Log.e(TAG, "GetDrinkFailure");
		updateDayData();
	}
	
}
