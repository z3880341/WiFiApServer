package com.example.user.wifiapserver;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/*
content:动态权限申请的class
time:2018-7-20 20：57
build:zhouqiang
用法说明：
1.在需要授权的activity里,重写onRequestPermissionsResult 授权回调,
并且将里面的值添加到DynamicPermissions.I().onRequestPermissionsResult()方法里
 */
public class DynamicPermissions {
    private static DynamicPermissions dynamicPermissions;
    private String TAG = "DynamicPermissions";
    public PermissionsTo mPermissionsTo;
    private int KEY = 101;
    private List<String> mList = new ArrayList<>();
    public Context mContext;
    private String[] mPermissionsGroup;
    private DynamicPermissions(){

    }

    //实现单例模式
    public static DynamicPermissions I(){
        if(dynamicPermissions == null){
            dynamicPermissions = new DynamicPermissions();

        }
        return dynamicPermissions;
    }
    //导入数据
    public void setInitData(Context context, String[] permissionsGroup, PermissionsTo permissionsTo){
        this.mContext = context;
        this.mPermissionsGroup = permissionsGroup;
        this.mPermissionsTo = permissionsTo;
        privilege();
    }

    //授权
    public void privilege(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            for(int i=0;i<mPermissionsGroup.length;i++){
                if (ContextCompat.checkSelfPermission(mContext, mPermissionsGroup[i]) != PackageManager.PERMISSION_GRANTED) {
                    mList.add(mPermissionsGroup[i]);
                }
            }

            if (!mList.isEmpty()) {
                String[] permissions = mList.toArray(new String[mList.size()]);
                ActivityCompat.requestPermissions((Activity) mContext, permissions, KEY);
                Log.e(TAG, "privilege: 正在授权");
            } else {
                Log.e(TAG, "privilege: 已经授权,不需要授权");
                if(mPermissionsTo!=null){
                    this.mPermissionsTo.hasAuthorizeinit(mContext);
                }

            }

        }else {
            Log.e(TAG, "privilege:6.0以下的版本无需授权");
            if(mPermissionsTo!=null){
                this.mPermissionsTo.noAuthorizeinit(mContext);
            }
        }

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode == KEY){
            int j = 0;
            if (grantResults.length > 0) {//安全写法，如果小于0，肯定会出错了
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "onRequestPermissionsResult：授权成功"+i);
                        j++;
                    } else {
                        ((Activity)mContext).finish();
                        Toast.makeText(mContext, "授权失败", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onRequestPermissionsResult：授权失败");
                    }
                }
            }
            if (j == grantResults.length){
                Log.e(TAG, "onRequestPermissionsResult：全部权限都授权成功");
                if(mPermissionsTo!=null) {
                    this.mPermissionsTo.authorizeinitFinish(mContext);
                }
            }

        }

    }
    //创建回调接口，在1.版本低不需要动态授权 2.已经授权过 3.授权完成后  这3个地方的后续运行逻辑进行统一的接口调出
    public interface PermissionsTo{
        void hasAuthorizeinit(Context context);
        void noAuthorizeinit(Context context);
        void authorizeinitFinish(Context context);

    }
}
