package com.qunar.gradle.plugin.transform

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

public interface AspectJPrepareBuilder {

    /**
     * 目录处理
     */
    class AspectJDirPrepareProcessBuilder {
        public File outDir
        public Set<String> newBinaryFiles
        private List<String> outputList = new ArrayList()

        public AspectJDirPrepareProcessBuilder outDir(String outDir) {
            this.outDir = new File(outDir)
            return this
        }

        public AspectJDirPrepareProcessBuilder newBinaryFiles(Set<String> newBinaryFiles) {
            this.newBinaryFiles = newBinaryFiles
            return this
        }

        /**
         * 处理字节码入口
         * @return 已处理列表
         */
        public List<String> build() {
            if (newBinaryFiles != null) {
                newBinaryFiles.each { binaryFile ->
                    processClassFile(new File(binaryFile))
                }
            } else {
                processBuild(outDir)
            }
            return outputList
        }

        public void processBuild(File file) {
            if (file.directory) {
                file.eachFile { item ->
                    processBuild(item)
                }
            } else {
                processClassFile(file)
            }

        }

        /**
         * 处理class字节
         * @param file
         * @param outDir
         * @param srcDir
         */
        private void processClassFile(File classFile) {
            if (classFile.name.endsWith(".class")) {
                try {
//                    QBuildLogger.log "filterAspectJ from processClassFile file $classFile.path"
                    AspectJHelper.filterAspectJ(FileUtils.readFileToByteArray(classFile), classFile, outputList)
                } catch (IOException e) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * jar字节码处理
     */
    class AspectJJarPrepareProcessBuilder {
        private File tempJar
        private File targetJar
        private List<String> outputList = new ArrayList()

        public AspectJJarPrepareProcessBuilder tempJar(String tempJar) {
            this.tempJar = new File(tempJar)
            return this
        }

        public AspectJJarPrepareProcessBuilder targetJar(String targetJar) {
            this.targetJar = new File(targetJar)
            return this
        }

        /**
         * 处理jar字节码
         * @return
         */
        public List<String> build() {
            try {
                processBuild(tempJar, targetJar)
            } catch (IOException e) {
                e.printStackTrace()
            }
            return outputList
        }

        private static void processBuild(File tempJar, File targetJar) throws IOException {
            try {
                JarFile zis = new JarFile(tempJar)
                JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(targetJar))
                Enumeration enumeration = zis.entries()
                while (enumeration.hasMoreElements()) {
                    JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                    InputStream inputStream = zis.getInputStream(jarEntry)
                    String entryName = jarEntry.name
                    ZipEntry zipEntry = new ZipEntry(entryName)
                    jarOutputStream.putNextEntry(zipEntry)

                    byte[] modifiedClassBytes = null
                    byte[] sourceClassBytes = IOUtils.toByteArray(inputStream)
                    if (entryName.endsWith(".class")) {
                        modifiedClassBytes = AspectJHelper.filterAspectJ(sourceClassBytes)
                    }
                    if (modifiedClassBytes == null) {
                        jarOutputStream.write(sourceClassBytes)
                    } else {
                        jarOutputStream.write(modifiedClassBytes)
                    }
                    jarOutputStream.closeEntry()
                }
                jarOutputStream.close()
                zis.close()
            } catch (Exception e) {
                e.printStackTrace()
            }

        }

    }
}
