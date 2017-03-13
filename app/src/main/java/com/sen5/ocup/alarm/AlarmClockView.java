package com.sen5.ocup.alarm;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.sen5.ocup.R;
import com.sen5.ocup.activity.DatePickerDlg;
import com.sen5.ocup.callback.CustomInterface.IDialog;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.util.DBManager;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 闹钟列表
 */
public class AlarmClockView extends View implements IDialog, Callback {

	private final static String TAG = "AlarmClockView";
	private static final int DEL_ALARM_OK = 1;
	private static final int ADD_TIMER_OK = 2;
	private static final int EDITTIMER_TIMER_OK = 3;
	public static final int REQUEST_CODE = 0x01;
	private Context context;
	private Activity activty;
	private Handler mHandler;
	private ListView mAlarmListView;
	private DBManager mDBManager;
	private Time_show timesss;
	// private MyTimePickerDialog mytimepikerdialog;
	private List<Time_show> times;

	private LinearLayout add_alarm;
	private AlarmClockAdapter alarmAdapter;

	Calendar c = Calendar.getInstance();
	String tmpS1 = "";
	short isOpenAlarm = 1;
	private int clickPos;//点击编辑的index

	public AlarmClockView(Activity activity, final Context context) {
		super(context);

		this.context = context;
		this.activty = activity;
		mDBManager = new DBManager(context);
		mHandler = new Handler(this);
		times = new ArrayList<Time_show>();
		mAlarmListView = (ListView) activity.findViewById(R.id.alarm_list);
		add_alarm = (LinearLayout) activity.findViewById(R.id.add_alarm);
		add_alarm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				add_alarm();
			}
		});
		Log.d(TAG, "queryAlarmData(getcurrenttime()).size()=" + mDBManager.queryAlarmData(getcurrenttime()).size());
		alarmAdapter = new AlarmClockAdapter(context, mAlarmListView, times);
		mAlarmListView.setAdapter(alarmAdapter);

		mAlarmListView.setItemsCanFocus(true);
		mAlarmListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
				CustomDialog mCustomDialog = new CustomDialog(context, AlarmClockView.this, R.style.custom_dialog, CustomDialog.DEL_ALARM_DIALOG, arg2);
				mCustomDialog.show();
				return true;
			}

		});

		mAlarmListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
				clickPos = arg2;
				Intent intent = new Intent();
				intent.setClass(context, DatePickerDlg.class);
				intent.putExtra("type_timeDialog", DatePickerDlg.timedialog_edit);
				intent.putExtra("Time",times.get(arg2).getTime());
				intent.putExtra("dialogType", DatePickerDlg.dialog_time);
				activty.startActivityForResult(intent, REQUEST_CODE);
			}
		});
	}

	public void add_alarm() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		int mHour = c.get(Calendar.HOUR_OF_DAY);
		int mMinute = c.get(Calendar.MINUTE);
		
		Intent intent = new Intent();
		intent.setClass(context, DatePickerDlg.class);
		intent.putExtra("type_timeDialog", DatePickerDlg.timedialog_add);
		intent.putExtra("Time",mHour+":"+mMinute);
		intent.putExtra("dialogType", DatePickerDlg.dialog_time);
		activty.startActivityForResult(intent, REQUEST_CODE);
	}
	/**
	 * 对闹钟进行排序
	 */
	private void sortAlarm() {
		Collections.sort(times, new Comparator<Time_show>() {
			@Override
			public int compare(Time_show lhs, Time_show rhs) {
				// TODO Auto-generated method stub
				long date1 = timetomillion(lhs.getTime());
				long date2 = timetomillion( rhs.getTime());
				// 对日期字段进行升序
				if (date1 > date2) {
					return 1;
				}
				return -1;
			}
		});
		alarmAdapter.notifyDataSetChanged();
	}

	public String getcurrenttime() {

		Date date = new Date();
		Log.d(TAG, "date: " + date.toString());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 设置当前日期的格式
		String currentDate = sdf.format(date);// 当前日期
		return currentDate;

	}

	/**
	 *            yyyy-MM-dd
	 * @return
	 */
	private long timetomillion(String times) {
		String[] strTime = times.split(":");
		return Integer.parseInt(strTime[0].trim())*60+Integer.parseInt(strTime[1].trim());
	}

	public static Date stringToDate(String dateString) {
		ParsePosition position = new ParsePosition(0);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date dateValue = simpleDateFormat.parse(dateString, position);
		return dateValue;
	}

	private String format(int x) {
		String s = "" + x;
		if (s.length() == 1)
			s = "0" + s;
		return s;
	}

	public class MyTimePickerDialog extends TimePickerDialog {

		public MyTimePickerDialog(Context context, int theme, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
			super(context, theme, callBack, hourOfDay, minute, is24HourView);
		}

		@Override
		protected void onStop() {

		}

		@Override
		public void setTitle(CharSequence title) {
			super.setTitle(title);
		}
	}

	public List<Time_show> getRemindTime() {
		return times;
	}

	/**
	 * 
	 * @param b
	 */
	public void setGetCupRemind(boolean b) {
		if (b) {
			times.addAll(mDBManager.queryAlarmData(getcurrenttime()));
			alarmAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void ok(int type) {
		
	}

	@Override
	public void ok(int type, Object obj) {
		if (type == CustomDialog.DEL_ALARM_DIALOG) {
			Message msg = new Message();
			msg.what = DEL_ALARM_OK;
			msg.arg1 = (Integer) obj;
			mHandler.sendMessage(msg);
		}else if (type == CustomDialog.ADDTIMER_DIALOG) {
			Message msg = new Message();
			msg.what = ADD_TIMER_OK;
			String[] time = ((String) obj).split(":");
			msg.arg1 = Integer.parseInt(time[0]);
			msg.arg2 = Integer.parseInt(time[1]);
			mHandler.sendMessage(msg);
		}else if (type == CustomDialog.EDITTIMER_DIALOG) {
			Message msg = new Message();
			msg.what = EDITTIMER_TIMER_OK;
			String[] time = ((String) obj).split(":");
			msg.arg1 = Integer.parseInt(time[0]);
			msg.arg2 = Integer.parseInt(time[1]);
			mHandler.sendMessage(msg );
		}
	}

	@Override
	public void cancel(int type) {
		
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == DEL_ALARM_OK) {
			times.remove(msg.arg1);
			alarmAdapter.notifyDataSetChanged();
		}else if(msg.what == ADD_TIMER_OK){
			dealAddAlarm(msg.arg1,msg.arg2);
		}else if(msg.what == EDITTIMER_TIMER_OK){
			dealEditAlarm(msg.arg1,msg.arg2);
		}
		return false;
	}

	public String dealEditAlarm(int hour,int minute) {
		tmpS1 = format(hour) + ":" + format(minute);
		timesss = new Time_show(tmpS1, isOpenAlarm);
		times.get(clickPos).setTime(tmpS1);
		sortAlarm();
		return timesss.getTime();
	}
/**
 * 
 * @param hour 00
 * @param minute 00
 */
	public Time_show dealAddAlarm(int hour,int minute) {
		tmpS1 = format(hour) + ":" + format(minute);
		Log.d(TAG, times.size() + " ADD_TIMER_OK  tmpS1=="+tmpS1);
		timesss = new Time_show(tmpS1, isOpenAlarm);
		times.add(timesss);
		// 去重
		Set<Time_show> set = new HashSet<Time_show>(times);
		times.clear();
		times.addAll(set);
		Log.d(TAG, times.size() + " ADD_TIMER_OK  timenum...........");
		// 对时间进行排序
		sortAlarm();
		return timesss;
	}
}
