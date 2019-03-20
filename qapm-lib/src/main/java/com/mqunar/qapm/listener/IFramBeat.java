package com.mqunar.qapm.listener;

public interface IFramBeat {
    void onCreate();

    void onDestroy();

    void addListener(IFrameBeatListener listener);

    void removeListener(IFrameBeatListener listener);
}
