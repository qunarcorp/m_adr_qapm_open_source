package com.mqunar.qapm.storage.database;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.mqunar.qapm.storage.QApmConstant;
import com.mqunar.qapm.storage.domain.QContentValue;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库缓存
 * 防止频繁IO：1.导致数据无法写入；2.性能问题
 *
 * @author Qunar Team
 */
public class QApmDatabaseCacheHelper {

    private static final int PRE_WRITE = 0;             // 带数据
    private static final int WRITE_DB = 1;
    private static final int TIME_OUT = 2;

    private long mLastTime;                             // 最后一次写数据库时间

    private Handler mHandler;

    private QApmSQLiteOpenHelper mQApmSQLiteOpenHelper;

    private final List<QContentValue> mDataQueue = new ArrayList<>();

    public QApmDatabaseCacheHelper(QApmSQLiteOpenHelper qApmSQLiteOpenHelper) {
        mLastTime = System.currentTimeMillis();
        mQApmSQLiteOpenHelper = qApmSQLiteOpenHelper;
        initQApmDatabaseCacheHelper();
    }

    private void initQApmDatabaseCacheHelper() {
        HandlerThread handlerThread = new HandlerThread(QApmConstant.THREAD_DATABASE_CACHE);
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                dispatchHandleMessage(msg);
            }
        };
    }

    // message dispather mothed
    private void dispatchHandleMessage(Message msg) {
        switch (msg.what) {
            case PRE_WRITE:
                QContentValue holder = null;
                try {
                    holder = (QContentValue) msg.obj;
                } catch (ClassCastException ex) {
                    // TODO Log  LogX.d(Env.TAG, SUB_TAG, "class cast exception : " + ex.getMessage());
                }
                if (holder != null) {
                    dataToCacheQueue(holder);
                } else {
                    // TODO Log  LogX.d(Env.TAG, SUB_TAG, "holder == null");
                }
                break;
            case WRITE_DB:
                updateTime(System.currentTimeMillis());
                cacheQueueToDataBase ();
                break;

            case TIME_OUT:
                if (mHandler.hasMessages(WRITE_DB)) {
                    mHandler.removeMessages(WRITE_DB);
                }
                mHandler.sendEmptyMessage(WRITE_DB);
                break;
        }
    }

    public boolean saveDataToDB(QContentValue data) {
        if (data == null) {
            return false;
        }
        Message message = mHandler.obtainMessage(PRE_WRITE);
        message.obj = data;
        mHandler.sendMessage(message);
        return true;
    }

    private void  dataToCacheQueue (QContentValue data) {
        int size;
        synchronized (mDataQueue) {
            if (!mDataQueue.contains(data)) {
                mDataQueue.add(data);
            }
            size = mDataQueue.size();
        }
        // LogX.d(Env.TAG, SUB_TAG, "saveDataToDB size = " + size + " interval = " + interval + " : name " + data.tableName + " | info : " + data.info.toString());
        long cur = System.currentTimeMillis();
        long interval = cur - mLastTime;
        if (mHandler.hasMessages(TIME_OUT)) {
            mHandler.removeMessages(TIME_OUT);
        }

        // 双重保护。
        // 1. 防止频繁发生数据库IO操作，设置最小时间间隔为INTERVAL；
        // 2. 防止数据写入太快，队列溢出，当队列达到10的时候，即批量写入数据库
        if (interval >= QApmConstant.SAVE_DB_INTERVAL || size >= QApmConstant.SAVE_DB_MAX_COUNT) {
            if (mHandler.hasMessages(WRITE_DB)) {
                mHandler.removeMessages(WRITE_DB);
            }
            mHandler.sendEmptyMessage(WRITE_DB);
        } else {
            mHandler.sendEmptyMessageDelayed(TIME_OUT, QApmConstant.SAVE_DB_INTERVAL);
        }
    }

    private void cacheQueueToDataBase () {
        SQLiteDatabase database = mQApmSQLiteOpenHelper.getDatabase();
        if (mDataQueue.isEmpty() || database == null) {
            return ;
        }
        int count = 0;
//        long start = System.currentTimeMillis();
        boolean isBeginTransaction = false;

        try {
            database.beginTransaction();
            isBeginTransaction = true;
            // LogX.d(TAG, SUB_TAG, "readFromListAndWriteToDB beginTransaction");

            while (mDataQueue.size() > 0) {
                QContentValue contentValues = mDataQueue.get(0);
                count++;
                if (contentValues != null) {
                    long rowId = database.insert(contentValues.tableName, null, contentValues.info);
                    if (rowId < 0) {
                        break;
                    }
                    synchronized (mDataQueue) {
                        if (mDataQueue.size() > 0) {
                            mDataQueue.remove(0);
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            //  LogX.d(TAG, SUB_TAG, "readFromListAndWriteToDB error\r\n" + Log.getStackTraceString(e));
        } finally {
            if (isBeginTransaction) {
                database.endTransaction();
            }
        }
    }

    private void updateTime(long l) {
        mLastTime = l;
    }

}
