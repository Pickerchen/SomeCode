package com.sen5.ocup.yili;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.activity.MainActivity;
import com.sen5.ocup.alarm.Time_show;
import com.sen5.ocup.callback.CustomInterface;
import com.sen5.ocup.receiver.HuanxinBroadcastReceiver;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.HuanxinUtil;
import com.sen5.ocup.util.Tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenqianghua on 2017/2/28.
 */

public class UpdateCupConnecteThread{
    private static String TAG = UpdateCupConnecteThread.class.getSimpleName();
    public static String group_msg = "#1#0#0";
    private static int sleepTime = 20000;
    private static int remindDialogSleepTime = 50000;
    private static List<FriendInfo> sFriendInfos;
    public static void startSendGroup(final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Logger.e(TAG, "startSendGroup coming in");
                        HuanxinUtil.getInstance().sendGroupMsg(context, group_msg);
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public static void startmeasureTime(final Context context){
        final DBManager dbManager = new DBManager(context);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    sFriendInfos = dbManager.queryFriends();
                    for (FriendInfo friendInfo : sFriendInfos) {
                        long currentTime = Tools.getCurrentMinutes();
                        int lastUptime = friendInfo.getLastuptime();
                        Logger.e(TAG, "currentTime - lastUpTime is " + (currentTime - lastUptime));
                        if ((currentTime - lastUptime) > 2) {
                            if (friendInfo.isHaveCup()) {
                                friendInfo.setOnLine(false);
                                //更新数据库
                                dbManager.updateFriendsInfo2(friendInfo);
                                //更新UI
                                Intent intent = new Intent(HuanxinBroadcastReceiver.ReceiverGroupChat);
                                context.sendBroadcast(intent);
                            }
                        }
                    }
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static List<String> times = new ArrayList<>();//在添加闹钟的页面随时进行更改
    private static int code = 1;
    private static String name = "";
    public static void startAlarm(final Context context, final CustomInterface.IDialog callback){
        final DBManager dbManager = new DBManager(context);
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
                                //在后台，进行notifycation
                                if (Tools.isAppIsInBackground(context)){
                                    callback.ok(MainActivity.showSuspend);
                                }
                                else {
                                    callback.ok(MainActivity.showRemindDialog);
                                }
                            }
                        }
                        try {
                            Thread.sleep(remindDialogSleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }
}
