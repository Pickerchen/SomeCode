package com.sen5.ocup.yili;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.ybq.android.spinkit.style.Circle;
import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.activity.ChooseActivity;
import com.sen5.ocup.callback.RequestCallback;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;

import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import static com.sen5.ocup.R.id.btn_login;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener,RequestCallback.ILoginCallBack,RequestCallback.IGetInfoCallBack{
    //ui
    private LinearLayout ll_widget;
    private LinearLayout ll_login;
    private EditText mUseName;
    private TextInputLayout mUserNameWrapper;
    private EditText mPassword2;
    private TextInputLayout mPasswordWrapper;
    private TextView mTvGetCode;
    private Button mBtnLogin;
    private Dialog mDialog;//loading对话框
    private Circle mCircleDrawable;
    //constant:
    private static String TAG = LoginActivity.class.getSimpleName();
    private final static int countDown = 2;//开始计时
    private final static int networkError = 0;//网络错误
    private final static int overNum = 8;//短信数量超额
    private final static int illegalPhoneNum = 6;//非法的电话号码
    private final static int loginFail = 10;//登录失败
    private final static int getUserInfoSuccess = 4;//获取用户信息成功，开始跳转
    //data:
    private String phone;
    private String code;
    private int second =59;
    public  static LoginActivity instance;
    //flag:
    private boolean hasReceiver;//是否收到验证码
    private boolean isLOLLIPOP;//sdk是否大于21
    //logic:
    private EventHandler eh;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case countDown:
                    mTvGetCode.setBackgroundResource(R.drawable.shape_time_countdown);
                    mBtnLogin.setBackgroundResource(R.drawable.selector_register_sure2);
                    mTvGetCode.setText(second+"秒");
                    second --;
                    if (second >0) {
                        mHandler.sendEmptyMessageDelayed(2, 1000);
                    }
                    else {
                        mTvGetCode.setText(getString(R.string.getcode));
//                            mTvGetCode.setBackground(getDrawable(R.drawable.selector_timecountdown));
                            mTvGetCode.setBackgroundResource(R.drawable.selector_timecountdown);
                        mTvGetCode.setClickable(true);
                    }
                    break;
                case networkError:
                    Tools.showToast(LoginActivity.this,getString(R.string.check_network));
                    mTvGetCode.setBackgroundResource(R.drawable.selector_timecountdown);
                    mTvGetCode.setClickable(true);
                    break;
                case overNum:
                    Tools.showToast(LoginActivity.this,getString(R.string.overNum));
//                    mTvGetCode.setBackground(getDrawable(R.drawable.selector_timecountdown));
                    mTvGetCode.setBackgroundResource(R.drawable.selector_timecountdown);
                    mTvGetCode.setClickable(true);
                    break;
                case illegalPhoneNum:
                    Tools.showToast(LoginActivity.this,getString(R.string.inputcorretphoneNum));
//                    mTvGetCode.setBackground(getDrawable(R.drawable.selector_timecountdown));
                    mTvGetCode.setBackgroundResource(R.drawable.selector_timecountdown);
                    mTvGetCode.setClickable(true);
                    break;
                case loginFail:
                    mDialog.dismiss();
                    String content = (String) msg.obj;
                    Tools.showToast(LoginActivity.this,content);
                    break;
                case getUserInfoSuccess:
                    optimizeUI();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_login);
        initView();
        initSDK();
        instance = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            isLOLLIPOP = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 保存http单例的信息
        HttpRequest.getInstance().saveStatues();
        SMSSDK.unregisterEventHandler(eh);
    }

    private void initView() {
                ll_widget = (LinearLayout)findViewById(R.id.ll_widget);
                        mBtnLogin = (Button) findViewById(btn_login);
        mBtnLogin.setOnClickListener(this);
                        ll_login = (LinearLayout)findViewById(R.id.activity_login);
                        mUseName = (EditText)findViewById(R.id.username);
                        mUseName.addTextChangedListener(mTextWatcher);
                        mUserNameWrapper = (TextInputLayout)findViewById(R.id.userNameWrapper);
                        mPassword2 = (EditText)findViewById(R.id.password2);
                        mPasswordWrapper = (TextInputLayout)findViewById(R.id.passwordWrapper);
                        mTvGetCode = (TextView)findViewById(R.id.tv_getCode);
        mTvGetCode.setOnClickListener(this);
        mUserNameWrapper.setHint(getString(R.string.inputphoneNum));
        mPasswordWrapper.setHint(getString(R.string.inputcode));
    }

    private void showDialog() {
        mDialog = new Dialog(LoginActivity.this,R.style.custom_dialog_loading);
        mDialog.setContentView(R.layout.dialog_register_loading);
        mDialog.getWindow().setLayout((1* Tools.getScreenWH(this)[0])/2,200);
        ImageView imageView = (ImageView) mDialog.findViewById(R.id.dialog_loading_iv);
        mCircleDrawable = new Circle();
        imageView.setBackground(mCircleDrawable);
        mCircleDrawable.setColor(android.graphics.Color.parseColor("#FF818C"));
        mCircleDrawable.start();
        mDialog.show();
    }
    //登录操作
    private void login(){
        phone = mUseName.getText().toString();
        showDialog();
        code = mPassword2.getText().toString();
        HttpRequest.getInstance().login(LoginActivity.this,phone,code,0,LoginActivity.this);
    }
    //获取用户信息
    private void getUserInfo() {
        HttpRequest.getInstance().getUserInfo(LoginActivity.this,this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_getCode:
                    phone = mUseName.getText().toString();
                if (phone == "" || phone.length() != 11){
                    Tools.showToast(LoginActivity.this,getString(R.string.inputcorretphoneNum));
                    return;
                }
                    SMSSDK.getVerificationCode("86", phone);
                    mTvGetCode.setClickable(false);//发送完之后获取验证码不能再点击
                break;
            case btn_login:
                if (mUseName.getText().toString().length() == 11 && mPassword2.getText().toString().length() >= 4){
                    login();
                }
                else {
                        Tools.showToast(LoginActivity.this,"请输入正确的号码或验证码");
                }
                break;
        }
    }

    private void initSDK() {
        eh = new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    //回调完成
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        //提交验证码，并验证成功提交用户资料
                        Logger.e("TAG", "提交验证码成功" + data.toString());
                        HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
                        String country = (String) phoneMap.get("country");
                        phone = (String) phoneMap.get("phone");
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        //获取验证码成功
                        Logger.e("TAG", "获取验证码成功" + data.toString());
                        hasReceiver = true;
                        mHandler.sendEmptyMessage(countDown);
                    } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                        //返回支持发送验证码的国家列表
                    } else if (event == SMSSDK.EVENT_SUBMIT_USER_INFO) {
                        //提交已注册成功的用户信息
                        Logger.e("用户已提交注册资料");
                    } else if (event == SMSSDK.EVENT_GET_CONTACTS) {

                    } else if (event == SMSSDK.EVENT_GET_NEW_FRIENDS_COUNT) {
                    } else if (event == SMSSDK.EVENT_GET_FRIENDS_IN_APP) {
                    }
                } else {
                    ((Throwable) data).printStackTrace();
                    //输入的短信有误
                    Logger.e(((Throwable) data).getMessage() + "错误信息");
                    if (((Throwable) data).getMessage() != null) {
                        if (((Throwable) data).getMessage().contains("No address associated with hostname")) {
                            mHandler.sendEmptyMessage(networkError);
                        } else if (((Throwable) data).getMessage().contains("\"请填写正确的手机号码\"")) {
                            mHandler.sendEmptyMessage(illegalPhoneNum);
                        } else if (((Throwable) data).getMessage().contains("\"非法手机号\"")) {
                            mHandler.sendEmptyMessage(illegalPhoneNum);
                        } else if (((Throwable) data).getMessage().contains("数量超过限额")) {
                            mHandler.sendEmptyMessage(overNum);
                        }
                        else if (((Throwable)data).getMessage().contains("socket failed: EACCES")){
                            mHandler.sendEmptyMessage(networkError);
                        }
                    }
                }
            }
        };
        //注册短信验证的监听
        SMSSDK.registerEventHandler(eh);
    }

    //et_username监听器
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
            if (temp.length() >= 11){
                mTvGetCode.setBackgroundResource(R.drawable.selector_timecountdown2);
            }
        }
    };

    @Override
    public void getSuccess(int type, String content) {
        //获取用户信息成功之后则进行页面跳转
        if (type == UtilContact.getUserInfo){
            Logger.e("获取好友信息成功,sendMessage开始发送切线程");
            mHandler.sendEmptyMessage(getUserInfoSuccess);
        }
    }

    @Override
    public void getFail(int type) {
        switch (type){

        }
    }

    @Override
    public void getIng(int type) {

    }

    @Override
    public void loginSuccess() {
        //登录成功回调
        Logger.e("登录成功回调");
        getUserInfo();
    }

    @Override
    public void loginFail(int type) {
        String content = null;
        Message message = new Message();
        //登录失败
        switch (type){
            case UtilContact.loginFail_paramError:
                content = getString(R.string.paramsWrong);
                break;
            case UtilContact.loginFail_unVerify:
                content = getString(R.string.unVerify);
                break;
            case UtilContact.loginFail_webError:
                content = getString(R.string.webIsWrong);
                break;
            default:
                break;
        }
            message.what = loginFail;
            message.obj = content;
            mHandler.sendMessage(message);
    }

    @Override
    public void RegisterSuccess() {

    }

    @Override
    public void RegisterFail(int type) {

    }

    //动画优化UI效果
    public void optimizeUI(){
        if (mCircleDrawable != null) {
            mCircleDrawable.stop();
            mDialog.dismiss();
        }
        ll_widget.setVisibility(View.GONE);
        ObjectAnimator moveIn = ObjectAnimator.ofFloat(ll_login, "translationX", -500f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ll_login,"scaleY",1.0f,1.2f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ll_login,"scaleX",1.0f,1.2f);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(ll_login, "rotation", 0f, 360f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY);
//                    animSet.play(rotate).with(scale).after(moveIn);
        animSet.setDuration(800);
        animSet.start();
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Intent intent = new Intent(LoginActivity.this, ChooseActivity.class);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                    startActivity(intent);
                }
                else {
                    startActivity(intent,
                            ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this).toBundle());
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
}
