package com.mqunar.network.sender;

import android.content.Context;
import android.text.TextUtils;

import com.mqunar.QAPMConstant;
import com.mqunar.logging.AgentLog;
import com.mqunar.logging.AgentLogManager;
import com.mqunar.utils.AndroidUtils;
import com.mqunar.utils.IOUtils;
import com.mqunar.utils.NetWorkUtils;
import com.mqunar.utils.QAPMCompressUtils;
import com.mqunar.utils.SafeUtils;

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
                log.info("send necro data : " + fileName);
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
            String compressFileName = fileName + "gz";
            log.info("发送 JSON数据：" + jsonData);
            QAPMCompressUtils.doCompressString(jsonData, compressFileName);
            jsonData = null;
            log.info("send necro file : " + compressFileName);
            final HttpHeader reqHeader = new HttpHeader();
            if (!TextUtils.isEmpty(mRequestId)) {
                reqHeader.addHeader("qrid", mRequestId);
            }
            ArrayList<FormPart> formParts = getFormParts(compressFileName);

            Pitcher pitcher = new Pitcher(context, mHostUrl, formParts, reqHeader);
            if (!TextUtils.isEmpty(mPitcherUrl)) {
                pitcher.setProxyUrl(mPitcherUrl);
            }

            PitcherResponse response = pitcher.request();

            if (response.e != null) {
                log.info("send necro file error : " + compressFileName + " error : " + response.e);
                //错误 -- 删除压缩文件
                deleteFile(new File(compressFileName));
            } else if (response.respcode > 400) {
                log.info("send necro file failed : " + compressFileName + "  resCode is: " + response.respcode);
                //失败 -- 删除压缩文件
                deleteFile(new File(compressFileName));
            } else {
                log.info("send necro file success : " + compressFileName);
                //成功 -- 1.本地删除加密原始文件;2.删除压缩文件。
                deleteFile(new File(compressFileName));
                deleteFile(new File(fileName));
            }
        }
    }

    private ArrayList<FormPart> getFormParts(String compressFileName) {
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
        FormPart bPart = new FormPart("b", compressFileName, "application/octet-stream");
        bPart.addHeader("X-ClientEncoding", "gzip");
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
