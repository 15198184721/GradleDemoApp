package com.mars.united.international.gradles.plugins.methodmonitor.tasks

import com.mars.united.international.gradles.plugins.methodmonitor.generates.GenerateConsumingCalculationFile
import com.mars.united.international.gradles.plugins.methodmonitor.helper.MethodMonitorConfigHelper
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * 生成Java源代码的Task，主要负责生成当前插件的源文件代码
 */
abstract class AddJavaSourcesGenerateTask : DefaultTask() {
    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    // 包路径
    private val generatePackage: String by lazy {
        MethodMonitorConfigHelper.projectInfo.generatendJavaSourcePackage
    }

    @TaskAction
    fun taskAction() {
        MethodMonitorConfigHelper.projectInfo.generatendJavaSourcePath =
            outputFolder.asFile.get().absolutePath
        // 方法耗时计算代码生成
        GenerateConsumingCalculationFile(generatePackage).generateCode()
    }
}