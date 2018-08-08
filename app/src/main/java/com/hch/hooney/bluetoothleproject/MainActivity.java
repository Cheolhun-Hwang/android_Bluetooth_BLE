package com.hch.hooney.bluetoothleproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hch.hooney.bluetoothleproject.App.Application;
import com.hch.hooney.bluetoothleproject.BluetoothLE.BluetoothLEHandler;

import static com.hch.hooney.bluetoothleproject.App.Application.*;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private BluetoothLEHandler bleHandler;
    private TextView bleStateTextview;
    private Button bleOnButton;
    private Button bleSearchButton;
    private Button bleSendButton;
    private boolean flagScanning;
    private TextView bleReadValue;
    private EditText bleSendValue;

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
//        bleHandler.pause();
    }

    @Override
    protected void onStop() {
//        bleHandler.stop();
        super.onStop();
    }

    private void init() throws Exception {
        bleHandler = new BluetoothLEHandler(MainActivity.this);
        bleOnButton = (Button) findViewById(R.id.ble_on);
        bleSearchButton = (Button) findViewById(R.id.ble_search);
        bleStateTextview = (TextView) findViewById(R.id.main_ble_state_textview);
        bleSendButton = (Button) findViewById(R.id.ble_send);
        flagScanning = false;
        bleSendValue = (EditText) findViewById(R.id.main_ble_send_value);
        bleReadValue = (TextView) findViewById(R.id.main_ble_get_value);
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
                    bleHandler.sendDateToBLE(bleSendValue.getText().toString());
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

    public void displayPrepareSendValue(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bleReadValue.setVisibility(View.VISIBLE);
                bleSendValue.setVisibility(View.VISIBLE);
            }
        });
    }

    public void displayReadValue(final String res){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String before = bleReadValue.getText().toString();
                bleReadValue.setText(before+"\n"+res);
            }
        });
    }
}
