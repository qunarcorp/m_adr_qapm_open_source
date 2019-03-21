package com.qunar.gradle.plugin.inject.asm.qnecro

import com.qunar.gradle.plugin.inject.asm.qnecro.classvisitor.HookInitMethodClassVisitor
import com.qunar.gradle.plugin.inject.asm.qnecro.constants.NecroConstants
import com.qunar.gradle.plugin.inject.asm.qnecro.instrument.HookInitMethodContext
import groovyjarjarasm.asm.ClassReader
import groovyjarjarasm.asm.ClassWriter

class HookInitMethodClassProxy {

    static byte[] invoke(byte[] classBuffer) {
        ClassReader cr = new ClassReader(classBuffer)
        def name = cr.className
        def ignore = NecroConstants.EXCLUDED_PACKAGES.find {
            name?.startsWith(it)
        }

        if (ignore) {
            return classBuffer
        }

        //创建一个Context对象，默认不hook，先进行扫描
        HookInitMethodContext context = new HookInitMethodContext()
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        cr.accept(new HookInitMethodClassVisitor(cw, name, context), ClassReader.SKIP_FRAMES)
        if (context.needHook) {//扫描完成，需要Hook
            cw = new ClassWriter(ClassWriter.COMPUTE_MAXS)
            cr.accept(new HookInitMethodClassVisitor(cw, name, context), ClassReader.SKIP_FRAMES)
            return cw.toByteArray()
        } else {
            return classBuffer
        }
    }
}