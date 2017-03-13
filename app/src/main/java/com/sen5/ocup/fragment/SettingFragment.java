package com.sen5.ocup.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.activity.AboutOcupActivity;
import com.sen5.ocup.activity.MainActivity;
import com.sen5.ocup.activity.NicknameActivity;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.activity.Ocup_setting_activity;
import com.sen5.ocup.activity.TwocodeActivity;
import com.sen5.ocup.activity.WaterProjectActivity;
import com.sen5.ocup.activity.WaterTemLEDActivity;
import com.sen5.ocup.blutoothstruct.CupPara;
import com.sen5.ocup.blutoothstruct.CupStatus;
import com.sen5.ocup.callback.BluetoothCallback.IGetCupParaCallback;
import com.sen5.ocup.callback.BluetoothCallback.ISetCupParaCallback;
import com.sen5.ocup.callback.CustomInterface.IDialog;
import com.sen5.ocup.callback.RequestCallback;
import com.sen5.ocup.callback.RequestCallback.GetAPKVersionCallback;
import com.sen5.ocup.callback.RequestCallback.GetCupInfoCallback;
import com.sen5.ocup.callback.RequestCallback.UploadUserImageCallback;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.gui.SegoTextView;
import com.sen5.ocup.service.BluetoothService;
import com.sen5.ocup.util.BlueToothRequest;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.HuanxinUtil;
import com.sen5.ocup.util.TipsBitmapLoader;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;
import com.sen5.ocup.yili.AvatarOOS;
import com.sen5.ocup.yili.OOSClientUtils;
import com.sen5.ocup.yili.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SettingFragment extends Fragment implements OnClickListener,
		Callback, IDialog, UploadUserImageCallback, GetCupInfoCallback,
		ISetCupParaCallback, IGetCupParaCallback, GetAPKVersionCallback,
		RequestCallback.IGetInfoCallBack,RequestCallback.IUploadAvatarCallBack,RequestCallback.IGetTipsCallBack{

	private static final String TAG = "SettingFragment";
	private static final String UPDATE_SERVERAPK = "Latest_ocup.apk";
	public static String renameSuccess = "nickname";
	public static String updateAvatarSuccess = "avatarSuccess";

	private FragmentActivity mActivity;
	private HttpRequest mHttpRequest;
	private BlueToothRequest mBluetoothRequest;
	private Handler mHandler;
	private View settingView;

	private CupPara mCupPara;

	private ProgressBar pb_bluetooth_connecting;// 表示蓝牙正在连接
	private ImageView iv_bluetooth_state;// 表示蓝牙连接状态
	private String newname;// 设置新昵称，不一定上传成功
	private Bitmap newphoto;// 设置新头像，不一定上传成功
//	private LinearLayout mLayout_QQ;
	private SegoTextView tv_nicheng, tv_watertemLED, auto_study, ocup_settting;
	private ImageView iv_userimage,iv_load_handWram,iv_load_vibration;
	private RelativeLayout rl_headimager, rl_nickname, rl_two_code,
			rl_waterled, waterproject, relativelayout_setting,
			about_ocup;
	private CustomDialog mDailog_setHeadimage;

//	private SwitchView mSwitch_remind2drink;
	private ch.ielse.view.SwitchView mSwitch_handwarm1,switch_opendata,mSwitch_vibration;

	private boolean isFirstVisible;

//	private TextView txt_qq_auth = null;
	private CustomDialog tencent_dialog = null;
	private CustomDialog tencent_dialog_login = null;
	private CustomDialog tencent_dialog_another_login = null;

	private FrameLayout mFrameLayout;

	//阿里云的使用
	private boolean  hasChanged;
	private OOSClientUtils mOOSClientUtils;
	private AvatarOOS avatarOOS;
	private String filePath;
	private final int getUserInfoSeccess = 27;
	private final int getAvatarComplete = 28;
	private final int getAvatarFail = 29;

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
					.equals(BluetoothConnectUtils.getInstance().ACTION_BLUETOOTHSTATE)) {
				int bluestate = intent.getIntExtra(
						BluetoothConnectUtils.getInstance().KEY_BLUETOOTHSTATE,
						-1);
				Log.d(TAG, "bluetooth connectstate bluestate==" + bluestate);
				if (bluestate == BluetoothConnectUtils.getInstance().CONNECT_OK) {
					mHandler.sendEmptyMessage(CONNECTBLUETOOTH_OK);
				} else if (bluestate == BluetoothConnectUtils.getInstance().CONNECT_NO) {
					mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO);
				} else if (bluestate == BluetoothConnectUtils.getInstance().CONNECT_ING) {
					mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO);
				}
			} else if (action.equals(BluetoothService.ACTOIN_CUPPARA)) {
				if (null != SettingFragment.this.getView()) {
					setCupPara();
				}
			}
		}
	};

	/**
	 * 取消viewPage的预加载
	 */
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		Logger.d(TAG, "setUserVisibleHint()-----------isVisibleToUser=="
				+ isVisibleToUser);
		// 判断fragment 是否可见
		if (isVisibleToUser) {
			if (mCupPara != null){
				mCupPara.setGotCupPara(false);
			}

			if (isFirstVisible && null != mHttpRequest) {
//				mHttpRequest.getUserInfo(mActivity, SettingFragment.this);
			}
			// 设置蓝牙状态
			setBluetoothState();
			setUserInfo();
			setCupPara();

			if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
				mBluetoothRequest.sendMsg2getCupInfo(SettingFragment.this);
			}
		}
		super.setUserVisibleHint(isVisibleToUser);
	}

	@Override
	public void onResume() {
		Log.e(TAG, "--------------------------onResume");
		setUserInfo();
		super.onResume();
	}

	/**
	 * 设置用户信息
	 */
	private void setUserInfo() {
		// 设置昵称
		if (null != tv_nicheng) {
				DBManager manager = new DBManager(mActivity);
				UserInfo userInfo = manager.queryYiLiCup();
			if (userInfo != null) {
					tv_nicheng.setText(userInfo.getNickname());
			}
			else {
				tv_nicheng.setText(UtilContact.DEFAULT_OCUP_NAME);
			}
		}
	}

	/**
	 * 根据蓝牙连接状态，修改状态图标
	 */
	private void setBluetoothState() {
		if (null != iv_bluetooth_state) {
			Log.d(TAG,
					"setBluetoothState--------BluetoothConnectUtils.getInstance().bluetoothState =="
							+ BluetoothConnectUtils.getInstance()
									.getBluetoothState());
			if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED
					&& BluetoothAdapter.getDefaultAdapter().isEnabled()) {
				pb_bluetooth_connecting.setVisibility(View.GONE);
				iv_bluetooth_state.setVisibility(View.VISIBLE);
				iv_bluetooth_state
						.setImageResource(R.drawable.btn_bluetooth_active);
			} else if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTING) {
				pb_bluetooth_connecting.setVisibility(View.VISIBLE);
				iv_bluetooth_state.setVisibility(View.GONE);
				iv_bluetooth_state
						.setImageResource(R.drawable.btn_bluetooth_inactive);
			} else if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
				BluetoothConnectUtils.getInstance().setBluetoothState(
						BluetoothConnectUtils.BLUETOOTH_NONE);
				pb_bluetooth_connecting.setVisibility(View.GONE);
				iv_bluetooth_state.setVisibility(View.VISIBLE);
				iv_bluetooth_state
						.setImageResource(R.drawable.btn_bluetooth_inactive);
			} else {
				pb_bluetooth_connecting.setVisibility(View.GONE);
				iv_bluetooth_state.setVisibility(View.VISIBLE);
				iv_bluetooth_state
						.setImageResource(R.drawable.btn_bluetooth_inactive);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView---------");
		// 设置头像
		if (settingView == null) {
			mActivity = getActivity();
			settingView = inflater.inflate(R.layout.fragment_setting,
					container, false);
			initView();
		} else {
			// mView判断是否已经被加过parent，如果没删除，会发生mView已有parent的错误
			ViewGroup parent = (ViewGroup) settingView.getParent();
			if (parent != null) {
				parent.removeView(settingView);
			}
		}
		return settingView;
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart---------");
		super.onStart();
		IntentFilter filter = new IntentFilter(
				BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothConnectUtils.ACTION_BLUETOOTHSTATE);
		filter.addAction(BluetoothService.ACTOIN_CUPPARA);
		mActivity.registerReceiver(receiver, filter);
		setBluetoothState();
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop---------");
		super.onStop();
		mActivity.unregisterReceiver(receiver);
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		mFrameLayout = (FrameLayout) settingView.findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(mFrameLayout,mActivity);

		mHttpRequest = HttpRequest.getInstance();
		isFirstVisible = true;
		mBluetoothRequest = BlueToothRequest.getInstance();
		mBluetoothRequest.sendMsg2getCupInfo(SettingFragment.this);
		mCupPara = CupPara.getInstance();
		mHandler = new Handler(this);

		auto_study = (SegoTextView) settingView.findViewById(R.id.auto_study);
		iv_bluetooth_state = (ImageView) settingView
				.findViewById(R.id.iv_bluetooth_state);
		pb_bluetooth_connecting = (ProgressBar) settingView
				.findViewById(R.id.pb_bluetooth_connecting);
		iv_bluetooth_state.setOnClickListener(this);

		rl_headimager = (RelativeLayout) settingView
				.findViewById(R.id.relativelayout_headimage);
		iv_userimage = (ImageView) settingView.findViewById(R.id.user_image);
		//加载头像，一次性操作，放在onCreateView中
		loaderAvatar();
		rl_headimager.setOnClickListener(this);

		tv_nicheng = (SegoTextView) settingView.findViewById(R.id.nicheng);
		rl_nickname = (RelativeLayout) settingView
				.findViewById(R.id.relativelayout_nickname);
		rl_nickname.setOnClickListener(this);

		rl_two_code = (RelativeLayout) settingView
				.findViewById(R.id.relativelayout_two_code);
		rl_two_code.setOnClickListener(this);

		waterproject = (RelativeLayout) settingView
				.findViewById(R.id.Water_project);
		waterproject.setOnClickListener(this);

		//暖手控制开关相关
		iv_load_handWram = (ImageView) settingView.findViewById(R.id.iv_load_push_hand_warme1);
		((AnimationDrawable)iv_load_handWram.getBackground()).start();
		mSwitch_handwarm1 = (ch.ielse.view.SwitchView) settingView.findViewById(R.id.switch_hand_warme1);
		mSwitch_handwarm1.setOnStateChangedListener(new ch.ielse.view.SwitchView.OnStateChangedListener() {
			@Override
			public void toggleToOn(ch.ielse.view.SwitchView view) {
				Logger.e("toggletoon 执行");
				if (judgeBluetoothState()){
					iv_load_handWram.setVisibility(View.VISIBLE);
						mCupPara.setHand_warmer_SW(1);
						BlueToothRequest.getInstance().sendMsg2setCupPara(SettingFragment.this,BlueToothRequest.type_handwarm_1);
					}
					else {
						mSwitch_handwarm1.toggleSwitch(false);
					}
			}

			@Override
			public void toggleToOff(ch.ielse.view.SwitchView view) {
				Logger.e("toggleTooff 执行");
				if (judgeBluetoothState()){
					iv_load_handWram.setVisibility(View.VISIBLE);
					Logger.e("judgeBluetoothState "+judgeBluetoothState());
						mCupPara.setHand_warmer_SW(0);
						BlueToothRequest.getInstance().sendMsg2setCupPara(SettingFragment.this,BlueToothRequest.type_handwarm_0);
					}
					else {
						mSwitch_handwarm1.toggleSwitch(true);
					}
			}
		});


		//震动控制开关相关
		iv_load_vibration = (ImageView) settingView.findViewById(R.id.iv_load_push_vibration);
		((AnimationDrawable)iv_load_vibration.getBackground()).start();
		mSwitch_vibration = (ch.ielse.view.SwitchView) settingView.findViewById(R.id.switch_vibration1);
		mSwitch_vibration.setOnStateChangedListener(new ch.ielse.view.SwitchView.OnStateChangedListener() {
			@Override
			public void toggleToOn(ch.ielse.view.SwitchView view) {
				Logger.e("toggletoon 执行");
				if (judgeBluetoothState()){
					iv_load_vibration.setVisibility(View.VISIBLE);
					Logger.e("judgeBluetoothState =="+judgeBluetoothState());
					mCupPara.setShake_sw(1);
					BlueToothRequest.getInstance().sendMsg2setCupPara(SettingFragment.this,BlueToothRequest.type_vibration_1);
				}
				else {
					mSwitch_vibration.toggleSwitch(false);
				}
			}

			@Override
			public void toggleToOff(ch.ielse.view.SwitchView view) {
				Logger.e("toggleTooff 执行");
				if (judgeBluetoothState()){
					iv_load_vibration.setVisibility(View.VISIBLE);
					Logger.e("judgeBluetoothState "+judgeBluetoothState());
					mCupPara.setShake_sw(0);
					BlueToothRequest.getInstance().sendMsg2setCupPara(SettingFragment.this,BlueToothRequest.type_vibration_0);
				}
				else {
					mSwitch_vibration.toggleSwitch(true);
				}
			}
		});

		switch_opendata = (ch.ielse.view.SwitchView) settingView.findViewById(R.id.switch_opendata);
		if ("true".equals(Tools.getPreference(mActivity,UtilContact.OPENDATA))){
			switch_opendata.toggleSwitch(true);
		}
		else {
			switch_opendata.toggleSwitch(false);
		}
		switch_opendata.setOnStateChangedListener(new ch.ielse.view.SwitchView.OnStateChangedListener() {
			@Override
			public void toggleToOn(ch.ielse.view.SwitchView view) {
				switch_opendata.toggleSwitch(true);
				Tools.savePreference(mActivity,UtilContact.OPENDATA,"true");
				if ("true".equals(Tools.getPreference(mActivity,UtilContact.isAlived))){
					String content = "#1#1#" + CupStatus.getInstance().getCur_water_temp();
					HuanxinUtil.getInstance().sendGroupMsg(mActivity,content);
				}
			}
			@Override
			public void toggleToOff(ch.ielse.view.SwitchView view) {
				switch_opendata.toggleSwitch(false);
				Tools.savePreference(mActivity,UtilContact.OPENDATA,"false");
				if ("true".equals(Tools.getPreference(mActivity,UtilContact.isAlived))){
					String content = "#1#1#0";
					HuanxinUtil.getInstance().sendGroupMsg(mActivity,content);
				}
			}
		});

		tv_watertemLED = (SegoTextView) settingView.findViewById(R.id.watertem);
		rl_waterled = (RelativeLayout) settingView
				.findViewById(R.id.relativelayout_watertem);
		rl_waterled.setOnClickListener(this);

		// 进入Ocup设置界面
		ocup_settting = (SegoTextView) settingView
				.findViewById(R.id.ocup_setting);
		relativelayout_setting = (RelativeLayout) settingView.findViewById(R.id.relativelayout_setting);
		relativelayout_setting.setOnClickListener(this);

		about_ocup = (RelativeLayout) settingView.findViewById(R.id.about_ocup);
		about_ocup.setOnClickListener(this);

//		mLayout_QQ.setOnClickListener(this);
//		txt_qq_auth = (TextView) settingView.findViewById(R.id.text_qq_auth);

		mDailog_setHeadimage = new CustomDialog(mActivity, this,
				R.style.select_dialog, CustomDialog.SET_HEADIMAGE_DIALOG, null);

		tencent_dialog = new CustomDialog(mActivity, this,
				R.style.custom_dialog, CustomDialog.TENCENT_DIALOG, null);
		tencent_dialog_login = new CustomDialog(mActivity, this,
				R.style.custom_dialog, CustomDialog.TENCENT_DIALOG_LOGIN, null);
		tencent_dialog_another_login = new CustomDialog(mActivity, this,
				R.style.custom_dialog,
				CustomDialog.TENCENT_DIALOG_ANOTHER_LOGIN, null);
	}

	public boolean judgeBluetoothState(){
		if (BluetoothConnectUtils.getInstance().getBluetoothState() != BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
			Tools.showToast(mActivity,getString(R.string.unconnect_2cup));
			return false;
		}
		if (BlueToothRequest.getInstance().getRequesting()) {
			Tools.showToast(mActivity,getString(R.string.requesting));
			return false;
		}
		//是否已经读取杯子的状态信息，如果还没有读取完信息则提示用户正在同步
		if (!CupPara.getInstance().isGotCupPara()) {
			Tools.showToast(mActivity,getString(R.string.syncup_failed));
			return false;
		}
		return true;
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == CustomDialog.NONE)
			return;
		// 拍照
		if (requestCode == CustomDialog.PHOTOHRAPH) {
			// 设置文件保存路径这里放在跟目录下
			String pth = Environment.getExternalStorageDirectory()
					+ "/ownerOcup/"
					+ OcupApplication.getInstance().mOwnCup.getCupID()
					+ "userimage.jpg";
			File picture = new File(pth);
			startPhotoZoom(Uri.fromFile(picture));
		}
		if (data == null){
			return;
		}


		// 读取相册缩放图片
		if (requestCode == CustomDialog.PHOTOZOOM) {
			Uri data2 = data.getData();
			startPhotoZoom(data.getData());
		}
		// 处理结果
		if (requestCode == CustomDialog.PHOTORESOULT) {
			HttpRequest.getInstance().getAvatorToken(mActivity,SettingFragment.this);
			//部分小米手机系统无返回
			Bundle extras = data.getExtras();
			Logger.e(TAG,"requestCode == CustomDialog.PHOTORESOULT == 收到处理结果 == "+extras);
				filePath =  Tools.getSDPath() + Tools.OCUP_DIR + Tools.AVATAR_FILE_NAME;
		}
		// 重命名
		if (requestCode == Tools.RENAME_REQUEST_CODE) {
			Logger.e("收到重命名result"+data.getStringExtra("newName"));
			newname = data.getStringExtra("newName");
			tv_nicheng.setText(newname);

			Intent intent = new Intent(renameSuccess);
			intent.putExtra("nickname",newname);
			mActivity.sendBroadcast(intent);
			Logger.e("广播发送出去");
			// OwnerCupInfo.getInstance().setName(newname);
			// mHttpRequest.updateUserInfo(mActivity, SettingFragment.this,
			// newname, OwnerCupInfo.getInstance().getMood());
		}
		// 设置水杯返回
		if (requestCode == Tools.OCUPSETTING_REQUEST_CODE) {
			if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
				mBluetoothRequest.sendMsg2getCupInfo(SettingFragment.this);
			}
		}
		// 设置喝水计划是否为自动学习
		if (requestCode == Tools.PROJECT_REQUEST_CODE) {
			if (auto_study != null) {
				if (mCupPara.getLearn_sw() == 0) {
					auto_study.setText(getString(R.string.custom_watertem_led));
				} else {
					auto_study.setText(getString(R.string.auto_study_project));
				}
			}
		}
	}

	/**
	 * 处理图片
	 * 
	 * @param uri
	 */
	private void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高h
		intent.putExtra("outputX", 280);
		intent.putExtra("outputY", 280);
		// intent.putExtra("return-data", true);

		intent.putExtra("return-data", false);
		intent.putExtra("noFaceDetection", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, getAvatarTempUri());
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		startActivityForResult(intent, CustomDialog.PHOTORESOULT);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_bluetooth_state:
			break;
		case R.id.relativelayout_headimage:
			if (MainActivity.netState != 3){
				mDailog_setHeadimage.show();
			}
			else {
				//断网提示
				Toast.makeText(mActivity,getString(R.string.check_network_avatar),Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.relativelayout_two_code:
			mActivity.startActivity(new Intent(mActivity, TwocodeActivity.class));
			break;
		case R.id.relativelayout_nickname:
			Intent intent = new Intent(mActivity, NicknameActivity.class);
			intent.putExtra("oldname", tv_nicheng.getText().toString());
			startActivityForResult(intent, Tools.RENAME_REQUEST_CODE);
			break;
		case R.id.Water_project:
				Intent intent_project = new Intent(mActivity,
						WaterProjectActivity.class);
				startActivityForResult(intent_project,
						Tools.PROJECT_REQUEST_CODE);
			break;
		case R.id.relativelayout_watertem:
			Intent intent_waterled = new Intent(mActivity,
					WaterTemLEDActivity.class);
			startActivityForResult(intent_waterled, Tools.WATERLED_REQUEST_CODE);
			break;
		case R.id.relativelayout_setting:
			Intent intent_setting = new Intent(mActivity,
					Ocup_setting_activity.class);
			startActivityForResult(intent_setting,
					Tools.OCUPSETTING_REQUEST_CODE);
			break;
		case R.id.about_ocup:
			Intent intent_about_ocup = new Intent(mActivity,
					AboutOcupActivity.class);
			startActivity(intent_about_ocup);
			break;
		}
	}


	private final int CONNECTBLUETOOTH_OK = 0;
	private final int CONNECTBLUETOOTH_NO = 1;
	private final int CONNECTBLUETOOTH_ING = 25;
	private final int FROM_CAMERA = 2;
	private final int FROM_PHOTO = 3;
	private final int UPDATECUPINFO_OK = 4;
	private final int UPDATECUPINFO_NO = 5;
	private final int UPLOADUSERIMAGE_OK = 6;
	private final int UPLOADUSERIMAGE_NO = 7;
	private final int GETCUPINFO_OK = 9;
	private final int GETCUPINFO_NOTLOGIN = 10;
	private final int GETCUPINFO_NO = 11;
	@SuppressWarnings("unused")
	private final int SET_NICKNAME = 12;

	private final int GETCUPPARA_OK = 13;
	private final int GETCUPPARA_NO = 14;
	private final int setCupPara_ok = 22;
	private final int setCupPara_no = 23;

	@SuppressWarnings("unused")
	private final int EXIT_OK = 15;
	private final int GET_APKVERSION_OK = 19;
	private final int GET_APKVERSION_NO = 20;
	private final int UPDATE_APK = 21;

	@Override
	public boolean handleMessage(Message msg) {
		if (null != SettingFragment.this.getView()) {
			switch (msg.what) {
			case CONNECTBLUETOOTH_OK:
				if (null != iv_bluetooth_state) {
					pb_bluetooth_connecting.setVisibility(View.GONE);
					iv_bluetooth_state.setVisibility(View.VISIBLE);
					iv_bluetooth_state
							.setImageResource(R.drawable.btn_bluetooth_active);
				}
				break;
			case CONNECTBLUETOOTH_NO:
				if (null != iv_bluetooth_state) {
					pb_bluetooth_connecting.setVisibility(View.GONE);
					iv_bluetooth_state.setVisibility(View.VISIBLE);
					iv_bluetooth_state
							.setImageResource(R.drawable.btn_bluetooth_inactive);
				}
				break;
			case CONNECTBLUETOOTH_ING:
				pb_bluetooth_connecting.setVisibility(View.VISIBLE);
				iv_bluetooth_state.setVisibility(View.GONE);
				break;
			// 从相机选择头像
			case FROM_CAMERA:
				String path = Environment.getExternalStorageDirectory()
						+ "/ownerOcup/";

				File f = new File(path);
				if (!f.exists()) {
					f.mkdirs();
				}
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(
						MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(new File(path
								+ OcupApplication.getInstance().mOwnCup
										.getCupID() + "userimage.jpg")));
				startActivityForResult(intent, CustomDialog.PHOTOHRAPH);
				break;
			// 从相册选择头像
			case FROM_PHOTO:
				Intent intent_photo = new Intent(Intent.ACTION_PICK, null);
				intent_photo
						.setDataAndType(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								"image/*");
				startActivityForResult(intent_photo, CustomDialog.PHOTOZOOM);
				break;
			case GETCUPINFO_OK:
				isFirstVisible = false;
				getOwnBitmap();
				String nickname = OcupApplication.getInstance().mOwnCup
						.getName();
				if (null == nickname) {
					Log.d(TAG,
							"handmassge---------GETCUPINFO_OK-----null ==nickname");
				} else {
//					tv_nicheng.setText(OcupApplication.getInstance().mOwnCup
//							.getName());
				}

				break;
			case GETCUPINFO_NOTLOGIN:
				break;
			case GETCUPINFO_NO:
				break;
			case GETCUPPARA_NO:
				mCupPara.setGotCupPara(false);
				break;
			case GETCUPPARA_OK:
				Log.d(TAG,
						"handmsg-----------GETCUPPARA_OK   mCupPara.getHand_warmer_SW()=="
								+ mCupPara.getHand_warmer_SW()
								+ "  mCupPara.getHeater_SW()=="
								+ mCupPara.getHeater_SW());
				mCupPara.setGotCupPara(true);
				setCupPara();
				break;
			// 更改昵称成功
			case UPDATECUPINFO_OK:
				tv_nicheng.setText(newname);
				OcupApplication.getInstance().mOwnCup = new DBManager(
						OcupApplication.getInstance())
						.queryOwnCup(OcupApplication.getInstance().mOwnCup
								.getCupID());
				OcupApplication.getInstance().mOwnCup.setName(newname, 2);
				new DBManager(OcupApplication.getInstance())
						.updateOwnCup(OcupApplication.getInstance().mOwnCup);
				OcupToast.makeText(mActivity,
						getString(R.string.modify_success), Toast.LENGTH_SHORT).show();
				break;
			case UPDATECUPINFO_NO:
				OcupToast.makeText(mActivity,
						getString(R.string.modify_failed), Toast.LENGTH_SHORT).show();
				break;
			// 更头像称成功
			case UPLOADUSERIMAGE_OK:
				newphoto = Tools.getRoundedBitmap(newphoto,100);
				iv_userimage.setImageBitmap(newphoto);
				OcupToast.makeText(mActivity,
						getString(R.string.modify_success), Toast.LENGTH_SHORT).show();
				break;
			case UPLOADUSERIMAGE_NO:
				OcupToast.makeText(mActivity,
						getString(R.string.modify_failed), Toast.LENGTH_SHORT).show();
				break;
			case getUserInfoSeccess:
				//加载头像
					loaderAvatar();
				//发送广播,通知哄你界面更改UI
				Intent intent1 = new Intent(updateAvatarSuccess);
				mActivity.sendBroadcast(intent1);
					break;
				case getAvatarComplete:
					Toast.makeText(mActivity,getString(R.string.modify_success),Toast.LENGTH_LONG).show();
					break;
				case getAvatarFail:
					Toast.makeText(mActivity,getString(R.string.modify_failed),Toast.LENGTH_LONG).show();
					break;
				case setCupPara_ok:
					if (msg.arg1 == BlueToothRequest.type_handwarm_1){
						iv_load_handWram.setVisibility(View.GONE);
						mSwitch_handwarm1.toggleSwitch(true);
					}
					else if (msg.arg1 == BlueToothRequest.type_handwarm_0){
						iv_load_handWram.setVisibility(View.GONE);
						mSwitch_handwarm1.toggleSwitch(false);
					}
					else if (msg.arg1 == BlueToothRequest.type_vibration_1){
						iv_load_vibration.setVisibility(View.GONE);
						mSwitch_vibration.toggleSwitch(true);
					}
					else if (msg.arg1 == BlueToothRequest.type_vibration_0){
						iv_load_vibration.setVisibility(View.GONE);
						mSwitch_vibration.toggleSwitch(false);
					}
					break;
				case setCupPara_no:
					if (msg.arg1 == BlueToothRequest.type_handwarm_1){
						iv_load_handWram.setVisibility(View.GONE);
						mSwitch_handwarm1.toggleSwitch(false);
					}
					else if (msg.arg1 == BlueToothRequest.type_handwarm_0){
						iv_load_handWram.setVisibility(View.GONE);
						mSwitch_handwarm1.toggleSwitch(true);
					}
					else if (msg.arg1 == BlueToothRequest.type_vibration_1){
						iv_load_vibration.setVisibility(View.GONE);
						mSwitch_vibration.toggleSwitch(false);
					}
					else if (msg.arg1 == BlueToothRequest.type_vibration_0){
						iv_load_vibration.setVisibility(View.GONE);
						mSwitch_vibration.toggleSwitch(true);
					}
					break;
			default:
				break;
			}
		}
		return false;
	}

	/**
	 * 根据杯子参数设置界面
	 */
	private void setCupPara() {
		// 设置暖手宝开关显示
		if(mSwitch_handwarm1 != null){
			if (mCupPara.getHand_warmer_SW() == 0){
				mSwitch_handwarm1.setOpened(false);
			}
			else {
				mSwitch_handwarm1.setOpened(true);
			}
		}
		if (mSwitch_vibration != null){
			if (mCupPara.getShake_sw() == 0){
				mSwitch_vibration.setOpened(false);
			}
			else {
				mSwitch_vibration.setOpened(true);
			}
		}

		// 设置喝水计划是否为自动学习
		if (auto_study != null) {
			if (mCupPara.getLearn_sw() == 0) {
				auto_study.setText(getString(R.string.custom_watertem_led));
			} else {
				auto_study.setText(getString(R.string.auto_study_project));
			}
		}
	}

	/**
	 * 选择头像从相机or相册
	 */
	@Override
	public void ok(int type) {
		if (type == CustomDialog.PHOTOHRAPH) {
			mHandler.sendEmptyMessage(FROM_CAMERA);
		} else if (type == CustomDialog.PHOTOZOOM) {
			mHandler.sendEmptyMessage(FROM_PHOTO);
		} else if (type == CustomDialog.APK_UPDATE_DIALOG) {
			mHandler.sendEmptyMessage(UPDATE_APK);
		}
	}

	// 取得存储头像文件的临时目录
	private Uri getAvatarTempUri() {
		try {
			File file = getAvatarTempFile();
			if (file != null) {
				return Uri.fromFile(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// 取得头像的临时位图
	private Bitmap getAvatarTempBitmap() {
		String filePath = Tools.getSDPath() + Tools.OCUP_DIR
				+ Tools.AVATAR_FILE_NAME;
		try {
			FileInputStream stream = new FileInputStream(filePath);
			Bitmap bitmap = BitmapFactory.decodeStream(stream);
			return bitmap;
		} catch (Exception e) {
			Log.e(TAG, "========================e = " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	// 取得存储头像文件的临时文件
	protected File getAvatarTempFile() {
		String SDPath = Tools.getSDPath();
		File dirFile = new File(SDPath + Tools.OCUP_DIR);
		if (!dirFile.exists()) {
			if (!dirFile.mkdirs()) {
				Log.d(TAG, "failed to make dir /sdcard" + Tools.OCUP_DIR);
			}
		}
		String filePath = dirFile.toString() + Tools.AVATAR_FILE_NAME;
		File file = new File(filePath);
		try {
			file.createNewFile();
			return file;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void ok(int type, Object obj) {
	}

	@Override
	public void cancel(int type) {

	}

	@Override
	public void uploadUserImage_OK() {
		Log.d(TAG, "uploadUserImage_OK-----------------------------------");
		mHandler.sendEmptyMessage(UPLOADUSERIMAGE_OK);
	}

	@Override
	public void uploadUserImage_NO() {
		Log.d(TAG, "uploadUserImage_NO-----------------------------------");
		mHandler.sendEmptyMessage(UPLOADUSERIMAGE_NO);
	}

	@Override
	public void GetCupInfo_OK(String cupid) {
		Log.d(TAG, "GetCupInfo_OK-----------------------------------");
		mHandler.sendEmptyMessage(GETCUPINFO_OK);
	}

	@Override
	public void GetCupInfo_notLogin() {
		Log.d(TAG, "GetCupInfo_notLogin-----------------------------------");
		mHandler.sendEmptyMessage(GETCUPINFO_NOTLOGIN);
	}

	@Override
	public void GetCupInfo_NO() {
		Log.d(TAG, "GetCupInfo_NO-----------------------------------");
		if (null != mHandler) {
			mHandler.sendEmptyMessage(GETCUPINFO_NO);
		}
	}

	@Override
	public void setCupPara_OK(int type) {
		Message message = new Message();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		switch (type){
			case BlueToothRequest.type_handwarm_1:
				message.arg1 = BlueToothRequest.type_handwarm_1;
				message.what = setCupPara_ok;
				break;
			case BlueToothRequest.type_handwarm_0:
				message.arg1 = BlueToothRequest.type_handwarm_0;
				message.what = setCupPara_ok;
				break;
			case BlueToothRequest.type_vibration_1:
				message.arg1 = BlueToothRequest.type_vibration_1;
				message.what = setCupPara_ok;
				break;
			case BlueToothRequest.type_vibration_0:
				message.arg1 = BlueToothRequest.type_vibration_0;
				message.what = setCupPara_ok;
				break;
		}
		mHandler.sendMessage(message);
	}

	@Override
	public void setCupPara_NO(int type) {
		Message message = new Message();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		switch (type){
			case BlueToothRequest.type_handwarm_1:
				message.arg1 = BlueToothRequest.type_handwarm_1;
				message.what = setCupPara_no;
				break;
			case BlueToothRequest.type_handwarm_0:
				message.arg1 = BlueToothRequest.type_handwarm_0;
				message.what = setCupPara_no;
				break;
			case BlueToothRequest.type_vibration_1:
				message.arg1 = BlueToothRequest.type_vibration_1;
				message.what = setCupPara_no;
				break;
			case BlueToothRequest.type_vibration_0:
				message.arg1 = BlueToothRequest.type_vibration_0;
				message.what = setCupPara_no;
				break;
		}
		mHandler.sendEmptyMessage(setCupPara_no);
	}

	@Override
	public void getCupPara_OK() {
		Log.d(TAG, "getCupPara_OK-----------------------------------");
		mHandler.sendEmptyMessage(GETCUPPARA_OK);
	}

	@Override
	public void getCupPara_NO() {

	}

	@Override
	public void GetCupInfoing() {

	}

	@SuppressWarnings("unused")
	public void getOwnBitmap() {
		if (OcupApplication.getInstance().mOwnCup.getAvatorPath() == null) {
			iv_userimage.setImageResource(R.drawable.user_me);
			Logger.e("mOwncup.avatorpath = "+"null");
			return;
		}
		String avatorPath = OcupApplication.getInstance().mOwnCup
				.getAvatorPath();
		Logger.e(TAG, "-----------------------avatorPath = " + avatorPath);
		Bitmap bmp = TipsBitmapLoader.getInstance().getFromMemory(
				OcupApplication.getInstance().mOwnCup.getAvatorPath());
		Log.e(TAG, "-------------------------------660 = " + (bmp == null));
		if (bmp == null) {
			// bmp =
			// TipsBitmapLoader.getInstance().getFromFile(OcupApplication.getInstance().mOwnCup.getAvatorPath());
			if (bmp == null) {
				iv_userimage.setImageResource(R.drawable.user_me);
				TipsBitmapLoader.getInstance().asyncLoadBitmap(
						OcupApplication.getInstance().mOwnCup.getAvatorPath(),
						new TipsBitmapLoader.asyncLoadCallback() {
							@Override
							public void load(Bitmap bitmap) {
								if (null !=bitmap) {
									bitmap = Tools.getRoundedBitmap(bitmap, 100);
								}
								 final  Bitmap bt = bitmap;
								mActivity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (bt != null) {
											iv_userimage.setImageBitmap(bt);
										}
									}
								});
								OcupApplication.getInstance().mOwnCup
										.setBmp_head(bitmap);
							}
						});
			} else {
				bmp = Tools.getRoundedBitmap(bmp,100);
				iv_userimage.setImageBitmap(bmp);
				OcupApplication.getInstance().mOwnCup.setBmp_head(bmp);
			}
		} else {
			bmp = Tools.getRoundedBitmap(bmp,100);
			iv_userimage.setImageBitmap(bmp);
			OcupApplication.getInstance().mOwnCup.setBmp_head(bmp);
		}
	}

	/**
	 * 获取用户头像，下载和
	 */
	private void loaderAvatar() {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.imageScaleType(ImageScaleType.NONE)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.delayBeforeLoading(100)//下载的延时时间
				.displayer(new FadeInBitmapDisplayer(500))//图片加载好之后的动画
				.considerExifParams(true)
				.build();
		String url = null;
		url = Tools.getPreference(mActivity,UtilContact.OwnAvatar);
		Logger.e(TAG,"LoaderAvatar = " + url);
		if(url != "" && url != null){
			loaderImage(url, iv_userimage, options);
		}
	}

	//通过imageLoader加载图片
	public void loaderImage(String url,ImageView iv_long,DisplayImageOptions options){
		Logger.e(TAG,"loaderImage.url = "+url);
		ImageLoader.getInstance()
				.displayImage(url, iv_long, options, new SimpleImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
//						spinner.setVisibility(View.VISIBLE);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
						String message = null;
						switch (failReason.getType()) {
							case IO_ERROR:
//								message = mActivity.getString(R.string.check_network_avatar);
								break;
							case DECODING_ERROR:
//								message = mActivity.getString(R.string.check_network_avatar);
								break;
							case NETWORK_DENIED:
								message = mActivity.getString(R.string.check_network_avatar);
								Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
								break;
							case OUT_OF_MEMORY:
//								message = mActivity.getString(R.string.check_network_avatar);
								break;
							case UNKNOWN:
//								message = mActivity.getString(R.string.check_network_avatar);
								break;
						}
//						spinner.setVisibility(View.GONE);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//						spinner.setVisibility(View.GONE);
						//只有在更换头像时，显示成功的时候才提示更改成功。
						if (hasChanged){
							mHandler.sendEmptyMessage(getAvatarComplete);
						}
					}
				});
	}

	@Override
	public void getApkversion_success() {
		mHandler.sendEmptyMessage(GET_APKVERSION_OK);
	}

	@Override
	public void getApkversion_failed() {
		mHandler.sendEmptyMessage(GET_APKVERSION_NO);
	}

	@Override
	public void getSuccess(int type, String content) {
		switch (type){
			case UtilContact.getAvatorInfo:
//				Gson gson = new Gson();
//				 avatarOOS = gson.fromJson(content,AvatarOOS.class);
				Logger.e(TAG,"getAvatorInfo successful");
				avatarOOS = new AvatarOOS();
				try {
					JSONObject jsonObject = new JSONObject(content);
					Logger.e(TAG,content);
					avatarOOS.setAccessKeyId(jsonObject.getString("AccessKeyId"));
					avatarOOS.setAccessKeySecret(jsonObject.getString("AccessKeySecret"));
					avatarOOS.setBucket(jsonObject.getString("bucket"));
					avatarOOS.setExpiration(jsonObject.getString("Expiration"));
					avatarOOS.setPath(jsonObject.getString("path"));
					avatarOOS.setFilename(jsonObject.getString("filename"));
					avatarOOS.setSecurityToken(jsonObject.getString("SecurityToken"));
					mOOSClientUtils = new OOSClientUtils(mActivity,avatarOOS,filePath,SettingFragment.this);
					mOOSClientUtils.uploadFile();
					Logger.e(TAG,"获取阿里云token成功");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case UtilContact.getUserInfo:
				//更改头像后，请求用户信息成功后加载图片
				//注意这里是获取用户信息的回调，是在子线程中执行的，而ImageLoader内部是自己切线程，但是必须在主线程中启动，否则会报错
				mHandler.sendEmptyMessage(getUserInfoSeccess);
				break;
		}
	}


	@Override
	public void getFail(int type) {

	}

	@Override
	public void getIng(int type) {

	}

	//上传头像文件到阿里云回调
	@Override
	public void uploadSuccess() {
		//上传成功之后再去获取用户信息，获取得到的avatar可以直接获取
		HttpRequest.getInstance().getUserInfo(mActivity,SettingFragment.this);
		hasChanged = true;
	}

	@Override
	public void uploadFail() {
		//上传失败
		mHandler.sendEmptyMessage(getAvatarFail);
	}

	@Override
	public void uploadProgress(int progress) {

	}

	@Override
	public void getTipSuccess(String content) {

	}

	@Override
	public void getTipFail() {

	}
}
