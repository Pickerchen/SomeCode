package com.sen5.ocup.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sen5.ocup.R;
import com.sen5.ocup.callback.CustomInterface.IDialog;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.receiver.HomeWatcher;
import com.sen5.ocup.util.Tools;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 *          类说明 : 关于ocup 界面
 */
public class AboutOcupActivity extends BaseActivity implements OnClickListener, IDialog {

	private HomeWatcher mHomeKeyReceiver = null;

	private LinearLayout mLayout_main;
	private RelativeLayout home_page;
	private LinearLayout back;
	private TextView package_version, company_adddr;

	private CustomDialog mDialog_version;
	private final static int count = 4;
	private int count_clickLayout;// 计数点击页面，点击4次就显示测试版本号及固件版本号
	private long time1 = System.currentTimeMillis();
	private long time2 = System.currentTimeMillis();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_ocup);
		initview();
	}

	/**
	 * 初始化控件
	 */
	public void initview() {
		FrameLayout titleLayout = (FrameLayout) findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(titleLayout,this);
		mHomeKeyReceiver = new HomeWatcher(this);

		mLayout_main = (LinearLayout) this.findViewById(R.id.layout_main);
		home_page = (RelativeLayout) this.findViewById(R.id.home_page);
		back = (LinearLayout) this.findViewById(R.id.layout_back);
		package_version = (TextView) this.findViewById(R.id.version_code_text);
		company_adddr = (TextView) this.findViewById(R.id.addr);

		mDialog_version = new CustomDialog(this, this, R.style.custom_dialog, CustomDialog.VERSION_DIALOG, null);

		package_version.setText(Tools.getVersion(AboutOcupActivity.this));

		back.setOnClickListener(this);
		home_page.setOnClickListener(this);
		mLayout_main.setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// 启动home键监听
		mHomeKeyReceiver.startWatch();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// 停止home键监听
		mHomeKeyReceiver.stopWatch();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 点击公司主页，打开浏览器
		case R.id.home_page:
			Uri sUrl = Uri.parse("http://" + company_adddr.getText() + "/");
			Intent it = new Intent(Intent.ACTION_VIEW, sUrl);
			startActivity(it); // 启动浏览器
			break;
		// 点击返回图标
		case R.id.layout_back:
			AboutOcupActivity.this.finish();
			break;
		// 点击空白处4下，弹出测试版本信息
		case R.id.layout_main:
			time2 = System.currentTimeMillis();
			if ((time2 - time1) < 2000) {
				count_clickLayout++;
			} else {
				count_clickLayout = 1;
			}
			time1 = time2;
			if (count_clickLayout >= count) {
				count_clickLayout = 0;
				// 显示内侧版本号
				mDialog_version.show();
			}
			break;
		}
	}

	@Override
	public void ok(int type) {
	}

	@Override
	public void ok(int type, Object obj) {
	}

	@Override
	public void cancel(int type) {
	}
}
