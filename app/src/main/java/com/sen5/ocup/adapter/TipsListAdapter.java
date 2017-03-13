package com.sen5.ocup.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.sen5.ocup.R;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.gui.MyListViewPullDownAndUp;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.struct.Tips;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.TipsBitmapLoader;
import com.sen5.ocup.util.Tools;
import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.util.ArrayList;
import java.util.List;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.OnekeyShareTheme;

/**
 * 停用
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 * 
 *          类说明 : 贴士列表适配器
 */
public class TipsListAdapter extends BaseAdapter {

	protected static final String TAG = "TipsListAdapter";
	// private TipsHolder holder = null;
	private Activity mActivity;
	private Context mContext;
	private int mWidth;
	private LayoutParams lp;// item 的布局
	private ArrayList<Tips> data_tips;
	private MyListViewPullDownAndUp mListview;
	private DBManager dbMan;
	private int curpage;// 当前显示的是那一页

	private Bitmap mDefaultBG = null; // 默认的背景图片
//	public Bitmap mBlurBG = null; // 点击后模糊的背景图片
	private int mBlurPos = -1; // 点击模糊的图片所在位置
	private int mLastPosition = -1; // 记录最后一个item的位置， 以实现预加载

	public ArrayList<Bitmap> mListBmp = new java.util.ArrayList<Bitmap>();

	private final static String APPID = "1101513923";
	public static QQAuth mQQAuth;
	private Tencent mTencent;
	private QQShare mQQShare = null;
	private int shareType;
	private String imgPath;

