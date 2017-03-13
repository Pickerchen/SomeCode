package com.sen5.ocup.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.activity.MainActivity;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.blutoothstruct.CupStatus;
import com.sen5.ocup.callback.BluetoothCallback.IGetDrinkDataCallback;
import com.sen5.ocup.callback.RequestCallback;
import com.sen5.ocup.gui.Circle_ProgressBar;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.yili.OkHttpRequest;
import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class OmeFragment extends Fragment implements Callback, IGetDrinkDataCallback,RequestCallback.IGetTipsCallBack{

	private  Circle_ProgressBar pb;
	private FrameLayout mFrameLayout;

	private static final String TAG = "OmeFragment";
	private static final int CONNECTBLUETOOTH_OK = 3;
	private static final int CONNECTBLUETOOTH_NO = 4;
	private static final int CONNECTBLUETOOTH_ING = 9;
	private static final int CUPSTATUS_OK = 5;
	private static final int CUPSTATUS_NO = 6;
	private static final int DRINKDATA_OK = 7;
	private static final int DRINKDATA_NO = 8;

	private static final int getTipsSuccess = 10;
	private static final int goals = 1800;
	public static final long DURATOIN_GETCUPSTATUS = 5000;
	private View mView;
	private Activity mActivity;
	private Handler mHandler;
	private DBManager mDBManager;

	private TextView mTV_curtemprature;
	private TextView mtv_tips;
	private int progress;

	private ImageView data_button;
	private ImageView iv_bg_temperature;

	private ProgressBar pb_bluetooth_connecting;// 表示蓝牙正在连接
	private ImageView mIv_bluetooth_state;// 表示蓝牙连接状态

	public boolean isRun_getcupstatus; // 标识是否要定时去杯子状态
	public int lastTemp = 0;//上次群聊发送出去的温度值

	private final static String APPID = "1101513923";
	public static QQAuth mQQAuth;
	private Tencent mTencent;
	private QQShare mQQShare = null;
	private int shareType;
	private String imgPath;

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "onReceive---------------------action===" + action);
			if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
				// mHandler.sendEmptyMessage(CONNECTBLUETOOTH_OK);
			} else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
				mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO);
			} else if (action
					.equals(BluetoothConnectUtils.ACTION_BLUETOOTHSTATE)) {
				int bluestate = intent.getIntExtra(
						BluetoothConnectUtils.KEY_BLUETOOTHSTATE, -1);
				Logger.e(TAG, "bluetooth connectstate bluestate==" + bluestate);
				if (bluestate == BluetoothConnectUtils.CONNECT_OK) {
					mHandler.sendEmptyMessage(CONNECTBLUETOOTH_OK);
				} else if (bluestate == BluetoothConnectUtils.CONNECT_NO) {
					mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO);
				} else if (bluestate == BluetoothConnectUtils.CONNECT_ING) {
					mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO);
				}
			}
			//收到了cupstatus信息，该广播由mainActivity发出
			else if (action.equals(MainActivity.receiverCupStatusInfo)){
				Logger.e(TAG,"Ome receiverCupstatusInfo");
				if (null != mTV_curtemprature) {
					int temperature = CupStatus.getInstance().getCur_water_temp();
					if (temperature >= 40 && temperature < 60) {//大于人体温度时显示黄色背景
						//粉色
						iv_bg_temperature.setImageResource(R.drawable.temp3);
					}
					else if (temperature >= 60) {
						//红色
						iv_bg_temperature.setImageResource(R.drawable.temp2);
					}
					else {
						//蓝色
						iv_bg_temperature.setImageResource(R.drawable.temp1);
					}
					mTV_curtemprature.setText(""
							+ temperature + getString(R.string.tempratureunit));
				}
			}
		}
	};


	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		Log.d(TAG, "setUserVisibleHint()-----------isVisibleToUser=="
				+ isVisibleToUser);
