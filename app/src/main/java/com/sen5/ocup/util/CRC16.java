package com.sen5.ocup.util;

/**
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :获取crc校验码
 */
public class CRC16 {

	 private short[] crcTable = new short[256];
	    private int gPloy = 0x1021; // 生成多项式
	    public CRC16() {
	        computeCrcTable();
	    }
	    private short getCrcOfByte(int aByte) {
	        int value = aByte << 8;
	        for (int count = 7; count >= 0; count--) {
	            if ((value & 0x8000) != 0) { // 高第16位为1，可以按位异或
	                value = (value << 1) ^ gPloy;
	            } else {
	                value = value << 1; // 首位为0，左移
	            }
	        }
	        value = value & 0xFFFF; // 取低16位的值
	        return (short)value;
	    }
	    /*
	     * 生成0 - 255对应的CRC16校验码
	     */
	    private void computeCrcTable() {
	        for (int i = 0; i < 256; i++) {
	            crcTable[i] = getCrcOfByte(i);
	        }
	    }
	    public short getCrc(byte[] data) {
	        int crc = 0;
	        int length = data.length;
	        for (int i = 0; i < length; i++) {
	            crc = ((crc & 0xFF) << 8) ^ crcTable[(((crc & 0xFF00) >> 8) ^ data[i]) & 0xFF];
	        }
	        crc = crc & 0xFFFF;
	        return (short)crc;
	    }
}
