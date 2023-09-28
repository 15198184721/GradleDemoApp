package com.mars.united.international.gradles.plugins.methodmonitor.generates

import com.mars.united.international.gradles.bases.generates.BaseGenerateSourceCode
import com.mars.united.international.gradles.plugins.methodmonitor.helper.MethodMonitorConfigHelper
import java.io.File
import java.nio.charset.Charset


/**
 * 生成耗时计算相关工具
 */
class GenerateConsumingCalculationFile(
    filePackage: String
) : BaseGenerateSourceCode(filePackage) {

    private val javaFileName = "MethodMonitorUtils.java"

    override fun getGenerateSourceFileSaveDir(): String {
        return MethodMonitorConfigHelper.projectInfo.generatendJavaSourcePath
    }

    override fun generateCode() {
        val outputFile = File(getSourceFilePackageSaveDir(), javaFileName)
        if (outputFile.exists()) {
            return
        }
        outputFile.parentFile.mkdirs()
        outputFile.writeText(getGenerateFile())
    }

    // 直接获取生成资源文件
    private fun getGenerateFile(): String {
        val url = javaClass.classLoader.getResource("generateds/$javaFileName")
        val ins = url.openStream();
        val b = ins.reader(Charset.forName("UTF-8"))
        return b.readText().let { oldCode ->
            // 处理动态参数
            oldCode
                .replace(
                    "\$package",
                    "${MethodMonitorConfigHelper.projectInfo.generatendJavaSourcePackage}"
                )
                .replace(
                    "\$isOnlyAllowedMainThread",
                    "${MethodMonitorConfigHelper.methodMonitorConfig.onlyAllowedMainThread}"
                )
                .replace(
                    "\$logPrintCallStack",
                    "${MethodMonitorConfigHelper.methodMonitorConfig.logPrintCallStack}"
                )
                .replace(
                    "\$logTag",
                    "\"${MethodMonitorConfigHelper.methodMonitorConfig.logTag}\""
                )
                .replace(
                    "\$logAbbreviatedMode",
                    "${MethodMonitorConfigHelper.methodMonitorConfig.logAbbreviatedMode}"
                )
                .replace(
                    "\$logFilterMode",
                    "${MethodMonitorConfigHelper.methodMonitorConfig.logFilterMode}"
                )
                .replace(
                    "\$methodThresholdTime",
                    "${MethodMonitorConfigHelper.methodMonitorConfig.thresholdTime}"
                )

        }
    }
}