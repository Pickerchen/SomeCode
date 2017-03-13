package com.sen5.ocup.struct;

import android.graphics.Bitmap;

public class SharePlatformApp {
	private int iconId;
	private String name;
	
	public SharePlatformApp(int iconId, String name) {
		super();
		this.iconId = iconId;
		this.name = name;
	}
	public int getIconId() {
		return iconId;
	}
	public void setIconId(int iconId) {
		this.iconId = iconId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
