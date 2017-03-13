package com.sen5.ocup.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.easemob.chat.CmdMessageBody;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.orhanobut.logger.Logger;
import com.sen5.ocup.callback.CustomInterface.IReceiveChat;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.Tools;

public class HuanxinBroadcastReceiver1 extends BroadcastReceiver {
	public static final String ACTION_HUANXIN_CMD = "easemob.cmdmsg.rrioo.yili";
	private static final String TAG = "HuanxinBroadcastReceiver1";
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
		 if (intent.getAction().equals(ACTION_HUANXIN_CMD)){
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
}
