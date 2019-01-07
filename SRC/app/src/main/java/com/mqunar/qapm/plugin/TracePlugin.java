package com.mqunar.qapm.plugin;

import android.app.Application;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.mqunar.qapm.tracing.FPSTracer;
import com.mqunar.qapm.tracing.FrameBeat;

public class TracePlugin extends Plugin {
    private static final String TAG = "TracePlugin";

    private FPSTracer mFPSTracer;

    public TracePlugin() {
    }

    @Override
    public void init(Application app) {
        super.init(app);
        Log.i(TAG, "trace plugin init");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            Log.e(TAG, String.format("[FrameBeat] API is low Build.VERSION_CODES.JELLY_BEAN(16), TracePlugin is not supported"));
            unSupportPlugin();
            return;
        }
        mFPSTracer = new FPSTracer(this);


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
