package com.mqunar.qapm.logging;

/**
 * Created by jingmin.xing on 2015/8/30.
 */
public class DefaultAgentLog implements AgentLog {
    private AgentLog impl = new NullAgentLog();

    public DefaultAgentLog() {
    }

    public void setImpl(AgentLog impl) {
        synchronized(this) {
            this.impl = impl;
        }
    }

    public void debug(String message) {
        synchronized(this) {
            this.impl.debug(message);
        }
    }

    public void info(String message) {
        synchronized(this) {
            this.impl.info(message);
        }
    }

    public void verbose(String message) {
        synchronized(this) {
            this.impl.verbose(message);
        }
    }

    public void warning(String message) {
        synchronized(this) {
            this.impl.warning(message);
        }
    }

    public void error(String message) {
        synchronized(this) {
            this.impl.error(message);
        }
    }

    public void error(String message, Throwable cause) {
        synchronized(this) {
            this.impl.error(message, cause);
        }
    }

    public int getLevel() {
        synchronized(this) {
            return this.impl.getLevel();
        }
    }

    public void setLevel(int level) {
        synchronized(this) {
            this.impl.setLevel(level);
        }
    }
}
