package com.sen5.ocup.gui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.activity.MainActivity;
import com.sen5.ocup.blutoothstruct.CupPara;
import com.sen5.ocup.callback.CustomInterface.IDialog;
import com.sen5.ocup.struct.ChatMsgEntity;
import com.sen5.ocup.struct.RequestHost;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;
import com.sen5.ocup.zxing.CaptureActivity;

import java.util.Calendar;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

public class CustomDialog extends Dialog implements Callback {

	protected static final String TAG = "CustomDialog";
	//该测试版本号会在关于ocup界面点击空白4次后显示，主要为了进行版本控制，应在发布版本后及时更改
	private final static String testVersion = "2.0.2.2";
	private Context mContext;
	private Handler mHandler;

	// 对话框类型
	private int mType;

	private IDialog mCallback;
	private Object mInfo;
	private EditText et_mood;//

	private Thread mThread_timer;
	private TextView tv_hour;
	private TextView tv_min;
	private Button btneditmoodOk;
	private Button btneditmoodCancel;
	private TextView mTV_moodoffset;

	public CustomDialog(Context context, IDialog callback, int theme, int type, Object info) {
		super(context, theme);
		mContext = context;
		mCallback = callback;
		mType = type;
		mInfo = info;
		mHandler = new Handler(this);
	}

	public void setInfo(Object info) {
		mInfo = info;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initDialog();
	}

