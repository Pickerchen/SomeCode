package com.sen5.ocup.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sen5.ocup.R;
import com.sen5.ocup.receiver.HomeWatcher;
import com.sen5.ocup.struct.RequestHost;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.util.UtilContact;
import com.sen5.ocup.zxing.CaptureActivity;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 二维码界面
 */
public class TwocodeActivity extends BaseActivity {

	private static final String TAG = "TwocodeActivity";
	private HomeWatcher mHomeKeyReceiver = null;
	private LinearLayout layout_back;
	private ImageView iv_twocode;
	private TextView mTV_qrtips;
	private TextView mTv_cupid;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twocode);
		initView();
		initData();
	}
	
	private void initView() {
		FrameLayout titleLayout = (FrameLayout) findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(titleLayout,this);
		layout_back = (LinearLayout) findViewById(R.id.layout_back);
		iv_twocode = (ImageView) findViewById(R.id.iv_twocode);
		mTV_qrtips = (TextView) findViewById(R.id.tv_qrtips);
		mTv_cupid = (TextView)findViewById(R.id.tv_cupid);
	}
	
	private void initData() {
		mHomeKeyReceiver = new HomeWatcher(this);
		Log.d(TAG, "initData  OwnerCupInfo.getInstance().getCupID()=="+OcupApplication.getInstance().mOwnCup.getCupID());
		mTV_qrtips.setText(getString(R.string.qr_tips));
		mTv_cupid.setText(Tools.getPreference(this, UtilContact.HuanXinId));
		String content = RequestHost.appDownUrl+Tools.getPreference(this, UtilContact.HuanXinId);
		iv_twocode.setImageBitmap(CaptureActivity.createQRImage(content));
		layout_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TwocodeActivity.this.finish();
			}
		});
	}
	@Override
	protected void onStart() {
		super.onStart();
		mHomeKeyReceiver.startWatch();
	}
	@Override
	protected void onStop() {
		super.onStop();
		mHomeKeyReceiver.stopWatch();
	}
}
