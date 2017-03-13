package com.sen5.ocup.zxing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.activity.MainActivity;
import com.sen5.ocup.activity.OcupApplication;
import com.sen5.ocup.callback.CustomInterface.IDialog;
import com.sen5.ocup.gui.CustomDialog;
import com.sen5.ocup.gui.MTextView;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.struct.RequestHost;
import com.sen5.ocup.util.Tools;
import com.sen5.ocup.zxing.camera.CameraManager;
import com.sen5.ocup.zxing.decoding.CaptureActivityHandler;
import com.sen5.ocup.zxing.decoding.InactivityTimer;
import com.sen5.ocup.zxing.image.RGBLuminanceSource;
import com.sen5.ocup.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class CaptureActivity extends Activity implements Callback, android.os.Handler.Callback,IDialog {
	private static final String TAG = "CaptureActivity";
	/**
	 * 表示退出 模式为 ：扫描二维码未成功或者手动退出
	 */
	private final String BACK_MODE_NODO = "cancle";
	private static  int QR_WIDTH = 600;
	private static  int QR_HEIGHT = 600;
	private static final int REQUEST_CODE = 100;
	private static final int PARSE_BARCODE_SUC = 300;
	private static final int PARSE_BARCODE_FAIL = 303;
	private static final long VIBRATE_DURATION = 200L;
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private Vector<BarcodeFormat> decodeFormats;
	private InactivityTimer inactivityTimer;
	private boolean hasSurface;
	private boolean vibrate;
	private String characterSet;
	private ImageView iv_qr;
	private CustomDialog mPairDIalog;

	private String otherCupid;
	private MTextView mScanTips;
	private LinearLayout mLayout_back;
	private ImageButton mButton_function;
	private ProgressDialog mProgress;
	private String photo_path;
	private Bitmap scanBitmap;
	private Handler mHandler;
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zxing);
		mHandler = new Handler(this);

		FrameLayout titleLayout = (FrameLayout) findViewById(R.id.durian_head_layout);
		Tools.setImmerseLayout(titleLayout,this);

		String cupid = getIntent().getStringExtra("cupid");
		Log.d(TAG, "CaptureActivity)----onCreate)---cupid==" + cupid);
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		mLayout_back = (LinearLayout) findViewById(R.id.layout_back);
		iv_qr = (ImageView) findViewById(R.id.iv_qr);
		
		mScanTips = (MTextView) findViewById(R.id.tv_scantips);
		mScanTips.setMText(getString(R.string.scanqr_tips));
		mScanTips.setGravity(Gravity.CENTER);

		Bitmap bmp = createQRImage(RequestHost.appDownUrl+cupid);
		if (null == bmp) {

		} else {
			iv_qr.setImageBitmap(bmp);
		}

		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		mLayout_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				backPreActivity(BACK_MODE_NODO);
			}
		});
		initView();
	}

	private void initView() {
		mButton_function = (ImageButton)findViewById(R.id.button_function);
		mButton_function.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*打开手机中的相册*/
				Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); //"android.intent.action.GET_CONTENT"
		        innerIntent.setType("image/*");
		        Intent wrapperIntent = Intent.createChooser(innerIntent, getString(R.string.qrcode_choose));
		        startActivityForResult(wrapperIntent, REQUEST_CODE);	
			}
		});
		
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;
		vibrate = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			Camera camera = CameraManager.get().openDriver(surfaceHolder);
			camera.setPreviewDisplay(surfaceHolder);
			viewfinderView.canDraw = true;
			CameraManager.get().setCameraPara();
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
			}
		} catch (IOException ioe) {
			Log.d(TAG, "initCamera-------IOException------ioe==" + ioe);
			OcupToast.makeText(CaptureActivity.this, getString(R.string.not_surport_camera), Toast.LENGTH_LONG).show();
			backPreActivity(BACK_MODE_NODO);
			return;
		} catch (RuntimeException e) {
			Log.d(TAG, "initCamera-------RuntimeException------e==" + e);
			OcupToast.makeText(CaptureActivity.this, getString(R.string.not_surport_camera), Toast.LENGTH_LONG).show();
			backPreActivity(BACK_MODE_NODO);
			return;
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	public void handleDecode(final Result obj, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		otherCupid = obj.getText();
		Logger.e(TAG,"parseQrcode result = "+otherCupid);
//			otherCupid =
		//判断出是否扫描的是本应用内的二维码
		if (otherCupid.length() > 6){
			otherCupid = otherCupid.substring(otherCupid.indexOf("#")+8,otherCupid.length());
			if (Tools.isNumeric(otherCupid)){
				mPairDIalog = new CustomDialog(CaptureActivity.this, CaptureActivity.this, R.style.custom_dialog, CustomDialog.PAIR_DIALOG, 0);
				mPairDIalog.show();
			}
			else {
				CustomDialog dialog_mScanCorrectQR = new CustomDialog(CaptureActivity.this,CaptureActivity.this,R.style.custom_dialog,CustomDialog.DIALOG_SCAN_CORRECTQR,0);
				dialog_mScanCorrectQR.show();
			}
		}
		else {
			if (Tools.isNumeric(otherCupid)) {
				mPairDIalog = new CustomDialog(CaptureActivity.this, CaptureActivity.this, R.style.custom_dialog, CustomDialog.PAIR_DIALOG, 0);
				mPairDIalog.show();
			}
			else {
				CustomDialog dialog_mScanCorrectQR = new CustomDialog(CaptureActivity.this,CaptureActivity.this,R.style.custom_dialog,CustomDialog.DIALOG_SCAN_CORRECTQR,0);
				dialog_mScanCorrectQR.show();
			}
		}
	}

	private void playBeepSoundAndVibrate() {
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * 
	 * @param url
	 *            要转换的地址或字符串,可以是中文
	 */
	public static Bitmap createQRImage(String url) {
		QR_WIDTH = (int)(MainActivity.mScreenWidth-Tools.dip2px(OcupApplication.getInstance(), 10));
		QR_HEIGHT = (int)(MainActivity.mScreenWidth-Tools.dip2px(OcupApplication.getInstance(), 10));
		Logger.e("CreateQrImage","createQRImage.url = "+url);
		try {
			// 判断URL合法性
			if (url == null || "".equals(url) || url.length() < 1) {
				return null;
			}
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			// 图像数据转换，使用了矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
			int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
			/* 下面这里按照二维码的算法，逐个生成二维码的图片，
						 两个for循环是图片横列扫描的结果*/
			for (int y = 0; y < QR_HEIGHT; y++) {
				for (int x = 0; x < QR_WIDTH; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * QR_WIDTH + x] = 0xff000000;
					} else {
						pixels[y * QR_WIDTH + x] = 0xffffffff;
					}
				}
			}
			
			// 生成二维码图片的格式，使用ARGB_8888 
			Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
			return bitmap;
			// 生成二维码图片的格式，使用ARGB_8888 
			// sweepIV.setImageBitmap(bitmap);
		} catch (WriterException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backPreActivity(BACK_MODE_NODO);

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void ok(int type) {
		backPreActivity(otherCupid);
	}

	@Override
	public void ok(int type, Object obj) {
		finish();
	}

	@Override
	public void cancel(int type) {
		backPreActivity(BACK_MODE_NODO);
	}
	
	private void backPreActivity(String str){
		Intent resultIntent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("result", str);
		resultIntent.putExtras(bundle);
		CaptureActivity.this.setResult(RESULT_OK, resultIntent);
		finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch(requestCode){
			case REQUEST_CODE:
				//获取选中图片的路径
				Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
				if (cursor.moveToFirst()) {
					photo_path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
				}
				cursor.close();
				
				mProgress = new ProgressDialog(CaptureActivity.this);
//				mProgress.setMessage(getString(R.string.qrcodescaning));
				mProgress.setCancelable(false);
				mProgress.show();
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						Result result = scanningImage(photo_path);
						if (result != null) {
							Message m = mHandler.obtainMessage();
							m.what = PARSE_BARCODE_SUC;
							m.obj = result.getText();
							mHandler.sendMessage(m);
						} else {
							Message m = mHandler.obtainMessage();
							m.what = PARSE_BARCODE_FAIL;
//							m.obj = getString(R.string.qrcodescan_fail);
							mHandler.sendMessage(m);
						}
					}
				}).start();
				break;
			}
		}
	}
	
	/**
	 * 扫描二维码图片的方法
	 * @param path
	 * @return
	 */
	public Result scanningImage(String path) {
		if(TextUtils.isEmpty(path)){
			return null;
		}
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //设置二维码内容的编码

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // 先获取原大小
		scanBitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // 获取新的大小
		int sampleSize = (int) (options.outHeight / (float) 200);
		if (sampleSize <= 0)
			sampleSize = 1;
		options.inSampleSize = sampleSize;
		scanBitmap = BitmapFactory.decodeFile(path, options);
		RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader reader = new QRCodeReader();
		try {
			return reader.decode(bitmap1, hints);

		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (ChecksumException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}
		return null;
	}
	

	@Override
	public boolean handleMessage(Message msg) {
		mProgress.dismiss();
		switch (msg.what) {
		case PARSE_BARCODE_SUC:
			otherCupid = (String)msg.obj;
			Logger.e(TAG,"parseQrcode result = "+otherCupid);
//			otherCupid =
			//判断出是否扫描的是本应用内的二维码
			if (otherCupid.length() > 6){
				otherCupid = otherCupid.substring(otherCupid.indexOf("#")+8,otherCupid.length());
				if (Tools.isNumeric(otherCupid)){
					mPairDIalog = new CustomDialog(CaptureActivity.this, CaptureActivity.this, R.style.custom_dialog, CustomDialog.PAIR_DIALOG, 0);
					mPairDIalog.show();
				}
				else {
					CustomDialog dialog_mScanCorrectQR = new CustomDialog(CaptureActivity.this,CaptureActivity.this,R.style.custom_dialog,CustomDialog.DIALOG_SCAN_CORRECTQR,0);
					dialog_mScanCorrectQR.show();
				}
			}
			else {
				if (Tools.isNumeric(otherCupid)) {
					mPairDIalog = new CustomDialog(CaptureActivity.this, CaptureActivity.this, R.style.custom_dialog, CustomDialog.PAIR_DIALOG, 0);
					mPairDIalog.show();
				}
				else {
					CustomDialog dialog_mScanCorrectQR = new CustomDialog(CaptureActivity.this,CaptureActivity.this,R.style.custom_dialog,CustomDialog.DIALOG_SCAN_CORRECTQR,0);
					dialog_mScanCorrectQR.show();
				}
			}
			break;
		case PARSE_BARCODE_FAIL:
			Toast.makeText(CaptureActivity.this, (String)msg.obj, Toast.LENGTH_LONG).show();
			break;
		}
		return false;
	}

}