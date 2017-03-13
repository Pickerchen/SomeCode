package com.sen5.ocup.gui;

import com.sen5.ocup.R;
import com.sen5.ocup.util.BitmapUtil;
import com.sen5.ocup.util.Tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.BaseSavedState;
import android.widget.ProgressBar;

public class OmeView extends SurfaceView implements SurfaceHolder.Callback {
//	public class OmeView extends View {
	private static final String TAG = "OmeView";
	private final int WATER_COLOR = Color.rgb(38, 158, 241);
	private Context mContext;
	private SurfaceHolder sfh;
	private boolean isInit;
	private boolean isRun;
	/**
	 * view的宽
	 */
	private int mWidth;
	/**
	 * view的高
	 */
	private int mHeight;
	/**
	 * 人的宽
	 */
	private int mW_person;
	/**
	 * 人的高
	 */
	private int mH_person;

	private float mL_person;
	private float mT_person;
	private float mR_person;
	private float mB_person;
	/**
	 * 画人的画笔
	 */
	private Paint mPaint_person;
	/**
	 * 画水波的画笔
	 */
	private Paint mPaint_water;
	private Bitmap bmp_person;
	private Bitmap bmp_person_bg;
	private float mDensity;
	private float mProgress;
	private final float offset = 0.0f; 
	
	//为了保持人不变形，可能会宽或高不和view一样大小，要保证人居中，就会有偏移
	private  float offset_x ;
	private  float offset_y ;
	// refresh thread
    private RefreshProgressRunnable mRefreshProgressRunnable;

