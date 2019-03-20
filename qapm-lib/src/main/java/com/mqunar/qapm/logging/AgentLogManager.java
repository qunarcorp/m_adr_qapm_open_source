package com.mqunar.qapm.logging;


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
