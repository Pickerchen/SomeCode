package com.sen5.ocup.struct;

import android.graphics.Bitmap;

public class FriendData {
	private String huanxin_id;
	private String name;
	private int progress;
	private int goals;
	private String mood;
	private int count_offline_msg;
	

	public FriendData(String huanxin_id, String name, int progress, int goals,String mood) {
		super();
		this.huanxin_id = huanxin_id;
		this.name = name;
		this.progress = progress;
		this.goals = goals;
		this.mood = mood;
	}

	public FriendData() {
		super();
	}

	public String getMood() {
		return mood;
	}

	public void setMood(String mood) {
		this.mood = mood;
	}

	public int getCount_offline_msg() {
		return count_offline_msg;
	}

	public void setCount_offline_msg(int count_offline_msg) {
		this.count_offline_msg = count_offline_msg;
	}


	public String getHuanxin_id() {
		return huanxin_id;
	}

	public void setHuanxin_id(String huanxin_id) {
		this.huanxin_id = huanxin_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getGoals() {
		return goals;
	}

	public void setGoals(int goals) {
		this.goals = goals;
	}
	
	
}
