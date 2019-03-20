package com.mqunar.qapm.config;

import android.text.TextUtils;
import android.util.Patterns;

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

    private static volatile  ConfigManager instance;

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
        AgentLogManager.setAgentLog((mConfig.isLogEnable() ? new AndroidAgentLog() : new NullAgentLog()));
    }

    public ISender getSender() {
        if (mConfig.getSender() == null) {
            //sender未设置，使用传入的HostUrl配置默认上传sender
            if( Patterns.WEB_URL.matcher(mConfig.getHostUrl()).matches()) {
                mConfig.setSender(new DefaultSender(mConfig.getHostUrl()));
            } else {
                throw new IllegalStateException("hostUrl is invalid!");
            }
        }
        return mConfig.getSender();
    }

    public String getHostUrl() {
        if (mConfig.getSender() == null) {
            return mConfig.getHostUrl();
        } else {
            if (TextUtils.isEmpty(mConfig.getSender().getHostUrl())) {
                return mConfig.getHostUrl();
            } else {
                return mConfig.getSender().getHostUrl();
            }
        }
    }

    public String getPid() {
        return mConfig.getPid();
    }

    public String getVid() {
        return mConfig.getVid();
    }

    public String getCid() {
        return mConfig.getCid();
    }

}
