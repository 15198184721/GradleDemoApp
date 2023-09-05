package com.mars.united.international.gradles.plugins.methodmonitor

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.mars.united.international.gradles.bases.BasePlugin
import com.mars.united.international.gradles.plugins.methodmonitor.configs.MethodMonitorConfig
import com.mars.united.international.gradles.plugins.methodmonitor.factory.MethodMonitorClassVisitorFactory
import com.mars.united.international.gradles.plugins.methodmonitor.helper.MethodMonitorConfigHelper
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
        // 创建参数
        createGradleConfig("MethodMonitorConfig", MethodMonitorConfig::class.java)
        getGradleConfig(MethodMonitorConfig::class.java)?.apply {
            MethodMonitorConfigHelper.methodMonitorConfig = this
            LogUtil.logI("MethodMonitorPlugin 方法监控配置已生效！！--->${this}")
        }
        //这里appExtension获取方式与原transform api不同，可自行对比
        val appExtension = project.extensions.getByType(
            AndroidComponentsExtension::class.java
        )
        //这里通过transformClassesWith替换了原registerTransform来注册字节码转换操作
        appExtension.onVariants { variant ->
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
}