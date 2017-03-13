package com.sen5.ocup.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.sen5.ocup.R;
import com.sen5.ocup.blutoothstruct.BluetoothType;
import com.sen5.ocup.blutoothstruct.CupPara;
import com.sen5.ocup.callback.BluetoothCallback.PakageCallback;
import com.sen5.ocup.callback.BluetoothCallback.SendOnlineCipherCallback;
import com.sen5.ocup.callback.CustomInterface.IDialog;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.gui.NumberProgressBar;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.gui.NumberProgressBar.ProgressTextVisibility;
import com.sen5.ocup.receiver.Bluetooth3Receiver;
import com.sen5.ocup.receiver.HomeWatcher;
import com.sen5.ocup.receiver.HomeWatcher.OnHomePressedListener;
import com.sen5.ocup.service.BluetoothService;
import com.sen5.ocup.struct.BluetoothPakageType;
import com.sen5.ocup.util.BlueToothRequest;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.CodecUtil;
import com.sen5.ocup.util.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 : 固件升级界面
 */
public class UpdateFirmWare extends BaseActivity implements SendOnlineCipherCallback, Callback, PakageCallback, IDialog {

	private static final String TAG = "UpdateFirmWare";

	private Handler mHander;
	private BlueToothRequest mBlueToothRequest;
	private static HomeWatcher mHomeKeyReceiver = null;
	// test
	// private final static byte[] hex = "0123456789ABCDEF".getBytes();

	// 数据包序号
	public byte order_index = 1;
	// public byte complement_index = (byte) 255;
	public byte[] content_bytes = null;

	public byte[] head_content_bytes = null;

	private List<byte[]> bytes = new ArrayList<byte[]>();
	private int data = 0;
	private byte[] send_package = null;
	private byte[] total_bins;
	private int length = 0;
	private static Context mContext;
	private InputStream in = null, in_test = null;
	private int counter = 0;
	private int sent_count = 0;
	private int sent_command_count = 0;
	private int timeout_count = 0;
	// private int flag = 0;
	// private Timer timer;

	private CustomDialog mDailog_exit;
	private CustomDialog mDailog_updateTips;

	private LinearLayout layout_back;
	private TextView tv_versiontips;
	private TextView tv_stopUpdate;
	private TextView tv_updateProgress;
	private NumberProgressBar pb_update;
	
