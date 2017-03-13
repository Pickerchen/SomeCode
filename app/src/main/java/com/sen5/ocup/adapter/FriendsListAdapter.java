package com.sen5.ocup.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sen5.ocup.R;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.fragment.OchatFragment;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.gui.MyListViewPullDownAndUp;
import com.sen5.ocup.gui.SegoTextView;
import com.sen5.ocup.struct.FriendData;
import com.sen5.ocup.util.TipsBitmapLoader;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;

import java.util.ArrayList;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 好友列表适配器
 */
public class FriendsListAdapter extends BaseAdapter {

	protected static final String TAG = "FriendsListAdapter";
	private Activity mActivity;
	private Context mContext;
	private Fragment fragment;
	private MyListViewPullDownAndUp mListview;
	private ArrayList<FriendData> data_friend;
	private CustomDialog dialog_editmood;
	private boolean isFirst;//是否是第一次setadapter

	public FriendsListAdapter(Activity activity, Context mContext, Fragment fragment, ArrayList<FriendData> data_friend, MyListViewPullDownAndUp listview, CustomDialog dialog_editmood) {
		super();
		 mActivity = activity;
		this.mContext = mContext;
		this.fragment = fragment;
		this.data_friend = data_friend;
		Log.e(TAG, "-----123321----------data_friend = " + data_friend.size());
		mListview = listview;
		this.dialog_editmood = dialog_editmood;
	}

	@Override
	public int getCount() {
		return data_friend.size();
	}

	@Override
	public FriendData getItem(int position) {
		return data_friend.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "getView() ----------------position==" + position);
		Log.e(TAG, "-----123321----------data_friend = " + data_friend.size());
		Holder holder = null;
		if (null == convertView) {
			holder = new Holder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_friend, null);
			holder.tv_offline_msg = (SegoTextView) convertView.findViewById(R.id.tv_offline_msg);
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_username);
			holder.tv_progress = (TextView) convertView.findViewById(R.id.tv_progress);
			holder.tv_goals = (TextView) convertView.findViewById(R.id.tv_goals);
			holder.tv_mood = (TextView) convertView.findViewById(R.id.tv_mood);
			holder.tv_edit = (TextView) convertView.findViewById(R.id.tv_edit);
			holder.iv_user = (ImageView) convertView.findViewById(R.id.iv_user);
//			holder.pb = (Circle_ProgressBar) convertView.findViewById(R.id.pb);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		if (position == 0) {
			holder.tv_edit.setVisibility(View.VISIBLE);
		}else{
			holder.tv_edit.setVisibility(View.GONE);
		}
		holder.tv_edit.setOnClickListener(mOnClickListener);
//		convertView.setOnClickListener(mOnClickListener);
		if (!OchatFragment.isVisible && !isFirst) {
			Log.d(TAG, "getView() ------Invisible--------");
			return convertView;
		}
		isFirst = false;
		Log.d(TAG, "getView() ---data_friend.get(position).getCount_offline_msg()==" + data_friend.get(position).getCount_offline_msg());
		// 设置未读消息
		if (data_friend.get(position).getCount_offline_msg() > 0) {
			holder.tv_offline_msg.setVisibility(View.VISIBLE);
			holder.tv_offline_msg.setText("" + data_friend.get(position).getCount_offline_msg());
		} else {
			holder.tv_offline_msg.setVisibility(View.GONE);
		}
		// 设置昵称
		if (data_friend.get(position).getName()== null || data_friend.get(position).getName().equals("")) {
			holder.tv_name.setText(UtilContact.DEFAULT_OCUP_NAME);
		}else{
			holder.tv_name.setText(data_friend.get(position).getName());
		}
		// 设置心情
		holder.tv_mood.setText(data_friend.get(position).getMood());
		
		//设置头像
		holder.iv_user.setImageResource(R.drawable.user_me);
		if (position == 0) {
			getBitmap(holder.iv_user, OcupApplication.getInstance().mOwnCup.getAvatorPath());
		}else{
			getBitmap(holder.iv_user, OcupApplication.getInstance().mOtherCup.getAvatorPath());
		}
		int progress = data_friend.get(position).getProgress();
		Log.e(TAG, "============================" +progress);
		holder.tv_progress.setText("" + progress+"%");
		holder.tv_goals.setText("" + data_friend.get(position).getGoals()+"ml");
//		holder.pb.setIDrawProgress(new IDrawProgress() {
//
//			@Override
//			public void drawProgress(int pb, Circle_ProgressBar viewpb, Canvas canvas) {
//				Log.d(TAG, "getView() -----drawProgress");
//				viewpb.setCustomProgress(pb, 100, canvas);
//			}
//		}, progress);
		return convertView;
	}

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int position = mListview.getPositionForView(v) - 1;
			if (v.getId() == R.id.tv_edit) {// 编辑心情
				dialog_editmood.setInfo(data_friend.get(position).getMood());
				dialog_editmood.show();
			} else {// 进入聊天界面
			}
		}
	};
	
	public void getBitmap(final ImageView view, String url) {
			if (url == null) {
				return;
			}
			Bitmap bmp = TipsBitmapLoader.getInstance().getFromMemory(url);
			Log.d(TAG, "-------------------------url = == " + url + ":::bmp = " + (bmp == null));
			if (bmp == null) {
//				bmp = TipsBitmapLoader.getInstance().getFromFile(url,5);
				if (bmp == null) {
					TipsBitmapLoader.getInstance().asyncLoadBitmap(url, new TipsBitmapLoader.asyncLoadCallback() {

						@Override
						public void load(Bitmap bitmap) {
							if (null != bitmap){
								bitmap = Tools.getRoundedBitmap(bitmap,100);
							}
							final Bitmap bt = bitmap;
							mActivity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (bt!=null) {
										view.setImageBitmap(bt);
									}
								}
							});
						}
					});
				} else {
					bmp = Tools.getRoundedBitmap(bmp,100);
					view.setImageBitmap(bmp);
				}
			} else {
				bmp = Tools.getRoundedBitmap(bmp,100);
				view.setImageBitmap(bmp);
			}
	}
	
	class Holder {
		private SegoTextView tv_offline_msg;
		private TextView tv_name;
		private TextView tv_progress;
		private TextView tv_goals;
		private ImageView iv_user;
//		private Circle_ProgressBar pb;
		private TextView tv_mood;
		private TextView tv_edit;
	}
	

}
