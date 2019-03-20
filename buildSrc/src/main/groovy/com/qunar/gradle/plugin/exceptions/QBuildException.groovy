package com.qunar.gradle.plugin.exceptions

import com.qunar.gradle.plugin.utils.QBuildLogger
import org.gradle.api.GradleException

/**
 * Created by shutao.xiang on 2015/10/20.
 */
class QBuildException extends GradleException {

    static final TAG = getClass().simpleName

    QBuildException(String var1) {
        super(TAG + ":" + var1)
        QBuildLogger.err(message)
    }

    QBuildException(String var1, Throwable var2) {
        super(TAG + ":" + var1, var2)
        QBuildLogger.err(message)
    }

    QBuildException(String var1, Throwable var2, boolean var3, boolean var4) {
        super("$TAG:$var1", var2, var3, var4)
        QBuildLogger.err(message)
    }

    QBuildException(Throwable var1) {
        super(var1)
        QBuildLogger.err(TAG + ":" + var1.message)
    }

    QBuildException() {
        super()
        QBuildLogger.err(TAG + ":")
    }
}