	@Override
	public void show() {
		if (this.isShowing()) {
			return;
		}
		super.show();
		switch (mType) {
		case EDITMOOD_DIALOG:
			// 设置编辑框第一次显示上一次的心情
			String oldmood = (String) mInfo;
			if (null != oldmood) {
				et_mood.setText(oldmood);
			} else {
				et_mood.setText("");
			}
			if (et_mood.getText().toString().length() == 0) {
				btneditmoodOk.setClickable(false);
				btneditmoodOk.setAlpha(0.4f);
			}else{
				btneditmoodOk.setClickable(true);
				btneditmoodOk.setAlpha(1.0f);
			}
			// 设置编辑框有焦点时弹出软键盘并选中所有文字
			// et_mood.setSelectAllOnFocus(true);
			break;
		case RECOVERY_DIALOG:
			if(null != mCheckBoxRecoveryCloudData){
				mCheckBoxRecoveryCloudData.setChecked(false);
			}
			break;
		}
	}
	private CheckBox mCheckBoxRecoveryCloudData;
	public boolean getCheckBoxRecoveryCloudData(){
		if(null == mCheckBoxRecoveryCloudData){
			return false;
		}
		return mCheckBoxRecoveryCloudData.isChecked();
	}
	
	
	private void initDialog() {
		this.setCanceledOnTouchOutside(true);
		switch (mType) {
		// 重新发送消息 // 删除配对杯子 //退出 与杯子解绑
		case SEND_DIALOG:
		case DEL_FRIEND_DIALOG:
		case RECOVERY_DIALOG:
		case SLEEP_DIALOG:
		case EXIT_DIALOG:
		case VERSION_DIALOG:
		case DEL_ALARM_DIALOG:
		case EXIT_FIRMWARE_DIALOG:
		case TIPS_FIRMWARE_DIALOG:
		case PAIR_DIALOG:
		case DIALOG_SCAN_CORRECTQR:
 	    case TENCENT_DIALOG:
		case TENCENT_DIALOG_LOGIN:
		case ADDED_DIALOG:
		case DEMATED_DIALOG:
		case TENCENT_DIALOG_ANOTHER_LOGIN:

			setContentView(R.layout.dialog_sendchat);
			// getWindow().setLayout(4 * mWidth / 5, mHeight / 5);
			getWindow().setLayout(9 * MainActivity.mScreenWidth / 10, LayoutParams.WRAP_CONTENT);

			MTextView txt_dia_content = (MTextView) findViewById(R.id.txt_dia_content);
			View ll_clearcloud = findViewById(R.id.ll_clearcloud);
			mCheckBoxRecoveryCloudData = (CheckBox)findViewById(R.id.checkbox_recoveryclouddata);
			Button btnOk = (Button) findViewById(R.id.btn_ok);
			Button btnCancel = (Button) findViewById(R.id.btn_cancel);
			if (mType == SEND_DIALOG) {
				txt_dia_content.setMText(mContext.getString(R.string.reSend));
			} else if (mType == DEL_FRIEND_DIALOG) {
				txt_dia_content.setMText(mContext.getString(R.string.del_cup_notify));
			} else if (mType == RECOVERY_DIALOG) {
				ll_clearcloud.setVisibility(View.VISIBLE);
				txt_dia_content.setMText(mContext.getString(R.string.recover_tips));
				mCheckBoxRecoveryCloudData.setText(mContext.getString(R.string.clear_cloud_data));
			} else if (mType == EXIT_DIALOG) {
				txt_dia_content.setMText(mContext.getString(R.string.exit_tips));
			} else if (mType == SLEEP_DIALOG) {
//				txt_dia_content.setMText(mContext.getString(R.string.shutdown_tips));
				txt_dia_content.setMText(mContext.getString(R.string.standby_text));
			} else if (mType == VERSION_DIALOG) {
				btnCancel.setVisibility(View.GONE);
				txt_dia_content.setMText(mContext.getString(R.string.test_version) + testVersion + "  " + mContext.getString(R.string.cup_version)
						+ CupPara.getInstance().getPara_verion() + ".0");
			} else if (mType == DEL_ALARM_DIALOG) {
				txt_dia_content.setMText(mContext.getString(R.string.del_tips));
			} else if (mType == EXIT_FIRMWARE_DIALOG) {
				txt_dia_content.setMText(mContext.getString(R.string.sure2exitUpdate));
			} else if (mType == TIPS_FIRMWARE_DIALOG) {
				txt_dia_content.setMText(mContext.getString(R.string.update_or_not));
			} else if (mType == PAIR_DIALOG) {
				txt_dia_content.setMText(mContext.getString(R.string.pair_text));
			}
			else if (mType == DIALOG_SCAN_CORRECTQR){
				txt_dia_content.setMText(mContext.getString(R.string.ScanCorrectQrcode));
				btnCancel.setVisibility(View.GONE);
			}
			else if(mType == TENCENT_DIALOG){
				txt_dia_content.setMText(mContext.getString(R.string.qq_auth_message));
			}else if(mType == TENCENT_DIALOG_LOGIN){
				txt_dia_content.setMText(mContext.getString(R.string.qq_login_message));
			}else if(mType == ADDED_DIALOG){
				txt_dia_content.setMText(mContext.getString(R.string.added_ok));
			}else if(mType == DEMATED_DIALOG){
				btnCancel.setVisibility(View.GONE);
				txt_dia_content.setMText(mContext.getString(R.string.demated_ok));
			}else if (mType == TENCENT_DIALOG_ANOTHER_LOGIN) {
				txt_dia_content.setMText(mContext.getString(R.string.qq_another_login_message));
				btnOk.setText(R.string.btn_switch);
			}
			btnOk.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mType == SEND_DIALOG) {
						mCallback.ok(mType, (Integer) mInfo);
					} else if (mType == DEL_FRIEND_DIALOG) {
						mCallback.ok(mType);
					} else if (mType == RECOVERY_DIALOG) {
						mCallback.ok(mType);
						return;
					} else if (mType == EXIT_DIALOG) {
						mCallback.ok(mType);
					} else if (mType == SLEEP_DIALOG) {
						mCallback.ok(mType);
					} else if (mType == DEL_ALARM_DIALOG) {
						mCallback.ok(mType, (Integer) mInfo);
					} else if (mType == VERSION_DIALOG) {
						mCallback.ok(mType);
					} else if (mType == EXIT_FIRMWARE_DIALOG) {
						mCallback.ok(mType);
					} else if (mType == TIPS_FIRMWARE_DIALOG) {
						mCallback.ok(mType);
					} else if (mType == PAIR_DIALOG) {
						mCallback.ok(mType);
					}else if (mType == TENCENT_DIALOG) {
						mCallback.ok(mType);
					}  else if (mType == TENCENT_DIALOG_LOGIN) {
						mCallback.ok(mType);
					}else if (mType == ADDED_DIALOG) {
					}else if (mType == DEMATED_DIALOG) {
					}else if (mType == TENCENT_DIALOG_ANOTHER_LOGIN) {
						mCallback.ok(mType);
					}
					dismiss();
				}
			});
			btnCancel.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mType == SEND_DIALOG) {
						mCallback.cancel(mType);
					} else if (mType == DEL_FRIEND_DIALOG) {
						mCallback.cancel(mType);
					} else if (mType == RECOVERY_DIALOG) {
					} else if (mType == EXIT_DIALOG) {
					} else if (mType == SLEEP_DIALOG) {
					}else if (mType == PAIR_DIALOG) {
						mCallback.cancel(mType);
					}
					dismiss();
				}
			});
			break;

		// 设置头像
		case SET_HEADIMAGE_DIALOG:
			setContentView(R.layout.dialog_set_headimage);
			getWindow().setGravity(Gravity.BOTTOM);
			getWindow().setLayout(MainActivity.mScreenWidth, MainActivity.mScreenHeight / 3);

			TextView txt_from_camera = (TextView) findViewById(R.id.txt_from_camera);
			TextView txt_from_photo = (TextView) findViewById(R.id.txt_from_photo);
			TextView txt_cancel = (TextView) findViewById(R.id.txt_cancel);
			txt_from_camera.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mCallback.ok(PHOTOHRAPH);
					dismiss();
				}
			});
			txt_from_photo.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mCallback.ok(PHOTOZOOM);
					dismiss();
				}
			});
			txt_cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
			break;
		// 编辑心情
		case EDITMOOD_DIALOG:
			setContentView(R.layout.dialog_editmood);
			getWindow().setLayout(9 * MainActivity.mScreenWidth / 10, LayoutParams.WRAP_CONTENT);
			// getWindow().setLayout(4 * mWidth / 5, mHeight / 3);
			et_mood = (EditText) findViewById(R.id.et_dia_content);
			MTextView tv_moodtips = (MTextView) findViewById(R.id.tv_feeling_tips);
			mTV_moodoffset = (TextView) findViewById(R.id.tv_offset);
			tv_moodtips.setMText(mContext.getString(R.string.edit_feeling_tip));
			tv_moodtips.setTextColor(Color.parseColor("#7a7a7a"));
			 btneditmoodOk = (Button) CustomDialog.this.findViewById(R.id.btn_ok);
			 btneditmoodCancel = (Button) CustomDialog.this.findViewById(R.id.btn_cancel);
			// 设置编辑框第一次显示上一次的心情
			String oldmood = (String) mInfo;
			if (null != oldmood) {
				et_mood.setText(oldmood);
				if (oldmood.length() > 0) {
					et_mood.setSelection(0, oldmood.length() - 1);
				}
			} else {
				et_mood.setText("");
			}
			if (et_mood.getText().toString().length() == 0) {
				btneditmoodOk.setClickable(false);
				btneditmoodOk.setAlpha(0.4f);
			}else{
				btneditmoodOk.setClickable(true);
				btneditmoodOk.setAlpha(1.0f);
			}
			// 设置编辑框有焦点时弹出软键盘并选中所有文字
			et_mood.setSelectAllOnFocus(true);
			et_mood.addTextChangedListener(mTextWatch_mood);
			
			et_mood.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					et_mood.selectAll();
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			});

			btneditmoodOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (et_mood.getText().toString().length() <= 0) {
//						new CustomDialog(mContext, null, R.style.custom_dialog, CustomDialog.TOAST_DIALOG, mContext.getString(R.string.feel_null)).show();
						// ToastUtils.showToast(mContext,
						// mContext.getString(R.string.feel_null), 1000);
					} else {
						mCallback.ok(mType, et_mood.getText().toString());
						dismiss();
					}
				}
			});
			btneditmoodCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
						dismiss();
				}
			});
			break;
		// 服务端最新apk版本信息
		case APK_UPDATE_DIALOG:
			setContentView(R.layout.dialog_updatecup);
			// getWindow().setLayout(4 * mWidth / 5, mHeight / 5);
			getWindow().setLayout(4 * MainActivity.mScreenWidth / 5, LayoutParams.WRAP_CONTENT);
			TextView txt_version_content = (TextView) findViewById(R.id.txt_dia_content);
			String updateInfo = (String) mInfo;
			txt_version_content.setText(updateInfo);
			Button btn_updateOk = (Button) findViewById(R.id.btn_update);
			Button btn_remind = (Button) findViewById(R.id.btn_remind);
			Button btn_neverRemind = (Button) findViewById(R.id.btn_neverremind);
			btn_updateOk.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {// 升级
					mCallback.ok(mType);
					dismiss();
				}
			});
			//稍后提示
			btn_remind.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Tools.savePreference(mContext,UtilContact.ISREQUESTUPDATEINFO,"true");
					dismiss();
				}
			});
			//从不提示
			btn_neverRemind.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
					Tools.savePreference(mContext,UtilContact.ISREQUESTUPDATEINFO,"false");
				}
			});
			break;
		//饮水提醒
			case DIALOG_REMIND_DRINK:
				setContentView(R.layout.dialog_remind_drink);
				getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				ImageView imageView = (ImageView) findViewById(R.id.remind_iv_cancel);
				imageView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
				TextView tv_name = (TextView) findViewById(R.id.remind_tv_name);
				String name = (String) mInfo;
				tv_name.setText("亲爱的"+name+",");
				break;
		case TOAST_DIALOG:
			setContentView(R.layout.dialog_toast);
			getWindow().setLayout(4 * MainActivity.mScreenWidth / 5, LayoutParams.WRAP_CONTENT);
			MTextView txt_toast_content = (MTextView) findViewById(R.id.txt_toast_content);
			txt_toast_content.setGravity(Gravity.CENTER);
			txt_toast_content.setMText((CharSequence) mInfo);

			if (mThread_timer != null) {
				mThread_timer.interrupt();
				mThread_timer = null;
			}
			mThread_timer = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(3000);
						mHandler.sendEmptyMessage(dismissToastDialog);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			mThread_timer.start();
			dismiss();
			break;
		case ADDTIMER_DIALOG:
		case EDITTIMER_DIALOG:

			setContentView(R.layout.dialog_settime);
			// getWindow().setLayout(4 * mWidth / 5, mHeight / 5);
			getWindow().setLayout(4 * MainActivity.mScreenWidth / 5, LayoutParams.WRAP_CONTENT);
			ImageView iv_addHour = (ImageView) findViewById(R.id.iv_addhour);
			ImageView iv_delHour = (ImageView) findViewById(R.id.iv_delhour);
			ImageView iv_addMin = (ImageView) findViewById(R.id.iv_addmin);
			ImageView iv_delMinr = (ImageView) findViewById(R.id.iv_delmin);
			tv_hour = (TextView) findViewById(R.id.tv_hour);
			tv_min = (TextView) findViewById(R.id.tv_min);
			if (mInfo == null) {
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(System.currentTimeMillis());
				int mHour = c.get(Calendar.HOUR_OF_DAY);
				int mMinute = c.get(Calendar.MINUTE);
				tv_hour.setText(String.format("%02d", mHour));
				tv_min.setText(String.format("%02d", mMinute));
			} else {
				String[] time = ((String) mInfo).split(":");
				int mHour = Integer.parseInt(time[0].trim());
				int mMinute = Integer.parseInt(time[1].trim());
				tv_hour.setText(String.format("%02d", mHour));
				tv_min.setText(String.format("%02d", mMinute));
			}

			iv_addHour.setOnClickListener(mTimeClick);
			iv_delHour.setOnClickListener(mTimeClick);
			iv_addMin.setOnClickListener(mTimeClick);
			iv_delMinr.setOnClickListener(mTimeClick);

			Button btnAddOk = (Button) findViewById(R.id.btn_ok);
			Button btnAddCancel = (Button) findViewById(R.id.btn_cancel);
			btnAddOk.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mCallback.ok(mType, tv_hour.getText() + ":" + tv_min.getText());
					dismiss();
				}
			});
			btnAddCancel.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
			break;
		case DIALOG_GETCONTACTS:
			setContentView(R.layout.contacts_dialog);
			getWindow().setLayout(4 * MainActivity.mScreenWidth / 5, LayoutParams.WRAP_CONTENT);
			TextView tv_ok = (TextView) findViewById(R.id.tv_ok);
			TextView tv_no = (TextView) findViewById(R.id.tv_no);
			tv_ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mCallback.ok(mType);
					dismiss();
				}
			});

			tv_no.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
			break;
			case DIALOG_SHARE_QRCODE:
				setContentView(R.layout.share_qrcode_dialog);
				getWindow().setLayout(4 * MainActivity.mScreenWidth / 5, LayoutParams.WRAP_CONTENT);
				TextView tv_weixin = (TextView) findViewById(R.id.tv_weixin);
				TextView tv_pyq = (TextView) findViewById(R.id.tv_pyq);
				final ImageView iv_qr = (ImageView) findViewById(R.id.share_qr);
				Bitmap bitmap = CaptureActivity.createQRImage(RequestHost.appDownUrl+Tools.getPreference(mContext, UtilContact.HuanXinId));
				iv_qr.setImageBitmap(bitmap);
				Tools.saveBitmap2SDcard(mContext.getString(R.string.app_name) + "_qr" + ".jpg",bitmap);
				tv_weixin.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
