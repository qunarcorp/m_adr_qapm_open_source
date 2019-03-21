package com.mqunar.qapm.config;

import android.text.TextUtils;
import android.util.Patterns;

import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.logging.AndroidAgentLog;
import com.mqunar.qapm.logging.NullAgentLog;
import com.mqunar.qapm.network.sender.DefaultSender;
import com.mqunar.qapm.network.sender.ISender;

/**
 * Date: 2019/2/28,7:49 PM ;<p/>
 * Description: 统一管理QAPM配置参数;<p/>
 * Other: ;
 */
public class QConfigManager {

    private static volatile QConfigManager instance;

    private QAPMConfig mConfig; //apm配置
    private final static long INTERVAL = 1000; //默认间隔
    private final static long BATTERY_INTERVAL = 60 * 1000;//电池统计间隔

    private QConfigManager() {
    }

    public static QConfigManager getInstance() {
        if (null == instance) {
            synchronized (QConfigManager.class) {
                if (null == instance) {
                    instance = new QConfigManager();
                }
            }
        }
        return instance;
    }

    private void checkConfig(QAPMConfig config) {
        if (null == config) {
            throw new RuntimeException("QAPMConfig is invalid!");
        }
    }

    public void setConfig(QAPMConfig mConfig) {
        checkConfig(mConfig);
        this.mConfig = mConfig;
        //配置log
        AgentLogManager.setAgentLog((mConfig.isLogEnable() ? new AndroidAgentLog() : new NullAgentLog()));
    }

    public ISender getSender() {
        if (mConfig.getSender() == null) {
            //sender未设置，使用传入的HostUrl配置默认上传sender
            if (Patterns.WEB_URL.matcher(mConfig.getHostUrl()).matches()) {
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

    public boolean isUseFpsTrace() {
        if (null == mConfig.getFpsTraceConfig()) {
            return false;
        } else {
            return mConfig.getFpsTraceConfig().isUseTrace;
        }
    }

    public long getFpsTraceInterval() {
        if (null == mConfig.getFpsTraceConfig()) {
            return INTERVAL;
        } else {
            return checkDelayMillis(mConfig.getFpsTraceConfig().delayMillis);
        }
    }

    public boolean isUseCpuTrace() {
        if (null == mConfig.getCpuTraceConfig()) {
            return false;
        } else {
            return mConfig.getCpuTraceConfig().isUseTrace;
        }
    }

    public long getCpuTraceInterval() {
        if (null == mConfig.getCpuTraceConfig()) {
            return INTERVAL;
        } else {
            return checkDelayMillis(mConfig.getCpuTraceConfig().delayMillis);
        }
    }

    public boolean isUseMemoryTrace() {
        if (null == mConfig.getMemoryTraceConfig()) {
            return false;
        } else {
            return mConfig.getMemoryTraceConfig().isUseTrace;
        }
    }

    public long getMemoryTraceInterval() {
        if (null == mConfig.getMemoryTraceConfig()) {
            return INTERVAL;
        } else {
            return checkDelayMillis(mConfig.getMemoryTraceConfig().delayMillis);
        }
    }

    public boolean isUseBatteryTrace() {
        if (null == mConfig.getBatteryTraceConfig()) {
            return false;
        } else {
            return mConfig.getBatteryTraceConfig().isUseTrace;
        }
    }

    public long getBatteryTraceInterval() {
        if (null == mConfig.getBatteryTraceConfig()) {
            return BATTERY_INTERVAL;
        } else {
            return mConfig.getBatteryTraceConfig().delayMillis < BATTERY_INTERVAL ? BATTERY_INTERVAL :
                    mConfig.getBatteryTraceConfig().delayMillis;
        }
    }

    private long checkDelayMillis(long delayMillis) {
        return delayMillis < INTERVAL ? INTERVAL : delayMillis;
    }
}
