package com.qunar.gradle.plugin.inject.model

import groovyjarjarasm.asm.commons.Method

/**
 * 解析Properties封装的类
 * Created by jingmin.xing on 2015/12/14.
 */
final class ClassMethod {

    private final String className
    private final String methodName
    private final String methodDesc

    public ClassMethod(String className, String methodName, String methodDesc) {
        this.className = className
        this.methodName = methodName
        this.methodDesc = methodDesc
    }

    Method getMethod() {
        return new Method(this.methodName, this.methodDesc)
    }

    public String getClassName() {
        return this.className
    }

    public String getMethodName() {
        return this.methodName
    }

    public String getMethodDesc() {
        return this.methodDesc
    }

    public int hashCode() {
        int prime = 31
        int result = 1
        result = prime * result + (this.className == null ? 0 : this.className.hashCode())
        result = prime * result + (this.methodDesc == null ? 0 : this.methodDesc.hashCode())
        result = prime * result + (this.methodName == null ? 0 : this.methodName.hashCode())
        return result
    }

    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true
//        }
        if (obj == null) {
            return false
        }
        if (this.class != obj.class) {
            return false
        }
        ClassMethod other = (ClassMethod)obj
        if (this.className == null) {
            if (other.className != null) {
                return false
            }
        } else if (this.className != other.className) {
            return false
        }
        if (this.methodDesc == null) {
            if (other.methodDesc != null) {
                return false
            }
        } else if (this.methodDesc != other.methodDesc) {
            return false
        }
        if (this.methodName == null) {
            if (other.methodName != null) {
                return false
            }
        } else if (this.methodName != other.methodName) {
            return false
        }
        return true
    }

    public String toString() {
        return this.className + '.' + this.methodName + this.methodDesc
    }
}
