package com.mars.united.international.gradles.plugins.methodmonitor.configs

import com.mars.united.international.gradles.bases.BaseExtensionConfig

/**
 * 方法检测的配置参数(Gradle配置)
 * 对应gradle配置名称：MethodMonitorConfig
 */
open class MethodMonitorConfig : BaseExtensionConfig() {
    /**
     * 输出的日志的Tag
     */
    var logTag: String = "monitor"

    /**
     * 需要处理的包名,只有包含再此包下面的才会被处理
     */
    var processPackages: Array<String> = arrayOf()

    /**
     * 方法鉴定为卡顿阈值(或者说耗时太长的阈值),单位:毫秒
     */
    var thresholdTime: Long = 350

    override fun toString(): String {
        return StringBuilder()
            .append("logTag=$logTag \n")
            .append("processPackages=$processPackages \n")
            .append("thresholdTime=$thresholdTime \n")
            .toString()
    }
}