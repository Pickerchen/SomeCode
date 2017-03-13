package com.sen5.ocup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.blutoothstruct.CupStatus;
import com.sen5.ocup.callback.RequestCallback;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.gui.MTextView;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.HuanxinUtil;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 对话框式活动（被加or被解除好友提示对话框以活动形式显示）
 */
public class DialogActivity extends BaseActivity implements RequestCallback.IAddFriendConfirmCallBack{

	private String TAG = DialogActivity.class.getSimpleName();

	public final static String dialogType ="dialogType";
	//添加他人为好友时：收到好友同意；他人添加自己为好友时：手动确定后。广播action。
	public static String CheckSure = "MakeFriendCheckSure";
	public final static int add_sure = 110;
	private int type;
	private String token;
	private String nickName;
	private final int comfirmPass = 1;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
				case comfirmPass:
					 String group_msg = "";
				if ("true".equals(Tools.getPreference(DialogActivity.this, UtilContact.isAlived))){
					if ("true".equals(Tools.getPreference(DialogActivity.this,UtilContact.OPENDATA))){
						group_msg = "#1#1#"+ CupStatus.getInstance().getCur_water_temp();
					}
					else {
						group_msg = "#1#1#0";
					}
				}
				else {
					if (Tools.getPreference(DialogActivity.this,UtilContact.BLUE_ADD) != null){
						group_msg = "#1#0#0";
					}
				}
					Logger.e(TAG,"handle comfirmpass"+"group_msg is "+group_msg);
				HuanxinUtil.getInstance().sendGroupMsg(DialogActivity.this,group_msg);
			}
		}
	};
	private OnClickListener mOnclickLisener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.btn_ok) {
				if (nickName != null){
					//确定完之后发送广播到OchatFragment中刷新好友列表
					Intent intent = new Intent(CheckSure);
					DialogActivity.this.sendBroadcast(intent);
					String group_msg = "";
					if ("true".equals(Tools.getPreference(DialogActivity.this, UtilContact.isAlived))){
						if ("true".equals(Tools.getPreference(DialogActivity.this,UtilContact.OPENDATA))){
							group_msg = "#1#1#"+ CupStatus.getInstance().getCur_water_temp();
						}
						else {
							group_msg = "#1#1#0";
						}
					}
					else {
						if (Tools.getPreference(DialogActivity.this,UtilContact.BLUE_ADD) != null){
							group_msg = "#1#0#0";
						}
					}
					HuanxinUtil.getInstance().sendGroupMsg(DialogActivity.this,group_msg);
					DialogActivity.this.finish();
				}

				if (type == CustomDialog.ADDED_DIALOG || type == CustomDialog.DEMATED_DIALOG) {
					HttpRequest.getInstance().addFriendConfirm(DialogActivity.this,token,DialogActivity.this);
				}
			}else if(v.getId() == R.id.btn_cancel){
				if (type == CustomDialog.ADDED_DIALOG || type == CustomDialog.DEMATED_DIALOG) {
					DialogActivity.this.finish();
				}
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_sendchat);
		getWindow().setLayout(4 * MainActivity.mScreenWidth / 5, LayoutParams.WRAP_CONTENT);
		Intent intent = getIntent();
		type = intent.getIntExtra(dialogType, -1);
		if (type == CustomDialog.ADDED_DIALOG){
			token = intent.getStringExtra("token");
		}
		Logger.e("DialogActivity","token = "+token);
		MTextView txt_dia_content = (MTextView) findViewById(R.id.txt_dia_content);
		Button btnOk = (Button) this.findViewById(R.id.btn_ok);
		Button btnCancel = (Button) this.findViewById(R.id.btn_cancel);
		  if (type == CustomDialog.ADDED_DIALOG) {
			txt_dia_content.setMText(DialogActivity.this.getString(R.string.added_ok));
		}else if (type == CustomDialog.DEMATED_DIALOG) {
			txt_dia_content.setMText(DialogActivity.this.getString(R.string.demated_ok));
		}
		  else if (type == add_sure){
			  nickName = intent.getStringExtra("nickName");
			  Logger.e(TAG,"dialogActivity nickName = "+nickName);
			  txt_dia_content.setMText(nickName+DialogActivity.this.getString(R.string.agreeToAdd));
			  btnCancel.setVisibility(View.GONE);
		  }
		
		btnOk.setOnClickListener(mOnclickLisener);
		btnCancel.setOnClickListener(mOnclickLisener);
	}

	@Override
	public void confirmSuccess() {
		Intent intent = new Intent(CheckSure);
		DialogActivity.this.sendBroadcast(intent);
		Logger.e(TAG,"确认添加好友成功");
		mHandler.sendEmptyMessageDelayed(comfirmPass,2000);
		DialogActivity.this.finish();
	}

	@Override
	public void confirmFail(int type) {
		Logger.e("确认失败");
		DialogActivity.this.finish();
	}
}
