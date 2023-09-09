package com.mars.united.international.gradles.plugins.test

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.mars.united.international.gradles.bases.BasePlugin
import com.mars.united.international.gradles.plugins.methodmonitor.helper.MethodMonitorConfigHelper
import com.mars.united.international.gradles.plugins.methodmonitor.tasks.AddJavaSourcesGenerateTask
import com.mars.united.international.gradles.utils.LogUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 测试插件，用于测试插件demo的连通性
 * 引用方式：classpath("com.mars.united.international.plugins:aggregation:${plugin_version}")
 * 应用方式：id 'plugin.test'
 */
class TestPlugin : BasePlugin<Project>() {
    override fun pluginApply(project: Project) {
        LogUtil.logI("TestPlugin 插件应用成功。。。。。。。")
        testTask()
    }

    /**
     * 生成代码相关的变体构建
     * 1、配置源码文件
     * 2、配置输出目录为源目录。否则不会参与编译
     */
    private fun testTask() {
        //这里appExtension获取方式与原transform api不同，可自行对比
        val appVariant = project.extensions.getByType(
            ApplicationAndroidComponentsExtension::class.java
        )
        appVariant.onVariants { variant ->
            LogUtil.logI("TestPlugin 插件应用成功。。。。。。。")
        }
    }
}