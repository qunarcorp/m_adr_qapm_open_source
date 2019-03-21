package com.qunar.qapm.demo.okhttp;

import android.util.Log;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpUtils {
    public static String TAG = OkHttpUtils.class.getSimpleName();

    /**
     * okhttp异步网络请求
     *
     * @param url      请求URL
     * @param callback 网络回调，可以为空
     */
    public static void syncGetRequest(String url, final Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HttpUrl requestUrl = call.request().url();
                Log.d(TAG, "okhttp request [" + requestUrl + "] failuer");
                if (callback != null) {
                    callback.onFailure(call, e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                Log.d(TAG, "response : " + response.body().string());
                if (callback != null) {
                    callback.onResponse(call, response);
                }
            }
        });
    }


    /**
     * okhttp同步网络请求，请求方确认在子线程执行
     *
     * @param url 请求URL
     */
    public static String asyncGetRequest(String url) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(url).get().build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            String result = response.body().string();
            Log.d(TAG, "run: " + result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 异步post请求
     *
     * @param url
     * @param requestBody
     * @param callback
     */
    public static void syncPostRequest(String url, Map<String, String> requestBody, final Callback callback) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : requestBody.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        Request request = new Request.Builder()
                .url(url).post(builder.build())
                .build();
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HttpUrl requestUrl = call.request().url();
                Log.d(TAG, "okhttp request [" + requestUrl + "] failuer");
                if (callback != null) {
                    callback.onFailure(call, e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, response.protocol() + " " +response.code() + " " + response.message());
                Headers headers = response.headers();
                for (int i = 0; i < headers.size(); i++) {
                    Log.d(TAG, headers.name(i) + ":" + headers.value(i));
                }
//                Log.d(TAG, "onResponse: " + response.body().string());
                if (callback != null) {
                    callback.onResponse(call, response);
                }
            }
        });
    }


}
