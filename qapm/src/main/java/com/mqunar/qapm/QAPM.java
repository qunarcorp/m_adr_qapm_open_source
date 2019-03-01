package com.mqunar.qapm;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.mqunar.qapm.config.Config;
import com.mqunar.qapm.config.ConfigManager;
import com.mqunar.qapm.core.ApplicationLifeObserver;
import com.mqunar.qapm.dao.Storage;
import com.mqunar.qapm.domain.BaseData;
import com.mqunar.qapm.logging.AgentLog;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.network.sender.ISender;
import com.mqunar.qapm.tracing.BackgroundTrace;
import com.mqunar.qapm.tracing.WatchMan;
import com.mqunar.qapm.utils.AndroidUtils;
import com.mqunar.qapm.utils.IOUtils;
import com.mqunar.qapm.utils.NetWorkUtils;

/**
 * QAPM的管理类
 */
public class QAPM implements IQAPM {

    private static QAPM sInstance = null;
    private static Context mContext;

    private WatchMan mWatchMan;
    private Handler mWorkHandler;
    private HandlerThread mWorkLooper;

    private final AgentLog mLog = AgentLogManager.getAgentLog();

    public static QAPM getInstance() {
        return sInstance;
    }

    private QAPM(Context context, Config config) {
        mContext = getSafeContext(context);
        //交给ConfigManager管理
        ConfigManager.getInstance().setConfig(config);
        this.mWatchMan = new BackgroundTrace();
        initApplicationLifeObserver();
        mWorkLooper = new HandlerThread(QAPMConstant.THREAD_UPLOAD);
        mWorkLooper.start();
        mWorkHandler = new Handler(mWorkLooper.getLooper());
        registerActivityLifecycleCallbacks();
    }

    public static QAPM make(Context context, Config config) {
        if (context == null) {
            throw new IllegalArgumentException("context is not null");
        }
        return makeQAPM(context, config);
    }

    private static QAPM makeQAPM(Context context, Config config) {
        if (sInstance == null) {
            synchronized (QAPM.class) {
                if (sInstance == null) {
                    sInstance = new QAPM(context, config);
                }
            }
        }
        return sInstance;
    }

    @Override
    public void addCustomMonitor(BaseData baseData) {
        if (baseData != null) {
            Storage.newStorage().putData(baseData);
        }
    }

    @Override
    public void release() {
        if (mWorkLooper != null) {
            mWorkLooper.quit();
        }
        unregisterActivityLifecycleCallbacks();
    }

    public Context getContext() {
        return mContext;
    }

    private void registerActivityLifecycleCallbacks() {
        if (mContext != null && mContext instanceof Application && mWatchMan != null) {
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


    public void upload(final boolean isForceSend) {
        //防止主线程调用引起ANR
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!NetWorkUtils.isNetworkConnected(mContext)) {// 没有网络先不处理
                    return;
                }
                if (isForceSend) {
                    Storage.newStorage().popData();
                }
                String uploadDir = IOUtils.getUploadDir(mContext);
                if (uploadDir != null) {
                    String[] uploadFiles = IOUtils.getFileByNameFilter(uploadDir);
                    if (uploadFiles != null && uploadFiles.length > 0) {
                        for (final String fileName : uploadFiles) {
                            String bParam = IOUtils.file2Str(fileName);
                            String cParam = AndroidUtils.getCParam(mContext);
                            ConfigManager.getInstance().getSender().sendParamData(bParam, cParam,
                                    new ISender.SenderListener() {
                                        @Override
                                        public void onSendDataSuccess() {
                                            mLog.info("uploadFile onSendDataSuccess=" + fileName);
                                            IOUtils.deleteFile(fileName);
                                        }

                                        @Override
                                        public void onSendDataFail() {
                                            mLog.info("uploadFile onSendDataFail=" + fileName);
                                        }
                                    });
                        }
                    } else {
                        mLog.info("uploadFiles is null");
                    }
                } else {
                    mLog.info("uploadDir is null");
                }
            }
        });
    }

}
