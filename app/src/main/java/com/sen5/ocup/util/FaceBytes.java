package com.sen5.ocup.util;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :表情转换成字节
 */
public class FaceBytes {
	/**
	 * 表情在字符串中开始的位置
	 */
	private int start;
	/**
	 * 表情在字符串中结束的位置
	 */
	private int end;
	/**
	 * 表情转换后的字节
	 */
	private byte[] bytes_face;

	public FaceBytes(int start, int end, byte[] bytes_face) {
		super();
		this.start = start;
		this.end = end;
		this.bytes_face = bytes_face;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public byte[] getBytes_face() {
		return bytes_face;
	}

	public void setBytes_face(byte[] bytes_face) {
		this.bytes_face = bytes_face;
	}

}
