package com.sen5.ocup.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.receiver.Bluetooth3Receiver;
import com.sen5.ocup.service.BluetoothService;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 : 蓝牙连接工具
 */
public class BluetoothConnectUtils {
	private static final String TAG = "BluetoothConnectUtils";
	/**
	 * 创建BluetoothConnectUtils的单例
	 */
	private static BluetoothConnectUtils mInstance = null;
	/**
	 * 定时重连同步锁
	 */
	private byte[] mLock_reconnectTimer = new byte[] { (byte) 0xf0, (byte) 0xf1 };
	public static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	/**
	 * 蓝牙连接结果
	 */
	private int mConnectState;

	/**
	 * 蓝牙连接结果的广播
	 */
	public static final String ACTION_BLUETOOTHSTATE = "com.sen5.ocup.receiver.BluetoothConnectStateReceiver";
	public static final String KEY_BLUETOOTHSTATE = "blueState";
	public static final int CONNECT_OK = 1;
	public static final int CONNECT_NO = 2;
	public static final int CONNECT_WAITE = 3;
	public static final int CONNECT_ING = 4;
	public static final int CONNECT_NO_NOT_OPEN_BLUETOOTH = 5;
	public static final int CONNECT_PAIRING = 6;
	public static final int CONNECT_UNPAIRING = 7;
	public static final int CONNECT_NO_PAIR = 8;
	public static final int CONNECT_NO_UNPAIR = 9;
	public static final int CONNECT_NO_CONNECT = 10;

	/**
	 * 蓝牙当前状态： none:空闲，断开连接 discovery：正在查找 connecting：正在连接 connected：已连接
	 * 
	 */
	private int bluetoothState;
	public static final int BLUETOOTH_NONE = 1;
	public static final int BLUETOOTH_DISCOVERY = 2;
	public static final int BLUETOOTH_CONNECTING = 3;
	public static final int BLUETOOTH_CONNECTED = 4;
	public static final int BLUETOOTH_BONDING = 5;
	public static final int BLUETOOTH_UNBONDING = 6;
	public static final int BLUETOOTH_REDISCOVERY = 7;

	private BluetoothSocket socket;
	private BluetoothDevice device;
	private ConnectThread mConnectThread;
	public boolean isRunFront;// 应用在前台运行,退到后台运行时不启用蓝牙自动重连机制
	private boolean isConfirmPass = true;// 杯子确认蓝牙可通信
	private int mCount_connectFailed;// 蓝牙连续连接失败的次数
	private Timer timer_removeBond = new Timer();

	public static BluetoothConnectUtils getInstance() {
		if (mInstance == null) {
			mInstance = new BluetoothConnectUtils();
		}
		return mInstance;
	}

	private BluetoothConnectUtils() {
	}

	public BluetoothDevice getDevice() {
		return device;
	}

	public void setDevice(BluetoothDevice device) {
		this.device = device;
	}

	public synchronized void setBluetoothState(int bluetoothstate) {
		Log.d(TAG, "setBluetoothState---bluetoothstate==" + bluetoothstate);
		this.bluetoothState = bluetoothstate;
	}

	public synchronized int getBluetoothState() {
		return bluetoothState;
	}

	public synchronized void setConfirmPass(boolean b) {
		Log.d(TAG, "setConfirmPass---b==" + b);
		this.isConfirmPass = b;
	}

	public synchronized boolean getConfirmPass() {
		return isConfirmPass;
	}

	public synchronized void setSocket(BluetoothSocket socket) {
		Log.d(TAG, "setSocket--==socket" + socket);
		this.socket = socket;
	}

	public synchronized BluetoothSocket getSocket() {
		return socket;
	}

	public synchronized int getmConnectState() {
		return mConnectState;
	}

	public synchronized void setmConnectState(int mConnectState) {
		this.mConnectState = mConnectState;
	}

	/**
	 * 关闭蓝牙通信
	 */
	public void closeBluetoothCommunication() {
		Log.d(TAG, "closeBluetoothCommunication------------------------");
		// 停止接收蓝牙信息的线程
		BluetoothService.isReadRun = false;
		Intent intent = new Intent(OcupApplication.getInstance().getApplicationContext(),BluetoothService.class);
		intent.setAction(BluetoothService.ACTION_BLUETOOTHSERVICE);
//		OcupApplication.getInstance().stopService(new Intent(BluetoothService.ACTION_BLUETOOTHSERVICE));
		OcupApplication.getInstance().stopService(intent);
		cancelConnectThread();
		// setBluetoothState(BLUETOOTH_NONE);接收到蓝牙断开广播后方可设置状态
	}

