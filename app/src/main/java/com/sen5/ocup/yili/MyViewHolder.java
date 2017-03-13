package com.sen5.ocup.yili;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;

/**
 * Created by chenqianghua on 2016/10/21.
 */
public class  MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
    private TextView tv_time,tv_nickName,tv_last_sms,tv_offline_msg;
    public TextView tv_current_temp;
    public ImageView iv_cup_on,iv_cup_off;//水杯在线不在线状态
    public ImageView iv_temp;//温度显示log
    private View mView;
    public ImageView mImageView;//头像
    private int position;
    private OnItemClickListener mlistener;
    public MyViewHolder(View itemView,OnItemClickListener listener) {
        super(itemView);
        mView = itemView;
        tv_time =  (TextView) itemView.findViewById(R.id.tv_chat_time);
        tv_nickName = (TextView) itemView.findViewById(R.id.tv_nickName);
        tv_last_sms = (TextView) itemView.findViewById(R.id.tv_last_sms);
        tv_offline_msg = (TextView) itemView.findViewById(R.id.tv_offline_msg);
        mImageView = (ImageView) itemView.findViewById(R.id.iv_user);
        iv_cup_off = (ImageView) itemView.findViewById(R.id.iv_cup_off);
        iv_cup_on = (ImageView) itemView.findViewById(R.id.iv_cup_on);
        iv_temp = (ImageView) itemView.findViewById(R.id.iv_temp);
        tv_current_temp = (TextView) itemView.findViewById(R.id.tv_temp);
        this.mlistener = listener;
        mView.setOnClickListener(this);
        mView.setOnLongClickListener(this);
        setIsRecyclable(false);
    }

    public void setNickName(String name){
        tv_nickName.setText(name);
    }

    public void setTv_last_sms(String sms){
        tv_last_sms.setText(sms);
    }

    public void setTv_time(String sms){
        tv_time.setText(sms);
    }
    public void setTv_offline_msg(int count){
        tv_offline_msg.setVisibility(View.VISIBLE);
        tv_offline_msg.setText(count+"");
    }
    public void setTv_offline_Gone(){
        tv_offline_msg.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        Logger.e("MyViewHolder","getAdapterPosition = "+getAdapterPosition()+"getPosition = "+getPosition()+"getItemid = "+getItemId());
        if (getAdapterPosition() != -1) {
            mlistener.onItemClick(v, getAdapterPosition());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        Logger.e("MyViewHolder","onLongClick执行"+"getAdapterPosition = "+getAdapterPosition());
        //第一个item，不能被删除。注意getposition在某种情况下为-1，还未找到原因
        if (getAdapterPosition() != -1 && getAdapterPosition() != 0){
            mlistener.onLongItemClick(v,getAdapterPosition());
        }
        return true;
    }

//        Intent intent = new Intent(mActivity, ChatActivity.class);
//        intent.putExtra("IsMe", true);
//        intent.putExtra("userName", mDataFriend.get(pos).getName());
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivityForResult(intent, Tools.CHAT_GREQUEST_CODE);
}
