package com.qunar.gradle.plugin.transform

import java.util.concurrent.Callable

/**
 * 预处理字节码
 */
public class AspectJPrepareWork {
    List<TaskBuild> taskBuildList = new ArrayList<>()
    List<String> outputAspectJ = new ArrayList<>()

    public AspectJPrepareWork addTask(TaskBuild taskBuild) {
        taskBuildList.add(taskBuild)
        return this
    }

    public void execute() {
        ThreadPool threadPool = new ThreadPool()
        taskBuildList.each { item ->
            threadPool.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    if (item instanceof TaskBuild.JarTaskBuildConfig) {
                        outputAspectJ.addAll(new AspectJPrepareBuilder.AspectJJarPrepareProcessBuilder()
                                .tempJar(((TaskBuild.JarTaskBuildConfig) item).tempJar)
                                .targetJar(((TaskBuild.JarTaskBuildConfig) item).outputJar)
                                .build())

                    } else if (item instanceof TaskBuild.DirListTaskBuildConfig) {
                        outputAspectJ.addAll(new AspectJPrepareBuilder.AspectJDirPrepareProcessBuilder()
                                .outDir(((TaskBuild.DirListTaskBuildConfig) item).outputDir)
                                .newBinaryFiles(((TaskBuild.DirListTaskBuildConfig) item).newBinaryFiles)
                                .build())
                    }
                    return null
                }
            })
        }
        threadPool.invokeAll()
    }

}
