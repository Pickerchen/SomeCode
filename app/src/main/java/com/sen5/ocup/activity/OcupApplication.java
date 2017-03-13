package com.sen5.ocup.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.easemob.chat.ConnectionListener;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMChatOptions;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.OnMessageNotifyListener;
import com.easemob.chat.OnNotificationClickListener;
import com.easemob.chat.TextMessageBody;
import com.facebook.stetho.Stetho;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.blutoothstruct.BluetoothType;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.struct.ChatMsgEntity;
import com.sen5.ocup.struct.CupInfo;
import com.sen5.ocup.util.BlueToothRequest;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.yili.FriendInfo;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cn.sharesdk.framework.ShareSDK;
import cn.smssdk.SMSSDK;

import static com.sen5.ocup.receiver.HuanxinBroadcastReceiver.ReceiverGroupChat;
import static com.sen5.ocup.service.TeaService.context;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 : Application,异常退出处理、环信注册、环信后台消息处理、环信掉线处理
 */
public class OcupApplication extends Application implements UncaughtExceptionHandler {
	private static final String TAG = "OcupApplication";
	public List<Activity> mList = new LinkedList<Activity>();
	private static OcupApplication instance;

	public CupInfo mOwnCup = new CupInfo();
	public CupInfo mOtherCup = new CupInfo();

	private String text;// 接收到的文本内容
	private int type;// 接收到的消息类型
	private EMChatOptions options;

	// 填写从短信SDK应用后台注册得到的APPKEY
	private static String APPKEY = "181ac90b900b9";
	// 填写从短信SDK应用后台注册得到的APPSECRET
	private static String APPSECRET = "482385718f777840e7e45bfbd645c8a3";

	//升级水杯apkdialog
	private CustomDialog customDialog;

	/**
	 * 记录是否首次读取杯子信息
	 */
	public boolean isFirstReadCupInfo = true;
	//接收到谁的信息
	private String fromUserId;
	//好友列表中的昵称
	private String fromWho;
	private DBManager mDBManager;

