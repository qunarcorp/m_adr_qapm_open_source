package com.mqunar.network.instrumentation;

import java.lang.annotation.Target;

/**
 * Created by jingmin.xing on 2016/1/4.
 */
@Target({java.lang.annotation.ElementType.METHOD})
public @interface Trace {
    public static final String NULL = "";
    String metricName() default "";
    boolean skipTransactionTrace() default false;
    MetricCategory category() default MetricCategory.NONE;
}
