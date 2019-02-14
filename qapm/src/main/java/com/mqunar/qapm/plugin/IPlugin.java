package com.mqunar.qapm.plugin;

import android.app.Application;

public interface IPlugin {
    void init(Application application);

    void start();

    void stop();

    void destroy();

    String getTag();

    void onForeground(boolean isForeground);
}

