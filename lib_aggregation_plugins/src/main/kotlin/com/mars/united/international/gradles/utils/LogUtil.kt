package com.mars.united.international.gradles.utils

import org.gradle.api.Project

/**
 * 日志输出工具
 */
class LogUtil(
    private val project: Project
) {

    companion object{
        /**
         * 输出普通日志
         */
        fun logI(msg: String, any: Any? = null) {
            if (any == null) {
                println(msg)
                return
            }
            println("${msg}:${any}")
        }
    }

    /**
     * 输出错误日志
     */
    fun logE(msg: String, any: Any? = null) {
        if (any == null) {
            project.logger.error(msg)
            return
        }
        project.logger.error(msg, any)
    }

    /**
     * 输出错误日志
     */
    fun logTrace(msg: String, any: Any? = null) {
        if (any == null) {
            project.logger.trace(msg)
            return
        }
        project.logger.trace(msg, any)
    }
}