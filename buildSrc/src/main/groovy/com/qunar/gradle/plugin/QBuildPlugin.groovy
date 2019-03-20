package com.qunar.gradle.plugin

import com.android.build.gradle.AppExtension
import com.qunar.gradle.plugin.extensions.QExtension
import com.qunar.gradle.plugin.transform.AspectJAppTransform

import com.qunar.gradle.plugin.utils.QBuildLogger
import org.gradle.api.Plugin
import org.gradle.api.Project


class QBuildPlugin implements Plugin<Project> {


    @Override
    void apply(Project project) {
        QBuildLogger.init(project)
        QBuildLogger.log('~~~~~~~~~~~ QBuildPlugin Start ~~~~~~~~~~~')
        QBuildLogger.log("projet.properties=${project.properties.toMapString().join("\n")}")
        try {
            project.extensions.create("qExtension", QExtension)
        } catch (Exception e) {
            project.qExtension = QExtension.newInstance()
            QBuildLogger.warnning(e.message)
        }

        AppExtension android = project.extensions.getByType(AppExtension.class)
        android?.registerTransform(new AspectJAppTransform(project))



        QBuildLogger.log('~~~~~~~~~~~ QBuildPlugin End ~~~~~~~~~~~')
    }


}
