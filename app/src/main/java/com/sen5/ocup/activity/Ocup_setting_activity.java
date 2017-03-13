package com.sen5.ocup.activity;

import android.app.Activity;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.blutoothstruct.BluetoothType;
import com.sen5.ocup.blutoothstruct.CupPara;
import com.sen5.ocup.blutoothstruct.CupStatus;
import com.sen5.ocup.blutoothstruct.DrinkData;
import com.sen5.ocup.callback.BluetoothCallback.IControlCupCallback;
import com.sen5.ocup.callback.BluetoothCallback.IGetCupParaCallback;
import com.sen5.ocup.callback.BluetoothCallback.ISetCupParaCallback;
import com.sen5.ocup.callback.CustomInterface.IDialog;
import com.sen5.ocup.callback.RequestCallback.IRecoveryFactoryCallback;
import com.sen5.ocup.callback.RequestCallback.UpdateUserInfoCallback;
import com.sen5.ocup.fragment.OchatFragment;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.receiver.Bluetooth3Receiver;
import com.sen5.ocup.receiver.HomeWatcher;
import com.sen5.ocup.service.BackGroundControlService;
import com.sen5.ocup.struct.CupInfo;
import com.sen5.ocup.struct.Tips;
import com.sen5.ocup.util.BlueToothRequest;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.DataCleanManager;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.InternetInfoUtil;
import com.sen5.ocup.util.TeaListUtil;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;
import com.sen5.ocup.yili.LoginActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 : 水杯设置界面
 */
