package com.mqunar.qapm.storage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.mqunar.qapm.storage.domain.QTableRecord;
import com.mqunar.qapm.storage.utils.StorageUtils;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("all")
public class QApmStorage implements IStorage {

    private ContentResolver mContentResolver;

    private static QApmStorage sInstance;

    private Context mContext;

    private QApmStorage (Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    public static QApmStorage getInstance(Context context) {
        if (sInstance == null) {
            synchronized (QApmStorage.class) {
                if (sInstance == null) {
                    sInstance = new QApmStorage(context);
                }
            }
        }
        return sInstance;
    }

    @Override
    public QTableRecord get(Integer id) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from ").append(QApmConstant.DB_TABLE_NAME).append(" where ");
        stringBuffer.append(QApmConstant.MainTable.ID).append("=");
        stringBuffer.append(id);
        String sql = stringBuffer.toString();
        List<QTableRecord> infoList = getDataFormDatabase(sql);
        return (null == infoList || infoList.isEmpty()) ? null : infoList.get(0);
    }

    @Override
    public List<QTableRecord> getAll() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from ").append(QApmConstant.DB_TABLE_NAME);
        String sql = stringBuffer.toString();
        return getDataFormDatabase(sql);
    }


    @Override
    public List<QTableRecord> getData(int index, int count) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from ").append(QApmConstant.DB_TABLE_NAME).append(" order by id asc");
        stringBuffer.append(" limit ").append(count).append(" offset ").append(index);
        String sql = stringBuffer.toString();
        return getDataFormDatabase(sql);
    }

    @Override
    public int deleteDataByTime(long time) {
        try {
            return mContentResolver.delete(
                    StorageUtils.getTableUri(mContext.getPackageName(), QApmConstant.DB_TABLE_NAME),
                    QApmConstant.MainTable.MODIFY_TIME + "< ?", new String[]{String.valueOf(time)});
        } catch (Throwable throwable) {
            // LogX.d(Env.TAG, "deleteByTime ex : " + Log.getStackTraceString(e));
        }
        return -2;
    }

    @Override
    public boolean deleteData(int id) {
        try {
             return mContentResolver.delete(
                     StorageUtils.getTableUri(mContext.getPackageName(), QApmConstant.DB_TABLE_NAME),
                     QApmConstant.MainTable.ID, new String[]{String.valueOf(id)}) > 0;
        } catch (Throwable throwable) {
            // LogX.d(Env.TAG, "delete ex : " + Log.getStackTraceString(e));
        }
        return false;
    }

    @Override
    public boolean cleanData() {
        try {
            return mContentResolver.delete(StorageUtils.getTableUri(mContext.getPackageName(),
                    QApmConstant.DB_TABLE_NAME), null, null) > 0;
        } catch (Throwable throwable) {
            // LogX.d(Env.TAG, "clean ex : " + Log.getStackTraceString(e));
        }
        return false;
    }

    @Override
    public boolean cleanDataByCount(int count) {
        try {
            return mContentResolver.delete(
                    StorageUtils.getTableUri(mContext.getPackageName(), QApmConstant.DB_TABLE_NAME),
                    "id in(select id from " + QApmConstant.DB_TABLE_NAME + " order by id asc limit " + count + ")",
                    null) > 0;
        } catch (Throwable throwable) {
            // LogX.d(Env.TAG, "cleanByCount ex : " + Log.getStackTraceString(e));
        }
        return false;
    }

    @Override
    public boolean insertData(String action, long time, String jsonData) {
        ContentValues qTableRecord = new QTableRecord(action, time, jsonData).toContentValues();
        try {
            return mContentResolver.insert(StorageUtils.getTableUri(
                    mContext.getPackageName(), QApmConstant.DB_TABLE_NAME), qTableRecord) != null;
        } catch (Throwable throwable) {
            // LogX.d(Env.TAG, "save ex : " + Log.getStackTraceString(e));
        }
        return false;
    }

    @Override
    public boolean update(Integer id, ContentValues contentValues) {
        try {
            return mContentResolver.update(StorageUtils.getTableUri(
                    mContext.getPackageName(), QApmConstant.DB_TABLE_NAME), contentValues,
                    QApmConstant.MainTable.ID + "=? ", new String[]{String.valueOf(id)}) != -1;
        } catch (Throwable throwable) {
            // LogX.d(Env.TAG, "update ex : " + Log.getStackTraceString(e));
        }
        return false;
    }

    private List<QTableRecord> getDataFormDatabase (String sql) {
        List<QTableRecord> list = new LinkedList<>();
        Cursor cursor = null ;
        try {
            cursor = mContentResolver.query(StorageUtils.getTableUri(mContext.getPackageName(), QApmConstant.DB_TABLE_NAME),
                    null, sql, null, null);
            if (null == cursor || !cursor.moveToFirst()) {
                closeCursor(cursor);
                return list;
            }

            int idIndex = cursor.getColumnIndex(QApmConstant.MainTable.ID);
            int actionIndex = cursor.getColumnIndex(QApmConstant.MainTable.ACTION);
            int contentIndex = cursor.getColumnIndex(QApmConstant.MainTable.CONTENT);
            int timeIndex = cursor.getColumnIndex(QApmConstant.MainTable.TIME);
            int isUploadIndex = cursor.getColumnIndex(QApmConstant.MainTable.IS_UPLOAD);

            do {
                list.add(new QTableRecord(
                        cursor.getString(idIndex),
                        cursor.getString(actionIndex),
                        cursor.getString(contentIndex),
                        cursor.getString(timeIndex),
                        cursor.getString(isUploadIndex)
                ));

            } while (cursor.moveToNext());
        } catch (Throwable throwable) {
            // LogX.e(TAG, SUB_TAG, getName() + "; " + e.toString());
        } finally {
            closeCursor(cursor);
        }
        return list;
    }

    private void closeCursor (Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

}