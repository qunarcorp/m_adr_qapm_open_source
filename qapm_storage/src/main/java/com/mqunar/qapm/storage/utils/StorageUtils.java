package com.mqunar.qapm.storage.utils;

import android.net.Uri;
import android.text.TextUtils;

import com.mqunar.qapm.storage.QApmConstant;

public class StorageUtils {

    public static Uri getTableUri(String pkgName, String tableName) {
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(tableName)) {
            return null;
        }
        String uriStr = TextUtils.concat(
                QApmConstant.CONTENT_PATH_PREFIX,
                getAuthority(pkgName),
                "/",
                tableName).toString();
        return Uri.parse(uriStr);
    }

//    public static void main (String[] ages) {
//        Uri mainTable = getTableUri("com,Qunar", "MainTable");
//        System.out.println(mainTable.toString());
//    }

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
