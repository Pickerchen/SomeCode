package com.sen5.ocup.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sen5.ocup.R;
import com.sen5.ocup.adapter.TipsListAdapter;
import com.sen5.ocup.callback.RequestCallback.GetTipsCallback;
import com.sen5.ocup.gui.MyListViewPullDownAndUp;
import com.sen5.ocup.gui.MyListViewPullDownAndUp.RefreshListener;
import com.sen5.ocup.struct.RequestHost;
import com.sen5.ocup.struct.Tips;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.Tools;

import java.util.ArrayList;

import cn.sharesdk.framework.ShareSDK;

public class TipsFragment extends Fragment implements GetTipsCallback, Callback {

	private FrameLayout mFrameLayout;

	private static final String TAG = "TipsFragment";
	public static final int tipCount = 3;
	public static boolean isVisible;
	private View mView = null;
	private Activity mActivity = null;
	private Handler mHandler = null;
	private int mWidth;
	private int mHeight;

	private LinearLayout mLayout_tab;
	private TextView mTv_alltips;
	private TextView mTv_marktips;

	private View emptyView = null; // listview 无数据显示时显示该view
	private RelativeLayout layout_empty = null; // listview 无数据显示时显示该view
	private ImageView mIV_refresh;
	private TextView mTV_refresh;

	private MyListViewPullDownAndUp listview_tips = null;
	private ArrayList<Tips> data_tips = null;
	private ArrayList<Tips> data_alltips = null;
	private ArrayList<Tips> data_marktips = null;
	private TipsListAdapter adapter_tips = null;
	private DBManager bdMgr;

	private int currentPage = 0;// 当前页面 0-----》所有 1--------》收藏

	private ProgressBar pb_bluetooth_connecting;// 表示蓝牙正在连接
	private ImageView iv_bluetooth_state;// 表示蓝牙连接状态

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "onReceive---------------------action===" + action);
			if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
