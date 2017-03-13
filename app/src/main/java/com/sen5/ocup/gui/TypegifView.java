package com.sen5.ocup.gui;
import com.sen5.ocup.R;
import com.sen5.ocup.util.BitmapUtil;
import com.sen5.ocup.util.GifOpenHelper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class TypegifView extends View implements Runnable {
	private static final String TAG = "TypegifView";
	private GifOpenHelper gHelper;
	private Paint mPaint;
	private boolean isStop = true;
	private int delta;
	private String title;

	private Bitmap bmp;
	private int mFrameCount;// gif一共有多少帧
	private int mCurFrameIndex;// 当前是第几帧
	private int w;
	private int h;
	private Handler mHandler;
	public final static  int stopGif = 1;
	private int srcWidth;
	private int srcHeight;
	private Matrix matrix;

	// construct - refer for java
	public TypegifView(Context context) {
		this(context, null);

	}

	// construct - refer for xml
	public TypegifView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
		// 添加属性
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.gifView);
		int n = ta.getIndexCount();

		for (int i = 0; i < n; i++) {
			int attr = ta.getIndex(i);

			switch (attr) {
			case R.styleable.gifView_src:
				int id = ta.getResourceId(R.styleable.gifView_src, 0);
				setSrc(id);
				break;

			case R.styleable.gifView_delay:
				int idelta = ta.getInteger(R.styleable.gifView_delay, 1);
				setDelta(idelta);
				break;

			case R.styleable.gifView_stop:
				boolean sp = ta.getBoolean(R.styleable.gifView_stop, false);
				if (!sp) {
					setStop();
				}
				break;
			}

		}

		ta.recycle();
	}

	/**
	 * 设置停止
	 * 
	 * @param stop
	 */
	public void setStop() {
		Log.d(TAG, "setStop--------------");
		isStop = false;
	}

	/**
	 * 设置启动
	 */
	public void setStart() {
		Log.d(TAG, "setStart--------------");
		isStop = true;

		Thread updateTimer = new Thread(this);
		updateTimer.start();
	}

	/**
	 * 通过下票设置第几张图片显示
	 * 
	 * @param id
	 */
	public void setSrc(int id) {
		gHelper = new GifOpenHelper();
		gHelper.read(TypegifView.this.getResources().openRawResource(id));
		mFrameCount = gHelper.getFrameCount();
		bmp = gHelper.getImage();// 得到第一张图片
		srcWidth = bmp.getWidth();
		srcHeight = bmp.getHeight();
		Log.d(TAG, "setSrc-------mFrameCount==" + mFrameCount);
	}

	/**
	 * 设置图片的宽高
	 * 
	 * @param w
	 * @param h
	 */
	public void setWH(int w, int h) {
		this.w = w;
		this.h = h;
		float scaleWidth = ((float) w + 0.00f) / srcWidth;
		float scaleHeight = ((float) h + 0.00f) / srcHeight;
		matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
	}
	
	public void setHandler(Handler h) {
		mHandler = h;
	}

	public void setDelta(int is) {
		delta = is;
	}

	// to meaure its Width & Height
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}

	private int measureWidth(int measureSpec) {
		return w;
//		 return gHelper.getWidth();
	}

	private int measureHeight(int measureSpec) {
		return h;
//		 return gHelper.getHeigh();
	}

	protected void onDraw(Canvas canvas) {
		if (mCurFrameIndex >= (mFrameCount-2)) {
			setStop();
			mHandler.sendEmptyMessage(stopGif );
		}else{
//			canvas.drawBitmap(bmp, 0, 0, new Paint());
			mCurFrameIndex++;
			canvas.drawBitmap(bmp, matrix, mPaint);
//			canvas.drawBitmap(BitmapUtil.resizeImage(bmp, w, h), 0, 0, new Paint());
			bmp = gHelper.nextBitmap();
		}
	}

	public void run() {
		while (isStop) {
			try {
				this.postInvalidate();
				Thread.sleep(gHelper.nextDelay() / delta);
			} catch (Exception ex) {
				Log.d(TAG, "run----Exception e=" + ex);
			}
		}
	}

}
