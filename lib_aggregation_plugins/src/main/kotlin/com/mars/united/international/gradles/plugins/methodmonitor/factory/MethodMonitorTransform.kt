package com.mars.united.international.gradles.plugins.methodmonitor.factory

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.mars.united.international.gradles.plugins.methodmonitor.classvistors.MethodTimeMonitorClassVisitor
import com.mars.united.international.gradles.plugins.methodmonitor.classvistors.MethodTimeMonitorOldClassVisitor
import com.mars.united.international.gradles.plugins.methodmonitor.helper.MethodMonitorConfigHelper
import com.mars.united.international.gradles.utils.LogUtil
import groovyjarjarasm.asm.ClassReader.EXPAND_FRAMES
import org.gradle.api.Project
import java.io.File
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * 方法监控的变换器
 * 参考：https://blog.csdn.net/intbird/article/details/107013895
 */
class MethodMonitorTransform(
    /** 项目对象 */
    private val project: Project
) : Transform() {

    override fun getName(): String {
        return this.javaClass.simpleName
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 是否支持增量编译
     * @return
     */
    override fun isIncremental(): Boolean {
        return false
    }

    override fun transform(
        context: Context?,
        inputs: MutableCollection<TransformInput>,
        referencedInputs: MutableCollection<TransformInput>?,
        outputProvider: TransformOutputProvider,
        isIncremental: Boolean
    ) {
        LogUtil.logI("[旧版] 开始处理。。。")
        val clearCache = !isIncremental
        // clean build cache
        if (clearCache) {
            outputProvider.deleteAll()
        }
        inputs.forEach {
            it.jarInputs.forEach { jarInput ->
                outputProvider.apply {

                }
                val dest = outputProvider.getContentLocation(
                    jarInput.file.absolutePath,
                    jarInput.contentTypes,
                    jarInput.scopes,
                    Format.JAR
                )
                transformJar(jarInput.file, dest)
            }
            it.directoryInputs.forEach { directoryInput ->
                val dest = outputProvider.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )

                transformDirectory(directoryInput.file, dest)
            }
        }
    }

    // 处理Jar
    private fun transformJar(srcFile: File, destFile: File) {
        FileUtils.copyFile(srcFile, destFile)
    }

    // 处理目录
    private fun transformDirectory(srcFile: File, destFile: File) {
        LogUtil.logI("[旧版] -----transformDirectory 1------\n$srcFile\n$destFile")
        if (destFile.exists()) {
            FileUtils.forceDelete(destFile)
        }
        FileUtils.forceMkdir(destFile)

        val srcDirPath = srcFile.absolutePath
        val destDirPath = destFile.absolutePath
        LogUtil.logI("[旧版] -----transformDirectory 2------\n$srcDirPath\n$destDirPath")
        val files = srcFile.listFiles()
        for (file in files) {
            val destFilePath = file.absolutePath.replace(srcDirPath, destDirPath)
            val destFile = File(destFilePath)
            LogUtil.logI("[旧版] -----transformDirectory 3------\n$destFile")
            if (file.isDirectory) {
                transformDirectory(file, destFile)
            } else if (file.isFile) {
                FileUtils.touch(destFile)
                transformFile(file, destFile)
            }
        }
    }

    private fun transformFile(input: File, dest: File) {
        LogUtil.logI("[旧版] -----transformSingleFile------\n$input\n$dest")
        val inputPath = input.absolutePath
        val outputPath = dest.absolutePath
        try {
            if (isInstrumentable(inputPath)) {
                val classReader = ClassReader(input.readBytes())
                val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)

                val classVisitor = MethodTimeMonitorOldClassVisitor(classWriter)
                classReader.accept(classVisitor, EXPAND_FRAMES)

                val fileOutputStream = FileOutputStream(outputPath)
                fileOutputStream.write(classWriter.toByteArray())
                fileOutputStream.close()
            } else {
                val fileOutputStream = FileOutputStream(outputPath)
                fileOutputStream.write(input.readBytes())
                fileOutputStream.close()
            }
        } catch (e: Exception) {
            LogUtil.logI(
                "[旧版] -----transformSingleFile Exception------\n$inputPath\n${e.printStackTrace()}"
            )
        }
    }

    // 判断是否需要拦截
    private fun isInstrumentable(className: String): Boolean {
        for (processPackage in MethodMonitorConfigHelper.methodMonitorConfig.processPackages) {
            if (className.contains(processPackage.replace(".", File.separator))) {
                LogUtil.logI("需要处理的类：${className}")
                return true
            }
        }
        return false
    }
}