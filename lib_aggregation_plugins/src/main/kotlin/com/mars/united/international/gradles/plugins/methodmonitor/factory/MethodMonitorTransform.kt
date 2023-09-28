package com.mars.united.international.gradles.plugins.methodmonitor.factory

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.mars.united.international.gradles.plugins.methodmonitor.classvistors.MethodTimeMonitorOldClassVisitor
import com.mars.united.international.gradles.plugins.methodmonitor.helper.MethodMonitorConfigHelper
import com.mars.united.international.gradles.utils.LogUtil
import groovyjarjarasm.asm.ClassReader.EXPAND_FRAMES
import org.gradle.api.Project
import java.io.File
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * 方法监控的变换器
 * 参考：https://blog.csdn.net/intbird/article/details/107013895
 */
class MethodMonitorTransform(
    /** 项目对象 */
    private val project: Project,
) : Transform() {

    private val android by lazy {
        project.extensions.getByType(
            AppExtension::class.java
        )
    }

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
        if(!MethodMonitorConfigHelper.methodMonitorConfig.enableMonitor){
            super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)
            return
        }
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

    // 处理Jar和第三方依赖的家暴
    private fun transformJar(srcFile: File, destFile: File) {
        try {
            val newFileName = "copy_method_monitor_${destFile.name}"
            val newTemFile = File(destFile.parent, newFileName)
            val jos = JarOutputStream(FileOutputStream(newTemFile))

            val jarFile = JarFile(srcFile)
            val entries = jarFile.entries()

            var isReBuild = false  // 是否重构了
            while (entries.hasMoreElements()) {
                val entry: JarEntry = entries.nextElement()
                //处理
                if (!MethodMonitorConfigHelper.methodMonitorConfig.enableMonitor) {
                    break
                }
                val bytes = jarFile.getInputStream(entry).readAllBytes()
                jos.putNextEntry(JarEntry(entry.name))
                if (!entry.name.endsWith(".class")) {
                    jos.write(bytes)
                    continue
                }
                if (!isInstrumentable(entry.name)) {
                    jos.write(bytes)
                    continue
                }
                isReBuild = true
                jos.write(
                    buildNewJarEntry(bytes)
                )
            }
            jos.flush()
            jos.close()
            if (isReBuild &&
                MethodMonitorConfigHelper.methodMonitorConfig.enableMonitor
            ) {
                FileUtils.copyFile(newTemFile, destFile)
            } else {
                FileUtils.copyFile(srcFile, destFile)
            }
            newTemFile.delete()
        } catch (e: Exception) {
            FileUtils.copyFile(srcFile, destFile)
        }
    }

    // 构建Jar实体
    private fun buildNewJarEntry(
        srcBytes: ByteArray
    ): ByteArray {
        try {
            val classReader = ClassReader(srcBytes)
            val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)

            val classVisitor = MethodTimeMonitorOldClassVisitor(
                classReader.className
                    .replace(File.separator, ".")
                    .replace("/", ".")
                    .split("$")[0],
                classWriter
            )
            classReader.accept(classVisitor, EXPAND_FRAMES)
            return classWriter.toByteArray()
        } catch (e: Exception) {
            return srcBytes
        }
    }

    // 处理目录
    private fun transformDirectory(srcFile: File, destFile: File) {
        if (destFile.exists()) {
            FileUtils.forceDelete(destFile)
        }
        FileUtils.forceMkdir(destFile)

        val srcDirPath = srcFile.absolutePath
        val destDirPath = destFile.absolutePath
        val files = srcFile.listFiles()
        for (file in files) {
            val destFilePath = file.absolutePath.replace(srcDirPath, destDirPath)
            val destFile = File(destFilePath)
            if (file.isDirectory) {
                transformDirectory(file, destFile)
            } else if (file.isFile) {
                FileUtils.touch(destFile)
                transformFile(file, destFile)
            }
        }
    }

    private fun transformFile(input: File, dest: File) {
        val inputPath = input.absolutePath
        val outputPath = dest.absolutePath
        try {
            if (!MethodMonitorConfigHelper.methodMonitorConfig.enableMonitor) {
                val fileOutputStream = FileOutputStream(outputPath)
                fileOutputStream.write(input.readBytes())
                fileOutputStream.close()
                return
            }
            if (isInstrumentable(inputPath)) {
                val classReader = ClassReader(input.readBytes())
                val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)

                val classVisitor = MethodTimeMonitorOldClassVisitor(
                    classReader.className
                        .replace(File.separator, ".")
                        .replace("/", ".")
                        .split("$")[0],
                    classWriter
                )
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
        val flg = if (className.contains(File.separator)) {
            File.separator
        } else {
            "/"
        }
        for (processPackage in MethodMonitorConfigHelper.methodMonitorConfig.processPackages) {
            if (className.contains(processPackage.replace(".", flg))) {
                LogUtil.logI("需要处理的类：${className}")
                return true
            }
        }
        return false
    }
}