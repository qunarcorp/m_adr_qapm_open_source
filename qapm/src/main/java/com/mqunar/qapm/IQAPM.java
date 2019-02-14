package com.mqunar.qapm;

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
     * @param uiMonitorMapData 外部需要我们上传的参数
     */
    void addUIMonitor(Map<String,String> uiMonitorMapData);
    void addNetMonitor(Map<String,String> netMonitorMapData);

//    /**
//     * 返回当前网络的运营商
//     * @return 当前网络的运营商
//     */
//    String getActiveNetworkCarrier();
//    String getActiveNetworkWanType();
}
