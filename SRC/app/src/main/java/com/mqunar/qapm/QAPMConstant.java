package com.mqunar.qapm;

/**
 * Created by pengchengpc.liu on 2018/11/22.
 */
public class QAPMConstant {

    public static final String TRACE_ID = "L-Uuid"; //添加到请求头的附加参数

    // QAPM 自己上传路径，通过排除自己的上传路径来排除ANDROID_MONITOR监控请求数据
    public static String t = "http://l-wap6.wap.beta.cn6.qunar.com:9088/api/log/androidPerformanceLog";

    // id 相关， 只有pid为外部必须传入， 其他参数为可选择传入
    public static String pid = "";      // 去哪儿旅行客户端Id
    public static String vid = "";      // app版本号，如80011001  80开头是iphone，60开头是安卓
    public static String cid = "";      // 渠道号
    public static String uid = "";      // 设备唯一号，如android为手机串号，iphone为mac地址 ，ios7以后为iid

    //线上服务器地址
    public static final String HOST_URL = "http://mloganalysts.corp.qunar.com/api/log/unifiedLog";
    public static final String HOST_URL_BETA = "http://l-acra1.wap.beta.cn0.qunar.com:9099/api/log/unifiedLog";

    //线上Pitcher地址
    public static final String PITCHER_URL = "http://front.pitcher.beta.qunar.com/pitcher-proxy";

    //C参数
    public static final String C_PARAM = "";
    //requestId
    public static final String REQUEST_ID = "";

    // 上报数据的格式
    public static final String PLATFORM = "adr";        // 上传的时候的平台标识，adr：android 平台
    public static final String LOG_TYPE = "apm";        // 当前上传的内容标识符，apm：性能监控相关。
    public static final String LOG_NET_TYPE = "adrNet"; // 网络监控的Log_type
    public static final String LOG_UI_TYPE = "adrUI";   // 页面加载监控的Log_type

    // Thread 相关
    public static final String THREAD_UPLOAD = "QAPM-Thread-upload";
    public static final String THREAD_STORAGE = "QAPM-Thread-storage";

//    public static String pitcher = "http://front.pitcher.beta.qunar.com/pitcher-proxy";
//    public static String t = "http://l-client3.wap.beta.cn6.qunar.com:9088/api/log/androidPerformanceLog";
//    public static String t = "http://l-wap6.wap.beta.cn6.qunar.com:9088/api/log/androidPerformanceLog";
//    public static String t = "http://l-wappubsuport4.wap.dev.cn0.qunar.com:9088/api/log/androidMonitorLog";

//    日志服务地址 dev: http://l-wappubsuport4.wap.dev.cn0.qunar.com:9088/api/log/androidPerformanceLog
//    beta: http://l-wap6.wap.beta.cn6.qunar.com:9088/api/log/androidPerformanceLog
//    prod: http://mwhale.corp.qunar.com/api/log/androidPerformanceLog

}
