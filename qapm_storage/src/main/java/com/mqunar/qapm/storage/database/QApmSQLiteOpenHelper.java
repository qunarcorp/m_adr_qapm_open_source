package com.mqunar.qapm.storage.database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mqunar.qapm.storage.QApmConstant;
import com.mqunar.qapm.storage.table.ITable;

public class QApmSQLiteOpenHelper extends SQLiteOpenHelper {

    private ITable mTable;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public QApmSQLiteOpenHelper(Context context) {
        super(context, QApmConstant.DB_NAME, null, QApmConstant.DB_VERSION);
        initQApmSQLiteOpenHelper(context);
    }

    public QApmSQLiteOpenHelper(Context context, DatabaseErrorHandler errorHandler) {
        super(context, QApmConstant.DB_NAME, null, QApmConstant.DB_VERSION, errorHandler);
        initQApmSQLiteOpenHelper(context);
    }

    private void initQApmSQLiteOpenHelper(Context context){
        mContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Log
        if (mTable != null) {
            db.execSQL(mTable.getCreateTableSql());
            // TODO Log
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Log
        deleteDatabaseByName();
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Log
        deleteDatabaseByName();
    }

    // TODO getWritableDatabase, getReadableDatabase, 两者的区别
    public SQLiteDatabase getDatabase (){
        if (mDatabase != null){
            mDatabase = getWritableDatabase();
        }
        return mDatabase;
    }

    public void setTable (ITable table) {
        if (table.checkTable()) {
            mTable = table;
        }
        // TODO Log
    }

    private void deleteDatabaseByName () {
        mContext.deleteDatabase(QApmConstant.DB_NAME);
    }
}
