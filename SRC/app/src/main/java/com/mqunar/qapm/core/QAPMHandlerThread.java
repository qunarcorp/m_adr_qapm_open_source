package com.mqunar.qapm.core;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.util.HashSet;
import java.util.Iterator;

public class QAPMHandlerThread {
    private static final String TAG = "QAPMHandlerThread";

    public static final String QAPM_THREAD_NAME = "default_qapm_thread";


    private static volatile HandlerThread defaultHandlerThread;
    private static volatile Handler defaultHandler;
    private static volatile Handler defaultMainHandler = new Handler(Looper.getMainLooper());
    private static HashSet<HandlerThread> handlerThreads = new HashSet<>();

    public static Handler getDefaultMainHandler() {
        return defaultMainHandler;
    }

    public static HandlerThread getDefaultHandlerThread() {

        synchronized (QAPMHandlerThread.class) {
            if (null == defaultHandlerThread) {
                defaultHandlerThread = new HandlerThread(QAPM_THREAD_NAME);
                defaultHandlerThread.start();
                defaultHandler = new Handler(defaultHandlerThread.getLooper());
                Log.w(TAG, "create default handler thread, we should use these thread normal");
            }
            return defaultHandlerThread;
        }
    }

    public static Handler getDefaultHandler() {
        return defaultHandler;
    }

    public static HandlerThread getNewHandlerThread(String name) {
        for (Iterator<HandlerThread> i = handlerThreads.iterator(); i.hasNext(); ) {
            HandlerThread element = i.next();
            if (!element.isAlive()) {
                i.remove();
                Log.w(TAG, String.format("warning: remove dead handler thread with name %s", name));
            }
        }
        HandlerThread handlerThread = new HandlerThread(name);
        handlerThread.start();
        handlerThreads.add(handlerThread);
        Log.w(TAG, String.format("warning: create new handler thread with name %s, alive thread size:%d", name, handlerThreads.size()));
        return handlerThread;
    }
}
