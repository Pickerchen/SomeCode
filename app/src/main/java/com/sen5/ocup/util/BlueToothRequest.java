package com.sen5.ocup.util;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.sen5.ocup.activity.UpdateFirmWare;
import com.sen5.ocup.alarm.Time_show;
import com.sen5.ocup.blutoothstruct.BluetoothType;
import com.sen5.ocup.blutoothstruct.CupPara;
import com.sen5.ocup.callback.BluetoothCallback.IControlCupCallback;
import com.sen5.ocup.callback.BluetoothCallback.IGetCupParaCallback;
import com.sen5.ocup.callback.BluetoothCallback.IGetCupStatusCallback;
import com.sen5.ocup.callback.BluetoothCallback.IGetDrinkDataCallback;
import com.sen5.ocup.callback.BluetoothCallback.IGetRemindDataCallback;
import com.sen5.ocup.callback.BluetoothCallback.ISetCupParaCallback;
import com.sen5.ocup.callback.BluetoothCallback.ISetRemindDataCallback;
import com.sen5.ocup.callback.BluetoothCallback.ISetTeaPercentCallback;
import com.sen5.ocup.callback.BluetoothCallback.PakageCallback;
import com.sen5.ocup.callback.BluetoothCallback.SendOnlineCipherCallback;
import com.sen5.ocup.struct.ChatMsgEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 : 封装蓝牙请求
 */
public class BlueToothRequest {
	private static final String TAG = "BlueToothRequest";

	/**
	 * 发送蓝牙信息同步锁
	 */
	private byte[] mLock_sendBluetooth = new byte[] { (byte) 0xf0, (byte) 0xf2 };

	/**
	 * 发送消息的最大字节数
	 */
	private int maxBytes = 500;
	
	/**
	 * 创建BlueToothRequest的单例
	 */
	private static BlueToothRequest mInstance = null;
	
	/**
	 * 蓝牙数据请求表示， true表示正在请求中；false表示不再请求中
	 */
	private boolean isRequesting;

	/**
	 * 当前获取的饮水数据的时间, 单位为小时
	 */
	public int drinktime;
	
	/**
	 * 表示固件是否正在升级， true表示正在升级；false表示不再升级中
	 */
	public boolean isUpdate;

	/**
	 * 每次询问是否请求成功的间隔时间
	 */
	private final int mAskDuaration = 1;
	
	/**
	 * 询问是否请求成功的总次数
	 */
	private final int mAskCount = 3000;
	

	public ISetCupParaCallback mSetCupParaCallback;
	public IGetCupParaCallback mGetCupParaCallback;
	public IGetCupStatusCallback mIGetCupStatusCallback;
	public IGetDrinkDataCallback mGetDrinkDataCallback;
	public ISetRemindDataCallback mSetRemindDataCallback;
	public IGetRemindDataCallback mGetRemindDataCallback;
	public SendOnlineCipherCallback mSendOnlineCipherCallback;
	public ISetTeaPercentCallback mSetTeaPercentCallback;
	public IControlCupCallback mControlCupCallback;
	public PakageCallback mPakageCallback;
	

	public static BlueToothRequest getInstance() {
		if (mInstance == null) {
			mInstance = new BlueToothRequest();
		}
		return mInstance;
	}

	private BlueToothRequest() {
	}

	public synchronized void setRequesting(boolean b) {
		this.isRequesting = b;
	}

	public synchronized boolean getRequesting() {
		return isRequesting;
	}

	/**
	 * 发送请求获取杯子参数 1、 $S1AGP APP向Cup发起获取Cup参数请求 格式：n$S1AGP,*<1><CR><LF>
	 */
	public void sendMsg2getCupInfo(final IGetCupParaCallback getCupParaCallback) {
		Log.d(TAG, "sendMsg2getCupInfo)-----");
		if (isUpdate) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mLock_sendBluetooth) {
					try {
						while (getRequesting()) {
							Thread.sleep(mAskDuaration);
						}
						Log.d(TAG, "sendMsg2getCupInfo)-----enter");
						mGetCupParaCallback = getCupParaCallback;
						setRequesting(true);

						/* 组包*/
						byte[] byte_header = (1 + BluetoothType.getCupInfo + "*").getBytes();
						byte byte_r = 13;
						byte byte_nr = 10;
						byte[] byte_content = new byte[byte_header.length + 2];
						for (int i = 0; i < byte_header.length; i++) {
							byte_content[i] = byte_header[i];
						}
						byte_content[byte_header.length] = byte_r;
						byte_content[byte_header.length + 1] = byte_nr;

						/* 写包*/
						OutputStream os = BluetoothConnectUtils.getInstance().getSocket().getOutputStream();
						if (null != os && null != byte_content) {
							os.write(byte_content);
						} else {
							Log.d(TAG, "sendMsg2getCupInfo)--------null != os && null != byte_content");
						}

						/* 释放byte数组*/
						byte_header = null;
						byte_content = null;

						/* 请求超时监控*/
						int iCount = 0;
						while (iCount++ < mAskCount) {
							if (getRequesting()) {
								Thread.sleep(mAskDuaration);
							} else {
								break;
							}
						}
						Log.d(TAG, "sendMsg2getCupInfo)-----------iCount==" + iCount);
						if (iCount >= mAskCount) {/* 发送失败*/
							setRequesting(false);
						}
					} catch (IOException e) {
						Log.d(TAG, "sendMsg2getCupInfo)-----------IOException e==" + e);
						setRequesting(false);
						e.printStackTrace();
					} catch (Exception e) {
						Log.d(TAG, "sendMsg2getCupInfo)-----------Exception e==" + e);
						setRequesting(false);
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

	/**
	 * $S1AGS APP向Cup发起获取Cup当前状态请求 格式：n$S1AGS,*<1><CR><LF>
	 * 
	 */
	public void sendMsg2getCupStatus(final IGetCupStatusCallback IGetCupStatusCallback) {
		Log.d(TAG, "sendMsg2getCupStatus-------------------");
		if (isUpdate) {
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (mLock_sendBluetooth) {
					try {
						while (getRequesting()) {
							Thread.sleep(mAskDuaration);
						}
						Log.d(TAG, "sendMsg2getCupStatus-----enter--------------");
						mIGetCupStatusCallback = IGetCupStatusCallback;

						setRequesting(true);

						byte[] byte_header = (2 + BluetoothType.getCupStatus + "*").getBytes();
						byte byte_r = 13;
						byte byte_nr = 10;
						byte[] byte_content = new byte[byte_header.length + 2];
						for (int i = 0; i < byte_header.length; i++) {
							byte_content[i] = byte_header[i];
						}
						byte_content[byte_header.length] = byte_r;
						byte_content[byte_header.length + 1] = byte_nr;

						OutputStream os = BluetoothConnectUtils.getInstance().getSocket().getOutputStream();
						if (null != os && null != byte_content) {
							os.write(byte_content);
						} else {
							Log.d(TAG, "sendMsg2getCupStatus--------null != os && null != byte_content");
						}

						/* 释放byte数组*/
						byte_header = null;
						byte_content = null;

						/* 请求超时监控*/
						int iCount = 0;
						while (iCount++ < mAskCount) {
							if (getRequesting()) {
								Thread.sleep(mAskDuaration);
							} else {
								break;
							}
						}
						Log.d(TAG, "sendMsg2getCupStatus-----------iCount ==" + iCount);
						if (iCount >= mAskCount) {/* 发送失败*/
							setRequesting(false);
						}
					} catch (Exception e) {
						Log.d(TAG, "sendMsg2getCupStatus-----------IOException e==" + e);
						e.printStackTrace();
						setRequesting(false);
					}
				}
			}
		}).start();

	}

