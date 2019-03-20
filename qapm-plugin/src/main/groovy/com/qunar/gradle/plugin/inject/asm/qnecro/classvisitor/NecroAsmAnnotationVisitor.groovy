package com.qunar.gradle.plugin.inject.asm.qnecro.classvisitor

import com.qunar.gradle.plugin.inject.asm.qnecro.constants.NecroConstants
import com.qunar.gradle.plugin.inject.asm.qnecro.instrument.InstrumentationContext
import groovyjarjarasm.asm.ClassVisitor
import groovyjarjarasm.asm.Opcodes
/**
 * Created by jingmin.xing on 2015/12/15.
 */
class NecroAsmAnnotationVisitor extends ClassVisitor {
    private final InstrumentationContext context

    NecroAsmAnnotationVisitor(ClassVisitor classVisitor, InstrumentationContext context) {
        super(Opcodes.ASM5, classVisitor)
        this.context = context;
    }

    @Override
    void visitEnd() {
        if (this.context.isClassModified()) {
            this.context.addUniqueTag(NecroConstants.TAG_INSTRUMENTED)
            super.visitAnnotation(NecroConstants.TAG_INSTRUMENTED, false)
//            QBuildLogger.log "$context.friendlyClassName tagging as instrumented"
        }
        super.visitEnd()
    }
}
