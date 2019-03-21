package com.mqunar.qapm.tracing;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;

import com.mqunar.qapm.QAPMConstant;
import com.mqunar.qapm.config.QConfigManager;
import com.mqunar.qapm.core.ApplicationLifeObserver;
import com.mqunar.qapm.dao.Storage;
import com.mqunar.qapm.domain.CpuData;
import com.mqunar.qapm.schedule.AsyncExecutor;
import com.mqunar.qapm.utils.CpuUtils;
import com.mqunar.qapm.utils.ProcessUtils;

import java.util.List;

/**
 * Author: haonan.he ;<p/>
 * Date: 2019/1/25,4:43 PM ;<p/>
 * Description: ;<p/>
 * Other: ;
 */
public class CpuTracer implements ApplicationLifeObserver.IObserver {

    private static final String TAG = "CpuTracer";
    private long DELAY_MILLIS;

    private boolean mIsStart = false;
    private boolean mIsCanWork = true;
    private Context mContext;
    private CpuUtils mCpuUtils;

    private static CpuTracer instance;

    private CpuTracer() {
    }

    public static CpuTracer getInstance() {
        if (instance == null) {
            synchronized (CpuTracer.class) {
                if (instance == null) {
                    instance = new CpuTracer();
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
                CpuData cpuData = getCpuUtils();
                if (null != cpuData) {
                    Storage.newStorage().putData(cpuData);
                }
                AsyncExecutor.executeDelayed(runnable, DELAY_MILLIS);
            }
        }
    };

    private void init(Context context) {
        mContext = context;
        mCpuUtils = new CpuUtils(context);
        DELAY_MILLIS = QConfigManager.getInstance().getCpuTraceInterval();
    }

    /**
     * 获取cpu信息
     *
     * @return
     */
    private CpuData getCpuUtils() {
        //Log.d(TAG, "name: " + mCpuUtils.getCpuName());//cpu名称
        //Log.d(TAG, "num: " + mCpuUtils.getCpuNum());//个数

        List<String> ratioInfo = mCpuUtils.getCpuRatioInfo();
        if (ratioInfo != null && ratioInfo.size() > 0) {
            CpuData cpuData = new CpuData();
            if (!ratioInfo.get(0).equals("0") & !ratioInfo.get(0).equals("0.00")) {
                //当前进程cpu使用率
                cpuData.usagRate = ratioInfo.get(0);
                //ratioInfo.get(1)是cpu总使用率
            } else {//计算需要两次时间对比，可能为0
                Log.d(TAG, "getCpuRatioInfo is zero");
                cpuData.usagRate = "-1";//获取失败传-1
            }
            cpuData.action = QAPMConstant.LOG_CPU_TYPE;
            cpuData.currentProcess = ProcessUtils.getCurrentProcessName();
            return cpuData;
        } else {
            Log.d(TAG, "getCpuRatioInfo is null");
            return null;
        }
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
