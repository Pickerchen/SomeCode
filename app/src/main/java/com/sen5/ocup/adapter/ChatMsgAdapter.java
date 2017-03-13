package com.sen5.ocup.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.activity.ChatActivity;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.callback.CustomInterface.IDialog;
import com.sen5.ocup.callback.CustomInterface.IDrawPoint;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.gui.MyListViewPullDownAndUp;
import com.sen5.ocup.gui.ScrowlView;
import com.sen5.ocup.struct.ChatMsgEntity;
import com.sen5.ocup.util.GifOpenHelper;
import com.sen5.ocup.util.TipsBitmapLoader;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 : 消息列表适配器
 */
public class ChatMsgAdapter extends BaseAdapter implements IDialog{

	protected static final String TAG = "ChatMsgAdapter";
	private ViewHolder viewHolder = null;
	private Activity mActivity;
	private ChatMsgEntity deleteEntity = null;

	private IDialog mIDiaog;
	private List<ChatMsgEntity> coll;
	private LayoutInflater mInflater;
	private Context context;
	private MyListViewPullDownAndUp mListview;

	private String avator_url;

	//长按删除聊天对话框
	private CustomDialog mCustomDialog;

	private OnClickListener mOnclickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.iv_chatstatus) {// 处理消息状态图标的点击事件
				int position = mListview.getPositionForView(v) - 1;
				if (coll.get(position).getStatus() == 2) {// 发送不成功的消息再次发送？
					CustomDialog mCustomDialog = new CustomDialog(context, mIDiaog, R.style.custom_dialog, CustomDialog.SEND_DIALOG, position);
					mCustomDialog.show();
				} else {
				}
			}
		}
	};

	public ChatMsgAdapter(Activity activity, Context context, IDialog iDiaog, List<ChatMsgEntity> coll, MyListViewPullDownAndUp listview, boolean isme,String avator) {
		mActivity = activity;
		mIDiaog = iDiaog;
		this.coll = coll;
		mInflater = LayoutInflater.from(context);
		this.context = context;
		mListview = listview;
		this.avator_url = avator;
	}

	public int getCount() {
		return coll.size();
	}

	public Object getItem(int position) {
		return coll.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		// 区别两种view的类型，标注两个不同的变量来分别表示各自的类型
		return coll.get(position).getFromFlag();
	}
	ChatMsgEntity entity;
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Logger.e(TAG,"chatAdapter.getView"+position+" and data.size is"+coll.size());
		entity = coll.get(position);
		viewHolder = null;
