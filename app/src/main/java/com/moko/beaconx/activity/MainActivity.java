package com.moko.beaconx.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.moko.beaconx.R;
import com.moko.beaconx.adapter.BeaconXListAdapter;
import com.moko.beaconx.entity.BeaconXInfo;
import com.moko.beaconx.service.MokoService;
import com.moko.beaconx.utils.BeaconXInfoParseableImpl;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.DeviceInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends Activity implements MokoScanDeviceCallback {


    @Bind(R.id.iv_refresh)
    ImageView ivRefresh;
    @Bind(R.id.lv_devices)
    ListView lvDevices;
    @Bind(R.id.tv_device_num)
    TextView tvDeviceNum;
    private MokoService mMokoService;
    private HashMap<String, BeaconXInfo> beaconXInfoHashMap;
    private ArrayList<BeaconXInfo> beaconXInfos;
    private BeaconXListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Intent intent = new Intent(this, MokoService.class);
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        beaconXInfoHashMap = new HashMap<>();
        beaconXInfos = new ArrayList<>();
        adapter = new BeaconXListAdapter(this);
        adapter.setItems(beaconXInfos);
        lvDevices.setAdapter(adapter);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMokoService = ((MokoService.LocalBinder) service).getService();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(MokoConstants.ACTION_CONNECT_SUCCESS);
            filter.addAction(MokoConstants.ACTION_CONNECT_DISCONNECTED);
            filter.addAction(MokoConstants.ACTION_RESPONSE_SUCCESS);
            filter.addAction(MokoConstants.ACTION_RESPONSE_TIMEOUT);
            filter.addAction(MokoConstants.ACTION_RESPONSE_FINISH);
            filter.setPriority(100);
            registerReceiver(mReceiver, filter);
            if (!MokoSupport.getInstance().isBluetoothOpen()) {
                // 蓝牙未打开，开启蓝牙
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, MokoConstants.REQUEST_CODE_ENABLE_BT);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (MokoConstants.ACTION_CONNECT_SUCCESS.equals(action)) {

                }
                if (MokoConstants.ACTION_CONNECT_DISCONNECTED.equals(action)) {

                }
                if (MokoConstants.ACTION_RESPONSE_TIMEOUT.equals(action)) {

                }
                if (MokoConstants.ACTION_RESPONSE_FINISH.equals(action)) {

                }
                if (MokoConstants.ACTION_RESPONSE_SUCCESS.equals(action)) {
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case MokoConstants.REQUEST_CODE_ENABLE_BT:

                    break;

            }
        } else {
            switch (requestCode) {
                case MokoConstants.REQUEST_CODE_ENABLE_BT:
                    // 未打开蓝牙
                    MainActivity.this.finish();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
    }


    @Override
    public void onStartScan() {
    }

    private BeaconXInfoParseableImpl beaconXInfoParseable;

    @Override
    public void onScanDevice(DeviceInfo deviceInfo) {
        final BeaconXInfo beaconXInfo = beaconXInfoParseable.parseDeviceInfo(deviceInfo);
        if (beaconXInfo == null) {
            return;
        }
        beaconXInfoHashMap.put(beaconXInfo.mac, beaconXInfo);
        updateDevices();
    }

    @Override
    public void onStopScan() {
        findViewById(R.id.iv_refresh).clearAnimation();
        animation = null;
        updateDevices();
    }

    private void updateDevices() {
        beaconXInfos.clear();
        beaconXInfos.addAll(beaconXInfoHashMap.values());
        Collections.sort(beaconXInfos, new Comparator<BeaconXInfo>() {
            @Override
            public int compare(BeaconXInfo lhs, BeaconXInfo rhs) {
                if (lhs.rssi > rhs.rssi) {
                    return -1;
                } else if (lhs.rssi < rhs.rssi) {
                    return 1;
                }
                return 0;
            }
        });
        adapter.notifyDataSetChanged();
        tvDeviceNum.setText(String.format("Devices(%d)", beaconXInfos.size()));
    }


    private ProgressDialog mScaningDialog;

    private void showScaningProgressDialog() {
        mScaningDialog = new ProgressDialog(this);
        mScaningDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mScaningDialog.setCanceledOnTouchOutside(false);
        mScaningDialog.setCancelable(false);
        mScaningDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mScaningDialog.setMessage("Scaning...");
        if (!isFinishing() && mScaningDialog != null && !mScaningDialog.isShowing()) {
            mScaningDialog.show();
        }
    }

    private void dismissScaningProgressDialog() {
        if (!isFinishing() && mScaningDialog != null && mScaningDialog.isShowing()) {
            mScaningDialog.dismiss();
        }
    }

    private ProgressDialog mLoadingDialog;

    private void showLoadingProgressDialog() {
        mLoadingDialog = new ProgressDialog(MainActivity.this);
        mLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoadingDialog.setMessage("Connecting...");
        if (!isFinishing() && mLoadingDialog != null && !mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    private void dismissLoadingProgressDialog() {
        if (!isFinishing() && mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    private Animation animation = null;

    @OnClick({R.id.iv_refresh, R.id.iv_about, R.id.rl_edit_filter})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_refresh:
                if (animation == null) {
                    beaconXInfoHashMap.clear();
                    animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
                    view.startAnimation(animation);
                    beaconXInfoParseable = new BeaconXInfoParseableImpl();
                    mMokoService.startScanDevice(this);
                    mMokoService.mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMokoService.stopScanDevice();
                        }
                    }, 1000 * 60);
                } else {
                    mMokoService.mHandler.removeMessages(0);
                    mMokoService.stopScanDevice();
                }
                break;
            case R.id.iv_about:
                break;
            case R.id.rl_edit_filter:
                break;
        }
    }
}
