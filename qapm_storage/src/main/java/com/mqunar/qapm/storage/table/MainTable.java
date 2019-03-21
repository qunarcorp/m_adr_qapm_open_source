package com.mqunar.qapm.storage.table;

import android.text.TextUtils;

import com.mqunar.qapm.storage.QApmConstant;

public class MainTable implements ITable {

    private static final String TABLE_NAME = QApmConstant.DB_TABLE_NAME;
    private static final String TABLE_CREATE_SQL = "CREATE TABLE " + TABLE_NAME + " ( " +
            QApmConstant.MainTable.ID + " INT PRIMARY KEY NOT NULL," +
            QApmConstant.MainTable.TYPE + " TEXT," +
            QApmConstant.MainTable.ACTION + " TEXT    NOT NULL," +
            QApmConstant.MainTable.CONTENT + " TEXT    NOT NULL," +
            QApmConstant.MainTable.TIME + " TEXT    NOT NULL," +
            QApmConstant.MainTable.MODIFY_TIME + " TEXT," +
            QApmConstant.MainTable.IS_UPLOAD + " TEXT    NOT NULL" +
            " );";

    @Override
    public String getCreateTableSql() {
        return TABLE_CREATE_SQL;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public boolean checkTable() {
        return !(TextUtils.isEmpty(TABLE_NAME) || TextUtils.isEmpty(TABLE_CREATE_SQL));
    }
}