//		if (convertView == null) {
		viewHolder = new ViewHolder();
		if (entity.getFromFlag() == ChatMsgEntity.FROM_ME) {
			convertView = mInflater.inflate(R.layout.listitem_chat_right, null);
			viewHolder.iv_chatstatus = (ImageView) convertView.findViewById(R.id.iv_chatstatus);
			viewHolder.progressBar_send = (ProgressBar) convertView.findViewById(R.id.progressBar_send);
		} else if (OcupApplication.getInstance().mOwnCup.getHuanxin_userid() != null && entity.getHuanxinID().equals(OcupApplication.getInstance().mOwnCup.getHuanxin_userid())) {
			convertView = mInflater.inflate(R.layout.listitem_chat_right, null);
			viewHolder.iv_chatstatus = (ImageView) convertView.findViewById(R.id.iv_chatstatus);
			viewHolder.progressBar_send = (ProgressBar) convertView.findViewById(R.id.progressBar_send);
		} else {
			convertView = mInflater.inflate(R.layout.listitem_chat_left2, null);
			viewHolder.iv_chatstatus = (ImageView) convertView.findViewById(R.id.iv_chatstatus);
			viewHolder.progressBar_send = (ProgressBar) convertView.findViewById(R.id.progressBar_send);
		}
		viewHolder.allChatContent = convertView.findViewById(R.id.allchatcontent);
		viewHolder.allChatContent.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				Logger.e(TAG,"onlongClick coming position is"+position+"  and data.size is"+coll.size());
				deleteEntity = coll.get(position);
				mCustomDialog = new CustomDialog(mActivity,ChatMsgAdapter.this,R.style.custom_dialog,CustomDialog.DIALOG_DELETE_MSG,deleteEntity);
				mCustomDialog.show();
				//在dialog回调中进行UI更新
				Logger.e("onLongClickListener","position ="+position+"onLongClick+content="+deleteEntity.getText()+"date ="+deleteEntity.getDate());
				return false;
			}
		});
		viewHolder.iv_user = (ImageView) convertView.findViewById(R.id.iv_user);
		viewHolder.iv_user.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(mActivity, R.style.CustomDialog);
				dialog.setContentView(R.layout.dialog_big_pic);
				ImageView imageView = (ImageView)dialog.findViewById(R.id.dialog_big_pic_iv);
				ProgressBar progressBar = (ProgressBar)dialog.findViewById(R.id.dialog_big_progressBar1);
				FrameLayout layout = (FrameLayout)dialog.findViewById(R.id.dialog_big_pic_ll);

				layout.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}});
				dialog.show();
				if (((Integer)v.getTag()) != 1) { // 设置自己的头像 tag 不等于1表示点击的图标是自己
//						Bitmap bitmap = TipsBitmapLoader.getInstance().getFromFile(OcupApplication.getInstance().mOwnCup.getAvatorPath());
					String avatorPath = Tools.getPreference(context,UtilContact.OwnAvatar);
					Log.e(TAG, "----------------avatorPath =  " + avatorPath);
					getBitmap(imageView,avatorPath,progressBar);
					if(!TextUtils.isEmpty(avatorPath)){
						avatorPath = avatorPath.split("@")[0];
					}
					Log.e(TAG, "----------------avatorPath =  " + avatorPath);
					progressBar.setVisibility(View.VISIBLE);
					getBitmap(imageView,avatorPath,progressBar);
				} else {
					String avatorOtherPath = avator_url;
					getBitmap(imageView, avatorOtherPath,progressBar);
					Log.e(TAG, "----------------avatorOtherPath =  " + avatorOtherPath);
					if(!TextUtils.isEmpty(avatorOtherPath)){
						avatorOtherPath = avatorOtherPath.split("@")[0];
					}
					Log.e(TAG, "----------------avatorOtherPath =  " + avatorOtherPath);
					progressBar.setVisibility(View.VISIBLE);
					getBitmap(imageView, avatorOtherPath,progressBar);
				}
			}
		});
		viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
		viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_chatcontent);
		viewHolder.layout_text = (LinearLayout) convertView.findViewById(R.id.layout_text);
		viewHolder.layout_sc = (LinearLayout) convertView.findViewById(R.id.layout_scrawl);
		viewHolder.ivContent = (ScrowlView) convertView.findViewById(R.id.sc_chatcontent);

		convertView.setTag(viewHolder);
