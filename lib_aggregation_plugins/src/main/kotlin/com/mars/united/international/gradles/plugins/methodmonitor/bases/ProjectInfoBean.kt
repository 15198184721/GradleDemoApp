package com.mars.united.international.gradles.plugins.methodmonitor.bases

import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

/**
 * 保存项目基础信息
 */
class ProjectInfoBean {
    /** 编译模式:可能有多重类型 */
    val buildType: MutableList<String> = mutableListOf()

    /** applicationId,也就是项目主的包名 */
    var applicationId: String = ""

    /** 生成的Java文件路径 */
    lateinit var generatendJavaSourcePath: String

    /** 生成的Java文件的任务名称，此名称对应位置：generated/java/<此名称> */
    var generatendTaskName: String = "methodMonitorGenerated"

    /** 生成的Java源码包路径 */
    var generatendJavaSourcePackage: String =
        "com.mars.united.international.gradles.plugins.methodmonitor"


}