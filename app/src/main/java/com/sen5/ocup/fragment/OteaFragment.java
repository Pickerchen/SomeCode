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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sen5.ocup.R;
import com.sen5.ocup.activity.MainActivity;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.blutoothstruct.BluetoothType;
import com.sen5.ocup.blutoothstruct.CupStatus;
import com.sen5.ocup.blutoothstruct.NFCInfo;
import com.sen5.ocup.callback.BluetoothCallback;
import com.sen5.ocup.callback.BluetoothCallback.IGetCupStatusCallback;
import com.sen5.ocup.callback.BluetoothCallback.ISetTeaPercentCallback;
import com.sen5.ocup.gui.NumberProgressBar;
import com.sen5.ocup.gui.NumberProgressBar.ProgressTextVisibility;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.gui.SeekArcDailog;
import com.sen5.ocup.gui.SeekArcDailog.OnSeekChangeListener;
import com.sen5.ocup.gui.SegoTextView;
import com.sen5.ocup.receiver.Bluetooth3Receiver;
import com.sen5.ocup.service.TeaService;
import com.sen5.ocup.struct.ChatMsgEntity;
import com.sen5.ocup.util.BlueToothRequest;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.TeaListUtil;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;

import java.util.Timer;
import java.util.TimerTask;

public class OteaFragment extends Fragment implements IGetCupStatusCallback, ISetTeaPercentCallback,BluetoothCallback.ISetCupParaCallback, Callback {

	private FrameLayout mFrameLayout;
	private static final String TAG = "OteaFragment";
	public static final long DURATOIN_GETCUPSTATUS = 5000;
	private Activity mActivity;
	private View mView = null;
	private Handler mHandler = null;

	private ProgressBar pb_bluetooth_connecting;// 表示蓝牙正在连接
	private ImageView iv_bluetooth_state;// 表示蓝牙连接状态

//	private LinearLayout mLayout_tea;
//	private SegoTextView mTV_teanickname;
//	private SegoTextView mTV_teaname;
//	private MarqueeTextView mTV_teataste;
//	private MarqueeTextView mTV_teaplace;
//	private SegoTextView mTV_teaage;
	private SegoTextView mTV_curwaterTempature;
	private SegoTextView mTV_stop;
	private RelativeLayout mLayout_time;
	private NumberProgressBar mPb_tea;
	private SegoTextView mTV_countdown;
//	private ImageView mIV_teaone, mIV_teatwo, mIV_teathree, mIV_teafour, mIV_teafive;
	private int[] mTeaSrcs_n;
	private int[] mTeaSrcs_p;
	private ImageView[] mIVs_tea;

	private SeekArcDailog mArcDailog;

	private int selectedPos = 2;// 当前选中的茶在TeaList的位置
	private NFCInfo mNFCInfo;
	// private boolean isNFC;
	public static boolean isStart_byuser; // 是否手动泡茶
	private int countDownTotalTime; // 倒计时的总时间
	private boolean isFirstNFC; // 通过通知打开应用，mVIew为null，需要先执行oncreate后再处理NFC信息
	public static boolean isRecieveNFC; // 标识是否在收到未处理NFC
	
	public boolean isRun_getcupstatus; // 标识是否要定时去杯子状态

	private ServiceTeaReceiver mReceiverTea;
	/**
	 * 启动服务的intent
	 */
	private Intent intent_serviceTea;

