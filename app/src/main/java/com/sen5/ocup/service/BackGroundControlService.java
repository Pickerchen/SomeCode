package com.sen5.ocup.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

import com.sen5.ocup.R;
import com.sen5.ocup.activity.MainActivity;
import com.sen5.ocup.alarm.Time_show;
import com.sen5.ocup.callback.CustomInterface;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.Tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenqianghua on 2017/3/2.
 */

public class BackGroundControlService extends Service implements CustomInterface.IDialog{

    private Context context;
    public static boolean hasStart = false;
    public static boolean shouldRemind = true;//是否需要提醒
    private String TAG = BackGroundControlService.class.getSimpleName();
    public static final int showRemindDialog = 6;
    public static final int showSuspend = 7;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == showRemindDialog) {
                if (dialog == null){
                    dialog = new CustomDialog(context, BackGroundControlService.this, R.style.custom_dialog, CustomDialog.DIALOG_REMIND_DRINK, name);
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    dialog.show();
                }
                else {
                    if (!dialog.isShowing()){
                        dialog.show();
                    }
                }
            }
            else if (msg.what == showSuspend){
                Tools.showSuspend(MainActivity.mContext,name);
            }
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = BackGroundControlService.this;
        hasStart = true;
        startAlarm();
        return super.onStartCommand(intent, flags, startId);
    }

    public static List<String> times = new ArrayList<>();//在添加闹钟的页面随时进行更改
    private static int code = 1;
    private static String name = "";
    private CustomDialog dialog;
    private void startAlarm(){

         DBManager dbManager = new DBManager(context);
        name = dbManager.queryYiLiCup().getNickname();
        List<Time_show> time_shows =  dbManager.queryAlarmData("");
        for (Time_show time_show : time_shows){
            String time = time_show.getTime();
            times.add(Tools.splitNub(time));
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (times.size() != 0) {
                        for (String time : times) {
                            Log.e(TAG, "time is" + time + "tools.formatTime is " + Tools.formatTimeHM()+Tools.isAppIsInBackground(context));
                            if (Tools.formatTimeHM().equals(time)) {
                                if (!Tools.isAppIsInBackground(context)){
                                    if (shouldRemind) {
                                        mHandler.sendEmptyMessage(showRemindDialog);
                                    }
                                }
                                else {
                                    if (shouldRemind) {
                                        mHandler.sendEmptyMessage(showSuspend);
                                    }
                                }
                            }
                        }
                        try {
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    public void ok(int type) {

    }

    @Override
    public void ok(int type, Object obj) {

    }

    @Override
    public void cancel(int type) {

    }
}