	private boolean isUpdateNow;//是否立即升级
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_updatefirmware);
		isUpdateNow = getIntent().getBooleanExtra("updateNow", false);
		mInterruptUPGRADE = false;
		initview();
		mHomeKeyReceiver = new HomeWatcher(this);
		mHander = new Handler(this);
		mContext = UpdateFirmWare.this;
		mHomeKeyReceiver.setOnHomePressedListener(new OnHomePressedListener() {

			@Override
			public void onHomePressed() {
				Log.d(TAG, "onHomePressed-------------");
				// dialog();
				BlueToothRequest.getInstance().isUpdate = false;
				UpdateFirmWare.this.finish();
			}

			@Override
			public void onHomeLongPressed() {
				Log.d(TAG, "onHomeLongPressed-------------");
				// dialog();
				BlueToothRequest.getInstance().isUpdate = false;
				UpdateFirmWare.this.finish();
			}
		});
		mBlueToothRequest = BlueToothRequest.getInstance();
		if (CupPara.getInstance().getPara_verion() % 2 == 0) {// 双数版
			if (CupPara.getInstance().getPara_verion() >= Tools.even_version && CupPara.getInstance().getPara_verion() != 8192) {
				tv_versiontips.setText(getString(R.string.apk_new) + CupPara.getInstance().getPara_verion() + ".0");
				tv_updateProgress.setVisibility(View.INVISIBLE);
				pb_update.setVisibility(View.INVISIBLE);
			} else {
				tv_versiontips.setText(getString(R.string.find_newcupversion) + Tools.even_version + ".0");
			}
		} else {// 单数版
			if (CupPara.getInstance().getPara_verion() >= Tools.odd_version) {
				tv_versiontips.setText(getString(R.string.apk_new) + CupPara.getInstance().getPara_verion() + ".0");
				tv_updateProgress.setVisibility(View.INVISIBLE);
				pb_update.setVisibility(View.INVISIBLE);
			} else {
				tv_versiontips.setText(getString(R.string.find_newcupversion) + Tools.odd_version + ".0");
			}
		}
		tv_updateProgress.setOnClickListener(mOnClickListener);
		layout_back.setOnClickListener(mOnClickListener);
		tv_stopUpdate.setOnClickListener(mOnClickListener);
		Log.e(TAG, "----------create---------------isUpdateNow = " + isUpdateNow);
		if (isUpdateNow) {
			mHander.sendEmptyMessage(UPDATE_SURE);
		}
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.textview_progress:
				Log.d(TAG, "onClick  CupPara.getInstance().getPara_verion()===" + CupPara.getInstance().getPara_verion());
				if (tv_updateProgress.getText().equals(getString(R.string.click_update))) {
//					if(getSaveInterrupt()){
//						OcupToast.makeText(UpdateFirmWare.this, getString(R.string.delay_update), 3000).show();
//						return;
//					}
					if (CupPara.getInstance().getPara_verion() % 2 == 0) {// 双数版
						if (CupPara.getInstance().getPara_verion() >= Tools.even_version) {
							OcupToast.makeText(UpdateFirmWare.this, getString(R.string.apk_new) + CupPara.getInstance().getPara_verion() + ".0", 3000).show();
						} else {
							mDailog_updateTips = new CustomDialog(UpdateFirmWare.this, UpdateFirmWare.this, R.style.custom_dialog, CustomDialog.TIPS_FIRMWARE_DIALOG, null);
							mDailog_updateTips.show();
						}
					} else {// 单数版  
						if (CupPara.getInstance().getPara_verion() >= Tools.odd_version) {
							OcupToast.makeText(UpdateFirmWare.this, getString(R.string.apk_new) + CupPara.getInstance().getPara_verion() + ".0", 3000).show();
						} else {
							mDailog_updateTips = new CustomDialog(UpdateFirmWare.this, UpdateFirmWare.this, R.style.custom_dialog, CustomDialog.TIPS_FIRMWARE_DIALOG, null);
							mDailog_updateTips.show();
						}
					}
				} else {
					// 正在升级
				}
				break;
			case R.id.layout_back:
			case R.id.textview_stop:
				dialog();
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 初始化控件
	 */
	private void initview() {

		FrameLayout titleLayout = (FrameLayout) findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(titleLayout,this);

		layout_back = (LinearLayout) this.findViewById(R.id.layout_back);
		tv_versiontips = (TextView) this.findViewById(R.id.textview_versiontips);
		tv_stopUpdate = (TextView) this.findViewById(R.id.textview_stop);
		tv_updateProgress = (TextView) this.findViewById(R.id.textview_progress);
		pb_update = (NumberProgressBar) this.findViewById(R.id.numberbar_updatecup);

		pb_update.setProgressTextVisibility(ProgressTextVisibility.Invisible);
		pb_update.setProgress(100);
	}

	/*
	 * public static String Bytes2HexString(byte[] b) { byte[] buff = new byte[2
	 * * b.length]; for (int i = 0; i < b.length; i++) { buff[2 * i] = hex[(b[i]
	 * >> 4) & 0x0f]; buff[2 * i + 1] = hex[b[i] & 0x0f]; } return new
	 * String(buff); }
	 */
	// int转字节
	public static byte[] intToByteArray1(int i) {
		byte[] result = new byte[4];
		result[0] = (byte) ((i >> 24) & 0xFF);
		result[1] = (byte) ((i >> 16) & 0xFF);
		result[2] = (byte) ((i >> 8) & 0xFF);
		result[3] = (byte) (i & 0xFF);
		return result;
	}

	// public void Timeout() {
	// timer = new Timer();
	// timer.schedule(new TimerTask() {
	//
	// @Override
	// public void run() {
	// mHander.sendEmptyMessage(TIMEOUT_PROG);
	// }
	//
	// }, 1000, 3000);
	// }

	// 流转换为字节数组的方法
	public byte[] readBytes(InputStream in) throws IOException {
		byte[] temp = new byte[in.available()];
		byte[] result = new byte[0];
		int size = 0;
		while ((size = in.read(temp)) != -1) {
			byte[] readBytes = new byte[size];
			System.arraycopy(temp, 0, readBytes, 0, size);
			result = Tools.mergeArray(result, readBytes);
		}
		return result;
	}

	@Override
	protected void onStart() {
		super.onStart();
		// 打开监听HOME
		Log.e(TAG, "----------onStart---------------isUpdateNow = " + isUpdateNow);
		mHomeKeyReceiver.startWatch();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.e(TAG, "----------onResume---------------isUpdateNow = " + isUpdateNow);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mHomeKeyReceiver.stopWatch();// 在onPause中停止监听，不然会报错的。
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy-----------");
		mBlueToothRequest.isUpdate = false;
		sent_count = 3;// home键退出，不再发包
		BluetoothService.flag = 0;
		// if (timer != null) {
		// timer.cancel();
		// }
		sent_command_count = 0;
		if (in_test != null) {
			try {
				in_test.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		new Thread(){
//
//			public void run() {
//				if(getSaveInterrupt()){
//					try {
//						Thread.sleep(6300);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					setSaveInterrupt(mDefaultSharedPreferences, false);
//				}
//			};
//		}.start();
		super.onDestroy();

	}

	@Override
	public void onlineCipher_OK() {
		Log.d(TAG, "update: onlineCipher_OK,send PAKAGE_OK");
		mHander.sendEmptyMessage(PAKAGE_ACK);
	}

	@Override
	public void onlineCipher_NO() {
		mHander.sendEmptyMessage(SEND_COMMAND_NO);
	}

	@Override
	public void pakage_ACK() {
		Log.d(TAG, "update: pakage_OK,send PAKAGE_OK");
		mHander.sendEmptyMessage(PAKAGE_ACK);
	}

	@Override
	public void Pakage_NO() {
		Log.d(TAG, "update: Pakage_NO,send PAKAGE_NO");
		mHander.sendEmptyMessage(PAKAGE_NO);
	}

	@Override
	public void interrupt_UPGRADE() {// 升级被强制终止
		BlueToothRequest.getInstance().isUpdate = false;
		sent_command_count = 3;
		mHander.sendEmptyMessage(SEND_COMMAND_NO);
		// UpdateFirmWare.this.finish();
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (UpdateFirmWare.this.getWindow().isActive()) {
			switch (msg.what) {
			case SEND_COMMAND_OK:
				OcupToast.makeText(mContext, R.string.send_command_ok, Toast.LENGTH_SHORT).show();
				break;
			// 联机密码没有发送成功就重新发送
			case SEND_COMMAND_NO:
				Log.d(TAG, "handmsg-----SEND_COMMAND_NO  sent_command_count==" + sent_command_count);
				if (sent_command_count < 3) {
					if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
						mBlueToothRequest.sendupdate_command(UpdateFirmWare.this, head_content_bytes);
					} else {
						OcupToast.makeText(UpdateFirmWare.this, getString(R.string.unconnect_2cup), 3000).show();
						Log.d(TAG, "SEND_COMMAND_NO  unconnect_2cup  updateFailed");
						updateFailed();
						break;
					}
					sent_command_count++;

				} else {
					Log.d(TAG, "SEND_COMMAND_NO  sent_command_count>=3  updateFailed");
					updateFailed();
				}
				break;
			case PAKAGE_ACK:
				Log.d(TAG, "handmsg-----PAKAGE_ACK");
				// 防止有ACK进来发送了上一个包
				// if (timer != null) {
				// timer.cancel();
				// }
				boolean bIsFinish = false;
				counter = counter + data;
				if (counter >= length) {
						bIsFinish = true;
				}
				if (bIsFinish) {
					// send finish,send finsh msg to cup
					if (counter >= length) {// 升级文件发送完成
						Log.d(TAG, "PAKAGE_ACK  bIsFinish = true counter==" + counter + "length==" + length);
						mBlueToothRequest.sendupdate_command(UpdateFirmWare.this, BluetoothPakageType.EOT);
					}
				} else {
					setUpdatePackageACK(false);
				}
				break;

			// 传送不成功则重新传送
			case PAKAGE_NO:
				Log.d(TAG, "handmsg  update: get PAKAGE_NO  sent_count==" + sent_count);
				// if (timer != null) {
				// timer.cancel();
				// }
				if (sent_count < 3) {
					if (BlueToothRequest.getInstance().isUpdate && BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
						Log.d(TAG, "handmsg  update: get PAKAGE_NO  " + content_bytes[1]);
						mBlueToothRequest.SendUpdateFile2Cup(UpdateFirmWare.this, content_bytes, 1);
					} else {
						OcupToast.makeText(UpdateFirmWare.this, getString(R.string.unconnect_2cup), 3000).show();
						Log.d(TAG, "PAKAGE_NO  unconnect_2cup  updateFailed");
						updateFailed();
						return true;
					}
					sent_count++;
				} else {
					Log.d(TAG, "PAKAGE_NO  sent_count>=3  updateFailed");
					updateFailed();
					return true;
				}
				break;
			case TIMEOUT_PROG:
				Log.d(TAG, "timeout_count==" + timeout_count + "");
				if (timeout_count < 3) {
					if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
						Log.d(TAG, "handmsg  update: get TIMEOUT_PROG  " + content_bytes[1]);
						mBlueToothRequest.SendUpdateFile2Cup(UpdateFirmWare.this, content_bytes, 2);
					} else {
						OcupToast.makeText(UpdateFirmWare.this, getString(R.string.unconnect_2cup), 3000).show();
						Log.d(TAG, "TIMEOUT_PROG  unconnect_2cup  updateFailed");
						updateFailed();
						return true;
					}

					timeout_count++;
					Log.d(TAG, "timeout_count==" + timeout_count + "");
					// timeout_count = 0;
					Log.d(TAG, "update：mBlueToothRequest.SendUpdateFile2Cup(mBlueToothRequest.socket,UpdateFirmWare.this,content_bytes);");
				} else {
					Log.d(TAG, "TIMEOUT_PROG  timeout_count>=3  updateFailed");
					updateFailed();
					return true;
				}
				break;
			case EXIT_OK:
				BlueToothRequest.getInstance().isUpdate = false;
				sent_count = 3;
				mBlueToothRequest.setRequesting(false);
				Log.d(TAG, "--------------SendUpdateFile2Cup-----------EXIT_OK");
				mHander.sendEmptyMessageDelayed(UPDATE_EXIT_DELAY, 500);
				mHander.sendEmptyMessageDelayed(DESTROY_ACTIVITY, 1000);

				break;
			case UPDATE_SURE:
				Log.d(TAG, "UPDATE_SURE------");
				try {
					if (CupPara.getInstance().getPara_verion() % 2 == 0) {// 双数版
						in = mContext.getResources().getAssets().open(Tools.even_filename);
						in_test = mContext.getResources().getAssets().open(Tools.even_filename);
					} else {
						in = mContext.getResources().getAssets().open(Tools.odd_filename);
						in_test = mContext.getResources().getAssets().open(Tools.odd_filename);
					}
					length = in.available();
					total_bins = readBytes(in);
					pb_update.setMax(length);
				} catch (IOException e1) {
					e1.printStackTrace();
					Log.d(TAG, "start2update---IOException e1==" + e1);
				}
				tv_stopUpdate.setVisibility(View.VISIBLE);
				tv_updateProgress.setText(getString(R.string.update_is_Runing));
				String header_ota = BluetoothType.updatefirmware;
				bytes.clear();
				byte[] header = header_ota.getBytes();
				bytes.add(header);
				int filesize = length;
				byte[] filesize_byte = intToByteArray1(filesize);
				bytes.add(filesize_byte);
				int filesize_contrary = ~(filesize);
				byte[] filesize_complement = intToByteArray1(filesize_contrary);
				bytes.add(filesize_complement);
				byte[] crc16_rerify = CodecUtil.crc16Bytes(total_bins);
				// String stest = Bytes2HexString(crc16_rerify);
				byte[] crc16_complement = { (byte) ~crc16_rerify[0], (byte) ~crc16_rerify[1] };
				bytes.add(crc16_rerify);
				bytes.add(crc16_complement);
				Log.d(TAG, "filesize==" + length + ";" + crc16_rerify);
				for (int i = 0; i < crc16_rerify.length; i++) {
					Log.d(TAG, "crc------" + String.format("%02x", crc16_rerify[i]));
				}
				head_content_bytes = sysCopy(bytes);
				pb_update.setProgress(0);
				if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
					mBlueToothRequest.sendupdate_command(UpdateFirmWare.this, head_content_bytes);
				} else {
					OcupToast.makeText(UpdateFirmWare.this, getString(R.string.unconnect_2cup), 3000).show();
					Log.d(TAG, "UPDATE_SURE  unconnect_2cup  updateFailed");
					updateFailed();
					break;
				}
				sent_command_count = 0;
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case UPDATE_SUCCEED:
				pb_update.setProgress(length);
				tv_stopUpdate.setVisibility(View.INVISIBLE);
				tv_updateProgress.setVisibility(View.INVISIBLE);
				tv_updateProgress.setText(getString(R.string.click_update));
				pb_update.setVisibility(View.INVISIBLE);
				if (CupPara.getInstance().getPara_verion() % 2 == 0) {// 1K版
					CupPara.getInstance().setPara_verion(Tools.even_version);
				} else {
					CupPara.getInstance().setPara_verion(Tools.odd_version);
				}
				tv_versiontips.setText(getString(R.string.apk_new) + CupPara.getInstance().getPara_verion() + ".0");
				OcupToast.makeText(UpdateFirmWare.this, getString(R.string.update_finish), 3000).show();
				counter = 0;
				try {
					in_test.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				CupPara.getInstance().setGotCupPara(false);
				Log.d(TAG, "handleMessage--------升级完成---closeBluetoothCommunication-");
				Bluetooth3Receiver.mUpdate = true;
				mBlueToothRequest.isUpdate = false;
				BluetoothConnectUtils.getInstance().closeBluetoothCommunication();
				// 升级完成断开蓝牙不会收到断开的广播，手动将状态改为none
				BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_NONE);
				break;
			case UPDATE_EXIT_DELAY:
				Log.d(TAG, "------------SendUpdateFile2Cup111---mInterruptUPGRADE = " + UpdateFirmWare.mInterruptUPGRADE);
				setUpdatePackageACK(true);
				mInterruptUPGRADE = true;
				
				break;
			case DESTROY_ACTIVITY:
				UpdateFirmWare.this.finish();
			break;
			}
		}
		return false;
	}

	//jyc 2015-03-18 start
	public static boolean mInterruptUPGRADE = false;
	private boolean setUpdatePackageACK(boolean interruptUPGRADE){
		// not finish,send next package to cup
		try {
			send_package = new byte[512];
			int n = in_test.read(send_package, 0, 512);
			if (n > 0) {
				data = send_package.length;
				byte[] header_byte = { 0x01 };
				byte[] order = null;
				if(interruptUPGRADE){
					order = new byte[]{ order_index-- };
				}else{
					order = new byte[]{ order_index++ };
				}
				Log.d(TAG, "--------------order_index = " + order_index);
				byte[] complement = { (byte) ~order[0] };
				byte[] crc16_rerify = CodecUtil.crc16Bytes(send_package);
				bytes.clear();
				bytes.add(header_byte);
				bytes.add(order);
				bytes.add(complement);
				bytes.add(send_package);
				bytes.add(crc16_rerify);
				content_bytes = null;
				content_bytes = sysCopy(bytes);
				Log.d(TAG, "PAKAGE_ACK  bIsFinish =false order==" + order[0]);
				if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
					mBlueToothRequest.SendUpdateFile2Cup(UpdateFirmWare.this, content_bytes, order_index);
				} else {
					OcupToast.makeText(UpdateFirmWare.this, getString(R.string.unconnect_2cup), 3000).show();
					Log.d(TAG, "PAKAGE_ACK  unconnect_2cup  updateFailed");
					updateFailed();
					return true;
				}
				timeout_count = 0;
				// Timeout();// 启动定时器
				pb_update.incrementProgressBy(data);
				Log.d(TAG, "PAKAGE_ACK  bIsFinish = false update: counter==" + counter + ", length==" + length);
				pb_update.setProgress(counter);
			}

		} catch (Exception e) {
			Log.d(TAG, "PAKAGE_ACK  bIsFinish = false: Exception==" + e);
			mBlueToothRequest.isUpdate = false;
			e.printStackTrace();
		}
		return false;
	}
	
	
	
	//jyc 2015-03-18 end
	
	/**
	 * 升级失败，更新界面
	 */
	private void updateFailed() {
		Log.d(TAG, "updateFailed=");
		mBlueToothRequest.isUpdate = false;
		mInterruptUPGRADE = false;
		tv_stopUpdate.setVisibility(View.INVISIBLE);
		tv_updateProgress.setText(getString(R.string.click_update));
		OcupToast.makeText(UpdateFirmWare.this, getString(R.string.update_fail), 3000).show();
		pb_update.setProgress(pb_update.getMax());
		resetCount();
		if (in_test != null) {
			try {
				in_test.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// if (timer != null) {
		// timer.cancel();// 退出定时器
		// // try {
		// //Thread.sleep(1000);
		// timeout_count = 0;
		// // } catch (InterruptedException e) {
		// // e.printStackTrace();
		// // }
		// }
	}

	// 数组合并方法
	public static byte[] sysCopy(List<byte[]> srcArrays) {
		int len = 0;
		for (byte[] srcArray : srcArrays) {
			len += srcArray.length;
		}
		byte[] destArray = new byte[len];
		int destLen = 0;
		for (byte[] srcArray : srcArrays) {
			System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
			destLen += srcArray.length;
		}
		return destArray;
	}

	/**
	 * 重置计数器
	 */
	private void resetCount() {
		order_index = 1;
		counter = 0;
		sent_count = 0;
		sent_command_count = 0;
		timeout_count = 0;
		mBlueToothRequest.isUpdate = false;
		BluetoothService.flag = 0;
	}

	protected void dialog() {
		if (tv_updateProgress.getText().equals(getString(R.string.update_is_Runing))) {
			mDailog_exit = new CustomDialog(UpdateFirmWare.this, this, R.style.custom_dialog, CustomDialog.EXIT_FIRMWARE_DIALOG, null);
			mDailog_exit.show();
		} else {
			UpdateFirmWare.this.finish();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) { // 监控/拦截/屏蔽返回键
			dialog();
			return false;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			// rl.setVisibility(View.VISIBLE);
			// dialog();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private static final int SEND_COMMAND_OK = 1;
	private static final int SEND_COMMAND_NO = 2;
	private static final int PAKAGE_NO = 6;
	private static final int PAKAGE_ACK = 7;
	private static final int TIMEOUT_PROG = 8;
	private static final int EXIT_OK = 9;
	private static final int UPDATE_SURE = 10;
	private static final int UPDATE_SUCCEED = 11;
	private static final int UPDATE_EXIT_DELAY = 12;
	private static final int DESTROY_ACTIVITY = 13;

	
	
	@Override
	public void ok(int type) {
		if (type == CustomDialog.EXIT_FIRMWARE_DIALOG) {
			Log.d(TAG, "------------------------------CustomDialog.EXIT_FIRMWARE_DIALOG = " + CustomDialog.EXIT_FIRMWARE_DIALOG);
			mHander.sendEmptyMessage(EXIT_OK);
		} else if (type == CustomDialog.TIPS_FIRMWARE_DIALOG) {
			mHander.sendEmptyMessage(UPDATE_SURE);
		}
	}

	@Override
	public void ok(int type, Object obj) {
	}

	@Override
	public void cancel(int type) {
	}

	@Override
	public void onlineCipher_updateSucceed() {
		mHander.sendEmptyMessage(UPDATE_SUCCEED);

	}
}
