package com.sen5.ocup.gui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.sen5.ocup.R;
import com.sen5.ocup.gui.SeekArc.OnSeekArcChangeListener;

public class SeekArcDailog implements OnSeekArcChangeListener,
		OnDismissListener {

	public static final int MENU_WALLPAPER_ID = 0;
	public static final int MENU_MANAGER_APPS_ID = 1;
	public static final int MENU_REBOOT_ID = 2;
	private Context mContext;
	private Dialog dialog;

	private SeekArc mSeekArc;
	private OnSeekChangeListener mListener = null;
	private int progress = 0;

	public SeekArcDailog(Context context) {
		this.mContext = context;
		init();
	}

	private void init() {
		dialog = new Dialog(mContext, R.style.dialogStyle);
		dialog.setContentView(R.layout.dialog_seek);
		mSeekArc = (SeekArc) dialog.findViewById(R.id.bangThreshold);
		mSeekArc.setOnSeekArcChangeListener(this);
		dialog.setOnDismissListener(this);

		Window window = dialog.getWindow();
		window.setGravity(Gravity.CENTER);
		LayoutParams params = window.getAttributes();
		window.setAttributes(params);
	}

	/**
	 * 显示对话框
	 */
	public void show() {
		if (dialog != null) {
			dialog.show();
		}
	}

	/**
	 * 关闭对话框
	 */
	public void dismiss() {
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	/**
	 * 更新进度条
	 * 
	 * @param progress
	 */
	public void setProgress(int progress) {
		mSeekArc.setProgress(progress);
	}

	@Override
	public void onProgressChanged(SeekArc seekArc, int progress,
			boolean fromUser) {
		this.progress = progress;
		
	}

	@Override
	public void onStartTrackingTouch(SeekArc seekArc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekArc seekArc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mListener != null) {
			mListener.onProgressChanged(progress);
		}
	}

	/**
	 * 获取当前的滑动的值,值是0~100
	 * 
	 * @param mlListener
	 */
	public void setOnSeekArcChangListener(OnSeekChangeListener mlListener) {
		this.mListener = mlListener;
	}

	public interface OnSeekChangeListener {
		public void onProgressChanged(int progress);
	}

}
