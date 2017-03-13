package com.sen5.ocup.util;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.easemob.EMCallBack;
import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.orhanobut.logger.Logger;
import com.sen5.ocup.activity.MainActivity;
import com.sen5.ocup.blutoothstruct.CupStatus;
import com.sen5.ocup.callback.RequestCallback.SendMsgCallback;

import java.util.List;


/**
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :环信请求工具类
 */
public class HuanxinUtil {
	private DBManager mDBManager;
	private static final String TAG = "HuanxinUtil";
	public EMGroup group;
	private String groupId;
	/**
	 * 创建Request的单例
	 */
	private static HuanxinUtil mInstance = null;

	public static HuanxinUtil getInstance() {
		if (mInstance == null) {
			mInstance = new HuanxinUtil();
		}
		return mInstance;
	}

	private HuanxinUtil() {
	}

	public void register(String name, String pwd) {
		CreateAccountTask task = new CreateAccountTask();
		task.execute(name, pwd);
	}

	private class CreateAccountTask extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... args) {
			String userid = args[0];
			String pwd = args[1];
			try {
				EMChatManager.getInstance().createAccountOnServer(userid, pwd);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return userid;
		}
	}

	/**
	 * 登录到环信
	 * 
	 * @param pwd
	 */
	public void login(Context appcontext, String huanxinID, String pwd) {
		Logger.e(TAG, "login------------huanxinID==" + huanxinID + "  pwd==" + pwd);
		if (huanxinID == null || pwd == null) {
			return;
		}
		if (null == EMChatManager.getInstance()) {
			Log.d(TAG, "login-------null == HuanxinUtil.getInstance()");
			return;
			// HuanxinUtil.getInstance().initHuanxin(appcontext);
		} else {
			Log.d(TAG, "login-------null != HuanxinUtil.getInstance()");
		}
		// 登录到聊天服务器 null-----------
		try {
			EMChatManager.getInstance().login(huanxinID, pwd, new EMCallBack() {

				@Override
				public void onError(int arg0, final String errorMsg) {
					Log.d(TAG, "login)--------onError----------errorMsg==" + errorMsg);
				}

				@Override
				public void onProgress(int arg0, String arg1) {
					Log.d(TAG, "login)--------onProgress----------");
				}

				@Override
				public void onSuccess() {
					Log.d(TAG, "login)--------onSuccess----------");
				}
			});
		} catch (Exception e) {
			Log.d(TAG, "login)--------Exception----------e==" + e);
			// 初始化环信聊天SDK
//			Log.d(TAG, "initialize EMChat SDK");
//			EMChat.getInstance().setDebugMode(true);
//			EMChat.getInstance().init(appcontext);
		}
	}

	//组群聊天
	public void createEMGroup(Context context){
		Logger.e("createEMGroup","createEMGroup is coming");
		 groupId = Tools.getPreference(context,UtilContact.GROUPID);
		try {
			Logger.e("createEMGroup","group is "+groupId);
			 group = EMGroupManager.getInstance().getGroupFromServer(groupId);
			if (group != null){
				Logger.e("createEMGroup",group.toString());
				List<String> members = group.getMembers();//获取群成员
				if (members != null){
					for (String member:members){
						Logger.e("CreateEMGroup","群成员："+member);
					}
				}
				String owner = group.getOwner();//获取群主
				Logger.e("CreateEMGroup","群主："+owner);
			}
			else {
				Logger.e("createEMGroup","group is null");
			}

		} catch (EaseMobException e) {
			Logger.e("createEMGroup","exception = "+e.getMessage());
			e.printStackTrace();
		}
	}

	private int sendCount = 0;
