package com.sen5.ocup.struct;

import android.graphics.Bitmap;

public class Tea {
	
	private String nameCode;
	private String name;
	private String placeCode;
	
	private String teaPlace;
	private String teaTaste;
//	private String teaFunction;
	private String teaAge;
	
//	private String function;
//	private int waterTemprerature;
//	private int waterYield;
//	private int duaration;
	private int teaImgSourceID;
//	private String ulr_bmp;
//	private Bitmap bmp;
	
	private int druraion_90;//水温90对应的泡茶时长
	private int druraion_80;
	private int druraion_70;
	
	private int percent;//泡茶时间修改的百分值 （10---90）  最终time =初始时间*percent/50
	
	public Tea() {
		super();
		// TODO Auto-generated constructor stub
	}
	
/**
 * @param nameCode
 * @param name
 * @param placeCode
 * @param teaPlace
 * @param teaTaste
 * @param teaAge
 * @param teaImgSourceID
 * @param percent
 * @param druraion_90
 * @param druraion_80
 * @param druraion_70
 */
public Tea(String nameCode, String name, String placeCode, String teaPlace, String teaTaste, String teaAge, int teaImgSourceID, int percent,int druraion_90, int druraion_80, int druraion_70) {
	super();
	this.nameCode = nameCode;
	this.name = name;
	this.placeCode = placeCode;
	
	this.teaTaste = teaTaste;
//	this.teaFunction = teaFunction;
	this.teaPlace = teaPlace;
	this.teaAge = teaAge;
	
	this.teaImgSourceID = teaImgSourceID;
	this.percent = percent;
	this.druraion_90 = druraion_90;
	this.druraion_80 = druraion_80;
	this.druraion_70 = druraion_70;
}

public int getPercent() {
	return percent;
}
public void setPercent(int percent) {
	this.percent = percent;
}
public String getNameCode() {
	return nameCode;
}
public void setNameCode(String nameCode) {
	this.nameCode = nameCode;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getPlaceCode() {
	return placeCode;
}
public void setPlaceCode(String placeCode) {
	this.placeCode = placeCode;
}

public String getTeaPlace() {
	return teaPlace;
}
public void setTeaPlace(String teaPlace) {
	this.teaPlace = teaPlace;
}
public String getTeaTaste() {
	return teaTaste;
}
public void setTeaTaste(String teaTaste) {
	this.teaTaste = teaTaste;
}
//public String getTeaFunction() {
//	return teaFunction;
//}
//public void setTeaFunction(String teaFunction) {
//	this.teaFunction = teaFunction;
//}
public String getTeaAge() {
	return teaAge;
}
public void setTeaAge(String teaAge) {
	this.teaAge = teaAge;
}
public int getTeaImgSourceID() {
	return teaImgSourceID;
}
public void setTeaImgSourceID(int teaImgSourceID) {
	this.teaImgSourceID = teaImgSourceID;
}
public int getDruraion_90() {
	return druraion_90;
}
public void setDruraion_90(int druraion_90) {
	this.druraion_90 = druraion_90;
}
public int getDruraion_80() {
	return druraion_80;
}
public void setDruraion_80(int druraion_80) {
	this.druraion_80 = druraion_80;
}
public int getDruraion_70() {
	return druraion_70;
}
public void setDruraion_70(int druraion_70) {
	this.druraion_70 = druraion_70;
}

	
}
