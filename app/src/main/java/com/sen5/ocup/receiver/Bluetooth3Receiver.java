package com.sen5.ocup.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.sen5.ocup.R;
import com.sen5.ocup.activity.MainActivity;
import com.sen5.ocup.blutoothstruct.CupStatus;
import com.sen5.ocup.blutoothstruct.NFCInfo;
import com.sen5.ocup.fragment.OteaFragment;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.TeaListUtil;
import com.sen5.ocup.util.Tools;

import java.lang.reflect.Method;

/**
 * 接收蓝牙广播
 * 
 * @author caoxia
 *
 */

public class Bluetooth3Receiver extends BroadcastReceiver {

	private static final String TAG = "Bluetooth3Receiver";

	public static final String ACTION_RECIEVE_NFCDATA = "com.sen5.ocup.receiver.bluetooth.recieverNFCData";
	public static final String ACTION_RECIEVE_SOKET_CONNECTED = "com.sen5.ocup.receiver.bluetooth.socketconnected";

	public static int icount_bondFailed;// 绑定失败的次数
	
	public static boolean mStatndy;// 是否待机
	public static boolean mUpdate;// 是否升级

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.d(TAG, "onReceive------intent.getAction()===" + action);
		if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			Log.e(TAG, "onReceive-----ACTION_ACL_DISCONNECTED===" + device.getAddress());
			if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED 
					||BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_NONE 
							|| BluetoothConnectUtils.getInstance().getmConnectState() == BluetoothConnectUtils.CONNECT_WAITE 
					|| mStatndy|| mUpdate) {
				if (mStatndy) {
					mStatndy = false;
				}
				if (mUpdate) {
					mUpdate = false;
				}
				BluetoothConnectUtils.getInstance().closeBluetoothCommunication();
				// 如果未连接上杯子，则不做处理
				BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_NONE);
				// 尝试重连
				Log.d(TAG, "onReceive)-------ACTION_ACL_DISCONNECTED ---reconnect_timer==");
				BluetoothConnectUtils.getInstance().reconnect_timer();
			}
			// 蓝牙连接上
		} else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			if(null == device){
				Log.e(TAG, "onReceive)-------ACTION_ACL_CONNECTED ---== null == device");
			}else{
				Log.e(TAG, "onReceive)-------ACTION_ACL_CONNECTED ---==" + device.getAddress());
			}
		} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
			BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			BluetoothDevice device = BluetoothConnectUtils.getInstance().getDevice();
			if (null == device || null == d || null == d.getAddress() || !d.getAddress().equals(device.getAddress())) {
				return;
			}
			Log.d(TAG, "onReceive)-------ACTION_BOND_STATE_CHANGED ----d.getAddress()="+d.getAddress());
		
			if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
				Log.d(TAG, "onReceive----ACTION_BOND_STATE_CHANGED)==bond  succeed=");
				//连接上
				BluetoothConnectUtils.getInstance().doConnect(device.getAddress());
				icount_bondFailed = 0;
			} else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
				//解除配对
				BluetoothConnectUtils.getInstance().dealRemoveBond(device);
			}
		
		} else if (BluetoothDevice.ACTION_FOUND.equals(action)) { // 发现设备
			// 从intent中获取蓝牙设备对象
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			int deviceType = getBluetoothDeviceType(device);
			Log.d(TAG, "onReceive)---------ACTION_FOUND==device.getName()==" + device.getName()+"  deviceType=="+deviceType);
			if (deviceType != 3 && deviceType != 1) {
				return;
			}
			
			if (BluetoothConnectUtils.getInstance().isRunFront) {
				Log.d(TAG, "onReceive)---------ACTION_FOUND==BluetoothConnectUtils.getInstance().isRunFront==" + BluetoothConnectUtils.getInstance().isRunFront);
				if (BluetoothConnectUtils.getInstance().getDevice() != null && BluetoothConnectUtils.getInstance().getDevice().getAddress().equals(device.getAddress())) {
					Log.e(TAG, "onReceive)---------ACTION_FOUND connect");
					BluetoothConnectUtils.getInstance().connect(device);
				} else {
					Log.d(TAG, "onReceive)---------ACTION_FOUND=BluetoothConnectUtils.getInstance().getDevice() == null  or address not equal");
				}
			}
			// 扫描设备结束
		} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
			Log.d(TAG, "onReceive)---------ACTION_DISCOVERY_FINISHED==BluetoothConnectUtils.getInstance().isRunFront==" + BluetoothConnectUtils.getInstance().isRunFront);
			if (BluetoothConnectUtils.getInstance().isRunFront && BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_REDISCOVERY) {
				// 重新搜索。。。。。。。。
				if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
					BluetoothAdapter.getDefaultAdapter().startDiscovery();
					BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_REDISCOVERY);
				}
			}
		} else if ("android.bluetooth.device.action.PAIRING_REQUEST".equals(action)) {
			Log.d(TAG, "onReceive)------paring");
			BluetoothDevice device = BluetoothConnectUtils.getInstance().getDevice();
			BluetoothConnectUtils.getInstance().setPin(BluetoothDevice.class, device, "8888");
			BluetoothConnectUtils.getInstance().setPairingConfirmation(BluetoothDevice.class, device, true);
		} else if (ACTION_RECIEVE_NFCDATA.equals(action)) {// 接收到杯子发回的消息
			Log.d(TAG, "onReceive)-----ACTION_RECIEVEDATA  NFCInfo.getInstance().getCur_time()==" + NFCInfo.getInstance().getCur_time()
					+ "   Tools.minute2hour(NFCInfo.getInstance().getCur_time())==" + Tools.minute2hour(NFCInfo.getInstance().getCur_time()));
			
			int curIndex = TeaListUtil.getInstance().getCurTeaIndex(context);
			
			if (curIndex > 0 && TeaListUtil.getInstance().teaList.size() >= curIndex &&
					CupStatus.getInstance().getCur_water_temp() >= 60 && NFCInfo.getInstance().getDur() != 65535) {
				String teaName = TeaListUtil.getInstance().teaList.get(curIndex).getName();// NFCInfo.getInstance().getTeaVarietyCode()
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.icon_29)
						.setContentTitle(context.getString(R.string.nfc_notification_title) + teaName).setContentText(Tools.minute2hour(NFCInfo.getInstance().getCur_time() / 60));
				mBuilder.setTicker(context.getString(R.string.nfc_notification_title) + teaName);// 第一次提示消息的时候显示在通知栏上
				mBuilder.setAutoCancel(true);// 自己维护通知的消失

				// 构建一个Intent
				Intent resultIntent = new Intent(context, MainActivity.class);
				resultIntent.putExtra("fromNFC", true);
				resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 2, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				// 设置通知主题的意图
				mBuilder.setContentIntent(resultPendingIntent);
				// 获取通知管理器对象
				NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;
				mNotificationManager.notify(2, mBuilder.build());
				/*暂时关闭这段代码*/
//				MainActivity.isFromNFC = true;
				
				if (!BluetoothConnectUtils.getInstance().isRunFront) {
					OteaFragment.isRecieveNFC = true;
				}
			}else{
				Log.e(TAG, "doNFC---Reciver " + curIndex  + "::::" + TeaListUtil.getInstance().teaList.size());
				Log.e(TAG, "doNFC---Reciver " + CupStatus.getInstance().getCur_water_temp() + "::::" + NFCInfo.getInstance().getDur());
			}
		}
	}

	
	
	/**
	 * 获取设备类型
	 * @param device
	 * @return  1：经典    2：ble   3:双模  0：未知
	 */
	private int getBluetoothDeviceType(BluetoothDevice device){
		Class btClass = null;
		int ret = 1;
		try {
			btClass = Class.forName("android.bluetooth.BluetoothDevice");
			Method getTypeMethod = btClass.getMethod("getType");
			if (getTypeMethod != null) {
				ret = (Integer) getTypeMethod.invoke(device);
			}
		} catch (Exception e) {
			Log.d(TAG, "getBluetoothDeviceType-----------Exception--" + e);
			ret = 1;
		}
		return ret;
	}

}
