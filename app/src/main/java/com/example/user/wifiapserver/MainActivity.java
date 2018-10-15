package com.example.user.wifiapserver;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.user.wifiapserver.service.PhoneServer;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mBtnPermission;
    private ToggleButton mBtnWiFiAP;
    private String [] mWifiPermissions = new String[]{Manifest.permission.CHANGE_NETWORK_STATE,Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.ACCESS_WIFI_STATE,Manifest.permission.INTERNET};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnPermission = (Button)findViewById(R.id.btn_permission);
        mBtnWiFiAP = (ToggleButton)findViewById(R.id.btn_offAndOn);
        mBtnPermission.setOnClickListener(this);
        mBtnWiFiAP.setOnClickListener(this);

        DynamicPermissions.I().setInitData(MainActivity.this, mWifiPermissions, new DynamicPermissions.PermissionsTo() {
            @Override
            public void hasAuthorizeinit(Context context) {
                Toast.makeText(MainActivity.this,"授权完成",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void noAuthorizeinit(Context context) {
                Toast.makeText(MainActivity.this,"授权完成",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void authorizeinitFinish(Context context) {
                Toast.makeText(MainActivity.this,"授权完成",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_permission:
                WiFiAP.getI().setPermissions(this);
                break;
            case R.id.btn_offAndOn:
                Intent intent = new Intent(MainActivity.this,PhoneServer.class);
                if (mBtnWiFiAP.isChecked()){
                    startService(intent);
                }else {
                    stopService(intent);
                }
                break;
            default:
                break;
        }
    }
}