	/**
	 * 发起获取Cup ID请求 格式：n$S1GID, *<1><CR><LF>
	 */
	public void sendMsg2getCupID() {
		Log.d(TAG, "sendMsg2getCupID--------------------");
		if (isUpdate) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mLock_sendBluetooth) {
					try {
						while (getRequesting()) {
							Thread.sleep(mAskDuaration);
						}
						Log.d(TAG, "sendMsg2getCupID-----------enter---------");
						setRequesting(true);

						byte[] byte_header = (3 + BluetoothType.getCupID + "*").getBytes();
						byte byte_r = 13;
						byte byte_nr = 10;

						byte[] byte_content = new byte[byte_header.length + 2];
						for (int i = 0; i < byte_header.length; i++) {
							byte_content[i] = byte_header[i];
						}
						byte_content[byte_header.length] = byte_r;
						byte_content[byte_header.length + 1] = byte_nr;

						OutputStream os = BluetoothConnectUtils.getInstance().getSocket().getOutputStream();
						if (null != os && null != byte_content) {
							Log.d(TAG, "sendMsg2getCupID)--------byte_content.length==="+byte_content.length);
							os.write(byte_content);
						} else {
							Log.d(TAG, "sendMsg2getCupID)--------null != os && null != byte_content");
						}

						/* 释放byte数组*/
						byte_header = null;
						byte_content = null;

						/* 请求超时监控*/
						int iCount = 0;
						while (iCount++ < mAskCount) {
							if (getRequesting()) {
								Thread.sleep(mAskDuaration);
							} else {
								break;
							}
						}
						Log.d(TAG, "sendMsg2getCupID)-----------iCount==" + iCount);
						if (iCount >= mAskCount) {/* 发送失败*/
							setRequesting(false);
							BluetoothConnectUtils.getInstance().closeBluetoothCommunication();
						}
					} catch (Exception e) {
						Log.d(TAG, "sendMsg2getCupID)-----------Exception e==" + e);
						setRequesting(false);
						BluetoothConnectUtils.getInstance().closeBluetoothCommunication();
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

	/**
	 * APP向Cup设置时间 n$S1AST,<1>*<N><CR><LF> <1>部分为uint16 cur_time 分钟为单位， eg.
	 * 09：28 换算为568分钟十六进制（0x0238） <1>部分的值为：0X0238
	 * 
	 */
	public void sendMsg2setCupTime(final int mimute) {
		Log.d(TAG, "sendMsg2setCupTime--------------------");
		if (isUpdate) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mLock_sendBluetooth) {
					try {
						while (getRequesting()) {
							Thread.sleep(mAskDuaration);
						}
						Log.d(TAG, "sendMsg2setCupTime---------enter-----------");
						setRequesting(true);

						byte[] byte_header = (4 + BluetoothType.setCupTime).getBytes();
						byte[] byte_time = DataSwitch.int2Bytes(mimute);
						byte[] byte_end = new byte[] { "*".getBytes()[0], 13, 10 };
						byte[] byte_content = new byte[byte_header.length + byte_time.length + byte_end.length];
						for (int i = 0; i < byte_header.length; i++) {
							byte_content[i] = byte_header[i];
						}
						for (int i = 0; i < byte_time.length; i++) {
							byte_content[byte_header.length + i] = byte_time[i];
						}
						for (int i = 0; i < byte_end.length; i++) {
							byte_content[byte_header.length + byte_time.length + i] = byte_end[i];
						}

						OutputStream os = BluetoothConnectUtils.getInstance().getSocket().getOutputStream();
						if (null != os && null != byte_content) {
							os.write(byte_content);
						} else {
							Log.d(TAG, "sendMsg2setCupTime)--------null != os && null != byte_content");
						}

						/* 释放byte数组*/
						byte_header = null;
						byte_time = null;
						byte_end = null;
						byte_content = null;

						/* 请求超时监控*/
						int iCount = 0;
						while (iCount++ < mAskCount) {
							if (getRequesting()) {
								Thread.sleep(mAskDuaration);
							} else {
								break;
							}
						}
						Log.d(TAG, "sendMsg2setCupTime)-----------iCount==" + iCount);
						if (iCount >= mAskCount) {/* 发送失败*/
							setRequesting(false);
						}
					} catch (Exception e) {
						Log.d(TAG, "sendMsg2setCupTime)-----------IOException e==" + e);
						e.printStackTrace();
						setRequesting(false);
					}
				}
			}
		}).start();
	}

	public final static int type_waterProject_1 = 101;//设置饮水计划开
	public final static int type_waterProject_0 = 102;//设置饮水计划
	public final static int type_handwarm_1 = 103;//设置暖手开关
	public final static int type_handwarm_0 = 104;//设置暖手开关
	public final static int type_vibration_1 = 105;//设置震动开关
	public final static int type_vibration_0 = 106;//设置震动开关
	public final static int type_led_1 = 107;//灯光提醒开关
	public final static int type_led_0 = 108;//灯光提醒开关
	/**
	 * APP向Cup发起设置Cup参数请求 格式：n$S1ASP, <1>*<2><CR><LF> 示例：n$S1ASP,<1>
	 * *<2><CR><LF> 。  <1>部分为CUP_PARA结构体
	 * 
	 */
	public void sendMsg2setCupPara(final ISetCupParaCallback setCupParaCallback, final int type) {
		Log.d(TAG, "sendMsg2setCupPara  ------------");
		if (isUpdate) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mLock_sendBluetooth) {
					try {
						while (getRequesting()) {
							Thread.sleep(mAskDuaration);
						}
						Log.d(TAG, "sendMsg2setCupPara  ---enter---------");
						mSetCupParaCallback = setCupParaCallback;

						setRequesting(true);

						CupPara mCupPara = CupPara.getInstance();
						byte[] byte_header = (5 + BluetoothType.setCupPara).getBytes();
						byte[] byte_starttime = DataSwitch.int2Bytes(mCupPara.getStart_time());
						byte[] byte_endtime = DataSwitch.int2Bytes(mCupPara.getEnd_time());
						byte[] byte_advise_water_yield = DataSwitch.int2Bytes(mCupPara.getAdvise_water_yield());
						byte[] byte_para_version = DataSwitch.int2Bytes(mCupPara.getPara_verion());
						byte byte_remind_times = DataSwitch.int2Bytes(mCupPara.getRemind_times())[0];
						byte byte_t_high = DataSwitch.int2Bytes(mCupPara.getLED_data().getT_high())[0];
						byte byte_t_norm = DataSwitch.int2Bytes(mCupPara.getLED_data().getT_norm())[0];
						byte byte_t_low = DataSwitch.int2Bytes(mCupPara.getLED_data().getT_low())[0];
						int int_sw = mCupPara.getHeater_SW() + mCupPara.getHand_warmer_SW() * 2 + mCupPara.getLed_sw() * (int) Math.pow(2, 2) + mCupPara.getShake_sw()
								* (int) Math.pow(2, 3) + mCupPara.getRemind_sw() * (int) Math.pow(2, 4) + mCupPara.getLearn_sw() * (int) Math.pow(2, 5) + mCupPara.getNfc_sw()
								* (int) Math.pow(2, 6);
						byte byte_sw = DataSwitch.int2Bytes(int_sw)[0];
						byte byte_null = 0;
						byte[] byte_end = new byte[] { "*".getBytes()[0], 13, 10 };

						byte[] byte_content = new byte[byte_header.length + byte_starttime.length + byte_endtime.length + byte_advise_water_yield.length + byte_para_version.length
								+ 6 + byte_end.length];

						for (int i = 0; i < byte_header.length; i++) {
							byte_content[i] = byte_header[i];
						}
						for (int i = 0; i < byte_starttime.length; i++) {
							byte_content[byte_header.length + i] = byte_starttime[i];
						}
						for (int i = 0; i < byte_endtime.length; i++) {
							byte_content[byte_header.length + byte_starttime.length + i] = byte_endtime[i];
						}
						for (int i = 0; i < byte_advise_water_yield.length; i++) {
							byte_content[byte_header.length + byte_starttime.length + byte_endtime.length + i] = byte_advise_water_yield[i];
						}
						for (int i = 0; i < byte_para_version.length; i++) {
							byte_content[byte_header.length + byte_starttime.length + byte_endtime.length + byte_advise_water_yield.length + i] = byte_para_version[i];
						}
						byte_content[byte_header.length + byte_starttime.length + byte_endtime.length + byte_advise_water_yield.length + byte_para_version.length + 0] = byte_remind_times;
						byte_content[byte_header.length + byte_starttime.length + byte_endtime.length + byte_advise_water_yield.length + byte_para_version.length + 1] = byte_t_high;
						byte_content[byte_header.length + byte_starttime.length + byte_endtime.length + byte_advise_water_yield.length + byte_para_version.length + 2] = byte_t_norm;
						byte_content[byte_header.length + byte_starttime.length + byte_endtime.length + byte_advise_water_yield.length + byte_para_version.length + 3] = byte_t_low;
						byte_content[byte_header.length + byte_starttime.length + byte_endtime.length + byte_advise_water_yield.length + byte_para_version.length + 4] = byte_sw;
						byte_content[byte_header.length + byte_starttime.length + byte_endtime.length + byte_advise_water_yield.length + byte_para_version.length + 5] = byte_null;
						for (int i = 0; i < byte_end.length; i++) {
							byte_content[byte_header.length + byte_starttime.length + byte_endtime.length + byte_advise_water_yield.length + byte_para_version.length + 6 + i] = byte_end[i];
						}

						OutputStream os = BluetoothConnectUtils.getInstance().getSocket().getOutputStream();
						if (null != os && null != byte_content) {
							os.write(byte_content);
							setCupParaCallback.setCupPara_OK(type);
						} else {
							Log.d(TAG, "sendMsg2setCupPara)--------null != os && null != byte_content");
						}

						/* 释放byte数组*/
						byte_header = null;
						byte_starttime = null;
						byte_endtime = null;
						byte_advise_water_yield = null;
						byte_para_version = null;
						byte_end = null;
						byte_content = null;

						/* 请求超时监控*/
						int iCount = 0;
						while (iCount++ < mAskCount) {
							if (getRequesting()) {
								Thread.sleep(mAskDuaration);
							} else {
								break;
							}
						}
						Log.d(TAG, "sendMsg2setCupPara)-----------iCount==" + iCount);
						if (iCount >= mAskCount) {/* 发送失败*/
							setRequesting(false);
							mSetCupParaCallback.setCupPara_NO(type);
						}
					} catch (Exception e) {
						Log.d(TAG, "sendMsg2setCupPara)-----------Exception e==" + e);
						e.printStackTrace();
						setRequesting(false);
					}
				}
			}
		}).start();

	}

	/**
	 * APP向Cup发起获取用户当天喝水记录请求 格式：n$ S1AGD, *<1><CR><LF> 示例：n$S1AGD, *<1><CR><LF>
	 * 
	 * 格式：n$ S1AGD,< n>*<1><CR><LF> 示例：n$S1AGD, <n>*<1><CR><LF> <
	 * n>：请求喝水记录的单位小时数，范围：0x00-0x17
	 *
	 * 
	 */
	public void sendMsg2getWaterData(final int time, final IGetDrinkDataCallback GetDrinkDataCallback) {
		Log.d(TAG, "sendMsg2getWaterData--------time==" + time);
		if (isUpdate) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mLock_sendBluetooth) {
					try {
						while (getRequesting()) {
							Thread.sleep(mAskDuaration);
						}
						Log.d(TAG, "sendMsg2getWaterData---enter-----time==" + time);
						drinktime = time;
						mGetDrinkDataCallback = GetDrinkDataCallback;

						setRequesting(true);

						byte[] byte_header = (6 + BluetoothType.getWaterDataDay).getBytes();
						byte byte_time = (byte) (time);
						byte[] byte_end = new byte[] { "*".getBytes()[0], 13, 10 };
						byte[] byte_content = new byte[byte_header.length + 1 + byte_end.length];
						for (int i = 0; i < byte_header.length; i++) {
							byte_content[i] = byte_header[i];
						}
						byte_content[byte_header.length] = byte_time;
						for (int i = 0; i < byte_end.length; i++) {
							byte_content[byte_header.length + 1 + i] = byte_end[i];
						}

						OutputStream os = BluetoothConnectUtils.getInstance().getSocket().getOutputStream();
						if (null != os && null != byte_content) {
							os.write(byte_content);
						} else {
							Log.d(TAG, "sendMsg2getWaterData)--------null != os && null != byte_content");
						}
						/* 释放byte数组*/
						byte_header = null;
						byte_end = null;
						byte_content = null;

						/* 请求超时监控*/
						int iCount = 0;
						while (iCount++ < mAskCount) {
							if (getRequesting()) {
								Thread.sleep(mAskDuaration);
							} else {
								break;
							}
						}
						Log.d(TAG, "sendMsg2getWaterData)-----------iCount==" + iCount);
						if (iCount >= mAskCount) {/* 发送失败*/
							setRequesting(false);
							mSetCupParaCallback.setCupPara_NO(type_waterProject_1);
						}
					} catch (IOException e) {
						Log.d(TAG, "sendMsg2getWaterData)-----------IOException e==" + e);
						e.printStackTrace();
						setRequesting(false);
					} catch (Exception e) {
						Log.d(TAG, "sendMsg2getWaterData)-----------Exception e==" + e);
						setRequesting(false);
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

	/**
	 * $S1CTL APP向Cup发送控制命令 格式：n$S1CTL,<1>*<2><CR><LF> <1>控制命令字：’a’---‘z’，
	 * 例如：a.控制Cup进行深度睡眠模式 b.振动 Cup接收到命令后，返回n$S1CCL,<1>*<2><CR><LF>
	 * 
	 */
	public void sendMsg2ControlCup(final IControlCupCallback controlCupCallback, final String controlAlpha) {
		Log.d(TAG, "sendMsg2ControlCup)------controlAlpha==" + controlAlpha);
		if (isUpdate) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mLock_sendBluetooth) {
					try {
						while (getRequesting()) {
							Thread.sleep(mAskDuaration);
						}
						Log.d(TAG, "sendMsg2ControlCup)--enter----controlAlpha==" + controlAlpha);
						mControlCupCallback = controlCupCallback;

						setRequesting(true);

						byte[] byte_header = (7 + BluetoothType.controlCup + controlAlpha + "*").getBytes();
						byte byte_r = 13;
						byte byte_nr = 10;
						byte[] byte_content = new byte[byte_header.length + 2];
						for (int i = 0; i < byte_header.length; i++) {
							byte_content[i] = byte_header[i];
						}
						byte_content[byte_header.length] = byte_r;
						byte_content[byte_header.length + 1] = byte_nr;

						OutputStream os = BluetoothConnectUtils.getInstance().getSocket().getOutputStream();
						if (null != os && null != byte_content) {
							os.write(byte_content);
						} else {
							Log.d(TAG, "sendMsg2ControlCup)--------null != os && null != byte_content" + "  controlAlpha==" + controlAlpha);
						}

						/* 释放byte数组*/
						byte_header = null;
						byte_content = null;

						/* 请求超时监控*/
						int iCount = 0;
						while (iCount++ < mAskCount) {
							if (getRequesting()) {
								Thread.sleep(mAskDuaration);
							} else {
								break;
							}
						}
						Log.d(TAG, "sendMsg2ControlCup)-----------iCount==" + iCount);
						if (iCount >= mAskCount) {/* 发送失败*/
							setRequesting(false);
							if (null != controlCupCallback) {
								controlCupCallback.controlCup_NO(controlAlpha);
							}
						}

					} catch (IOException e) {
						Log.d(TAG, "sendMsg2ControlCup)-----------IOException e==" + e);
						e.printStackTrace();
						setRequesting(false);
						if (null != controlCupCallback) {
							controlCupCallback.controlCup_NO(controlAlpha);
						}
					} catch (Exception e) {
						Log.d(TAG, "sendMsg2ControlCup)-----------Exception e==" + e);
						setRequesting(false);
						if (null != controlCupCallback) {
							controlCupCallback.controlCup_NO(controlAlpha);
						}
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

	/**
	 * 往Cup的LED屏上发送内容 格式：n$S1LSD,<n>,<N>,<1>,<2>,<text>*<CR><LF> /r/n
	 * <n>:正前的LSD包index，数据类型char，从0x00开始计数;
	 * <N>:LSD包总数为N，数据类型char，例如一共有3帧，<N>的值等于0x03； <1>显示类型：0x00：文字和表情，0x01：动画
	 * 数据类型char <2>显示效果/动画帧数： 0x01：静态显示；0x02：左向跑马灯显示;
	 * Cup接收到命令后，返回n$S1CRS,<1>*<2><CR><LF>
	 * 
	 * @param msg
	 */
	public void sendMsg2LED(final ChatMsgEntity msg, int tag) {
		Log.d(TAG, "sendMsg2LED)-----------" + tag);
		if (isUpdate) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mLock_sendBluetooth) {
					try {
						while (getRequesting()) {
							Thread.sleep(mAskDuaration);
						}
						Log.d(TAG, "sendMsg2LED)--------enter----msg==" + msg.getText());
						setRequesting(true);
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						OutputStream os = BluetoothConnectUtils.getInstance().getSocket().getOutputStream();
						writeOneDate2LED(msg, os, 1, 0);

						/* 请求超时监控*/
						int iCount = 0;
						while (iCount++ < mAskCount) {
							if (getRequesting()) {
								Thread.sleep(mAskDuaration);
							} else {
								break;
							}
						}
						Log.d(TAG, "sendMsg2LED)-----------iCount==" + iCount);
						if (iCount >= mAskCount) {/* 发送失败*/
							setRequesting(false);
						}
					} catch (IOException e) {
						Log.d(TAG, "sendMsg2LED)-----------IOException e==" + e);
						e.printStackTrace();
						setRequesting(false);
					} catch (Exception e) {
						Log.d(TAG, "sendMsg2LED)-----------InterruptedException e==" + e);
						e.printStackTrace();
						setRequesting(false);
					}
				}
			}
		}).start();

	}

	/**
	 * 发送一个数据包到cupLED
	 * 
	 * @param msg
	 * @param os
	 * @param count
	 * @param index
	 * @throws IOException
	 */
	private void writeOneDate2LED(ChatMsgEntity msg, OutputStream os, int count, int index) throws IOException {
		int type = 0;
		if (null != msg) {
			int t = msg.getType();
			switch (t) {
			case ChatMsgEntity.TYPE_TXT:
			case ChatMsgEntity.TYPE_SCRAWL:
				type = 0;
				break;
			case ChatMsgEntity.TYPE_ANIM_FACE:
			case ChatMsgEntity.TYPE_SCRAWL_ANIM:
				type = 1;
				break;
			default:
				break;
			}
		}
		byte[] byte_head = (8 + BluetoothType.send2LED).getBytes();
		byte byte_index = DataSwitch.int2Bytes(index)[0];
		byte[] byte_comma = ",".getBytes();
		byte byte_count = DataSwitch.int2Bytes(count)[0];

		byte byte_type = DataSwitch.int2Bytes(type)[0];
		byte[] byte_end = new byte[] { "*".getBytes()[0], DataSwitch.int2Bytes(13)[0], DataSwitch.int2Bytes(10)[0] };

		byte[] byte_text = getBytes_text(msg);
		for (int i = 0; i < byte_text.length; i++) {
			Log.d(TAG, "-------------9762311111-------------------" + String.format("%02x", byte_text[i]));

		}
		Log.d(TAG, "-------------9762311111-------------------=-==" + byte_text.length);
		byte byte_displaymethod = DataSwitch.int2Bytes(2)[0];/* 跑马灯显示*/
		Log.d(TAG, "writeOneDate2LED-------------type == " + type);
		if (type == 1) {/* 动画*/
			Log.d(TAG, "writeOneDate2LED-------------type == 1");
			byte_displaymethod = DataSwitch.int2Bytes(4)[0];
		}
		byte[] byte_content = null;
		if (null != byte_text) {
			Log.d(TAG, "writeOneDate2LED----------byte_text.length==" + byte_text.length);
			if (byte_text.length > 18 && type == 0) {
				byte_displaymethod = DataSwitch.int2Bytes(2)[0];
			}
			byte_content = new byte[byte_head.length + 4 * byte_comma.length + byte_text.length + byte_end.length + 4];
		} else {
			byte_content = new byte[byte_head.length + 4 * byte_comma.length + byte_end.length + 4];
			return;
		}

		for (int i = 0; i < byte_head.length; i++) {
			byte_content[i] = byte_head[i];
		}
		byte_content[byte_head.length] = byte_index;
		for (int i = 0; i < byte_comma.length; i++) {
			byte_content[byte_head.length + 1 + i] = byte_comma[i];
		}
		byte_content[byte_head.length + 1 + byte_comma.length] = byte_count;
		for (int i = 0; i < byte_comma.length; i++) {
			byte_content[byte_head.length + 2 + byte_comma.length + i] = byte_comma[i];
		}
		byte_content[byte_head.length + 2 + 2 * byte_comma.length] = byte_type;
		for (int i = 0; i < byte_comma.length; i++) {
			byte_content[byte_head.length + 3 + 2 * byte_comma.length + i] = byte_comma[i];
		}
		byte_content[byte_head.length + 3 + 3 * byte_comma.length] = byte_displaymethod;
		for (int i = 0; i < byte_comma.length; i++) {
			byte_content[byte_head.length + 4 + 3 * byte_comma.length + i] = byte_comma[i];
		}
		for (int i = 0; i < byte_text.length; i++) {
			byte_content[byte_head.length + 4 + 4 * byte_comma.length + i] = byte_text[i];
		}
		for (int i = 0; i < byte_end.length; i++) {
			byte_content[byte_head.length + 4 + 4 * byte_comma.length + byte_text.length + i] = byte_end[i];
			Log.d(TAG, "sendMsg2LED)---------- byte_end.length==" + String.format("%02x", byte_end[i]));
		}

		if (null != os && null != byte_content) {
			for (int i = 0; i < byte_content.length; i++) {
				Log.d(TAG, "-------------976231-22222------------------" + String.format("%02x", byte_content[i]));

			}
			Log.d(TAG, "sendMsg2LED)--------null != os && null != byte_content = " + byte_content.length);
			
			os.write(byte_content);
		}
		/* 释放byte数组*/
		byte_head = null;
		byte_comma = null;
		byte_text = null;
		byte_end = null;
		byte_content = null;
	}

	/**
	 * 将消息转为字节
	 * 
	 * @param msg
	 * @return
	 */
	private byte[] getBytes_text(ChatMsgEntity msg) {
		MapingUtil mMapingUtil = MapingUtil.getInstance();
		byte[] byte_text = null;
		if (msg.getType() == ChatMsgEntity.TYPE_TXT) {/* 文字*/
			String text = msg.getText();
			String upperText = text.toUpperCase(Locale.getDefault());
			Log.d(TAG, "writeOneDate2LED----text  msg.getText()==" + upperText);
			ArrayList<FaceBytes> list_faceBytes = mMapingUtil.getBytesFace(text);
			int size_list_faceBytes = list_faceBytes.size();
			Log.d(TAG, "writeOneDate2LED-  size_list_faceBytes==" + size_list_faceBytes);
			byte[] byte_temp = new byte[maxBytes];
			int k = 0;

			int index_face = 0;
			Log.d(TAG, "writeOneDate2LED--upperText.length()==" + text.length());
			for (int i = 0; i < text.length(); i++) {
				Log.d(TAG, "writeOneDate2LED--  i==" + i);
				if (index_face < size_list_faceBytes && i == list_faceBytes.get(index_face).getStart()) {
					if (k+list_faceBytes.get(index_face).getBytes_face().length>=maxBytes) {
						break;
					}
					for (int j = 0; j < list_faceBytes.get(index_face).getBytes_face().length; j++) {
						/**	if (k >= maxBytes) {
							return null;
						}**/	
						byte_temp[k++] = list_faceBytes.get(index_face).getBytes_face()[j];
					}
					i = list_faceBytes.get(index_face).getEnd() - 1;
					Log.d(TAG, "writeOneDate2LED--end  i==" + i);
					index_face++;
				} else {
					Log.d(TAG, "writeOneDate2LED--  i==" + i + "  upperText.charAt(i)==" + ("" + upperText.charAt(i)));

					byte[] b = mMapingUtil.map_library.get(upperText.charAt(i));
					if (null != b) {
						if (k+b.length>=maxBytes) {
							break;
						}
						for (int j = 0; j < b.length; j++) {
							/**	if (k >= maxBytes) {
								return null;
							}**/
							byte_temp[k++] = b[j];
							Log.d(TAG, "writeOneDate2LED-------------------" + String.format("%02x", b[j]));
						}
					}
				}

			}

			byte_text = new byte[k];
			for (int i = 0; i < k; i++) {
				byte_text[i] = byte_temp[i];
			}
			byte_temp = null;
		} else if (msg.getType() == ChatMsgEntity.TYPE_SCRAWL) {/* 涂鸦*/
			Log.d(TAG, "writeOneDate2LED----scrawl  msg.getText()==" + msg.getText());
			int[][] int_text = new int[18][8];
			/* 将收到的涂鸦字符串转成18*8的二位整型数组*/
			String str_content = msg.getText();
			if (null != str_content) {
				char[] char_content = str_content.toCharArray();
				int j = 0;
				int k = 0;
				for (int i = 0; i < char_content.length; i++) {
					int_text[j][k] = Integer.parseInt("" + char_content[i]);
					if ((i + 1) % 8 == 0) {
						j++;
						k = 0;
					} else {
						k++;
					}
				}
			}

			byte_text = new byte[18];
			for (int i = 0; i < byte_text.length; i++) {
				int n = 0;
				for (int j = 0; j < 8; j++) {
					n += int_text[i][j] * ((int) Math.pow(2, (7 - j)));
				}
				byte_text[i] = DataSwitch.int2Bytes(n)[0];
			}
			int_text = null;
		} else if (msg.getType() == ChatMsgEntity.TYPE_ANIM_FACE) {/* 动画表情*/
			Log.d(TAG, "writeOneDate2LED---anim face   msg.getText()==" + msg.getText());
			byte_text = mMapingUtil.map_Face.get(msg.getText());
		} else if (msg.getType() == ChatMsgEntity.TYPE_SCRAWL_ANIM) {/* 动画涂鸦*/
			String str = msg.getText().toString();
			Log.d(TAG, "writeOneDate2LED---anim srawl  " + str.length());
			/* String[] str_contents = msg.getText().split("-");*/
			int page = 1 + (str.length() - 1) / 144;
			Log.d(TAG, "writeOneDate2LED---anim srawl  page==" + page);
			String[] str_contents = new String[page];
			for (int i = 0; i < page; i++) {
				if (str.length() >= 144 + i * 144) {
					str_contents[i] = str.substring(i * 144, 144 + i * 144);
				} else {
					str_contents[i] = str.substring(i * 144, str.length());
				}
			}

			byte_text = new byte[18 * str_contents.length];
			Log.d(TAG, "writeOneDate2LED---anim srawl  str_contents.length==" + str_contents.length);
			for (int x = 0; x < str_contents.length; x++) {
				String str_content = str_contents[x];

				Log.d(TAG, "writeOneDate2LED---anim srawl  x==" + x + "   str_content==" + str_content);
				int[][] int_text = new int[18][8];
				if (null != str_content) {
					char[] char_content = str_content.toCharArray();
					Log.d(TAG, "writeOneDate2LED-char_content.length==" + char_content.length);
					int j = 0;
					int k = 0;
					for (int i = 0; i < char_content.length; i++) {
						int_text[j][k] = Integer.parseInt("" + char_content[i]);
						if ((i + 1) % 8 == 0) {
							j++;
							k = 0;
						} else {
							k++;
						}
					}
				}

				for (int i = 0; i < 18; i++) {
					int n = 0;
					for (int j = 0; j < 8; j++) {
						n += int_text[i][j] * ((int) Math.pow(2, (7 - j)));
					}
					byte_text[i + 18 * x] = DataSwitch.int2Bytes(n)[0];
				}
				int_text = null;
			}
		}
		return byte_text;
	}

	/**
	 * $S1UDA: Cup 响应GDA命令，发送用户喝水提醒闹钟数据给APP 格式：n$S1UDA,<n>,<T>,……*<CR><LF>
	 * <c>:当前包号 <T>:总包数 ...... ：闹钟内容 <CR><LF>:包结束标识
	 * 如果没有闹钟数据，则发送的数据格式为：n$S1UDA,*<CR><LF>
	 * 
	 */
	public void sendMsg2SetRemind(final List<Time_show> remindTime, final ISetRemindDataCallback SetRemindDataCallback) {

		Log.d(TAG, "sendMsg2SetRemind)------");
		if (isUpdate) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mLock_sendBluetooth) {
					try {
						while (getRequesting()) {
							Thread.sleep(mAskDuaration);
						}
						Log.d(TAG, "sendMsg2SetRemind)----enter--");
						mSetRemindDataCallback = SetRemindDataCallback;
						setRequesting(true);

						byte[] byte_header = (9 + BluetoothType.send2SetRemind).getBytes();
						byte byte_index = DataSwitch.int2Bytes(0)[0];
						byte[] byte_comma = ",".getBytes();
						byte byte_count = DataSwitch.int2Bytes(1)[0];
						byte[] byte_end = new byte[] { "*".getBytes()[0], DataSwitch.int2Bytes(13)[0], DataSwitch.int2Bytes(10)[0] };
						int k = 0;
						byte[] byte_remind = new byte[1024];
						for (int i = 0; i < remindTime.size(); i++) {
							String[] strtime = remindTime.get(i).getTime().split(":");
							int time = Integer.parseInt(strtime[0].trim()) * 60 + Integer.parseInt(strtime[1].trim());
							int flag = remindTime.get(i).isFlag();
							byte_remind[k++] = DataSwitch.int2Bytes(time)[0];
							byte_remind[k++] = DataSwitch.int2Bytes(time)[1];
							byte_remind[k++] = DataSwitch.int2Bytes(flag)[0];
						}

						byte[] byte_content = new byte[byte_header.length + byte_comma.length * 2 + 2 + k + byte_end.length];
						for (int i = 0; i < byte_header.length; i++) {
							byte_content[i] = byte_header[i];
						}
						byte_content[byte_header.length] = byte_index;
						for (int i = 0; i < byte_comma.length; i++) {
							byte_content[byte_header.length + 1 + i] = byte_comma[i];
						}
						byte_content[byte_header.length + 1 + byte_comma.length] = byte_count;
						for (int i = 0; i < byte_comma.length; i++) {
							byte_content[byte_header.length + 2 + byte_comma.length + i] = byte_comma[i];
						}
						for (int i = 0; i < k; i++) {
							byte_content[byte_header.length + 2 + byte_comma.length * 2 + i] = byte_remind[i];
						}
						for (int i = 0; i < byte_end.length; i++) {
							byte_content[byte_header.length + 2 + byte_comma.length * 2 + k + i] = byte_end[i];
						}

						OutputStream os = BluetoothConnectUtils.getInstance().getSocket().getOutputStream();
						if (null != os && null != byte_content) {
							os.write(byte_content);
						} else {
							Log.d(TAG, "sendMsg2SetRemind)--------null != os && null != byte_content");
						}

						/* 释放byte数组*/
						byte_header = null;
						byte_comma = null;
						byte_remind = null;
						byte_end = null;
						byte_content = null;

						/* 请求超时监控*/
						int iCount = 0;
						while (iCount++ < mAskCount) {
							if (getRequesting()) {
								Thread.sleep(mAskDuaration);
							} else {
								break;
							}
						}
						Log.d(TAG, "sendMsg2SetRemind)-----------iCount==" + iCount);
						if (iCount >= mAskCount) {/* 发送失败*/
							setRequesting(false);
							SetRemindDataCallback.setRemindData_NO();
						}
					} catch (IOException e) {
						SetRemindDataCallback.setRemindData_NO();
						Log.d(TAG, "sendMsg2SetRemind)-----------IOException e==" + e);
						e.printStackTrace();
						setRequesting(false);
					} catch (Exception e) {
						Log.d(TAG, "sendMsg2SetRemind)-----------Exception e==" + e);
						setRequesting(false);
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * $S1OKO: APP收到cup 发送的 AHA指令后，响应Cup 格式：n$S1OKO,*<CR><LF>
	 * 
	 * @param socket
	 */
	public void sendMsg2ReplyCupLife(BluetoothSocket socket) {
		Log.d(TAG, "sendMsg2ReplyCupLife)------isRequesting==" + getRequesting() + "  isUpdate==" + isUpdate);
	
		if (getRequesting()) {
			return;
		}
		if (isUpdate) {
			return;
		}
		synchronized (mLock_sendBluetooth) {
			try {
				if (getRequesting()) {
					return;
				}
				Log.d(TAG, "sendMsg2ReplyCupLife)--enter----==");
				setRequesting(true);

				byte[] byte_header = (0 + BluetoothType.reply_cupLife).getBytes();
				byte[] byte_end = new byte[] { "*".getBytes()[0], DataSwitch.int2Bytes(13)[0], DataSwitch.int2Bytes(10)[0] };
				byte[] byte_content = new byte[byte_header.length + byte_end.length];
				for (int i = 0; i < byte_header.length; i++) {
					byte_content[i] = byte_header[i];
				}
				for (int i = 0; i < byte_end.length; i++) {
					byte_content[byte_header.length + i] = byte_end[i];
				}

				OutputStream os = socket.getOutputStream();
				if (null != os && null != byte_content) {
					os.write(byte_content);
				} else {
					Log.d(TAG, "sendMsg2ReplyCupLife)--------null != os && null != byte_content");
				}
				/* 释放byte数组*/
				byte_header = null;
				byte_end = null;
				byte_content = null;

				Thread.sleep(500);
				setRequesting(false);
			} catch (Exception e) {
				Log.d(TAG, "sendMsg2ReplyCupLife)-----------IOException e==" + e);
				e.printStackTrace();
				setRequesting(false);
			}
		}
	}

	/**
	 * 发送升级包到杯子
	 * 
	 * @param receive_data
	 */
	public void SendUpdateFile2Cup(final PakageCallback pakageCallback, final byte[] receive_data, final int i) {
		Log.d(TAG, "SendUpdateFile2Cup)------i = "+ i );
		new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (mLock_sendBluetooth) {
					try {
						while (getRequesting()) {
							Thread.sleep(mAskDuaration);
						}
						Log.d(TAG, "SendUpdateFile2Cup)--enter----"+ String.format("%02x", receive_data[0])+"    =="+ String.format("%02x", receive_data[1]));
						setRequesting(true);
						
						mPakageCallback = pakageCallback;
						
						OutputStream os = BluetoothConnectUtils.getInstance().getSocket().getOutputStream();
						if (null != os && null != receive_data) {
							os.write(receive_data);
						} else {
							Log.d(TAG, "SendUpdateFile2Cup)-null == os && null == receive_data");
							setRequesting(false);
						}
						
						/* 请求超时监控*/
						int iCount = 0;
						Log.d(TAG, "----------SendUpdateFile2Cup--555---mInterruptUPGRADE = " + UpdateFirmWare.mInterruptUPGRADE);
						while (iCount++ < mAskCount) {
							if(UpdateFirmWare.mInterruptUPGRADE){
								Log.e(TAG, "----------SendUpdateFile2Cup--222---mInterruptUPGRADE = " + UpdateFirmWare.mInterruptUPGRADE);
								iCount = mAskCount + 1;
							}
							if (getRequesting()) {
								Thread.sleep(mAskDuaration);
							} else {
								break;
							}
						}
						Log.d(TAG, "------------333---mInterruptUPGRADE = " + UpdateFirmWare.mInterruptUPGRADE);
						if(UpdateFirmWare.mInterruptUPGRADE){
							iCount = mAskCount + 1;
						}
						Log.d(TAG, "SendUpdateFile2Cup)-----------iCount==" + iCount + ":::" + i);
						if (iCount >= mAskCount) {/* 发送失败*/
							setRequesting(false);
							mPakageCallback.Pakage_NO();
						}
					} catch (Exception e) {
						Log.d(TAG, "SendUpdateFile2Cup)---Exception---"+e);
						setRequesting(false);
						mPakageCallback.Pakage_NO();
					}
				}
			}
		}).start();

	}

	/**
	 * 发送OTA联机命令
	 * 
	 * @param command
	 */
	public void sendupdate_command(final SendOnlineCipherCallback sendOnlineCipherCallback, final byte[] command) {
		Log.d(TAG, "sendupdate_command)-------------");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				synchronized (mLock_sendBluetooth) {
					try {
						while (getRequesting()) {
							Thread.sleep(mAskDuaration);
						}
						Log.d(TAG, "sendupdate_command)----enter---------"+command[0]);
						setRequesting(true);
						isUpdate = true;
						
						mSendOnlineCipherCallback = sendOnlineCipherCallback;
						
						byte[] update_command = command;
						OutputStream os = BluetoothConnectUtils.getInstance().getSocket().getOutputStream();
						if (os != null && update_command != null) {
							os.write(update_command);
							/**	if (Arrays.equals(command, BluetoothPakageType.EOT)) {
								isUpdate = false;
								setRequesting(false);
							} else {
								setRequesting(false);
							}**/
						} else {
							setRequesting(false);
						}
						
						/* 请求超时监控*/
						int iCount = 0;
						while (iCount++ < 10000) {
							if (getRequesting()) {
								Thread.sleep(mAskDuaration);
							} else {
								break;
							}
						}
						Log.d(TAG, "sendupdate_command)-----------iCount==" + iCount);
						if (iCount >= 10000) {/* 发送失败*/
							isUpdate = false;
							setRequesting(false);
							mPakageCallback.Pakage_NO();
						}

					} catch (Exception e) {
						Log.d(TAG, "sendupdate_command)-----------Exception==" + e);
						isUpdate = false;
						isUpdate = false;
						setRequesting(false);
						mSendOnlineCipherCallback.onlineCipher_NO();
					}
				}
			}
		}).start();
	}

	/** $S1CUM: APK/APP向杯子发送用户的心情
	 	格式：n$S1CUM,<c>,<T>,……*<CR><LF>
	 	其中：<c>：表示当前包号
	 	<T>：总包数
	……：用户的心情语**/
	public void sendMood2Cup(final String mood) {
		Log.d(TAG, "sendMood2Cup)------------");
		if (isUpdate) {
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (mLock_sendBluetooth) {
					try {
						while (getRequesting()) {
							Thread.sleep(mAskDuaration);
						}
						Log.d(TAG, "sendMood2Cup)---enter---------");
						setRequesting(true);
						
						byte byte_n = 10;
						byte[] byte_header = (BluetoothType.send_mood2cup).getBytes();
						byte byte_index = 0;
						byte byte_count = 1;
						byte[] byte_comma = ",".getBytes();
						byte[] byte_text = mood.getBytes();
						byte[] byte_end = new byte[] { "*".getBytes()[0], 13, 10 };
						byte[] byte_content = new byte[byte_header.length + 2 * byte_comma.length + byte_text.length + byte_end.length + 3];
						byte_content[0] = byte_n;
						for (int i = 0; i < byte_header.length; i++) {
							byte_content[i + 1] = byte_header[i];
						}
						byte_content[1 + byte_header.length] = byte_index;
						for (int j = 0; j < byte_comma.length; j++) {
							byte_content[2 + byte_header.length + j] = byte_comma[j];
						}
						byte_content[byte_header.length + byte_comma.length + 2] = byte_count;
						for (int j = 0; j < byte_comma.length; j++) {
							byte_content[3 + byte_header.length + byte_comma.length + j] = byte_comma[j];
						}
						for (int i = 0; i < byte_text.length; i++) {
							byte_content[3 + byte_header.length + 2 * byte_comma.length + i] = byte_text[i];
						}
						for (int i = 0; i < byte_end.length; i++) {
							byte_content[3 + byte_header.length + 2 * byte_comma.length + byte_text.length + i] = byte_end[i];
						}

						OutputStream os = BluetoothConnectUtils.getInstance().getSocket().getOutputStream();
						if (null != os && null != byte_content) {
							os.write(byte_content);
						}
						/* 释放byte数组*/
						byte_header = null;
						byte_comma = null;
						byte_text = null;
						byte_end = null;
						byte_content = null;
						
						/* 请求超时监控*/
						int iCount = 0;
						while (iCount++ < mAskCount) {
							if (getRequesting()) {
								Thread.sleep(mAskDuaration);
							} else {
								break;
							}
						}
						Log.d(TAG, "sendMood2Cup)-----------iCount==" + iCount);
						if (iCount >= mAskCount) {/* 发送失败*/
							setRequesting(false);
						}
					} catch (IOException e) {
						Log.d(TAG, "sendMood2Cup)-----------IOException e==" + e);
						e.printStackTrace();
						setRequesting(false);
					} catch (Exception e) {
						Log.d(TAG, "sendMood2Cup)-----------Exception e==" + e);
						setRequesting(false);
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

	/** $S1GDA: APP 向cup 发起获取用户喝水提醒闹钟的请求
		格式：n$S1GDA,*<CR><LF>
	**/
	public void sendMsg2GetCupRemind(final IGetRemindDataCallback callback) {
		Log.d(TAG, "sendMsg2GetCupRemind)------------");
		if (isUpdate) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mLock_sendBluetooth) {
					try {
						while (getRequesting()) {
							Thread.sleep(mAskDuaration);
						}
						Log.d(TAG, "sendMsg2GetCupRemind)---enter---------");
						setRequesting(true);
						mGetRemindDataCallback = callback;
						
						byte byte_n = 11;
						byte[] byte_header = (BluetoothType.send2getCupRemind).getBytes();
						byte[] byte_end = new byte[] { "*".getBytes()[0], 13, 10 };
						byte[] byte_content = new byte[byte_header.length + byte_end.length + 1];
						byte_content[0] = byte_n;
						for (int i = 0; i < byte_header.length; i++) {
							byte_content[i + 1] = byte_header[i];
						}
						for (int j = 0; j < byte_end.length; j++) {
							byte_content[1 + byte_header.length + j] = byte_end[j];
						}
						
						OutputStream os = BluetoothConnectUtils.getInstance().getSocket().getOutputStream();
						if (null != os && null != byte_content) {
							os.write(byte_content);
						}
						/* 释放byte数组*/
						byte_header = null;
						byte_end = null;
						byte_content = null;
						
						/* 请求超时监控*/
						int iCount = 0;
						while (iCount++ < mAskCount) {
							if (getRequesting()) {
								Thread.sleep(mAskDuaration);
							} else {
								break;
							}
						}
						Log.d(TAG, "sendMsg2GetCupRemind)-----------iCount==" + iCount);
						if (iCount >= mAskCount) {/* 发送失败*/
							setRequesting(false);
							callback.getRemindData_NO();
						}
					} catch (IOException e) {
						Log.d(TAG, "sendMsg2GetCupRemind)-----------IOException e==" + e);
						e.printStackTrace();
						setRequesting(false);
					} catch (Exception e) {
						Log.d(TAG, "sendMsg2GetCupRemind)-----------Exception e==" + e);
						setRequesting(false);
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

	/**
	 * $S1TEA: APP 向杯子发送用户自定义茶的泡茶时长 格式：n$S1TEA,……*<CR><LF> 其中：…… 为两字节长的泡茶时长 Cup
	 * 收到该命令后，返回n$S1CRS,*<CR><LF>给APK
	 * 
	 * @param duration
	 */
	public void sendMsg2SetTeaing(final int duration) {
		Log.d(TAG, "sendMsg2SetTeaing)------------");
		if (isUpdate) {
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (mLock_sendBluetooth) {
					try {
						while (getRequesting()) {
							Thread.sleep(mAskDuaration);
						}
						Log.d(TAG, "sendMsg2SetTeaing)----enter--------duration=="+duration);
						setRequesting(true);

						byte byte_n = 12;
						byte[] byte_header = (BluetoothType.send2SetTeaing).getBytes();
						byte[] byte_dur = DataSwitch.int2Bytes(duration);
						byte[] byte_end = new byte[] { "*".getBytes()[0], 13, 10 };
						byte[] byte_content = new byte[byte_header.length + byte_dur.length + byte_end.length + 1];
						byte_content[0] = byte_n;
						for (int i = 0; i < byte_header.length; i++) {
							byte_content[i + 1] = byte_header[i];
						}
						for (int j = 0; j < byte_dur.length; j++) {
							byte_content[1 + byte_header.length + j] = byte_dur[j];
						}
						for (int j = 0; j < byte_end.length; j++) {
							byte_content[1 + byte_dur.length + byte_header.length + j] = byte_end[j];
						}

						OutputStream os = BluetoothConnectUtils.getInstance().getSocket().getOutputStream();
						if (null != os && null != byte_content) {
							os.write(byte_content);
						}
						/* 释放byte数组*/
						byte_header = null;
						byte_dur = null;
						byte_end = null;
						byte_content = null;

						/* 请求超时监控*/
						int iCount = 0;
						while (iCount++ < mAskCount) {
							if (getRequesting()) {
								Thread.sleep(mAskDuaration);
							} else {
								break;
							}
						}
						Log.d(TAG, "sendMsg2SetTeaing)-----------iCount==" + iCount);
						if (iCount >= mAskCount) {/* 发送失败*/
							setRequesting(false);
						}
					} catch (IOException e) {
						Log.d(TAG, "sendMsg2SetTeaing)-----------IOException e==" + e);
						e.printStackTrace();
						setRequesting(false);
					} catch (Exception e) {
						Log.d(TAG, "sendMsg2SetTeaing)-----------Exception e==" + e);
						setRequesting(false);
						e.printStackTrace();
					}
				}
			}
		}).start();

	}

	/**
	 * 设定teao的泡茶时间设定，具体格式如下： //n$S1CTL,l,m*\r\n /*m两个字节，第一个字节传Tea的种类id.如：0x01;
	 * //第二个字节传泡茶的时间系数,范围是：10---90;下传时转换成16制：0x0a---0x5A
	 * 
	 * @param time
	 * @param teaCode
	 */
	public void sendMsg2changeTea(final ISetTeaPercentCallback callback, final int time, final int teaCode) {
		Log.d(TAG, "sendMsg2changeTea)------------");
		if (isUpdate) {
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (mLock_sendBluetooth) {
					try {
						while (getRequesting()) {
							Thread.sleep(mAskDuaration);
						}
						Log.d(TAG, "sendMsg2changeTea)----enter--------time==" + time + "   teaCode== " + teaCode);
						mSetTeaPercentCallback = callback;
						setRequesting(true);
						
						byte byte_n = 13;
						byte[] byte_header = (BluetoothType.controlCup + BluetoothType.control_tea + ",").getBytes();
						byte byte_teacode = DataSwitch.int2Bytes(teaCode)[0];
						byte byte_time = DataSwitch.int2Bytes(time)[0];
						byte[] byte_end = new byte[] { "*".getBytes()[0], 13, 10 };
						byte[] byte_content = new byte[byte_header.length + byte_end.length + 3];
						byte_content[0] = byte_n;
						for (int i = 0; i < byte_header.length; i++) {
							byte_content[i + 1] = byte_header[i];
						}
						byte_content[1 + byte_header.length] = byte_teacode;
						byte_content[2 + byte_header.length] = byte_time;
						for (int j = 0; j < byte_end.length; j++) {
							byte_content[3 + byte_header.length + j] = byte_end[j];
						}
						
						OutputStream os = BluetoothConnectUtils.getInstance().getSocket().getOutputStream();
						if (null != os && null != byte_content) {
							os.write(byte_content);
						}
						/* 释放byte数组*/
						byte_header = null;
						byte_end = null;
						byte_content = null;
						
						/* 请求超时监控*/
						int iCount = 0;
						while (iCount++ < mAskCount) {
							if (getRequesting()) {
								Thread.sleep(mAskDuaration);
							} else {
								break;
							}
						}
						Log.d(TAG, "sendMsg2changeTea)-----------iCount==" + iCount);
						if (iCount >= mAskCount) {/* 发送失败*/
							setRequesting(false);
							callback.setTeaPercent_NO();
						}
					} catch (IOException e) {
						Log.d(TAG, "sendMsg2changeTea)-----------IOException e==" + e);
						e.printStackTrace();
						setRequesting(false);
						callback.setTeaPercent_NO();
					} catch (Exception e) {
						Log.d(TAG, "sendMsg2SetTeaing)-----------Exception e==" + e);
						setRequesting(false);
						callback.setTeaPercent_NO();
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}
