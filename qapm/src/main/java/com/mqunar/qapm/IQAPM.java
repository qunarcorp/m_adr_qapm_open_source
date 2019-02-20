package com.mqunar.qapm;

import com.mqunar.qapm.domain.BaseData;
import com.mqunar.qapm.network.sender.ISender;

import java.util.Map;

public interface IQAPM {

    void release();

    /**
     * 设施网络发送器，并且发送器必须是实现了ISender接口的类
     */
    void upload(final boolean isForceSend);
    void setSender(ISender sender);
    ISender getSender();


    /**
     * 暴露给外部使用，例如RN、hy等等
     */
    void addNetMonitor(Map<String,String> netMonitorMapData);

    /**
     * 添加一些Qunar特有的数据结构的接口，只要该接口当中上传的数据实现BaseData接口即可
     */
    void addQunarMonitor(BaseData baseData);

//    /**
//     * 返回当前网络的运营商
//     * @return 当前网络的运营商
//     */
//    String getActiveNetworkCarrier();
//    String getActiveNetworkWanType();
}
