package com.sen5.ocup.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.activity.MainActivity;
import com.sen5.ocup.suspend.HeadsUp;
import com.sen5.ocup.suspend.HeadsUpManager;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * @version ：2015年1月28日 下午2:03:54
 *          <p/>
 *          类说明 :应用工具类
 */
public class Tools {
    private static final String TAG = "Tools";

    public final static int even_version = 22;
    public final static int odd_version = 31;
    public final static String even_filename = "Ocup_V22_OTA.bin";
    public final static String odd_filename = "Ocup_V31_OTA.bin";

    public static final int SCANNIN_GREQUEST_CODE = 1;
    public static final int SCRAWL_REQUEST_CODE = 2;
    public static final int CHAT_GREQUEST_CODE = 3;
    public static final int CONNECT_BLUETOOTH_REQUEST_CODE = 4;
    public static final int RENAME_REQUEST_CODE = 5;
    public static final int WATERLED_REQUEST_CODE = 6;
    public static final int BLUETOOTH_SEARCH_REQUEST_CODE = 7;
    // ??
    public static final int PROJECT_REQUEST_CODE = 8;
    public static final int OCUPSETTING_REQUEST_CODE = 9;
    public static final int SCRAWLANIM_REQUEST_CODE = 10;

    public static final String OCUP_DIR = "/ocup";
    public static final String AVATAR_FILE_NAME = "/avator.jpg";
    public static final String AVATAR_NORMAL_FILE_NAME = "/avator_big.jpg";
    public static final String CUPINFO_FILE_NAME = "/cup_info";
    public static final String COOKIE_FILE_NAME = "/cookie1";


    //获取一张圆角或者圆形图片
    public static Bitmap getRoundedBitmap(Bitmap bitmap, int radius) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap roundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        radius = (width > height) ? (height / 2) : (width / 2);
        Canvas canvas = new Canvas(roundBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        Rect rect = new Rect(0, 0, width, height);
        RectF rectF = new RectF(rect);
        //圆角
//        canvas.drawRoundRect(rectF,radius,radius,paint);
        //圆形图片
        canvas.drawCircle(width / 2, height / 2, radius, paint);

        //取两层的交集并且只显示上层的
        PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        paint.setXfermode(xfermode);
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return roundBitmap;
    }

    /**
     * dp转px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }


    //api19以上使用
    public static void setImmerseLayout(View view, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Activity activity = (Activity) context;
            Window window = activity.getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            int statusBarHeight = Tools.getStatusBarHeight(context);
            view.setPadding(0, statusBarHeight, 0, 0);
        }
    }

    /**
     * 用于获取状态栏的高度。 使用Resource对象获取（推荐这种方式）
     *
     * @return 返回状态栏高度的像素值。
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * px转dp
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        return (int) (pxValue / (context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        return (int) (spValue * (context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    /**
     * 将content保存至SharedPreferences
     *
     * @param context
     * @param key
     * @param content
     */
    public static void savePreference(Context context, String key, String content) {
        SharedPreferences.Editor sharedata = PreferenceManager.getDefaultSharedPreferences(context).edit();
        sharedata.putString(key, content);
        sharedata.commit();
    }

    /**
     * 从SharedPreferences中取出内容
     *
     * @param key
     * @return
     */
    public static String getPreference(Context context, String key) {
        SharedPreferences sharedata = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedata.getString(key, "");
    }

    public static void clearKeyPreference(Context context, String key) {
        SharedPreferences.Editor sharedata = PreferenceManager.getDefaultSharedPreferences(context).edit();
        sharedata.putString(key, "");
        sharedata.commit();
    }

