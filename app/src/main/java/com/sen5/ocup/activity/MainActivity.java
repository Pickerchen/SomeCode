package com.sen5.ocup.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.ConnectionListener;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.blutoothstruct.CupPara;
import com.sen5.ocup.blutoothstruct.CupStatus;
import com.sen5.ocup.callback.BluetoothCallback;
import com.sen5.ocup.callback.CustomInterface;
import com.sen5.ocup.callback.RequestCallback;
import com.sen5.ocup.fragment.OchatFragment2;
import com.sen5.ocup.fragment.OmeFragment;
import com.sen5.ocup.fragment.OteaFragment;
import com.sen5.ocup.fragment.SettingFragment;
import com.sen5.ocup.fragment.TipsFragment;
import com.sen5.ocup.gui.Circle_ProgressBar;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.receiver.Bluetooth3Receiver;
import com.sen5.ocup.receiver.HomeWatcher;
import com.sen5.ocup.receiver.HomeWatcher.OnHomePressedListener;
import com.sen5.ocup.receiver.HuanxinBroadcastReceiver;
import com.sen5.ocup.service.BackGroundControlService;
import com.sen5.ocup.service.BluetoothService;
import com.sen5.ocup.service.TeaService;
import com.sen5.ocup.util.AnimTab;
import com.sen5.ocup.util.BlueToothRequest;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.HuanxinUtil;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;
import com.sen5.ocup.yili.OkHttpRequest;
import com.sen5.ocup.yili.UpdateCupConnecteThread;

import java.io.File;
import java.util.Calendar;

import static com.sen5.ocup.service.TeaService.context;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 : 应用主界面
 */
