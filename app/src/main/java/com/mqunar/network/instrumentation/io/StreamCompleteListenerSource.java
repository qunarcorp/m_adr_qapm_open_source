package com.mqunar.network.instrumentation.io;

/**
 * Created by jingmin.xing on 2015/8/29.
 */
public interface StreamCompleteListenerSource {
    void addStreamCompleteListener(StreamCompleteListener var1);

    void removeStreamCompleteListener(StreamCompleteListener var1);
}
