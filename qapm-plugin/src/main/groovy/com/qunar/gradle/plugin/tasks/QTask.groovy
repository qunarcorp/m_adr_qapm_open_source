package com.qunar.gradle.plugin.tasks

import org.gradle.api.Project


interface QTask {
    /**
     * 任务之前的处理
     * @param project：当前构建的project对象
     * @param task：一般指具体的Task
     */
    void doFisrt(Project project, def task)
    /**
     * 任务之后的处理
     * @param project：当前构建的project对象
     * @param task：一般指具体的Task
     */
    void doLast(Project project, def task)

}