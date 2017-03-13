package com.sen5.ocup.callback;

import android.graphics.Canvas;

import com.sen5.ocup.gui.Circle_ProgressBar;
import com.sen5.ocup.gui.ScrowlView;

public class CustomInterface {
	public interface ISendPoint {
		public void sendPoint();
	}
	
	public interface IDrawProgress {
		public void drawProgress(int pb, Circle_ProgressBar viewpb,Canvas canvas);
	}
	
	public interface IDrawScrawl {
		public void reset();
	}
	
	public interface IDrawPoint {
		public void setPoints(ScrowlView view, String points);
	}
	
	public interface IReceiveChat {
		public void updateUI(String cupId,String toCupId, String content, String time,int type);
	}
	
	public interface IDialog {
		public void ok(int type);
		public void ok(int type,Object obj);
		public void cancel(int type);
	}
}
