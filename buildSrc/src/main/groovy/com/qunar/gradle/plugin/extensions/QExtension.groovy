package com.qunar.gradle.plugin.extensions

/**
 * Created by jingmin.xing on 2016/5/30.
 */
class QExtension {

    QNecroConfig qNecroConfig = new QNecroConfig();


    void configQNecro(Closure c) {
        c.delegate = qNecroConfig
        c.setResolveStrategy Closure.DELEGATE_FIRST
        c()
    }
}
