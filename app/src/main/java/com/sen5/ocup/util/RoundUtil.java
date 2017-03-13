package com.sen5.ocup.util;

/**
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 :涂鸦，判断点是否在圈内
 */
public class RoundUtil {
	/**
	 * 点在圆内
	 * 
	 * @param sx
	 * @param sy
	 * @param r
	 * @param x
	 * @param y
	 * @return
	 */
	public static boolean checkInRound(float sx, float sy, float r, float x,
			float y) {
		return Math.sqrt((sx - x) * (sx - x) + (sy - y) * (sy - y)) < r;
	}
}
