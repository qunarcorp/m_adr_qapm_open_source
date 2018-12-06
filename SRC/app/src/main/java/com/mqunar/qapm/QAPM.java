package com.mqunar.qapm;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.mqunar.qapm.check.ExceptionFinder;
import com.mqunar.qapm.dao.NetworkDataParse;
import com.mqunar.qapm.dao.Storage;
import com.mqunar.qapm.dao.UIDataParse;
import com.mqunar.qapm.domain.BaseData;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.logging.AndroidAgentLog;
import com.mqunar.qapm.logging.NullAgentLog;
import com.mqunar.qapm.network.sender.ISender;
import com.mqunar.qapm.network.sender.QAPMSender;
import com.mqunar.qapm.tracing.BackgroundTrace;
import com.mqunar.qapm.tracing.WatchMan;
import com.mqunar.qapm.utils.AndroidUtils;
import com.mqunar.qapm.utils.IOUtils;
import com.mqunar.qapm.utils.NetWorkUtils;
import com.mqunar.qapm.utils.ReflectUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * 最上层的管理类
 */
public class QAPM implements IQAPM{

    private static QAPM sInstance = null;
    private static boolean isRelease;

    public static Context mContext;
    private ISender mSender;
    private WatchMan mWatchMan;

    private String cParam ;
    private Handler mWorkHandler;
    private HandlerThread mWorkLooper;

    private QAPM(Context context, String cParam){
        mContext = getSafeContext(context) ;
        this.cParam = cParam == null ? getCParam() : cParam;
        this.mWatchMan = new BackgroundTrace();
        mWorkLooper = new HandlerThread(QAPMConstant.THREAD_UPLOAD);
        mWorkLooper.start();
        mWorkHandler = new Handler(mWorkLooper.getLooper());
        registerActivityLifecycleCallbacks();
    }

    public static QAPM make(Context context, int pid) {
        QAPMConstant.pid = pid + "";
        return context != null ? make(context, null) : null;
    }

    public static QAPM make(Context context, String cParam) {
        if (sInstance == null) {
            synchronized (QAPM.class) {
                if (sInstance == null) {
                    sInstance = new QAPM(context, cParam);
                }
            }
        }
        return sInstance;
    }

    public static QAPM getInstance(){
        return sInstance;
    }

    public String getCParam() {
        if (mContext == null) {
            return null;
        }
        if(cParam != null){
            return cParam;
        }
        JSONObject jobj = new JSONObject();
        try {
            String pkgName = mContext.getPackageName();
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(pkgName, 0);
            jobj.put("ma", AndroidUtils.getMac());
            jobj.put("osVersion", Build.VERSION.RELEASE + "_" + Build.VERSION.SDK_INT);
            jobj.put("pid", QAPMConstant.pid);
            jobj.put("uid", AndroidUtils.getIMEI(mContext));
            jobj.put("vid", packageInfo.versionCode);
            jobj.put("ke", String.valueOf(System.currentTimeMillis()));
        } catch (Exception ignore) {}
        return jobj.toString();
    }

    @Override
    public void addUIMonitor(Map<String, String> uiMonitorMapData) {
        if(uiMonitorMapData != null && uiMonitorMapData.size() > 0){
            BaseData uiLoadingData = UIDataParse.newInstance().convertMap2BaseData(uiMonitorMapData);
            Storage.newStorage(mContext).putData(uiLoadingData);
        }
    }

    @Override
    public void addNetMonitor(Map<String, String> netMonitorMapData) {
        if(netMonitorMapData != null && netMonitorMapData.size() > 0){
            BaseData netMonitorData = NetworkDataParse.newInstance().convertMap2BaseData(netMonitorMapData);
            Storage.newStorage(mContext).putData(netMonitorData);
        }
    }

    @Override
    public void setSender(ISender sender) {
        if(sender != null){
            mSender = sender;
        }
    }

    @Override
    public ISender getSender(){
        if (mSender == null) {
            String requestId = (String) ReflectUtils.invokeStaticMethod("com.mqunar.qav.uelog.QAVLog", "getRequestId", null, null);
            if(isRelease){
                mSender = new QAPMSender(QAPMConstant.HOST_URL, "", getCParam(), requestId);
            } else {
                mSender = new QAPMSender(QAPMConstant.HOST_URL_BETA, QAPMConstant.PITCHER_URL, getCParam(), requestId);
            }
        }
        return mSender;
    }

    public QAPM withLogEnabled(boolean enabled) {
        isRelease = !enabled;
        AgentLogManager.setAgentLog((enabled ? new AndroidAgentLog() : new NullAgentLog()));
        return this;
    }


    @Override
    public void release(){
        if (mWorkLooper != null) {
            mWorkLooper.quit();
        }
        unregisterActivityLifecycleCallbacks();
    }

    private void registerActivityLifecycleCallbacks() {
        if(mContext != null && mContext instanceof Application && mWatchMan != null){
            ((Application) mContext).registerActivityLifecycleCallbacks(mWatchMan);
        }
    }

    private void unregisterActivityLifecycleCallbacks() {
        if(mContext != null && mContext instanceof Application && mWatchMan != null){
            ((Application) mContext).unregisterActivityLifecycleCallbacks(mWatchMan);
        }
    }

    private Context getSafeContext(Context context) {
        if (context == null) {
            throw new NullPointerException("context is empty!");
        }
        if (context instanceof Application) {
            return context;
        } else {
            Context ctx = context.getApplicationContext();
            if (ctx != null) {
                return ctx;
            } else {
                //可能会内存泄漏
                return context;
            }
        }
    }

    public static String getSaveDataFile(String name){
        String path = IOUtils.getUploadDir(mContext);
        File destFile = new File(path, name);
        try {
            destFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return destFile.toString();
    }

    public void upload(final boolean isforceSend) {
//        ExceptionFinder.getInstance().checkForThrows(mContext);
        //防止主线程调用引起ANR
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!NetWorkUtils.isNetworkConnected(mContext)) {// 没有网络先不处理
                    return;
                }
                if (isforceSend) {
                    Storage.newStorage(mContext).popData();
                }
                String[] tempFileName = new File(IOUtils.getUploadDir(mContext)).list();
                if (tempFileName != null && tempFileName.length > 0) {
                    getSender().send(mContext, IOUtils.getUploadDir(mContext), cParam);
                }
            }
        });
    }

    public static String getActiveNetworkCarrier() {
        return AndroidUtils.carrierNameFromContext(mContext);
    }

    public static String getActiveNetworkWanType() {
        return AndroidUtils.wanType(mContext);
    }
}
