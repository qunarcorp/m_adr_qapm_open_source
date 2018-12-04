package com.mqunar.logging;

import android.util.Log;

/**
 * Created by jingmin.xing on 2015/8/30.
 */
public class AndroidAgentLog implements AgentLog {
    private static final String TAG = "com.mqunar.necro.agent";
    private int level = INFO;

    public AndroidAgentLog() {
    }

    public void debug(String message) {
        if(this.level == DEBUG) {
            Log.d(TAG, message);
        }

    }

    public void verbose(String message) {
        if(this.level >= VERBOSE) {
            Log.v(TAG, message);
        }

    }

    public void info(String message) {
        if(this.level >= INFO) {
            Log.i(TAG, message);
        }

    }

    public void warning(String message) {
        if(this.level >= WARNING) {
            Log.w(TAG, message);
        }

    }

    public void error(String message) {
        if(this.level >= ERROR) {
            Log.e(TAG, message);
        }

    }

    public void error(String message, Throwable cause) {
        if(this.level >= 1) {
            Log.e(TAG, message, cause);
        }

    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        if(level <= 5 && level >= 1) {
            this.level = level;
        } else {
            throw new IllegalArgumentException("Log level is not between ERROR and DEBUG");
        }
    }
}
