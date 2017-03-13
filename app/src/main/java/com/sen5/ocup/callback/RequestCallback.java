package com.sen5.ocup.callback;

import com.sen5.ocup.struct.Teas;
import com.sen5.ocup.struct.Tips;

import java.util.ArrayList;

public interface RequestCallback {
	
	public interface GetTipsCallback {
		public void getTips_ImagePathSuccess(String imagePath);
		public void getTips_ImagePathFailed();
		
		public void getTips_success(ArrayList<Tips> tips);
		public void getTips_failed();
		
		public void getMoreTips_success(ArrayList<Tips> tips);
		public void getMoreTips_failed();

		public void getRefreshTips_success(ArrayList<Tips> tips);
		public void getRefreshTips_failed();
		
		public void getTipsImg_success();
		public void getTipsImg_failed();
		
		public void getTipsing();

	}
	public interface GetWatherCallback {
		public void getWather_success(String temp);
		public void getWather_failed();
	}
	
	public interface RegisterBaiduInfo {
		public void registerBaiduInfo_OK();
		public void registerBaiduInfo_NO();
	}
	
	public interface GetCupInfoCallback {
		public void GetCupInfo_OK(String cupid);
		public void GetCupInfo_notLogin();
		public void GetCupInfo_NO();
		public void GetCupInfoing();
	}
	
	public interface UpdateUserInfoCallback {
		public void updateCupInfo_OK();
		public void updateCupInfo_NO();
		
	}

	public interface UploadUserImageCallback {
		public void uploadUserImage_OK();
		public void uploadUserImage_NO();
		
	}
	public interface GetRelationshipCallback {
		public void getRelationship_OK(String mate_id);
		public void getRelationship_OK_noFriends();
		public void getRelationship_notLogin();
		public void getRelationship_NO();
	}
	
	public interface mateCupCallback {
		public void mateCup_OK(String mate_cupid);
		public void mateCup_NO(int mode);
		public void mateCup_NO_net();
	}
	
	public interface DemateCupCallback {
		public void demateCup_OK();
		public void demateCup_NO();
	}
	
	public interface CheckLoginCallback {
		public void checkLogin_OK(String cupid);
		public void checkLogin_NO();
	}
	
	public interface ExitLogin2APPSrvCallback {
		public void exitLogin2APPSrv_OK();
		public void exitLogin2APPSrv_NO();
	}
	
	public interface Login2APPSrvCallback {
		public void Login2APPSrv_OK();
		public void Login2APPSrv_NO();
	}
	
	public interface SendMsgCallback {
		public void sendMsg_ok(String msg,int position, boolean isRepeat);
		public void sendMsg_no(String msg,int position,boolean isRepeat,String errorString);
	}
	
	public interface ConnectBluetoothCallback {
		public void connectBluetooth_ok(String bluetoothAddr);
		public void connectBluetooth_no();
	}
	
	public interface GetTeaCallback {
		public void getTea_success(ArrayList<Teas> teas,int startIndex);
		public void getTea_failed();

	}
	
	public interface GetMateDrink {
		public void getMateDrink_success();
		public void getMateDrink_failed();

	}
	
	public interface GetDrinkCallback {
		public void getdrinkr_success();
		public void getdrinkr_failed();
	}
	
	public interface GetAPKVersionCallback {
		public void getApkversion_success();
		public void getApkversion_failed();
	}
	
	public interface IRecoveryFactoryCallback{
		public void recoveryFactory_Success();
		public void recoveryFactory_Failed(int status);
	}

	//yili
	public interface ILoginCallBack{
		//成功则
		public void loginSuccess();
		//失败类型
		public void loginFail(int type);

		public void RegisterSuccess();
		public void RegisterFail(int type);
	}

	//获取信息请求的回调：用户信息、用户联系人信息、阿里云头像上传信息
	public interface IGetInfoCallBack{
		/**
		 * 请求数据成功回调
		 * @param type：请求类型：请求好友列表，请求用户信息
		 * @param content：返回的json内容
         */
		public void getSuccess(int type,String content);

		/**
		 * 请求数据失败回调
		 * @param type：请求类型
         */
		public void getFail(int type);

		/**
		 * 正在请求信息
		 * @param type：请求类型
         */
		public void getIng(int type);
	}

	public interface IUpdateInfoCallBack{
		public void updateSuccess();
		public void updateFail(int type);
	}

	public interface IAddFriendCallBack{
		public void sendSuccess(String token);
		public void sendFail(int type);
		//已经是好友关系了
		public void hasAdded();
	}

	public interface IAddFriendConfirmCallBack{
		public void confirmSuccess();
		//403,404,500:具体请查看api文档
		public void confirmFail(int type);
	}

	//刪除好友囘調
	public interface IDeleteFriendCallBack{
		public void deleteSuccess();
		public void deleteFail(int type);
	}

	//上傳头像到阿里云回调
	public interface IUploadAvatarCallBack{
		public void uploadSuccess();
		public void uploadFail();
		//更新UI
		public void uploadProgress(int progress);
	}

	//获取伊利tip回调
	public interface IGetTipsCallBack{
		public void getTipSuccess(String content);
		public void getTipFail();
	}

	//获取yiliapk升级信息
	public interface IGetUpdateInfo{
		public void getUpdateInfoSuccess(String downLoadPhth,String versionCode,String detail,String versionName);
		public void getUpdateInfoFail();
	}
	//下载apk文件
	public interface IDownLoadAPk{
		public void downLoadSuccess();
		public void downLoadProgress(int progress);
		public void downLoadFail();
	}
	//更新用户登录状态
	public interface IRefreshLoginStatus{
		public void refreshSuccess(String callBackString);
		public void refreshFail();
	}

}
