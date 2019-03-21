package com.mqunar.qapm.storage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.mqunar.qapm.storage.domain.QTableRecord;

import java.util.List;

@SuppressWarnings("all")
public class QApmStorage implements IStorage {

    private ContentResolver mContentResolver;

    private static QApmStorage sInstance;

    private QApmStorage (Context context) {
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
        return null;
    }

    @Override
    public List<QTableRecord> getAll() {
        return null;
    }

    @Override
    public List<QTableRecord> getData(int index, int count) {
        return null;
    }

    @Override
    public int deleteDataByTime(long time) {
        return 0;
    }

    @Override
    public boolean deleteData(long time) {
        return false;
    }

    @Override
    public boolean cleanData() {
        return false;
    }

    @Override
    public boolean cleanDataByCount() {
        return false;
    }

    @Override
    public boolean insertData(String jsonData) {
        return false;
    }

    @Override
    public boolean update(Integer id, ContentValues contentValues) {
        return false;
    }
}
