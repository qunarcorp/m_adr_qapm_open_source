package com.mqunar.qapm.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;

import com.mqunar.qapm.config.ConfigManager;
import com.mqunar.qapm.logging.AgentLogManager;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.UUID;

import static android.net.ConnectivityManager.TYPE_BLUETOOTH;
import static android.net.ConnectivityManager.TYPE_DUMMY;
import static android.net.ConnectivityManager.TYPE_ETHERNET;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_MOBILE_DUN;
import static android.net.ConnectivityManager.TYPE_MOBILE_HIPRI;
import static android.net.ConnectivityManager.TYPE_MOBILE_MMS;
import static android.net.ConnectivityManager.TYPE_MOBILE_SUPL;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.ConnectivityManager.TYPE_WIMAX;

public class AndroidUtils {

    private static final String TAG = "AndroidUtils";

    private static final String ANDROID = "Android";
    private static final String WIFI = "wifi";

    public static final String TIMEOUT = "timeout";//请求超时
    public static final String UNKNOWN = "Unknown";
    public static final String UNCONNECT = "unconnect";

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
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();
        } catch (Throwable e) {

        }
        return imei;
    }

    /**
     * 获取C参数，
     * 具体格式如下所示 ：
     * *vid:
     * *pid:
     * *cid:
     * uid:
     * osVersion: 系统版本
     * model: 机型
     * *loc：位置信息，获取不到传Unknown
     * mno：运营商信息，获取不到传Unknown
     * key: 时间戳
     * ext：{}扩展字段，目前没有
     *
     * @param context ：Android 上下文环境
     *
     * @return json格式的Cparam
     */
    public static String getCParam(Context context) {
        if (context == null) {
            return null;
        }
        JSONObject object = new JSONObject();
        try {
            String pkgName = context.getPackageName();
            String mon = AndroidUtils.carrierNameFromContext(context);
            String loc = LocationUtils.getLocation(context);
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
            object.put("pid", ConfigManager.getInstance().getPid());
            String vid = ConfigManager.getInstance().getVid();
            object.put("vid", !TextUtils.isEmpty(vid) ? vid : packageInfo.versionCode + "");
            String cid = ConfigManager.getInstance().getCid();
            object.put("cid", !TextUtils.isEmpty(cid) ? cid : UNKNOWN);
            object.put("uid", AndroidUtils.getIMEI(context));
            object.put("osVersion", Build.VERSION.RELEASE + "_" + Build.VERSION.SDK_INT);
            object.put("model", Build.MODEL);
            object.put("loc", TextUtils.isEmpty(loc) ? UNKNOWN : loc);
            object.put("mno", TextUtils.isEmpty(mon) ? UNKNOWN : mon);
            object.put("key", String.valueOf(System.currentTimeMillis()));
            object.put("ext", ""); // 该字段先不支持
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static String getSN() {
        if (!"unknown".equals(sn)) {
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
        if (!TextUtils.isEmpty(macSerial)) {
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
     * ANDROID_ID seems a good choice for a unique device identifier. There are downsides: First, it is not
     * 100%
     * reliable on releases of Android prior to 2.2 (“Froyo”). Also, there has been at least one
     * widely-observed bug in
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

        if (!isConnected(networkInfo)) {
            return UNCONNECT;
        } else if (isWan(networkInfo)) {
            return carrierNameFromTelephonyManager(context);
        } else if (isWifi(networkInfo)) {
            return WIFI;
        } else {
            //            log.warning(MessageFormat.format("Unknown network type: {0} [{1}]", new
            // Object[]{networkInfo.getTypeName(), Integer.valueOf(networkInfo.getType())}));
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

        return !isConnected(networkInfo) ? UNCONNECT : (isWifi(networkInfo) ? WIFI : (isWan(networkInfo) ?
                connectionNameFromNetworkSubtype(networkInfo.getSubtype()) : UNKNOWN));
    }

    private static boolean isConnected(NetworkInfo networkInfo) {
        return networkInfo != null && networkInfo.isConnected();
    }

    private static boolean isWan(NetworkInfo networkInfo) {
        switch (networkInfo.getType()) {
            case TYPE_MOBILE:
            case TYPE_MOBILE_MMS:
            case TYPE_MOBILE_SUPL:
            case TYPE_MOBILE_DUN:
            case TYPE_MOBILE_HIPRI:
                return true;
            case TYPE_WIFI:
            default:
                return false;
        }
    }

    private static boolean isWifi(NetworkInfo networkInfo) {
        switch (networkInfo.getType()) {
            case TYPE_WIFI:
            case TYPE_WIMAX:
            case TYPE_BLUETOOTH:
            case TYPE_ETHERNET:
                return true;
            case TYPE_MOBILE_MMS:
            case TYPE_MOBILE_SUPL:
            case TYPE_MOBILE_DUN:
            case TYPE_MOBILE_HIPRI:
            case TYPE_DUMMY:
            default:
                return false;
        }
    }

    private static NetworkInfo getNetworkInfo(Context context) throws SecurityException {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            return connectivityManager.getActiveNetworkInfo();
        } catch (SecurityException var3) {
            //            log.warning("Cannot determine network state. Enable android.permission
            // .ACCESS_NETWORK_STATE in your manifest.");
            throw var3;
        }
    }

    private static String carrierNameFromTelephonyManager(Context context) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = telephonyManager.getSimOperator();
        if (networkOperator == null || networkOperator.trim().isEmpty()) {
            networkOperator = "unknown";
        }
        boolean smellsLikeAnEmulator =
                Build.PRODUCT.equals("google_sdk") || Build.PRODUCT.equals("sdk") || Build.PRODUCT.equals(
                        "sdk_x86") || Build.FINGERPRINT.startsWith("generic");
        return networkOperator.equals(ANDROID) && smellsLikeAnEmulator ? WIFI : networkOperator;
    }

    private static String connectionNameFromNetworkSubtype(int subType) {
        switch (subType) {
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
        if (requestId == null) {
            requestId = "";
        }
        return requestId;
    }

    public static String getPageName(Context context) {

        if (context instanceof Application) {
            AgentLogManager.getAgentLog().info("Warning! getPageName but context is application!");
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

        return pageName;
    }


    public static boolean isInMainThread(final long threadId) {
        return Looper.getMainLooper().getThread().getId() == threadId;
    }

    public static String getSceneForString(Activity activity, Fragment fragment) {
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
