package com.sen5.ocup.struct;

import android.bluetooth.BluetoothDevice;

public class Device {

	private String name;
	private String addr;
	private String rssi;
	private BluetoothDevice device;
	private boolean isconnect;
	
	
	public Device(BluetoothDevice device, String name, String addr,String rssi,  boolean isconnect) {
		super();
		this.device = device;
		this.name = name;
		this.addr = addr;
		this.rssi = rssi;
		this.isconnect = isconnect;
	}
	
	public BluetoothDevice getDevice() {
		return device;
	}

	public void setDevice(BluetoothDevice device) {
		this.device = device;
	}

	public String getRssi() {
		return rssi;
	}

	public void setRssi(String rssi) {
		this.rssi = rssi;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public boolean isIsconnect() {
		return isconnect;
	}
	public void setIsconnect(boolean isconnect) {
		this.isconnect = isconnect;
	}
	
	
}
