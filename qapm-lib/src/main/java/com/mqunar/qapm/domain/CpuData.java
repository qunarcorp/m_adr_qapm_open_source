package com.mqunar.qapm.domain;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: haonan.he ;<p/>
 * Date: 2019/2/1,4:46 PM ;<p/>
 * Description: ;<p/>
 * Other: ;
 */
public class CpuData extends BaseAPMData {

    public String logTime;

    public String currentProcess;//进程名
    public String usagRate;//当前进程占用cpu百分比

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", this.action);
            jsonObject.put("logTime", Long.toString(java.lang.System.currentTimeMillis()));
            jsonObject.put("extra", this.extra);
            jsonObject.put("currentProcess", this.currentProcess);
            jsonObject.put("usagRate", this.usagRate);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "CpuData{" +
                "action='" + action + '\'' +
                ", logTime='" + logTime + '\'' +
                ", currentProcess='" + currentProcess + '\'' +
                ", usagRate='" + usagRate + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }
}
