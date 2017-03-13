package com.sen5.ocup.blutoothstruct;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :蓝牙通信协议中定义的信息类别结构体
 */
public class BluetoothType {

	// 获取杯子参数
	public final static String getCupInfo = "$S1AGP,";
	public final static String receiveCupInfo = "$S1CRP,";

	// 获取杯子状态
	public final static String getCupStatus = "$S1AGS,";
	public final static String receiveCupStatus = "$S1CRC,";

	// 获取杯子ID
	public final static String getCupID = "$S1GID,";
	public final static String receiveCupID = "$S1SID,";

	// 设置杯子时间
	public final static String setCupTime = "$S1AST,";

	// 设置杯子参数
	public final static String setCupPara = "$S1ASP,";

	// 获取当天喝水记录
	public final static String getWaterDataDay = "$S1AGD,";
	public final static String receiveWaterDataDay = "$S1CRD,";

	// 向杯子发送控制命令
	public final static String controlCup = "$S1CTL,";
	public final static String controlCupResult = "$S1CCL,";
	
	// 向杯子led屏发送内容
		public final static String send2LED = "$S1LSD,";
		public final static String receive_ok = "$S1CRS,";
		
		//接收到杯子发的NFC消息
		public final static String receive_nfc = "$S1NFC,";
		
		//设置闹钟提醒喝水
		public final static String send2SetRemind= "$S1SDA,";
		
		//控制命令
		public final static String control_sleep = "a";//睡眠
		public final static String control_shake = "b";//振动
		public final static String control_ledr = "c";//led  红
		public final static String control_ledg = "d";///led  绿
		public final static String control_ledb = "e";//led 蓝
		public final static String control_gesture = "f";//进入手势
		public final static String control_correcttouch = "h";//	h.厂测水量传感器常温空杯校正功能
		public final static String control_teaok = "j";//	j. APP 告诉cup 茶已ok
		public final static String control_recovery = "k";//恢复出厂设置
		public final static String control_tea = "l";//修改茶的时间

		//联机密码
		public final static String updatefirmware ="OTA";

		
		//0$S1AHA,cup心跳
		public final static String receive_cupLife = "$S1AHA,";
		public final static String reply_cupLife = "$S1OKO,";
		
		//$S1PAS,接收到杯子通行证
		public final static String receive_cupPass = "$S1PAS,";
		
		//发送心情到杯子
		public final static String send_mood2cup = "$S1CUM,";
		
		//获取杯子闹钟
		public final static String send2getCupRemind = "$S1GDA,";
		public final static String recieverCupRemind = "$S1UDA,";//$S1UDA: Cup 响应GDA命令，发送用户喝水提醒闹钟数据给APP
		
		//设置泡茶时间给杯子
		public final static String send2SetTeaing = "$S1TEA,";	
		
		//自定义泡茶时间给杯子
		public final static String send2ChangeTea = "$S1TEA,";
}
