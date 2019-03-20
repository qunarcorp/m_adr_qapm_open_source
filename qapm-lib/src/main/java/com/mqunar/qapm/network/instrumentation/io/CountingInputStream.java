package com.mqunar.qapm.network.instrumentation.io;

import com.mqunar.qapm.logging.AgentLog;
import com.mqunar.qapm.logging.AgentLogManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by jingmin.xing on 2015/8/29.
 */
public final class CountingInputStream extends InputStream implements StreamCompleteListenerSource {

    public static final int DEFAULT_RESPONSE_BODY_LIMIT = 2048;

    private final InputStream impl;
    private long count = 0L;
    private final StreamCompleteListenerManager listenerManager = new StreamCompleteListenerManager();
    private final ByteBuffer buffer;
    private boolean enableBuffering = false;
    private static final AgentLog log = AgentLogManager.getAgentLog();

    public CountingInputStream(InputStream impl) {
        this.impl = impl;
        if(this.enableBuffering) {
            this.buffer = ByteBuffer.allocate(DEFAULT_RESPONSE_BODY_LIMIT);
            this.fillBuffer();
        } else {
            this.buffer = null;
        }

    }

    public CountingInputStream(InputStream impl, boolean enableBuffering) {
        this.impl = impl;
        this.enableBuffering = enableBuffering;
        if(enableBuffering) {
            this.buffer = ByteBuffer.allocate(DEFAULT_RESPONSE_BODY_LIMIT);
            this.fillBuffer();
        } else {
            this.buffer = null;
        }

    }

    public void addStreamCompleteListener(StreamCompleteListener streamCompleteListener) {
        this.listenerManager.addStreamCompleteListener(streamCompleteListener);
    }

    public void removeStreamCompleteListener(StreamCompleteListener streamCompleteListener) {
        this.listenerManager.removeStreamCompleteListener(streamCompleteListener);
    }

    public int read() throws IOException {
        int n;
        if(this.enableBuffering) {
            ByteBuffer e = this.buffer;
            synchronized(this.buffer) {
                if(this.bufferHasBytes(1L)) {
                    n = this.readBuffer();
                    if(n >= 0) {
                        ++this.count;
                    }

                    return n;
                }
            }
        }

        try {
            n = this.impl.read();
            if(n >= 0) {
                ++this.count;
            } else {
                this.notifyStreamComplete();
            }

            return n;
        } catch (IOException var4) {
            this.notifyStreamError(var4);
            throw var4;
        }
    }

