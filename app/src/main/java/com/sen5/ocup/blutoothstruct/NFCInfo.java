package com.sen5.ocup.blutoothstruct;

import android.util.Log;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :NFC信息结构体
 */
public class NFCInfo {
//uint16 cur_time;//检测到NFC 时的当前时间
//    
//    //以下信息来自NFC
//uint8   teaVarietyCode;//茶的品种代码
//uint8   teaProductionPlaceCode;//茶的产地代码
	
//PRODUCE_TIME    teaProduceTime;//茶的生产时间
//	 uint16  produce_year;//生产时间:年
//	    uint8   produce_month;//生产时间:月
//	    uint8   procude_day;//生产时间:日 
	
//uint8   comment[30];//茶的其他明文备注信息
	/****************************************************************************************************************/
//	//茶生产时间结构体
//	typedef struct produce_time
//	{
//	    Uint8  produce_year[4];//生产时间:年ASCII码0000---9999
//	    uint8   produce_month[2];//生产时间:月ASCII码00---12
//	    uint8   procude_day[2];//生产时间:日ASCII码00---31
//	}PRODUCE_TIME;
//	//NFC 信息结构体
//	typedef struct nfc_info
//	{
//	    uint16 cur_time;//检测到NFC 时的当前时间
//
//	    //以下信息来自NFC
//	uint8   teaVarietyCode[2];//茶的品种代码 ASCII码00---99
//	uint8   teaProductionPlaceCode[2];//茶的产地代码ASCII码00---99
//	PRODUCE_TIME    teaProduceTime;//茶的生产时间
//	uint8   comment[24];//茶的其他明文备注信息
//	}NFC_INFO;

	
	private static final String TAG = "NFCInfo";
	private int  cur_time;// 收到nfc当前系统时间，单位秒
	private int dur;//收到杯子告诉的要泡多长时间，单位是秒
	private String teaVarietyCode;
	private String produce_year;
	private String produce_month;
	private String procude_day;
	private String teaProductionPlaceCode;
	
	/**
	 * 创建单例
	 */
	private static NFCInfo mInstance = null;
	public static NFCInfo getInstance() {
		if (mInstance == null) {
			mInstance = new NFCInfo();
		}
		return mInstance;
	}

	private NFCInfo() {
	}


	public int getDur() {
		return dur;
	}

	public void setDur(int dur) {
		this.dur = dur;
	}

	public int getCur_time() {
		return cur_time;
	}

	public void setCur_time(int cur_time) {
		this.cur_time = cur_time;
	}


	public String getTeaVarietyCode() {
		return teaVarietyCode;
	}

	public void setTeaVarietyCode(String teaVarietyCode) {
		this.teaVarietyCode = teaVarietyCode;
	}

	public String getProduce_year() {
		return produce_year;
	}

	public void setProduce_year(String produce_year) {
		this.produce_year = produce_year;
	}

	public String getProduce_month() {
		return produce_month;
	}

	public void setProduce_month(String produce_month) {
		this.produce_month = produce_month;
	}

	public String getProcude_day() {
		return procude_day;
	}

	public void setProcude_day(String procude_day) {
		this.procude_day = procude_day;
	}

	public String getTeaProductionPlaceCode() {
		return teaProductionPlaceCode;
	}

	public void setTeaProductionPlaceCode(String teaProductionPlaceCode) {
		this.teaProductionPlaceCode = teaProductionPlaceCode;
	}

	public void clearNFCInfo(){
		cur_time = 0;
		teaVarietyCode= "";
		produce_year= "";
		produce_month=  "";
		procude_day=  "";
		teaProductionPlaceCode=  "";
	}
	
public void getString(){
	Log.d(TAG, "cur_time="+cur_time+"  teaVarietyCode=="+teaVarietyCode+"  produce_year=="+produce_year+"  produce_month=="+produce_month+"  procude_day=="+procude_day+" teaProductionPlaceCode== "+teaProductionPlaceCode);
}
}