    /**
     * 读取表情配置文件
     *
     * @param context
     * @return
     */
    public static List<String> getEmojiFile(Context context) {
        try {
            List<String> list = new ArrayList<String>();
            InputStream in = context.getResources().getAssets().open("emoji");
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String str = null;
            while ((str = br.readLine()) != null) {
                list.add(str);
            }

            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将分钟转成HH:mm
     *
     * @param minute
     * @return
     */
    public static String minute2hour(int minute) {
        String str = "";
        int h = minute / 60;
        int m = minute % 60;
        str = String.format("%02d", h) + " : " + String.format("%02d", m);
        return str;
    }

    /**
     * 获取当前时间的秒数
     *
     * @return
     */
    public static int getCurSecond() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date d = new Date(System.currentTimeMillis());
        String date = sdf.format(d);
        Log.d(TAG, "getCurSecond   date==" + date);
        String[] strs = date.split("-");
        if (strs.length > 0) {
            int s = Integer.parseInt(strs[strs.length - 1]);
            int m = Integer.parseInt(strs[strs.length - 2]);
            int h = Integer.parseInt(strs[strs.length - 3]);
            return (h * 60 * 60 + m * 60 + s);
        } else {
            return 0;
        }
    }

    /**
     * 获取指定日期的时间的秒数
     *
     * @return
     */
    public static int getSecond(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date d = new Date(time);
        String date = sdf.format(d);
        Log.d(TAG, "getSecond   date==" + date);
        String[] strs = date.split("-");
        if (strs.length > 0) {
            int s = Integer.parseInt(strs[strs.length - 1]);
            int m = Integer.parseInt(strs[strs.length - 2]);
            int h = Integer.parseInt(strs[strs.length - 3]);
            return (h * 60 * 60 + m * 60 + s);
        } else {
            return 0;
        }
    }


    /**
     * 将当前日期转成"yyyy-MM-dd"
     *
     * @return
     */
    public static String getcurrenttime() {
        Date date = new Date();
        Log.d(TAG, "date: " + date.toString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 设置当前日期的格式
        String currentDate = sdf.format(date);// 当前日期
        return currentDate;
    }

    /**
     * 将"yyyy-MM-dd"转成毫秒
     *
     * @param date yyyy-MM-dd
     * @return
     */
    public static long datetomillion(String date) {
        long time = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d2 = null;
        try {
            d2 = sdf.parse(date);
            Log.d(TAG, "datetomillion---d2===" + d2);
            time = d2.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    /**
     * 将"yyyy-MM"转成毫秒
     * <p/>
     * yyyy-MM-dd
     *
     * @return
     */
    public static long getCurmonthtomillion() {
        long time = 0;
        Date date = new Date();
        Log.d(TAG, "date: " + date.toString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 设置当前日期的格式
        String currentDate = sdf.format(date);// 当前日期
        int y = Integer.parseInt(currentDate.toString().split("-")[0]);
        int m = Integer.parseInt(currentDate.toString().split("-")[1]);
        time = datetomillion(y + "-" + m + "-" + "01");
        return time;
    }

    // 依赖的函数
    public static byte[] mergeArray(byte[]... a) {
        // 合并完之后数组的总长度
        int index = 0;
        int sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum = sum + a[i].length;
        }
        byte[] result = new byte[sum];
        for (int i = 0; i < a.length; i++) {
            int lengthOne = a[i].length;
            if (lengthOne == 0) {
                continue;
            }
            // 拷贝数组
            System.arraycopy(a[i], 0, result, index, lengthOne);
            index = index + lengthOne;
        }
        return result;
    }

    /**
     * 获取sd卡的路径
     *
     * @return
     */
    static public String getSDPath() {
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            File sdDir = Environment.getExternalStorageDirectory();
            return sdDir.toString();
        } else {
            Log.d(TAG, "sd card not found");
        }
        return null;
    }

    public static String getVersion(Activity activity) {
        try {
            PackageManager manager = activity.getPackageManager();
            PackageInfo info = manager.getPackageInfo(activity.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return activity.getString(R.string.can_not_find_version_name);
        }
    }

    public static int getVersionCode(Activity activity) {
        try {
            PackageManager manager = activity.getPackageManager();
            PackageInfo info = manager.getPackageInfo(activity.getPackageName(), 0);
            int version = info.versionCode;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 将view转成图片保存到sdcard
     *
     * @param view
     */
    public static void saveView2SDcard(View view, String filename) {
        Log.d(TAG, "saveView2SDcard----view.getWidth()==" + view.getWidth());
        //OcupApplication.getInstance().getString(R.string.app_name) + "_data" + ".jpg"
        String fname = Tools.getSDPath() + "/" + filename;
        File file = new File(fname);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        view.setDrawingCacheEnabled(true);
        view.destroyDrawingCache();
        view.buildDrawingCache();
        Bitmap bitmap = BitmapUtil.optimizeBitmap(view.getDrawingCache());
        if (bitmap != null) {
            Log.d(TAG, "saveView2SDcard----bitmap != null---");
            try {
                FileOutputStream out = new FileOutputStream(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                out.write(baos.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "saveView2SDcard----Exception e==" + e);
            }
        }
    }

    /**
     * 将bitmap保存到sdcard
     *
     */
    public static void saveBitmap2SDcard(String filename,Bitmap bitmap) {
        //OcupApplication.getInstance().getString(R.string.app_name) + "_data" + ".jpg"
        String fname = Tools.getSDPath() + "/" + filename;
        File file = new File(fname);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (bitmap != null) {
            Log.d(TAG, "saveView2SDcard----bitmap != null---");
            try {
                FileOutputStream out = new FileOutputStream(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                out.write(baos.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "saveView2SDcard----Exception e==" + e);
            }
        }
    }

    //时间格式化：将时间格式化到时分
    public static String formatTime(long time) {
        String date = new SimpleDateFormat("HH:mm").format(time);
        return date;
    }

    public static  int[] getScreenWH(Activity activity) {
        int wh[] = new int[2];
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        wh[0] = dm.widthPixels;
        wh[1] = dm.heightPixels;
        return wh;
    }
    private static Toast sToast;
    public static void showToast( Context mcontext,  String content){
        synchronized (Tools.class){
            if (sToast == null){
                sToast = Toast.makeText(mcontext,content,Toast.LENGTH_LONG);
            }
            else {
                sToast.setText(content);
            }
        }
        sToast.show();
    }

    public static void sendSms(String phone,Context mContext){
        Uri smsToUri = Uri.parse("smsto:"+phone);
        Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri );
        mIntent.putExtra("sms_body", "快加入暖哄哄水杯 网址：http://sj.qq.com/myapp/detail.htm?apkName=com.sen5.nhh.ocup");
        mContext.startActivity(mIntent);
    }

    //获取sd的状态
    public static boolean getStorageState(){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return true;
        }
        else {
            return false;
        }
    }
    //判断是否为纯数字
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    //毫秒值转换为分值
    public static int getCurrentMinutes(){
        long time = System.currentTimeMillis();
        long minutes =  (time/(1000*60));
        return (int)minutes;
    }
    //手机振动
    public static void Vibrate(final Activity activity) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(100);
    }
    //适用于5.0之后
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        Tools.printInfo(TAG,"isInbackGround is"+isInBackground);
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                //前台程序
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }
    //将当前毫秒值转换为时分
    public static String formatTimeHM(){
        SimpleDateFormat formatter = new SimpleDateFormat("HHmm");
//        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        String hm = formatter.format(System.currentTimeMillis());
        return hm;
    }
    //截取所有数字
    public static String splitNub(String content){
        String regEx="[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(content);
        return m.replaceAll("").trim();
    }
    //notifycation
    public static void showNotify(Context context,int num,String name){
        NotificationManager manger = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        //为了版本兼容  选择V7包下的NotificationCompat进行构造
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        //Ticker是状态栏显示的提示
        builder.setTicker("亲爱的"+name+",喝杯水休息一下吧。");
        //第一行内容  通常作为通知栏标题
        builder.setContentTitle("暖哄哄");
        //第二行内容 通常是通知正文
        builder.setContentText("亲爱的"+name+",喝杯水休息一下吧。");
        //第三行内容 通常是内容摘要什么的 在低版本机器上不一定显示
        builder.setSubText("懂你,暖你,爱你。");
        //ContentInfo 在通知的右侧 时间的下面 用来展示一些其他信息
        //builder.setContentInfo("2");
        //number设计用来显示同种通知的数量和ContentInfo的位置一样，如果设置了ContentInfo则number会被隐藏
        builder.setNumber(num);
        //可以点击通知栏的删除按钮删除
        builder.setAutoCancel(true);
        //系统状态栏显示的小图标
        builder.setSmallIcon(R.drawable.icon_192);
        //下拉显示的大图标
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.icon_192));
        Intent intent = new Intent(context,MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context,1,intent,0);
        //点击跳转的intent
        builder.setContentIntent(pIntent);
        //通知默认的声音 震动 呼吸灯
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        Notification notification = builder.build();
        manger.notify(1,notification);
    }

    //仿微信弹框
    public static void showSuspend(Context context,String name){
        PendingIntent pendingIntent=PendingIntent.getActivity(context,11,new Intent(context,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
        HeadsUpManager manage = HeadsUpManager.getInstant(((Activity)context).getApplication());
        HeadsUp.Builder builder = new HeadsUp.Builder(context);
        builder.setContentTitle("暖哄哄").setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                //要显示通知栏通知,这个一定要设置
                .setSmallIcon(R.drawable.icon_192)
                //2.3 一定要设置这个参数,负责会报错
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent,false)
                .setContentText(context.getString(R.string.reminddrindfirst)+name+context.getString(R.string.reminddrindsecond)+context.getString(R.string.reminddrindthird));
        HeadsUp headsUp = builder.buildHeadUp();
        headsUp.setSticky(true);
        manage.notify(1, headsUp);
    }

    //检查权限
    public static void checkPermission(Context context){
        String packageName = context.getPackageName();
        PackageManager pm = context.getPackageManager();
        boolean permission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.SYSTEM_ALERT_WINDOW", packageName));
        if (permission) {
            showToast(context,"已获取");
        }else {
            showToast(context,"未获取权限");
        }
    }

    private static boolean debug = true;//正式发布时设为false
    //日志打印
    public static void printInfo(String TAG,String content){
        if (debug){
            Logger.d(TAG,content);
        }
    }
}