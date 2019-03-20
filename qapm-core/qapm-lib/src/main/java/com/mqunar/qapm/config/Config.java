package com.mqunar.qapm.config;

import android.text.TextUtils;

import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.network.sender.ISender;

/**
 * Author: haonan.he ;<p/>
 * Date: 2019/2/28,5:45 PM ;<p/>
 * Description: APM配置项;<p/>
 * Other: ;
 */
public class Config {

    private String pid = "";// 客户端Id
    private String vid = "";// app版本号
    private String cid = "";// 渠道号
    private boolean isLogEnable;//是否输出log开关
    private ISender sender; //日志发送sender对象
    private String hostUrl; //日志上传地址

    public String getPid() {
        return pid;
    }

    public String getVid() {
        return vid;
    }

    public void setSender(ISender sender) {
        this.sender = sender;
    }

    public String getCid() {

        return cid;
    }

    public boolean isLogEnable() {
        return isLogEnable;
    }

    public ISender getSender() {
        return sender;
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public static class ConfigBuilder {
        private Config config = new Config();

        public ConfigBuilder setPid(String pid) {
            this.config.pid = pid;
            return this;
        }

        public ConfigBuilder setVid(String vid) {
            this.config.vid = vid;
            return this;
        }

        public ConfigBuilder setCid(String cid) {
            this.config.cid = cid;
            return this;
        }

        public ConfigBuilder setLogEnable(boolean logEnable) {
            this.config.isLogEnable = logEnable;
            return this;
        }

        public ConfigBuilder setSender(ISender sender) {
            this.config.sender = sender;
            return this;
        }

        public ConfigBuilder setHostUrl(String hostUrl) {
            this.config.hostUrl = hostUrl;
            return this;
        }

        public Config build() {
            //check相关参数，如果异常直接提示错误
            if (TextUtils.isEmpty(config.pid)) {
                throw new IllegalArgumentException("Please configure pid!");
            }
            if (TextUtils.isEmpty(config.cid)) {
                AgentLogManager.getAgentLog().warning("cid is empty");
            }
            if (TextUtils.isEmpty(config.vid)) {
                AgentLogManager.getAgentLog().warning("vid is empty");
            }
            if (TextUtils.isEmpty(config.hostUrl) && null == config.sender) {
                throw new RuntimeException("Please configure hostUrl or sender!");
            }
            return config;
        }
    }

}
