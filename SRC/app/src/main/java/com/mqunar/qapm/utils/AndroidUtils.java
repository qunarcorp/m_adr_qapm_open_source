package com.mqunar.qapm.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;


import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.UUID;

public class AndroidUtils {

    private static final String TAG = "AndroidUtils";

    private static final String ANDROID = "Android";
    private static final String WIFI = "wifi";

    public static final String UNKNOWN = "Unknown";
    public static final String UNCONNECT = "unconnect";

    public static final int QAPM_OPEN_PAGE_KEY = 0x6FFEDCBA + 1;

    private static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
    private static String imei = null;

    private static String macSerial = "";
    private static String sn = "unknown";

    @SuppressLint("MissingPermission")
    public static String getIMEI(Context context) {
        if (!TextUtils.isEmpty(imei)) {
            return imei;
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();
        } catch (Throwable e) {

        }
        return imei;
    }

    public static String getSN() {
        if(!"unknown".equals(sn)){
            return sn;
        }
        try {
            if (Build.VERSION.SDK_INT >= 9) {
                sn = new Object() {

                    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
                    public String getSerial() {
                        return Build.SERIAL;
                    }
                }.getSerial();
            }
            if ("unknown".equals(sn)) {
                try {
                    Class<?> c = Class.forName("android.os.SystemProperties");
                    Method m = c.getDeclaredMethod("get", String.class, String.class);
                    sn = (String) m.invoke(null, "ro.serialno", "unknown");
                    if ("unknown".equals(sn)) {
                        sn = (String) m.invoke(null, "gsm.device.sn", "unknown");
                    }
                    if ("unknown".equals(sn)) {
                        sn = (String) m.invoke(null, "ril.serialnumber", "unknown");
                    }

                } catch (Exception e) {
                }
            }
            // String pre = Build.BRAND + "-" + Build.DEVICE + "-";
            if ("unknown".equals(sn)) {
                return "";
            }
        } catch (Throwable e) {

        }
        return sn;
    }

    public static String getMac() {
        if(!TextUtils.isEmpty(macSerial)){
            return macSerial;
        }
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (Throwable e) {

        }
        return macSerial;
    }

    /**
     * ANDROID_ID seems a good choice for a unique device identifier. There are downsides: First, it is not 100%
     * reliable on releases of Android prior to 2.2 (“Froyo”). Also, there has been at least one widely-observed bug in
     * a popular handset from a major manufacturer, where every instance has the same ANDROID_ID.
     */
    public static String getADID(Context context) {
        try {
            String aid = android.provider.Settings.Secure.getString(context.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);
            if ("9774d56d682e549c".equalsIgnoreCase(aid) || TextUtils.isEmpty(aid)) {
                return getIMEI(context);
            } else {
                return aid;
            }
        } catch (Throwable e) {

        }
        return "";
    }

