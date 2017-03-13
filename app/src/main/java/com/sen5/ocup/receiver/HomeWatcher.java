package com.sen5.ocup.receiver;

import com.sen5.ocup.util.BluetoothConnectUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : Home键监听封装
 */
public class HomeWatcher {

	static final String TAG = "HomeWatcher";
	private Context mContext;
	private IntentFilter mFilter;
	private OnHomePressedListener mListener;
	private InnerRecevier mRecevier;

	// 回调接口
	public interface OnHomePressedListener {
		public void onHomePressed();

		public void onHomeLongPressed();
	}

	public HomeWatcher(Context context) {
		mContext = context;
		mRecevier = new InnerRecevier();
		mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	}

	/**
	 * 
	 * 设置监听
	 * 
	 * @param listener
	 */
	public void setOnHomePressedListener(OnHomePressedListener listener) {
		mListener = listener;
	}

	/**
	 * 开始监听，注册广播
	 */
	public void startWatch() {
		if (mRecevier != null) {
			mContext.registerReceiver(mRecevier, mFilter);
		}
	}

	/**
	 * 停止监听，注销广播
	 */
	public void stopWatch() {
		if (mRecevier != null) {
			mContext.unregisterReceiver(mRecevier);
		}
	}

	/**
	 * 广播接收者
	 */
	class InnerRecevier extends BroadcastReceiver {
		final String SYSTEM_DIALOG_REASON_KEY = "reason";
		final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
		final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
		final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
				Log.d(TAG, "action:" + action + ",reason:" + reason);

				if (reason != null) {
					
					// 触发home键，当监听不为空时，调用监听的方法
					if (mListener != null) {
						Log.d(TAG, "action:" + action + ",reason:" + reason + "  mListener != null");
						if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
							// 短按home键
							mListener.onHomePressed();
						} else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
							// 长按home键
							mListener.onHomeLongPressed();
						}
					}

					// home键退出，蓝牙断开后不自动重连
					if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
						Log.d(TAG, "action:" + action + ",reason:" + reason + "  home键退出，蓝牙断开后不自动重连");
						BluetoothConnectUtils.getInstance().isRunFront = false;
					}
				}
			}
		}
	}
}