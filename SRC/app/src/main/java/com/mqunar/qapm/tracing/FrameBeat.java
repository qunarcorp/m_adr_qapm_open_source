package com.mqunar.qapm.tracing;

import android.app.Activity;
import android.app.Fragment;
import android.util.Log;
import android.view.Choreographer;

import com.mqunar.qapm.core.ApplicationLifeObserver;
import com.mqunar.qapm.listener.IFramBeat;
import com.mqunar.qapm.listener.IFrameBeatListener;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.utils.AndroidUtils;

import java.util.LinkedList;

public class FrameBeat implements IFramBeat, Choreographer.FrameCallback, ApplicationLifeObserver.IObserver {
    private static final String TAG = "FrameBeat";
    private static FrameBeat mInstance;
    private final LinkedList<IFrameBeatListener> mFrameListeners;
    private Choreographer mChoreographer;
    private boolean isCreated;
    private volatile boolean isPause = true;
    private long mLastFrameNanos;
    private static final long FRAME_INTERVAL_NANOS = (long) (1000000000 / 60.0);

    private FrameBeat() {
        mFrameListeners = new LinkedList<>();
    }


    public static FrameBeat getInstance() {
        if (null == mInstance) {
            mInstance = new FrameBeat();
        }
        return mInstance;
    }

    public boolean isPause() {
        return isPause;
    }

    public void pause() {
        if (!isCreated) {
            return;
        }
        isPause = true;
        if (null != mChoreographer) {
            mChoreographer.removeFrameCallback(this);
            mLastFrameNanos = 0;
            for (IFrameBeatListener listener : mFrameListeners) {
                listener.cancelFrame();
            }
        }
    }

    public void resume() {
        if (!isCreated) {
            return;
        }
        isPause = false;
        if (null != mChoreographer) {
            mChoreographer.removeFrameCallback(this);
            mChoreographer.postFrameCallback(this);
            mLastFrameNanos = 0;
        }
    }

    @Override
    public void onCreate() {
        if (!AndroidUtils.isInMainThread(Thread.currentThread().getId())) {
            AgentLogManager.getAgentLog().error( "[onCreate] FrameBeat must create on main thread");
            return;
        }
        AgentLogManager.getAgentLog().info("[onCreate] FrameBeat real onCreate!");
        if (!isCreated) {
            isCreated = true;
            ApplicationLifeObserver.getInstance().register(this);
            mChoreographer = Choreographer.getInstance();
            if (ApplicationLifeObserver.getInstance().isForeground()) {
                resume();
            }
        } else {
            AgentLogManager.getAgentLog().info("[onCreate] FrameBeat is created!");
        }
    }


    @Override
    public void onDestroy() {
        if (isCreated) {
            isCreated = false;
            if (null != mChoreographer) {
                mChoreographer.removeFrameCallback(this);
                for (IFrameBeatListener listener : mFrameListeners) {
                    listener.cancelFrame();
                }
            }
            mChoreographer = null;
            if (null != mFrameListeners) {
                mFrameListeners.clear();
            }
            ApplicationLifeObserver.getInstance().unregister(this);
        } else {
            AgentLogManager.getAgentLog().warning("[onDestroy] FrameBeat is not created!");
        }

    }

    @Override
    public void addListener(IFrameBeatListener listener) {
        if (null != mFrameListeners && !mFrameListeners.contains(listener)) {
            mFrameListeners.add(listener);
            if (isPause()) {
                resume();
            }
        }
    }

    @Override
    public void removeListener(IFrameBeatListener listener) {
        if (null != mFrameListeners) {
            mFrameListeners.remove(listener);
            if (mFrameListeners.isEmpty()) {
                pause();
            }
        }
    }

    /**
     * 垂直同步信号---vsync 到来时候调用
     *
     * @param frameTimeNanos 每一帧的渲染结束时间，以纳秒为单位
     */
    @Override
    public void doFrame(long frameTimeNanos) {
        if (isPause) {
            return;
        }
        if (frameTimeNanos < mLastFrameNanos || mLastFrameNanos <= 0) {
            mLastFrameNanos = frameTimeNanos;
            if (null != mChoreographer) {
                mChoreographer.postFrameCallback(this);
            }
            return;
        }

        if (null != mFrameListeners) {

            for (IFrameBeatListener listener : mFrameListeners) {
                listener.doFrame(mLastFrameNanos, frameTimeNanos);
            }

            if (null != mChoreographer) {
                mChoreographer.postFrameCallback(this);
            }
            final long jitterNanos = frameTimeNanos - mLastFrameNanos;
            if (jitterNanos >= FRAME_INTERVAL_NANOS) {
                final long skippedFrames = jitterNanos / FRAME_INTERVAL_NANOS;
                if (skippedFrames > 20) {
                    AgentLogManager.getAgentLog().info("Skipped " + skippedFrames + " frames!  "
                            + "The application may be doing too much work on its main thread.");
                }
            }
            mLastFrameNanos = frameTimeNanos;
        }

    }


    @Override
    public void onFront(Activity activity) {
        AgentLogManager.getAgentLog().info( String.format("[onFront] isCreated:%s postFrameCallback", isCreated));
        resume();
    }

    @Override
    public void onBackground(Activity activity) {
        AgentLogManager.getAgentLog().info( String.format("[onBackground] isCreated:%s removeFrameCallback", isCreated));
        pause();
    }

    @Override
    public void onChange(Activity activity, Fragment fragment) {
        AgentLogManager.getAgentLog().info( String.format("[onChange] resetIndex mLastFrameNanos, current activity:%s", activity.getClass().getSimpleName()));
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
