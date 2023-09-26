package com.mars.united.international.gradles.plugins.methodmonitor.helper

import com.mars.united.international.gradles.plugins.methodmonitor.bases.ProjectInfoBean
import com.mars.united.international.gradles.plugins.methodmonitor.configs.MethodMonitorConfig

/**
 * 参数辅助类
 */
object MethodMonitorConfigHelper {
    /** 方法监控参数配置 */
    var methodMonitorConfig:MethodMonitorConfig = MethodMonitorConfig()

    /** 保存项目相关基础信息 */
    val projectInfo:ProjectInfoBean = ProjectInfoBean()
}