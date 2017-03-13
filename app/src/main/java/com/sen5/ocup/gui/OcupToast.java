package com.sen5.ocup.gui;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.sen5.ocup.R;

public class OcupToast extends Toast {

	private static Toast result = null;

	public OcupToast(Context context) {
		super(context);
	}

	public static Toast makeText(Context context, CharSequence text, int duration) {
		if (result != null) {
			result.cancel();
		}
		result = new Toast(context);
		LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflate.inflate(R.layout.dialog_toast, null);
		MTextView tv = (MTextView) v.findViewById(R.id.txt_toast_content);
		tv.setMText(text);
		tv.setTextColor(Color.rgb(255, 255, 255));
		tv.setGravity(Gravity.CENTER);
		result.setView(v);
		// setGravity方法用于设置位置
		result.setGravity(Gravity.CENTER, 0, 0);
		result.setDuration(Toast.LENGTH_SHORT);

		return result;
	}
}
