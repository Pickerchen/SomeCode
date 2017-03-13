package com.sen5.ocup.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import com.sen5.ocup.activity.OcupApplication;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

/**
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :qq连接工具类
 */
public class QQConnect {

	static public class QQLoginInfo {
		public String openid;
		public String accessToken;
		public String expire;
		
		public QQLoginInfo(String openid, String accessToken, String expire) {
			this.openid = openid;
			this.accessToken = accessToken;
			this.expire = expire;
		}
	};
	
	public static QQConnect getInstance() {
		return gInstance;
	}
	
	//初始化，在application类中调用
	public void initQQConnect() {
		if (tencent_instance == null) {	
			tencent_instance = Tencent.createInstance(QQ_APP_ID, OcupApplication.getInstance().getApplicationContext());

			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
					OcupApplication.getInstance().getApplicationContext());	
			String openid = sp.getString(PRE_QQ_OPEN_ID, "");
			String accessToken = sp.getString(PRE_QQ_ACCESS_TOKEN, "");
			String expire = sp.getString(PRE_QQ_EXPIRES_IN, "");
			try {
				if (openid.length() > 0 && accessToken.length() > 0) {
					tencent_instance.setOpenId(openid);
					tencent_instance.setAccessToken(accessToken, expire);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	//设置发送信息的句柄
	public void setHandler(Handler handler) {
		mHandler = handler;
	}
	
	//保存/获取QQ拉起apk时发来的登录信息
	public QQLoginInfo getRemoteLoginInfo() {
		return mQQRemoteLoginInfo;
	}
	public void setRemoteLoginInfo(QQLoginInfo info) {
		mQQRemoteLoginInfo = info;
	}
	
	public QQLoginInfo getLoginInfo() {
		if (tencent_instance != null) {
			return new QQLoginInfo(
				tencent_instance.getOpenId(),
				tencent_instance.getAccessToken(),
				String.valueOf(tencent_instance.getExpiresIn()));
		}
		return null;
	}
	
	public void setLoginInfo(String openid, String accessToken, String expire) {
		if (tencent_instance != null) {			
			try {
				if (openid != null && openid.length() > 0 &&
					accessToken != null && accessToken.length() > 0 &&
					expire != null && expire.length() > 0 ) {					
					tencent_instance.setOpenId(openid);
					tencent_instance.setAccessToken(accessToken, expire);					
				}
				else {
					//参数为空时，为logout，新建一个实例
					tencent_instance = Tencent.createInstance(QQ_APP_ID, OcupApplication.getInstance().getApplicationContext());
				}
				
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
						OcupApplication.getInstance().getApplicationContext());	
				Editor editor = sp.edit();
				editor.putString(PRE_QQ_OPEN_ID, openid);
				editor.putString(PRE_QQ_ACCESS_TOKEN, accessToken);
				editor.putString(PRE_QQ_EXPIRES_IN, expire);
				editor.apply();
				editor.commit();
				
			}catch (Exception e) {
				Log.d(TAG, "set qq login info error");
				e.printStackTrace();
			}
		}
	}
	
	public boolean isLogin() {
		return tencent_instance.isSessionValid();
	}
	
	public void login(Fragment fragment, final int msgCode) {
		if (tencent_instance != null && (!tencent_instance.isSessionValid() || mTempFlag)) {
			tencent_instance.login(fragment, QQ_SCOPE, new QQLoginListener(msgCode));
		}
	}
	
	public void logout(final int msgCode) {
		if (tencent_instance.isSessionValid()) { 
			tencent_instance.logout(OcupApplication.getInstance().getApplicationContext());
			setLoginInfo("", "", "");
			sendMessage(mHandler, msgCode, RES_OK);
		}
	}
	
	//更新饮水数据
	public void uploadData(
			final int msgCode, 
			final int totalWater, 
			final int goalWater, 
			final int[] detail) {		
		if (!tencent_instance.isSessionValid()) {
			Log.d(TAG, "QQ not login before update data");
			
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(
					OcupApplication.getInstance().getApplicationContext());	
			String accessToken = sp.getString(PRE_QQ_ACCESS_TOKEN, "");
			if (accessToken.length() > 0) {
				sendMessage(mHandler, msgCode, RES_NO_LOGIN);
				//登录信息已经过期
				//将要上传的数据暂存下来，下次登录成功后马上上传
				mTempFlag = true;
				mTempMsgCode = msgCode;
				mTempTotalWater = totalWater;
				mTempGoalWater = goalWater;
				mTempDetail = detail;
			}
			return;
		}
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpPost httpPost = new HttpPost("https://openmobile.qq.com/v3/health/report_drinking");
				
				List<NameValuePair> params = new ArrayList<NameValuePair>(); 
				params.add(new BasicNameValuePair("access_token", tencent_instance.getAccessToken()));
				params.add(new BasicNameValuePair("oauth_consumer_key", QQ_APP_ID));
				params.add(new BasicNameValuePair("openid", tencent_instance.getOpenId()));
				params.add(new BasicNameValuePair("pf", "qzone"));
				
				//时间戳
				Long tsLong = System.currentTimeMillis()/1000;
				params.add(new BasicNameValuePair("time", tsLong.toString()));
				
				//喝水总量
				params.add(new BasicNameValuePair("total_water", String.valueOf(totalWater)));
				
				//目标喝水量
				int g = goalWater;
				if (g < 0) { //如果目标饮水量为负数，就使用默认的目标饮水量
					g = QQ_DEFAULT_GOAL;
				}
				params.add(new BasicNameValuePair("water_goal", String.valueOf(g)));
				
				//每半小时的饮水量
				//拼成形如[100,200,300...]这样的字串
				if (detail != null && detail.length > 0) {
					StringBuffer strDetail = new StringBuffer();
					strDetail.append('[');
					for (int i = 0, j = 0; i < 24; i++) {
						int countHalf = 0; //半个小时的水量
						if (j < detail.length) {
							countHalf = detail[j++] >> 1; //把一个小时的饮水量除2，以得到半小时的饮水量
						}
						if (i != 0) {
							strDetail.append(',');
						}
						strDetail.append(String.format("%d,%d", countHalf, countHalf));
					}
					strDetail.append(']');
					
					params.add(new BasicNameValuePair("drink_detail", strDetail.toString()));
				}
				
				try {
					for (int i = 0; i < REQUEST_RETRY_COUNT; i++) { //如果上传失败则重试
						Log.d(TAG, "send request to qq, totoal water=" + totalWater);
						HttpEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
						httpPost.setEntity(entity);
						HttpClient httpClient = new DefaultHttpClient();
						HttpResponse response = httpClient.execute(httpPost);
						if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							String strResult = EntityUtils.toString(response.getEntity());  
			                
			                JSONObject json = new JSONObject(strResult);
			                int ret = json.getInt("ret");
			                if (ret != 0) {
			                	if (ret == -73) {
			        				//token已失效，原因一般是修改了QQ密码
			                		//将要上传的数据暂存下来，下次登录成功后马上上传
			                		Log.d(TAG, "token is invalid, re-login...");
			        				sendMessage(mHandler, msgCode, RES_NO_LOGIN);
			        				mTempFlag = true;
			        				mTempMsgCode = msgCode;
			        				mTempTotalWater = totalWater;
			        				mTempGoalWater = goalWater;
			        				mTempDetail = detail;
			        				break;
			        			} else {
				                	Log.d(TAG, "post data failed, error code: " + ret);
				                	sendMessage(mHandler, msgCode, RES_ERROR);
				                	continue;
			        			}
			                } else {
			                	sendMessage(mHandler, msgCode, RES_OK);
			                	break;
			                }
						} else {
							Log.d(TAG, "post data to qq failed, http code: " + response.getStatusLine().getStatusCode());
							sendMessage(mHandler, msgCode, RES_ERROR);
							continue;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.d(TAG, "post data to qq failed: " + e.toString());
				} 
			}
			
		}).start();
	}
	
