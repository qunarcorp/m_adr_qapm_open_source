package com.mqunar.qapm.tracing;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.mqunar.qapm.QAPM;
import com.mqunar.qapm.domain.ActivityInfo;
import com.mqunar.qapm.domain.UIData;
import com.mqunar.qapm.pager.QLoadingReportHelper;
import com.mqunar.qapm.utils.AndroidUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by chaos on 16/3/8.
 */
public abstract class WatchMan implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = WatchMan.class.getSimpleName();
    private int mLifeCount = 0;

    private WeakHashMap<Activity, Handler> mWeakHandlerMap;
    public static WeakHashMap<Object, UIData> sLoadingBeanMap = new WeakHashMap<>();
    public static List<ActivityInfo> sActivityInfos = new LinkedList<>();
    private boolean mIsFirstActivityStart = true;
    private boolean mIsFirstActivityStop = true;
    private boolean isBackToDesktop = false;

    public static String sCurrentActivityName = null;

    private String mFirstQavStartActivityName = null;
    private String currentActivityName;

    private Handler myHandler;

    @Override
    public void onActivityCreated(final Activity activity, Bundle savedInstanceState) {
        recordCreateTime(activity.toString());
        if (myHandler == null) {
            myHandler = new Handler(Looper.getMainLooper());
        }
        if (mWeakHandlerMap == null) {
            mWeakHandlerMap = new WeakHashMap<>();
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        //记录第一次开始的Activity
        if (mIsFirstActivityStop && mIsFirstActivityStart) {
            mIsFirstActivityStart = false;
            //第一个Activity记录名字
            mFirstQavStartActivityName = activity.getClass().getSimpleName();
        }
        mLifeCount = mLifeCount + 1;
        if (isBackToDesktop && mLifeCount == 1) {
            isBackToDesktop = false;
            //从后台切回来了
            onForegroundListener();
        }
    }

    @Override
    public void onActivityResumed(final Activity activity) {
        final String activityName = AndroidUtils.getPageName(activity);
        sCurrentActivityName = activityName;
        recordResumedTime(activity.toString());
        if (activityName.equals(currentActivityName)) {
            return;
        }
        if (myHandler == null) {
            myHandler = new Handler(Looper.getMainLooper());
        }
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                View peekDecorView = activity.getWindow().peekDecorView();
                if (peekDecorView != null) {
                    final String pageName = AndroidUtils.getPageName(activity);
                    currentActivityName = activityName;
                }
            }
        });
        upload(false);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d("onActivityPaused(%s)", activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        //如果是第一次Stop,并且未有经历Start或者非第一个Activity的Stop，则补上Start
        QLoadingReportHelper.newInstance().saveReportMessage();
        if (mIsFirstActivityStop) {
            mIsFirstActivityStop = false;
            if (!activity.getClass().getSimpleName().equals(mFirstQavStartActivityName)) {
                mLifeCount++;
            }
            mFirstQavStartActivityName = null;
        }
        mLifeCount = mLifeCount - 1;
        if (mLifeCount < 0) {
            mLifeCount = 0;
        } else if (mLifeCount == 0) {
            //到后台了
            onBackgroundListener();
            Log.d(TAG, "[事件-后台]捕获到应用切换到后台的事件!");
            if (!isBackToDesktop) {
                Log.d(TAG, "强制上传QAV日志");
                isBackToDesktop = true;
                upload(true);
            }
        } else {
            upload(false);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {
        recordDestroyedTime(activity.toString());
        if (mWeakHandlerMap != null) {
            Handler gidHandler = mWeakHandlerMap.get(activity);
            if (gidHandler != null) {
                gidHandler.removeCallbacksAndMessages(null);
                mWeakHandlerMap.remove(activity);
            }

        }
    }

    public static String getCurrentActivityName(){
        if(TextUtils.isEmpty(sCurrentActivityName)){
            sCurrentActivityName = AndroidUtils.UNKNOWN;
        }
        return sCurrentActivityName;
    }

    private void upload(boolean isforceSend){
        QAPM instance = QAPM.getInstance();
        if(instance != null){
            instance.upload(isforceSend);
        }
    }

    private void recordCreateTime(String activityName){
        ActivityInfo activityInfo = new ActivityInfo();
        activityInfo.activityName = activityName;
        activityInfo.createTime = System.currentTimeMillis();
        activityInfo.creatTimeInNano = System.nanoTime();
        sActivityInfos.add(activityInfo);
    }

    private void recordResumedTime(String activityName){
        for (ActivityInfo activityInfo : sActivityInfos){
            if(activityInfo.activityName.equalsIgnoreCase(activityName) && activityInfo.isFirstResumed){
                activityInfo.firstResumedTimeInNano = System.nanoTime();
                activityInfo.firstResumedTime = System.currentTimeMillis();
                activityInfo.isFirstResumed = false;
            }
        }
    }

    private void recordDestroyedTime(String activityName) {
        int removeIndex = -1 ;
        for (ActivityInfo activityInfo : sActivityInfos){
            if(activityInfo.activityName.equalsIgnoreCase(activityName)){
                removeIndex = sActivityInfos.indexOf(activityInfo);
            }
        }
        if(removeIndex != -1){
            sActivityInfos.remove(removeIndex);
        }
    }


    protected abstract void onForegroundListener();
    protected abstract void onBackgroundListener();
}