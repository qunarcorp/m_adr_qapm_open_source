package com.mqunar.qapm.storage.domain;

import android.content.ContentValues;

import com.mqunar.qapm.storage.QApmConstant;

public class QTableRecord {

    public String id ;
    public String type ;
    public String action ;
    public String content ;
    public String time ;
    public String modifyTime ;
    public String isUpload ;


    public QTableRecord (String action, long time, String jonsData) {
        this.action = action;
        this.content = jonsData;
        this.time = String.valueOf(time);
        this.isUpload = "0";
    }

    public QTableRecord(String id, String action, String content, String time, String isUpload) {
        this.id = id;
        this.action = action;
        this.content = content;
        this.time = time;
        this.isUpload = isUpload;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(QApmConstant.MainTable.ACTION, action);
        values.put(QApmConstant.MainTable.CONTENT, content);
        values.put(QApmConstant.MainTable.TIME, time);
        values.put(QApmConstant.MainTable.IS_UPLOAD, isUpload);
        return values;
    }
}
