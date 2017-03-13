package com.sen5.ocup.util;

import java.lang.reflect.Field;

import org.json.JSONObject;

import android.util.Log;
/**
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :string 转为jason
 */
public class String2jsonUtil {
	private static final String TAG = "String2jsonUtil";

	/**
	 * 聊天内容string 转为jason
	 * 
	 * @param obj
	 * @return
	 */
	public static String chatmsg2JsonString(String content, int type) {
		Log.d(TAG, "chatmsg2JsonString-------content==" + content);//chatmsg2JsonString json==+{"content":"","contentType":4}
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("content", content);
			obj.put("contentType", type);
			json=obj.toString();
		} catch (Exception e) {
			throw new RuntimeException("cause:" + e.toString());
		}
		Log.d(TAG, "chatmsg2JsonString json==+"+json);
		return json;
	}
}
