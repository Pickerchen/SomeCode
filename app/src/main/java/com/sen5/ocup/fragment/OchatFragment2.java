package com.sen5.ocup.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.activity.ChatActivity;
import com.sen5.ocup.activity.DialogActivity;
import com.sen5.ocup.activity.MainActivity;
import com.sen5.ocup.activity.RegisterActivity;
import com.sen5.ocup.callback.CustomInterface;
import com.sen5.ocup.callback.RequestCallback;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.receiver.HuanxinBroadcastReceiver;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.FaceConversionUtil;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;
import com.sen5.ocup.yili.ActionItem;
import com.sen5.ocup.yili.ChatRecycleViewAdapter;
import com.sen5.ocup.yili.ContactsActivity;
import com.sen5.ocup.yili.FriendInfo;
import com.sen5.ocup.yili.OnItemClickListener;
import com.sen5.ocup.yili.PhoneNumSearchActivity;
import com.sen5.ocup.yili.TitlePopwindow;
import com.sen5.ocup.yili.UserInfo;
import com.sen5.ocup.yili.UserInfoActivity;
import com.sen5.ocup.zxing.CaptureActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * Created by chenqianghua on 2016/10/21.
 */
public class OchatFragment2 extends Fragment implements RequestCallback.IGetInfoCallBack,
        SwipeRefreshLayout.OnRefreshListener,Handler.Callback,OnItemClickListener,View.OnClickListener,
        CustomInterface.IDialog,RequestCallback.IAddFriendCallBack,RequestCallback.IDeleteFriendCallBack,
        RequestCallback.IRefreshLoginStatus{

    //flag
    private static  String TAG = OchatFragment2.class.getSimpleName();
    private static  final int refreshOK = 1;
    private static  final int notifyAdapter = 24;
    private static final int deleteFail = 26;
    private static final int getFail = 28;
    private static final int refreshGone = 29;
    private static final int GETRELATIONSHIP_OK = 2;
    private static final int GETRELATIONSHIP_OK_NOFRIENDS = 3;
    private static final int GETRELATIONSHIP_NO = 4;

    private static final int MATECUP_OK = 5;
    private static final int MATECUP_NO = 6;
    private static final int MATECUP_NO_NET = 7;

    private static final int NOT_LOGIN = 8;
    private final int CONNECTBLUETOOTH_OK = 9;
    private final int CONNECTBLUETOOTH_NO = 10;
    private final int CONNECTBLUETOOTH_ING = 22;
    private final int renameSuccess = 23;
    //该好友已经添加了
    private final int hasAdded = 25;
    //开始获取好友信息
    private final int startGetUserFriends = 27;
    //data
    private Activity mActivity;
    private Context mcontext;
    private List<Object> data;
    private ChatRecycleViewAdapter adapter;
    private Handler mhandle = new Handler(this);
    private DBManager mDBManager;
    private int count_unreadMsg;
    private UserInfo userInfo;
    //当前被长按的position
    private int nowPosition;
    //view
    private View mView;
    private SwipeRefreshLayout refresh;
    private RecyclerView recycleView;
    private FrameLayout mFrameLayout;
    private ImageView iv_bluetooth_state;
    private ProgressBar pb_bluetooth_connecting;// 表示蓝牙正在连接

    //弹窗
    private TitlePopwindow titlePopup;

    //dialog
    private CustomDialog mDialog;

    //click
    private ImageView iv_pop;

    //广播监听
    //实时更新蓝牙状态
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
//				mHandler.sendEmptyMessage(CONNECTBLUETOOTH_OK);
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                if (null != iv_bluetooth_state) {
                    mhandle.sendEmptyMessage(CONNECTBLUETOOTH_NO);
                }
            } else if (action.equals(HuanxinBroadcastReceiver.ACTION_DISCONNECTED)) {
                String errorString = intent.getStringExtra("errorString");
                // if (getString(R.string.login_in_other).equals(errorString)) {
                mhandle.sendEmptyMessage(NOT_LOGIN);
            } else if (action.equals(BluetoothConnectUtils.ACTION_BLUETOOTHSTATE)) {
                int bluestate = intent.getIntExtra(BluetoothConnectUtils.KEY_BLUETOOTHSTATE, -1);
                Logger.e(TAG, "bluetooth connectstate bluestate==" + bluestate);
                if (bluestate == BluetoothConnectUtils.CONNECT_OK) {
                    mhandle.sendEmptyMessage(CONNECTBLUETOOTH_OK);
                } else if (bluestate == BluetoothConnectUtils.CONNECT_NO) {
                    mhandle.sendEmptyMessage(CONNECTBLUETOOTH_NO);
                } else if (bluestate == BluetoothConnectUtils.CONNECT_ING) {
                    mhandle.sendEmptyMessage(CONNECTBLUETOOTH_NO);
                }
            }
            else if (action.equals(SettingFragment.renameSuccess)){
                //收到更改昵称的广播
                Logger.e("OchatFragment2","收到renameSuccess广播");
                String nickname = intent.getStringExtra("nickname");
                Message message = new Message();
                message.what = renameSuccess;
                message.obj = nickname;
                mhandle.sendMessage(message);
            }
            else if (action.equals(HuanxinBroadcastReceiver.ReceiverChat)){
                //收到信息，来自huanxinBroadcastReceiver的广播,更新未读信息UI
                notifyUnreadCount();
            }
            else if(action.equals(HuanxinBroadcastReceiver.ReceiverGroupChat)){
                //收到群聊修改状态的广播，更新UI
                String contact_id = intent.getStringExtra("contact_id");
                updateFriendsStatus(contact_id);
            }
            else if (action.equals(SettingFragment.updateAvatarSuccess)){
                //头像已更改
                UserInfo userInfo = (UserInfo) data.get(0);
                userInfo.setAvator(Tools.getPreference(mcontext,UtilContact.OwnAvatar));
                data.remove(0);
                data.add(0,userInfo);
                adapter.notifyDataSetChanged();
            }
            else if (action.equals(UserInfoActivity.DeleteBroadcast)){
                //该好友已经被删除
                List<FriendInfo> friendInfos = mDBManager.queryFriends();
                for(int i =1; i<data.size(); i++){
                    data.remove(i);
                }
                if (friendInfos != null){
                    data.addAll(friendInfos);
                }
                adapter.notifyDataSetChanged();
            }
            else if (action.equals(DialogActivity.CheckSure)){
                //收到好友同意的广播，向服务器请求好友列表
                HttpRequest.getInstance().getUserFriends(mcontext,OchatFragment2.this);
            }
            else if (action.equals(MainActivity.receiverCupStatusInfo)){
                Logger.e(TAG,"OchatreceiverCupstatusInfo");
//                data.remove(userInfo);
//                data.add(0,userInfo);
//                adapter.notifyItemChanged(0);
                data.clear();
                data.add(mDBManager.queryYiLiCup());
                data.addAll(mDBManager.queryFriends());
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    //只会执行一次
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        mActivity = getActivity();
        mcontext = getContext();
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_chat2, container, false);
            initialComponent();
            initData();
        } else {
            // mView判断是否已经被加过parent，如果没删除，会发生mView已有parent的错误
            ViewGroup parent = (ViewGroup) mView.getParent();
            if (parent != null) {
               parent.removeView(mView);
            }
        }
        mhandle.sendEmptyMessageDelayed(startGetUserFriends,2000);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(HuanxinBroadcastReceiver.ACTION_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothConnectUtils.ACTION_BLUETOOTHSTATE);
        //接收更改用户昵称的广播
        filter.addAction(SettingFragment.renameSuccess);
        //头像已更改
        filter.addAction(SettingFragment.updateAvatarSuccess);
        //收到聊天更改UI
        filter.addAction(HuanxinBroadcastReceiver.ReceiverChat);
        //收到环信群聊status信息
        filter.addAction(HuanxinBroadcastReceiver.ReceiverGroupChat);
        //在USerInfoActivity界面删除好友广播
        filter.addAction(UserInfoActivity.DeleteBroadcast);
        //好友申请，得到好友同意
        filter.addAction(DialogActivity.CheckSure);
        //收到cupstatus
        filter.addAction(MainActivity.receiverCupStatusInfo);
        mActivity.registerReceiver(receiver, filter);
        setBluetoothState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity.unregisterReceiver(receiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.e("OchatFragment2","onActivityResult执行");
        if (requestCode == Tools.SCANNIN_GREQUEST_CODE) {
            if (resultCode == mActivity.RESULT_OK) {
                Bundle bundle = data.getExtras();
                String result = bundle.getString("result");
                Logger.e(TAG+"onActivityResult","二维码扫描结果为"+result.toString());
                if (null != result) {
                    if (result.equals("cancle")) {
                        return;
                    } else if (result.length()<6){
                        HttpRequest.getInstance().addFriendRequest(mActivity,result,OchatFragment2.this);
                    }
                    //扫描到的是下载链接
                    else{
                        result = result.substring(result.indexOf("#")+8,result.length());
                        Logger.e(TAG,"result = "+result);
                        HttpRequest.getInstance().addFriendRequest(mActivity,result,OchatFragment2.this);
                    }
                } else {
                }
            }
        }
        else if (requestCode == Tools.CHAT_GREQUEST_CODE){
            notifyUnreadCount();
        }
    }

    private void initialComponent() {
        iv_bluetooth_state = (ImageView) mView.findViewById(R.id.iv_bluetooth_state);
        pb_bluetooth_connecting = (ProgressBar) mView.findViewById(R.id.pb_bluetooth_connecting);
        refresh = (SwipeRefreshLayout) mView.findViewById(R.id.swiperefreshlayout);
        //设置颜色
        refresh.setColorSchemeResources(R.color.main_color_2,R.color.main_color);
        refresh.setOnRefreshListener(this);
        recycleView = (RecyclerView) mView.findViewById(R.id.recyclerview);
        mFrameLayout = (FrameLayout) mView.findViewById(R.id.durian_head_layout);
        iv_pop = (ImageView) mView.findViewById(R.id.iv_popwin);
        iv_pop.setOnClickListener(this);
        Tools.setImmerseLayout(mFrameLayout,mActivity);
        initPop();
    }
        private void initData(){
            //另起线程加载表情
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FaceConversionUtil.getInstace().getFileText(mActivity.getApplication());
                }
            }).start();
            data = new ArrayList<>();
            mDBManager = new DBManager(mcontext);
             userInfo = mDBManager.queryYiLiCup();
            if (userInfo != null) {
                Logger.e("ChatFragment2", userInfo.getNickname() + "------" + userInfo.getUserID());
            }
            if (userInfo != null){
                data.add(userInfo);
            }
            data.addAll(mDBManager.queryFriends());
            LinearLayoutManager manager = new LinearLayoutManager(mcontext);
            manager.setOrientation(LinearLayoutManager.VERTICAL);
            adapter = new ChatRecycleViewAdapter(mcontext,data,this);
            recycleView.setLayoutManager(manager);
            recycleView.setAdapter(adapter);
        }


    //更新未读信息数量
    private void notifyUnreadCount(){
        if (null != data && null != adapter) {
            Logger.e(TAG, "receiverHuanxinChatBroadcast---");
            if (data.size() > 1) {
                List<FriendInfo> friendInfos = new ArrayList<>();
                for (int i = 1; i < data.size(); i++) {
                    FriendInfo friendInfo = (FriendInfo) (data.get(i));
                    //把有未读消息的放入一个集合中通过时间排序
                    Logger.e(TAG,"data.size = "+data.size()+"friendInfo.lastTime = "+friendInfo.getLastMsgTime());
                    count_unreadMsg = mDBManager.countUnreadMsg(friendInfo.getContact_id());
                    if (count_unreadMsg > 0){
                        //收到消息置顶，放在自己下边
                        friendInfos.add(friendInfo);
//                        data.add(1,friendInfo);
                    }
                    friendInfo.setUnReadCount(count_unreadMsg);
                }
                for (FriendInfo friendInfo : friendInfos){
                    data.remove(friendInfo);
                }
                if (friendInfos.size() != 0 && friendInfos.size() != 1) {
                    Collections.sort(friendInfos, new Comparator<FriendInfo>() {
                        @Override
                        public int compare(FriendInfo lhs, FriendInfo rhs) {
                            long time1 = lhs.getLastMsgTime();
                            long time2 = rhs.getLastMsgTime();
                            Logger.e(TAG,"time1 = "+time1+"time2 = "+time2);
                            if (time1 > time2) {
                                return -1;
                            }
                            return 1;
                        }
                    });
                }
                data.addAll(1,friendInfos);
                mhandle.sendEmptyMessage(notifyAdapter);
            }
            else {
                mhandle.sendEmptyMessage(notifyAdapter);
            }
        }
    }

    //更新好友状态值：
    private void updateFriendsStatus(String contact_id){
        //需要更改状态的好友
//        FriendInfo updateFrined = null;
//        FriendInfo oldFriend = null;
//       List<FriendInfo>  friendInfos = mDBManager.queryFriends();
//        for (FriendInfo friendInfo:friendInfos){
//            if (friendInfo.getContact_id() == contact_id){
//                updateFrined = friendInfo;
//            }
//        }
//        for (int i =0; i<data.size(); i++){
//
//        }
        Logger.e(TAG,"updateFriendsStatus is coming");
        data.clear();
        data.add(mDBManager.queryYiLiCup());
        data.addAll(mDBManager.queryFriends());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Logger.e("setUserVisibleHint触发");
        if (isVisibleToUser) {
            setBluetoothState();
            if (null != mDBManager) {
                Log.d(TAG, "setUserVisibleHint)-------count_unreadMsg==" + count_unreadMsg);
                notifyUnreadCount();
            }
        }
    }

    @Override
    public void onRefresh() {
        HttpRequest.getInstance().getUserFriends(mcontext,this);
        //十秒钟后刷新还没结束，自动消失
        mhandle.sendEmptyMessageDelayed(refreshGone,10000);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case startGetUserFriends:
                HttpRequest.getInstance().getUserFriends(mcontext,OchatFragment2.this);
                break;
            case notifyAdapter:
                adapter.notifyDataSetChanged();
                break;
            case refreshOK:
                refresh.setRefreshing(false);
                break;
            case CONNECTBLUETOOTH_OK:
                if (null != iv_bluetooth_state) {
                    pb_bluetooth_connecting.setVisibility(View.GONE);
                    iv_bluetooth_state.setVisibility(View.VISIBLE);
                    iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_active);
                }
                break;
            case CONNECTBLUETOOTH_ING:
                pb_bluetooth_connecting.setVisibility(View.VISIBLE);
                iv_bluetooth_state.setVisibility(View.GONE);
                break;
            case CONNECTBLUETOOTH_NO:
                if (null != iv_bluetooth_state) {
                    pb_bluetooth_connecting.setVisibility(View.GONE);
                    iv_bluetooth_state.setVisibility(View.VISIBLE);
                    iv_bluetooth_state.setImageResource(R.drawable.btn_bluetooth_inactive);
                }
                break;
            case renameSuccess:
                if (data != null && data.size() > 0){
                    UserInfo userInfo = (UserInfo) data.get(0);
                    userInfo.setNickname(msg.obj.toString());
                    adapter.notifyDataSetChanged();
                }
                break;
            case hasAdded:
                Toast.makeText(mcontext,R.string.hasAdded,Toast.LENGTH_LONG).show();
                break;
            case deleteFail:
                Toast.makeText(mcontext,R.string.deletefail,Toast.LENGTH_LONG).show();
                break;
            //获取联系人失败
            case getFail:
                if (msg.arg1 == 401){
                    Toast.makeText(mcontext,getString(R.string.loginDate),Toast.LENGTH_LONG).show();
                    refresh.setRefreshing(false);
                    Intent intent = new Intent(mcontext,RegisterActivity.class);
                    intent.putExtra("isReLogin",true);
                    startActivity(intent);
                }
                else if (msg.arg1 == 500){
                    Toast.makeText(mcontext,R.string.check_network,Toast.LENGTH_LONG).show();
                    refresh.setRefreshing(false);
                }
                break;
            case refreshGone:

            break;

        }
        return false;
    }

    @Override
    public void onItemClick(View view,int position) {
        if (position == 0) {// 和自己聊天
            Intent intent = new Intent(mActivity, ChatActivity.class);
            intent.putExtra("IsMe", true);
            UserInfo userInfo = (UserInfo) data.get(0);
            intent.putExtra("userName", userInfo.getNickname());
            intent.putExtra("avatar",userInfo.getAvator());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            OchatFragment2.this.startActivityForResult(intent, Tools.CHAT_GREQUEST_CODE);
        } else if (position != 0) {// 和对方聊天
                Intent intent = new Intent(mActivity, ChatActivity.class);
                intent.putExtra("IsMe", false);
            Logger.e("data.size = "+data.size()+""+"position = "+position+"");
            FriendInfo friendInfo =  (FriendInfo) data.get(position);
                    intent.putExtra("userName", friendInfo.getNickname());
                    intent.putExtra("to_huanxinID", friendInfo.getContact_id());
                    intent.putExtra("avatar",friendInfo.getAvator());
                    intent.putExtra("phoneNum",friendInfo.getPhoneNum());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                OchatFragment2.this.startActivityForResult(intent, Tools.CHAT_GREQUEST_CODE);
                // }
            }
    }

    @Override
    public void onLongItemClick(View view, int position) {
        nowPosition = position;
        CustomDialog dialog = new CustomDialog(mcontext,OchatFragment2.this,R.style.custom_dialog,CustomDialog.DIALOG_DELETE_FRIEND,null);
        dialog.show();
    }

    /**
     * 根据蓝牙连接状态，修改状态图标
     */
    private void setBluetoothState() {
        if (null != iv_bluetooth_state) {
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


    private void initPop() {
        // 实例化标题栏弹窗
        titlePopup = new TitlePopwindow(mcontext, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        titlePopup.setItemOnClickListener(onitemClick);
        // 给标题栏弹窗添加子类
        titlePopup.addAction(new ActionItem(mcontext, R.string.scan,
                R.drawable.ic_pop_scan));
        titlePopup.addAction(new ActionItem(mcontext, R.string.inputphoneNum,
                R.drawable.ic_pop_friend));
        titlePopup.addAction(new ActionItem(mcontext, R.string.contactsfriend,
                R.drawable.ic_pop_contacts));
        titlePopup.addAction(new ActionItem(mcontext,R.string.shareqrcode,
                R.drawable.ic_pop_share));
    }
    private TitlePopwindow.OnItemOnClickListener onitemClick = new TitlePopwindow.OnItemOnClickListener() {
        @Override
        public void onItemClick(ActionItem item, int position) {
            // mLoadingDialog.show();
            switch (position) {
                case 0:// 扫一扫
                    Intent intent = new Intent(mActivity, CaptureActivity.class);
                    String huanXinId = Tools.getPreference(mcontext,UtilContact.HuanXinId);
                    Logger.e(TAG+"PopItemClick",huanXinId);
                    intent.putExtra("cupid",huanXinId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, Tools.SCANNIN_GREQUEST_CODE);
                    break;
                case 1:// 输入手机号查找好友
                    Intent intent1 = new Intent(mcontext,PhoneNumSearchActivity.class);
                    startActivityForResult(intent1,1);
                    break;
                case 2:// 通讯录好友
                    Intent intent2 = new Intent(mcontext,ContactsActivity.class);
                    startActivity(intent2);
                    break;
                case 3:// 分享二维码
            mDialog = new CustomDialog(mcontext, OchatFragment2.this, R.style.custom_dialog, CustomDialog.DIALOG_SHARE_QRCODE, null);
            mDialog.show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_popwin:
                titlePopup.show(iv_pop);
            break;
        }
    }

    @Override
    public void ok(int type) {
        if (type == CustomDialog.DIALOG_SHARE_QRCODE){
            ShareSDK.initSDK(mcontext);
            Platform.ShareParams sp = new Platform.ShareParams();
            sp.setShareType(Platform.SHARE_TEXT);
            Platform weixin = ShareSDK.getPlatform(Wechat.NAME);
            weixin.setPlatformActionListener(new PlatformActionListener() {
                @Override
                public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                    Logger.e("分享完成");
                }

                @Override
                public void onError(Platform platform, int i, Throwable throwable) {
                    Logger.e("分享出错");
                }

                @Override
                public void onCancel(Platform platform, int i) {
                    Logger.e("分享取消");
                }
            });
            weixin.share(sp);
        }
        else if (type == CustomDialog.DIALOG_DELETE_FRIEND){
            Logger.e(TAG,"确认删除好友"+"好友位置为："+nowPosition);
            FriendInfo friendInfo = (FriendInfo) data.get(nowPosition);
            HttpRequest.getInstance().deleteFriendRequest(mcontext,friendInfo.getContact_id(),OchatFragment2.this);
        }
    }

    @Override
    public void ok(int type, Object obj) {

    }

    @Override
    public void cancel(int type) {
        if (type == CustomDialog.DIALOG_SHARE_QRCODE) {
            Logger.e("进行qq分享");
            ShareSDK.initSDK(mcontext);
            QQ.ShareParams sp = new QQ.ShareParams();
            sp.setShareType(Platform.SHARE_TEXT);
            sp.setText("测试文本");
            sp.setTitleUrl("www.baidu.com");
//        sp.setImagePath(“/mnt/sdcard/测试分享的图片.jpg”);

//        Platform weibo = ShareSDK.getPlatform(Wechat.NAME);
//        weibo.setPlatformActionListener(paListener); // 设置分享事件回调
// 执行图文分享
            Platform qq = ShareSDK.getPlatform(WechatMoments.NAME);
            qq.share(sp);
        }
        else if (type == CustomDialog.DIALOG_DELETE_FRIEND){
            Logger.e(TAG,"取消删除，当前pisition = "+nowPosition);
        }
    }

    @Override
    public void getSuccess(int type, String content) {
        switch (type){
            case UtilContact.getFriendInfo:
                Logger.e(TAG,"获取用户联系人成功");
                List<FriendInfo> friendInfos = mDBManager.queryFriends();
                data.removeAll(data);
                data.add(userInfo);
                data.addAll(friendInfos);
                mhandle.sendEmptyMessageDelayed(refreshOK,1000);
                mhandle.sendEmptyMessage(notifyAdapter);
                HttpRequest.getInstance().refreshLoginStatus(mcontext,OchatFragment2.this);
            break;
        }
    }

    //错误码：500、401
    @Override
    public void getFail(int type) {
        Logger.e("获取信息失败");
        //swiprefresh刷新完成
        mhandle.sendEmptyMessageDelayed(refreshOK,1000);
        Message message = new Message();
        message.what = getFail;
        switch (type){
            case 500:
                message.arg1 = 500;
                mhandle.sendMessage(message);
                break;
            case 401:
                message.arg1 = 401;
                mhandle.sendMessage(message);
                break;
        }
    }

    @Override
    public void getIng(int type) {
        Logger.e("正在获取用户信息");
    }


    //添加好友：
    @Override
    public void sendSuccess(String content) {
        //发送请求成功
    }

    @Override
    public void sendFail(int type) {
        //发送失败
    }

    @Override
    public void hasAdded(){
        //已经是好友关系了
        mhandle.sendEmptyMessage(hasAdded);
    }


    @Override
    public void deleteSuccess() {
        Logger.e("OchatFragment2","deleteSuccess成功");
        FriendInfo friendInfo = (FriendInfo) data.get(nowPosition);
        mDBManager.deleteOneFriend(friendInfo.getContact_id());
        data.remove(nowPosition);
        mhandle.sendEmptyMessage(notifyAdapter);
    }

    @Override
    public void deleteFail(int type) {
        switch (type){
            case 400:
                Logger.e(TAG,"好友ID不正确");
                break;
            case 404:
                Logger.e(TAG,"找不到好友");
                break;
            case 500:
                Logger.e(TAG,"服务器内部错误");
                mhandle.sendEmptyMessage(deleteFail);
                break;
        }
    }

    @Override
    public void refreshSuccess(String callBackString) {
        Logger.e(TAG,"refreshLoginStatus success and callback is "+callBackString);
    }

    @Override
    public void refreshFail() {

    }
}
