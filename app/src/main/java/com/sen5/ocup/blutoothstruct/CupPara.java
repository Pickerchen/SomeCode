package com.sen5.ocup.blutoothstruct;

import android.util.Log;

//uint16       start_time;    //每天开始提醒时间，以分钟为单位
//uint16       end_time;      //每天结束提醒时间，以分钟为单位
//uint16       advise_water_yield;   //每天建议喝水总量
//uint16       para_verion;    //参数版本号
//uint8        remind_times;    //每天提醒次数
//LED_COLOR_DATA    LED_data; //LED灯光颜色设定

// 0---heater_sw
//1--hand_warm_sw
//2--led_sw
//3--shake_sw
//4--remind_sw
//5--learn_sw
//6---nfc_sw

//uint8        heater_SW;    //加热开关
//uint8        head_warmer_SWr;    //暖手宝开关 0----关   1-----开

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :蓝牙通信协议中定义的杯子参数结构体
 */
public class CupPara {

	/**
	 * 创建单例
	 */
	private static CupPara mInstance = null;
	public boolean isGotCupPara = false;// 是否已经获取过杯子参数

	private int start_time= 540;
	private int end_time = 1140;
	private int advise_water_yield = 1500;
	private int para_verion = 2;
	private int remind_times = 8;

	private int heater_SW = 0;
	private int hand_warmer_SW = 0;
	private int led_sw= 1;
	private int shake_sw= 1;
	private int remind_sw= 1;
	private int learn_sw= 1;
	private int nfc_sw= 1;

	public int default_start_time = 540;
	public int default_end_time = 1140;
	public int default_advise_water_yield = 1500;
	public int default_para_verion = 2;
	public int default_remind_times = 8;

	public int default_t_high = 0;
	public int default_t_norm = 3;
	public int default_t_low = 2;

	public int default_heater_SW = 0;
	public int default_hand_warmer_SW = 0;
	public int default_led_sw = 1;
	public int default_shake_sw = 1;
	public int default_remind_sw = 1;
	public int default_learn_sw = 1;
	public int default_nfc_sw = 1;
	
	public static CupPara getInstance() {
		if (mInstance == null) {
			mInstance = new CupPara();
		}
		return mInstance;
	}

	private CupPara() {
	}

	/**
	 * 将杯子参数恢复至出厂设置
	 */
//	public void recovery2default() {
//		start_time = default_start_time;
//		end_time = default_end_time;
//		advise_water_yield = default_advise_water_yield;
//		para_verion = default_para_verion;
//		remind_times = default_remind_times;
//		// private CupLEDColor LED_data;
//		CupLEDColor.getInstance().setT_high(default_t_high);
//		CupLEDColor.getInstance().setT_norm(default_t_norm);
//		CupLEDColor.getInstance().setT_low(default_t_low);
//		heater_SW = default_heater_SW;
//		hand_warmer_SW = default_hand_warmer_SW;
//		led_sw = default_led_sw;
//		shake_sw = default_shake_sw;
//		remind_sw = default_remind_sw;
//		learn_sw = default_learn_sw;
//		nfc_sw = default_nfc_sw;
//	}

	public boolean isGotCupPara() {
		return isGotCupPara;
	}

	public void setGotCupPara(boolean isGotCupPara) {
		this.isGotCupPara = isGotCupPara;
	}

	public int getStart_time() {
		return start_time;
	}

	public void setStart_time(int start_time) {
		this.start_time = start_time;
	}

	public int getEnd_time() {
		return end_time;
	}

	public void setEnd_time(int end_time) {
		this.end_time = end_time;
	}

	public int getAdvise_water_yield() {
		return advise_water_yield;
	}

	public void setAdvise_water_yield(int advise_water_yield) {
		this.advise_water_yield = advise_water_yield;
	}

	public int getPara_verion() {
		return para_verion;
	}

	public void setPara_verion(int para_verion) {
		this.para_verion = para_verion;
	}

	public int getRemind_times() {
		return remind_times;
	}

	public void setRemind_times(int remind_times) {
		this.remind_times = remind_times;
	}

	public CupLEDColor getLED_data() {
		return CupLEDColor.getInstance();
	}

	public void setLED_data(int t_high, int t_norm, int t_low) {
		CupLEDColor mCupLEDColor = CupLEDColor.getInstance();
		mCupLEDColor.setT_high(t_high);
		mCupLEDColor.setT_norm(t_norm);
		mCupLEDColor.setT_low(t_low);
	}

	public int getHeater_SW() {
		return heater_SW;
	}

	public void setHeater_SW(int heater_SW) {
		this.heater_SW = heater_SW;
	}

	public int getHand_warmer_SW() {
		return hand_warmer_SW;
	}

	public void setHand_warmer_SW(int hand_warmer_SW) {
		this.hand_warmer_SW = hand_warmer_SW;
	}

	public int getLed_sw() {
		return led_sw;
	}

	public void setLed_sw(int led_sw) {
		this.led_sw = led_sw;
	}

	public int getShake_sw() {
		return shake_sw;
	}

	public void setShake_sw(int shake_sw) {
		this.shake_sw = shake_sw;
	}

	public int getRemind_sw() {
		return remind_sw;
	}

	public void setRemind_sw(int remind_sw) {
		this.remind_sw = remind_sw;
	}

	public int getLearn_sw() {
		return learn_sw;
	}

	public void setLearn_sw(int learn_sw) {
		this.learn_sw = learn_sw;
	}

	public int getNfc_sw() {
		return nfc_sw;
	}

	public void setNfc_sw(int nfc_sw) {
		this.nfc_sw = nfc_sw;
	}

	public void getString() {
		Log.d("CupPara", "---------------start_time==" + start_time + "   end_time==" + end_time + "  advise_water_yield==" + advise_water_yield + "  para_verion==" + para_verion
				+ "  remind_times==" + remind_times + "   LED_data==" + CupLEDColor.getInstance().getT_high() + "," + CupLEDColor.getInstance().getT_norm() + ","
				+ CupLEDColor.getInstance().getT_low() + "  heater_SW==" + heater_SW + "  hand_warmer_SW==" + hand_warmer_SW + "  led_sw==" + led_sw + "   shake_sw== " + shake_sw
				+ "  remind_sw==" + remind_sw + "  learn_sw==" + learn_sw + "  nfc_sw==" + nfc_sw);
	}
}
