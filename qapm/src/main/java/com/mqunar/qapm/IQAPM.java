package com.mqunar.qapm;

import com.mqunar.qapm.domain.BaseData;

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
    void addCustomMonitor(BaseData baseData);

    /**
     * 设施网络发送器，并且发送器必须是实现了ISender接口的类
     *
     * @param isForceSend 是否强制发送
     */
    void upload(final boolean isForceSend);

}
