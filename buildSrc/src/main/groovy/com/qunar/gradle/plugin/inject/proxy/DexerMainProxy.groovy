package com.qunar.gradle.plugin.inject.proxy


import com.qunar.gradle.plugin.inject.asm.qnecro.classvisitor.*
import com.qunar.gradle.plugin.inject.asm.qnecro.constants.NecroConstants
import com.qunar.gradle.plugin.inject.asm.qnecro.instrument.InstrumentationContext
import com.qunar.gradle.plugin.utils.QBuildLogger
import groovyjarjarasm.asm.ClassReader
import groovyjarjarasm.asm.ClassVisitor
import groovyjarjarasm.asm.ClassWriter
import org.gradle.util.TextUtil

/**
 *
 * 用来hook业务需要的类
 * 所有hook具体类的入口
 *
 */
class DexerMainProxy {

    public static byte[] invoke(InstrumentationContext context, byte[] bytes) {

        ClassReader cr = new ClassReader(bytes)
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS)

        context.reset()
        def name = cr.className

        if (!context.hasTag(NecroConstants.TAG_INSTRUMENTED)) {//注入没有注入的类
            cr = new ClassReader(bytes)
            cw = new ClassWriter(ClassWriter.COMPUTE_MAXS)

            // 类没有hook
            ClassVisitor cv = cw
            String className = context.className

            QBuildLogger.log("begin to hook class classname " + className + " name " + name)

            /******************************* QNecro Begin *******************************************************/
            QBuildLogger.log("begin to hook qnecro class hookQNecro " + NecroConstants.hookQNecro)
            if (NecroConstants.hookQNecro) {
                def excludedPackage = isExcludedPackage(name)
                if (excludedPackage) {//TODO 判断是否需要直接return
//                    return bytes
                    QBuildLogger.log("Excluded class classname " + className + " name " + name)

                    cr.accept(cv, ClassReader.SKIP_FRAMES)
                    return cw.toByteArray()
                }
                cv = new NecroAsmAnnotationVisitor(cv, context)
                cv = new NecroAsmWrapReplaceClassVisitor(cv, context)
                cv = new ContextInitializationClassVisitor(cv, context)
                QBuildLogger.log("end to hook qnecro class")
            }
            /******************************* QNecro end *******************************************************/

            cr.accept(cv, ClassReader.SKIP_FRAMES)
        } else {
            QBuildLogger.log "[$context.friendlyClassName] class is already instrumented! skipping ..."
        }

        return cw.toByteArray()
    }

/**
 * 业务排除的包，不进行hook
 * @param packageName
 * @return
 */
    private static boolean isExcludedPackage(String packageName) {
//        if (packageName == null || packageName.length() == 0) {
//            return false
//        }
//
//        for (String pck : NecroConstants.EXCLUDED_PACKAGES) {
//            if (packageName.startsWith(pck)) {
//                return true
//            }
//        }
//        return false
//
        return NecroConstants.EXCLUDED_PACKAGES.find {
            packageName?.startsWith(it)
        }
    }

}
