package com.sen5.ocup.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.sen5.ocup.R;
import com.sen5.ocup.adapter.CalendarViewAdapter;
import com.sen5.ocup.blutoothstruct.DrinkYield;
import com.sen5.ocup.struct.CalendarDate;
import com.sen5.ocup.util.DBManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class CalendarView extends RelativeLayout implements OnItemClickListener {

	private static final String TAG = CalendarView.class.getSimpleName();
	private static final int UPDATE_CALENDAR = 0x01;
	private static final int LOAD_WEEK = 0x02;
	private static final int LOAD_CALENDAR = 0x03;
	private Context mContext;
	private OnDateClickListener mDateListener;
	private GridView gridCalendar;
	private CalendarViewAdapter mAdapter;
	private DBManager mDBManager;
	private ArrayList<CalendarDate> mDayList = null;
	private ArrayList<DrinkYield> mDrinkYieldList = null;
	private int mSelectDay = 0;
	private int mDateFlag = 2;

	public CalendarView(Context context) {
		this(context, null);
	}

	public CalendarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CalendarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	@SuppressLint("NewApi")
	private void init() {

		mDBManager = new DBManager(mContext);
		mDayList = new ArrayList<CalendarDate>(overrideDayList(0, 0));
		mSelectDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
//		updateDrinkYieldList();
		mDrinkYieldList = new ArrayList<DrinkYield>();
		gridCalendar = new GridView(mContext);
		gridCalendar.setVerticalSpacing(10);
		gridCalendar.setHorizontalSpacing(4);
		gridCalendar.setNumColumns(7);
//		Color.parseColor("#00000000")
		gridCalendar.setBackgroundColor(Color.TRANSPARENT);
		gridCalendar.setSelector(mContext.getResources().getDrawable(R.drawable.grid_selector));//不设置这个选中时为默认的显示
		gridCalendar.setChoiceMode(GridView.CHOICE_MODE_NONE);
		gridCalendar.setDrawSelectorOnTop(true);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		gridCalendar.setLayoutParams(params);
		gridCalendar.setOnItemClickListener(this);
//		mAdapter = new CalendarViewAdapter(mContext, mDateList, mDrinkYieldList, mSelectDay);
//		gridCalendar.setAdapter(mAdapter);
		addView(gridCalendar);

		loadCurrenWeek();

	}

	public int loadCurrenCalendar() {

		int nCount = mDayList.size() / 7;
		if((mDayList.size() % 7) != 0) {
			nCount++;
		}
		Log.e(TAG, "DayList===" + mDayList.size() + ":::Count===" + nCount);
//		mAdapter.updateDateList(mDateList);
		mAdapter = new CalendarViewAdapter(mContext, mDayList, mDrinkYieldList, mSelectDay, mDateFlag);
		gridCalendar.setAdapter(mAdapter);
		return nCount;
	}

	public void loadCurrenWeek() {

		int nCurrentDayIndex = 0;
		for(int i = 0; i < mDayList.size(); i++) {
			if(mSelectDay == mDayList.get(i).getDay()) {
				nCurrentDayIndex = i;
				break;
			}
		}
		// 7为一个星期只有七天
		int nCount = nCurrentDayIndex / 7;
		int nBeginDayIndex = 7 * nCount;
		ArrayList<CalendarDate> dateList = new ArrayList<CalendarDate>();
		for(int i = 0; i < 7; i++) {
			try {
				dateList.add(mDayList.get(i + nBeginDayIndex));
			} catch(IndexOutOfBoundsException e) {
				break;
			}
		}
//		mAdapter.updateDateList(dateList);
		mAdapter = new CalendarViewAdapter(mContext, dateList, mDrinkYieldList, mSelectDay, mDateFlag);
		gridCalendar.setAdapter(mAdapter);
	}

	public void updateDrinkYieldList(int nYear, int nMonth) {

//		// 把整个月搜索出来
//		// 找到第一天
//		CalendarDay startDay = null;
//		for(CalendarDay day : mDayList) {
//			if(0 != day.getDay()) {
//				startDay = day;
//				break;
//			}
//		}
//		if(null == startDay) {
//			return;
//		}
//		String strStartDate = startDay.getYear() + "-" + (startDay.getMonth() + 1) + "-" + startDay.getDay();
//		CalendarDay endDay = mDayList.get(mDayList.size() - 1);
//		String strEndDate = endDay.getYear() + "-" + (endDay.getMonth() + 1) + "-" + endDay.getDay();
//		long nStartDate = dateToMillion(strStartDate);
//		long nEndDate = dateToMillion(strEndDate);
//		mDrinkYieldList = new ArrayList<DrinkYield>(mDBManager.queryDrinkYield_month2(nStartDate, nEndDate));

		// 重新加载日子
		mDayList = new ArrayList<CalendarDate>(overrideDayList(nYear, nMonth));
		mDateFlag = judgeDateFlag(nYear, nMonth);
		// 一天一天地查询
		mDrinkYieldList = new ArrayList<DrinkYield>();
		int index = 1;
		for(CalendarDate date : mDayList) {
			if(0 != date.getDay()) {
				index++;
				mDrinkYieldList.add(mDBManager.queryDrinkYield_day(date.getDate()));
				if(0 == index % 10) {
//					handler.sendEmptyMessage(UPDATE_CALENDAR);
				}
			}
		}
	}

	/**
	 * 日期标志位
	 * 0：和手机时间比较，过去日子
	 * 1：和手机时间比较，当前日子
	 * 2：和手机时间比较，未来日子
	 * @param nYear
	 * @param nMonth
	 * @return
	 */
	private int judgeDateFlag(int nYear, int nMonth) {

		int nDateFlag = 0;
		if(nYear > 0) {
			long currentDate = dateToMillion(String.format("%d-%d-01"
					, Calendar.getInstance().get(Calendar.YEAR)
					, Calendar.getInstance().get(Calendar.MONTH)));
			long selectDate = dateToMillion(String.format("%d-%d-01", nYear, nMonth));
			if(currentDate == selectDate) {
				nDateFlag = 1;
			} else if(currentDate > selectDate) {
				nDateFlag = 0;
			} else {
				nDateFlag = 2;
			}
		}
		return nDateFlag;
	}

	private long dateToMillion(String date) {
		long time=0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date d2 = null;
		try {
			d2 = sdf.parse(date);
			time = d2.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}

	private ArrayList<CalendarDate> overrideDayList(int nYear, int nMonth) {

		Calendar calendar = Calendar.getInstance();
		if(nYear > 0) {
			calendar.set(nYear, nMonth, 1);
		}
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		String[] days = null;
		ArrayList<CalendarDate> dayList = new ArrayList<CalendarDate>();
		int nDayCount = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		int nFirstDay = (int)calendar.get(Calendar.DAY_OF_WEEK);
		Log.e(TAG, "----------&&&&&&&------nDayCount = " + nDayCount + ":::nFirstDay = " + nFirstDay);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);

		// size
//        if(1 == nFirstDay){
//        	days = new String[nDayCount];
//        } else {
		days = new String[nDayCount + nFirstDay - 1];
//        }

		// 1号前面的都是空白日子
		if(nFirstDay > 1) {
			for (int j = 1; j < nFirstDay; j++) {
				days[j] = "";
				CalendarDate d = new CalendarDate(0, 0, 0);
				dayList.add(d);
			}
		}

		// days
		int dayNumber = 1;
		for (int i = nFirstDay - 1; i < days.length; i++) {
			CalendarDate date = new CalendarDate(dayNumber, year, month);
			Log.e(TAG, "[Date]year==" + year + "::month==" + month + "::dayNumber==" + dayNumber);
			Calendar cal = Calendar.getInstance();
			cal.set(year, month, dayNumber);
			days[i] = String.valueOf(dayNumber);
			dayNumber++;
			dayList.add(date);
		}
		return dayList;
	}

	public Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what) {
				case UPDATE_CALENDAR:
//				mAdapter.updateDrinkYieldList(mDrinkYieldList);
					mAdapter.notifyDataSetChanged();
					break;

				case LOAD_CALENDAR:
					loadCurrenCalendar();
					break;

				case LOAD_WEEK:
					Log.e(TAG, "LOAD_WEEK");
					loadCurrenWeek();
					break;

				default:
					break;
			}
			return false;
		}
	});

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		int nCurrentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		CalendarDate date = mAdapter.getItem(position);
		if(mDateFlag == 2 || (1 == mDateFlag && nCurrentDay < date.getDay())) {
			return;
		}
		mSelectDay = date.getDay();
		mAdapter.setSelectDay(mSelectDay);
		mDateListener.onDayClicked(date);
	}

	public CalendarDate getSelectCalendarDate() {

//		CalendarDate date = mAdapter.getItem(mSelectDay);
		return mAdapter.getCalendarDate();
	}

	public int getDateFlag() {

		return mDateFlag;
	}

	/**
	 * 月：第一天时间
	 * @return
	 */
	public long getStartDate() {

		CalendarDate startDate = null;
		for(CalendarDate date : mDayList) {
			if(0 != date.getDay()) {
				startDate = date;
				break;
			}
		}
		return startDate.getDate();

	}

	/**
	 * 月：最后一天时间
	 * @return
	 */
	public long getEndDate() {

		CalendarDate endDate = mDayList.get(mDayList.size() - 1);
		String strEndDate = String.format("%d-%d-%d %d:%d:%d"
				, endDate.getYear(), endDate.getMonth() + 1, endDate.getDay()
				, 23, 59, 59);
		Log.e(TAG, "testDate==" + strEndDate);
		long time=0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d2 = null;
		try {
			d2 = sdf.parse(strEndDate);
			time = d2.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;

	}

	public void updateDayList(int nYear, int nMonth) {

		mDateFlag = judgeDateFlag(nYear, nMonth);
		mDayList = new ArrayList<CalendarDate>(overrideDayList(nYear, nMonth));
	}

	public interface OnDateClickListener {
		public void onDayClicked(CalendarDate date);
	}

	public void setOnDateClickListener(OnDateClickListener listener){
		if(null != gridCalendar){
			mDateListener = listener;
		}
	}
}



