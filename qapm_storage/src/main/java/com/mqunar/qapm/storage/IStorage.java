package com.mqunar.qapm.storage;

import android.content.ContentValues;

import com.mqunar.qapm.storage.domain.QTableRecord;

import java.util.List;

/**
 * Storage 对外提供的暴露接口，在此约定好相关的API 其他模块要时候该模块提供的API的时候，必须进过
 * 这个接口的实现类，提供相关的增删改查的业务逻辑
 */
public interface IStorage {

    /**
     * Query 相关查找语句
     * @return 查找到的相关 ReusltSet
     */
    QTableRecord get (Integer id) ;
    List<QTableRecord> getAll ();
    List<QTableRecord> getData (int index, int count);

    /**
     * Delete 相关删除语句
     * @return 是否删除成功，或者本次删除多少条数据
     */
    int deleteDataByTime (long time);
    boolean deleteData (int id);
    boolean cleanData ();
    boolean cleanDataByCount (int count);

    /**
     * insert 插入相关语句
     * @param jsonData 所要插入的语句
     * @return  是否插入成功
     */
    boolean insertData (String action, long time, String jsonData);

    /**
     * update 更新相关语句
     * @param id 所要更新的列
     * @param contentValues 更新之后的数据
     * @return 是否更新成功
     */
    boolean update (Integer id, ContentValues contentValues);

}
