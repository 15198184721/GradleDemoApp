package com.mars.united.international.gradles.plugins.methodmonitor

import com.android.build.api.variant.Variant
import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.ApplicationVariant
import com.android.tools.build.bundletool.validation.AppBundleValidator
import com.mars.united.international.gradles.bases.BasePlugin
import com.mars.united.international.gradles.plugins.methodmonitor.configs.MethodMonitorConfig
import com.mars.united.international.gradles.plugins.methodmonitor.factory.MethodMonitorTransform
import com.mars.united.international.gradles.plugins.methodmonitor.generates.GenerateConsumingCalculationFile
import com.mars.united.international.gradles.plugins.methodmonitor.helper.MethodMonitorConfigHelper
import com.mars.united.international.gradles.plugins.methodmonitor.tasks.AddJavaSourcesGenerateTask
import com.mars.united.international.gradles.utils.LogUtil
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import java.io.File

/**
 * 方法监控插件
 * 引用方式：classpath("com.mars.united.international.plugins:aggregation:${plugin_version}")
 * 应用方式：id 'plugin.method.monitor'
 *
 * 原理：通过编译期间对所有的方法进行插入统计代码。统计方法运行时长，超过统计时长的方法将被记录
 */
class MethodMonitorPlugin : BasePlugin<Project>() {
    override fun pluginApply(project: Project) {
        // 参数构建关联
        buildParams()
        // 编译变体构建
        buildGeneratedRelatedVariant()
        // 通用变体构建
        androidComponentsExtensionVariantBuild()
    }

    // 参数构建
    private fun buildParams() {
        // 创建参数
        createGradleConfig("MethodMonitorConfig", MethodMonitorConfig::class.java)
    }

    /**
     * 生成代码相关的变体构建
     * 1、配置源码文件
     * 2、配置输出目录为源目录。否则不会参与编译
     */
    private fun buildGeneratedRelatedVariant() {
        //这里appExtension获取方式与原transform api不同，可自行对比
        val android = project.extensions.getByType(
            AppExtension::class.java
        )
        android.applicationVariants.all { variant ->
            if (!MethodMonitorConfigHelper.methodMonitorConfig.isEnableMonitor) {
                LogUtil.logI("MethodMonitorPlugin 统计功能已禁用!!")
                return@all
            }
            if (!isDebugType(variant)) {
                // 非debug编译。放弃处理
                LogUtil.logI("MethodMonitorPlugin 非debug模式。放弃处理")
                return@all
            }
            // 构建项目基本参数
            MethodMonitorConfigHelper.projectInfo.apply {
                this.buildType.add(variant.name)
                this.applicationId = variant.applicationId
            }
            variant.addJavaSourceFoldersToModel(
                project.layout.buildDirectory.dir("generated").get().asFile
            )
            if (project.tasks.findByName(MethodMonitorConfigHelper.projectInfo.generatendTaskName) != null) {
                // 任务已经存在。放弃添加
                return@all
            }
            // 执行生成代码的任务(因为不区分环境。所以名称固定)
            val regTask = project.tasks.register(
                MethodMonitorConfigHelper.projectInfo.generatendTaskName,
                AddJavaSourcesGenerateTask::class.java
            ) {
                it.outputFolder.set(project.layout.buildDirectory.dir("generated"))
                MethodMonitorConfigHelper.projectInfo.generatendJavaSourcePath =
                    it.outputFolder.asFile.get().absolutePath
                // 方法耗时计算代码生成
                GenerateConsumingCalculationFile(it.generatePackage).generateCode()
            }
            regTask.get().doLast {
                LogUtil.logI("MethodMonitorPlugin 生成代码任务执行完成了")
            }
        }
    }

    /**
     * ASM 统计代码插入
     */
    private fun androidComponentsExtensionVariantBuild() {
        getGradleConfig(MethodMonitorConfig::class.java)?.apply {
            MethodMonitorConfigHelper.methodMonitorConfig = this
            LogUtil.logI("MethodMonitorPlugin 方法监控配置已生效！！--->${this}")
        }
        // 旧版版的变换器注册
        val android = project.extensions.getByType(AppExtension::class.java)
        val transform = MethodMonitorTransform(project)
        android.applicationVariants.all { variant ->
            if(isDebugType(variant)){
                return@all
            }
            if(!android.transforms.contains(transform)){
                android.registerTransform(MethodMonitorTransform(project))
            }
        }
    }

    // 是否为debug模式
    private fun isDebugType(variant: ApplicationVariant): Boolean {
        return variant.buildType.isDebuggable
    }
}