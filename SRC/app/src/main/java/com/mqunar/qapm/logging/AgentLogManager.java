package com.mqunar.qapm.logging;

/**
 * Created by jingmin.xing on 2015/8/30.
 */
public class AgentLogManager {
    private static DefaultAgentLog instance = new DefaultAgentLog();

    public AgentLogManager() {
    }

    public static AgentLog getAgentLog() {
        return instance;
    }

    public static void setAgentLog(AgentLog log) {
        instance.setImpl(log);
    }
}
