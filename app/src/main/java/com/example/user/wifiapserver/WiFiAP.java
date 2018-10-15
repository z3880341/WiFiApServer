package com.example.user.wifiapserver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
/*
content:创建WiFi热点class
time:2018-7-23 11：23
build:zhouqiang
 */

public class WiFiAP {
    private static WiFiAP mWiFiAP;
    private static WifiManager mWifManager;
    private WiFiAP(){}

    public static WiFiAP getI(){
        if (mWiFiAP == null){
            mWiFiAP = new WiFiAP();
        }
        return mWiFiAP;
    }

    /**
     * 手动得到系统权限的方法，提供给外部启动系统权限界面，以实现手动添加系统权限
     * @param context 外部activity的上下文
     */
    public void setPermissions(Context context){
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);

    }

    /**
     * 打开热点并且创建WiFi热点的方法
     * @param context 外部上下文
     * @param ssid 要创建WiFi热点的账号名称
     * @param password 要创建WiFi热点的密码
     * 注意,此方法直接使用WPA2_PSK 的安全策略创建WiFi热点,低版本的Android系统如果需要使用请切换。
     */
    @SuppressLint("MissingPermission")
    public void openWiFiAP(Context context, String ssid, String password){

        mWifManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (mWifManager.isWifiEnabled()) {
            //如果wifi处于打开状态，则关闭wifi,
            mWifManager.setWifiEnabled(false);
        }
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = ssid;
        config.preSharedKey = password;
        config.hiddenSSID = false;//是否隐藏热点true=隐藏
        config.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.OPEN);//开放系统认证
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        int indexOfWPA2_PSK = 4;
        //从WifiConfiguration.KeyMgmt数组中查找WPA2_PSK的值
        for (int i = 0; i < WifiConfiguration.KeyMgmt.strings.length; i++)
        {
            if(WifiConfiguration.KeyMgmt.strings[i].equals("WPA2_PSK"))
            {
                indexOfWPA2_PSK = i;
                break;
            }
        }
        //WifiConfiguration.KeyMgmt.WPA_PSK
        config.allowedKeyManagement.set(indexOfWPA2_PSK);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;
        //通过反射调用设置热点
        try {
            Method method = mWifManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            boolean enable = (Boolean) method.invoke(mWifManager, config, true);

            if (enable) {
                Log.e("WiFiAP", "热点已开启 SSID:" + ssid + " Password:"+password);
            } else {
                Log.e("WiFiAP", "创建热点失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("WiFiAP", "创建热点失败"+e);
        }

    }

    /**
     * 关闭WiFi热点的方法
     * @param context 外部activity的上下文
     */
    public void closeWiFiAP(Context context){
        mWifManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if( mWifManager == null){
            Log.e("closeWiFiAP", "Error: mWifManager is null");
            return;
        }
        try {
            Method method = mWifManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            WifiConfiguration config = (WifiConfiguration) method.invoke(mWifManager);
            Method method2 = mWifManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method2.invoke(mWifManager, config, false);
            //mText.setText("wifi热点关闭");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}
