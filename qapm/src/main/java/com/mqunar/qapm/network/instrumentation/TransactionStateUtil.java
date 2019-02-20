package com.mqunar.qapm.network.instrumentation;

import com.mqunar.qapm.QAPM;
import com.mqunar.qapm.dao.Storage;
import com.mqunar.qapm.domain.NetworkData;
import com.mqunar.qapm.logging.AgentLog;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.network.instrumentation.httpclient.HttpRequestEntityImpl;
import com.mqunar.qapm.network.instrumentation.httpclient.HttpResponseEntityImpl;
import com.mqunar.qapm.utils.AndroidUtils;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpHostConnectException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jingmin.xing on 2015/8/30.
 */
public class TransactionStateUtil {
    private static final AgentLog log = AgentLogManager.getAgentLog();
    private static final String CONTENT_LENGTH_HEADER = "Content-Length";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String APP_DATA_HEADER = "X-NewNecro-App-Data";
    public static final String CROSS_PROCESS_ID_HEADER = "X-NewNecro-ID";

    public static final String REQUEST_HEADER_HOST = "Host";
    public static final String REQUEST_HEADER_PITCHER_TYPE = "Pitcher-Type";


    public TransactionStateUtil() {
    }

    private static void inspectAndInstrument(TransactionState transactionState, String url, String httpMethod) {
        log.debug("inspectAndInstrument url " + url);
        transactionState.setUrl(url);
        transactionState.setHttpMethod(httpMethod);
        transactionState.setCarrier(QAPM.getActiveNetworkCarrier());
        transactionState.setWanType(QAPM.getActiveNetworkWanType());
    }

    public static void inspectAndInstrument(TransactionState transactionState, HttpURLConnection conn) {
        inspectAndInstrument(transactionState, conn.getURL().toString(), conn.getRequestMethod());
    }

    private static void inspectAndInstrumentResponse(TransactionState transactionState, String appData, int contentLength, int statusCode) {
        if(appData != null && !"".equals(appData)) {
            transactionState.setAppData(appData);
        }

        if(contentLength >= 0) {
            transactionState.setBytesReceived((long)contentLength);
        }

        transactionState.setStatusCode(statusCode);
    }

    public static void inspectAndInstrumentResponse(TransactionState transactionState, HttpURLConnection conn) {
        String appData = conn.getHeaderField(APP_DATA_HEADER);
        int contentLength = conn.getContentLength();
        int statusCode = -1;

        try {
            statusCode = conn.getResponseCode();
        } catch (IOException var6) {
            log.debug("Failed to retrieve response code due to an I/O exception: " + var6.getMessage());
        } catch (NullPointerException var7) {
            log.error("Failed to retrieve response code due to underlying (Harmony?) NPE", var7);
        }

//        parseConnectionHeader(transactionState, conn);
        inspectAndInstrumentResponse(transactionState, appData, contentLength, statusCode);
    }

    public static HttpRequest inspectAndInstrument(TransactionState transactionState, HttpHost host, HttpRequest request) {
        String url = null;
        RequestLine requestLine = request.getRequestLine();
        if(requestLine != null) {
            String e = requestLine.getUri();
            boolean isAbsoluteUri = e != null && e.length() >= 10 && e.substring(0, 10).indexOf("://") >= 0;
            if(!isAbsoluteUri && e != null && host != null) {
                String prefix = host.toURI().toString();
                url = prefix + (!prefix.endsWith("/") && !e.startsWith("/")?"/":"") + e;
            } else if(isAbsoluteUri) {
                url = e;
            }
        }

        log.debug("inspectAndInstrument url " + url);

        if(transactionState.getUrl() != null && transactionState.getHttpMethod() != null) {
            inspectAndInstrument(transactionState, url, requestLine.getMethod());
            parseRequestHeader(transactionState, request);
            wrapRequestEntity(transactionState, request);
            return request;
        } else {
            try {
                throw new Exception("TransactionData constructor was not provided with a valid URL, host or HTTP method");
            } catch (Exception var8) {
                AgentLogManager.getAgentLog().error(MessageFormat.format("TransactionStateUtil.inspectAndInstrument(...) for {0} could not determine request URL or HTTP method [host={1}, requestLine={2}]", new Object[]{request.getClass().getCanonicalName(), host, requestLine}), var8);
                return request;
            }
        }
    }