public class MainActivity extends BaseFragmentActivity implements
		ConnectionListener, OnHomePressedListener,RequestCallback.ILoginCallBack,RequestCallback.IGetUpdateInfo,
		RequestCallback.IDownLoadAPk,CustomInterface.IDialog,BluetoothCallback.IGetCupStatusCallback{

	private static final String TAG = "MainActivity";
	public static Context mContext;
	private DBManager dbMgr;


	private Circle_ProgressBar pb;

	public static int mScreenWidth;
	public static int mScreenHeight;

	private SettingFragment mSettingFragment = null;

	private HomeWatcher mHomeKeyReceiver = null;
	/**
	 * 是否通过nfc进入的
	 */
	public static boolean isFromNFC;
	/**
	 * 是否通过chat进入的
	 */
	public static boolean isFromChat;

	private ViewPager viewPager;
	private ImageView iv_user, iv_tips, iv_shortcut, iv_heart;
	private TextView one_tab, two_tab, three_tab, four_tab;
	private RelativeLayout data_tab, tips_tab, teas_tab, heart_tab;
	private CustomDialog dialog;


	//全局监听网络状态
	public static  int netState;
	//升级水杯apkdialog
	private  File apk;
	private  String downLoadUrl;
	private CustomDialog customDialog;
	private final int showDialog = 1;
	private final int dissMissDialog = 2;
	private final int updateProgress = 3;
	private final int createDialog = 4;
	private final int DURATOIN_GETCUPSTATUS = 15000;
	private final int receiverCupStatus = 5;
	public static String receiverCupStatusInfo = "RECEIVERCUPSTATUSINFO";
	private boolean hasReceiverStatus;
	private boolean sendCallBackSuccess;
	private boolean hasBindWindow;//判断是否已经绑定窗口
	public static int hasBinded = 8;//绑定完成
	public static final int startSendGroupMsg = 9;//开始发送群聊信息
	public static final int startRemindDrink = 10;//开始监听饮水闹钟
	public static final int startJudgeOnline = 11;//开始判断好友是否在线信息
	public static final int startRequestUpdateInfo = 12;//获取apk更新信息
	public static final int startGetGroup_id = 13;//获取群id
	public static final int startRequestPermission = 14;//获取权限

	public static final int showRemindDialog = 6;
	public static final int showSuspend = 7;
	private String name;

	private int lastTemp = 0;
	private int thisTemp = 0;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case 10000:
					initView();
					initData();
					break;
				case showDialog:
					customDialog.show();
					break;
				case dissMissDialog:
					customDialog.dismiss();
					break;
				case updateProgress:
					int progress = msg.arg1;
					notifyNotification(progress);
					break;
				case createDialog:
					String content = msg.obj.toString();
					customDialog = new CustomDialog(MainActivity.this,MainActivity.this,R.style.custom_dialog,CustomDialog.APK_UPDATE_DIALOG,content);
					break;
				case receiverCupStatus:
				//发送群聊信息，通知所有好友已上线
				String content2 = "";
					thisTemp = CupStatus.getInstance().getCur_water_temp();
					//温度不一样的时候再发送数据
						if ("true".equals(Tools.getPreference(MainActivity.this, UtilContact.OPENDATA))){
							content2 = "#1#1#" + thisTemp;
							Logger.e(TAG,"receiverCupStatus,and send GroupMsg temp is"+thisTemp);
						}
						else {
							content2 = "#1#1#0";
						}
						HuanxinUtil.getInstance().sendGroupMsg(MainActivity.this,content2);
						//发送广播，通知其他页面更新UI
						Intent intent = new Intent(receiverCupStatusInfo);
						sendBroadcast(intent);
					lastTemp = thisTemp;
					break;
				case showRemindDialog:
					DBManager dbManager = new DBManager(MainActivity.this);
					name = dbManager.queryYiLiCup().getNickname();
					if (dialog == null){
						dialog = new CustomDialog(MainActivity.this,MainActivity.this, R.style.custom_dialog,CustomDialog.DIALOG_REMIND_DRINK,name);
						if (hasBindWindow){
							dialog.show();
						}
					}
					else {
						if (!dialog.isShowing() && hasBindWindow){
							dialog.show();
						}
					}
					break;
				case showSuspend:
					Tools.showSuspend(MainActivity.this,name);
					break;
				case startJudgeOnline:
					UpdateCupConnecteThread.startmeasureTime(MainActivity.this);
					break;
				case startRemindDrink:
					if (! BackGroundControlService.hasStart){
						BackGroundControlService.shouldRemind = true;
						Intent intent2 = new Intent(MainActivity.this,BackGroundControlService.class);
						startService(intent2);
					}
					break;
				case startSendGroupMsg:
					UpdateCupConnecteThread.startSendGroup(MainActivity.this);
					break;
				case startRequestUpdateInfo:
					requestUpdateInfo();
					break;
				case startGetGroup_id:
					getGroupId();
					break;
				case startRequestPermission:
					break;
			}
		}
	};
	/**
	 * tab的图片
	 */
	private int[] tabImg_id = {R.drawable.ic_temperature_default,R.drawable.ic_chat_default,R.drawable.ic_shop_default
			,R.drawable.ic_settings_default};
	/**
	 * tab有焦点时的图片
	 */
	private int[] tabImg_id_p = {R.drawable.ic_temperature_selected,R.drawable.ic_chat_selected,R.drawable.ic_shop_selected
			,R.drawable.ic_settings_selected};
	/**
	 * tab中的imageView
	 */
	private ImageView[] ivs;
	private TextView[] rtl;
	/**
	 * tab的焦点背景图
	 */
	private ImageView iv_tab_p;
	/**
	 * 当前页面的位置
	 */
	private int selectID = 0;
	/**
	 * 前一次页面的位置
	 */
	private int preSelectID = 0;
	/**
	 * tab的动画
	 */
	private AnimTab animation;
	/**
	 * 广播接收器
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Bluetooth3Receiver.ACTION_RECIEVE_NFCDATA)) {// 接收到NFC信息
				Log.d(TAG, "onReceive-----ACTION_RECIEVEDATA ");
				if (null != viewPager && null != iv_shortcut) {
					isFromNFC = true;
					teas_tab.performClick();
					isFromNFC = false;
				}
			}
			else  if (action.equals(EMChatManager.getInstance().getCmdMessageBroadcastAction())){//cmd(透传环信消息无法通过静态注册广播接收者收到消息)改为动态
				String msgId = intent.getStringExtra("msgid"); // 消息id
				Tools.printInfo(TAG,"msgID is "+msgId);
				EMMessage message = intent.getParcelableExtra("message");
				dealCMDMsgs(message);
			}
			else  if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo.State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
				NetworkInfo.State mobileState =null;
				if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null){//盒子端没有ConnectivityManager.TYPE_MOBILE这个属性
					mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
				}
				if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED != wifiState && NetworkInfo.State.CONNECTED == mobileState) {
					//数据流量
					Logger.e(TAG,"使用数据流量");
					netState = 1;
				} else if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED == wifiState && NetworkInfo.State.CONNECTED != mobileState) {
					//wifi
					Logger.e(TAG,"连上wifi");
					netState = 2;
				} else if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED != wifiState && NetworkInfo.State.CONNECTED != mobileState) {
					//断网
					Logger.e(TAG,"断网啦");
					netState = 3;
					Tools.showToast(MainActivity.this,MainActivity.this.getString(R.string.check_network));
				}
			}
			else if (action.equals(BluetoothConnectUtils.ACTION_BLUETOOTHSTATE)){
				int bluestate = intent.getIntExtra(BluetoothConnectUtils.KEY_BLUETOOTHSTATE, -1);
				if (bluestate == BluetoothConnectUtils.CONNECT_OK){
					OcupToast.makeText(MainActivity.this,
							getString(R.string.connect_succeed), Toast.LENGTH_SHORT).show();
					Logger.e(TAG,"bluestate = "+bluestate);
						getCupstatu();
					//连接上时先发送消息告知在线，获取温度的数据需要一段时间才能拿到
					String content = "#1#1#0";
					HuanxinUtil.getInstance().sendGroupMsg(MainActivity.this,content);
					//更新水杯在线状态
					Tools.savePreference(MainActivity.this,UtilContact.isAlived,"true");
				}
			}
			//与杯子蓝牙断开
			else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
				String content = "#1#0#0";
				HuanxinUtil.getInstance().sendGroupMsg(MainActivity.this,content);
				//更新水杯在线状态
				Tools.savePreference(MainActivity.this,UtilContact.isAlived,"false");
			}
			//收到水杯状态值
			else if (action.equals(BluetoothService.ACTION_RECEIVERSTATUS)){
				Logger.e(TAG,"BluetoothService.ACTION_RECEIVERSTATUS");
				mHandler.sendEmptyMessage(receiverCupStatus);
				hasReceiverStatus = true;
				//发送广播，通知其他页面更新UI
				Intent intent2 = new Intent(receiverCupStatusInfo);
				sendBroadcast(intent2);
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OcupApplication.getInstance().addActivity(this);
		Logger.e(TAG, "onCreate()-------the currentTime is "+Tools.getCurrentMinutes());
		setContentView(R.layout.activity_main);
		mContext = this;
		initView();
		initData();
		mHandler.sendEmptyMessageDelayed(startRequestUpdateInfo,500);
		mHandler.sendEmptyMessageDelayed(startGetGroup_id,1200);
		hasBindWindow = true;
		mHandler.sendEmptyMessageDelayed(startSendGroupMsg,2000);
		mHandler.sendEmptyMessageDelayed(startJudgeOnline,4000);
		mHandler.sendEmptyMessageDelayed(startRemindDrink,6000);
	}

	//请求apk最新信息
	public void requestUpdateInfo(){
		long time = System.currentTimeMillis();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		int dayAtWeek = calendar.get(Calendar.DAY_OF_WEEK);
		String flag = Tools.getPreference(MainActivity.this,UtilContact.ISREQUESTUPDATEINFO);
		Logger.e(TAG,"flag = "+flag);
		if (flag.equals("true")){
			OkHttpRequest.getApkInfo(MainActivity.this);
		}
		else if (flag == null || flag == ""){
			OkHttpRequest.getApkInfo(MainActivity.this);
		}
	}
	//根据本地cupid请求GroupID
	public void getGroupId(){
		HttpRequest.getInstance().getGroupID(MainActivity.this);
	}

	private void notifyNotification(int progress){
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.icon_192)
				.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_192))
				.setContentTitle(getString(R.string.app_name));
		NotificationManager notificationManager = (NotificationManager) MainActivity.this.getSystemService(NOTIFICATION_SERVICE);
		if(progress>0 && progress<=100){
			builder.setProgress(100,progress,false);
		}else{
			builder.setProgress(0, 0, false);
		}
		builder.setAutoCancel(true);
		builder.setWhen(System.currentTimeMillis());
		builder.setContentIntent(progress>=100 ? installAPK() :
				PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT));
		Notification notification = builder.build();
		notificationManager.notify(0, notification);
	}

	//安装应用界面
	private PendingIntent installAPK() {
		Log.e("tag", "getContentIntent()");
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.parse("file://"+apk.getAbsolutePath()),
				"application/vnd.android.package-archive");
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		startActivity(intent);
		return pendingIntent;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		Logger.e(TAG,"onWindowFocusChanged-----");
	}

	@Override
	protected void onStart() {
		super.onStart();
		// 监听环信连接状况
		EMChatManager emChatManager = EMChatManager.getInstance();
		emChatManager.addConnectionListener(this);
		loginHuanxin();

		Logger.e(TAG, "onStart()-----");
		IntentFilter filter = new IntentFilter(
				BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(Bluetooth3Receiver.ACTION_RECIEVE_NFCDATA);
		filter.addAction(BluetoothConnectUtils.ACTION_BLUETOOTHSTATE);

		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(BluetoothConnectUtils.ACTION_BLUETOOTHSTATE);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(BluetoothService.ACTION_RECEIVERSTATUS);

		filter.addAction(EMChatManager.getInstance().getCmdMessageBroadcastAction());
		Tools.printInfo(TAG,EMChatManager.getInstance().getCmdMessageBroadcastAction());
		this.registerReceiver(receiver, filter);

		BluetoothConnectUtils.getInstance().isRunFront = true;
		mHomeKeyReceiver.startWatch();
		// 启动泡茶服务器
		Intent intent_serviceTea = new Intent();
		intent_serviceTea.setClass(MainActivity.this, TeaService.class);
		MainActivity.this.startService(intent_serviceTea);
	}

	public static void loginHuanxin(){
		//登录环信：环信id注册完之后就已经保存到本地
		final String huanxinID = Tools.getPreference(mContext, UtilContact.HuanXinId);
		final String huanxinPwd = Tools.getPreference(mContext,UtilContact.HuanXinPWD);
		new Thread(new Runnable() {
			@Override
			public void run() {
				HuanxinUtil.getInstance().login(mContext, huanxinID, huanxinPwd);
			}
		}).start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Logger.e(TAG,"onResume------");
		Intent intent = getIntent();
		isFromChat = intent.getBooleanExtra("fromChat", false);
		if (isFromChat) {
			tips_tab.performClick();
			isFromChat = false;
		}
	}

	//解决sigleTask下的getIntent只获取最原始intent的问题
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop()-----");
		this.unregisterReceiver(receiver);
		mHomeKeyReceiver.stopWatch();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Logger.e(TAG,"ondestroy is coming");
		BluetoothConnectUtils.getInstance().isRunFront = false;
		OcupApplication.getInstance().isFirstReadCupInfo = true;
		// 保存http单例的信息
		HttpRequest.getInstance().saveStatues();
		//如果水杯是在线的状态应用退出发送蓝牙离线群消息
		new Thread(new Runnable() {
			@Override
			public void run() {
				if ("true".equals(Tools.getPreference(MainActivity.this,UtilContact.isAlived))){
					String content = "#1#0#0";
					Logger.e(TAG,"应用退出前发送下线消息到其他好友");
					HuanxinUtil.getInstance().sendGroupMsg(MainActivity.this,content);
					Tools.savePreference(MainActivity.this,UtilContact.isAlived,"false");
					Tools.savePreference(MainActivity.this,UtilContact.LASTEXITSTATUS,"true");
				}
			}
		}).start();
		hasBindWindow = false;
//		OcupApplication.getInstance().exit();
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		mScreenWidth = getScreenWH(MainActivity.this)[0];
		mScreenHeight = getScreenWH(MainActivity.this)[1];

		iv_user = (ImageView) findViewById(R.id.iv_user);
		iv_tips = (ImageView) findViewById(R.id.iv_tip);
		iv_shortcut = (ImageView) findViewById(R.id.iv_shortcut);
		iv_heart = (ImageView) findViewById(R.id.iv_heart);

		one_tab = (TextView) findViewById(R.id.iv_user_text);
		two_tab = (TextView) findViewById(R.id.iv_tip_text);
		three_tab = (TextView) findViewById(R.id.iv_shortcut_text);
		four_tab = (TextView) findViewById(R.id.iv_heart_text);

		data_tab = (RelativeLayout) findViewById(R.id.one_tab);
		tips_tab = (RelativeLayout) findViewById(R.id.two_tab);
		teas_tab = (RelativeLayout) findViewById(R.id.three_tab);
		heart_tab = (RelativeLayout) findViewById(R.id.four_tab);

		ivs = new ImageView[] { iv_user, iv_tips, iv_shortcut, iv_heart };

		rtl = new TextView[] { one_tab, two_tab, three_tab, four_tab };
		viewPager = (ViewPager) findViewById(R.id.viewPager);
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		String huanXin_Id = Tools.getPreference(this,UtilContact.HuanXinId);
		String huanXin_Pwd = Tools.getPreference(this,UtilContact.HuanXinPWD);
		Logger.e(TAG,huanXin_Id);
		OcupApplication.getInstance().mOwnCup.setHuanxin_userid(huanXin_Id);
		OcupApplication.getInstance().mOwnCup.setHuanxin_pwd(huanXin_Pwd);

		data_tab.setOnClickListener(mOnClickListener);
		tips_tab.setOnClickListener(mOnClickListener);
		teas_tab.setOnClickListener(mOnClickListener);
		heart_tab.setOnClickListener(mOnClickListener);

				viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(4);
		viewPager.setOnPageChangeListener(changeListener);
		CupPara.getInstance().setGotCupPara(false);
		mHomeKeyReceiver = new HomeWatcher(this);
		mHomeKeyReceiver.setOnHomePressedListener(this);

		data_tab.performClick();
		//从蓝牙连接跳转到这个页面的参数
		Intent intent = getIntent();
		boolean bluetoothConnected_ok = intent.getBooleanExtra("backfromBluetooth",false);
		if (bluetoothConnected_ok || BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED){
			Tools.savePreference(MainActivity.this,UtilContact.isAlived,"true");
			//更新oChatFragment的UI
			Intent intent1 = new Intent(receiverCupStatusInfo);
			sendBroadcast(intent1);
			//发送群消息
			String content = "#1#1#"+CupStatus.getInstance().getCur_water_temp();
			HuanxinUtil.getInstance().sendGroupMsg(MainActivity.this,content);
			BlueToothRequest.getInstance().sendMsg2getCupStatus(MainActivity.this);
		}
		else{
			//上次退出前是在线状态，进来之后蓝牙并没有连接，更新UI
			Tools.savePreference(MainActivity.this,UtilContact.isAlived,"false");
			Intent intent1 = new Intent(receiverCupStatusInfo);
			sendBroadcast(intent1);
		}
	}

	//用来获取水杯实时温度,隔时发送一次请求
	private void getCupstatu(){
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
					if (!sendCallBackSuccess || !hasReceiverStatus){
						Logger.e(TAG,"getCupstatus start");
						BlueToothRequest.getInstance().sendMsg2getCupStatus(MainActivity.this);
					}
					else {
						return;
					}
				}
				try {
					Thread.sleep(DURATOIN_GETCUPSTATUS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(runnable).start();
	}

	/**
	 * 成员变量：监听点击事件
	 */
	private OnClickListener mOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
				// 切换到user页面
				case R.id.one_tab:
					if (selectID == 0) {
						return;
					} else {
						viewPager.setCurrentItem(0);
					}
					break;

				// 切换到tip页面
				case R.id.two_tab:
					if (selectID == 1) {
						return;
					} else {
						viewPager.setCurrentItem(1);
					}
					break;

				// 切换到shortcut页面
				case R.id.three_tab:
					if (selectID == 2) {
						return;
					} else {
						viewPager.setCurrentItem(2);
					}
					break;

				// 切换到heat页面
				case R.id.four_tab:
					if (selectID == 3) {
						return;
					} else {
						viewPager.setCurrentItem(3);
					}
					break;
			}
		}
	};

	/**
	 * 当前fragment改变时，修改tab选中项
	 *
	 * @param position
	 *            0 1 2 3 4
	 */
	private void setSelectedTab(int position) {
		Log.d(TAG, "setSelectedTab()-------position==" + position
				+ "  selectID=" + selectID);
//		animation.startAnim(selectID, position);
		preSelectID = selectID;
		selectID = position;
		for (int i = 0; i < ivs.length; i++) {
			if (i == selectID) {
				ivs[selectID].setImageResource(tabImg_id_p[selectID]);
				rtl[selectID].setTextColor(color_text_p);
			} else {
				ivs[i].setImageResource(tabImg_id[i]);
				rtl[i].setTextColor(color_text_n);
			}
		}
	}

	/**
	 * 成员变量：fragmentPager的适配器
	 */
	private FragmentPagerAdapter adapter = new FragmentPagerAdapter(
			getSupportFragmentManager()) {
		@Override
		public int getCount() {
			return ivs.length;
		}

		@Override
		public Fragment getItem(int position) {
			Log.d(TAG, "FragmentPagerAdapter position===" + position);
			Fragment fragment = null;
			switch (position) {
				case 0:
					fragment = new OmeFragment();
					Log.d(TAG, "FragmentPagerAdapter getItem new OmeFragment");
					break;
				case 2:
					fragment = new TipsFragment();
					Log.d(TAG, "FragmentPagerAdapter getItem new TipsFragment");
					break;
				case 4:
					fragment = new OteaFragment();
					Log.d(TAG, "FragmentPagerAdapter getItem new OteaFragment");
					break;
				case 1:
					fragment = new OchatFragment2();
					Log.d(TAG, "FragmentPagerAdapter getItem new OchatFragment");
					break;
				case 3:
					mSettingFragment = new SettingFragment();
					fragment = mSettingFragment;
					Log.d(TAG, "FragmentPagerAdapter getItem new SettingFragment");
					break;
			}
			return fragment;
		}
	};
	/**
	 * 成员变量：监听fragmentPager 的变化
	 */
	private SimpleOnPageChangeListener changeListener = new SimpleOnPageChangeListener() {
		public void onPageSelected(int position) {
			Log.d(TAG, "FragmentPagerAdapter onPageSelected  position==="
					+ position);
			if (position != selectID) {
				setSelectedTab(position);
			}
		}
	};

	/**
	 * 处理透传消息(透传只用在群消息)
	 */
	private void dealCMDMsgs(EMMessage message) {
		Tools.printInfo(TAG, "收到群聊信息");
		String content = null;
		String contact_id = "";
		if (message != null){
			 contact_id = message.getFrom();
		}
		CmdMessageBody messageBody = (CmdMessageBody) message.getBody();
			content = messageBody.action;
		Tools.printInfo(TAG,"content is "+content+"contact_id is"+contact_id);
		String[] status_String = content.split("#");//#1#1#36:切割出四个
		if (status_String.length == 4) {
			int[] status = new int[3];
			status[0] = Integer.parseInt(status_String[1]);
			status[1] = Integer.parseInt(status_String[2]);
			status[2] = Integer.parseInt(status_String[3]);
			if (null == dbMgr) {
				dbMgr = new DBManager(context);
			}
			dbMgr.updateFriendsStatus(contact_id, status, Tools.getCurrentMinutes());
			Intent intent_status = new Intent(HuanxinBroadcastReceiver.ReceiverGroupChat);
			intent_status.putExtra("contact_id", contact_id);
			context.sendBroadcast(intent_status);
		}
	}


	@Override
	public void onConnected() {
		Log.d(TAG, "onConnected------------------------------");
	}

	@Override
	public void onConnecting(String arg0) {
		Log.d(TAG, "onConnecting------------------------------");
	}

	@Override
	public void onDisConnected(String errorString) {
		Log.d(TAG, "onDisConnected------------------------------errorString=="
				+ errorString);
	}

	@Override
	public void onReConnected() {
		Log.d(TAG, "onReConnected------------------------------");
	}

	@Override
	public void onReConnecting() {
		Log.d(TAG, "onReConnecting------------------------------");
	}

	private int[] getScreenWH(Activity activity) {
		int wh[] = new int[2];
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		wh[0] = dm.widthPixels;
		wh[1] = dm.heightPixels;
		return wh;
	}

	private int color_text_p = Color.rgb(255, 129, 140);
	private int color_text_n = Color.rgb(89, 87, 87);

	@Override
	public void onHomePressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onHomeLongPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loginSuccess() {
		Logger.e("MainActivity","登录成功");
	}

	@Override
	public void loginFail(int type) {

	}

	@Override
	public void RegisterSuccess() {

	}

	@Override
	public void RegisterFail(int type) {

	}

	//apk信息获取和下载apk所需回调
	@Override
	public void downLoadSuccess() {

	}

	@Override
	public void downLoadProgress(int progress) {
		Message message = new Message();
		message.what = updateProgress;
		message.arg1 = progress;
		mHandler.sendMessage(message);
	}

	@Override
	public void downLoadFail() {

	}

	@Override
	public void getUpdateInfoSuccess(String downLoadPhth, String versionCode, String detail, String versionName) {
		long code = Long.parseLong(versionCode);
		Tools.printInfo(TAG,"version code is "+code);
		Message message = new Message();
		message.obj = versionCode;
		message.what = createDialog;
		mHandler.sendMessage(message);
		PackageManager pm = getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(getPackageName(),0);
			long localCode = info.versionCode;
			if (code > localCode){
				//未设置过不再提醒则对话框弹出
				if (!Tools.getPreference(MainActivity.this,UtilContact.ISREQUESTUPDATEINFO).equals("false")){
					mHandler.sendEmptyMessage(showDialog);
				}
			}
			File file5 = Environment.getExternalStorageDirectory();
			File file6 = null;
			if (Tools.getStorageState()){
				file6 = new File(file5,"/sen5/nhh");
				if (!file6.exists()){
					file6.mkdirs();
				}
			}
			downLoadUrl = downLoadPhth;
			apk = new File(file6,"nhh.apk");
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getUpdateInfoFail() {

	}

	@Override
	public void ok(int type) {
		if (type == showRemindDialog){
			mHandler.sendEmptyMessage(showRemindDialog);
		}
		else if (type == showSuspend){
			mHandler.sendEmptyMessage(showSuspend);
		}
		else{
			OkHttpRequest.getAPK(downLoadUrl,apk,MainActivity.this);
		}
	}

	@Override
	public void ok(int type, Object obj) {

	}

	@Override
	public void cancel(int type) {

	}

	@Override
	public void getCupStatus_OK() {
		sendCallBackSuccess = true;
		Logger.e(TAG,"getCupStatus_ok");
	}

	@Override
	public void getCupStatus_NO() {

	}
}
