package com.sen5.ocup.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.Circle;
import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.callback.RequestCallback;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;

import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

//这个界面需要全屏，状态栏也没有
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener,
        RequestCallback.ILoginCallBack,RequestCallback.IGetInfoCallBack{

    private EditText et_phoneNum,et_sms;
    private Button btn_sure,btn_reLogin,btn_login,btn_sure1,btn_sure2,btn_sure3,btn_sure4;
    private LinearLayout ll_sms,ll_phoneNum;
    private TextInputLayout userNameTIL;
    private TextInputLayout passWordTIL;
    private TextView tv_again,tv_tile;
    private int second = 59;
    private Circle mCircleDrawable;
    private Dialog mDialog;
    private EventHandler eh;
    private boolean isLogin;
    private long lastShowToastTime = 0;

    /*
    data
     */
    private DBManager mDBManager;
    private String phone;
    private String code;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    Toast.makeText(RegisterActivity.this,"请先连接网络",Toast.LENGTH_LONG).show();
                    break;
                case 6:
                    Toast.makeText(RegisterActivity.this,"请输入正确的手机号码",Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    tv_again.setText(second+"秒");
                    second --;
                    if (second >0) {
                        mHandler.sendEmptyMessageDelayed(2, 1000);
                    }
                    else {
                        tv_again.setText("重新发送");
                        tv_again.setClickable(true);
                    }
                    break;
                case 3:
                    switch (msg.arg1){
                        case 400:
                            Toast.makeText(RegisterActivity.this,R.string.paramsWrong,Toast.LENGTH_LONG).show();
                            break;
                        case 401:
                            Toast.makeText(RegisterActivity.this,R.string.codeIsWrong,Toast.LENGTH_LONG).show();
                            break;
                        case 409:
                            Toast.makeText(RegisterActivity.this,R.string.phoneNumHasRegistered,Toast.LENGTH_LONG).show();
                            break;
                        case 500:
                            Toast.makeText(RegisterActivity.this,R.string.check_network,Toast.LENGTH_LONG).show();
                            break;
                    }
                    mDialog.dismiss();
                    break;
                //获取好友信息成功后把dialog消失，然后进行跳转
                case 4:
                    if (mCircleDrawable != null) {
                        mCircleDrawable.stop();
                        mDialog.dismiss();
                    }
                    break;
                case 5:
                    register();
                    break;
                case 7:
                    ll_sms.setVisibility(View.VISIBLE);
                    tv_again.setClickable(false);
                    mHandler.sendEmptyMessage(2);
                    btn_sure.setText(getString(R.string.register));
                    btn_reLogin.setText(getString(R.string.login));
                    btn_login.setText(getString(R.string.login));
                    btn_login.setBackgroundResource(R.drawable.selector_register_sure2);
                    btn_reLogin.setBackgroundResource(R.drawable.selector_register_sure2);
                    break;
                case 8:
                    Toast.makeText(RegisterActivity.this,"该手机号码今日发送短信数量已超过限额",Toast.LENGTH_LONG).show();
                    break;
                case 9:
                        Toast.makeText(RegisterActivity.this,getString(R.string.loginFail),Toast.LENGTH_LONG).show();
                    if (mDialog != null){
                        mDialog.dismiss();
                    }
                    break;
            }
        }
    };

    /*
    flag:是否收到短信
     */
    private boolean hasReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initSDK();
        initView();

        Intent intent = getIntent();
        //cookies验证过期需要进行重新登录
        boolean isReLogin = intent.getBooleanExtra("isReLogin",false);
        phone = intent.getStringExtra("phone");

        //实例化短信监听器