	//更新蓝牙状态
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "onReceive---------------------action===" + action);
			if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
				// mHandler.sendEmptyMessage(CONNECTBLUETOOTH_OK);
			} else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
				mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO);
			} else if (action.equals(BluetoothConnectUtils.ACTION_BLUETOOTHSTATE)) {
				int bluestate = intent.getIntExtra(BluetoothConnectUtils.KEY_BLUETOOTHSTATE, -1);
				Log.d(TAG, "bluetooth connectstate bluestate==" + bluestate);
				if (bluestate == BluetoothConnectUtils.CONNECT_OK) {
					mHandler.sendEmptyMessage(CONNECTBLUETOOTH_OK);
				} else if (bluestate == BluetoothConnectUtils.CONNECT_NO) {
					mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO);
				} else if (bluestate == BluetoothConnectUtils.CONNECT_ING) {
					mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO);
				}
			}
		}
	};
	//NFC接收器
	private BroadcastReceiver receiver_bluetoothNFC = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "onReceive-----------receiver_bluetoothNFC----------action===" + action);
			if (action.equals(Bluetooth3Receiver.ACTION_RECIEVE_NFCDATA)) {
				doNFC();
			}
		}
	};
	private boolean isRegisterNFCReciever;// 是否注册了NFC接收器
	protected int mTeacode;// 修改泡茶时间对应的茶编码
	protected int mTimePercent;// 修改的泡茶时间比例（10---90） x/

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		Log.d(TAG, "setUserVisibleHint()-----------isVisibleToUser==" + isVisibleToUser);
		Log.e(TAG, "-------------isRun_getcupstatus = " +isRun_getcupstatus + "   isVisibleToUser==" + isVisibleToUser);
		
		// 判断fragment 是否可见
		if (isVisibleToUser) {
			if (!isRun_getcupstatus) {
				new GetCupStatusThread().start();
				isRun_getcupstatus = true;
			}
			if (null != mTV_curwaterTempature) {//
				mTV_curwaterTempature.setText("" + CupStatus.getInstance().getCur_water_temp());
			}
			if (null != mActivity) {
				isRegisterNFCReciever = true;
				IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
				filter.addAction(Bluetooth3Receiver.ACTION_RECIEVE_NFCDATA);
				mActivity.registerReceiver(receiver_bluetoothNFC, filter);
			}
			setBluetoothState();
			Log.e(TAG, "---------11----isRun_getcupstatus = " +isRun_getcupstatus);
			
			if (MainActivity.isFromNFC){
				if (null != mView) {
					mTV_curwaterTempature.setText("" + CupStatus.getInstance().getCur_water_temp());
					Log.e(TAG, "------222-------isRun_getcupstatus = " +isRun_getcupstatus);
					doNFC();
				} else {
					isFirstNFC = true;
					
					Log.d(TAG, "setUserVisibleHint()--------null =====mView");
				}
			}else if(isRecieveNFC){
				Log.e(TAG, "----------333---isRun_getcupstatus = " +isRun_getcupstatus);
				
				doNFC();
			}

		} else {
			isRun_getcupstatus = false;
			if (mActivity != null && isRegisterNFCReciever) {
				isRegisterNFCReciever = false;
				mActivity.unregisterReceiver(receiver_bluetoothNFC);
			}
		}
		super.setUserVisibleHint(isVisibleToUser);
	}

	/**
	 * 处理NFC信息
	 */
	private void doNFC() {
		OteaFragment.isRecieveNFC = false;
		MainActivity.isFromNFC = false;
		int curTeaIndex = TeaListUtil.getInstance().getCurTeaIndex(mActivity);
		Log.d(TAG, "doNFC()------curTeaIndex==" + curTeaIndex);
		Log.e(TAG, "-----------------------1");
		if (curTeaIndex >= 0 && mNFCInfo != null) {
			countDownTotalTime = mNFCInfo.getDur() + mNFCInfo.getCur_time() - Tools.getCurSecond();
			
			Log.d(TAG, "doNFC()--countDownTotalTime==" + mNFCInfo.getDur() + ":::getCur_time = " + mNFCInfo.getCur_time()+
					":::getCurSecond = " + Tools.getCurSecond());
			long cur_water_yield = CupStatus.getInstance().getCur_water_yield();
			
			if (CupStatus.getInstance().getCur_water_temp() < 60) {// 水温过低，不宜泡茶
				
				if(null != mTV_curwaterTempature){
					mTV_curwaterTempature.setText("" + CupStatus.getInstance().getCur_water_temp());
				}
//				OcupToast.makeText(mActivity, getString(R.string.teaing_failed), Toast.LENGTH_SHORT).show();
				countDownTotalTime = 0;
				return;
			}
			if (mNFCInfo.getDur() == 65535 && CupStatus.getInstance().getCur_water_temp() < 60) {
//				OcupToast.makeText(mActivity, getString(R.string.error) + mNFCInfo.getDur(), Toast.LENGTH_SHORT).show();
//				OcupToast.makeText(mActivity, getString(R.string.teaing_failed), Toast.LENGTH_SHORT).show();
				NFCInfo.getInstance().clearNFCInfo();
				mPb_tea.setProgress(100);
				mTV_countdown.setText(getString(R.string.start_tea));
				mTV_stop.setVisibility(View.INVISIBLE);
				mLayout_time.setVisibility(View.VISIBLE);
				countDownTotalTime = 0;
				return;
			}
			if(mNFCInfo.getDur() == 65535){
				Log.e(TAG, "doNFC()--countDownTotalTime==" + countDownTotalTime + ":::::cur_water_yield = " + cur_water_yield);
				return;
			}
			Log.e(TAG, "-----------------------2");
			if (countDownTotalTime > 0) {
				mPb_tea.setProgress(0);
				mTV_countdown.setText(getString(R.string.teaing) + countDownTotalTime / 60 + getString(R.string.teaing_minutes) + (countDownTotalTime % 60)
						+ getString(R.string.teaing_seconds));
				mTV_stop.setVisibility(View.VISIBLE);
				mLayout_time.setVisibility(View.INVISIBLE);

				startTea(countDownTotalTime);
				Log.e(TAG, "-----------------------3");
			} else {
				//泡茶已结束
//				OcupToast.makeText(mActivity, getString(R.string.teaing_failed), Toast.LENGTH_SHORT).show();
				countDownTotalTime = 0;
			}

			if (countDownTotalTime > 0) {
				Log.d(TAG, "doNFC()---setSelectPosition---curTeaIndex==" + curTeaIndex);
				switch (curTeaIndex) {
//				case 0:
//					mIV_teaone.performClick();
//					break;
//				case 1:
//					mIV_teatwo.performClick();
//					break;
//				case 2:
//					mIV_teathree.performClick();
//					break;
//				case 3:
//					mIV_teafour.performClick();
//					break;
//				case 4:
//					mIV_teafive.performClick();
//					break;
				default:
					break;
				}
			}
			Log.e(TAG, "-----------------------4");
		}else{
			Log.d(TAG, "doNFC()------curTeaIndex==" + curTeaIndex + ":::mNFCInfo = " + (mNFCInfo == null));
		}
	}

	/**
	 * 发送请求获取杯子状态
	 */
	public void getCupStatus() {
		if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
			BlueToothRequest.getInstance().sendMsg2getCupStatus(OteaFragment.this);
		} else {
		}
	}

	/**
	 * 根据蓝牙连接状态，修改状态图标
	 */
	private void setBluetoothState() {
		if (null != iv_bluetooth_state) {
			Log.d(TAG, "setBluetoothState--------BluetoothConnectUtils.getInstance().bluetoothState ==" + BluetoothConnectUtils.getInstance().getBluetoothState());
			if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED && BluetoothAdapter.getDefaultAdapter().isEnabled()) {
				pb_bluetooth_connecting.setVisibility(View.GONE);
				iv_bluetooth_state.setVisibility(View.VISIBLE);
				iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_active);
			} else if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTING) {
				pb_bluetooth_connecting.setVisibility(View.VISIBLE);
				iv_bluetooth_state.setVisibility(View.GONE);
				iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_inactive);
			} else if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
				BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_NONE);
				pb_bluetooth_connecting.setVisibility(View.GONE);
				iv_bluetooth_state.setVisibility(View.VISIBLE);
				iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_inactive);
			} else {
				pb_bluetooth_connecting.setVisibility(View.GONE);
				iv_bluetooth_state.setVisibility(View.VISIBLE);
				iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_inactive);
			}
		}
	}

	// 返回此fragment自己的view
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView--------isFirstNFC==-" + isFirstNFC);
		if (mView == null) {
			mActivity = getActivity();
			mView = inflater.inflate(R.layout.fragment_tea, container, false);
			initialComponent();
		} else {
			// mView判断是否已经被加过parent，如果没删除，会发生mView已有parent的错误
			ViewGroup parent = (ViewGroup) mView.getParent();
			if (parent != null) {
				parent.removeView(mView);
			}
		}

		if (isFirstNFC) {
			isFirstNFC = false;

			doNFC();
		}
		return mView;

	}

	private void initialComponent() {
		mHandler = new Handler(this);
		mFrameLayout = (FrameLayout) mView.findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(mFrameLayout,mActivity);

//		mLayout_tea = (LinearLayout) mView.findViewById(R.id.tea_layout);
//		mTV_teanickname = (SegoTextView) mView.findViewById(R.id.tv_teanickname);
//		mTV_teaname = (SegoTextView) mView.findViewById(R.id.tv_teaname);
//		mTV_teataste = (MarqueeTextView) mView.findViewById(R.id.tv_tea_taste);
//		mTV_teaplace = (MarqueeTextView) mView.findViewById(R.id.tv_tea_place);
//		mTV_teaage = (SegoTextView) mView.findViewById(R.id.tv_tea_age);
		mTV_curwaterTempature = (SegoTextView) mView.findViewById(R.id.tv_curwatertemperature);
		mTV_stop = (SegoTextView) mView.findViewById(R.id.tv_stop);
		mLayout_time = (RelativeLayout) mView.findViewById(R.id.layout_time);
		mPb_tea = (NumberProgressBar) mView.findViewById(R.id.numberbar_tea);
		mTV_countdown = (SegoTextView) mView.findViewById(R.id.tv_countdowm);
		mTV_countdown.setOnClickListener(mOnClickListener);
//		mIV_teaone = (ImageView) mView.findViewById(R.id.iv_teaone);
//		mIV_teatwo = (ImageView) mView.findViewById(R.id.iv_teatwo);
//		mIV_teathree = (ImageView) mView.findViewById(R.id.iv_teathree);
//		mIV_teafour = (ImageView) mView.findViewById(R.id.iv_teafour);
//		mIV_teafive = (ImageView) mView.findViewById(R.id.iv_teafive);

		mTeaSrcs_n = new int[] { R.drawable.btn_nan_n, R.drawable.btn_xi_n, R.drawable.btn_yi_n, R.drawable.btn_hao_n, R.drawable.btn_bu_n };
		mTeaSrcs_p = new int[] { R.drawable.btn_nan_p, R.drawable.btn_xi_p, R.drawable.btn_yi_p, R.drawable.btn_hao_p, R.drawable.btn_bu_p };
//		mIVs_tea = new ImageView[] { mIV_teaone, mIV_teatwo, mIV_teathree, mIV_teafour, mIV_teafive };

		if (TeaService.mTeaState == TeaService.TEA_FREE) {
			mPb_tea.setMax(100);
			mPb_tea.setProgress(100);
			mTV_stop.setVisibility(View.INVISIBLE);
			mLayout_time.setVisibility(View.VISIBLE);
		} else {
			mPb_tea.setMax(TeaService.mCountDownTime);
			mTV_stop.setVisibility(View.VISIBLE);
			mLayout_time.setVisibility(View.INVISIBLE);
		}
		mPb_tea.setProgressTextVisibility(ProgressTextVisibility.Invisible);// 设置进度上的百分比不可见

		iv_bluetooth_state = (ImageView) mView.findViewById(R.id.iv_bluetooth_state);
		pb_bluetooth_connecting = (ProgressBar) mView.findViewById(R.id.pb_bluetooth_connecting);
		setBluetoothState();
		iv_bluetooth_state.setOnClickListener(mOnClickListener);

		mTV_curwaterTempature.setText("" + CupStatus.getInstance().getCur_water_temp());
		mNFCInfo = NFCInfo.getInstance();

		mArcDailog = new SeekArcDailog(mActivity);
		mArcDailog.setOnSeekArcChangListener(mOnSeekChangeListener);

		mPb_tea.setOnClickListener(mOnClickListener);
		mTV_stop.setOnClickListener(mOnClickListener);
		mLayout_time.setOnClickListener(mOnClickListener);
//		mIV_teaone.setOnClickListener(mOnClickListener);
//		mIV_teatwo.setOnClickListener(mOnClickListener);
//		mIV_teathree.setOnClickListener(mOnClickListener);
//		mIV_teafour.setOnClickListener(mOnClickListener);
//		mIV_teafive.setOnClickListener(mOnClickListener);

		// 注册BroadcastReceiver
		mReceiverTea = new ServiceTeaReceiver();
		IntentFilter mFilter = new IntentFilter(TeaService.SERVICETEA_SERVICE);
		mActivity.registerReceiver(mReceiverTea, mFilter);

	}

	/**
	 * 启动泡茶任务
	 * 
	 * @param time
	 */
	private void startTea(int time) {
		if (TeaService.mTeaState == TeaService.TEA_WORK) {
			// 若泡茶服务已经启动，则先停止
			TeaService.mTeaState = TeaService.TEA_FREE;
		}

		// 设置进度条的最大值
		mPb_tea.setMax(time);

		// 启动泡茶服务
		TeaService.startTea(time);
		setTeaBtnEnableClick(false);
	}

	private Timer mTimer;
	private void startTea1() {
		if (mTimer != null){
			mTimer.cancel();
			mTimer = null;
		}
		Timer timer = new Timer();
		mTimer = timer;
		mPb_tea.setMax(100);
		mPb_tea.setVisibility(View.VISIBLE);
		mTimer.schedule(new MyTimerTask(),10,1000);
	}

	class  MyTimerTask extends TimerTask{
		int temTime = 0;
		@Override
		public void run() {
			temTime += 1000;
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					int pb = temTime / 6000;
					if (pb == 100) {
						mTV_countdown.setClickable(true);
						mPb_tea.setVisibility(View.GONE);
						mTV_countdown.setVisibility(View.VISIBLE);
						mTV_countdown.setText("开始泡茶");
						if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
							setCupPara("");
						} else {
							OcupToast.makeText(mActivity, getString(R.string.unconnect_2cup), Toast.LENGTH_LONG).show();
						}
						MyTimerTask.this.cancel();
						mTimer.cancel();
					} else {
						if (mTV_countdown.isClickable()){
							mTV_countdown.setClickable(false);
						}
						int min = (600000 - temTime) / 60000;
						String mins = "";
						if (min == 0) {
							mins = "";
						}
						else {
							mins = min+"分";
						}
						int sec = ((600000 - temTime) % 60000) / 1000;
						mTV_countdown.setText("离一杯好茶还有" + mins + sec + "秒");
						if (sec == 0) {
							mTV_countdown.setText("开始泡茶");
						}
						mPb_tea.setProgress(pb);
					}
				}
			});
		}
	}

	private void setCupPara(String content) {
		content = "The Tea Is Ready";
		ChatMsgEntity entity = null;
		entity = ChatMsgEntity.createChatMsgEntity(Tools.getPreference(mActivity, UtilContact.HuanXinId), Tools.getPreference(mActivity,UtilContact.HuanXinId),
				content, "", 0, ChatMsgEntity.FROM_ME);
		entity.setStatus(1);
		BlueToothRequest.getInstance().sendMsg2LED(entity, 1);
	}

	/**
	 * 停止泡茶任务
	 */
	private void stopTea() {
		TeaService.mTeaState = TeaService.TEA_FREE;
		setTeaBtnEnableClick(true);
	}

	private void setTeaBtnEnableClick(boolean enableClick){
		if(mIVs_tea == null){
			return;
		}
		int length = mIVs_tea.length;
		for (int i = 0; i < length; i++) {
			ImageView imageView = mIVs_tea[i];
			if(null != imageView){
				imageView.setClickable(enableClick);
			}
		}
	}
	
	private OnSeekChangeListener mOnSeekChangeListener = new OnSeekChangeListener() {

		@Override
		public void onProgressChanged(int progress) {
			Log.d(TAG, "mOnSeekChangeListener-------progress==" + progress);
			if (progress >= 0 && progress <= 80) {
				if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
					mTeacode = Integer.parseInt(TeaListUtil.getInstance().teaList.get(selectedPos).getNameCode());
					Log.d(TAG, "onProgressChanged-------------progress==" + (progress + 10) + "   teacode==" + mTeacode);
					mTimePercent = progress + 10;
					BlueToothRequest.getInstance().sendMsg2changeTea(OteaFragment.this, mTimePercent, mTeacode);
				} else {
					OcupToast.makeText(mActivity, getString(R.string.unconnect_2cup), Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.iv_bluetooth_state:// 断开or连接蓝牙
				Log.d(TAG, "onclick             iv_bluetooth_state");
				break;
//			case R.id.iv_teaone:
//				mIVs_tea[selectedPos].setImageResource(mTeaSrcs_n[selectedPos]);
//				selectedPos = 0;
//				mIVs_tea[selectedPos].setImageResource(mTeaSrcs_p[selectedPos]);
//
//				mTV_teanickname.setText(getString(R.string.teanickname_nan));
//				mTV_teaname.setText(getString(R.string.teaname_nan));
//				mTV_teataste.setText(getString(R.string.taste_nan));
//				mTV_teaplace.setText(getString(R.string.teaplace_nan));
//				mTV_teaage.setText("0.5");
//				mLayout_tea.setBackgroundResource(R.drawable.tea_nan_bg);
//				break;
//			case R.id.iv_teatwo:
//				mIVs_tea[selectedPos].setImageResource(mTeaSrcs_n[selectedPos]);
//				selectedPos = 1;
//				mIVs_tea[selectedPos].setImageResource(mTeaSrcs_p[selectedPos]);
//				mTV_teanickname.setText(getString(R.string.teanickname_xi));
//				mTV_teaname.setText(getString(R.string.teaname_xi));
//				mTV_teataste.setText(getString(R.string.taste_xi));
//				mTV_teaplace.setText(getString(R.string.teaplace_xi));
//				mTV_teaage.setText("0.5");
//				mLayout_tea.setBackgroundResource(R.drawable.tea_xi_bg);
//				break;
//			case R.id.iv_teathree:
//				mIVs_tea[selectedPos].setImageResource(mTeaSrcs_n[selectedPos]);
//				selectedPos = 2;
//				mIVs_tea[selectedPos].setImageResource(mTeaSrcs_p[selectedPos]);
//				mTV_teanickname.setText(getString(R.string.teanickname_yi));
//				mTV_teaname.setText(getString(R.string.teaname_yi));
//				mTV_teataste.setText(getString(R.string.taste_yi));
//				mTV_teaplace.setText(getString(R.string.teaplace_yi));
//				mTV_teaage.setText("0.5");
//				mLayout_tea.setBackgroundResource(R.drawable.tea_yi_bg);
//				break;
//			case R.id.iv_teafour:
//				mIVs_tea[selectedPos].setImageResource(mTeaSrcs_n[selectedPos]);
//				selectedPos = 3;
//				mIVs_tea[selectedPos].setImageResource(mTeaSrcs_p[selectedPos]);
//				mTV_teanickname.setText(getString(R.string.teanickname_hao));
//				mTV_teaname.setText(getString(R.string.teaname_hao));
//				mTV_teataste.setText(getString(R.string.taste_hao));
//				mTV_teaplace.setText(getString(R.string.teaplace_hao));
//				mTV_teaage.setText("0.5");
//				mLayout_tea.setBackgroundResource(R.drawable.tea_hao_bg);
//				break;
//			case R.id.iv_teafive:
//				mIVs_tea[selectedPos].setImageResource(mTeaSrcs_n[selectedPos]);
//				selectedPos = 4;
//				mIVs_tea[selectedPos].setImageResource(mTeaSrcs_p[selectedPos]);
//				mTV_teanickname.setText(getString(R.string.teanickname_bu));
//				mTV_teaname.setText(getString(R.string.teaname_bu));
//				mTV_teataste.setText(getString(R.string.taste_bu));
//				mTV_teaplace.setText(getString(R.string.teaplace_bu));
//				mTV_teaage.setText("0.5");
//				mLayout_tea.setBackgroundResource(R.drawable.tea_bu_bg);
//				break;
			case R.id.numberbar_tea:
				Log.d(TAG, "onclick----------numberbar_tea");
				if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
					
				} else {
					OcupToast.makeText(mActivity, getString(R.string.unconnect_2cup), Toast.LENGTH_SHORT).show();
					return;
				}
				if (TeaService.mTeaState == TeaService.TEA_FREE) {
					
					if (CupStatus.getInstance().getCur_water_temp() < 60) {// 水温过低，不宜泡茶
//						OcupToast.makeText(mActivity, getString(R.string.teaing_failed), Toast.LENGTH_SHORT).show();
						countDownTotalTime = 0;
						return;
					}
					if (countDownTotalTime > 60000) {
//						OcupToast.makeText(mActivity, getString(R.string.teaing_failed), Toast.LENGTH_SHORT).show();
						NFCInfo.getInstance().clearNFCInfo();
						mPb_tea.setProgress(100);
						mTV_countdown.setText(getString(R.string.start_tea));
						mTV_stop.setVisibility(View.INVISIBLE);
						mLayout_time.setVisibility(View.VISIBLE);
						countDownTotalTime = 0;
						return;
					}

					int dur = TeaListUtil.getInstance().getTeaDuaration(mActivity, selectedPos, CupStatus.getInstance().getCur_water_temp());
					countDownTotalTime = dur;

					if (countDownTotalTime > 0) {
						mPb_tea.setProgress(0);
						mTV_countdown.setText(getString(R.string.teaing) + countDownTotalTime / 60 + getString(R.string.teaing_minutes) + (countDownTotalTime % 60)
								+ getString(R.string.teaing_seconds));
						mTV_stop.setVisibility(View.VISIBLE);
						mLayout_time.setVisibility(View.INVISIBLE);

						startTea(countDownTotalTime);
						
						//发送请求告知杯子
						if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
							BlueToothRequest.getInstance().sendMsg2SetTeaing(countDownTotalTime);
						} else {
							OcupToast.makeText(mActivity, getString(R.string.unconnect_2cup), Toast.LENGTH_SHORT).show();
						}
					} else {
//						OcupToast.makeText(mActivity, getString(R.string.teaing_failed), Toast.LENGTH_SHORT).show();
						countDownTotalTime = 0;
					}
				}
				break;

			case R.id.tv_stop:
				Log.d(TAG, "mOnClickListener-----stop tea");
				stopTea();

				NFCInfo.getInstance().clearNFCInfo();

				mPb_tea.setMax(100);
				mPb_tea.setProgress(100);
				mTV_countdown.setText(getString(R.string.start_tea));
				mTV_stop.setVisibility(View.INVISIBLE);
				mLayout_time.setVisibility(View.VISIBLE);

				if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
					BlueToothRequest.getInstance().sendMsg2ControlCup(null, BluetoothType.control_teaok);
				} else {
					OcupToast.makeText(mActivity, getString(R.string.unconnect_2cup), Toast.LENGTH_SHORT).show();
				}
				break;

			case R.id.layout_time:
				Log.d(TAG, "click tv_time   selectedPos==" + selectedPos + "  percent== " + (TeaListUtil.getInstance().teaList.get(selectedPos).getPercent()));
				mArcDailog.setProgress((TeaListUtil.getInstance().teaList.get(selectedPos).getPercent() - 10));
				mArcDailog.show();
				break;
				case R.id.tv_countdowm:
					startTea1();
					break;
			default:
				break;
			}
		}
	};

	@Override
	public void onResume() {
		Log.d(TAG, "onResume--------his.getUserVisibleHint()=-" + this.getUserVisibleHint());
		super.onResume();
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothConnectUtils.ACTION_BLUETOOTHSTATE);
		mActivity.registerReceiver(receiver, filter);
		// 页面是否可见
		if (this.getUserVisibleHint() && !isRun_getcupstatus) {
			new GetCupStatusThread().start();
			isRun_getcupstatus = true;
		}
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart---------");
		super.onStart();

	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause---------");
		super.onPause();
		mActivity.unregisterReceiver(receiver);
		if (isRegisterNFCReciever) {
			isRegisterNFCReciever = false;
			mActivity.unregisterReceiver(receiver_bluetoothNFC);
		}

		isRun_getcupstatus = false;
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop---------");
		super.onStop();

	}

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView---------");
		super.onDestroyView();
		mActivity.unregisterReceiver(mReceiverTea);
