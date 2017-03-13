/**
 * 
 */
package com.sen5.ocup.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * 检查网络连接
 * 
 * @author tangmingcong
 * 
 */
public class InternetInfoUtil {

	public static boolean checkInternet(Context mContext) {
		ConnectivityManager cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			Log.e("null", "-------------info.isConnected() = " + info.isConnected());
			return true;
		} else {
			return false;
		}

	}
}
