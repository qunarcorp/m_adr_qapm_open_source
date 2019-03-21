package com.mqunar.qapm.storage.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.mqunar.qapm.storage.database.QApmDatabaseCacheHelper;
import com.mqunar.qapm.storage.database.QApmSQLiteOpenHelper;
import com.mqunar.qapm.storage.domain.QContentValue;
import com.mqunar.qapm.storage.table.ITable;
import com.mqunar.qapm.storage.table.MainTable;
import com.mqunar.qapm.storage.utils.StorageUtils;

public class QApmContentProvider extends ContentProvider {

    public static final String TABLE_MAIN = "main";     // 虚拟表的名称

    private QApmSQLiteOpenHelper openHelper;            // 数据库打开
    private QApmDatabaseCacheHelper cacheHelper;         // 数据库写入

    private final UriMatcher mTableMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SparseArray<ITable> mTableMap;


    @Override
    public boolean onCreate() {
        initContentProviderMatcher();
        openHelper = new QApmSQLiteOpenHelper(getContext());
        openHelper.setTable(new MainTable());
        cacheHelper = new QApmDatabaseCacheHelper(openHelper);
        return true;
    }

    private void initContentProviderMatcher() {
        mTableMap = new SparseArray<>();
        mTableMap.append(0, new MainTable());
        mTableMatcher.addURI(StorageUtils.getAuthority(getContext().getPackageName()), TABLE_MAIN, 0);
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        ITable table = mTableMap.get(mTableMatcher.match(uri));
        if (table == null) return null;
        try {
            Cursor cursor = openHelper.getDatabase().query(table.getTableName(),
                    projection, selection, selectionArgs, sortOrder, null, null);
            if (cursor != null && getContext() != null) {
                cursor.setNotificationUri(getContext().getContentResolver(), null);
            } else {
                // LogX.d(Env.TAG, SUB_TAG, "cursor == null");
            }
            return cursor;
        } catch (Throwable throwable) {
            // LogX.d(Env.TAG, "query ex : " + Log.getStackTraceString(e));
        }
        return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        ITable table = mTableMap.get(mTableMatcher.match(uri));
        if (null == values || null == table) return null;
        boolean result = cacheHelper.saveDataToDB(new QContentValue(values, table.getTableName()));
        return result ? uri : null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        ITable table = mTableMap.get(mTableMatcher.match(uri));
        if (null == table) return -1;
        int count = -1;
        try {
            count = openHelper.getDatabase().delete(table.getTableName(), selection, selectionArgs);
            // ogX.d(TAG, "数据库成功删除表（" + table.getTableName() + "）: " + count + "条数据");
        } catch (Throwable throwable) {
            // LogX.e(TAG, "数据库删除表（" + table.getTableName() + "）数据失败: " + e.toString());
            return count;
        }
        notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        ITable table = mTableMap.get(mTableMatcher.match(uri));
        if (null == values || null == table) return 0;
        int count = 0;
        try {
            count = openHelper.getDatabase().update(table.getTableName(), values, selection, selectionArgs);
        } catch (Throwable throwable) {
            // LogX.e(TAG, "数据库更新失败: " + e.toString());
            return count;
        }
        // 根据业务插入的时候，不用通知Observer
//        notifyChange(uri, null);
        return count;
    }


    private void notifyChange(Uri uri, ContentObserver observer) {
        try {
            ContentResolver contentResolver = getContext().getContentResolver();
            if (contentResolver != null) {
                contentResolver.notifyChange(uri, observer);
            }
        } catch (Exception e) {
            // LogX.d(Env.TAG, "notifyChange ex : " + Log.getStackTraceString(e));
        }
    }
}
