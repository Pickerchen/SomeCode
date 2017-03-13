package com.sen5.ocup.struct;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * 功能描述：弹窗内部子类项（绘制标题和图标）
 */
public class SettingItem {
	// 定义图片对象
	public Drawable mDrawable;
	// 定义文本对象
	public String mTitle;

	public SettingItem(Drawable drawable, String title) {
		this.mDrawable = drawable;
		this.mTitle = title;
	}

	public SettingItem(Context context, int titleId, int drawableId) {
		this.mTitle = (String) context.getResources().getText(titleId);
		this.mDrawable = context.getResources().getDrawable(drawableId);
	}

	public SettingItem(Context context, String title, int drawableId) {
		this.mTitle = title;
		this.mDrawable = context.getResources().getDrawable(drawableId);
	}
}
