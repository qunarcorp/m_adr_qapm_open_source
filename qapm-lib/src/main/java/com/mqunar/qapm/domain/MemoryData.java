package com.mqunar.qapm.domain;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 内存
 */
public class MemoryData extends BaseAPMData {

    public String action;
    public String logTime;
    //public String extra;

    public String currentProcessName;
    public System system;
    public CurrentProcess currentProcess;

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", this.action);
            jsonObject.put("currentProcessName", this.currentProcessName);
            jsonObject.put("system", this.system.toJSONObject());
            jsonObject.put("currentProcess", this.currentProcess.toJSONObject());
            jsonObject.put("logTime", Long.toString(java.lang.System.currentTimeMillis()));
            jsonObject.put("extra", this.extra);
            return jsonObject;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class System implements BaseData {
        public String used;  //设备已使用内存 单位K
        public String free;  //设备剩余内存 单位K
        public String total;  //设备总内存 单位K

        public JSONObject toJSONObject() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("used", this.used);
                jsonObject.put("free", this.free);
                jsonObject.put("total", this.total);
                return jsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public String toString() {
            return "System{" +
                    "used='" + used + '\'' +
                    ", free='" + free + '\'' +
                    ", total='" + total + '\'' +
                    '}';
        }
    }

    public static class CurrentProcess implements BaseData {
        public String max;  //当前应用分配最大的内存  单位K
        public String used;  //当前应用占用内存  单位K
        public String usedRate;//当前应用占用的内存比  单位%

        public JSONObject toJSONObject() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("max", this.max);
                jsonObject.put("used", this.used);
                jsonObject.put("usedRate", this.usedRate);
                return jsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public String toString() {
            return "CurrentProcess{" +
                    "max='" + max + '\'' +
                    ", used='" + used + '\'' +
                    ", usedRate='" + usedRate + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "MemoryData{" +
                "action='" + action + '\'' +
                ", currentProcessName='" + currentProcessName + '\'' +
                ", system=" + system +
                ", currentProcess=" + currentProcess +
                ", logTime='" + logTime + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }


}