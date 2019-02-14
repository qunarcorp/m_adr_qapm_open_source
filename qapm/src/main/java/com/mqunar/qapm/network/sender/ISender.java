package com.mqunar.qapm.network.sender;

import android.content.Context;

import java.io.File;
import java.util.Map;

public interface ISender {

    void send(Context context, String filePath);

    String getCParam(Context context);
}
