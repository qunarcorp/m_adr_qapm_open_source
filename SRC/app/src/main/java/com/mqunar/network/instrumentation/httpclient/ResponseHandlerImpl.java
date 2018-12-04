package com.mqunar.network.instrumentation.httpclient;


import com.mqunar.network.instrumentation.TransactionState;
import com.mqunar.network.instrumentation.TransactionStateUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import java.io.IOException;

/**
 * Created by jingmin.xing on 2015/8/29.
 */
public class ResponseHandlerImpl <T> implements ResponseHandler<T> {
    private final ResponseHandler<T> impl;
    private final TransactionState transactionState;

    private ResponseHandlerImpl(ResponseHandler<T> impl, TransactionState transactionState) {
        this.impl = impl;
        this.transactionState = transactionState;
    }

    public T handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        TransactionStateUtil.inspectAndInstrument(this.transactionState, response);
        return this.impl.handleResponse(response);
    }

    public static <T> ResponseHandler<? extends T> wrap(ResponseHandler<? extends T> impl, TransactionState transactionState) {
        return new ResponseHandlerImpl(impl, transactionState);
    }
}
