package com.qunar.gradle.plugin.transform

import com.qunar.gradle.plugin.inject.asm.qnecro.instrument.ClassRemapperConfig
import com.qunar.gradle.plugin.inject.asm.qnecro.instrument.InstrumentationContext
import com.qunar.gradle.plugin.inject.proxy.DexerMainProxy
import com.qunar.gradle.plugin.utils.QBuildLogger
import org.apache.commons.io.FileUtils

public class AspectJHelper {

    /**
     * 处理class字节码，并把处理完的放到一个列表中
     * @param bytes
     * @param targetFile
     * @param outputList
     */
    static void filterAspectJ(byte[] bytes, File targetFile, List outputList) {
        try {
            byte[] result = filterAspectJ(bytes)
            FileUtils.writeByteArrayToFile(targetFile, result)
            outputList.add(targetFile.path)
        } catch (IOException e) {
            QBuildLogger.log "filterAspectJ error $e.message"
            e.printStackTrace()
        }
    }

    static byte[] filterAspectJ(byte[] bytes){
        def result = DexerMainProxy.invoke(new InstrumentationContext(ClassRemapperConfig.instance), bytes)
        return result
    }


}
