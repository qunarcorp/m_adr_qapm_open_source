package com.mqunar.tracing;

public class BackgroundTrace extends WatchMan {

    private static boolean sCurrentAppIsForeground = true;
    private static long sBackgroundTime ;
    private static long sForegroundTime ;

    @Override
    protected void onForegroundListener() {
        sCurrentAppIsForeground = true;
        sForegroundTime = System.currentTimeMillis();
    }

    @Override
    protected void onBackgroundListener() {
        sCurrentAppIsForeground = false;
        sBackgroundTime = System.currentTimeMillis();
    }

    public static long getBackgroundTime(){
        return sBackgroundTime;
    }

    public static long getForegronudTime(){
        return sForegroundTime;
    }

    public static boolean appIsForeground(){
        return sCurrentAppIsForeground;
    }
}
