package com.qunar.moudle;

import android.app.Application;

import com.mqunar.qapm.QAPM;


/**
 * 测试Application
 */
public class TestApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        QAPM.make(this, "10010");
        QAPM.getInstance().withLogEnabled(true);

    }
}
