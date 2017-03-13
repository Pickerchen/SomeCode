package com.sen5.ocup.struct;

import java.io.Serializable;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Teas implements Parcelable {

	private  String id;
	private  String date;
	
	private  String  teaName;
	private  String  desc;//描述
	private  String  imgurl;
	private  Bitmap  bmp;
	
	private  boolean  isdefaultimg;
	
/*	public static final Parcelable.Creator<Teas>  CREATOR = new Creator<Teas>() {
		
		@Override
		public Teas[] newArray(int size) {
			// TODO Auto-generated method stub
			
			return new Teas[size];
		}
		
		@Override
		public Teas createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			Teas teas = new Teas();
			teas.setImgname(source.readString());
			return teas;
		}
	};*/

	/**
	 * 茶名字
	 * @return
	 */
	public String getTeaName() {
		return teaName;
	}

	public void setTeaName(String name) {
		this.teaName = name;
	}
    /**
     * 茶的简介
     * @return
     */
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
    /**
     * 对应茶图片的url
     * @return
     */
	public String getImgurl() {
		return imgurl;
	}

	public void setImgurl(String imgurl) {
		this.imgurl = imgurl;
	}
/**
 * 图片
 * @return
 */
	public Bitmap getBmp() {
		return bmp;
	}

	public void setBmp(Bitmap bmp) {
		this.bmp = bmp;
	}
    /**
     * 是否默认从网络下载
     * @return
     */
	public boolean isIsdefaultimg() {
		return isdefaultimg;
	}

	public void setIsdefaultimg(boolean isdefaultimg) {
		this.isdefaultimg = isdefaultimg;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeParcelable(bmp, flags);
	}

}
