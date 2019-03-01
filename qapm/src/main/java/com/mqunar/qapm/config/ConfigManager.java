package com.mqunar.qapm.config;

import android.text.TextUtils;

import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.logging.AndroidAgentLog;
import com.mqunar.qapm.logging.NullAgentLog;
import com.mqunar.qapm.network.sender.DefaultSender;
import com.mqunar.qapm.network.sender.ISender;

/**
 * Author: haonan.he ;<p/>
 * Date: 2019/2/28,7:49 PM ;<p/>
 * Description: 统一管理apm的配置参数;<p/>
 * Other: ;
 */
public class ConfigManager {

    private static ConfigManager instance;

    private Config mConfig; //apm配置

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        if (null == instance) {
            synchronized (ConfigManager.class) {
                if (null == instance) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    public void setConfig(Config mConfig) {
        this.mConfig = mConfig;
        //配置log
        AgentLogManager.setAgentLog((mConfig.isLogEnable ? new AndroidAgentLog() : new NullAgentLog()));
    }

    public ISender getSender() {
        if (mConfig.sender == null) {
            //sender未设置，使用传入的HostUrl配置默认上传sender
            if (mConfig.hostUrl.toLowerCase().contains("http")) {
                mConfig.sender = new DefaultSender(mConfig.hostUrl);
            } else {
                throw new IllegalStateException("hostUrl is invalid!");
            }
        }
        return mConfig.sender;
    }

    public String getHostUrl() {
        if (mConfig.sender == null) {
            return mConfig.hostUrl;
        } else {
            if (TextUtils.isEmpty(mConfig.sender.getHostUrl())) {
                return mConfig.hostUrl;
            } else {
                return mConfig.sender.getHostUrl();
            }
        }
    }

    public String getPid() {
        return mConfig.pid;
    }

    public String getVid() {
        return mConfig.vid;
    }

    public String getCid() {
        return mConfig.cid;
    }

}
