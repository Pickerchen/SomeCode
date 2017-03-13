package com.sen5.ocup.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sen5.ocup.callback.CustomInterface.ISendPoint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class LineView extends SurfaceView implements SurfaceHolder.Callback {
	private final static String X_KEY = "Xpos";
	private final static String Y_KEY = "Ypos";
	private static final String TAG = "LineView";
	
	private  int gapY = 50;// 横线的间距
	private  int gapX = 100;// 竖线的间距

	private final int left = 30;// 原点的横坐标
	private final int top = 30;// 原点的纵坐标
	
	private final int count_Xline = 10;//一共有多少横线
	private final int count_Yline = 10;//一共有多少竖线
	
	private final int WATER_COLOR = Color.rgb(38, 158, 241);
	
	private int mWidth;
	private int mHeight;

	private List<Map<String, Integer>> mListPoint = new ArrayList<Map<String, Integer>>();

	Paint mPaint = new Paint();
	private SurfaceHolder sfh;
	private ISendPoint iSendPoint;

	public LineView(Context context) {
		super(context);
	}

	public LineView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setZOrderOnTop(true);// 设置置顶（不然实现不了透明）
		sfh = this.getHolder();
		sfh.addCallback(this);
		sfh.setFormat(PixelFormat.TRANSLUCENT);// 设置背景透明
	}

	/**
	 * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i("系统消息", "surfaceCreated");
		mWidth = getWidth();
		mHeight = getHeight();
		gapX = mWidth/count_Yline;
		gapY = mHeight/count_Xline;
		iSendPoint.sendPoint();//发送要画的点
		new Thread(new Runnable() {
			@Override
			public void run() {
				clearCanvas();
				initCoordinateDraw();
			}
		}).start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Log.i("系统信息", "surfaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i("系统信息", "surfaceDestroyed");
	}

	/**
	 * @param curX
	 *            which x position you want to draw.
	 * @param curY
	 *            which y position you want to draw.
	 * @see all you put x-y position will connect to a line.
	 */
	public void setLinePoint(int curX, int curY) {
		Map<String, Integer> temp = new HashMap<String, Integer>();
		temp.put(X_KEY, curX);
		temp.put(Y_KEY, curY);
		mListPoint.add(temp);  
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				for (int i = 0; i < mListPoint.size(); i++) {
					if (mListPoint.size() ==1) {
						initCoordinateDraw();
					}
					pointDraw(i);
				}
			}
		}).start();
		
	}

	private void pointDraw(int i){
		Log.d(TAG, "pointDraw()------i=="+i+" startx=="+( left+i*gapX)+"  startY=="+top+"  endX=="+( left+(i+1)*gapX)+"  endY=="+(top+gapY));
		Canvas canvas = sfh.lockCanvas(new Rect( left, top, left+i*gapX, top+gapY*(count_Xline-1)));// 范围选取正确
		mPaint.setColor(WATER_COLOR);
		mPaint.setStrokeWidth(5);
		mPaint.setAntiAlias(true);
		if (null != canvas) {
//			Path path = new Path();//定义一条路径
//			path.moveTo(left, top+ gapY * count_Xline);
			for (int index = 0; index < mListPoint.size(); index++) {
				if (index > 0) {
//					canvas.drawLine(left + mListPoint.get(index - 1).get(X_KEY), top + mListPoint.get(index - 1).get(Y_KEY), left + mListPoint.get(index).get(X_KEY), top
//							+ mListPoint.get(index).get(Y_KEY), mPaint);
					canvas.drawLine(left + mListPoint.get(index - 1).get(X_KEY), top + mListPoint.get(index - 1).get(Y_KEY), left + mListPoint.get(index).get(X_KEY), top
							+ mListPoint.get(index).get(Y_KEY), mPaint);
					canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
				}
//				path.lineTo(left + mListPoint.get(index).get(X_KEY), top + mListPoint.get(index).get(Y_KEY));
			}
//			path.lineTo( left + gapX * (count_Yline-1), top + gapY * count_Xline);
//			path.lineTo(left, top + gapY * (count_Xline-1));
//			canvas.drawPath(path, mPaint);
			sfh.unlockCanvasAndPost(canvas);
		}else{
			Log.d(TAG, "pointDraw()------------null ========= canvas");
		}

	}

	/**
	 * 画出坐标系
	 * 
	 * @param canvas
	 */
	private void coordinateDraw(Canvas canvas) {
		if (null == canvas) {
			return;
		}
		Log.d(TAG, "coordinateDraw---------");
		Paint mbackLinePaint = new Paint();// 用来画坐标系了
		mbackLinePaint.setColor(Color.WHITE);
		mbackLinePaint.setAntiAlias(true);
		mbackLinePaint.setStrokeWidth(3);
		mbackLinePaint.setStyle(Style.FILL);

		Paint mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setTextSize(18F);// 设置温度值的字体大小

		// 绘制坐标系
		for (int i = 0; i < count_Xline; i++) {
				canvas.drawLine(left, top + gapY * i, left + gapX *( count_Yline-1), top + gapY * i, mbackLinePaint);// 画坐标中的所有横线
				// canvas.drawText(temMin+space*i, 10, bottom-20*i, mTextPaint);
				// Math.round(((temMin + space * i) * 100) / 100.0);
				mTextPaint.setTextAlign(Align.RIGHT);
				canvas.drawText("" + 5 * i, left - 3, top + gapY * (count_Xline - 1-i), mTextPaint);
		}
		for (int i = 0; i < count_Yline; i++) {
				canvas.drawLine(left + gapX * i, top, left + gapX * i, top + gapY * (count_Yline-1), mbackLinePaint);// 画坐标中的所有竖线
				mTextPaint.setTextAlign(Align.CENTER);
				canvas.drawText("" + i, left + gapX * i, top + gapY * (count_Xline-1) + 18, mTextPaint);
		}
	}
	
	private void initCoordinateDraw(){
		Canvas canvas = sfh.lockCanvas();
		coordinateDraw(canvas);
		sfh.unlockCanvasAndPost(canvas);
	}
	
	/**
	 * 把画布擦干净，准备绘图使用。
	 */
	private void clearCanvas() {
		Canvas canvas = sfh.lockCanvas();
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);// 清除画布
		coordinateDraw(canvas);
		sfh.unlockCanvasAndPost(canvas);
	}
	
	public void setCallback(ISendPoint iSendPoint){
		this.iSendPoint = iSendPoint;
	}
}
