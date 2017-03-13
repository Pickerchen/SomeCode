package com.sen5.ocup.yili;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenqianghua on 2016/10/25.
 */
public class UserInfo {

    /**
     * userID : 41
     * nickname : 测试
     * password : a53542c3951d722b3d09589ac1e5ef52
     * mood : 心情
     * email : asdf@asdf.com
     * avator : http://hdimg.otelligent.com/avator_customer/yili/1477396070667.png
     * group_id : 255350008658788776
     * zone : 86
     * phone : 18520009493
     * avatorThumbnail : http://hdimg.otelligent.com/avator_customer/yili/1477396070667.png@1e_100w_100h_1c_0i_1o_1x.png
     */

    @SerializedName("userID")
    private int userID;
    @SerializedName("nickname")
    private String nickname;
    @SerializedName("password")
    private String password;
    @SerializedName("mood")
    private String mood;
    @SerializedName("email")
    private String email;
    @SerializedName("avator")
    private String avator;
    @SerializedName("group_id")
    private String group_id;
    @SerializedName("zone")
    private String zone;
    @SerializedName("phone")
    private String phone;
    @SerializedName("avatorThumbnail")
    private String avatorThumbnail;

    public static UserInfo objectFromData(String str) {

        return new com.google.gson.Gson().fromJson(str, UserInfo.class);
    }

    public static List<UserInfo> arrayUserInfoFromData(String str) {

        Type listType = new com.google.gson.reflect.TypeToken<ArrayList<UserInfo>>() {
        }.getType();

        return new com.google.gson.Gson().fromJson(str, listType);
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvator() {
        return avator;
    }

    public void setAvator(String avator) {
        this.avator = avator;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatorThumbnail() {
        return avatorThumbnail;
    }

    public void setAvatorThumbnail(String avatorThumbnail) {
        this.avatorThumbnail = avatorThumbnail;
    }
}
