package com.mqunar.network.instrumentation;

import java.lang.annotation.Target;

/**
 * Created by jingmin.xing on 2015/12/15.
 */
@Target({java.lang.annotation.ElementType.METHOD})
public @interface WrapReturn {
    String className();
    String methodName();
    String methodDesc();
}
