package com.sen5.ocup.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.sen5.ocup.R;
import com.sen5.ocup.adapter.DeviceListAdapter;
import com.sen5.ocup.gui.OcupToast;
import com.sen5.ocup.gui.WaitingBar;
import com.sen5.ocup.service.BluetoothService;
import com.sen5.ocup.struct.Device;
import com.sen5.ocup.util.BluetoothConnectUtils;
import com.sen5.ocup.util.Tools;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *          <p>
 *          类说明 : 蓝牙搜索连接界面
 */
public class BlueTooth3Activity extends BaseActivity implements Callback {

    protected static final String TAG = "BlueTooth3Activity";
    /**
     * 自动连接蓝牙的信号临界点，当信号大于它时，结束搜索，自动连接
     */
    private int mRssi_connect = -35;
    private Handler mHandler;
    private BluetoothAdapter mBtAdapter;

    private int connectMethod = 0;
    /**
     * 自动搜索
     */
    private final static int CONNECTMETHOD_AOTO = 0;
    /**
     * 手动搜索
     */
    private final static int CONNECTMETHOD_MANU = 1;

    private final static int MY_PERMISSION_REQUEST_CONSTANT = 2;


    private FrameLayout mFrameLayout;
    /**
     * 手动模式下的搜索按钮
     */
    private TextView tv_research;
    private ImageView cup, phone, iv_back;

    private ListView mListView_device;
    private DeviceListAdapter mAdapterDevice;
    private ArrayList<Device> mData_devices;

    private LinearLayout gesture_p, layout_bluestatus;
    private WaitingBar mWaitingBar;

    private ArrayList<String> mList_bluetoothAdrr = new ArrayList<String>();
    /**
     * 打开蓝牙结果
     */
    private static final int ACTION_BLUETOOTH_ENABLE = 3;
    /**
     * 当前连接的蓝牙地址
     */
    // protected String mConnectAddr;

    private LinearLayout mLayout_manualconnect;
    private RelativeLayout mLayout_autoconnect;
    /**
     * 自动模式下的开始搜索按钮
     */
    private Button mBtn_autoconnect;
    /**
     * 自动模式下的连接状态
     */
    private TextView mTV_autoconnectstate;
    /**
     * 连接模式切换按钮
     */
    private TextView mTV_startcustom;
    private EditText mTV_log;

    private int mMaxRssi;// 搜索到的最大的Rssi
    private BluetoothDevice mMaxRssiDevice;// 搜索到的最大的Rssi对应的设备

