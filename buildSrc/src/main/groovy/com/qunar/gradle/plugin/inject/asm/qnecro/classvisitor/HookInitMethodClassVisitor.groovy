package com.qunar.gradle.plugin.inject.asm.qnecro.classvisitor

import com.qunar.gradle.plugin.inject.asm.qnecro.instrument.ClassRemapperConfig
import com.qunar.gradle.plugin.inject.asm.qnecro.instrument.HookInitMethodContext
import groovyjarjarasm.asm.ClassVisitor
import groovyjarjarasm.asm.MethodVisitor
import groovyjarjarasm.asm.commons.AdviceAdapter

import static groovyjarjarasm.asm.Opcodes.ASM5

/**
 * 修改init方法调用，举例如下：
 * 把方法调用 new OkHttpClient.Builder() 修改为 OkHttpUtils.getOkHttpClientBuilder()
 *
 * 具体实现：
 * mv.visitTypeInsn(NEW, "okhttp3/OkHttpClient$Builder");
 * mv.visitInsn(DUP);
 * mv.visitMethodInsn(INVOKESPECIAL, "okhttp3/OkHttpClient$Builder", "<init>", "()V", false);
 *
 * 修改为：
 * mv.visitMethodInsn(INVOKESTATIC, "com/mqunar/qapm/network/instrumentation/okhttp3/QOkHttpUtils", "getOkHttpClientBuilder", "()Lokhttp3/OkHttpClient$Builder;", false);
 *
 */
class HookInitMethodClassVisitor extends ClassVisitor {
    String cname
    HookInitMethodContext context
    final boolean hook

    /**
     * 自定义ClassVisitor
     * @param classVisitor classWrite
     * @param className 类名
     * @param context hookInitMethodContext对象
     */
    HookInitMethodClassVisitor(ClassVisitor classVisitor, String className, HookInitMethodContext context) {
        super(ASM5, classVisitor)
        this.cname = className
        this.context = context
        this.hook = context.needHook
    }

    /**
     * 重写visitMethod方法，返回一个MethodVisitor对象
     */
    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor originMv = super.visitMethod(access, name, desc, signature, exceptions)
        if (hook) {
            def modifyMethod = context?.hookMethods?.find {
                it.access == access && it.methodName == name && it.desc == desc && it.signature == signature && it.exceptions?.length == exceptions?.length
            }
            if (modifyMethod) {
                return new HookInitMethodVisitor(originMv, modifyMethod)
            }
            return originMv
        }
        return new HookInitMethodVisitor(originMv, new HookInitMethodContext.NeedHookMethod(access, name, desc, signature, exceptions))
    }

    /**
     * 自定义MethodVisitor
     */
    class HookInitMethodVisitor extends AdviceAdapter {
        HookInitMethodContext.NeedHookMethod method
        private int newVisiTypeInsnIndex
        private int dupVisiInsnIndexs

        HookInitMethodVisitor(MethodVisitor methodVisitor, HookInitMethodContext.NeedHookMethod method) {
            super(ASM5, methodVisitor, method.access, method.methodName, method.desc)
            this.method = method
        }

        @Override
        void visitTypeInsn(int opcode, String type) {
            if (opcode == NEW) {
                newVisiTypeInsnIndex++
                if (hook && method.newVisiTypeInsnIndexs.contains(newVisiTypeInsnIndex)) {
                    return
                }
            }
            super.visitTypeInsn(opcode, type)
        }

        @Override
        void visitInsn(int opcode) {
            if (opcode == DUP) {
                dupVisiInsnIndexs++
                if (hook && method.dupVisiInsnIndexs.contains(dupVisiInsnIndexs)) {
                    return
                }
            }
            super.visitInsn(opcode)
        }

        /**
         *
         * @param opcode
         * @param owner
         * @param name
         * @param desc
         * @param b
         */
        @Override
        void visitMethodInsn(int opcode, String owner, String name, String desc, boolean b) {
            for (def it : ClassRemapperConfig.instance.methodInits) {
                if (opcode == INVOKESPECIAL && name == it.key.methodName && desc == it.key.methodDesc && !b && owner == it.key.className) {
                    if (hook) {//替换方法
                        super.visitMethodInsn(INVOKESTATIC, it.value.className, it.value.methodName, it.value.methodDesc, false)
                        return
                    } else {//标记方法
                        method.newVisiTypeInsnIndexs << newVisiTypeInsnIndex
                        method.dupVisiInsnIndexs << dupVisiInsnIndexs
                        if (!context.hookMethods.contains(method)) {
                            context.hookMethods.add(method)
                            context.needHook = true
                        }
                    }
                }
            }
            super.visitMethodInsn(opcode, owner, name, desc, b)
        }
    }
}
