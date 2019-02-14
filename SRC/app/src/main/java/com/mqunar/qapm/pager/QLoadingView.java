package com.mqunar.qapm.pager;

import android.view.View;

import com.mqunar.qapm.domain.ActivityInfo;
import com.mqunar.qapm.domain.UIData;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.tracing.BackgroundTrace;
import com.mqunar.qapm.tracing.WatchMan;

import static com.mqunar.qapm.domain.UIData.MS_NS_UNIT;

/**
 * Created by dhc on 2018/4/27.
 * 当前类为Hook的原型类，这里上报相应的四个比较重要的时间戳，分别是
 *
 *      1、createTime ： Activity的创建的时间
 *      2、resumeTime：Activity的显示时间
 *      3、showTime ： 当前骆驼加载View显示出来的时间
 *      4、HiddeTime ：当前骆驼加载View隐藏的时间，当加载View隐藏也就代表这，当前的Load已经结束。
 *
 *      createTime ----- resumeTime ----- showTime(onVisible) ----- HideTime
 *
 * <p>
 * hook Qunar客户端的骆驼加载
 */

public class QLoadingView {

    public static void onVisibilityChanged(Object object, View changedView, int visibility) {
        if(object == null || !object.getClass().getName().equalsIgnoreCase("com.mqunar.framework.view.LoadingView")){
            return;
        }
        StringBuilder builder = new StringBuilder("QLoadingView.onVisibilityChanged, ");
        builder.append("object = ").append(object==null?"null!!":object.toString()).append(",changedView = ").append(changedView==null?"null!!":changedView.toString())
                .append(",visibility = ").append(visibility==View.VISIBLE?"View.VISIBLE":(visibility==View.INVISIBLE?"View.INVISIBLE":"View.GONE"));
         AgentLogManager.getAgentLog().info(builder.toString());
        //HyWebActivity会被重复使用
        if (WatchMan.sCurrentActivityName.contains("HyWebActivity") ||
                changedView.getClass().getName().equalsIgnoreCase("com.android.internal.policy.DecorView")) {
            // 屏幕整体的显示和隐藏 不用关心。 所以排除 com.android.internal.policy.DecorView 的回掉
            return;
        }
        if(changedView.getClass().getName().equalsIgnoreCase("com.mqunar.framework.view.stateview.LoadingContainer")){
            onLoadingContainerVisibilityChanged(object, changedView, visibility);
            return;
        }
        if (visibility == View.VISIBLE) {
            if(!WatchMan.sLoadingBeanMap.containsKey(object)){
                recordLoading(object);
            }
        } else if (visibility == View.INVISIBLE || visibility == View.GONE) {
            if(WatchMan.sLoadingBeanMap.containsKey(object)){
                writerLog(object, false);
            }
        }
    }

    private static void onLoadingContainerVisibilityChanged(Object object, View changedView, int visibility){
        if (visibility == View.VISIBLE) {
            if(!WatchMan.sLoadingBeanMap.containsKey(object)){
                recordLoading(object);
            }
        } else if (visibility == View.INVISIBLE || visibility == View.GONE){
            if(WatchMan.sLoadingBeanMap.containsKey(object)){
                writerLog(object, true);
            }
        }
    }

    private static void recordLoading(Object viewObject){
        if(!BackgroundTrace.appIsForeground()){
            return;
        }
        UIData loadingBean = new UIData();
        ActivityInfo info = WatchMan.sActivityInfos.get(WatchMan.sActivityInfos.size() -1);
        loadingBean.createTime = info.createTime;
        long duraTime = (info.firstResumedTimeInNano - info.creatTimeInNano) / MS_NS_UNIT;//转换成毫秒值
        loadingBean.resumeTime =  loadingBean.createTime + duraTime;
        loadingBean.showTime = System.currentTimeMillis();
        loadingBean.showTimeInNano = System.nanoTime();
        loadingBean.page = WatchMan.sCurrentActivityName.replaceAll("_", "—");
        WatchMan.sLoadingBeanMap.put(viewObject, loadingBean);
    }

    private static void writerLog(Object object, boolean isLoadingContainer){
        UIData uiData = WatchMan.sLoadingBeanMap.get(object);
        if(uiData == null){
            return ;
        }
        if(!isLoadingContainer && uiData.showTime - uiData.resumeTime > 5000){
            return;
        }
        long bTime = BackgroundTrace.getBackgroundTime();
        long duraTime = (System.nanoTime() - uiData.showTimeInNano) / MS_NS_UNIT;
        uiData.hiddenTime = uiData.showTime + duraTime;
        if(bTime != -1 && bTime > uiData.showTime && bTime < uiData.hiddenTime){
            return; // 代表，Activity 创建 - LoadingView 的 隐藏途中，用户将当前App有过切入后台的情况出现，所以此条数据存在不准确的情况，应该废弃掉
        }

        QLoadingReportHelper.newInstance().addReportMessage(uiData);
        AgentLogManager.getAgentLog().info("object ="+object.toString()+",showTime = "+uiData.showTime+",loading duraTime = ("+duraTime+")ms, new  loadingTime = (" + (uiData.hiddenTime - uiData.showTime) + ")  " +
                "createTime = (" + (uiData.resumeTime - uiData.createTime) + ")");
        WatchMan.sLoadingBeanMap.remove(object);
    }

}