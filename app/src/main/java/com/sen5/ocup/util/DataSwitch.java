package com.sen5.ocup.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @version ：2015年1月28日 下午2:03:54
 * 
 *          类说明 :数据类型转换
 */
public class DataSwitch {

	private static final String TAG = "DataSwitch";
	//
	/**
	 * 字节到字符转换
	 * @param b
	 * @return
	 */
	public static char getChar(byte[] b, int index) {
		int s = 0;
		if (b[index + 1] > 0)
			s += b[index + 1];
		else
			s += 256 + b[index + 0];
		s *= 256;
		if (b[index + 0] > 0)
			s += b[index + 1];
		else
			s += 256 + b[index + 0];
		char ch = (char) s;
		return ch;
	}

	/**
	 * 将一个2个字节数组转换为intt。
	 * 
	 * @param b
	 * @return
	 */
	public static int bytesTwo2Int(byte[] b) {
		int a1 = b[0] & 0xff;
		int a2 = (b[1] & 0xff) * 256;
		return (a1 + a2);
	}

	/**
	 * 将一个4个字节数组转换为intt。
	 * 
	 * @param b
	 * @return
	 */
	public static int bytesFour2Int(byte[] b) {
		int a1 = b[0] & 0xff;
		int a2 = (b[1] & 0xff) * 256;
		int a3 = (b[2] & 0xff) * 65536;
		int a4 = (b[3] & 0xff) * 16777216;
		return (a1 + a2 + a3 + a4);
	}

	public static String ten2sixteen(long ten) {
		return Long.toHexString(ten);
	}

	public static byte[] long2Bytes(long num) {
		byte[] byteNum = new byte[8];
		for (int ix = 0; ix < 8; ++ix) {
			int offset = 64 - (ix + 1) * 8;
			byteNum[ix] = (byte) ((num >> offset) & 0xff);
		}
		return byteNum;
	}

	public static byte[] int2Bytes(int num) {
		byte[] byteNum = new byte[4];
		for (int ix = 0; ix < 4; ++ix) {
			int offset = 32 - (ix + 1) * 8;
			byteNum[ix] = (byte) ((num >> offset) & 0xff);
		}
		byte[] byte_result = new byte[] { byteNum[3], byteNum[2] };
//		for (int i=0; i<byteNum.length; i++){
//			Logger.e(TAG,"int2Bytes"+"byte"+i+"="+byteNum[i]);
//		}
		return byte_result;
	}

	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * ascii to string
	 * 
	 * @param value
	 * @return
	 */
	public static String asciiToString(String value) {
		StringBuffer sbu = new StringBuffer();
		String[] chars = value.split(",");
		for (int i = 0; i < chars.length; i++) {
			sbu.append((char) Integer.parseInt(chars[i]));
		}
		return sbu.toString();
	}

	/**
	 * string to ascii
	 * 
	 * @param value
	 * @return
	 */
	public static String stringToAscii(String value) {
		StringBuffer sbu = new StringBuffer();
		char[] chars = value.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (i != chars.length - 1) {
				sbu.append((int) chars[i]).append(",");
			} else {
				sbu.append((int) chars[i]);
			}
		}
		return sbu.toString();
	}

	public static byte[] getByte(File file) {
		byte[] bytes = null;
		if (file != null) {
			try {
				InputStream is = new FileInputStream(file);
				int length = (int) file.length();
				Log.d(TAG, "the file length is= " + length);
				if (length > Integer.MAX_VALUE) {
					System.out.println("this file is max");
					return null;
				}
				bytes = new byte[length];
				int offset = 0;
				int numRead = 0;
				while (offset == 0) {
					offset += numRead;
				}
				if (offset != length) {
					System.out.println("file length is error");
					return null;
				}
				is.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return bytes;
	}
}
