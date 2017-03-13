package com.sen5.ocup.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.HuanxinUtil;

public class NetworkStateBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "NetworkState";
	private ConnectivityManager connectivityManager;
	private NetworkInfo info;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "     intent.getAction()===" + intent.getAction());
//		if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
//			Log.d(TAG, "网络状态已经改变");
//			connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//			info = connectivityManager.getActiveNetworkInfo();
//			if (info != null && info.isAvailable()) {
//				String name = info.getTypeName();
//				Log.d(TAG, "当前网络名称：" + name);
//				String huanxinID = OwnerCupInfo.getInstance().getHuanxin_userid() ;
//				String huanxinPWD = OwnerCupInfo.getInstance().getHuanxin_pwd();
//				if (OwnerCupInfo.getInstance().isLogin2Srv()) {
//					if (null !=huanxinID&& null !=huanxinPWD) {
//						Log.d(TAG, "---------------huanxinID==="+huanxinID+"-------------huanxinPWD=="+huanxinPWD);
//						HuanxinUtil.getInstance().login(context,OwnerCupInfo.getInstance().getHuanxin_userid(), OwnerCupInfo.getInstance().getHuanxin_pwd());
//					}
//				}else{
//					String cupID = OwnerCupInfo.getInstance().getCupID();
//					if (null != cupID) {
//						HttpRequest.getInstance().login(context,cupID,0);
//					}
//				}
//			} else {
//				Log.d(TAG, "没有可用网络");
////				Toast.makeText(context, context.getString(R.string.check_network), Toast.LENGTH_SHORT).show();
//			}
//		}
	}
}
