package com.sen5.ocup.callback;



public class BluetoothCallback {
	
	public interface RecieveDataFromBlueCallback {
		public void recieveDataFromBlue_ok(String msg);
		public void recieveDataFromBlue_no();
	}	
	public interface ISetCupParaCallback {
		public void setCupPara_OK(int type);
		public void setCupPara_NO(int type);
	}
	public interface IGetCupParaCallback {
		public void getCupPara_OK();
		public void getCupPara_NO();
	}
	
	public interface IGetCupStatusCallback {
		public void getCupStatus_OK();
		public void getCupStatus_NO();
	}
	
	public interface IGetDrinkDataCallback {
		public void getDrinkData_OK();
		public void getDrinkData_NO();
	}
	
	public interface ISetRemindDataCallback {
		public void setRemindData_OK();
		public void setRemindData_NO();
	}
	
	public interface IGetRemindDataCallback {
		public void getRemindData_OK();
		public void getRemindData_NO();
	}
	
	public interface ISetTeaPercentCallback {
		public void setTeaPercent_OK();
		public void setTeaPercent_NO();
	}
	
	public interface IControlCupCallback {
		public void controlCup_OK(String controlAlp);
		public void controlCup_NO(String controlAlp);
	}
	
	//发送联机密码成功如否
	public interface SendOnlineCipherCallback{
		public void onlineCipher_OK();
		public void onlineCipher_NO();
		public void onlineCipher_updateSucceed();
	}
	//数据包是否发送成功
    public interface PakageCallback{
    	public void  pakage_ACK();
    	public void  Pakage_NO();
    	public void  interrupt_UPGRADE();
    }
}
