package com.mqunar.atom.voice.myapplication;

import android.app.Application;

import com.mqunar.qapm.QAPM;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        QAPM.make(this, 10010);
        QAPM.getInstance().withLogEnabled(true);

    }


}
