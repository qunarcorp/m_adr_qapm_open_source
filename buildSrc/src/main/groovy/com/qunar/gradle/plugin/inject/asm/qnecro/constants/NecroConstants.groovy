package com.qunar.gradle.plugin.inject.asm.qnecro.constants


 class NecroConstants {

    public static boolean hookQNecro = false

    public static void init(boolean hookQNecro) {
        this.hookQNecro = hookQNecro
    }
    public static final HashSet<String> EXCLUDED_PACKAGES = new HashSet(["com/mqunar/qapm/network", "com/squareup/okhttp"])

    public static final String WRAP_METHOD_IDENTIFIER = "WRAP_METHOD:"
    public static final String REPLACE_CALL_SITE_IDENTIFIER = "REPLACE_CALL_SITE:"
    public static final String MAPPING_PATH = "/type_map.properties"

    public static final String TAG_INSTRUMENTED = "Lcom/mqunar/qapm/network/instrumentation/Instrumented;"


}
