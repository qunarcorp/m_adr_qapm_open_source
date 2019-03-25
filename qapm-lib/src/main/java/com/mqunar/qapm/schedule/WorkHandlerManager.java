package com.mqunar.qapm.schedule;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.mqunar.qapm.QAPMConstant;
import com.mqunar.qapm.config.QConfigManager;
import com.mqunar.qapm.dao.Storage;
import com.mqunar.qapm.logging.AgentLog;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.network.sender.ISender;
import com.mqunar.qapm.utils.AndroidUtils;
import com.mqunar.qapm.utils.IOUtils;
import com.mqunar.qapm.utils.NetWorkUtils;

import java.io.File;

/**
 * Author: haonan.he ;<p/>
 * Date: 2019/3/1,4:47 PM ;<p/>
 * Description: 管理WorkHandler;<p/>
 * Other: ;
 */
public class WorkHandlerManager {

    private static WorkHandlerManager instance;
    private Handler mWorkHandler;
    private HandlerThread mWorkLooper;
    private AgentLog mLog = AgentLogManager.getAgentLog();

    private WorkHandlerManager() {
    }

    public static WorkHandlerManager getInstance() {
        if (null == instance) {
            synchronized (WorkHandlerManager.class) {
                if (null == instance) {
                    instance = new WorkHandlerManager();
                }
            }
        }
        return instance;
    }

    public void init() {
        mWorkLooper = new HandlerThread(QAPMConstant.THREAD_UPLOAD);
        mWorkLooper.start();
        mWorkHandler = new Handler(mWorkLooper.getLooper());
    }

    public void post(Runnable runnable) {
        //防止主线程调用引起ANR
        mWorkHandler.post(runnable);
    }

    public void postToUpload(final Context context, final boolean isForceSend) {
        //防止主线程调用引起ANR
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!NetWorkUtils.isNetworkConnected(context)) {// 没有网络先不处理
                    return;
                }
                if (isForceSend) {
                    Storage.newStorage().popData();
                }
                String uploadDir = IOUtils.getUploadDir(context);
                if (uploadDir != null) {
                    String[] uploadFiles = IOUtils.getFileByNameFilter(uploadDir);
                    if (uploadFiles != null && uploadFiles.length > 0) {
                        for (final String fileName : uploadFiles) {
                            String bParam = IOUtils.file2Str(uploadDir + File.separator + fileName);
                            String cParam = AndroidUtils.getCParam(context);
                            QConfigManager.getInstance().getSender().sendParamData(bParam, cParam,
                                    new ISender.SenderListener() {
                                        @Override
                                        public void onSendDataSuccess() {
                                            mLog.info("uploadFile onSendDataSuccess=" + fileName);
                                            IOUtils.deleteFile(fileName);
                                        }

                                        @Override
                                        public void onSendDataFail() {
                                            mLog.info("uploadFile onSendDataFail=" + fileName);
                                        }
                                    });
                        }
                    } else {
                        mLog.info("uploadFiles is null");
                    }
                } else {
                    mLog.info("uploadDir is null");
                }
            }
        });
    }


    public void quit() {
        if (mWorkLooper != null) {
            mWorkLooper.quit();
        }
    }

}
