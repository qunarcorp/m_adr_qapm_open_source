/**
 * Copyright © 2014 Qunar.com Inc. All Rights Reserved.
 */
package com.mqunar.utils;


import com.mqunar.logging.AgentLog;
import com.mqunar.logging.AgentLogManager;

import java.lang.reflect.Method;

class ReflectUtils {
    private static final AgentLog log = AgentLogManager.getAgentLog();

    private static Method getMethod(Class<?> clazz, String mName, Class<?>[] paramType) {
        Method m = null;
        while (clazz != null) {
            try {
                m = clazz.getDeclaredMethod(mName, paramType);
            } catch (Exception ignored) {
            }
            if (m != null) {
                m.setAccessible(true);
                break;
            }
            clazz = clazz.getSuperclass();
        }
        return m;
    }


    /**
     * 反射静态方法
     *
     * @param mName       方法名
     * @param paramType   参数类型
     * @param paramValues 值
     * @return Object
     * @since 2014年1月21日下午4:35:42
     */
    static Object invokeStaticMethod(String className, String mName,
                                     Class<?>[] paramType, Object[] paramValues) {
        try {
            Class<?> objClz = Class.forName(className);
            Method method = getMethod(objClz, mName, paramType);
            return method.invoke(null, paramValues);
        } catch (Exception e) {
            log.error("reflect failed :" + e);
        }
        return null;
    }


    /**
     * 判断某个方法是否存在
     */
    static boolean isMethodExit(String className, String mName,
                                Class<?>[] paramType) {
        try {
            Class<?> objClz = Class.forName(className);
            objClz.getDeclaredMethod(mName, paramType);
        } catch (Exception exception) {
            //判处异常表示 不存在 该类 或 方法
            return false;
        }

        return true;
    }

}
