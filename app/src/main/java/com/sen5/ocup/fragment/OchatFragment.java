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
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.sen5.ocup.R;
import com.sen5.ocup.activity.ChatActivity;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.adapter.FriendsListAdapter;
import com.sen5.ocup.blutoothstruct.CupStatus;
import com.sen5.ocup.callback.CustomInterface.IDialog;
import com.sen5.ocup.callback.CustomInterface.IReceiveChat;
import com.sen5.ocup.callback.RequestCallback.DemateCupCallback;
import com.sen5.ocup.callback.RequestCallback.GetCupInfoCallback;
import com.sen5.ocup.callback.RequestCallback.GetMateDrink;
import com.sen5.ocup.callback.RequestCallback.GetRelationshipCallback;
import com.sen5.ocup.callback.RequestCallback.UpdateUserInfoCallback;
import com.sen5.ocup.callback.RequestCallback.mateCupCallback;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.gui.MyListViewPullDownAndUp;
import com.sen5.ocup.gui.MyListViewPullDownAndUp.RefreshListener;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.receiver.HuanxinBroadcastReceiver;
import com.sen5.ocup.struct.ChatMsgEntity;
import com.sen5.ocup.struct.FriendData;
import com.sen5.ocup.util.BlueToothRequest;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.FaceConversionUtil;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;
import com.sen5.ocup.zxing.CaptureActivity;

import java.util.ArrayList;

public class OchatFragment extends Fragment implements Callback, GetRelationshipCallback, mateCupCallback, IReceiveChat, GetCupInfoCallback, IDialog, UpdateUserInfoCallback,
		GetMateDrink, DemateCupCallback {

	private FrameLayout mFrameLayout;
	
	private static final String TAG = "HeartFragment";
	public static boolean isVisible;
	private View mView;
	private Activity mActivity;
	private Handler mHandler;
	private HttpRequest mRequest;
	private DBManager dbMgr;

	private MyListViewPullDownAndUp mListviewFriend;
	private FriendsListAdapter mAdapterFriend;
	public static ArrayList<FriendData> mDataFriend = new ArrayList<FriendData>();

	private View emptyView = null; // listview 无数据显示时显示该view
	private RelativeLayout layout_empty = null; // listview 无数据显示时显示该view
	private ImageView iv_addFri;
	// 记录上次打开该页面时的huanxinID,不同时要重新刷新用户信息
	private String huanxinID = "";

	private ProgressBar pb_bluetooth_connecting;// 表示蓝牙正在连接
	private ImageView iv_bluetooth_state;// 表示蓝牙连接状态

	private int count_unreadMsg;

	private int cur_waterYield;
	private int goalsWater = 1500;
	private int progress;
	private CustomDialog mDailog_editmood;
	private String newMood;// 修改后的心情，不一定修改成功
	/**
	 * 对方的信息
	 */
	private FriendData mDataOther;
	
	//实时更新蓝牙状态
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "onReceive---------------------action===" + action);
			if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
