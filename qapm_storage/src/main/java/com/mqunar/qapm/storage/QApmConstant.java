package com.mqunar.qapm.storage;

public class QApmConstant {

    public static final String CONTENT_PATH_PREFIX = "content://";
    public static final String AUTHORITY_SUFFIX = "qapm.storage";
    public static final String DB_NAME = "qapm.db";             // 数据库名称
    public static final String DB_TABLE_NAME = "QapmMain";     // 表名称

    public static final int DB_VERSION = 1;                    // 数据库版本号

    public static final int SAVE_DB_INTERVAL = 5 * 1000;// 写入数据库最短时间间隔，防止频繁IO
    public static final int SAVE_DB_MAX_COUNT = 10;// 写入数据库最大缓存条数

    public static final String THREAD_DATABASE_CACHE = "thread_database_cache";

    public static class MainTable {
        public static final String ID = "id";
        public static final String TYPE = "type";
        public static final String ACTION = "action";
        public static final String CONTENT = "content";
        public static final String TIME = "time";
        public static final String MODIFY_TIME = "modifyTime";
        public static final String IS_UPLOAD = "isUpload";
    }

}