	public OmeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		setZOrderOnTop(true);// 设置置顶（不然实现不了透明）
		sfh = this.getHolder();
		sfh.addCallback(this);
		sfh.setFormat(PixelFormat.TRANSLUCENT);// 设置背景透明
//		this.setBackgroundResource(R.drawable.person_bg);
		setWillNotDraw(false);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated");
		isRun = true;
		if (!isInit) {
			init();
		}
//		new Thread(new Runnable() {
//			public void run() {
//				float phase = 120 * mDensity;// 相位
//				float amplifier = 2 * mDensity;// 幅值;
//				boolean isPlus = true;
//				while (isRun) {
//					Canvas canvas = sfh.lockCanvas();
//					if (null != canvas) {
//						canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);// 清除画布
//						drawPersonBG(canvas);
//						drawWater(canvas, phase, amplifier, mProgress);
//						drawPerson(canvas);
//						sfh.unlockCanvasAndPost(canvas);
//						if (isPlus) {
//							phase += 1;
//						} else {
//							phase -= 1;
//						}
//						try {
//							Thread.sleep(300);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//		}).start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "surfaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");
		isRun = false;
		super.destroyDrawingCache();
	}

	/**
	 * 初始化
	 */
	private void init() {
		mWidth = getWidth();
		mHeight = getHeight();
		mDensity = getResources().getDisplayMetrics().density;
		Log.d(TAG, "init----mWidth==" + mWidth + "  mHeight==" + mHeight+"mDensity =="+mDensity);
		Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.person);
		if ((mHeight+0.000f)/(mWidth+0.000f) > 2.26) {//人的比例为2。26
			mHeight = (int) (mWidth*2.26f);
		}else{
			mWidth = (int) (mHeight/2.26f);
			offset_x =(getWidth()-mWidth)/2;
		}
		bmp_person = BitmapUtil.resizeImage(bmp, mWidth, mHeight);
		Bitmap bmp_bg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.person_bg);
		bmp_person_bg = BitmapUtil.resizeImage(bmp_bg, mWidth, mHeight);
		mW_person = bmp_person.getWidth();
		mH_person = bmp_person.getHeight();
		mL_person = (float) ((mWidth - mW_person + 0.0) / 2)+offset_x;
		mT_person = (float) ((mHeight - mH_person + 0.0) / 2);
		mR_person = mL_person + mW_person;
		mB_person = mT_person + mH_person;

		Log.d(TAG, "init----mW_person==" + mW_person + "  mH_person==" + mH_person);

		// 设置画人的画笔
		mPaint_person = new Paint();
		mPaint_person.setAntiAlias(true);// 消除锯齿
		mPaint_person.setStyle(Paint.Style.STROKE); // 设置图形为空心
		mPaint_person.setColor(Color.BLACK);
		mPaint_person.setStrokeWidth(5 * mDensity);

		// 设置画水量的画笔
		mPaint_water = new Paint();
		mPaint_water.setAntiAlias(true);// 消除锯齿
		mPaint_water.setStyle(Paint.Style.FILL);
		mPaint_water.setColor(WATER_COLOR);
		mPaint_water.setStrokeWidth(1 * mDensity);

		isInit = true;
	}

	/**
	 * 
	 * @param canvas
	 * @param phase
	 * @param amplifier
	 * @param waterHeight
	 *            水高占人高的比重
	 */
	private void drawWater(Canvas canvas, float phase, float amplifier, float progress) {
		float frequency = 1.0f*mDensity;// 频率
//		Log.d(TAG, "drawWater------phase=="+phase+"  amplifier=="+amplifier+"   frequency=="+frequency+"  mDensity=="+mDensity);;
		float startx = mL_person+offset*mDensity;
		float endx = startx + mW_person-2*offset*mDensity;
		float starty = mB_person - progress * mH_person+offset*mDensity;
		Path path = new Path();
		path.moveTo(mL_person+offset*mDensity, starty);

		for (int i = (int) startx; i < endx + 1; i++) {
			path.lineTo((float) i, starty - amplifier * (float) (Math.sin(phase * 2 / (float) Math.PI + 2 * Math.PI * frequency * i / endx)));
		}
		path.lineTo(mR_person-2*offset*mDensity, mB_person-2*offset*mDensity);
		path.lineTo(mL_person+offset*mDensity, mB_person-2*offset*mDensity);
		path.lineTo(mL_person+offset*mDensity, starty);
		path.close();
		canvas.drawPath(path, mPaint_water);
	}

	private void drawPerson(Canvas canvas) {
		canvas.drawBitmap(bmp_person, mL_person, mT_person, mPaint_person);
	}
	private void drawPersonBG(Canvas canvas) {
		canvas.drawBitmap(bmp_person_bg, mL_person, mT_person, mPaint_person);
	}
	public void setProgress(float progress) {
		mProgress = progress;
//		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float amplifier = 2.0f*mDensity;// 幅值;
//		Log.d(TAG, "onDraw----------isInit=="+isInit+"   isRun="+isRun+"  amplifier=="+amplifier);
		if (!isInit) {
			init();
		}
	
//		float phase = 120 * mDensity;// 相位
//		float amplifier = 2 * mDensity;// 幅值;
//		boolean isPlus = true;
//		while (isRun) {
			if (null != canvas) {
				canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);// 清除画布
				canvas.drawColor(Color.WHITE);
				drawPersonBG(canvas);
				drawWater(canvas, phase, amplifier, mProgress);
				drawPerson(canvas);
				if (isPlus) {
					phase += 1;
				} else {
					phase -= 1;
				}
//				try {
//					Thread.sleep(300);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}
//		}
	}
	
	float phase = 5*mDensity;// 相位
	boolean isPlus = true;
	@Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
		Log.d(TAG, "onAttachedToWindow---------");
        mRefreshProgressRunnable = new RefreshProgressRunnable();
        getHandler().post(mRefreshProgressRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
		Log.d(TAG, "onDraw--------onDetachedFromWindow");
        getHandler().removeCallbacks(mRefreshProgressRunnable);
    }
    
    private class RefreshProgressRunnable implements Runnable {
        public void run() {
            synchronized (OmeView.this) {
//            	Log.d(TAG, "RefreshProgressRunnable-----------");
                invalidate();
                getHandler().postDelayed(this,300);
            }
        }
    }
}
