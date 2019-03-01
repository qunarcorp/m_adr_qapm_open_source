package com.mqunar.qapm.network.sender;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Description: 性能监控发送默认代理;<p/>
 * Other: ;
 */
public class DefaultSender implements ISender {

    private String mHostUrl;       // 线上服务器地址

    public DefaultSender(String hostUrl) {
        this.mHostUrl = hostUrl;
    }

    @Override
    public String getHostUrl() {
        return mHostUrl;
    }

    @Override
    public void sendParamData(String bParam, String cParam, SenderListener senderListener) {
        HttpURLConnection conn = null;
        try {
            JSONObject body = new JSONObject();
            body.put("c", bParam);
            body.put("b", cParam);
            URL url = new URL(mHostUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(80000);
            conn.setReadTimeout(80000);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            // 设置允许输出
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 设置contentType
            conn.setRequestProperty("Content-Type", "application/json");
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            String content = String.valueOf(body);
            os.writeBytes(content);
            os.flush();
            os.close();

            if (conn.getResponseCode() > 400) {
                senderListener.onSendDataFail();
            } else {
                senderListener.onSendDataSuccess();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

}
