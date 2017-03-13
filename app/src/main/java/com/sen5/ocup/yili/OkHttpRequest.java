package com.sen5.ocup.yili;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.callback.RequestCallback;
import com.sen5.ocup.struct.RequestHost;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Created by chenqianghua on 2016/12/15.
 */
public class OkHttpRequest {

    private static String TAG = OkHttpRequest.class.getSimpleName();
    private static OkHttpClient mOkHttpClient = new OkHttpClient();
    //获取okhttp实例
    public  static OkHttpClient getOKhttpInstance(){
        return mOkHttpClient;
    }
    public  static  void getTips(final RequestCallback.IGetTipsCallBack callback,String lang){
        Logger.e(TAG,"getAplInfo come in");
        final Request request = new Request.Builder().get().url(RequestHost.getTips+lang).build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    response = mOkHttpClient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (response != null) {
                    if (response.isSuccessful()) {
                        try {
                            String content = response.body().string();
                            JSONArray jsonArray = new JSONArray(content);
                            String[] returnString = new String[5];
                            for (int i =0; i<jsonArray.length();i++){
                                returnString[i] = jsonArray.getString(i);
                                Logger.e(TAG,"for"+i+"=="+returnString);
                            }
                            long time = System.currentTimeMillis();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(time);
                            int whatDay = calendar.get(Calendar.DAY_OF_WEEK);
                            callback.getTipSuccess(returnString[whatDay%5]);
                        }  catch (Exception e) {
                            Logger.e(TAG,"exception = "+e.getMessage().toString());
                            e.printStackTrace();
                        }
                    } else {
                        callback.getTipFail();
                    }
                }
                else {
                    callback.getTipFail();
                }
            }
        }).start();
    }

    //get请求
    public  static  void getApkInfo(final RequestCallback.IGetUpdateInfo callback){
        Logger.e(TAG,"getApkInfo");
        final String[] path = {null};
        final String[] versionCode = {null};
        final String[] detail = {null};
        final String[] versionName = {null};
        final Request request = new Request.Builder().get().url(RequestHost.apkUpdate).build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response  response = null;
                try {
                    response = mOkHttpClient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (response != null) {
                    if (response.isSuccessful()) {
                        try {
                            String content = response.body().string();
                            Logger.e(content);
                            JSONObject jsonObject = new JSONObject(content);
                            jsonObject = new JSONObject(content);
                            path[0] = jsonObject.getString("path");
                            versionCode[0] = jsonObject.getString("versionCode");
                            detail[0] = jsonObject.getString("detail");
                            versionName[0] = jsonObject.getString("version");
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        callback.getUpdateInfoSuccess(path[0],versionCode[0],detail[0],versionName[0]);
                    } else {
                        callback.getUpdateInfoFail();
                    }
                }
            }
        }).start();
    }


    //path:下载apk的地址
    public  static  void getAPK(final String url, final File file, final RequestCallback.IDownLoadAPk callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.e(TAG,"getApk");
                Request request = new Request.Builder().get().url(url).build();
                try {
                    Response response = mOkHttpClient.newCall(request).execute();
                    if (response != null) {
                        if (response.isSuccessful()) {
                            Logger.e(TAG,"getApkinfoSuccess");
                            ResponseBody body = response.body();
                            InputStream is = body.byteStream();
                            long length = body.contentLength();
                            Logger.e(TAG,"length = "+length);
                            long length_downed = 0;
                            int lastProgress = 0;
                            FileOutputStream fos = new FileOutputStream(file);
                            //获取文件长度
                            byte[] bytes = new byte[1024 * 8];
                            while (true) {
                                if (is != null) {
                                    int i = is.read(bytes);
                                    if (i < 0) {
                                        break;
                                    }
                                    fos.write(bytes, 0, i);
                                    length_downed += i;
                                    int progress = (int) (length_downed * 100 / length);
                                    if (progress > lastProgress) {
                                        callback.downLoadProgress(progress);
                                        lastProgress = progress;
                                    }
                                } else {
                                    //下载失败
                                    Logger.e(TAG,"downLoadFai");
                                    callback.downLoadFail();
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    Logger.e(TAG,e.getMessage().toString());
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
