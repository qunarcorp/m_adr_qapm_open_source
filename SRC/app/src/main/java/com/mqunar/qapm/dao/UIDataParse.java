package com.mqunar.qapm.dao;

import com.mqunar.qapm.QAPMConstant;
import com.mqunar.qapm.domain.BaseData;
import com.mqunar.qapm.domain.UIData;
import com.mqunar.qapm.logging.AgentLog;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.utils.AndroidUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by pengchengpc.liu on 2018/11/22.
 *
 * 页面性能的解析器，统一接口由IDataParse负责
 *
 */

public class UIDataParse implements IDataParse{

    private static final AgentLog log = AgentLogManager.getAgentLog();
    private static final long ILLEGAL_DATA = -10000;
    private static UIDataParse sInstance = null;

    private UIDataParse(){}

    public static UIDataParse newInstance(){
        if (sInstance == null) {
            synchronized (UIDataParse.class) {
                if (sInstance == null) {
                    sInstance = new UIDataParse();
                }
            }
        }
        return sInstance;
    }

    @Override
    public String convertBaseData2Json(List<BaseData> data) {
        JSONArray array =  new JSONArray();
        for (BaseData uiData : data) {
            JSONObject uiDataJson = convertImplData2Json(uiData);
            if(uiDataJson != null){
                array.put(uiDataJson);
            }
        }
        return array.toString();
    }

    @Override
    public JSONObject convertImplData2Json(BaseData baseData) {
        try {
            if(baseData instanceof UIData){
                UIData data = (UIData) baseData;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("action", QAPMConstant.LOG_UI_TYPE);
                jsonObject.put("page", data.page);
                jsonObject.put("createTime", data.createTime != ILLEGAL_DATA ? data.createTime : AndroidUtils.UNKNOWN);
                jsonObject.put("resumeTime", data.resumeTime != ILLEGAL_DATA ? data.resumeTime : AndroidUtils.UNKNOWN);
                jsonObject.put("hiddenTime", data.hiddenTime != ILLEGAL_DATA ? data.hiddenTime : AndroidUtils.UNKNOWN);
                jsonObject.put("showTime", data.showTime != ILLEGAL_DATA ? data.showTime : AndroidUtils.UNKNOWN);
                jsonObject.put("status", data.status);
                jsonObject.put("netType", data.netType);
                return jsonObject;
            }
        } catch (JSONException e) {
            log.error("convertNetworkData2Json failed : " + e.toString());
        }
        return null;
    }

    @Override
    public BaseData convertMap2BaseData(Map<String, String> data) {
        UIData uiData = new UIData();
        uiData.action = data.get("action") != null ? data.get("action") : AndroidUtils.UNKNOWN;
        uiData.page = data.get("page") != null ? data.get("page") : AndroidUtils.UNKNOWN;
        uiData.createTime = data.get("createTime") != null ? Long.parseLong(data.get("createTime")) : ILLEGAL_DATA;
        uiData.resumeTime = data.get("resumeTime") != null ? Long.parseLong(data.get("resumeTime")) : ILLEGAL_DATA;
        uiData.hiddenTime = data.get("hiddenTime") != null ? Long.parseLong(data.get("hiddenTime")) : ILLEGAL_DATA;
        uiData.showTime = data.get("hiddenTime") != null ? Long.parseLong(data.get("hiddenTime")) : ILLEGAL_DATA;
        uiData.status = data.get("status") != null ? data.get("status") : AndroidUtils.UNKNOWN;
        uiData.netType = data.get("netType") != null ? data.get("netType") : AndroidUtils.UNKNOWN;
        return uiData;
    }
}
