package com.qunar.moudle;

import android.app.Application;

import com.mqunar.qapm.QAPM;
import com.mqunar.qapm.config.QAPMConfig;

/**
 * 测试Application
 */
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //创建配置
        QAPMConfig.TraceConfig traceConfig = new QAPMConfig.TraceConfig();
        traceConfig.isUseTrace = true;
        traceConfig.delayMillis = 1000;
        QAPMConfig.ConfigBuilder builder = new QAPMConfig.ConfigBuilder().setPid("10001")
                .setCid("C1234").setVid("1.0.0").setLogEnable(true)
                .setHostUrl("http://test.host.com/path").setSender(null)
                .setFpsTraceConfig(traceConfig)
                .setCpuTraceConfig(traceConfig)
                .setMemoryTraceConfig(traceConfig)
                .setBatteryTraceConfig(traceConfig);

        //初始化
        QAPM.make(getApplicationContext(), builder.build());
    }
}
