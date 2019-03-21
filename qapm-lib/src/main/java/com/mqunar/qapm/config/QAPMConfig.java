package com.mqunar.qapm.config;

import android.text.TextUtils;

import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.network.sender.ISender;

/**
 * Date: 2019/2/28,5:45 PM ;<p/>
 * Description: APM配置项;<p/>
 * Other: ;
 */
public class QAPMConfig {

    private String pid = "";// 客户端Id
    private String vid = "";// app版本号
    private String cid = "";// 渠道号
    private boolean isLogEnable;//是否输出log开关
    private ISender sender; //日志发送sender对象
    private String hostUrl; //日志上传地址

    private TraceConfig fpsTraceConfig;
    private TraceConfig cpuTraceConfig;
    private TraceConfig memoryTraceConfig;
    private TraceConfig batteryTraceConfig;

    public static class TraceConfig { //监控项配置
        public boolean isUseTrace;//是否使用
        public long delayMillis;//监控间隔
    }

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

    public TraceConfig getFpsTraceConfig() {
        return fpsTraceConfig;
    }

    public TraceConfig getCpuTraceConfig() {
        return cpuTraceConfig;
    }

    public TraceConfig getMemoryTraceConfig() {
        return memoryTraceConfig;
    }

    public TraceConfig getBatteryTraceConfig() {
        return batteryTraceConfig;
    }

    public static class ConfigBuilder {
        private QAPMConfig config = new QAPMConfig();

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

        public ConfigBuilder setFpsTraceConfig(TraceConfig traceConfig) {
            this.config.fpsTraceConfig = traceConfig;
            return this;
        }

        public ConfigBuilder setCpuTraceConfig(TraceConfig traceConfig) {
            this.config.cpuTraceConfig = traceConfig;
            return this;
        }

        public ConfigBuilder setMemoryTraceConfig(TraceConfig traceConfig) {
            this.config.memoryTraceConfig = traceConfig;
            return this;
        }

        public ConfigBuilder setBatteryTraceConfig(TraceConfig traceConfig) {
            this.config.batteryTraceConfig = traceConfig;
            return this;
        }

        public QAPMConfig build() {
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
