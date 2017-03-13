package com.sen5.ocup.adapter;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.sen5.ocup.R;
import com.sen5.ocup.callback.RequestCallback.mateCupCallback;
import com.sen5.ocup.struct.ChatEmoji;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 表情填充器
 */
public class FaceAdapter extends BaseAdapter {

	private static final String TAG = "FaceAdapter";

	private Context mContext;
	private List<ChatEmoji> data;

	private LayoutInflater inflater;

	private int size = 0;

	public FaceAdapter(Context context, List<ChatEmoji> list) {
		mContext = context;
		this.inflater = LayoutInflater.from(context);
		this.data = list;
		this.size = list.size();
	}

	@Override
	public int getCount() {
		return this.size;
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ChatEmoji emoji = data.get(position);
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_face, null);
			viewHolder.iv_face = (ImageView) convertView.findViewById(R.id.item_iv_face);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (emoji.getId() == R.drawable.face_del_icon) {
			convertView.setBackgroundDrawable(null);
			viewHolder.iv_face.setImageResource(emoji.getId());
		} else if (TextUtils.isEmpty(emoji.getCharacter())) {
			convertView.setBackgroundDrawable(null);
			viewHolder.iv_face.setImageDrawable(null);
		} else {
			viewHolder.iv_face.setTag(emoji);
			viewHolder.iv_face.setImageResource(emoji.getId());
		}

		return convertView;
	}

	class ViewHolder {

		public ImageView iv_face;
	}
}