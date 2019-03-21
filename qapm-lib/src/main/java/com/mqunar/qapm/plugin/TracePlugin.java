package com.mqunar.qapm.plugin;

import android.app.Application;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.mqunar.qapm.config.QConfigManager;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.tracing.BatteryTracer;
import com.mqunar.qapm.tracing.CpuTracer;
import com.mqunar.qapm.tracing.FPSTracer;
import com.mqunar.qapm.tracing.FrameBeat;
import com.mqunar.qapm.tracing.MemoryTracer;

public class TracePlugin extends Plugin {
    private static final String TAG = "TracePlugin";

    private FPSTracer mFPSTracer;

    public TracePlugin() {
    }

    @Override
    public void init(Application app) {
        super.init(app);
        AgentLogManager.getAgentLog().info("trace plugin init");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            AgentLogManager.getAgentLog().info(String.format("[FrameBeat] API is low Build.VERSION_CODES" +
                    ".JELLY_BEAN(16), TracePlugin is not supported"));
            unSupportPlugin();
            return;
        }

        if (QConfigManager.getInstance().isUseFpsTrace()) {
            mFPSTracer = new FPSTracer(this, QConfigManager.getInstance().getFpsTraceInterval());
        }
        //内存
        if (QConfigManager.getInstance().isUseMemoryTrace()) {
            MemoryTracer.getInstance().start(getApplication().getApplicationContext());
        }
        //电量
        if (QConfigManager.getInstance().isUseBatteryTrace()) {
            BatteryTracer.getInstance().start(getApplication().getApplicationContext());
        }

        //cpu
        if (QConfigManager.getInstance().isUseCpuTrace()) {
            CpuTracer.getInstance().start(getApplication().getApplicationContext());
        }


    }

    @Override
    public void start() {
        super.start();
        if (!isSupported()) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                FrameBeat.getInstance().onCreate();
            }
        });

        if (null != mFPSTracer) {
            mFPSTracer.onCreate();
        }


    }

    @Override
    public void stop() {
        super.stop();
        if (!isSupported()) {
            return;
        }
        FrameBeat.getInstance().onDestroy();
        if (null != mFPSTracer) {
            mFPSTracer.onDestroy();
        }

        MemoryTracer.getInstance().stop();
        BatteryTracer.getInstance().stop();
        CpuTracer.getInstance().stop();

    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public String getTag() {
        return TAG;
    }


    public FPSTracer getFPSTracer() {
        return mFPSTracer;
    }
}
