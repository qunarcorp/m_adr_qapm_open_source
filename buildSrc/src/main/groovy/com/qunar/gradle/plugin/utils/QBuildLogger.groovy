package com.qunar.gradle.plugin.utils

import org.gradle.api.Project

import java.lang.reflect.Method
/**
 * Created by chaos on 15/8/10.
 */
class QBuildLogger {

    static boolean showLog = true

    public static final String KEY = "QBuild-Plugin"

    static Class<Enum> colorClz
    static Enum[] colorEnums
    static Class ansiClass
    static {
        try {
            ansiClass = Class.forName("org.fusesource.jansi.Ansi")
            colorClz = Class.forName('org.fusesource.jansi.Ansi$Color') as Class<Enum>
            colorEnums = colorClz.enumConstants
        } catch (Exception e) {
            println "$KEY 反射Ansi相关Class失败：$e.message"
        }
    }

    public static enum Color {
        BLACK(0, "BLACK"),
        RED(1, "RED"),
        GREEN(2, "GREEN"),
        YELLOW(3, "YELLOW"),
        BLUE(4, "BLUE"),
        MAGENTA(5, "MAGENTA"),
        CYAN(6, "CYAN"),
        WHITE(7, "WHITE"),
        DEFAULT(9, "DEFAULT")
        final int value
        final String name

        Color(int index, String name) {
            this.value = index
            this.name = name
        }

    }

    public static init(Project project) {
        showLog = project.properties.containsKey('showLog') && "true".equals(project.properties.'showLog')
        println "$KEY showLog ${showLog}"
    }

    static void log(String msg) {
        showMsg(msg, Color.WHITE, Color.BLACK)
    }

    static void log(String msg,String... args) {
        if(args){
            msg = String.format(msg, args)
        }
        log(msg)
    }

    public static String getStackTraceString(Throwable t) {
        StringWriter sw = new StringWriter(256)
        PrintWriter pw = new PrintWriter(sw, false)
        t.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }


    static void warnning(String msg) {
        showMsg(msg, Color.YELLOW, Color.BLACK)
    }

    static void err(Throwable t,String msg) {
        if(t){
            msg += "\n" + getStackTraceString(t)
        }
        err(msg)
    }

    static void err(String msg) {
        showMsg(msg, Color.RED, Color.BLACK)
    }

    static void showMsg(String message, Color bgColor, Color fontColor) {
        if (showLog) {
            if (ansiClass == null || colorClz == null) {
                println "$KEY $message"
                return
            }
            try {
                Enum backgroundColor = convertAndroidColor(bgColor)
                Enum msgColor = convertAndroidColor(fontColor)
                Method ansiMethod = ansiClass.getMethod("ansi")
                Object ansiInstance = ansiMethod.invoke(null)
                ansiInstance = ansiClass.getMethod("eraseScreen").invoke(ansiInstance)
                Method bgMethod = ansiClass.getMethod("bg", colorClz)
                Method fgMethod = ansiClass.getMethod("fg", colorClz)
                !backgroundColor ?: bgMethod.invoke(ansiInstance, backgroundColor)
                !msgColor ?: fgMethod.invoke(ansiInstance, msgColor)
                ansiInstance = ansiClass.getMethod('a', String.class).invoke(ansiInstance, message)
                ansiInstance = ansiClass.getMethod("reset").invoke(ansiInstance)
                println ansiInstance
            } catch (Exception e) {
                println "${e.message}\r\n$message"
            }
        }
    }

    static Enum convertAndroidColor(Color myColor) {
        if (colorClz == null || ansiClass == null || myColor == null) {
            return null
        }
        Method getValue = colorClz.getMethod("value")
        Enum item = colorEnums.find {
            getValue.invoke(it) == myColor.value
        }
        return item ? item : colorEnums[0]
    }

}