    private int mFinishDiscovery_method;// 结束搜索的方式：
    /**
     * 调试----正在配对的地址-----
     */
    // protected String addr;
    private final static int mFinishDiscovery_normal = 0;// 正常搜索结束
    private final static int mFinishDiscovery_changeConnectMethod = 1;// 切换连接方式导致结束搜索
    private final static int mFinishDiscovery_startConnect = 2;// 开始连接方式导致结束搜索

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "onReceive)----------action==" + action);
            // 发现设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 从intent中获取蓝牙设备对象
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                int deviceType = getBluetoothDeviceType(device);

                Logger.e(TAG, "onReceive)---------ACTION_FOUND==device.getName()==" + device.getName() + "  deviceType==" + deviceType);
                if (deviceType != 3 && deviceType != 1) {
                    return;
                }
                // 判断列表中是否 已有
                if (null != device && mList_bluetoothAdrr.contains(device.getAddress())) {
                    Log.d(TAG, "onReceive)---------ACTION_FOUND==already in list  ");
                } else if (null != device && null != device.getName() && device.getName().startsWith("Ocup_")) {// free
                    // 信号强度。
                    int rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                    Log.d(TAG, "onReceive)---" + "  device.getAddress()==" + device.getAddress() + "   rssi==" + rssi);
                    if (mLayout_autoconnect.getVisibility() == View.VISIBLE) {// 自动连接模式
                        if (rssi >= mRssi_connect) {// 自动连接
                            startConnect(device);
                        }

                        if (rssi > mMaxRssi) {// 保存信号最强的设备地址和信号，当搜索结束后，还没找到大于临界值的蓝牙，则连接信号最强的蓝牙
                            mMaxRssi = rssi;
                            mMaxRssiDevice = device;
                        }
                    } else {// 手动连接模式
                        mList_bluetoothAdrr.add(device.getAddress());
                        Device d = null;
                        if (device.getName().length() > 5) {
                            d = new Device(device, device.getName().substring(5), device.getAddress(), "" + rssi, false);
                        } else {
                            d = new Device(device, "", device.getAddress(), "" + rssi, false);
                        }
                        mData_devices.add(d);
                        mAdapterDevice.notifyDataSetChanged();
                        mListView_device.setSelection(mData_devices.size() - 1);
                    }
                }
                // 扫描设备结束
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "onReceive)------ACTION_DISCOVERY_FINISHED== mFinishDiscovery_method;==" + mFinishDiscovery_method);
                if (mFinishDiscovery_method == mFinishDiscovery_startConnect) {

                } else if (mFinishDiscovery_method == mFinishDiscovery_changeConnectMethod) {
                    BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_NONE);
                    changeConnectMethod();
                } else {
                    BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_NONE);
                    if (connectMethod == CONNECTMETHOD_AOTO) {
                        mFinishDiscovery_method = mFinishDiscovery_startConnect;
                        startConnect(mMaxRssiDevice);// 自动连接信号最强的设备
                        return;
                    } else {
                        searchFinish();
                    }
                }
            } else if (action.equals(BluetoothConnectUtils.ACTION_BLUETOOTHSTATE)) {
                int bluestate = intent.getIntExtra(BluetoothConnectUtils.KEY_BLUETOOTHSTATE, -1);
                // addr = intent.getStringExtra("blueadddr");
                Log.e(TAG, "bluetooth connectstate bluestate==" + bluestate);
                if (bluestate == BluetoothConnectUtils.CONNECT_OK) {
                    mHandler.sendEmptyMessage(CONNECTBLUETOOTH_OK);

                } else if (bluestate == BluetoothConnectUtils.CONNECT_NO) {
                    mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO);

                } else if (bluestate == BluetoothConnectUtils.CONNECT_WAITE) {
                    mHandler.sendEmptyMessage(CONNECTBLUETOOTH_WAIT);

                } else if (bluestate == BluetoothConnectUtils.CONNECT_ING) {
                    mHandler.sendEmptyMessage(CONNECTBLUETOOTH_ING);

                } else if (bluestate == BluetoothConnectUtils.CONNECT_PAIRING) {
                    mHandler.sendEmptyMessage(CONNECTBLUETOOTH_PARING);

                } else if (bluestate == BluetoothConnectUtils.CONNECT_UNPAIRING) {
                    mHandler.sendEmptyMessage(CONNECTBLUETOOTH_UNPAIRING);

                } else if (bluestate == BluetoothConnectUtils.CONNECT_NO_PAIR) {
                    mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO_PAIR);

                } else if (bluestate == BluetoothConnectUtils.CONNECT_NO_UNPAIR) {
                    mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO_UNPAIR);

                } else if (bluestate == BluetoothConnectUtils.CONNECT_NO_CONNECT) {
                    mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO_CONNECT);
                }
            } else if (action.equals(BluetoothService.ACTION_SOCKETCLOSE)) {
                Tools.showToast(BlueTooth3Activity.this, getString(R.string.socketClosedReminder));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OcupApplication.getInstance().addActivity(this);
        setContentView(R.layout.activity_bluetooth3);
        Log.d(TAG, "onCreate----------------------------------------");
        initView();
        initData();
        getPhoneModel();
        judgeMobileVersion();
    }

    /**
     * 获取手机型号,根据型号改mRssi_connect的值
     */
    private void getPhoneModel() {
        String phone = android.os.Build.MODEL;
        Log.d(TAG, "onCreate--------------------------------------phone--" + phone);
        if (phone.startsWith("MI")) {
            mRssi_connect = -45;
        } else if (phone.startsWith("LG-E988")) {
            mRssi_connect = -70;
        }
    }

    /**
     * 获取手机sdk版本，动态获取权限
     */
    private void judgeMobileVersion() {
        if (Build.VERSION.SDK_INT >= 6.0) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_REQUEST_CONSTANT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Logger.e(TAG, "requestCode ==" + requestCode);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CONSTANT:
                //已经获准
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case ACTION_BLUETOOTH_ENABLE:
                Log.d(TAG, "onActivityResult      resultCode==" + resultCode);
                if (resultCode != Activity.RESULT_OK) {
                    // 打开蓝牙失败
                    Log.d(TAG, "bluetooth not enabled");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int count = 0;// 检测蓝牙是否打开的次数
                            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                            while (count < 200) {
                                if (!adapter.isEnabled()) {
                                    count++;
                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    mHandler.sendEmptyMessage(BLUETOOTH_OPEN_SUCCEED);
                                    break;
                                }
                            }

                            if (!adapter.isEnabled()) {
                                // 打开蓝牙失败，退出程序
                                mHandler.sendEmptyMessage(BLUETOOTH_OPEN_FAILED);
                            } else {
                                mHandler.sendEmptyMessage(BLUETOOTH_OPEN_SUCCEED);
                            }
                        }
                    }).start();
                } else {
                    tv_research.performClick();
                }
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart----------------------------------------");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume----------------------------------------");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop----------------------------------------");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy----------------------------------------");
        // 注销蓝牙广播接收
        this.unregisterReceiver(mReceiver);
        // 确保不再搜索蓝牙
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        mFrameLayout = (FrameLayout) findViewById(R.id.durian_head_layout);
        Tools.setImmerseLayout(mFrameLayout, this);
        tv_research = (TextView) findViewById(R.id.tv_research);
        mListView_device = (ListView) findViewById(R.id.listView_device3);
        gesture_p = (LinearLayout) findViewById(R.id.cupgesture);
        cup = (ImageView) findViewById(R.id.cup);
        phone = (ImageView) findViewById(R.id.phone);
        layout_bluestatus = (LinearLayout) findViewById(R.id.layout_bluestatus);
        mWaitingBar = (WaitingBar) findViewById(R.id.waitingBar);

        mLayout_manualconnect = (LinearLayout) findViewById(R.id.layout_customsearch);
        mLayout_autoconnect = (RelativeLayout) findViewById(R.id.layout_autosearch);
        mBtn_autoconnect = (Button) findViewById(R.id.btn_autosearch);
        mTV_autoconnectstate = (TextView) findViewById(R.id.tv_autoconnectstate);
        mTV_startcustom = (TextView) findViewById(R.id.tv_startcustom);
        mTV_log = (EditText) findViewById(R.id.et_log);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mMaxRssi = mRssi_connect - 100;// 设置初始的最大信号值比可连接的信号值小100

        mHandler = new Handler(this);

		/* 取得默认的蓝牙适配器 */
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        int connectedState = mBtAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        if (connectedState == BluetoothAdapter.STATE_CONNECTED)

            if (mBtAdapter == null) {
                // 设备不支持蓝牙
                OcupToast.makeText(this, getString(R.string.not_surport_bluetooth), Toast.LENGTH_SHORT).show();
                this.finish();
            }

        mData_devices = new ArrayList<Device>();
        mAdapterDevice = new DeviceListAdapter(this, mData_devices);
        mListView_device.setAdapter(mAdapterDevice);
        tv_research.setOnClickListener(mOnClickListener);
        mBtn_autoconnect.setOnClickListener(mOnClickListener);
        mTV_startcustom.setOnClickListener(mOnClickListener);
        mListView_device.setOnItemClickListener(mOnItemClickListener);
        iv_back.setOnClickListener(mOnClickListener);

        // 注册蓝牙发现接收器
        IntentFilter discoveryFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        // 注册蓝牙搜索结束接收器
        discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        discoveryFilter.addAction("com.sen5.ocup.receiver.BluetoothConnectStateReceiver");
        //在BluetoothService中socket连接断开
        discoveryFilter.addAction(BluetoothService.ACTION_SOCKETCLOSE);
        this.registerReceiver(mReceiver, discoveryFilter);

        BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_NONE);

    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick------------BluetoothConnectUtils.getInstance().getBluetoothState()==" + BluetoothConnectUtils.getInstance().getBluetoothState());
            if (v.getId() == R.id.tv_research) {
                Log.d(TAG, "onClick-----manully search");
                // 手动模式下 ,点击搜索
                if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_NONE) {
                    // 蓝牙处于空闲状态
                    startSearch();
                } else {
                    OcupToast.makeText(BlueTooth3Activity.this, getString(R.string.requesting), Toast.LENGTH_SHORT).show();
                }
            } else if (v.getId() == R.id.btn_autosearch) {
                Log.d(TAG, "onClick----auto search");
                // 自动模式下搜索按钮的点击事件
                if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_NONE) {
                    // 蓝牙处于空闲状态
                    startSearch();
                } else {
                    OcupToast.makeText(BlueTooth3Activity.this, getString(R.string.requesting), Toast.LENGTH_SHORT).show();
                }
            } else if (v.getId() == R.id.tv_startcustom) {
                // 连接方式切换
                Log.d(TAG, "onclick  mClickConnectMethod===   connectMethod==" + connectMethod);
                if (connectMethod == CONNECTMETHOD_AOTO) {
                    // 当前是自动模式，切换到手动模式
                    if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_NONE) {
                        // 蓝牙当前空闲，直接切换
                        connectMethod = CONNECTMETHOD_MANU;
                        changeConnectMethod();
                    } else if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_DISCOVERY) {
                        // 正在搜索，先取消搜索，再切换页面
                        connectMethod = CONNECTMETHOD_MANU;
                        mFinishDiscovery_method = mFinishDiscovery_changeConnectMethod;
                        mBtAdapter.cancelDiscovery();
                    } else if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTING) {
                        // 正在连接或已连接，先断开，再切换页面
                        Log.d(TAG, "mOnClickListener------切换连接方式    当前是自动模式，切换到手动模式   closeBluetoothCommunication ");
                        BluetoothConnectUtils.getInstance().closeBluetoothCommunication();
                        connectMethod = CONNECTMETHOD_MANU;
                        changeConnectMethod();
                    } else {
                        // 正在绑定 or 解除绑定
                        Log.d(TAG, "mOnClickListener------切换连接方式     正在绑定 or 解除绑定");
                        OcupToast.makeText(BlueTooth3Activity.this, getString(R.string.requesting), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 当前是手动模式，切换到自动模式
                    if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_NONE) {
                        // 蓝牙当前空闲，直接切换
                        connectMethod = CONNECTMETHOD_AOTO;
                        changeConnectMethod();
                    } else if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_DISCOVERY) {
                        // 正在搜索，先取消搜索，再切换页面
                        connectMethod = CONNECTMETHOD_AOTO;
                        mFinishDiscovery_method = mFinishDiscovery_changeConnectMethod;
                        mBtAdapter.cancelDiscovery();
                    } else if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_CONNECTING) {
                        // 正在连接，先断开，再切换页面
                        Log.d(TAG, "mOnClickListener------切换连接方式     当前是手动模式，切换到自动模式   closeBluetoothCommunication ");
                        BluetoothConnectUtils.getInstance().closeBluetoothCommunication();
                        connectMethod = CONNECTMETHOD_AOTO;
                        changeConnectMethod();
                    } else {
                        // 正在绑定 or 解除绑定
                        Log.d(TAG, "mOnClickListener------切换连接方式     正在绑定 or 解除绑定");
                        OcupToast.makeText(BlueTooth3Activity.this, getString(R.string.requesting), Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (v.getId() == R.id.iv_back) {
                finish();
            }
        }
    };

    /**
     * 切换连接方式
     */
    private void changeConnectMethod() {
        if (connectMethod == CONNECTMETHOD_AOTO) {
            mLayout_autoconnect.setVisibility(View.VISIBLE);
            mLayout_manualconnect.setVisibility(View.INVISIBLE);
            mTV_startcustom.setText(getString(R.string.manually_connect));
            mTV_autoconnectstate.setText("");
            mBtn_autoconnect.setText(getString(R.string.start_connect));
        } else {
            mLayout_autoconnect.setVisibility(View.INVISIBLE);
            mLayout_manualconnect.setVisibility(View.VISIBLE);
            tv_research.setText(getString(R.string.search_bluedevice));
            mTV_startcustom.setText(getString(R.string.auto_connect));
        }
    }

    /**
     * 启动搜索
     */
    private void startSearch() {
        // 如果蓝牙没有打开则打开蓝牙mBtAdapter.enable()
        if (null == mBtAdapter) {
            Log.e(TAG, "startSearch   mBtAdapter == null");
            return;
        }
        if (!mBtAdapter.isEnabled()) {
            OcupToast.makeText(BlueTooth3Activity.this, getString(R.string.opening_bluetooth), Toast.LENGTH_SHORT).show();
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, ACTION_BLUETOOTH_ENABLE);
        } else {
            if (!mWaitingBar.getIsRun()) {
                mWaitingBar.setIsRun(true);
            }
            if (connectMethod == CONNECTMETHOD_AOTO) {
                mBtn_autoconnect.setText(R.string.retry_connect);
                mTV_autoconnectstate.setVisibility(View.VISIBLE);
                mTV_autoconnectstate.setText(getString(R.string.searching));
            } else {
                tv_research.setText(getString(R.string.searching));
                mAdapterDevice.notifyDataSetChanged();
            }
            mList_bluetoothAdrr.clear();
            mData_devices.clear();
            mFinishDiscovery_method = mFinishDiscovery_normal;
            /* 开始搜索 */
            int iCount = 0;
            while (iCount++ < 3) {
                boolean b = mBtAdapter.startDiscovery();
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (b) {
                    logMessage("正在搜索");
                    BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_DISCOVERY);
                    break;
                }
            }
            Log.d(TAG, "startSearch--------iCount==" + iCount);
            if (iCount > 3) {
                OcupToast.makeText(BlueTooth3Activity.this, getString(R.string.no_found_bluedevice), Toast.LENGTH_SHORT).show();

                tv_research.setText(getString(R.string.search_bluedevice));
                mBtn_autoconnect.setText(R.string.retry_connect);
                mTV_autoconnectstate.setVisibility(View.INVISIBLE);

                BluetoothConnectUtils.getInstance().setBluetoothState(BluetoothConnectUtils.BLUETOOTH_NONE);
            }
        }
    }

    /**
     * 搜索结束，刷新界面
     */
    private void searchFinish() {
        if (connectMethod == CONNECTMETHOD_AOTO) {
            mBtn_autoconnect.setText(getString(R.string.retry_connect));
            mTV_autoconnectstate.setText(getString(R.string.retry_tips));
        } else {
            tv_research.setText(getString(R.string.search_bluedevice));
        }
        mWaitingBar.setIsRun(false);
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_DISCOVERY
                    || BluetoothConnectUtils.getInstance().getBluetoothState() == BluetoothConnectUtils.BLUETOOTH_NONE) {
                startConnect(mData_devices.get(position).getDevice());
            }
        }
    };

    /**
     * 启动连接
     */
    private void startConnect(BluetoothDevice device) {
        if (null == device) {
            return;
        }
        Log.d(TAG, "startConnect   addr==" + device.getAddress());
        if (null == device.getAddress() || device.getAddress().equals("")) {
            mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO);
            return;
        }
        tv_research.setText(R.string.connecting);
        mTV_autoconnectstate.setText(R.string.connecting);
        if (!mWaitingBar.getIsRun()) {
            mWaitingBar.setIsRun(true);
        }
        mFinishDiscovery_method = mFinishDiscovery_startConnect;
        BluetoothConnectUtils.getInstance().connect(device);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "onKeyDown----KEYCODE_BACK----------closeBluetoothCommunication");
            BluetoothConnectUtils.getInstance().closeBluetoothCommunication();
            System.exit(0);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == CONNECTBLUETOOTH_OK) {
            Log.d(TAG, "handmsg-----CONNECTBLUETOOTH_OK");
            // 连接蓝牙成功
            Intent intent = new Intent(BlueTooth3Activity.this, MainActivity.class);
            intent.putExtra("backfromBluetooth", true);
            startActivity(intent);
            BlueTooth3Activity.this.finish();
        } else if (msg.what == CONNECTBLUETOOTH_NO) {
            Log.d(TAG, "handmsg-----CONNECTBLUETOOTH_NO");
            if (mLayout_autoconnect.getVisibility() == View.VISIBLE) {// 自动连接模式
                mTV_startcustom.setVisibility(View.VISIBLE);
                mTV_autoconnectstate.setText(R.string.retry_tips);
            } else {
                tv_research.setText(R.string.connect_failed);
            }
            // 连接蓝牙失败
            cup.setVisibility(View.VISIBLE);
            phone.setVisibility(View.VISIBLE);
            layout_bluestatus.setVisibility(View.VISIBLE);
            gesture_p.setVisibility(View.GONE);
            mMaxRssiDevice = null;
            mMaxRssi = mRssi_connect - 100;
        } else if (msg.what == CONNECTBLUETOOTH_WAIT) {
            // 等待手势
            Log.d(TAG, "handmsg-----CONNECTBLUETOOTH_WAIT");
            cup.setVisibility(View.GONE);
            phone.setVisibility(View.GONE);
            layout_bluestatus.setVisibility(View.GONE);
            gesture_p.setVisibility(View.VISIBLE);

            if (mLayout_autoconnect.getVisibility() == View.VISIBLE) {// 自动连接模式
                mTV_startcustom.setVisibility(View.INVISIBLE);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    int iCount = 0;
                    while (iCount < 80) {
                        if (BluetoothConnectUtils.getInstance().getConfirmPass()) {
                            break;
                        } else {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            iCount++;
                        }
                    }
                    if (iCount >= 80) {// 8秒后没有收到杯子发过来的确认连接信息
                        Log.d(TAG, "handmsg-----CONNECTBLUETOOTH_WAIT timeout");
                        BluetoothConnectUtils.getInstance().setConfirmPass(false);
                        // 发送广播通知连接失败
                        mHandler.sendEmptyMessage(CONNECTBLUETOOTH_NO);
                        BluetoothConnectUtils.getInstance().closeBluetoothCommunication();
                        // BluetoothConnectUtils.getInstance().dealAfaterDisconnect();
                    }
                }
            }).start();
        } else if (msg.what == BLUETOOTH_OPEN_SUCCEED) {
            // 打开蓝牙成功，开始搜索蓝牙
            Log.d(TAG, "handmsg----BLUETOOTH_OPEN_SUCCEED- click  tv_research");
            tv_research.performClick();
        } else if (msg.what == BLUETOOTH_OPEN_FAILED) {
            // 打开蓝牙失败，退出程序
            OcupToast.makeText(BlueTooth3Activity.this, getString(R.string.bluetooth_not_turnon), Toast.LENGTH_SHORT).show();
            BlueTooth3Activity.this.finish();

        } else if (msg.what == CONNECTBLUETOOTH_ING) {
            Log.d(TAG, "handmsg   connecting==");
            logMessage("正在连接");

        } else if (msg.what == CONNECTBLUETOOTH_PARING) {
            logMessage("正在配对");

        } else if (msg.what == CONNECTBLUETOOTH_UNPAIRING) {
            logMessage("正在解除配对");

        } else if (msg.what == CONNECTBLUETOOTH_NO_PAIR) {
            logMessage("连接失败：配对失败");

        } else if (msg.what == CONNECTBLUETOOTH_NO_UNPAIR) {
            logMessage("连接失败：解除配对失败");

        } else if (msg.what == CONNECTBLUETOOTH_NO_CONNECT) {
            logMessage("连接失败：连接失败");

        }
        return false;
    }

    public void logMessage(final String msg) {
        final long sysTime = System.currentTimeMillis();
        mTV_log.append(DateFormat.format("hh:mm:ss--", sysTime) + msg + "\n");
        Log.d("fqchen-test", DateFormat.format("hh:mm:ss--", sysTime) + msg);
    }

    /**
     * 通过放射的方法 获取设备类型
     *
     * @param device
     * @return 1:经典 ；   2:ble；    3:双模；    0:未知
     */
    private int getBluetoothDeviceType(BluetoothDevice device) {
        Class btClass = null;
        int ret = 1;
        try {
            btClass = Class.forName("android.bluetooth.BluetoothDevice");
            Method getTypeMethod = btClass.getMethod("getType");
            if (getTypeMethod != null) {
                ret = (Integer) getTypeMethod.invoke(device);
            }
        } catch (Exception e) {
            Log.d(TAG, "getBluetoothDeviceType-----------Exception--" + e);
            ret = 1;
        }
        return ret;
    }

    private final static int CONNECTBLUETOOTH_OK = 1;
    private final static int CONNECTBLUETOOTH_NO = 2;
    private final static int CONNECTBLUETOOTH_WAIT = 3;

    private final static int BLUETOOTH_OPEN_SUCCEED = 4;
    private final static int BLUETOOTH_OPEN_FAILED = 5;

    private final static int CONNECTBLUETOOTH_PARING = 6;
    private final static int CONNECTBLUETOOTH_UNPAIRING = 7;
    private final static int CONNECTBLUETOOTH_ING = 8;
    private final static int CONNECTBLUETOOTH_NO_PAIR = 9;
    private final static int CONNECTBLUETOOTH_NO_UNPAIR = 10;
    private final static int CONNECTBLUETOOTH_NO_CONNECT = 11;

}