    public int read(byte[] b) throws IOException {
        boolean n = false;
        int numBytesFromBuffer = 0;
        int inputBufferRemaining = b.length;
        int n1;
        if(this.enableBuffering) {
            ByteBuffer e = this.buffer;
            synchronized(this.buffer) {
                if(this.bufferHasBytes((long)inputBufferRemaining)) {
                    n1 = this.readBufferBytes(b);
                    if(n1 >= 0) {
                        this.count += (long)n1;
                        return n1;
                    }

                    throw new IOException("readBufferBytes failed");
                }

                int remaining = this.buffer.remaining();
                if(remaining > 0) {
                    numBytesFromBuffer = this.readBufferBytes(b, 0, remaining);
                    if(numBytesFromBuffer < 0) {
                        throw new IOException("partial read from buffer failed");
                    }

                    inputBufferRemaining -= numBytesFromBuffer;
                    this.count += (long)numBytesFromBuffer;
                }
            }
        }

        try {
            n1 = this.impl.read(b, numBytesFromBuffer, inputBufferRemaining);
            if(n1 >= 0) {
                this.count += (long)n1;
                return n1 + numBytesFromBuffer;
            } else if(numBytesFromBuffer <= 0) {
                this.notifyStreamComplete();
                return n1;
            } else {
                return numBytesFromBuffer;
            }
        } catch (IOException var8) {
            log.error(var8.toString());
            System.out.println("NOTIFY STREAM ERROR: " + var8);
            var8.printStackTrace();
            this.notifyStreamError(var8);
            throw var8;
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {
        boolean n = false;
        int numBytesFromBuffer = 0;
        int inputBufferRemaining = len;
        int n1;
        if(this.enableBuffering) {
            ByteBuffer e = this.buffer;
            synchronized(this.buffer) {
                if(this.bufferHasBytes((long)inputBufferRemaining)) {
                    n1 = this.readBufferBytes(b, off, len);
                    if(n1 >= 0) {
                        this.count += (long)n1;
                        return n1;
                    }

                    throw new IOException("readBufferBytes failed");
                }

                int remaining = this.buffer.remaining();
                if(remaining > 0) {
                    numBytesFromBuffer = this.readBufferBytes(b, off, remaining);
                    if(numBytesFromBuffer < 0) {
                        throw new IOException("partial read from buffer failed");
                    }

                    inputBufferRemaining -= numBytesFromBuffer;
                    this.count += (long)numBytesFromBuffer;
                }
            }
        }

        try {
            n1 = this.impl.read(b, off + numBytesFromBuffer, inputBufferRemaining);
            if(n1 >= 0) {
                this.count += (long)n1;
                return n1 + numBytesFromBuffer;
            } else if(numBytesFromBuffer <= 0) {
                this.notifyStreamComplete();
                return n1;
            } else {
                return numBytesFromBuffer;
            }
        } catch (IOException var10) {
            this.notifyStreamError(var10);
            throw var10;
        }
    }

    public long skip(long byteCount) throws IOException {
        long toSkip = byteCount;
        if(this.enableBuffering) {
            ByteBuffer e = this.buffer;
            synchronized(this.buffer) {
                if(this.bufferHasBytes(byteCount)) {
                    this.buffer.position((int)byteCount);
                    this.count += byteCount;
                    return byteCount;
                }

                toSkip = byteCount - (long)this.buffer.remaining();
                if(toSkip <= 0L) {
                    throw new IOException("partial read from buffer (skip) failed");
                }

                this.buffer.position(this.buffer.remaining());
            }
        }

        try {
            long n = this.impl.skip(toSkip);
            this.count += n;
            return n;
        } catch (IOException var9) {
            this.notifyStreamError(var9);
            throw var9;
        }
    }

    public int available() throws IOException {
        int remaining = 0;
        if(this.enableBuffering) {
            remaining = this.buffer.remaining();
        }

        try {
            return remaining + this.impl.available();
        } catch (IOException var3) {
            this.notifyStreamError(var3);
            throw var3;
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

    public void mark(int readlimit) {
        if(this.markSupported()) {
            this.impl.mark(readlimit);
        }
    }

    public boolean markSupported() {
        return this.impl.markSupported();
    }

    public void reset() throws IOException {
        if(this.markSupported()) {
            try {
                this.impl.reset();
            } catch (IOException var2) {
                this.notifyStreamError(var2);
                throw var2;
            }
        }
    }

    private int readBuffer() {
        return this.bufferEmpty()?-1:this.buffer.get();
    }

    private int readBufferBytes(byte[] bytes) {
        return this.readBufferBytes(bytes, 0, bytes.length);
    }

    private int readBufferBytes(byte[] bytes, int offset, int length) {
        if(this.bufferEmpty()) {
            return -1;
        } else {
            int remainingBefore = this.buffer.remaining();
            this.buffer.get(bytes, offset, length);
            return remainingBefore - this.buffer.remaining();
        }
    }

    private boolean bufferHasBytes(long num) {
        return (long)this.buffer.remaining() >= num;
    }

    private boolean bufferEmpty() {
        return !this.buffer.hasRemaining();
    }

    public void fillBuffer() {
        if(this.buffer != null) {
            if(!this.buffer.hasArray()) {
                return;
            }

            ByteBuffer var1 = this.buffer;
            synchronized(this.buffer) {
                int bytesRead = 0;

                try {
                    bytesRead = this.impl.read(this.buffer.array(), 0, this.buffer.capacity());
                } catch (IOException var5) {
                    log.error(var5.toString());
                }

                if(bytesRead <= 0) {
                    this.buffer.limit(0);
                } else if(bytesRead < this.buffer.capacity()) {
                    this.buffer.limit(bytesRead);
                }
            }
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

    public void setBufferingEnabled(boolean enableBuffering) {
        this.enableBuffering = enableBuffering;
    }

    public String getBufferAsString() {
        if(this.buffer != null) {
            ByteBuffer var1 = this.buffer;
            synchronized(this.buffer) {
                byte[] buf = new byte[this.buffer.limit()];

                for(int i = 0; i < this.buffer.limit(); ++i) {
                    buf[i] = this.buffer.get(i);
                }

                return new String(buf);
            }
        } else {
            return "";
        }
    }
}
