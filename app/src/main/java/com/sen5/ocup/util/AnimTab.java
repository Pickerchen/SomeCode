package com.sen5.ocup.util;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 实现tab的动画
 */
public class AnimTab {

	private TranslateAnimation animation;
	/**
	 * 一个tab的宽度
	 */
	private int tabWidth;
	/**
	 * 第一个tab的纵坐标
	 */
	private int firstTabY;
	/**
	 * 动画绑定的view
	 */
	private View view;
	private IAnimation iAnimation;
	
	public AnimTab(int firstTabY,int tabWidth,View view,IAnimation iAnimation) {
		super();
		this.tabWidth = tabWidth;
		this.firstTabY = firstTabY;
		this.view = view;
		this.iAnimation = iAnimation;
		animation = new TranslateAnimation(0, firstTabY,0,firstTabY);
//		view.setAnimation(animation);
	}

	public void startAnim(int fromX,int toX ){
		Log.d("MainActivity", "startAnim()---------fromX=="+fromX+"toX=="+toX+"  tabWidth=="+tabWidth);
		animation = new TranslateAnimation(fromX*tabWidth, toX*tabWidth,0,0);
		Log.d("MainActivity", "startAnim()---------fromX=="+fromX*tabWidth+"toX=="+toX*tabWidth+"      firstTabY=="+firstTabY);
		animation.setDuration(100);
		animation.setAnimationListener(mAnimationListener);
		view.startAnimation(animation);
		animation.setFillAfter(true);//动画结束后保留最后一帧
	}
	/**
	 * 监听动画的状态
	 */
	private AnimationListener mAnimationListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {
			
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			iAnimation.onAnimationEnd();
		}
	};
	/**
	 * 接口类，使用动画的类在动画结束时 实现处理
	 * @author caoxia
	 *
	 */
	public interface IAnimation {
		public void onAnimationEnd();
	}
}
