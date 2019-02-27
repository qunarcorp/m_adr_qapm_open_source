package com.mqunar.qapm.network.sender;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.text.TextUtils;

import com.mqunar.qapm.QAPMConstant;
import com.mqunar.qapm.logging.AgentLog;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.utils.AndroidUtils;
import com.mqunar.qapm.utils.IOUtils;
import com.mqunar.qapm.utils.LocationUtils;
import com.mqunar.qapm.utils.NetWorkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Description: 性能监控发送默认代理;<p/>
 * Other: ;
 */
public class DefaultSender implements ISender {

    private static final AgentLog log = AgentLogManager.getAgentLog();

    private String mHostUrl;       // 线上服务器地址
    private String jsonData = null;     // 防止线程栈过大，将jsonData放在堆内存，并在失效后及时置为null

    public DefaultSender(String hostUrl) {
        this.mHostUrl = hostUrl;
    }

    @Override
    public String getHostUrl() {
        return mHostUrl;
    }

    @Override
    public void send(Context context, String filePath) {
        //1.没有网不处理
        if (!NetWorkUtils.isNetworkConnected(context)) {
            return;
        }
        //2.只过滤时间戳的文件
        String[] fileNames = new File(filePath).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.matches("[0-9]+");
            }
        });
        //3.先发送old 文件
        if (fileNames != null && fileNames.length > 0) {
            Arrays.sort(fileNames, new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    return lhs.compareTo(rhs);
                }
            });
            //发送文件
            for (String fileName : fileNames) {
                log.info("send apm data : " + fileName);
                sendFile(context, filePath + "/" + fileName);
            }
        }
    }


    private void sendFile(Context context, String fileName) {
        if (fileName != null) {
            jsonData = IOUtils.file2Str(fileName);
            if (jsonData == null) {
                return;
            }
            log.info("发送 b 参：" + jsonData);

            String param = getCParam(context);
            log.info("发送 c 参：" + param);

            doRequest(fileName, jsonData, param);
        }
    }

    private void doRequest(String fileName, String bParam, String cParam) {
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
                log.info("send apm file failed : " + "  resCode is: " + conn.getResponseCode());
            } else {
                deleteFile(new File(fileName));
            }
            jsonData = null;

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


    /**
     * 获取C参数，
     * 具体格式如下所示 ：
     * *vid:
     * *pid:
     * *cid:
     * uid:
     * osVersion: 系统版本
     * model: 机型
     * *loc：位置信息，获取不到传Unknown
     * mno：运营商信息，获取不到传Unknown
     * key: 时间戳
     * ext：{}扩展字段，目前没有
     *
     * @param context ：Android 上下文环境
     *
     * @return json格式的Cparam
     */
    @Override
    public String getCParam(Context context) {
        if (context == null) {
            return null;
        }
        JSONObject jobj = new JSONObject();
        try {
            String pkgName = context.getPackageName();
            String mon = AndroidUtils.carrierNameFromContext(context);
            String loc = getLocation(context);
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
            jobj.put("vid", !TextUtils.isEmpty(QAPMConstant.vid) ? QAPMConstant.vid :
                    packageInfo.versionCode + "");
            jobj.put("pid", !TextUtils.isEmpty(QAPMConstant.pid) ? QAPMConstant.pid : AndroidUtils.UNKNOWN);
            jobj.put("cid", !TextUtils.isEmpty(QAPMConstant.cid) ? QAPMConstant.cid : AndroidUtils.UNKNOWN);
            jobj.put("uid", !TextUtils.isEmpty(QAPMConstant.uid) ? QAPMConstant.uid :
                    AndroidUtils.getIMEI(context));
            jobj.put("osVersion", Build.VERSION.RELEASE + "_" + Build.VERSION.SDK_INT);
            jobj.put("model", Build.MODEL);
            jobj.put("loc", TextUtils.isEmpty(loc) ? AndroidUtils.UNKNOWN : loc);
            jobj.put("mno", TextUtils.isEmpty(mon) ? AndroidUtils.UNKNOWN : mon);
            jobj.put("key", String.valueOf(System.currentTimeMillis()));
            jobj.put("ext", ""); // 该字段先不支持
        } catch (Exception ignore) {
        }
        return jobj.toString();
    }


    private void deleteFile(File file) {
        if (file != null && file.exists()) {
            if (!file.delete()) {
                log.info("delete file failed :" + file.getName());
            }
        }
    }

    private String getLocation(Context context) {
        return LocationUtils.getLocation(context);
    }

}