//						mCallback.ok(mType);
//						dismiss();
						Logger.e("微信朋友圈分享");
						Wechat.ShareParams sp = new Wechat.ShareParams();
						sp.setShareType(Platform.SHARE_IMAGE);
						sp.setImageUrl("www.yili.com");
						String imgPath = Tools.getSDPath() + "/"
								+ mContext.getString(R.string.app_name) + "_qr" + ".jpg";
						sp.setImagePath(imgPath);
						Platform platform = ShareSDK.getPlatform(Wechat.NAME);
						platform.share(sp);
					}
				});

				tv_pyq.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
//						mCallback.cancel(mType);
//						dismiss();
						Logger.e("微信朋友圈分享");
						WechatMoments.ShareParams sp = new WechatMoments.ShareParams();
						sp.setShareType(Platform.SHARE_IMAGE);
						sp.setImageUrl("www.yili.com");
						String imgPath = Tools.getSDPath() + "/"
								+ mContext.getString(R.string.app_name) + "_qr" + ".jpg";
						sp.setImagePath(imgPath);
						sp.setImagePath(imgPath);
						Platform platform = ShareSDK.getPlatform(WechatMoments.NAME);
						platform.share(sp);
//        sp.setImagePath(“/mnt/sdcard/测试分享的图片.jpg”);

