package com.sen5.ocup.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.sen5.ocup.R;
import com.sen5.ocup.activity.MainActivity;

public class TeaService extends Service implements Callback {
	protected static final String TAG = "TeaService";
	public static final String SERVICETEA_SERVICE = "com.sen5.ocup.service.TeaService";
	/**
	 * 修改泡茶状态同步锁
	 */
	private static byte[] mLock_teaState = new byte[] { (byte) 0xf0, (byte) 0xf5 };

	public static Context context;

	public static int mCountDownTime;// 倒计时总时长
	/**
	 * 当前的泡茶状态
	 */
	public static int mTeaState;
	public final static int TEA_FREE = 0;
	public final static int TEA_WORK = 1;

	private static Handler mHandler;
	private static Thread thread_tea;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand---------");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate---------");
		mHandler = new Handler(TeaService.this);
		// nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// myHandler = new MyHandler(Looper.myLooper(), TeaService.this);
		 context = this;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy---------");
		mTeaState = TEA_FREE;
	}

	public static void startTea(int totalTime) {
		synchronized (mLock_teaState) {
			mCountDownTime = totalTime;
			if (mCountDownTime <= 0) {
				mTeaState = TEA_FREE;
			} else {
				mTeaState = TEA_WORK;
				if (thread_tea != null) {
					thread_tea.interrupt();
				}
				thread_tea = new Thread(doWork);
				thread_tea.start();
			}
		}
	}

	public static void stopTea() {
		synchronized (mLock_teaState) {
			mCountDownTime = 0;
			mTeaState = TEA_FREE;
		}
	}

	private void updateProgressValue(int value) {
		Intent i = new Intent(SERVICETEA_SERVICE);
		Bundle b = new Bundle();
		b.putInt("ProgressValue", value);
		i.putExtras(b);
		sendBroadcast(i);
	}

	private static Runnable doWork = new Runnable() {

		@Override
		public void run() {
			int count = 0;
			while (mTeaState == TEA_WORK && count < mCountDownTime) {
				synchronized (mLock_teaState) {
					count++;
					Message msg = new Message();
					msg.what = UPDATE_UI;
					msg.arg1 = count;
					mHandler.sendMessage(msg);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			if (count>=mCountDownTime) {//茶泡好
					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.icon_29)
							.setContentTitle(context.getString(R.string.tea_done)).setContentText("");
					mBuilder.setTicker(context.getString(R.string.tea_done));// 第一次提示消息的时候显示在通知栏上
					mBuilder.setAutoCancel(true);// 自己维护通知的消失

					// 构建一个Intent
					Intent resultIntent = new Intent(context, MainActivity.class);
					resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					// 设置通知主题的意图
					mBuilder.setContentIntent(resultPendingIntent);
					// 获取通知管理器对象
					NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;
					mNotificationManager.notify(1, mBuilder.build());
			}
			
			mTeaState = TEA_FREE;
		}
	};

	private final static int UPDATE_UI = 1;
	private final static int LOAD_OK = 2;
	private final static int UPDATE_PROGRESS = 3;
	private final static int LOAD_FAILED = 4;

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == UPDATE_UI) {
			updateProgressValue(msg.arg1);
		}
		return false;
	}
}
