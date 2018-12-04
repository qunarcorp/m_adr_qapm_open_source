package com.mqunar.utils;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by hongchen.dong on 2016/6/22.
 * <p>
 * 发送工具类
 */
public class QAPMCompressUtils {
    /**
     * 将字符串进行压缩　gzip
     *
     * @param content     压缩的字符串
     * @param outFileName 压缩文件的输出位置和名称
     */
    public static void doCompressString(String content, String outFileName) {
        InputStream in = null;
        GZIPOutputStream out = null;

        try {
            in = new ByteArrayInputStream(content.getBytes());
            out = new GZIPOutputStream(new FileOutputStream(outFileName));

            //读取字节流到压缩流
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            out.finish();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.safeClose(in);
            IOUtils.safeClose(out);
        }
    }

}
