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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
 * 类说明 : 绘制动画涂鸦界面
 */
public class ScrawlAnimActivity extends BaseActivity implements IDrawScrawl, Callback {

	private static final String TAG = "ScrawlAnimActivity";
	private HomeWatcher mHomeKeyReceiver = null;
	private final static int Color_p = Color.rgb(255, 255, 255);
	private final static int Color_n = Color.rgb(0, 0, 0);
	private final static int Color_numberbg_p = Color.rgb(46,171,255);
	private final static int Color_numberbg_n = Color.rgb(122,205,250);

	private Handler mHander;

	private Button btn_1;
	private Button btn_2;
	private Button btn_3;
	private Button btn_4;
	private LinearLayout mLayout_back;
	private TextView tv_send;
	private ImageView iv_reset;
	private ImageView iv_pen;
	private ImageView iv_eraser;
	private ScrowlView mSrowlview1;
	private ScrowlView mSrowlview2;
	private ScrowlView mSrowlview3;
	private ScrowlView mSrowlview4;

	private Button[] mBtns;
	private ScrowlView[] mScrowlViews;
	private int curPage=0;//当前是第几页
	// 当前状态״̬
	private int currentStatus;// 0---->画笔模式 1--->橡皮擦模式
//	private String password="";
	private String[] points=new String[4];
	// 当前操作的消息实体
	protected ChatMsgEntity mChatMsgEntity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate-----------");
		setContentView(R.layout.activity_scrawlanim);
		mHander = new Handler(this);
		initview();
	}

	private void initview() {
		FrameLayout titleLayout = (FrameLayout) findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(titleLayout,this);

		mHomeKeyReceiver = new HomeWatcher(this);
		mLayout_back = (LinearLayout) findViewById(R.id.layout_back);
		mLayout_back.setOnClickListener(mOnClickListener);
		
		mSrowlview1 = (ScrowlView) this.findViewById(R.id.scrowlview1);
		mSrowlview2 = (ScrowlView) this.findViewById(R.id.scrowlview2);
		mSrowlview3 = (ScrowlView) this.findViewById(R.id.scrowlview3);
		mSrowlview4 = (ScrowlView) this.findViewById(R.id.scrowlview4);
		mSrowlview1.setIDrawScrawl(ScrawlAnimActivity.this);
		mSrowlview2.setIDrawScrawl(ScrawlAnimActivity.this);
		mSrowlview3.setIDrawScrawl(ScrawlAnimActivity.this);
		mSrowlview4.setIDrawScrawl(ScrawlAnimActivity.this);
		mSrowlview1.setOnCompleteListener(mCompleteListener1);
		mSrowlview2.setOnCompleteListener(mCompleteListener2);
		mSrowlview3.setOnCompleteListener(mCompleteListener3);
		mSrowlview4.setOnCompleteListener(mCompleteListener4);
		
		mScrowlViews = new ScrowlView[]{mSrowlview1,mSrowlview2,mSrowlview3,mSrowlview4};
		points = new String[]{"","","",""};

		btn_1 = (Button) findViewById(R.id.btn_1);
		btn_2 = (Button) findViewById(R.id.btn_2);
		btn_3 = (Button) findViewById(R.id.btn_3);
		btn_4 = (Button) findViewById(R.id.btn_4);
		mBtns = new Button[]{btn_1,btn_2,btn_3,btn_4};
		btn_1.setOnClickListener(mOnClickListener);
		btn_2.setOnClickListener(mOnClickListener);
		btn_3.setOnClickListener(mOnClickListener);
		btn_4.setOnClickListener(mOnClickListener);

		tv_send = (TextView) this.findViewById(R.id.tv_send);
		tv_send.setOnClickListener(mOnClickListener);

		iv_reset = (ImageView) this.findViewById(R.id.iv_reset);
		iv_reset.setOnClickListener(mOnClickListener);
		iv_pen = (ImageView) this.findViewById(R.id.iv_pen);
		iv_pen.setOnClickListener(mOnClickListener);
		iv_eraser = (ImageView) this.findViewById(R.id.iv_eraser);
		iv_eraser.setOnClickListener(mOnClickListener);
		
		btn_1.performClick();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume---------");
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

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause---------");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy-------");
	}

	private OnCompleteListener mCompleteListener1 = new OnCompleteListener() {
		
		@Override
		public void onComplete(String password) {
			points[0] = password;
		}
	};
private OnCompleteListener mCompleteListener2 = new OnCompleteListener() {
		
		@Override
		public void onComplete(String password) {
			points[1] = password;
		}
	};
private OnCompleteListener mCompleteListener3 = new OnCompleteListener() {
		
		@Override
		public void onComplete(String password) {
			points[2] = password;
		}
	};
private OnCompleteListener mCompleteListener4 = new OnCompleteListener() {
		
		@Override
		public void onComplete(String password) {
			points[3] = password;
		}
	};
	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.layout_back:
				finish();
				break;
			case R.id.tv_send:
				Log.d(TAG, "send   password======" );
				for (int i = 0; i < mScrowlViews.length; i++) {
					mScrowlViews[i].clearPassword();
				}
				Intent resultIntent = new Intent();
				resultIntent.putExtra("sendAnimSrawl", points);
				ScrawlAnimActivity.this.setResult(RESULT_OK, resultIntent);
				finish();
				break;
			case R.id.iv_reset:
				points[curPage] = "";
				mScrowlViews[curPage].clearPassword();
				break;
			case R.id.iv_eraser:
				if (currentStatus != 1) {
					currentStatus = 1;
					iv_pen.setImageResource(R.drawable.pencil_n);
					iv_eraser.setImageResource(R.drawable.eraser_p);
					mScrowlViews[curPage].setMode(mScrowlViews[curPage].MODE_ERASER);
				}
				break;
			case R.id.iv_pen:
				if (currentStatus != 0) {
					currentStatus = 0;
					iv_pen.setImageResource(R.drawable.pencil_p);
					iv_eraser.setImageResource(R.drawable.eraser_n);
					for (int j = 0; j < mScrowlViews.length; j++) {
						mScrowlViews[j].setMode(mScrowlViews[curPage].MODE_PEN);
					}
//					mScrowlViews[curPage].setMode(mScrowlViews[curPage].MODE_PEN);
				}
				break;
			case R.id.btn_1:
				chagePage(0);
				break;
			case R.id.btn_2:
				chagePage(1);
				break;
			case R.id.btn_3:
				chagePage(2);
				break;
			case R.id.btn_4:
				chagePage(3);
				break;
			}
		}

	};
/**
 * 切换涂鸦页面
 * @param position
 */
	private void chagePage(int position) {
		Log.d(TAG, "chagePage=====posiiton == "+position+"  curPage=="+curPage);
		curPage = position;
		iv_pen.performClick();
		for (int i = 0; i < mScrowlViews.length; i++) {
			if (position == i) {
				mBtns[i].setBackgroundColor(Color_numberbg_p);
				mScrowlViews[i].setVisibility(View.VISIBLE);
			}else{
				mBtns[i].setBackgroundColor(Color_numberbg_n);
				mScrowlViews[i].setVisibility(View.INVISIBLE);
			}
		}
		
	}
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
			for (int i = 0; i < mScrowlViews.length; i++) {
				mScrowlViews[i].setMode(mScrowlViews[i].MODE_PEN);
			}
		}
		return false;
	}
	private static final int RESET = 1;//橡皮擦模式下，全擦后会调用reset，启用画笔模式

}
