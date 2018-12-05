package com.mqunar.qapm.network.instrumentation;

import android.location.Location;

import com.mqunar.qapm.domain.NetworkData;
import com.mqunar.qapm.logging.AgentLog;
import com.mqunar.qapm.logging.AgentLogManager;
import com.mqunar.qapm.tracing.BackgroundTrace;
import com.mqunar.qapm.utils.AndroidUtils;
import com.mqunar.qapm.utils.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;


/**
 * Created by jingmin.xing on 2015/8/30.
 */
public final class TransactionState {
    private static final AgentLog log = AgentLogManager.getAgentLog();

    private static final String SUCCESS = "success";
    private static final String ERROR = "error";

    private static final int BACKGROUND_START_TIME = -1;

    private String url;
    private String httpMethod;
    private String errMsg;
    private String appData;
    private String carrier = AndroidUtils.UNKNOWN;
    private String wanType = AndroidUtils.UNKNOWN;
    private State state;
    private String contentType;
    private String netStatus = ERROR ;

    private int statusCode = -1;

    private long bytesSent;
    private long bytesReceived;
    private long startTime;
    private long endTime;

    private HashMap<String,String> headers;

    public TransactionState() {
        this.state = State.READY;
        startTime = !BackgroundTrace.appIsForeground() ? BACKGROUND_START_TIME : System.currentTimeMillis();
    }

    public void setCarrier(String carrier) {
        if(!this.isSent()) {
            this.carrier = carrier;
        } else {
            log.warning("setCarrier(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }

    public void setWanType(String wanType) {
        if(!this.isSent()) {
            this.wanType = wanType;
        } else {
            log.warning("setWanType(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }

    public void setAppData(String appData) {
        if(!this.isComplete()) {
            this.appData = appData;
        } else {
            log.warning("setAppData(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }

    public void setUrl(String urlString) {
        log.debug("setUrl urlString " + urlString);
        String url = StringUtils.sanitizeUrl(urlString);
        log.debug("setUrl sanitizeUrl url " + url);
        if(url != null) {
            if(!this.isSent()) {
                this.url = url;
            } else {
                log.warning("setUrl(...) called on TransactionState in " + this.state.toString() + " state");
            }
        }
    }

    public void setHttpMethod(String httpMethod) {
        if(!this.isSent()) {
            this.httpMethod = httpMethod;
        } else {
            log.warning("setHttpMethod(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }

    public String getUrl() {
        return this.url;
    }

    public String getHttpMethod() {
        return this.httpMethod;
    }

    public boolean isSent() {
        return this.state.ordinal() >= State.SENT.ordinal();
    }

    public boolean isComplete() {
        return this.state.ordinal() >= State.COMPLETE.ordinal();
    }

    public void setStatusCode(int statusCode) {
        if(!this.isComplete()) {
            this.statusCode = statusCode;
            this.netStatus = statusCode > 100 && statusCode < 399 ? SUCCESS : ERROR;
        } else {
            log.warning("setStatusCode(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }

    public void setErrorMsg(String errMsg) {
        if(!this.isComplete()) {
            this.errMsg = errMsg;
        } else {
            log.warning("setErrorCode(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }

    public void setBytesSent(long bytesSent) {
        if(!this.isComplete()) {
            this.bytesSent = bytesSent;
            this.state = State.SENT;
        } else {
            log.warning("setBytesSent(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }

    public void setBytesReceived(long bytesReceived) {
        if(!this.isComplete()) {
            this.bytesReceived = bytesReceived;
        } else {
            log.warning("setBytesReceived(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }

    public void setHeaders(HashMap<String, String> headers) {
        if(!this.isComplete()) {
            this.headers = headers;
        } else {
            log.warning("setHeaders(...) called on TransactionState in " + this.state.toString() + " state");
        }
    }

    public NetworkData end() {
        if(!this.isComplete()) {
            this.state = State.COMPLETE;
            this.endTime = System.currentTimeMillis();
        }
        return toSaveData();
    }

    private NetworkData toSaveData() {
        if(!this.isComplete()) {
            log.warning("toTransactionData() called on incomplete TransactionState");
        }

        if(this.url == null) {
            log.error("Attempted to convert a TransactionState instance with no URL into a TransactionData");
            return null;
        } else {
            NetworkData network = new NetworkData();
            network.reqUrl = url;
            network.startTime = String.valueOf(startTime); // 网络请求开始时的时间戳,精确到毫秒
            network.endTime = String.valueOf(endTime); // 网络请求结束或者出错时的时间戳,精确到毫秒
            network.reqSize = String.valueOf(bytesSent); // 网络请求大小，单位为*字节*
            network.resSize = String.valueOf(bytesReceived); // 收到的网络响应数据大小，单位为*字节*
            network.httpCode = statusCode == BACKGROUND_START_TIME ? AndroidUtils.UNKNOWN : String.valueOf(statusCode); // HTTP 请求的状态码，0表示正常，如“404”、“503”、“300”等
            network.netStatus = netStatus; // 当前网络请求的成功与否
            network.topPage = BackgroundTrace.getCurrentActivityName(); // 发起网络请求的Activity
            network.hf = errMsg; // http发生异常的原因,可选
            network.netType = wanType; // 发送网络请求时的网络类型，可选值为：“2G”、“3G”、“4G”、“Wifi”，“Cellular”，“Unknow”
            network.headers = headers; // 请求头--会做一些过滤
            return network;
        }
    }

    private String getLocation() {//大客户端暂时先反射大客户端
        try {
            Class<?> objClz = Class.forName("qunar.sdk.location.LocationFacade");
            Method method = objClz.getDeclaredMethod("getNewestCacheLocation");
            Location location = (Location) method.invoke(null);
            if (location != null) {
                return location.getLongitude() + "," + location.getLatitude();
            }
        } catch (Throwable e) {
//            QLog.e(e);
        }
        return "";
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    private static enum State {
        READY,
        SENT,
        COMPLETE;
        private State() {}
    }
}
