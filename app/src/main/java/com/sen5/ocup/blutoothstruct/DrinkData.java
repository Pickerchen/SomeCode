package com.sen5.ocup.blutoothstruct;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;
/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :饮水数据结构体
 */
public class DrinkData {
	private static final String TAG = "DrinkData";
	private String cupid;
	// 当前水温
	private int water_temp;
	// 每次喝水量
	private int water_yield;
	// 当次喝水时间，以秒钟为单位
	private int drink_time;
	private long drink_date;//2014-10-14 对应的毫秒数
	
	

	public DrinkData() {
		super();
	}

	public DrinkData(String cupid, int water_temp, int water_yield, int drink_time, long drink_date) {
		super();
		this.cupid = cupid;
		this.water_temp = water_temp;
		this.water_yield = water_yield;
		this.drink_time = drink_time;
		this.drink_date = drink_date;
	}


	public String getCupid() {
		return cupid;
	}


	public void setCupid(String cupid) {
		this.cupid = cupid;
	}


	public long getDrink_date() {
		return drink_date;
	}

	public void setDrink_date(long drink_date) {
//		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd");
		  String str_date = f.format(drink_date);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date d2 = null;
			try {
				d2 = sdf.parse(str_date);
				Log.d(TAG, "setDrink_date---d2===" + d2);
				this.drink_date = d2.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
	}

	public int getWater_temp() {
		return water_temp;
	}

	public void setWater_temp(int water_temp) {
		this.water_temp = water_temp;
	}

	public int getWater_yield() {
		return water_yield;
	}

	public void setWater_yield(int water_yield) {
		this.water_yield = water_yield;
	}

	public int getDrink_time() {
		return drink_time;
	}
	
	public void setDrink_time(int drink_time) {
			this.drink_time =drink_time;
	}

	public void getString(){
		Log.d(TAG, "  water_temp=="+water_temp+"  water_yield=="+water_yield+"   drink_time=="+drink_time+"   drink_date=="+drink_date);
	}
}
