package com.sen5.ocup.yili;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.Circle;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.callback.CustomInterface;
import com.sen5.ocup.callback.RequestCallback;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;

/**
 * Created by chenqianghua on 2016/12/6.
 * 手机联系人详情界面，用来判断是否是我们的用户
 */
public class ContactsInfoActivity extends Activity implements RequestCallback.IGetInfoCallBack,RequestCallback.IAddFriendCallBack,CustomInterface.IDialog{

    private static String TAG = ContactsActivity.class.getSimpleName();
    private final int paramsError = 1;
    private final int webError = 2;
    private final int notFound = 3;
    private final int isGetingInfo = 4;
    private final int requestSuccess =5;
    private final int hasAdded =6;
    private TextView tv_num,tv_name,tv_invite;
    private ImageView iv_avator,iv_back;
    private CustomDialog addFriendDialog;
    //通过intent传的值：
    private boolean isFriend;
    private String name;
    private String phone;
    private String url;

    //当前点击的用户ID,通过请求下来的
    private String id;

    //handle
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case requestSuccess:
                    mDialog.dismiss();
                    tv_invite.setText(getString(R.string.sendAdd));
                break;
                //对比失败，默认显示发送邀请
                case paramsError:
                    showToast(getString(R.string.paramsWrong));
                    tv_invite.setText(getString(R.string.sendInvite));
                    mDialog.dismiss();
                    break;
                case webError:
                    showToast(getString(R.string.webIsWrong));
                    tv_invite.setText(getString(R.string.sendInvite));
                    mDialog.dismiss();
                    break;
                case notFound:
                    tv_invite.setText(getString(R.string.sendInvite));
                    mDialog.dismiss();
                    break;
                case isGetingInfo:
                    showToast(getString(R.string.isGettingInfos));
                    tv_invite.setText(getString(R.string.sendInvite));
                    mDialog.dismiss();
                    break;
                case hasAdded:
                    showToast(getString(R.string.hasAdded));
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactsinfo);
        initView();
        initdata();
    }

    private void initdata() {
       Intent intent =  getIntent();
        isFriend = intent.getBooleanExtra("isFriend",false);
        name = intent.getStringExtra("name");
        phone = intent.getStringExtra("phoneNum");
        url = intent.getStringExtra("avator");
        loaderAvatar();
        tv_name.setText(name);
        tv_num.setText(phone);
        if (isFriend){
            tv_invite.setText(getString(R.string.friend));
        }
        else {
            showDialog();
            HttpRequest.getInstance().checkPhoneNum(phone,ContactsInfoActivity.this,ContactsInfoActivity.this);
        }
        //邀请或者添加好友
        tv_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击进入发短信页面
                if (tv_invite.getText().toString().equals(getString(R.string.sendInvite))){
                        Tools.sendSms(phone,ContactsInfoActivity.this);
                }
                //点击进行添加好友页面
                else if (tv_invite.getText().toString().equals(getString(R.string.sendAdd))){
                    if (id != null){
                        if (Tools.getPreference(OcupApplication.getInstance(),UtilContact.Phone_Num).equals(phone)){
                            showToast(getString(R.string.add_friend_noaddmyself));
                        }
                        else {
                            addFriendDialog = new CustomDialog(ContactsInfoActivity.this, ContactsInfoActivity.this, R.style.custom_dialog, CustomDialog.PAIR_DIALOG, 0);
                            addFriendDialog.show();
                        }
                    }
                }
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_avator = (ImageView) findViewById(R.id.iv_user);
        tv_num = (TextView) findViewById(R.id.tv_num);
        tv_name = (TextView) findViewById(R.id.tv_nickName);
        tv_invite = (TextView) findViewById(R.id.tv_invite);
    }


    private Dialog mDialog;
    private Circle mCircleDrawable;
    private void showDialog() {
        mDialog = new Dialog(this,R.style.custom_dialog_loading);
        mDialog.setContentView(R.layout.dialog_register_loading);
        mDialog.getWindow().setLayout((1* Tools.getScreenWH(this)[0])/2,200);
        ImageView imageView = (ImageView) mDialog.findViewById(R.id.dialog_loading_iv);
        mCircleDrawable = new Circle();
        imageView.setBackground(mCircleDrawable);
        mCircleDrawable.setColor(android.graphics.Color.parseColor("#FF818C"));
        mCircleDrawable.start();
        mDialog.show();
    }

    private void showToast(String content) {
        Toast.makeText(this,content,Toast.LENGTH_LONG).show();
    }

    //加载头像
    private void loaderAvatar() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.NONE)
                .displayer(new FadeInBitmapDisplayer(500))
                .considerExifParams(true)
                .build();
        if(url != "" && url != null){
            Logger.e(TAG,"LoaderAvatar = " + url);
            loaderImage(url, iv_avator, options);
        }
    }

    //通过imageLoader加载图片
    public void loaderImage(String url,ImageView iv_long,DisplayImageOptions options){
        ImageLoader.getInstance()
                .displayImage(url, iv_long, options, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                    }
                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        String message = null;
                        switch (failReason.getType()) {
                            case IO_ERROR:
                                break;
                            case DECODING_ERROR:
                                break;
                            case NETWORK_DENIED:
                                message = ContactsInfoActivity.this.getString(R.string.check_network_avatar);
                                showToast(message);
                                break;
                            case OUT_OF_MEMORY:
                                break;
                            case UNKNOWN:
                                break;
                        }
                    }
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    }
                });
    }


    //进行服务器比对，看是否是我们的用户
    @Override
    public void getSuccess(int type, String content) {
        Logger.e(TAG,"this phoneNum s id is = "+content);
        id = content;
        if (type == UtilContact.checkPhoneNum){
            mHandler.sendEmptyMessage(requestSuccess);
        }
    }

    @Override
    public void getFail(int type) {
        Logger.e(TAG,"getFail == "+type);
        if (type == 400){
            mHandler.sendEmptyMessage(paramsError);
        }
        else if (type == 404){
            mHandler.sendEmptyMessage(notFound);
        }
        else if (type == 500){
            mHandler.sendEmptyMessage(webError);
        }
    }

    @Override
    public void getIng(int type) {
        mHandler.sendEmptyMessage(isGetingInfo);
    }

    //发送添加好友请求
    @Override
    public void sendSuccess(String token) {

    }

    @Override
    public void sendFail(int type) {

    }

    @Override
    public void hasAdded() {
        mHandler.sendEmptyMessage(hasAdded);
    }


    //dialog
    @Override
    public void ok(int type) {
        HttpRequest.getInstance().addFriendRequest(ContactsInfoActivity.this,id,ContactsInfoActivity.this);
    }

    @Override
    public void ok(int type, Object obj) {

    }

    @Override
    public void cancel(int type) {

    }
}
