package com.mqunar.qapm.network.sender;

import android.content.Context;
import android.text.TextUtils;

import com.mqunar.qapm.QAPMConstant;
import com.mqunar.qapm.logging.AgentLog;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.utils.IOUtils;
import com.mqunar.qapm.utils.NetWorkUtils;
import com.mqunar.qapm.utils.SafeUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

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
    private String mCParam = "";        // C参数
    private String mRequestId = "";     // requestId
    private String jsonData = null;     // 防止线程栈过大，将jsonData放在堆内存，并在失效后及时置为null

    public QAPMSender(String hostUrl, String pitcherUrl, String cParam, String requestId) {
        this.mHostUrl = hostUrl;
        this.mPitcherUrl = pitcherUrl;
        this.mCParam = cParam;
        this.mRequestId = requestId;
    }

    @Override
    public void send(Context context, String filePath, String cParam) {
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
            if (SafeUtils.canEncryption()) {
                jsonData = SafeUtils.da(jsonData);
            }
            log.info("发送 JSON数据：" + jsonData);
            final HttpHeader reqHeader = new HttpHeader();
            if (!TextUtils.isEmpty(mRequestId)) {
                reqHeader.addHeader("qrid", mRequestId);
            }
            ArrayList<FormPart> formParts = getFormParts(jsonData);
            Pitcher pitcher = new Pitcher(context, mHostUrl, formParts, reqHeader);
            if (!TextUtils.isEmpty(mPitcherUrl)) {
                pitcher.setProxyUrl(mPitcherUrl);
            }
            PitcherResponse response = pitcher.request();
            if (response.e != null) {
                log.info("send apm file error : " + " error : " + response.e);
                //错误 -- 删除压缩文件
            } else if (response.respcode > 400) {
                log.info("send apm file failed : " + "  resCode is: " + response.respcode);
                //失败 -- 删除压缩文件
            } else {
                deleteFile(new File(fileName));
            }
            jsonData = null;
        }
    }

    private ArrayList<FormPart> getFormParts(String jsonData) {
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
        FormPart cPart = new FormPart("c", mCParam);
        cPart.addHeader("X-ClientEncoding", "none");
        formParts.add(cPart);
        return formParts;
    }

    private void deleteFile(File file) {
        if (file != null && file.exists()) {
            if (!file.delete()) {
                log.info("delete file failed :" + file.getName());
            }
        }
    }

}