public class Ocup_setting_activity extends BaseActivity implements OnClickListener, Callback, IDialog, IGetCupParaCallback, ISetCupParaCallback, IControlCupCallback,
		UpdateUserInfoCallback {
	private static String TAG = "Ocup_setting_activity";
	private HomeWatcher mHomeKeyReceiver = null;
	private CustomDialog mDailog_recovery;
	private CustomDialog mDailog_sleep;
	private CustomDialog mDailog_exit;
	private Handler mHandler;
	private BlueToothRequest mBluetoothRequest;
	private RelativeLayout rl_recove, rl_firmware, rl_shutdown, rl_exit,rl_relogin;
	private LinearLayout iv_back;
	private Context mcontext = Ocup_setting_activity.this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OcupApplication.getInstance().addActivity(this);
		setContentView(R.layout.activity_ocup_setting);
		intview();
	}
	/**
	 * 初始化控件
	 */
	public void intview() {

		FrameLayout titleLayout = (FrameLayout) findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(titleLayout,this);
		mBluetoothRequest = BlueToothRequest.getInstance();
		mHandler = new Handler(this);
		rl_recove = (RelativeLayout) findViewById(R.id.relativelayout_recover);
		rl_recove.setOnClickListener(this);

		rl_firmware = (RelativeLayout) findViewById(R.id.relativelayout_firmware);
		rl_firmware.setOnClickListener(this);
		rl_shutdown = (RelativeLayout) findViewById(R.id.relativelayout_shutdown);
		rl_shutdown.setOnClickListener(this);

		iv_back = (LinearLayout) findViewById(R.id.layout_back);
		iv_back.setOnClickListener(this);
		rl_exit = (RelativeLayout) findViewById(R.id.relativelayout_exit);
		rl_exit.setOnClickListener(this);

		rl_relogin = (RelativeLayout) findViewById(R.id.rl_reLogin);
		rl_relogin.setOnClickListener(this);

		mDailog_sleep = new CustomDialog(mcontext, this, R.style.custom_dialog, CustomDialog.SLEEP_DIALOG, null);
		mDailog_recovery = new CustomDialog(mcontext, this, R.style.custom_dialog, CustomDialog.RECOVERY_DIALOG, null);
		mDailog_exit = new CustomDialog(mcontext, this, R.style.custom_dialog, CustomDialog.EXIT_DIALOG, null);

		mHomeKeyReceiver = new HomeWatcher(this);
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 升级固件
		case R.id.relativelayout_firmware:
			Log.e(TAG, "--------------------startActivity UpdateFirmWare::" + CupPara.getInstance().isGotCupPara());
			if (CupPara.getInstance().isGotCupPara()) {
				Intent intent_firmware = new Intent(mcontext, UpdateFirmWare.class);
				startActivity(intent_firmware);
			} else {
				if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
					if (BlueToothRequest.getInstance().getRequesting()) {
						OcupToast.makeText(this, getString(R.string.requesting), Toast.LENGTH_LONG).show();
						return;
					}
					mBluetoothRequest.sendMsg2getCupInfo(Ocup_setting_activity.this);
				} else {
					OcupToast.makeText(Ocup_setting_activity.this, getString(R.string.unconnect_2cup), Toast.LENGTH_LONG).show();
				}
			}
			break;
		// 恢复出厂设置
		case R.id.relativelayout_recover:
			if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
				mDailog_recovery.show();
			} else {
				OcupToast.makeText(Ocup_setting_activity.this, getString(R.string.unconnect_2cup), Toast.LENGTH_LONG).show();
			}
			break;
		// 更换水杯
		case R.id.relativelayout_exit:
			mDailog_exit.show();
			break;
		// 关机
		case R.id.relativelayout_shutdown:
			if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
				mDailog_sleep.show();
			} else {
				OcupToast.makeText(Ocup_setting_activity.this, getString(R.string.unconnect_2cup), Toast.LENGTH_LONG).show();
			}
			break;
		// 返回
		case R.id.layout_back:
			Intent resultIntent = new Intent();
			Ocup_setting_activity.this.setResult(RESULT_OK, resultIntent);
			Ocup_setting_activity.this.finish();
			break;
		case R.id.rl_reLogin:
			//删除数据库
			DBManager dm = new DBManager(Ocup_setting_activity.this);
			dm.deleteFriends();
			dm.deleteYiLiCup();
			//把当前activity外的所有activity清掉
			List<Activity> list = OcupApplication.getInstance().mList;
			for (int i= 0; i<list.size()-1;i++){
				list.get(i).finish();
			}
			OcupApplication.getInstance().mList.clear();
			Tools.savePreference(Ocup_setting_activity.this,UtilContact.OwnAvatar,null);
			Tools.savePreference(Ocup_setting_activity.this,UtilContact.Phone_Num,"");
			Tools.savePreference(Ocup_setting_activity.this,UtilContact.CUP_ID,"");
			Logger.e(TAG,"注销："+"数据已经被清空");
				Intent intent = new Intent(Ocup_setting_activity.this,LoginActivity.class);
				intent.putExtra("logout",true);
				startActivity(intent);
			BackGroundControlService.shouldRemind = false;//注销后不提醒饮水
			finish();
				break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			Intent resultIntent = new Intent();
			Ocup_setting_activity.this.setResult(RESULT_OK, resultIntent);
			Ocup_setting_activity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void ok(int type) {
		if (type == CustomDialog.RECOVERY_DIALOG) {
			mHandler.sendEmptyMessage(RECOVERY_OK);
		} else if (type == CustomDialog.EXIT_DIALOG) {
			mHandler.sendEmptyMessage(EXIT_OK);
		} else if (type == CustomDialog.SLEEP_DIALOG) {
			mHandler.sendEmptyMessage(SLEEP_OK);
		}
	}

	@Override
	public void ok(int type, Object obj) {
	}

	@Override
	public void cancel(int type) {
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		// 更换杯子
		case EXIT_OK:
			Tools.savePreference(getApplicationContext(), UtilContact.CUP_ID, "");
			// 清空当前杯子信息
			OcupApplication.getInstance().mOwnCup = new CupInfo();
			OcupApplication.getInstance().mOtherCup = new CupInfo();
//			if(OchatFragment.mDataFriend.size()>1){
//				OchatFragment.mDataFriend.remove(1);
//				OchatFragment.mDataFriend.remove(0);
//			}else if(OchatFragment.mDataFriend.size()>0){
//				OchatFragment.mDataFriend.remove(0);
//			}
			// 清除sen5服务器cookie
			HttpRequest.getInstance().mCookies.clear();

			// 关闭蓝牙通信
			Log.d(TAG, "handmsg---- 更换杯子----------closeBluetoothCommunication+ " + "BluetoothConnectUtils.getInstance().getBluetoothState() =="
					+ BluetoothConnectUtils.getInstance().getBluetoothState());
			BluetoothConnectUtils.getInstance().closeBluetoothCommunication();

			// 关闭环信
//			if (null != EMChatManager.getInstance() && EMChatManager.getInstance().isConnected()) {//
//				Log.d(TAG, "handmsg-------null != EMChatManager.getInstance()isConnected");
//				try {
//					EMChatManager.getInstance().logout();
//				} catch (Exception e) {
//					Log.d(TAG, "handmsg--EMChatManager.getInstance().logout();  e==" + e);
//				}
//			}

			BluetoothConnectUtils.getInstance().isRunFront = false;
			OcupApplication.getInstance().isFirstReadCupInfo = true;

			Intent intent = new Intent(this, BlueTooth3Activity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
//			OcupApplication.getInstance().exit();
//			System.exit(0);
			break;

		// 确认恢复出厂设置
		case RECOVERY_OK:
			if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {

				mBluetoothRequest.sendMsg2ControlCup(Ocup_setting_activity.this, BluetoothType.control_correcttouch);

				if(mDailog_recovery.getCheckBoxRecoveryCloudData()){
					HttpRequest.getInstance().demateCups(Ocup_setting_activity.this, null);
					HttpRequest.getInstance().recoveryFactoryCloud(Ocup_setting_activity.this, new IRecoveryFactoryCallback() {

						@Override
						public void recoveryFactory_Success() {
							// TODO Auto-generated method stub
							DataCleanManager.cleanApplicationDataNoFile(Ocup_setting_activity.this);
							OcupApplication.getInstance().mOwnCup.setName(UtilContact.DEFAULT_OCUP_NAME, 11);
							new DBManager(mcontext).clearAllDrinkData();
							Tools.clearKeyPreference(Ocup_setting_activity.this, "1");
							Tools.clearKeyPreference(Ocup_setting_activity.this, "2");
							Tools.clearKeyPreference(Ocup_setting_activity.this, "3");
							Tools.clearKeyPreference(Ocup_setting_activity.this, "4");
							Tools.clearKeyPreference(Ocup_setting_activity.this, "5");
							TeaListUtil.getInstance().teaList.clear();
							TeaListUtil.setTeaListUtilNull();
							deleteAllTipsMark();
//							OchatFragment.mDataFriend.clear();

							new DBManager(mcontext).deleteCup_mate(Tools.getPreference(mcontext,UtilContact.HuanXinId));
							if(OchatFragment.mDataFriend.size()>1){
								OchatFragment.mDataFriend.remove(1);
							}
							CupStatus.setCupStatusNull();
							mHandler.sendEmptyMessageDelayed(CONTROL_RECOVERY_OK, 1111);
							// 清空当前杯子信息
//							OcupApplication.getInstance().mOwnCup = new CupInfo();
							OcupApplication.getInstance().mOtherCup = new CupInfo();
						}

						@Override
						public void recoveryFactory_Failed(int status) {
							// TODO Auto-generated method stub
							Message obtainMessage = mHandler.obtainMessage();
							if(!InternetInfoUtil.checkInternet(Ocup_setting_activity.this)){
								obtainMessage.obj = HttpRequest.NET_NOTCONNECT;
							}else{
								obtainMessage.obj = status;
							}
							obtainMessage.what = NETWORK_NO_CONNECTED;
							mHandler.sendMessage(obtainMessage);
						}
					});

				}
			} else {
				OcupToast.makeText(Ocup_setting_activity.this, getString(R.string.unconnect_2cup), Toast.LENGTH_LONG).show();
			}
			mHandler.sendEmptyMessageDelayed(DISMISS_RECOVERY_DIALOG,2000);
			
			break;
		// 回复出厂设置不管成功与否，重新获取杯子参数
		case SETCUPPARA_OK:
		case SETCUPPARA_NO:
			break;
		// touch校准ok
		case CONTROL_CORRECTTOUCH_OK:
			if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
				mBluetoothRequest.sendMsg2ControlCup(Ocup_setting_activity.this, BluetoothType.control_recovery);
			} else {
				OcupToast.makeText(Ocup_setting_activity.this, getString(R.string.recover_failed) + "B", Toast.LENGTH_LONG).show();
			}
			break;
		// 恢复出厂成功
		case CONTROL_RECOVERY_OK:
			BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_NONE);
			CupPara.getInstance().setGotCupPara(false);
			OcupToast.makeText(Ocup_setting_activity.this, getString(R.string.recover_ok), Toast.LENGTH_LONG).show();

			 // 删除对象中保存的饮水数据总量
			 CupStatus.getInstance().setTotal_water_yield(0);
			// // 删除本地数据库中当天饮水记录
			 new DBManager(mcontext).updateDrinkData();
			 // 同步数据到服务器
			 ArrayList<DrinkData> list2srv = new DBManager(mcontext).getDrinkNeedSrv(OcupApplication.getInstance().mOwnCup.getCupID());
			 if (list2srv != null && list2srv.size() > 0) {
				 HttpRequest.getInstance().uploadDrink(list2srv);
			 }
			// 删除服务器端的心情
			HttpRequest.getInstance().updateUserInfo(mcontext, Ocup_setting_activity.this, OcupApplication.getInstance().mOwnCup.getName(), "");
			// 删除数据库的心情
			OcupApplication.getInstance().mOwnCup.setMood("");
			new DBManager(getApplicationContext()).updateOwnCup(OcupApplication.getInstance().mOwnCup);
			break;
		case DISMISS_RECOVERY_DIALOG:
			if(mDailog_recovery != null){
				
				mDailog_recovery.dismiss();
			}
			break;
		case CONTROL_RECOVERY_NO:
		case CONTROL_CORRECTTOUCH_NO:
			OcupToast.makeText(Ocup_setting_activity.this, getString(R.string.recover_failed)+ "A", Toast.LENGTH_LONG).show();
			break;
		// 确认关机
		case SLEEP_OK:
			if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
				mBluetoothRequest.sendMsg2ControlCup(null, BluetoothType.control_sleep);
				// 关机收到蓝牙断开广播延迟，手动改变状态
				BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_NONE);
				Bluetooth3Receiver.mStatndy = true;
			} else {
				OcupToast.makeText(Ocup_setting_activity.this, getString(R.string.unconnect_2cup), Toast.LENGTH_LONG).show();
			}
			break;
		// 删除服务端心情成功
		case UPDATE_USERINFO_OK:
			//因为是恢复出厂设置， 同步心情只是其中的一个子操作，所以没有必要提示这个信息