//        SMSContentObserver mObserver = new SMSContentObserver(this, new Handler(),et_sms);
// 注册短信变化监听
//        getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mObserver);
        mDBManager = new DBManager(this);
    }

    private void initSDK() {
        eh = new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    //确保返回值正确
                    mHandler.sendEmptyMessage(7);
                    //回调完成
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        //提交验证码，并验证成功提交用户资料
                        Logger.e("TAG", "提交验证码成功"+data.toString());
                        HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
                        String country = (String) phoneMap.get("country");
                        phone = (String) phoneMap.get("phone");
                        //提交用户资料到mob服务器
//                        registerUser("86",phone);
                        //在注册sen5服务器成功之后提交用户信息到mob
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        //获取验证码成功
                        Logger.e("TAG", "获取验证码成功"+data.toString());
                        hasReceiver = true;
                    } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                        //返回支持发送验证码的国家列表
                    }
                    else if (event == SMSSDK.EVENT_SUBMIT_USER_INFO){
                        //提交已注册成功的用户信息
                        Logger.e("用户已提交注册资料");
                        //提交用户信息成功后进行sen5服务器验证
                        mHandler.sendEmptyMessage(5);
//                        register();
//                           btn_sure5.performClick();
                    }
                    else if (event == SMSSDK.EVENT_GET_CONTACTS){
                        Logger.e("获取手机联系人列表");
                        if (data != null){
                            Logger.e("获取手机联系人列表2"+data.toString());
                        }
                    }
                    else if (event == SMSSDK.EVENT_GET_NEW_FRIENDS_COUNT){
                        Logger.e("EVENT_GET_NEW_FRIENDS_COUNT");
                        if (data != null){
                            Logger.e("EVENT_GET_NEW_FRIENDS_COUNT2"+data.toString());
                        }
                    }
                    else if (event == SMSSDK.EVENT_GET_FRIENDS_IN_APP){
                        Logger.e("EVENT_GET_FRIENDS_IN_APP");
                        if (data != null){
                            Logger.e("EVENT_GET_FRIENDS_IN_APP"+data.toString());
                        }
                    }
                } else{
                    ((Throwable) data).printStackTrace();
                    //输入的短信有误
                    Logger.e(((Throwable) data).getMessage()+"错误信息");
                    if (((Throwable) data).getMessage() != null) {
                        if (((Throwable) data).getMessage().contains("No address associated with hostname")) {
                            mHandler.sendEmptyMessage(0);
                        } else if (((Throwable) data).getMessage().contains("\"请填写正确的手机号码\"")) {
                            mHandler.sendEmptyMessage(6);
                        } else if (((Throwable) data).getMessage().contains("\"非法手机号\"")) {
                            mHandler.sendEmptyMessage(6);
                        } else if (((Throwable) data).getMessage().contains("数量超过限额")) {
                            mHandler.sendEmptyMessage(8);
                        }
                    }
                }
            }
        };
        //注册短信验证的监听
        SMSSDK.registerEventHandler(eh);
    }

    private void initView() {
        et_phoneNum = (EditText) findViewById(R.id.et_phoneNum);
        et_phoneNum.setSelection(0);
        //注册按钮
        btn_sure = (Button) findViewById(R.id.btn_sure);
        //登录过期重新登录按钮
        btn_reLogin = (Button) findViewById(R.id.btn_relogin);
        //手动注销，登录按钮
        btn_login = (Button) findViewById(R.id.btn_login);

        btn_sure1 = (Button) findViewById(R.id.btn_sure1);
        btn_sure2 = (Button) findViewById(R.id.btn_sure2);
        btn_sure3 = (Button) findViewById(R.id.btn_sure3);
        btn_sure4 = (Button) findViewById(R.id.btn_sure4);

        ll_sms = (LinearLayout)findViewById(R.id.ll_sms);
        ll_phoneNum = (LinearLayout) findViewById(R.id.ll_phone);
        tv_again = (TextView)findViewById(R.id.tv_again);
        tv_tile = (TextView) findViewById(R.id.tv_title);
        et_sms = (EditText) findViewById(R.id.et_sms);

        et_phoneNum.addTextChangedListener(mTextWatcher);
        btn_sure.setOnClickListener(this);
        btn_sure1.setOnClickListener(this);
        btn_sure2.setOnClickListener(this);
        btn_sure3.setOnClickListener(this);
        btn_sure4.setOnClickListener(this);
        tv_again.setOnClickListener(this);
        btn_reLogin.setOnClickListener(this);
        btn_login.setOnClickListener(this);


        userNameTIL = (TextInputLayout) findViewById(R.id.userNameWrapper);
        passWordTIL = (TextInputLayout) findViewById(R.id.passwordWrapper);
        userNameTIL.setHint(getString(R.string.inputcode));
        passWordTIL.setHint(getString(R.string.inputphoneNum));
    }

    //editText监听器
    TextWatcher mTextWatcher = new TextWatcher() {
        private CharSequence temp;
        private int editStart;
        private int editEnd;
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub
            temp = s;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub
//          mTextView.setText(s);//将输入的内容实时显示
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            editStart = et_phoneNum.getSelectionStart();
            editEnd = et_phoneNum.getSelectionEnd();
            if (temp.length() >= 11){
               btn_sure.setBackgroundColor(getResources().getColor(R.color.main_color));
                btn_sure.setBackgroundResource(R.drawable.selector_register_sure2);
                btn_login.setBackgroundResource(R.drawable.selector_register_sure2);
                btn_reLogin.setBackgroundResource(R.drawable.selector_register_sure2);
                et_phoneNum.removeTextChangedListener(this);
            }
        }
    };

    //到sen5服务器注册
    private void register(){
        code = et_sms.getText().toString();
        HttpRequest.getInstance().register(RegisterActivity.this,phone,code,RegisterActivity.this);
        showDialog();
    }


