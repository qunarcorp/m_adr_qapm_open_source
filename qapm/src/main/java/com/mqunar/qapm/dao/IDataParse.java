package com.mqunar.qapm.dao;

import com.mqunar.qapm.domain.BaseData;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface IDataParse {

    String TAG = "DataParse";

    /**
     * 将对象转换成json字符串   --- 发送时调用
     *
     * @return 生成的JSON
     */
    String convertBaseData2Json(List<BaseData> data);

    /**
     * 将Map转换成BaseData
     *
     * @param data 要转换的数据
     * @return JSONObject
     */
    JSONObject convertImplData2Json(BaseData data);

    /**
     * 将NetworkData转换成Json
     *
     * @param data 要转换的数据
     * @return JSONObject
     */
    BaseData convertMap2BaseData(Map<String, String> data);
}
