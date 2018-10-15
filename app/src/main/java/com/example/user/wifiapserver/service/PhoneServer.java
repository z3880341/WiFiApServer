package com.example.user.wifiapserver.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.user.wifiapserver.WiFiAP;
import com.example.user.wifiapserver.handler.ResultHandier;
import com.example.user.wifiapserver.handler.SubmitHandier;
import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.Server;
import com.yanzhenjie.andserver.website.AssetsWebsite;

import java.util.concurrent.TimeUnit;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
/*
content：服务 主要工作注册广播 启动WiFi热点 启动服务器 接收服务器账号密码信息 ，执行注册广播 关闭热点关闭服务器 切换WiFi
time:2018-8-11 11：13
build:zhouqiang
 */
public class PhoneServer extends Service {
    private final static int WIFIAP_SLEEP = 101;
    private final static int WIFI_SLEEP = 102;

    private Server mAndServer;
    private Handler mHandler;
    private AndServer server;
    private IntentFilter mIntentFilter;
    private WiFiDataBroadcastReceiver mWiFiDataBroadcastReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;


    public PhoneServer() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        addReceiver();//注册广播
        WiFiAP.getI().openWiFiAP(this,"yuntian","88888888");//打开热点
        Toast.makeText(PhoneServer.this,"WiFi热点创建成功",Toast.LENGTH_SHORT).show();
        Log.e("PhoneServer", "WiFi热点创建成功");
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case WIFIAP_SLEEP:
                        buildServer();//创建服务器
                        break;
                    case WIFI_SLEEP:
                        Bundle bundle = msg.getData();
                        String userName = bundle.getString("userName");
                        String password = bundle.getString("password");
                        Log.e("MainActivity", "WiFi连接中...");
                        Toast.makeText(PhoneServer.this,"WiFi连接中...",Toast.LENGTH_SHORT).show();
                        mWiFiDataBroadcastReceiver.AddWifiConfig(PhoneServer.this,userName,password);
                        //createWifiConfiguration(userName,password,WifiConfiguration.KeyMgmt.WPA_PSK);
                        Log.e("MainActivity", "WiFi连接成功！");
                        Toast.makeText(PhoneServer.this,"WiFi连接成功！",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        };
        sleepThread();
    }

    /*
     * 等待线程，主要作用是给WiFi热点创建一些时间，防止服务器直接创建会获取不到热点ip地址
     */
    public void sleepThread(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message message = Message.obtain();
                message.what = WIFIAP_SLEEP;
                mHandler.sendMessage(message);

            }
        });
        thread.start();
    }

    //注册广播
    public void addReceiver(){
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mWiFiDataBroadcastReceiver = new WiFiDataBroadcastReceiver();
        mIntentFilter = new IntentFilter("com.example.user.wifiapserver.WIFI_DATA");
        mLocalBroadcastManager.registerReceiver(mWiFiDataBroadcastReceiver,mIntentFilter);
        Toast.makeText(PhoneServer.this,"广播注册成功",Toast.LENGTH_SHORT).show();

    }
    //创建服务器
    public void buildServer(){
        RequestHandler requestHandler = new ResultHandier(PhoneServer.this);
        AssetManager assetManager = getAssets();
        AssetsWebsite website = new AssetsWebsite(assetManager,"web");
        mAndServer = AndServer.serverBuilder()
                .port(8012)
                .timeout(10, TimeUnit.SECONDS)
                .website(website)
                .registerHandler("/result",requestHandler)
                .registerHandler("/submit",new SubmitHandier())
                .build();
        Toast.makeText(PhoneServer.this,"服务器创建成功",Toast.LENGTH_SHORT).show();
        Log.e("PhoneServer", "服务器创建成功");
        mAndServer.startup();
        Toast.makeText(PhoneServer.this,"服务器启动成功",Toast.LENGTH_SHORT).show();
        Log.e("PhoneServer", "服务器启动");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAndServer.shutdown();
        mLocalBroadcastManager.unregisterReceiver(mWiFiDataBroadcastReceiver);
        Toast.makeText(PhoneServer.this,"服务关闭",Toast.LENGTH_SHORT).show();
        Toast.makeText(this,"服务关闭",Toast.LENGTH_SHORT).show();
    }

    //自定义广播
    class WiFiDataBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String userName = intent.getStringExtra("userName");
            final String password = intent.getStringExtra("password");
            Log.e("MainActivity", "本地广播接受到信息 账号:"+userName+"密码:"+password);
            Toast.makeText(PhoneServer.this,"本地广播接受到信息 账号:"+userName+"密码:"+password,Toast.LENGTH_SHORT).show();
            mAndServer.shutdown();//关闭服务器
            Toast.makeText(context,"服务器关闭",Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "服务器关闭");
            WiFiAP.getI().closeWiFiAP(context);//关闭WiFi热点
            Log.e("MainActivity", "WiFi热点关闭");
            Toast.makeText(context,"WiFi热点关闭",Toast.LENGTH_SHORT).show();
            if(isWiFiActive(context) == false){
                isOpenWifi(context,true);//启动wifi
                Log.e("MainActivity", "WiFi启动");
                Toast.makeText(context,"WiFi启动",Toast.LENGTH_SHORT).show();
            }

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Message message = Message.obtain();
                    message.what = WIFI_SLEEP;
                    Bundle bundle = new Bundle();
                    bundle.putString("userName",userName);
                    bundle.putString("password",password);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            }, 10 * 1000);

        }

        /**
         * 返回WiFi状态
         * @param context 外部上下文
         * @return true=打开 false=关闭
         */
        public  boolean isWiFiActive(Context context){
            WifiManager wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);//得到wifi管理器对象

            return wifimanager.isWifiEnabled();
        }
        /**
         * 打开或者关闭WiFi
         * @param context 外部上下文
         * @param isOpen true=打开 false=关闭
         */
        public  void isOpenWifi(Context context,boolean isOpen) {
            WifiManager wifimanager  = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);//得到wifi管理器对象
            wifimanager.setWifiEnabled(isOpen); //打开或关闭
        }

        /**
         * 添加WiFi
         * @param context 外部上下文
         * @param wifiname WiFi账号
         * @param pwd WiFi密码
         */
        public  void AddWifiConfig(Context context, String wifiname , String pwd) {
            WifiManager wifimanager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);//得到wifi管理器对象
            int wifiId = -1;//自己定义的数值，判断用
            WifiConfiguration wifiCong = new WifiConfiguration();//这个类是我们构造wifi对象使用的，具体可以百度
            wifiCong.SSID = "\"" + wifiname + "\"";// \"转义字符，代表"//为成员变量赋值
            wifiCong.preSharedKey = "\"" + pwd + "\"";// WPA-PSK密码
            wifiCong.hiddenSSID = false;
            wifiCong.status = WifiConfiguration.Status.ENABLED;
            wifiId = wifimanager.addNetwork(wifiCong);// 将配置好的特定WIFI密码信息添加,添加完成后默认是不激活状态，成功返回ID，否则为-1
            if ( wifiId!=-1 ) {
                Log.e("MainActivity", "WiFi添加成功");
                //添加成功
            }else{
                Log.e("MainActivity", "WiFi添加失败");
                //添加失败
            }
            boolean isConected =  wifimanager.enableNetwork(wifiId, true);  // 连接配置好的指定ID的网络 true连接成功
            if ( isConected ) {
                Log.e("MainActivity", "WiFi连接成功");
                //连接成功
                WifiInfo info = wifimanager.getConnectionInfo();
            }else {
                Log.e("MainActivity", "WiFi连接失败");
                //连接失败
            }
        }
    }
}
