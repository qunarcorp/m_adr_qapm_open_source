package com.qunar.gradle.plugin.inject.asm.qnecro.instrument

import com.qunar.gradle.plugin.inject.asm.qnecro.constants.NecroConstants
import com.qunar.gradle.plugin.inject.asm.qnecro.util.PropertiesUtils
import com.qunar.gradle.plugin.inject.model.ClassMethod

/**
 * 用来解析properties配置文件
 * Created by jingmin.xing on 2015/12/14.
 */
class ClassRemapperConfig {
    private final Map<ClassMethod, ClassMethod> methodWrappers
    private final Map<String, Collection<ClassMethod>> callSiteReplacements

    private static volatile ClassRemapperConfig instance

    public static ClassRemapperConfig getInstance(){
        if(instance == null){
            synchronized (ClassRemapperConfig.class){
                if(instance == null){
                    instance = new ClassRemapperConfig()
                }
            }
        }
        return instance
    }

    private ClassRemapperConfig() {
        Map<String, String> remappings = PropertiesUtils.getPropertiesMap(NecroConstants.MAPPING_PATH)
        if (remappings) {
            methodWrappers = PropertiesUtils.getMethodWrappers(remappings)
            callSiteReplacements = PropertiesUtils.getCallSiteReplacements(remappings)
        }
    }

    public ClassMethod getMethodWrapper(ClassMethod orgMethod) {
        if (methodWrappers) {
            return methodWrappers.get(orgMethod)
        }
        return orgMethod
    }

    public Collection<ClassMethod> getCallSiteReplacements(String className, String methodName, String methodDesc) {
        ArrayList<ClassMethod> methods = new ArrayList()
        Collection<ClassMethod> matches = callSiteReplacements.get("$methodName:$methodDesc")
        if (matches != null) {
            methods.addAll(matches)
        }
        matches = callSiteReplacements.get("$className.$methodName:$methodDesc")
        if (matches != null) {
            methods.addAll(matches)
        }
        return methods
    }
}
