package com.sen5.ocup.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.blutoothstruct.DrinkData;
import com.sen5.ocup.callback.RequestCallback;
import com.sen5.ocup.callback.RequestCallback.DemateCupCallback;
import com.sen5.ocup.callback.RequestCallback.ExitLogin2APPSrvCallback;
import com.sen5.ocup.callback.RequestCallback.GetAPKVersionCallback;
import com.sen5.ocup.callback.RequestCallback.GetCupInfoCallback;
import com.sen5.ocup.callback.RequestCallback.GetDrinkCallback;
import com.sen5.ocup.callback.RequestCallback.GetMateDrink;
import com.sen5.ocup.callback.RequestCallback.GetRelationshipCallback;
import com.sen5.ocup.callback.RequestCallback.GetTipsCallback;
import com.sen5.ocup.callback.RequestCallback.IRecoveryFactoryCallback;
import com.sen5.ocup.callback.RequestCallback.UpdateUserInfoCallback;
import com.sen5.ocup.callback.RequestCallback.UploadUserImageCallback;
import com.sen5.ocup.callback.RequestCallback.mateCupCallback;
import com.sen5.ocup.struct.APKVersionInfo;
import com.sen5.ocup.struct.RequestHost;
import com.sen5.ocup.struct.Tips;
import com.sen5.ocup.yili.FriendInfo;
import com.sen5.ocup.yili.MyHttpClient;
import com.sen5.ocup.yili.UserInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import internal.org.apache.http.entity.mime.MultipartEntity;
import internal.org.apache.http.entity.mime.content.FileBody;

/**
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :HTTP请求工具类
 */
public class HttpRequest {

	private static final String TAG = "HttpRequest";
	/**
	 * 添加成功
	 */
	public static final int MATECUPS_SUCCESS_0= 0;
	/**
	 * 对方已有好友
	 */
	public static final int MATECUPS_ERROR_2001 = 2001;

	/**
	 * 您已有好友
	 */
	public static final int MATECUPS_ERROR_2013 = 2013;

	/**
	 * 未登录 
	 */
	public static final int MATECUPS_ERROR_4001 = 4001;

	/**
	 * 添加的是自己
	 */
	public static final int MATECUPS_ERROR_2003 = 2003;
	/**
	 * 创建Request的单例
	 */
	private static HttpRequest mInstance = null;
	private final static int requestTimeout = 10000;
	private final static int responseTimeout = 10000;

	private boolean isGetTips;
	private boolean isGetMoreTips;
	private boolean isGetRefreshTips;
	private boolean isGetRelationship;
	private boolean isGettingUserinfo;

	public List<Cookie> mCookies = new ArrayList<Cookie>();

	public synchronized static HttpRequest getInstance() {
		Logger.e("getInstance");
		if (mInstance == null) {
			Logger.e("httpRequest.geinstance","mInstance == null1");
			mInstance = deserialization();
			if (mInstance == null) {
				Logger.e("httpRequest.geinstance","mInstance == null2");
				mInstance = new HttpRequest();
			}
		}
		return mInstance;
	}

	private HttpRequest() {

	}

	public void saveStatues() {
		serialization();
	}

	// 将实例序列化保存下来，暂时只保存cookie
	private synchronized void serialization() {
		if (mCookies.size() == 0) {
			return;
		}

		String SDPath = Tools.getSDPath();
		if (SDPath == null) {
			return;
		}

		File dirFile = new File(SDPath + Tools.OCUP_DIR);
		if (!dirFile.exists()) {
			if (!dirFile.mkdirs()) {
				Log.e(TAG, "failed to make dir " + SDPath + Tools.COOKIE_FILE_NAME);
				return;
			}
		}

		String filePath = SDPath + Tools.OCUP_DIR + Tools.COOKIE_FILE_NAME;
		File file = new File(filePath);
		try {
			file.createNewFile();

		} catch (Exception e) {
			Log.d(TAG, "cannot create file: " + filePath);
			e.printStackTrace();
			return;
		}

		List<SerializableCookie> serCookies = new ArrayList<SerializableCookie>();
		for (int i = 0; i < mCookies.size(); i++) {
			serCookies.add(new SerializableCookie(mCookies.get(i)));
		}
		try {
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
			List<SerializableCookie> serCookies2 = (List<SerializableCookie>) input.readObject();
			if (serCookies != null && serCookies2 != null){
				if (serCookies.get(0).getCookie().toString().equals(serCookies2.get(0).getCookie().toString())){
					return;
				}
			}
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(serCookies);
			out.close();
		} catch (Exception e) {
			Log.d(TAG, "cannot serializ cup info");
			e.printStackTrace();
		}
	}

