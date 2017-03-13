/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013 Triggertrap Ltd
 * Author Neil Davies
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.sen5.ocup.gui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.sen5.ocup.R;

/**
 * 
 * SeekArc.java
 * 
 * This is a class that functions much like a SeekBar but follows a circle path instead of a straight line.
 * 
 * @author Neil Davies
 * 
 */
public class SeekArc extends View {

	private static final String TAG = SeekArc.class.getSimpleName();
	private static int INVALID_PROGRESS_VALUE = -1;
	// The initial rotational offset -90 means we start at 12 o'clock
	private final int mAngleOffset = -90;

	/**
	 * The Drawable for the seek arc thumbnail
	 */
	private Drawable mThumb;

	/**
	 * The Maximum value that this SeekArc can be set to
	 */
	private int mMax = 80;

	/**
	 * The Current value that the SeekArc is set to
	 */
	private int mProgress = 0;

	/**
	 * The width of the progress line for this SeekArc
	 */
	private int mProgressWidth = 20;

	/**
	 * 默认背景的宽度
	 */
	private int mArcWidth = 2;

	/**
	 * The Angle to start drawing this Arc from
	 */
	private int mStartAngle = 0;

	/**
	 * The Angle through which to draw the arc (Max is 360)
	 */
	private int mSweepAngle = 360;

	/**
	 * The rotation of the SeekArc- 0 is twelve o'clock
	 */
	private int mRotation = 0;

	/**
	 * Give the SeekArc rounded edges
	 */
	private boolean mRoundedEdges = false;

	/**
	 * Enable touch inside the SeekArc
	 */
	private boolean mTouchInside = true;

	/**
	 * Will the progress increase clockwise or anti-clockwise
	 */
	private boolean mClockwise = true;

	// Internal variables
	private int mArcRadius = 0;
	private float mProgressSweep = 0;
	private RectF mArcRect = new RectF();
	private Paint mArcPaint;
	private Paint mProgressPaint;
	private Paint mTextPaint;
	private int mTranslateX;
	private int mTranslateY;
	private int mThumbXPos;
	private int mThumbYPos;
	private double mTouchAngle;
	private float mTouchIgnoreRadius;
	private OnSeekArcChangeListener mOnSeekArcChangeListener;
	private int textArcStart;
	private float density;

	public interface OnSeekArcChangeListener {

		/**
		 * Notification that the progress level has changed. Clients can use the fromUser parameter to distinguish
		 * user-initiated changes from those that occurred programmatically.
		 * 
		 * @param seekArc
		 *            The SeekArc whose progress has changed
		 * @param progress
		 *            The current progress level. This will be in the range 0..max where max was set by
		 *            {@link ProgressArc#setMax(int)}. (The default value for max is 100.)
		 * @param fromUser
		 *            True if the progress change was initiated by the user.
		 */
		void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser);

		/**
		 * Notification that the user has started a touch gesture. Clients may want to use this to disable advancing the
		 * seekbar.
		 * 
		 * @param seekArc
		 *            The SeekArc in which the touch gesture began
		 */
		void onStartTrackingTouch(SeekArc seekArc);

