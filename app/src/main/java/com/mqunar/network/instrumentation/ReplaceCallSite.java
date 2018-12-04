package com.mqunar.network.instrumentation;

/**
 * Created by jingmin.xing on 2015/12/15.
 */
public @interface ReplaceCallSite {
    boolean isStatic() default false;
    String scope() default "";
}
