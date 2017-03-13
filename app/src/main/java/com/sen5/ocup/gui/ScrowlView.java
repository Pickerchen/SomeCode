package com.sen5.ocup.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.sen5.ocup.R;
import com.sen5.ocup.callback.CustomInterface.IDrawPoint;
import com.sen5.ocup.callback.CustomInterface.IDrawScrawl;
import com.sen5.ocup.struct.Point;
import com.sen5.ocup.util.BitmapUtil;
import com.sen5.ocup.util.RoundUtil;

/**
 * 
 * 涂鸦
 * 
 */
// public class ScrowlView extends SurfaceView implements SurfaceHolder.Callback
// {
public class ScrowlView extends View {
	private static final String TAG = "ScrowlView";
	// private SurfaceHolder sfh;
	private float w = 0;
	private float h = 0;
	/**
	 * 画笔
	 */
	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final static int Color_blue = Color.rgb(44, 175, 246);
	private final static int Color_gray = Color.rgb(122, 122, 122);
	private final static int Color_black = Color.rgb(61, 61, 61);
	/**
	 * 点集有几行
	 */
	private final int count_pointX = 8;
	/**
	 * 点集有几列
	 */
	private final int count_pointY = 18;
	/**
	 * 点
	 */
	private Point[][] mPoints;
	/**
	 * 点间距
	 */
	private static final int distance = 3;
	/**
	 * 圆的半径
	 */
//	private float r = 0;
	/**
	 * 选中的点集合
	 */
	private List<Point> sPoints = new ArrayList<Point>();
	/**
	 * 圆点初始状态时的图片
	 */
	// private Bitmap locus_round_original;
	/**
	 * 圆点点击时的图片
	 */
	// private Bitmap locus_round_click;
	/**
	 * 清除痕迹的时间
	 */
	private long CLEAR_TIME = 0;

	public boolean isCache = false;
	/**
	 * 画板当前的模式（橡皮擦 or 画笔）
	 */
	private int mode = MODE_PEN;
	/**
	 * 是否是可画模式
	 */
	public boolean isDraw = true;

	/**
	 * 显示模式： 涂鸦 or 动画
	 */
	private int mDisplayMode = 0;
	private final static int DISPLAYMODE_SCROWL = 0;
	private final static int DISPLAYMODE_ANIMAL = 1;

	/**
	 * 标识手绘 or 显示 模式 (显示模式下不显示 未选中的点，选中的点为黑色
	 */
	private int mScrawlMode = 0;
	private final static int SCRAWLMODE_SCRAWL = 0;
	private final static int SCRAWLMODE_DISPLAY = 1;

	/**
	 * 动画模式下所有点的集合 一个元素相当于一页的点集合
	 */
	private ArrayList<Point[][]> mPointAnimal = new ArrayList<Point[][]>();
	private int mCurPage;// 当前在画第几页

	private IDrawScrawl iDrawScrawl;
	private IDrawPoint iDrawPoint;
	private String points;// adapter传过来需要画的点
	private float roundW;// 圆点的半径

