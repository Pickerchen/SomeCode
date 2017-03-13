package com.sen5.ocup.blutoothstruct;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :蓝牙通信协议中定义的Gsensor结构体
 */
public class GsensorData {	
	/**
	 * 创建单例
	 */
	private static GsensorData mInstance = null;
	int x;
	int y;
	int z;
	
	public static GsensorData getInstance() {
		if (mInstance == null) {
			mInstance = new GsensorData();
		}
		return mInstance;
	}

	private GsensorData() {
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}
	
}
