package com.sen5.ocup.blutoothstruct;

/**
 * 记录一天饮水总量
 * @author caoxia
 *
 */
public class DrinkYield {
	// 水量
	private int water_yield;
	//日期  yyyy-MM-dd 毫秒
	private long drink_date;
	
	public int getWater_yield() {
		return water_yield;
	}
	public void setWater_yield(int water_yield) {
		this.water_yield = water_yield;
	}
	public long getDrink_date() {
		return drink_date;
	}
	public void setDrink_date(long drink_date) {
		this.drink_date = drink_date;
	}
}
