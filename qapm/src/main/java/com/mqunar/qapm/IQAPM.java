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

//    /**
//     * 返回当前网络的运营商
//     * @return 当前网络的运营商
//     */
//    String getActiveNetworkCarrier();
//    String getActiveNetworkWanType();
}
