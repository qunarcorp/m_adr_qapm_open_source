package com.qunar.moudle.okhttp;

import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpUtils {
    public static String TAG = OkHttpUtils.class.getSimpleName();

    /**
     * okhttp异步网络请求
     *
     * @param url 请求URL
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
                Log.d(TAG, "response : " + response.body().string());
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
    public static void startRequest(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            Log.d(TAG, "run: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
