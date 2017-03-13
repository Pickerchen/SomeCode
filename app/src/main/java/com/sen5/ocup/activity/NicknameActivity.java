package com.sen5.ocup.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.Circle;
import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.callback.RequestCallback;
import com.sen5.ocup.callback.RequestCallback.UpdateUserInfoCallback;
import com.sen5.ocup.gui.SegoTextView;
import com.sen5.ocup.receiver.HomeWatcher;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.Tools;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 修改昵称界面
 */
public class NicknameActivity extends BaseActivity implements UpdateUserInfoCallback, Callback {

	private static final String TAG = "NicknameActivity";
	private HomeWatcher mHomeKeyReceiver = null;
	private Handler mHandler = null;
	private LinearLayout layout_back = null;
	private SegoTextView mTV_nameLength = null;
	private EditText et_rename = null;
	private SegoTextView tv_save = null;
	private String oldname;
	private String newname;
	private Dialog mDialog;
	private Circle mCircleDrawable;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nickname);

		oldname = getIntent().getStringExtra("oldname");
		initView();
		initData();
	}

	private void showDialog() {
		mDialog = new Dialog(NicknameActivity.this,R.style.custom_dialog_loading);
		mDialog.setContentView(R.layout.dialog_register_loading);
		mDialog.getWindow().setLayout((1* Tools.getScreenWH(this)[0])/2,200);
		ImageView imageView = (ImageView) mDialog.findViewById(R.id.dialog_loading_iv);
		mCircleDrawable = new Circle();
		imageView.setBackground(mCircleDrawable);
		mCircleDrawable.setColor(android.graphics.Color.parseColor("#FF818C"));
		mCircleDrawable.start();
		mDialog.show();
	}


	/**
	 * 初始化控件
	 */
	private void initView() {
		FrameLayout titleLayout = (FrameLayout) findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(titleLayout,this);
		layout_back = (LinearLayout) findViewById(R.id.ll_back);
		mTV_nameLength = (SegoTextView) findViewById(R.id.textview_namelength);
		tv_save = (SegoTextView) findViewById(R.id.tv_save);
		et_rename = (EditText) findViewById(R.id.et_rename);
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		mHandler = new Handler(NicknameActivity.this);
		mHomeKeyReceiver = new HomeWatcher(this);
		et_rename.setText(oldname);
		//进入页面是选中文字
		if (oldname.length() > 0) {
			if (oldname.length() < 20) {
				et_rename.setSelection(0, oldname.length() - 1);
			} else {
				et_rename.setSelection(0, 19);
			}
		}
		et_rename.setSelectAllOnFocus(true);
		
		if (et_rename.getText().toString().length() == 0) {
			tv_save.setClickable(false);
			tv_save.setAlpha(0.4f);
		}else{
			tv_save.setClickable(true);
			tv_save.setAlpha(1.0f);
		}
		mTV_nameLength.setText(et_rename.getText().toString().length()+getString(R.string.content_length));
		
		et_rename.setOnFocusChangeListener(mOnFocusChangeListener);
		tv_save.setOnClickListener(mOnClickListener);
		layout_back.setOnClickListener(mOnClickListener);
		et_rename.addTextChangedListener(mTextWatch);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mHomeKeyReceiver.startWatch();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop-------");
		mHomeKeyReceiver.stopWatch();
	}

	/**
	 * 昵称编辑框的文本变化监听
	 */
	private TextWatcher mTextWatch = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			Log.d(TAG, "onTextChanged-------count=="+count);
			mTV_nameLength.setText(et_rename.getText().toString().length()+getString(R.string.content_length));
			if (et_rename.getText().toString().length() == 0) {
				tv_save.setClickable(false);
				tv_save.setAlpha(0.4f);
			}else{
				tv_save.setClickable(true);
				tv_save.setAlpha(1.0f);
			}
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
		}
	};
	
	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.ll_back) {
				NicknameActivity.this.finish();
			} else if (v.getId() == R.id.tv_save) {
				newname = et_rename.getText().toString();
				if (newname.length() <= 0) {
					Toast.makeText(NicknameActivity.this, getString(R.string.rename_null), Toast.LENGTH_LONG).show();
					return;
				}
				if (MainActivity.netState != 3){
					showDialog();
					HttpRequest.getInstance().updateUserInfo(NicknameActivity.this, newname, new RequestCallback.IUpdateInfoCallBack() {
						@Override
						public void updateSuccess() {
							Logger.e("NickName","更新成功");
							mHandler.sendEmptyMessage(modifyName_OK);
						}
						@Override
						public void updateFail(int type) {
							if (type == 401){
								Message message = new Message();
								message.arg1 = 401;
								message.what = modifyName_NO;
								mHandler.sendMessage(message);
							}
							else {
								mHandler.sendEmptyMessage(modifyName_NO);
							}
							Logger.e("NickName","更新失败");

						}
					});
				}
				else {
					//断网提示
					Toast.makeText(NicknameActivity.this,getString(R.string.check_network_avatar),Toast.LENGTH_LONG).show();
				}
			}
		}
	};

	/**
	 * 若昵称编辑框有焦点，弹出软键盘并选中所有文字
	 */
	private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				et_rename.selectAll();
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			}
		}
	};

	@Override
	public void updateCupInfo_OK() {
		OcupApplication.getInstance().mOwnCup = new DBManager(getApplicationContext()).queryOwnCup(OcupApplication.getInstance().mOwnCup.getCupID());
		OcupApplication.getInstance().mOwnCup.setName(newname, 1);
		
		mHandler.sendEmptyMessage(modifyName_OK);
	}

	@Override
	public void updateCupInfo_NO() {
		mHandler.sendEmptyMessage(modifyName_NO);
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (NicknameActivity.this.getWindow().isActive()) {
			switch (msg.what) {
			//修改昵称成功
			case modifyName_OK:
				mDialog.dismiss();
				 Intent resultIntent = new Intent();
				String name = et_rename.getText().toString();
				 resultIntent.putExtra("newName",name);
				Logger.e("NicknameActivity",name);
				 Toast.makeText(NicknameActivity.this, getString(R.string.save_success), Toast.LENGTH_LONG).show();
				 NicknameActivity.this.setResult(RESULT_OK, resultIntent);
				OcupApplication.getInstance().mOwnCup.setName(name,2);
				 finish();
				break;
			//修改昵称失败
			case modifyName_NO:
				mDialog.dismiss();
				if (msg.arg1 == 401){
					Intent intent = new Intent(NicknameActivity.this,RegisterActivity.class);
					intent.putExtra("isReLogin",true);
					startActivity(intent);
					Toast.makeText(NicknameActivity.this,getString(R.string.loginDate),Toast.LENGTH_LONG).show();
				}
				else {
					Toast.makeText(NicknameActivity.this, getString(R.string.save_failed), Toast.LENGTH_LONG).show();
				}
				finish();
				break;
			default:
				break;
			}
		}
		return false;
	}

	private final static int modifyName_OK = 1;
	private final static int modifyName_NO = 2;
}
