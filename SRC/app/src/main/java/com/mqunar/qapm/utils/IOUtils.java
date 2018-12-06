package com.mqunar.qapm.utils;

import android.content.Context;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class IOUtils {

    private static String sTempDir ;

    public static String getUploadDir(Context mContext){
        if (sTempDir == null) {
            try {
                sTempDir = mContext.getFilesDir().getAbsolutePath() + File.separator + "qapm";
            } catch (Exception e) {
                sTempDir = "/data/data/" + mContext.getPackageName() + "/files/qapm";
            }
            if (!new File(sTempDir).exists()) {
                new File(sTempDir).mkdirs();
            }
        }
        return sTempDir;
    }

    /**
     * 将字符串存到文件中
     *
     * @param fileName 文件名称
     * @param data     存储的数据
     * @return 是否存储成功
     */
    public static boolean str2File(String data, String fileName) {
        FileWriter fileWriter = null;
        try {
            File file = new File(fileName);
            fileWriter = new FileWriter(file);
            //写文件
            fileWriter.write(data);
        } catch (Throwable throwable) {
            return false;
        } finally {
            safeClose(fileWriter);
        }
        return true;
    }

    /**
     * 从文件中读取字符串
     *
     * @param fileName 文件名称
     * @return 返回字符串，失败返回null
     */
    public static String file2Str(String fileName) {
        String str = null;
        FileInputStream in = null;
        try {
            File file = new File(fileName);
            in = new FileInputStream(file);
            // 字串的长度 ，这里一次性读完
            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            str = new String(buffer, "UTF-8");
        } catch (IOException e) {
            return null;
        } finally {
            safeClose(in);
        }
        return str;
    }

    /**
     * 关闭IO流
     */
    public static void safeClose(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (IOException ignored) {}
    }
}
