package com.example.user.wifiapserver.handler;

import android.util.Log;

import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.httpcore.HttpClientConnection;
import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.entity.StringEntity;
import org.apache.httpcore.protocol.HttpContext;
import org.apache.httpcore.protocol.HttpRequestExecutor;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

/*
content: 提交html 处理class
time:2018-8-10 10：07
build:zhouqiang
 */


public class SubmitHandier implements RequestHandler {
    @Override
    //   HttpRequest 请求     HttpResponse响应
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);

        if (!params.containsKey("username") || !params.containsKey("password")) {
            StringEntity stringEntity = new StringEntity("Please enter your account number and password.", "utf-8");

            response.setStatusCode(400);
            response.setEntity(stringEntity);
            return;
        }

        String userName = URLDecoder.decode(params.get("username"), "utf-8");
        String password = URLDecoder.decode(params.get("password"), "utf-8");
        Log.e("asd", "userName="+userName);
        Log.e("asd", "password="+password);

    }
}
