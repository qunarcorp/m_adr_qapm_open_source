package com.qunar.gradle.plugin.inject.asm.qnecro.instrument

class HookInitMethodContext {

    public boolean needHook = false

    public List<NeedHookMethod> hookMethods = [] as List

    public static class NeedHookMethod {
        public int access
        public String methodName
        public String desc
        public String signature
        public String[] exceptions
        public def newVisiTypeInsnIndexs = [] as List
        public def dupVisiInsnIndexs = [] as List


        NeedHookMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
            this.access = access
            this.methodName = methodName
            this.desc = desc
            this.signature = signature
            this.exceptions = exceptions
        }
    }

}
