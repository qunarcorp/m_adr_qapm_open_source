package com.mqunar.network.instrumentation.httpclient;

import com.mqunar.network.instrumentation.io.CountingInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jingmin.xing on 2015/8/29.
 */
public class ContentBufferingResponseEntityImpl implements HttpEntity {
    final HttpEntity impl;
    private CountingInputStream contentStream;

    public ContentBufferingResponseEntityImpl(HttpEntity impl) {
        if(impl == null) {
            throw new IllegalArgumentException("Missing wrapped entity");
        } else {
            this.impl = impl;
        }
    }

    public void consumeContent() throws IOException {
        this.impl.consumeContent();
    }

    public InputStream getContent() throws IOException, IllegalStateException {
        if(this.contentStream != null) {
            return this.contentStream;
        } else {
            this.contentStream = new CountingInputStream(this.impl.getContent(), true);
            return this.contentStream;
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

    public void writeTo(OutputStream outputStream) throws IOException {
        this.impl.writeTo(outputStream);
    }
}
