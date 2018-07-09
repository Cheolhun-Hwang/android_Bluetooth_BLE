package com.hch.hooney.bluetoothleproject.BluetoothLE;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.hch.hooney.bluetoothleproject.R;

import java.util.ArrayList;
import java.util.List;

public class BluetoothLEHandler {
    private final String TAG = "BLE HANDLER... ";
    private final int REQUEST_ENABLE = 103;
    private final long SCAN_PERIOD = 10000;
    private Handler mHandler;
    private Activity runningActivity;
    private int BLU_STATE;

    private boolean mScanning;

    private BluetoothManager bthM;
    private BluetoothAdapter bthA;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;

    public BluetoothLEHandler(Activity nowActivity){
        this.runningActivity = nowActivity;
        this.bthM = (BluetoothManager) this.runningActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bthA = this.bthM.getAdapter();
        this.mHandler = new Handler();

        init();
    }

    public int getBLU_STATE() {
        return BLU_STATE;
    }
    public void setBLU_STATE(int BLU_STATE) {
        this.BLU_STATE = BLU_STATE;
    }
    public BluetoothManager getBthM() {
        return bthM;
    }
    public void setBthM(BluetoothManager bthM) {
        this.bthM = bthM;
    }
    public BluetoothAdapter getBthA() {
        return bthA;
    }
    public void setBthA(BluetoothAdapter bthA) {
        this.bthA = bthA;
    }

    private boolean init(){
        if(!checkFeatureBLE()){
            return false;
        }else{
            if (this.bthA == null || !bthA.isEnabled()) {
                alertOnBLE();
            }
        }
        return true;
    }

    public void stop(){
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
    }

    public void pause(){
        if (bthA != null && bthA.isEnabled()) {
            scanLeDevice(false);
        }
    }

    public void alertOnBLE(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.runningActivity);
        alertDialog.setTitle("Ava");
        alertDialog.setMessage("블루투스를 활성화되지 않았습니다.\n" +
                "블루투스를 활성화 하시겠습니까?");

        alertDialog.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Positive");
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        runningActivity.startActivityForResult(intent, REQUEST_ENABLE);
                    }
                });
        alertDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(runningActivity, "데이터통신을 통해 데이터가 전송됩니다.\n" +
                        "wifi 이외에 실행시 데이터가 소모됩니다.", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    private boolean checkFeatureBLE(){
        if (!this.runningActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(runningActivity, R.string.not_require_bluetooth_le_feature, Toast.LENGTH_SHORT).show();
            Log.e(TAG, runningActivity.getResources().getString(R.string.not_require_bluetooth_le_feature));
            return false;
        }
        return true;
    }

    public void BleScanning(){
        if (Build.VERSION.SDK_INT >= 21) {
            mLEScanner = bthA.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<ScanFilter>();
        }else{
            Toast.makeText(runningActivity, "안드로이드 버전 21이상만 지원합니다.",
                    Toast.LENGTH_SHORT).show();
        }
        scanLeDevice(true);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    if (Build.VERSION.SDK_INT < 21) {
//                        bthA.stopLeScan(mLeScanCallback);
//                    } else {
//                        mLEScanner.stopScan(mScanCallback);
//                    }
                    mScanning = false;
                    bthA.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
//            if (Build.VERSION.SDK_INT < 21) {
//                bthA.startLeScan(mLeScanCallback);
//            } else {
//                mLEScanner.startScan(filters, settings, mScanCallback);
//            }

            mScanning = true;
            bthA.startLeScan(mLeScanCallback);
        } else {
//            if (Build.VERSION.SDK_INT < 21) {
//                bthA.stopLeScan(mLeScanCallback);
//            } else {
//                mLEScanner.stopScan(mScanCallback);
//            }
            mScanning = false;
            bthA.stopLeScan(mLeScanCallback);
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();

            Log.i("Name", "Device : " + btDevice.getName());
            connectToDevice(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runningActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("onLeScan", device.toString());
                            Log.i("onLeScan", "NAME : "+device.getName());
                            connectToDevice(device);
                        }
                    });
                }
            };

    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(runningActivity, false, gattCallback);
            scanLeDevice(false);// will stop after first device detection
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
            gatt.readCharacteristic(services.get(1).getCharacteristics().get
                    (0));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            gatt.disconnect();
        }
    };
}
