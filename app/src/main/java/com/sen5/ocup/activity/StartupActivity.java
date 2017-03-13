package com.sen5.ocup.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.struct.CupInfo;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;
import com.sen5.ocup.yili.CustomVideoView;
import com.sen5.ocup.yili.LoginActivity;

import java.lang.reflect.Field;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 : 开机启动界面
 */
public class StartupActivity extends BaseActivity {
	private static final String TAG = "StartupActivity";

	/**
	 * 动画每帧播放时间
	 */
//	private int mDuration = 200;
	/**
	 * 动画总共的帧数
	 */
//	private int mFrameCount = 13;

	private RelativeLayout mLayout_startup;
//	private ImageView mIv_startGIf;

	private int mWidth;
	private int mHeight;
	private ImageView imageView;
	private CustomVideoView mCustomVideoView;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);

		mLayout_startup = (RelativeLayout) findViewById(R.id.layout_startup);
		 imageView = (ImageView) findViewById(R.id.iv_startup);
		mCustomVideoView = (CustomVideoView) findViewById(R.id.guide_vv);

		if (!Tools.getPreference(this,UtilContact.ISFRIST).equals("yes")){
			Tools.savePreference(this,UtilContact.ISFRIST,"yes");
			//默认打开数据显示
			Tools.savePreference(this,UtilContact.OPENDATA,"true");
			mCustomVideoView.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.nhh));
			mCustomVideoView.start();
			mCustomVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					String cupid = Tools.getPreference(getApplicationContext(), UtilContact.CUP_ID);
					Logger.e(TAG, "onCreate()------cupid==" + cupid);
					if (null != cupid && !cupid.equals("")) {
						startActivityFromStartUp(StartupActivity.this, MainActivity.class);
					}
					else if (Tools.getPreference(StartupActivity.this,UtilContact.Phone_Num).length() != 11){
						startActivityFromStartUp(StartupActivity.this, LoginActivity.class);
					}
					else {
						startActivityFromStartUp(StartupActivity.this, ChooseActivity.class);
					}
				}
			});
		}
		else {
			imageView.setVisibility(View.VISIBLE);
			Animation animation = AnimationUtils.loadAnimation(this,R.anim.activity_startup);
			imageView.startAnimation(animation);
			animation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// 启动搜索蓝牙的activity
					String cupid = Tools.getPreference(getApplicationContext(), UtilContact.CUP_ID);
					Log.d(TAG, "onCreate()------cupid==" + cupid);

					if (null != cupid && !cupid.equals("")) {
						startActivityFromStartUp(StartupActivity.this, MainActivity.class);
					}
					else if (Tools.getPreference(StartupActivity.this,UtilContact.Phone_Num).length() != 11){
						startActivityFromStartUp(StartupActivity.this, LoginActivity.class);
					}
					else {
						startActivityFromStartUp(StartupActivity.this, ChooseActivity.class);
					}
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
		}
		// 获取本地Cupid
		String cupid = Tools.getPreference(getApplicationContext(), UtilContact.CUP_ID);
		Log.d(TAG, "onCreate()------cupid==" + cupid);
		if (null != cupid && !cupid.equals("")) {
			// 本地保存了Cupid
			BluetoothConnectUtils.getInstance().isRunFront = true;
			BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBtAdapter == null) {
				// 设备不支持蓝牙
				OcupToast.makeText(this, getString(R.string.not_surport_bluetooth), Toast.LENGTH_SHORT).show();
				this.finish();
			}
			if (!mBtAdapter.isEnabled()) {
				OcupToast.makeText(StartupActivity.this, getString(R.string.bluetooth_closed), Toast.LENGTH_SHORT).show();
			}

			// 通过Cupid ，从数据库中获取自己的用户信息
			OcupApplication.getInstance().mOwnCup = new DBManager(getApplicationContext()).queryOwnCup(cupid);
			// 通过Cupid查表，查看是否有杯子与他配对
			String to_huanxinID = new DBManager(getApplicationContext()).queryCup_mate(cupid);
			if (null != to_huanxinID) {
				// 数据库中有此杯子的配对信息，通过对方的环信ID 获取对方的信息
				CupInfo cup = new DBManager(getApplicationContext()).queryOtherCup(to_huanxinID);
				if (cup != null) {
					OcupApplication.getInstance().mOtherCup = cup;
				}
			}

			// 判断蓝牙是否已经连接成功
			if (null == BluetoothConnectUtils.getInstance().getSocket()) {
				// socket为空，设置此时蓝牙状态为空闲
				BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_NONE);
				Logger.e(TAG,"bluetooth address is"+OcupApplication.getInstance().mOwnCup.getBlueAdd());
				//创建device
				BluetoothDevice device = BluetoothConnectUtils.getInstance().getRemoteDevice(OcupApplication.getInstance().mOwnCup.getBlueAdd());
				if (device != null) {
					try {
						Logger.e(TAG,"device is not null");
						// socket为空，通过BluetoothDevice创建socket
						BluetoothConnectUtils.getInstance().setSocket(device.createInsecureRfcommSocketToServiceRecord(BluetoothConnectUtils.myUUID));
						if (!BluetoothConnectUtils.getInstance().getSocket().isConnected()) {
							// 蓝牙未连接，开始连接蓝牙
							Logger.e(TAG, "---------------------开始连接蓝牙-");
							BluetoothConnectUtils.getInstance().connect(device);
						} else {
							// 蓝牙已连接
							BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_CONNECTED);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					// 重新扫描
					BluetoothConnectUtils.getInstance().isRunFront = false;
					// 启动搜索蓝牙的activity
					startActivityFromStartUp(StartupActivity.this, BlueTooth3Activity.class);
					return;
				}
			} else {
				// socket不为空，检查是否已连接上蓝牙
				if (!BluetoothConnectUtils.getInstance().getSocket().isConnected()) {
					// 未连接蓝牙，设置此时蓝牙状态为空闲
					BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_NONE);
				
					BluetoothDevice device = BluetoothConnectUtils.getInstance().getRemoteDevice(OcupApplication.getInstance().mOwnCup.getBlueAdd());
					if (device != null) {
						// 开始连接蓝牙
						BluetoothConnectUtils.getInstance().connect(device);
					} else {
						// 重新扫描
						BluetoothConnectUtils.getInstance().isRunFront = false;
						// 启动搜索蓝牙的activity
						startActivityFromStartUp(StartupActivity.this, BlueTooth3Activity.class);
						
						return;
					}
				} else {
					BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_CONNECTED);
					// 已连接上蓝牙，登录sen5服务器
				}
			}

			// 启动MainActivity
//			startActivityFromStartUp(this, MainActivity.class);
		} else {
			// 本地没有保存Cupid，启动开机动画,设置此时蓝牙状态为空闲
			BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_NONE);
		}
	}

	
	private void startActivityFromStartUp(Context context, Class<?> cls){
		Intent intent = new Intent(context, cls);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		StartupActivity.this.finish();
	};
	
	
	@Override
	protected void onStart() {
		super.onStart();
	}

	/**
	 * 获取状态栏高度
	 * 
	 * @return
	 */
	private int getStatusBar() {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			sbar = getResources().getDimensionPixelSize(x);
		} catch (Exception e1) {
			sbar = 75 * mHeight / 1920;
			e1.printStackTrace();
		}
		return sbar;
	}

	/**
	 * 获取设备的宽高
	 * 
	 * @param activity
	 * @return
	 */
	private int[] getScreenWH(Activity activity) {
		int wh[] = new int[2];
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		wh[0] = dm.widthPixels;
		wh[1] = dm.heightPixels;
		return wh;
	}
}
