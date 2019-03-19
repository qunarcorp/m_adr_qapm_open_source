package com.mqunar.qapm.network.instrumentation.okhttp3;

import com.mqunar.qapm.domain.NetworkData;
import com.mqunar.qapm.logging.AgentLog;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.network.instrumentation.TransactionState;
import com.mqunar.qapm.network.instrumentation.TransactionStateUtil;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class QOkHttpInterceptor implements Interceptor {

    public static final String TAG = QOkHttpInterceptor.class.getSimpleName();
    private static final AgentLog log = AgentLogManager.getAgentLog();

    private TransactionState transactionState;

    @Override
    public Response intercept(Chain chain) throws IOException {
        long startNs = System.currentTimeMillis();
        transactionState = new TransactionState();
        Request request = chain.request();
//        Log.d(TAG, " okhttp start request Url：" + request.url());
        recordRequest(request);
        Response response;
        try {
            response = chain.proceed(request);
        } catch (IOException e) {
            if (!transactionState.isComplete()) {
                TransactionStateUtil.setErrorCodeFromException(transactionState, e);
                TransactionStateUtil.end(transactionState);
            }
            throw e;
        }

        long costTime = System.currentTimeMillis() - startNs;
//        Log.d(TAG, " okhttp end request Url：" + request.url() + " 耗时：" + costTime);
        recordResponse(response);
        TransactionStateUtil.end(transactionState);
        NetworkData networkData = transactionState.end();
        if(networkData != null) {
            log.debug(TAG + "recordData = " + networkData.toJSONObject().toString());
        }
        return response;
    }

    /**
     * 记录OKHTTP request
     *
     * @param request
     */
    private void recordRequest(okhttp3.Request request) {
        TransactionStateUtil.inspectAndInstrument(transactionState, request.url().toString(), request.method());
        TransactionStateUtil.parseRequestHeader(transactionState, request);
        long requestLength = request.url().toString().getBytes().length;
        if (request.body() != null) {
            try {
                requestLength = request.body().contentLength();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        transactionState.setBytesSent(requestLength);
    }

    /**
     * 设置 code responseSize
     */
    private void recordResponse(Response response) {
        if (response == null) {
            return;
        }
        transactionState.setStatusCode(response.code());
//        Log.d(TAG, "okhttp chain.proceed 状态码：" + response.code());
        if (!response.isSuccessful()) {
            return;
        }
        long contentLength = 0;
        Headers headers = response.headers();
        if (headers != null) {
            try {
                contentLength = Long.valueOf(headers.get("Content-Length"));
                if (contentLength > 0) {
                    transactionState.setBytesReceived(contentLength);
//                    Log.d(TAG, "通过response Header 取到contentLength:" + contentLength);
                    return;
                }
            } catch (NumberFormatException e) {
//                Log.d(TAG, "Failed to parse content length: " + e.toString());
            }
        }

        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            contentLength = responseBody.contentLength();
            if (contentLength <= 0) {
                BufferedSource source = responseBody.source();
                if (source != null) {
                    try {
                        source.request(Long.MAX_VALUE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Buffer buffer = source.buffer();
                    contentLength = buffer.size();
//                    Log.d(TAG, "通过responseBody.source()才取到contentLength:" + contentLength);
                }
            }
        }

        transactionState.setBytesReceived(contentLength);
//        Log.d(TAG, "okhttp 接收字节数：" + contentLength);
    }
}