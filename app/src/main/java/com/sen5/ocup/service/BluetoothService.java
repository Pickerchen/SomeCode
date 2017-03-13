package com.sen5.ocup.service;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.activity.Dialog_updateActivity;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.alarm.Time_show;
import com.sen5.ocup.blutoothstruct.BluetoothType;
import com.sen5.ocup.blutoothstruct.CupPara;
import com.sen5.ocup.blutoothstruct.CupStatus;
import com.sen5.ocup.blutoothstruct.DrinkData;
import com.sen5.ocup.blutoothstruct.NFCInfo;
import com.sen5.ocup.callback.BluetoothCallback.RecieveDataFromBlueCallback;
import com.sen5.ocup.fragment.SettingFragment;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.receiver.Bluetooth3Receiver;
import com.sen5.ocup.struct.BluetoothPakageType;
import com.sen5.ocup.struct.CupInfo;
import com.sen5.ocup.util.BlueToothRequest;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.DataSwitch;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.QQConnect;
import com.sen5.ocup.util.TeaListUtil;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 : 接收蓝牙设备发回的消息
 */
public class BluetoothService extends Service {
	private static final String TAG = "BluetoothService";
	public static final String ACTION_BLUETOOTHSERVICE = "com.sen5.ocup.service.BluetoothService";
	public static final String ACTOIN_CUPPARA = "com.sen5.ocup.service.BluetoothService.cuppara";
	public static final String ACTION_SOCKETCLOSE = "socketIsClosed";
	public static final String ACTION_RECEIVERSTATUS = "bluetoothService_receiverStatus";
	private BluetoothBinder binder = new BluetoothBinder();
	public static int flag = 0;
	private readThread mreadThread;

