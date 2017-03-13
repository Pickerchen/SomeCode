package com.sen5.ocup.gui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.sen5.ocup.R;
import com.sen5.ocup.util.DBManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class ChartView extends View implements OnTouchListener {
	
	private static final String TAG = ChartView.class.getSimpleName();
	private static final String COLOR_GRAY = "#7a7a7a";
	private static final String COLOR_BLUE = "#adadae";
	private static final String COLOR_BLACK = "#3d3d3d";
	private static final String COLOR_WHITE = "#ffffff";
	private static final String COLOR_RED = "#ff0000";
	private static final String COLOR_WATER = "#adadae";
	private static final float SIZE_LINE = 2.0f;
	private static final float SIZE_CIRCLE = 2.0f;
	private static final String DATA_FORMAT = "yyyy-MM-dd";
	
	
	private Context mContext;
	private Paint mPaint = null;
	private Canvas mCanvas = null;
	private DBManager mDBManager;
	private String mVerticalName = "";
	private String mHorizontalName = "";
	private String mLineDescribe = "";
	/**
	 * 纵线的数目，默认24条，每小时一条
	 */
	private int mVerticalCount = 24;
	/**
	 * 横线的数目，默认为6条
	 */
	private int mHorizontalCount = 6;
	/**
	 * 虚线的大小
	 */
	private float mDottedLine = 5.0f;
	/**
	 * 每刻度的水量
	 */
	private float mTxtSizeBig = 0;
	private float mTxtSize = 0;
	private int mEachScale = 0;
	/**
	 *每二分之一刻度的高度 
	 */
	private float mGapsEveryLineY = 0.0f;
	
	/**
	 * 每刻度的宽度
	 */
	private float mGapsEveryLineX = 0.0f;
	private int mChartHeight = 0;
	private int mChartWidth = 0;
	private int mScreenWidth;
	private int mFstGapsY = 0;
	private int mSndGapsY = 0;
	private int mTrdGapsY = 0;
	private int mFurGapsY = 0;
	private float mTxtGapsX = 0.0f;
	private float mTimeGapsX = 0.0f;
	private float mVerticalGapsX = 0.0f;
	private float mRadius = 0.0f;
	private float mLinePadding = 0.0f;
	private float mScrollOldDistance = 0.0f;
	/**
	 * 通过这个变量来控制滑动折线图时显示的位置
	 */
	private float mScrollDistance = 0.0f;
	private int mWhichHour = -1;
	private int mDrinkSum = 0;
	private float mDownDistance = 0.0f;
	private float mDensity;
	/**
	 * 是否显示数据
	 */
	private boolean mDisplayData = true;
	private ArrayList<Integer> mDribkData;
	
	public ChartView(Context context) {
		this(context, null);
	}
	
	public ChartView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
		initData();
		mDBManager = new DBManager(mContext);
		setOnTouchListener(this);
		
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChartView, defStyle, 0);
		mVerticalName = typedArray.getString(R.styleable.ChartView_vertical_axis_name);
		//时间
		mHorizontalName = typedArray.getString(R.styleable.ChartView_horizontal_axis_name);
		//饮水总量
		mLineDescribe = typedArray.getString(R.styleable.ChartView_line_describe);
		mVerticalCount = typedArray.getInt(R.styleable.ChartView_vertical_count, 24);
		mHorizontalCount = typedArray.getInt(R.styleable.ChartView_horizontal_count, 6);
		mEachScale = typedArray.getInt(R.styleable.ChartView_each_scale, 300);
		typedArray.recycle();
		
		// 整个View的宽度为屏幕的宽度减去左右Padding
