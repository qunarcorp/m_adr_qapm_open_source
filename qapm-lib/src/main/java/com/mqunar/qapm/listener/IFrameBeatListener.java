package com.mqunar.qapm.listener;

public interface IFrameBeatListener {
    void doFrame(long lastFrameNanos, long frameNanos);

    void cancelFrame();
}
