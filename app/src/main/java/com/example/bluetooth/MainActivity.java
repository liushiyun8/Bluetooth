package com.example.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity implements BluetoothSPP.BluetoothStateListener {
    public String TAG = getClass().getSimpleName();
    private BluetoothSPP bt;
    private Button btn;
    private BluetoothReciever receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bt.isBluetoothEnabled()) {
                    btn.setText("打开");
                    bt.getBluetoothAdapter().disable();
                } else {
                    btn.setText("关闭");
                    bt.enable();
                }
            }
        });
        registerBlueBradcast();
        bt = new BluetoothSPP(this);
        if (!bt.isBluetoothAvailable()) {
            Log.e(TAG, "蓝牙不可用");
        }
        if (bt.isBluetoothEnabled()) {
            btn.setText("关闭");
        } else btn.setText("打开");
        bt.setBluetoothStateListener(this);
    }

    private void registerBlueBradcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        receiver = new BluetoothReciever();
        registerReceiver(receiver, intentFilter);
    }

    private void unRegisterBlueBradcast() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onServiceStateChanged(int state) {
        Log.e(TAG, "state:" + state);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBlueBradcast();
    }

    private class BluetoothReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null)
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                        switch (state) {
                            case BluetoothAdapter.STATE_ON:
                                Log.e(TAG, "蓝牙打开");
                                bt.setupService();
                                bt.startService(BluetoothState.DEVICE_ANDROID);
//                                setup();
                                startActivityForResult(new Intent(MainActivity.this, DeviceList.class),BluetoothState.REQUEST_CONNECT_DEVICE);
                                break;
                            case BluetoothAdapter.STATE_OFF:
                                Log.e(TAG, "蓝牙关闭");
                                break;
                            case BluetoothAdapter.STATE_TURNING_ON:
                                Log.e(TAG, "蓝牙正在打开");
                                break;
                            case BluetoothAdapter.STATE_TURNING_OFF:
                                Log.e(TAG, "蓝牙正在关闭");
                                break;
                        }
                        break;
                    case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        String name = device.getName();
                        Log.d("aaa", "device name: " + name);
                        state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                        switch (state) {
                            case BluetoothDevice.BOND_NONE:
                                Log.d(TAG, "BOND_NONE 删除配对");
                                break;
                            case BluetoothDevice.BOND_BONDING:
                                Log.d(TAG, "BOND_BONDING 正在配对");
                                break;
                            case BluetoothDevice.BOND_BONDED:
                                Log.d(TAG, "BOND_BONDED 配对成功");
                                bt.connect(device.getAddress());
                                break;
                        }
                        break;

                }
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK){
                String address = data.getExtras().getString(BluetoothState.EXTRA_DEVICE_ADDRESS);
                BluetoothDevice device = bt.getBluetoothAdapter().getRemoteDevice(address);
                device.createBond();
            }

        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
//                setup();
            } else {
                // Do something if user doesn't choose any device (Pressed back)
            }
        }
    }
}
