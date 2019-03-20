package com.qunar.gradle.plugin.transform

public interface TaskBuild {

    class JarTaskBuildConfig implements TaskBuild {
        public String outputJar
        public String tempJar

        public JarTaskBuildConfig(String tempJar, String outputJar) {
            this.tempJar = tempJar
            this.outputJar = outputJar
        }
    }

    class DirListTaskBuildConfig implements TaskBuild {
        public Set<String> newBinaryFiles
        public String outputDir

        public DirListTaskBuildConfig(Set<String> newBinaryFiles, String outputDir) {
            this.newBinaryFiles = newBinaryFiles
            this.outputDir = outputDir
        }
    }

}