	/**
	 * 蓝牙连接
	 */
	public synchronized void connect(BluetoothDevice device) {
		setDevice(device);
		if (null == device) {
			Log.e(TAG, "connect--------null == device");
			return;
		}
		Log.e(TAG, "connect----------------address==" + device.getAddress() + "   bluetoothState==" + bluetoothState);
		if (bluetoothState != BLUETOOTH_NONE && bluetoothState != BLUETOOTH_DISCOVERY && bluetoothState != BLUETOOTH_REDISCOVERY) {
			return;
		}
		if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			sendBroadcast_BluetoothconnectResult(CONNECT_NO_NOT_OPEN_BLUETOOTH);
			Log.e(TAG, "connect---------------bluetooth not open  reconnect_timer ");
			reconnect_timer();
			return;
		}
//		getRemoteDevice(address);
//		if (null == device) {
//			Log.e(TAG, "connect--------null == device");
//			setBluetoothState(BLUETOOTH_NONE);
//			sendBroadcast_BluetoothconnectResult(CONNECT_NO);
//			return;
//		}

		cancelConnectThread();
//		 sendBroadcast_BluetoothconnectResult(CONNECT_ING);
		if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
			createBond(device);
		} else {
			removeBond(device);
		}
	}

	public void doConnect(String addr) {
		if (null == device) {
			Log.d(TAG, "doConnect        failed to get remote device");
			setBluetoothState(BLUETOOTH_NONE);
			sendBroadcast_BluetoothconnectResult(CONNECT_NO);
			return;
		}
		setBluetoothState(BLUETOOTH_CONNECTING);
		sendBroadcast_BluetoothconnectResult(CONNECT_ING);
		Logger.e(TAG,"ConnectThread start right away and addr is"+addr);
		mConnectThread = new ConnectThread(addr);
		mConnectThread.start();
	}

	public boolean createBond(BluetoothDevice device) {
		Log.d(TAG, "createBond-------------");
		sendBroadcast_BluetoothconnectResult(CONNECT_PAIRING);
		setBluetoothState(BLUETOOTH_BONDING);
		Class btClass = null;
		boolean ret = false;
		try {
			btClass = Class.forName("android.bluetooth.BluetoothDevice");
			Method createBondMethod = btClass.getMethod("createBond");
			if (createBondMethod != null) {
				ret = (Boolean) createBondMethod.invoke(device);
			}
		} catch (Exception e) {
			Log.d(TAG, "createBond-----------Exception--" + e);
			ret = false;
		}
		//接收到BluetoothDevice.ACTION_BOND_STATE_CHANGED广播之后执行doConnect()方法
		return ret;
	}

	public boolean removeBond(BluetoothDevice device) {
		Logger.e(TAG, "removeBond-------------");
		setBluetoothState(BLUETOOTH_UNBONDING);
		sendBroadcast_BluetoothconnectResult(CONNECT_UNPAIRING);
		Class btClass = null;
		boolean ret = false;
		try {
			btClass = Class.forName("android.bluetooth.BluetoothDevice");
			Method removeBondMethod = btClass.getMethod("removeBond");
			if (removeBondMethod != null) {
				ret = (Boolean) removeBondMethod.invoke(device);
				timer_removeBond.schedule(mTask_removeBond, 2000);
			} else {
				Log.d(TAG, "removeBond   removeBondMethod == null");
			}
		} catch (Exception e) {
			Log.d(TAG, "removeBond-----------Exception--" + e);
		}
		return ret;
	}

	public boolean setPin(Class btClass, BluetoothDevice btDevice, String str) {
		Log.d(TAG, "setPin------------" + str);
		Boolean returnValue = false;
		try {
			Method setPinMethod = btClass.getDeclaredMethod("setPin", new Class[] { byte[].class });
			returnValue = (Boolean) setPinMethod.invoke(btDevice, str.getBytes("UTF-8"));
		} catch (Exception e) {
			Log.e(TAG, "setPin----------Exception--" + e);
			e.printStackTrace();
		}
		return returnValue;
	}

	public boolean setPairingConfirmation(Class btClass, BluetoothDevice btDevice, boolean b) {
		Log.d(TAG, "setPairingConfirmation------------" + b);
		Boolean returnValue = false;
		try {
			Method setPairingConfirmation = btClass.getDeclaredMethod("setPairingConfirmation", new Class[] { byte[].class });
			returnValue = (Boolean) setPairingConfirmation.invoke(btDevice, b);
		} catch (Exception e) {
			Log.e(TAG, "setPairingConfirmation----------Exception--" + e);
			e.printStackTrace();
		}
		return returnValue;
	}

	/**
	 * 取消蓝牙连接
	 */
	private void cancelConnectThread() {
		if (null != getSocket()) {
			try {
				getSocket().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			setSocket(null);
		}
		// 确保蓝牙不再搜索
		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
		if (null != mConnectThread) {
			mConnectThread.interrupt();
			mConnectThread = null;
		}
	}

	/**
	 * /连接建立之前先配对
	 */
	// private void bondDevice() {
	// try {
	// int bondstate = device.getBondState();
	// Log.d(TAG, "bondDevice   bondstate==" + bondstate);
	// if (bondstate == BluetoothDevice.BOND_NONE) {
	// int iCount = 0;
	//
	// while (iCount < 10) {
	// Log.d(TAG, "bondDevice     开始配对, iCount = " + iCount);
	// Method creMethod = BluetoothDevice.class.getMethod("createBond");
	// creMethod.invoke(device);
	// if (bondstate == BluetoothDevice.BOND_BONDED) {
	// break;
	// }
	// Thread.sleep(200);
	// iCount++;
	// }
	// }
	// } catch (Exception e) {
	// Log.d(TAG, "bondDevice    无法配对！Exception==" + e);
	// e.printStackTrace();
	// }
	// }

	/**
	 * 获取蓝牙通信的远程设备
	 * 
	 */
	public BluetoothDevice getRemoteDevice(final String address) {
		try {
			if (!BluetoothAdapter.checkBluetoothAddress(address)) { // 检查蓝牙地址是否有效
				Log.e(TAG, "getRemoteDevice---devAdd un effient!");
				return device = null;
			}
			device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
			return device;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "getRemoteDevice--------Exception  e==" + e);
			return device = null;
		}
	}

	/**
	 * 蓝牙连接线程
	 * 
	 * @author caoxia
	 *
	 */
	private class ConnectThread extends Thread {
		private String mAddr;// 连接的设备地址
		private boolean mIsConnectTimeout;// 是否连接超时

		public ConnectThread(String addr) {
			super();
			this.mAddr = addr;
			Log.e(TAG,"connectThread is created");
		}

		public void run() {
			Log.e("ConnectThread","ConnectThread coming in");
			try {
				if (null != getSocket()) {
					getSocket().close();
					setSocket(null);
					Thread.sleep(2);
				}
				Log.e("ConnectThread","ConnectThread coming in2"+myUUID);
				setSocket(device.createInsecureRfcommSocketToServiceRecord(myUUID));
				Log.e("ConnectThread","ConnectThread coming in3"+myUUID);
				setConfirmPass(false);
				Log.e(TAG, "ConnectThread---------------connect  start  socket.getAddress()=====" + getSocket().getRemoteDevice().getAddress());
				Thread thead = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							sleep(18000);
							Log.e(TAG, "ConnectThread--------connect block---");
							cancelConnectThread();
						} catch (InterruptedException e) {
						}
					}
				});
				thead.start();
				getSocket().connect();
				Log.e(TAG, "ConnectThread---------------connect  success");
				mCount_connectFailed = 0;
				// mIsConnectTimeout = true;
				if (thead.isAlive()) {
					thead.interrupt();
				}
				dealAfterConnected();
			} catch (Exception e) {
				if (e != null){
					Log.e(TAG, "exception = " + e.getMessage().toString());
				}
				sendBroadcast_BluetoothconnectResult(CONNECT_NO_CONNECT);
				e.printStackTrace();
				mIsConnectTimeout = true;
				// if (getBluetoothState() != BLUETOOTH_BLOCK) {
				closeBluetoothCommunication();
				if (mCount_connectFailed++ < 2) {
					setBluetoothState(BLUETOOTH_NONE);
					Log.e(TAG, "ConnectThread---------------Exception connect");
					connect(device);
				} else {
					mCount_connectFailed = 0;
					dealAfaterDisconnect();
				}
				// }
			}
		}
	}

	/**
	 * 蓝牙连接成功后的处理
	 * 
	 */
	public void dealAfterConnected() {
		Log.d(TAG, "dealAfterConnected---------isRunFront==" + isRunFront);
		// 启动服务接收蓝牙消息
//		OcupApplication.getInstance().getApplicationContext().startService(new Intent(BluetoothService.ACTION_BLUETOOTHSERVICE));

		Intent intent = new Intent(OcupApplication.getInstance().getApplicationContext(),BluetoothService.class);
		OcupApplication.getInstance().getApplicationContext().startService(intent);
		if (isRunFront) {
			// 重连不需要手势确认
			confirmPass();
		} else {
			// 发送广播通知挥动手势
			sendBroadcast_BluetoothconnectResult(CONNECT_WAITE);
		}
	}

	/**
	 * 蓝牙连接失败后的处理
	 */
	public void dealAfaterDisconnect() {
		// 发送广播通知连接失败
		sendBroadcast_BluetoothconnectResult(CONNECT_NO);
		setBluetoothState(BLUETOOTH_NONE);
		Log.d(TAG, "dealAfaterDisconnect-----------cancelConnectThread-");
		cancelConnectThread();

		Log.d(TAG, "dealAfaterDisconnect-----------reconnect_timer-");
		reconnect_timer();
	}

	/**
	 * 定时重连
	 */
	public void reconnect_timer() {
		Log.d(TAG, "reconnect_timer----------isRunFront==" + isRunFront);
		if (BluetoothConnectUtils.getInstance().isRunFront) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					synchronized (mLock_reconnectTimer) {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
							Log.d(TAG, "reconnect_timer)----重连---==already connected");
						} else {
							Log.e(TAG, "reconnect_timer connect");
							BluetoothConnectUtils.getInstance().connect(device);
						}
					}
				}
			}).start();
		} else {
			Log.d(TAG, "reconnect_timer)----重连- ---==BluetoothConnectUtils.getInstance() is not RunFront");
		}
	}

	/**
	 * 发送蓝牙连接结果广播
	 * 
	 * @param connectState
	 *            1---连接成功 2---连接失败 3---提示手势确认连接
	 *            6---正在配对 4---正在连接
	 */
	public synchronized void sendBroadcast_BluetoothconnectResult(int connectState) {
		mConnectState = connectState;
		Intent intent = new Intent(ACTION_BLUETOOTHSTATE);
		intent.putExtra(KEY_BLUETOOTHSTATE, connectState);
		intent.putExtra("blueadddr", device.getAddress());
		Logger.e(TAG, "sendBroadcast_BluetoothconnectResult    connectState==" + connectState);
		OcupApplication.getInstance().sendBroadcast(intent);
	}

	/**
	 * 确认连接成功
	 */
	public void confirmPass() {
		Logger.e(TAG,"confirmpass coming");
		if (!getConfirmPass()) {
			setConfirmPass(true);
			OcupApplication.getInstance().mOwnCup = new DBManager(OcupApplication.getInstance()).queryOwnCup(OcupApplication.getInstance().mOwnCup.getCupID());
			OcupApplication.getInstance().mOwnCup.setBlueAdd(getSocket().getRemoteDevice().getAddress());
			new DBManager(OcupApplication.getInstance()).updateOwnCup(OcupApplication.getInstance().mOwnCup);
			Tools.savePreference(OcupApplication.getInstance(), UtilContact.BLUE_ADD, getSocket().getRemoteDevice().getAddress());
			// 将蓝牙请求状态致为false
			BlueToothRequest.getInstance().setRequesting(false);
			// 获取cupid
			BlueToothRequest.getInstance().sendMsg2getCupID();
			// // 发送广播通知已连接
			sendBroadcast_BluetoothconnectResult(CONNECT_OK);
			setBluetoothState(BLUETOOTH_CONNECTED);
		}
	}

	TimerTask mTask_removeBond = new TimerTask() {
		@Override
		public void run() {
			Log.d(TAG, "mTask_removeBond------");
			if (device != null && device.getBondState() == BluetoothDevice.BOND_NONE) {
				// 解除绑定成功，但未收到解除成功的广播
				dealRemoveBond(device);
			}
		}
	};

	/**
	 * 解除配对后的处理
	 * 
	 * @param device
	 */
	public void dealRemoveBond(BluetoothDevice device) {
		if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_BONDING) {
			Log.d(TAG, "dealRemoveBond==bond  failed=");
			Bluetooth3Receiver.icount_bondFailed++;
			BluetoothConnectUtils.getInstance().sendBroadcast_BluetoothconnectResult(BluetoothConnectUtils.CONNECT_NO_PAIR);
			// 绑定失败

			if (Bluetooth3Receiver.icount_bondFailed > 3) {
				Bluetooth3Receiver.icount_bondFailed = 0;
				if (!BluetoothConnectUtils.getInstance().isRunFront) {
					BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_NONE);
					BluetoothConnectUtils.getInstance().sendBroadcast_BluetoothconnectResult(BluetoothConnectUtils.CONNECT_NO);
				} else {
					// 尝试重连
					Log.d(TAG, "dealRemoveBond---reconnect_timer==");
					BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_NONE);
					BluetoothConnectUtils.getInstance().reconnect_timer();
				}
			} else {
				BluetoothConnectUtils.getInstance().createBond(device);
			}

		} else {
			// 解除绑定成功
			Log.d(TAG, "dealRemoveBond==unbond  succeed  =BluetoothConnectUtils.getInstance().isRunFront=" + BluetoothConnectUtils.getInstance().isRunFront);
			timer_removeBond.cancel();
			// if (BluetoothConnectUtils.getInstance().isRunFront) {
			// // 重连 解除绑定成功
			// // 重新搜索。。。。。。。。
			// if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			// BluetoothAdapter.getDefaultAdapter().startDiscovery();
			// BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_REDISCOVERY);
			// }
			// } else {
			// // 首次连接 解除绑定成功
			BluetoothConnectUtils.getInstance().createBond(device);
			// }
		}
	}

}
