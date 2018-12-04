package com.mqunar.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetWorkUtils {

    /**
     * 判断是否有网络连接
     * getActiveNetworkInfo()在有些手机上会报异常：
     * Neither user 10122 nor current process has android.permission.ACCESS_NETWORK_STATE.
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            try {
                ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable();
                }
            }catch (Exception e){

            }
        }
        return false;
    }
}