//				mHandler.sendEmptyMessage(CONNECTBLUETOOTH_OK);
			} else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
				if (null != iv_bluetooth_state) {
					mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO);
				}
			} else if (action.equals(HuanxinBroadcastReceiver.ACTION_DISCONNECTED)) {
				String errorString = intent.getStringExtra("errorString");
				Log.d(TAG, "ACTION_DISCONNECTED    errorString==" + errorString);
				// if (getString(R.string.login_in_other).equals(errorString)) {
				mHandler.sendEmptyMessage(NOT_LOGIN);
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

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		isVisible = isVisibleToUser;
		Log.d(TAG, "setUserVisibleHint---------isVisibleToUser==" + isVisibleToUser);
		if (isVisibleToUser) {
			setBluetoothState();
			setAddFriendState();
			if (null != dbMgr) {
				count_unreadMsg = dbMgr.countUnreadMsg(OcupApplication.getInstance().mOtherCup.getHuanxin_userid());
				Log.d(TAG, "setUserVisibleHint)-------count_unreadMsg==" + count_unreadMsg);
			}
			// 是否需要从网络获取用户信息
			if (OcupApplication.getInstance().mOwnCup.getHuanxin_userid() != null) {
				if (!OcupApplication.getInstance().mOwnCup.getHuanxin_userid().equals(huanxinID) || OcupApplication.getInstance().mOwnCup.getHuanxin_userid().equals("")) {
					huanxinID = OcupApplication.getInstance().mOwnCup.getHuanxin_userid();
					if (null == mRequest) {
						mRequest = HttpRequest.getInstance();
					}
//					mRequest.getUserInfo(mActivity, OchatFragment.this);
				}
			}

			if (null != mDataFriend && null != mAdapterFriend) {
				Log.d(TAG, "setUserVisibleHint)----redraw progress===" + mDataFriend.size());
				if (mDataFriend.size() > 1) {
					mDataFriend.get(1).setCount_offline_msg(count_unreadMsg);
				}
				if (mDataFriend.size() > 0) {
					// 饮水进度
					getCurWaterYield();
					progress = (cur_waterYield * 100)
							/ (OcupApplication.getInstance().mOwnCup.getIntakegoal() == 0 ? goalsWater : OcupApplication.getInstance().mOwnCup.getIntakegoal());
					if (progress > 100) {
						progress = 100;
					}
					mDataFriend.get(0).setProgress(progress);
					// 名字
					if (null != OcupApplication.getInstance().mOwnCup.getName()) {
						mDataFriend.get(0).setName(OcupApplication.getInstance().mOwnCup.getName());
					}
					// 心情
					if (null != OcupApplication.getInstance().mOwnCup.getMood()) {
						mDataFriend.get(0).setMood(OcupApplication.getInstance().mOwnCup.getMood());
					}
				}
				mAdapterFriend.notifyDataSetChanged();
			}
		}
	}

	/**
	 * 设置添加好友图标状态：按钮复用，有好友时则为删除好友功能
	 */
	private void setAddFriendState() {
		if (iv_addFri!=null) {
			if (null != getFriendsIDInDB()) {
				iv_addFri.setImageResource(R.drawable.selector_deleter_friend);
			} else {
				iv_addFri.setImageResource(R.drawable.selector_add_friend);
			}
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
			}else{
				pb_bluetooth_connecting.setVisibility(View.GONE);
				iv_bluetooth_state.setVisibility(View.VISIBLE);
				iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_inactive);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView---------");
		if (mView == null) {
			mActivity = getActivity();
			mView = inflater.inflate(R.layout.fragment_chat, container, false);
			initialComponent();
			initData();
		} else {
			// mView判断是否已经被加过parent，如果没删除，会发生mView已有parent的错误
			ViewGroup parent = (ViewGroup) mView.getParent();
			if (parent != null) {
				parent.removeView(mView);
			}
		}

		return mView;
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume---------");
		super.onResume();
		// 设置环信消息接收回调
		HuanxinBroadcastReceiver.setCallback(this);
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart---------");
		super.onStart();
		// 注册监听环信连接断开的广播
		IntentFilter filter = new IntentFilter(HuanxinBroadcastReceiver.ACTION_DISCONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(BluetoothConnectUtils.ACTION_BLUETOOTHSTATE);
		mActivity.registerReceiver(receiver, filter);

		if (null != dbMgr) {
			count_unreadMsg = dbMgr.countUnreadMsg(OcupApplication.getInstance().mOtherCup.getHuanxin_userid());
			Log.d(TAG, "onStart)-------count_unreadMsg==" + count_unreadMsg);
		}
		if (mDataFriend.size() > 1) {
			mDataFriend.get(1).setCount_offline_msg(count_unreadMsg);
		}
		mAdapterFriend.notifyDataSetChanged();

		setBluetoothState();
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause---------");
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop---------");
		super.onStop();
		mActivity.unregisterReceiver(receiver);
	}

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView---------");
		super.onDestroyView();
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		Log.d(TAG, "initData()---------");
		// 另起线程加载表情
		new Thread(new Runnable() {
			@Override
			public void run() {
				FaceConversionUtil.getInstace().getFileText(mActivity.getApplication());
			}
		}).start();

		mHandler = new Handler(this);
		mRequest = HttpRequest.getInstance();
		dbMgr = new DBManager(mActivity);
		setAddFriendState();

		iv_addFri.setOnClickListener(mOnClickListener);
		iv_bluetooth_state.setOnClickListener(mOnClickListener);
		layout_empty.setOnClickListener(mOnClickListener);

		mDataFriend.clear();
		getCurWaterYield();
		progress = (cur_waterYield * 100) / (OcupApplication.getInstance().mOwnCup.getIntakegoal() == 0 ? goalsWater : OcupApplication.getInstance().mOwnCup.getIntakegoal());
		if (progress > 100) {
			progress = 100;
		}
		// FriendData data = new FriendData();

		mDataFriend
				.add(new FriendData(OcupApplication.getInstance().mOwnCup.getHuanxin_userid(), OcupApplication.getInstance().mOwnCup.getName(), progress, (OcupApplication
						.getInstance().mOwnCup.getIntakegoal() == 0 ? goalsWater : OcupApplication.getInstance().mOwnCup.getIntakegoal()), OcupApplication.getInstance().mOwnCup
						.getMood()));
		mAdapterFriend = new FriendsListAdapter(mActivity, mActivity, this, mDataFriend, mListviewFriend, mDailog_editmood);
		mListviewFriend.setAdapter(mAdapterFriend);

		mListviewFriend.setOnItemClickListener(mOnItemClickListener);
		mListviewFriend.setCanPullUp(true);
		mListviewFriend.setCanPullDown(true);
		mListviewFriend.setRefreshListener(new RefreshListener() {

			@Override
			public void pullUpRefresh() {
				mListviewFriend.onPullupRefreshComplete();
			}

			@Override
			public void pullDownRefresh() {
				// 下拉结束，更新listview显示信息（隐藏下拉箭头等。。。）
				Log.d(TAG, "pullDownRefresh----------------");
//				mRequest.getUserInfo(mActivity, OchatFragment.this);
			}

			@Override
			public void pullUpStart() {

			}
		});
	}

	/**
	 * 初始化控件
	 */
	private void initialComponent() {
		mFrameLayout = (FrameLayout) mView.findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(mFrameLayout,mActivity);
		mDailog_editmood = new CustomDialog(mActivity, this, R.style.custom_dialog, CustomDialog.EDITMOOD_DIALOG, OcupApplication.getInstance().mOwnCup.getMood());
		iv_bluetooth_state = (ImageView) mView.findViewById(R.id.iv_bluetooth_state);
		pb_bluetooth_connecting = (ProgressBar) mView.findViewById(R.id.pb_bluetooth_connecting);
		iv_addFri = (ImageView) mView.findViewById(R.id.iv_addfriend);
		mListviewFriend = (MyListViewPullDownAndUp) mView.findViewById(R.id.listview_friendlist);
		emptyView = LayoutInflater.from(mActivity).inflate(R.layout.view_empty, null);
		layout_empty = (RelativeLayout) emptyView.findViewById(R.id.layout_emptyview);
		ViewGroup parentView = (ViewGroup) mListviewFriend.getParent();
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		parentView.addView(emptyView, 1, params);// 你需要在这儿设置正确的位置，以达到你需要的效果
		mListviewFriend.setEmptyView(emptyView);
	}

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Log.d(TAG, "onItemClick   position==" + position);
			int pos = position - 1;
			if (pos == 0) {// 和自己聊天
				Intent intent = new Intent(mActivity, ChatActivity.class);
				intent.putExtra("IsMe", true);
				intent.putExtra("userName", mDataFriend.get(pos).getName());
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				OchatFragment.this.startActivityForResult(intent, Tools.CHAT_GREQUEST_CODE);
			} else if (pos == 1) {// 和对方聊天
				Intent intent = new Intent(mActivity, ChatActivity.class);
				intent.putExtra("IsMe", false);
				if (mDataFriend.size() > 1) {
					intent.putExtra("userName", mDataFriend.get(pos).getName());
				} else {
					return;
				}
				if (mDataFriend.size() > 1) {
					intent.putExtra("to_huanxinID", mDataFriend.get(pos).getHuanxin_id());
				} else {
					return;
				}
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				OchatFragment.this.startActivityForResult(intent, Tools.CHAT_GREQUEST_CODE);
				// }
			}
		}
	};

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.iv_addfriend) {
				if (null != getFriendsIDInDB()) {// 删除好友
					Log.d(TAG, "onclick-------------------add friends      has friend already");
					// OcupToast.makeText(mActivity,
					// getString(R.string.have_friends), Toast.LENGTH_SHORT).show();
					CustomDialog mCustomDialog = new CustomDialog(mActivity, OchatFragment.this, R.style.custom_dialog, CustomDialog.DEL_FRIEND_DIALOG, null);
					mCustomDialog.show();
				} else {// 添加好友
					Log.d(TAG, "onclick-------------------add friends");
					Intent intent = new Intent(mActivity, CaptureActivity.class);
					intent.putExtra("cupid", OcupApplication.getInstance().mOwnCup.getCupID());
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivityForResult(intent, Tools.SCANNIN_GREQUEST_CODE);
				}
			} else if (v.getId() == R.id.iv_bluetooth_state) {
				Log.d(TAG, "onclick             iv_bluetooth_state");
//				if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED && BluetoothAdapter.getDefaultAdapter().isEnabled()) {
//					Log.d(TAG, "onclick             iv_bluetooth_state      socket.isConnected()");
//					return;
//				}
//				pb_bluetooth_connecting.setVisibility(View.VISIBLE);
//				iv_bluetooth_state.setVisibility(View.GONE);
//				BluetoothConnectUtils.getInstance().connect(OcupApplication.getInstance().mOwnCup.getBlueAdd());
			} else if (v.getId() == R.id.layout_emptyview) {// 获取数据
				Log.d(TAG, "onclick    refresh-----");
//				mRequest.getUserInfo(mActivity, OchatFragment.this);
			}
		}
	};

	/**
	 * 判断本地是否保存有配对杯子
	 * 
	 * @return
	 */
	private String getFriendsIDInDB() {
		String to_huanxinID = null;
		// 查询数据库
		if (null != OcupApplication.getInstance().mOwnCup.getHuanxin_userid() && null != dbMgr) {
			to_huanxinID = dbMgr.queryCup_mate(OcupApplication.getInstance().mOwnCup.getHuanxin_userid());
		}
		return to_huanxinID;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult-------requestCode==" + requestCode + "   resultCode==" + resultCode + "  (mDataFriend.size()==" + mDataFriend.size());
		isVisible = true;
		if (requestCode == Tools.CHAT_GREQUEST_CODE) {
			if (resultCode == mActivity.RESULT_OK) {
				Bundle bundle = data.getExtras();
				if (null != bundle) {
					boolean isme = bundle.getBoolean("Isme");
					if (isme) {
						if (null != dbMgr) {
							count_unreadMsg = dbMgr.countUnreadMsg(OcupApplication.getInstance().mOtherCup.getHuanxin_userid());
							Log.d(TAG, "setUserVisibleHint)-------count_unreadMsg==" + count_unreadMsg);
						}
					} else {
						count_unreadMsg = 0;
					}
					if (mDataFriend.size() == 2) {
						mDataFriend.get(1).setCount_offline_msg(count_unreadMsg);
						mAdapterFriend.notifyDataSetChanged();
					}
					String chatResult = bundle.getString("chat");
					Log.d(TAG, "onActivityResult-------chatResult==" + chatResult);
					if (null != chatResult && chatResult.equals("demate")) {
						iv_addFri.setImageResource(R.drawable.selector_add_friend);
						// 删除对应数据库记录
						dbMgr.deleteCup_mate(OcupApplication.getInstance().mOwnCup.getHuanxin_userid());
						if (mDataFriend.size() > 1) {
							mDataFriend.remove(1);
						}
						mAdapterFriend.notifyDataSetChanged();
					} else if (null != chatResult && chatResult.equals("unlogin")) {
						mHandler.sendEmptyMessage(NOT_LOGIN);
					}
				}
			}
		} else if (requestCode == Tools.SCANNIN_GREQUEST_CODE) {
			if (resultCode == mActivity.RESULT_OK) {
				Bundle bundle = data.getExtras();
				String result = bundle.getString("result");
				Log.d(TAG, "onActivityResult()---result== " + result);
				if (null != result) {
					if (result.equals("cancle")) {
						return;
					} else {
						mRequest.mateCups(mActivity, OchatFragment.this, bundle.getString("result"));
					}
				} else {
				}
			}
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (null != OchatFragment.this.getView()) {
			switch (msg.what) {
			// 有配对杯子
			case GETRELATIONSHIP_OK:
				Log.d(TAG, "handleMessage--GETRELATIONSHIP_OK--");
				mListviewFriend.onPulldownRefreshComplete();
				count_unreadMsg = dbMgr.countUnreadMsg(OcupApplication.getInstance().mOtherCup.getHuanxin_userid());
				if (mDataFriend.size() > 1) {
					mDataOther = mDataFriend.get(1);
					mDataFriend.remove(1);
				} else {
					mDataOther = new FriendData();
				}
				// } else {
				// mDataOther.setBmp(cup.getBmp_head());
				// }
				mDataOther.setCount_offline_msg(count_unreadMsg);
				mDataOther.setGoals(OcupApplication.getInstance().mOtherCup.getIntakegoal());
				mDataOther.setHuanxin_id(OcupApplication.getInstance().mOtherCup.getHuanxin_userid());
				mDataOther.setMood(OcupApplication.getInstance().mOtherCup.getMood());
				mDataOther.setName(OcupApplication.getInstance().mOtherCup.getName());
				mDataFriend.add(mDataOther);
				mAdapterFriend.notifyDataSetChanged();
				HttpRequest.getInstance().getMateDrink(mActivity, OchatFragment.this, System.currentTimeMillis() / 1000 - Tools.getCurSecond(), System.currentTimeMillis() / 1000);
				break;

			// 没有登录
			case GETRELATIONSHIP_NOTLOGIN:
				mListviewFriend.onPulldownRefreshComplete();
				Log.d(TAG, "handleMessage-----UPDATECUPIFO_NOTLOGIN    not login");
				updateview_nologin();
				break;
			// 获取杯子信息失败，提示设置网络
			case GETRELATIONSHIP_NO:
				mListviewFriend.onPulldownRefreshComplete();
				updateview_nologin();
				OcupToast.makeText(mActivity, getString(R.string.check_network), Toast.LENGTH_SHORT).show();
				// new CustomDialog(mActivity, null, R.style.custom_dialog,
				// CustomDialog.TOAST_DIALOG,
				// getString(R.string.check_network)).show();
				// ToastUtils.showToast(mActivity,
				// getString(R.string.check_network), 1000);
				break;

			// 当前杯子没有配对的杯子
			case GETRELATIONSHIP_OK_NOFRIENDS:
				mListviewFriend.onPulldownRefreshComplete();
				if (mDataFriend.size() > 1) {
					mDataFriend.remove(1);
					mAdapterFriend.notifyDataSetChanged();
				}
				// 删除数据库配对信息
				dbMgr.deleteCup_mate(OcupApplication.getInstance().mOwnCup.getHuanxin_userid());
				break;

			// 配对杯子成功
			case MATECUP_OK:
				OcupToast.makeText(mActivity, getString(R.string.add_friend_ok), Toast.LENGTH_SHORT).show();
				iv_addFri.setImageResource(R.drawable.selector_deleter_friend);
				mAdapterFriend.notifyDataSetChanged();
				break;
			case MATECUP_NO_NET:
				OcupToast.makeText(mActivity, getString(R.string.check_network), Toast.LENGTH_SHORT).show();
				// new CustomDialog(mActivity, null, R.style.custom_dialog,
				// CustomDialog.TOAST_DIALOG,
				// getString(R.string.check_network)).show();
				// ToastUtils.showToast(mActivity,
				// getString(R.string.check_network), 1000);
				break;
			case MATECUP_NO:
				String str = (String)msg.obj;
				OcupToast.makeText(mActivity, "" + str, Toast.LENGTH_SHORT).show();
				// new CustomDialog(mActivity, null, R.style.custom_dialog,
				// CustomDialog.TOAST_DIALOG,
				// getString(R.string.add_friends_err)).show();
				// ToastUtils.showToast(mActivity,
				// getString(R.string.add_friends_err), 1000);
				break;
			// 刷新界面
			case NOT_LOGIN:
				break;
			case CONNECTBLUETOOTH_OK:
				if (null != iv_bluetooth_state) {
					pb_bluetooth_connecting.setVisibility(View.GONE);
					iv_bluetooth_state.setVisibility(View.VISIBLE);
					iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_active);
				}
				break;
			case CONNECTBLUETOOTH_ING:
				pb_bluetooth_connecting.setVisibility(View.VISIBLE);
				iv_bluetooth_state.setVisibility(View.GONE);
				break;
			case CONNECTBLUETOOTH_NO:
				if (null != iv_bluetooth_state) {
					pb_bluetooth_connecting.setVisibility(View.GONE);
					iv_bluetooth_state.setVisibility(View.VISIBLE);
					iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_inactive);
				}
				break;
			case GETCUPINFO_OK:
				Log.d(TAG, "handmsg----------GETCUPINFO_OK  OwnerCupInfo.getInstance().getMood()==" + OcupApplication.getInstance().mOwnCup.getMood());
				mListviewFriend.onPulldownRefreshComplete();
				getCurWaterYield();
				progress = (cur_waterYield * 100)
						/ (OcupApplication.getInstance().mOwnCup.getIntakegoal() == 0 ? goalsWater : OcupApplication.getInstance().mOwnCup.getIntakegoal());
				if (progress > 100) {
					progress = 100;
				}
				if (mDataFriend.size() > 0) {
					mDataFriend.get(0).setHuanxin_id(OcupApplication.getInstance().mOwnCup.getHuanxin_userid());
					mDataFriend.get(0).setName(OcupApplication.getInstance().mOwnCup.getName());
					mDataFriend.get(0).setProgress(progress);
					mDataFriend.get(0).setGoals(OcupApplication.getInstance().mOwnCup.getIntakegoal());
					mDataFriend.get(0).setMood(OcupApplication.getInstance().mOwnCup.getMood());
				} else {
					mDataFriend.add(new FriendData(OcupApplication.getInstance().mOwnCup.getHuanxin_userid(), OcupApplication.getInstance().mOwnCup.getName(), progress,
							(OcupApplication.getInstance().mOwnCup.getIntakegoal() == 0 ? goalsWater : OcupApplication.getInstance().mOwnCup.getIntakegoal()), OcupApplication
									.getInstance().mOwnCup.getMood()));
				}
				if (mDataFriend.size() > 1) {
					mDataFriend.remove(1);
				}
				mAdapterFriend.notifyDataSetChanged();
				mRequest.getRelationship(mActivity, OchatFragment.this);
				break;
			case GETCUPINFO_NOTLOGIN:
				mListviewFriend.onPulldownRefreshComplete();
				updateview_nologin();
				break;
			case GETCUPINFO_NO:
				mListviewFriend.onPulldownRefreshComplete();
				updateview_nologin();
				break;
			case GETCUPINFO_ING:
				mListviewFriend.onPulldownRefreshComplete();
				mAdapterFriend.notifyDataSetChanged();
				break;
			// 编辑心情对话框确认后的处理
			case EDITMOOD_OK:
				newMood = (String) msg.obj;
				mRequest.updateUserInfo(mActivity, OchatFragment.this, OcupApplication.getInstance().mOwnCup.getName(), newMood);
				break;
			// 修改用户信息成功
			case UPDATE_USERINFO_OK:
				OcupApplication.getInstance().mOwnCup = new DBManager(OcupApplication.getInstance()).queryOwnCup(OcupApplication.getInstance().mOwnCup.getCupID());
				OcupApplication.getInstance().mOwnCup.setMood(newMood);
				new DBManager(OcupApplication.getInstance()).updateOwnCup(OcupApplication.getInstance().mOwnCup);
				OcupToast.makeText(mActivity, getString(R.string.modify_success), Toast.LENGTH_SHORT).show();
				if (mDataFriend.size() > 0) {
					mDataFriend.get(0).setMood(OcupApplication.getInstance().mOwnCup.getMood());
					mAdapterFriend.notifyDataSetChanged();
				}
				if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
					BlueToothRequest.getInstance().sendMood2Cup(OcupApplication.getInstance().mOwnCup.getMood());
				} else {
					OcupToast.makeText(mActivity, getString(R.string.unconnect_2cup), Toast.LENGTH_SHORT).show();
				}
				break;
			// 修改用户信息失败
			case UPDATE_USERINFO_NO:
				OcupToast.makeText(mActivity, getString(R.string.modify_failed), Toast.LENGTH_SHORT).show();
				break;
			// 获取好友饮水数据成功
			case GETMATEDRINK_OK:
				Log.d(TAG, "handmsg   GETMATEDRINK_OK  ");
				if (mDataFriend.size() > 1) {
					if (OcupApplication.getInstance().mOtherCup.getIntakegoal() > 0) {
						int pb = (OcupApplication.getInstance().mOtherCup.getIntake()) * 100 / OcupApplication.getInstance().mOtherCup.getIntakegoal();
						if (pb > 100) {
							pb = 100;
						}
						mDataFriend.get(1).setProgress(pb);
					}
					mDataFriend.get(1).setGoals(OcupApplication.getInstance().mOtherCup.getIntakegoal());
					mAdapterFriend.notifyDataSetChanged();
				}
				break;
			case GETMATEDRINK_NO:
				break;
			// 解除配对成功
			case DEMATE_OK:
				Log.d(TAG, "handmsg     DEMATE_OK");
				iv_addFri.setImageResource(R.drawable.selector_add_friend);
				OcupToast.makeText(mActivity, getString(R.string.del_cup_ok), Toast.LENGTH_SHORT).show();

				// 删除对应数据库记录
				dbMgr.deleteCup_mate(OcupApplication.getInstance().mOwnCup.getHuanxin_userid());
				if (mDataFriend.size() > 1) {
					mDataFriend.remove(1);
				}
				mAdapterFriend.notifyDataSetChanged();
				break;
			// 解除配对失败
			case DEMATE_NO:
				Log.d(TAG, "handmsg     DEMATE_NO");
				OcupToast.makeText(mActivity, getString(R.string.del_cup_no), Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
		return false;
	}

	/**
	 * 
	 * 获取当前杯子当天饮水总量
	 */
	private void getCurWaterYield() {
		if (CupStatus.getInstance().getTotal_water_yield() != 0) {
			cur_waterYield = CupStatus.getInstance().getTotal_water_yield();
		} else {
			if (null != dbMgr) {
				ArrayList<Integer> drinkdatass = dbMgr.queryDrinkData(Tools.datetomillion(Tools.getcurrenttime()));
				cur_waterYield = drinkdatass.get(drinkdatass.size() - 1);
			}
		}
	}

	/**
	 * 未登录时，界面显示
	 */
	private void updateview_nologin() {
		if (null != OcupApplication.getInstance().mOwnCup.getHuanxin_userid() && !OcupApplication.getInstance().mOwnCup.getHuanxin_userid().equals("")) {
			getCurWaterYield();
			progress = (cur_waterYield * 100) / (OcupApplication.getInstance().mOwnCup.getIntakegoal() == 0 ? goalsWater : OcupApplication.getInstance().mOwnCup.getIntakegoal());
			if (progress > 100) {
				progress = 100;
			}
			if (mDataFriend.size() > 0) {
				mDataFriend.get(0).setHuanxin_id(OcupApplication.getInstance().mOwnCup.getHuanxin_userid());
				mDataFriend.get(0).setName(OcupApplication.getInstance().mOwnCup.getName());
				mDataFriend.get(0).setProgress(progress);
				mDataFriend.get(0).setGoals(OcupApplication.getInstance().mOwnCup.getIntakegoal());
				mDataFriend.get(0).setMood(OcupApplication.getInstance().mOwnCup.getMood());

			} else {
				mDataFriend.add(new FriendData(OcupApplication.getInstance().mOwnCup.getHuanxin_userid(), OcupApplication.getInstance().mOwnCup.getName(), progress,
						(OcupApplication.getInstance().mOwnCup.getIntakegoal() == 0 ? goalsWater : OcupApplication.getInstance().mOwnCup.getIntakegoal()), OcupApplication
								.getInstance().mOwnCup.getMood()));
			}
			if (mDataFriend.size() > 1) {
				mDataFriend.remove(1);
			}

			// 检查数据库中是否有该杯子的配对杯子
			String to_huanxinID = getFriendsIDInDB();
			if (null != to_huanxinID) {
				OcupApplication.getInstance().mOtherCup = dbMgr.queryOtherCup(to_huanxinID);
				mDataOther = new FriendData();
				mDataOther.setCount_offline_msg(count_unreadMsg);
				mDataOther.setGoals(OcupApplication.getInstance().mOtherCup.getIntakegoal());
				mDataOther.setHuanxin_id(OcupApplication.getInstance().mOtherCup.getHuanxin_userid());
				mDataOther.setMood(OcupApplication.getInstance().mOtherCup.getMood());
				mDataOther.setName(OcupApplication.getInstance().mOtherCup.getName());
				mDataFriend.add(mDataOther);
				count_unreadMsg = dbMgr.countUnreadMsg(OcupApplication.getInstance().mOtherCup.getHuanxin_userid());
				mDataFriend.get(1).setCount_offline_msg(count_unreadMsg);// ///index
			}
		}
		mAdapterFriend.notifyDataSetChanged();

	}

	private static final int GETRELATIONSHIP_NOTLOGIN = 1;

	private static final int GETRELATIONSHIP_OK = 2;
	private static final int GETRELATIONSHIP_OK_NOFRIENDS = 3;
	private static final int GETRELATIONSHIP_NO = 4;

	private static final int MATECUP_OK = 5;
	private static final int MATECUP_NO = 6;
	private static final int MATECUP_NO_NET = 7;

	private static final int NOT_LOGIN = 8;
	private final int CONNECTBLUETOOTH_OK = 9;
	private final int CONNECTBLUETOOTH_NO = 10;
	private final int CONNECTBLUETOOTH_ING = 22;

	private final int GETCUPINFO_OK = 11;
	private final int GETCUPINFO_NOTLOGIN = 12;
	private final int GETCUPINFO_NO = 13;
	private final int GETCUPINFO_ING = 14;

	private final int EDITMOOD_OK = 15;
	private final int UPDATE_USERINFO_OK = 16;
	private final int UPDATE_USERINFO_NO = 17;
	private final int GETMATEDRINK_OK = 18;
	private final int GETMATEDRINK_NO = 19;

	private static final int DEMATE_OK = 20;
	private static final int DEMATE_NO = 21;

	/**
	 * 配对成功
	 */
	@Override
	public void mateCup_OK(String mate_cupid) {
		// 发送消息告诉对方
		// HuanxinUtil.getInstance().sendMsg(mActivity.getApplicationContext(),
		// null, mate_cupid, "", ChatMsgEntity.TYPE_ADDED, 0, false);

		// mRequest.getUserInfo(mActivity, OchatFragment.this);
		mRequest.getRelationship(mActivity, OchatFragment.this);
		// 写入到数据库
		dbMgr.addCup_mate(OcupApplication.getInstance().mOwnCup.getHuanxin_userid(), mate_cupid);
		mHandler.sendEmptyMessage(MATECUP_OK);
	}
//	public static final int MATECUPS_ERROR_3007 = 3007;
//	public static final int MATECUPS_ERROR_3008 = 3008;
//	public static final int MATECUPS_ERROR_3009 = 3009;
//	public static final int MATECUPS_ERROR_4001 = 4001;
	/**
	 * 配对失败
	 */
	@Override
	public void mateCup_NO(int mode) {
		String msg = "";
		switch (mode) {
		case HttpRequest.MATECUPS_ERROR_2001:
			msg = getString(R.string.other_cup_hasfriend);
			break;
		case HttpRequest.MATECUPS_ERROR_2013:
			msg = getString(R.string.have_friends);
			break;
		case HttpRequest.MATECUPS_ERROR_4001:
			msg = getString(R.string.unlogin);
			break;
		case HttpRequest.MATECUPS_ERROR_2003:
			msg = getString(R.string.add_friend_noaddmyself);
			break;
		default:
			msg = getString(R.string.add_friends_err) + " " + getString(R.string.error) + mode ;
			break;
		}
		Message obtainMessage = mHandler.obtainMessage();
		obtainMessage.obj = msg;
		obtainMessage.what = MATECUP_NO;
		mHandler.sendMessage(obtainMessage);
	}

	/**
	 * 配对失败,提示设置网络
	 */
	@Override
	public void mateCup_NO_net() {
		mHandler.sendEmptyMessage(MATECUP_NO_NET);
	}

	@Override
	public void getRelationship_OK(String mate_id) {
		// 写入到数据库
		dbMgr.addCup_mate(OcupApplication.getInstance().mOwnCup.getHuanxin_userid(), OcupApplication.getInstance().mOtherCup.getHuanxin_userid());
		mHandler.sendEmptyMessage(GETRELATIONSHIP_OK);

	}

	@Override
	public void getRelationship_notLogin() {
		mHandler.sendEmptyMessage(GETRELATIONSHIP_NOTLOGIN);
	}

	@Override
	public void getRelationship_NO() {
		mHandler.sendEmptyMessage(GETRELATIONSHIP_NO);
	}

	@Override
	public void getRelationship_OK_noFriends() {
		// 删除对应数据库记录
		dbMgr.deleteCup_mate(OcupApplication.getInstance().mOwnCup.getHuanxin_userid());
		mHandler.sendEmptyMessage(GETRELATIONSHIP_OK_NOFRIENDS);
	}

	@Override
	public void updateUI(String cupId, String toCupId, String content, String time, int type) {
		Log.d(TAG, "updateUI---------type===" + type);
		// 加为好友
		if (type == ChatMsgEntity.TYPE_ADDED) {
			iv_addFri.setImageResource(R.drawable.selector_deleter_friend);
			// 添加到数据库
			dbMgr.addCup_mate(Tools.getPreference(mActivity, UtilContact.HuanXinId), cupId);
			// 刷新列表
			mRequest.getRelationship(mActivity, this);

			// 被解除好友关系
		} else if (type == ChatMsgEntity.TYPE_DEMATED) {
			iv_addFri.setImageResource(R.drawable.selector_add_friend);
			// 删除对应数据库记录
			dbMgr.deleteCup_mate(OcupApplication.getInstance().mOwnCup.getHuanxin_userid());
			// 刷新列表
			mRequest.getRelationship(mActivity, this);
			// 未读消息
		} else {
			Log.d(TAG, "updateUI---------type===" + type + "  mDataFriend.size()==" + mDataFriend.size());
			if (mDataFriend.size() > 1) {
				count_unreadMsg = dbMgr.countUnreadMsg(Tools.getPreference(mActivity,UtilContact.HuanXinId));
				Log.d(TAG, "updateUI---------type===" + type + "  count_unreadMsg==" + count_unreadMsg + "  isVisible==" + isVisible);
				if (isVisible) {
					mDataFriend.get(1).setCount_offline_msg(count_unreadMsg);
					mAdapterFriend.notifyDataSetChanged();
				}
			}
		}
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
		mHandler.sendEmptyMessage(GETCUPINFO_NO);
	}

	@Override
	public void GetCupInfoing() {
		mHandler.sendEmptyMessage(GETCUPINFO_ING);
	}

	@Override
	public void ok(int type) {
		if (type == CustomDialog.DEL_FRIEND_DIALOG) {// 删除配对杯子
			mRequest.demateCups(mActivity, OchatFragment.this);
		}
	}

	@Override
	public void ok(int type, Object obj) {
		if (type == CustomDialog.EDITMOOD_DIALOG) {
			Message msg = new Message();
			msg.what = EDITMOOD_OK;
			msg.obj = obj;
			mHandler.sendMessage(msg);
		}
	}

	@Override
	public void cancel(int type) {
	}

	@Override
	public void updateCupInfo_OK() {
		mHandler.sendEmptyMessage(UPDATE_USERINFO_OK);
	}

	@Override
	public void updateCupInfo_NO() {
		mHandler.sendEmptyMessage(UPDATE_USERINFO_NO);
	}

	@Override
	public void getMateDrink_success() {
		mHandler.sendEmptyMessage(GETMATEDRINK_OK);
	}

	@Override
	public void getMateDrink_failed() {
		mHandler.sendEmptyMessage(GETMATEDRINK_NO);
	}

	@Override
	public void demateCup_OK() {
		mHandler.sendEmptyMessage(DEMATE_OK);
	}

	@Override
	public void demateCup_NO() {
		mHandler.sendEmptyMessage(DEMATE_NO);
	}
}
