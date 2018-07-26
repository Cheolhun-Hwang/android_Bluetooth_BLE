package com.hch.hooney.bluetoothleproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hch.hooney.bluetoothleproject.BluetoothLE.BluetoothLEHandler;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private BluetoothLEHandler bleHandler;
    private TextView bleStateTextview;
    private Button bleOnButton;
    private Button bleSearchButton;
    private Button bleSendButton;
    private boolean flagScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        stateText();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            setUi();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        bleHandler.pause();
    }

    @Override
    protected void onStop() {
        bleHandler.stop();
        super.onStop();
    }

    private void init() throws Exception {
        bleHandler = new BluetoothLEHandler(MainActivity.this);
        bleOnButton = (Button) findViewById(R.id.ble_on);
        bleSearchButton = (Button) findViewById(R.id.ble_search);
        bleStateTextview = (TextView) findViewById(R.id.main_ble_state_textview);
        bleSendButton = (Button) findViewById(R.id.ble_send);
        flagScanning = false;
        setEvent();
    }

    private void setEvent() throws Exception{
        bleOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bleHandler.getBthA().isEnabled()){
                    bleHandler.alertOnBLE();
                }else{
                    Toast.makeText(getApplicationContext(), "블루투스가 이미 실행 중 입니다.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        bleSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bleHandler.getBthA().isEnabled()){
                    bleHandler.alertOnBLE();
                }else{
                    if(!flagScanning){
                        //search
                        bleHandler.BleScanning();
                        flagScanning = true;
                        bleSearchButton.setText("검색 중...");
                    }else{
                        bleHandler.stop();
                        flagScanning = false;
                        bleSearchButton.setText("블루투스 검색");
                    }
                }
            }
        });

        bleSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bleHandler.getmGatt()== null){
                    Toast.makeText(getApplicationContext(),
                            "BLE 검색을 먼저 시작해주세요.",
                            Toast.LENGTH_SHORT).show();
                }else{
                    bleHandler.sendDateToBLE("HELLO");
                }
            }
        });
    }

    private void setUi() throws Exception{
        stateText();
    }

    private void stateText(){
        if(bleHandler.getBthA().isEnabled()){
            bleStateTextview.setVisibility(View.GONE);
        }else{
            bleStateTextview.setVisibility(View.VISIBLE);
        }
    }
}
