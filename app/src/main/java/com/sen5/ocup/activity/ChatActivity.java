package com.sen5.ocup.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.adapter.ChatMsgAdapter;
import com.sen5.ocup.blutoothstruct.BluetoothType;
import com.sen5.ocup.callback.CustomInterface.IDialog;
import com.sen5.ocup.callback.CustomInterface.IReceiveChat;
import com.sen5.ocup.callback.RequestCallback.SendMsgCallback;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.gui.FaceRelativeLayout;
import com.sen5.ocup.gui.FaceRelativeLayout.OnCorpusSelectedListener;
import com.sen5.ocup.gui.MarqueeTextView;
import com.sen5.ocup.gui.MyListViewPullDownAndUp;
import com.sen5.ocup.gui.MyListViewPullDownAndUp.RefreshListener;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.receiver.HomeWatcher;
import com.sen5.ocup.receiver.HuanxinBroadcastReceiver;
import com.sen5.ocup.struct.ChatEmoji;
import com.sen5.ocup.struct.ChatMsgEntity;
import com.sen5.ocup.util.BlueToothRequest;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.ClassForSoftInput;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.FaceConversionUtil;
import com.sen5.ocup.util.HuanxinUtil;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;
import com.sen5.ocup.yili.UserInfoActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 : 聊天界面
 */
public class ChatActivity extends BaseActivity implements OnClickListener, Callback, SendMsgCallback, IReceiveChat, IDialog, OnTouchListener, OnCorpusSelectedListener,AdapterView.OnItemLongClickListener {

	private HomeWatcher mHomeKeyReceiver = null;
	private static final String TAG = "ChatActivity";
	private static final int maxBytes = 500;
	public static String KEY_SHAKE = "//viabrate";
	// 是否是发给自己杯子
	private boolean isMe;
	// 消息处理对象
	private Handler mHander;
	private BlueToothRequest mBlueToothRequest;
	// 操作数据库的对象
	private DBManager dbMgr;

	private LinearLayout mLayout_back;
	private MarqueeTextView tv_title_username;
	private RelativeLayout mLayout_facechoose;
	private View mLayout_chatAdd;
	private View mLayout_addScrawl;
	private View mLayout_addShake;
	private View mLayout_addscrawlanim;
	private ImageView mIvSendOrAdd;
	private ImageView iv_detail;
	private FaceRelativeLayout mFaceRelativeLayout;

	private EditText mEditTextContent;

	private MyListViewPullDownAndUp mListView;

	private ChatMsgAdapter mAdapterChat;

	private List<ChatMsgEntity> mDataChat = new ArrayList<ChatMsgEntity>();
	/**
	 * 消息发送的对象
	 */
	private String userName;
	// 键盘操作对象
	private InputMethodManager mInputMethodManager;

	//intent传值
	private String to_huanxinID;
	private String avatar_url;
	private String  huanxinId;
	private String phoneNum;

