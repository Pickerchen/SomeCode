package com.sen5.ocup.alarm;

/**
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 闹钟的结构体
 */
public class Time_show {

	private String time;//"13:44"
	private int flag;//1--->开    0------>关
	
	public Time_show(){
		super();
	}
	
	public Time_show(String time, int flag){
		this.time = time;
		this.flag =flag;
	}
	public String getTime() {
		return time;
	}


	public void setTime(String time) {
		this.time = time;
	}


	public int isFlag() {
		return flag;
	}


	public void setFlag(int s) {
		this.flag = s;
	}

}
