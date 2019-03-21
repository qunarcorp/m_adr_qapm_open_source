package com.mqunar.qapm;

import com.mqunar.qapm.domain.BaseAPMData;

/**
 * QAPM接口类
 */
public interface IQAPM {

    /**
     * 释放相关资源
     */
    void release();

    /**
     * 添加自定义监控
     *
     * @param baseData 自定义数据 <extend BaseData>
     */
    void addCustomMonitor(BaseAPMData baseData);

}
