package com.mqunar.qapm.network.sender;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.mqunar.qapm.QAPMConstant;
import com.mqunar.qapm.logging.AgentLog;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.utils.AndroidUtils;
import com.mqunar.qapm.utils.IOUtils;
import com.mqunar.qapm.utils.LocationUtils;
import com.mqunar.qapm.utils.NetWorkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import qunar.lego.utils.FormPart;
import qunar.lego.utils.HttpHeader;
import qunar.lego.utils.Pitcher;
import qunar.lego.utils.PitcherResponse;

/**
 * Created by pengchengpc.liu on 2018/11/22.
 * <p>
 * 性能监控发送代理
 */
public class QAPMSender implements ISender {

    private static final AgentLog log = AgentLogManager.getAgentLog();

    private String mHostUrl = "";       // 线上服务器地址
    private String mPitcherUrl = "";    // 线上Pitcher地址
    private String mRequestId = "";     // requestId
    private String jsonData = null;     // 防止线程栈过大，将jsonData放在堆内存，并在失效后及时置为null

    public QAPMSender(String hostUrl, String pitcherUrl, String requestId) {
        this.mHostUrl = hostUrl;
        this.mPitcherUrl = pitcherUrl;
        this.mRequestId = requestId;
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


    private void sendFile(Context context, String fileName){
        if (fileName != null) {
            jsonData = IOUtils.file2Str(fileName);
            if (jsonData == null) {
                return;
            }
            log.info("发送 b 参：" + jsonData);
            final HttpHeader reqHeader = new HttpHeader();
            if (!TextUtils.isEmpty(mRequestId)) {
                reqHeader.addHeader("qrid", mRequestId);
            }
            String param = getCParam(context);
            ArrayList<FormPart> formParts = getFormParts(jsonData, param);
            if(formParts == null){
                return ;
            }
            log.info("发送 c 参：" + param);
            Pitcher pitcher = new Pitcher(context, mHostUrl, formParts, reqHeader);
            if (!TextUtils.isEmpty(mPitcherUrl)) {
                pitcher.setProxyUrl(mPitcherUrl);
            }
            PitcherResponse response = pitcher.request();
            if (response.e != null) {
                log.info("send apm file error : " + " error : " + response.e);
            } else if (response.respcode > 400) {
                log.info("send apm file failed : " + "  resCode is: " + response.respcode);
            } else {
                deleteFile(new File(fileName));
            }
            jsonData = null;
        }
    }

    private ArrayList<FormPart> getFormParts(String jsonData, String cParam) {
        if(isJson(jsonData)) {
            ArrayList<FormPart> formParts = new ArrayList<>();
            //p参数
            FormPart pPart = new FormPart("p", QAPMConstant.PLATFORM);
            pPart.addHeader("X-ClientEncoding", "none");
            formParts.add(pPart);
            //logType
            FormPart logTypePart = new FormPart("logType", QAPMConstant.LOG_TYPE);
            logTypePart.addHeader("X-ClientEncoding", "none");
            formParts.add(logTypePart);
            //b参数
            FormPart bPart = new FormPart("b", jsonData);
            bPart.addHeader("X-ClientEncoding", "none");
            formParts.add(bPart);
            //C参数
            FormPart cPart = new FormPart("c", cParam);
            cPart.addHeader("X-ClientEncoding", "none");
            formParts.add(cPart);
            return formParts;
        }
        return null;
    }

    private boolean isJson(String content){
        try{
            new JSONArray(content);
            return true;
        }catch(Exception e){
            log.error("send apm cParam failed :" + content);
            return false;
        }
    }

    /**
     * 获取C参数，
     *      具体的C参数具体格式如下wiki所示 ：
     *          http://wiki.corp.qunar.com/confluence/pages/viewpage.action?pageId=222747416
     *      具体格式如下所示 ：
     *          *vid:
     *          *pid:
     *          *cid:
     *          uid:
     *          osVersion: 系统版本
     *          model: 机型
     *          *loc：位置信息，获取不到传Unknown
     *          mno：运营商信息，获取不到传Unknown
     *          key: 时间戳
     *          ext：{}扩展字段，目前没有
     * @param context ：Android 上下文环境
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
            jobj.put("vid", !TextUtils.isEmpty(QAPMConstant.vid) ? QAPMConstant.vid : packageInfo.versionCode+"");
            jobj.put("pid", !TextUtils.isEmpty(QAPMConstant.pid) ? QAPMConstant.pid : AndroidUtils.UNKNOWN);
            jobj.put("cid", !TextUtils.isEmpty(QAPMConstant.cid) ? QAPMConstant.cid : AndroidUtils.UNKNOWN);
            jobj.put("uid", !TextUtils.isEmpty(QAPMConstant.uid) ? QAPMConstant.uid : AndroidUtils.getIMEI(context));

            jobj.put("osVersion", Build.VERSION.RELEASE + "_" + Build.VERSION.SDK_INT);
            jobj.put("model", Build.MODEL);
            jobj.put("loc", TextUtils.isEmpty(loc) ? AndroidUtils.UNKNOWN : loc);
            jobj.put("mno", TextUtils.isEmpty(mon) ? AndroidUtils.UNKNOWN : mon);
            jobj.put("key", String.valueOf(System.currentTimeMillis()));
            jobj.put("ext", ""); // 该字段先不支持
        } catch (Exception ignore) {}
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
        try {
            //大客户端暂时先反射大客户端
            Class<?> objClz = Class.forName("qunar.sdk.location.LocationFacade");
            Method method = objClz.getDeclaredMethod("getNewestCacheLocation");
            Location location = (Location) method.invoke(null);
            if (location != null) {
                return location.getLongitude() + "," + location.getLatitude();
            }
        } catch (Throwable e) {
//            QLog.e(e);
        }
        return LocationUtils.getLocation(context);
    }

}
