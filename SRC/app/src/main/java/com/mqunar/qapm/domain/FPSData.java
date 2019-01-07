package com.mqunar.qapm.domain;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * 帧率统计
 */
public class FPSData implements BaseData {
    private static final long serialVersionUID = 1L;
    public String action; // hyNet/rnNet/iosNet/adrNet
    public String scene;//当前场景
    public FPSLevel dropLevel;//sumTime时间段 丢帧评级占比
    public FPSLevel dropSum;//sumTime时间段 丢失的具体帧数
    public float fps;//平均帧率
    public int count;//绘制次数
    public int sumTime;//绘制总时间

    @Override
    public String toString() {
        return new StringBuilder()
                .append("FPSData:{")
                .append("action = '").append(action).append("\'").append(",")
                .append("scene = '").append(scene).append("\'").append(",")
                .append("dropLevel:{").append(dropLevel.toString()).append("}").append(",")
                .append("dropSum:{").append(dropSum.toString()).append("}").append(",")
                .append("fps = '").append(fps).append("\'").append(",")
                .append("count = '").append(count).append("\'").append(",")
                .append("sumTime = '").append(sumTime).append("\'").append("}")
                .toString();
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", this.action);
            jsonObject.put("scene", this.scene);
            jsonObject.put("dropLevel", this.dropLevel);
            jsonObject.put("dropSum", this.dropSum);
            jsonObject.put("fps", this.fps);
            jsonObject.put("count", this.count);
            jsonObject.put("sumTime", this.sumTime);
            return jsonObject;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static class FPSLevel implements BaseData {
        public int dropped_frozen;//<丢帧数>42
        public int dropped_high;//24<丢帧数<42
        public int dropped_middle;//9<丢帧数<24
        public int dropped_normal;//3<丢帧数<9
        public int dropped_best;//0<=丢帧数<3

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
