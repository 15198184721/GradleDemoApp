package com.mars.united.international.gradles.plugins.methodmonitor.configs

import com.mars.united.international.gradles.bases.BaseExtensionConfig

/**
 * 方法检测的配置参数(Gradle配置)
 * 对应gradle配置名称：MethodMonitorConfig
 */
open class MethodMonitorConfig : BaseExtensionConfig() {

    /**
     * 是否启用统计，默认启用
     *  T:启用，F:禁用
     */
    var enableMonitor:Boolean = true

    /**
     * 跳过构造方法
     *  T:跳过构造方法,不统计  F:不忽略构造方法
     */
    var jumpOverConstructionMethod: Boolean = true

    /**
     * 跳过类加载方法
     *  T:跳过class加载 不统计  F:不跳过,需要统计
     */
    var jumpOverClinitMethod: Boolean = true

    /**
     * 是否只检查主线程
     *  T:只检查主线程，F:所有线程
     */
    var onlyAllowedMainThread: Boolean = true

    /**
     * 开启筛选模式，因为每个方法都有统计。所以会存在一个调用链只要一个超时那么这个调用链上的所有
     * 方法都会输出一遍日志。导致日志杂乱，所以可以开启筛选模式。一个调用周期内只输出一次日志
     * 注：
     *  此功能是实验性性质的。暂不确定能否保证100%成功。可以配合使用。日志太多可以开启。日志少时候建议放开保证准确性
     * 参数值：
     *  T:开启调用链筛选(一个调用链只输出一次日志)
     *  F:不筛选,全部输出
     */
    var logFilterMode: Boolean = false

    /**
     * 是否需要输出调用栈
     *  T:输出，F:不输出
     */
    var logPrintCallStack = false

    /**
     * 输出的日志的Tag
     */
    var logTag: String = "monitor"

    /**
     * 简略模式输出日志
     */
    var logAbbreviatedMode:Boolean = false

    /**
     * 白名单(支持包、类、方法级别)
     * 支持包：a.b.c ->表示这个包下所有class都不处理
     * 支持class：a.b.c.D ->表示D.class 所有方法都不统计
     * 支持方法：a.b.c.D#A ->表示D.class的A方法全部忽略(包括重载方法)
     */
    var logWhiteList:Array<String> = arrayOf()

    /**
     * 需要处理的包名,只有包含再此包下面的才会被处理(只支持包级别)
     */
    var processPackages: Array<String> = arrayOf()

    /**
     * 方法鉴定为卡顿阈值(或者说耗时太长的阈值),单位:毫秒
     */
    var thresholdTime: Long = 350

    override fun toString(): String {
        return StringBuilder()
            .append("\tenableMonitor=$enableMonitor \n")
            .append("\tjumpOverConstructionMethod=$jumpOverConstructionMethod \n")
            .append("\tjumpOverClinitMethod=$jumpOverClinitMethod \n")
            .append("\tonlyAllowedThread=$onlyAllowedMainThread \n")
            .append("\tlogPrintCallStack=$logPrintCallStack \n")
            .append("\tlogTag=$logTag \n")
            .append("\tlogAbbreviatedMode=$logAbbreviatedMode \n")
            .append("\tlogWhiteList=$logWhiteList \n")
            .append("\tprocessPackages=$processPackages \n")
            .append("\tthresholdTime=$thresholdTime \n")
            .toString()
    }
}