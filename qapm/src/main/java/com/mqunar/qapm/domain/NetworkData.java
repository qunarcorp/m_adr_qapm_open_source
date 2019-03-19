package com.mqunar.qapm.domain;

import android.text.TextUtils;

import com.mqunar.qapm.QAPMConstant;
import com.mqunar.qapm.config.ConfigManager;
import com.mqunar.qapm.tracing.BackgroundTrace;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 网络数据的JavaBean
 * Created by pengchengpc.liu on 2018/11/22.
 */
public class NetworkData implements BaseData {

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
    public String errorType;//网络错误类型

    public HashMap<String, String> headers = new HashMap<>();//过滤后的header

    /**
     * 排除不需要的图片相关的数据
     * @return 是否需要排除 true 排除掉 false 保留数据
     */
    public boolean excludeImageData() {
        if (TextUtils.isEmpty(reqUrl)) {
            return true;
        } else {
            // 排除ANDROID_MONITOR监控请求数据
            return reqUrl.contains(ConfigManager.getInstance().getHostUrl());
        }
    }

    /**
     * 排除用户取消请求的情况
     *
     * @return 是否需要排除 true 排除掉 false 保留数据
     */
    public boolean excludeIllegalData() {
        return startTimeInNano == BACKGROUND_START_TIME ||
                (BackgroundTrace.getBackgroundTime() > startTimeInNano && BackgroundTrace.getBackgroundTime() < endTimeInNano);
    }


    @Override
    public JSONObject toJSONObject() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", QAPMConstant.LOG_NET_TYPE);
            jsonObject.put("reqUrl", this.reqUrl);
            jsonObject.put("startTime", this.startTime);
            jsonObject.put("endTime", this.endTime);
            jsonObject.put("reqSize", this.reqSize);
            jsonObject.put("resSize", this.resSize);
            jsonObject.put("httpCode", this.httpCode);
            jsonObject.put("hf", this.hf);
            jsonObject.put("errorType", this.errorType);
            jsonObject.put("netType", this.netType);
            jsonObject.put("netStatus", this.netStatus);
            jsonObject.put("topPage", this.topPage);
            // 添加header
            JSONObject headerJsonObject = new JSONObject();
            if (this.headers != null && this.headers.size() > 0) {
                for (Map.Entry<String, String> entry : this.headers.entrySet()) {
                    headerJsonObject.put(entry.getKey(), entry.getValue());
                }
            }
            if (headerJsonObject.length() != 0) {
                jsonObject.put("header", headerJsonObject);
            }
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
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