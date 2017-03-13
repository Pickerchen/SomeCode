/*
 * Copyright (C) 2013 Sen5 LABS Technology
 *
 * Author: Kay.Zheng
 */
package com.sen5.ocup.gui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 *
 *自定义textview 设置字体
 */
public class SegoTextView extends TextView {

	public SegoTextView(Context context) {
		super(context);
		setTypeface(context);
	}

	public SegoTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTypeface(context);
	}

	public SegoTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setTypeface(context);
	}

	private void setTypeface(Context context) {
//		Typeface type= Typeface.createFromAsset(context.getAssets(),"font_zh.ttf");
//		setTypeface(type);
	}
}
