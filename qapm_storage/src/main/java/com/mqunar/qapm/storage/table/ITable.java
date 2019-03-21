package com.mqunar.qapm.storage.table;

public interface ITable {

    String getCreateTableSql ();

    String getTableName ();

    boolean checkTable ();
}
