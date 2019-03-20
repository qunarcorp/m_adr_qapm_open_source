package com.mqunar.qapm.schedule;

import android.os.Handler;
import android.os.HandlerThread;

public class LazyScheduler {

    private final long delay;
    private final Handler mHandler;
    private volatile boolean isSetUp = false;

    public LazyScheduler(HandlerThread handlerThread, long delay) {
        this.delay = delay;
        mHandler = new Handler(handlerThread.getLooper());
    }

    public boolean isSetUp() {
        return isSetUp;
    }

    public void setUp(final ILazyTask task, String key, boolean cycle) {
        if (null != mHandler) {
            this.isSetUp = true;
            mHandler.removeCallbacksAndMessages(null);
            RetryRunnable retryRunnable = new RetryRunnable(mHandler, key, delay, task, cycle);
            mHandler.postDelayed(retryRunnable, delay);
        }
    }

    public void cancel() {
        if (null != mHandler) {
            this.isSetUp = false;
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public void setOff() {
        cancel();
    }

    public interface ILazyTask {
        void onTimeExpire(String key);
    }

    static class RetryRunnable implements Runnable {
        private final Handler handler;
        private final long delay;
        private final ILazyTask lazyTask;
        private final boolean cycle;
        private final String mKey;

        RetryRunnable(Handler handler, String key, long delay, ILazyTask lazyTask, boolean cycle) {
            this.handler = handler;
            this.delay = delay;
            this.lazyTask = lazyTask;
            this.cycle = cycle;
            this.mKey = key;
        }

        @Override
        public void run() {
            lazyTask.onTimeExpire(this.mKey);
            if (cycle) {
                handler.postDelayed(this, delay);
            }
        }
    }

}
