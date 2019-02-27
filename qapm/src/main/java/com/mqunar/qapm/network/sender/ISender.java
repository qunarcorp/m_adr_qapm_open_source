package com.mqunar.qapm.network.sender;

import android.content.Context;

public interface ISender {

    void send(Context context, String filePath);

    String getCParam(Context context);

    String getHostUrl();
}
