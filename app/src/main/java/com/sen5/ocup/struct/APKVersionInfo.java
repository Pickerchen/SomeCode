package com.sen5.ocup.struct;

/**
 * 服务端apk版本信息
 *
 */
public class APKVersionInfo {
	
	public static APKVersionInfo mInstance = null;
	
	private String srv_version= null;
	private int srv_versionCode ;
	private String srv_versionInfo= null;
	private String srv_apkPath= null;
	
	public static APKVersionInfo getInstance() {
		if (mInstance == null) {
			mInstance = new APKVersionInfo();
		}
		return mInstance;
	}

	private APKVersionInfo() {
	}

	public int getSrv_versionCode() {
		return srv_versionCode;
	}

	public void setSrv_versionCode(int srv_versionCode) {
		this.srv_versionCode = srv_versionCode;
	}

	public String getSrv_version() {
		return srv_version;
	}

	public void setSrv_version(String srv_version) {
		this.srv_version = srv_version;
	}

	public String getSrv_versionInfo() {
		return srv_versionInfo;
	}

	public void setSrv_versionInfo(String srv_versionInfo) {
		this.srv_versionInfo = srv_versionInfo;
	}

	public String getSrv_apkPath() {
		return srv_apkPath;
	}

	public void setSrv_apkPath(String srv_apkPath) {
		this.srv_apkPath = srv_apkPath;
	}
	
	
}
