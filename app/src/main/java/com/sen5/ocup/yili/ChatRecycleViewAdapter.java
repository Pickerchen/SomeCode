package com.sen5.ocup.yili;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.blutoothstruct.CupStatus;
import com.sen5.ocup.struct.ChatMsgEntity;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.TipsBitmapLoader;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;

import java.util.List;

/**
 * Created by chenqianghua on 2016/10/21.
 */
public class ChatRecycleViewAdapter extends RecyclerView.Adapter{

    private String TAG = ChatRecycleViewAdapter.class.getSimpleName();
    private Context mContext;
    private List<Object> data;
    private OnItemClickListener listener;
    private DBManager mDBManager;
    private String huanXinId;
    private Activity mActivity;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflate = LayoutInflater.from(mContext);
        View itemView = inflate.inflate(R.layout.item_recycleview,parent,false);
        return new MyViewHolder(itemView,listener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyViewHolder viewHolder = (MyViewHolder) holder;
        if(data.get(position) instanceof UserInfo){
            UserInfo userInfo = (UserInfo) data.get(position);
            String nickName = userInfo.getNickname();
            if (nickName.startsWith("86")){
                nickName = userInfo.getNickname().substring(2,nickName.length());
            }
            viewHolder.setNickName(nickName);
            getBitmap(viewHolder.mImageView,userInfo.getAvator(),null);
            ChatMsgEntity entity = mDBManager.queryLastChat(huanXinId,huanXinId);
            if (entity != null){
                viewHolder.setTv_time(Tools.formatTime(entity.getDate()));
                if (entity.getType() != ChatMsgEntity.TYPE_SCRAWL && entity.getType() != ChatMsgEntity.TYPE_SCRAWL_ANIM && entity.getType() != ChatMsgEntity.TYPE_SHAKE){
                    viewHolder.setTv_last_sms(entity.getText());
                }
                else{
                    if (entity.getType() == ChatMsgEntity.TYPE_SHAKE){
                        viewHolder.setTv_last_sms(mContext.getResources().getString(R.string.chat_shake));
                    }
                    else {
                        viewHolder.setTv_last_sms(mContext.getResources().getString(R.string.chat_scrawl));
                    }
                }
            }
            if (Tools.getPreference(mContext,UtilContact.BLUE_ADD) != "" && Tools.getPreference(mContext,UtilContact.BLUE_ADD) != null){
                if ("true".equals(Tools.getPreference(mContext,UtilContact.isAlived))){
                    viewHolder.iv_cup_off.setVisibility(View.GONE);
                    viewHolder.iv_cup_on.setVisibility(View.VISIBLE);
                    viewHolder.iv_temp.setVisibility(View.VISIBLE);
                    viewHolder.tv_current_temp.setVisibility(View.VISIBLE);
                    if (CupStatus.getInstance().getCur_water_temp() != 0){
                        viewHolder.tv_current_temp.setText(CupStatus.getInstance().getCur_water_temp()+mContext.getString(R.string.tempratureunit));
                    }
                }
                else {
                    viewHolder.iv_cup_on.setVisibility(View.GONE);
                    viewHolder.iv_cup_off.setVisibility(View.VISIBLE);
                    viewHolder.iv_temp.setVisibility(View.VISIBLE);
                    viewHolder.tv_current_temp.setVisibility(View.VISIBLE);
                }
            }
            //从未连接过水杯
            else {
                viewHolder.iv_temp.setVisibility(View.GONE);
                viewHolder.tv_current_temp.setVisibility(View.GONE);
                viewHolder.iv_cup_off.setVisibility(View.GONE);
                viewHolder.iv_cup_on.setVisibility(View.GONE);
            }
        }
        else if (data.get(position) instanceof FriendInfo){
            FriendInfo friendInfo = (FriendInfo)data.get(position);
            viewHolder.setNickName(friendInfo.getNickname());
                Logger.e(TAG,"friendInfo = "+friendInfo.getAvator());
                getBitmap(viewHolder.mImageView, friendInfo.getAvator(), null);
//            Logger.e("ChatRecycleViewAdapter","position = "+position+"unReadCount = "+friendInfo.getUnReadCount());
            if (friendInfo.getUnReadCount() >0){
                viewHolder.setTv_offline_msg(friendInfo.getUnReadCount());
            }
            else {
                viewHolder.setTv_offline_Gone();
            }
            String toHuanXinId = friendInfo.getContact_id();
            ChatMsgEntity entity = mDBManager.queryLastChat(huanXinId,toHuanXinId);
            if (entity != null){
                viewHolder.setTv_time(Tools.formatTime(entity.getDate()));
                if (entity.getType() != ChatMsgEntity.TYPE_SCRAWL && entity.getType() != ChatMsgEntity.TYPE_SCRAWL_ANIM && entity.getType() != ChatMsgEntity.TYPE_SHAKE){
                    viewHolder.setTv_last_sms(entity.getText());
                }
                else{
                    if (entity.getType() == ChatMsgEntity.TYPE_SHAKE){
                        viewHolder.setTv_last_sms(mContext.getResources().getString(R.string.chat_shake));
                    }
                    else {
                        viewHolder.setTv_last_sms(mContext.getResources().getString(R.string.chat_scrawl));
                    }
                }
            }
            if (friendInfo.getOpenData() > 0){
                viewHolder.iv_temp.setVisibility(View.VISIBLE);
                viewHolder.tv_current_temp.setVisibility(View.VISIBLE);
                viewHolder.tv_current_temp.setText(friendInfo.getOpenData()+mContext.getString(R.string.tempratureunit));
            }
            else {
                viewHolder.iv_temp.setVisibility(View.GONE);
                viewHolder.tv_current_temp.setVisibility(View.GONE);
            }
            if (friendInfo.isHaveCup()){
                viewHolder.iv_cup_off.setVisibility(View.VISIBLE);
                viewHolder.iv_cup_on.setVisibility(View.GONE);
                if (friendInfo.isOnLine()){
                    viewHolder.iv_cup_off.setVisibility(View.GONE);
                    viewHolder.iv_cup_on.setVisibility(View.VISIBLE);
                    if (friendInfo.getOpenData() > 0){
                        viewHolder.tv_current_temp.setVisibility(View.VISIBLE);
                        viewHolder.iv_temp.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    viewHolder.tv_current_temp.setVisibility(View.GONE);
                    viewHolder.iv_temp.setVisibility(View.GONE);
                }
            }
            else {
                viewHolder.iv_cup_on.setVisibility(View.GONE);
                viewHolder.iv_cup_off.setVisibility(View.GONE);
            }
        }
    }

    public ChatRecycleViewAdapter(Context context, List<Object> data, OnItemClickListener listener) {
        mContext = context;
        mActivity = (Activity)mContext;
        this.data = data;
        this.listener = listener;
        mDBManager = new DBManager(mContext);
        huanXinId = Tools.getPreference(context, UtilContact.HuanXinId);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }



    public void getBitmap(final ImageView view, String url, final ProgressBar progressBar) {
        if (url == null) {
            return;
        }
        Bitmap bmp = TipsBitmapLoader.getInstance().getFromMemory(url);
        if (bmp == null) {
			bmp = TipsBitmapLoader.getInstance().getFromFile(url);
            if (bmp == null) {
                TipsBitmapLoader.getInstance().asyncLoadBitmap(url, new TipsBitmapLoader.asyncLoadCallback() {
                    @Override
                    public void load(Bitmap bitmap) {
                        final Bitmap bt = bitmap;
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (bt != null) {
                                    view.setImageBitmap(bt);
                                }
                                if(null != progressBar){
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                });
            }
			else {
				view.setImageBitmap(bmp);
				if(null != progressBar){

					progressBar.setVisibility(View.INVISIBLE);
				}
			}
        } else {
            view.setImageBitmap(bmp);
            if(null != progressBar){
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
    }
}
