package com.sen5.ocup.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.activity.ChatActivity;
import com.sen5.ocup.activity.DialogActivity;
import com.sen5.ocup.blutoothstruct.BluetoothType;
import com.sen5.ocup.callback.CustomInterface.IReceiveChat;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.struct.ChatMsgEntity;
import com.sen5.ocup.util.BlueToothRequest;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;

import org.json.JSONException;
import org.json.JSONObject;

public class HuanxinBroadcastReceiver extends BroadcastReceiver {
	public static final String ACTION_HUANXIN = "easemob.newmsg.rrioo.yili";
	public static final String ACTION_HUANXIN_CMD = "easemob.cmdmsg.rrioo.yili";
	public static final String ACTION_DISCONNECTED = "com.sen5.nhh.ocup.receiver.HuanxinBroadcastReceiver.disconnect";
	private static final String TAG = "HuanxinBroadcastReceiver";
	//收到信息发送广播，chatFragment2接收更改未读信息UI
	public static final String ReceiverChat = "ReceiverChat";
	public static final String ReceiverGroupChat = "ReceiverGroupChat";//收到群聊状态码消息
	private static IReceiveChat mIReceiveChat;
	private static String token;
	// 操作数据库的对象
	private DBManager dbMgr;

	@Override
	public void onReceive(Context context, Intent intent) {
		Logger.e(TAG, "onReceive------intent.getAction()===" + intent.getAction());
		if (intent.getAction().equals(ACTION_HUANXIN)) {
			String msgId = intent.getStringExtra("msgid"); // 消息id
			// 从SDK 根据消息ID 可以获得消息对象
			EMChatManager mEMChatManager = EMChatManager.getInstance();
			if(null == mEMChatManager){
				Log.e(TAG, "onReceive mEMChatManager == null");
				return;
			}
			EMMessage message = mEMChatManager.getMessage(msgId);
//			if (message.getChatType() == EMMessage.ChatType.GroupChat){
//				Logger.e(TAG,"收到群聊信息");
//				String content = null;
//				String contact_id = message.getFrom();
//				try {
//					Logger.e(TAG,message.getBody().toString());
//					String jsonString = message.getBody().toString().substring(5);
//					JSONObject jsonObject = new JSONObject(jsonString);
//					content = jsonObject.getString("content");
//					Logger.e(TAG,"content is"+content);
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//				String[] status_String = content.split("#");//#1#1#36:切割出四个
//				if (status_String.length == 4) {
//					int[] status = new int[3];
//					status[0] = Integer.parseInt(status_String[1]);
//					status[1] = Integer.parseInt(status_String[2]);
//					status[2] = Integer.parseInt(status_String[3]);
//					if (null == dbMgr) {
//						dbMgr = new DBManager(context);
//					}
//					dbMgr.updateFriendsStatus(contact_id, status, Tools.getCurrentMinutes());
//					Intent intent_status = new Intent(ReceiverGroupChat);
//					intent_status.putExtra("contact_id", contact_id);
//					context.sendBroadcast(intent_status);
////					Tools.showToast(context, message.getBody().toString());
//				}
//				return;
//			}
			Logger.e("收到通知，通知的ID是："+msgId+"通过消息ID获得的message == " + message.getBody());
			switch (message.getType()) {
			case TXT:
				try {
					//收到加好友的消息
					if (message.getIntAttribute("ntype") == 1){
                        token = message.getStringAttribute("token");
						// 添加到数据库
						Logger.e("收到被添加好友的消息");
//						dbMgr.addCup_mate(OcupApplication.getInstance().mOwnCup.getHuanxin_userid(), message.getFrom());
						//提示对话框
						Intent intent_add = new Intent(context, DialogActivity.class);
						intent_add.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent_add.putExtra(DialogActivity.dialogType, CustomDialog.ADDED_DIALOG);
						intent_add.putExtra("token",token);
						context.startActivity(intent_add );
						return;
                    }
					else if (message.getIntAttribute("ntype") == 2){
							Logger.e(TAG,"对方已同意您的好友申请");
						String nickName = message.getStringAttribute("nickname");
						Logger.e(TAG,"nickname = "+nickName);
						Intent intent1 = new Intent(context,DialogActivity.class);
						intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent1.putExtra(DialogActivity.dialogType,DialogActivity.add_sure);
						intent1.putExtra("nickName",nickName);
						context.startActivity(intent1);
					}
				} catch (EaseMobException e) {
					e.printStackTrace();
				}

				TextMessageBody txtBody = (TextMessageBody) message.getBody();
				String content = txtBody.getMessage();
				Log.d(TAG, "text message from:" + message.getFrom() + " text:" + txtBody.getMessage() + " \n\r");
				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(content);
					String text = jsonObject.getString("content");
					int type = jsonObject.getInt("contentType");
					Log.d(TAG, "text====" + text + "  type==" + type);
					// 发送至cup
					ChatMsgEntity chatMsgEntity = ChatMsgEntity.createChatMsgEntity(message.getFrom(), Tools.getPreference(context,UtilContact.HuanXinId), text, message.getMsgTime()
							+ "", type,ChatMsgEntity.FROM_OTHER);
					chatMsgEntity.setStatus(3);// 0-----正在发送 1----已查看消息// 2------未发送消息 3----离线消息
					if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
						if (text.equals(ChatActivity.KEY_SHAKE)) {
							BlueToothRequest.getInstance().sendMsg2ControlCup(null, BluetoothType.control_shake);
						} else {
							BlueToothRequest.getInstance().sendMsg2LED(chatMsgEntity, 3);
						}
					} else {
						OcupToast.makeText(context, context.getString(R.string.unconnect_2cup), Toast.LENGTH_LONG).show();
					}

					// 写入数据库
					if (null == dbMgr) {
						dbMgr = new DBManager(context);
					}
					// 收到：被添加为好友or被解除好友关系
					if (type != ChatMsgEntity.TYPE_ADDED && type != ChatMsgEntity.TYPE_DEMATED) {
						dbMgr.addChat(chatMsgEntity);
						dbMgr.updateFriendsInfoTime(chatMsgEntity.getDate(),chatMsgEntity.getToHuanxinID());
						Logger.e("HuanxinBroadCastReceiver","收到信息发送广播更改UI");
						Intent intent1 = new Intent(ReceiverChat);
						context.sendBroadcast(intent1);
					} else {
						if (type == ChatMsgEntity.TYPE_ADDED) {
							// 添加到数据库
							Logger.e("收到被添加好友的消息");
							dbMgr.addCup_mate(Tools.getPreference(context,UtilContact.HuanXinId), message.getFrom());
							//提示对话框
							Intent intent_add = new Intent(context, DialogActivity.class);
							intent_add.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent_add.putExtra(DialogActivity.dialogType, CustomDialog.ADDED_DIALOG);
							context.startActivity(intent_add);
						} else if (type == ChatMsgEntity.TYPE_DEMATED) {
							// 从数据库删除相应记录
							dbMgr.deleteCup_mate(Tools.getPreference(context,UtilContact.HuanXinId));
							//提示对话框
							Intent intent_add = new Intent(context, DialogActivity.class);
							intent_add.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent_add.putExtra(DialogActivity.dialogType, CustomDialog.DEMATED_DIALOG);
							context.startActivity(intent_add );
						}
					}

					// 刷新消息列表界面
					if (null != mIReceiveChat) {
						Log.d(TAG, "null !=mIReceiveChat");
						mIReceiveChat.updateUI(message.getFrom(), Tools.getPreference(context, UtilContact.HuanXinId), text, message.getMsgTime() + "", type);// 更新界面
					} else {
						Log.d(TAG, "null ============mIReceiveChat");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		else if (intent.getAction().equals(ACTION_HUANXIN_CMD)){
			String msgId = intent.getStringExtra("msgid"); // 消息id
			// 从SDK 根据消息ID 可以获得消息对象
			EMChatManager mEMChatManager = EMChatManager.getInstance();
			if(null == mEMChatManager){
				Log.e(TAG, "onReceive mEMChatManager == null");
				return;
			}
			EMMessage message = mEMChatManager.getMessage(msgId);
			CmdMessageBody body = (CmdMessageBody) message.getBody();
			String conten = body.action;
			Tools.showToast(context,"收到透传信息 is"+conten);
		}
	}

	//在chatActivity中设置
	public static void setCallback(IReceiveChat iReceiveChat) {
		mIReceiveChat = iReceiveChat;
	}

	//在chatActivity退出前中移除callback
	public static  void removeCallBack(){
		mIReceiveChat = null;
	}
}
