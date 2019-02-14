package com.mqunar.qapm.plugin;

import android.app.Application;

public class Plugin implements IPlugin{
    private static final String TAG = "QAPM.Plugin";

    public static final int PLUGIN_CREATE    = 0x00;
    public static final int PLUGIN_INITED    = 0x01;
    public static final int PLUGIN_STARTED   = 0x02;
    public static final int PLUGIN_STOPPED   = 0x04;
    public static final int PLUGIN_DESTROYED = 0x08;

    private Application application;

    private boolean isSupported = true;

    private int status = PLUGIN_CREATE;
    @Override
    public void init(Application app) {
        if (application != null ) {
            throw new RuntimeException("plugin duplicate init, application or plugin listener is not null");
        }
        status = PLUGIN_INITED;
        this.application = app;
    }



    public Application getApplication() {
        return application;
    }

    @Override
    public void start() {
        if (isPluginDestroyed()) {
            throw new RuntimeException("plugin start, but plugin has been already destroyed");
        }

        if (isPluginStarted()) {
            throw new RuntimeException("plugin start, but plugin has been already started");
        }

        status = PLUGIN_STARTED;


    }

    @Override
    public void stop() {
        if (isPluginDestroyed()) {
            throw new RuntimeException("plugin stop, but plugin has been already destroyed");
        }

        if (!isPluginStarted()) {
            throw new RuntimeException("plugin stop, but plugin is never started");
        }

        status = PLUGIN_STOPPED;

    }

    @Override
    public void destroy() {
        // stop first
        if (isPluginStarted()) {
            stop();
        }
        if (isPluginDestroyed()) {
            throw new RuntimeException("plugin destroy, but plugin has been already destroyed");
        }
        status = PLUGIN_DESTROYED;

    }

    @Override
    public String getTag() {
        return getClass().getName();
    }

    @Override
    public void onForeground(boolean isForground) {

    }

    public int getStatus() {
        return status;
    }

    public boolean isPluginStarted() {
        return (status == PLUGIN_STARTED);
    }

    public boolean isPluginStoped() {
        return (status == PLUGIN_STOPPED);
    }

    public boolean isPluginDestroyed() {
        return (status == PLUGIN_DESTROYED);
    }

    public boolean isSupported() {
        return isSupported;
    }

    public void unSupportPlugin() {
        isSupported = false;
    }
}
