package com.mqunar.qapm;

/**
 * Created by pengchengpc.liu on 2018/11/22.
 */
public class QAPMConstant {


    // id 相关， 只有pid为外部必须传入， 其他参数为可选择传入
    public static String pid = "";      // 去哪儿旅行客户端Id
    public static String vid = "";      // app版本号，如80011001  80开头是iphone，60开头是安卓
    public static String cid = "";      // 渠道号
    public static String uid = "";      // 设备唯一号，如android为手机串号，iphone为mac地址 ，ios7以后为iid

    //C参数
    public static final String C_PARAM = "";
    //requestId
    public static final String REQUEST_ID = "";

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
