package com.mqunar.qapm.network.instrumentation.httpclient;

import com.mqunar.qapm.logging.AgentLog;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.network.instrumentation.TransactionState;
import com.mqunar.qapm.network.instrumentation.TransactionStateUtil;
import com.mqunar.qapm.network.instrumentation.io.CountingInputStream;
import com.mqunar.qapm.network.instrumentation.io.CountingOutputStream;
import com.mqunar.qapm.network.instrumentation.io.StreamCompleteEvent;
import com.mqunar.qapm.network.instrumentation.io.StreamCompleteListener;
import com.mqunar.qapm.network.instrumentation.io.StreamCompleteListenerSource;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.message.AbstractHttpMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jingmin.xing on 2015/8/29.
 */
public class HttpResponseEntityImpl implements HttpEntity, StreamCompleteListener {
    private static final String TRANSFER_ENCODING_HEADER = "Transfer-Encoding";
    private static final String ENCODING_CHUNKED = "chunked";
    private final HttpEntity impl;
    private final TransactionState transactionState;
    private final long contentLengthFromHeader;
    private CountingInputStream contentStream;
    private static final AgentLog log = AgentLogManager.getAgentLog();

    public HttpResponseEntityImpl(HttpEntity impl, TransactionState transactionState, long contentLengthFromHeader) {
        this.impl = impl;
        this.transactionState = transactionState;
        this.contentLengthFromHeader = contentLengthFromHeader;
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
        if(this.contentStream != null) {
            return this.contentStream;
        } else {
            try {
                boolean e = true;
                if(this.impl instanceof AbstractHttpMessage) {
                    AbstractHttpMessage entityWrapper = (AbstractHttpMessage)this.impl;
                    Header transferEncodingHeader = entityWrapper.getLastHeader(TRANSFER_ENCODING_HEADER);
                    if(transferEncodingHeader != null && ENCODING_CHUNKED.equalsIgnoreCase(transferEncodingHeader.getValue())) {
                        e = false;
                    }
                } else if(this.impl instanceof HttpEntityWrapper) {
                    HttpEntityWrapper entityWrapper1 = (HttpEntityWrapper)this.impl;
                    e = !entityWrapper1.isChunked();
                }

                this.contentStream = new CountingInputStream(this.impl.getContent(), e);
                this.contentStream.addStreamCompleteListener(this);
                return this.contentStream;
            } catch (IOException var4) {
                this.handleException(var4);
                throw var4;
            }
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
        if(!this.transactionState.isComplete()) {
            CountingOutputStream outputStream = new CountingOutputStream(outstream);

            try {
                this.impl.writeTo(outputStream);
            } catch (IOException var4) {
                this.handleException(var4, Long.valueOf(outputStream.getCount()));
                var4.printStackTrace();
                throw var4;
            }

            if(!this.transactionState.isComplete()) {
                if(this.contentLengthFromHeader >= 0L) {
                    this.transactionState.setBytesReceived(this.contentLengthFromHeader);
                } else {
                    this.transactionState.setBytesReceived(outputStream.getCount());
                }

                this.addTransactionAndErrorData(this.transactionState);
            }
        } else {
            this.impl.writeTo(outstream);
        }

    }

    public void streamComplete(StreamCompleteEvent e) {
        StreamCompleteListenerSource source = (StreamCompleteListenerSource)e.getSource();
        source.removeStreamCompleteListener(this);
        if(!this.transactionState.isComplete()) {
            if(this.contentLengthFromHeader >= 0L) {
                this.transactionState.setBytesReceived(this.contentLengthFromHeader);
            } else {
                this.transactionState.setBytesReceived(e.getBytes());
            }

            this.addTransactionAndErrorData(this.transactionState);
        }

    }

    public void streamError(StreamCompleteEvent e) {
        StreamCompleteListenerSource source = (StreamCompleteListenerSource)e.getSource();
        source.removeStreamCompleteListener(this);
        TransactionStateUtil.setErrorCodeFromException(this.transactionState, e.getException());
        if(!this.transactionState.isComplete()) {
            this.transactionState.setBytesReceived(e.getBytes());
        }

    }

    private void addTransactionAndErrorData(TransactionState transactionState) {
        TransactionStateUtil.end(transactionState);
    }

    private void handleException(Exception e) {
        this.handleException(e, (Long)null);
    }

    private void handleException(Exception e, Long streamBytes) {
        TransactionStateUtil.setErrorCodeFromException(this.transactionState, e);
        if(!this.transactionState.isComplete()) {
            if(streamBytes != null) {
                this.transactionState.setBytesReceived(streamBytes.longValue());
            }
            TransactionStateUtil.end(transactionState);
        }
    }
}
