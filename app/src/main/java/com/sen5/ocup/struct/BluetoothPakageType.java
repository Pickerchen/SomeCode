package com.sen5.ocup.struct;

public class BluetoothPakageType {

	//确认OK
	public static final byte[]  ACK = {6};	
	//错误，重传
	public static final byte[]  NAK = {21};
	//传输结束
	public static final byte[]  EOT = {4};
	//强制终止
	public static final byte[]  CAN = {24};
	
}
