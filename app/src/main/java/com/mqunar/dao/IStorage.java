package com.mqunar.dao;

import com.mqunar.domain.BaseData;

import java.util.List;

public interface IStorage {

    /**
     * 将所有的数据保存在内存当中
     * @param data ，数据
     */
    void putData(BaseData data, IDataParse parse);

    /**
     * 将数据写入到文件当中
     * @param data ，数据
     */
    void saveData(final BaseData data, IDataParse parse);

    /**
     * 将数据写入到文件当中
     */
    void popData();
}
