package com.mqunar.qapm.network.instrumentation.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jingmin.xing on 2015/8/29.
 */
public final class CountingOutputStream extends OutputStream implements StreamCompleteListenerSource {
    private final OutputStream impl;
    private long count = 0L;
    private final StreamCompleteListenerManager listenerManager = new StreamCompleteListenerManager();

    public CountingOutputStream(OutputStream impl) {
        this.impl = impl;
    }

    public void addStreamCompleteListener(StreamCompleteListener streamCompleteListener) {
        this.listenerManager.addStreamCompleteListener(streamCompleteListener);
    }

    public void removeStreamCompleteListener(StreamCompleteListener streamCompleteListener) {
        this.listenerManager.removeStreamCompleteListener(streamCompleteListener);
    }

    public long getCount() {
        return this.count;
    }

    public void write(int oneByte) throws IOException {
        try {
            this.impl.write(oneByte);
            ++this.count;
        } catch (IOException var3) {
            this.notifyStreamError(var3);
            throw var3;
        }
    }

    public void write(byte[] buffer) throws IOException {
        try {
            this.impl.write(buffer);
            this.count += (long)buffer.length;
        } catch (IOException var3) {
            this.notifyStreamError(var3);
            throw var3;
        }
    }

    public void write(byte[] buffer, int offset, int count) throws IOException {
        try {
            this.impl.write(buffer, offset, count);
            this.count += (long)count;
        } catch (IOException var5) {
            this.notifyStreamError(var5);
            throw var5;
        }
    }

    public void flush() throws IOException {
        try {
            this.impl.flush();
        } catch (IOException var2) {
            this.notifyStreamError(var2);
            throw var2;
        }
    }

    public void close() throws IOException {
        try {
            this.impl.close();
            this.notifyStreamComplete();
        } catch (IOException var2) {
            this.notifyStreamError(var2);
            throw var2;
        }
    }

    private void notifyStreamComplete() {
        if(!this.listenerManager.isComplete()) {
            this.listenerManager.notifyStreamComplete(new StreamCompleteEvent(this, this.count));
        }

    }

    private void notifyStreamError(Exception e) {
        if(!this.listenerManager.isComplete()) {
            this.listenerManager.notifyStreamError(new StreamCompleteEvent(this, this.count, e));
        }

    }
}