//        Platform weibo = ShareSDK.getPlatform(Wechat.NAME);
//        weibo.setPlatformActionListener(paListener); // 设置分享事件回调
// 执行图文分享
						Platform qq = ShareSDK.getPlatform(WechatMoments.NAME);
						qq.share(sp);
					}
				});
				break;
			case DIALOG_DELETE_FRIEND:
				setContentView(R.layout.delete_friend_dialog);
				getWindow().setLayout(4 * MainActivity.mScreenWidth / 5, LayoutParams.WRAP_CONTENT);
				TextView tv_delete_ok = (TextView) findViewById(R.id.tv_ok);
				TextView tv_delete_no = (TextView) findViewById(R.id.tv_no);
				tv_delete_ok.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mCallback.ok(mType);
						dismiss();
					}
				});

				tv_delete_no.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
				break;
			case DIALOG_DELETE_MSG:
				setContentView(R.layout.delete_friend_dialog);
				getWindow().setLayout(4 * MainActivity.mScreenWidth / 5, LayoutParams.WRAP_CONTENT);
				TextView tv_delete_ok_1 = (TextView) findViewById(R.id.tv_ok);
				TextView tv_delete_no_1 = (TextView) findViewById(R.id.tv_no);
				TextView tv_content = (TextView) findViewById(R.id.dialog_delete);
				tv_content.setText(R.string.dialog_delete_msg);
				tv_delete_ok_1.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mCallback.ok(mType);
						ChatMsgEntity entity = (ChatMsgEntity) mInfo;
						Logger.e("date","delete chat where date is"+entity.getDate());
						long date = entity.getDate();
						DBManager dbManager = new DBManager(mContext);
						dbManager.deleteChat(date);
						dismiss();
					}
				});

				tv_delete_no_1.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
				break;
		default:
			break;
		}
	}

	private android.view.View.OnClickListener mTimeClick = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_addhour:
				int hour1 = (Integer.parseInt(tv_hour.getText().toString())) + 1;
				if (hour1 > 23) {
					hour1 = 0;
				}
				tv_hour.setText(String.format("%02d", hour1));
				break;
			case R.id.iv_delhour:
				int hour2 = (Integer.parseInt(tv_hour.getText().toString())) - 1;
				if (hour2 < 0) {
					hour2 = 23;
				}
				tv_hour.setText(String.format("%02d", hour2));
				break;
			case R.id.iv_addmin:
				int min1 = (Integer.parseInt(tv_min.getText().toString())) + 1;
				if (min1 > 59) {
					min1 = 0;
				}
				tv_min.setText(String.format("%02d", min1));
				break;
			case R.id.iv_delmin:
				int min2 = (Integer.parseInt(tv_min.getText().toString())) - 1;
				if (min2 < 0) {
					min2 = 59;
				}
				tv_min.setText(String.format("%02d", min2));
				break;
			default:
				break;
			}
		}
	};
	
