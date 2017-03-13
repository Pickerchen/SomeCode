package com.sen5.ocup.yili;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.callback.RequestCallback;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.Tools;

import java.util.List;

public class UserInfoActivity extends Activity implements View.OnClickListener,RequestCallback.IDeleteFriendCallBack{

    //Flag
    private String TAG = UserInfoActivity.class.getSimpleName();
    public static String DeleteBroadcast = "deletebroadcast";

    //view
    private ImageView iv_avator;
    private ImageView iv_back;
    private TextView tv_nickName;
    private TextView tv_phoneNum;
    private TextView tv_delete;

    //data
    private String phoneNum;
    private String userID;
    private String avator;
    private String nickName;

    //looper
    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    Intent intent = new Intent(DeleteBroadcast);
                    UserInfoActivity.this.sendBroadcast(intent);
                    Toast.makeText(UserInfoActivity.this,getString(R.string.deletesuccess),Toast.LENGTH_LONG).show();
                    break;
                case 1:
                    Toast.makeText(UserInfoActivity.this,getString(R.string.deletefail),Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        initView();
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        phoneNum = intent.getStringExtra("phoneNum");
        userID = intent.getStringExtra("userID");
        avator = intent.getStringExtra("avator");
        nickName = intent.getStringExtra("nickName");
        tv_phoneNum.setText(phoneNum);
        tv_nickName.setText(nickName);
        loaderAvatar();
    }

    private void initView() {
        FrameLayout titleLayout = (FrameLayout) findViewById(R.id.durian_head_layout);
        Tools.setImmerseLayout(titleLayout,this);

        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_avator = (ImageView) findViewById(R.id.iv_user);
        tv_nickName = (TextView) findViewById(R.id.tv_nickName);
        tv_phoneNum = (TextView) findViewById(R.id.tv_num);
        tv_delete = (TextView) findViewById(R.id.tv_delete);

        iv_back.setOnClickListener(this);
        tv_delete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finish();
            break;
            case R.id.tv_delete:
                HttpRequest.getInstance().deleteFriendRequest(UserInfoActivity.this,userID,UserInfoActivity.this);
                break;
        }
    }


    private void loaderAvatar() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.NONE)
                .displayer(new FadeInBitmapDisplayer(500))
                .considerExifParams(true)
                .build();
        Logger.e(TAG,"LoaderAvatar = " + avator);
        if(avator != "" && avator != null){
            loaderImage(avator, iv_avator, options);
        }
    }

    //通过imageLoader加载图片
    public void loaderImage(String url,ImageView iv_long,DisplayImageOptions options){
        ImageLoader.getInstance()
                .displayImage(url, iv_long, options, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
//						spinner.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        String message = null;
                        switch (failReason.getType()) {
                            case IO_ERROR:
//								message = mActivity.getString(R.string.check_network_avatar);
                                break;
                            case DECODING_ERROR:
//								message = mActivity.getString(R.string.check_network_avatar);
                                break;
                            case NETWORK_DENIED:
                                message = UserInfoActivity.this.getString(R.string.check_network_avatar);
                                Toast.makeText(UserInfoActivity.this, message, Toast.LENGTH_SHORT).show();
                                break;
                            case OUT_OF_MEMORY:
//								message = mActivity.getString(R.string.check_network_avatar);
                                break;
                            case UNKNOWN:
//								message = mActivity.getString(R.string.check_network_avatar);
                                break;
                        }
//						spinner.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    }
                });
    }


    @Override
    public void deleteSuccess() {
        Logger.e("OchatFragment2","deleteSuccess成功");
                        DBManager dbManager = new DBManager(this);
                if (userID != null){
                    dbManager.deleteOneFriend(userID);
                }
        List<Activity> activitys = OcupApplication.getInstance().mList;
        //除了MainActivity就是chatActivity,删除意味着直接回退到MainActivity中
        activitys.get(1).finish();
        mhandler.sendEmptyMessage(0);
        finish();
    }

    @Override
    public void deleteFail(int type) {
        switch (type){
            case 400:
                Logger.e(TAG,"好友ID不正确");
                break;
            case 404:
                Logger.e(TAG,"找不到好友");
                break;
            case 500:
                mhandler.sendEmptyMessage(1);
                Logger.e(TAG,"服务器内部错误");
                break;
        }
        finish();
    }

}
