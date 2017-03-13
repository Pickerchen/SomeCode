package com.sen5.ocup.struct;

public class RequestHost {
	public static final String WATHER_HOST = "http://tranquil-coast-6338.herokuapp.com";
	//message.asdfplus.net:4848
//	public static final String TIPS_HOST = "http://192.168.0.8:3001/news";
//	public static final String LOGIN_HOST = "http://192.168.0.8:3001";
	//app:api.otelligent.com
	public static final String TIPS_HOST = "http://api.otelligent.com/news";
	public static final String LOGIN_HOST = "http://api.otelligent.com";

	public static final String TEAS_HOST = "http://api.otelligent.com/tea";
	//服务器上存放的APK版本更新信息，包括版本信息、更新内容、新版本更新地址
	public static final String APK_HOST = "http://dl.otelligent.com/update/cn";

	//yili
	public static String registerUrl = "https://yiliapp.otelligent.com/users/signup";
//	public static String login = "https://yiliapp.otelligent.com/users/login";
	public static String login = "https://yiliapp.otelligent.com/users/phonelogin";
	public static String loginout = "http://192.168.0.88:3000/users/logout";
	public static String friendList = "https://yiliapp.otelligent.com/contacts/list";
	public static String connectedCup = "https://yiliapp.otelligent.com/cup/connect";//连接上水杯，进行上报



	//post时作为更新用户信息用处带有两个参数：两者必须要一个nickname、mood
	//get时作为获取用户信息作用
	public static String updateInfo = "https://yiliapp.otelligent.com/users/info";//获取信息和修改信息url一致，前者get，后者post
	public static String avatar = "https://yiliapp.otelligent.com/users/avatortoken";
	public static String addFriend = "https://yiliapp.otelligent.com/contacts/addreq";
	public static String addFriendConfirm = "https://yiliapp.otelligent.com/contacts/addconfirm";
	public static String deleteFriend = "https://yiliapp.otelligent.com/contacts/del";
	public static String checkPhone = "https://yiliapp.otelligent.com/contacts/checkphone";
	public static String getTips = "http://img.otelligent.com/customer/yili/tips/";//拼接zh-cn或者en
	public static String shopUrl_JD = "https://yiliapp.otelligent.com/shop?addr=jd";
	public static String shopUrl_TM = "https://yiliapp.otelligent.com/shop?addr=tmall";
	public static String refreshUrl = "https://yiliapp.otelligent.com/users/refresh";
	//应用宝app下载地址:这串url是用作生成二维码的，我们的应用扫描出来的是添加好友，微信扫面出来的进行下载
	public static String appDownUrl ="http://a.app.qq.com/o/simple.jsp?pkgname=com.sen5.nhh.ocup#userID=";
	public static String apkUpdate = "https://yiliapp.otelligent.com/misc/androidupdate/1";
}
