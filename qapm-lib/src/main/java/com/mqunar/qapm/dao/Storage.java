package com.mqunar.qapm.dao;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.mqunar.qapm.QAPMConstant;
import com.mqunar.qapm.domain.BaseAPMData;
import com.mqunar.qapm.storage.QApmStorage;

/**
 * QAPM-main 模块对 QAPM-Storage 模块的引用，主要通过 QApmStorage 类 进行数据
 * 层面的持久化。
 *
 * @author  Qunar Team
 * @since   19 - 03 - 21
 */
public class Storage {

    private QApmStorage mQApmStorage;
    private static Storage sInstance = null;

    private static HandlerThread mStorageHandlerThread;
    private static Handler mStorageHandler;

    private HandlerThread getStorageHandlerThread() {
        if (mStorageHandlerThread == null) {
            mStorageHandlerThread = new HandlerThread(QAPMConstant.THREAD_STORAGE);
            mStorageHandlerThread.start();
        }
        return mStorageHandlerThread;
    }

    private Handler getStorageHandler() {
        if (mStorageHandler == null) {
            synchronized (Storage.class) {
                if (mStorageHandler == null) {
                    mStorageHandler = new Handler(getStorageHandlerThread().getLooper());
                }
            }
        }
        return mStorageHandler;
    }

    private Storage(Context context) {
        mQApmStorage = QApmStorage.getInstance(context);
    }

    public static Storage newStorage(Context context) {
        if (sInstance == null) {
            synchronized (Storage.class) {
                if (sInstance == null) {
                    sInstance = new Storage(context);
                }
            }
        }
        return sInstance;
    }

    public void putData(final BaseAPMData data) {
        getStorageHandler().post(new Runnable() {
            public void run() {
                mQApmStorage.insertData(data.action, System.currentTimeMillis(), data.toJSONObject().toString());
            }
        });
    }

}