//		int nPadding = context.getResources().getDimensionPixelOffset(R.dimen.chart_padding);
		mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
		int nScreenHeight = context.getResources().getDisplayMetrics().heightPixels;
		// 计算图表的宽度和高度
		mChartWidth = mScreenWidth * 3;
		mChartHeight = 2*(nScreenHeight /5);
		mTrdGapsY = mSndGapsY + mChartHeight + mFstGapsY;
		mFurGapsY = mTrdGapsY + mFstGapsY;
		// 计算每个间隔所占的距离
		mGapsEveryLineX = mChartWidth / (mVerticalCount - 1);
		// 总的水量
		mDrinkSum = mHorizontalCount * mEachScale;
		// 初始化数据
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(DATA_FORMAT);
		String strCurrentDate = sdf.format(date);
		updateDataByTime(dateToMillion(strCurrentDate));
	}

	/**
	 * 初始化数据，使得数据适应屏幕
	 */
	private void initData() {
		
		mFstGapsY = mContext.getResources().getDimensionPixelSize(R.dimen.chart_gaps);
		Log.e(TAG, "GapsY:::" + mFstGapsY);
		mSndGapsY = mFstGapsY * 2;
		mDottedLine = mContext.getResources().getDimensionPixelSize(R.dimen.dotted_line);
		mTxtSizeBig = mContext.getResources().getDimensionPixelSize(R.dimen.chart_txt_size_big);
		mTxtSize = mContext.getResources().getDimensionPixelSize(R.dimen.chart_txt_size);
		
		mDensity = getResources().getDisplayMetrics().density;
		mTxtGapsX = 5 * mDensity;
		mTimeGapsX = 35 * mDensity;
		mVerticalGapsX = mTimeGapsX + 16 * mDensity;
		mRadius = 5 * mDensity;
		mLinePadding = mTimeGapsX;
		
	}
	
	/**
	 * 是否在DOT上显示数值
	 * @param isDisplayData
	 */
	public void isDisplayData(boolean isDisplayData) {
		mDisplayData = isDisplayData;
	}
	
	/**
	 * 根据时间设定坐标轴显示的位置
	 */
	public void updateDataByTime(long nDate) {
		
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(DATA_FORMAT);
		String strCurrentDate = sdf.format(date);
		if(nDate == dateToMillion(strCurrentDate)) {
			Calendar calendar = Calendar.getInstance();
			mWhichHour = calendar.get(Calendar.HOUR_OF_DAY);
			// 把当前的时间显示给用户看
			if(4 < mWhichHour) {
				mScrollDistance = -(mWhichHour - 4) * mGapsEveryLineX;
				
			} else {
				mScrollDistance = 0;
				
			}
		} else {
			mWhichHour = -1;
			// 历史数据定位到24点
			float fDistance =  -(19 * mGapsEveryLineX);
			if(fDistance < -(mChartWidth - mScreenWidth / 2)) {
				fDistance = -(mChartWidth - mScreenWidth / 2);
			}
			mScrollDistance = fDistance;
//			mScrollDistance = 0;
		}
		mScrollOldDistance = mScrollDistance;
		mDribkData = new ArrayList<Integer>(mDBManager.queryDrinkData(nDate));
		invalidate();
	}
	
	/**
	 * 把String形式的时间转化成毫秒long形式
	 * @param date
	 * @return
	 */
	private long dateToMillion(String date) {
		long time=0;
		SimpleDateFormat sdf = new SimpleDateFormat(DATA_FORMAT);
		Date d2 = null;
		try {
			d2 = sdf.parse(date);
			time = d2.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		Log.e(TAG, "OnDrow");
		mCanvas = canvas;
		if(null == mPaint) {
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
//			mPaint.setFilterBitmap(true);
			mPaint.setStrokeWidth(SIZE_LINE);
		}
		
		drawTitle();
		
		drawHorizontalLine();
		
//		drawTime();
		
		drawScrollData();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int nLayoutHeight = mFurGapsY + 80;
		setMeasuredDimension(mScreenWidth, nLayoutHeight);
	}
	
	/**
	 * 画标题
	 */
	private void drawTitle() {
		
		mPaint.setColor(Color.parseColor(COLOR_GRAY));
//		mCanvas.drawLine(0, 0, mScreenWidth, 0, mPaint);
		
		mPaint.setColor(Color.parseColor(COLOR_BLACK));
		mPaint.setTextSize(mTxtSizeBig);
//		mCanvas.drawText(mVerticalName, 0, mFstGapsY, mPaint);
		
		float fLeft = mScreenWidth - mPaint.measureText(mLineDescribe)*2;
		mCanvas.drawText(mLineDescribe, fLeft, mFstGapsY, mPaint);
		mPaint.setColor(Color.parseColor(COLOR_BLUE));
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(SIZE_CIRCLE);
//		float fY = mFstGapsY - 4.5f * mDensity;
//		fLeft = fLeft - 2 * mRadius - 1.5f * mDensity;
//		mCanvas.drawCircle(fLeft, fY, mRadius, mPaint);
		
	}
	
	/**
	 * 画横向线、值
	 */
	private void drawHorizontalLine() {
		
//		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.parseColor(COLOR_GRAY));
		Path path = new Path();
		PathEffect effects = new DashPathEffect(new float[]{mDottedLine, mDottedLine}, 1);
		mPaint.setPathEffect(effects);
		// 第一条虚线
//        path.moveTo(mLinePadding, mSndGapsY);
//        path.lineTo(mScreenWidth - mLinePadding, mSndGapsY);
//        mCanvas.drawPath(path, mPaint);
        // mHorizontalCount
        mGapsEveryLineY = mChartHeight / (mHorizontalCount * 2 + 1);
//        for(int index = 0; index < mHorizontalCount; index++) {
//        	 float fGaps = mSndGapsY + mGapsEveryLineY * (2 * index + 1);
//        	 path.moveTo(mLinePadding, fGaps);
//             path.lineTo(mScreenWidth - mLinePadding, fGaps);
//             mCanvas.drawPath(path, mPaint);
//        }
        // 画最后一条实线
        effects = new DashPathEffect(new float[]{0, 0}, 1);
        mPaint.setPathEffect(effects);
        mCanvas.drawLine(0, mSndGapsY + mChartHeight, mScreenWidth, mSndGapsY + mChartHeight, mPaint);
        
        // 画刻度
        mPaint.setStyle(Paint.Style.FILL);
//        float fGap = mSndGapsY + mGapsEveryLineY;
//        mCanvas.drawText(String.valueOf(mDrinkSum), 0, fGap, mPaint);
//        fGap += mGapsEveryLineY * 2 * (mHorizontalCount - 1);
//        mCanvas.drawText(String.valueOf(mEachScale), 0, fGap, mPaint);
	}
	
	/**
	 * 画"时间"、"比例"这两个词条
	 */
//	private void drawTime() {
//		float dimension = getResources().getDimension(R.dimen.chartview_padding);
//		// 时间
//		mPaint.setTextSize(mTxtSize);
//		mCanvas.drawText(mHorizontalName, mTxtGapsX - dimension, mTrdGapsY, mPaint);
//		// 比例
//		String strProportion = mContext.getString(R.string.percent_graph);
//		mCanvas.drawText(strProportion, mTxtGapsX - dimension, mFurGapsY, mPaint);
//	}
	
	/**
	 * 画曲线、圆点、填充色、竖线、时间、画水柱
	 */
	private void drawScrollData() {
		
		// 确认画布的大小
		mCanvas.clipRect(0, mSndGapsY, mScreenWidth, mFurGapsY);
//		mCanvas.drawColor(Color.BLUE);
//		mGapsEveryLineX = mChartWidth / (mVerticalCount - 1);
		// 时间
		String[] times = mContext.getResources().getStringArray(R.array.times);
		for(int i = 0; i < mVerticalCount; i++) {
			float fGapX = mTimeGapsX + mGapsEveryLineX * i + mScrollDistance;
			mCanvas.drawText(times[i], fGapX, mTrdGapsY, mPaint);
		}
		
		for (int i = 0; i < mDribkData.size(); i++) {
			float fGapX = mVerticalGapsX + mGapsEveryLineX * i + mScrollDistance;
			// 竖线
//			mCanvas.drawLine(fGapX, mSndGapsY, fGapX, mSndGapsY + mChartHeight, mPaint);
			// 比例
			fGapX += -(7.0f * mDensity);
			int nProportion = mDribkData.get(i) * 100 / mDrinkSum ;
			mCanvas.drawText(nProportion + "%", fGapX, mFurGapsY, mPaint);
			if(mWhichHour == i) {
				break;
			}
		}
		
		// 线的数目为总的线数减一
		for (int i = 0; i < mDribkData.size() - 1; i++) {
			
			if(mWhichHour == i) {
				break;
			}
			// 变化曲线
			mPaint.setColor(Color.parseColor(COLOR_BLUE));
			// 起点
			float fStartGapX = mVerticalGapsX + mGapsEveryLineX * i + mScrollDistance;
			float fStartHeight = ((mDribkData.get(i) * 100 / mDrinkSum) * (mChartHeight - mGapsEveryLineY)) / 100;
			float fStartGapY = mSndGapsY + mGapsEveryLineY + (mChartHeight - mGapsEveryLineY) - fStartHeight;
			// 终点
			float fEndGapX = mVerticalGapsX + mGapsEveryLineX * (i + 1) + mScrollDistance;
			float fEndHeight = ((mDribkData.get(i + 1) * 100 / mDrinkSum) * (mChartHeight - mGapsEveryLineY)) / 100;
			float fEndGapY = mSndGapsY + mGapsEveryLineY + (mChartHeight - mGapsEveryLineY) - fEndHeight;
			mCanvas.drawLine(fStartGapX, fStartGapY, fEndGapX, fEndGapY, mPaint);
			
			// 画水柱
			mPaint.setColor(Color.parseColor(COLOR_WATER));
			Path path = new Path();
			CornerPathEffect effects = new CornerPathEffect(10);

			path.reset();
			path.moveTo(fStartGapX, fStartGapY);
			path.lineTo(fEndGapX, fEndGapY);
			path.lineTo(fEndGapX, mSndGapsY + mChartHeight);
			path.lineTo(fStartGapX, mSndGapsY + mChartHeight);
//			mPaint.setPathEffect(effects);
			mCanvas.drawPath(path, mPaint);
			
		}
		
		// dot
		for (int i = 0; i < mDribkData.size(); i++) {
			float fGapX = mVerticalGapsX + mGapsEveryLineX * i + mScrollDistance;
			if(mDribkData.get(i) > 0) {
				// 计算点的Y轴距离
				// 点所占的高度
				float fHeight = ((mDribkData.get(i) * 1000 / mDrinkSum) * (mChartHeight - mGapsEveryLineY)) / 1000;
				float fGapY = mSndGapsY + mGapsEveryLineY + (mChartHeight - mGapsEveryLineY) - fHeight;

				// 显示每个点的数据
				if(mDisplayData) {
					mPaint.setColor(Color.parseColor(COLOR_GRAY));
					if(fGapY > (mSndGapsY + mGapsEveryLineY)) {
						mCanvas.drawText(String.valueOf(mDribkData.get(i))
								, fGapX - (10.0f * mDensity)
								, fGapY - (18.0f * mDensity)
								, mPaint);
					} else {
						mCanvas.drawText(String.valueOf(mDribkData.get(i))
								, fGapX - (10.0f * mDensity)
								, fGapY + (38.0f * mDensity)
								, mPaint);
					}
				}
				if(mWhichHour != i) {
					// 空心蓝圈
//					mPaint.setColor(Color.parseColor(COLOR_BLUE));
//					mPaint.setStyle(Paint.Style.STROKE);
//					mCanvas.drawCircle(fGapX, fGapY, mRadius, mPaint);
//					// 实心白圈
//					mPaint.setColor(Color.parseColor(COLOR_WHITE));
//					mPaint.setStyle(Paint.Style.FILL);
//					mCanvas.drawCircle(fGapX, fGapY, mRadius - 1, mPaint);
				} else {
//					// 实心蓝圈
//					mPaint.setColor(Color.parseColor(COLOR_BLUE));
//					mPaint.setStyle(Paint.Style.FILL);
//					mCanvas.drawCircle(fGapX, fGapY, mRadius, mPaint);
					break;
				}
			} else {
				float fGapY = mSndGapsY + mChartHeight;

				if(mWhichHour != i) {
					// 空心红圈
//					mPaint.setColor(Color.parseColor(COLOR_RED));
//					mPaint.setStyle(Paint.Style.STROKE);
//					mCanvas.drawCircle(fGapX, fGapY, mRadius, mPaint);
					// 实心白圈
//					mPaint.setColor(Color.parseColor(COLOR_WHITE));
//					mPaint.setStyle(Paint.Style.FILL);
//					mCanvas.drawCircle(fGapX, fGapY, mRadius - 1, mPaint);

				} else {
					// 实心红圈
//					mPaint.setColor(Color.parseColor(COLOR_RED));
//					mPaint.setStyle(Paint.Style.FILL);
//					mCanvas.drawCircle(fGapX, fGapY, mRadius, mPaint);
					break;
				}
			}
		}
	
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownDistance = event.getX();
			break;
			
		case MotionEvent.ACTION_MOVE:
			if(event.getY() > mSndGapsY && event.getY() < mFurGapsY) {
				Log.e(TAG, "Scroll:" + event.getX());
				mScrollDistance = mScrollOldDistance + event.getX() - mDownDistance;
				if(mScrollDistance > 0) {
					mScrollDistance = 0;
				} else if(mScrollDistance < -(mChartWidth - mScreenWidth / 2)) {
					mScrollDistance = -(mChartWidth - mScreenWidth / 2);
				}
				invalidate();
			}
			break;
			
		case MotionEvent.ACTION_UP:
		default:
			mScrollOldDistance = mScrollDistance;
			break;
		}
		return true;
	}
	
}
