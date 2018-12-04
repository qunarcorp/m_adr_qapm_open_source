package com.mqunar.network.instrumentation.io;

import java.util.EventObject;

/**
 * Created by jingmin.xing on 2015/8/29.
 */
public class StreamCompleteEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private final long bytes;
    private final Exception exception;

    public StreamCompleteEvent(Object source, long bytes, Exception exception) {
        super(source);
        this.bytes = bytes;
        this.exception = exception;
    }

    public StreamCompleteEvent(Object source, long bytes) {
        this(source, bytes, (Exception)null);
    }

    public long getBytes() {
        return this.bytes;
    }

    public Exception getException() {
        return this.exception;
    }

    public boolean isError() {
        return this.exception != null;
    }
}