	public ScrowlView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
	}

	public ScrowlView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d(TAG, "ScrowlView()----------isCache=" + isCache);
		setWillNotDraw(false);
	}

	public ScrowlView(Context context) {
		super(context);
		setWillNotDraw(false);
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (!isCache) {
			initCache();
		}

		if (mDisplayMode == DISPLAYMODE_ANIMAL) {
			drawToCanvas(canvas, mPoints);// 清除画布
			if (mCurPage > (mPointAnimal.size() - 1)) {
				mCurPage = 0;
			}
			Log.d(TAG, "onDraw animal page==" + mCurPage);
			drawToCanvas(canvas, mPointAnimal.get(mCurPage));
			mCurPage++;
		} else {
			drawToCanvas(canvas, mPoints);
		}
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		super.setOnLongClickListener(l);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		super.setOnClickListener(l);
	}

	/**
	 * 
	 * 功能描述: 图像绘制<br>
	 * Canvas canvas
	 * 
	 * @param canvas
	 */
	public void drawToCanvas(Canvas canvas, Point[][] points) {
		// 画所有点
		for (int i = 0; i < points.length; i++) {
			for (int j = 0; j < points[i].length; j++) {
				Point p = points[i][j];
				if (p != null) {
					if (p.state == Point.STATE_CHECK) {
						// 选中状态
						// canvas.drawBitmap(locus_round_click, p.x - r, p.y -
						// r, mPaint);
						if (mScrawlMode == SCRAWLMODE_SCRAWL) {
							mPaint.setColor(Color_blue);
						} else {
							mPaint.setColor(Color_black);
						}
						canvas.drawCircle(p.x, p.y, roundW, mPaint);
					} else {
						// 未选中状态
						if (mScrawlMode == SCRAWLMODE_SCRAWL) {
							// canvas.drawBitmap(locus_round_original, p.x - r,
							// p.y - r, mPaint);
							mPaint.setColor(Color_gray);
							canvas.drawCircle(p.x, p.y, roundW, mPaint);
						}
					}
				}
			}
		}
	}

	/**
	 * 初始化Cache信息
	 * 
	 * @param canvas
	 */
	public void initCache() {
		mPaint.setStyle(Paint.Style.FILL);

		w = this.getWidth();
		h = this.getHeight();
		Log.d(TAG, "initCache()---w==" + w + "  h==" + h);
		// locus_round_original =
		// BitmapFactory.decodeResource(this.getResources(),
		// R.drawable.point_gray);
		// locus_round_click = BitmapFactory.decodeResource(this.getResources(),
		// R.drawable.point_blue);

		mPoints = new Point[count_pointX][count_pointY];

		// 计算圆圈图片的大小
		float roundMinW = Math.min((w - (count_pointY - 1) * distance) / (count_pointY * 2.0f) * 2, (h - (count_pointX - 1) * distance) / (count_pointX * 2.0f) * 2);
		roundW = roundMinW / 2.f;

		// if (locus_round_original != null) {
		// if (locus_round_original.getWidth() > roundMinW) {
		// // 取得缩放比例，将所有的图片进行缩放
		// float sf = roundMinW * 1.0f / locus_round_original.getWidth();
		// locus_round_original = BitmapUtil.zoom(locus_round_original, sf);
		// locus_round_click = BitmapUtil.zoom(locus_round_click, sf);
		// roundW = locus_round_original.getWidth() / 2;
		// }
		// // 获得圆形的半径
		// r = locus_round_original.getHeight() / 2;// roundW;

		initPoints(mPoints);
		// }
		isCache = true;
		if (null != iDrawPoint) {
			iDrawPoint.setPoints(this, points);
		}
	}

	public void initPoints(Point[][] pp) {
		float x0 = (w - count_pointY * roundW * 2 - (count_pointY - 1) * distance) / 2.0f + roundW;
		float y0 = (h - count_pointX * roundW * 2 - (count_pointX - 1) * distance) / 2.0f + roundW;
		// new 出每个点
		for (int i = 0; i < pp.length; i++) {
			float temp_y = 0;
			if (i > 0) {
				temp_y = pp[i - 1][0].y + distance + roundW * 2;
			} else {
				temp_y = y0;
			}
			for (int j = 0; j < pp[i].length; j++) {
				if (j > 0) {
					pp[i][j] = new Point(pp[0][j - 1].x + distance + roundW * 2, temp_y);
				} else {
					pp[i][j] = new Point(x0, temp_y);
				}
			}
		}
		// 为每个点设置初始状态
		int k = 0;
		for (Point[] ps : pp) {
			for (Point p : ps) {
				p.index = k;
				k++;
			}
		}
	}

	/**
	 * 
	 * 检查点是否被选择
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private Point checkSelectPoint(float x, float y) {
		for (int i = 0; i < mPoints.length; i++) {
			for (int j = 0; j < mPoints[i].length; j++) {
				Point p = mPoints[i][j];
				if (RoundUtil.checkInRound(p.x, p.y, roundW, (int) x, (int) y)) {
					return p;
				}
			}
		}
		return null;
	}

	/**
	 * 重置点状态
	 */
	private void reset() {
		for (Point p : sPoints) {
			p.state = Point.STATE_NORMAL;
		}
		sPoints.clear();

		if (null != iDrawScrawl) {
			iDrawScrawl.reset();
		}
	}

	/**
	 * 向选中点集合中添加一个点
	 * 
	 * @param point
	 */
	private void addPoint(Point point) {
		this.sPoints.add(point);
	}

	/**
	 * 将选中的点转换为String
	 * 
	 * @param points
	 * @return
	 */
	private String toPointString() {
		StringBuffer sf = new StringBuffer();
		for (Point p : sPoints) {
			sf.append(",");
			sf.append(p.index);
		}
		if (sf.length() > 0) {
			return sf.deleteCharAt(0).toString();
		} else {
			return sf.toString();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isDraw) {
			float ex = event.getX();
			float ey = event.getY();
			boolean isFinish = false;
			Point p = null;

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: // 点下
				p = checkSelectPoint(ex, ey);
				// 如果正在清除密码,则取消
				if (task != null) {
					task.cancel();
					task = null;
					Log.d("task", "touch cancel()");
				}
				break;
			case MotionEvent.ACTION_MOVE: // 移动
				p = checkSelectPoint(ex, ey);
				break;
			case MotionEvent.ACTION_UP: // 提起
				p = checkSelectPoint(ex, ey);
				isFinish = true;
				break;
			}

			if (!isFinish && p != null) {// && checking
				if (mode == MODE_PEN) {
					p.state = Point.STATE_CHECK;
					if (!sPoints.contains(p)) {
						addPoint(p);
					}

				} else {
					p.state = Point.STATE_NORMAL;
					if (sPoints.contains(p)) {
						sPoints.remove(p);
					}
				}
			}

			if (isFinish) {
				if (this.sPoints.size() < 1) {
					this.reset();
				}
				if (mCompleteListener != null) {
					mCompleteListener.onComplete(toPointString());
				}
			}
			invalidate();
		}
		return true;
	}

	private Timer timer = new Timer();
	private TimerTask task = null;

	/**
	 * 清除密码
	 */
	public void clearPassword() {
		clearPassword(CLEAR_TIME);
	}

	/**
	 * 清除密码
	 */
	public void clearPassword(final long time) {
		if (time > 1) {
			if (task != null) {
				task.cancel();
				Log.d("task", "clearPassword cancel()");
			}
			invalidate();
			task = new TimerTask() {
				public void run() {
					reset();
					invalidate();
				}
			};
			Log.d("task", "clearPassword schedule(" + time + ")");
			timer.schedule(task, time);
		} else {
			reset();
			invalidate();
		}
	}

	/**
	 * 设置密码
	 * 
	 * @param password
	 */
	public void resetPassWord(String password) {
		SharedPreferences settings = this.getContext().getSharedPreferences(this.getClass().getName(), 0);
		Editor editor = settings.edit();
		editor.putString("password", password);
		editor.commit();
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	private OnCompleteListener mCompleteListener;

	/**
	 * @param mCompleteListener
	 */
	public void setOnCompleteListener(OnCompleteListener mCompleteListener) {
		this.mCompleteListener = mCompleteListener;
	}

	/**
	 * 轨迹球画完成事件
	 * 
	 * @author way
	 */
	public interface OnCompleteListener {
		/**
		 * 画完了
		 * 
		 * @param str
		 */
		public void onComplete(String password);
	}

	public String getmPoints() {
		return toPointString();
	}

	/**
	 * 设置选中的点
	 * 
	 * @param selPoint
	 */
	public void setmPoints(String points) {
		mScrawlMode = SCRAWLMODE_DISPLAY;
		if (null != points) {
			reset();
			int pointsLength = points.length();
			Log.d(TAG, "setmPoints       selPoint.length()== " + pointsLength);

			if (pointsLength > count_pointX * count_pointY) {
				// 有多页，设置显示模式为动画
				mDisplayMode = DISPLAYMODE_ANIMAL;
				int page = (pointsLength - 1) / (count_pointX * count_pointY) + 1;
				for (int i = 0; i < page; i++) {
					Point[][] p = getPoints_perPage(points.substring(i * count_pointX * count_pointY, (1 + i) * count_pointX * count_pointY));
					mPointAnimal.add(p);
				}
				getHandler().post(mRefreshProgressRunnable);
			} else {
				mDisplayMode = DISPLAYMODE_SCROWL;
				mPoints = getPoints_perPage(points);
				postInvalidate();
			}
		}
	}

	/**
	 * 获取每页点集合的状态
	 * 
	 * @param selPoint
	 */
	private Point[][] getPoints_perPage(String selPoint) {

		Point[][] points = new Point[count_pointX][count_pointY];
		initPoints(points);

		StringBuffer str_selPoint = new StringBuffer("");
		char[] char_point = selPoint.toCharArray();

		for (int i = 0; i < char_point.length; i++) {
			if (char_point[i] == 49) {// 选中的点 assci 1
				if ((i + 1) % count_pointX == 0) {
					str_selPoint.append((count_pointX - 1) * count_pointY + i / 8);
				} else {
					str_selPoint.append(((i + 1) % count_pointX - 1) * count_pointY + i / 8);
				}
				str_selPoint.append(",");
			}
		}
		Log.d(TAG, "getPoints_perPage  str_selPoint==" + str_selPoint);
		String[] selPointsIndex = str_selPoint.toString().split(",");
		int index = 0;// 选中点的位置
		int x = 0; // 选中点的横坐标
		int y = 0;// 选中点的纵坐标
		for (int i = 0; i < selPointsIndex.length; i++) {
			index = Integer.parseInt(selPointsIndex[i]);
			Log.d(TAG, "getPoints_perPage   index==" + index);
			x = index / count_pointY;
			y = index - x * count_pointY;
			Log.d(TAG, "  x==" + x + "  y==" + y);
			if (null == points[x][y]) {
				Log.d(TAG, "getPoints_perPage  null == mPoints[x][y]");
			} else {
				points[x][y].state = Point.STATE_CHECK;
				addPoint(points[x][y]);
			}
		}
		return points;
	}

	private RefreshProgressRunnable mRefreshProgressRunnable;

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Log.d(TAG, "onAttachedToWindow---------");
		mRefreshProgressRunnable = new RefreshProgressRunnable();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		Log.d(TAG, "onDraw--------onDetachedFromWindow");
		getHandler().removeCallbacks(mRefreshProgressRunnable);
	}

	private class RefreshProgressRunnable implements Runnable {
		public void run() {
			synchronized (ScrowlView.this) {
				postInvalidate();
				getHandler().postDelayed(this, 750);
			}
		}
	}

	public static final int MODE_PEN = 0;
	public static final int MODE_ERASER = 1;

	public void setIDrawScrawl(IDrawScrawl iDrawScrawl) {
		this.iDrawScrawl = iDrawScrawl;
	}

	public void setIDrawPoint(IDrawPoint iDrawPoint, String points) {
		this.iDrawPoint = iDrawPoint;
		this.points = points;
	}
}
