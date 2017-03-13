package com.sen5.ocup.activity;

import android.app.ActionBar.LayoutParams;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.Circle;
import com.sen5.ocup.R;
import com.sen5.ocup.blutoothstruct.CupLEDColor;
import com.sen5.ocup.blutoothstruct.CupPara;
import com.sen5.ocup.callback.BluetoothCallback.IGetCupParaCallback;
import com.sen5.ocup.callback.BluetoothCallback.ISetCupParaCallback;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.gui.SwitchView;
import com.sen5.ocup.gui.SwitchView.OnChangedListener;
import com.sen5.ocup.receiver.HomeWatcher;
import com.sen5.ocup.util.BlueToothRequest;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.Tools;

import java.lang.reflect.Field;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 修改水杯灯光提醒颜色
 */
public class WaterTemLEDActivity extends BaseActivity implements ISetCupParaCallback, Callback, IGetCupParaCallback {

	private static final String TAG = "WaterTemLEDActivity";
	private HomeWatcher mHomeKeyReceiver = null;
	private Handler mHandler;
	private CupPara mCupPara;
	private LinearLayout layout_back = null;
	private int statusBarHeight;
	private View copyV = null, copy1 = null;
	private ImageView cup_color_change1, cup_color_change2;
	private SwitchView mSwitch_defaut = null;
	private LinearLayout temp;// 自定义颜色布局 mLayout_custom_waterled = null,
	private LinearLayout mLayout_selcolor_below40, mLayout_selcolor_middle, mLayout_selcolor_above60;// 颜色选择的布局
	private LinearLayout mLayout_color_below40, mLayout_colo_middle, mLayout_color_above60;// 当前选定的颜色布局
	private ImageView mTv_color_below40, mTV_color_middle;// 当前选定的颜色
	// 低于40
	private View radioButton_below40_cyan, radioButton_below40_yellow, radioButton_below40_pink,radioButton_below40_blue, radioButton_below40_green, radioButton_below40_purple, radioButton_below40_white;
	// 40--60
	private View radioButton_middle_cyan, radioButton_middle_yellow, radioButton_middle_blue, radioButton_middle_pink,radioButton_middle_green, radioButton_middle_purple, radioButton_middle_white;
	/**
	 * 水温提醒开关
	 */
	private OnChangedListener mOnChangedListener_waterled = new OnChangedListener() {

		@Override
		public void OnChanged(View view, boolean checkState) {
			Log.d(TAG, " mSwitch_defaut  mOnChangedListener_waterled===");
			if (checkState && mCupPara.getLed_sw() == 0) {
				mCupPara.setLed_sw(1);
				setCupPara();
			} else if (!checkState && mCupPara.getLed_sw() == 1) {
				mCupPara.setLed_sw(0);
				setCupPara();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_watertem_led);
		initView();
		initData();
		showDialog();
	}

	private void initView() {

		FrameLayout titleLayout = (FrameLayout) findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(titleLayout,this);

		mHandler = new Handler(this);
		mCupPara = CupPara.getInstance();

		layout_back = (LinearLayout) findViewById(R.id.layout_back);

		mSwitch_defaut = (SwitchView) findViewById(R.id.switch_default_watertem_led);
		mSwitch_defaut.set2bluetooth(false);
		temp = (LinearLayout) findViewById(R.id.temp);

		cup_color_change1 = (ImageView) findViewById(R.id.pic_change_one);
		cup_color_change2 = (ImageView) findViewById(R.id.pic_change_two);
		mLayout_color_below40 = (LinearLayout) findViewById(R.id.layout_color_below40);
		mLayout_colo_middle = (LinearLayout) findViewById(R.id.layout_color_middle);
		mLayout_color_above60 = (LinearLayout) findViewById(R.id.layout_color_above60);
		mLayout_selcolor_below40 = (LinearLayout) findViewById(R.id.layout_selcolor_below40);
		mLayout_selcolor_middle = (LinearLayout) findViewById(R.id.layout_selcolor_middle);
		mLayout_selcolor_above60 = (LinearLayout) findViewById(R.id.layout_selcolor_above60);
		mTv_color_below40 = (ImageView) findViewById(R.id.tv_color_below40);
		mTV_color_middle = (ImageView) findViewById(R.id.tv_color_middle);

		// 低于40
		radioButton_below40_cyan = findViewById(R.id.radioButton_below40_cyan);
		radioButton_below40_yellow = findViewById(R.id.radioButton_below40_yellow);
		radioButton_below40_blue = findViewById(R.id.radioButton_below40_blue);
		radioButton_below40_green = findViewById(R.id.radioButton_below40_green);
		radioButton_below40_purple = findViewById(R.id.radioButton_below40_purple);
		radioButton_below40_white = findViewById(R.id.radioButton_below40_white);
		radioButton_below40_pink = findViewById(R.id.radioButton_below40_pink);
		// 40-60
		radioButton_middle_cyan = findViewById(R.id.radioButton_middle_cyan);
		radioButton_middle_yellow = findViewById(R.id.radioButton_middle_yellow);
		radioButton_middle_blue = findViewById(R.id.radioButton_middle_blue);
		radioButton_middle_green = findViewById(R.id.radioButton_middle_green);
		radioButton_middle_purple = findViewById(R.id.radioButton_middle_purple);
		radioButton_middle_white = findViewById(R.id.radioButton_middle_white);
		radioButton_middle_pink = findViewById(R.id.radioButton_middle_pink);
	}

	private void initData() {
		mHomeKeyReceiver = new HomeWatcher(this);
		
		layout_back.setOnClickListener(mOnClickListener);
		mLayout_color_below40.setOnClickListener(mOnClickListener);
		mLayout_colo_middle.setOnClickListener(mOnClickListener);
		mLayout_color_above60.setOnClickListener(mOnClickListener);

		// 低于40
		radioButton_below40_cyan.setOnClickListener(mOnClickListener);
		radioButton_below40_yellow.setOnClickListener(mOnClickListener);
		radioButton_below40_blue.setOnClickListener(mOnClickListener);
		radioButton_below40_green.setOnClickListener(mOnClickListener);
		radioButton_below40_purple.setOnClickListener(mOnClickListener);
		radioButton_below40_white.setOnClickListener(mOnClickListener);
		radioButton_below40_pink.setOnClickListener(mOnClickListener);
		// 40-60
		radioButton_middle_cyan.setOnClickListener(mOnClickListener);
		radioButton_middle_yellow.setOnClickListener(mOnClickListener);
		radioButton_middle_blue.setOnClickListener(mOnClickListener);
		radioButton_middle_green.setOnClickListener(mOnClickListener);
		radioButton_middle_purple.setOnClickListener(mOnClickListener);
		radioButton_middle_white.setOnClickListener(mOnClickListener);
		radioButton_middle_pink.setOnClickListener(mOnClickListener);

		// 添加测量
		int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		radioButton_below40_cyan.measure(measureSpec, measureSpec);
		radioButton_below40_yellow.measure(measureSpec, measureSpec);
		radioButton_below40_blue.measure(measureSpec, measureSpec);
		radioButton_below40_green.measure(measureSpec, measureSpec);
		radioButton_below40_purple.measure(measureSpec, measureSpec);
		radioButton_below40_white.measure(measureSpec, measureSpec);
		radioButton_below40_pink.measure(measureSpec,measureSpec);

		radioButton_middle_cyan.measure(measureSpec, measureSpec);
		radioButton_middle_yellow.measure(measureSpec, measureSpec);
		radioButton_middle_blue.measure(measureSpec, measureSpec);
		radioButton_middle_green.measure(measureSpec, measureSpec);
		radioButton_middle_purple.measure(measureSpec, measureSpec);
		radioButton_middle_white.measure(measureSpec, measureSpec);
		radioButton_middle_pink.measure(measureSpec,measureSpec);

		// 获取通知栏高度
		statusBarHeight = getStatusBarHeight(this);

		mSwitch_defaut.SetOnChangedListener(mOnChangedListener_waterled);

		if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
			Log.d(TAG, "mOnClickListener-----save   " + mCupPara.getLED_data().getT_high() + "  " + mCupPara.getLED_data().getT_norm() + "  " + mCupPara.getLED_data().getT_low());
				BlueToothRequest.getInstance().sendMsg2getCupInfo(WaterTemLEDActivity.this);
		} else {
			OcupToast.makeText(WaterTemLEDActivity.this, getString(R.string.unconnect_2cup), Toast.LENGTH_LONG).show();
			this.finish();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		mHomeKeyReceiver.startWatch();
	}
	@Override
	protected void onStop() {
		super.onStop();
		mHomeKeyReceiver.stopWatch();
	}
	/**
	 * 设置杯子参数
	 */
	private void setCupPara() {
		if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
			BlueToothRequest.getInstance().sendMsg2setCupPara(WaterTemLEDActivity.this,BlueToothRequest.type_led_1);
		} else {
			OcupToast.makeText(WaterTemLEDActivity.this, getString(R.string.unconnect_2cup), Toast.LENGTH_LONG).show();
		}
	}