    public static HttpUriRequest inspectAndInstrument(TransactionState transactionState, HttpUriRequest request) {
        inspectAndInstrument(transactionState, request.getURI().toString(), request.getMethod());
        parseRequestHeader(transactionState, request);
        wrapRequestEntity(transactionState, request);
        return request;
    }

    private static void wrapRequestEntity(TransactionState transactionState, HttpRequest request) {
        if(request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest entityEnclosingRequest = (HttpEntityEnclosingRequest)request;
            if(entityEnclosingRequest.getEntity() != null) {
                entityEnclosingRequest.setEntity(new HttpRequestEntityImpl(entityEnclosingRequest.getEntity(), transactionState));
            }
        }
    }


    //分析请求header，过滤出需要的header，并添加到bean中。


    private static final String REQUEST_HEADER_CONTENT_TYPE = "Content-Type";
    private static final String REQUEST_HEADER_X_CLIENTENCODING = "X-ClientEncoding";
    //    private static final String REQUEST_HEADER_HOST= "Host";
    private static final String REQUEST_HEADER_CONNECTION = "Connection";
    private static final String REQUEST_HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String REQUEST_HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String REQUEST_HEADER_COOKIE = "Cookie";


    private static void parseRequestHeader(TransactionState transactionState, HttpRequest request) {
        try {
            Header[] headers = request.getAllHeaders();
            if (headers != null) {
                //只收集 request header 中指定字段
                HashMap<String, String> usefulHeaders = new HashMap<>();
                for (Header header : headers) {

                    if (header != null && !"".equals(header.getValue())) {
                        String headerName = header.getName();
                        if (!REQUEST_HEADER_CONTENT_TYPE.equals(headerName)
                                && !REQUEST_HEADER_X_CLIENTENCODING.equals(headerName)
                                && !REQUEST_HEADER_HOST.equals(headerName)
                                && !REQUEST_HEADER_CONNECTION.equals(headerName)
                                && !REQUEST_HEADER_ACCEPT_ENCODING.equals(headerName)
                                && !REQUEST_HEADER_CONTENT_LENGTH.equals(headerName)
                                && !REQUEST_HEADER_COOKIE.equals(headerName)) {
                            usefulHeaders.put(header.getName(), header.getValue());
                        }
                    }
                }
                //  将需要的Header 存起来
                if (usefulHeaders.size() > 0) {
                    transactionState.setHeaders(usefulHeaders);
                }
            }
        }catch (Throwable throwable) {
            //记录header失败
            log.warning("parseRequestHeader Failed parse header: " + throwable.toString());
        }
    }

    private static final String REQUEST_HEADER_PITCHER_URL = "Pitcher-Url";

    static void parseConnectionHeader(TransactionState transactionState, HttpURLConnection conn) {

        try {
            Map<String, List<String>> headers = conn.getRequestProperties();
            if (headers != null) {
                //只收集 request header 中指定字段
                HashMap<String, String> usefulHeaders = new HashMap<>();
                for (Map.Entry<String, List<String>> header : headers.entrySet()) {

                    if (header != null && !"".equals(header.getValue().toString())) {
                        String headerName = header.getKey();
                        if (!REQUEST_HEADER_CONTENT_TYPE.equals(headerName)
                                && !REQUEST_HEADER_X_CLIENTENCODING.equals(headerName)
                                && !REQUEST_HEADER_HOST.equals(headerName)
                                && !REQUEST_HEADER_CONNECTION.equals(headerName)
                                && !REQUEST_HEADER_ACCEPT_ENCODING.equals(headerName)
                                && !REQUEST_HEADER_CONTENT_LENGTH.equals(headerName)
                                && !REQUEST_HEADER_COOKIE.equals(headerName)) {
                            usefulHeaders.put(header.getKey(), header.getValue().toString().replace("[","").replace("]",""));
                        }
                    }
                }

                if (usefulHeaders.size() > 0) {
                    transactionState.setHeaders(usefulHeaders);
                }
            }
        }catch (Throwable throwable) {
            //记录header失败
            log.warning("parseConnectionHeader Failed parse header: " + throwable.toString());
        }
    }