	public static boolean isReadRun = true;

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand--------");
		isReadRun = true;
		if (null != BluetoothConnectUtils.getInstance().getSocket()) {
			Logger.e("OnstartCommand","socket = "+BluetoothConnectUtils.getInstance().getSocket().toString());
			readDataFromBlue(null, BluetoothConnectUtils.getInstance().getSocket());
		}
		return super.onStartCommand(intent, Service.START_REDELIVER_INTENT, startId);
//		return Service.START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy--------");
		if (mreadThread != null) {
			mreadThread.interrupt();
			mreadThread = null;
		}
		// flag = 0;
		super.onDestroy();
	}

	class BluetoothBinder extends Binder {
		public BluetoothService getService() {
			return BluetoothService.this;
		}
	}

	public void readDataFromBlue(RecieveDataFromBlueCallback callback, BluetoothSocket socket) {
		Log.d(TAG, "readDataFromBlue------------");
		mreadThread = new readThread(callback, socket);
		mreadThread.start();
	}

	/**
	 * 读取数据
	 */
	private class readThread extends Thread {
		BluetoothSocket socket;
		RecieveDataFromBlueCallback callback;
		public readThread(RecieveDataFromBlueCallback callback, BluetoothSocket socket) {
			super();
			this.callback = callback;
			this.socket = socket;
		}
		public void run() {
			Log.e(TAG,"readThread is start to run");
			byte[] buffer = new byte[1024];
			int bytes = 0;
			InputStream mmInStream = null;
			try {
				mmInStream = socket.getInputStream();
				Log.e(TAG, "readThread--------socket.getInputStream()====");
			} catch (IOException e1) {
				e1.printStackTrace();
				Log.e(TAG, "readThread-------1111111111socket.getInputStream()       -IOException e1====" + e1);
			}
			Log.e(TAG,"isReadRun = "+isReadRun);
			while (isReadRun) {
				try {
					// Read from the InputStream
					if (mmInStream != null) {
						if ((bytes = mmInStream.read(buffer)) > 0) {
							Log.e(TAG, "readThread-------------length==" + bytes);
							String s = new String(buffer);
							if (s.length() > 0 && BlueToothRequest.getInstance().isUpdate) {
								//在进行升级操作
								doUpdate(buffer, s);
							} else {
								if (s.length() > 8) {
									String s_sub = s.substring(1, 8);
									// 接收到cup参数信息
									if (s_sub.equals(BluetoothType.receiveCupInfo)) {
										BlueToothRequest.getInstance().setRequesting(false);
										readCupInfo(buffer);
										if (null != BlueToothRequest.getInstance().mGetCupParaCallback) {
											BlueToothRequest.getInstance().mGetCupParaCallback.getCupPara_OK();
										}
									} else if (s_sub.equals(BluetoothType.receiveCupStatus)) {
										BlueToothRequest.getInstance().setRequesting(false);
										readCupStatus(buffer);
										if (null != BlueToothRequest.getInstance().mIGetCupStatusCallback) {
											BlueToothRequest.getInstance().mIGetCupStatusCallback.getCupStatus_OK();
										}
									} else if (s_sub.equals(BluetoothType.receiveCupID)) {
										BlueToothRequest.getInstance().setRequesting(false);
										readCupID(buffer);
									} else if (s_sub.equals(BluetoothType.controlCupResult)) {
										BlueToothRequest.getInstance().setRequesting(false);
										String s_contrl = s.substring(8, 9);
										Log.e(TAG, "readcontrolCupResult     s.toCharArray()[0]==" + (s_contrl));
										if (null != BlueToothRequest.getInstance().mSetTeaPercentCallback && (s_contrl).equals(BluetoothType.control_tea)) {
											BlueToothRequest.getInstance().mSetTeaPercentCallback.setTeaPercent_OK();
										}
										if (null != BlueToothRequest.getInstance().mControlCupCallback && (s_contrl).equals(BluetoothType.control_correcttouch)) {
											BlueToothRequest.getInstance().mControlCupCallback.controlCup_OK(s_contrl);
										} else if (null != BlueToothRequest.getInstance().mControlCupCallback && (s_contrl).equals(BluetoothType.control_recovery)) {
											BlueToothRequest.getInstance().mControlCupCallback.controlCup_OK(s_contrl);
										}
									} else if (s_sub.equals(BluetoothType.receive_ok)) {
										BlueToothRequest.getInstance().setRequesting(false);
										Log.e(TAG, "readCupTimeResult  ====receive_ok" + String.format("%02x", buffer[8]));
										if (null != BlueToothRequest.getInstance().mSetCupParaCallback && (s.toCharArray()[0] + "").equals("5")) {
											Log.d(TAG, "readThread--null != mSetCupParaCallback");
											BlueToothRequest.getInstance().mSetCupParaCallback.setCupPara_OK(0);
										} else if (null != BlueToothRequest.getInstance().mSetRemindDataCallback && (s.toCharArray()[0] + "").equals("9")) {
											Log.e(TAG, "readThread--null != mSetRemindDataCallback");
											BlueToothRequest.getInstance().mSetRemindDataCallback.setRemindData_OK();
										} else if ((s.toCharArray()[0] + "").equals("4")) {// 设置时间成功
											Log.e(TAG, "readThread-receive_ok= settime");
											BlueToothRequest.getInstance().sendMsg2getCupInfo(null);
										}
									} else if (s_sub.equals(BluetoothType.receiveWaterDataDay)) {
										BlueToothRequest.getInstance().setRequesting(false);
										Log.e(TAG,
												"receiveWaterDataDay  ===bytes=" + bytes + "   N==" + String.format("%02x", buffer[8]) + "  drinktime="
														+ BlueToothRequest.getInstance().drinktime);
										if (buffer[8] != 42) {
											readDrinkData(buffer);
										} else {
											Log.e(TAG, "receiveWaterDataDay  ==no drink data");
										}
										if (null != BlueToothRequest.getInstance().mGetDrinkDataCallback) {
											BlueToothRequest.getInstance().mGetDrinkDataCallback.getDrinkData_OK();
										}
									} else if (s_sub.equals(BluetoothType.receive_nfc)) {
										Log.e(TAG, "receiveNFCData------------------");
										readNFCInfo(buffer);
									} else if (s_sub.equals(BluetoothType.receive_cupLife)) {
										Log.e(TAG, "receiveCupLife------------------");
										if (socket != null) {// &&
											// socket.isConnected()
											BlueToothRequest.getInstance().sendMsg2ReplyCupLife(socket);
										}
									} else if (s_sub.equals(BluetoothType.receive_cupPass)) {
										Log.e(TAG, "receive_cupPass------------------");
										BluetoothConnectUtils.getInstance().confirmPass();

									} else if (s_sub.equals(BluetoothType.recieverCupRemind)) {
										BlueToothRequest.getInstance().setRequesting(false);
										Log.e(TAG, "readCupRemind------------------");
										new DBManager(getApplicationContext()).deleteAllAlarm_time();
										if (buffer[8] != 42) {
											readCupRemind(buffer, buffer[8], buffer[10]);
										} else {
											Log.e(TAG, "readCupRemind  ==no remind data");
										}
										if (null != BlueToothRequest.getInstance().mGetRemindDataCallback) {
											BlueToothRequest.getInstance().mGetRemindDataCallback.getRemindData_OK();
										}
									}
								}
							}
						}
					}
					else {
						Log.e(TAG,"mmInStream is null");
					}
				} catch (IOException e) {
					Log.e(TAG, "readThread -------IOException  e===" + e);
					//蓝牙连接断开，提示用户重新连接
					if (e.getMessage().toString().contains(getString(R.string.socketClose))){
						//发送广播启动土司
						Intent intent = new Intent(ACTION_SOCKETCLOSE);
						sendBroadcast(intent);
					}
					try {
						mmInStream.close();
					} catch (IOException e1) {
						Log.e(TAG, "readThread -------IOException  e1===" + e1);
						e1.printStackTrace();
					}
					BlueToothRequest.getInstance().setRequesting(false);
					break;
				}
			}
		}
	}

	/**
	 * 升级处理
	 * 
	 * @param buffer
	 * @param s
	 */
	private void doUpdate(byte[] buffer, String s) {
		BlueToothRequest.getInstance().setRequesting(false);
		byte[] s_suc = { buffer[0] };
		String s_up = s.substring(0, 1);
		String s_end = s.substring(0, 2);
		Log.d(TAG, "-----------doUpdate  buffer[0]==" + String.format("%02x", buffer[0]) + "   s_end==" + s_end + "::::s_suc = " + s_suc);
		// 判断升级包的发送情况
		if (Arrays.equals(s_suc, (BluetoothPakageType.ACK))) {
			Log.d(TAG, "---SendUpdateFile2Cup doUpdate  update: ACK");
			if (null != BlueToothRequest.getInstance().mPakageCallback) {
				BlueToothRequest.getInstance().mPakageCallback.pakage_ACK();
			}
		} else if (Arrays.equals(s_suc, (BluetoothPakageType.NAK))) {
			Log.d(TAG, "----SendUpdateFile2Cup doUpdate  update: NAK");
			BlueToothRequest.getInstance().mPakageCallback.Pakage_NO();
		} else if (Arrays.equals(s_suc, BluetoothPakageType.CAN)) {
			if (null != BlueToothRequest.getInstance().mPakageCallback) {
				Log.d(TAG, "doUpdate  update: CAN");
				Log.e(TAG, "------------------------11------CustomDialog.EXIT_FIRMWARE_DIALOG = " + CustomDialog.EXIT_FIRMWARE_DIALOG);
				BlueToothRequest.getInstance().mPakageCallback.interrupt_UPGRADE();
			}
		} else if (s_up.equals("C")) {
			// 判断升级指令是否执行成功，如成功则发送升级包，否则继续发送升级命令
			Log.d(TAG, "doUpdate: command execute success !!!");
			if (flag == 0) {
				if (null != BlueToothRequest.getInstance().mSendOnlineCipherCallback) {
					Log.d(TAG, "doUpdate: onlineCipher_OK 1");
					BlueToothRequest.getInstance().mSendOnlineCipherCallback.onlineCipher_OK();
					flag = 1;
				}
			}
		} else if (s_end.equals("OK")) {
			// 升级成功
			Log.d(TAG, "doUpdate  升级成功");
			BlueToothRequest.getInstance().mSendOnlineCipherCallback.onlineCipher_updateSucceed();
		} else {
			if (null != BlueToothRequest.getInstance().mSendOnlineCipherCallback) {
				// 要改
				if (flag == 0) {
					Log.d(TAG, "doUpdate: onlineCipher_OK 2");
					BlueToothRequest.getInstance().mSendOnlineCipherCallback.onlineCipher_NO();
				}
			}
		}
	}

	/**
	 * 读取NFC消息
	 * 
	 * @param buffer
	 */
	private void readNFCInfo(byte[] buffer) {
		String str = new String(buffer);
		Log.d(TAG, "readNFCInfo---readNFCInfo==" + str);
		Intent intent = new Intent(Bluetooth3Receiver.ACTION_RECIEVE_NFCDATA);
		NFCInfo mNFCInfo = NFCInfo.getInstance();
		int dur = DataSwitch.bytesTwo2Int(new byte[] { buffer[12], buffer[13] });
		Log.d(TAG, "readNFCInfo---dur==" + dur);
		mNFCInfo.setDur(DataSwitch.bytesTwo2Int(new byte[] { buffer[12], buffer[13] }));
		mNFCInfo.setCur_time(Tools.getCurSecond());
		Log.d(TAG, "readNFCInfo-----cur_time==" + mNFCInfo.getCur_time());
		// mNFCInfo.setDur(DataSwitch.bytesFour2Int(new byte[] { buffer[12],
		// buffer[13] ,buffer[14], buffer[15]}));
		Log.d(TAG, "readNFCInfo-----------" + String.format("%02x", buffer[12]) + "   " + String.format("%02x", buffer[13]) + "   " + String.format("%02x", buffer[14]) + "  "
				+ String.format("%02x", buffer[15]));
		// mNFCInfo.setCur_time(DataSwitch.bytesTwo2Int(new byte[] { buffer[12],
		// buffer[13] }) * 60);
		mNFCInfo.setTeaVarietyCode(("" + (char) buffer[14]) + ("" + (char) buffer[15]));
		mNFCInfo.setTeaProductionPlaceCode(("" + (char) buffer[16]) + ("" + (char) buffer[17]));
		mNFCInfo.setProduce_year(("" + (char) buffer[18]) + ("" + (char) buffer[19]) + ("" + (char) buffer[20]) + ("" + (char) buffer[21]));
		mNFCInfo.setProduce_month(("" + (char) buffer[22]) + ("" + (char) buffer[23]));
		mNFCInfo.setProcude_day(("" + (char) buffer[24]) + ("" + (char) buffer[25]));
		mNFCInfo.getString();
		sendBroadcast(intent);
	}

	/**
	 * 读取一条指定小时的饮水数据
	 * 
	 * @param buffer
	 */
	private void readDrinkData(byte[] buffer) {
		Log.d(TAG,
				"readDrinkData-----------------BlueToothRequest.getInstance().drinktime====" + BlueToothRequest.getInstance().drinktime + "  8== "
						+ String.format("%02x", buffer[8]) + "  9==" + String.format("%02x", buffer[9]));
		DBManager mDBManager = new DBManager(getApplicationContext());
		DrinkData mDrinkData = new DrinkData();
		mDrinkData.setCupid(OcupApplication.getInstance().mOwnCup.getCupID());
		mDrinkData.setWater_yield(DataSwitch.bytesTwo2Int(new byte[] { buffer[8], buffer[9] }));
		mDrinkData.setDrink_time(BlueToothRequest.getInstance().drinktime * 60 * 60);
		mDrinkData.setDrink_date(System.currentTimeMillis());
		mDrinkData.setWater_temp(0);

		mDrinkData.getString();
		// 加入数据库
		mDBManager.addDrinkData(mDrinkData);

		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");// 初始化Formatter的转换格式。
		String hms = formatter.format(System.currentTimeMillis());
		int h = Integer.parseInt(hms.split(":")[0]);
		int time = (BlueToothRequest.getInstance().drinktime + 1) <= h ? (BlueToothRequest.getInstance().drinktime + 1) : (-1);
		if (time > 0) {
			if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
				BlueToothRequest.getInstance().sendMsg2getWaterData(time, null);
			}
		}
	}

	/**
	 * 读取杯子的闹钟
	 * 
	 * @param buffer
	 */
	private void readCupRemind(byte[] buffer, int index, int count) {
		Log.d(TAG, "readCupRemind----------index=" + String.format("%02x", buffer[8]) + "  count==" + String.format("%02x", buffer[10]));
		DBManager mDBManager = new DBManager(getApplicationContext());
		Time_show timer = new Time_show();
		for (int i = 0; i < count; i++) {
			timer = null;
			timer = new Time_show();
			int time = DataSwitch.bytesTwo2Int(new byte[] { buffer[12 + i * 3], buffer[13 + i * 3] });
			timer.setTime(Tools.minute2hour(time));// "14..33"
			timer.setFlag(buffer[14 + i * 3]);
			// 加入数据库
			mDBManager.add_alarmtime(timer);
			Log.d(TAG, "readCupRemind---time==" + timer.getTime() + "  time--==" + time + "   time,isFlag==" + timer.isFlag());
		}
	}

	/**
	 * 解析杯子ID 2f0013304a00454e5830
	 * @param buffer
	 */
	private void readCupID(byte[] buffer) {
		String last_cupID = Tools.getPreference(getApplicationContext(), UtilContact.CUP_ID);
		if (last_cupID != null) {
			Logger.e(TAG, "readCupID-----------------last_cupID==" + last_cupID);
		} else {
			Logger.e(TAG, "readCupID-----------------last_cupID==" + null);
		}
		String cupID = String.format("%02x", buffer[8]) + String.format("%02x", buffer[9]) + String.format("%02x", buffer[10]) + String.format("%02x", buffer[11])
				+ String.format("%02x", buffer[12]) + String.format("%02x", buffer[13]) + String.format("%02x", buffer[14]) + String.format("%02x", buffer[15])
				+ String.format("%02x", buffer[16]) + String.format("%02x", buffer[17]);
		Logger.e(TAG, "readCupID---cupID==" + cupID);
		Tools.savePreference(BluetoothService.this,UtilContact.CUP_ID,cupID);
		OcupApplication.getInstance().mOwnCup = new DBManager(getApplicationContext()).queryOwnCup(cupID);
		String to_huanxinID = new DBManager(getApplicationContext()).queryCup_mate(cupID);
		if (null != to_huanxinID) {
			CupInfo cup = new DBManager(getApplicationContext()).queryOtherCup(to_huanxinID);
			if (cup != null) {
				OcupApplication.getInstance().mOtherCup = cup;
			}
		}
		OcupApplication.getInstance().mOwnCup.setCupID(cupID);
		Tools.savePreference(getApplicationContext(), "cupid", cupID);
		if (last_cupID == null) {
			HttpRequest.getInstance().mCookies.clear();
		} else if (!cupID.equals(last_cupID)) {
			HttpRequest.getInstance().mCookies.clear();
			Tools.savePreference(BluetoothService.this,UtilContact.GROUPID,"");
		}
		HttpRequest.getInstance().getGroupID(BluetoothService.this);
		new DBManager(getApplicationContext()).addCup(OcupApplication.getInstance().mOwnCup);// 将此ID的杯子加入数据库
		BlueToothRequest.getInstance().sendMsg2setCupTime(TeaListUtil.getInstance().getCurMinute());
	}
	/**
	 * 读取cup信息
	 * 
	 * @param buffer
	 */
	private void readCupInfo(byte[] buffer) {
		Log.d(TAG, "readCupInfo----------------isFirstReadCupInfo==-"+OcupApplication.getInstance().isFirstReadCupInfo);
		CupPara mCupPara = CupPara.getInstance();
		mCupPara.setStart_time(DataSwitch.bytesTwo2Int(new byte[] { buffer[8], buffer[9] }));
		mCupPara.setEnd_time(DataSwitch.bytesTwo2Int(new byte[] { buffer[10], buffer[11] }));
		mCupPara.setAdvise_water_yield(DataSwitch.bytesTwo2Int(new byte[] { buffer[12], buffer[13] }));
		mCupPara.setPara_verion((int) DataSwitch.bytesTwo2Int(new byte[] { buffer[14], buffer[15] }));
		mCupPara.setRemind_times(buffer[16]);
		mCupPara.setLED_data(buffer[17], buffer[18], buffer[19]);

		int sw = buffer[20];

		Log.d(TAG, "readCupInfo-------------sw=-======" + String.format("%02x", sw));
		mCupPara.setHeater_SW(sw & 0x01);
		mCupPara.setHand_warmer_SW((sw & 0x02) >> 1);
		mCupPara.setLed_sw((sw & 0x04) >> 2);
		mCupPara.setShake_sw((sw & 0x08) >> 3);
		mCupPara.setRemind_sw((sw & 0x10) >> 4);
		mCupPara.setLearn_sw((sw & 0x20) >> 5);
		mCupPara.setNfc_sw(((sw & 0x40) >> 6));

		mCupPara.setGotCupPara(true);
		mCupPara.getString();

		if (OcupApplication.getInstance().isFirstReadCupInfo) {
			OcupApplication.getInstance().isFirstReadCupInfo = false;
			if (mCupPara.getPara_verion() % 2 == 0) {// 双数版
				if (mCupPara.getPara_verion() < Tools.even_version) {// 有新版本
					if (new DBManager(OcupApplication.getInstance()).queryRemindUpdate(OcupApplication.getInstance().mOwnCup.getCupID(), Tools.even_version)) {
						Intent intent = new Intent(OcupApplication.getInstance(), Dialog_updateActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}
				}
			} else {// 单数版
				if (mCupPara.getPara_verion() <  Tools.odd_version) {// 有新版本
					if (new DBManager(OcupApplication.getInstance()).queryRemindUpdate(OcupApplication.getInstance().mOwnCup.getCupID(), Tools.odd_version)) {
						Intent intent = new Intent(OcupApplication.getInstance(), Dialog_updateActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}
				}
			}
		}

		OcupApplication.getInstance().sendBroadcast(new Intent(ACTOIN_CUPPARA));
	}

	/**
	 * 读取杯子状态
	 * @param buffer
	 */
	public void readCupStatus(byte[] buffer) {
		Logger.e(TAG, "readCupStatus-----------------");
		CupStatus mCupStatus = CupStatus.getInstance();
		int lastYield = mCupStatus.getTotal_water_yield();
		mCupStatus.setCur_water_temp((int) DataSwitch.bytesTwo2Int(new byte[] { buffer[8], buffer[9] }));
		mCupStatus.setCur_water_yield(DataSwitch.bytesTwo2Int(new byte[] { buffer[10], buffer[11] }));
		// mCupStatus.setCur_water_temp(82);
		// mCupStatus.setCur_water_yield(320);
		mCupStatus.setPrev_water_yield(DataSwitch.bytesTwo2Int(new byte[] { buffer[12], buffer[13] }));
		mCupStatus.setTotal_water_yield(DataSwitch.bytesTwo2Int(new byte[] { buffer[14], buffer[15] }));
		mCupStatus.setGsensor_data(buffer[16], buffer[17], buffer[18]);
		mCupStatus.setCur_battery_capacity(buffer[19]);
		mCupStatus.setCur_time(DataSwitch.bytesTwo2Int(new byte[] { buffer[20], buffer[21] }));
		mCupStatus.setDrink_valid_flag(buffer[22]);

		mCupStatus.getString();
		Intent intent = new Intent(ACTION_RECEIVERSTATUS);
		BluetoothService.this.sendBroadcast(intent);
//		Log.d(TAG, "readCupStatus-----------------lastYield==" + lastYield + "   mCupStatus.getTotal_water_yield()==" + mCupStatus.getTotal_water_yield());
//		if (lastYield != mCupStatus.getTotal_water_yield()) {
//			int time = new DBManager(getApplicationContext()).queryDrinkDataMaxTime();
//			if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
//				BlueToothRequest.getInstance().sendMsg2getWaterData(time / 60 / 60, null);
//			}
//		} else {
//			// 更新到服务器
//			ArrayList<DrinkData> list2srv = new DBManager(getApplicationContext()).getDrinkNeedSrv(OcupApplication.getInstance().mOwnCup.getCupID());
//			Log.d(TAG, "readCupStatus------list2srv.size() ==" + list2srv.size());
//			if (list2srv != null && list2srv.size() > 0) {
//				HttpRequest.getInstance().uploadDrink(list2srv);
//			}
//		}
	}
}
