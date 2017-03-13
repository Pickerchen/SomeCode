package com.sen5.ocup.blutoothstruct;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :蓝牙通信协议中定义的led颜色结构体
 */
public class CupLEDColor {
	/**
	 * 创建CupLEDColor的单例
	 */
	private static CupLEDColor mInstance = null;
	
	public enum led_color {//led_color
		LED_RED, LED_GREEN, LED_BLUE, LED_YELLOW, LED_CYAN, LED_PURPLE, LED_WHITE, LED_PINK;
	}
	
	private int t_high= 0;
	private int t_norm= 3;
	private int t_low= 2;
	
	public static CupLEDColor getInstance() {
		if (mInstance == null) {
			mInstance = new CupLEDColor();
		}
		return mInstance;
	}

	private CupLEDColor() {
	}
	
	public int getT_high() {
		return t_high;
	}
	public void setT_high(int t_high) {
		this.t_high = t_high;
	}
	public int getT_norm() {
		return t_norm;
	}
	public void setT_norm(int t_norm) {
		this.t_norm = t_norm;
	}
	public int getT_low() {
		return t_low;
	}
	public void setT_low(int t_low) {
		this.t_low = t_low;
	}
	
}
