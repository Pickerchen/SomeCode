package com.sen5.ocup.yili;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.activity.BaseActivity;
import com.sen5.ocup.contacts.CharacterParser;
import com.sen5.ocup.contacts.ChineseToEnglish;
import com.sen5.ocup.contacts.ContactsRecycleViewAdapter;
import com.sen5.ocup.contacts.PinyinComparator;
import com.sen5.ocup.contacts.SortModel;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.Tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by chenqianghua on 2016/10/24.
 */
public class ContactsActivity extends BaseActivity implements View.OnClickListener,ContactsRecycleViewAdapter.IItemClickListen{

    private String TAG = ContactsActivity.class.getSimpleName();
    private RecyclerView sortListView;
    private FrameLayout mFrameLayout;
    private ImageView iv_search;
    private RelativeLayout title_layout_2;
    private ImageView iv_back;
    private EditText et_search;
    private ContactsRecycleViewAdapter adapter;
    private DBManager mdbManager;
    //已添加好友的数据
    private List<FriendInfo> friendInfos;
    //已经添加的好友且在通讯录中，需要显示出来的
    private List<FriendInfo> friendInfos_show = new ArrayList<>();
    private List<SortModel> frinedList = new ArrayList<>();
    private int[] indexs;
    //已添加好友的电话号码
    private List<String> friends = new ArrayList<>();
    //已经是好友的，但是不在通讯录中
    private List<String> contacts = new ArrayList<>();

    //测量view的值
    private int title_width;
    private int iv_search_width;

    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    private List<SortModel> SourceDateList;
    private List<Object> datas = new ArrayList<>();

    private PinyinComparator pinyinComparator;
    /** 库 phone表字段 **/
    private static final String[] PHONES_PROJECTION = new String[] {
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.Contacts.Photo.PHOTO_ID, ContactsContract.CommonDataKinds.Phone.CONTACT_ID };
    /** 联系人显示名称 **/
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;
    /** 电话号码 **/
    private static final int PHONES_NUMBER_INDEX = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        initData();
        initView();
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

    private void initView(){
        iv_back = (ImageView)findViewById(R.id.iv_back);
        iv_search = (ImageView) findViewById(R.id.iv_search);
        title_layout_2 = (RelativeLayout) findViewById(R.id.title_layout_2);
        title_layout_2.post(new Runnable() {
            @Override
            public void run() {
//                title_layout_2.getWidth();
                title_width = title_layout_2.getWidth();
                iv_search_width = iv_search.getWidth();
                Logger.e(TAG,"Title_layout_2 width ="+title_layout_2.getWidth());
                Logger.e(TAG,"et_search = "+iv_search.getWidth());
            }
        });
        iv_back.setOnClickListener(this);
        title_layout_2.setOnClickListener(this);
        sortListView = (RecyclerView)findViewById(R.id.recyclerview);
        mFrameLayout = (FrameLayout) findViewById(R.id.durian_head_layout);
        Tools.setImmerseLayout(mFrameLayout,this);

        adapter = new ContactsRecycleViewAdapter(this,datas);
        adapter.setOnItemClickListen(ContactsActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayout.VERTICAL);
        sortListView.setLayoutManager(linearLayoutManager);
        sortListView.setAdapter(adapter);
        adapter.setOnItemClickListen(ContactsActivity.this);
        adapter.setFriendSize(friendInfos_show);
        initEditText();
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
                Logger.e("TextChanged","afterTextChanged has be executed"+"and et_search.getText.toString is"+et_search.getText().toString());
                if (et_search.getText().toString().length() == 11){
                    filterFrined(et_search.getText().toString());
                }
            }
        });
    }

    private void filterFrined(String currentFilterData) {
        List<FriendInfo> friendInfos = mdbManager.queryFriends();
        for (FriendInfo friendInfo:friendInfos){
            Logger.e("TextChanged",friendInfo.getPhoneNum());
            if (friendInfo.getPhoneNum().equals(currentFilterData)){
                datas.clear();
                datas.add(friendInfo);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void filterData(String filterStr){
        List<SortModel> filterDateList = new ArrayList<SortModel>();
        Logger.e(TAG,"filterStr = "+filterStr);
            if (TextUtils.isEmpty(filterStr)) {
//            filterDateList = SourceDateList;
            } else {
                filterDateList.clear();
                try {
                    for (SortModel sortModel : SourceDateList) {
                        String name = sortModel.getName();
                        String phoneNum = sortModel.getMobile();
                        if (phoneNum.indexOf(filterStr.toString()) != -1 || name.indexOf(filterStr.toString()) != -1) {//|| characterParser.getSelling(name).startsWith(filterStr.toString()))
                            filterDateList.add(sortModel);
                        }
                    }
                } catch (Exception e) {
                    Logger.e(TAG, e.getMessage());
                }
                if (filterDateList.size() == 1 && friends.contains(filterDateList.get(0).getMobile())){
                    filterFrined(filterDateList.get(0).getMobile());
                }
                else {
                    Collections.sort(filterDateList, pinyinComparator);
                    datas.clear();
                    datas.addAll(filterDateList);
                    adapter.notifyDataSetChanged();
                }
            }
    }


    private void initData(){
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();
        SourceDateList = loadPhoneContactData();
        // 根据a-z进行排序源数据
        Collections.sort(SourceDateList, pinyinComparator);
        datas.addAll(SourceDateList);
        mdbManager = new DBManager(this);

        friendInfos = mdbManager.queryFriends();
        Logger.e(TAG,"friendInfos.size = "+friendInfos.size());
        for (FriendInfo info:friendInfos) {
            friends.add(info.getPhoneNum());
        }
            if (friends != null) {
                for (int i = 0; i < SourceDateList.size(); i++) {
                    for (int j=0;j<friendInfos.size();j++){
                        if(friendInfos.get(j).getPhoneNum().equals(SourceDateList.get(i).getMobile())){
                            frinedList.add(SourceDateList.get(i));
                            friendInfos.get(j).setNickname(SourceDateList.get(i).getName());
                            contacts.add(friendInfos.get(j).getPhoneNum());
                        }
                    }
                }
            }
        for (int j=0;j<friendInfos.size();j++){
            if (contacts.contains(friendInfos.get(j).getPhoneNum())){
                friendInfos_show.add(friendInfos.get(j));
            }
        }

        if (frinedList != null){
            datas.removeAll(frinedList);
        }
        if (friendInfos_show != null){
            datas.addAll(0,friendInfos_show);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finish();
            break;
            case R.id.title_layout_2:
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
                break;
        }
    }

    private Intent mIntent;
    @Override
    public void onItemClick(int position) {
        datas.get(position);
        if (datas.get(position) instanceof FriendInfo){
            //已经是好友关系
            mIntent = new Intent(this,ContactsInfoActivity.class);
            FriendInfo info = (FriendInfo)datas.get(position);
            mIntent.putExtra("isFriend",true);
            mIntent.putExtra("name",info.getNickname());
            mIntent.putExtra("phoneNum",info.getPhoneNum());
            mIntent.putExtra("avator",info.getAvator());
            Logger.e(TAG,"onItemClick"+info.getAvator());
        }
        else {
            mIntent = new Intent(this,ContactsInfoActivity.class);
            SortModel sortModel = (SortModel)datas.get(position);
            mIntent.putExtra("isFriend",false);
            mIntent.putExtra("name",sortModel.getName());
            mIntent.putExtra("phoneNum",sortModel.getMobile());
        }
        startActivity(mIntent);
    }
}
