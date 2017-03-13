package com.sen5.ocup.util;

public class UtilContact {
	public static final String DEFAULT_OCUP_NAME = "Ofans";
	public static final String CUP_ID = "cupid";//储存到本地的cupId在获取groupId的时候使用
	public static final String BLUE_ADD = "blueAdd";
	public static final String Phone_Num = "phoneNum";
	public static final String Phone_Code = "phoneCode";
	public static final String HuanXinId = "huanxinID";
	public static final String HuanXinPWD = "huanxinPWD";
	public static final String OwnAvatar = "ownAvatar";
	public static final String Cookies = "cookies";
	public static final String ISFRIST = "isFrist";
	public static final String ISREQUESTUPDATEINFO = "true";
	public static final String OPENDATA = "opendata";//是否公开数据，公开数据，其他朋友就可以进行看到温度信息
	public static final String GROUPID = "groupID";//以该用户连接的水杯ID建立的一个群主
	public static final String isAlived = "alive";//水杯是否连接
	public static final String LASTEXITSTATUS = "lastexitstatus";//上次退出应用的状态

	public static final int getUserInfo = 1;
	public static final int getFriendInfo = 2;
	public static final int getAvatorInfo = 3;
	public static final int loginFail = 4;
	public static final int loginFail_paramError = 400;
	public static final int loginFail_unVerify = 401;
	public static final int loginFail_webError = 500;
	public static final int checkPhoneNum = 5;

	public static final int requestSuccess = 6;
	public static final int requestFail = 7;
	public static final int requestIng = 8;
	public static final int hasAddedFriend = 9;
}
