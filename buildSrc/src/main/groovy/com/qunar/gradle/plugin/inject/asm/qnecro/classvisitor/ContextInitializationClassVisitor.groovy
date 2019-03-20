package com.qunar.gradle.plugin.inject.asm.qnecro.classvisitor

import com.qunar.gradle.plugin.inject.asm.qnecro.instrument.InstrumentationContext
import groovyjarjarasm.asm.ClassVisitor
import groovyjarjarasm.asm.Opcodes

/**
 * Created by jingmin.xing on 2015/12/16.
 */
class ContextInitializationClassVisitor extends ClassVisitor {
    private final InstrumentationContext context
    ContextInitializationClassVisitor(ClassVisitor classVisitor, InstrumentationContext context) {
        super(Opcodes.ASM5, classVisitor)
        this.context = context
    }

    @Override
    void visit(int version, int access, String name, String sig, String superName, String[] interfaces) {
        this.context.setClassName(name)
        this.context.setSuperClassName(superName)
        super.visit(version, access, name, sig, superName, interfaces)
    }
}
