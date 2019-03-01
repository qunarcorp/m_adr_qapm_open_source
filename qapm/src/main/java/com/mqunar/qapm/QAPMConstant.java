package com.mqunar.qapm;

/**
 * Created by pengchengpc.liu on 2018/11/22.
 */
public class QAPMConstant {

    // 上报数据的格式
    public static final String PLATFORM = "adr";        // 上传的时候的平台标识，adr：android 平台
    public static final String LOG_TYPE = "apm";        // 当前上传的内容标识符，apm：性能监控相关。
    public static final String LOG_NET_TYPE = "adrNet"; // 网络监控的Log_type
    public static final String LOG_FPS_TYPE = "fps";    // 页面帧率监控的log_type

    // Thread 相关
    public static final String THREAD_UPLOAD = "QAPM-Thread-upload";
    public static final String THREAD_STORAGE = "QAPM-Thread-storage";


    public static final float DEFAULT_DEVICE_REFRESH_RATE = 16.666667f;
    public static final int TIME_MILLIS_TO_NANO = 1000000;
    public static final int DEFAULT_DROPPED_NORMAL = 3;
    public static final int DEFAULT_DROPPED_MIDDLE = 9;
    public static final int DEFAULT_DROPPED_HIGH = 24;
    public static final int DEFAULT_DROPPED_FROZEN = 42;

}