//	public void sendGroupMsg(final Context context,  String content){
//		if ("true".equals(Tools.getPreference(context,UtilContact.isAlived))){
//				if ("true".equals(Tools.getPreference(context,UtilContact.OPENDATA))){
//					content = "#1#1#"+ CupStatus.getInstance().getCur_water_temp();
//				}
//			else {
//					content = "#1#1#0";
//				}
//		}
//		groupId = Tools.getPreference(context,UtilContact.GROUPID);
//		Logger.e("sendGroupMsg","group is "+groupId);
//		EMMessage msg = EMMessage.createSendMessage(EMMessage.Type.TXT);
//		msg.setAttribute("em_ignore_notification",true);
//		msg.setChatType(EMMessage.ChatType.GroupChat);
//		//设置需要发到的群组
//		msg.setReceipt(groupId);
//		String json = String2jsonUtil.chatmsg2JsonString(content, ChatMsgEntity.TYPE_TXT);
//		TextMessageBody textMessageBody = new TextMessageBody(json);
//		msg.addBody(textMessageBody);
//		final String finalContent = content;
//		EMChatManager.getInstance().sendGroupMessage(msg, new EMCallBack() {
//			@Override
//			public void onSuccess() {
//				Logger.e("sendGroupMsg","发送群消息成功");
//				sendCount = 0;
//			}
//
//			@Override
//			public void onError(int i, String s) {
//				Logger.e("sendGroupMsg","发送群消息出错:"+s);//该api出错概率比较大，所以进行重复发送
//				if (sendCount < 8){
//					sendGroupMsg(context, finalContent);
//					sendCount++;
//				}
//			}
//
//			@Override
//			public void onProgress(int i, String s) {
//				Logger.e("sendGroupMsg","消息正在发送中");
//			}
//		});
//	}

	public void sendGroupMsg(final Context context,  String content){
		if ("true".equals(Tools.getPreference(context,UtilContact.isAlived))){
			if ("true".equals(Tools.getPreference(context,UtilContact.OPENDATA))){
				content = "#1#1#"+ CupStatus.getInstance().getCur_water_temp();
			}
			else {
				content = "#1#1#0";
			}
		}
		groupId = Tools.getPreference(context,UtilContact.GROUPID);
		Logger.e("sendGroupMsg","group is "+groupId);
		EMMessage msg = EMMessage.createSendMessage(EMMessage.Type.CMD);
		msg.setChatType(EMMessage.ChatType.GroupChat);
		//设置需要发到的群组
		msg.setReceipt(groupId);
		String action = content;
//		msg.setAttribute("a", "a");//支持自定义扩展
		CmdMessageBody messageBody = new CmdMessageBody(action);
		msg.addBody(messageBody);
		final String finalContent = content;
		EMChatManager.getInstance().sendGroupMessage(msg, new EMCallBack() {
			@Override
			public void onSuccess() {
				Tools.printInfo("sendGroupMsg","发送群消息成功");
				sendCount = 0;
			}

			@Override
			public void onError(int i, String s) {
				Tools.printInfo("sendGroupMsg","发送群消息出错:"+s);//该api出错概率比较大，所以进行重复发送
				if (sendCount < 8){
					sendGroupMsg(context, finalContent);
					sendCount++;
				}
		}

			@Override
			public void onProgress(int i, String s) {
				Tools.printInfo("sendGroupMsg","消息正在发送中");
			}
		});
	}

	//发送消息给个人
	public void sendMsg(final Context context, final SendMsgCallback callback, String toWho, final String content, int type, final int position, final boolean isRepeat) {
		try {
			EMMessage msg = EMMessage.createSendMessage(EMMessage.Type.TXT);
			Logger.e("sendMsg"+"towho = "+toWho+"sendTime = "+msg.getMsgTime());
			if (mDBManager != null){
				mDBManager.updateFriendsInfoTime(msg.getMsgTime(),toWho);
			}
			else {
				mDBManager = new DBManager(context);
				mDBManager.updateFriendsInfoTime(msg.getMsgTime(),toWho);
			}

			if (toWho != null) {
				//设置信息接收人
				msg.setReceipt(toWho);
			}
			String json = String2jsonUtil.chatmsg2JsonString(content, type);
			TextMessageBody body = new TextMessageBody(json);
			msg.addBody(body);
			Log.e(TAG, "发送      toWho===" + toWho + "   content==" + content + "   type==" + type);
			//添加扩展属性
			msg.setAttribute("extStringAttr", "String Test Value");

			// send out msg
			try {
				EMChatManager.getInstance().sendMessage(msg, new EMCallBack() {
					@Override
					public void onSuccess() {
						Tools.printInfo(TAG, "sendMsg-----消息发送成功:");
						if (null != callback) {
							callback.sendMsg_ok(content, position, isRepeat);
						}
					}

					@Override
					public void onProgress(int arg0, String arg1) {
						Tools.printInfo(TAG, "sendMsg----------正在消息发送。。。。。。。。。。:");
					}

					@Override
					public void onError(int arg0, String arg1) {
						Tools.printInfo(TAG, "sendMsg---------消息发送失败！！！！！！！！arg0=="+arg0+"  arg1==" + arg1);
//						EMChat.getInstance().setDebugMode(true);
//						EMChat.getInstance().init(context);
						if (null != callback) {
							callback.sendMsg_no(content, position, isRepeat, arg1);
						}
						
						if (arg1==null  || (arg1!=null && arg1.contains("connect"))) {// connection
							String huanxinId = Tools.getPreference(context,UtilContact.HuanXinId);
							String huanPwd = Tools.getPreference(context,UtilContact.HuanXinPWD);
							Tools.printInfo(TAG,"id is "+huanxinId+"pwd is "+huanPwd);
							//在登录之前先做登出操作，否则可能会导致同一个会话在断网后无法发送
							EMChatManager.getInstance().logout();
							MainActivity.loginHuanxin();
								if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
									BlueToothRequest.getInstance().sendMsg2getCupID();
								}
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, "消息发送失败:Exception  e==" + e);
				if (null != callback) {
					callback.sendMsg_no(content, position, isRepeat, e.getMessage());
				}
			}
		} catch (Exception e) {
			if (null != callback) {
				callback.sendMsg_no(content, position, isRepeat, e.getMessage());
			}
		}	
	}
}
