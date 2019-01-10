package com.mqunar.qapm.domain;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * 帧率统计
 */
public class FPSData extends BaseAPMData {
    private static final long serialVersionUID = 1L;
    public String action; // hyNet/rnNet/iosNet/adrNet
    public String page;//当前场景
    public FPSLevel dropLevel;//sumTime时间段 丢帧评级占比
    public FPSLevel dropSum;//sumTime时间段 丢失的具体帧数
    public String fps;//平均帧率
    public String count;//绘制次数
    public String sumTime;//绘制总时间
    public String statisticsTime;//统计时间

    @Override
    public String toString() {
        return new StringBuilder()
                .append("FPSData:{")
                .append("action = '").append(action).append("\'").append(",")
                .append("page = '").append(page).append("\'").append(",")
                .append("dropLevel:{").append(dropLevel.toString()).append("}").append(",")
                .append("dropSum:{").append(dropSum.toString()).append("}").append(",")
                .append("fps = '").append(fps).append("\'").append(",")
                .append("count = '").append(count).append("\'").append(",")
                .append("sumTime = '").append(sumTime).append("\'").append(",")
                .append("logTime = '").append(statisticsTime).append("\'").append("}")
                .toString();
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", this.action);
            jsonObject.put("scene", this.page);
            jsonObject.put("dropLevel", this.dropLevel.toJSONObject());
            jsonObject.put("dropSum", this.dropSum.toJSONObject());
            jsonObject.put("fps", this.fps);
            jsonObject.put("count", this.count);
            jsonObject.put("sumTime", this.sumTime);
            jsonObject.put("logTime", this.statisticsTime);
            return jsonObject;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static class FPSLevel implements BaseData {
        public String dropped_frozen;//<丢帧数>42
        public String dropped_high;//24<丢帧数<42
        public String dropped_middle;//9<丢帧数<24
        public String dropped_normal;//3<丢帧数<9
        public String dropped_best;//0<=丢帧数<3

        public JSONObject toJSONObject() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("frozen", this.dropped_frozen);
                jsonObject.put("high", this.dropped_high);
                jsonObject.put("middle", this.dropped_middle);
                jsonObject.put("normal", this.dropped_normal);
                jsonObject.put("best", this.dropped_best);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        public String toString() {
            return new StringBuilder().append("dropped_frozen = '").append(dropped_frozen).append("\'").append(",")
                    .append("dropped_high = '").append(dropped_high).append("\'").append(",")
                    .append("dropped_middle = '").append(dropped_middle).append("\'").append(",")
                    .append("dropped_normal = '").append(dropped_normal).append("\'").append(",")
                    .append("dropped_best = '").append(dropped_best).append("\'").toString();
        }
    }

}
