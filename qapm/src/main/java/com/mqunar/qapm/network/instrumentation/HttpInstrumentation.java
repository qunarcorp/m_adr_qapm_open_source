package com.mqunar.qapm.network.instrumentation;

import com.mqunar.qapm.network.instrumentation.httpclient.ResponseHandlerImpl;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by jingmin.xing on 2015/8/30.
 */
public final class   HttpInstrumentation {
    private HttpInstrumentation() {
    }

    @WrapReturn(
            className = "java/net/URL",
            methodName = "openConnection",
            methodDesc = "()Ljava/net/URLConnection;"
    )
    public static URLConnection openConnection(URLConnection connection) {
        return (URLConnection)(connection instanceof HttpsURLConnection ?new HttpsURLConnectionExtension((HttpsURLConnection)connection):(connection instanceof HttpURLConnection ?new HttpURLConnectionExtension((HttpURLConnection)connection):connection));
    }

    @WrapReturn(
            className = "java.net.URL",
            methodName = "openConnection",
            methodDesc = "(Ljava/net/Proxy;)Ljava/net/URLConnection;"
    )
    public static URLConnection openConnectionWithProxy(URLConnection connection) {
        return (URLConnection)(connection instanceof HttpsURLConnection ?new HttpsURLConnectionExtension((HttpsURLConnection)connection):(connection instanceof HttpURLConnection ?new HttpURLConnectionExtension((HttpURLConnection)connection):connection));
    }

    @ReplaceCallSite
    public static HttpResponse execute(HttpClient httpClient, HttpHost target, HttpRequest request, HttpContext context) throws IOException {
        TransactionState transactionState = new TransactionState();

        try {
            return inspectAndInstrument(httpClient.execute(target, inspectAndInstrument(target, request, transactionState), context), transactionState);
        } catch (IOException ioe) {
            httpClientError(transactionState, ioe);
            throw ioe;
        } catch (Exception e) {
            httpClientError(transactionState, e);
            throw e;
        }
    }

    @ReplaceCallSite
    public static <T> T execute(HttpClient httpClient, HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        TransactionState transactionState = new TransactionState();

        try {
            return httpClient.execute(target, inspectAndInstrument(target, request, transactionState), inspectAndInstrument(responseHandler, transactionState), context);
        } catch (ClientProtocolException cpe) {
            httpClientError(transactionState, cpe);
            throw cpe;
        } catch (IOException ioe) {
            httpClientError(transactionState, ioe);
            throw ioe;
        } catch (Exception e) {
            httpClientError(transactionState, e);
            throw e;
        }
    }

    @ReplaceCallSite
    public static <T> T execute(HttpClient httpClient, HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        TransactionState transactionState = new TransactionState();

        try {
            return httpClient.execute(target, inspectAndInstrument(target, request, transactionState), inspectAndInstrument(responseHandler, transactionState));
        } catch (ClientProtocolException cpe) {
            httpClientError(transactionState, cpe);
            throw cpe;
        } catch (IOException ioe) {
            httpClientError(transactionState, ioe);
            throw ioe;
        } catch (Exception e) {
            httpClientError(transactionState, e);
            throw e;
        }
    }

    @ReplaceCallSite
    public static HttpResponse execute(HttpClient httpClient, HttpHost target, HttpRequest request) throws IOException {
        TransactionState transactionState = new TransactionState();

        try {
            return inspectAndInstrument(httpClient.execute(target, inspectAndInstrument(target, request, transactionState)), transactionState);
        } catch (IOException ioe) {
            httpClientError(transactionState, ioe);
            throw ioe;
        } catch (Exception e) {
            httpClientError(transactionState, e);
            throw e;
        }
    }

    @ReplaceCallSite
    public static HttpResponse execute(HttpClient httpClient, HttpUriRequest request, HttpContext context) throws IOException {
        TransactionState transactionState = new TransactionState();

        try {
            return inspectAndInstrument(httpClient.execute(inspectAndInstrument(request, transactionState), context), transactionState);
        } catch (IOException ioe) {
            httpClientError(transactionState, ioe);
            throw ioe;
        } catch (Exception e) {
            httpClientError(transactionState, e);
            throw e;
        }
    }

    @ReplaceCallSite
    public static <T> T execute(HttpClient httpClient, HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        TransactionState transactionState = new TransactionState();

        try {
            return httpClient.execute(inspectAndInstrument(request, transactionState), inspectAndInstrument(responseHandler, transactionState), context);
        } catch (ClientProtocolException cpe) {
            httpClientError(transactionState, cpe);
            throw cpe;
        } catch (IOException ioe) {
            httpClientError(transactionState, ioe);
            throw ioe;
        } catch (Exception e) {
            httpClientError(transactionState, e);
            throw e;
        }
    }

    @ReplaceCallSite
    public static <T> T execute(HttpClient httpClient, HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        TransactionState transactionState = new TransactionState();

        try {
            return httpClient.execute(inspectAndInstrument(request, transactionState), inspectAndInstrument(responseHandler, transactionState));
        } catch (ClientProtocolException cpe) {
            httpClientError(transactionState, cpe);
            throw cpe;
        } catch (IOException ioe) {
            httpClientError(transactionState, ioe);
            throw ioe;
        } catch (Exception e) {
            httpClientError(transactionState, e);
            throw e;
        }
    }

    @ReplaceCallSite
    public static HttpResponse execute(HttpClient httpClient, HttpUriRequest request) throws IOException {
        TransactionState transactionState = new TransactionState();

        try {
            return inspectAndInstrument(httpClient.execute(inspectAndInstrument(request, transactionState)), transactionState);
        } catch (IOException ioe) {
            httpClientError(transactionState, ioe);
            throw ioe;
        } catch (Exception e) {
            httpClientError(transactionState, e);
            throw e;
        }
    }

    private static void httpClientError(TransactionState transactionState, Exception e) {
        if(!transactionState.isComplete()) {
            TransactionStateUtil.setErrorCodeFromException(transactionState, e);
            TransactionStateUtil.end(transactionState);
        }
    }

    private static HttpUriRequest inspectAndInstrument(HttpUriRequest request, TransactionState transactionState) {
        return TransactionStateUtil.inspectAndInstrument(transactionState, request);
    }

    private static HttpRequest inspectAndInstrument(HttpHost host, HttpRequest request, TransactionState transactionState) {
        return TransactionStateUtil.inspectAndInstrument(transactionState, host, request);
    }

    private static HttpResponse inspectAndInstrument(HttpResponse response, TransactionState transactionState) {
        return TransactionStateUtil.inspectAndInstrument(transactionState, response);
    }

    private static <T> ResponseHandler<? extends T> inspectAndInstrument(ResponseHandler<? extends T> handler, TransactionState transactionState) {
        return ResponseHandlerImpl.wrap(handler, transactionState);
    }
}
