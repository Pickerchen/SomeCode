package com.sen5.ocup.gui;

import com.sen5.ocup.callback.CustomInterface.IDrawProgress;
import com.sen5.ocup.util.Tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * 自定义进度条（圆形）
 * 
 */
// public class Circle_ProgressBar extends SurfaceView implements SurfaceHolder.Callback {
public class Circle_ProgressBar extends View {
	protected static final String TAG = "Circle_ProgressBar";
	private int mWidth;
	private int mHeight;
	private boolean isSurfaceCreate;
	private Context mContext;
	// private SurfaceHolder sfh;
	private Paint mPaint;
	private RectF mRectF;

//	private int lastProgress;// 记录上一次的进度
	private int progress;// 当前的进度
	private volatile float sweepAngle;

	private IDrawProgress iDrawProgress;

	public Circle_ProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public Circle_ProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		// setZOrderOnTop(true);// 设置置顶（不然实现不了透明）
		// sfh = this.getHolder();
		// sfh.addCallback(this);
		// sfh.setFormat(PixelFormat.TRANSLUCENT);// 设置背景透明

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!isSurfaceCreate) {
			init();
			isSurfaceCreate = true;
		}
		if (null != iDrawProgress) {
			iDrawProgress.drawProgress(progress, this, canvas);
		}
	}

	/**
	 * 初始化画布、画笔
	 */
	private void init() {

		int left = Tools.dip2px(mContext, 0+1);
//		int top = Tools.dip2px(mContext, 15 + (200 - 150) / 2);
		int top = Tools.dip2px(mContext, 0+1 );
		int right = Tools.dip2px(mContext, (120-2));
		// int right = 120;
		int bottom = Tools.dip2px(mContext, (120-2));

		mRectF = new RectF(left, top, right, bottom);// 圆的外切矩形
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeCap(Paint.Cap.ROUND);// 圆形边界
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setDither(true);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(Tools.dip2px(mContext, Tools.dip2px(mContext, 0+1)));// 进度条的条宽
//		mPaint.setColor(Color.rgb(17, 181, 254));
		// 渐变色
		int[] colors = new int[] {Color.parseColor("#f8564f"),  Color.parseColor("#f1f097"), Color.parseColor("#74e6cf"), Color.parseColor("#09ddfe") ,Color.parseColor("#10d0ac"),Color.parseColor("#14c97f"),Color.parseColor("#1abf3a")};
		LinearGradient lgGradient = new LinearGradient(left+(right-left)/2, top,(left+(right-left)/2), bottom, colors, null, LinearGradient.TileMode.MIRROR);// 设置阴影过度渐变
//		float r_x = (float) (left+(right-left+0.00f)/2);
//		float r_y = (float) top+(bottom-top+0.00f)/2;
//		SweepGradient spGradient  = new SweepGradient(r_x, r_y, colors, null);
//		RadialGradient rGradient = new RadialGradient(r_x, r_y, r, colors, null, Shader.TileMode.MIRROR);
		mPaint.setShader(lgGradient);
	}

	/**
	 * 设置进度和最大值
	 * 
	 * @param progress
	 * @param max
	 */
	public void setCustomProgress(int progress, int max, Canvas canvas) {
		Log.d(TAG, "setCustomProgress()          progress == " + progress);
//		lastProgress = progress;
		sweepAngle = ((float) progress / max) * 360;
		StartDraw(canvas);
	}

	/**
	 * 画圆弧
	 * 
	 * @param degree
	 */
	public void StartDraw(Canvas canvas) {
		Log.d(TAG, "myDraw()---------sweepAngle==" + sweepAngle);
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// 锁定画布
		// Canvas canvas = sfh.lockCanvas();
		if (null == canvas) {
			Log.d(TAG, "myDraw()-----null == canvas----");
			return;
		}
		// 画进度
		for (int i = 0; i < sweepAngle; i += 10) {
			canvas.drawArc(mRectF,270, i, false, mPaint);
		}
		canvas.drawArc(mRectF,270, sweepAngle, false, mPaint);
		// sfh.unlockCanvasAndPost(canvas);

		// }
		// }).start();
	}

	// @Override
	// public void surfaceCreated(SurfaceHolder holder) {
	// Log.d(TAG, "surfaceCreated()------------");
	// isSurfaceCreate = true;
	// init();
	// if (null == iDrawProgress) {
	// StartDraw();
	// }else{
	// iDrawProgress.drawProgress(this);
	// }
	// }

	// @Override
	// public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	// Log.d(TAG, "surfaceChanged()------------");
	// isSurfaceCreate = true;
	// }
	//
	// @Override
	// public void surfaceDestroyed(SurfaceHolder holder) {
	// Log.d(TAG, "surfaceDestroyed()------------");
	// isSurfaceCreate = false;
	// }

	public void setIDrawProgress(IDrawProgress iDrawProgress, int progress) {
		this.iDrawProgress = iDrawProgress;
		this.progress = progress;
	}

}
