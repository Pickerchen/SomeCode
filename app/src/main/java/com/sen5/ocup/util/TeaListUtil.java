package com.sen5.ocup.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;

import com.sen5.ocup.R;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.blutoothstruct.NFCInfo;
import com.sen5.ocup.struct.Tea;
/**
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :茶列表处理工具类
 */
public class TeaListUtil {
	
	private static final String TAG = "TeaListUtil";
	/**
	 * 创建单例
	 */
	private static TeaListUtil mInstance = null;
	public ArrayList<Tea> teaList = new ArrayList<Tea>();
	public HashMap<String, Integer> teacodeMap = new HashMap<String, Integer>();//key---->teacode     value---->tea index
	private int percent[] = new int[5];
	
	public static TeaListUtil getInstance() {
		if (mInstance == null) {
			mInstance = new TeaListUtil();
			mInstance.addTea(OcupApplication.getInstance().getApplicationContext());
		}
		return mInstance;
	}
	
	public static void setTeaListUtilNull(){
		if(null != mInstance){
			mInstance = null;
		}
	}
	
	private TeaListUtil(){
		
	}

	public  ArrayList<Tea> getTeaList(Context contetxt) {
		if (teaList.size() <= 0) {
			addTea(contetxt);
		}
		return teaList;
	}
	
	/**
	 * 获取当前正在泡的茶在列表中的索引
	 * 
	 * @return
	 */
	public int getCurTeaIndex(Context contetxt) {
		Log.d(TAG, "getCurTeaIndex--------");
		if (null != TeaListUtil.getInstance().teaList) {
			if (teaList.size() <= 0) {
				addTea(contetxt);
			}
			Log.d(TAG, "getCurTeaIndex--------(null != TeaListUtil.getInstance().teaList   teaList.size()=="+ teaList.size());
			if (teacodeMap.containsKey(NFCInfo.getInstance().getTeaVarietyCode()) && teaList.size() > teacodeMap.get(NFCInfo.getInstance().getTeaVarietyCode())) {
				return teacodeMap.get(NFCInfo.getInstance().getTeaVarietyCode());
			}
		}
		return -1;
	}
	
	/**
	 * 获取当前正在修改的茶在列表中的索引
	 * 
	 * @return
	 */
	public int getCurChangeTeaIndex(Context context,String teacode) {
		Log.d(TAG, "getCurChangeTeaIndex--------teacode=="+teacode);
		if (null != TeaListUtil.getInstance().teaList) {
			if (teaList.size() <= 0) {
				addTea(context);
			}
			Log.d(TAG, "getCurChangeTeaIndex--------(null != TeaListUtil.getInstance().teaList   teaList.size()=="+ teaList.size());
			if (teacodeMap.containsKey(teacode) && teaList.size() > teacodeMap.get(teacode)) {
				return teacodeMap.get(teacode);
			}
		}
		return -1;
	}
/**
 * 添加茶
 * @param contetxt
 * this.nameCode = nameCode;
	this.name = name;
	this.placeCode = placeCode;
	
	this.teaTaste = teaTaste;
	this.teaFunction = teaFunction;
	this.teaPlace = teaPlace;
	this.teaAge = teaAge;
	
	this.teaImgSourceID = teaImgSourceID;
	this.druraion_90 = druraion_90;
	this.druraion_80 = druraion_80;
	this.druraion_70 = druraion_70;
 */
	private void addTea(Context contetxt) {
		teacodeMap.clear();
		Tea tea_nan = new Tea("02", contetxt.getString(R.string.teanickname_nan), "02", contetxt.getString(R.string.teaplace_nan), contetxt.getString(R.string.taste_nan), "0.5", R.drawable.tea_nan_bg,50,90,105,165);
		teaList.add(tea_nan);
		teacodeMap.put("02", 0);
		Tea tea_xi = new Tea("03", contetxt.getString(R.string.teanickname_xi), "03", contetxt.getString(R.string.teaplace_xi), contetxt.getString(R.string.taste_xi),  "0.5",R.drawable.tea_xi_bg,50,180,210,270);
		teaList.add(tea_xi);
		teacodeMap.put("03", 1);
		Tea tea_yi = new Tea("04", contetxt.getString(R.string.teanickname_yi), "04", contetxt.getString(R.string.teaplace_yi), contetxt.getString(R.string.taste_yi),  "0.5", R.drawable.tea_yi_bg,50,210,210,270);
		teaList.add(tea_yi);
		teacodeMap.put("04", 2);
		Tea tea_hao = new Tea("01", contetxt.getString(R.string.teanickname_hao), "01",contetxt.getString(R.string.teaplace_hao), contetxt.getString(R.string.taste_hao),  "0.5",R.drawable.tea_hao_bg,50,180,210,360);
		teaList.add(tea_hao);
		teacodeMap.put("01", 3);
		Tea tea_bu = new Tea("05", contetxt.getString(R.string.teanickname_bu), "05", contetxt.getString(R.string.teaplace_bu), contetxt.getString(R.string.taste_bu),  "0.5", R.drawable.tea_bu_bg,50,120,150,210);
		teaList.add(tea_bu);
		teacodeMap.put("05", 4);
		
		for (int i = 0; i < percent.length; i++) {
			String percent = Tools.getPreference(OcupApplication.getInstance(), ""+Integer.parseInt(teaList.get(i).getNameCode()));
			Log.e(TAG, "-------------------percent = " + percent + ":::"+Integer.parseInt(teaList.get(i).getNameCode()));
			if (percent != null && !percent.equals("")) {
				teaList.get(i).setPercent(Integer.parseInt(percent));
			}else{
//				teaList.get(i).setPercent(0);
			}
		}
	}

	/**
	 * 获取当前时间的分钟数
	 * 
	 * @return
	 */
	public int getCurMinute() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
		Date d = new Date(System.currentTimeMillis());
		String date = sdf.format(d);
		Log.d(TAG, "getCurMinute   date==" + date);
		String[] strs = date.split("-");
		if (strs.length > 0) {
			int m = Integer.parseInt(strs[strs.length - 1]);
			int h = Integer.parseInt(strs[strs.length - 2]);
			return (h * 60 + m);
		} else {
			return 0;
		}
	}
	
	public int getTeaDuaration(Context context, int teaIndex, int watertem){
		if (teaList.size()<=0) {
			addTea(context);
		}
		if (teaList.size()<=teaIndex) {
			return -1;
		}
		int dua = 0;
		if (watertem>=90) {
			dua =(- teaList.get(teaIndex).getDruraion_90()*(watertem-90)/90+teaList.get(teaIndex).getDruraion_90())* teaList.get(teaIndex).getPercent()/50;
		}else if (watertem>=80) {
			dua =(-(teaList.get(teaIndex).getDruraion_80()-teaList.get(teaIndex).getDruraion_90())*(watertem-80)/10+teaList.get(teaIndex).getDruraion_80())* teaList.get(teaIndex).getPercent()/50;
		}else if (watertem>=70) {
			dua =(- (teaList.get(teaIndex).getDruraion_70()-teaList.get(teaIndex).getDruraion_80())*(watertem-70)/10+teaList.get(teaIndex).getDruraion_70())* teaList.get(teaIndex).getPercent()/50;
		}else if (watertem>=60) {
			dua =( teaList.get(teaIndex).getDruraion_70()*(70-watertem)/70+teaList.get(teaIndex).getDruraion_70())* teaList.get(teaIndex).getPercent()/50;
		}else{
//			dua = teaList.get(teaIndex).getDruraion_70()/70*watertem;
			dua = -1;
		}
		return dua;
	}
	
}
