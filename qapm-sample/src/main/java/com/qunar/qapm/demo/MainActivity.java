package com.qunar.qapm.demo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.qunar.qapm.demo.okhttp.OkHttpUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends Activity implements Callback {
    String[] permissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    List<String> mPermissionList = new ArrayList<>();
    TextView resultView;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultView = findViewById(R.id.result);
        requestPermiss();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermiss() {
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }
        if (mPermissionList.isEmpty()) {
            showToast("已经授权");
        } else {
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            requestPermissions(permissions, 1000);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("权限已申请");
            } else {
                showToast("权限已拒绝");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showToast(String string) {
        Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.okhttp_sync_get:
                String url = "https://reg.163.com/logins.jsp?id=helloworld&pwd=android";
                OkHttpUtils.syncGetRequest(url, this);
                resultView.setText("start 异步Get请求 " + url);
                break;
            case R.id.okhttp_async_get:
                final String getUrl = "http://bz.budejie.com/?typeid=2&ver=3.4.3&no_cry=1&client=android&c=wallPaper&a=wallPaperNew&index=1&size=60&bigid=0";
                new Thread() {
                    @Override
                    public void run() {
                        final String result = OkHttpUtils.asyncGetRequest(getUrl);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resultView.setText(result);
                            }
                        });
                    }
                }.start();
                resultView.setText("start 同步Get请求 " + getUrl);
                break;
            case R.id.okhttp_sync_post:
                String postUrl = "https://reg.163.com/logins.jsp";
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("id", "helloworld");
                requestBody.put("pwd", "android");
                OkHttpUtils.syncPostRequest(postUrl, requestBody, this);
                resultView.setText("start 异步Post请求 " + postUrl);
                break;
            case R.id.okhttp_async_post:
                break;
        }

    }

    /**
     * Called when the request could not be executed due to cancellation, a connectivity problem or
     * timeout. Because networks can fail during an exchange, it is possible that the remote server
     * accepted the request before the failure.
     *
     * @param call
     * @param e
     */
    @Override
    public void onFailure(Call call, IOException e) {
        final Request request = call.request();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultView.setText("onFailurerequest " + request.url().toString());
            }
        });
    }

    /**
     * Called when the HTTP response was successfully returned by the remote server. The callback may
     * proceed to read the response body with {@link Response#body}. The response is still live until
     * its response body is {@linkplain ResponseBody closed}. The recipient of the callback may
     * consume the response body on another thread.
     *
     * <p>Note that transport-layer success (receiving a HTTP response code, headers and body) does
     * not necessarily indicate application-layer success: {@code response} may still indicate an
     * unhappy HTTP response code like 404 or 500.
     *
     * @param call
     * @param response
     */
    @Override
    public void onResponse(Call call, final Response response) {
        final HttpUrl url = call.request().url();
        String resultStr = null;
        try {
            resultStr = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final String finalResultStr = resultStr;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultView.setText("request " + url.toString() + "\n statusCode = " + response.code() + "\nresponseBody = " + response.protocol() + " " + finalResultStr.trim());
            }
        });
    }


}
