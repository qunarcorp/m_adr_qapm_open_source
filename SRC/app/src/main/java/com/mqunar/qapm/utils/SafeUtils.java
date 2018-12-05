package com.mqunar.qapm.utils;

import android.annotation.TargetApi;
import android.util.Base64;

import com.mqunar.qapm.logging.AgentLog;
import com.mqunar.qapm.logging.AgentLogManager;

/**
 * Created by dhc on 2017/4/1.
 * <p>
 * 数据加解密专用类
 */

public class SafeUtils {
    private static final AgentLog log = AgentLogManager.getAgentLog();

    //加密
    private static byte[] ea(byte[] param) {
        try {
            //反射goblin 的加密方法
            return (byte[]) ReflectUtils.invokeStaticMethod("qunar.lego.utils.Goblin", "ea", new Class[]{byte[].class}, new Object[]{param});
        } catch (Throwable e) {
            log.error("reflect failed :" + e);
            return param;
        }
    }

    //解密
    private static byte[] da(byte[] param) {
        try {
            //反射goblin 的解密方法
            return (byte[]) ReflectUtils.invokeStaticMethod("qunar.lego.utils.Goblin", "da", new Class[]{byte[].class}, new Object[]{param});
        } catch (Throwable e) {
            log.error("reflect failed :" + e);
            return param;
        }
    }

    /**
     * 对字符串进行加密
     *
     * @param originStr 需要加密的String
     * @return 加密后的String
     */
    @TargetApi(8)
    public static String ea(String originStr) {
        try {
            //1.String2byte[]；2.加密；3.Base64
            byte[] originByte = originStr.getBytes("UTF-8");
            byte[] encipheredByte = new byte[originByte.length + 1];
            encipheredByte[0] = (byte) 7;
            System.arraycopy(originByte, 0, encipheredByte, 1, originByte.length);
            //加密
            originByte = SafeUtils.ea(encipheredByte);

            return Base64.encodeToString(originByte, Base64.NO_WRAP);
        } catch (Throwable throwable) {
            log.error("ea str failed : " + throwable);
            return originStr;
        }

    }

    /**
     * 对字符串进行解密
     */
    @TargetApi(8)
    public static String da(String encipheredStr) {
        try {
            //1.反Base64；2.解密；3.byte[]2String
            byte[] encipheredByte = Base64.decode(encipheredStr, Base64.NO_WRAP);
            encipheredByte = da(encipheredByte);
            byte[] originByte = new byte[encipheredByte.length - 1];
            System.arraycopy(encipheredByte, 1, originByte, 0, originByte.length);

            return new String(originByte, "UTF-8");
        } catch (Throwable throwable) {
            log.error("ea str failed : " + throwable);
            return encipheredStr;
        }

    }


    /**
     * 判断是否有加密工具类
     *
     * @return 是否有加密工具类 --使用Goblin加密并且加解密方法存在返回true,Goblin加密库不存在返回false
     */
    public static boolean canEncryption() {
        return ReflectUtils.isMethodExit("qunar.lego.utils.Goblin", "ea", new Class[]{byte[].class})
                && ReflectUtils.isMethodExit("qunar.lego.utils.Goblin", "da", new Class[]{byte[].class});
    }
}
