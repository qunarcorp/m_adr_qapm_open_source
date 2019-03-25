package com.qunar.qapm.demo;

import android.app.Application;

import com.mqunar.qapm.QAPM;
import com.mqunar.qapm.config.QAPMConfig;
import com.qunar.qapm.demo.sender.DemoSender;

/**
 * 测试Application
 */
public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //创建监控配置
        QAPMConfig.TraceConfig traceConfig = new QAPMConfig.TraceConfig();
        traceConfig.isUseTrace = true;//是否启用
        traceConfig.delayMillis = 5000;//监控间隔，默认1000毫秒，小于默认值无效
        //生成Builder
        QAPMConfig.ConfigBuilder builder = new QAPMConfig.ConfigBuilder()
                .setPid("10001")
                .setCid("C1234")
                .setVid("1.0.0")
                .setLogEnable(true)//配置是否输出log
                .setHostUrl("http://test.host.com/path")//配置上传地址
                .setSender(new DemoSender())//配置发送器，如果设置，优先使用自定义Sender内部的HostUrl与上传逻辑
                //.setFpsTraceConfig(traceConfig)//配置FPS监控，如果不设置，默认不开启
                .setCpuTraceConfig(traceConfig)//配置CPU监控，如果不设置，默认不开启
                .setMemoryTraceConfig(traceConfig)//配置内存监控，如果不设置，默认不开启
                .setBatteryTraceConfig(traceConfig);//配置电量监控，如果不设置，默认不开启
        //初始化
        QAPM.make(getApplicationContext(), builder.build());
    }
}
