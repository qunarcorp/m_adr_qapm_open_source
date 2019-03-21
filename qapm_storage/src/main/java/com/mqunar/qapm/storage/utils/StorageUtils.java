package com.mqunar.qapm.storage.utils;

import android.text.TextUtils;

import com.mqunar.qapm.storage.QApmConstant;

public class StorageUtils {

    /**
     * 获取AUTHORITY
     *
     * @param pkgName 包名
     */
    public static String getAuthority(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return "";
        }
        return TextUtils.concat(pkgName, ".", QApmConstant.AUTHORITY_SUFFIX).toString();
    }
}
