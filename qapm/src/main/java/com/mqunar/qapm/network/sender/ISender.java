package com.mqunar.qapm.network.sender;

public interface ISender {

    /**
     * 发送统计参数
     *
     * @param bParam business参数
     * @param cParam client参数
     */
    void sendParamData(String bParam, String cParam, SenderListener senderListener);

    /**
     * 上传参数地址
     *
     * @return
     */
    String getHostUrl();

    /**
     * 发送数据监听器
     * 成功回调：onSendDataSuccess
     * 失败回调：onSendDataFail
     */
    interface SenderListener {
        void onSendDataSuccess();

        void onSendDataFail();
    }
}