		/**
		 * Notification that the user has finished a touch gesture. Clients may want to use this to re-enable advancing
		 * the seekarc.
		 * 
		 * @param seekArc
		 *            The SeekArc in which the touch gesture began
		 */
		void onStopTrackingTouch(SeekArc seekArc);
	}

	public SeekArc(Context context) {
		super(context);
		init(context, null, 0);
	}

	public SeekArc(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, R.attr.seekArcStyle);
	}

	public SeekArc(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 *            上下文
	 * @param attrs
	 *            参数方法
	 * @param defStyle
	 *            样式
	 */
	private void init(Context context, AttributeSet attrs, int defStyle) {

		Log.d(TAG, "Initialising SeekArc");
		final Resources res = getResources();
		density = context.getResources().getDisplayMetrics().density;

		// Defaults, may need to link this into theme settings
		// 默认颜色
		// int arcColor = res.getColor(R.color.progress_gray);
		// 默认进度条颜色
		int progressColor = res.getColor(android.R.color.holo_blue_light);

		int thumbHalfheight = 0;
		int thumbHalfWidth = 0;
		// 移动的按钮
		mThumb = res.getDrawable(R.drawable.seek_arc_control_selector);
		// 移动进度宽度
		mProgressWidth = (int) (mProgressWidth * density);

		// 获取你设置的值
		if (attrs != null) {
			// Attribute initialization
			final TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.SeekArc, defStyle, 0);

			Drawable thumb = a.getDrawable(R.styleable.SeekArc_thumb);
			if (thumb != null) {
				mThumb = thumb;
			}

			thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 4;
			thumbHalfWidth = (int) mThumb.getIntrinsicWidth() /4;
			mThumb.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth,
					thumbHalfheight);

			mMax = a.getInteger(R.styleable.SeekArc_max_pb, mMax);
			mProgress = a.getInteger(R.styleable.SeekArc_cur_progress, mProgress);
			mProgressWidth = (int) a.getDimension(
					R.styleable.SeekArc_progressWidth, mProgressWidth);
			mArcWidth = (int) a.getDimension(R.styleable.SeekArc_arcWidth,
					mArcWidth);
			mStartAngle = a.getInt(R.styleable.SeekArc_startAngle, mStartAngle);
			mSweepAngle = a.getInt(R.styleable.SeekArc_sweepAngle, mSweepAngle);
			mRotation = a.getInt(R.styleable.SeekArc_rotation, mRotation);
			mRoundedEdges = a.getBoolean(R.styleable.SeekArc_roundEdges,
					mRoundedEdges);
			mTouchInside = a.getBoolean(R.styleable.SeekArc_touchInside,
					mTouchInside);
			mClockwise = a
					.getBoolean(R.styleable.SeekArc_clockwise, mClockwise);

			// arcColor = a.getColor(R.styleable.SeekArc_arcColor, arcColor);
			progressColor = a.getColor(R.styleable.SeekArc_progressColor,
					progressColor);

			a.recycle();
		}

		mProgress = (mProgress > mMax) ? mMax : mProgress;
		mProgress = (mProgress < 0) ? 0 : mProgress;

		mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
		mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;

		mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
		mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;

		// 设置背景图片的颜色值
		mArcPaint = new Paint();
		// mArcPaint.setColor(arcColor);
		mArcPaint.setAntiAlias(true);
		mArcPaint.setStyle(Paint.Style.STROKE);
		mArcPaint.setStrokeWidth(mArcWidth);

		mArcPaint.setStrokeCap(Paint.Cap.ROUND); // 圆形边界
		// mArcPaint.setStrokeJoin(Paint.Join.ROUND);
		// mArcPaint.setDither(true);

		// mArcPaint.setAlpha(45);

		mProgressPaint = new Paint();
		mProgressPaint.setColor(progressColor);
		mProgressPaint.setAntiAlias(true);
		mProgressPaint.setStyle(Paint.Style.STROKE);
		mProgressPaint.setStrokeWidth(mProgressWidth);

		if (mRoundedEdges) {
			mArcPaint.setStrokeCap(Paint.Cap.ROUND);
			mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
		}

		mTextPaint = new Paint();
		mTextPaint.setColor(Color.parseColor("#2caff6"));
		mTextPaint.setTextSize(getResources().getDimension(R.dimen.seek_number_size));
		mTextPaint.setStyle(Paint.Style.STROKE);
		mTextPaint.setAntiAlias(true);
	}

	@SuppressLint("DrawAllocation") 
	@Override
	protected void onDraw(Canvas canvas) {
		if (!mClockwise) {
			canvas.scale(-1, 1, mArcRect.centerX(), mArcRect.centerY());
		}

		// Draw the arcs
		final int arcStart = mStartAngle + mAngleOffset + mRotation;
		final int arcSweep = mSweepAngle;

		int[] colors = new int[] { Color.parseColor("#4eb133"),
				Color.parseColor("#00aecb"), Color.parseColor("#f7ee4c"),
				Color.parseColor("#e7373c"), };

		SweepGradient sgGradient = new SweepGradient(
				getMeasuredWidth() / 2, getMeasuredHeight() / 2,
				colors, null);
		Matrix localM = new Matrix();
		localM.setRotate(75, getMeasuredWidth() / 2, getMeasuredHeight() / 2);
		
		sgGradient.setLocalMatrix(localM);
		mArcPaint.setShader(sgGradient);

		//canvas.drawArc(mArcRect, 0, 360, false, mArcPaint);
		
		// 调进度框的颜色
		canvas.drawArc(mArcRect, arcStart, arcSweep, false, mArcPaint);
		canvas.drawArc(mArcRect, arcStart, mProgressSweep, false,
				mProgressPaint);

		// 加载字体
		for (int i = 1; i <= 9; i++) {
			int mTextXPos = (int) (((mArcRadius - (24 * density)) * Math
					.cos(Math.toRadians(textArcStart + 34 * (i - 1)))) + (6 * density));
			int mTextYPos = (int) (((mArcRadius - (24 * density)) * Math
					.sin(Math.toRadians(textArcStart + 34 * (i - 1)))) - (6 * density));

			canvas.drawText(i + "", mTranslateX - mTextXPos, mTranslateY
					- mTextYPos, mTextPaint);
		}

		// Draw the thumb nail
		canvas.translate(mTranslateX - mThumbXPos, mTranslateY - mThumbYPos);

		mThumb.draw(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		final int height = getDefaultSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		final int width = getDefaultSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		final int min = Math.min(width, height);
		float top = 0;
		float left = 0;
		int arcDiameter = 0;

		mTranslateX = (int) (width * 0.5f);
		mTranslateY = (int) (height * 0.5f);

		arcDiameter = min - getPaddingLeft();
		mArcRadius = arcDiameter / 2;
		top = height / 2 - (arcDiameter / 2);
		left = width / 2 - (arcDiameter / 2);
		mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);

		int arcStart = (int) mProgressSweep + mStartAngle + mRotation + 90;

		textArcStart = 315;

		mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(arcStart)));
		mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(arcStart)));

		setTouchInSide(mTouchInside);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			onStartTrackingTouch();
			updateOnTouch(event);
			break;
		case MotionEvent.ACTION_MOVE:
			updateOnTouch(event);
			break;
		case MotionEvent.ACTION_UP:
			onStopTrackingTouch();
			setPressed(false);
			break;
		case MotionEvent.ACTION_CANCEL:
			onStopTrackingTouch();
			setPressed(false);

			break;
		}

		return true;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mThumb != null && mThumb.isStateful()) {
			int[] state = getDrawableState();
			mThumb.setState(state);
		}
		invalidate();
	}

	private void onStartTrackingTouch() {
		if (mOnSeekArcChangeListener != null) {
			mOnSeekArcChangeListener.onStartTrackingTouch(this);
		}
	}

	private void onStopTrackingTouch() {
		if (mOnSeekArcChangeListener != null) {
			mOnSeekArcChangeListener.onStopTrackingTouch(this);
		}
	}

	private void updateOnTouch(MotionEvent event) {
		boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
		if (ignoreTouch) {
			return;
		}
		setPressed(true);
		mTouchAngle = getTouchDegrees(event.getX(), event.getY());
		int progress = getProgressForAngle(mTouchAngle);
		onProgressRefresh(progress, true);
	}

	private boolean ignoreTouch(float xPos, float yPos) {
		boolean ignore = false;
		float x = xPos - mTranslateX;
		float y = yPos - mTranslateY;

		float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
		if (touchRadius < mTouchIgnoreRadius) {
			ignore = true;
		}
		return ignore;
	}

	private double getTouchDegrees(float xPos, float yPos) {
		float x = xPos - mTranslateX;
		float y = yPos - mTranslateY;
		// invert the x-coord if we are rotating anti-clockwise
		x = (mClockwise) ? x : -x;
		// convert to arc Angle
		double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2)
				- Math.toRadians(mRotation));
		if (angle < 0) {
			angle = 360 + angle;
		}
		angle -= mStartAngle;
		return angle;
	}

	private int getProgressForAngle(double angle) {
		int touchProgress = (int) Math.round(valuePerDegree() * angle);

		touchProgress = (touchProgress < 0) ? INVALID_PROGRESS_VALUE
				: touchProgress;
		touchProgress = (touchProgress > mMax) ? INVALID_PROGRESS_VALUE
				: touchProgress;
		return touchProgress;
	}

	private float valuePerDegree() {
		return (float) mMax / mSweepAngle;
	}

	private void onProgressRefresh(int progress, boolean fromUser) {
		updateProgress(progress, fromUser);
	}

	private void updateThumbPosition() {
		int thumbAngle = (int) (mStartAngle + mProgressSweep + mRotation + 90);
		mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(thumbAngle)));
		mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(thumbAngle)));
	}

	private void updateProgress(int progress, boolean fromUser) {

		if (progress == INVALID_PROGRESS_VALUE) {
			return;
		}

		if (mOnSeekArcChangeListener != null) {
			mOnSeekArcChangeListener
					.onProgressChanged(this, progress, fromUser);
		}

		progress = (progress > mMax) ? mMax : progress;
		progress = (mProgress < 0) ? 0 : progress;

		mProgress = progress;
		mProgressSweep = (float) progress / mMax * mSweepAngle;

		updateThumbPosition();

		invalidate();
	}

	/**
	 * Sets a listener to receive notifications of changes to the SeekArc's progress level. Also provides notifications
	 * of when the user starts and stops a touch gesture within the SeekArc.
	 * 
	 * @param l
	 *            The seek bar notification listener
	 * 
	 * @see SeekArc.OnSeekBarChangeListener
	 */
	public void setOnSeekArcChangeListener(OnSeekArcChangeListener l) {
		mOnSeekArcChangeListener = l;
	}

	public void setProgress(int progress) {
		updateProgress(progress, false);
	}

	public int getProgressWidth() {
		return mProgressWidth;
	}

	public void setProgressWidth(int mProgressWidth) {
		this.mProgressWidth = mProgressWidth;
		mProgressPaint.setStrokeWidth(mProgressWidth);
	}

	public int getArcWidth() {
		return mArcWidth;
	}

	public void setArcWidth(int mArcWidth) {
		this.mArcWidth = mArcWidth;
		mArcPaint.setStrokeWidth(mArcWidth);
	}

	public int getArcRotation() {
		return mRotation;
	}

	public void setArcRotation(int mRotation) {
		this.mRotation = mRotation;
		updateThumbPosition();
	}

	public int getStartAngle() {
		return mStartAngle;
	}

	public void setStartAngle(int mStartAngle) {
		this.mStartAngle = mStartAngle;
		updateThumbPosition();
	}

	public int getSweepAngle() {
		return mSweepAngle;
	}

	public void setSweepAngle(int mSweepAngle) {
		this.mSweepAngle = mSweepAngle;
		updateThumbPosition();
	}

	public void setRoundedEdges(boolean isEnabled) {
		mRoundedEdges = isEnabled;
		if (mRoundedEdges) {
			mArcPaint.setStrokeCap(Paint.Cap.ROUND);
			mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
		} else {
			mArcPaint.setStrokeCap(Paint.Cap.SQUARE);
			mProgressPaint.setStrokeCap(Paint.Cap.SQUARE);
		}
	}

	public void setTouchInSide(boolean isEnabled) {
		int thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
		int thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
		mTouchInside = isEnabled;
		if (mTouchInside) {
			mTouchIgnoreRadius = (float) mArcRadius / 4;
		} else {
			// Don't use the exact radius makes interaction too tricky
			mTouchIgnoreRadius = mArcRadius
					- Math.min(thumbHalfWidth, thumbHalfheight);
		}
	}

	public void setClockwise(boolean isClockwise) {
		mClockwise = isClockwise;
	}
}
