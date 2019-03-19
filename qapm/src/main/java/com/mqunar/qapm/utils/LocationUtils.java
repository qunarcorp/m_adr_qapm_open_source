package com.mqunar.qapm.utils;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationUtils {

    /**
     * 初始化位置信息
     * @param context
     */
    public static String getLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return AndroidUtils.UNKNOWN;
                }
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    return location.getLongitude() + "," + location.getLatitude();
                }
            } else {
                LocationListener locationListener = new LocationListener() {
                    // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}
                    // Provider被enable时触发此函数，比如GPS被打开
                    @Override
                    public void onProviderEnabled(String provider) {}
                    // Provider被disable时触发此函数，比如GPS被关闭
                    @Override
                    public void onProviderDisabled(String provider) {}
                    // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
                    @Override
                    public void onLocationChanged(Location location) {}
                };
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    return location.getLongitude() + "," + location.getLatitude();
                }
            }
        }
        return AndroidUtils.UNKNOWN;
    }

    private static int checkSelfPermission(Context context, String permission) {
        if (context == null || context.getPackageManager() == null) {
            return -1;
        }
        return context.getPackageManager().checkPermission(permission, context.getPackageName());
    }

}
