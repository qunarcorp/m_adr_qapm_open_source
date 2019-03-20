package com.mqunar.qapm.network.instrumentation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jingmin.xing on 2015/8/30.
 */
public enum MetricCategory {
    NONE("None"),
    VIEW_LOADING("View Loading"),
    VIEW_LAYOUT("Layout"),
    DATABASE("Database"),
    IMAGE("Images"),
    JSON("JSON"),
    NETWORK("Network");

    private String categoryName;
    private static final Map<String, MetricCategory> methodMap;

    private MetricCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public static MetricCategory categoryForMethod(String fullMethodName) {
        if(fullMethodName == null) {
            return NONE;
        } else {
            String methodName = null;
            int hashIndex = fullMethodName.indexOf("#");
            if(hashIndex >= 0) {
                methodName = fullMethodName.substring(hashIndex + 1);
            }

            MetricCategory category = (MetricCategory)methodMap.get(methodName);
            if(category == null) {
                category = NONE;
            }

            return category;
        }
    }

    static {
        methodMap = new HashMap() {
            {
                this.put("onCreate", MetricCategory.VIEW_LOADING);
            }
        };
    }
}
