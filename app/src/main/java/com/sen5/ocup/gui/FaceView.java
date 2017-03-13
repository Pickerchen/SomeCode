package com.sen5.ocup.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 画出水量笑脸view
 * 
 * @author caoxia
 * 
 */
public class FaceView extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = "FaceView";
	private final int WATER_COLOR = Color.rgb(38, 158, 241);
	private int mWidth;
	private int mHeight;
	
	/**
	 * 脸的原点横坐标
	 */
	private int face_X;
	/**
	 * 脸的原点纵坐标
	 */
	private int face_Y;
	/**
	 * 脸的半径
	 */
	private int r_face;
	/**
	 * 眼睛的半径
	 */
	private final int r_eye = 15;
	/**
	 * 是否已经初始化过
	 */
	private boolean isCache;
	/**
	 * 笑脸的画笔
	 */
	private Paint mPaint_face = null;
	/**
	 * 眼睛的画笔
	 */
	private Paint mPaint_eye = null;
	/**
	 * 水量的画笔
	 */
	private Paint mPaint_water = null;
	/**
	 * 水波的画笔
	 */
	private Paint mPaint_wave = null;
	private SurfaceHolder sfh;
	private boolean isRun;

	public FaceView(Context context) {
		super(context);
	}

	public FaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setZOrderOnTop(true);// 设置置顶（不然实现不了透明）
		sfh = this.getHolder();
		sfh.addCallback(this);
		sfh.setFormat(PixelFormat.TRANSLUCENT);// 设置背景透明
	}

	public FaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	// @Override
	// protected void onDraw(Canvas canvas) {
	// if (!isCache) {
	// initCache();
	// }
	// drawToCanvas(canvas);
	// }

	/**
	 * 初始化
	 */
	private void initCache() {
		isCache = true;
		
		mWidth = getWidth();
		mHeight = getHeight();
		r_face = mWidth / 4;
		face_X = mWidth/2;
		face_Y = Math.min(mWidth / 4, mHeight/2) + 10;

		// 设置画脸的画笔
		mPaint_face = new Paint();
		mPaint_face.setAntiAlias(true);// 消除锯齿
		mPaint_face.setStyle(Paint.Style.STROKE); // 设置图形为空心
		mPaint_face.setColor(Color.WHITE);
		mPaint_face.setStrokeWidth(5);
		// 设置画眼睛的画笔
		mPaint_eye = new Paint();
		mPaint_eye.setAntiAlias(true);
		mPaint_eye.setStyle(Paint.Style.FILL);
		mPaint_eye.setColor(Color.WHITE);
		mPaint_eye.setStrokeWidth(5);

		// 设置画水量的画笔
		mPaint_water = new Paint();
		mPaint_water.setAntiAlias(true);// 消除锯齿
		mPaint_water.setStyle(Paint.Style.STROKE);
		mPaint_water.setColor(WATER_COLOR);
		mPaint_water.setStrokeWidth(3);

		// 设置画水波的画笔
		mPaint_wave = new Paint();
		mPaint_wave.setAntiAlias(true);// 消除锯齿
		mPaint_wave.setStyle(Paint.Style.FILL);
		mPaint_wave.setColor(WATER_COLOR);
		mPaint_wave.setStrokeWidth(0);

	}

	/**
	 * 开始画到画布上
	 * 
	 * @param canvas
	 */
	private void drawToCanvas(final Canvas canvas, int degree, int phase, float amplifier, int faceDegree) {
		int frequency = 1;// 频率
		// 画水量
		// RectF oval_water = new RectF(mWidth / 2 - r_face, mHeight / 2 - r_face, mWidth / 2 + r_face, mHeight / 2 + r_face);
		// canvas.drawArc(oval_water, degree, 180 - degree * 2, false, mPaint_water);

		// 画水波
		drawWave(canvas, degree, phase, amplifier, frequency);

		// 画脸
		canvas.drawCircle(face_X, face_Y, r_face, mPaint_face);
		// 画眼睛
		canvas.drawCircle(face_X - 80, face_Y - 80, r_eye, mPaint_eye);
		canvas.drawCircle(face_X + 80, face_Y - 80, r_eye, mPaint_eye);
		// 画嘴巴
		int y = 20;// 嘴巴对应的矩阵y坐标微调
		RectF oval = new RectF(face_X - r_face / 2, face_Y - r_face / 2 + y, face_X + r_face / 2, face_Y + r_face / 2 + y);
		canvas.drawArc(oval, faceDegree, 180 - faceDegree * 2, false, mPaint_face);
		// canvas.drawArc(oval, 40, 100, false, mPaint_face);
		// canvas.drawArc(oval, 60, 60, false, mPaint_face);
		// canvas.drawArc(oval, 80, 20, false, mPaint_face);

		// RectF oval = new RectF(mWidth / 2 - r_face / 2, mHeight / 2 + r_face/2-y , mWidth / 2 + r_face / 2, mHeight / 2 + 3*r_face/2-y );
		// canvas.drawArc(oval, -20, -140, false, mPaint_face);
		// canvas.drawArc(oval, -40, -100, false, mPaint_face);

	}
