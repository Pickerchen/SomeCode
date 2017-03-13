package com.sen5.ocup.yili;

import com.orhanobut.logger.Logger;

/**
 * Created by chenqianghua on 2016/10/26.
 */
public class FriendInfo {
    /*
    *  {
    "contact_id": 42,
    "group_id": "255355051705369004",
    "avator": null,
	"nickname": "8610923782375"
  }
    * */
    private String contact_id;
    private String group_id;
    private String avator;
    private String nickname;
    private int unReadCount;
    private String phoneNum;
    //伊利水杯2.0版本新增的几个参数
    private boolean haveCup;
    private boolean isOnLine;
    private int openData;//0代表关闭，大于零显示温度值代表打开温度

    public  int getLastuptime() {
        return lastuptime;
    }

    public void setLastuptime(int lastuptime) {
        this.lastuptime = lastuptime;
    }

    private int lastuptime;//上次更新状态的时间

    public int getOpenData() {
        return openData;
    }

    public void setOpenData(int openData) {
        this.openData = openData;
    }

    public boolean isOnLine() {
        return isOnLine;
    }

    public void setOnLine(boolean onLine) {
        isOnLine = onLine;
    }

    public boolean isHaveCup() {
        return haveCup;
    }

    public void setHaveCup(boolean haveCup) {
        this.haveCup = haveCup;
    }

    private boolean isFriend = true;

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public long getLastMsgTime() {
        return lastMsgTime;
    }

    public void setLastMsgTime(long lastMsgTime) {
        this.lastMsgTime = lastMsgTime;
    }

    private long lastMsgTime;

    public int getUnReadCount() {
        return unReadCount;
    }

    public void setUnReadCount(int unReadCount) {
        this.unReadCount = unReadCount;
    }

    public FriendInfo(String contact_id, String group_id, String avator, String nickname) {
        this.contact_id = contact_id;
        this.group_id = group_id;
        this.avator = avator;
        this.nickname = nickname;
        this.lastMsgTime = System.currentTimeMillis();
        Logger.e("FriendInfo","lastMsgTime = "+lastMsgTime);
    }

    public FriendInfo(String contact_id, String group_id, String avator, String nickname,String phoneNum) {
        this.contact_id = contact_id;
        this.group_id = group_id;
        this.avator = avator;
        this.nickname = nickname;
        this.phoneNum = phoneNum;
        this.lastMsgTime = System.currentTimeMillis();
        Logger.e("FriendInfo","lastMsgTime = "+lastMsgTime);
    }

    public FriendInfo(String contact_id, String group_id, String avator, String nickname,long time) {
        this.contact_id = contact_id;
        this.group_id = group_id;
        this.avator = avator;
        this.nickname = nickname;
        this.lastMsgTime = time;
    }

    public FriendInfo(String contact_id, String group_id, String avator, String nickname,long time,String phoneNum) {
        this.phoneNum = phoneNum;
        this.contact_id = contact_id;
        this.group_id = group_id;
        this.avator = avator;
        this.nickname = nickname;
        this.lastMsgTime = time;
    }

    public String getContact_id() {
        return contact_id;
    }

    public void setContact_id(String contact_id) {
        this.contact_id = contact_id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getAvator() {
        return avator;
    }

    public void setAvator(String avator) {
        this.avator = avator;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setIsNotFriend(){
        isFriend = false;
    }
    public boolean getIsFriend(){
        return isFriend;
    }
}