	private class QQLoginListener implements IUiListener 
	{
		public QQLoginListener(int msgCode) {
			this.msgCode = msgCode;
		}
		
		private int msgCode;
		
		@Override
		public void onComplete(Object response) {
			if (null == response) {
				sendMessage(mHandler, msgCode, RES_ERROR);
                return;
            }
            JSONObject jsonResponse = (JSONObject)response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
            	sendMessage(mHandler, msgCode, RES_ERROR);
                return;
            }
            try {
            	int ret = jsonResponse.getInt("ret");
            	if (ret != 0) {
            		Log.d(TAG, "QQ Connect error, result code: " + ret);
            		sendMessage(mHandler, msgCode, RES_ERROR);
            		return;
            	}
            	
				String openid = jsonResponse.getString(com.tencent.connect.common.Constants.PARAM_OPEN_ID);
				String accessToken = jsonResponse.getString(com.tencent.connect.common.Constants.PARAM_ACCESS_TOKEN);
				String expires = jsonResponse.getString(com.tencent.connect.common.Constants.PARAM_EXPIRES_IN);
				setLoginInfo(openid, accessToken, expires);
				sendMessage(mHandler, msgCode, RES_OK);
				
				//如果有暂存的数据，上传之
				if (mTempFlag) {
					uploadData(mTempMsgCode, mTempTotalWater, mTempGoalWater, mTempDetail);
					mTempFlag = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				sendMessage(mHandler, msgCode, RES_ERROR);
                return;
			}
		}
		
