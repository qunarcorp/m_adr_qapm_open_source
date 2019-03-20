package com.mqunar.qapm.logging;


public interface AgentLog {
    int DEBUG = 5;
    int VERBOSE = 4;
    int INFO = 3;
    int WARNING = 2;
    int ERROR = 1;

    void debug(String var1);

    void verbose(String var1);

    void info(String var1);

    void warning(String var1);

    void error(String var1);

    void error(String var1, Throwable var2);

    int getLevel();

    void setLevel(int var1);
}
