package com.qunar.gradle.plugin.transform

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.qunar.gradle.plugin.exceptions.QBuildException

import com.qunar.gradle.plugin.inject.asm.qnecro.constants.NecroConstants
import com.qunar.gradle.plugin.utils.QBuildLogger
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import static com.android.build.api.transform.Status.*

public class AspectJAppTransform extends Transform {

    Project project
    AspectJPrepareWork aspectJPrepareWork

    AspectJAppTransform(Project project) {
        this.project = project
    }

    //transform的名称
    //transformClassesWithAspectjForDebug 运行时Context的名字
    //transformClassesWith + name + For + Debug或Release
    @Override
    String getName() {
        return "aspectj"
    }

    //需要处理的数据类型，有两种枚举类型
    //CLASSES和RESOURCES，CLASSES代表处理的java的class文件，RESOURCES代表要处理java的资源
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    //指Transform要操作内容的范围，官方文档Scope有7种类型：
    //EXTERNAL_LIBRARIES        只有外部库
    //PROJECT                       只有项目内容
    //PROJECT_LOCAL_DEPS            只有项目的本地依赖(本地jar)
    //PROVIDED_ONLY                 只提供本地或远程依赖项
    //SUB_PROJECTS              只有子项目。
    //SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
    //TESTED_CODE                   由当前变量(包括依赖项)测试的代码
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    //指明当前Transform是否支持增量编译
    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        transformInvocation.outputProvider.deleteAll()
        //:app:transformClassesWithAspectjForADebug
        long start = System.currentTimeMillis()
        NecroConstants.init(project.qExtension.qNecroConfig.hookQNecro)
        //预处理什么的？先把class处理到一个自定义的目录
        QBuildLogger.log "filterAspectJ~~~~~ aspectj transform,,,,start"
        aspectJPrepareWork = new AspectJPrepareWork()
        if (transformInvocation.incremental) {
            executeIncremental(transformInvocation)
        } else {
            executeAll(transformInvocation)
        }
        aspectJPrepareWork.execute()
        QBuildLogger.log "filterAspectJ~~~~~ aspectj transform,,,,end, useTime = ${System.currentTimeMillis() - start}"
    }


    public void executeAll(TransformInvocation transformInvocation) {
        transformInvocation.outputProvider.deleteAll()
        transformInvocation.inputs.each { transformInput ->
            transformInput.directoryInputs.each { directoryInput ->
                File des = transformInvocation.outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, des)//先把文件夹copy过去，然后处理单个文件
                aspectJPrepareWork.addTask(new TaskBuild.DirListTaskBuildConfig(null, des.path))
            }
            transformInput.jarInputs.each { jarInput ->
                def outName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8) + ".jar"//windows电脑无法使用jarInput.name，因为有特属符号
                File des = new File(transformInvocation.context.temporaryDir, outName)
                File targetFile = transformInvocation.outputProvider.getContentLocation(jarInput.name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, des)//先copy到temp.jar，然后处理temp.jar到targetFile
                aspectJPrepareWork.addTask(new TaskBuild.JarTaskBuildConfig(des.path, targetFile.absolutePath))
            }
        }
    }

    public void executeIncremental(TransformInvocation transformInvocation) {
        transformInvocation.inputs.each { transformInput ->
            transformInput.directoryInputs.each { directoryInput ->
                File dir = transformInvocation.outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                Set<String> newBinaryFiles = new HashSet<>()
                directoryInput.changedFiles.each { file ->
                    String targetPath = file.key.absolutePath.replaceFirst(directoryInput.file.absolutePath, dir.absolutePath)//相对路径
                    switch (file.value) {
                        case REMOVED:
                            FileUtils.forceDeleteOnExit(new File(targetPath))
                            break
                        case CHANGED:
//                                FileUtils.forceDeleteOnExit(new File(targetPath))
                        case ADDED:
                            FileUtils.copyFile(file.key, new File(targetPath))
                            newBinaryFiles.add(targetPath)
                            break
                        default:
                            break
                    }
                }
                if (newBinaryFiles.size() > 0) {
                    aspectJPrepareWork.addTask(
                            new TaskBuild.DirListTaskBuildConfig(newBinaryFiles, dir.path))
                }
            }

            transformInput.jarInputs?.each { jarInput ->
//                def outName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8) + "_" + jarInput.name
                File des = new File(transformInvocation.context.temporaryDir, jarInput.name)//中间产物路径
                File targetFile = transformInvocation.outputProvider.getContentLocation(jarInput.name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                switch (jarInput.status) {
                    case REMOVED:
                        FileUtils.forceDeleteOnExit(des)
                        FileUtils.forceDeleteOnExit(targetFile)
                        break
                    case CHANGED:
//                            FileUtils.forceDeleteOnExit(des)
//                            FileUtils.forceDeleteOnExit(targetFile)
                    case ADDED:
                        FileUtils.copyFile(jarInput.file, des)
                        aspectJPrepareWork.addTask(new TaskBuild.JarTaskBuildConfig(des.path, targetFile.absolutePath))
                        break
                    default:
                        break
                }
            }
        }
    }

}