//		mActivity.registerReceiver(mReceiverTea, mFilter);
	}

	@Override
	public void setCupPara_OK(int type) {

	}

	@Override
	public void setCupPara_NO(int type) {

	}

	class GetCupStatusThread extends Thread {

		@Override
		public void run() {
			super.run();
			while (isRun_getcupstatus) {
				Log.d(TAG, "GetCupStatusThread--------isRun_getcupstatus=true");
				getCupStatus();
				try {
					Thread.sleep(DURATOIN_GETCUPSTATUS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private final int UPDATE_PROGRESS_VISIBLE = 1;
	private final int UPDATE_PROGRESS_INVISIBLE = 2;
	private final int GETCUPSTATUS_OK = 3;
	// private final int GETCUPSTATUS_NO = 4;
	private final int CONNECTBLUETOOTH_OK = 6;
	private final int CONNECTBLUETOOTH_NO = 7;
	private final int CONNECTBLUETOOTH_ING = 10;
	private final int SET_TEAPERCENT_OK = 8;
	private final int SET_TEAPERCENT_NO = 9;

	@Override
	public void getCupStatus_OK() {
		if (null == mHandler) {
			return;
		}
		mHandler.sendEmptyMessage(GETCUPSTATUS_OK);
	}

	@Override
	public void getCupStatus_NO() {

	}

	@Override
	public void setTeaPercent_OK() {
		mHandler.sendEmptyMessage(SET_TEAPERCENT_OK);
	}

	@Override
	public void setTeaPercent_NO() {
		mHandler.sendEmptyMessage(SET_TEAPERCENT_NO);
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (null != OteaFragment.this.getView()) {

			switch (msg.what) {
			case CONNECTBLUETOOTH_OK:
				if (null != iv_bluetooth_state) {
					pb_bluetooth_connecting.setVisibility(View.GONE);
					iv_bluetooth_state.setVisibility(View.VISIBLE);
					iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_active);
				}
				break;
			case CONNECTBLUETOOTH_NO:
				if (null != iv_bluetooth_state) {
					pb_bluetooth_connecting.setVisibility(View.GONE);
					iv_bluetooth_state.setVisibility(View.VISIBLE);
					iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_inactive);
				}
				break;
			case CONNECTBLUETOOTH_ING:
				pb_bluetooth_connecting.setVisibility(View.VISIBLE);
				iv_bluetooth_state.setVisibility(View.GONE);
				break;
			// 获取杯子状态成功，检查是否可泡茶
			case GETCUPSTATUS_OK:
				Log.d(TAG, "handmsg-----------GETCUPSTATUS_OK");
				mTV_curwaterTempature.setText("" + CupStatus.getInstance().getCur_water_temp());
				break;
			case SET_TEAPERCENT_OK:// 将改变的时间百分比设置到服务器？
				OcupToast.makeText(mActivity, getString(R.string.set_succed), Toast.LENGTH_SHORT).show();
				int index = TeaListUtil.getInstance().getCurChangeTeaIndex(mActivity, "0" + mTeacode);
				Log.d(TAG, "handmsg   SET_TEAPERCENT_OK   index =" + index);
				if (index >= 0) {
					TeaListUtil.getInstance().teaList.get(index).setPercent(mTimePercent);
					Tools.savePreference(OcupApplication.getInstance(), "" + mTeacode, "" + mTimePercent);
				}
				break;
			case SET_TEAPERCENT_NO:
				OcupToast.makeText(mActivity, getString(R.string.set_failed), Toast.LENGTH_SHORT).show();
				break;
			}
		}
		return false;
	}

	/**
	 * 泡茶进度广播接收
	 * @author yaojiaxu
	 *
	 */
	private class ServiceTeaReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (null != OteaFragment.this.getView()) {
				Bundle bundle = intent.getExtras();
				int pd_value = bundle.getInt("ProgressValue");
				mPb_tea.setProgress(pd_value);
				mTV_countdown.setText(getString(R.string.teaing) + (TeaService.mCountDownTime - pd_value) / 60 + getString(R.string.teaing_minutes)
						+ ((TeaService.mCountDownTime - pd_value) % 60) + getString(R.string.teaing_seconds));
				Log.d(TAG, "ServiceTeaReceiver   pd_value==" + pd_value + "      TeaService.mCountDownTime==" + TeaService.mCountDownTime);
				if (pd_value >= TeaService.mCountDownTime) {
					// 泡茶结束
					mTV_countdown.setText(getString(R.string.start_tea));
					mTV_stop.setVisibility(View.INVISIBLE);
					mLayout_time.setVisibility(View.VISIBLE);
					OcupToast.makeText(mActivity, getString(R.string.tea_done), Toast.LENGTH_SHORT).show();
					setTeaBtnEnableClick(true);
				}
			}
		}
	}

}