	public TipsListAdapter(Activity activity, Context mContext, int width,
			int height, ArrayList<Tips> data_tips,
			MyListViewPullDownAndUp listview, DBManager dbMan) {
		super();
		mActivity = activity;
		this.mContext = mContext;
		mWidth = width;
		this.data_tips = data_tips;
		mListview = listview;
		this.dbMan = dbMan;

		mDefaultBG = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.picture);
	}

	@Override
	public int getCount() {
		return data_tips.size();
	}

	@Override
	public Tips getItem(int position) {
		return data_tips.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setCurpage(int curpage) {
		this.curpage = curpage;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		mLastPosition = position;
		TipsHolder holder = null;
		if (null == convertView) {
			holder = new TipsHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.listitem_tips, null);
			holder.norImgView = (ImageView) convertView
					.findViewById(R.id.normal_image);
			holder.iv_share = (ImageView) convertView
					.findViewById(R.id.imageView_share);
			holder.iv_mark = (ImageView) convertView
					.findViewById(R.id.imageView_mark);

			holder.iv_share.setOnClickListener(mOnClickListener);
			holder.iv_mark.setOnClickListener(mOnClickListener);

			holder.norImgView.setScaleType(ScaleType.FIT_XY);
			lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			convertView.setLayoutParams(lp);
			convertView.setTag(holder);
		} else {
			holder = (TipsHolder) convertView.getTag();
		}
		holder.setTipView(position);

		return convertView;
	}

	public void bufferBitmap() {
		if (mLastPosition >= 0) {
			int pos = mLastPosition;
			mLastPosition = -1;

			if (data_tips == null) {
				return;
			}

			int size = data_tips.size();
			List<String> bufferUrls = new ArrayList<String>();
			for (int i = pos - 2; i <= pos + 2; i++) {
				if (i >= 0 && i < size && i != pos) {
					bufferUrls.add(data_tips.get(i).getImgUrl());
				}
			}

			TipsBitmapLoader.getInstance().asyncLoadBitmapBuffer(bufferUrls);
		}
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.imageView_share) {// 分享
				showShare(v);
				// ShareSDK.initSDK(mActivity);
				// share(v);
			} else if (v.getId() == R.id.imageView_mark) {// 收藏
				if (dbMan.queryTipsmark(data_tips.get(
						mListview.getPositionForView(v) - 1).getDate())) {// 已收藏
					// 取消收藏
					dbMan.deleteTip(dbMan.tab_tip_mark,
							data_tips.get(mListview.getPositionForView(v) - 1));
					if (curpage == 0) {
						data_tips.get(mListview.getPositionForView(v) - 1)
								.setIsMarked(0);
					} else {
						data_tips.remove(mListview.getPositionForView(v) - 1);
					}
					notifyDataSetChanged();
				} else {
					mark(v);
				}
			}
		}
	};

	/**
	 * 一键分享
	 * 
	 * @param v
	 */
	private void showShare(View v) {
		Tools.saveView2SDcard((View) v.getParent().getParent().getParent(),
				mContext.getString(R.string.app_name) + "_tip" + ".jpg");
		ShareSDK.initSDK(mActivity);
		OnekeyShare oks = new OnekeyShare();
		// 关闭sso授权
		oks.disableSSOWhenAuthorize();
		// 设置分享主题，天蓝色基调主题，默认为传统主题
		oks.setTheme(OnekeyShareTheme.SKYBLUE);

		// 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
		// oks.setNotification(R.drawable.icon_29,
		// mContext.getString(R.string.app_name));
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(mContext.getString(R.string.app_name));
		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		oks.setTitleUrl("http://www.rrioo.com");
		// text是分享文本，所有平台都需要这个字段
		oks.setText(data_tips.get(mListview.getPositionForView(v) - 1)
				.getBrief().toString());
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		imgPath = Tools.getSDPath() + "/"
				+ mContext.getString(R.string.app_name) + "_tip" + ".jpg";
		oks.setImagePath(imgPath);// (Tools.getSDPath()+"/" + mContext.getString(R.string.app_name) + ".jpg");// 确保SDcard下面存在此张图片

		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl("http://www.rrioo.com");
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite(mContext.getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		oks.setSiteUrl("http://www.rrioo.com");
		// 启动分享GUI
		oks.show(mActivity);
	}

	/**
	 * 收藏
	 * 
	 * @param v
	 */
	protected void mark(View v) {
		dbMan.addTipOrTipmark(dbMan.tab_tip_mark,
				data_tips.get(mListview.getPositionForView(v) - 1));
		data_tips.get(mListview.getPositionForView(v) - 1).setIsMarked(1);
		notifyDataSetChanged();
	}

	class TipsHolder {
		private int position = -1;
		private ImageView norImgView;
//		private BlurImageView blurImgView;
//		private TextView tv_title;
//		private TextView tv_brief;
		private ImageView iv_share;
		private ImageView iv_mark;

		private void setTipView(int position) {
			this.position = position;
			Tips tip = data_tips.get(position);
			if (dbMan.queryTipsmark(tip.getDate())) {// 已收藏
				iv_mark.setImageResource(R.drawable.ic_fav_p);
			} else {
				iv_mark.setImageResource(R.drawable.ic_fav_n);
			}
//			tv_title.setText(tip.getTitle());
//			tv_brief.setText(tip.getBrief());

			// 设置背景图片
			Bitmap BGBitmap = TipsBitmapLoader.getInstance().getFromMemory(
					tip.getImgUrl());
			mListBmp.add(BGBitmap);
			if (BGBitmap != null) {
				if (tip.isBlur()) {
//					if (mBlurBG == null || mBlurPos != position) {
//						mBlurBG = Blur.fastblur(mContext, BGBitmap, 25);
//						mBlurPos = position;
//					}
//					norImgView.setImageBitmap(mBlurBG);
				} else {
					norImgView.setImageBitmap(BGBitmap);
				}
			} else {
				norImgView.setImageBitmap(mDefaultBG);
				final int posBak = TipsHolder.this.position;
				TipsBitmapLoader.getInstance().asyncLoadBitmap(tip.getImgUrl(),
						new TipsBitmapLoader.asyncLoadCallback() {

							@Override
							public void load(Bitmap bitmap) {
								final Bitmap bt = bitmap;
								mActivity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (TipsHolder.this.position == posBak) {
											if (null == bt) {
//												norImgView
//														.setImageResource(R.drawable.picture);
											} else {
												norImgView.setImageBitmap(bt);
											}
										}
									}
								});
							}
						});
			}
		}
	}

	/**
	 * 分享到QQ
	 */
	private OnClickListener mClick_QQshareListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			initQQ();
			shareType = QQShare.SHARE_TO_QQ_TYPE_IMAGE;// 纯图片分享
			final Bundle params = new Bundle();
			int mExtarFlag = 0x00;
			params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imgPath);
			params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "Ocup");
			params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, shareType);
			params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, mExtarFlag);
			doShareToQQ(params);
		}
	};

	/**
	 * 用异步方式启动分享
	 * 
	 * @param params
	 */
	private void doShareToQQ(final Bundle params) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				mQQShare.shareToQQ(mActivity, params, new IUiListener() {

					@Override
					public void onCancel() {
						if (shareType != QQShare.SHARE_TO_QQ_TYPE_IMAGE) {
							toastMessage(mActivity, "onCancel: ", "d");
						}
					}

					@Override
					public void onComplete(Object response) {
						toastMessage(mActivity,
								"onComplete: " + response.toString(), "d");
					}

					@Override
					public void onError(UiError e) {
						toastMessage(mActivity, "onError: " + e.errorMessage,
								"e");
					}

				});
			}
		}).start();
	}

	/**
	 * 用Toast显示消息
	 * 
	 * @param activity
	 * @param message
	 * @param logLevel
	 *            填d, w, e分别代表debug, warn, error; 默认是debug
	 */
	private static final void toastMessage(final Activity activity,
			final String message, String logLevel) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				OcupToast.makeText(activity, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * 初始化QQAPi工具
	 */
	private void initQQ() {
		mQQAuth = QQAuth.createInstance(APPID, OcupApplication.getInstance());
		mTencent = Tencent.createInstance(APPID, mActivity);
		mQQShare = new QQShare(mActivity, TipsListAdapter.mQQAuth.getQQToken());
	}
}
