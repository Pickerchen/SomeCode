package com.sen5.ocup.yili;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.callback.RequestCallback;
import com.sen5.ocup.contacts.ChineseToEnglish;
import com.sen5.ocup.contacts.ContactsRecycleViewAdapter;
import com.sen5.ocup.contacts.PinyinComparator;
import com.sen5.ocup.contacts.SortModel;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.HttpRequest;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhoneNumSearchActivity extends Activity implements RequestCallback.IGetInfoCallBack{

    private String TAG = PhoneNumSearchActivity.class.getSimpleName();
    //view
    private EditText et_search;
    private TextView tv_sure;
    private RecyclerView recycleView;
    private ImageView iv_back;
    private ImageView iv_search;
    private RelativeLayout title_layout_2;
    private FrameLayout mFrameLayout;
    //属性动画
    private int title_width;
    private int iv_search_width;
    /** 库 phone表字段 **/
    private static final String[] PHONES_PROJECTION = new String[] {
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.Contacts.Photo.PHOTO_ID, ContactsContract.CommonDataKinds.Phone.CONTACT_ID };
    /** 联系人显示名称 **/
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;
    /** 电话号码 **/
    private static final int PHONES_NUMBER_INDEX = 1;
    private List<SortModel> SourceDateList;
    private List<Object> datas = new ArrayList<>();
    private PinyinComparator pinyinComparator;
    private ContactsRecycleViewAdapter adapter;
    private final int paramsError = 1;
    private final int webError = 2;
    private final int notFound = 3;
    private final int isGetingInfo = 4;
    private final int requestSuccess =5;
    private DBManager mDBManager;
    private List<String> friendsPhoneNum = new ArrayList<>();
    private List<FriendInfo> friends = new ArrayList<>();
    private List<FriendInfo> friensInContacts = new ArrayList<>();//已注册了暖哄哄的手机号码
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case paramsError:
                    showToast(getString(R.string.paramsWrong));
                    break;
                case notFound:
                    showToast(getString(R.string.notFound));
                    SortModel sortModel = new SortModel();
                    sortModel.setMobile(et_search.getText().toString());
                    sortModel.setName(et_search.getText().toString());
                    datas.add(sortModel);
                    adapter.notifyDataSetChanged();
                    break;
                case webError:
                    showToast(getString(R.string.webIsWrong));
                    break;
                case isGetingInfo:
                    showToast(getString(R.string.isGettingInfos));
                    break;
                case requestSuccess:
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_num_search);
        mFrameLayout = (FrameLayout) findViewById(R.id.durian_head_layout_origin);
        Tools.setImmerseLayout(mFrameLayout,this);
        pinyinComparator = new PinyinComparator();
        SourceDateList = loadPhoneContactData();
        adapter = new ContactsRecycleViewAdapter(this,datas);
        recycleView = (RecyclerView)findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayout.VERTICAL);
        recycleView.setLayoutManager(linearLayoutManager);
        recycleView.setAdapter(adapter);

        mDBManager = new DBManager(this);
         friends = mDBManager.queryFriends();
        for (FriendInfo friend : friends){
            friendsPhoneNum.add(friend.getPhoneNum());
        }
        initEditText();
        initSure();
    }

    private void initSure() {
        tv_sure = (TextView) findViewById(R.id.tv_sure);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_search = (ImageView) findViewById(R.id.iv_search);
        title_layout_2 = (RelativeLayout) findViewById(R.id.title_layout);
        title_layout_2.post(new Runnable() {
            @Override
            public void run() {
                title_width = title_layout_2.getWidth();
                iv_search_width = iv_search.getWidth();
            }
        });
        title_layout_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int move_length = title_width/2-iv_search_width-30;
                title_layout_2.setClickable(false);
                Logger.e(TAG,"move_length = "+move_length);
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(iv_search, "translationX", 0f, -move_length, -move_length);
                objectAnimator.setDuration(600);
                objectAnimator.addListener(new ObjectAnimator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        et_search.setVisibility(View.VISIBLE);
                        et_search.requestFocus();
                        InputMethodManager methodManager = (InputMethodManager) et_search.getContext().getSystemService(INPUT_METHOD_SERVICE);
                        methodManager.toggleSoftInput(0,InputMethodManager.SHOW_FORCED);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                objectAnimator.start();
            }
        });
        tv_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_search.getText().toString().equals(Tools.getPreference(PhoneNumSearchActivity.this,UtilContact.Phone_Num))){
                    showToast(getString(R.string.isYourSelf));
                }
                HttpRequest.getInstance().checkPhoneNum(et_search.getText().toString(),PhoneNumSearchActivity.this,PhoneNumSearchActivity.this);
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void initEditText() {
        et_search = (EditText) findViewById(R.id.et_search);

        //根据输入框输入值的改变来过滤搜索
        et_search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void showToast(String content) {
        Toast.makeText(this,content,Toast.LENGTH_LONG).show();
    }

    /**
     * 根据输入框中的值来过滤数据并更新ListView
     * @param filterStr
     */
    private void filterData(String filterStr){
//        if (filterStr.length() >= 10){
//            recycleView.setVisibility(View.GONE);
//        }
//        else {
//            recycleView.setVisibility(View.VISIBLE);
//        }
        List<SortModel> filterDateList = new ArrayList<SortModel>();
        Logger.e(TAG,"filterStr = "+filterStr);
        if(TextUtils.isEmpty(filterStr)){
//            filterDateList = SourceDateList;
        }else{
            filterDateList.clear();
            try{
                for(SortModel sortModel : SourceDateList){
                    String name = sortModel.getName();
                    String phoneNum = sortModel.getMobile();
                    if(phoneNum.indexOf(filterStr.toString()) != -1 || name.indexOf(filterStr.toString()) != -1){//|| characterParser.getSelling(name).startsWith(filterStr.toString()))
                        filterDateList.add(sortModel);
                    }
                }
            }
            catch (Exception e){
                Logger.e(TAG,e.getMessage());
            }
        }
        // 根据a-z进行排序
        if (filterDateList != null){
            datas.clear();
            friensInContacts.clear();
            for (SortModel sortModel : filterDateList){
                String phoneNum = sortModel.getMobile();
                if (friendsPhoneNum.contains(phoneNum)){
                    for (int i =0; i<friends.size();i++){
                        friends.get(i).getPhoneNum().equals(phoneNum);
                        SourceDateList.remove(sortModel);
                        friensInContacts.add(friends.get(i));
                        adapter.setFriendSize(friensInContacts);
                    }
                }
            }
            Collections.sort(filterDateList, pinyinComparator);
            datas.addAll(filterDateList);
            datas.addAll(0,friensInContacts);
            adapter.notifyDataSetChanged();
        }
    }


    /**
     * 加载手机联系人
     */
    private List<SortModel>  loadPhoneContactData() {
        Logger.e("正在加载联系人信息");
        List<SortModel> mSortList = new ArrayList<SortModel>();

        ContentResolver resolver = this.getContentResolver();
        Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PHONES_PROJECTION, null, null, null);

        SortModel sort = null;

        String phoneNumber = "";

        String phoneName = "";

        // Long photoID = 0l;

        if (phoneCursor != null) {
            Logger.e("phoneCursor不为空" + phoneCursor.getCount()+"个");
            while (phoneCursor.moveToNext()) {
                phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX)
                        .replace(" ", "");

                if (phoneNumber==null||phoneNumber=="")
                    continue;

                phoneName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);

                sort = new SortModel();
                sort.setMobile(phoneNumber);
                sort.setName(phoneName);

                // 汉字转换成拼音
//				String pinyin = characterParser.getSelling(phoneName);
//				String sortString = pinyin.substring(0, 1).toUpperCase();

                String pinyin = ChineseToEnglish.getPinYinHeadChar(phoneName);
                String sortString = pinyin.substring(0, 1).toUpperCase();

                sort.setSortLetters(sortString);

                // 正则表达式，判断首字母是否是英文字母
                if (sortString.matches("[A-Z]")) {
                    sort.setSortLetters(sortString.toUpperCase());
                } else {
                    sort.setSortLetters("#");
                }

                mSortList.add(sort);
            }

            phoneCursor.close();
        }
        Logger.e(mSortList.size()+"个");
        return mSortList;
    }

    @Override
    public void getSuccess(int type, String content) {
        Logger.e(TAG,"this phoneNum s id is = "+content);
        recycleView.setVisibility(View.VISIBLE);
        if (type == UtilContact.checkPhoneNum){
//            adapter.setAddFriendVisible();
            datas.clear();
            friensInContacts.clear();
            FriendInfo contact = new FriendInfo(content,null,null,null,et_search.getText().toString());
            if (!friendsPhoneNum.contains(et_search.getText().toString())){
                contact.setIsNotFriend();
                datas.add(contact);
            }
            else {
                List<FriendInfo> friendInfos = new ArrayList<>();
                for (FriendInfo friendInfo : friends){
                    if (friendInfo.getPhoneNum().equals(et_search.getText().toString())){
                        friendInfos.add(friendInfo);
                        contact.setNickname(friendInfo.getNickname());
                    }
                }
                adapter.setFriendSize(friendInfos);
                datas.addAll(friendInfos);
            }
            mHandler.sendEmptyMessage(requestSuccess);
        }
    }

    @Override
    public void getFail(int type) {
        if (type == 400){
            mHandler.sendEmptyMessage(paramsError);
        }
        else if (type == 404){
            mHandler.sendEmptyMessage(notFound);
            datas.clear();
        }
        else if (type == 500){
            mHandler.sendEmptyMessage(webError);
        }
    }

    @Override
    public void getIng(int type) {
        mHandler.sendEmptyMessage(isGetingInfo);
    }
}
