package com.mars.united.international.gradles.plugins.test

import com.mars.united.international.gradles.bases.BasePlugin
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
        LogUtil.logI("测试插件应用成功。。。。。。。")
    }
}