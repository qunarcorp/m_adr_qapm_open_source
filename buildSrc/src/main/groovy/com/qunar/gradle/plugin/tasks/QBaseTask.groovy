package com.qunar.gradle.plugin.tasks

import com.qunar.gradle.plugin.utils.QBuildLogger
import org.gradle.api.Project

public class QBaseTask implements QTask{

    public QBaseTask(){
    }

    @Override
    public void doFisrt(Project project, def task) {
        QBuildLogger.log(task.class.toString()+"_doFisrt() runing")
    }

    @Override
    public void doLast(Project project, def task) {
        QBuildLogger.log(task.class.toString()+"_doLast() runing")
    }

    public static void clear() {
        //nothing to do
    }
}
