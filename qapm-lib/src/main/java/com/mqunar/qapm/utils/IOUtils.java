package com.mqunar.qapm.utils;

import android.content.Context;

import com.mqunar.qapm.logging.AgentLogManager;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class IOUtils {

    private static String sTempDir;

    public static String getSaveDataFile(Context context, String name) {
        String path = getUploadDir(context);
        if (path == null) {
            return null;
        }
        File destFile = new File(path, name);
        try {
            destFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return destFile.toString();
    }

    public static String getUploadDir(Context mContext) {
        if (sTempDir == null) {
            if (mContext == null) {
                return null;
            }
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
     *
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
     *
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
        if (closeable == null)
            return;
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * 过滤要发送的文件
     *
     * @param uploadDir 上传文件目录
     *
     * @return
     */
    public static String[] getFileByNameFilter(String uploadDir) {
        //只过滤时间戳的文件
        String[] fileNames = new File(uploadDir).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.matches("[0-9]+");
            }
        });
        //先发送old 文件
        if (fileNames != null && fileNames.length > 0) {
            Arrays.sort(fileNames, new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    return lhs.compareTo(rhs);
                }
            });
        }
        return fileNames;
    }

    public static void deleteFile(String fileName) {
        try {
            File file = new File(fileName);
            if (file.exists()) {
                if (!file.delete()) {
                    AgentLogManager.getAgentLog().info("delete file failed :" + fileName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AgentLogManager.getAgentLog().info("delete file exception :" + fileName);
        }
    }


}
