package com.mqunar.qapm.storage.domain;

import android.content.ContentValues;

public class QContentValue {
    public ContentValues info;
    public String tableName;

    public QContentValue(ContentValues contentValues, String name) {
        info = contentValues;
        tableName = name;
    }
}