//			OcupToast.makeText(Ocup_setting_activity.this, getString(R.string.feel_sycn_ok), Toast.LENGTH_LONG).show();
			break;
		case UPDATE_USERINFO_NO:
			//因为是恢复出厂设置， 同步心情只是其中的一个子操作，所以没有必要提示这个信息
//			OcupToast.makeText(Ocup_setting_activity.this, getString(R.string.feel_sycn_no), Toast.LENGTH_LONG).show();
			break;
		case GETCUPPARA_OK:
			Intent intent_firmware = new Intent(mcontext, UpdateFirmWare.class);
			startActivity(intent_firmware);
			break;
		case NETWORK_NO_CONNECTED:
			Integer errori = (Integer)msg.obj;
			if(HttpRequest.NET_NOTCONNECT == errori){
				OcupToast.makeText(Ocup_setting_activity.this, getString(R.string.check_network), Toast.LENGTH_LONG).show();
			}else{
				OcupToast.makeText(Ocup_setting_activity.this, getString(R.string.error) + errori, Toast.LENGTH_LONG).show();
			}
			break;
		}
		return false;
	}
	/**
	 * 删除全部收藏的tips
	 */
	protected void deleteAllTipsMark() {
		// 从数据库获取数据
		DBManager dbMan = new DBManager(Ocup_setting_activity.this);
		ArrayList<Tips> dbMarkTips = dbMan.queryTipsOrTipsmark(dbMan.tab_tip_mark);
		Log.d(TAG, "getMarkDatas   bdMgr.query()=dbMarkTips.size=" + dbMarkTips.size());
	
		for (int i = 0; i < dbMarkTips.size(); i++) {
			dbMan.deleteTip(dbMan.tab_tip_mark, dbMarkTips.get(i));
		}
	}
	
	private final int UPDATE_USERINFO_OK = 7;
	private final int UPDATE_USERINFO_NO = 8;
	private final int CONTROL_RECOVERY_NO = 9;
	private final int CONTROL_CORRECTTOUCH_NO = 10;
	private final int CONTROL_RECOVERY_OK = 11;
	private final int DISMISS_RECOVERY_DIALOG = 111;
	private final int CONTROL_CORRECTTOUCH_OK = 12;
	private final int EXIT_OK = 14;
	private final int RECOVERY_OK = 15;
	private final int GETCUPPARA_OK = 13;
	private final int SLEEP_OK = 16;
	private final int SETCUPPARA_OK = 17;
	private final int SETCUPPARA_NO = 18;
	private final int NETWORK_NO_CONNECTED = 19;

	@Override
	public void getCupPara_OK() {
		// TODO Auto-generated method stub
		Log.d(TAG, "getCupPara_OK-----------------------------------");
		mHandler.sendEmptyMessage(GETCUPPARA_OK);
	}

	@Override
	public void getCupPara_NO() {
		mHandler.sendEmptyMessage(SETCUPPARA_NO);
	}

	@Override
	public void setCupPara_OK(int type) {
		Log.d(TAG, "setCupPara_OK-----------------------------------");
		mHandler.sendEmptyMessage(SETCUPPARA_OK);
	}

	@Override
	public void setCupPara_NO(int type) {

	}

	@Override
	public void controlCup_OK(String controlAlp) {
		if (controlAlp.equals(BluetoothType.control_correcttouch)) {
			mHandler.sendEmptyMessage(CONTROL_CORRECTTOUCH_OK);
		} else if (controlAlp.equals(BluetoothType.control_recovery)) {
			if(!mDailog_recovery.getCheckBoxRecoveryCloudData()){
				mHandler.sendEmptyMessage(CONTROL_RECOVERY_OK);
			}
		}
	}

	@Override
	public void controlCup_NO(String controlAlp) {

	}

	@Override
	public void updateCupInfo_OK() {
		mHandler.sendEmptyMessage(UPDATE_USERINFO_OK);
	}

	@Override
	public void updateCupInfo_NO() {
		mHandler.sendEmptyMessage(UPDATE_USERINFO_NO);
	}

}
