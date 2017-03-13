/**
 * @copyright 2013 Sen LABS technology
 * @author Kay.Zheng
 * @version build��2013
 */
package com.sen5.ocup.util;

import java.io.InputStream;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 优化图片占用空间
 */
public class BitmapOptimiz {
	public static Drawable getDrawable(Context context, int resId){ 
		BitmapFactory.Options opt = new BitmapFactory.Options(); 
		opt.inPreferredConfig = Bitmap.Config.RGB_565; 
		opt.inPurgeable = true; 
		opt.inInputShareable = true; 
		opt.inSampleSize = computeSampleSize(opt, -1, 128*128); 
		opt.inJustDecodeBounds = false; 

		InputStream is = context.getResources().openRawResource(resId); 

		return new BitmapDrawable(BitmapFactory.decodeStream(is, null, opt)); 
	} 

	public static Bitmap getBitmap(Context context, int resId){ 
		BitmapFactory.Options opt = new BitmapFactory.Options(); 
		opt.inPreferredConfig = Bitmap.Config.RGB_565; 
		opt.inPurgeable = true; 
		opt.inInputShareable = true; 
		opt.inSampleSize = computeSampleSize(opt, -1, 128*128);
		opt.inJustDecodeBounds = false; 
		
		InputStream is = context.getResources().openRawResource(resId); 

		return BitmapFactory.decodeStream(is, null, opt); 
	} 

	private static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8 ) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 :
			(int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 :
			(int) Math.min(Math.floor(w / minSideLength),
					Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) &&
				(minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}
}