	// 尝试从本地将信息反序列回，暂时只读取cookie
	private synchronized static HttpRequest deserialization() {
		Logger.e(TAG, "deserialization cookie");
		String sdRoot = Tools.getSDPath();
		if (sdRoot == null) {
			Logger.e("sdRoot is null");
			return null;
		}

		String filePath = sdRoot + Tools.OCUP_DIR + Tools.COOKIE_FILE_NAME;
		File file = new File(filePath);
		if (!file.exists()) {
			Logger.e("return null");
			return null;
		}
		Logger.e("file is exists");
		try {
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
			Logger.e(input.toString()+"");
			List<SerializableCookie> serCookies = (List<SerializableCookie>) input.readObject();
			Logger.e(serCookies.toString());
			input.close();

			HttpRequest instance = new HttpRequest();
			for (int i = 0; i < serCookies.size(); i++) {
				Logger.e("instance add mCookies");
				instance.mCookies.add(serCookies.get(i).getCookie());
			}

			return instance;
		} catch (Exception e) {
			Logger.e("deserialization", "failed to deserialization cookie");
			Logger.e(e.toString()+"");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取最新健康小贴士
	 *
	 * @param context
	 * @param mGetTipsCallback
	 * @param count
	 *            一次获取的条数
	 * @return
	 */
	public void getTips(final Activity activity, final Context context, final GetTipsCallback mGetTipsCallback, final int count, DBManager db) {
		Log.d(TAG, "getTips------isGetTips==" + isGetTips);
		if (isGetTips) {
			mGetTipsCallback.getTipsing();
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				isGetTips = true;
				String url = RequestHost.TIPS_HOST + "/getLatestNews/" + count+"?lang="+context.getString(R.string.language);
				//http://api.otelligent.com/news/getLatestNews/3?lang=zh-cn
				Log.d(TAG, "getTips--------url==" + url);

				HttpGet httpRequest = new HttpGet(url);
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
				HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

				DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				ArrayList<Tips> dataTips = null;
				try {
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					Log.d(TAG, "getTips--------statusCode==" + statusCode);
					if (statusCode == STATUS_CODE_OK) {
						dataTips = new ArrayList<Tips>();
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							isGetTips = false;
							return;
						}
						String strResult = EntityUtils.toString(entity);
						Log.d(TAG, "getTips--------strResult==" + strResult);
						JSONObject jsonObject = new JSONObject(strResult);
						String imagePathRoot = jsonObject.getString(IMAGEPATH);
						JSONArray resultArray = new JSONArray(jsonObject.getString(RESULTS));
						Log.d(TAG, "getTips--------size==" + resultArray.length());
						for (int i = 0; i < resultArray.length(); i++) {
							Tips tip = new Tips();
							JSONObject result = (JSONObject) resultArray.opt(i);
							tip.setId(result.getString(ID));
							tip.setTitle(result.getString(TITLE));
							tip.setBrief(result.getString(BRIEF));
							tip.setDate(result.getString(DATE));
							tip.setImgName(result.getString(IMAGE_NAME));
							if (tip.getImgName().startsWith("http")) {
								tip.setImgUrl(tip.getImgName());
							} else {
								tip.setImgUrl(imagePathRoot + tip.getImgName());
							}
							// loadImage(activity, mGetTipsCallback,
							// tip.getImgUrl(), 0, tip, imgLoader);
							dataTips.add(tip);
						}
						Tools.savePreference(context, "ocupLanguage", context.getString(R.string.language));
						mGetTipsCallback.getTips_success(dataTips);
					} else {
						mGetTipsCallback.getTips_failed();
					}
				} catch (ClientProtocolException e) {
					Log.d(TAG, "getTips--------ClientProtocolException==" + e);
					mGetTipsCallback.getTips_failed();
					e.printStackTrace();
				} catch (IOException e) {
					Log.d(TAG, "getTips--------IOException==" + e);
					mGetTipsCallback.getTips_failed();
					e.printStackTrace();
				} catch (JSONException e) {
					Log.d(TAG, "getTips--------JSONException==" + e);
					mGetTipsCallback.getTips_failed();
					e.printStackTrace();
				}
				isGetTips = false;

			}
		}).start();

	}


	public void getTips2(final Activity activity, final Context context, final GetTipsCallback mGetTipsCallback, final int count, DBManager db) {
		Log.d(TAG, "getTips------isGetTips==" + isGetTips);
		if (isGetTips) {
			mGetTipsCallback.getTipsing();
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				isGetTips = true;
				String url = RequestHost.TIPS_HOST + "/tips_rose";
				Log.d(TAG, "getTips--------url==" + url);

				HttpGet httpRequest = new HttpGet(url);
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
				HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

				DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				ArrayList<Tips> dataTips = null;
				try {
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					Log.d(TAG, "getTips--------statusCode==" + statusCode);
					if (statusCode == STATUS_CODE_OK) {
						dataTips = new ArrayList<Tips>();
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							isGetTips = false;
							return;
						}
						String strResult = EntityUtils.toString(entity);
						Log.d(TAG, "getTips--------strResult==" + strResult);
						JSONObject jsonObject = new JSONObject(strResult);
//						String imagePathRoot = jsonObject.getString(IMAGEPATH);
						JSONArray resultArray = new JSONArray(jsonObject.getString("tips"));
						Log.d(TAG, "getTips--------size==" + resultArray.length());
						for (int i = 0; i < resultArray.length(); i++) {
							Tips tip = new Tips();
							JSONObject result = (JSONObject) resultArray.opt(i);
							tip.setId(result.getString("id"));
							tip.setTitle("");
							tip.setBrief("");
							String date = ""+(System.currentTimeMillis()-i);
							tip.setDate(date);
							tip.setImgName(result.getString("img_url"));
							if (tip.getImgName().startsWith("http")) {
								tip.setImgUrl(tip.getImgName());
							} else {
//								tip.setImgUrl(imagePathRoot + tip.getImgName());
							}
							// loadImage(activity, mGetTipsCallback,
							// tip.getImgUrl(), 0, tip, imgLoader);
							dataTips.add(tip);
						}
						Tools.savePreference(context, "ocupLanguage", context.getString(R.string.language));
						mGetTipsCallback.getTips_success(dataTips);
					} else {
						mGetTipsCallback.getTips_failed();
					}
				} catch (ClientProtocolException e) {
					Log.d(TAG, "getTips--------ClientProtocolException==" + e);
					mGetTipsCallback.getTips_failed();
					e.printStackTrace();
				} catch (IOException e) {
					Log.d(TAG, "getTips--------IOException==" + e);
					mGetTipsCallback.getTips_failed();
					e.printStackTrace();
				} catch (JSONException e) {
					Log.d(TAG, "getTips--------JSONException==" + e);
					mGetTipsCallback.getTips_failed();
					e.printStackTrace();
				}
				isGetTips = false;

			}
		}).start();

	}

	/**
	 * 获取start_date时间之前健康小贴士
	 *
	 * @param context
	 * @param mGetTipsCallback
	 * @param count
	 *            一次获取的条数
	 * @param start_date
	 *            从什么时候取
	 * @return
	 */
	public void getMoreTips(final Activity activity, final Context context, final GetTipsCallback mGetTipsCallback, final int count, final String start_date, DBManager db) {
		if (isGetMoreTips) {
			mGetTipsCallback.getTipsing();
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				isGetMoreTips = true;
				String url = RequestHost.TIPS_HOST + "/getMoreNewsList/" + start_date + "/" + count+"?lang="+context.getString(R.string.language);
				Log.d(TAG, "getMoreTips--------url==" + url);

				HttpGet httpRequest = new HttpGet(url);
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
				HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

				DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				ArrayList<Tips> dataTips = null;
				try {
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					Log.d(TAG, "getMoreTips--------statusCode==" + statusCode);
					if (statusCode == STATUS_CODE_OK) {
						dataTips = new ArrayList<Tips>();
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							mGetTipsCallback.getMoreTips_failed();
							isGetMoreTips = false;
							return;
						}
						String strResult = EntityUtils.toString(entity);
						Log.d(TAG, "getMoreTips--------strResult==" + strResult);
						JSONObject jsonObject = new JSONObject(strResult);
						String imagePathRoot = jsonObject.getString(IMAGEPATH);
						JSONArray resultArray = new JSONArray(jsonObject.getString(RESULTS));
						Log.d(TAG, "getMoreTips--------size==" + resultArray.length());
						for (int i = 0; i < resultArray.length(); i++) {
							Tips tip = new Tips();
							JSONObject result = (JSONObject) resultArray.opt(i);
							if (null != result) {
								tip.setId(result.getString(ID));
								tip.setTitle(result.getString(TITLE));
								tip.setBrief(result.getString(BRIEF));
								tip.setDate(result.getString(DATE));
								tip.setImgName(result.getString(IMAGE_NAME));
								if (tip.getImgName().startsWith("http")) {
									tip.setImgUrl(tip.getImgName());
								} else {
									tip.setImgUrl(imagePathRoot + tip.getImgName());
								}
								// loadImage(activity, mGetTipsCallback,
								// tip.getImgUrl(), 0, tip, imgLoader);
								dataTips.add(tip);
								Log.d(TAG, "getMoreTips--------tip.getDate()==" + tip.getDate());
							} else {
								Log.d(TAG, "getMoreTips--------(JSONObject) resultArray.opt(i)==null   i==" + i);
							}
						}
						Tools.savePreference(context, "ocupLanguage", context.getString(R.string.language));
						mGetTipsCallback.getMoreTips_success(dataTips);
					} else {
						mGetTipsCallback.getMoreTips_failed();
					}
				} catch (ClientProtocolException e) {
					Log.d(TAG, "getMoreTips--------ClientProtocolException==" + e);
					mGetTipsCallback.getMoreTips_failed();
					e.printStackTrace();
				} catch (IOException e) {
					Log.d(TAG, "getMoreTips--------IOException==" + e);
					mGetTipsCallback.getMoreTips_failed();
					e.printStackTrace();
				} catch (JSONException e) {
					Log.d(TAG, "getMoreTips--------JSONException==" + e);
					mGetTipsCallback.getMoreTips_failed();
					e.printStackTrace();
				}
				isGetMoreTips = false;
			}
		}).start();

	}

	/**
	 * 获取start_date时间之后的健康小贴士
	 *
	 * @param context
	 * @param mGetTipsCallback
	 * @param count
	 *            一次获取的条数
	 * @param start_date
	 *            从什么时候取
	 * @return
	 */
	public void getRefreshTips(final Activity activity, final Context context, final GetTipsCallback mGetTipsCallback, final int count, final String start_date, DBManager db) {
		if (isGetRefreshTips) {
			mGetTipsCallback.getTipsing();
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				isGetRefreshTips = true;

				//lang:abstract语言选择,目前支持en-us和zh-cn.
				String url = RequestHost.TIPS_HOST + "/updateNewsList/" + start_date + "/" + count+"?lang="+context.getString(R.string.language);
				Log.d(TAG, "getRefreshTips--------url==" + url);

				HttpGet httpRequest = new HttpGet(url);
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
				HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

				DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				ArrayList<Tips> dataTips = null;
				try {
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					Log.d(TAG, "getRefreshTips--------statusCode==" + statusCode);

					if (statusCode == STATUS_CODE_OK) {
						dataTips = new ArrayList<Tips>();
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							mGetTipsCallback.getRefreshTips_failed();
							isGetRefreshTips = false;
							return;
						}
						String strResult = EntityUtils.toString(entity);
						Log.d(TAG, "getRefreshTips--------strResult==" + strResult);
						JSONObject jsonObject = new JSONObject(strResult);
						String imagePathRoot = jsonObject.getString(IMAGEPATH);
						JSONArray resultArray = new JSONArray(jsonObject.getString(RESULTS));
						Log.d(TAG, "getRefreshTips--------size==" + resultArray.length());
						for (int i = 0; i < resultArray.length(); i++) {
							Tips tip = new Tips();
							JSONObject result = (JSONObject) resultArray.opt(i);
							if (null != result) {
								tip.setId(result.getString(ID));
								tip.setTitle(result.getString(TITLE));
								tip.setBrief(result.getString(BRIEF));
								tip.setDate(result.getString(DATE));
								tip.setImgName(result.getString(IMAGE_NAME));
								if (tip.getImgName().startsWith("http")) {
									tip.setImgUrl(tip.getImgName());
								} else {
									tip.setImgUrl(imagePathRoot + tip.getImgName());
								}
								// loadImage(activity, mGetTipsCallback,
								// tip.getImgUrl(), 0, tip, imgLoader);
								dataTips.add(tip);
							} else {
								Log.d(TAG, "getRefreshTips--------(JSONObject) resultArray.opt(i)==null   i==" + i);
							}
						}
						Tools.savePreference(context, "ocupLanguage", context.getString(R.string.language));
						mGetTipsCallback.getRefreshTips_success(dataTips);
					} else {
						mGetTipsCallback.getRefreshTips_failed();
					}
				} catch (ClientProtocolException e) {
					Log.d(TAG, "getRefreshTips--------ClientProtocolException==" + e);
					mGetTipsCallback.getRefreshTips_failed();
					e.printStackTrace();
				} catch (IOException e) {
					Log.d(TAG, "getRefreshTips--------IOException==" + e);
					mGetTipsCallback.getRefreshTips_failed();
					e.printStackTrace();
				} catch (JSONException e) {
					Log.d(TAG, "getRefreshTips--------JSONException==" + e);
					mGetTipsCallback.getRefreshTips_failed();
					e.printStackTrace();
				}
				isGetRefreshTips = false;
			}
		}).start();

	}

	// public void loadImage(final Context context, final Object callback, final
	// String imgUrl, final int type, final Object obj, final AsyncImageLoader
	// imgLoader) {
	// if (null != imgLoader) {
	// Log.d(TAG, "loadImage------imgUrl==" + imgUrl + "    type====" + type);
	//
	// imgLoader.downloadImage(imgUrl, true/* false */, new
	// AsyncImageLoader.ImageCallback() {
	// @Override
	// public void onImageLoaded(Bitmap bitmap, String imageUrl, int type,
	// Object obj) {
	// if (bitmap != null) {
	// Log.d(TAG, "loadImage-----success==type==" + type);
	// if (type == 0) {
	// ((Tips) obj).setBmp(bitmap);
	// // ((Tips)
	// // obj).setBmp(BitmapUtil.optimizeBitmap(bitmap));
	// // ((Tips) obj).setBmp_blur(Blur.fastblur(context,
	// // bitmap, 25));
	// ((Tips) obj).setIsdefaultImg(false);
	// } else if (type == 1) {
	// ((Teas) obj).setBmp(bitmap);
	// } else if (type == 3) {
	// ((OwnerCupInfo) obj).setBmp_head(bitmap);
	// } else if (type == 4) {
	// ((Cup) obj).setBmp_head(bitmap);
	// }
	// } else {
	// // 下载失败，设置默认图片
	// Log.d(TAG, "loadImage-----failed==type==" + type);
	// if (type == 0) {
	// Bitmap bmpDefault = BitmapFactory.decodeResource(context.getResources(),
	// R.drawable.picture);
	// ((Tips) obj).setBmp(bmpDefault);
	// // ((Tips) obj).setBmp_blur(Blur.fastblur(context,
	// // bmpDefault, 25));
	// ((Tips) obj).setIsdefaultImg(true);
	// } else if (type == 1) {
	// Bitmap bmpDefault = BitmapFactory.decodeResource(context.getResources(),
	// R.drawable.picture);
	// ((Teas) obj).setBmp(bmpDefault);
	// } else if (type == 3) {
	// Bitmap bmpDefault = BitmapFactory.decodeResource(context.getResources(),
	// R.drawable.user_me);
	// ((OwnerCupInfo) obj).setBmp_head(bmpDefault);
	// } else if (type == 4) {
	// Bitmap bmpDefault = BitmapFactory.decodeResource(context.getResources(),
	// R.drawable.user_me);
	// ((Cup) obj).setBmp_head(bmpDefault);
	// }
	// // tea.setIsdefaultImg(true);
	// }
	// }
	// }, type, obj);
	// }
	// }


