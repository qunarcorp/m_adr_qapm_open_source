package com.qunar.gradle.plugin.inject.asm.qnecro.util

import com.qunar.gradle.plugin.inject.asm.qnecro.constants.NecroConstants
import com.qunar.gradle.plugin.inject.model.ClassMethod
import com.qunar.gradle.plugin.utils.QBuildLogger

/**
 * Created by jingmin.xing on 2015/12/14.
 */
class PropertiesUtils {

    /**
     * 解析本地properties文件
     * @param path
     * @return
     */
    public static Map getPropertiesMap(String path) {
        Properties props = new Properties()
        URL resource = PropertiesUtils.class.getResource(path)
        if (resource == null) {
            QBuildLogger.err("Unable to find the type map");
            System.exit(1)
        }
        InputStream inputStream = null
        try {
            inputStream = new ByteArrayInputStream(resource.bytes)
            props.load(inputStream)
        } catch (Throwable ex) {
            QBuildLogger.err(ex,"Unable to read the type map")
            System.exit(1)
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (IOException e) {
                }
            }
        }
        return props
    }

    /**
     * 解析文件中WRAP_METHOD:开头的
     * @param remappings
     * @return
     */
    public static Map<ClassMethod, ClassMethod> getMethodWrappers(Map remappings) {
        HashMap<ClassMethod, ClassMethod> methodWrappers = new HashMap()
        remappings.entrySet().each { entry ->
            if (entry.key.startsWith(NecroConstants.WRAP_METHOD_IDENTIFIER)) {
                String orgSig = entry.key.substring(NecroConstants.WRAP_METHOD_IDENTIFIER.length())
                ClassMethod orgMethod = getClassMethod(orgSig)
                ClassMethod wrappingMethod = getClassMethod(entry.value as String)
                methodWrappers.put(orgMethod, wrappingMethod)
            }
        }
        return methodWrappers;
    }

    /**
     * 解析文件中INIT_METHOD:开头的配置
     * @param remappings
     * @return
     */
    public static Map<ClassMethod, ClassMethod> getMethodInits(Map remappings) {
        HashMap<ClassMethod, ClassMethod> methodWrappers = new HashMap()
        remappings.entrySet().each { entry ->
            if (entry.key.startsWith(NecroConstants.INIT_METHOD_IDENTIFIER)) {
                String orgSig = entry.key.substring(NecroConstants.INIT_METHOD_IDENTIFIER.length())
                ClassMethod orgMethod = getClassMethod(orgSig)
                ClassMethod wrappingMethod = getClassMethod(entry.value as String)
                methodWrappers.put(orgMethod, wrappingMethod)
            }
        }
        return methodWrappers
    }


    /**
     * 解析文件中REPLACE_CALL_SITE:开头的
     * @param remappings
     * @return
     */
    public static Map<String, Collection<ClassMethod>> getCallSiteReplacements(Map<String, String> remappings) {
        HashMap<String, Set<ClassMethod>> callSiteReplaces = new HashMap()
        remappings.entrySet().each { entry ->
            if (entry.key.startsWith(NecroConstants.REPLACE_CALL_SITE_IDENTIFIER)) {
                String orgSig = entry.key.substring(NecroConstants.REPLACE_CALL_SITE_IDENTIFIER.length())
                if (orgSig.contains(".")) {
                    ClassMethod orgMethod = getClassMethod(orgSig)
                    ClassMethod replaceMethod = getClassMethod(entry.value)
                    String key = "$orgMethod.className.$orgMethod.methodName:$orgMethod.methodDesc"

                    Set<ClassMethod> replacements = callSiteReplaces.get(key)
                    if (replacements == null) {
                        replacements = new HashSet()
                        replacements.add(replaceMethod)
                    } else {
                        replacements.add(replaceMethod)
                    }
                } else {
//                    String[] nameDesc = orgSig.split(":");
                    int paren = orgSig.indexOf("(")
                    String methodName = orgSig.substring(0, paren)
                    String methodDesc = orgSig.substring(paren)

                    String key = "$methodName:$methodDesc"
                    ClassMethod replacement = getClassMethod(entry.value)

                    Set<ClassMethod> replacements = (Set) callSiteReplaces.get(key)
                    if (replacements == null) {
                        replacements = new HashSet()
                        callSiteReplaces.put(key, replacements)
                    }
                    replacements.add(replacement)
                }
            }
        }
        HashMap<String, Collection<ClassMethod>> callSiteReplacements = new HashMap()
        callSiteReplaces.entrySet().each {
            callSiteReplacements.put(it.key, it.value)
        }
        return callSiteReplacements
    }

    private static ClassMethod getClassMethod(String signature) {
        try {
            int descIndex = signature.lastIndexOf('(')
            String methodDesc
            if (descIndex == -1) {
                descIndex = signature.length()
                methodDesc = ""
            } else {
                methodDesc = signature.substring(descIndex)
            }
            String beforeMethodDesc = signature.substring(0, descIndex)
            int methodIndex = beforeMethodDesc.lastIndexOf('.')

            return new ClassMethod(signature.substring(0, methodIndex), signature.substring(methodIndex + 1, descIndex), methodDesc)
        }
        catch (Exception ex) {
            throw new RuntimeException("Error parsing " + signature, ex)
        }
    }

    public static void main(String[] args) {
        Map map = getPropertiesMap(NecroConstants.MAPPING_PATH)
        getMethodWrappers(map)
        getCallSiteReplacements(map)
        QBuildLogger.log("dd")
    }
}
