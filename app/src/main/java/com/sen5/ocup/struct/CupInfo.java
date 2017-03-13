package com.sen5.ocup.struct;


import android.graphics.Bitmap;
import android.util.Log;

import com.sen5.ocup.activity.OcupApplication;

public class CupInfo {
	private static final String TAG = "CupInfo";
	
	private String cupID ;
	private String blueAdd;
	private String name;
	private String huanxin_userid;
	private String huanxin_pwd;
	private String email;
	private String avatorPath;
	private String mood;
	private int intakegoal;
	private int intake;
	private Bitmap bmp_head;
	public String getCupID() {
		return cupID;
	}
	public void setCupID(String cupID) {
		this.cupID = cupID;
	}
	public String getBlueAdd() {
		return blueAdd;
	}
	public void setBlueAdd(String blueAdd) {
		this.blueAdd = blueAdd;
	}
	public int getIntake() {
		return intake;
	}
	public void setIntake(int intake) {
		this.intake = intake;
	}
	public String getName() {
		return name;
	}
	public void setName(String name, int tag) {
		Log.e(TAG, "---------------11nicheng = " +OcupApplication.getInstance().mOwnCup.getName() + "::::" + tag);
		this.name = name;
	}
	public String getHuanxin_userid() {
		return huanxin_userid;
	}
	public void setHuanxin_userid(String huanxin_userid) {
		Log.e(TAG, "-----------------------------------------huanxin_userid = " + huanxin_userid);
		this.huanxin_userid = huanxin_userid;
	}
	public String getHuanxin_pwd() {
		return huanxin_pwd;
	}
	public void setHuanxin_pwd(String huanxin_pwd) {
		this.huanxin_pwd = huanxin_pwd;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getAvatorPath() {
		return avatorPath;
	}
	public void setAvatorPath(String avatorPath) {
		this.avatorPath = avatorPath;
	}
	public String getMood() {
		return mood;
	}
	public void setMood(String mood) {
		this.mood = mood;
	}
	public int getIntakegoal() {
		return intakegoal;
	}
	public void setIntakegoal(int intakegoal) {
		this.intakegoal = intakegoal;
	}
	public Bitmap getBmp_head() {
		return bmp_head;
	}
	public void setBmp_head(Bitmap bmp_head) {
		this.bmp_head = bmp_head;
	}
}