private TextWatcher mTextWatch_mood = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			Log.d(TAG, "onTextChanged-------et_mood.getText().toString().length()=="+et_mood.getText().toString().length());
			if (et_mood.getText().toString().length() == 0 || et_mood.getText().toString().length()>100) {
				btneditmoodOk.setClickable(false);
				btneditmoodOk.setAlpha(0.4f);
			}else{
				btneditmoodOk.setClickable(true);
				btneditmoodOk.setAlpha(1.0f);
			}
			if (et_mood.getText().toString().length()>=90 && et_mood.getText().toString().length()<=100) {
				mTV_moodoffset.setVisibility(View.VISIBLE);
				mTV_moodoffset.setText(""+(100-et_mood.getText().toString().length()));
				mTV_moodoffset.setTextColor(Color.parseColor("#7a7a7a"));
			}else if(et_mood.getText().toString().length()>100){
				mTV_moodoffset.setVisibility(View.VISIBLE);
				mTV_moodoffset.setText(""+(100-et_mood.getText().toString().length()));
				mTV_moodoffset.setTextColor(Color.RED);
			}else{
				mTV_moodoffset.setVisibility(View.GONE);
			}

		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	public static final int NONE = 0;
	public static final int PHOTOHRAPH = 1;// 拍照
	public static final int PHOTOZOOM = 2; // 缩放
	public static final int PHOTORESOULT = 3;// 缩放照片结果

	public static final int SEND_DIALOG = 1;
	public static final int SCANBLUE_DIALOG = 2;
	public static final int DEL_FRIEND_DIALOG = 3;
	public static final int SET_HEADIMAGE_DIALOG = 4;
	public static final int EXIT_DIALOG = 5;
	public static final int RECOVERY_DIALOG = 6;
	public static final int SLEEP_DIALOG = 7;
	public static final int EDITMOOD_DIALOG = 8;
	public static final int VERSION_DIALOG = 9;
	public static final int DEL_ALARM_DIALOG = 10;
	public static final int APK_UPDATE_DIALOG = 11;
	public static final int EXIT_FIRMWARE_DIALOG = 12;
	public static final int TIPS_FIRMWARE_DIALOG = 13;
	public static final int PAIR_DIALOG = 14;
	public static final int TOAST_DIALOG = 15;
	public static final int ADDTIMER_DIALOG = 16;
	public static final int EDITTIMER_DIALOG = 17;
	public static final int TENCENT_DIALOG = 18;
	public static final int TENCENT_DIALOG_LOGIN = 19;
	public static final int ADDED_DIALOG = 20;
	public static final int DEMATED_DIALOG = 21;
	public static final int TENCENT_DIALOG_ANOTHER_LOGIN = 22;
	public static final int DIALOG_GETCONTACTS = 23;
	//分享二维码
	public static final int DIALOG_SHARE_QRCODE = 24;
	public static final int DIALOG_DELETE_FRIEND = 25;
	//删除聊天记录
	public static final int DIALOG_DELETE_MSG = 27;
	//提示扫描正确的二维码
	public static final int DIALOG_SCAN_CORRECTQR = 26;
	//提示饮水
	public static final int DIALOG_REMIND_DRINK = 30;


	public final static int dismissToastDialog = 1;

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case dismissToastDialog:
			if (this.isShowing()) {
				this.dismiss();
			}
			break;
		default:
			break;
		}
		return false;
	}

}
