package com.qunar.gradle.plugin.inject.asm.qnecro.annotation

import com.qunar.gradle.plugin.inject.asm.qnecro.instrument.InstrumentationContext
import groovyjarjarasm.asm.AnnotationVisitor
import groovyjarjarasm.asm.Opcodes
import groovyjarjarasm.asm.Type

/**
 * Created by jingmin.xing on 2015/12/14.
 */
class TraceAnnotationVisitor extends AnnotationImpl {
    private final String methodName
    private final InstrumentationContext context
    TraceAnnotationVisitor(AnnotationVisitor annotationVisitor,
                           String methodName, InstrumentationContext context) {
        super(Opcodes.ASM5, annotationVisitor)
        this.methodName = methodName
        this.context = context
    }

    @Override
    void visit(String name, Object value) {
        super.visit(name, value)
        this.context.addTracedMethodParameter(methodName, name, value.class.name, value.toString())
    }

    @Override
    void visitEnum(String name, String desc, String value) {
        super.visitEnum(name, desc, value)
        String className = Type.getType(desc).className
        this.context.addTracedMethodParameter(methodName, name, className, value)
    }
}