	/**
	 * 设置40--60的颜色
	 */
	private void setCurLedColor_middle() {
		Log.d(TAG, "setCurLedColor_middle-------------mCupPara.getLED_data().getT_norm()===" + mCupPara.getLED_data().getT_norm());
		if (mCupPara.getLED_data().getT_norm() == CupLEDColor.led_color.LED_GREEN.ordinal()) {
			cup_color_change2.setImageResource(R.drawable.green_small);
			mTV_color_middle.setImageResource(R.drawable.circle_grenn);
		} else if (mCupPara.getLED_data().getT_norm() == CupLEDColor.led_color.LED_BLUE.ordinal()) {
			cup_color_change2.setImageResource(R.drawable.blue_small);
			mTV_color_middle.setImageResource(R.drawable.circle_blue);
		} else if (mCupPara.getLED_data().getT_norm() == CupLEDColor.led_color.LED_YELLOW.ordinal()) {
			cup_color_change2.setImageResource(R.drawable.yellow_small);
			mTV_color_middle.setImageResource(R.drawable.circle_yellow);
		} else if (mCupPara.getLED_data().getT_norm() == CupLEDColor.led_color.LED_PURPLE.ordinal()) {
			cup_color_change2.setImageResource(R.drawable.purple_small);
			mTV_color_middle.setImageResource(R.drawable.circle_purple);
		} else if (mCupPara.getLED_data().getT_norm() == CupLEDColor.led_color.LED_CYAN.ordinal()) {
			cup_color_change2.setImageResource(R.drawable.cyan_small);
			mTV_color_middle.setImageResource(R.drawable.circle_cyan);
		} else if (mCupPara.getLED_data().getT_norm() == CupLEDColor.led_color.LED_WHITE.ordinal()) {
			cup_color_change2.setImageResource(R.drawable.white_smal);
			mTV_color_middle.setImageResource(R.drawable.circle_white);
		}
		else if (mCupPara.getLED_data().getT_norm() == CupLEDColor.led_color.LED_PINK.ordinal()) {
			cup_color_change2.setImageResource(R.drawable.pink_small);
			mTV_color_middle.setImageResource(R.drawable.circle_pink);
		}
	}

