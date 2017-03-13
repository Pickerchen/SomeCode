package com.sen5.ocup.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.callback.CustomInterface.IDrawScrawl;
import com.sen5.ocup.gui.ScrowlView;
import com.sen5.ocup.gui.ScrowlView.OnCompleteListener;
import com.sen5.ocup.receiver.HomeWatcher;
import com.sen5.ocup.struct.ChatMsgEntity;
import com.sen5.ocup.util.Tools;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 涂鸦界面
 */
public class ScrawlActivity extends BaseActivity implements IDrawScrawl, Callback {

	private static final String TAG = "ScrawlActivity";
	private  HomeWatcher mHomeKeyReceiver = null;
	private final static int Color_p = Color.rgb(255, 255, 255);
	private final static int Color_n = Color.rgb(0, 0, 0);

	private Handler mHander;

	// 当前状态״̬
	private int currentStatus;// 0---->画笔模式 1--->橡皮擦模式

	private LinearLayout mLayout_back;

	private TextView tv_send;
	private ImageView iv_reset;
	private ImageView iv_pen;
	private ImageView iv_eraser;

	private ScrowlView lpwv;
	private String password="";
	// 当前操作的消息实体
	protected ChatMsgEntity mChatMsgEntity;

	private boolean isFirstCreate = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate------------isFirstCreate==" + isFirstCreate);
		mHander = new Handler(this);

		if (isFirstCreate) {
			isFirstCreate = false;
			setContentView(R.layout.activity_scrawl);
			lpwv = (ScrowlView) this.findViewById(R.id.mLocusPassWordView);
		}

		FrameLayout titleLayout = (FrameLayout) findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(titleLayout,this);

		lpwv.setOnCompleteListener(new OnCompleteListener() {
			@Override
			public void onComplete(String mPassword) {
				Log.d(TAG, "OnCompleteListener------------mPassword==" + mPassword);
				password = mPassword;
			}
		});
		lpwv.setIDrawScrawl(ScrawlActivity.this);

		mLayout_back = (LinearLayout) findViewById(R.id.layout_back);
		mLayout_back.setOnClickListener(mOnClickListener);

		tv_send = (TextView) this.findViewById(R.id.tv_send);
		tv_send.setOnClickListener(mOnClickListener);

		iv_reset = (ImageView) this.findViewById(R.id.iv_reset);
		iv_reset.setOnClickListener(mOnClickListener);
		iv_pen = (ImageView) this.findViewById(R.id.iv_pen);
		iv_pen.setOnClickListener(mOnClickListener);
		iv_eraser = (ImageView) this.findViewById(R.id.iv_eraser);
		iv_eraser.setOnClickListener(mOnClickListener);
		mHomeKeyReceiver = new HomeWatcher(this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume---------");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause---------");
	}
	@Override
	protected void onStart() {
		super.onStart();
		mHomeKeyReceiver.startWatch();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop-------");
		mHomeKeyReceiver.stopWatch();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy-------");
	}

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.layout_back:
				finish();
				break;
			case R.id.tv_send:
				Logger.e(TAG, "send   password======" + password);
				lpwv.clearPassword();
				Intent resultIntent = new Intent();
				resultIntent.putExtra("sendMsg", password);
				ScrawlActivity.this.setResult(RESULT_OK, resultIntent);
				finish();
				break;
			case R.id.iv_reset:
				password = "";
				lpwv.clearPassword();
				break;
			case R.id.iv_eraser:
				if (currentStatus != 1) {
					currentStatus = 1;
					iv_pen.setImageResource(R.drawable.pencil_n);
					iv_eraser.setImageResource(R.drawable.eraser_p);
					lpwv.setMode(lpwv.MODE_ERASER);
				}
				break;
			case R.id.iv_pen:
				if (currentStatus != 0) {
					currentStatus = 0;
					iv_pen.setImageResource(R.drawable.pencil_p);
					iv_eraser.setImageResource(R.drawable.eraser_n);
					lpwv.setMode(lpwv.MODE_PEN);
				}
				break;
			}
		}
	};

	@Override
	public void reset() {
		mHander.sendEmptyMessage(RESET);
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == RESET) {
			currentStatus = 0;
			iv_pen.setImageResource(R.drawable.pencil_p);
			iv_eraser.setImageResource(R.drawable.eraser_n);
			lpwv.setMode(lpwv.MODE_PEN);
		}
		return false;
	}
	private static final int RESET = 1;//橡皮擦模式下，全擦后会调用reset，启用画笔模式

}
