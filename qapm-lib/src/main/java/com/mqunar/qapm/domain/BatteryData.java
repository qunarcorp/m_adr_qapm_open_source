package com.mqunar.qapm.domain;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: haonan.he ;<p/>
 * Date: 2019/1/25,3:45 PM ;<p/>
 * Description: ;<p/>
 * Other: ;
 */
public class BatteryData extends BaseAPMData {

    public String action;
    public String logTime;

    public String currentBatteryRate;//电量
    public String isCharging;//是否是充电
    //public float batteryTemp;//电池温度

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", this.action);
            jsonObject.put("logTime", Long.toString(java.lang.System.currentTimeMillis()));
            jsonObject.put("currentBatteryRate", this.currentBatteryRate);
            jsonObject.put("isCharging", this.isCharging);
            jsonObject.put("extra", this.extra);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "BatteryData{" +
                "action='" + action + '\'' +
                ", logTime='" + logTime + '\'' +
                ", currentBatteryRate=" + currentBatteryRate +
                ", isCharging=" + isCharging +
                '}';
    }
}
