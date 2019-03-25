package com.qunar.qapm.demo.sender;

import android.util.Log;

import com.mqunar.qapm.network.sender.ISender;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description: 性能监控发送默认代理;<p/>
 * Other: ;
 */
public class DemoSender implements ISender {

    private static final String TAG = "DemoSender";

    @Override
    public String getHostUrl() {
        //上传的服务器地址
        return "http://test.host.com/path";
    }

    @Override
    public void sendParamData(String bParam, String cParam, SenderListener senderListener) {
        try {
            JSONObject body = new JSONObject();
            body.put("b", bParam);
            body.put("c", cParam);
            Log.d(TAG, body.toString());
            senderListener.onSendDataSuccess();
        } catch (JSONException e) {
            e.printStackTrace();
            senderListener.onSendDataFail();
        }

    }

}
