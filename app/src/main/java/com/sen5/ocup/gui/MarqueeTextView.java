/*
 * Copyright (C) 2013 Sen5 LABS Technology
 *
 * Author: Kay.Zheng
 */

package com.sen5.ocup.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 类名 : MarqueeTextView 功能 : 重写原生文本控件TextView，内容大于view时滚动显示
 */
public class MarqueeTextView extends TextView {

	public MarqueeTextView(Context context) {
		super(context);
		setTypeface(context);
	}

	public MarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setTypeface(context);
	}

	public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setTypeface(context);
	}

	public void setTypeface(Context context) {
		// 使用指定的细体
		// Typeface type=
		// Typeface.createFromAsset(context.getAssets(),"font/segoemcl.ttf");
		// setTypeface(type);
	}

	/**
	 * 强制获取焦点实现跑马灯效果
	 */
	@Override
	public boolean isFocused() {
		// 设置焦点一直为true
		return true;
	}
}