	/**
	 * 设置低于40的颜色
	 */
	private void setCurLedColor_below40() {
		Log.d(TAG, "setCurLedColor_below40-------------mCupPara.getLED_data().getT_low()===" + mCupPara.getLED_data().getT_low());
		if (mCupPara.getLED_data().getT_low() == CupLEDColor.led_color.LED_GREEN.ordinal()) {
			cup_color_change1.setImageResource(R.drawable.green_small);
			mTv_color_below40.setImageResource(R.drawable.circle_grenn);
		} else if (mCupPara.getLED_data().getT_low() == CupLEDColor.led_color.LED_BLUE.ordinal()) {
			cup_color_change1.setImageResource(R.drawable.blue_small);
			mTv_color_below40.setImageResource(R.drawable.circle_blue);
		} else if (mCupPara.getLED_data().getT_low() == CupLEDColor.led_color.LED_YELLOW.ordinal()) {
			cup_color_change1.setImageResource(R.drawable.yellow_small);
			mTv_color_below40.setImageResource(R.drawable.circle_yellow);
		} else if (mCupPara.getLED_data().getT_low() == CupLEDColor.led_color.LED_PURPLE.ordinal()) {
			cup_color_change1.setImageResource(R.drawable.purple_small);
			mTv_color_below40.setImageResource(R.drawable.circle_purple);
		} else if (mCupPara.getLED_data().getT_low() == CupLEDColor.led_color.LED_CYAN.ordinal()) {
			cup_color_change1.setImageResource(R.drawable.cyan_small);
			mTv_color_below40.setImageResource(R.drawable.circle_cyan);
		} else if (mCupPara.getLED_data().getT_low() == CupLEDColor.led_color.LED_WHITE.ordinal()) {
			cup_color_change1.setImageResource(R.drawable.white_smal);
			mTv_color_below40.setImageResource(R.drawable.circle_white);
		}
		else if (mCupPara.getLED_data().getT_low() == CupLEDColor.led_color.LED_WHITE.ordinal()) {
			cup_color_change1.setImageResource(R.drawable.pink_small);
			mTv_color_below40.setImageResource(R.drawable.circle_pink);
		}
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.layout_back:
				WaterTemLEDActivity.this.finish();
				break;
			case R.id.radioButton_below40_green:
				// zoomup(radioButton_below40_green,temp);
				cup_color_change1.setImageResource(R.drawable.green_small);
				mTv_color_below40.setImageResource(R.drawable.circle_grenn);
				mCupPara.getLED_data().setT_low(CupLEDColor.led_color.LED_GREEN.ordinal());
				setCupPara();
				// mBluetoothRequest.sendMsg2setCupPara(mBluetoothRequest.socket,WaterTemLEDActivity.this);
				break;
			case R.id.radioButton_below40_blue:
				// zoomup(radioButton_below40_blue,temp);
				cup_color_change1.setImageResource(R.drawable.blue_small);
				mTv_color_below40.setImageResource(R.drawable.circle_blue);
				mCupPara.getLED_data().setT_low(CupLEDColor.led_color.LED_BLUE.ordinal());
				setCupPara();
				// mBluetoothRequest.sendMsg2setCupPara(mBluetoothRequest.socket,WaterTemLEDActivity.this);
				break;
			case R.id.radioButton_below40_yellow:
				// zoomup(radioButton_below40_yellow,temp);
				cup_color_change1.setImageResource(R.drawable.yellow_small);
				mTv_color_below40.setImageResource(R.drawable.circle_yellow);
				mCupPara.getLED_data().setT_low(CupLEDColor.led_color.LED_YELLOW.ordinal());
				setCupPara();
				// mBluetoothRequest.sendMsg2setCupPara(mBluetoothRequest.socket,WaterTemLEDActivity.this);
				break;
			case R.id.radioButton_below40_cyan:
				// zoomup(radioButton_below40_cyan,temp);
				cup_color_change1.setImageResource(R.drawable.cyan_small);
				mTv_color_below40.setImageResource(R.drawable.circle_cyan);
				mCupPara.getLED_data().setT_low(CupLEDColor.led_color.LED_CYAN.ordinal());
				setCupPara();
				// mBluetoothRequest.sendMsg2setCupPara(mBluetoothRequest.socket,WaterTemLEDActivity.this);
				break;
			case R.id.radioButton_below40_purple:
				// zoomup(radioButton_below40_purple,temp);
				cup_color_change1.setImageResource(R.drawable.purple_small);
				mTv_color_below40.setImageResource(R.drawable.circle_purple);
				mCupPara.getLED_data().setT_low(CupLEDColor.led_color.LED_PURPLE.ordinal());
				setCupPara();
				// mBluetoothRequest.sendMsg2setCupPara(mBluetoothRequest.socket,WaterTemLEDActivity.this);
				break;

				case R.id.radioButton_below40_pink:
					// zoomup(radioButton_below40_purple,temp);
					cup_color_change1.setImageResource(R.drawable.pink_small);
					mTv_color_below40.setImageResource(R.drawable.circle_pink);
					mCupPara.getLED_data().setT_low(CupLEDColor.led_color.LED_PINK.ordinal());
					setCupPara();
					// mBluetoothRequest.sendMsg2setCupPara(mBluetoothRequest.socket,WaterTemLEDActivity.this);
					break;
			case R.id.radioButton_below40_white:
				// zoomup(radioButton_below40_white,temp);
				cup_color_change1.setImageResource(R.drawable.white_smal);
				mTv_color_below40.setImageResource(R.drawable.circle_white);
				mCupPara.getLED_data().setT_low(CupLEDColor.led_color.LED_WHITE.ordinal());
				setCupPara();
				// mBluetoothRequest.sendMsg2setCupPara(mBluetoothRequest.socket,WaterTemLEDActivity.this);
				break;
			case R.id.radioButton_middle_green:
				// zoomdown(radioButton_middle_green,temp);
				cup_color_change2.setImageResource(R.drawable.green_small);
				mTV_color_middle.setImageResource(R.drawable.circle_grenn);
				mCupPara.getLED_data().setT_norm(CupLEDColor.led_color.LED_GREEN.ordinal());
				setCupPara();
				// mBluetoothRequest.sendMsg2setCupPara(mBluetoothRequest.socket,WaterTemLEDActivity.this);
				break;
			case R.id.radioButton_middle_blue:
				// zoomdown(radioButton_middle_blue,temp);
				cup_color_change2.setImageResource(R.drawable.blue_small);
				mTV_color_middle.setImageResource(R.drawable.circle_blue);
				mCupPara.getLED_data().setT_norm(CupLEDColor.led_color.LED_BLUE.ordinal());
				setCupPara();
				// mBluetoothRequest.sendMsg2setCupPara(mBluetoothRequest.socket,WaterTemLEDActivity.this);
				break;
			case R.id.radioButton_middle_yellow:
				// zoomdown(radioButton_middle_yellow,temp);
				cup_color_change2.setImageResource(R.drawable.yellow_small);
				mTV_color_middle.setImageResource(R.drawable.circle_yellow);
				mCupPara.getLED_data().setT_norm(CupLEDColor.led_color.LED_YELLOW.ordinal());
				setCupPara();
				// mBluetoothRequest.sendMsg2setCupPara(mBluetoothRequest.socket,WaterTemLEDActivity.this);
				break;
			case R.id.radioButton_middle_cyan:
				// zoomdown(radioButton_middle_cyan,temp);
				cup_color_change2.setImageResource(R.drawable.cyan_small);
				mTV_color_middle.setImageResource(R.drawable.circle_cyan);
				mCupPara.getLED_data().setT_norm(CupLEDColor.led_color.LED_CYAN.ordinal());
				setCupPara();
				// mBluetoothRequest.sendMsg2setCupPara(mBluetoothRequest.socket,WaterTemLEDActivity.this);
				break;
			case R.id.radioButton_middle_purple:
				// zoomdown(radioButton_middle_purple,temp);
				cup_color_change2.setImageResource(R.drawable.purple_small);
				mTV_color_middle.setImageResource(R.drawable.circle_purple);
				mCupPara.getLED_data().setT_norm(CupLEDColor.led_color.LED_PURPLE.ordinal());
				setCupPara();
				// mBluetoothRequest.sendMsg2setCupPara(mBluetoothRequest.socket,WaterTemLEDActivity.this);
				break;

				case R.id.radioButton_middle_pink:
					// zoomdown(radioButton_middle_purple,temp);
					cup_color_change2.setImageResource(R.drawable.pink_small);
					mTV_color_middle.setImageResource(R.drawable.circle_pink);
					mCupPara.getLED_data().setT_norm(CupLEDColor.led_color.LED_PURPLE.ordinal());
					setCupPara();
					// mBluetoothRequest.sendMsg2setCupPara(mBluetoothRequest.socket,WaterTemLEDActivity.this);
					break;
			case R.id.radioButton_middle_white:
				// zoomdown(radioButton_middle_white,temp);
				cup_color_change2.setImageResource(R.drawable.white_smal);
				mTV_color_middle.setImageResource(R.drawable.circle_white);
				mCupPara.getLED_data().setT_norm(CupLEDColor.led_color.LED_WHITE.ordinal());
				setCupPara();
				// mBluetoothRequest.sendMsg2setCupPara(mBluetoothRequest.socket,WaterTemLEDActivity.this);
				break;
			case R.id.layout_color_below40:
				if (mLayout_selcolor_below40.getVisibility() == View.VISIBLE) {
					if (null != copyV) {
						temp.removeView(copyV);
					}
					mLayout_selcolor_below40.setVisibility(View.GONE);
				} else {
					mLayout_selcolor_below40.setVisibility(View.VISIBLE);
					mLayout_selcolor_middle.setVisibility(View.GONE);
					mLayout_selcolor_above60.setVisibility(View.GONE);
				}
				break;
			case R.id.layout_color_middle:
				if (mLayout_selcolor_middle.getVisibility() == View.VISIBLE) {
					if (null != copy1) {
						temp.removeView(copy1);
					}
					mLayout_selcolor_middle.setVisibility(View.GONE);
				} else {
					mLayout_selcolor_middle.setVisibility(View.VISIBLE);
					mLayout_selcolor_below40.setVisibility(View.GONE);
					mLayout_selcolor_above60.setVisibility(View.GONE);
				}

			}
		}
	};


	public void showBtn(final View view, LinearLayout temp) {
		// 获取View高宽
		int h = view.getMeasuredHeight();
		int w = view.getMeasuredWidth();
		// 获取坐标，相对于整个手机屏幕，包括通知栏等
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		int x = location[0];
		int y = location[1];
		// 复制View
		View copyV = new View(this);
		copyV.setBackgroundColor(Color.MAGENTA);
		RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(w, h);
		// 注1：高度需要减掉通知栏高度
		lps.setMargins(x, y - statusBarHeight, 0, 0);
		copyV.setLayoutParams(lps);
		temp.addView(copyV);
		temp.postInvalidate();
	}

	public void zoomup(View v, LinearLayout temp) {
		// 按下，复制View并作放大动画
		// 获取View高宽
		int h = v.getMeasuredHeight();
		int w = v.getMeasuredWidth();
		// 获取坐标，相对于整个手机屏幕，包括通知栏等
		int[] location = new int[2];
		v.getLocationOnScreen(location);
		int x = location[0];
		int y = location[1];
		Log.d(TAG, "location: x,y===" + x + "," + y);
		// 判断移除之前的View
		if (null != copyV) {
			temp.removeView(copyV);
			temp.postInvalidate();
		}
		// 创建View
		copyV = new View(this);
		copyV.setBackgroundDrawable(v.getBackground());
		LinearLayout.LayoutParams lps = new LinearLayout.LayoutParams(w, h);
		// 注1：高度需要减掉通知栏高度
		lps.setMargins(x, Tools.dip2px(getApplicationContext(), -200), 0, 0);
		lps.gravity = Gravity.CENTER;
		copyV.setLayoutParams(lps);
		int[] test = new int[2];
		copyV.getLocationOnScreen(test);
		Log.d(TAG, "Test: x,y===" + test[0] + "," + test[1]);
		temp.addView(copyV);
		temp.postInvalidate();
		// 放大动画
		ScaleAnimation zoomAnimation = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		zoomAnimation.setDuration(300);
		zoomAnimation.setInterpolator(new AccelerateInterpolator());
		zoomAnimation.setFillAfter(true);
		copyV.startAnimation(zoomAnimation);
	}

	public void zoomdown(View v, LinearLayout temp) {
		// 按下，复制View并作放大动画
		// 获取View高宽
		int h = v.getMeasuredHeight();
		int w = v.getMeasuredWidth();
		// 获取坐标，相对于整个手机屏幕，包括通知栏等
		int[] location = new int[2];
		v.getLocationOnScreen(location);
		int x = location[0];
		int y = location[1];
		Log.d(TAG, "location: x,y===" + x + "," + y);
		// 判断移除之前的View
		if (null != copy1) {
			temp.removeView(copy1);
			temp.postInvalidate();
		}
		// 创建View
		copy1 = new View(this);
		copy1.setBackgroundDrawable(v.getBackground());
		LinearLayout.LayoutParams lps = new LinearLayout.LayoutParams(w, h);
		// 注1：高度需要减掉通知栏高度
		lps.setMargins(x, Tools.dip2px(getApplicationContext(), -145), 0, 0);
		LayoutParams layoutParams = new LayoutParams(lps);
		layoutParams.gravity = Gravity.CENTER;
		copy1.setLayoutParams(lps);
		int[] test = new int[2];
		copy1.getLocationOnScreen(test);
		Log.d(TAG, "Test: x,y===" + test[0] + "," + test[1]);
		temp.addView(copy1);
		temp.postInvalidate();
		// 放大动画
		ScaleAnimation zoomAnimation = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		zoomAnimation.setDuration(300);
		zoomAnimation.setInterpolator(new AccelerateInterpolator());
		zoomAnimation.setFillAfter(true);
		copy1.startAnimation(zoomAnimation);
	}

	/**
	 * 获取通知栏高度
	 * 
	 * @param context
	 *            Context对象
	 * @return 通知栏高度
	 */
	public static int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0;
		int statusBarHeight = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return statusBarHeight;
	}

	@Override
	public void setCupPara_OK(int type) {
		Log.d(TAG, "setCupPara_OK------------");
		mHandler.sendEmptyMessage(setCupPara_OK);

	}

	@Override
	public void setCupPara_NO(int type) {

	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == setCupPara_OK) {
			Log.d(TAG, "handleMessage--------setCupPara_OK-");
//			Intent resultIntent = new Intent();
//			WaterTemLEDActivity.this.setResult(RESULT_OK, resultIntent);
//			finish();
		} else if (msg.what == SETCUPPARA_NO) {
			Log.d(TAG, "handleMessage--------SETCUPPARA_NO-");
			OcupToast.makeText(WaterTemLEDActivity.this, getString(R.string.set_failed), Toast.LENGTH_LONG).show();
//			new CustomDialog(WaterTemLEDActivity.this, null, R.style.custom_dialog, CustomDialog.TOAST_DIALOG, getString(R.string.set_failed)).show();
//			Intent resultIntent = new Intent();
//			WaterTemLEDActivity.this.setResult(RESULT_OK, resultIntent);
//			finish();
		} else if (msg.what == getCupPara_OK) {
			if (mCupPara.getLed_sw() == 1) {
				mSwitch_defaut.setChecked(mSwitch_defaut, true);
			} else {
				mSwitch_defaut.setChecked(mSwitch_defaut, false);
			}
			setCurLedColor_middle();
			setCurLedColor_below40();
			mDialog.dismiss();
			// BlueToothRequest.getInstance().sendMsg2setCupPara(BlueToothRequest.getInstance().socket,
			// WaterTemLEDActivity.this);
		} else if (msg.what == getCUPPARA_NO) {
			OcupToast.makeText(WaterTemLEDActivity.this, getString(R.string.syncup_failed), Toast.LENGTH_LONG).show();
//			new CustomDialog(WaterTemLEDActivity.this, null, R.style.custom_dialog, CustomDialog.TOAST_DIALOG, getString(R.string.syncup_failed)).show();
			this.finish();
		}
		return false;
	}

	private final static int setCupPara_OK = 1;
	private final static int SETCUPPARA_NO = 2;
	private final static int getCupPara_OK = 3;
	private final static int getCUPPARA_NO = 4;

	@Override
	public void getCupPara_OK() {
		mHandler.sendEmptyMessage(getCupPara_OK);

	}

	@Override
	public void getCupPara_NO() {
		mHandler.sendEmptyMessage(getCUPPARA_NO);

	}

	private Dialog mDialog;
	private Circle mCircleDrawable;
	private void showDialog() {
		mDialog = new Dialog(WaterTemLEDActivity.this,R.style.custom_dialog_loading);
		mDialog.setContentView(R.layout.dialog_register_loading);
		mDialog.getWindow().setLayout((1* Tools.getScreenWH(this)[0])/2,200);
		ImageView imageView = (ImageView) mDialog.findViewById(R.id.dialog_loading_iv);
		mCircleDrawable = new Circle();
		imageView.setBackground(mCircleDrawable);
		mCircleDrawable.setColor(android.graphics.Color.parseColor("#FF818C"));
		mCircleDrawable.start();
		mDialog.show();
	}

}