	/**
	 * 接收到环信断开广播
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String errorString = intent.getStringExtra("errorString");
			Log.d(TAG, "ACTION_DISCONNECTED    errorString==" + errorString);
			mHander.sendEmptyMessage(NOT_LOGIN);
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		Intent intent = getIntent();
		isMe = intent.getBooleanExtra("IsMe", false);
		userName = intent.getStringExtra("userName");
		to_huanxinID = intent.getStringExtra("to_huanxinID");
		avatar_url = intent.getStringExtra("avatar");
		phoneNum = intent.getStringExtra("phoneNum");

		huanxinId = Tools.getPreference(this, UtilContact.HuanXinId);
		//解决沉浸式状态栏和键盘弹出冲突问题
		ClassForSoftInput.assistActivity(this);
		initView();
		initData();
		OcupApplication.getInstance().addActivity(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()----------------");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart()----------------");
		// 注册监听环信连接断开的广播
		IntentFilter filter = new IntentFilter(HuanxinBroadcastReceiver.ACTION_DISCONNECTED);
		this.registerReceiver(receiver, filter);
		mHomeKeyReceiver.startWatch();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop()----------------");
		super.onStop();
		this.unregisterReceiver(receiver);
		mHomeKeyReceiver.stopWatch();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy()----------------");
		super.onDestroy();
		OcupApplication.getInstance().mList.remove(this);
		clearObject();
		//移除环信广播接收者中的收到信息回调
		HuanxinBroadcastReceiver.removeCallBack();
	}

	/**
	 * 将本类中的对象至空
	 */
	private void clearObject() {
		mHander = null;
		mBlueToothRequest = null;
		dbMgr = null;
		mLayout_back = null;
		tv_title_username = null;
		mLayout_facechoose = null;
		mLayout_chatAdd = null;
		mLayout_addScrawl = null;
		mLayout_addShake = null;
		mIvSendOrAdd = null;
		mFaceRelativeLayout = null;
		mEditTextContent = null;
		mListView = null;
		mAdapterChat = null;
		mDataChat = null;
		userName = null;
		mInputMethodManager = null;
		to_huanxinID = null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 涂鸦结束
		if (requestCode == Tools.SCRAWL_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String result = data.getStringExtra("sendMsg");
				if (null != result && result.length() > 0) {
					Logger.e(TAG, "onActivityResult)---send---result==" + result);
					StringBuffer strContent = new StringBuffer("");
					// 将涂鸦从下标索引方式转成01表示
					int[][] int_text = new int[18][8];
					String[] str_text = result.split(",");
					for (int i = 0; i < str_text.length; i++) {
						int x = Integer.parseInt(str_text[i]) % 18;
						int y = Integer.parseInt(str_text[i]) / 18;
						int_text[x][y] = 1;
					}
					for (int i = 0; i < 18; i++) {
						for (int j = 0; j < 8; j++) {
							strContent.append(int_text[i][j]);
						}
					}
					send(strContent.toString(), ChatMsgEntity.TYPE_SCRAWL);
				}
				Log.d(TAG, "onActivityResult)-------result,length==" + result.length() + "  result=" + result);
			}
			// 涂鸦动画结束
		} else if (requestCode == Tools.SCRAWLANIM_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String[] result = data.getStringArrayExtra("sendAnimSrawl");
				StringBuffer strContent = new StringBuffer("");
				for (int i = 0; i < result.length; i++) {
					if (null != result[i] && result[i].length() > 0) {
						Logger.e(TAG, "onActivityResult)---send---result==" + result[i]);
						// 将涂鸦从下标索引方式转成01表示
						int[][] int_text = new int[18][8];
						String[] str_text = result[i].split(",");
						for (int j = 0; j < str_text.length; j++) {
							int x = Integer.parseInt(str_text[j]) % 18;
							int y = Integer.parseInt(str_text[j]) / 18;
							int_text[x][y] = 1;
						}
						for (int k = 0; k < 18; k++) {
							for (int j = 0; j < 8; j++) {
								strContent.append(int_text[k][j]);
							}
						}
					}
					// strContent.append("-");
					Log.d(TAG, "onActivityResult)-------result,length==" + result[i].length() + "  result=" + result[i]);
				}
				send(strContent.toString(), ChatMsgEntity.TYPE_SCRAWL_ANIM);
			}
		}
	}

	/**
	 * 初始化控件
	 */
	public void initView() {
		mHander = new Handler(this);
		dbMgr = new DBManager(this);

		FrameLayout titleLayout = (FrameLayout) findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(titleLayout,this);
		mLayout_back = (LinearLayout) findViewById(R.id.layout_back);
		tv_title_username = (MarqueeTextView) findViewById(R.id.tv_username);

		mListView = (MyListViewPullDownAndUp) findViewById(R.id.listview);
		mFaceRelativeLayout = (FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout);
		mLayout_addScrawl = findViewById(R.id.layout_addscrawl);
		mLayout_addShake = findViewById(R.id.layout_addshake);
		mLayout_addscrawlanim = findViewById(R.id.layout_addscrawlanim);
		mLayout_facechoose = (RelativeLayout) findViewById(R.id.ll_facechoose);
		mLayout_chatAdd = findViewById(R.id.inclu_chatadd);
		mIvSendOrAdd = (ImageView) findViewById(R.id.iv_sendoradd);
		iv_detail = (ImageView) findViewById(R.id.iv_detail);
		if (isMe){
			iv_detail.setVisibility(View.GONE);
		}
		mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);

		mEditTextContent.addTextChangedListener(mTextWatch);
		mEditTextContent.setOnClickListener(this);
		mListView.setOnTouchListener(this);
		mListView.setOnItemLongClickListener(this);
		mIvSendOrAdd.setOnClickListener(this);
		iv_detail.setOnClickListener(this);
		mLayout_addScrawl.setOnClickListener(this);
		mLayout_addscrawlanim.setOnClickListener(this);
		mLayout_addShake.setOnClickListener(this);
		mLayout_back.setOnClickListener(this);
	}

	/**
	 * 初始化数据
	 */
	public void initData() {
		mHomeKeyReceiver = new HomeWatcher(this);
		mBlueToothRequest = BlueToothRequest.getInstance();
		HuanxinBroadcastReceiver.setCallback(ChatActivity.this);
		Log.d(TAG, "huanxin    recieve action ==" + EMChatManager.getInstance().getNewMessageBroadcastAction());

		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mFaceRelativeLayout.setOnCorpusSelectedListener(this);

		tv_title_username.setText(userName);
		// 更新数据库消息状态
		dbMgr.updateChatStatus(to_huanxinID);
		int chatLog = getChatLog(true);
//		mAdapterChat = new ChatMsgAdapter(ChatActivity.this, this, this, mDataChat, mListView, isMe,avatar_url);
		mAdapterChat = new ChatMsgAdapter(ChatActivity.this, this, this, mDataChat, mListView, isMe,avatar_url);
		mListView.setAdapter(mAdapterChat);
		if(chatLog > 0){
			mListView.setSelection(chatLog-1);
		}
		mListView.setCanPullDown(true);
		mListView.setCanPullUp(false);
		mListView.setRefreshListener(new RefreshListener() {
			@Override
			public void pullUpRefresh() {
				mListView.onPullupRefreshComplete();
			}

			@Override
			public void pullDownRefresh() {
				int chatLog = getChatLog(false);
				mAdapterChat.notifyDataSetChanged();
				if(chatLog > 0){
					mListView.setSelection(chatLog -1);
				}else if(mListView.getChildCount() > 0){
					mListView.setSelection(0);
				}
				mListView.onPulldownRefreshComplete();
			}

			@Override
			public void pullUpStart() {
			}
		});
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Log.d(TAG, "onTouch action_down-------");
			if (v.getId() == R.id.listview) {
				mInputMethodManager.hideSoftInputFromWindow(mEditTextContent.getWindowToken(), 0);
				mFaceRelativeLayout.hideFaceView();
				if (mLayout_chatAdd.getVisibility() == View.VISIBLE) {
					mLayout_chatAdd.setVisibility(View.GONE);
				}
			}
		}
		return false;
	}

	/**
	 * 输入框的输入监听 
	 */
	private TextWatcher mTextWatch = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String content = mEditTextContent.getText()+"";
