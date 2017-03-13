package com.sen5.ocup.yili;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenqianghua on 2016/11/9.
 */
public class RegisterInfo {

    /**
     * zone : 86
     * phone : 18649768543
     * userID : 146
     * nickname : 8618649768543
     * password : 684e80d94dd6b493d299af9c6c6ceccd
     */

    private String zone;
    private String phone;
    private int userID;
    private String nickname;
    private String password;

    public static RegisterInfo objectFromData(String str) {

        return new Gson().fromJson(str, RegisterInfo.class);
    }

    public static List<RegisterInfo> arrayRegisterInfoFromData(String str) {

        Type listType = new TypeToken<ArrayList<RegisterInfo>>() {
        }.getType();

        return new Gson().fromJson(str, listType);
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
}
