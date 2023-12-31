package com.mars.united.international.gradles.plugins.methodmonitor

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.mars.united.international.gradles.bases.BasePlugin
import com.mars.united.international.gradles.plugins.methodmonitor.configs.MethodMonitorConfig
import com.mars.united.international.gradles.plugins.methodmonitor.factory.MethodMonitorClassVisitorFactory
import com.mars.united.international.gradles.plugins.methodmonitor.helper.MethodMonitorConfigHelper
import com.mars.united.international.gradles.plugins.methodmonitor.tasks.AddJavaSourcesGenerateTask
import com.mars.united.international.gradles.utils.LogUtil
import org.gradle.api.Project

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
        val appVariant = project.extensions.getByType(
            ApplicationAndroidComponentsExtension::class.java
        )
        appVariant.onVariants { variant ->
            if (!MethodMonitorConfigHelper.methodMonitorConfig.isEnableMonitor) {
                LogUtil.logI("MethodMonitorPlugin 统计功能已禁用!!")
                return@onVariants
            }
            if (!isDebugType(variant)) {
                // 非debug编译。放弃处理
                LogUtil.logI("MethodMonitorPlugin 非debug模式。放弃处理")
                return@onVariants
            }
            LogUtil.logI("MethodMonitorPlugin 方法监控配置已生效！！--->\n${MethodMonitorConfigHelper.methodMonitorConfig}")
            // 构建项目基本参数
            MethodMonitorConfigHelper.projectInfo.apply {
                this.buildType.add(variant.name)
                this.applicationId = variant.applicationId.get()
            }

            if (project.tasks.findByName(MethodMonitorConfigHelper.projectInfo.generatendTaskName) != null) {
                // 任务已经存在。放弃添加
                return@onVariants
            }
            // 执行生成代码的任务(因为不区分环境。所以名称固定)
            val addSourceTaskProvider = project.tasks.register(
                MethodMonitorConfigHelper.projectInfo.generatendTaskName,
                AddJavaSourcesGenerateTask::class.java
            ) {
                it.outputFolder.set(project.layout.buildDirectory.dir("generated"))
            }
            // 添加源码路径
            variant.sources.java?.let { java ->
                // 添加源码目录:addSourceTaskProvider 所设置的
                java.addGeneratedSourceDirectory(
                    addSourceTaskProvider,
                    AddJavaSourcesGenerateTask::outputFolder
                )
            }
        }
    }

    /**
     * ASM 统计代码插入
     */
    private fun androidComponentsExtensionVariantBuild() {
        getGradleConfig(MethodMonitorConfig::class.java)?.apply {
            MethodMonitorConfigHelper.methodMonitorConfig = this
        }
        //这里appExtension获取方式与原transform api不同，可自行对比
        val appExtension = project.extensions.getByType(
            AndroidComponentsExtension::class.java
        )
        //这里通过transformClassesWith替换了原registerTransform来注册字节码转换操作
        appExtension.onVariants { variant ->
            if (!MethodMonitorConfigHelper.methodMonitorConfig.isEnableMonitor) {
                LogUtil.logI("MethodMonitorPlugin 统计功能已禁用!!")
                return@onVariants
            }
            if (!isDebugType(variant)) {
                // 非debug编译。放弃处理
                LogUtil.logI("MethodMonitorPlugin 非debug模式。放弃处理")
                return@onVariants
            }
            //可以通过variant来获取当前编译环境的一些信息，最重要的是可以 variant.name 来区分是debug模式还是release模式编译
            variant.instrumentation.transformClassesWith(
                MethodMonitorClassVisitorFactory::class.java,
                InstrumentationScope.ALL
            ) {
            }
            //InstrumentationScope.ALL 配合 FramesComputationMode.COPY_FRAMES可以指定该字节码转换器在全局生效，包括第三方lib
            variant.instrumentation.setAsmFramesComputationMode(
                FramesComputationMode.COPY_FRAMES
            )
        }
    }

    // 是否为debug模式
    private fun isDebugType(variant: Variant): Boolean {
        return variant.buildType == "debug"
    }
}