    public static HttpResponse inspectAndInstrument(TransactionState transactionState, HttpResponse response) {
        log.debug("statuscode " + response.getStatusLine().getStatusCode());
        transactionState.setStatusCode(response.getStatusLine().getStatusCode());
        Header[] appDataHeader = response.getHeaders(APP_DATA_HEADER);
        if(appDataHeader != null && appDataHeader.length > 0 && !"".equals(appDataHeader[0].getValue())) {
            transactionState.setAppData(appDataHeader[0].getValue());
        }

        Header[] contentLengthHeader = response.getHeaders(CONTENT_LENGTH_HEADER);
        long contentLengthFromHeader = -1L;
        if(contentLengthHeader != null && contentLengthHeader.length > 0) {
            try {
                contentLengthFromHeader = Long.parseLong(contentLengthHeader[0].getValue());
                transactionState.setBytesReceived(contentLengthFromHeader);
                addTransactionAndErrorData(transactionState, response);
            } catch (NumberFormatException var7) {
                log.warning("Failed to parse content length: " + var7.toString());
            }
        } else if(response.getEntity() != null) {
            response.setEntity(new HttpResponseEntityImpl(response.getEntity(), transactionState, contentLengthFromHeader));
        } else {
            transactionState.setBytesReceived(0L);
            addTransactionAndErrorData(transactionState, (HttpResponse)null);
        }

        return response;
    }

    public static void setErrorCodeFromException(TransactionState transactionState, Exception e) {
        log.error("TransactionStateUtil: Attempting to convert network exception " + e.getClass().getName() + " to error code.");
        transactionState.setErrorMsg(e.getMessage());
        setErrorType(transactionState,e);
    }

    /**
     * 通过exception确定网络错误类型
     * @param transactionState
     * @param e
     */
    private static void setErrorType(TransactionState transactionState,Exception e){
        if(transactionState == null){
            return;
        }
        //badurl timeout unconnect hostErr ioErr sslErr
        if(e == null){
            transactionState.errorType = AndroidUtils.UNKNOWN;
        }else{
            if(e instanceof  SecurityException || e instanceof UnknownHostException
                    || e instanceof IllegalStateException || e instanceof HttpHostConnectException){
                transactionState.errorType = AndroidUtils.UNCONNECT;
            }else if(e instanceof ConnectException){
                if(e.getMessage().contains("TIMEDOUT") || e.getMessage().contains("timed out")){
                    transactionState.errorType = AndroidUtils.TIMEOUT;
                }else if(e.getMessage().contains("ECONNREFUSED") || e.getMessage().contains("Connection refused")){
                    transactionState.errorType = AndroidUtils.UNCONNECT;
                }
            }else if(e instanceof SocketTimeoutException){
                transactionState.errorType = AndroidUtils.TIMEOUT;
            }else{
                transactionState.errorType = AndroidUtils.UNKNOWN;
            }
        }
    }

    private static void addTransactionAndErrorData(TransactionState transactionState, HttpResponse response) {
        end(transactionState);
    }

    public static void end(TransactionState transactionState) {
        NetworkData networkData = transactionState.end();
        if(networkData != null && !networkData.excludeImageData() && !networkData.excludeIllegalData()) {
            Storage.newStorage().putData(networkData);
        }
    }
}
