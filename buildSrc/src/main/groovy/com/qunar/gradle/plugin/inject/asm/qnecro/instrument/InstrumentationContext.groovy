package com.qunar.gradle.plugin.inject.asm.qnecro.instrument


import com.qunar.gradle.plugin.inject.model.ClassMethod
import com.qunar.gradle.plugin.utils.QBuildLogger

/**
 * Created by jingmin.xing on 2015/12/12.
 */
class InstrumentationContext {

    private String className
    private boolean classModified
    private String superClassName
    private HashMap<String, String> tracedMethods
    private HashMap<String, String> skippedMethods
    private final ArrayList<String> tags = new ArrayList()

    private final ClassRemapperConfig config

    private final HashMap<String, ArrayList<String>> tracedMethodParameters = new HashMap()

    public InstrumentationContext(ClassRemapperConfig config) {
        this.config = config
        this.tracedMethods = new HashMap()
        this.skippedMethods = new HashMap()
    }

    public void reset() {
        this.classModified = false
        this.className = null
        this.superClassName = null
        this.tags.clear()
    }

    public void setClassName(String className) {
        this.className = className
    }

    public String getClassName() {
        return this.className
    }

    public void markModified() {
        this.classModified = true
    }

    public boolean isClassModified() {
        return this.classModified
    }

    public String getSimpleClassName() {
        if (this.className.contains("/")) {
            return this.className.substring(this.className.lastIndexOf("/") + 1);
        }
        return this.className
    }

    public String getFriendlyClassName() {
        return this.className?.replaceAll("/", ".")
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = superClassName;
    }

    public String getSuperClassName() {
        return this.superClassName
    }

    public String getFriendlySuperClassName() {
        return this.superClassName?.replaceAll("/", ".")
    }

    public void addTag(String tag) {
        this.tags.add(tag)
    }

    public void addUniqueTag(String tag) {
        while (this.tags.remove(tag)) {}
        addTag(tag)
    }

    public boolean hasTag(String tag) {
        return this.tags.contains(tag)
    }

    public void addTracedMethod(String name, String desc) {
        QBuildLogger.log("Will trace method " + this.className + "#" + name + ":" + desc + " as requested")
        this.tracedMethods.put(this.className + "#" + name, desc)
    }

    public void addSkippedMethod(String name, String desc) {
        QBuildLogger.log("Will skip all tracing in method " + this.className + "#" + name + ":" + desc + " as requested")
        this.skippedMethods.put(this.className + "#" + name, desc)
    }

    public boolean isTracedMethod(String name, String desc) {
        return searchMethodMap(this.tracedMethods, name, desc)
    }

    public boolean isSkippedMethod(String name, String desc) {
        return searchMethodMap(this.skippedMethods, name, desc)
    }

    private boolean searchMethodMap(Map<String, String> map, String name, String desc) {
        String descToMatch = (String)map.get(this.className + "#" + name)
        if (descToMatch == null) {
            return false
        }
        if (desc.equals(desc)) {
            return true
        }
        return false
    }

    public void addTracedMethodParameter(String methodName, String parameterName, String parameterClass, String parameterValue) {
        QBuildLogger.log("Adding traced method parameter " + parameterName + " for method " + methodName);

        String name = this.className + "#" + methodName
        if (!this.tracedMethodParameters.containsKey(name)) {
            this.tracedMethodParameters.put(name, new ArrayList())
        }
        ArrayList<String> methodParameters = (ArrayList)this.tracedMethodParameters.get(name);
        methodParameters.add(parameterName)
        methodParameters.add(parameterClass)
        methodParameters.add(parameterValue)
    }

    public ArrayList<String> getTracedMethodParameters(String methodName) {
        return (ArrayList)this.tracedMethodParameters.get(this.className + "#" + methodName);
    }

    public ClassMethod getMethodWrapper(ClassMethod method) {
        return this.config.getMethodWrapper(method)
    }

    public Collection<ClassMethod> getCallSiteReplacements(String className, String methodName, String methodDesc) {
        return this.config.getCallSiteReplacements(className, methodName, methodDesc)
    }
}
