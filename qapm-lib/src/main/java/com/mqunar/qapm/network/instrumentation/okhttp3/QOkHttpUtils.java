package com.mqunar.qapm.network.instrumentation.okhttp3;

import okhttp3.OkHttpClient;

public class QOkHttpUtils {

    public static OkHttpClient.Builder getOkHttpClientBuilder(){
        OkHttpClient.Builder builer = new OkHttpClient.Builder();
        builer.addInterceptor(new QOkHttpInterceptor());
        return builer;
    }

    public static OkHttpClient getOkHttpClient() {
        return getOkHttpClientBuilder().build();
    }

}
