package com.sen5.ocup.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.Circle;
import com.sen5.ocup.R;
import com.sen5.ocup.alarm.AlarmClockView;
import com.sen5.ocup.alarm.Time_show;
import com.sen5.ocup.blutoothstruct.CupPara;
import com.sen5.ocup.callback.BluetoothCallback.IGetCupParaCallback;
import com.sen5.ocup.callback.BluetoothCallback.IGetRemindDataCallback;
import com.sen5.ocup.callback.BluetoothCallback.ISetCupParaCallback;
import com.sen5.ocup.callback.BluetoothCallback.ISetRemindDataCallback;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.gui.SwitchView;
import com.sen5.ocup.gui.SwitchView.OnChangedListener;
import com.sen5.ocup.receiver.HomeWatcher;
import com.sen5.ocup.service.BackGroundControlService;
import com.sen5.ocup.util.BlueToothRequest;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.Tools;

import java.util.List;

/**
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 设置饮水计划界面（添加饮水闹钟）
 * 添加一个闹钟后不会立即发送指令给杯子
 * 在用户点击back键时才会进行发送
 */
public class WaterProjectActivity extends BaseActivity implements OnClickListener, ISetCupParaCallback,ISetRemindDataCallback, Callback, IGetCupParaCallback, IGetRemindDataCallback {

