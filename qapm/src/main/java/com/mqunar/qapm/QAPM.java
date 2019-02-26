package com.mqunar.qapm;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import com.mqunar.qapm.core.ApplicationLifeObserver;
import com.mqunar.qapm.dao.Storage;
import com.mqunar.qapm.domain.BaseData;
import com.mqunar.qapm.domain.NetworkData;
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

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * 最上层的管理类
 */
public class QAPM implements IQAPM {

    private static QAPM sInstance = null;
    private static boolean isRelease;

    public static Context mContext;
    private ISender mSender;
    private WatchMan mWatchMan;

    private Handler mWorkHandler;
    private HandlerThread mWorkLooper;

    private QAPM (Context context, String pid) {
        mContext = getSafeContext(context) ;
        setPid(pid);
        this.mWatchMan = new BackgroundTrace();
        initApplicationLifeObserver();
        mWorkLooper = new HandlerThread(QAPMConstant.THREAD_UPLOAD);
        mWorkLooper.start();
        mWorkHandler = new Handler(mWorkLooper.getLooper());
        registerActivityLifecycleCallbacks();
    }

    public static QAPM make(Context context, String pid) {
        if(pid == null || context == null){
            throw new IllegalArgumentException("pid || context is not null");
        }
        return makeQAPM(context, pid);
    }

    private static QAPM makeQAPM(Context context, String pid) {
        if (sInstance == null) {
            synchronized (QAPM.class) {
                if (sInstance == null) {
                    sInstance = new QAPM(context, pid);
                }
            }
        }
        return sInstance;
    }

    public QAPM setVid(String vid){
        QAPMConstant.vid = vid;
        return this;
    }

    public QAPM setPid(String pid){
        QAPMConstant.pid = pid;
        return this;
    }

    public QAPM setCid(String cid){
        QAPMConstant.cid = cid;
        return this;
    }

    public static QAPM getInstance(){
        return sInstance;
    }

    public static void addNetMonitor(Map<String, String> netMonitorMapData) {
        if (netMonitorMapData != null && netMonitorMapData.size() > 0) {
            BaseData netMonitorData = NetworkData.convertMap2BaseData(netMonitorMapData);
            Storage.newStorage().putData(netMonitorData);
        }
    }

    public static void addQunarMonitor(BaseData baseData) {
        if (baseData != null) {
            Storage.newStorage().putData(baseData);
        }
    }

    @Override
    public void setSender(ISender sender) {
        if (sender != null) {
            mSender = sender;
        }
    }

    @Override
    public ISender getSender(){
        if (mSender == null) {
            String requestId = (String) ReflectUtils.invokeStaticMethod("com.mqunar.qav.uelog.QAVLog", "getRequestId", null, null);
            if(isRelease){
                mSender = new QAPMSender(QAPMConstant.HOST_URL, "", requestId);
            } else {
                mSender = new QAPMSender(QAPMConstant.HOST_URL_BETA, QAPMConstant.PITCHER_URL, requestId);
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

    private void initApplicationLifeObserver() {
        if (ApplicationLifeObserver.getInstance() == null) {
            ApplicationLifeObserver.init((Application) mContext);
        }
        ApplicationLifeObserver.getInstance().initTracePlugin((Application) mContext);
    }

    private void unregisterActivityLifecycleCallbacks() {
        if (mContext != null && mContext instanceof Application && mWatchMan != null) {
            ((Application) mContext).unregisterActivityLifecycleCallbacks(mWatchMan);
            ((Application) mContext).unregisterActivityLifecycleCallbacks(ApplicationLifeObserver.getInstance());
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
        if(path == null){
            return null;
        }
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
                    Storage.newStorage().popData();
                }
                String path = IOUtils.getUploadDir(mContext);
                if(path != null){
                    String[] tempFileName = new File(path).list();
                    if (tempFileName != null && tempFileName.length > 0) {
                        getSender().send(mContext, path);
                    }
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
