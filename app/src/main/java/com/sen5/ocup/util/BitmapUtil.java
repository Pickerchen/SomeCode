package com.sen5.ocup.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 图片处理
 */
public class BitmapUtil {
	
	/**
	 * 缩放图片以达到指定的大小以下
	 * 
	 * @param bitmap
	 * @param f
	 * @return
	 */
	public static Bitmap resizeBitmap(final Bitmap bitmap, ByteArrayOutputStream os, long size) {
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
		if (os.toByteArray().length < size) {
			return bitmap;
		}
		
		int oriW = bitmap.getWidth();
		int oriH = bitmap.getHeight();
		float scale = 0.8f; //宽高的缩放比例

		//根据不同的宽高初始的缩小比例
		if (oriW > 1000 || oriH > 1000) {
			scale = 0.2f;
		} 
		else if (oriW > 500 || oriH > 500) {
			scale = 0.4f;
		} 
		
		Bitmap tmpBitmap = null;
		Bitmap newBitmap = bitmap;
		int w, h;
		do {
			os.reset();
			w = (int)(oriW * scale + 0.5);
			h = (int)(oriH * scale + 0.5);
			newBitmap = resizeImage(newBitmap, w, h);
			scale *= 0.8;
			
			if (tmpBitmap != null) {
				tmpBitmap.recycle();
				
			}
			tmpBitmap = newBitmap;
			newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
		}
		while(os.toByteArray().length >= size && w > 50 && h > 50);
		
		System.gc();
		return newBitmap;
	}

	/**
	 * 缩放图片
	 * 
	 * @param bitmap
	 * @param f
	 * @return
	 */
	public static Bitmap zoom(Bitmap bitmap, float zf) {
		Matrix matrix = new Matrix();
		matrix.postScale(zf, zf);
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
	}

	/**
	 * 缩放图片
	 * 
	 * @param bitmap
	 * @param f
	 * @return
	 */
	public static Bitmap zoom(Bitmap bitmap, float wf, float hf) {
		Matrix matrix = new Matrix();
		matrix.postScale(wf, hf);
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
	}

	/**
	 * 图片圆角处理
	 * 
	 * @param bitmap
	 * @param roundPX
	 * @return
	 */
	public static Bitmap getRCB(Bitmap bitmap, float roundPX) {
		// RCB means
		// Rounded
		// Corner Bitmap
		Bitmap dstbmp = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(dstbmp);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return dstbmp;
	}
	
	/**
	 * 缩放大小，w：缩放后的宽度，h 缩放后的高度
	 * 
	 * */
	public static Bitmap resizeImage(Bitmap bitmap, float w, float h) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float scaleWidth = ((float) w + 0.00f) / width;
		float scaleHeight = ((float) h + 0.00f) / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		
		return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	}
	
	/**
	 * 优化图片占用资源
	 * 
	 * @param buffer
	 * @return
	 */
	public static Bitmap optimizeBitmap(byte[] buffer) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		opt.inSampleSize = computeInitialSampleSize(opt, -1, 128 * 128);
		opt.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(buffer, 0, buffer.length, opt);
	}

	/**
	 * 优化图片占用资源
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap optimizeBitmap(Bitmap bitmap) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		opt.inSampleSize = computeSampleSize(opt, -1, 128 * 128);
		opt.inJustDecodeBounds = false;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		InputStream sbs = new ByteArrayInputStream(baos.toByteArray());

		return BitmapFactory.decodeStream(sbs, null, opt);
	}

	/**
	 * 优化图片占用资源
	 * 
	 * @param options
	 * @param minSideLength
	 * @param maxNumOfPixels
	 * @return
	 */
	private static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	/**
	 * 作用:优化图片占用空间
	 * 
	 * @param options
	 * @param minSideLength
	 * @param maxNumOfPixels
	 * @return
	 */
	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

}
