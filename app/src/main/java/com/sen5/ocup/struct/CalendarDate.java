package com.sen5.ocup.struct;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import android.text.format.Time;

public class CalendarDate{
	
	private int startDay;
	private int monthEndDay;
	private int day;
	private int year;
	private int month;
	
	public CalendarDate(int day, int year, int month){
		this.day = day;
		this.year = year;
		this.month = month;
		Calendar cal = Calendar.getInstance();
		cal.set(year, month-1, day);
		int end = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(year, month, end);
		TimeZone tz = TimeZone.getDefault();
		monthEndDay = Time.getJulianDay(cal.getTimeInMillis(), TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(cal.getTimeInMillis())));
	}
	
	public int getMonth(){
		return month;
	}
	
	public int getYear(){
		return year;
	}
	
	public void setDay(int day){
		this.day = day;
	}
	
	public int getDay(){
		return day;
	}
	
	public int getStartDay() {
		return startDay;
	}
	
	public int getMonthEndDay() {
		return monthEndDay;
	}
	
	public long getDate() {
		
		String strDate = year + "-" + (month + 1) + "-" + day;
		return dateToMillion(strDate);
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

}