    /**
     * 获取APN名称
     */
    public static String getApnName(Context context) {
        String apnName = "";
        try {
            Cursor cursor = context.getContentResolver().query(PREFERRED_APN_URI, new String[]{"_id",
                    "apn", "type"}, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int counts = cursor.getCount();
                if (counts != 0) {
                    if (!cursor.isAfterLast()) {
                        apnName = cursor.getString(cursor.getColumnIndex("apn"));
                    }
                }
                cursor.close();
            } else {
                // 适配中国电信定制机,如海信EG968,上面方式获取的cursor为空，所以换种方式
                cursor = context.getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    apnName = cursor.getString(cursor.getColumnIndex("user"));
                    cursor.close();
                }
            }
        } catch (Exception e) {
            try {
                ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context
                        .CONNECTIVITY_SERVICE);
                NetworkInfo ni = conManager.getActiveNetworkInfo();
                apnName = ni.getExtraInfo();
            } catch (Exception e1) {
                apnName = "";
            }
        }
        return apnName;
    }

    /**
     * 获取运营商
     *
     * @return
     */
    public static String getSimOperator(Context context) {
        String operator = "";
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            operator = manager.getSimOperator();
        } catch (Throwable e) {

        }
        return operator;
    }

    public static String carrierNameFromContext(Context context) {
        NetworkInfo networkInfo;
        try {
            networkInfo = getNetworkInfo(context);
        } catch (SecurityException var3) {
            return UNKNOWN;
        }

        if(!isConnected(networkInfo)) {
            return UNCONNECT;
        } else if(isWan(networkInfo)) {
            return carrierNameFromTelephonyManager(context);
        } else if(isWifi(networkInfo)) {
            return WIFI;
        } else {
//            log.warning(MessageFormat.format("Unknown network type: {0} [{1}]", new Object[]{networkInfo.getTypeName(), Integer.valueOf(networkInfo.getType())}));
            return UNKNOWN;
        }
    }

    public static String wanType(Context context) {
        NetworkInfo networkInfo;
        try {
            networkInfo = getNetworkInfo(context);
        } catch (SecurityException var3) {
            return UNKNOWN;
        }

        return !isConnected(networkInfo) ? UNCONNECT : (isWifi(networkInfo) ? WIFI : (isWan(networkInfo)?connectionNameFromNetworkSubtype(networkInfo.getSubtype()) : UNKNOWN));
    }

    private static boolean isConnected(NetworkInfo networkInfo) {
        return networkInfo != null && networkInfo.isConnected();
    }

    private static boolean isWan(NetworkInfo networkInfo) {
        switch(networkInfo.getType()) {
            case 0:
            case 2:
            case 3:
            case 4:
            case 5:
                return true;
            case 1:
            default:
                return false;
        }
    }

    private static boolean isWifi(NetworkInfo networkInfo) {
        switch(networkInfo.getType()) {
            case 1:
            case 6:
            case 7:
            case 9:
                return true;
            case 2:
            case 3:
            case 4:
            case 5:
            case 8:
            default:
                return false;
        }
    }

    private static NetworkInfo getNetworkInfo(Context context) throws SecurityException {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            return connectivityManager.getActiveNetworkInfo();
        } catch (SecurityException var3) {
//            log.warning("Cannot determine network state. Enable android.permission.ACCESS_NETWORK_STATE in your manifest.");
            throw var3;
        }
    }

    private static String carrierNameFromTelephonyManager(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = telephonyManager.getSimOperator();
        if(networkOperator == null || networkOperator.trim().isEmpty()){
            networkOperator = "unknown";
        }
        boolean smellsLikeAnEmulator = Build.PRODUCT.equals("google_sdk") || Build.PRODUCT.equals("sdk") || Build.PRODUCT.equals("sdk_x86") || Build.FINGERPRINT.startsWith("generic");
        return networkOperator.equals(ANDROID) && smellsLikeAnEmulator ? WIFI : networkOperator;
    }

    private static String connectionNameFromNetworkSubtype(int subType) {
        switch(subType) {
            case 0:
            default:
                return UNKNOWN;
            case 1:
                return "GPRS";
            case 2:
                return "EDGE";
            case 3:
                return "UMTS";
            case 4:
                return "CDMA";
            case 5:
                return "EVDO rev 0";
            case 6:
                return "EVDO rev A";
            case 7:
                return "1xRTT";
            case 8:
                return "HSDPA";
            case 9:
                return "HSUPA";
            case 10:
                return "HSPA";
            case 11:
                return "IDEN";
            case 12:
                return "EVDO rev B";
            case 13:
                return "LTE";
            case 14:
                return "HRPD";
            case 15:
                return "HSPAP";
        }
    }

    /*
    * 字符串 MD5 加密
    * */
    public static String stringToMD5(String string) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10)
                    hex.append("0");
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        } catch (Throwable e) {
        }
        return "";
    }


    /*
    * 获取网络请求ID，md5(uuid + imei)
    * */
    public static String getTraceId(Context context) {
        String requestId = stringToMD5(UUID.randomUUID().toString() + getIMEI(context));
        if(requestId == null) {
            requestId = "";
        }
        return requestId;
    }

    public static String getPageName(Context context) {

        if (context instanceof Application) {
            Log.d(TAG, "Warning! getPageName but context is application!");
        }

        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                break;
            } else {
                if (((ContextWrapper) context).getBaseContext() != null) {
                    context = ((ContextWrapper) context).getBaseContext();
                } else {
                    break;
                }
            }
        }

        String pageName = context.getClass().getSimpleName();
        try {
            Class transparentFragmentActivityBaseCls = Class.forName("com.mqunar.core.basectx.launcherfragment.TransparentFragmentActivityBase");
            Class launcherFragmentActivityBaseCls = Class.forName("com.mqunar.core.basectx.launcherfragment.LauncherFragmentActivityBase");
            if (transparentFragmentActivityBaseCls.isAssignableFrom(context.getClass())) {
                Field _fragmentName = transparentFragmentActivityBaseCls.getDeclaredField("_fragmentName");
                _fragmentName.setAccessible(true);
                String _pageName = (String) _fragmentName.get(context);
                if (!TextUtils.isEmpty(_pageName)) {
                    pageName = _pageName;
                }
            } else if (launcherFragmentActivityBaseCls.isAssignableFrom(context.getClass())) {
                Field _fragmentName = launcherFragmentActivityBaseCls.getDeclaredField("_fragmentName");
                _fragmentName.setAccessible(true);
                String _pageName = (String) _fragmentName.get(context);
                if (!TextUtils.isEmpty(_pageName)) {
                    pageName = _pageName;
                }
            }
        } catch (Throwable ignore) {
            ignore.printStackTrace();
        }


        String pkgName = getAtomName(context.getClass());

        //QFragment
        if (TextUtils.isEmpty(pkgName) &&
                pageName.indexOf(".") > 0) {
            //证明是sibei的activity复用机制
            try {
                Class<?> clazz = Class.forName(pageName);
                pkgName = getAtomName(clazz);
                pageName = clazz.getSimpleName();
            } catch (Throwable t) {
                Log.e(TAG, "found class crash");
            }
        }

        if (!TextUtils.isEmpty(pkgName)) {
            pageName = pkgName + "." + pageName;
        }

        //看看有没有自定义的PageName
        if (context instanceof Activity) {
            String pn = getPageName((Activity) context);
            if (!TextUtils.isEmpty(pn)) {
                return pageName + "-" + replace(pn);
            }
        }

        return pageName;
    }

    /**
     * 获取模块名称
     *
     * @param clz
     * @return
     */
    public static String getAtomName(Class clz) {
        try {
            Class<?> clazz = Class.forName("com.mqunar.core.QunarApkLoader");
            Method method = clazz.getDeclaredMethod("getPkgName", String.class);
            if (method == null) {
                method = clazz.getDeclaredMethod("getPackageName", String.class);
            }
            String packageName = (String) method.invoke(null, clz.getName());
            return packageName.replace("com.mqunar.", "").replace("atom.", "");
        } catch (Throwable ignore) {
        }
        return null;
    }

    private static String getPageName(Activity page) {
        View decor = page.getWindow().peekDecorView();
        if (decor != null) {
            return getValue(decor, QAPM_OPEN_PAGE_KEY);
        } else {
            return null;
        }
    }
    public static boolean isInMainThread(final long threadId) {
        return Looper.getMainLooper().getThread().getId() == threadId;
    }
    public static String getSceneForString( Activity activity, Fragment fragment) {
        if (null == activity) {
            return "null";
        }
        return activity.getClass().getName() + (fragment == null ? "" : "&" + fragment.getClass().getName());
    }

    @TargetApi(Build.VERSION_CODES.DONUT)
    private static String getValue(View v, int key) {
        Object object = v.getTag(key);
        if (object instanceof String) {
            return (String) object;
        } else {
            return null;
        }
    }

    public static String replace(String value) {
        if (TextUtils.isEmpty(value)) {
            return value;
        }
        return value
                .replace("|", "｜")
                .replace("*", "＊")
                .replace(":", "：")
                .replace("&", "＆")
                .replace("\n", "、Ｎ")
                .replace("^", "＾")
                .trim();
    }
}