	private static final String TAG = "WaterProjectActivity";
	private HomeWatcher mHomeKeyReceiver = null;
	private DBManager mDBManager;
	private Handler mHandler;
	private LinearLayout layout_back = null;
	private LinearLayout mLayout_alarm;
	private RelativeLayout mRelativeLayout;
	private ImageView imgAddAlarm;
	private SwitchView switchview;
	private SwitchView remind_switch;
	private AlarmClockView mAlarmClockView;
	private boolean isOpen = false;
	public static boolean isGetCupRemind = false;// 是否拿到杯子的提醒闹钟

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_waterproject);
		initView();
		setCupPara2();
		showDialog();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == AlarmClockView.REQUEST_CODE) {
			if(resultCode == DatePickerDlg.RESULT_CODE) {
				 int hour = data.getIntExtra("hour", 0);
				 int minute = data.getIntExtra("minute", 0);
				 int type = data.getIntExtra("dialogType", DatePickerDlg.timedialog_add);
				String time = "";
				 if (type == DatePickerDlg.timedialog_add) {
					  time = Tools.splitNub(mAlarmClockView.dealAddAlarm(hour, minute).getTime());
					 BackGroundControlService.times.add(time);
				}else{
					time = Tools.splitNub(mAlarmClockView.dealEditAlarm(hour, minute));
					 BackGroundControlService.times.remove(time);
				}
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		mHomeKeyReceiver.startWatch();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mHomeKeyReceiver.stopWatch();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isGetCupRemind = false;
	}

	public void initView() {
		FrameLayout titleLayout = (FrameLayout) findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(titleLayout,this);
		mHomeKeyReceiver = new HomeWatcher(this);
		mDBManager = new DBManager(this);

		mHandler = new Handler(this);
		switchview = (SwitchView) findViewById(R.id.switch_autostudy);
		remind_switch = (SwitchView) findViewById(R.id.switch_remind);
		remind_switch.set2bluetooth(true);
		switchview.set2bluetooth(false);
		mRelativeLayout = (RelativeLayout) findViewById(R.id.relativelayout_remind2drink);
		mLayout_alarm = (LinearLayout) findViewById(R.id.box);
		mAlarmClockView = new AlarmClockView(this, this);
		layout_back = (LinearLayout) findViewById(R.id.layout_back);
		layout_back.setOnClickListener(this);
		imgAddAlarm = (ImageView) findViewById(R.id.img_add_alarm);
		imgAddAlarm.setOnClickListener(this);
		switchview.SetOnChangedListener(new OnChangedListener() {
			@Override
			public void OnChanged(View view, boolean checkState) {
				if (checkState && CupPara.getInstance().getLearn_sw() == 0) {
					CupPara.getInstance().setLearn_sw(1);
					mLayout_alarm.setVisibility(View.GONE);
					imgAddAlarm.setClickable(false);
					imgAddAlarm.setAlpha(0.4f);
					imgAddAlarm.setVisibility(View.GONE);
					setCupPara();
				} else if (!checkState && CupPara.getInstance().getLearn_sw() == 1) {
					CupPara.getInstance().setLearn_sw(0);
					mLayout_alarm.setVisibility(View.VISIBLE);
					imgAddAlarm.setVisibility(View.VISIBLE);
					imgAddAlarm.setClickable(true);
					imgAddAlarm.setAlpha(1.0f);
					setCupPara();
				}
			}
		});


		remind_switch.SetOnChangedListener(new OnChangedListener() {
			@Override
			public void OnChanged(View view, boolean checkState) {
				if (checkState && CupPara.getInstance().getRemind_sw() == 0) {
					CupPara.getInstance().setRemind_sw(1);
					BlueToothRequest.getInstance().sendMsg2setCupPara(WaterProjectActivity.this,BlueToothRequest.type_waterProject_1);
					mRelativeLayout.setVisibility(View.VISIBLE);
					mLayout_alarm.setVisibility(View.VISIBLE);
					imgAddAlarm.setVisibility(View.VISIBLE);
				} else if (!checkState && CupPara.getInstance().getRemind_sw() == 1) {
					CupPara.getInstance().setRemind_sw(0);
					BlueToothRequest.getInstance().sendMsg2setCupPara(WaterProjectActivity.this,BlueToothRequest.type_waterProject_0);
					mRelativeLayout.setVisibility(View.GONE);
					mLayout_alarm.setVisibility(View.GONE);
					imgAddAlarm.setVisibility(View.GONE);
				}
			}
		});

		if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
			BlueToothRequest.getInstance().sendMsg2getCupInfo(WaterProjectActivity.this);
		} else {
			OcupToast.makeText(WaterProjectActivity.this, getString(R.string.unconnect_2cup), Toast.LENGTH_LONG).show();
			this.finish();
		}
	}

	/**
	 * 设置杯子参数
	 */
	private void setCupPara() {
		if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
			BlueToothRequest.getInstance().sendMsg2setCupPara(WaterProjectActivity.this,BlueToothRequest.type_waterProject_1);
		} else {
			OcupToast.makeText(WaterProjectActivity.this, getString(R.string.unconnect_2cup), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.layout_back:
			if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
				List<Time_show> remindTime = mAlarmClockView.getRemindTime();
				BlueToothRequest.getInstance().sendMsg2SetRemind(remindTime, WaterProjectActivity.this);
			} else {
				OcupToast.makeText(WaterProjectActivity.this, getString(R.string.set_failed), Toast.LENGTH_LONG).show();
			}
			finish();
			break;

		case R.id.img_add_alarm:
			// mInterface.addAlarm();
			mAlarmClockView.add_alarm();
			break;

		default:
			break;

		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				layout_back.performClick();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void setCupPara_OK(int type) {
		Log.d(TAG, "setCupPara_OK------------");
		mHandler.sendEmptyMessage(setCupPara_OK);
	}

	@Override
	public void setCupPara_NO(int type) {
		Log.d(TAG, "setCupPara_NO------------");
		mHandler.sendEmptyMessage(setCupPara_NO);
	}

	private final static int setCupPara_OK = 1;
	private final static int setRemind_OK = 2;
	private final static int setCupPara_NO = 3;
	private final static int setRemind_NO = 4;
	private final static int getCupPara_NO = 5;
	private final static int getCupPara_OK = 6;
	private final static int getCupRemind_NO = 7;
	private final static int getCupRemind_OK = 8;

	@Override
	public boolean handleMessage(Message msg) {
		if (WaterProjectActivity.this.getWindow().isActive()) {
			if (msg.what == setCupPara_OK) {

			} else if (msg.what == setRemind_OK) {
				mDBManager.deleteAllAlarm_time();
				List<Time_show> remindTime = mAlarmClockView.getRemindTime();
				for (int i = 0; i < remindTime.size(); i++) {
					mDBManager.add_alarmtime(remindTime.get(i));
				}
				Intent resultIntent = new Intent();
				WaterProjectActivity.this.setResult(RESULT_OK, resultIntent);
				finish();
			} else if (msg.what == setCupPara_NO || msg.what == setRemind_NO || msg.what == getCupPara_NO) {// 设置失败
				OcupToast.makeText(WaterProjectActivity.this, getString(R.string.set_failed), Toast.LENGTH_LONG).show();
				Intent resultIntent = new Intent();
				WaterProjectActivity.this.setResult(RESULT_OK, resultIntent);
				finish();
			} else if (msg.what == getCupPara_OK) {
				if (CupPara.getInstance().getLearn_sw() == 0) {
					switchview.setChecked(switchview, false);
					if (isOpen){
						mRelativeLayout.setVisibility(View.VISIBLE);
						mLayout_alarm.setVisibility(View.VISIBLE);
					}
					imgAddAlarm.setClickable(true);
					imgAddAlarm.setAlpha(1.0f);
				} else {
					switchview.setChecked(switchview, true);
					mLayout_alarm.setVisibility(View.GONE);
					imgAddAlarm.setClickable(false);
					imgAddAlarm.setAlpha(0.4f);
				}
				if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
					BlueToothRequest.getInstance().sendMsg2GetCupRemind(this);
				} else {
					OcupToast.makeText(WaterProjectActivity.this, getString(R.string.unconnect_2cup), Toast.LENGTH_LONG).show();
					finish();
				}
			} else if (msg.what == getCupRemind_OK) {
				isGetCupRemind = true;
				mDialog.dismiss();
				mAlarmClockView.setGetCupRemind(true);
			} else if (msg.what == getCupRemind_NO) {
				OcupToast.makeText(WaterProjectActivity.this, getString(R.string.get_cupremind_failed), Toast.LENGTH_LONG).show();
				Intent resultIntent = new Intent();
				WaterProjectActivity.this.setResult(RESULT_OK, resultIntent);
				finish();
			}
		}
		return false;
	}

	@Override
	public void setRemindData_OK() {
		Log.d(TAG, "setRemindData_OK------------");
		mHandler.sendEmptyMessage(setRemind_OK);
	}

	@Override
	public void setRemindData_NO() {
		mHandler.sendEmptyMessage(setRemind_NO);
	}

	@Override
	public void getCupPara_OK() {
		mHandler.sendEmptyMessage(getCupPara_OK);
	}

	@Override
	public void getCupPara_NO() {
		mHandler.sendEmptyMessage(getCupPara_NO);

	}

	@Override
	public void getRemindData_OK() {
		mHandler.sendEmptyMessage(getCupRemind_OK);
	}

	@Override
	public void getRemindData_NO() {
		mHandler.sendEmptyMessage(getCupRemind_NO);
	}

	private Dialog mDialog = null;
	private Circle mCircleDrawable = null;

	private void showDialog() {
		mDialog = new Dialog(WaterProjectActivity.this,R.style.custom_dialog_loading);
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
	 * 根据杯子参数设置界面
	 * 总开关
	 * 还有一个自动提醒开关是用来控制水杯是否能进行自动提醒
	 */
	private void setCupPara2() {
		// 设置饮水提醒开关显示
		if (remind_switch != null) {
			if (CupPara.getInstance().getRemind_sw() == 0) {
				remind_switch.setChecked(remind_switch, false);
				mRelativeLayout.setVisibility(View.GONE);
				mLayout_alarm.setVisibility(View.GONE);
				imgAddAlarm.setVisibility(View.GONE);
			} else {
				isOpen = true;
				remind_switch.setChecked(remind_switch, true);
			}
		}
	}
}
