package com.sen5.ocup.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.sen5.ocup.R;
import com.sen5.ocup.blutoothstruct.DrinkYield;
import com.sen5.ocup.gui.SegoTextView;
import com.sen5.ocup.struct.CalendarDate;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 日历适配器
 */
public class CalendarViewAdapter extends BaseAdapter {
	
	private static final String TAG = "CalendarViewAdapter";
	private Context mContext;
	private final static int DRINK_GOAL = 1500;
	private ArrayList<CalendarHolder> mCalendarHolder;
	private int mCurrentDay = 1;
	/**
	 * 用户选择的日子
	 */
	private int mSelectDay = 0;
	private ArrayList<CalendarDate> mDateList = null;
	private ArrayList<DrinkYield> mDrinkYieldList = null;
	/**
	 * 日期标志位
	 * 0：和手机时间比较，过去日子
	 * 1：和手机时间比较，当前日子
	 * 2：和手机时间比较，未来日子
	 */
	private int mDateFlag = 0;
	
	public CalendarViewAdapter(Context context
							, ArrayList<CalendarDate> dateList
							, ArrayList<DrinkYield> drinkYieldList
							, int nSelectDay
							, int nDateFlag) {
		mContext = context;
		mDateList = new ArrayList<CalendarDate>(dateList);
		mDrinkYieldList = new ArrayList<DrinkYield>(drinkYieldList);
		mCurrentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		mSelectDay = nSelectDay;
		mDateFlag = nDateFlag;
	}

	@Override
	public int getCount() {
		return mDateList.size();
	}

	@Override
	public CalendarDate getItem(int position) {
		return mDateList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public ArrayList<CalendarHolder> getCalendarHolder() {
		return mCalendarHolder;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		CalendarHolder holder = null;
		if(null == convertView) {
			synchronized (CalendarViewAdapter.this) {
				holder = new CalendarHolder();
				mCalendarHolder = new ArrayList<CalendarHolder>();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_calander_item, null);
				holder.tvCalendarDay = (SegoTextView) convertView.findViewById(R.id.tv_calendar_day);
				holder.imgDayDescribe = (ImageView) convertView.findViewById(R.id.iv_day_describe);
				convertView.setTag(holder);
				mCalendarHolder.add(holder);
			}
		} else {
			holder = (CalendarHolder) convertView.getTag();
		}
		
		// 数据处理
		CalendarDate date = mDateList.get(position);
		int nWhichDay = date.getDay();
		// 天
		if(0 != nWhichDay) {
			
			holder.tvCalendarDay.setVisibility(View.VISIBLE);
			if(mDateFlag == 2) {
				holder.tvCalendarDay.setTextColor(Color.parseColor("#adadae"));
				holder.tvCalendarDay.setBackgroundColor(Color.TRANSPARENT);
				holder.tvCalendarDay.setText(String.valueOf(date.getDay()));
			
			} else {
				if(mDateFlag == 0 || nWhichDay <= mCurrentDay) {
					if(mSelectDay == nWhichDay) {
						holder.tvCalendarDay.setTextColor(Color.parseColor("#ffffff"));
						holder.tvCalendarDay.setBackgroundResource(R.drawable.day_selected);
					} else {
						holder.tvCalendarDay.setTextColor(mContext.getResources().getColorStateList(R.color.date_day_black));
						holder.tvCalendarDay.setBackgroundResource(R.drawable.date_number_background);					
					}
					holder.tvCalendarDay.setText(String.valueOf(nWhichDay));
					// 判断当天的饮水量情况
					int nDrinkYield = 0;
					if(null == mDrinkYieldList || mDrinkYieldList.size() <= 0) {
						nDrinkYield = 0;
					} else {
						if(mDrinkYieldList.size() > (nWhichDay - 1)) {
							nDrinkYield = mDrinkYieldList.get(nWhichDay - 1).getWater_yield();
							Log.e(TAG, "db DrinkYield:" + nDrinkYield);
						} else {
							nDrinkYield = 0;
						}
					}
					if(nDrinkYield >= DRINK_GOAL){
						holder.imgDayDescribe.setBackgroundResource(R.drawable.yes);
						holder.imgDayDescribe.setVisibility(View.VISIBLE);
					} else {
						if(0 >= nDrinkYield) {
							holder.imgDayDescribe.setBackgroundResource(R.drawable.no);
							holder.imgDayDescribe.setVisibility(View.VISIBLE);
						} else {
							holder.imgDayDescribe.setBackgroundResource(R.drawable.yes);
							holder.imgDayDescribe.setVisibility(View.VISIBLE);
						}
					}
				} else {
					holder.tvCalendarDay.setTextColor(Color.parseColor("#adadae"));
					holder.tvCalendarDay.setBackgroundColor(Color.TRANSPARENT);
					holder.tvCalendarDay.setText(String.valueOf(date.getDay()));
				}
			}
		}
		return convertView;
	}
	
	public void updateDrinkYieldList(ArrayList<DrinkYield> drinkYieldList) {
		
		mDrinkYieldList = new ArrayList<DrinkYield>(drinkYieldList);
		notifyDataSetChanged();
	}
	
	public void updateDateList(ArrayList<CalendarDate> dateList) {
		
		mDateList = new ArrayList<CalendarDate>(dateList);
		notifyDataSetChanged();
	}
	
	public void setSelectDay(int nSelectDay) {
		mSelectDay = nSelectDay;
		notifyDataSetChanged();
	}
	
	public CalendarDate getCalendarDate() {
		
		CalendarDate calendarDate = null;
		for(CalendarDate date : mDateList) {
			if( mSelectDay == date.getDay()) {
				calendarDate = date;
				break;
			}
		}
		return calendarDate;
	}
	
	public class CalendarHolder {
		
		public SegoTextView tvCalendarDay;
		public ImageView imgDayDescribe;
	}
		
}
