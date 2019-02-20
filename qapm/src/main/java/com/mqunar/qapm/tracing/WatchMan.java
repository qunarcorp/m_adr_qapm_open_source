package com.mqunar.qapm.tracing;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;

import com.mqunar.qapm.QAPM;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.utils.AndroidUtils;

/**
 * Created by chaos on 16/3/8.
 */
public abstract class WatchMan implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = WatchMan.class.getSimpleName();

    private boolean mIsFirstActivityStart = true;
    private boolean mIsFirstActivityStop = true;
    private boolean isBackToDesktop = false;

    private static String sCurrentActivityName = null;

    private String mFirstQavStartActivityName = null;

    private int mLifeCount = 0;

    @Override
    public void onActivityCreated(final Activity activity, Bundle savedInstanceState) {
        AgentLogManager.getAgentLog().debug("onActivityCreated(" + activity.getClass().getSimpleName() + ")");
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
        sCurrentActivityName = AndroidUtils.getPageName(activity);
        upload(false);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        AgentLogManager.getAgentLog().debug("onActivityPaused(" + activity.getClass().getSimpleName() + ")");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        //如果是第一次Stop,并且未有经历Start或者非第一个Activity的Stop，则补上Start
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
            AgentLogManager.getAgentLog().debug("[事件-后台]捕获到应用切换到后台的事件!");
            if (!isBackToDesktop) {
                AgentLogManager.getAgentLog().debug("强制上传QAV日志");
                isBackToDesktop = true;
                upload(true);
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        AgentLogManager.getAgentLog().debug("onActivitySaveInstanceState(" + activity.getClass().getSimpleName() + ")");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        AgentLogManager.getAgentLog().debug("onActivityDestroyed(" + activity.getClass().getSimpleName() + ")");
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

    protected abstract void onForegroundListener();
    protected abstract void onBackgroundListener();
}