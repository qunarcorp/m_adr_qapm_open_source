package com.mqunar.qapm.core;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.mqunar.qapm.plugin.TracePlugin;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

public class ApplicationLifeObserver implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "ApplicationLifeObserver";
    private static final long CHECK_DELAY = 600;
    private static ApplicationLifeObserver mInstance;
    private LinkedList<IObserver> mObservers;
    private Handler mMainHandler;
    private boolean mIsPaused, mIsForeground;
    private String mCurActivityHash;
    private Runnable mCheckRunnable;
    private TracePlugin tracePlugin;

    public boolean isForeground() {
        return mIsForeground;
    }

    private ApplicationLifeObserver(final Application application) {
        if (application == null) {
            return;
        }
        application.unregisterActivityLifecycleCallbacks(this);
        application.registerActivityLifecycleCallbacks(this);

        mObservers = new LinkedList<>();
        mMainHandler = new Handler(Looper.getMainLooper());

    }


    public void initTracePlugin(Application application) {
        if (tracePlugin == null) {
            tracePlugin = new TracePlugin();
            tracePlugin.init(application);
            tracePlugin.start();
        }

    }

    public void onDestroy() {
        if (tracePlugin != null) {
            tracePlugin.stop();
        }

    }

    public static void init(Application application) {
        if (null == mInstance) {
            mInstance = new ApplicationLifeObserver(application);
        }
    }

    public static ApplicationLifeObserver getInstance() {
        return mInstance;
    }

    public void register(IObserver observer) {
        if (null != mObservers) {
            mObservers.add(observer);
        }
    }

    public void unregister(IObserver observer) {
        if (null != mObservers) {
            mObservers.remove(observer);
        }
    }

    @Override
    public void onActivityResumed(final Activity activity) {
        for (IObserver listener : mObservers) {
            listener.onActivityResume(activity);
        }
        mIsPaused = false;
        final boolean wasBackground = !mIsForeground;
        mIsForeground = true;
        final String activityHash = getActivityHash(activity);

        if (!activityHash.equals(mCurActivityHash)) {
            for (IObserver listener : mObservers) {
                listener.onChange(activity, null);
            }
            mCurActivityHash = activityHash;
        }
        final WeakReference<Activity> mActivityWeakReference = new WeakReference<>(activity);
        mMainHandler.postDelayed(mCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (wasBackground) {
                    Activity ac = mActivityWeakReference.get();
                    if (null == ac) {
                        Log.w(TAG, "onFront ac is null!");
                        return;
                    }
                    for (IObserver listener : mObservers) {
                        listener.onFront(activity);
                    }
                }
            }
        }, CHECK_DELAY);
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        for (IObserver listener : mObservers) {
            listener.onActivityPause(activity);
        }
        mIsPaused = true;
        if (mCheckRunnable != null) {
            mMainHandler.removeCallbacks(mCheckRunnable);
        }

        final WeakReference<Activity> mActivityWeakReference = new WeakReference<>(activity);
        mMainHandler.postDelayed(mCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (mIsForeground && mIsPaused) {
                    mIsForeground = false;
                    Activity ac = mActivityWeakReference.get();
                    if (null == ac) {
                        Log.w(TAG, "onBackground ac is null!");
                        return;
                    }
                    Log.w(TAG, "onBackground ");
                    for (IObserver listener : mObservers) {
                        listener.onBackground(ac);
                    }
                }
            }
        }, CHECK_DELAY);
    }

    @Override
    public void onActivityCreated(final Activity activity, Bundle savedInstanceState) {
        for (IObserver listener : mObservers) {
            listener.onActivityCreated(activity);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        for (IObserver listener : mObservers) {
            listener.onActivityStarted(activity);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.i(TAG,"onActivityStopped");
        for (IObserver listener : mObservers) {
            listener.onActivityStopped(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(final Activity activity) {
        Log.i(TAG,"onActivityDestroyed");
        if (getActivityHash(activity).equals(mCurActivityHash)) {
            mCurActivityHash = null;
        }
    }

    public interface IObserver {
        void onFront(Activity activity);

        void onBackground(Activity activity);

        void onChange(Activity activity, Fragment fragment);

        void onActivityCreated(final Activity activity);

        void onActivityPause(final Activity activity);

        void onActivityResume(final Activity activity);

        void onActivityStarted(final Activity activity);

        void onActivityStopped(Activity activity);
    }

    private String getActivityHash(Activity activity) {
        return activity.getClass().getName() + activity.hashCode();
    }


}
