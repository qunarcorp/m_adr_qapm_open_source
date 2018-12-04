package com.mqunar.domain;

/**
 * 页面数据的JavaBean
 * Created by pengchengpc.liu on 2018/11/22.
 */
public class UIData implements BaseData{

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";

    private static final long serialVersionUID = 2L;

    public String action; // 页面名称
    public String page; //	顶层页面
    public String status; // load成功或失败，“success”； “error”；
    public String netType; // 4g/wifi...Unknow(未知网络)，无网：(unconnect)

    public long createTime; // 创建Activity时间
    public long resumeTime; // 页面创建完成时间(即将展示)
    public long hiddenTime; // 页面loading完成时间
    public long showTime; // 页面loading展示时间

    @Override
    public String toString() {
        return "UIData{" + "action='" + action + '\'' +
                ", page='" + page + '\'' + ", createTime='" + createTime + '\'' +
                ", resumeTime='" + resumeTime + '\'' + ", hiddenTime='" + hiddenTime + '\'' +
                ", showTime='" + showTime + '\'' + ", status='" + status + '\'' +
                ", netType='" + netType + '\'' + '}';
    }
}
