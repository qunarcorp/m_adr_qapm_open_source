package com.mqunar.qapm.network.instrumentation.io;

/**
 * Created by jingmin.xing on 2015/8/29.
 */
public interface StreamCompleteListener {
    void streamComplete(StreamCompleteEvent var1);

    void streamError(StreamCompleteEvent var1);
}
