package com.sen5.ocup.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.yili.MyHttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * @version ：2015年1月28日 下午2:03:54
 *
 *         类说明 :tip图片加载处理
 */
public class TipsBitmapLoader {	
	private static final String      TAG            = "TipsCache";
	private static final String      TIP_REFERER    = "http://api.otelligent.com";
	private static final int         MAX_CACHE_SIZE = 50*1024*1024;
	
	private static final int ASYNC_WAIT      = 500;
	private static final int ASYNC_WAIT_FILE = 2;
	private static final int ASYNC_WAIT_NET  = 10;        
	
	private static TipsBitmapLoader  sInstance= new TipsBitmapLoader();
	
	//内存缓存
	private LruCache<String, Bitmap> mMemoryCache   = null;
	
	//文件缓存目录
	private String mCachedDir = null;
	
	//读/取图片的任务集合
	private Set<String> mAsyncSet = null;

	//异步获取图片的回调函数
	public interface asyncLoadCallback {
		public void load(Bitmap bitmap); 
	}
	
	public static TipsBitmapLoader getInstance() {
		return sInstance;
	}
	
	//异步获取图片
	public void asyncLoadBitmap(final String url, final asyncLoadCallback callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Bitmap bitmap = getFromCache(url);
				callback.load(bitmap);
			}
		}).start();
	}
	
	//异步缓存一些图片（以实现预加载）
	private Object asyncFlag = new Object();
	public void asyncLoadBitmapBuffer(final List<String> bufferUrls) {		
		if (bufferUrls != null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					synchronized(asyncFlag) {
						for (String url : bufferUrls)
							getFromCache(url);
					}
				}
			}).start();
		}
	}
	
	public void addToCache(String url, Bitmap bitmap) {			
		// 缓存至内存中
		addToMemory(url, bitmap);
		
		// 缓存至文件中
		addToFile(url, bitmap);
	}

	public Bitmap getFromCache(String url) {
		if (url == null) {
			return null;
		}
		
		Bitmap bitmap = null;
		//尝试从内存中取
		bitmap = getFromMemory(url);
		if (bitmap != null) {
			return bitmap;
		}

		//尝试从缓存目录中取
		bitmap = getFromFile(url);
		if (bitmap != null) {
			return bitmap;
		}
		Log.d(TAG, "-------------------------------661 = url = " + url);
		//尝试从网络获取
		bitmap = getFromNet(url);
		if (bitmap != null) {
			return bitmap;
		}
		Log.d(TAG, "-------------------------------662 = ");
		return bitmap;
	}
	
	private void addToMemory(String url, Bitmap bitmap) {
		Log.d(TAG, "addToMemory---------------------"+ (url != null ) + "::" + (bitmap != null) + "::" +( getFromMemory(url) == null ));
		Log.d(TAG, "addToMemory-----------111----------url = " + url);
		if (url != null && bitmap != null && getFromMemory(url) == null) {
			mMemoryCache.put(url, bitmap);
		}
	}
	
	private void addToFile(String url, Bitmap bitmap) {
		if (url == null || bitmap == null) {
			return;
		}
		
		//已经有异步任务在处理这个图片
		if (!tryToAddAsyncItem(url)) {
			return;
		}
		
		String urls[] = url.split("/");
		if (urls.length > 0) {
			String fileName = urls[urls.length - 1];
			String filePath = mCachedDir + "/" + fileName;
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(filePath);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, "failed to save bitmap to file: " + filePath);
			}
		}
		removeAsyncItem(url);
	}
	
	public Bitmap getFromMemory(String url) {
		return mMemoryCache.get(url);
	}
	
	public Bitmap getFromFile(String url) {
		//如果有异步的任务也在取这个图片，则尝试取到图片或者等待结束
		if (!tryToAddAsyncItem(url)) {
			return asyncGet(url, ASYNC_WAIT_FILE);
		}

		Bitmap bitmap = null;
		String urls[] = url.split("/");
		if (urls.length > 0) {
			String fileName = urls[urls.length - 1];
			String filePath = mCachedDir + "/" + fileName;
			File f = new File(filePath);
			if (f.exists()) {
				try {
					FileInputStream fis = new FileInputStream(filePath);
					try {
						bitmap = BitmapFactory.decodeStream(fis);
						addToMemory(url, bitmap);
					} catch (OutOfMemoryError e) {
						e.printStackTrace();
					}
					fis.close();
					removeAsyncItem(url);
					
//					if(null != bitmap && !bitmap.isRecycled()){
//						bitmap.recycle();
//						bitmap = null;
//					}
//					return bitmap;
					return bitmap;
				} catch (Exception e) {
					e.printStackTrace();
					Log.d(TAG, "failed to get bitmap from file: " + filePath);
					bitmap = null;
				}
			}
		}
		removeAsyncItem(url);
		return bitmap;
	}
	
	private Bitmap getFromNet(String url) {
		//如果有异步的任务也在取这个图片，则尝试取到图片或者等待结束
		if (!tryToAddAsyncItem(url)) {
			return asyncGet(url, ASYNC_WAIT_NET);
		}
		
		Bitmap bitmap = null;
		HttpGet httpRequest = new HttpGet(url);
		httpRequest.setHeader("Referer", TIP_REFERER);
		DefaultHttpClient httpClient = (DefaultHttpClient) MyHttpClient.getNewHttpClient();
		HttpResponse httpResponse;
		try {
			httpResponse = httpClient.execute(httpRequest);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == 200) {
//				BitmapFactory.Options options = new BitmapFactory.Options();
//				options.inJustDecodeBounds = true;
//				BitmapFactory.decodeResource(getResources(), R.id.myimage, options);
//				int imageHeight = options.outHeight;
//				int imageWidth = options.outWidth;
//				bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(EntityUtils.toByteArray(httpResponse.getEntity())), null, options);
				HttpEntity entity = httpResponse.getEntity();
				Log.e(TAG, "---------entity = " + (entity==null));

				CookieStore mCookieStore = ((AbstractHttpClient) httpClient).getCookieStore();
				List<Cookie> cookies = mCookieStore.getCookies();
				if (cookies.isEmpty()) {
				} else {
					for (int i = 0; i < cookies.size(); i++) {
						// 保存cookie
						Cookie cookie = cookies.get(i);
						String cookieContent = cookies.get(i).getName()+"="+cookies.get(i).getValue();
						Tools.savePreference(OcupApplication.getInstance(),UtilContact.Cookies,cookieContent);
					}
				}


				bitmap = BitmapFactory.decodeStream(
						new ByteArrayInputStream(EntityUtils.toByteArray(entity)));
				if (null != bitmap) {
					removeAsyncItem(url);
					addToCache(url, bitmap);
					return bitmap;
				} else {
					Log.d(TAG, "failed to decode bitmap form url: " + url);
				}
			} else {
				Log.d(TAG, String.format("error on geting bitmap from %s, http code: %d", url, statusCode));
			}
		} catch(Exception e) {
			e.printStackTrace();
			Log.d(TAG, "failed to get bitmap from: " + url+"   e=="+e);
		}
		
		removeAsyncItem(url);
		return bitmap;
	}
	
	private boolean tryToAddAsyncItem(final String url) {
		synchronized(mAsyncSet) {
			if (!mAsyncSet.contains(url)) {
				mAsyncSet.add(url);
				return true;
			}
			return false;
		}
	}
	
	private void removeAsyncItem(final String url) {
		synchronized(mAsyncSet) {
			mAsyncSet.remove(url);
		}
	}
	
	private Bitmap asyncGet(String url, int times) {
		for (int i = 0; i < times; i++) {
			try {
				Thread.sleep(ASYNC_WAIT);
				if (tryToAddAsyncItem(url)) {
					removeAsyncItem(url);
					return getFromMemory(url);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	private TipsBitmapLoader() {
		mMemoryCache = new LruCache<String, Bitmap>(MAX_CACHE_SIZE) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				
				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};
		mAsyncSet = new HashSet<String>();
		mCachedDir = OcupApplication.getInstance().getCacheDir().getAbsolutePath();
	}
	
}
