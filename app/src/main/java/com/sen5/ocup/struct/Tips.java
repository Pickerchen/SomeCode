package com.sen5.ocup.struct;

import java.io.Serializable;

import android.graphics.Bitmap;

public class Tips implements Serializable{
	/**
	 * 标题
	 */
	private String title;
	/**
	 * 简介
	 */
	private String brief;
	/**
	 * 图片
	 */
	private Bitmap bmp;
	/**
	 * 对应的模糊图片
	 */
	private Bitmap bmp_blur;
	/**
	 * 图片url
	 */
	private String imgUrl;	
	/**
	 * 图片名字
	 */
	private String imgName;
	/**
	 * 是否是默认图片（图片网络获取失败）
	 */
	private boolean isdefaultImg;
	/**
	 * 是否显示为毛玻璃
	 */
	private boolean isBlur;
	
	private String id;
	private String date;
	/**
	 * 是否收藏了 0---->没有收藏    1----------->收藏了
	 */
	private int isMarked;;
	
	
	public int getIsMarked() {
		return isMarked;
	}
	public void setIsMarked(int isMarked) {
		this.isMarked = isMarked;
	}
	public boolean isBlur() {
		return isBlur;
	}
	public void setBlur(boolean isBlur) {
		this.isBlur = isBlur;
	}
	public Bitmap getBmp_blur() {
		return bmp_blur;
	}
	public void setBmp_blur(Bitmap bmp_blur) {
		//this.bmp_blur = bmp_blur;
	}
	public boolean isIsdefaultImg() {
		return isdefaultImg;
	}
	public void setIsdefaultImg(boolean isdefaultImg) {
		this.isdefaultImg = isdefaultImg;
	}
	public String getImgName() {
		return imgName;
	}
	public void setImgName(String imgName) {
		this.imgName = imgName;
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBrief() {
		return brief;
	}
	public void setBrief(String brief) {
		this.brief = brief;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public Bitmap getBmp() {
		return bmp;
	}
	public void setBmp(Bitmap bmp) {
		//this.bmp = bmp;
	}	
}