//				mHandler.sendEmptyMessage(CONNECTBLUETOOTH_OK);
			} else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
				mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO);
			} else if (action.equals(BluetoothConnectUtils.ACTION_BLUETOOTHSTATE)) {
				int bluestate = intent.getIntExtra(BluetoothConnectUtils.KEY_BLUETOOTHSTATE, -1);

				Log.d(TAG, "bluetooth connectstate bluestate==" + bluestate);
				if (bluestate == BluetoothConnectUtils.CONNECT_OK) {
					mHandler.sendEmptyMessage(CONNECTBLUETOOTH_OK);
				} else if (bluestate == BluetoothConnectUtils.CONNECT_NO) {
					mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO);
				} else if (bluestate == BluetoothConnectUtils.CONNECT_ING) {
					if (mHandler != null){
						mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO);
					}
				}
			}
		}
	};

	/**
	 * 取消viewPage的预加载
	 */
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		Log.d(TAG, "setUserVisibleHint()-----------isVisibleToUser==" + isVisibleToUser);
		isVisible = isVisibleToUser;
		// 判断fragment 是否可见
		if (isVisibleToUser) {
			// 判断是否有数据
//			if (mView != null && data_tips.size() <= 0 && currentPage == 0) {
//				getData();// 获取数据
//			}
			setBluetoothState();
		}
		super.setUserVisibleHint(isVisibleToUser);
	}

	/**
	 * 根据蓝牙连接状态，修改状态图标
	 */
	private void setBluetoothState() {
		if (null != iv_bluetooth_state) {
			Log.d(TAG, "setBluetoothState--------BluetoothConnectUtils.getInstance().bluetoothState ==" + BluetoothConnectUtils.getInstance().getBluetoothState());
			if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTED && BluetoothAdapter.getDefaultAdapter().isEnabled()) {
				pb_bluetooth_connecting.setVisibility(View.GONE);
				iv_bluetooth_state.setVisibility(View.VISIBLE);
				iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_active);
			} else if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTING) {
				pb_bluetooth_connecting.setVisibility(View.VISIBLE);
				iv_bluetooth_state.setVisibility(View.GONE);
				iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_inactive);
			} else if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
				BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_NONE);
				pb_bluetooth_connecting.setVisibility(View.GONE);
				iv_bluetooth_state.setVisibility(View.VISIBLE);
				iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_inactive);
			}else{
				pb_bluetooth_connecting.setVisibility(View.GONE);
				iv_bluetooth_state.setVisibility(View.VISIBLE);
				iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_inactive);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView---------");
		if (mView == null) {
			mActivity = getActivity();
			mView = inflater.inflate(R.layout.fragment_tips, container, false);
			initialComponent();
		} else {
			// mView判断是否已经被加过parent，如果没删除，会发生mView已有parent的错误
			ViewGroup parent = (ViewGroup) mView.getParent();
			if (parent != null) {
				parent.removeView(mView);
			}
		}
		return mView;
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume---------");
		if(null != adapter_tips){
			adapter_tips.notifyDataSetChanged();
		}
		super.onResume();
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart---------");
		super.onStart();
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothConnectUtils.ACTION_BLUETOOTHSTATE);
		mActivity.registerReceiver(receiver, filter);
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop---------");
		super.onStop();
		mActivity.unregisterReceiver(receiver);
	}

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView---------");
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy---------");
		super.onDestroy();
		ShareSDK.stopSDK(mActivity);
		// if (null != adapter_tips.mListBmp) {
		// for (int i = 0; i < adapter_tips.mListBmp.size(); i++) {
		// Bitmap bitmap = adapter_tips.mListBmp.get(i);
		// if(null != bitmap && !bitmap.isRecycled()){
		// bitmap.recycle();
		// bitmap = null;
		// }
		// }
		// }
	}

	/**
	 * 初始化控件
	 */
	private void initialComponent() {
		mFrameLayout = (FrameLayout) mView.findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(mFrameLayout,mActivity);

		ImageView iv_jd = (ImageView) mView.findViewById(R.id.iv_jd);
		iv_jd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri sUrl = Uri.parse(RequestHost.shopUrl_JD);
				Intent it = new Intent(Intent.ACTION_VIEW, sUrl);
				startActivity(it); // 启动浏览器
			}
		});
		ImageView iv_tm = (ImageView) mView.findViewById(R.id.iv_tm);
		iv_tm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri sUrl = Uri.parse(RequestHost.shopUrl_TM);
				Intent it = new Intent(Intent.ACTION_VIEW, sUrl);
				startActivity(it); // 启动浏览器
			}
		});


		DisplayMetrics dm = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		mWidth = dm.widthPixels;
		mHeight = dm.heightPixels;
		mHandler = new Handler(this);

		// 初始化DBManager, Request, Task_tips
		bdMgr = new DBManager(mActivity);

		iv_bluetooth_state = (ImageView) mView.findViewById(R.id.iv_bluetooth_state);
		pb_bluetooth_connecting = (ProgressBar) mView.findViewById(R.id.pb_bluetooth_connecting);
		setBluetoothState();

		mLayout_tab = (LinearLayout) mView.findViewById(R.id.layout_tab);
		mTv_alltips = (TextView) mView.findViewById(R.id.tv_alltips);
		mTv_marktips = (TextView) mView.findViewById(R.id.tv_marktips);

		emptyView = LayoutInflater.from(mActivity).inflate(R.layout.view_empty, null);
		layout_empty = (RelativeLayout) emptyView.findViewById(R.id.layout_emptyview);
		mIV_refresh = (ImageView) emptyView.findViewById(R.id.iv_refresh);
		mTV_refresh = (TextView) emptyView.findViewById(R.id.tv_refresh);

		listview_tips = (MyListViewPullDownAndUp) mView.findViewById(R.id.tips_list);
		listview_tips.setCanPullDown(true);
		listview_tips.setCanPullUp(true);
		ViewGroup parentView = (ViewGroup) listview_tips.getParent();
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		parentView.addView(emptyView, 1, params);// 你需要在这儿设置正确的位置，以达到你需要的效果
		listview_tips.setEmptyView(emptyView);
		data_tips = new ArrayList<Tips>();
		data_alltips = new ArrayList<Tips>();
		data_marktips = new ArrayList<Tips>();

		adapter_tips = new TipsListAdapter(mActivity, mActivity, mWidth, mHeight, data_tips, listview_tips, bdMgr);
		listview_tips.setAdapter(adapter_tips);
		listview_tips.setRefreshListener(new MyRefreshListener());
		layout_empty.setOnClickListener(mOnClickListener);
		mTv_alltips.setOnClickListener(mOnClickListener);
		mTv_marktips.setOnClickListener(mOnClickListener);
		iv_bluetooth_state.setOnClickListener(mOnClickListener);

		getData();// 获取数据
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.layout_emptyview) {// 获取数据
				if (currentPage == 0) {
					getData();
				} else {
					mLayout_tab.setVisibility(View.VISIBLE);
				}
			} else if (v.getId() == R.id.tv_alltips) {// 显示所有数据
				Log.d(TAG, "onclick                 all tips-----------------currentPage==" + currentPage);
				if (currentPage == 1) {
					mTv_alltips.setTextColor(Color.rgb(255, 255, 255));
					mTv_marktips.setTextColor(Color.rgb(0, 128, 255));
					mTv_alltips.setBackgroundResource(R.drawable.switch_l);
					mTv_marktips.setBackgroundResource(R.drawable.switch_frame_r);
					currentPage = 0;
					mIV_refresh.setVisibility(View.VISIBLE);
					mTV_refresh.setVisibility(View.VISIBLE);
					// iv_refresh.setVisibility(View.VISIBLE);
					listview_tips.setCanPullUp(true);

					data_tips.clear();
					data_tips.addAll(data_alltips);
					adapter_tips.setCurpage(currentPage);
					adapter_tips.notifyDataSetChanged();
				}
			} else if (v.getId() == R.id.tv_marktips) {// 显示收藏的数据
				mLayout_tab.setVisibility(View.VISIBLE);
				Log.d(TAG, "onclick                 mark tips-----------------currentPage==" + currentPage);
				if (currentPage == 0) {
					mTv_marktips.setTextColor(Color.rgb(255, 255, 255));
					mTv_alltips.setTextColor(Color.rgb(0, 128, 255));
					mTv_alltips.setBackgroundResource(R.drawable.switch_frame_l);
					mTv_marktips.setBackgroundResource(R.drawable.switch_r);
					currentPage = 1;
					mIV_refresh.setVisibility(View.GONE);
					mTV_refresh.setVisibility(View.GONE);
					// iv_refresh.setVisibility(View.GONE);
					listview_tips.setCanPullUp(false);

					data_alltips.clear();
					data_alltips.addAll(data_tips);

					data_tips.clear();
					data_marktips.clear();
					getMarkDatas();
					data_tips.addAll(data_marktips);
					adapter_tips.setCurpage(currentPage);
					adapter_tips.notifyDataSetChanged();
					if (data_tips.size() <= 0) {
						mLayout_tab.setVisibility(View.VISIBLE);
					}
				}
			} else if (v.getId() == R.id.iv_bluetooth_state) {// 断开or连接蓝牙
				Log.d(TAG, "onclick             iv_bluetooth_state");
				// if (BluetoothConnectUtils.getInstance().getBluetoothState()
				// == BluetoothConnectUtils.BLUETOOTH_CONNECTED &&
				// BluetoothAdapter.getDefaultAdapter().isEnabled()) {
				// Log.d(TAG,
				// "onclick             iv_bluetooth_state      socket.isConnected()");
				// return;
				// }
				// pb_bluetooth_connecting.setVisibility(View.VISIBLE);
				// iv_bluetooth_state.setVisibility(View.GONE);
			}
		}
	};

	/**
	 * 没有数据时，获取数据
	 */
	public void getData() {
		// 判断是否切换语言
		String sysLanguage_country = mActivity.getString(R.string.language);
		String preLanguage_country = Tools.getPreference(mActivity, "ocupLanguage");
		Log.d(TAG, "getData-------sysLanguage_country=="+sysLanguage_country+" preLanguage_country=="+preLanguage_country);
		if (preLanguage_country.equals("")) {
			//未保存语言状态，设置为默认 
			preLanguage_country = "zh-CN";
		}
		if (!sysLanguage_country.equals(preLanguage_country)) {
			//当前系统语言和上次保存的语言不一致,删除数据库中的tip
			bdMgr.deleteAll(bdMgr.tab_tip);
		}
		// 从数据库获取数据
		ArrayList<Tips> dbTips = bdMgr.queryTipsOrTipsmark(bdMgr.tab_tip);
		Log.d(TAG, "getData   bdMgr.query()=dbTips.size=" + dbTips.size());
		if (dbTips.size() > 0) {
			data_tips.addAll(dbTips);
			adapter_tips.notifyDataSetChanged();
		}
		if (data_tips.size() <= 0) {
			HttpRequest.getInstance().getTips2(mActivity, mActivity, TipsFragment.this, tipCount, bdMgr);
		}

	}

	/**
	 * 获取收藏的tips
	 */
	protected void getMarkDatas() {
		// 从数据库获取数据
		ArrayList<Tips> dbMarkTips = bdMgr.queryTipsOrTipsmark(bdMgr.tab_tip_mark);
		Log.d(TAG, "getMarkDatas   bdMgr.query()=dbMarkTips.size=" + dbMarkTips.size());
		if (dbMarkTips.size() > 0) {
			data_marktips.addAll(dbMarkTips);
		}
	}

	class MyRefreshListener implements RefreshListener {

		// 处理下拉刷新
		@Override
		public void pullDownRefresh() {
			Log.d(TAG, "MyRefreshListener-------pullDownRefresh--currentPage==" + currentPage);
			mLayout_tab.setVisibility(View.VISIBLE);
			if (currentPage == 0) {
				refresh();
			} else {
				listview_tips.onPulldownRefreshComplete();
			}
		}

		// 处理上拉刷新
		@Override
		public void pullUpRefresh() {
			Log.d(TAG, "MyRefreshListener-------pullUpRefresh---currentPage==" + currentPage);
			if (currentPage == 0) {
				loadMore();
			} else {
				listview_tips.onPullupRefreshComplete();
			}
		}

		@Override
		public void pullUpStart() {
			Log.d(TAG, "MyRefreshListener-------pullUpStart--");
			if (currentPage == 1 && data_tips.size() <= 0) {
				Log.d(TAG, "MyRefreshListener------pullUpStart-111111111--");
			} else {
				Log.d(TAG, "MyRefreshListener-------pullUpStart-22222222222222222-");
				mHandler.sendEmptyMessage(TAB_GONE);
			}
		}
	}

	/**
	 * 获取更多tips
	 */
	private void loadMore() {
		if (data_tips.size() > 0) {
			Log.d(TAG, "loadMore()-----data_tips.size()==" + data_tips.size() + "  data_tips.get(data_tips.size()-1).getDate()==" + data_tips.get(data_tips.size() - 1).getDate());
			listview_tips.onPullupRefreshComplete();
//			HttpRequest.getInstance().getMoreTips(mActivity, mActivity, TipsFragment.this, tipCount, data_tips.get(data_tips.size() - 1).getDate(), bdMgr);
		} else {
			listview_tips.onPullupRefreshComplete();
		}
	}

	/**
	 * 刷新tips
	 */
	private void refresh() {
		if (data_tips.size() > 0) {
			HttpRequest.getInstance().getRefreshTips(mActivity, mActivity, TipsFragment.this, tipCount, data_tips.get(0).getDate(), bdMgr);
		} else {
			listview_tips.onPulldownRefreshComplete();
		}
	}

	@Override
	public void getTips_ImagePathSuccess(String imagePath) {
	}

	@Override
	public void getTips_ImagePathFailed() {
		mHandler.sendEmptyMessage(GET_TIP_FAILED);
	}

	@Override
	public void getTips_failed() {
		mHandler.sendEmptyMessage(GET_TIP_FAILED);
	}

	@Override
	public void getTips_success(ArrayList<Tips> tips) {
		if (null != bdMgr) {
			bdMgr.addTipsOrTipsmark(bdMgr.tab_tip, tips);
		}
		Message msg = new Message();
		msg.what = GET_TIP_SSUCCESS;
		msg.obj = tips;
		mHandler.sendMessage(msg);
	}

	@Override
	public void getMoreTips_success(ArrayList<Tips> tips) {
		if (null != bdMgr) {
			bdMgr.addTipsOrTipsmark(bdMgr.tab_tip, tips);
		}
		Message msg = new Message();
		msg.what = GET_MORE_TIP_SSUCCESS;
		msg.obj = tips;
		mHandler.sendMessage(msg);
	}

	@Override
	public void getMoreTips_failed() {
		mHandler.sendEmptyMessage(GET_MORE_TIP_FAILED);
	}

	@Override
	public void getRefreshTips_success(ArrayList<Tips> tips) {
		Message msg = new Message();
		msg.what = GET_REFRESH_TIP_SSUCCESS;
		msg.obj = tips;
		mHandler.sendMessage(msg);
	}

	@Override
	public void getRefreshTips_failed() {
		mHandler.sendEmptyMessage(GET_REFRESH_TIP_FAILED);
	}

	@Override
	public void getTipsImg_success() {
		mHandler.sendEmptyMessage(GET_TIP_IMG_SSUCCESS);
	}

	@Override
	public void getTipsImg_failed() {
		mHandler.sendEmptyMessage(GET_TIP_IMG_SSUCCESS);
	}

	@Override
	public void getTipsing() {
		mHandler.sendEmptyMessage(GET_TIP_ING);

	}

	@Override
	public boolean handleMessage(Message msg) {
		if (null != TipsFragment.this.getView()) {
			switch (msg.what) {
			// 隐藏tab
			case TAB_GONE:
				mLayout_tab.setVisibility(View.GONE);
				break;
			// 获取最近tips or 刷新tips成功
			case GET_TIP_SSUCCESS:
				listview_tips.onPulldownRefreshComplete();
				data_tips.clear();
				getData();
				adapter_tips.notifyDataSetChanged();
				break;
			case GET_REFRESH_TIP_SSUCCESS:
				listview_tips.onPulldownRefreshComplete();
				ArrayList<Tips> tips = (ArrayList<Tips>) msg.obj;
				if (tips.size() < tipCount) {
					if (null != bdMgr) {
						bdMgr.addTipsOrTipsmark(bdMgr.tab_tip, tips);
					}
				} else {
					if (null != bdMgr) {
						bdMgr.deleteAll(bdMgr.tab_tip);
						bdMgr.addTipsOrTipsmark(bdMgr.tab_tip, tips);
					}
				}
				data_tips.clear();
				getData();
				adapter_tips.notifyDataSetChanged();
				break;
			// 获取更多tips成功
			case GET_MORE_TIP_SSUCCESS:
				ArrayList<Tips> tips_more = (ArrayList<Tips>) msg.obj;
				if (null != data_tips) {
					Log.d(TAG, "getMoreTips_success  tips_more.size()-===========" + tips_more.size());
					data_tips.addAll(tips_more);
				}
				listview_tips.onPullupRefreshComplete();
				adapter_tips.notifyDataSetChanged();
				break;
			// 获取最近tips or 刷新tips失败
			case GET_TIP_FAILED:
			case GET_REFRESH_TIP_FAILED:
				listview_tips.onPulldownRefreshComplete();
//				OcupToast.makeText(mActivity, getString(R.string.getTips_failed), Toast.LENGTH_SHORT).show();
				adapter_tips.notifyDataSetChanged();
				break;
			// 获取更多tips失败
			case GET_MORE_TIP_FAILED:
				listview_tips.onPullupRefreshComplete();
				adapter_tips.notifyDataSetChanged();
				break;
			case GET_TIP_ING:
				listview_tips.onPulldownRefreshComplete();
				listview_tips.onPullupRefreshComplete();
				adapter_tips.notifyDataSetChanged();
				break;
			// 获取tips图片成功
			case GET_TIP_IMG_SSUCCESS:
				break;
			case CONNECTBLUETOOTH_OK:
				if (null != iv_bluetooth_state) {
					pb_bluetooth_connecting.setVisibility(View.GONE);
					iv_bluetooth_state.setVisibility(View.VISIBLE);
					iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_active);
				}
				break;
			case CONNECTBLUETOOTH_NO:
				if (null != iv_bluetooth_state) {
					pb_bluetooth_connecting.setVisibility(View.GONE);
					iv_bluetooth_state.setVisibility(View.VISIBLE);
					iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_inactive);
				}
				break;
			case CONNECTBLUETOOTH_ING:
				pb_bluetooth_connecting.setVisibility(View.VISIBLE);
				iv_bluetooth_state.setVisibility(View.GONE);
				break;
			default:
				break;
			}
		}
		return false;
	}

	private final int GET_TIP_SSUCCESS = 0;
	private final int GET_TIP_FAILED = 1;
	private final int GET_MORE_TIP_SSUCCESS = 2;
	private final int GET_MORE_TIP_FAILED = 3;
	private final int GET_REFRESH_TIP_SSUCCESS = 4;
	private final int GET_REFRESH_TIP_FAILED = 5;
	private final int GET_TIP_IMG_SSUCCESS = 6;
	private final int TAB_GONE = 8;
	private final int CONNECTBLUETOOTH_OK = 9;
	private final int CONNECTBLUETOOTH_NO = 10;
	private final int CONNECTBLUETOOTH_ING = 12;
	private final int GET_TIP_ING = 11;// 正在请求
}
