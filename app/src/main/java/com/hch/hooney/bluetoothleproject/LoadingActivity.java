package com.hch.hooney.bluetoothleproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

public class LoadingActivity extends AppCompatActivity {
    private final int SIGNAL_PERMISSION = 12;
    private final String TAG = "LoadingActivity";

    private ProgressBar progress;
    private Thread loadingThread;
    private Handler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception{
        progress = (ProgressBar) findViewById(R.id.loading_progress);

        myHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case 101:
                        progress.setVisibility(View.VISIBLE);
                        break;
                    case 102:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkDangerousPermissions();
    }

    private Thread initLoadingThread(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Message msg = myHandler.obtainMessage();
                    msg.what = 101;
                    myHandler.sendMessage(msg);
                    Thread.sleep(1000);
                    Message msg2 = myHandler.obtainMessage();
                    msg2.what = 102;
                    myHandler.sendMessage(msg2);


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /* 사용자 권한 확인 메서드
          - import android.Manifest; 를 시킬 것
        */
    private void checkDangerousPermissions() {
        String[] permissions = {//import android.Manifest;
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        //권한을 가지고 있는지 체크
        boolean isall = true;
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(LoadingActivity.this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                isall = false;
                break;
            }
        }

        if (isall) {
            Log.d(TAG, "권한있음");
            loadingThread = initLoadingThread();
            loadingThread.start();
        } else {
            Log.d(TAG, "권한없음");

            ActivityCompat.requestPermissions(LoadingActivity.this, permissions, SIGNAL_PERMISSION);
        }
    }//end of checkDangerousPermissions



    // 사용자의 권한 확인 후 사용자의 권한에 대한 응답 결과를 확인하는 콜백 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == SIGNAL_PERMISSION) {
            boolean isall = true;
            for(int temp : grantResults){
                if(temp == PackageManager.PERMISSION_DENIED){
                    isall = false;
                }
            }

            if(isall){
                loadingThread = initLoadingThread();
                loadingThread.start();
            }else{
                checkDangerousPermissions();
            }
        }
    }//end of onRequestPermissionsResult

}