//	public void login(final Context appContext, final String cupId, final int loginCount) {
//		Log.d(TAG, "login------   cupId===" + cupId  + "::::mCookies.size() = " + mCookies.size());
//		if (mCookies.size() > 0) {
//			Log.d(TAG, "login------ mCookies.size()>0==");
//			return;
//		}
//		if (!BluetoothConnectUtils.getInstance().isRunFront) {
//			return;
//		}
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				// String url = RequestHost.LOGIN_HOST +
//				// "/users/loginWithCupID/" + cupId;
//				//http://api.otelligent.com/users/loginWithCupID
//				String url = RequestHost.LOGIN_HOST + "/users/loginWithCupID";
//				Log.d(TAG, "login-------url==" + url + "   cupId==" + cupId);
//				HttpPost httpRequest = new HttpPost(url);
//				// 封装数据
//				Map<String, String> parmas = new HashMap<String, String>();
//				parmas.put("cupID", cupId);
//				ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
//				if (parmas != null) {
//					Set<String> keys = parmas.keySet();
//					for (Iterator<String> i = keys.iterator(); i.hasNext();) {
//						String key = (String) i.next();
//						pairs.add(new BasicNameValuePair(key, parmas.get(key)));
//					}
//				}
//
//				try {
//					UrlEncodedFormEntity p_entity = new UrlEncodedFormEntity(pairs, "utf-8");
//					httpRequest.setEntity(p_entity);
//
//					HttpParams httpParameters = new BasicHttpParams();
//					HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
//					HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);
//
//					DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
//
//					HttpResponse httpResponse = httpClient.execute(httpRequest);
//					int statusCode = httpResponse.getStatusLine().getStatusCode();
//					Log.d(TAG, "login--------statusCode==" + statusCode);
//					if (statusCode == STATUS_CODE_OK) {
//						mCookies.clear();
//						HttpEntity entity = httpResponse.getEntity();
//						if(null == entity){
//							if (loginCount < 5) {
//								login(appContext, cupId, loginCount + 1);
//							}
//							return;
//						}
//						String strResult = EntityUtils.toString(entity);
//						CookieStore mCookieStore = ((AbstractHttpClient) httpClient).getCookieStore();
//						List<Cookie> cookies = mCookieStore.getCookies();
//						if (cookies.isEmpty()) {
//							Log.d(TAG, "-login------Cookie NONE---------");
//						} else {
//							for (int i = 0; i < cookies.size(); i++) {
//								// 保存cookie
//								Cookie cookie = cookies.get(i);
//								Log.d(TAG, "login-------" + cookies.get(i).getName() + "=" + cookies.get(i).getValue());
//								mCookies.add(cookie);
//							}
//						}
//
//						Log.d(TAG, "login-------strResult==" + strResult);
//						JSONObject jsonObject = new JSONObject(strResult);
//						int retCode = jsonObject.getInt(RETCODE);
//						if (0 == retCode) {
//							Log.d(TAG, "login-------OK==");
//							String id = jsonObject.getString(LOGINID);
//							String huanxinID = jsonObject.getJSONObject(CHAT).getString(HUANXIN_USERID);
//							String huanxinPwd = jsonObject.getJSONObject(CHAT).getString(HUANXIN_PWD);
//							Log.d(TAG, "login-------OK==huanxinID==" + huanxinID + "  huanxinPwd==" + huanxinPwd);
//							OcupApplication.getInstance().mOwnCup = new DBManager(OcupApplication.getInstance()).queryOwnCup(OcupApplication.getInstance().mOwnCup.getCupID());
//							OcupApplication.getInstance().mOwnCup.setHuanxin_userid(huanxinID);
//							OcupApplication.getInstance().mOwnCup.setHuanxin_pwd(huanxinPwd);
//							Tools.savePreference(appContext, "huanxinID", huanxinID);
//							Tools.savePreference(appContext, "huanxinPWD", huanxinPwd);
//							new DBManager(OcupApplication.getInstance()).updateOwnCup(OcupApplication.getInstance().mOwnCup);
//
//							HuanxinUtil.getInstance().login(appContext, huanxinID, huanxinPwd);
//
//							getUserInfo(appContext, null);
//							return;
//						}
//					} else {
//						mCookies.clear();
//					}
//				} catch (Exception e) {
//					Log.d(TAG, "login--------Exception==" + e);
//					e.printStackTrace();
//				}
//				Log.d(TAG, "login-------NO==  count_loginFailed==" + loginCount);
//				if (loginCount < 5) {
//					login(appContext, cupId, loginCount + 1);
//				}
//			}
//		}).start();
//	}

	/**
	 * 好友申请：需要等待对方进行确认
	 *
	 * @param context
	 * @param callback
	 * @param mate_cupid
	 */
	public void mateCups(final Context context, final mateCupCallback callback, final String mate_cupid) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = RequestHost.LOGIN_HOST + "/cups/mateCups";
				Log.d(TAG, "mateCups-------url==" + url);
				HttpPost httpRequest = new HttpPost(url);
				// 封装数据
				Map<String, String> parmas = new HashMap<String, String>();
				parmas.put("mateCupID", mate_cupid);
				ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
				if (parmas != null) {
					Set<String> keys = parmas.keySet();
					for (Iterator<String> i = keys.iterator(); i.hasNext();) {
						String key = (String) i.next();
						pairs.add(new BasicNameValuePair(key, parmas.get(key)));
					}
				}
				int retCode = -1;
				try {
					if (mCookies != null) {
						for (int i = 0; i < mCookies.size(); i++) {
							httpRequest.addHeader("Cookie", mCookies.get(i).getName() + "=" + mCookies.get(i).getValue());
						}
					}
					UrlEncodedFormEntity p_entity = new UrlEncodedFormEntity(pairs, "utf-8");
					httpRequest.setEntity(p_entity);
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
					HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

					DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					Log.d(TAG, "mateCups--------statusCode==" + statusCode);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							callback.mateCup_NO(retCode);
							return;
						}
						String strResult = EntityUtils.toString(entity);
						Log.d(TAG, "mateCups-------strResult==" + strResult);
						JSONObject jsonObject = new JSONObject(strResult);
						retCode = jsonObject.getInt(RETCODE);
						Log.d(TAG, "mateCups-------retCode == " + retCode);
						if (MATECUPS_SUCCESS_0 == retCode) {
							Log.d(TAG, "mateCups-------OK==");
							String id = jsonObject.getString(MATE_ID);
							callback.mateCup_OK(id);
							return;
						} else if (MATECUPS_ERROR_4001 == retCode) {
							reLogin(context, 1);
						}

					}
				} catch (Exception e) {
					Log.d(TAG, "mateCupsit--------IOException==" + e);
					e.printStackTrace();
				}
				callback.mateCup_NO(retCode);
				Log.d(TAG, "mateCups-------NO==");
			}
		}).start();
	}

	/**
	 * 解除配对
	 *
	 * @param context
	 * @param callback
	 */
	public void demateCups(final Context context, final DemateCupCallback callback) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String url = RequestHost.LOGIN_HOST + "/cups/demateCups";
				Log.d(TAG, "demateCups-------url==" + url);
				HttpGet httpRequest = new HttpGet(url);
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
				HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

				DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				try {
					if (mCookies != null) {
						for (int i = 0; i < mCookies.size(); i++) {
							httpRequest.addHeader("Cookie", mCookies.get(i).getName() + "=" + mCookies.get(i).getValue());
						}
					}
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					Log.d(TAG, "demateCups--------statusCode==" + statusCode);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if(null != callback){
								callback.demateCup_NO();
							}
							return;
						}
						String strResult = EntityUtils.toString(entity);
						Log.d(TAG, "demateCups-------strResult==" + strResult);
						JSONObject jsonObject = new JSONObject(strResult);
						int retCode = jsonObject.getInt(RETCODE);
						if (0 == retCode) {
							Log.d(TAG, "demateCups-------OK==");
							if(null != callback){

								callback.demateCup_OK();
							}
							return;
						} else if (4001 == retCode) {
							reLogin(context, 2);
						}
					}
				} catch (Exception e) {
					Log.d(TAG, "demateCups--------IOException==" + e);
					e.printStackTrace();
				}
				if(null != callback){

					callback.demateCup_NO();
				}
				Log.d(TAG, "demateCups-------NO==");
			}
		}).start();
	}

	/**
	 * 退出
	 *
	 * @param context
	 */
	public void exit(Context context, ExitLogin2APPSrvCallback mExitLogin2APPSrvCallback) {
		final ExitLogin2APPSrvCallback callback = mExitLogin2APPSrvCallback;
		new Thread(new Runnable() {

			@Override
			public void run() {
				String url = RequestHost.LOGIN_HOST + "/users/logout";
				Log.d(TAG, "exit-------url==" + url);
				HttpGet httpRequest = new HttpGet(url);
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
				HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

				DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				try {
					if (mCookies != null) {
						for (int i = 0; i < mCookies.size(); i++) {
							httpRequest.addHeader("Cookie", mCookies.get(i).getName() + "=" + mCookies.get(i).getValue());
						}
					}
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					Log.d(TAG, "exit--------statusCode==" + statusCode);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if (null != callback) {
								callback.exitLogin2APPSrv_NO();
							}
							return;
						}

						String strResult = EntityUtils.toString(entity);
						Log.d(TAG, "exit-------strResult==" + strResult);
						JSONObject jsonObject = new JSONObject(strResult);
						int retCode = jsonObject.getInt(RETCODE);
						if (0 == retCode) {
							Log.d(TAG, "exit-------OK==");
							if (null != callback) {
								callback.exitLogin2APPSrv_OK();
							}
							return;
						}
					}
				} catch (Exception e) {
					Log.d(TAG, "exit--------JSONException==" + e);
					e.printStackTrace();
				}
				Log.d(TAG, "exit-------NO==");
				if (null != callback) {
					callback.exitLogin2APPSrv_NO();
				}
			}
		}).start();
	}

	public void recoveryFactoryCloud(Context context, final IRecoveryFactoryCallback iRecoveryFactoryCallback){
		new Thread(){
			@Override
			public void run() {
				String url = RequestHost.LOGIN_HOST + "/profile/unbindUser";
				HttpPost httpPost = new HttpPost(url);
				HttpParams params = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(params, requestTimeout);
				HttpConnectionParams.setSoTimeout(params, responseTimeout);
				DefaultHttpClient defaultHttpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				HttpResponse execute = null;
				int retCode = -1;
				try {
					if (mCookies != null) {
						for (int i = 0; i < mCookies.size(); i++) {
							httpPost.addHeader("Cookie", mCookies.get(i).getName() + "=" + mCookies.get(i).getValue());
							Log.e(TAG, "---------mCookies.get(i).getName() = " +mCookies.get(i).getName()
									+":::mCookies.get(i).getValue() = " + mCookies.get(i).getValue());
						}
					}
					execute = defaultHttpClient.execute(httpPost);
					int statusCode = execute.getStatusLine().getStatusCode();
					Log.d(TAG, "=============recovery statusCode = " + statusCode);
					if(statusCode == STATUS_CODE_OK){
						HttpEntity entity = execute.getEntity();
						if(null == entity){
							if(null != iRecoveryFactoryCallback){
								iRecoveryFactoryCallback.recoveryFactory_Failed(retCode);
							}
							return;
						}
						String string = EntityUtils.toString(entity);
						JSONObject jsonObject = new JSONObject(string);
						retCode = jsonObject.getInt(RETCODE);

						Log.d(TAG, "=============recovery 111statusCode = " +string +":::" + retCode);
						if(retCode == 0){
							iRecoveryFactoryCallback.recoveryFactory_Success();
							return;
						}
					}

				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(TAG, "=============ClientProtocolException  = " + e.getMessage());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(TAG, "=============IOException  = " + e.getMessage());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "=============JSONException  = " + e.getMessage());

					e.printStackTrace();
				}
				if(null != iRecoveryFactoryCallback){
					iRecoveryFactoryCallback.recoveryFactory_Failed(retCode);
				}
			}
		}.start();
	}

	/**
	 * 获取当前用户的好友关系
	 *
	 * @param context
	 * @param callback
	 */
	public void getRelationship(final Context context, final GetRelationshipCallback callback) {
		Log.d(TAG, "getRelationship------isGetRelationship==" + isGetRelationship);
		if (isGetRelationship) {
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				isGetRelationship = true;
				String url = RequestHost.LOGIN_HOST + "/relationship/getMateInfo";
				Log.d(TAG, "getRelationship-------url==" + url);

				HttpGet httpRequest = new HttpGet(url);
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
				HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

				DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				try {
					if (mCookies != null) {
						for (int i = 0; i < mCookies.size(); i++) {
							httpRequest.addHeader("Cookie", mCookies.get(i).getName() + "=" + mCookies.get(i).getValue());
						}
					}
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					Log.d(TAG, "getRelationship--------statusCode==" + statusCode);
					if (statusCode == STATUS_CODE_OK) {

						isGetRelationship = false;
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if(null != callback){

								callback.getRelationship_NO();
							}
							return;
						}
						String strResult = EntityUtils.toString(entity);
						JSONObject jsonObject = new JSONObject(strResult);
						String retCode = jsonObject.getString(RETCODE);
						Log.d(TAG, "getRelationship---retCode==" + retCode + "----strResult==" + strResult);
						if (retCode.equals("0")) {
							String mateCupId = jsonObject.getString(MATE_ID);
							String mate_huanxinID = jsonObject.getJSONObject(CHAT).getString(MATE_HUANXIN_ID);
							String mate_nickname = jsonObject.getString(MATE_NICKNAME);
							String mate_headimage = jsonObject.getString(MATE_AVATORPATH);
							String mood = jsonObject.getString(MOOD);
							int intakegoal = jsonObject.getInt(INTAKEGOAL);
							OcupApplication.getInstance().mOtherCup.setHuanxin_userid(mate_huanxinID);
							if (mate_nickname.equals("null")) {
								OcupApplication.getInstance().mOtherCup.setName(UtilContact.DEFAULT_OCUP_NAME,7);
							} else {
								OcupApplication.getInstance().mOtherCup.setName(mate_nickname,8);
							}
							OcupApplication.getInstance().mOtherCup.setAvatorPath(mate_headimage);
							if (mood.equals("null")) {
								OcupApplication.getInstance().mOtherCup.setMood("");
							} else {
								OcupApplication.getInstance().mOtherCup.setMood(mood);
							}
							OcupApplication.getInstance().mOtherCup.setHuanxin_userid(mate_huanxinID);
							OcupApplication.getInstance().mOtherCup.setAvatorPath(mate_headimage);
							OcupApplication.getInstance().mOtherCup.setIntakegoal(intakegoal);

							OcupApplication.getInstance().mOtherCup.setBlueAdd("");
							OcupApplication.getInstance().mOtherCup.setCupID("");
							OcupApplication.getInstance().mOtherCup.setHuanxin_pwd("");
							OcupApplication.getInstance().mOtherCup.setEmail("");
							OcupApplication.getInstance().mOtherCup.setIntake(0);
							new DBManager(OcupApplication.getInstance()).addOtherCup(OcupApplication.getInstance().mOtherCup);
							callback.getRelationship_OK(mateCupId);
							isGetRelationship = false;
							return;
						} else if (retCode.equals("4001")) {// 未登陆
							isGetRelationship = false;
							callback.getRelationship_notLogin();
							reLogin(context, 3);
							Log.d(TAG, "getRelationship-------not login=");
							return;
						} else if (retCode.equals("3008")) {// 未配对
							callback.getRelationship_OK_noFriends();
							Log.d(TAG, "getRelationship-------OK_noFriends=");
							isGetRelationship = false;
							return;
						}
					}
				} catch (IOException e) {
					Log.d(TAG, "getRelationship--------IOException==" + e);
					e.printStackTrace();
				} catch (JSONException e) {
					Log.d(TAG, "getRelationship--------IOException==" + e);
					e.printStackTrace();
				}
				if(null != callback){

					callback.getRelationship_NO();
				}
				isGetRelationship = false;
			}
		}).start();
	}

	public void getAvatarToken(final Context context, final GetCupInfoCallback callback) {
		Log.d(TAG, "getUserInfo-------isGettingUserinfo==" + isGettingUserinfo + ":::: mode = ");
		if (isGettingUserinfo) {
			if (null != callback) {
				callback.GetCupInfoing();
			}
			return;
		}
		isGettingUserinfo = true;
		new Thread(new Runnable() {
			@Override
			public void run() {

				String url = RequestHost.avatar;
				Logger.e(TAG, "getUserInfo-------url==" + url);

				HttpGet httpRequest = new HttpGet(url);
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
				HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

				DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				try {
					if (mCookies != null) {
						for (int i = 0; i < mCookies.size(); i++) {
							httpRequest.addHeader("Cookie", mCookies.get(i).getName() + "=" + mCookies.get(i).getValue());
							Logger.e("mCookies != null");
						}
					}
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
					Logger.e(TAG, "getUserInfo--------statusCode==" + statusCode + "::::" + reasonPhrase);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if (null != callback) {
								callback.GetCupInfo_NO();
							}
							isGettingUserinfo = false;
							return;
						}
						String strResult = EntityUtils.toString(entity);
						JSONObject jsonObject = new JSONObject(strResult);

						Logger.e(TAG, "getUserInfo-------strResult==" + strResult);
						isGettingUserinfo = false;
						return;
					}
					else if (statusCode == 404){
						//找不到用户
						Logger.e("找不到用户");
					}
					else  if (statusCode == 500){
						Logger.e("服务器内部错误");
					}
				} catch (Exception e) {
					Logger.e("getUserInfo--------Exception==" + e);
					e.printStackTrace();
				}
				if (null != callback) {
					callback.GetCupInfo_NO();
				}
				isGettingUserinfo = false;
			}
		}).start();
	}

	/**
	 * 获取配对对方的饮水数据
	 *
	 * @param context
	 * @param start
	 * @param end
	 */
	public void getMateDrink(final Context context, final GetMateDrink callback, final long start, final long end) {
		new Thread(new Runnable() {

			@Override
			public void run() {

				String url = RequestHost.LOGIN_HOST + "/water/mateIntakeData/2?start=" + start + "&end=" + end;
				Log.d(TAG, "getMateDrink-------url==" + url);

				HttpGet httpRequest = new HttpGet(url);
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
				HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

				DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				try {
					if (mCookies != null) {
						for (int i = 0; i < mCookies.size(); i++) {
							httpRequest.addHeader("Cookie", mCookies.get(i).getName() + "=" + mCookies.get(i).getValue());
						}
					}
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					Log.d(TAG, "getMateDrink--------statusCode==" + statusCode);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if(null != callback){
								callback.getMateDrink_failed();
							}
							return;
						}
						String strResult = EntityUtils.toString(entity);
						JSONObject jsonObject = new JSONObject(strResult);
						int retCode = jsonObject.getInt(RETCODE);
						Log.d(TAG, "getMateDrink---retCode==" + retCode + "----strResult==" + strResult);
						if (retCode == 0) {
							int intakegoal = jsonObject.getInt(INTAKEGOAL);
							int intake = jsonObject.getInt(INTAKE);
							OcupApplication.getInstance().mOtherCup.setIntakegoal(intakegoal);
							OcupApplication.getInstance().mOtherCup.setIntake(intake);
							Log.d(TAG, "getMateDrink--intakegoal==" + intakegoal + "  intake==" + intake);
							callback.getMateDrink_success();
							return;
						} else if (retCode == 4001) {// 未登陆
							Log.d(TAG, "getMateDrink-------not login=");
							reLogin(context, 5);
						}
					}
				} catch (Exception e) {
					Log.d(TAG, "getMateDrink--------Exception==" + e);
					e.printStackTrace();
				}
				callback.getMateDrink_failed();
			}

		}).start();
	}

	/**
	 * 网络获取消息返回未登录信息后的操作（重新登录）
	 *
	 * @param context
	 */
	private void reLogin(final Context context, int mode) {
		Log.e(TAG, "-------------------login---------mode = " + mode);
		// 重新登录
		mCookies.clear();
		String phone = Tools.getPreference(context,UtilContact.Phone_Num);
		String code = Tools.getPreference(context,UtilContact.Phone_Code);
		login(context, phone, code, 0, new RequestCallback.ILoginCallBack() {
			@Override
			public void loginSuccess() {
				Logger.e("reLogin_____重新登录成功");
			}

			@Override
			public void loginFail(int type) {

			}

			@Override
			public void RegisterSuccess() {

			}

			@Override
			public void RegisterFail(int type) {

			}
		});
	}

	/**
	 * 更新用户信息
	 *
	 * @param context
	 * @param callback
	 */
	public void updateUserInfo(final Context context, final UpdateUserInfoCallback callback, final String name, final String mood) {
		Log.d(TAG, "updateUserInfo------  =====");
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = RequestHost.LOGIN_HOST + "/profile/updateUserInfo";
				Log.d(TAG, "updateUserInfo-------url==" + url);
				HttpPost httpRequest = new HttpPost(url);
				// 封装数据
				Map<String, String> parmas = new HashMap<String, String>();
				parmas.put(NICKNAME, name);
				parmas.put(MOOD, mood);
				// parmas.put("email",cup.getEmail());
				ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
				if (parmas != null) {
					Set<String> keys = parmas.keySet();
					for (Iterator<String> i = keys.iterator(); i.hasNext();) {
						String key = (String) i.next();
						pairs.add(new BasicNameValuePair(key, parmas.get(key)));
					}
				}
				try {
					UrlEncodedFormEntity p_entity = new UrlEncodedFormEntity(pairs, "utf-8");
					httpRequest.setEntity(p_entity);
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
					HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

					DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
					if (mCookies != null) {
						for (int i = 0; i < mCookies.size(); i++) {
							httpRequest.addHeader("Cookie", mCookies.get(i).getName() + "=" + mCookies.get(i).getValue());
							Log.e(TAG, "---------updateUserInfo = " + mCookies.get(i).getName() + ":::" +
									mCookies.get(i).getValue());
						}
					}
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					Log.d(TAG, "updateUserInfo--------statusCode==" + statusCode);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if(null != callback){

								callback.updateCupInfo_NO();
							}
							return;
						}
						String strResult = EntityUtils.toString(entity);
						Log.d(TAG, "updateUserInfo-------strResult==" + strResult);
						JSONObject jsonObject = new JSONObject(strResult);
						int retCode = jsonObject.getInt(RETCODE);
						if (0 == retCode) {
							Log.d(TAG, "updateUserInfo-------OK==");
							callback.updateCupInfo_OK();
							return;
						} else if (4001 == retCode) {
							reLogin(context ,6);
						}
					}
				} catch (Exception e) {
					Log.d(TAG, "updateUserInfo--------JSONException==" + e);
					e.printStackTrace();
				}
				if(null != callback){

					callback.updateCupInfo_NO();
				}
			}
		}).start();
	}

	public void uploadUserImage(final Context context, final UploadUserImageCallback callback, final Bitmap bmp) {
		Log.d(TAG, "uploadUserImage------  =====" + (context.getCacheDir().getAbsolutePath() + "/headimage.jpg"));
		Log.e(TAG, "----------bmp size = " + bmp.getByteCount());
		new Thread(new Runnable() {
			@Override
			public void run() {
				Bitmap headBmp = bmp;
				// 将bitmap写入到文件
				File picture = new File(context.getCacheDir().getAbsolutePath() + "/headimage.jpg");
				if (!picture.exists()) {
					try {
						picture.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
						Log.d(TAG, "uploadUserImage------  =====IOException e=" + e);
					}
				}
				FileOutputStream out;
				try {
					out = new FileOutputStream(picture);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					headBmp = BitmapUtil.resizeBitmap(headBmp, baos, 200 * 1024);
					out.write(baos.toByteArray());
					baos.close();
					out.close();
				} catch (FileNotFoundException e1) {
					Log.d(TAG, "uploadUserImage------  =====FileNotFoundException e1=" + e1);
					e1.printStackTrace();
				} catch (IOException e) {
					Log.d(TAG, "uploadUserImage------  =====IOException e===" + e);
					e.printStackTrace();
				}

				// 上传
				String url = RequestHost.LOGIN_HOST + "/profile/avator";
				HttpPost httppost = new HttpPost(url);
				MultipartEntity mpEntity = new MultipartEntity();
				if (mCookies != null) {
					for (int i = 0; i < mCookies.size(); i++) {
						httppost.addHeader("Cookie", mCookies.get(i).getName() + "=" + mCookies.get(i).getValue());
					}
				}
				try {
					if (!picture.exists()) {
						Log.d(TAG, "uploadUserImage------imageFile  not exit  =====" + (context.getCacheDir().getAbsolutePath() + "/headimage.jpg"));
						callback.uploadUserImage_NO();
						return;
					}
					FileBody file = new FileBody(picture);
					mpEntity.addPart("avator", file);
					httppost.setEntity(mpEntity);
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
					HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);
					DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
					HttpResponse response = httpClient.execute(httppost);
					int code = response.getStatusLine().getStatusCode();
					Log.d(TAG, "uploadUserImage   code-===" + code);
					if (code == STATUS_CODE_OK) {
						HttpEntity entity = response.getEntity();
						if(null == entity){
							if(null != callback){

								callback.uploadUserImage_NO();
							}
							return;
						}
						String str_result = EntityUtils.toString(entity);
						Log.d(TAG, "uploadUserImage------str_result=====" + str_result);
						JSONObject jsonObject = new JSONObject(str_result);
						int retCode = jsonObject.getInt(RETCODE);
						if (0 == retCode) {
							Log.d(TAG, "uploadUserImage-------OK==");
							callback.uploadUserImage_OK();
							String avatorPath = jsonObject.getString("path");

							OcupApplication.getInstance().mOwnCup = new DBManager(OcupApplication.getInstance()).queryOwnCup(OcupApplication.getInstance().mOwnCup.getCupID());
							OcupApplication.getInstance().mOwnCup.setAvatorPath(avatorPath);
							new DBManager(OcupApplication.getInstance()).updateOwnCup(OcupApplication.getInstance().mOwnCup);

							TipsBitmapLoader.getInstance().addToCache(url, headBmp);
							// 删除原图片文件
							picture.delete();
							return;
						} else if (4001 == retCode) {
							reLogin(context, 7);
						}
					}
				} catch (Exception e) {
				}
				if(null != callback){

					callback.uploadUserImage_NO();
				}
			}
		}).start();
	}

	/**
	 * 上传饮水数据
	 *
	 * @param drinkdatas
	 */
	public void uploadDrink(final ArrayList<DrinkData> drinkdatas) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = RequestHost.LOGIN_HOST + "/water/intakeData/2";
				Log.d(TAG, "uploadDrink-------url==" + url + "  " + drinkdatas.size());
				HttpPost httpRequest = new HttpPost(url);
				if (mCookies != null) {
					for (int i = 0; i < mCookies.size(); i++) {
						httpRequest.addHeader("Cookie", mCookies.get(i).getName() + "=" + mCookies.get(i).getValue());
					}
				}
				// 封装数据
				try {
					JSONArray jsonArray = new JSONArray();
					for (int i = 0; i < Math.min(drinkdatas.size(), 240); i++) {
						JSONArray jArray = new JSONArray();
						jArray.put(drinkdatas.get(i).getDrink_date() / 1000 + drinkdatas.get(i).getDrink_time());
						jArray.put(drinkdatas.get(i).getWater_yield());
						jsonArray.put(jArray);
					}
					Log.d(TAG, "uploadDrink------jsonArray====" + "" + jsonArray.toString());
					StringEntity entity = new StringEntity(jsonArray.toString(), "utf-8");
					entity.setContentType("application/json");
					httpRequest.setEntity(entity);
					Log.d(TAG, "uploadDrink-----url==" + httpRequest.getEntity().toString());

					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
					HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

					DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();

					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					Log.d(TAG, "uploadDrink--------statusCode==" + statusCode);

					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity2 = httpResponse.getEntity();
						if(null == entity2){
							return;
						}
						String strResult = EntityUtils.toString(entity2);
						JSONObject jsonObject = new JSONObject(strResult);
						int retCode = jsonObject.getInt(RETCODE);
						Log.d(TAG, "uploadDrink-----retCode==" + retCode);
						if (retCode == 0) {
							new DBManager(OcupApplication.getInstance()).setDrinkNeedSrv(drinkdatas);
							return;
						} else if (4001 == retCode) {
							reLogin(OcupApplication.getInstance(), 8);
						}
					}
				} catch (Exception e) {
					Log.d(TAG, "uploadDrink--------JSONException==" + e);
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * 下载饮水数据
	 *
	 */
	public void getDrink(final GetDrinkCallback callback, final long start, final long end) {
		Log.d(TAG, "getDrink-------start==" + start + "    end==" + end);
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = RequestHost.LOGIN_HOST + "/water/intakeData/2?start=" + start + "&end=" + end;//
				Log.d(TAG, "getDrink-------url==" + url);

				HttpGet httpRequest = new HttpGet(url);
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
				HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

				DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				try {
					if (mCookies != null) {
						for (int i = 0; i < mCookies.size(); i++) {
							httpRequest.addHeader("Cookie", mCookies.get(i).getName() + "=" + mCookies.get(i).getValue());
						}
					}
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					Log.d(TAG, "getDrink--------statusCode==" + statusCode);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if(null != callback){
								callback.getdrinkr_failed();
							}
							return ;
						}
						String strResult = EntityUtils.toString(entity);
						JSONObject jsonObject = new JSONObject(strResult);
						int retCode = jsonObject.getInt(RETCODE);
						JSONArray jsonArray = new JSONArray(jsonObject.getString(INTAKEDATA));
						Log.d(TAG, "getDrink---retCode==" + retCode + "----strResult==" + strResult);// INTAKETIME
						// INTAKE_PER
						if (retCode == 0) {
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONArray reslut = (JSONArray) jsonArray.opt(i);
								int time = reslut.optInt(0);
								int intake = reslut.optInt(1);
								Log.d(TAG, "getDrink--time==" + time + "  intake==" + intake);
								long t = time * 1000l;
								DBManager mDBManager = new DBManager(OcupApplication.getInstance());
								DrinkData mDrinkData = new DrinkData();
								mDrinkData.setCupid(OcupApplication.getInstance().mOwnCup.getCupID());
								mDrinkData.setWater_yield(intake);
								mDrinkData.setDrink_time(Tools.getSecond(t));
								mDrinkData.setDrink_date(t);
								mDrinkData.setWater_temp(0);
								// 更新数据库
								mDBManager.updateDrinkFromSrv(mDrinkData);
							}
							callback.getdrinkr_success();
							return;
						} else if (retCode == 4001) {// 未登陆
							Log.d(TAG, "getDrink-------not login=");
							reLogin(OcupApplication.getInstance(), 9);
						}
					}
				} catch (Exception e) {
					Log.d(TAG, "getDrink--------Exception==" + e);
					e.printStackTrace();
				}
				if(null != callback){
					callback.getdrinkr_failed();
				}
			}

		}).start();
	}
	/**
	 * 检测服务端apk版本及更新内容，获取服务端新版本下载地址
	 *
	 */
	public void getAPKVersion(final GetAPKVersionCallback callback) {
		Log.d(TAG, "getAPKVersion-------");
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = RequestHost.APK_HOST;
				HttpGet httpRequest = new HttpGet(url);
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
				HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

				DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				try {
					if (mCookies != null) {
						for (int i = 0; i < mCookies.size(); i++) {
							httpRequest.addHeader("Cookie", mCookies.get(i).getName() + "=" + mCookies.get(i).getValue());
						}
					}
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					Log.d(TAG, "getAPKVersion--------statusCode==" + statusCode);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if(null != callback){

								callback.getApkversion_failed();
							}
							return;
						}
						String strResult = EntityUtils.toString(entity, "utf-8");
						JSONObject jsonObject = new JSONObject(strResult);
						String version = jsonObject.getString(APK_VERSION);
						int versioncode = jsonObject.getInt(APK_VERSIONCODE);
						String apk_path = jsonObject.getString(APK_PATH);
						String apk_info = jsonObject.getString(APK_INfo);
						APKVersionInfo.getInstance().setSrv_version(version);
						APKVersionInfo.getInstance().setSrv_versionCode(versioncode);
						APKVersionInfo.getInstance().setSrv_apkPath(apk_path);
						APKVersionInfo.getInstance().setSrv_versionInfo(apk_info);
						Log.d(TAG, "getAPKVersion---version==" + version + "------versioncode== " + versioncode + "----apk_path==" + apk_path);
						callback.getApkversion_success();
						return;
					}
				} catch (Exception e) {
					Log.d(TAG, "getAPKVersion--------Exception==" + e);
					e.printStackTrace();
				}
				if(null != callback){

					callback.getApkversion_failed();
				}
			}

		}).start();
	}

	/**
	 * 请求返回正常状态码
	 */
	private final static int STATUS_CODE_OK = 200;

	public final static int NET_NOTCONNECT = 100000;

	/**
	 * 返回码
	 */
	private final String RETCODE = "retCode";
	/**
	 * 获取用户信息返回码
	 */
	private final String CUPID = "cupID";
	private final String NICKNAME = "nickname";
	private final String EMAIL = "email";
	private final String AVATORPATH = "avatorPath";
	private final String MOOD = "mood";
	private final String INTAKEGOAL = "intakeGoal";
	private final String INTAKE = "amount";
	/**
	 * 获取饮水数据返回码[timestamp,intake],
	 */
	private final String INTAKEDATA = "data";
	private final String INTAKETIME = "timestamp";
	private final String INTAKE_PER = "intake";
	/**
	 * 获取apk版本返回码[timestamp,intake],
	 */
	private final String APK_VERSION = "version";
	private final String APK_VERSIONCODE = "versionCode";
	private final String APK_PATH = "path";
	private final String APK_INfo = "detail";

	/**
	 * 登录到appsrv返回码
	 */
	private final String LOGINID = "id";
	private final String CHAT_INFO = "chat";
	private final String HUANXIN_USERID = "userID";
	private final String HUANXIN_PWD = "password";
	/**
	 * 获取好友列表信息返回码
	 */
	private final String MATE_ID = "mateUserID";
	private final String CHAT = "chat";
	private final String MATE_HUANXIN_ID = "userID";
	private final String MATE_NICKNAME = "mateNickname";
	private final String MATE_AVATORPATH = "mateAvatorPath";
	/**
	 * 请求tip返回的状态标签
	 */
	private final String IMAGEPATH = "image_download_root";
	private final String RESULTS = "results";
	private final String ID = "_id";
	private final String TITLE = "title";
	private final String BRIEF = "abstract";
	private final String DATE = "date";
	private final String IMAGE_NAME = "pickey";

	/*
	* yiliRequest
	* */
	/**
	 * 注册
	 *
	 */
	public void register(final Context appContext, final String phone, final String code, final RequestCallback.ILoginCallBack callback) {
		Logger.e(TAG, "register------   cupId===" + "::::mCookies.size() = " + mCookies.size());
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = RequestHost.registerUrl;
				Logger.e(TAG, "register-------url==" + url);
				HttpPost httpRequest = new HttpPost(url);
				// 封装数据
				try {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("zone", "86");
					jsonObject.put("phone", phone);
					jsonObject.put("code", code);
//					jsonObject.put("platform", "mob_android");
					jsonObject.put("platform", "debug");

					StringEntity p_entity = new StringEntity(jsonObject.toString());
					//setContentType一定要加上，否则服务器不能判定为是json数据格式
					p_entity.setContentType("application/json");//发送json数据需要设置contentType
					httpRequest.setEntity(p_entity);

					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
					HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

//					DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
					//解决https网络证书认证问题
					DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					Logger.e( "register--------statusCode==" + statusCode);
					if (statusCode == STATUS_CODE_OK) {
						mCookies.clear();
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							return;
						}
						String strResult = EntityUtils.toString(entity);
						CookieStore mCookieStore = ((AbstractHttpClient) httpClient).getCookieStore();
						List<Cookie> cookies = mCookieStore.getCookies();
						if (cookies.isEmpty()) {
							Logger.e(TAG, "-register------Cookie NONE---------");
						} else {
							for (int i = 0; i < cookies.size(); i++) {
								// 保存cookie
								Cookie cookie = cookies.get(i);
								Logger.e(TAG, "register 中Cookies：" + cookies.get(i).getName() + "=" + cookies.get(i).getValue());
								mCookies.add(cookie);
								String cookieContent = cookies.get(i).getName()+"="+cookies.get(i).getValue();
								Tools.savePreference(appContext,UtilContact.Cookies,cookieContent);
							}
						}

						JSONObject jsonObject1 = new JSONObject(strResult);
//						Gson gson = new Gson();
//						RegisterInfo registerInfo = gson.fromJson(strResult,RegisterInfo.class);
						Logger.e(TAG, "register-------strResult==" + strResult);
						UserInfo userInfo = new UserInfo();
						userInfo.setUserID(jsonObject1.getInt("userID"));
						userInfo.setZone(jsonObject1.getString("zone"));
						userInfo.setNickname( jsonObject1.getString("nickname"));
						userInfo.setPhone(jsonObject1.getString("phone"));
						userInfo.setPassword(jsonObject1.getString("password"));
//						userInfo.setMood(jsonObject1.getString("mood"));
//						userInfo.setEmail(jsonObject1.getString("email"));
//						userInfo.setGroup_id(jsonObject1.getString("group_id"));
//						userInfo.setAvatorThumbnail("");
						DBManager manager = new DBManager(appContext);
						UserInfo userInfo1 = manager.queryYiLiCup();
						if (userInfo1 == null){
							//数据库中不存在数据则添加，该数据表只存储一个数据
							manager.addYiLiCup(userInfo);
						}
						else  if (userInfo1.getUserID() != userInfo.getUserID()){
							manager.updateYiLicup(userInfo1.getUserID()+"",userInfo);
							//把所有的好友列表删除
							manager.deleteFriends();
						}
						else if (userInfo1.getUserID() == userInfo.getUserID()){
							manager.updateYiLicup(userInfo1.getUserID()+"",userInfo);
						}
						Logger.e(userInfo.toString());
						String id = jsonObject1.getString("phone");
						String huanxinID = jsonObject1.getInt("userID")+"";
						String huanxinPwd = jsonObject1.getString("password");
						OcupApplication.getInstance().mOwnCup = new DBManager(OcupApplication.getInstance()).queryOwnCup(OcupApplication.getInstance().mOwnCup.getCupID());
						OcupApplication.getInstance().mOwnCup.setHuanxin_userid(huanxinID);
						OcupApplication.getInstance().mOwnCup.setHuanxin_pwd(huanxinPwd);
						Tools.savePreference(appContext, UtilContact.HuanXinId, huanxinID);
						Tools.savePreference(appContext, UtilContact.HuanXinPWD, huanxinPwd);
//							new DBManager(OcupApplication.getInstance()).updateOwnCup(OcupApplication.getInstance().mOwnCup);
						//登录sen5服务器成功后则登录到环信的服务器
						HuanxinUtil.getInstance().login(appContext, huanxinID, huanxinPwd);
//							getUserInfo(appContext, null);
						//保存好用户注册时的号码和验证码,在startUpActivity页面以此判断是否已经注册来决定进入哪个页面
						Tools.savePreference(appContext,UtilContact.Phone_Num,phone);
						Tools.savePreference(appContext,UtilContact.Phone_Code,code);
						//注成功册回调，在该回调中进行登录操作
						callback.RegisterSuccess();
						return;
					} else {
						callback.RegisterFail(statusCode);
						mCookies.clear();
					}
				} catch (Exception e) {
					callback.RegisterFail(400);
					Logger.e("register--------Exception==" + e);
					e.printStackTrace();
				}
			}
		}).start();
	}

	//登录
	public void login(final Context appContext, final String phone, final String code, final int loginCount, final RequestCallback.ILoginCallBack callback) {
		if (mCookies.size() > 0) {
			Logger.e("login------ " + mCookies.get(0).getName() + mCookies.get(0).getValue());
		}
//		if (mCookies.size() > 0) {
//			callback.loginSuccess();
//			return;
//		}
//		else {
//			Logger.e("login------mCookies.size = 0");
//		}
//		if (!BluetoothConnectUtils.getInstance().isRunFront) {
//			return;
//		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				// String url = RequestHost.LOGIN_HOST +
				// "/users/loginWithCupID/" + cupId;
				//http://api.otelligent.com/users/loginWithCupID
				String url = RequestHost.login;
				Logger.e(TAG, "login-------url==" + url);
				HttpPost httpRequest = new HttpPost(url);
				// 封装数据
				try {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("zone", "86");
					jsonObject.put("phone",phone);
					jsonObject.put("code", code);
					jsonObject.put("platform", "mob_android");
//					jsonObject.put("platform", "debug");

					StringEntity p_entity = new StringEntity(jsonObject.toString());
					//setContentType一定要加上，否则服务器不能判定为是json数据格式
					p_entity.setContentType("application/json");//发送json数据需要设置contentType
					httpRequest.setEntity(p_entity);
					HttpParams httpParameters = new BasicHttpParams();
					DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();

					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					Logger.e( "login--------statusCode==" + statusCode);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if (loginCount < 5) {
								login(appContext, phone, code,loginCount + 1,callback);
							}
							return;
						}
						String strResult = EntityUtils.toString(entity);
						CookieStore mCookieStore = ((AbstractHttpClient) httpClient).getCookieStore();
						List<Cookie> cookies = mCookieStore.getCookies();
						if (cookies.isEmpty()) {
							Logger.e(TAG, "-login------Cookie NONE---------");
						} else {
							for (int i = 0; i < cookies.size(); i++) {
								// 保存cookie
								Cookie cookie = cookies.get(i);
								Logger.e(TAG, "login-------" + cookie.getName()+cookie.getValue());
								mCookies.add(cookie);
								String cookieContent = cookies.get(i).getName()+"="+cookies.get(i).getValue();
								Tools.savePreference(appContext,UtilContact.Cookies,cookieContent);
							}
						}
						JSONObject jsonObject1 = new JSONObject(strResult);
//						Gson gson = new Gson();
//						UserInfo userInfo = gson.fromJson(strResult,UserInfo.class);
						UserInfo userInfo = new UserInfo();
						userInfo.setAvator(jsonObject1.getString("avator"));
						userInfo.setUserID(jsonObject1.getInt("userID"));
						userInfo.setZone(jsonObject1.getString("zone"));
						userInfo.setNickname(jsonObject1.getString("nickname"));
						userInfo.setPhone(jsonObject1.getString("phone"));
						userInfo.setPassword(jsonObject1.getString("password"));
						userInfo.setMood(jsonObject1.getString("mood"));
						userInfo.setEmail(jsonObject1.getString("email"));
						userInfo.setGroup_id(jsonObject1.getString("group_id"));
						userInfo.setAvatorThumbnail("");
						DBManager manager = new DBManager(appContext);
						UserInfo userInfo1 = manager.queryYiLiCup();
						if (userInfo1 == null){
							manager.addYiLiCup(userInfo);
						}
						else  if (userInfo1.getUserID() != userInfo.getUserID()){
							manager.updateYiLicup(userInfo1.getUserID()+"",userInfo);
							manager.deleteFriends();
						}
						else if (userInfo1.getUserID() == userInfo.getUserID()){
							manager.updateYiLicup(userInfo1.getUserID()+"",userInfo);
						}
						Logger.e(TAG, "login-------strResult==" + strResult);
						String id = jsonObject1.getString("phone");
						String huanxinID = jsonObject1.getInt("userID")+"";
						String huanxinPwd = jsonObject1.getString("password");
						OcupApplication.getInstance().mOwnCup = new DBManager(OcupApplication.getInstance()).queryOwnCup(OcupApplication.getInstance().mOwnCup.getCupID());
						OcupApplication.getInstance().mOwnCup.setHuanxin_userid(huanxinID);
						OcupApplication.getInstance().mOwnCup.setHuanxin_pwd(huanxinPwd);
						Tools.savePreference(appContext, UtilContact.HuanXinId, huanxinID);
						Tools.savePreference(appContext, UtilContact.HuanXinPWD, huanxinPwd);
//							new DBManager(OcupApplication.getInstance()).updateOwnCup(OcupApplication.getInstance().mOwnCup);
						HuanxinUtil.getInstance().login(appContext, huanxinID, huanxinPwd);
						Tools.savePreference(appContext,UtilContact.Phone_Num,phone);
						callback.loginSuccess();
						return;
					} else {
						callback.loginFail(statusCode);
						return;
					}
				} catch (Exception e) {
					Logger.e("login--------Exception==" + e);
					e.printStackTrace();
				}
				Logger.e(TAG, "login-------NO==  count_loginFailed==" + loginCount);
				if (loginCount < 5) {
					login(appContext, phone,code, loginCount + 1,callback);
				}
				else {
					callback.loginFail(UtilContact.loginFail);
				}
			}
		}).start();
	}

	//获取用户信息
	public void getUserInfo(final Context context, final RequestCallback.IGetInfoCallBack callback) {
		final int type = UtilContact.getUserInfo;
		if (mCookies.size() > 0) {
			Logger.e("getUserInfo", "Cookies：" + mCookies.get(0).getName() + "=" + mCookies.get(0).getValue());
		}
		else {
			Logger.e("mCookies.size = 0");
		}
		if (isGettingUserinfo) {
			if (null != callback) {
				callback.getIng(type);
			}
			return;
		}
		isGettingUserinfo = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = RequestHost.updateInfo;
				Logger.e(TAG, "getUserInfo-------url==" + url);
				HttpGet httpRequest = new HttpGet(url);
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
				HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

				DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();

				try {
					if (mCookies != null && mCookies.size()>0) {
						for (int i = 0; i < mCookies.size(); i++) {
							httpRequest.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
							Logger.e("cookies加入成功 Cookie = "+Tools.getPreference(context,UtilContact.Cookies));
							break;
						}
					}
					else{
						Logger.e("mCookies为null,使用sharePrefrence Cookie = "+Tools.getPreference(context,UtilContact.Cookies));
						httpRequest.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
					}

					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
					Logger.e(TAG, "getUserInfo--------statusCode==" + statusCode + "::::" + reasonPhrase);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if (null != callback) {
								callback.getFail(type);
							}
							isGettingUserinfo = false;
							return;
						}
						String strResult = EntityUtils.toString(entity);

						CookieStore mCookieStore = ((AbstractHttpClient) httpClient).getCookieStore();
						List<Cookie> cookies = mCookieStore.getCookies();
						if (cookies.isEmpty()) {
							Logger.e(TAG, "getUserInfo------Cookie NONE---------");
						} else {
							mCookies.clear();
							for (int i = 0; i < cookies.size(); i++) {
								// 保存cookie
								Cookie cookie = cookies.get(i);
								Logger.e(TAG, "getUserInfo-------" + cookie.getName()+cookie.getValue());
								mCookies.add(cookie);
								String cookieContent = cookies.get(i).getName()+"="+cookies.get(i).getValue();
								Tools.savePreference(context,UtilContact.Cookies,cookieContent);
							}
						}

						Logger.e(TAG, "getUserInfo-------strResult==" + strResult);
						JSONObject jsonObject = new JSONObject(strResult);
//						Gson gson = new Gson();
//						UserInfo userInfo = gson.fromJson(strResult,UserInfo.class);
						UserInfo userInfo = new UserInfo();
												userInfo.setUserID(jsonObject.getInt("userID"));
						userInfo.setZone(jsonObject.getString("zone"));
						userInfo.setAvator(jsonObject.getString("avator"));
						userInfo.setNickname(jsonObject.getString("nickname"));
						userInfo.setPhone(jsonObject.getString("phone"));
						userInfo.setPassword(jsonObject.getString("password"));
						userInfo.setMood(jsonObject.getString("mood"));
						userInfo.setEmail(jsonObject.getString("email"));
						userInfo.setGroup_id(jsonObject.getString("group_id"));
						userInfo.setAvatorThumbnail("");
						DBManager manager = new DBManager(context);
						UserInfo userInfo1 = manager.queryYiLiCup();
						if (userInfo1 == null){
							manager.addYiLiCup(userInfo);
						}
						else {
							manager.updateYiLicup(userInfo1.getUserID()+"",userInfo);
						}
						String nickname = jsonObject.getString("nickname");
						String avatorPath = jsonObject.getString("avator");
						OcupApplication.getInstance().mOwnCup.setName(nickname,6);
						OcupApplication.getInstance().mOwnCup.setAvatorPath(avatorPath);
						Tools.savePreference(context,UtilContact.OwnAvatar,avatorPath);
						//以防注册时发生意外未及时保存到环信的id和密码，所以在取用户信息时保存一次
						Tools.savePreference(context, UtilContact.HuanXinId, jsonObject.getString("userID"));
						Tools.savePreference(context, UtilContact.HuanXinPWD, jsonObject.getString("password"));
						isGettingUserinfo = false;
						callback.getSuccess(type,strResult);
						return;
					}
					else {
						callback.getFail(statusCode);
					}
				} catch (Exception e) {
					callback.getFail(501);
					Logger.e("getUserInfo--------Exception==" + e);
					e.printStackTrace();
				}
				if (null != callback) {
					callback.getFail(type);
				}
				isGettingUserinfo = false;
			}
		}).start();
	}

	//获取用户好友列表
	public void getUserFriends(final Context context, final RequestCallback.IGetInfoCallBack callback) {
		if (mCookies.size() > 0) {
			Logger.e("getUserFriends", "Cookies：" + mCookies.get(0).getName() + "=" + mCookies.get(0).getValue());
		}
		else {
			Logger.e("mCookies.size = 0");
		}
		final int type = UtilContact.getFriendInfo;
		if (isGettingUserinfo) {
			if (null != callback) {
				callback.getIng(type);
			}
			return;
		}
		isGettingUserinfo = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = RequestHost.friendList;
				Logger.e(TAG, "getFriendInfo-------url==" + url);

				HttpGet httpRequest = new HttpGet(url);
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
				HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

				DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				try {
					httpRequest.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
					Logger.e("getUserFriends",Tools.getPreference(context,UtilContact.Cookies));
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
					Logger.e(TAG, "getFriendInfo--------statusCode==" + statusCode + "::::" + reasonPhrase);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if (null != callback) {
								callback.getFail(type);
							}
							isGettingUserinfo = false;
							return;
						}

						CookieStore mCookieStore = ((AbstractHttpClient) httpClient).getCookieStore();
						List<Cookie> cookies = mCookieStore.getCookies();
						if (cookies.isEmpty()) {
							Logger.e(TAG, "getFriendInfo------Cookie NONE---------");
						} else {
							mCookies.clear();
							for (int i = 0; i < cookies.size(); i++) {
								// 保存cookie
								Cookie cookie = cookies.get(i);
								Logger.e(TAG, "getFriendInfo-------" + cookie.getName()+cookie.getValue());
								mCookies.add(cookie);
								String cookieContent = cookies.get(i).getName()+"="+cookies.get(i).getValue();
								Tools.savePreference(context,UtilContact.Cookies,cookieContent);
							}
						}
						String strResult = EntityUtils.toString(entity);
						Logger.e("getFriendInfo-------strResult==" + strResult);
						JSONArray jsonArray = new JSONArray(strResult);
						DBManager dbManager = new DBManager(context);
						List<FriendInfo> friendInfos = new ArrayList<FriendInfo>();
						for (int i = 0; i < jsonArray.length(); i++){
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							String contact_id = jsonObject.getInt("contact_id")+"";
							String group_id = jsonObject.getString("group_id");
							String avator = jsonObject.getString("avator");
							String nickName = jsonObject.getString("nickname");
							String phoneNum = jsonObject.getString("phone");
							Logger.e("HttpRequest",nickName+"==========="+nickName);
							FriendInfo friendInfo = new FriendInfo(contact_id,group_id,avator,nickName,phoneNum);
							friendInfos.add(friendInfo);
						}
						dbManager.addYiLiFriend(friendInfos);
						isGettingUserinfo = false;
						callback.getSuccess(type,strResult);
						return;
					}
					else {
						callback.getFail(statusCode);
					}
				} catch (Exception e) {
					Logger.e("getFriendInfo--------Exception==" + e);
					e.printStackTrace();
				}
				if (null != callback) {
					callback.getFail(type);
				}
				isGettingUserinfo = false;
			}
		}).start();
	}


	//获取用户好友列表
	public void updateUserInfo(final Context context, final String nickName, final RequestCallback.IUpdateInfoCallBack callback) {
		if (mCookies.size() > 0) {
			Logger.e("updateUserInfo", "Cookies：" + mCookies.get(0).getName() + "=" + mCookies.get(0).getValue());
		}
		else {
			Logger.e("mCookies.size = 0");
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = RequestHost.updateInfo;
				Logger.e(TAG, "updateUserInfo-------url==" + url);

				HttpPost httpRequest = new HttpPost(url);
				try {
					JSONObject jsonObject2 = new JSONObject();
					jsonObject2.put("nickname", nickName);
					StringEntity p_entity = new StringEntity(jsonObject2.toString(),"utf-8");
					//setContentType一定要加上，否则服务器不能判定为是json数据格式
					p_entity.setContentType("application/json");//发送json数据需要设置contentType
					httpRequest.setEntity(p_entity);

					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
					HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

					DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();

					if (mCookies != null && mCookies.size() == 1) {
						for (int i = 0; i < mCookies.size(); i++) {
							httpRequest.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
							Logger.e(TAG,"cookies加入成功"+Tools.getPreference(context,UtilContact.Cookies));
							break;
						}
					}
					else {
						Logger.e(TAG,"mCookies为null,使用sharePrefrence"+Tools.getPreference(context,UtilContact.Cookies));
						httpRequest.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
					}
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
					Logger.e(TAG, "updateUserInfo--------statusCode==" + statusCode + "::::" + reasonPhrase);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if (null != callback) {
								callback.updateFail(500);
							}
							return;
						}

						CookieStore mCookieStore = ((AbstractHttpClient) httpClient).getCookieStore();
						List<Cookie> cookies = mCookieStore.getCookies();
						if (cookies.isEmpty()) {
							Logger.e(TAG, "updateUserInfo------Cookie NONE---------");
						} else {
							mCookies.clear();
							for (int i = 0; i < cookies.size(); i++) {
								// 保存cookie
								Cookie cookie = cookies.get(i);
								Logger.e(TAG, "updateUserInfo-------" + cookie.getName()+cookie.getValue());
								mCookies.add(cookie);
								String cookieContent = cookies.get(i).getName()+"="+cookies.get(i).getValue();
								Tools.savePreference(context,UtilContact.Cookies,cookieContent);
							}
						}
						String strResult = EntityUtils.toString(entity);
						Logger.e("updateUserInfo-------strResult==" + strResult);
						DBManager dbManager = new DBManager(context);
						JSONObject jsonObject = new JSONObject(strResult);
						//更新数据库
						dbManager.updateNickNameAndMood(jsonObject.getString("userID"),jsonObject.getString("nickname"),jsonObject.getString("mood"));
						callback.updateSuccess();
						return;
					}
					else {
						callback.updateFail(statusCode);
					}
				} catch (Exception e) {
					Logger.e("getFriendInfo--------Exception==" + e);
					e.printStackTrace();
				}
				if (null != callback) {
					callback.updateFail(500);
				}
			}
		}).start();
	}


	//添加好友
	public void addFriendRequest(final Context context, final String huanxinId, final RequestCallback.IAddFriendCallBack callback) {
		if (mCookies.size() > 0) {
			Logger.e("addFriendRequest", "Cookies：" + mCookies.get(0).getName() + "=" + mCookies.get(0).getValue());
		}
		else {
			Logger.e("mCookies.size = 0");
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = RequestHost.addFriend;
				Logger.e(TAG, "addFriendRequest-------url==" + url);

				HttpPost httpRequest = new HttpPost(url);
				try {
					JSONObject jsonObject2 = new JSONObject();
					jsonObject2.put("contactID", huanxinId);
					StringEntity p_entity = new StringEntity(jsonObject2.toString(),"utf-8");
					//setContentType一定要加上，否则服务器不能判定为是json数据格式
					p_entity.setContentType("application/json");//发送json数据需要设置contentType
					httpRequest.setEntity(p_entity);

					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
					HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

					DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();

					if (mCookies != null && mCookies.size()>0) {
						for (int i = 0; i < mCookies.size(); i++) {
							httpRequest.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
							Logger.e("cookies加入成功");
							break;
						}
					}
					else{
						Logger.e("mCookies为null,使用sharePrefrence");
						httpRequest.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
					}
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
					Logger.e(TAG, "addFriendRequest--------statusCode==" + statusCode + "::::" + reasonPhrase);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if (null != callback) {
								callback.sendFail(500);
							}
							return;
						}

						CookieStore mCookieStore = ((AbstractHttpClient) httpClient).getCookieStore();
						List<Cookie> cookies = mCookieStore.getCookies();
						if (cookies.isEmpty()) {
							Logger.e(TAG, "addFriendRequest------Cookie NONE---------");
						} else {
							mCookies.clear();
							for (int i = 0; i < cookies.size(); i++) {
								// 保存cookie
								Cookie cookie = cookies.get(i);
								Logger.e(TAG, "addFriendRequest-------" + cookie.getName()+cookie.getValue());
								mCookies.add(cookie);
								String cookieContent = cookies.get(i).getName()+"="+cookies.get(i).getValue();
								Tools.savePreference(context,UtilContact.Cookies,cookieContent);
							}
						}
						String strResult = EntityUtils.toString(entity);
						Logger.e("addFriendRequest-------strResult==" + strResult);
						DBManager dbManager = new DBManager(context);
						JSONObject jsonObject = new JSONObject(strResult);
						String token = jsonObject.getString("token");
						callback.sendSuccess(token);
						return;
					}
					else if (statusCode == 403){
						//已经是好友关系了
						callback.hasAdded();
					}
					else  if (statusCode == 500){
						Logger.e("服务器内部错误");
					}
					else if (statusCode == 401){
						callback.sendFail(401);
					}
				} catch (Exception e) {
					Logger.e("addFriendRequest--------Exception==" + e);
					e.printStackTrace();
				}
				if (null != callback) {
					callback.sendFail(500);
				}
			}
		}).start();
	}


	//添加好友确认
	public void addFriendConfirm(final Context context, final String token, final RequestCallback.IAddFriendConfirmCallBack callback) {
		if (mCookies.size() > 0) {
			Logger.e("addFriendConfirm", "Cookies：" + mCookies.get(0).getName() + "=" + mCookies.get(0).getValue());
		}
		else {
			Logger.e("mCookies.size = 0");
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = RequestHost.addFriendConfirm;
				Logger.e(TAG, "addFriendConfirm-------url==" + url);

				HttpPost httpRequest = new HttpPost(url);
				try {
					JSONObject jsonObject2 = new JSONObject();
					jsonObject2.put("token", token);
					StringEntity p_entity = new StringEntity(jsonObject2.toString(),"utf-8");
					//setContentType一定要加上，否则服务器不能判定为是json数据格式
					p_entity.setContentType("application/json");//发送json数据需要设置contentType
					httpRequest.setEntity(p_entity);

					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
					HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

					DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();

					if (mCookies != null && mCookies.size()>0) {
						for (int i = 0; i < mCookies.size(); i++) {
							httpRequest.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
							Logger.e("cookies加入成功");
							break;
						}
					}
					else{
						Logger.e("mCookies为null,使用sharePrefrence");
						httpRequest.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
					}
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
					Logger.e(TAG, "addFriendConfirm--------statusCode==" + statusCode + "::::" + reasonPhrase);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if (null != callback) {
								callback.confirmFail(400);
							}
							return;
						}

						CookieStore mCookieStore = ((AbstractHttpClient) httpClient).getCookieStore();
						List<Cookie> cookies = mCookieStore.getCookies();
						if (cookies.isEmpty()) {
							Logger.e(TAG, "addFriendConfirm------Cookie NONE---------");
						} else {
							mCookies.clear();
							for (int i = 0; i < cookies.size(); i++) {
								// 保存cookie
								Cookie cookie = cookies.get(i);
								Logger.e(TAG, "addFriendConfirm-------" + cookie.getName()+cookie.getValue());
								mCookies.add(cookie);
								String cookieContent = cookies.get(i).getName()+"="+cookies.get(i).getValue();
								Tools.savePreference(context,UtilContact.Cookies,cookieContent);
							}
						}
						String strResult = EntityUtils.toString(entity);
						Logger.e("addFriendConfirm-------strResult==" + strResult);
						callback.confirmSuccess();
						return;
					}
					else {
						callback.confirmFail(statusCode);
					}
				} catch (Exception e) {
					Logger.e("addFriendConfirm--------Exception==" + e);
					e.printStackTrace();
				}
				if (null != callback) {
					callback.confirmFail(400);
				}
			}
		}).start();
	}


	//删除好友
	public void deleteFriendRequest(final Context context, final String huanxinId, final RequestCallback.IDeleteFriendCallBack callback) {
		if (mCookies.size() > 0) {
			Logger.e("deleteFriendRequest", "Cookies：" + mCookies.get(0).getName() + "=" + mCookies.get(0).getValue());
		}
		else {
			Logger.e("mCookies.size = 0");
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = RequestHost.deleteFriend;
				Logger.e(TAG, "deleteFriendRequest-------url==" + url);

				HttpPost httpRequest = new HttpPost(url);
				try {
					JSONObject jsonObject2 = new JSONObject();
					jsonObject2.put("contactID", huanxinId);
					StringEntity p_entity = new StringEntity(jsonObject2.toString(),"utf-8");
					//setContentType一定要加上，否则服务器不能判定为是json数据格式
					p_entity.setContentType("application/json");//发送json数据需要设置contentType
					httpRequest.setEntity(p_entity);

					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
					HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

					DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();

					if (mCookies != null && mCookies.size()>0) {
						for (int i = 0; i < mCookies.size(); i++) {
							httpRequest.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
							Logger.e("cookies加入成功");
							break;
						}
					}
					else{
						Logger.e("deleteFriendRequest","mCookies为null,使用sharePrefrence");
						httpRequest.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
					}
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
					Logger.e(TAG, "deleteFriendRequest--------statusCode==" + statusCode + "::::" + reasonPhrase);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if (null != callback) {
								callback.deleteFail(500);
							}
							return;
						}

						CookieStore mCookieStore = ((AbstractHttpClient) httpClient).getCookieStore();
						List<Cookie> cookies = mCookieStore.getCookies();
						if (cookies.isEmpty()) {
							Logger.e(TAG, "deleteFriendRequest------Cookie NONE---------");
						} else {
							mCookies.clear();
							for (int i = 0; i < cookies.size(); i++) {
								// 保存cookie
								Cookie cookie = cookies.get(i);
								Logger.e(TAG, "deleteFriendRequest-------" + cookie.getName()+cookie.getValue());
								mCookies.add(cookie);
								String cookieContent = cookies.get(i).getName()+"="+cookies.get(i).getValue();
								Tools.savePreference(context,UtilContact.Cookies,cookieContent);
							}
						}
						callback.deleteSuccess();
						String strResult = EntityUtils.toString(entity);
						Logger.e("deleteFriendRequest-------strResult==" + strResult);
						return;
					}
					else {
						callback.deleteFail(statusCode);
					}
				} catch (Exception e) {
					Logger.e("deleteFriendRequest--------Exception==" + e);
					e.printStackTrace();
				}
				if (null != callback) {
					callback.deleteFail(500);
				}
			}
		}).start();
	}


	//获取阿里云token用于上传头像
	public void getAvatorToken(final Context context, final RequestCallback.IGetInfoCallBack callback) {
		if (mCookies.size() > 0) {
			Logger.e("getAvatorToken", "Cookies：" + mCookies.get(0).getName() + "=" + mCookies.get(0).getValue());
		}
		else {
			Logger.e("mCookies.size = 0");
		}
		final int type = UtilContact.getAvatorInfo;
		if (isGettingUserinfo) {
			if (null != callback) {
				callback.getIng(type);
			}
			return;
		}
		isGettingUserinfo = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = RequestHost.avatar;
				Logger.e(TAG, "getAvatorToken-------url==" + url);

				HttpGet httpRequest = new HttpGet(url);
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, requestTimeout);
				HttpConnectionParams.setSoTimeout(httpParameters, responseTimeout);

				DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				try {
//					if (mCookies != null && mCookies.size()>0) {
//						for (int i = 0; i < mCookies.size(); i++) {
//							httpRequest.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
//							Logger.e(TAG,"cookies加入成功"+Tools.getPreference(context,UtilContact.Cookies));
//							break;
//						}
//					}
						Logger.e(TAG,"使用sharePrefrence"+Tools.getPreference(context,UtilContact.Cookies));
						httpRequest.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
					Logger.e(TAG, "getAvatorToken--------statusCode==" + statusCode + "::::" + reasonPhrase);
					if (statusCode == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if(null == entity){
							if (null != callback) {
								callback.getFail(type);
							}
							isGettingUserinfo = false;
							return;
						}

						CookieStore mCookieStore = ((AbstractHttpClient) httpClient).getCookieStore();
						List<Cookie> cookies = mCookieStore.getCookies();
						if (cookies.isEmpty()) {
							Logger.e(TAG, "getAvatorToken------Cookie NONE---------");
						} else {
							mCookies.clear();
							for (int i = 0; i < cookies.size(); i++) {
								// 保存cookie
								Cookie cookie = cookies.get(i);
								Logger.e(TAG, "getAvatorToken-------" + cookie.getName()+cookie.getValue());
								mCookies.add(cookie);
								String cookieContent = cookies.get(i).getName()+"="+cookies.get(i).getValue();
								Tools.savePreference(context,UtilContact.Cookies,cookieContent);
							}
						}
						String strResult = EntityUtils.toString(entity);
						Logger.e("getAvatorToken-------strResult==" + strResult);
						isGettingUserinfo = false;
						callback.getSuccess(type,strResult);
						return;
					}
					else {
						callback.getFail(statusCode);
					}
				} catch (Exception e) {
					Logger.e("getAvatorToken--------Exception==" + e);
					e.printStackTrace();
				}
				if (null != callback) {
					callback.getFail(type);
				}
				isGettingUserinfo = false;
			}
		}).start();
	}

	//验证手机号码获取环信ID
	public void checkPhoneNum(final String phoneNum,final Context context,final RequestCallback.IGetInfoCallBack callBack){
		final int type = UtilContact.getAvatorInfo;
		if (isGettingUserinfo){
			if (null != callBack){
				callBack.getIng(UtilContact.checkPhoneNum);
			}
			return;
		}
		isGettingUserinfo = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = RequestHost.checkPhone+"?phone="+phoneNum+"&zone=86";
				HttpGet httpGet = new HttpGet(url);
				HttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams,requestTimeout);
				HttpConnectionParams.setSoTimeout(httpParams,responseTimeout);
				DefaultHttpClient client = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				if (mCookies != null && mCookies.size()>0) {
					for (int i = 0; i < mCookies.size(); i++) {
						httpGet.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
						Logger.e("cookies加入成功");
						break;
					}
				}
				else{
					Logger.e("mCookies为null,使用sharePrefrence");
					httpGet.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
				}
				try {
					HttpResponse httpResponse = client.execute(httpGet);
					int status = httpResponse.getStatusLine().getStatusCode();
					if (status == STATUS_CODE_OK) {
						HttpEntity entity = httpResponse.getEntity();
						if (entity == null) {
							callBack.getFail(UtilContact.checkPhoneNum);
							isGettingUserinfo = false;
							return;
						}
						CookieStore mCookieStore = client.getCookieStore();
						List<Cookie> cookies = mCookieStore.getCookies();
						if (cookies.isEmpty()) {
							Logger.e(TAG, "checkPhoneNum------Cookie NONE---------");
						} else {
							mCookies.clear();
							for (int i = 0; i < cookies.size(); i++) {
								Cookie cookie = cookies.get(i);
								mCookies.add(cookie);
								mCookies.add(cookie);
								String cookieContent = cookies.get(i).getName() + "=" + cookies.get(i).getValue();
								Tools.savePreference(context, UtilContact.Cookies, cookieContent);
							}
						}
						String content = EntityUtils.toString(entity);
						JSONObject jsonObject = new JSONObject(content);
						String id = jsonObject.getString("id");
						isGettingUserinfo = false;
						callBack.getSuccess(UtilContact.checkPhoneNum,id);
						return;
					}
					else if (status == 404){
						Logger.e(TAG,"status = "+404);
						callBack.getFail(404);
						isGettingUserinfo = false;
					}
					else if (status == 400){
						Logger.e(TAG,"status = "+400);
						callBack.getFail(400);
						isGettingUserinfo = false;
					}
					else if (status == 500){
						Logger.e(TAG,"status = "+500);
						isGettingUserinfo = false;
					}
					else{
						Logger.e(TAG,"else");
						callBack.getFail(500);
					}
				}
					catch (Exception e) {
					e.printStackTrace();
						isGettingUserinfo = false;
						Logger.e(TAG,e.getMessage());
						callBack.getFail(500);
				}
			}
		}).start();
	}

	//刷新用户登录状态：
	public void refreshLoginStatus(final Context context,final RequestCallback.IRefreshLoginStatus callback){
		Logger.e("refreshLoginStatus,","refreshLoginStatus coming in");
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = RequestHost.refreshUrl;
				HttpPost httpPost = new HttpPost(url);
				HttpParams httpParams = new BasicHttpParams();
				DefaultHttpClient client = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				httpPost.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
				try {
					HttpResponse response = client.execute(httpPost);
					int code = response.getStatusLine().getStatusCode();
					if (code == HttpRequest.STATUS_CODE_OK){
						HttpEntity entity = response.getEntity();
						if(null == entity){
							if (null != callback) {
								callback.refreshFail();
							}
							return;
						}

						CookieStore mCookieStore = ((AbstractHttpClient) client).getCookieStore();
						List<Cookie> cookies = mCookieStore.getCookies();
						if (cookies.isEmpty()) {
							Logger.e("refreshLoginStatus", "refreshLoginStatus------Cookie NONE---------");
						} else {
							mCookies.clear();
							for (int i = 0; i < cookies.size(); i++) {
								// 保存cookie
								Cookie cookie = cookies.get(i);
								mCookies.add(cookie);
								String cookieContent = cookies.get(i).getName()+"="+cookies.get(i).getValue();
								Logger.e("refreshLoginStatus", "refreshLoginStatus-------" + cookieContent);
								Tools.savePreference(context,UtilContact.Cookies,cookieContent);
							}
						}
						String strResult = EntityUtils.toString(entity);
						Logger.e("addFriendRequest-------strResult==" + strResult);
						callback.refreshSuccess(strResult);
						return;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	//手机连到杯子，上传cupID生成groupID
	public void getGroupID(final Context context){
		Logger.e("getGroupID,","getGroupID coming in");
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
				String url = RequestHost.connectedCup;
				HttpPost httpPost = new HttpPost(url);
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("cupID", Tools.getPreference(context,UtilContact.CUP_ID));
					Logger.e("getGroupId","cupid = "+Tools.getPreference(context,UtilContact.CUP_ID));
				StringEntity p_entity = new StringEntity(jsonObject2.toString(),"utf-8");
				//setContentType一定要加上，否则服务器不能判定为是json数据格式
				p_entity.setContentType("application/json");//发送json数据需要设置contentType
				httpPost.setEntity(p_entity);

				DefaultHttpClient client = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
				httpPost.addHeader("Cookie",Tools.getPreference(context,UtilContact.Cookies));
					HttpResponse response = client.execute(httpPost);
					int code = response.getStatusLine().getStatusCode();
					if (code == HttpRequest.STATUS_CODE_OK){
						HttpEntity entity = response.getEntity();
						if(null == entity){
							return;
						}
						CookieStore mCookieStore = ((AbstractHttpClient) client).getCookieStore();
						List<Cookie> cookies = mCookieStore.getCookies();
						if (cookies.isEmpty()) {
							Logger.e("refreshLoginStatus", "refreshLoginStatus------Cookie NONE---------");
						} else {
							mCookies.clear();
							for (int i = 0; i < cookies.size(); i++) {
								// 保存cookie
								Cookie cookie = cookies.get(i);
								mCookies.add(cookie);
								String cookieContent = cookies.get(i).getName()+"="+cookies.get(i).getValue();
								Logger.e("refreshLoginStatus", "refreshLoginStatus-------" + cookieContent);
								Tools.savePreference(context,UtilContact.Cookies,cookieContent);
							}
						}
						if (response.getStatusLine().getStatusCode() == STATUS_CODE_OK){
							String strResult = EntityUtils.toString(entity);
							Logger.e("getGroupID","getGroupID-------strResult==" + strResult);
							JSONObject jsonObject = new JSONObject(strResult);
							String groupID = jsonObject.getString("group_id");
							Tools.savePreference(context,UtilContact.GROUPID,groupID);
						}
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

}
