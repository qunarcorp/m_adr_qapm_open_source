package com.qunar.gradle.plugin.inject.asm.qnecro.classvisitor

import com.qunar.gradle.plugin.inject.asm.qnecro.instrument.InstrumentationContext

import com.qunar.gradle.plugin.inject.model.ClassMethod
import com.qunar.gradle.plugin.utils.QBuildLogger
import groovyjarjarasm.asm.ClassVisitor
import groovyjarjarasm.asm.Label
import groovyjarjarasm.asm.MethodVisitor
import groovyjarjarasm.asm.Opcodes
import groovyjarjarasm.asm.commons.GeneratorAdapter
import groovyjarjarasm.asm.commons.Method

import static groovyjarjarasm.asm.Opcodes.ASM5

/**
 * Created by jingmin.xing on 2015/12/16.
 */
class NecroAsmWrapReplaceClassVisitor extends ClassVisitor {

    private final InstrumentationContext context

    NecroAsmWrapReplaceClassVisitor(ClassVisitor classVisitor, InstrumentationContext context) {
        super(ASM5, classVisitor)
        this.context = context
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
        if (context.isSkippedMethod(name, desc)) {
            return mv
        }
        return new WrapReplaceMethodVisitor(mv, access, name, desc);
    }

    private final class WrapReplaceMethodVisitor extends GeneratorAdapter implements Opcodes {

        private final String name
        private final String desc
        private boolean newInstructionFound = false
        private boolean dupInstructionFound = false

        protected WrapReplaceMethodVisitor(MethodVisitor methodVisitor, int access, String name, String desc) {
            super(ASM5, methodVisitor, access, name, desc)
            this.name = name
            this.desc = desc
        }

        @Override
        void visitMethodInsn(int opcode, String owner, String name, String desc, boolean b) {
            if (opcode == INVOKEDYNAMIC) {
                QBuildLogger.log "$context.className INVOKEDYNAMIC instruction cannot be instrumented"
                super.visitMethodInsn(opcode, owner, name, desc, b)
                return
            }
            if ((!tryReplaceCallSite(opcode, owner, name, desc, b)) &&
                    (!tryWrapReturnValue(opcode, owner, name, desc, b))) {
                super.visitMethodInsn(opcode, owner, name, desc, b)
            }
        }

        @Override
        void visitTypeInsn(int opcode, String type) {
            if (opcode == NEW) {
                this.newInstructionFound = true
                this.dupInstructionFound = false
            }
            super.visitTypeInsn(opcode, type)
        }

        @Override
        void visitInsn(int opcode) {
            if (opcode == DUP) {
                this.dupInstructionFound = true
            }
            super.visitInsn(opcode)
        }

        private boolean tryWrapReturnValue(int opcode, String owner, String name, String desc, boolean b) {
            ClassMethod method = new ClassMethod(owner, name, desc)
            ClassMethod wrappingMethod = context.getMethodWrapper(method)
            if (wrappingMethod != null) {
//                QBuildLogger.log "$context.className wrapping call to $method with $wrappingMethod"
                super.visitMethodInsn(opcode, owner, name, desc, b)
                super.visitMethodInsn(INVOKESTATIC, wrappingMethod.className, wrappingMethod.methodName, wrappingMethod.methodDesc, false);
                context.markModified()
                return true
            }
            return false
        }

        private boolean tryReplaceCallSite(int opcode, String owner, String name, String desc, boolean b) {
            Collection<ClassMethod> replacementMethods = context.getCallSiteReplacements(owner, name, desc);
            if (replacementMethods.isEmpty()) {
                return false;
            }
            ClassMethod method = new ClassMethod(owner, name, desc);
            Iterator i$ = replacementMethods.iterator();
            if (i$.hasNext()) {
                ClassMethod replacementMethod = (ClassMethod)i$.next();
                boolean isSuperCallInOverride = (opcode == INVOKESPECIAL) && (!owner.equals(context.className)) && (this.name.equals(name)) && (this.desc.equals(desc));
                if (isSuperCallInOverride) {
                    QBuildLogger.log "$context.className skipping call site replacement for super call in overriden method: $name:$desc"
                    return false
                }
                if ((opcode == INVOKESPECIAL) && (name.equals("<init>"))) {
                    Method originalMethod = new Method(name, desc)
                    if (context.superClassName?.equals(owner)) {
                        QBuildLogger.log "$context.friendlyClassName skipping call site replacement for class extending $context.friendlySuperClassName"
                        return false
                    }
                    QBuildLogger.log "[$context.friendlyClassName] tracing constructor call to $method - $owner"

                    int[] locals = new int[originalMethod.argumentTypes.length]
                    for (int i = locals.length - 1; i >= 0; i--) {
                        locals[i] = newLocal(originalMethod.argumentTypes[i])
                        storeLocal(locals[i])
                    }
                    visitInsn(POP)
                    if ((this.newInstructionFound) && (this.dupInstructionFound)) {
                        visitInsn(POP)
                    }
                    for (int local : locals) {
                        loadLocal(local)
                    }
                    super.visitMethodInsn(INVOKESTATIC, replacementMethod.className, replacementMethod.methodName, replacementMethod.methodDesc, false)
                    if ((this.newInstructionFound) && (!this.dupInstructionFound)) {
                        visitInsn(POP)
                    }
                } else if (opcode == INVOKESTATIC) {
                    QBuildLogger.log "$context.className replacing static call to $method with $replacementMethod"
                    super.visitMethodInsn(INVOKESTATIC, replacementMethod.className, replacementMethod.methodName, replacementMethod.methodDesc, false)
                } else {
                    Method newMethod = new Method(replacementMethod.methodName, replacementMethod.methodDesc)
                    QBuildLogger.log "$context.className replacing call to $method with $replacementMethod (with instance check)"

                    Method originalMethod = new Method(name, desc)
                    int[] locals = new int[originalMethod.argumentTypes.length]
                    for (int i = locals.length - 1; i >= 0; i--) {
                        locals[i] = newLocal(originalMethod.argumentTypes[i])
                        storeLocal(locals[i])
                    }
                    dup();

                    instanceOf(newMethod.argumentTypes[0])
                    Label isInstanceOfLabel = new Label()
                    visitJumpInsn(IFNE, isInstanceOfLabel)
                    for (int local : locals) {
                        loadLocal(local)
                    }
                    super.visitMethodInsn(opcode, owner, name, desc, b)

                    Label end = new Label()
                    visitJumpInsn(GOTO, end)
                    visitLabel(isInstanceOfLabel)

                    checkCast(newMethod.argumentTypes[0])
                    for (int local : locals) {
                        loadLocal(local)
                    }
                    super.visitMethodInsn(INVOKESTATIC, replacementMethod.className, replacementMethod.methodName, replacementMethod.methodDesc, false)
                    visitLabel(end)
                }
                context.markModified()
                return true
            }
            return false
        }
    }
}
