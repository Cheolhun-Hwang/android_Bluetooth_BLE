package com.hch.hooney.bluetoothleproject.BluetoothLE;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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
import android.os.ParcelUuid;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.hch.hooney.bluetoothleproject.MainActivity;
import com.hch.hooney.bluetoothleproject.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothLEHandler {
    private final String TAG = "BLE HANDLER... ";
    private final int REQUEST_ENABLE = 103;
    private final long SCAN_PERIOD = 10000;
    private UUID MY_AVA_SERVER_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-000000000000");
    private UUID MY_AVA_AUTH_CHARACTER_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-100000000000");
    private UUID MY_AVA_WIFI_CHARACTER_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-200000000000");
    private UUID MY_AVA_AUTH_DESCRIPTOR_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-1f1000000000");
    private UUID MY_AVA_WIFI_DESCRIPTOR_UUID = UUID.fromString("ffffffff-ffff-ffff-ffff-2f1000000000");

    private Handler mHandler;
    private Activity runningActivity;
    private int BLU_STATE;
    private String AVA_MAC_ADDRESS;

    private BluetoothManager bthM;
    private BluetoothAdapter bthA;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private BluetoothGattCharacteristic mAuthCharacter;
    private BluetoothGattCharacteristic mWifiCharacter;

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
    public ScanSettings getSettings() {
        return settings;
    }
    public void setSettings(ScanSettings settings) {
        this.settings = settings;
    }
    public BluetoothGatt getmGatt() {
        return mGatt;
    }
    public void setmGatt(BluetoothGatt mGatt) {
        this.mGatt = mGatt;
    }
    public BluetoothGattCallback getGattCallback() {
        return gattCallback;
    }

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

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            if(result.toString().contains(MY_AVA_SERVER_UUID.toString()) &&
                    btDevice.getName().equals("AVA_raspberry_model")){
                Log.i(TAG, "Device : " + btDevice.getName());
                Log.i(TAG, "MAC : " + btDevice.getAddress());
                AVA_MAC_ADDRESS = btDevice.getAddress();
                connectToDevice(btDevice);
            }
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
            BluetoothGattService service = gatt.getService(MY_AVA_SERVER_UUID);
            MY_AVA_SERVER_UUID = service.getUuid();
            if(service == null){
                Log.d(TAG, "SERVICE UUID : "+ MY_AVA_SERVER_UUID);
                Log.d(TAG, "Can't Discover...");
            }else{
                mLEScanner.stopScan(mScanCallback);

                mAuthCharacter = service.getCharacteristic(MY_AVA_AUTH_CHARACTER_UUID);
                mWifiCharacter = service.getCharacteristic(MY_AVA_WIFI_CHARACTER_UUID);

//                gatt.readCharacteristic(mAuthCharacter);
                gatt.readCharacteristic(mWifiCharacter);
                ((MainActivity)runningActivity).displayPrepareSendValue();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            readCharacteristic(characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            Log.d(TAG, "Write...");
            if((status == BluetoothGatt.GATT_SUCCESS)){
                gatt.readCharacteristic(characteristic);
                prepareMac(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            Log.d(TAG, "onChangedCharacter");
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

            String read = byteToString(descriptor.getValue());
            if(read == null){
                Log.d(TAG, "Descriptor Read None...");
            }else{
                Log.d(TAG, "Response : " + read);
            }
        }


    };

    private void prepareMac(BluetoothGattCharacteristic characteristic){
        ((MainActivity)runningActivity).displayReadValue(characteristic.getStringValue(0));
    }

    private String byteToString(byte[] input){
        if(input != null && input.length > 0){
            //hex byte to binary byte;
            StringBuilder stringBuilder = new StringBuilder(input.length);
            for(byte byteChar : input){
                stringBuilder.append(String.format("%02X", byteChar));
            }
            //binary byte to String Ascii
            String txt = stringBuilder.toString();
            byte[] txtInByte = new byte[txt.length()/2];
            int j = 0;
            for(int i = 0; i<txt.length(); i+=2){
                txtInByte[j++] = Byte.parseByte(txt.substring(i, i+2), 16);
            }
            return new String(txtInByte);
        }
        return null;
    }

    private void readCharacteristic( BluetoothGattCharacteristic characteristic){
        Log.d(TAG, "BLE Character : " + characteristic.getStringValue(0));

//        ((MainActivity)runningActivity).displayReadValue();
    }

    public boolean sendDateToBLE(String msg){
        if(msg != null){
            if(mWifiCharacter != null){
                mWifiCharacter.setValue(msg.getBytes());
                mWifiCharacter.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                mGatt.writeCharacteristic(mWifiCharacter);
            }else{
                Log.e(TAG, "It isn't Connected BLE Server...");
            }
        }else{
            Log.e(TAG, "Empty BLE Send MSG...");
        }
        return false;
    }

    public void disConnected(){
        mGatt.disconnect();
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
                    if (Build.VERSION.SDK_INT < 21) {
                        bthA.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);
                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                bthA.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                bthA.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    public void printService(){
        Log.d(TAG, "UUID : " + UUID.fromString("ec0e"));
    }

    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            Log.d(TAG, "Connect to "+ device.getName());
            mGatt = device.connectGatt(runningActivity, true, gattCallback);
            scanLeDevice(false);// will stop after first device detection
        }
    }
}
