package com.sen5.ocup.contacts;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ybq.android.spinkit.style.Circle;
import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.callback.RequestCallback;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.TipsBitmapLoader;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;
import com.sen5.ocup.yili.CircleImageView;
import com.sen5.ocup.yili.FriendInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by chenqianghua on 2016/10/17.
 */
public class ContactsRecycleViewAdapter extends RecyclerView.Adapter {

    private String TAG = ContactsRecycleViewAdapter.class.getSimpleName();
    private Context mContext;
    private List<Object> datas;
    private LayoutInflater inflater;
    public List<FriendInfo> friends = new ArrayList<>();
    private IItemClickListen listen;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UtilContact.requestSuccess:
                    mDialog.dismiss();
                    Tools.showToast(mContext,mContext.getString(R.string.addRequest));
                    break;
                case UtilContact.hasAddedFriend:
                    Tools.showToast(mContext,mContext.getString(R.string.hasAdded));
                    break;
            }
        }
    };

    public void setFriendSize(List<FriendInfo> friends2){
        friends = friends2;
    }

    public ContactsRecycleViewAdapter(Context context, List<Object> datas) {
        this.mContext = context;
        this.datas = datas;
        inflater = LayoutInflater.from(mContext);
    }

    public ContactsRecycleViewAdapter(IItemClickListen listen) {
        this.listen = listen;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ChildViewHolder holder = (ChildViewHolder) viewHolder;
//        holder.setText(datas.get(position));
        if (datas.get(position) instanceof FriendInfo){
            final FriendInfo friendInfo = (FriendInfo) datas.get(position);
            if (!friendInfo.getIsFriend()){
                holder.btn_friend.setVisibility(View.GONE);
                holder.btn_invite.setVisibility(View.GONE);
                holder.btn_add.setVisibility(View.VISIBLE);
                holder.tv_name.setTextSize(15);
                holder.setName(friendInfo.getPhoneNum());
                holder.btn_add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog();
                        HttpRequest.getInstance().addFriendRequest(mContext,friendInfo.getContact_id(),mCallBack);
                    }
                });
            }
            else {
                getBitmap(holder.iv_head,friends.get(position).getAvator(),null);
                holder.iv_head.setTag(position);
                holder.btn_friend.setVisibility(View.VISIBLE);
                holder.setName(friendInfo.getNickname());
            }
        }
        else {
            holder.btn_invite.setVisibility(View.VISIBLE);
                    holder.setData((SortModel)(datas.get(position)),position);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewHolder, int position) {
        View view = inflater.inflate(R.layout.contact_recycle_item, null);
        return new ChildViewHolder(view,listen);
    }

    public class ChildViewHolder extends RecyclerView.ViewHolder {
    public TextView tv_name;
    public CircleImageView iv_head;
    public Button btn_add,btn_invite,btn_friend;

    public ChildViewHolder(View view, final IItemClickListen listen) {
        super(view);
        iv_head = (CircleImageView) view.findViewById(R.id.imgHead);
        tv_name = (TextView) view.findViewById(R.id.title);
        btn_add = (Button) view.findViewById(R.id.btn_addFriend);
        btn_invite = (Button) view.findViewById(R.id.btn_invite);
        btn_friend = (Button) view.findViewById(R.id.btn_friend);
        this.setIsRecyclable(false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listen != null){
                    listen.onItemClick(getAdapterPosition());
                }
            }
        });
    }

        public void setName(String name){
            Logger.e(TAG,"setName = "+name);
            tv_name.setText(name);
        }

    public void setData(final SortModel sortModel, int position){
        tv_name.setText(sortModel.getName());

        btn_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listen != null){
                    listen.onItemClick(getAdapterPosition());
                }
                else {
                    Uri smsToUri = Uri.parse("smsto:"+sortModel.getMobile());
                    Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri );
                    mIntent.putExtra("sms_body", "快加入暖哄哄水杯 网址：http://a.app.qq.com/o/simple.jsp?pkgname=com.sen5.nhh.ocup");
                    mContext.startActivity(mIntent);
                }
            }
        });
    }
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
                        Activity activity = (Activity)mContext;
                        activity.runOnUiThread(new Runnable() {
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

    private RequestCallback.IAddFriendCallBack mCallBack = new RequestCallback.IAddFriendCallBack() {
        @Override
        public void sendSuccess(String token) {
            mHandler.sendEmptyMessage(UtilContact.requestSuccess);
        }

        @Override
        public void sendFail(int type) {
            switch (type){
            }
        }

        @Override
        public void hasAdded() {
            mHandler.sendEmptyMessage(UtilContact.hasAddedFriend);
        }
    };

    private Dialog mDialog;
    private Circle mCircleDrawable;
    private void showDialog() {
        mDialog = new Dialog(mContext,R.style.custom_dialog_loading);
        mDialog.setContentView(R.layout.dialog_register_loading);
        mDialog.getWindow().setLayout((1* Tools.getScreenWH((Activity) mContext)[0])/2,200);
        ImageView imageView = (ImageView) mDialog.findViewById(R.id.dialog_loading_iv);
        mCircleDrawable = new Circle();
        imageView.setBackground(mCircleDrawable);
        mCircleDrawable.setColor(android.graphics.Color.parseColor("#FF818C"));
        mCircleDrawable.start();
        mDialog.show();
    }

    public interface IItemClickListen{
        void onItemClick(int position);
    }
    public void setOnItemClickListen(IItemClickListen listen1){
        this.listen = listen1;
    }
}
