package com.mqunar.qapm.tracing;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.mqunar.qapm.QAPMConstant;
import com.mqunar.qapm.core.ApplicationLifeObserver;
import com.mqunar.qapm.dao.Storage;
import com.mqunar.qapm.domain.BatteryData;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.schedule.AsyncExecutor;

/**
 * Author: haonan.he ;<p/>
 * Date: 2019/1/25,4:43 PM ;<p/>
 * Description: ;<p/>
 * Other: ;
 */
public class BatteryTracer implements ApplicationLifeObserver.IObserver{

    private static final String TAG = "BatteryTracer";
    private static final int DELAY_MILLIS = 60 * 1000;

    private boolean mIsStart = false;
    private boolean mIsCanWork = true;
    private Context mContext;

    private static BatteryTracer instance;

    private BatteryTracer() {
    }

    public static BatteryTracer getInstance() {
        if (instance == null) {
            synchronized (BatteryTracer.class) {
                if (instance == null) {
                    instance = new BatteryTracer();
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
                BatteryData batteryData = getBatteryInfo();
                if (null != batteryData) {
                    Storage.newStorage().putData(batteryData);
                }
                AsyncExecutor.executeDelayed(runnable, DELAY_MILLIS);
            }
        }
    };

    public void init(Context context) {
        mContext = context;
    }

    /**
     * 获取当前内存信息
     */
    private BatteryData getBatteryInfo() {
        Intent intent = new ContextWrapper(mContext).registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = 0;
        if (intent != null) {
            BatteryData batteryData = new BatteryData();
            status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            int level = (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) /
                    intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            //温度
            //String temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) * 0.1f + "°C";
            batteryData.action = QAPMConstant.LOG_BATTERY_TYPE;
            batteryData.isCharging = Boolean.toString(isCharging);
            batteryData.currentBatteryRate = Integer.toString(level);
            return batteryData;
        } else {
            AgentLogManager.getAgentLog().error(TAG + "getBatteryInfo is null");
            return null;
        }
    }


    public void start(Context context) {
        mContext = context;
        if (!mIsStart) {
            AsyncExecutor.executeDelayed(runnable, 0);
            mIsStart = true;
            mIsCanWork = true;
        }
    }

    public void stop() {
        mIsCanWork = false;
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
