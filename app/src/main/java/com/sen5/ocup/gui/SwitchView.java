package com.sen5.ocup.gui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.sen5.ocup.R;
import com.sen5.ocup.blutoothstruct.CupPara;
import com.sen5.ocup.util.BitmapUtil;
import com.sen5.ocup.util.BlueToothRequest;
import com.sen5.ocup.util.BluetoothConnectUtils;

public class SwitchView extends View implements OnTouchListener {
	private static final String TAG = "SwitchView";
	private Context mContext;
	private boolean nowChoose = false;// 记录当前按钮是否打开，true为打开，false为关闭
	private boolean onSlip = false;// 记录用户是否在滑动
	private float downX, nowX; // 按下时的x，当前的x
	private Rect btn_on, btn_off;// 打开和关闭状态下，游标的Rect

	private boolean isChgLsnOn = false;// 是否设置监听
	private OnChangedListener changedLis;

	private Bitmap bg_on, bg_off, slip_btn;
	private boolean is2bluetooth; // view的开关是否蓝牙发送

	public SwitchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public SwitchView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	private void init() {
		// 载入图片资源
		// bg_on = BitmapFactory.decodeResource(getResources(),
		// R.drawable.switch_on);
		// bg_off = BitmapFactory.decodeResource(getResources(),
		// R.drawable.switch_off);
		// slip_btn = BitmapFactory.decodeResource(getResources(),
		// R.drawable.hander);
		bg_on = BitmapUtil.zoom(BitmapFactory.decodeResource(getResources(),
				R.drawable.switch_on), 0.6f);
		bg_off = BitmapUtil.zoom(BitmapFactory.decodeResource(getResources(),
				R.drawable.switch_off), 0.6f);
		slip_btn = BitmapUtil
				.zoom(BitmapFactory.decodeResource(getResources(),
						R.drawable.hander), 0.6f);
		// 获得需要的Rect数据
		btn_on = new Rect(0, 0, slip_btn.getWidth(), slip_btn.getHeight());
		btn_off = new Rect(bg_off.getWidth() - slip_btn.getWidth(), 0,
				bg_off.getWidth(), slip_btn.getHeight());
		setOnTouchListener(this);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		Matrix matrix = new Matrix();
		Paint paint = new Paint();
		float x;
		if (nowChoose) {// 根据现在的开关状态设置画游标的位置
			x = btn_off.left;
			canvas.drawBitmap(bg_on, matrix, paint);// 画出打开时的背景
		} else {
			x = btn_on.left;
			canvas.drawBitmap(bg_off, matrix, paint);// 画出关闭时的背景
		}

		if (x < 0) { // 对游标位置进行异常判断..
			x = 0;
		} else if (x > bg_on.getWidth() - slip_btn.getWidth()) {
			x = bg_on.getWidth() - slip_btn.getWidth();
		}
		canvas.drawBitmap(slip_btn, x, 0, paint);// 画出游标.
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(TAG,
				"  ontouch-----------event.getAction()==" + event.getAction());
		if (is2bluetooth) {// 对杯子是否处于蓝牙的访问状态进行判断并提示
			if (BluetoothConnectUtils.getInstance().getBluetoothState() != BluetoothConnectUtils.BLUETOOTH_CONNECTED) {
				OcupToast.makeText(mContext,
						mContext.getString(R.string.unconnect_2cup), Toast.LENGTH_LONG)
						.show();
				return true;
			}
			if (BlueToothRequest.getInstance().getRequesting()) {
				OcupToast.makeText(mContext,
						mContext.getString(R.string.requesting), Toast.LENGTH_LONG).show();
				return true;
			}
			//是否已经读取杯子的状态信息，如果还没有读取完信息则提示用户正在同步
			if (!CupPara.getInstance().isGotCupPara()) {
				OcupToast.makeText(mContext,
						mContext.getString(R.string.syncup_failed), Toast.LENGTH_LONG)
						.show();
				BlueToothRequest.getInstance().sendMsg2getCupInfo(null);
				return true;
			}
		}
		switch (event.getAction()) {// 根据动作来执行代码

		case MotionEvent.ACTION_MOVE:// 滑动
			Log.d(TAG, "  ontouch-------ACTION_MOVE");
			nowX = event.getX();
			break;
		case MotionEvent.ACTION_DOWN:// 按下
			Log.d(TAG, "  ontouch-------ACTION_DOWN");
			if (event.getX() > bg_on.getWidth()
					|| event.getY() > bg_on.getHeight()) {
				return false;
			}
			onSlip = true;
			downX = event.getX();
			nowX = downX;
			break;
		case MotionEvent.ACTION_UP:// 松开
			Log.d(TAG, "  ontouch-------ACTION_UP");
			onSlip = false;
			boolean lastChoose = nowChoose;
			nowChoose = !lastChoose;
			// if (event.getX() >= (bg_on.getWidth() / 3)) {
			// nowChoose = true;
			// } else {
			// nowChoose = false;
			// }
			if (isChgLsnOn && (lastChoose != nowChoose)) {// 如果设置了监听器,就调用其方法.
				changedLis.OnChanged(v, nowChoose);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			Log.d(TAG, "  ontouch-------ACTION_CANCEL");
			boolean lastC = nowChoose;
			nowChoose = !lastC;
			if (isChgLsnOn && (lastC != nowChoose)) {// 如果设置了监听器,就调用其方法.
				changedLis.OnChanged(v, nowChoose);
			}
			break;
		default:
			break;
		}
		invalidate();
		return true;
	}

	@Override
	/**
	 * 测量尺寸的回调方法
	 */
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(bg_on.getWidth(), bg_on.getHeight());// 设置控件的宽和高，单位是像素
	}

	public void SetOnChangedListener(OnChangedListener l) {// 设置监听器,当状态修改的时候
		isChgLsnOn = true;
		changedLis = l;
	}

	public void setChecked(View view, boolean b) {
		boolean lastChoose = nowChoose;
		nowChoose = b;
		Log.d(TAG, " isChgLsnOn===" + isChgLsnOn + "      nowChoose=="
				+ nowChoose + "     lastChoose==" + lastChoose);
		if (isChgLsnOn && (lastChoose != nowChoose)) {// 如果设置了监听器,就调用其方法.
			changedLis.OnChanged(view, nowChoose);
		}
		invalidate();
	}

	public boolean isChecked() {
		return nowChoose;
	}

	public void set2bluetooth(boolean b) {
		is2bluetooth = b;
	}

	public interface OnChangedListener {
		// abstract void OnChanged(boolean checkState);
		abstract void OnChanged(View view, boolean checkState);
	}
}
