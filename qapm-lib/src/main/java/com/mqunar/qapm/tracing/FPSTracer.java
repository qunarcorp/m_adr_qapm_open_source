package com.mqunar.qapm.tracing;

import android.app.Activity;
import android.view.ViewTreeObserver;

import com.mqunar.qapm.QAPM;
import com.mqunar.qapm.QAPMConstant;
import com.mqunar.qapm.core.ApplicationLifeObserver;
import com.mqunar.qapm.core.QAPMHandlerThread;
import com.mqunar.qapm.dao.Storage;
import com.mqunar.qapm.domain.BaseData;
import com.mqunar.qapm.domain.FPSData;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.plugin.TracePlugin;
import com.mqunar.qapm.schedule.LazyScheduler;
import com.mqunar.qapm.utils.AndroidUtils;

import java.util.HashMap;
import java.util.LinkedList;

public class FPSTracer extends BaseTracer implements LazyScheduler.ILazyTask,
        ViewTreeObserver.OnDrawListener {

    private static final String TAG = "FPSTracer";

    private static final int OFFSET_TO_MS = 100;
    private static final int FACTOR = QAPMConstant.TIME_MILLIS_TO_NANO / OFFSET_TO_MS;
    private boolean isDrawing = false;
    private boolean isInvalid = false;
    private HashMap<String, LinkedList<Integer>> mReportMap;
    private LazyScheduler mLazyScheduler;
    private long mFpsInterval;

    public FPSTracer(TracePlugin plugin, long fpsInterval) {
        super(plugin);
        this.mFpsInterval = fpsInterval < 1000 ? 1000 : fpsInterval;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mLazyScheduler = new LazyScheduler(QAPMHandlerThread.getDefaultHandlerThread(), mFpsInterval);
        if (ApplicationLifeObserver.getInstance().isForeground()) {
            onFront(null);
        }
    }

    @Override
    public void onActivityCreated(Activity activity) {
        super.onActivityCreated(activity);
    }

    /**
     * 应用回到前台被调用
     *
     * @param activity
     */
    @Override
    public void onFront(Activity activity) {
        super.onFront(activity);
        //          if (null != mLazyScheduler) {
        //              mLazyScheduler.cancel();
        //             this.mLazyScheduler.setUp(this, true);
        //         }
    }

    /**
     * 应用退回后台被调用
     *
     * @param activity
     */
    @Override
    public void onBackground(Activity activity) {
        super.onBackground(activity);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onDraw() {
        isDrawing = true;
    }

    @Override
    protected String getTag() {
        return TAG;
    }


    @Override
    public void onTimeExpire(String key) {
        doReport(key);
    }


    @Override
    public void doFrame(long lastFrameNanos, long frameNanos) {
        if (!isInvalid) {
            handleDoFrame(lastFrameNanos, frameNanos, getScene());
        }
        isDrawing = false;
    }

    @Override
    public void onActivityResume(final Activity activity) {
        super.onActivityResume(activity);
        this.isInvalid = false;
        addDrawListener(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (null != mLazyScheduler) {
            // mLazyScheduler.cancel();
            this.mLazyScheduler.setUp(this, AndroidUtils.getSceneForString(activity, null), false);
        }
    }

    @Override
    public void onActivityPause(Activity activity) {
        super.onActivityPause(activity);
        removeDrawListener(activity);
        this.isInvalid = true;
    }

    private void addDrawListener(final Activity activity) {
        activity.getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                activity.getWindow().getDecorView().getViewTreeObserver().removeOnDrawListener(FPSTracer.this);
                activity.getWindow().getDecorView().getViewTreeObserver().addOnDrawListener(FPSTracer.this);
            }
        });
    }

    private void removeDrawListener(final Activity activity) {
        activity.getWindow().getDecorView().getViewTreeObserver().removeOnDrawListener(FPSTracer.this);
    }


    private void handleDoFrame(long lastFrameNanos, long frameNanos, String scene) {


        if (mReportMap == null) {
            mReportMap = new HashMap<>();
        }

        if (mReportMap.get(scene) == null) {
            LinkedList<Integer> datas = new LinkedList<>();
            mReportMap.put(scene, datas);
        }


        int offset = (int) (frameNanos - lastFrameNanos) / FACTOR;//纳秒值除以10000 也就是100倍的毫秒 防止装不下
        synchronized (this.getClass()) {
            mReportMap.get(scene).add(offset);//放的是两次绘制时间间隔
        }


    }

    /**
     * report FPS
     */
    private void doReport(String scene) {
        LinkedList<Integer> reportList;
        synchronized (this.getClass()) {
            if (mReportMap == null || mReportMap.get(scene) == null || mReportMap.get(scene).isEmpty()) {
                return;
            }
            reportList = new LinkedList<>();
            reportList.addAll(mReportMap.get(scene));
            mReportMap.get(scene).clear();
        }

        int sumTime = 0;//得到的数据单位是毫秒的100倍
        int count = 0;
        int[] dropLevel = new int[DropStatus.values().length]; // record the level of frames dropped each time
        int[] dropSum = new int[DropStatus.values().length]; // record the sum of frames dropped each time
        int refreshRate = (int) QAPMConstant.DEFAULT_DEVICE_REFRESH_RATE * OFFSET_TO_MS;
        for (int i = 0; i < reportList.size(); i++) {
            Integer period = reportList.get(i);
            sumTime += period;
            count++;
            int tmp = period / refreshRate - 1;
            if (tmp >= QAPMConstant.DEFAULT_DROPPED_FROZEN) {
                dropLevel[DropStatus.DROPPED_FROZEN.index]++;
                dropSum[DropStatus.DROPPED_FROZEN.index] += tmp;
            } else if (tmp >= QAPMConstant.DEFAULT_DROPPED_HIGH) {
                dropLevel[DropStatus.DROPPED_HIGH.index]++;
                dropSum[DropStatus.DROPPED_HIGH.index] += tmp;
            } else if (tmp >= QAPMConstant.DEFAULT_DROPPED_MIDDLE) {
                dropLevel[DropStatus.DROPPED_MIDDLE.index]++;
                dropSum[DropStatus.DROPPED_MIDDLE.index] += tmp;
            } else if (tmp >= QAPMConstant.DEFAULT_DROPPED_NORMAL) {
                dropLevel[DropStatus.DROPPED_NORMAL.index]++;
                dropSum[DropStatus.DROPPED_NORMAL.index] += tmp;
            } else {
                dropLevel[DropStatus.DROPPED_BEST.index]++;
                dropSum[DropStatus.DROPPED_BEST.index] += (tmp < 0 ? 0 : tmp);
            }

        }


        int fps = (int) Math.min(60.f, 1000.f * OFFSET_TO_MS * (count) / sumTime);

        try {

            FPSData fpsData = new FPSData();

            FPSData.FPSLevel fpsLevel = new FPSData.FPSLevel();
            fpsLevel.dropped_best = String.valueOf(dropLevel[DropStatus.DROPPED_BEST.index]);
            fpsLevel.dropped_normal = String.valueOf(dropLevel[DropStatus.DROPPED_NORMAL.index]);
            fpsLevel.dropped_middle = String.valueOf(dropLevel[DropStatus.DROPPED_MIDDLE.index]);
            fpsLevel.dropped_high = String.valueOf(dropLevel[DropStatus.DROPPED_HIGH.index]);
            fpsLevel.dropped_frozen = String.valueOf(dropLevel[DropStatus.DROPPED_FROZEN.index]);

            FPSData.FPSLevel fpsSum = new FPSData.FPSLevel();
            fpsSum.dropped_best = String.valueOf(dropSum[DropStatus.DROPPED_BEST.index]);
            fpsSum.dropped_normal = String.valueOf(dropSum[DropStatus.DROPPED_NORMAL.index]);
            fpsSum.dropped_middle = String.valueOf(dropSum[DropStatus.DROPPED_MIDDLE.index]);
            fpsSum.dropped_high = String.valueOf(dropSum[DropStatus.DROPPED_HIGH.index]);
            fpsSum.dropped_frozen = String.valueOf(dropSum[DropStatus.DROPPED_FROZEN.index]);

            fpsData.dropLevel = fpsLevel;
            fpsData.dropSum = fpsSum;
            fpsData.count = String.valueOf(count);
            fpsData.fps = String.valueOf(fps);
            fpsData.sumTime = String.valueOf(sumTime / OFFSET_TO_MS);
            fpsData.page = scene;
            fpsData.action = QAPMConstant.LOG_FPS_TYPE;
            fpsData.statisticsTime = String.valueOf(System.currentTimeMillis());

            AgentLogManager.getAgentLog().info(fpsData.toString());
            sendReport(fpsData);
        } catch (Exception e) {
            AgentLogManager.getAgentLog().error(String.format("json error , error :%s", e.getMessage()));
        }
        AgentLogManager.getAgentLog().info(String.format("scene:%s count: %s average_fps:%s sumTime:%s ms",
                scene, count, fps, sumTime / OFFSET_TO_MS));

    }

    @Override
    protected void sendReport(BaseData baseData) {
        FPSData fpsData = (FPSData) baseData;
        Storage.newStorage(QAPM.getInstance().getContext()).putData(fpsData);
    }

    private enum DropStatus {
        DROPPED_FROZEN(4), DROPPED_HIGH(3), DROPPED_MIDDLE(2), DROPPED_NORMAL(1), DROPPED_BEST(0);
        int index;

        DropStatus(int index) {
            this.index = index;
        }

    }
}