	public OcupApplication() {
		super();
		Log.d(TAG, "OcupApplication------------------");
		instance = this;
//		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	public synchronized static OcupApplication getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		//chrom调试
		Stetho.initialize(Stetho.newInitializerBuilder(this)
				.enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
				.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this)).build());
		Log.d(TAG, "onCreate------------------");
		instance = this;
		initHuanxin();
		initImageLoader(this);
		//腾讯bug抓取,只在释放到测试部的时候使用
		CrashReport.initCrashReport(this, "900059012", true);
		CrashReport.setUserId(this, "sen5测试部");

		//mob分享初始化
		ShareSDK.initSDK(this);

		//mob短信初始化
		SMSSDK.initSDK(this, APPKEY, APPSECRET);
		mDBManager = new DBManager(this);
	}

	//初始化imageLoader
	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024) // 50 Mb
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	/**
	 * 初始化环信
	 */
	private void initHuanxin() {
		int pid = android.os.Process.myPid();
		String processAppName = getAppName(pid);
		// 如果使用到百度地图或者类似启动remote service的第三方库，这个if判断不能少
		if (processAppName == null || processAppName.equals("")) {
			Log.d(TAG, "onCreate---------processAppName == null || processAppName.equals()");
			return;
		}
		// 关闭环信自动登录功能
		EMChat.getInstance().setAutoLogin(false);
		// 初始化环信聊天SDK	
		EMChat.getInstance().setDebugMode(true);
		EMChat.getInstance().init(instance);
		EMChat.getInstance().setAppInited();
//		boolean loggedIn = EMChat.getInstance().isLoggedIn();
		Log.d(TAG, "initialize EMChat SDK");
		// 获取到EMChatOptions对象
		options = EMChatManager.getInstance().getChatOptions();
		options.setNotifyBySoundAndVibrate(false);
		// 设置notification消息点击时，跳转的intent为自定义的intent
		options.setOnNotificationClickListener(getNotificationClickListener());
		//设置后台监听
		options.setNotifyText(getMessageNotifyListener());

		// 设置一个connectionlistener监听账户重复登陆
		EMChatManager.getInstance().addConnectionListener(new MyConnectionListener());
	}

	// add Activity
	public void addActivity(Activity activity) {
		mList.add(activity);
	}

	// 关闭所有活动，退出整个应用
	public void exit() {
		try {
			for (Activity activity : mList) {
				if (activity != null)
					activity.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Log.d(TAG, "'exit------------------");
			System.gc();
		}
	}

	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
		Log.d(TAG, "'exit-----------onLowMemory-------");
	}

	// 程序异常退出前调用，清理资源用
	public void uncaughtException(Thread thread, Throwable ex) {
		if (null != ex) {
			Log.e(TAG, "application crash..." + ex.getMessage());
			Log.e(TAG, "application crash...ex.getCause()===" + ex.getCause());
			Log.e(TAG, "application crash...ex.getLocalizedMessage()===" + ex.getLocalizedMessage());
		} else {
			Log.e(TAG, "-----uncaughtException  ex == null");
		}

		// 保存http单例的信息
		HttpRequest.getInstance().saveStatues();

		// 关闭蓝牙通信
		Log.d(TAG, "handmsg---- 更换杯子----------closeBluetoothCommunication+ " + "BluetoothConnectUtils.getInstance().getBluetoothState() =="
				+ BluetoothConnectUtils.getInstance().getBluetoothState());
		BluetoothConnectUtils.getInstance().closeBluetoothCommunication();

		// 关闭环信
		if (null != EMChatManager.getInstance() && EMChatManager.getInstance().isConnected()) {//
			Log.d(TAG, "handmsg-------null != EMChatManager.getInstance()isConnected");
			try {
				EMChatManager.getInstance().logout();
			} catch (Exception e) {
				Log.d(TAG, "handmsg--EMChatManager.getInstance().logout();  e==" + e);
			}
		}
		OcupApplication.getInstance().exit();
		System.exit(0);
	}

	private boolean isGroup_msg = false;

	protected OnMessageNotifyListener getMessageNotifyListener() {
		//app在后台，有新消息来时，状态栏的消息提示换成自己写的
		final OnMessageNotifyListener listener = new OnMessageNotifyListener() {
			@Override
			public String onNewMessageNotify(EMMessage message) {
				if (message.getChatType().equals(ChatType.GroupChat)) {
					isGroup_msg = true;
					options.setShowNotificationInBackgroud(false);
//					options.setNotificationEnable(false);
					Logger.e(TAG, "收到群聊信息");
					String content = null;
					String contact_id = message.getFrom();
					try {
						Logger.e(TAG, message.getBody().toString());
						String jsonString = message.getBody().toString().substring(5);
						JSONObject jsonObject = new JSONObject(jsonString);
						content = jsonObject.getString("content");
						Logger.e(TAG, "content is" + content);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					String[] status_String = content.split("#");//#1#1#36:切割出四个
					int[] status = new int[3];
					status[0] = Integer.parseInt(status_String[1]);
					status[1] = Integer.parseInt(status_String[2]);
					status[2] = Integer.parseInt(status_String[3]);
					if (null == mDBManager) {
						mDBManager = new DBManager(context);
					}
					mDBManager.updateFriendsStatus(contact_id, status, Tools.getCurrentMinutes());
					Intent intent_status = new Intent(ReceiverGroupChat);
					intent_status.putExtra("contact_id", contact_id);
					context.sendBroadcast(intent_status);
					return null;
				} else {
//					options.setNotificationEnable(true);
					options.setShowNotificationInBackgroud(true);
					isGroup_msg = false;
				}
				Logger.e(TAG, "onNewMessageNotify--------------------getMessageNotifyListener-----");
				dealMsg(message);
				// 设置状态栏的消息提示，可以根据message的类型做相应提示
				String ticker = getMessageDigest(message, getApplicationContext());
				if (type == ChatMsgEntity.TYPE_TXT || type == ChatMsgEntity.TYPE_ANIM_FACE) {
					ticker = text;
				} else if (type == ChatMsgEntity.TYPE_SCRAWL_ANIM) {
					ticker = getApplicationContext().getString(R.string.scrawlanim);
				} else if (type == ChatMsgEntity.TYPE_SCRAWL) {
					ticker = getApplicationContext().getString(R.string.title_scrawl);
				} else if (type == ChatMsgEntity.TYPE_ADDED) {
					ticker = getApplicationContext().getString(R.string.added_ok);
				} else if (type == ChatMsgEntity.TYPE_DEMATED) {
					ticker = getApplicationContext().getString(R.string.demated_ok);
				}
				List<FriendInfo> friendInfos = mDBManager.queryFriends();
				if (friendInfos != null && friendInfos.size() != 0) {
					for (FriendInfo friend : friendInfos) {
						if (friend.getContact_id().equals(fromUserId)) {
							fromWho = friend.getNickname();
							Logger.e(TAG, "fromWho = " + fromWho);
						}
					}
				} else {
					fromWho = "";
				}
				String msg = fromWho + ": " + ticker;
				Logger.e(TAG, "msg = " + msg);
				return msg;
			}

			@Override
			public String onLatestMessageNotify(EMMessage message, int fromUsersNum, int messageNum) {
				return null;
			}

			@Override
			public String onSetNotificationTitle(EMMessage message) {
				Logger.e(TAG, "onSetNotificationTitle--------------------");
				if (message.getChatType() == ChatType.GroupChat) {
					return null;
				}
				// 修改标题
				String ticker = getMessageDigest(message, getApplicationContext());
				if (type == ChatMsgEntity.TYPE_TXT || type == ChatMsgEntity.TYPE_ANIM_FACE) {
					Logger.e(TAG, "type =" + type);
					ticker = text;
				} else if (type == ChatMsgEntity.TYPE_SCRAWL_ANIM) {
					ticker = getApplicationContext().getString(R.string.scrawlanim);
				} else if (type == ChatMsgEntity.TYPE_SCRAWL) {
					ticker = getApplicationContext().getString(R.string.title_scrawl);
				} else if (type == ChatMsgEntity.TYPE_ADDED) {
					ticker = getApplicationContext().getString(R.string.added_ok);
				} else if (type == ChatMsgEntity.TYPE_DEMATED) {
					ticker = getApplicationContext().getString(R.string.demated_ok);
				}

				String msg = fromWho + ": " + ticker;
				return msg;
			}
		};
		return listener;
	}
//		if (isGroup_msg){
//			Logger.e(TAG,"isGroup_msg");
//			options.setNotificationEnable(false);
//			options.setShowNotificationInBackgroud(false);
//			return null;
//		}
//		else {
//			Logger.e(TAG,"isnotGroup_id");
//			options.setNotificationEnable(true);
//			options.setShowNotificationInBackgroud(true);
//			return listener;
//		}


	protected OnNotificationClickListener getNotificationClickListener() {
		return new OnNotificationClickListener() {
			@Override
			public Intent onNotificationClick(EMMessage message) {
				Log.d(TAG, "OnNotificationClickListener--------------------");
				Intent intent = null;
				ChatType chatType = message.getChatType();
				if (chatType == ChatType.Chat) { // 单聊信息
					 intent = new Intent(getApplicationContext(), MainActivity.class);
					intent.putExtra("fromChat", true);
					intent.putExtra("userId", message.getFrom());
				} else { // 群聊信息
				}
				return intent;
			}
		};
	}

	/**
	 * 根据消息内容和消息类型获取消息内容提示
	 * 
	 * @param message
	 * @param context
	 * @return
	 */
	public static String getMessageDigest(EMMessage message, Context context) {
		String digest = "";
		switch (message.getType()) {
		case LOCATION: // 位置消息
			if (message.direct == EMMessage.Direct.RECEIVE) {
				// 从sdk中提到了ui中，使用更简单不犯错的获取string方法
				digest = getStrng(context, R.string.huanxin_location_recv);
				digest = String.format(digest, message.getFrom());
				return digest;
			} else {
				digest = getStrng(context, R.string.huanxin_location_prefix);
			}
			break;
		case IMAGE: // 图片消息
			digest = getStrng(context, R.string.huanxin_picture);
			break;
		case VOICE:// 语音消息
			digest = getStrng(context, R.string.huanxin_voice);
			break;
		case VIDEO: // 视频消息
			digest = getStrng(context, R.string.huanxin_video);
			break;
		case TXT: // 文本消息
			if (!message.getBooleanAttribute("is_voice_call", false)) {
				TextMessageBody txtBody = (TextMessageBody) message.getBody();
				digest = txtBody.getMessage();
			} else {
				TextMessageBody txtBody = (TextMessageBody) message.getBody();
				digest = getStrng(context, R.string.huanxin_voice_call) + txtBody.getMessage();
			}
			break;
		case FILE: // 普通文件消息
			digest = getStrng(context, R.string.huanxin_file);
			break;
		default:
			System.err.println("error, unknow type");
			return "";
		}

		return digest;
	}

	static String getStrng(Context context, int resId) {
		return context.getResources().getString(resId);
	}

	/**
	 * 处理后台接收到的消息
	 * @param message
	 */
	private void dealMsg(EMMessage message) {
		if (message.getChatType() == ChatType.GroupChat){//如果是群聊的消息，群聊信息是用来作为状态值标识的，所以不需要加入到数据库中

			return;
		}
		fromUserId = message.getFrom();
		Log.d(TAG, "new messagefrom:" + message.getFrom() + " type:" + message.getType() + " body:" + message.getBody());
		switch (message.getType()) {
		case TXT:
			TextMessageBody txtBody = (TextMessageBody) message.getBody();
			String content = txtBody.getMessage();
			Logger.e(TAG, "text message from:" + message.getFrom() + " text:" + txtBody.getMessage());
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(content);
				text = jsonObject.getString("content");
				type = jsonObject.getInt("contentType");
				Logger.e(TAG, "text====" + text + "  type==" + type);

				// 发送至cup
				ChatMsgEntity chatMsgEntity = ChatMsgEntity.createChatMsgEntity(message.getFrom(), mOwnCup.getHuanxin_userid(), text, message.getMsgTime() + "", type,
						ChatMsgEntity.FROM_OTHER);
				chatMsgEntity.setStatus(3);// 0-----正在发送 1----已查看消息//
											// 2------未发送消息 3----离线消息
				if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
					if (text.equals(ChatActivity.KEY_SHAKE)) {
						//震动信息则发送震动指令
						BlueToothRequest.getInstance().sendMsg2ControlCup(null, BluetoothType.control_shake);
					} else {
						//发送信息到杯子
						BlueToothRequest.getInstance().sendMsg2LED(chatMsgEntity, 2);
					}
				}

				// 写入数据库
				// 收到：被添加为好友or被解除好友关系
				if (type != ChatMsgEntity.TYPE_ADDED && type != ChatMsgEntity.TYPE_DEMATED) {
					new DBManager(this).addChat(chatMsgEntity);
				} else {
					if (type == ChatMsgEntity.TYPE_ADDED) {
						// 添加到数据库
						new DBManager(this).addCup_mate(mOwnCup.getHuanxin_userid(), message.getFrom());
					} else if (type == ChatMsgEntity.TYPE_DEMATED) {
						// 从数据库删除相应记录
						new DBManager(this).deleteCup_mate(mOwnCup.getHuanxin_userid());
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}

	class MyConnectionListener implements ConnectionListener {
		@Override
		public void onReConnecting() {
			Log.d(TAG, "onDisConnected--------");
		}

		@Override
		public void onReConnected() {
			Log.d(TAG, "onReConnected--------");
		}

		@Override
		public void onDisConnected(String errorString) {
			Log.d(TAG, "onDisConnected------------------------------errorString==" + errorString);
			if (errorString != null && errorString.contains("conflict")) {
				OcupToast.makeText(getApplicationContext(), getString(R.string.login_in_other), Toast.LENGTH_SHORT).show();
				// 先调用sdk logout，在清理app中自己的数据
				EMChatManager.getInstance().logout();
			} else if (errorString != null && errorString.contains("connection is disconnected")) {
				// 进入应用会先接收到此消息，此处在有网络的情况下也提示连接不上服务器，不排除是环信sdk问题
//				OcupToast.makeText(getApplicationContext(), getString(R.string.disconnect_chat_service), Toast.LENGTH_SHORT).show();
			} else {
				// "连接不到聊天服务器"
			}
		}

		@Override
		public void onConnecting(String progress) {
			Log.d(TAG, "onConnecting--------");
		}

		@Override
		public void onConnected() {
			Log.d(TAG, "onConnected--------");
		}
	}

	private String getAppName(int pID) {
		String processName = null;
		ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
		List l = am.getRunningAppProcesses();
		Iterator i = l.iterator();
		PackageManager pm = this.getPackageManager();
		while (i.hasNext()) {
			ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
			try {
				if (info.pid == pID) {
					CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
					processName = info.processName;
					return processName;
				}
			} catch (Exception e) {
			}
		}
		return processName;
	}

}