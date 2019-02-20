package com.mqunar.qapm.dao;

import com.mqunar.qapm.QAPMConstant;
import com.mqunar.qapm.domain.BaseData;
import com.mqunar.qapm.domain.NetworkData;
import com.mqunar.qapm.logging.AgentLog;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.utils.AndroidUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
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
                jsonObject.put("action", QAPMConstant.LOG_NET_TYPE);
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

                jsonObject.put("startTimeInNano", data.startTimeInNano + "");
                jsonObject.put("endTimeInNano", data.endTimeInNano + "");
                jsonObject.put("errorType", data.errorType);
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

        /**
         * jsonObject.put("", data.startTimeInNano + "");
         jsonObject.put("endTimeInNano", data.endTimeInNano + "");
         jsonObject.put("errorType", data.errorType);
         */
//        networkData.startTimeInNano = data.get("startTimeInNano") != null ? Long.parseLong(data.get("startTimeInNano")) :
// TODO   这里需要重新约定一下;


        String headers = data.get("headers") != null ? data.get("headers") : AndroidUtils.UNKNOWN;
       if(headers != null){
            try {
                HashMap<String, String> headersMap = new HashMap<>();
                JSONObject jsonObject = new JSONObject(headers);
                if(jsonObject.get("Pitcher-Type") != null){
                    headersMap.put("Pitcher-Type",jsonObject.get("Pitcher-Type").toString());
                }
                if(jsonObject.get("Pitcher-Url") != null){
                    headersMap.put("Pitcher-Url",jsonObject.get("Pitcher-Url").toString());
                }
                if(jsonObject.get("L-Date") != null){
                    headersMap.put("L-Date",jsonObject.get("L-Date").toString());
                }
                if(jsonObject.get("User-Agent") != null){
                    headersMap.put("User-Agent",jsonObject.get("User-Agent").toString());
                }
                if(jsonObject.get("qrid") != null){
                    headersMap.put("qrid",jsonObject.get("qrid").toString());
                }
                if(jsonObject.get("L-Uuid") != null){
                    headersMap.put("L-Uuid",jsonObject.get("L-Uuid").toString());
                }
                networkData.headers = headersMap;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return networkData;
    }
}