//			Log.d(TAG, "mTextWatch-----count==" + count + "  s=" + s + " length()==" + content.length()+"start=="+start+" before=="+before);
			if (content.trim().length() > 0) {
				mIvSendOrAdd.setImageResource(R.drawable.btn_send);
			} else {
				mIvSendOrAdd.setImageResource(R.drawable.addfri_sel);
				return;
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//			String content = mEditTextContent.getText()+"";
//			Log.d(TAG, "beforeTextChanged-----count==" + count + "  s=" + s + " length()==" + content.length()+"start=="+start+"   after=="+after);
		}

		@Override
		public void afterTextChanged(Editable s) {
//			Log.d(TAG, "afterTextChanged----mEditTextContent.getText().length()==" + mEditTextContent.getText().length());
		}
	};


	/**
	 * 获取消息记录
	 * 
	 * @param isFirst
	 *            取最新的消息
	 */
	private int getChatLog(boolean isFirst) {
		ArrayList<ChatMsgEntity> chatList = new ArrayList<ChatMsgEntity>();
		if (!isFirst && mDataChat.size() > 0) {
			if (isMe) {
				chatList.addAll(dbMgr.queryChat(huanxinId, huanxinId, mDataChat
						.get(0).get_id(), isFirst));
			} else {
				chatList.addAll(dbMgr.queryChat(huanxinId, to_huanxinID, mDataChat.get(0).get_id(), isFirst));
			}
		} else {
			Logger.e("getChatLog----isFirst"+mDataChat.size());
			mDataChat.clear();
			if (isMe) {
				chatList.addAll(dbMgr.queryChat(huanxinId, huanxinId,0,isFirst));
			} else {
				chatList.addAll(dbMgr.queryChat(huanxinId, to_huanxinID, 0, isFirst));
			}
		}
		Log.d(TAG, "getChatLog()-----chatList.size()==" + chatList.size());
		if (chatList.size() > 0) {
			for (int i = 0; i < chatList.size(); i++) {
				mDataChat.add(0, chatList.get(i));
			}
		}
		return chatList.size();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 发送 or 进入附加功能
		case R.id.iv_sendoradd:
			String contString = mEditTextContent.getText() + "";
			if (contString.trim().length() > 0) {
				mEditTextContent.setText("");
				send(contString, 0);
			} else {
				// 隐藏键盘
				mInputMethodManager.hideSoftInputFromWindow(mEditTextContent.getWindowToken(), 0);
				if (mLayout_chatAdd.getVisibility() == View.VISIBLE) {
					mLayout_chatAdd.setVisibility(View.GONE);
				} else {
					mLayout_facechoose.setVisibility(View.GONE);
					mLayout_chatAdd.setVisibility(View.VISIBLE);
				}
			}

			break;
		// 显示软键盘，隐藏表情选择和附加功能选择框
		case R.id.et_sendmessage:
			if (mLayout_chatAdd.getVisibility() == View.VISIBLE) {
				mLayout_chatAdd.setVisibility(View.GONE);
			}
			if (mLayout_facechoose.getVisibility() == View.VISIBLE) {
				mLayout_facechoose.setVisibility(View.GONE);
			}
			mInputMethodManager.showSoftInputFromInputMethod(mEditTextContent.getWindowToken(), InputMethodManager.SHOW_FORCED);

			break;
		// 进入涂鸦
		case R.id.layout_addscrawl:
			mLayout_chatAdd.setVisibility(View.GONE);
			mLayout_facechoose.setVisibility(View.GONE);
			Intent intent = new Intent(ChatActivity.this, ScrawlActivity.class);
			startActivityForResult(intent, Tools.SCRAWL_REQUEST_CODE);
			break;
		// 开始画动画涂鸦
		case R.id.layout_addscrawlanim:
			mLayout_chatAdd.setVisibility(View.GONE);
			mLayout_facechoose.setVisibility(View.GONE);

			Intent intent_srawlanim = new Intent(ChatActivity.this, ScrawlAnimActivity.class);
			startActivityForResult(intent_srawlanim, Tools.SCRAWLANIM_REQUEST_CODE);
			break;

		// 进入抖动
		case R.id.layout_addshake:
			send(KEY_SHAKE, 0);
			break;

		// 返回到friendslist页面
		case R.id.layout_back:
			Log.d(TAG, "onclick-------layout_back--");
			Intent resultIntent = new Intent();
			resultIntent.putExtra("Isme", isMe);
			ChatActivity.this.setResult(RESULT_OK, resultIntent);
//			HuanxinBroadcastReceiver.removeCallBack();
			finish();
			break;
			case R.id.iv_detail:
				Intent intent_userInfo = new Intent(ChatActivity.this, UserInfoActivity.class);
				intent_userInfo.putExtra("phoneNum",phoneNum);
				intent_userInfo.putExtra("userID",to_huanxinID);
				intent_userInfo.putExtra("avator",avatar_url);
				intent_userInfo.putExtra("nickName",userName);
				startActivity(intent_userInfo);
				break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mFaceRelativeLayout.hideFaceView()) {
				return true;
			}
			if (mLayout_chatAdd.getVisibility() == View.VISIBLE) {
				mLayout_chatAdd.setVisibility(View.GONE);
				return true;
			}
			Intent resultIntent = new Intent();
			resultIntent.putExtra("Isme", isMe);
			ChatActivity.this.setResult(RESULT_OK, resultIntent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void send(String contString, int type) {
		ChatMsgEntity entity = null;
		if (contString.trim().length() <= 0) {
			return;
		}
		if (isMe) {
			// 只发送到杯子
			entity = ChatMsgEntity.createChatMsgEntity(Tools.getPreference(ChatActivity.this,UtilContact.HuanXinId), Tools.getPreference(ChatActivity.this,UtilContact.HuanXinId),
					contString, "", type, ChatMsgEntity.FROM_ME);
			entity.setStatus(1);
			// 写入到数据库
			dbMgr.addChat(entity);
			mDataChat.add(entity);
			Logger.e(TAG,entity.getText().toString());
			if (!contString.equals(KEY_SHAKE)) {// 不是振动
				if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
					mBlueToothRequest.sendMsg2LED(entity, 1);
				} else {
					OcupToast.makeText(ChatActivity.this, getString(R.string.unconnect_2cup), Toast.LENGTH_LONG).show();
				}
			} else {
				if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
					mBlueToothRequest.sendMsg2ControlCup(null, BluetoothType.control_shake);
				} else {
					OcupToast.makeText(ChatActivity.this, getString(R.string.unconnect_2cup), Toast.LENGTH_LONG).show();
				}
			}
			
		} else {
			// 发送
			entity = ChatMsgEntity.createChatMsgEntity(Tools.getPreference(ChatActivity.this,UtilContact.HuanXinId), to_huanxinID, contString, "", type, ChatMsgEntity.FROM_ME);
			entity.setStatus(0);
			mDataChat.add(entity);
			HuanxinUtil.getInstance().sendMsg(getApplicationContext(), ChatActivity.this, to_huanxinID, contString, type, mDataChat.size() - 1, false);
		}
		mHander.sendEmptyMessage(SEND_OK_NO_CHECK);
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (ChatActivity.this.getWindow().isActive()) {
			switch (msg.what) {
			// 刷新聊天内容
			case UPDATE_CHAT:
				if (ChatActivity.this.hasWindowFocus()) {
					mAdapterChat.notifyDataSetChanged();
					mListView.setSelection(mListView.getCount() - 1);
				}
				break;

			// 发送成功
			case SEND_OK:
				mAdapterChat.notifyDataSetChanged();
				mListView.setSelection(mListView.getCount() - 1);
				break;

			// 发送失败
			case SEND_NO:
				mAdapterChat.notifyDataSetChanged();
				mListView.setSelection(mListView.getCount() - 1);
				// Toast.makeText(ChatActivity.this, (String) msg.obj,
				// Toast.LENGTH_SHORT).show();

				break;
			case SEND_OK_NO_CHECK:
				mAdapterChat.notifyDataSetChanged();
				mListView.setSelection(mListView.getCount() - 1);
				break;
			case NOT_LOGIN:
				break;

			default:
				break;
			}
		}
		return false;
	}

	@Override
	//发送信息成功回调
	public void sendMsg_ok(String msg, int position, boolean isRepeat) {
		Log.d(TAG, "sendMsg_ok  msg==" + msg + "  isRepeat==" + isRepeat);
		ChatMsgEntity entity = mDataChat.get(position);
		entity.setStatus(1);
		Log.d(TAG, "sendMsg_ok  " + entity.getText());
		// 将消息添加到数据库
		if (isRepeat) {// 重复发送成功
			dbMgr.updateChat(entity.getDate(), entity.getStatus());
		} else {
			dbMgr.addChat(entity);
		}
		Log.d(TAG, "sendMsg_ok    mDataChat.size()==" + mDataChat.size() + "     position==" + position);
		mDataChat.set(position, entity);
		Log.d(TAG, "sendMsg_ok  update---  mDataChat.size()==" + mDataChat.size());
		mHander.sendEmptyMessage(SEND_OK);
	}

	@Override
	public void sendMsg_no(String msg, int position, boolean isRepeat, String errorString) {
		Log.d(TAG, "sendMsg_no  msg==" + msg + "  errorString==" + errorString);
		if (mDataChat == null) {
			return;
		}
		ChatMsgEntity entity = mDataChat.get(position);
		entity.setStatus(2);
		Log.d(TAG, "sendMsg_no  " + entity.getText());
		// 将消息添加到数据库 重复添加
		if (isRepeat) {// 重复发送成功
			dbMgr.updateChat(entity.getDate(), entity.getStatus());
		} else {
			dbMgr.addChat(entity);
		}
		mDataChat.set(position, entity);
		Message message = new Message();
		message.obj = errorString;
		message.what = SEND_NO;
		mHander.sendMessage(message);
	}

	/**
	 * 接收到服务端推送过来的消息
	 */
	@Override
	public void updateUI(String cupId, String toCupId, String content, String time, int type) {
		Logger.e(TAG, "updateUI()------  from==" + cupId + "to==" + toCupId + "content: " + content + "  type==" + type + "  isMe==" + isMe);
		if (type == ChatMsgEntity.TYPE_DEMATED) {
			Intent resultIntent = new Intent();
			resultIntent.putExtra("chat", "demate");
			ChatActivity.this.setResult(RESULT_OK, resultIntent);
			finish();
		} else if (type != ChatMsgEntity.TYPE_ADDED) {
			if (!isMe) {
				ChatMsgEntity chatMsgEntity = ChatMsgEntity.createChatMsgEntity(cupId, toCupId, content, time, type, ChatMsgEntity.FROM_OTHER);
				if (chatMsgEntity != null && mDataChat != null) {
					mDataChat.add(chatMsgEntity);
				}
				mHander.sendEmptyMessage(UPDATE_CHAT);
				// 更新数据库消息状态
				dbMgr.updateChatStatus(to_huanxinID);
			}
		}
	}

	@Override
	public void ok(int type, Object obj) {
		if (type == CustomDialog.SEND_DIALOG) {// 发送消息
			HuanxinUtil.getInstance().sendMsg(getApplicationContext(), ChatActivity.this, to_huanxinID, mDataChat.get((Integer) obj).getText(),
					mDataChat.get((Integer) obj).getType(), (Integer) obj, true);
		}
//		if (type == CustomDialog.DIALOG_DELETE_MSG){//删除消息,该功能还未做
//			mDataChat.remove(delete_position);
//			mAdapterChat.notifyDataSetChanged();
//		}
	}

	@Override
	public void ok(int type) {
	}

	@Override
	public void cancel(int type) {
	}

	private static final int UPDATE_CHAT = 1;
	private static final int SEND_OK = 2;
	private static final int SEND_NO = 3;
	private static final int SEND_OK_NO_CHECK = 12;
	private static final int NOT_LOGIN = 6;

	/**
	 * 选择动画表情
	 */
	@Override
	public void onCorpusSelected(ChatEmoji emoji) {
		Logger.e("onCorpusSelected-------------emoji.getFaceName()======" + emoji.getFaceName() + "  emoji.getCharacter()==" + emoji.getCharacter());
		SpannableString spannableString = FaceConversionUtil.getInstace().addFace(ChatActivity.this, emoji.getId(), emoji.getCharacter());
		send(spannableString.toString(), 2);
	}

	@Override
	public void onCorpusDeleted() {
	}

	private int delete_position;//即将需要被删除的id
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//		CustomDialog customDialog = new CustomDialog(ChatActivity.this,ChatActivity.this,R.style.custom_dialog,CustomDialog.DIALOG_DELETE_MSG,null);
//		customDialog.show();
//		delete_position = position;
		return false;
	}
}