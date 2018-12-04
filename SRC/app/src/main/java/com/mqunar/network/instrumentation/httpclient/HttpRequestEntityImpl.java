package com.mqunar.network.instrumentation.httpclient;

import com.mqunar.network.instrumentation.TransactionState;
import com.mqunar.network.instrumentation.TransactionStateUtil;
import com.mqunar.network.instrumentation.io.CountingInputStream;
import com.mqunar.network.instrumentation.io.CountingOutputStream;
import com.mqunar.network.instrumentation.io.StreamCompleteEvent;
import com.mqunar.network.instrumentation.io.StreamCompleteListener;
import com.mqunar.network.instrumentation.io.StreamCompleteListenerSource;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jingmin.xing on 2015/8/29.
 */
public class HttpRequestEntityImpl implements HttpEntity, StreamCompleteListener {
    private final HttpEntity impl;
    private final TransactionState transactionState;

    public HttpRequestEntityImpl(HttpEntity impl, TransactionState transactionState) {
        this.impl = impl;
        this.transactionState = transactionState;
    }

    public void consumeContent() throws IOException {
        try {
            this.impl.consumeContent();
        } catch (IOException var2) {
            this.handleException(var2);
            throw var2;
        }
    }

    public InputStream getContent() throws IOException, IllegalStateException {
        try {
            if(!this.transactionState.isSent()) {
                CountingInputStream e = new CountingInputStream(this.impl.getContent());
                e.addStreamCompleteListener(this);
                return e;
            } else {
                return this.impl.getContent();
            }
        } catch (IOException var2) {
            this.handleException(var2);
            throw var2;
        } catch (IllegalStateException var3) {
            this.handleException(var3);
            throw var3;
        }
    }

    public Header getContentEncoding() {
        return this.impl.getContentEncoding();
    }

    public long getContentLength() {
        return this.impl.getContentLength();
    }

    public Header getContentType() {
        return this.impl.getContentType();
    }

    public boolean isChunked() {
        return this.impl.isChunked();
    }

    public boolean isRepeatable() {
        return this.impl.isRepeatable();
    }

    public boolean isStreaming() {
        return this.impl.isStreaming();
    }

    public void writeTo(OutputStream outstream) throws IOException {
        try {
            if(!this.transactionState.isSent()) {
                CountingOutputStream e = new CountingOutputStream(outstream);
                this.impl.writeTo(e);
                this.transactionState.setBytesSent(e.getCount());
            } else {
                this.impl.writeTo(outstream);
            }

        } catch (IOException var3) {
            this.handleException(var3);
            throw var3;
        }
    }

    public void streamComplete(StreamCompleteEvent e) {
        StreamCompleteListenerSource source = (StreamCompleteListenerSource)e.getSource();
        source.removeStreamCompleteListener(this);
        this.transactionState.setBytesSent(e.getBytes());
    }

    public void streamError(StreamCompleteEvent e) {
        StreamCompleteListenerSource source = (StreamCompleteListenerSource)e.getSource();
        source.removeStreamCompleteListener(this);
        this.handleException(e.getException(), Long.valueOf(e.getBytes()));
    }

    private void handleException(Exception e) {
        this.handleException(e, (Long)null);
    }

    private void handleException(Exception e, Long streamBytes) {
        TransactionStateUtil.setErrorCodeFromException(this.transactionState, e);
        if(!this.transactionState.isComplete()) {
            if(streamBytes != null) {
                this.transactionState.setBytesSent(streamBytes.longValue());
            }
            TransactionStateUtil.end(transactionState);
        }
    }
}