//		} else {
//			viewHolder = (ViewHolder) convertView.getTag();
//		}

		viewHolder.tvSendTime.setText(formatTime(entity.getDate()));
		// 设置头像
		viewHolder.iv_user.setImageResource(R.drawable.user_me);
		if (entity.getFromFlag() == ChatMsgEntity.FROM_ME) { // 设置自己的头像
			viewHolder.iv_user.setTag(0);
			getBitmap(viewHolder.iv_user, Tools.getPreference(context, UtilContact.OwnAvatar), null);
		} else {
			viewHolder.iv_user.setTag(1);
			getBitmap(viewHolder.iv_user, avator_url, null);
		}
		if (null != viewHolder.progressBar_send) {
			viewHolder.iv_chatstatus.setOnClickListener(mOnclickListener);
		}
		if (entity.getType() == ChatMsgEntity.TYPE_TXT || entity.getType() == ChatMsgEntity.TYPE_ANIM_FACE) {
			Log.d(TAG, "position===" + position + "  type==0");
			viewHolder.layout_sc.setVisibility(View.GONE);
			viewHolder.tvContent.setVisibility(View.VISIBLE);
			GifOpenHelper mGifOpenHelper = new GifOpenHelper();
			SpannableString spannableString = mGifOpenHelper.getExpressionString(context, viewHolder.tvContent, entity.getText());
			viewHolder.tvContent.setText(spannableString);
		} else if (entity.getType() == ChatMsgEntity.TYPE_SCRAWL) {
			Log.d(TAG, "position===" + position + "  type==1");
			viewHolder.tvContent.setVisibility(View.GONE);
			viewHolder.layout_sc.setVisibility(View.VISIBLE);
			viewHolder.ivContent.setIDrawPoint(new IDrawPoint() {
				@Override
				public void setPoints(ScrowlView view, String points) {
					view.isDraw = false;
					view.setmPoints(points);
				}
			}, entity.getText());
		} else if (entity.getType() == ChatMsgEntity.TYPE_SCRAWL_ANIM) {
			viewHolder.layout_sc.setVisibility(View.VISIBLE);
			viewHolder.tvContent.setVisibility(View.GONE);
			viewHolder.ivContent.setIDrawPoint(new IDrawPoint() {

				@Override
				public void setPoints(ScrowlView view, String points) {
					view.isDraw = false;
					view.setmPoints(points);
				}
			}, entity.getText());
		} else {
			Log.d(TAG, "else position===" + position + "  type=="+entity.getType() );
		}
		if (entity.getText().toString().equals(ChatActivity.KEY_SHAKE)) {
			viewHolder.tvContent.setText(context.getString(R.string.btn_shake));
		}

		if (entity.getFromFlag() == ChatMsgEntity.FROM_ME) { // 设置自己发送状态
			if (null != viewHolder.progressBar_send) {
				switch (entity.getStatus()) {// 0-----正在发送 1----已查看消息 //
					// 2------未发送消息
					case 0:
						viewHolder.progressBar_send.setVisibility(View.VISIBLE);
						viewHolder.iv_chatstatus.setVisibility(View.GONE);
						break;
					case 1:
						viewHolder.progressBar_send.setVisibility(View.GONE);
						viewHolder.iv_chatstatus.setVisibility(View.GONE);
						break;
					case 2:
						viewHolder.iv_chatstatus.setImageResource(R.drawable.send_failed);
						viewHolder.progressBar_send.setVisibility(View.GONE);
						viewHolder.iv_chatstatus.setVisibility(View.VISIBLE);
						break;
					case 3:
						viewHolder.progressBar_send.setVisibility(View.GONE);
						viewHolder.iv_chatstatus.setVisibility(View.GONE);
						break;

					default:
						break;
				}
			}
		}
		return convertView;
	}

	class ViewHolder {
		public TextView tvSendTime;
		public TextView tvContent;
		public LinearLayout layout_sc;
		public LinearLayout layout_text;
		public ScrowlView ivContent;
		private ImageView iv_chatstatus;
		private ProgressBar progressBar_send;
		private ImageView iv_user;
		//包裹所有聊天内容的控件
		private View allChatContent;
	}

	/**
	 * 格式化时间
	 *
	 * @param time
	 * @return
	 */
	private String formatTime(long time) {
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
		return date;
	}

	public void getBitmap(final ImageView view, String url, final ProgressBar progressBar) {
		if (url == null) {
			return;
		}
		Bitmap bmp = TipsBitmapLoader.getInstance().getFromMemory(url);
		if (bmp == null) {
//			bmp = TipsBitmapLoader.getInstance().getFromFile(url);
			if (bmp == null) {
				TipsBitmapLoader.getInstance().asyncLoadBitmap(url, new TipsBitmapLoader.asyncLoadCallback() {
					@Override
					public void load(Bitmap bitmap) {
						final Bitmap bt = bitmap;
						mActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (bt != null) {
									Bitmap bitmap1 = bt;
									bitmap1 = Tools.getRoundedBitmap(bitmap1,100);
									view.setImageBitmap(bitmap1);
								}
								if(null != progressBar){
									progressBar.setVisibility(View.INVISIBLE);
								}
							}
						});
					}
				});
			}
//			else {
//				view.setImageBitmap(bmp);
//				if(null != progressBar){
//
//					progressBar.setVisibility(View.INVISIBLE);
//				}
//			}
		} else {
			bmp = Tools.getRoundedBitmap(bmp,100);
			view.setImageBitmap(bmp);
			if(null != progressBar){
				progressBar.setVisibility(View.INVISIBLE);
			}
		}
	}


//实现对话框回调
	@Override
	public void ok(int type) {
		switch (type){
			case CustomDialog.DIALOG_DELETE_MSG:
				Logger.e(TAG,"收到删除回调，删除的消息是"+deleteEntity.getText());
				coll.remove(deleteEntity);
				notifyDataSetChanged();
				break;
		}

	}

	@Override
	public void ok(int type, Object obj) {

	}

	@Override
	public void cancel(int type) {

	}
}