package com.mars.united.international.gradles.bases

import com.android.build.gradle.AppExtension
import com.mars.united.international.gradles.utils.LogUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 基础插件
 */
abstract class BasePlugin<T : Project> : Plugin<T> {

    private lateinit var project: Project

    /**
     * 插件应用
     */
    abstract fun pluginApply(project: T)

    /**
     * 获取gradle的配置参数
     */
    protected fun <T : BaseExtensionConfig> getGradleConfig(gradleConfigCzz: Class<T>): T? {
        return project.extensions.getByType(
            gradleConfigCzz
        )
    }

    /**
     * 创建配置，此方法需要再获取配置[getGradleConfig]之前执行
     */
    protected fun <T : BaseExtensionConfig> createGradleConfig(
        gradleConfigName: String,
        gradleConfigCzz: Class<T>
    ) {
        project.extensions.create(gradleConfigName, gradleConfigCzz)
    }

    final override fun apply(project: T) {
        this.project = project
        LogUtil.logI("${this.javaClass.simpleName} 插件已应用........")
        pluginApply(project)
        LogUtil.logI("${this.javaClass.simpleName} 初始化应用完成...")
    }
}