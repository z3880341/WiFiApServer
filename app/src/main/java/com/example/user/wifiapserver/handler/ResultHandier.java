package com.example.user.wifiapserver.handler;
/*
content:结果html 处理class
time:2018-8-10 10：07
build:zhouqiang
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.httpcore.Header;
import org.apache.httpcore.HttpEntity;
import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.entity.StringEntity;
import org.apache.httpcore.protocol.HttpContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Map;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ResultHandier implements RequestHandler {
    private Context mContext;
    private LocalBroadcastManager localBroadcastManager;
    public ResultHandier(Context context){
        this.mContext = context;

    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Log.e("asd", "ResultHandier启动");
        Map<String,String> params = HttpRequestParser.parseParams(request);

        if (!params.containsKey("username") || !params.containsKey("password")) {
            StringEntity stringEntity = new StringEntity("Please enter your account number and password.", "utf-8");
            response.setStatusCode(400);
            response.setEntity(stringEntity);
            return;
        }

        String userName = URLDecoder.decode(params.get("username"), "utf-8");
        String password = URLDecoder.decode(params.get("password"), "utf-8");
        Log.e("asd", "ResultHandieruserName账号："+userName);
        Log.e("asd", "ResultHandierpassword密码："+password);
        //发送本地广播
        localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        Intent intent = new Intent("com.example.user.wifiapserver.WIFI_DATA");
        intent.putExtra("userName",userName);
        intent.putExtra("password",password);
        localBroadcastManager.sendBroadcast(intent);

        StringEntity stringEntity = new StringEntity("   输入内容：\n\r   账号："+userName+" \n\r   密码："+password+
                " \n\r 正在切换wifi请稍后...","utf-8");
        response.setEntity(stringEntity);



    }
}
