package com.mqunar.qapm.tracing;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.os.Debug;

import com.mqunar.qapm.QAPMConstant;
import com.mqunar.qapm.config.QConfigManager;
import com.mqunar.qapm.core.ApplicationLifeObserver;
import com.mqunar.qapm.dao.Storage;
import com.mqunar.qapm.domain.MemoryData;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.schedule.AsyncExecutor;
import com.mqunar.qapm.utils.ProcessUtils;

import java.text.NumberFormat;

/**
 * 内存收集处理类
 */
public class MemoryTracer implements ApplicationLifeObserver.IObserver {

    private static final String TAG = "MemoryTracer";
    private long DELAY_MILLIS;
    private static final float B2KB = 1024;
    private boolean mIsStart = false;
    private boolean mIsCanWork = true;
    private Context mContext;

    private static MemoryTracer instance;

    private MemoryTracer() {
    }

    public static MemoryTracer getInstance() {
        if (instance == null) {
            synchronized (MemoryTracer.class) {
                if (instance == null) {
                    instance = new MemoryTracer();
                }
            }
        }
        return instance;
    }

    //定时任务
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mIsCanWork) {
                MemoryData memoryData = getMemoryInfo();
                if (null != memoryData) {
                    Storage.newStorage(mContext).putData(memoryData);
                }
                AsyncExecutor.executeDelayed(runnable, DELAY_MILLIS);
            }
        }
    };

    private void init(Context context) {
        mContext = context;
        DELAY_MILLIS = QConfigManager.getInstance().getMemoryTraceInterval();
    }

    /**
     * 获取当前内存信息
     */
    private MemoryData getMemoryInfo() {
        // 注意：这里是耗时和耗CPU的操作，一定要谨慎调用
        //获取使用情况
        Debug.MemoryInfo info = new Debug.MemoryInfo();
        Debug.getMemoryInfo(info);
        //获取设备内存信息
        ActivityManager activityManager =
                (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo acmMemoryInfo = new ActivityManager.MemoryInfo();
        if (activityManager != null) {
            activityManager.getMemoryInfo(acmMemoryInfo);
            //封装数据
            MemoryData memoryData = new MemoryData();
            memoryData.action = QAPMConstant.LOG_MEMORY_TYPE;
            memoryData.currentProcessName = ProcessUtils.getCurrentProcessName();
            memoryData.system = new MemoryData.System();
            memoryData.system.used =
                    String.valueOf((int) ((acmMemoryInfo.totalMem - acmMemoryInfo.availMem) / B2KB));
            memoryData.system.free = String.valueOf((int) (acmMemoryInfo.availMem / B2KB));//B转KB
            memoryData.system.total = String.valueOf((int) (acmMemoryInfo.totalMem / B2KB));
            memoryData.currentProcess = new MemoryData.CurrentProcess();
            memoryData.currentProcess.max = String.valueOf((int) (Runtime.getRuntime().maxMemory() / B2KB));
            memoryData.currentProcess.used = String.valueOf(info.getTotalPss());//单位kb
            memoryData.currentProcess.usedRate = String.valueOf(getPercent(info.getTotalPss(),
                    acmMemoryInfo.totalMem * 1.0f / B2KB));
            return memoryData;
        } else {
            AgentLogManager.getAgentLog().error(TAG + "getMemoryInfo is null");
            return null;
        }
    }

    private String getPercent(float num1, float num2) {
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat.format(num1 / num2 * 100) + "%";
    }

    public void start(Context context) {
        if (!mIsStart) {
            init(context);
            AsyncExecutor.executeDelayed(runnable, 0);
            mIsStart = true;
            mIsCanWork = true;
        }
    }

    public void stop() {
        mIsCanWork = false;
        mIsStart = false;
    }

    @Override
    public void onFront(Activity activity) {

    }

    @Override
    public void onBackground(Activity activity) {

    }

    @Override
    public void onChange(Activity activity, Fragment fragment) {

    }

    @Override
    public void onActivityCreated(Activity activity) {

    }

    @Override
    public void onActivityPause(Activity activity) {

    }

    @Override
    public void onActivityResume(Activity activity) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }
}