//		onDestroy();
		// 判断fragment 是否可见
		if (isVisibleToUser) {
			if (!isRun_getcupstatus) {
				isRun_getcupstatus = true;
			}
			if (null != mTV_curtemprature) {
				mTV_curtemprature.setText(""
						+ CupStatus.getInstance().getCur_water_temp()
						+ getString(R.string.tempratureunit));
				if (CupStatus.getInstance().getTotal_water_yield() != 0) {
					progress = (int) (((CupStatus.getInstance()
							.getTotal_water_yield() * 100) / goals));
					Log.d(TAG,
							"setUserVisibleHint()----progress=="
									+ progress
									+ "  CupStatus.getInstance().getTotal_water_yield()=="
									+ CupStatus.getInstance()
									.getTotal_water_yield());
				} else {
					updateUI_progress();
				}
			}
			setBluetoothState();
		} else {
			isRun_getcupstatus = false;
		}
		super.setUserVisibleHint(isVisibleToUser);
	}

	/**
	 * 根据蓝牙连接状态，修改状态图标
	 */
	private void setBluetoothState() {
		if (null != mIv_bluetooth_state) {
			Log.d(TAG,
					"setBluetoothState--------BluetoothConnectUtils.getInstance().bluetoothState =="
							+ BluetoothConnectUtils.getInstance()
							.getBluetoothState());
			if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED
					&& BluetoothAdapter.getDefaultAdapter().isEnabled()) {
				pb_bluetooth_connecting.setVisibility(View.GONE);
				mIv_bluetooth_state.setVisibility(View.VISIBLE);
				mIv_bluetooth_state
						.setImageResource(R.drawable.btn_bluetooth_active);
			} else if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTING) {
				pb_bluetooth_connecting.setVisibility(View.VISIBLE);
				mIv_bluetooth_state.setVisibility(View.GONE);
				mIv_bluetooth_state
						.setImageResource(R.drawable.btn_bluetooth_inactive);
			} else if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
				BluetoothConnectUtils.getInstance().setBluetoothState(
						BluetoothConnectUtils.BLUETOOTH_NONE);
				pb_bluetooth_connecting.setVisibility(View.GONE);
				mIv_bluetooth_state.setVisibility(View.VISIBLE);
				mIv_bluetooth_state
						.setImageResource(R.drawable.btn_bluetooth_inactive);
			} else {
				pb_bluetooth_connecting.setVisibility(View.GONE);
				mIv_bluetooth_state.setVisibility(View.VISIBLE);
				mIv_bluetooth_state
						.setImageResource(R.drawable.btn_bluetooth_inactive);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView---------");
		if (mView == null) {
			setUserVisibleHint(true);
			Log.d(TAG, "onCreateView----mView == null-----");
			mActivity = getActivity();
			mView = inflater.inflate(R.layout.fragment_me, container, false);
			mDBManager = new DBManager(mActivity);
			initialComponent();
			initData();
		} else {
			Log.d(TAG, "onCreateView----mView ！！！！！！！！！！= null-----");
			// mView判断是否已经被加过parent，如果没删除，会发生mView已有parent的错误
			ViewGroup parent = (ViewGroup) mView.getParent();
			if (parent != null) {
				parent.removeView(mView);
			}
		}
		return mView;
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart---------");
		setBluetoothState();
	}

	@Override
	public void onResume() {
		Log.d(TAG,
				"onResume---------this.getUserVisibleHint()=="
						+ this.getUserVisibleHint());
		super.onResume();
		IntentFilter filter = new IntentFilter(
				BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothConnectUtils.ACTION_BLUETOOTHSTATE);
		filter.addAction(MainActivity.receiverCupStatusInfo);
		mActivity.registerReceiver(receiver, filter);
		// 页面是否可见
		if (this.getUserVisibleHint() && !isRun_getcupstatus) {
			isRun_getcupstatus = true;
		}
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause---------");
		super.onPause();
		mActivity.unregisterReceiver(receiver);
		isRun_getcupstatus = false;
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop---------");
		super.onStop();

	}

	@Override
	public void onDestroy() {
		// waveView.destroyDrawingCache();
		// if(null != waveView.bmp_person && !waveView.bmp_person.isRecycled()){
		// waveView.bmp_person.recycle();
		// waveView.bmp_person = null;
		// }
		super.onDestroy();
	}

	/**
	 * 初始化数据
	 */
	public void initData() {
		if (CupStatus.getInstance().getTotal_water_yield() != 0) {
			progress = (int) (((CupStatus.getInstance().getTotal_water_yield() * 100) / goals));
			if (CupStatus.getInstance().getCur_water_temp() != 0) {
				if (null != mTV_curtemprature) {
					int temperature = CupStatus.getInstance().getCur_water_temp();
					if (temperature >= 40 && temperature < 60) {//大于人体温度时显示黄色背景
						//粉色
						iv_bg_temperature.setImageResource(R.drawable.temp3);
					}
					else if (temperature >= 60) {
						//红色
						iv_bg_temperature.setImageResource(R.drawable.temp2);
					}
					else {
						//蓝色
						iv_bg_temperature.setImageResource(R.drawable.temp1);
					}
					mTV_curtemprature.setText(""
							+ temperature + getString(R.string.tempratureunit));
				}
			}
//			waveView.setProgress((progress + 0.0f) / 100);
//			mTV_progress.setText("" + progress + getString(R.string.percent));
		} else {
			updateUI_progress();
		}
	}

	/**
	 * 初始化控件
	 */
	private void initialComponent() {

		mtv_tips = (TextView) mView.findViewById(R.id.tv_nuanni);
		mFrameLayout = (FrameLayout) mView.findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(mFrameLayout,mActivity);

		mHandler = new Handler(this);
		OkHttpRequest.getTips(OmeFragment.this,getString(R.string.language));
//		waveView = (OmeView) mView.findViewById(R.id.wave_view);
		data_button = (ImageView) mView.findViewById(R.id.data_bt);
		mIv_bluetooth_state = (ImageView) mView
				.findViewById(R.id.iv_bluetooth_state);
		pb_bluetooth_connecting = (ProgressBar) mView
				.findViewById(R.id.pb_bluetooth_connecting);
		mTV_curtemprature = (TextView) mView
				.findViewById(R.id.tv_curtemprature);
		data_button.setOnClickListener(mOnClickListener);
		mIv_bluetooth_state.setOnClickListener(mOnClickListener);

		iv_bg_temperature = (ImageView) mView.findViewById(R.id.iv_temp);

	}

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "mOnClickListener   v.getId()===" + v.getId());
			switch (v.getId()) {
				case R.id.data_bt:
					break;
				case R.id.iv_bluetooth_state:// 断开or连接蓝牙
					Log.d(TAG, "onclick             iv_bluetooth_state");
					// if (BluetoothConnectUtils.getInstance().getBluetoothState()
					// == BluetoothConnectUtils.BLUETOOTH_CONNECTED &&
					// BluetoothAdapter.getDefaultAdapter().isEnabled()) {
					// Log.d(TAG,
					// "onclick             iv_bluetooth_state      socket.isConnected()");
					// return;
					// }
					// pb_bluetooth_connecting.setVisibility(View.VISIBLE);
					// mIv_bluetooth_state.setVisibility(View.GONE);
					break;
				default:
					break;
			}
		}
	};
	/**
	 * 分享到QQ
	 */
	private OnClickListener mClick_QQshareListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			initQQ();
			shareType = QQShare.SHARE_TO_QQ_TYPE_IMAGE;// 纯图片分享
			final Bundle params = new Bundle();
			int mExtarFlag = 0x00;
			params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imgPath);
			params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "Ocup");
			params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, shareType);
			params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, mExtarFlag);
			doShareToQQ(params);
		}
	};

	/**
	 * 初始化QQAPi工具
	 */
	private void initQQ() {
		mQQAuth = QQAuth.createInstance(APPID, OcupApplication.getInstance());
		mTencent = Tencent.createInstance(APPID, mActivity);
		mQQShare = new QQShare(mActivity, OmeFragment.mQQAuth.getQQToken());
	}
	@Override
	public void getTipSuccess(String content) {
		Message message = new Message();
		message.what = getTipsSuccess;
		message.obj = content;
		mHandler.sendMessage(message);
	}

	@Override
	public void getTipFail() {

	}

	@Override
	public boolean handleMessage(Message msg) {
		if (null != OmeFragment.this.getView()) {
			switch (msg.what) {
				case CONNECTBLUETOOTH_OK:
					if (null != mIv_bluetooth_state) {
						pb_bluetooth_connecting.setVisibility(View.GONE);
						mIv_bluetooth_state.setVisibility(View.VISIBLE);
						mIv_bluetooth_state
								.setImageResource(R.drawable.btn_bluetooth_active);
					}
					break;
				case CONNECTBLUETOOTH_NO:
					if (null != mIv_bluetooth_state) {
						pb_bluetooth_connecting.setVisibility(View.GONE);
						mIv_bluetooth_state.setVisibility(View.VISIBLE);
						mIv_bluetooth_state
								.setImageResource(R.drawable.btn_bluetooth_inactive);
					}
					break;
				case CONNECTBLUETOOTH_ING:
					pb_bluetooth_connecting.setVisibility(View.VISIBLE);
					mIv_bluetooth_state.setVisibility(View.GONE);
					break;
				case getTipsSuccess:
						mtv_tips.setText(msg.obj.toString());
					break;
				default:
					break;
			}
		}
		return false;
	}

	/**
	 * 刷新喝水进度
	 */
	private void updateUI_progress() {
		Log.d(TAG, "updateUI_progress----");
		long y = 0;
		progress = (int) ((y * 100) / goals);
	}

	@Override
	public void getDrinkData_OK() {
		mHandler.sendEmptyMessage(DRINKDATA_OK);

	}

	@Override
	public void getDrinkData_NO() {

	}

	/**
	 * 用异步方式启动分享
	 *
	 * @param params
	 */
	private void doShareToQQ(final Bundle params) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				mQQShare.shareToQQ(mActivity, params, new IUiListener() {
					@Override
					public void onCancel() {
						if (shareType != QQShare.SHARE_TO_QQ_TYPE_IMAGE) {
							toastMessage(mActivity, "onCancel: ", "d");
						}
					}
					@Override
					public void onComplete(Object response) {
						toastMessage(mActivity,
								"onComplete: " + response.toString(), "d");
					}
					@Override
					public void onError(UiError e) {
						toastMessage(mActivity, "onError: " + e.errorMessage,
								"e");
					}
				});
			}
		}).start();
	}

	/**
	 * 用Toast显示消息
	 *
	 * @param activity
	 * @param message
	 * @param logLevel
	 *            填d, w, e分别代表debug, warn, error; 默认是debug
	 */
	private static final void toastMessage(final Activity activity,
										   final String message, String logLevel) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				OcupToast.makeText(activity, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

}
