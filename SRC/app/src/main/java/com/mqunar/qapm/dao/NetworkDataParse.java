package com.mqunar.qapm.dao;

import com.mqunar.qapm.domain.BaseData;
import com.mqunar.qapm.domain.NetworkData;
import com.mqunar.qapm.logging.AgentLog;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.utils.AndroidUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class NetworkDataParse implements IDataParse{

    private static final AgentLog log = AgentLogManager.getAgentLog();
    private static NetworkDataParse sInstance = null;

    private NetworkDataParse(){}

    public static NetworkDataParse newInstance(){
        if (sInstance == null) {
            synchronized (NetworkDataParse.class) {
                if (sInstance == null) {
                    sInstance = new NetworkDataParse();
                }
            }
        }
        return sInstance;
    }

    @Override
    public String convertBaseData2Json(List<BaseData> data) {
        JSONArray array =  new JSONArray();
        for (BaseData networkData : data) {
            JSONObject networkDataJson = convertImplData2Json(networkData);
            if(networkDataJson != null){
                array.put(networkDataJson);
            }
        }
        return array.toString();
    }

    @Override
    public JSONObject convertImplData2Json(BaseData baseData) {
        try {
            if(baseData instanceof NetworkData){
                NetworkData data = (NetworkData) baseData;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("action", data.action);
                jsonObject.put("reqUrl", data.reqUrl);
                jsonObject.put("startTime", data.startTime);
                jsonObject.put("endTime", data.endTime);
                jsonObject.put("reqSize", data.reqSize);
                jsonObject.put("resSize", data.resSize);
                jsonObject.put("httpCode", data.httpCode);
                jsonObject.put("hf", data.hf);
                jsonObject.put("netType", data.netType);
                jsonObject.put("netStatus", data.netStatus);
                jsonObject.put("topPage", data.topPage);
                // 添加header
                JSONObject headerJsonObject = new JSONObject();
                if (data.headers != null && data.headers.size() > 0) {
                    for (Map.Entry<String, String> entry : data.headers.entrySet()) {
                        headerJsonObject.put(entry.getKey(), entry.getValue());
                    }
                }
                if (headerJsonObject.length() != 0) {
                    jsonObject.put("header", headerJsonObject);
                }
                return jsonObject;
            }
        } catch (JSONException e) {
            log.error("convertNetworkData2Json failed : " + e.toString());
        }
        return null;
    }


    /**
     * \
     public String ; // hyNet/rnNet/iosNet/adrNet
     public String ; //请求的url地址
     public String ; // 网络请求开始时的时间戳,精确到毫秒
     public String ; // 网络请求结束或者出错时的时间戳,精确到毫秒
     public String ; // 网络请求大小，单位为*字节*
     public String ; // 收到的网络响应数据大小，单位为*字节*
     public String ; // HTTP 请求的状态码，0表示正常，如“404”、“503”、“300”等
     public String ; // http发生异常的原因,可选
     public String ; // 发送网络请求时的网络类型，可选值为：“2G”、“3G”、“4G”、“Wifi”，“Cellular”，“Unknow”
     public String ; // 请求成功或者失败;“success”( 络错误码在100~399); “error”(其它情 况);
     public String ; // 顶层页面，

     public HashMap<String, String>  = new HashMap<>();//过滤后的header
     * @param data 要转换的数据
     * @return
     */
    @Override
    public BaseData convertMap2BaseData(Map<String, String> data) {
        NetworkData networkData = new NetworkData();
        networkData.action = data.get("action") != null ? data.get("action") : AndroidUtils.UNKNOWN;
        networkData.reqUrl = data.get("reqUrl") != null ? data.get("reqUrl") : AndroidUtils.UNKNOWN;
        networkData.startTime = data.get("startTime") != null ? data.get("startTime") : AndroidUtils.UNKNOWN;
        networkData.endTime = data.get("endTime") != null ? data.get("endTime") : AndroidUtils.UNKNOWN;
        networkData.reqSize = data.get("reqSize") != null ? data.get("reqSize") : AndroidUtils.UNKNOWN;
        networkData.resSize = data.get("resSize") != null ? data.get("resSize") : AndroidUtils.UNKNOWN;
        networkData.httpCode = data.get("httpCode") != null ? data.get("httpCode") : AndroidUtils.UNKNOWN;
        networkData.hf = data.get("hf") != null ? data.get("hf") : AndroidUtils.UNKNOWN;
        networkData.netType = data.get("netType") != null ? data.get("netType") : AndroidUtils.UNKNOWN;
        networkData.netStatus = data.get("netStatus") != null ? data.get("netStatus") : AndroidUtils.UNKNOWN;
        networkData.topPage = data.get("topPage") != null ? data.get("topPage") : AndroidUtils.UNKNOWN;
        String headers = data.get("headers") != null ? data.get("headers") : AndroidUtils.UNKNOWN;
//        if(headers != null){
//            try {
//                JSONArray array = new JSONArray(headers);
//                for (int i = 0; i < array.length(); i++) {
//                    if(array.getJSONObject(i) != null){
//                        array.getJSONObject(i).getJSONObject
//                    }
//                    networkData.headers.put()
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
        return networkData;
    }
}
