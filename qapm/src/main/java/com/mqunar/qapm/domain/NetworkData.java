package com.mqunar.qapm.domain;

import android.text.TextUtils;

import com.mqunar.qapm.QAPMConstant;
import com.mqunar.qapm.tracing.BackgroundTrace;
import com.mqunar.qapm.utils.AndroidUtils;

import java.util.HashMap;

/**
 * 网络数据的JavaBean
 * Created by pengchengpc.liu on 2018/11/22.
 */
public class NetworkData implements BaseData {

    //    public String loc;  // 经纬度，经度在前，纬度在后，如"123.3,23.2"
    //    public String mno; // 运营商，如"移动"，"联通"

    private static final long serialVersionUID = 1L;
    private static final int BACKGROUND_START_TIME = -1;

    public long startTimeInNano;
    public long endTimeInNano;

    public String action; // hyNet/rnNet/iosNet/adrNet
    public String reqUrl; //请求的url地址
    public String startTime; // 网络请求开始时的时间戳,精确到毫秒
    public String endTime; // 网络请求结束或者出错时的时间戳,精确到毫秒
    public String reqSize; // 网络请求大小，单位为*字节*
    public String resSize; // 收到的网络响应数据大小，单位为*字节*
    public String httpCode; // HTTP 请求的状态码，0表示正常，如“404”、“503”、“300”等
    public String hf; // http发生异常的原因,可选
    public String netType; // 发送网络请求时的网络类型，可选值为：“2G”、“3G”、“4G”、“Wifi”，“Cellular”，“Unknow”
    public String netStatus; // 请求成功或者失败;“success”( 络错误码在100~399); “error”(其它情 况);
    public String topPage; // 顶层页面，
    public Str;//网络错误类型


    public static final String ERROR_TYPE_BAD_URL = "badurl";//错误的url
    public static final String ERROR_TYPE_TIMEOUT = "timeout";//请求超时
    public static final String ERROR_TYPE_UNCONNECT = "unconnect";//无网或者没有网络权限
    public static final String ERROR_TYPE_HOST_ERR = "hostErr";//服务器错误，如请求被拒
    public static final String ERROR_TYPE_IO_ERR = "ioErr";//io错误
    public static final String ERROR_TYPE_SSL_ERR = "sslErr";//ssl错误


    public HashMap<String, String> headers = new HashMap<>();//过滤后的header


    /**
     * 排除不需要的图片相关的数据
     * @return 是否需要排除
     */
    public boolean excludeImageData() {
        if (TextUtils.isEmpty(reqUrl)) {
            return true;
        } else {
            if (reqUrl.contains(QAPMConstant.t)) {
                // 排除ANDROID_MONITOR监控请求数据
                return true;
            } else if (reqUrl.contains(".jpg")
                    || reqUrl.contains(".JPG")
                    || reqUrl.contains(".png")
                    || reqUrl.contains(".PNG")
                    || reqUrl.contains(".jpeg")
                    || reqUrl.contains(".JPEG")
                    || reqUrl.contains(".webp")
                    || reqUrl.contains(".WEBP")) {
                if (Integer.parseInt(endTime) - Integer.parseInt(startTime) > 2000) {
                    return false;
                }
                // 排除图片
                return true;
            }
        }
        return false;
    }

    /**
     * 排除用户取消请求的情况
     * @return 是否需要排除
     */
    public boolean excludeIllegalData(){
        long startTime = Long.parseLong(this.startTime);
        long endTime = Long.parseLong(this.endTime);
        return startTime == BACKGROUND_START_TIME ||
                BackgroundTrace.getBackgroundTime() > startTime && BackgroundTrace.getBackgroundTime() < endTime ||
                httpCode.equals(AndroidUtils.UNKNOWN) || netStatus == null;
    }


    @Override
    public String toString() {
        return "NetworkData{" + "action='" + action + '\'' +
                ", reqUrl='" + reqUrl + '\'' + ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' + ", reqSize='" + reqSize + '\'' +
                ", resSize='" + resSize + '\'' + ", httpCode='" + httpCode + '\'' +
                ", hf='" + hf + '\'' + ", netType='" + netType + '\'' +
                ", netStatus='" + netStatus + '\'' + ", topPage='" + topPage + '\'' + ", headers=" + headers +
                '}';
    }
}