private void showDialog() {
    mDialog = new Dialog(RegisterActivity.this,R.style.custom_dialog_loading);
    mDialog.setContentView(R.layout.dialog_register_loading);
    mDialog.getWindow().setLayout((1* Tools.getScreenWH(this)[0])/2,200);
    ImageView imageView = (ImageView) mDialog.findViewById(R.id.dialog_loading_iv);
    mCircleDrawable = new Circle();
    imageView.setBackground(mCircleDrawable);
    mCircleDrawable.setColor(android.graphics.Color.parseColor("#FF818C"));
    mCircleDrawable.start();
    mDialog.show();
    }
    private void login(String phone){
//        phone = et_phoneNum.getText().toString();
        showDialog();
        code = et_sms.getText().toString();
        HttpRequest.getInstance().login(RegisterActivity.this,phone,code,0,RegisterActivity.this);
    }

    private void getUserInfo() {
        HttpRequest.getInstance().getUserInfo(RegisterActivity.this,this);
    }

    private void getFriendsInfo() {
        HttpRequest.getInstance().getUserFriends(RegisterActivity.this,this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_sure:
                if (et_phoneNum.getText().toString().length() == 11){
                    if (et_sms.getText().toString().length() == 4 && hasReceiver) {
//                        register();
                        SMSSDK.submitUserInfo("", "测试", "", "86", et_phoneNum.getText().toString());
                    }
                    else if (hasReceiver){
                        Toast.makeText(RegisterActivity.this,"请输入正确的验证码",Toast.LENGTH_SHORT).show();
                    }
                    else if (!hasReceiver){
                        phone = et_phoneNum.getText().toString();
                        SMSSDK.getVerificationCode("86", phone);
                    }
                }
                else {
                    Toast.makeText(RegisterActivity.this,"请输入正确的电话号码",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_again:
                phone = et_phoneNum.getText().toString();
                SMSSDK.getVerificationCode("86", phone);
                second = 60;
                mHandler.sendEmptyMessage(2);
                tv_again.setClickable(false);
                break;

            case R.id.btn_relogin:
//                if (phone == "" || phone == null){
//                    phone = Tools.getPreference(RegisterActivity.this, UtilContact.Phone_Num);
//                }
                phone = et_phoneNum.getText().toString();
                if(phone.toString().length() != 11){
                        Toast.makeText(RegisterActivity.this,"请输入正确的电话号码",Toast.LENGTH_SHORT).show();
                }
                if (! hasReceiver) {
                    SMSSDK.getVerificationCode("86", phone);
                    btn_reLogin.setText(getString(R.string.login));
                    hasReceiver = true;
                }
                else if (et_sms.getText().toString().length() == 4){
                    Logger.e("registerActivity","本地保存的号码为："+phone);
                    login(phone);
                }
                else {
                    Toast.makeText(RegisterActivity.this,"请输入正确的验证码",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btn_login:
                if (et_phoneNum.getText().toString().length() == 11){
                    if (et_sms.getText().toString().length() == 4 && hasReceiver) {
                        login(phone);
                    }
                    else if (hasReceiver){
                        Toast.makeText(RegisterActivity.this,"请输入正确的验证码",Toast.LENGTH_SHORT).show();
                    }
                    else if (!hasReceiver){
                        phone = et_phoneNum.getText().toString();
                        SMSSDK.getVerificationCode("86", phone);
                    }
                }
                else {
                    Toast.makeText(RegisterActivity.this,"请输入正确的电话号码",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btn_sure1:
                login(Tools.getPreference(RegisterActivity.this,UtilContact.Phone_Num));
                break;

            case R.id.btn_sure2:
                getUserInfo();
                break;
            case R.id.btn_sure3:
                getFriendsInfo();
                break;

            case R.id.btn_sure4:
                Intent intent = new Intent(RegisterActivity.this,ChooseActivity.class);
                startActivity(intent);
                break;
        }
    }

    //登录和注册共用一个接口:ILoginCallBack
    @Override
    public void loginSuccess() {
        //登录成功回调
        Logger.e("登录成功回调");
        getUserInfo();
    }

    @Override
    public void loginFail(int type) {
        if ((System.currentTimeMillis()-lastShowToastTime) > 1000){
            lastShowToastTime = System.currentTimeMillis();
            mHandler.sendEmptyMessage(9);
        }
    }

    @Override
    public void RegisterSuccess() {
        Logger.e("注册成功回调");
        //注册成功，进行登录操作
        getUserInfo();
    }



    @Override
    public void RegisterFail(int type) {
        Message message = new Message();
        switch (type){
            case 400:
                message.arg1 = 400;
                message.what = 3;
                mHandler.sendMessage(message);
            break;
            case 401:
                message.arg1 = 401;
                message.what = 3;
                mHandler.sendMessage(message);
                break;
            case 409:
                message.arg1 = 409;
                message.what = 3;
                mHandler.sendMessage(message);
                break;
            case 500:
                message.arg1 = 500;
                message.what = 3;
                mHandler.sendMessage(message);
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 保存http单例的信息
        HttpRequest.getInstance().saveStatues();
        SMSSDK.unregisterEventHandler(eh);
    }

    @Override
    public void getSuccess(int type, String content) {
        //获取用户信息成功之后则进行页面跳转
         if (type == UtilContact.getUserInfo){
            Logger.e("获取好友信息成功");
            mHandler.sendEmptyMessage(4);
            Intent intent = new Intent(RegisterActivity.this, ChooseActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void getFail(int type) {
    }

    @Override
    public void getIng(int type) {

    }
}