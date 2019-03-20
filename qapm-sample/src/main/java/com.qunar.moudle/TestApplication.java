package com.qunar.moudle;

import android.app.Application;

import com.mqunar.qapm.QAPM;
import com.mqunar.qapm.config.Config;

/**
 * 测试Application
 */
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //创建配置
        Config.ConfigBuilder builder = new Config.ConfigBuilder().setPid("10001")
                .setCid("C1234").setVid("1.0.0").setLogEnable(true)
                .setHostUrl("http://test.host.com/path").setSender(null);

        //初始化
        QAPM.make(getApplicationContext(), builder.build());
    }
}
