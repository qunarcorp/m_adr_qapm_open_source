package com.mqunar.qapm;

import android.app.Application;
import android.content.Context;

import com.mqunar.qapm.config.Config;
import com.mqunar.qapm.config.ConfigManager;
import com.mqunar.qapm.core.ApplicationLifeObserver;
import com.mqunar.qapm.dao.Storage;
import com.mqunar.qapm.domain.BaseData;
import com.mqunar.qapm.schedule.WorkHandlerManager;
import com.mqunar.qapm.tracing.BackgroundTrace;
import com.mqunar.qapm.tracing.WatchMan;

/**
 * QAPM的管理类
 */
public class QAPM implements IQAPM {

    private static QAPM sInstance = null;
    private static Context mContext;

    private WatchMan mWatchMan;

    public static QAPM getInstance() {
        return sInstance;
    }

    private QAPM(Context context, Config config) {
        mContext = getSafeContext(context);
        //交给ConfigManager管理
        ConfigManager.getInstance().setConfig(config);
        this.mWatchMan = new BackgroundTrace();
        //初始化工作线程
        WorkHandlerManager.getInstance().init();
        initApplicationLifeObserver();
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
        WorkHandlerManager.getInstance().quit();
        unregisterActivityLifecycleCallbacks();
    }

    public Context getContext() {
        return mContext;
    }

    private void registerActivityLifecycleCallbacks() {
        if(!(mContext instanceof Application)){
            throw new IllegalStateException("context is not instanceof Application!");

        }
        if (mWatchMan != null) {
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
        if(!(mContext instanceof Application)){
            throw new IllegalStateException("context is not instanceof Application!");

        }
        if (mWatchMan != null) {
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

}