		@Override
		public void onCancel() {
		}

		@Override
		public void onError(UiError arg0) {
		}
	};
	
	private static QQConnect gInstance = new QQConnect();
	
	public static final String TAG       = "QQConnect";
	public static final String QQ_APP_ID = "1101513923";
	public static final String QQ_SCOPE  = "all";
	
	//从QQ拉起apk时，intent传进来的登录信息
	public static final String QQ_FROM        = "from";
	public static final String QQ_FROM_VAL    = "qqhealth";
	public static final String QQ_OPENID      = "openid";
	public static final String QQ_ACCESSTOKEN = "accesstoken";
	public static final String QQ_EXPIRE      = "accesstokenexpiretime";
	
	public static final int QQ_DEFAULT_GOAL = 1500; //QQ健康的默认目标饮水量 
	
	public static final String PRE_QQ_OPEN_ID      = "qq_open_id";
	public static final String PRE_QQ_ACCESS_TOKEN = "qq_access_token";
	public static final String PRE_QQ_EXPIRES_IN   = "qq_expires_in"; 
	
	public static final int RES_OK       = 0;
	public static final int RES_ERROR    = 1;
	public static final int RES_CANEL    = 2;
	public static final int RES_NO_LOGIN = 3; //登录token已经过期或者失效
	
	private int  REQUEST_RETRY_COUNT = 3; //发送数据的重试次数
	
	private QQLoginInfo mQQRemoteLoginInfo = null; //从QQ拉起apk时的登录信息
	
	private  Tencent      tencent_instance = null;
	private  Handler      mHandler = null;
	
	//上传饮水数据，seesion失效时暂时保存的数据
	private boolean mTempFlag       = false;
	private int     mTempMsgCode    = -1;
	private int     mTempTotalWater = -1;
	private int     mTempGoalWater  = -1;
	private int[]   mTempDetail     = null;
	
	private QQConnect() {
	}
	
	private static void sendMessage(Handler handler, int msgCode, int resCode) { 
		if (handler != null) {
			Message message = new Message();
			message.what = msgCode;
			message.arg1 = resCode;
			handler.sendMessage(message);
		}
	}
	
}