/**
 * 画水波
 * @param canvas
 * @param degree
 * @param phase
 * @param amplifier
 * @param frequency
 */
	private void drawWave(final Canvas canvas, int degree, int phase, float amplifier, int frequency) {
		Path path = new Path();
		float startx = (float) (face_X - (r_face * (Math.cos((degree + 0.00f) / 180 * Math.PI))));
		float starty = (float) (face_Y + (r_face * Math.sin((degree + 0.00f) / 180 * Math.PI)));
		path.moveTo(startx, starty);

		float endx = (float) (face_X + (r_face) * (Math.cos((degree + 0.00f) / 180 * Math.PI)));
		float endy = (float) (face_Y + (r_face) * (Math.sin((degree + 0.00f) / 180 * Math.PI)));

		for (int i = (int) startx; i < endx + 1; i++) {
			path.lineTo((float) i, starty - amplifier * (float) (Math.sin(phase * 2 / (float) Math.PI + 2 * Math.PI * frequency * i / endx)));
		}
		if (endy >= face_Y) {
			for (int i = (int) endx - 1; i > startx + 2; i--) {
				int x = i;
				float y = (float) Math.sqrt(r_face * r_face - (i - (face_X)) * (i - face_X)) + (face_Y);
				path.lineTo(x, y);
			}
		} else {
			for (int i = (int) endy; i < face_Y; i++) {
				int y = i;
				float x = (float) Math.sqrt(r_face * r_face - ((face_Y) - i) * ((face_Y) - i)) + face_X;
				path.lineTo(x, y);
			}
			for (int i = (int) face_Y; i < face_Y + (face_Y - endy); i++) {
				int y = i;
				float x = (float) Math.sqrt(r_face * r_face - (i - (face_Y)) * (i - (face_Y))) + face_X;
				path.lineTo(x, y);
			}
			for (int i = (int) endx - 1; i > startx + 1; i--) {
				int x = i;
				float y = (float) Math.sqrt(r_face * r_face - (i - (face_X)) * (i - (face_X))) + (face_Y);
				path.lineTo(x, y);
			}
			for (int i = (int) (face_Y + (face_Y - endy)); i > face_Y; i--) {
				int y = i;
				float x = face_X - (float) Math.sqrt(r_face * r_face - (i - (face_Y)) * (i - (face_Y)));
				path.lineTo(x, y);
			}
			for (int i = (int) face_Y; i > endy; i--) {
				int y = i;
				float x = face_X - (float) Math.sqrt(r_face * r_face - ((face_Y) - i) * ((face_Y) - i));
				path.lineTo(x, y);
			}

		}
		path.lineTo(startx, starty - amplifier * (float) (Math.sin(phase * 2 * (float) Math.PI / 360.0f + 2 * Math.PI * frequency * (startx + 1) / endx)));
		path.close();
		canvas.drawPath(path, mPaint_wave);
	}

	/**
	 * 设置水量
	 */
	public void setWater() {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated");
		isRun = true;
		if (!isCache) {
			initCache();
		}
		final int initPhase = 30;

		new Thread(new Runnable() {
			public void run() {
				int degree = -30;
				int phase = 150;// 相位
				float amplifier = 6.000f;// 幅值
				int faceDgree = 20;
				int count = 0;
				boolean isPlus = true;
				while (isRun && count < 5) {
					Canvas canvas = sfh.lockCanvas();
					if (null != canvas) {

						canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);// 清除画布
						drawToCanvas(canvas, degree, phase, amplifier, faceDgree);
						sfh.unlockCanvasAndPost(canvas);
						if (isPlus) {
							phase += 1;
						} else {
							phase -= 1;
						}
						
						if (phase%initPhase==0) {
							count++;
						}

//						if (phase >= (initPhase + 10)) {
//							isPlus = false;
//							count++;
//						} else if (phase <= initPhase) {
//							isPlus = true;
//							count++;
//						}
						try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "surfaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");
		isRun = false;
	}
}
