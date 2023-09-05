package com.mars.united.international.gradles.bases

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
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
    protected fun <T:BaseExtensionConfig> getGradleConfig(gradleConfigCzz: Class<T>): T? {
        return project.extensions.getByType(
            gradleConfigCzz
        )
    }

    /**
     * 创建配置，此方法需要再获取配置[getGradleConfig]之前执行
     */
    protected fun <T:BaseExtensionConfig> createGradleConfig(gradleConfigName: String, gradleConfigCzz: Class<T>) {
        project.extensions.create(gradleConfigName,gradleConfigCzz)
    }

    /**
     * 默认方式注册
     * 提供一个默认的注册方式。只需要将提供者返回即可，null:表示自行处理
     */
    protected fun <C : AsmClassVisitorFactory<out InstrumentationParameters>> defaultRegister(): Class<C>? {
        return null
    }

    final override fun apply(project: T) {
        this.project = project
        LogUtil.logI("${this.javaClass.simpleName} 插件已应用........")
        val factoryClazz: Class<AsmClassVisitorFactory<InstrumentationParameters>>? =
            defaultRegister()
        factoryClazz?.apply {
            //这里appExtension获取方式与原transform api不同，可自行对比
            val appExtension = project.extensions.getByType(
                AndroidComponentsExtension::class.java
            )
            //这里通过transformClassesWith替换了原registerTransform来注册字节码转换操作
            appExtension.onVariants { variant ->
                //可以通过variant来获取当前编译环境的一些信息，最重要的是可以 variant.name 来区分是debug模式还是release模式编译
                variant.instrumentation.transformClassesWith(
                    this,
                    InstrumentationScope.ALL
                ) {
                }
                //InstrumentationScope.ALL 配合 FramesComputationMode.COPY_FRAMES可以指定该字节码转换器在全局生效，包括第三方lib
                variant.instrumentation.setAsmFramesComputationMode(
                    FramesComputationMode.COPY_FRAMES
                )
            }
        } ?: pluginApply(project)
        LogUtil.logI("${this.javaClass.simpleName} 初始化应用完成...")
    }
}