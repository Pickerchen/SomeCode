package com.sen5.ocup.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sen5.ocup.R;
import com.sen5.ocup.util.Tools;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyListViewPullDownAndUp extends ListView {
	
	private Context mContext;
	private static final String TAG = "MyListViewPullDownAndUp";
	 private GestureDetector mGestureDetector;  

	int firstVisibleItemIndex;// 屏幕显示的第一个item的索引值
	int lastVisibleItemIndex;// 屏幕能见的最后一个item的索引值
	private View header,footer;//头和脚的view
	private ImageView headerArrow,footerArrow;//向上的箭头和向下的箭头
	private ProgressBar headerProgressBar; //下拉时的进度条
	private TextView headerTitle;//头标题
	private TextView headerLastUpdated;//最后更新文本
	private ProgressBar footerProgressBar;//上拉时的进度条
	private TextView footerTitle;
	private TextView footerLastUpdated;

	//headerview
	private int headerWidth;
	private int headerHeight;
    //进度条的动画
	private Animation animation;
	private Animation reverseAnimation;

	//定义刷新的各个状态
	private static final int PULL_TO_REFRESH = 0;
	private static final int RELEASE_TO_REFERESH = 1;//释放立即刷新状态
	private static final int REFERESHING = 2;//正在刷新
	private static final int DONE = 3;//刷新完成
	private static final float RATIO = 3;//宽高比例

	private static boolean isBack = false;
	private boolean refereshEnable;//是否可以进行刷新
	private int state;//当前刷新状态

	boolean isRecorded;//是否记录
	float startY;
	float firstTempY = 0;
	float secondTempY = 0;
	RefreshListener rListener;//下拉刷新回调接口

	int pulltype;
/**
 * 是否支持上拉刷新
 */
	private boolean isPullUp;
	/**
	 * 是否支持下拉刷新
	 */
	private boolean isPullDown;
	public boolean isOnMeasure;
	
	private  int changestate_distance;

	public MyListViewPullDownAndUp(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MyListViewPullDownAndUp(Context context) {
		super(context);
		init(context);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//		Log.d("info", "MyListViewPullDownAndUp          onDraw()-----------");
	}

	/**
	 * 初始化listview
	 * 
	 * @param context
	 */
	private void init(Context context) {
		mContext = context;
		changestate_distance = Tools.dip2px(context, 2);
		mGestureDetector = new GestureDetector(context, new YScrollDetector());  
		
		animation = new RotateAnimation(-180.0f, 0.0f, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(150);
		animation.setFillAfter(true);
		animation.setInterpolator(new LinearInterpolator());//动画匀速进行
        //反转动画
		reverseAnimation = new RotateAnimation(0.0f, -180.0f, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setDuration(150);
		reverseAnimation.setFillAfter(true);
		reverseAnimation.setInterpolator(new LinearInterpolator());

		LayoutInflater inflater = LayoutInflater.from(context);
		header = inflater.inflate(R.layout.listview_header, null);
		headerArrow = (ImageView) header.findViewById(R.id.arrow);
		headerProgressBar = (ProgressBar) header.findViewById(R.id.progerssbar);
		headerTitle = (TextView) header.findViewById(R.id.title);
		headerLastUpdated = (TextView) header.findViewById(R.id.updated);
		headerArrow.setMinimumWidth(70);
		headerArrow.setMaxHeight(50);
        //初始化反方向布局
		footer = inflater.inflate(R.layout.listview_header, null);
		footerArrow = (ImageView) footer.findViewById(R.id.arrow);
		footerArrow.startAnimation(reverseAnimation);// 把箭头方向反转过来
		footerProgressBar = (ProgressBar) footer.findViewById(R.id.progerssbar);
		footerTitle = (TextView) footer.findViewById(R.id.title);
		footerLastUpdated = (TextView) footer.findViewById(R.id.updated);
		footerTitle.setText(context.getString(R.string.load_more));
		footerLastUpdated.setText(context.getString(R.string.load_more));
		footerArrow.setMinimumWidth(70);
		footerArrow.setMaxHeight(50);

		measureView(header);

		headerWidth = header.getMeasuredWidth();
		headerHeight = header.getMeasuredHeight();

		header.setPadding(0, -1 * headerHeight, 0, 0);// 设置 与界面上边距的距离
		header.invalidate();// 控件重绘

		footer.setPadding(0, -1 * headerHeight, 0, 0);// 设置与界面上边距的距离
		footer.invalidate();// 控件重绘
			addHeaderView(header);
//		if (isPullUp) {
			addFooterView(footer);
//		}

		state = DONE;
		refereshEnable = false;
	}

	private void measureView(View v) {
		ViewGroup.LayoutParams lp = v.getLayoutParams();
		if (lp == null) {
			lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int measureWidth = ViewGroup.getChildMeasureSpec(0, 0, lp.width);
		int measureHeight;
		if (lp.height > 0) {
			measureHeight = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
		} else {
			measureHeight = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.UNSPECIFIED);
		}
		v.measure(measureWidth, measureHeight);
	}

	public interface RefreshListener {
		public void pullDownRefresh();

		public void pullUpRefresh();
		
		public void pullUpStart();
	}

	public void setRefreshListener(RefreshListener l) {
		rListener = l;
		refereshEnable = true;
	}

	/**
	 * 处理下拉刷新完成后事项
	 */
	public void onPulldownRefreshComplete() {
		state = DONE;
		onHeaderStateChange();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		headerLastUpdated.setText(mContext.getString(R.string.lastest_load_time) + sdf.format(new Date()));
	}

	/**
	 * 处理上拉刷新完成后事项
	 */
	public void onPullupRefreshComplete() {
		state = DONE;
		onFooterStateChange();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		footerLastUpdated.setText(mContext.getString(R.string.lastest_load_time) + sdf.format(new Date()));
	}

	/**
	 * 中央控制台 几科所有的拉动事件皆由此驱动
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		lastVisibleItemIndex = getLastVisiblePosition() - 1;// 因为加有一尾视图，所以这里要咸一
		int totalCounts = getCount() - 1;// 因为给listview加了一头一尾）视图所以这里要减二
		if (refereshEnable) {

			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				Log.i(TAG, "ACTION_DOWN         ");

				firstTempY = ev.getY();
				isRecorded = false;
				if (getFirstVisiblePosition() == 0) {
					if (!isRecorded) {
						startY = ev.getY();
						Log.i(TAG, "touch_dwstartY=ev.getY();");
						isRecorded = true;
					}
					Log.i(TAG, "touch_downgetFirstVisiblePosition()==0");
				}
				break;
			case MotionEvent.ACTION_MOVE:
				Log.i(TAG, "touch_mv************************************************isPullUp=="+isPullUp+"   isPullDown=="+isPullDown);
				Log.i(TAG, "firstTempY=" + firstTempY + ",,,secondTempY=" + secondTempY + ",,,startY=" + startY);
				if (getFirstVisiblePosition() == 0  && isPullDown) {
					Log.i(TAG, "touch_mv-------------下拉刷新开始执行------------");
					Log.i(TAG, "touch_mv 下拉刷新 tempY=ev.getY()=" + secondTempY);
					firstTempY = secondTempY;
					secondTempY = ev.getY();
					if (!isRecorded) {
						startY = secondTempY;
						Log.i(TAG, "touch_mvstartY=tempY;");
						isRecorded = true;
					}
					if (state != REFERESHING) {
						Log.i(TAG, "touch_mv 下拉刷新 state != REFERESHING");
						if (state == DONE) {
							Log.i(TAG, "touch_mvstate == DONE");
							if (secondTempY - startY > 0) {
								// 刷新完成 /初始状态--》 进入 下拉刷新
								state = PULL_TO_REFRESH;
								onHeaderStateChange();
							}
						}
						if (state == PULL_TO_REFRESH) {
							Log.i(TAG, "touch_mvstate == PULL_TO_REFRESH");
							if ((secondTempY - startY) / RATIO > headerHeight && secondTempY - firstTempY > 3) {
								// 下啦刷新 --》 松开刷新
								state = RELEASE_TO_REFERESH;
								onHeaderStateChange();
							} else if (secondTempY - startY <= -changestate_distance) {
								// 下啦刷新 --》 回到 刷新完成
								state = DONE;
								onHeaderStateChange();
							}
						}
						if (state == RELEASE_TO_REFERESH) {
							Log.i(TAG, "touch_mvstate == RELEASE_TO_REFERESHheaderHeight=" + headerHeight);
							Log.i(TAG, "touch_mvstate == RELEASE_TO_REFERESHtempY=" + secondTempY + ",,,firstTempY=" + firstTempY);
							if (firstTempY - secondTempY > changestate_distance) {
								Log.i(TAG, "*touch_mv(tempY - startY) / RATIO < headerHeight && tempY - startY > 0");
								// 松开刷新 --》回到下拉刷新
								state = PULL_TO_REFRESH;
								isBack = true;// 从松开刷新 回到的下拉刷新
								onHeaderStateChange();
							} else if (secondTempY - startY <= -changestate_distance) {
								// 松开刷新 --》 回到 刷新完成
								state = DONE;
								onHeaderStateChange();
							}
						}

						Log.i(TAG, "touch_mvtempY =" + secondTempY + ",,,startY=" + startY);
						if (state == PULL_TO_REFRESH || state == RELEASE_TO_REFERESH) {
							header.setPadding(0, (int) ((secondTempY - startY) / RATIO - headerHeight), 0, 0);
						}
					} else {
						Log.i(TAG, "touch_mv 下拉刷新 state == REFERESHING");
					}
					Log.i(TAG, "touch_mv-------------下拉刷新执行完毕------------");
				}
				else if ((getLastVisiblePosition() == getCount() - 2 || getLastVisiblePosition() == getCount() - 1) && isPullUp) {
					Log.i(TAG, "touch_mv-------------上拉刷新开始执行------------");
					firstTempY = secondTempY;
					secondTempY = ev.getY();
					Log.i(TAG, "touch_mv 上拉刷新 tempY=ev.getY()=" + secondTempY);
					if (!isRecorded) {
						startY = secondTempY;
						Log.i("info", "touch_mvstartY=tempY;");
						isRecorded = true;
					}

					if (state != REFERESHING) {// 不是正在刷新状态
						Log.i(TAG, "touch_mv 上拉刷新 state != REFERESHING");
						if (state == DONE) {
							Log.i(TAG, "touch_mvstate == DONE");
							if (startY - secondTempY > 0) {
								// 刷新完成/初始状态 --》 进入 下拉刷新
								state = PULL_TO_REFRESH;
								onFooterStateChange();
							}
						}
						if (state == PULL_TO_REFRESH) {
							Log.i(TAG, "touch_mvstate == PULL_TO_REFRESH");
							if ((startY - secondTempY) / RATIO > headerHeight && firstTempY - secondTempY >= 9) {
								// 上拉刷新 --》 松开刷新
								state = RELEASE_TO_REFERESH;
								onFooterStateChange();
							} else if (startY - secondTempY <= 0) {
								// 上拉刷新 --》 回到 刷新完成
								state = DONE;
								onFooterStateChange();
							}
						}
						if (state == RELEASE_TO_REFERESH) {
							Log.i(TAG, "touch_mvstate == RELEASE_TO_REFERESHheaderHeight=" + headerHeight);
							Log.i(TAG, "   == RELEASE_TO_REFERESHtempY=" + secondTempY + ",,,firstTempY=" + firstTempY);
							if (firstTempY - secondTempY < -changestate_distance) {
								Log.i(TAG, "*touch_mv footer.getPaddingBottom()=" + footer.getPaddingBottom() + ",,,headerHeight="
										+ headerHeight);
								state = PULL_TO_REFRESH;
								isBack = true;// 从松开刷新 回到的上拉刷新
								onFooterStateChange();
							} else if (secondTempY - startY >= 0) {
								// 松开刷新 --》 回到 刷新完成
								state = DONE;
								onFooterStateChange();
							}
						}
						if ((state == PULL_TO_REFRESH || state == RELEASE_TO_REFERESH) && secondTempY < startY) {
							Log.i(TAG, "增加尾视图内边距");
							footer.setPadding(0, 0, 0, (int) ((startY - secondTempY) / RATIO - headerHeight));
						}
					} else {
						Log.i(TAG, "touch_mv 上拉刷新 state == REFERESHING");
					}
					Log.i(TAG, "touch_mv-------------上拉刷新执行完毕------------");
				}
				Log.i(TAG, "touch_mv************************************************");
				break;

			case MotionEvent.ACTION_UP:
				Log.i(TAG, "ACTION_UP         state=="+state);
				if (ev.getY()<firstTempY) {
					onPullUpStart();
				}
				if (state != REFERESHING) {

					if (state == PULL_TO_REFRESH) {
						Log.i(TAG, "up -----11111---state == PULL_TO_REFRESH   getFirstVisiblePosition()=="+getFirstVisiblePosition()+" isPullDown=="+isPullDown);
						state = DONE;
						if (getFirstVisiblePosition() == 0  && isPullDown)// 下拉
							onHeaderStateChange();
						if (getLastVisiblePosition() == getCount() - 1 || getLastVisiblePosition() == getCount() - 2 && isPullUp)// 上拉
							onFooterStateChange();
					}

					if (state == RELEASE_TO_REFERESH) {
						Log.i(TAG, "up -----22222222---state == RELEASE_TO_REFERESH  ");
						state = REFERESHING;
						if (getFirstVisiblePosition() == 0  && isPullDown) {
							// 下拉
							onHeaderStateChange();
							onPullDownRefresh();// 刷新得到服务器数据
						}
						if (getLastVisiblePosition() == getCount() - 1 || getLastVisiblePosition() == getCount() - 2 && isPullUp) {
							// 上拉
							onFooterStateChange();
							onPullUpRefresh();// 刷新得到服务器数据
						}
					}
				}
				break;
			}
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 更改尾视图显示状态
	 */
	private void onHeaderStateChange() {
		switch (state) {
		case PULL_TO_REFRESH:
			headerProgressBar.setVisibility(View.GONE);
			headerArrow.setVisibility(View.VISIBLE);
			headerTitle.setVisibility(View.VISIBLE);
			headerLastUpdated.setVisibility(View.VISIBLE);

			headerTitle.setText(mContext.getString(R.string.pull_to_refresh) );
			headerArrow.clearAnimation();
			if (isBack) {
				headerArrow.startAnimation(animation);
				isBack = false;
			}
			break;

		case RELEASE_TO_REFERESH:
			headerProgressBar.setVisibility(View.GONE);
			headerArrow.setVisibility(View.VISIBLE);
			headerTitle.setVisibility(View.VISIBLE);
			headerLastUpdated.setVisibility(View.VISIBLE);

			headerTitle.setText(mContext.getString(R.string.release));
			headerArrow.clearAnimation();
			headerArrow.startAnimation(reverseAnimation);
			break;

		case REFERESHING:
			headerProgressBar.setVisibility(View.VISIBLE);
			headerArrow.setVisibility(View.GONE);
			headerTitle.setVisibility(View.VISIBLE);
			headerLastUpdated.setVisibility(View.VISIBLE);

			headerTitle.setText(mContext.getString(R.string.loading));
			headerArrow.clearAnimation();

			header.setPadding(0, 0, 0, 0);
			break;
		case DONE:
			headerProgressBar.setVisibility(View.GONE);
			headerArrow.setVisibility(View.VISIBLE);
			headerTitle.setVisibility(View.VISIBLE);
			headerLastUpdated.setVisibility(View.VISIBLE);
			headerTitle.setText(mContext.getString(R.string.pull_to_refresh));
			headerArrow.clearAnimation();
			header.setPadding(0, -1 * headerHeight, 0, 0);
			break;
		}
	}

	/**
	 * 更改尾视图显示状态
	 */
	private void onFooterStateChange() {
		switch (state) {
		case PULL_TO_REFRESH:
			footerProgressBar.setVisibility(View.GONE);
			footerArrow.setVisibility(View.VISIBLE);
			footerTitle.setVisibility(View.VISIBLE);
			footerLastUpdated.setVisibility(View.VISIBLE);

			footerTitle.setText(mContext.getString(R.string.load_more));
			footerArrow.clearAnimation();
			if (isBack) {
				footerArrow.startAnimation(reverseAnimation);
				isBack = false;
			}
			break;

		case RELEASE_TO_REFERESH:
			footerProgressBar.setVisibility(View.GONE);
			footerArrow.setVisibility(View.VISIBLE);
			footerTitle.setVisibility(View.VISIBLE);
			footerLastUpdated.setVisibility(View.VISIBLE);

			footerTitle.setText(mContext.getString(R.string.release));
			footerArrow.clearAnimation();
			footerArrow.startAnimation(animation);
			break;

		case REFERESHING:
			footerProgressBar.setVisibility(View.VISIBLE);
			footerArrow.setVisibility(View.GONE);
			footerTitle.setVisibility(View.VISIBLE);
			footerLastUpdated.setVisibility(View.VISIBLE);

			footerTitle.setText(mContext.getString(R.string.loading));
			footerArrow.clearAnimation();

			footer.setPadding(0, 0, 0, 0);
			break;
		case DONE:
			footerProgressBar.setVisibility(View.GONE);
			footerArrow.setVisibility(View.VISIBLE);
			footerTitle.setVisibility(View.VISIBLE);
			footerLastUpdated.setVisibility(View.VISIBLE);

			footerTitle.setText(mContext.getString(R.string.load_more));
			footerArrow.clearAnimation();

			footer.setPadding(0, -1 * headerHeight, 0, 0);
			break;
		}
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		/* 因为在聊天页面需要实现长按复制聊天信息的功能，如返回以下代码 ，
		 *  会使在触摸到聊天信息框进行滑动listView使，长按事件和listview的拖动事件冲突，导致listview不能滚动*/
//		 return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
		return super.onInterceptTouchEvent(ev);
	}
	// Return false if we're scrolling in the x direction    
    class YScrollDetector extends SimpleOnGestureListener {  
        @Override  
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {  
            if(Math.abs(distanceY) > Math.abs(distanceX)) {  
                return true;  
            }  
            return false;  
        }  
    } 
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	isOnMeasure = true;
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	super.onLayout(changed, l, t, r, b);
    	isOnMeasure = false;
    }
    
	/**
	 * 下拉刷新的实现方法
	 */
	private void onPullDownRefresh() {
		if (rListener != null) {
			rListener.pullDownRefresh();
		}
	}
	
	/**
	 * 开始上拉的实现方法
	 */
	private void onPullUpStart() {
		if (rListener != null) {
			rListener.pullUpStart();
		}
	}

	/**
	 * 上拉刷新的实现方法
	 */
	private void onPullUpRefresh() {
		if (rListener != null)
			rListener.pullUpRefresh();
	}
	
	public void setCanPullUp(boolean isPullUp){
		this.isPullUp = isPullUp;
	}
	
	public void setCanPullDown(boolean isPullDown){
		this.isPullDown = isPullDown;
	}

}
