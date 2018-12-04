package com.mqunar.logging;

/**
 * Created by jingmin.xing on 2015/8/30.
 */
public class NullAgentLog implements AgentLog {
    public NullAgentLog() {
    }

    public void debug(String message) {
    }

    public void info(String message) {
    }

    public void verbose(String message) {
    }

    public void error(String message) {
    }

    public void error(String message, Throwable cause) {
    }

    public void warning(String message) {
    }

    public int getLevel() {
        return 5;
    }

    public void setLevel(int level) {
    }
}
