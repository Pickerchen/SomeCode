package com.sen5.ocup.blutoothstruct;

import android.util.Log;

//uint16 cur_water_temp;      //当前水温
//uint16 cur_water_yield;     //当前水杯中的水量
//  uint16 prev_water_yield;    //上一次水杯中的水量
//  uint16 total_water_yield;   //当天总的喝水量
//  GSENSOR_DATA gsensor_data;   //当前Gsensor的状态
//int8 cur_battery_capacity  //电量
//int16 cur_time;              //当前时间
//  int8 drink_valid_flag;       //当次喝水有效标志

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :蓝牙通信协议中定义的杯子状态结构体
 */
public class CupStatus {

	/**
	 * 创建单例
	 */
	private static CupStatus mInstance = null;

	private int cur_water_temp;
	private int cur_water_yield;
	private int prev_water_yield;
	private int total_water_yield;
	private int cur_battery_capacity;
	private int cur_time;
	private int drink_valid_flag;

	public static CupStatus getInstance() {
		if (mInstance == null) {
			mInstance = new CupStatus();
		}
		return mInstance;
	}
	
	public static void setCupStatusNull(){
		if(null != mInstance){
			mInstance = null;
		}
	}

	private CupStatus() {
	}

	
	public int getCur_battery_capacity() {
		return cur_battery_capacity;
	}

	public void setCur_battery_capacity(int cur_battery_capacity) {
		this.cur_battery_capacity = cur_battery_capacity;
	}

	public int getCur_water_temp() {
		return cur_water_temp;
	}

	public void setCur_water_temp(int cur_water_temp) {
		this.cur_water_temp = cur_water_temp;
	}

	public long getCur_water_yield() {
		return cur_water_yield;
	}

	public void setCur_water_yield(int cur_water_yield) {
		this.cur_water_yield = cur_water_yield;
	}

	public int getPrev_water_yield() {
		return prev_water_yield;
	}

	public void setPrev_water_yield(int prev_water_yield) {
		this.prev_water_yield = prev_water_yield;
	}

	public int getTotal_water_yield() {
		return total_water_yield;
	}

	public void setTotal_water_yield(int total_water_yield) {
		this.total_water_yield = total_water_yield;
	}

	public GsensorData getGsensor_data() {
		return GsensorData.getInstance();
	}

	public void setGsensor_data(int x, int y, int z) {
		GsensorData mGsensorData = GsensorData.getInstance();
		mGsensorData.setX(x);
		mGsensorData.setY(y);
		mGsensorData.setZ(z);
	}

	public int getCur_time() {
		return cur_time;
	}

	public void setCur_time(int cur_time) {
		this.cur_time = cur_time;
	}

	public int getDrink_valid_flag() {
		return drink_valid_flag;
	}

	public void setDrink_valid_flag(int drink_valid_flag) {
		this.drink_valid_flag = drink_valid_flag;
	}
	public void getString() {
		Log.d("CupStatus", "cur_water_temp==" + cur_water_temp + "   cur_water_yield====" + cur_water_yield + "    prev_water_yield==" + prev_water_yield
				+ "   total_water_yield===" + total_water_yield + "   GsensorData==(" + GsensorData.getInstance().getX() + "," + GsensorData.getInstance().getY() + ","
				+ GsensorData.getInstance().getZ() + ")     cur_time==" + cur_time + "  drink_valid_flag===" + drink_valid_flag );
	}

}
