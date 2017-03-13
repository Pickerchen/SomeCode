package com.sen5.ocup.activity;

import com.sen5.ocup.R;
import com.sen5.ocup.blutoothstruct.CupPara;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.gui.MTextView;
import com.sen5.ocup.util.DBManager;
import com.sen5.ocup.util.Tools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 : 提示更新杯子固件对话框
 */
public class Dialog_updateActivity extends BaseActivity {

	protected static final String TAG = "Dialog_updateActivity";

	private 	Button mBtn_update;
	private 	Button mBtn_updatelater;
	private 	Button mBtn_neverRemind;

	/**
	 * 监听按钮点击事件
	 */
	private OnClickListener mOnclickLisener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.btn_update) {//立即升级
			Intent intent = new Intent(Dialog_updateActivity.this, UpdateFirmWare.class);
			intent.putExtra("updateNow", true);
			startActivity(intent);
			Dialog_updateActivity.this.finish();
			} else if (v.getId() == R.id.btn_remind) {//以后提醒
				
				if (CupPara.getInstance().getPara_verion() % 2 == 0) {// 双数版
					new DBManager(Dialog_updateActivity.this).add_RemindUpdate(OcupApplication.getInstance().mOwnCup.getCupID(), 0, Tools.even_version);
				} else {// 单数版
					new DBManager(Dialog_updateActivity.this).add_RemindUpdate(OcupApplication.getInstance().mOwnCup.getCupID(), 0, Tools.odd_version);
				}
				Dialog_updateActivity.this.finish();
				
			} else if (v.getId() == R.id.btn_neverremind) {//不再提醒
				
				if (CupPara.getInstance().getPara_verion() % 2 == 0) {// 双数版
					new DBManager(Dialog_updateActivity.this).add_RemindUpdate(OcupApplication.getInstance().mOwnCup.getCupID(), 1, Tools.even_version);
				} else {// 单数版
					new DBManager(Dialog_updateActivity.this).add_RemindUpdate(OcupApplication.getInstance().mOwnCup.getCupID(), 1, Tools.odd_version);
				}
				Dialog_updateActivity.this.finish();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_updatecup);
		getWindow().setLayout(4 * MainActivity.mScreenWidth / 5, LayoutParams.WRAP_CONTENT);

		mBtn_update = (Button) this.findViewById(R.id.btn_update);
		mBtn_updatelater = (Button) this.findViewById(R.id.btn_remind);
		mBtn_neverRemind = (Button) this.findViewById(R.id.btn_neverremind);

		mBtn_update.setOnClickListener(mOnclickLisener);
		mBtn_updatelater.setOnClickListener(mOnclickLisener);
		mBtn_neverRemind.setOnClickListener(mOnclickLisener);
	}
}
